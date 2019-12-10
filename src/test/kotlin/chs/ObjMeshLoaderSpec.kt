package chs

import chs.assets.ObjMeshLoader
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

private fun loadMesh(str: String): ObjMeshLoader.ObjMesh {
    val loader = ObjMeshLoader()
    return loader.parse(str.reader())
}

class ObjMeshLoaderSpec : StringSpec({
    val meshFile = """
        v -0.454559 1.2 -9.02389e-017
        vt 17.896 47.2441
        vn 5.02214e-017 6.83466e-017 -1
        v -0.554559 0.4 -9.02389e-017
        vt 21.833 15.748
        v -0.554559 1.2 -9.02389e-017
        vt 21.833 47.2441
        f 1/1/1 2/2/1 3/3/1
    """.trimIndent()

    "Reads Vertices" {
        val mesh = loadMesh(meshFile)
        val vertices = mesh.vpack
        vertices shouldBe floatArrayOf(
            -0.454559f, 1.2f, -9.02389e-017f,
            -0.554559f, 0.4f, -9.02389e-017f,
            -0.554559f, 1.2f, -9.02389e-017f
            )
    }
    "Reads Texcoords" {
        val mesh = loadMesh(meshFile)
        val coords = mesh.tpack
        coords shouldBe floatArrayOf(
            17.896f, 47.2441f,
            21.833f, 15.748f,
            21.833f, 47.2441f
            )
    }
    "Reads Normals" {
        val mesh = loadMesh(meshFile)
        val vertices = mesh.npack
        vertices shouldBe floatArrayOf(
            5.02214e-017f, 6.83466e-017f, -1f,
            5.02214e-017f, 6.83466e-017f, -1f,
            5.02214e-017f, 6.83466e-017f, -1f
        )
    }
    "Reads Faces As Indices" {
        val mesh = loadMesh(meshFile)
        val indices = mesh.ipack
        indices shouldBe intArrayOf(0,1,2)
    }
})