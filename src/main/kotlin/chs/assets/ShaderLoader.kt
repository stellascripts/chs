package chs.assets

import chs.Assets
import chs.Shader

/**
 * Loads [Shader] objects given a path to their source code ending in .vert or .frag. The loader attempts to load the
 * corresponding implied code file by loading the same path with an alternate extension, e.g.
 * > shaders/example/shader.vert
 *
 * will attempt to also load
 * > shaders/example/shader.frag
 *
 * and vice versa.
 * @see Shader
 * @see Assets
 * @see Assets.ComplexLoader
 */
class ShaderLoader : Assets.ComplexLoader {
    override fun handles(location: String): Boolean {
        return location.endsWith(".vert") || location.endsWith(".frag")
    }
    override fun load(location: String): Shader {
        val extension = location.substringAfterLast('.')
        val vert: String
        val frag: String
        if(extension == "vert") {
            vert = Assets.read(location).readText()
            frag = Assets.read(location.removeSuffix("vert")+"frag").readText()
        } else {
            vert = Assets.read(location.removeSuffix("frag")+"vert").readText()
            frag = Assets.read(location).readText()
        }
        return Shader(vert, frag)
    }
}