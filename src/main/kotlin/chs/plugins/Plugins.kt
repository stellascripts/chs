package chs.plugins

import chs.API
import java.io.File
import java.io.FilePermission
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Exception
import java.lang.NullPointerException
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import java.security.*
import java.util.jar.JarFile

/**
 * Operations for loading external plugins. Plugins must implement the [Plugin] interface and have an accessible
 * parameterless constructor for instantiation.
 */
@API
object Plugins {
    /**
     * Sets up the security parameters for plugins and attempts to load any plugins from
     * the plugins directory.
     * @param errors A handler for errors that may occur while loading plugins. Defaults to printing errors to
     * [System.err]
     */
    @API
    fun setup(errors: (String)->Unit = System.err::println) {
        this.error = errors
        Policy.setPolicy(PluginPolicy())
        System.setSecurityManager(SecurityManager())

        val pluginDirectory = File("plugins")
        pluginDirectory.walkTopDown().onEnter { file ->
            if(!file.isFile) true
            else {
                if(file.extension == ".jar") searchJar(file)
                false
            }
        }
    }

    /**
     * Called after the engine has started completely and plugins are able to load assets and manipulate
     * engine parameters.
     */
    @API
    fun startAllPlugins() {
        for(plugin in plugins) {
            plugin.start()
        }
    }

    private lateinit var error: (String)->Unit
    private val plugins = ArrayList<Plugin>()

    private fun hasCorrectClassModifiers(mod: Int): Boolean {
        return (mod and Modifier.PUBLIC != 0) &&
                (mod and (Modifier.STATIC or Modifier.ABSTRACT or Modifier.INTERFACE) == 0)
    }

    private fun hasCorrectConstructorModifiers(mod: Int): Boolean {
        return (mod and Modifier.PUBLIC != 0) &&
                (mod and Modifier.NATIVE == 0)
    }

    private fun searchJar(file: File) {
        val jar = JarFile(file)
        val classes = ArrayList<String>()
        for(entry in jar.versionedStream()) {
            if(entry.isDirectory) continue
            if(entry.name.endsWith(".class"))
            classes.add(entry.name.dropLast(6))
        }
        loadPlugins(file, classes)
    }

    private fun loadPlugins(file: File, classes: List<String>) {
        val loader = URLClassLoader.newInstance(arrayOf(
            file.toURI().toURL()
        ))
        for(path in classes) {
            val pluginClass: Class<*>
            try {
                pluginClass = loader.loadClass(path)
            } catch(ex: ClassNotFoundException) {
                error("Class at $path was not found.")
                continue
            }
            if(Plugin::class.java.isAssignableFrom(pluginClass)) {
                if(!hasCorrectClassModifiers(pluginClass.modifiers)) {
                    error("Plugin ${pluginClass.name} is not accessible or instantiable.")
                    continue
                }
                val instance: Any
                try {
                    instance = pluginClass.constructors.first {
                        it.parameterCount == 0 &&
                                hasCorrectConstructorModifiers(it.modifiers)
                    }?.newInstance()!!
                } catch(e: NoSuchElementException) {
                    error("Plugin ${pluginClass.name} does not have accessible parameterless constructor.")
                    continue
                } catch(e: NullPointerException) {
                    error("Plugin ${pluginClass.name} has constructor that returns null.")
                    val stackTrace = StringWriter()
                    e.printStackTrace(PrintWriter(stackTrace))
                    error(stackTrace.buffer.toString())
                    continue
                } catch(e: Exception) {
                    error("Plugin ${pluginClass.name} could not be loaded due to the following exception:")
                    val stackTrace = StringWriter()
                    e.printStackTrace(PrintWriter(stackTrace))
                    error(stackTrace.buffer.toString())
                    continue
                }
                plugins.add(instance as Plugin)
            }
        }
    }

    private class PluginPolicy : java.security.Policy() {
        override fun getPermissions(source: CodeSource): PermissionCollection {
            val p = Permissions()
            if(!source.location.path.contains("plugins")) p.add(AllPermission())
            else {
                val slashIndex = source.location.path.lastIndexOf('/')
                val dotIndex = source.location.path.lastIndexOf('.')
                val sourceName = source.location.path.substring(slashIndex+1, dotIndex)
                p.add(FilePermission("plugins/", "read"))
                p.add(FilePermission("plugins/$sourceName/", "read, write, delete"))
            }
            return p
        }
    }
}

/**
 * A plugin for the Chs game engine. Any class implementing this interface may be instantiated and
 * started by the Chs plugin loader.
 */
interface Plugin {
    /**
     * Called when the engine has started with this plugin loaded.
     */
    fun start()
}