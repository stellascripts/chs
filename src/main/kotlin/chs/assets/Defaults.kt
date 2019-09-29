package chs.assets

import chs.Assets

/**
 * Sets up default asset loaders and locators, which includes:
 * [FileTreeLocator],
 * [ObjMeshLoader],
 * [ShaderLoader]
 */
fun Assets.useDefaults() {
    useLocator(FileTreeLocator("assets"))
    useLoader(ObjMeshLoader())
    Assets.useComplexLoader(ShaderLoader())
}