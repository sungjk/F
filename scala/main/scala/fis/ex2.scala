package fis

/**
  * Created by jeremy on 03/05/2019.
  */
object ex2 {
    def fib(n: Int): Int = {
        @annotation.tailrec
        def loop(n: Int, prev: Int, cur: Int): Int = if (n <= 0) prev else loop(n - 1, cur, prev + cur)
        loop(n, 0, 1)
    }

    def isSorted[A](as: Array[A], ordering: (A, A) => Boolean): Boolean = {
        @annotation.tailrec
        def go(n: Int): Boolean = if (n >= as.length - 1) true else if (ordering(as(n), as(n + 1))) false else go(n + 1)
        go(0)
    }

    def curry[A, B, C](f: (A, B) => C): A => B => C = a => b => f(a, b)
    def f1(a: Int, b: Int): Int = a + b
    def g1(a: Int)(b: Int): Int = a + b

    def uncurry[A, B, C](f: A => B => C): (A, B) => C = (a, b) => f(a)(b)
    def f2(a: Int, b: Int): Int = a + b
    def g2(a: Int)(b: Int): Int = a + b

    def compose[A, B, C](f: B => C, g: A => B): A => C = a => f(g(a))
    def f3(b: Int): Int = b / 2
    def g3(a: Int): Int = a + 2

    def main(args: Array[String]): Unit = {
        assert(fib(5) == 5)
        assert(isSorted(Array(1, 3, 5, 7), (x: Int, y: Int) => x > y))
        assert(!isSorted(Array(7, 5, 1, 3), (x: Int, y: Int) => x < y))
        assert(isSorted(Array("Scala", "Excersises"), (x: String, y: String) => x.length > y.length))
        assert(curry(f1)(1)(1) == f1(1, 1))
        assert(curry(f1)(1)(1) == g1(1)(1))
        assert(uncurry(g2)(1, 1) == g2(1)(1))
        assert(uncurry(g2)(1, 1) == f2(1, 1))
        assert(compose(f3, g3)(0) != compose(g3, f3)(0))
        assert(compose(f3, g3)(2) == 2)
        assert(compose(g3, f3)(2) == 3)
    }
}
