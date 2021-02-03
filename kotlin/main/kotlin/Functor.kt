import kotlin.Nothing

/**
 * Created by jeremy on 2021/02/03.
 */
interface Functor<out A> {
    fun <B> fmap(f: (A) -> B): Functor<B>
}

// Maybe functor
sealed class Maybe<out A> : Functor<A> {
    abstract override fun toString(): String
    abstract override fun <B> fmap(f: (A) -> B): Maybe<B>
}

data class Just<out A>(val value: A) : Maybe<A>() {
    override fun toString(): String = "Just($value)"
    override fun <B> fmap(f: (A) -> B): Maybe<B> = Just(f(value))
}

object Nothing : Maybe<Nothing>() {
    override fun toString(): String = "Nothing"
    override fun <B> fmap(f: (Nothing) -> B): Maybe<B> = Nothing
}

// Tree functor
sealed class Tree<out A> : Functor<A> {
    abstract override fun toString(): String
    abstract override fun <B> fmap(f: (A) -> B): Tree<B>
}

object EmptyTree : Tree<Nothing>() {
    override fun toString(): String = "E"
    override fun <B> fmap(f: (Nothing) -> B): Tree<B> = EmptyTree
}

data class Node<out A>(val value: A, val leftTree: Tree<A>, val rightTree: Tree<A>) : Tree<A>() {
    override fun toString(): String = "(N $value $leftTree $rightTree)"
    override fun <B> fmap(f: (A) -> B): Tree<B> =
        Node(f(value), leftTree.fmap(f), rightTree.fmap(f))
}

// Either functor
sealed class Either0<out L, out R> : Functor<R> {
    abstract override fun <R2> fmap(f: (R) -> R2): Either0<L, R2>
}

data class Left0<out L>(val value: L): Either0<L, Nothing>() {
    override fun <R2> fmap(f: (Nothing) -> R2): Either0<L, R2> = this
}

data class Right0<out R>(val value: R): Either0<Nothing, R>() {
    override fun <R2> fmap(f: (R) -> R2): Either0<Nothing, R2> = Right0(f(value))
}

// Unary functor
data class UnaryFunction<in T, out R>(val g: (T) -> R) : Functor<R> {
    override fun <R2> fmap(f: (R) -> R2): UnaryFunction<T, R2> = UnaryFunction { x: T -> f(g(x)) }

    fun invoke(input: T): R = g(input)
}
