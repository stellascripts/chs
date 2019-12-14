package chs

import java.io.IOException
import java.io.InputStream
import java.io.Reader
import java.lang.Exception

/**
 * Centralized tools for loading and manipulating assets from disk.
 */
object Assets {
    private val locators = ArrayList<Locator>()
    private val loaders = ArrayList<Loader>()
    private val complexLoaders = ArrayList<ComplexLoader>()

    /**
     * Registers a [Locator] to be used in translating paths to streams of asset data.
     */
    fun useLocator(locator: Locator) {
        locators.add(locator)
    }

    /**
     * Registers a [Loader] to be used in producing asset objects from input streams.
     */
    fun useLoader(loader: Loader) {
        loaders.add(loader)
    }

    /**
     * Registers a [ComplexLoader] to be used in producing asset objects which require multiple loads or other
     * nonstandard methods of creation.
     */
    fun useComplexLoader(loader: ComplexLoader) {
        complexLoaders.add(loader)
    }

    private fun open(location: String): InputStream? {
        for (locator in locators) {
            if (locator.handles(location)) {
                try {
                    return locator.open(location)
                } catch(e: Exception) {
                    if(e is AssetException) throw e
                    else throw AssetLoadingException("An exception occurred while locating an asset.", e)
                }
            }
        }
        return null
    }

    private fun loadFrom(location: String, stream: InputStream): Any? {
        for (loader in loaders) {
            if (loader.handles(location)) {
                return loader.load(stream)
            }
        }
        return null
    }

    private fun complexLoad(location: String): Any? {
        for(loader in complexLoaders) {
            if(loader.handles(location)) return loader.load(location)
        }
        return null
    }

    /**
     * Locates an asset and returns it as a readable stream of text data.
     * To be used in complex loaders with text formats such as [chs.assets.ShaderLoader].
     * @param location The location of the asset to be loaded.
     * @throws AssetNotFoundException if the asset could not be found by any of the registered locators.
     * @throws AssetLoadingException if there was an otherwise unspecified error while locating the asset.
     */
    fun read(location: String): Reader {
        val stream = open(location) ?: throw AssetNotFoundException("Could not locate: $location")
        return stream.reader()
    }

    /**
     * Locates an asset and returns it as a stream of bytes.
     * To be used in complex loaders with byte formats.
     * @param location The location of the asset to be loaded.
     * @throws AssetNotFoundException if the asset could not be found by any of the registered locators.
     * @throws AssetLoadingException if there was an otherwise unspecified error while locating the asset.
     */
    fun stream(location: String): InputStream {
        val stream = open(location) ?: throw AssetNotFoundException("Could not locate: $location")
        return stream
    }

    /**
     * Loads an asset of the specified type.
     * @return An asset object of type T.
     * @param T The type of object to be returned.
     * @param location The location of the asset to be loaded.
     * @throws AssetNotFoundException if the asset could not be found by any of the registered locators.
     * @throws AssetNotCreatableException if the asset was found, but no loader could create the object required.
     * @throws AssetLoadingException if there was an otherwise unspecified error while locating or loading the object.
     */
    inline fun <reified T> load(location: String): T {
        val r = loadAny(location)
        if (r !is T) {
            val expected = T::class.java.simpleName
            val actual = r::class.java.simpleName
            throw AssetLoadingException("Loaded $this expecting $expected, found $actual.")
        }
        return r
    }

    /**
     * Loads an asset of the type loadable at this location.
     */
    fun loadAny(location: String): Any {
        val stream = open(location) ?:
            return complexLoad(location) ?:
            throw AssetNotFoundException("Could not locate: $location")
        try {
            return loadFrom(location, stream) ?: return complexLoad(location) ?: throw AssetNotCreatableException(
                "No known loader exists for file: $location"
            )
        } catch(e: AssetNotFoundException) {
            throw e
        } catch(e: AssetNotCreatableException) {
            throw e
        } catch(e: Exception) {
            throw AssetLoadingException("An exception occurred while loading the asset at $location.", e)
        }
    }

    /**
     * Object responsible for opening asset streams at a provided location.
     */
    interface Locator {
        /**
         * Called to determine if the location path is one that this locator is responsible for.
         * @return true if this locator should locate the asset at this path, false otherwise.
         */
        fun handles(location: String): Boolean

        /**
         * Opens the asset at the specified location and streams its data.
         * @return an input stream of data at this location.
         */
        @Throws(IOException::class) fun open(location: String): InputStream
    }

    /**
     * Object responsible for loading asset objects from a provided data stream.
     */
    interface Loader {
        /**
         * Called to determine if the location path is one that this loader is responsible for.
         * @return true if this loader might load the asset at this path, false otherwise.
         */
        //todo: allow the loader to read the first bytes of the stream to determine file type?
        fun handles(location: String): Boolean

        /**
         * Loads the asset at the specified location from its data stream.
         * @return the loaded asset object.
         */
        @Throws(IOException::class) fun load(stream: InputStream): Any
    }

    /**
     * Object responsible for loading asset objects from a provided location which may require more
     * complex handling of the translation from location to asset.
     */
    interface ComplexLoader {
        /**
         * Called to determine if the location path is one that this loader is responsible for.
         * @return true if this loader might load the asset at this path, false otherwise.
         */
        fun handles(location: String): Boolean

        /**
         * Loads the asset at the specified location.
         * @return the loaded asset object.
         */
        @Throws(IOException::class) fun load(location: String): Any
    }
}