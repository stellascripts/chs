package chs.assets

import chs.Assets
import java.io.File
import java.io.InputStream

/**
 * Locates assets by searching loose files from a root in the project folder.
 */
class FileTreeLocator(private val rootFolder: String): Assets.Locator {
    override fun handles(location: String): Boolean {
        return true
    }

    override fun open(location: String): InputStream {
        return File(rootFolder, location).inputStream()
    }
}