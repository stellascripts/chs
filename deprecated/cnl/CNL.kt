package chs.cnl

import chs.API
import java.lang.RuntimeException

/**
 * An exception thrown when a Ciel Notation Language structure or parser encounters an error related to the language
 * or its type system.
 */
class CNLException(msg: String): RuntimeException()

/**
 * A readable Ciel Notation Language structure.
 */
interface ReadableCNL: Iterable<CNL.Pair> {
    private inline fun <reified T> Any.check(): T = if(this is T) this else
        throw CNLException("$this is not ${T::class.java.simpleName}")

    private inline fun <reified T> getType(key: String): T {
        return this.where { it.key == key }.first().value.check()
    }

    private inline fun <reified T> getPossibleType(key: String): T? {
        return this.where { it.key == key }.firstOrNull()?.value as? T
    }

    private inline fun <reified T: Any> getAllType(key: String): List<T> {
        return this.mapNotNull{ if(it.key == key) it.value.check<T>() else null }
    }

    private inline fun <reified T: Any> getAllPossibleType(key: String): List<T?> {
        return this.mapNotNull { if (it.key == key) it.value as? T else null }
    }
    /**
     * Gets the first defined value matching the key as a String.
     * @throws CNLException if no value is defined or the value is not a String.
     */
    @API
    fun getString(key: String): String = getType(key)
    /**
     * Gets the first value matching the key, or null of no
     * value is defined or the value is not a String.
     */
    @API
    fun getPossibleString(key: String): String? = getType(key)
    /**
     * Gets all defined values matching the key as a list of Strings.
     * @throws CNLException if any defined value is not a String.
     */
    @API
    fun getAllStrings(key: String): List<String> = getAllType(key)
    /**
     * Gets all defined values of type String matching the key as a list of Strings.
     * Matching values which are not of type String will not be included.
     */
    @API
    fun getAllPossibleStrings(key: String): List<String?> = getAllType(key)

    /**
     * Gets the first defined value matching the key as a Long.
     * @throws CNLException if no value is defined or the value is not an integer.
     */
    @API
    fun getInt(key: String): Long = getType(key)
    /**
     * Gets the first value matching the key as a Long, or null of no
     * value is defined or the value is not an integer.
     */
    @API
    fun getPossibleInt(key: String): Long? = getType(key)
    /**
     * Gets all defined values matching the key as a list of Longs.
     * @throws CNLException if any defined value is not an integer.
     */
    @API
    fun getAllInts(key: String): List<Long> = getAllType(key)
    /**
     * Gets all defined integer values matching the key as a list of Longs.
     * Matching values which are not integers will not be included.
     */
    @API
    fun getAllPossibleInts(key: String): List<Long?> = getAllType(key)

    /**
     * Gets the first defined value matching the key as a Double.
     * @throws CNLException if no value is defined or the value is not a floating-point value.
     */
    @API
    fun getFloat(key: String): Double = getType(key)
    /**
     * Gets the first value matching the key as a Double, or null of no
     * value is defined or the value is not a floating-point value.
     */
    @API
    fun getPossibleFloat(key: String): Double? = getType(key)
    /**
     * Gets all defined values matching the key as a list of Doubles.
     * @throws CNLException if any defined value is not a floating-point value.
     */
    @API
    fun getAllFloats(key: String): List<Double> = getAllType(key)
    /**
     * Gets all defined floating-point values matching the key as a list of Doubles.
     * Matching values which are not floating-point values will not be included.
     */
    @API
    fun getAllPossibleFloats(key: String): List<Double?> = getAllType(key)

    /**
     * Gets the first defined value matching the key as a Boolean.
     * @throws CNLException if no value is defined or the value is not a Boolean.
     */
    @API
    fun getBoolean(key: String): Boolean = getType(key)
    /**
     * Gets the first value matching the key as a Boolean, or null of no
     * value is defined or the value is not a Boolean.
     */
    @API
    fun getPossibleBoolean(key: String): Boolean? = getType(key)
    /**
     * Gets all defined values matching the key as a list of Booleans.
     * @throws CNLException if any defined value is not a Boolean.
     */
    @API
    fun getAllBooleans(key: String): List<Boolean> = getAllType(key)
    /**
     * Gets all defined Boolean values matching the key as a list of Booleans.
     * Matching values which are not Booleans will not be included.
     */
    @API
    fun getAllPossibleBooleans(key: String): List<Boolean?> = getAllType(key)

    /**
     * Gets the first defined value matching the key as an [Atom][CNL.Atom].
     * @throws CNLException if no value is defined or the value is not an [Atom][CNL.Atom].
     */
    @API
    fun getAtom(key: String): CNL.Atom = getType(key)
    /**
     * Gets the first value matching the key as an [Atom][CNL.Atom], or null of no
     * value is defined or the value is not an [Atom][CNL.Atom].
     */
    @API
    fun getPossibleAtom(key: String): CNL.Atom? = getType(key)
    /**
     * Gets all defined values matching the key as a list of [Atoms][CNL.Atom].
     * @throws CNLException if any defined value is not an [Atom][CNL.Atom].
     */
    @API
    fun getAllAtoms(key: String): List<CNL.Atom> = getAllType(key)
    /**
     * Gets all defined Atom values matching the key as a list of [Atoms][CNL.Atom].
     * Matching values which are not [Atoms][CNL.Atom] will not be included.
     */
    @API
    fun getAllPossibleAtoms(key: String): List<CNL.Atom?> = getAllType(key)

    /**
     * Gets the first defined value matching the key.
     * @throws CNLException if no value is defined.
     */
    @API
    fun get(key: String): Any = getType(key)

    /**
     * Gets all defined values matching the key as a list.
     */
    @API
    fun getAll(key: String): List<Any> = getAllType(key)

    /**
     * Gets the first defined value matching the key, or null if no value is defined.
     */
    @API
    fun getPossible(key: String): Any? = getType(key)

    /**
     * Returns a CNL structure containing only those keys which start with the provided partial key.
     */
    @API
    fun struct(partialKey: String): ReadableCNL

    /**
     * Returns an iterable subtable of this CNL structure matching the given predicate.
     */
    @API
    fun where(predicate: (CNL.Pair) -> Boolean): Iterable<CNL.Pair>
}

/**
 * A writable Ciel Notation Language structure.
 */
interface WritableCNL {
    /**
     * Inserts a list of possible values into the structure, each of which matches the provided key.
     * Null values in the list are not inserted.
     */
    @API
    fun putAll(key: String, value: List<Any?>) {
        for(v in value) {
            put(key, v)
        }
    }

    /**
     * Inserts a list of possible values into the structure, each of which matches the provided key.
     * Null values in the list are not inserted.
     */
    fun putAll(list: List<Pair<String, Any?>>) {
        for((k,v) in list) {
            put(k,v)
        }
    }

    /**
     * Inserts a value into the structure matching the provided key, or does nothing if the value is null.
     */
    @API
    fun put(key: String, value: Any?)

    /**
     * Returns a CNL structure containing only those keys which start with the provided partial keys.
     * Writes to the returned structure will be mirrored in this structure.
     */
    @API
    fun struct(partialKey: String): WritableCNL
}

/**
 * A Ciel Notation Language structure.
 */
class CNL private constructor(private val map: MutableList<Pair>, private val partialKey: String = ""):
    Iterable<CNL.Pair>, ReadableCNL, WritableCNL {
    companion object {
        private fun joinKeys(partA: String, partB: String): String {
            if(partA.isBlank()) return partB
            else if(partB.isBlank()) return partA
            else return "$partA.$partB"
        }
    }

    /**
     * A value which is not meant to be interpreted as text,
     * but as a named token similar to an enum.
     * @param name The name of the atom.
     */
    data class Atom(val name: String)

    /**
     * A key-value pair used in CNL structures.
     * @param key
     * @param value
     */
    data class Pair(val key: String, val value: Any)

    @API
    constructor() : this(ArrayList(), "")
    private constructor(parent: CNL, partialKey: String): this(parent.map, joinKeys(parent.partialKey, partialKey))

    override fun put(key: String, value: Any?) {
        if(value != null) map.add(Pair(joinKeys(partialKey, key), value))
    }

    override fun struct(partialKey: String): CNL {
        return CNL(this, partialKey)
    }

    override fun iterator(): Iterator<Pair> = CNLIterator(map, partialKey, null)

    override fun where(predicate: (Pair) -> Boolean): Iterable<Pair> =
        Iterable { CNLIterator(map, partialKey, predicate) }

    private class CNLIterator(val map: List<Pair>,
                              val partialKey: String,
                              val predicate: ((Pair) -> Boolean)?): Iterator<Pair> {
        var index = nextIndex()
        override fun hasNext(): Boolean = index < map.size

        private fun nextIndex(): Int {
            var i = 0
            while(!(map[i].key.startsWith(partialKey) && predicate?.invoke(map[i]) != false) && i < map.size) {
                ++i
            }
            return i
        }

        override fun next(): Pair {
            val v = map[index]
            index = nextIndex()
            return v
        }
    }
}