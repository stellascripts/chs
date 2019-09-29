package chs


/**
 * The superclass of all exceptions and errors which may be thrown by Chs.
 */
abstract class ChsException(msg: String, inner: Throwable? = null) : RuntimeException(msg, inner)

/**
 * An exception thrown when LWJGL generates an error.
 */
class InternalGLException(msg: String) : ChsException(msg)

/**
 * The class of all exceptions which may be thrown by [Assets.load].
 */
abstract class AssetException(msg: String, inner: Throwable? = null): ChsException(msg,inner)
/**
 * An exception thrown when an asset could not be located.
 */
class AssetNotFoundException(msg: String): AssetException(msg)

/**
 * An exception thrown when an asset was located, but could not be created.
 */
class AssetNotCreatableException(msg: String): AssetException(msg)

/**
 * An exception thrown when an asset was located and loading was attempted, but an error occurred.
 */
class AssetLoadingException(msg: String, inner: Throwable? = null): AssetException(msg, inner)