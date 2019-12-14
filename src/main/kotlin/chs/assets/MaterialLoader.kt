package chs.assets

import chs.*
import chs.encoding.*
import java.io.InputStream

/**
 * Loads .mat.chs files into [RenderMaterial]s.
 */
class ChsMaterialLoader: Assets.Loader {
    override fun handles(location: String): Boolean {
        return location.endsWith(".mat.chs")
    }

    private fun <T> MessageTable.intoMap(map: Map<String, T>, key: String, passName: String): T {
        val value = expect(key, passName)
        return map[value]?:throw AssetLoadingException("No such $key: $value in pass: $passName")
    }

    private fun MessageTable.expect(key: String, passName: String) = this.valueOf(key)
        ?:throw AssetLoadingException("No $key entry for pass: $passName")

    override fun load(stream: InputStream): Any {
        val root = stream.reader().use { it.parseChsConfig() }
        val material = RenderMaterial()
        val passes = root.tableOf("pass")?:throw AssetLoadingException("No passes in material file.")
        val shaders = HashMap<String, Shader>()
        passes.tableIterate { passName, _, table ->
            if(table == null) return@tableIterate

            //load shader
            val vertexFile = table.expect("vertex file", passName)
            val fragmentFile = table.expect("fragment file", passName)

            val shader = shaders.getOrPut("$vertexFile $fragmentFile") {
                val vertex = Assets.read(vertexFile).use { it.readText() }
                val fragment = Assets.read(fragmentFile).use { it.readText() }
                Shader(vertex, fragment)
            }

            val phase = table.intoMap(RenderPhase.parse, "phase", passName)
            val pass = RenderPass(shader)
            (material.passes(phase) as MutableList<RenderPass>).add(pass)

            //pass.clearDepth = table.valueOf("clear depth")?.toDoubleOrNull()
            //pass.clearColor = table.valueOf("clear color")?.toColor()
            pass.blendMode = table.intoMap(BlendMode.parse, "blend mode", passName)
            pass.testDepth = table.valueOf("depth")?.let { DepthMode.parse[it] }
            pass.writeDepth = table.valueOf("write depth?")?.toBoolean()?:true
            pass.writeColor = table.valueOf("write color?")?.toBoolean()?:true
            pass.perLight = table.valueOf("per light?")?.toBoolean()?:false
        }
        return material
    }
}