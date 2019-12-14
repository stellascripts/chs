package chs.assets

import chs.AssetLoadingException
import chs.Assets
import chs.Lookahead
import chs.Mesh
import java.io.InputStream
import java.io.Reader

/**
 * Loads Mesh objects from .obj files.
 */
class ObjMeshLoader: Assets.Loader {
    private data class Point(val x: Float, val y: Float, val z: Float)
    private val zero = Point(0f, 0f, 0f)

    private data class FaceIndex(val pos: Int, val texCoord: Int, val normal: Int)
    private data class Vertex(val pos: Point, val texCoord: Point, val normal: Point)

    /**
     * An object mesh, consisting of packed vertices, indices, texture coordinates and normals.
     */
    private class ObjMesh(
        /**
         * Packed vertex coordinates in triplets of floats.
         */
        val vpack: FloatArray,
        /**
         * Packed indices in single integers.
         */
        val ipack: IntArray,
        /**
         * Packed texture coordinates in pairs of floats.
         */
        val tpack: FloatArray,
        /**
         * Packed normal coordinates in triplets of floats.
         */
        val npack: FloatArray
    )

    private class State {
        val points = ArrayList<Point>()
        val textureCoords = ArrayList<Point>()
        val normals = ArrayList<Point>()
        val indices = ArrayList<FaceIndex>()
    }

    private fun ArrayList<Point>.tryGet(i: Int) : Point {
        return if(i < this.size && i >= 0) this[i]
        else zero
    }

    private fun readObjIndex(s:String, list: ArrayList<*>):Int {
        val i = s.toIntOrNull()?:return -1
        return if(i >= 0) i - 1
        else list.size - i
    }

    override fun handles(location: String): Boolean {
        return location.endsWith(".obj")
    }

    /**
     *  Loads a mesh from the specified reader.
     */
    override fun load(stream: InputStream): Mesh {
        val objmesh = parse(stream.reader())
        return Mesh(objmesh.vpack, objmesh.ipack, objmesh.tpack, objmesh.npack)
    }

    /**
     *  Parses a mesh from the specified reader.
     */
    private fun parse(reader: Reader): ObjMesh {
        return with(State()) {
            read(reader)
            val combos = ArrayList<Vertex>()
            val ipack = IntArray(indices.size)
            var j = 0

            for(i in 0 until indices.size) {
                val (p,t,n) = indices[i]
                val vtx = Vertex(
                    points.tryGet(p),
                    textureCoords.tryGet(t),
                    normals.tryGet(n)
                )
                //println("Vertex $i: $vtx")
                val idx = combos.indexOf(vtx)
                if(idx > 0) {
                    //println("  Found at $idx")
                    ipack[j++] = idx
                } else {
                    //println("  Created")
                    ipack[j++] = combos.size
                    combos.add(vtx)
                }
            }

            val vpack = FloatArray(combos.size * 3)
            val tpack = FloatArray(combos.size * 2)
            val npack = FloatArray(combos.size * 3)


            for(i in 0 until combos.size) {
                vpack[3 * i + 0] = combos[i].pos.x
                vpack[3 * i + 1] = combos[i].pos.y
                vpack[3 * i + 2] = combos[i].pos.z

                tpack[2 * i + 0] = combos[i].texCoord.x
                tpack[2 * i + 1] = combos[i].texCoord.y

                npack[3 * i + 0] = combos[i].normal.x
                npack[3 * i + 1] = combos[i].normal.y
                npack[3 * i + 2] = combos[i].normal.z
            }

            /*for (a in 0 until points.size) {
                vpack[3 * a + 0] = points[a].x
                vpack[3 * a + 1] = points[a].y
                vpack[3 * a + 2] = points[a].z
            }

            for (a in 0 until textureCoords.size) {
                tpack[2 * a + 0] = textureCoords[a].x
                tpack[2 * a + 1] = textureCoords[a].y
            }*/
            ObjMesh(vpack, ipack, tpack, npack)
        }
    }



    private fun Lookahead.readNextWord(): String? {
        this.readWhile { it.isWhitespace() && it != '\n' }
        val n = this.next()
        if(n == '\n') return "\n"
        else pushback(n)
        val w = this.readWhile { !it.isWhitespace() }
        return if(w.isEmpty()) null else w
    }

    private fun State.read(reader: Reader) {
        val varray = FloatArray(4)
        var vindex = 0
        val tokens = Lookahead(reader)
        while (true) {
            val word = tokens.readNextWord() ?: break
            if(word == "\n") continue
            when (word) {
                "v" -> {
                    print("v")
                    while (vindex < 4) {
                        val f = tokens.readNextWord()?.toFloatOrNull() ?: break
                        varray[vindex++] = f
                        print(" $f")
                    }
                    points.add(Point(varray[0], varray[1], varray[2]))
                    vindex = 0
                    varray.fill(0f)
                    println()
                }
                "vn" -> {
                    //print("vn")
                    while (vindex < 3) {
                        val f = tokens.readNextWord()?.toFloatOrNull() ?: break
                        varray[vindex++] = f
                        //print(" $f")
                    }
                    normals.add(Point(varray[0], varray[1], varray[2]))
                    vindex = 0
                    varray.fill(0f)
                    //println()
                }
                "vt" -> {
                    //print("vt")
                    while (vindex < 3) {
                        val f = tokens.readNextWord()?.toFloatOrNull() ?: break
                        varray[vindex++] = f
                        //print(" $f")
                    }
                    textureCoords.add(Point(varray[0], varray[1], 0f))
                    vindex = 0
                    varray.fill(0f)
                    //println()
                }
                "f" -> {
                    //print("f")
                    while (true) {
                        val i = tokens.readNextWord() ?: break
                        if (i == "\n") break
                        //print(" $i")
                        val split = i.split('/')
                        val index = when(split.size) {
                            0 -> throw AssetLoadingException("Object face has 0 coordinates.")
                            1 -> FaceIndex(
                                readObjIndex(
                                    split[0],
                                    points
                                ), -1, -1
                            )
                            2 -> FaceIndex(
                                readObjIndex(split[0], points),
                                readObjIndex(split[1], textureCoords), -1
                            )
                            else -> FaceIndex(
                                readObjIndex(split[0], points),
                                readObjIndex(split[1], textureCoords),
                                readObjIndex(split[2], normals)
                            )
                        }
                        indices.add(index)
                    }
                    //println()
                }
                else -> {
                    //print("(unknown) $word")
                    while (true) {
                        val w = tokens.readNextWord()?:break
                        if (w == "\n") break
                        //print(" $w")
                    }
                    //println()
                }
            }
        }
    }
}