package chs.example

import chs.*
import chs.assets.useDefaults
import org.joml.Vector3f


object ExampleApp {
    /*
    Sets up a window and draws some shapes in a lighting environment.
     */
    fun run() {
        //loads .obj and .mtl.toml files
        Assets.useDefaults()

        val win = Window(1024, 768, "ExampleApp")

        //make a new scene
        val scene = Scene()

        //set up some lights
        with(scene.lighting) {
            setAmbientColor(1f, 1f, 1f, 0.18f)
            addDirectionalLight(Vector3f(1f, -1f, 0f), Color3(1f, 1f, 1f), 2f)
            addPointLight(Vector3f(0f, 5f, 0f), Color3(1f, 1f, 0f), 25f)
            addPointLight(Vector3f(5f, 0f, 0f), Color3(1f, 0f, 1f), 25f)
            addPointLight(Vector3f(0f, 0f, 5f), Color3(0f, 1f, 1f), 25f)
        }

        //create a camera and have it look at the center of the scene
        val camera = Camera(
            45f,
            4f / 3f,
            1f, 20f
        )
        camera.setPosition(0f, 5f, 10f)
        camera.setAimPosition(0f, 0f, 0f)

        //load the material
        val material = Assets.load<RenderMaterial>("material.mat.chs")

        //reusable meshes
        val sphereMesh = Assets.load<Mesh>("sphere.obj")

        //make some spheres
        val sphere = scene.addObject(sphereMesh::draw, material)
        sphere.transform.position.add(2f,1f,0f)

        val sphere2 = scene.addObject(sphereMesh::draw, material)
        sphere2.transform.position.add(0f, 0f, 2f)
        sphere2.transform.rotation.rotateZ(90f* DEG_TO_RAD)

        //a cube
        val cube = scene.addObject(Assets.load<Mesh>("cube.obj")::draw, material)
        cube.transform.position.add(-1f, 1f, 0f)
        cube.transform.rotation.rotateX(45f * DEG_TO_RAD)
        cube.transform.rotation.rotateZ(45f * DEG_TO_RAD)

        //rendering loop
        win.loop {
            sphere.transform.rotation.rotateY(1f/60f * PIf)
            cube.transform.rotation.rotateY(1f/120f * PIf)
            scene.render(camera)
        }
    }
}

fun main() {
    ExampleApp.run()
}