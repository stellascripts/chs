package com.chiaroscuro.chiaroscuro.math

/**
 * Squares a number.
 */
inline fun sq(x: Float): Float = x * x

/**
 * Considers numbers approximately equal for a specific error value.
 */
val EPSILON = Math.ulp(1f)
infix fun Float.approx(other: Float): Boolean = (this - other) <= EPSILON

const val TWO_PI = (kotlin.math.PI * 2.0).toFloat()
const val PI = kotlin.math.PI.toFloat()
const val PI_OVER_2 = (kotlin.math.PI * 0.5).toFloat()
const val PI_OVER_4 = (kotlin.math.PI * 0.25).toFloat()
const val DEG_TO_RAD = (kotlin.math.PI / 180.0).toFloat()