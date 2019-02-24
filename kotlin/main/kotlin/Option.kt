/**
 * Created by jeremy on 08/27/2018.
 */
sealed class Option<out A> {
    abstract fun isEmpty(): Boolean

    fun isDefined(): Boolean = !isEmpty()

    abstract fun get(): A

    inline fun <B> map(func: (A) -> B): Option<B> = when (this) {
        is None -> this
        is Some -> Some(func(value))
    }

    inline fun <B> flatMap(func: (A) -> Option<B>): Option<B> = when (this) {
        is None -> this
        is Some -> func(value)
    }
}

object None : Option<Nothing>() {
    override fun isEmpty(): Boolean  = true

    override fun get() = throw NoSuchElementException("None.get")
}

data class Some<out A>(val value: A) : Option<A>() {
    override fun isEmpty(): Boolean = false

    override fun get(): A = value
}

fun <A> Option<A>.getOrElse(default: () -> A): A = if (isEmpty()) default() else get()

fun <A> Option<A>.orElse(alternative: () -> Option<A>): Option<A> = if (isEmpty()) alternative() else this
