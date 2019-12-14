package chs.assets

import chs.Assets
import chs.gltf.GLTFLoader

/**
 * Sets up default asset loaders and locators, which includes:
 * [FileTreeLocator],
 * [ObjMeshLoader],
 * [ShaderLoader]
 */
fun Assets.useDefaults() {
    useLocator(FileTreeLocator("assets"))
    useLoader(ObjMeshLoader())
    useComplexLoader(ShaderLoader())
    useLoader(ChsMaterialLoader())
}