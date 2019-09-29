package chs

/**
 * A color made of three components, red, green and blue.
 * @property r the red component, 0.0 to 1.0
 * @property g the green component, 0.0 to 1.0
 * @property b the blue component, 0.0 to 1.0
 */
data class Color3(override var r: Float, override var g: Float, override var b: Float): Color3c

/**
 * An immutable interface to a color made of three components, red, green and blue.
 * @property r the red component, 0.0 to 1.0
 * @property g the green component, 0.0 to 1.0
 * @property b the blue component, 0.0 to 1.0
 */
interface Color3c {
    val r: Float
    val g: Float
    val b: Float
}


/**
 * A color made of four components, red, green, blue and alpha. The alpha of a color determines its transparency.
 * @property r the red component, 0.0 to 1.0
 * @property g the green component, 0.0 to 1.0
 * @property b the blue component, 0.0 to 1.0
 * @property a the alpha component, 0.0 to 1.0
 */
@API
data class Color4(override var r: Float,
                  override var g: Float,
                  override var b: Float,
                  override var a: Float): Color4c

/**
 * An immutable interface to a color made of four components, red, green, blue and alpha. The alpha of a color
 * determines its transparency.
 * @property r the red component, 0.0 to 1.0
 * @property g the green component, 0.0 to 1.0
 * @property b the blue component, 0.0 to 1.0
 * @property a the alpha component, 0.0 to 1.0
 */
interface Color4c: Color3c {
    val a: Float
}