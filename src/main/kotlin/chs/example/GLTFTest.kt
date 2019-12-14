package chs.example

import chs.*
import chs.assets.useDefaults
import chs.gltf.GLTF
import chs.mesh.*
import com.chiaroscuro.chiaroscuro.checkGL
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import java.io.InputStream
import java.nio.ByteBuffer


object GLTFTest {
    /*
    Sets up a window and draws some shapes in a lighting environment.
     */
    fun run() {
        //loads .obj and .mtl.toml files
        Assets.useDefaults()
        Assets.useComplexLoader(GLTFNodeLoader())

        val win = Window(1024, 768, "ExampleApp")

        //make a new scene
        val lights = Lighting()

        //set up some lights
        with(lights) {
            setAmbientColor(1f, 1f, 1f, 0.18f)
            //addDirectionalLight(Vector3f(1f, -1f, 0f), Color3(1f, 1f, 1f), 2f)
            addPointLight(Vector3f(0f, 5f, 0f), Color3(1f, 1f, 0f), 25f)
            addPointLight(Vector3f(5f, 0f, 0f), Color3(1f, 0f, 1f), 25f)
            addPointLight(Vector3f(0f, 0f, 5f), Color3(0f, 1f, 1f), 25f)
        }

        //create a camera and have it look at the center of the scene
        val camera = Camera(
            fieldOfView = 45f,
            aspectRatio = 4f / 3f,
            nearPlane =   1f,
            farPlane =    20f
        )
        camera.setPosition(0f, 5f, 10f)
        camera.setAimPosition(0f, 0f, 0f)

        //load the material
        val material = Assets.load<RenderMaterial>("materials/material.mat.chs")

        val nodes = Assets.load<List<Node>>("models/gun.gltf")
        val roots = nodes.filter { it.parent == null }
        for(root in roots) {
            root.material = material
        }

        //make some spheres


        //rendering loop
        win.loop {
            for(phase in RenderPhase.all) {
                for (obj in roots) {
                    obj.transform.rotation.rotateAxis(-1f/240f, Y_AXIS)
                   // obj.children.first().transform.rotation.rotateLocalZ(1f/10f)
                    obj.draw(lights, camera, phase)
                }
            }
        }
    }
}

fun main() {
    GLTFTest.run()
}