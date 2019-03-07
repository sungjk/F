package fis

/**
  * Created by jeremy on 03/07/2019.
  */
object ex5 {
    sealed trait Stream[+A] {
        def headOption: Option[A] = foldRight(None: Option[A])((h, _) => Some(h))

        def foldRight[B](z: => B)(f: (A, => B) => B): B = this match {
            case Cons(h, t) => f(h(), t().foldRight(z)(f))
            case _ => z
        }

        def toList: List[A] = this match {
            case Cons(h, t) => h() :: t().toList
            case Empty => Nil
        }

        // returning the first n elements of a Stream
        def take(n: Int): Stream[A] = this match {
            case Cons(h, t) if n > 1 => Stream.cons(h(), t().take(n - 1))
            case Cons(h, _) if n == 1 => Stream.cons(h(), Stream.empty)
            case _ => Stream.empty
        }

        // skips the first n elements of a Stream
        def drop(n: Int): Stream[A] = this match {
            case Cons(_, t) if n > 0 => t().drop(n - 1)
            case _ => this
        }

        // return all starting elements of a Stream that match a given predicate
        def takeWhile(f: A => Boolean): Stream[A] = this match {
            case Cons(h, t) if f(h()) => Stream.cons(h(), t() takeWhile f)
            case _ => Stream.empty
        }

        def exists(p: A => Boolean): Boolean = this match {
            case Cons(h, t) => p(h()) || t().exists(p)
            case _ => false
        }

        def forAll(f: A => Boolean): Boolean = this.foldRight(true)((a, b) => f(a) && b)

        def takeWhile_1(f: A => Boolean): Stream[A] = foldRight(Stream.empty[A])((h, t) => if (f(h)) Stream.cons(h, t) else Stream.empty)

        def map[B](f: A => B): Stream[B] = foldRight(Stream.empty[B])((h, t) => Stream.cons(f(h), t))

        def filter(f: A => Boolean): Stream[A] = foldRight(Stream.empty[A])((h, t) => if (f(h)) Stream.cons(h, t) else t)

        def append[B >: A](s: => Stream[B]): Stream[B] = foldRight(s)((h, t) => Stream.cons(h, t))

        def flatMap[B](f: A => Stream[B]): Stream[B] = foldRight(Stream.empty[B])((h, t) => f(h) append t)

        def mapViaUnfold[B](f: A => B): Stream[B] = Stream.unfold(this) {
            case Cons(h, t) => Some((f(h()), t()))
            case _ => None
        }

        def takeViaUnfold(n: Int): Stream[A] = Stream.unfold((this, n)) {
            case (Cons(h, t), 1) => Some((h(), (Stream.empty, 0)))
            case (Cons(h, t), n) if n > 1 => Some((h(), (t(), n - 1)))
            case _ => None
        }

        def takeWhileViaUnfold(f: A => Boolean): Stream[A] = Stream.unfold(this) {
            case Cons(h, t) if f(h()) => Some((h(), t()))
            case _ => None
        }

        def zipWith[B, C](s2: Stream[B])(f: (A, B) => C): Stream[C] = Stream.unfold((this, s2)) {
            case (Cons(h1, t1), Cons(h2, t2)) => Some((f(h1(), h2()), (t1(), t2())))
            case _ => None
        }

        // it should continue the traversal as long as either stream has more elements
        // - it uses Option to indicate whether each stream has been exhausted
        def zipAll[B](s2: Stream[B]): Stream[(Option[A], Option[B])] = zipWithAll(s2)((_, _))

        def zipWithAll[B, C](s2: Stream[B])(f: (Option[A], Option[B]) => C): Stream[C] = Stream.unfold((this, s2)) {
            case (Empty, Empty) => None
            case (Cons(h, t), Empty) => Some(f(Some(h()), Option.empty[B]) -> (t(), Stream.empty[B]))
            case (Empty, Cons(h, t)) => Some(f(Option.empty[A], Some(h())) -> (Stream.empty[A] -> t()))
            case (Cons(h1, t1), Cons(h2, t2)) => Some(f(Some(h1()), Some(h2())) -> (t1() -> t2()))
        }

        def startsWith[A](s: Stream[A]): Boolean = zipAll(s).takeWhile(_._2.isDefined) forAll {
            case (h, h2) => h == h2
        }

        // returns the Stream of suffixes of a given Stream, starting with the original Stream
        def tails: Stream[Stream[A]] = Stream.unfold(this) {
            case Empty => None
            case s1 => Some((s1, s1 drop 1))
        } append Stream(Stream.empty)

        // the function scanRight, which is like a foldRight that returns a stream of the intermediate results
        def scanRight[B](z: B)(f: (A, => B) => B): Stream[B] = foldRight((z, Stream(z)))((a, p0) => {
            lazy val p1 = p0
            val b2 = f(a, p1._1)
            (b2, Stream.cons(b2, p1._2))
        })._2
    }

    case object Empty extends Stream[Nothing]
    case class Cons[+A](h: () => A, t: () => Stream[A]) extends Stream[A]

    object Stream {
        def cons[A](hd: => A, t1: => Stream[A]): Stream[A] = {
            lazy val head = hd
            lazy val tail = t1
            Cons(() => head, () => tail)
        }

        def empty[A]: Stream[A] = Empty

        def apply[A](as: A*): Stream[A] = if (as.isEmpty) empty else cons(as.head, apply(as.tail: _*))

        // an infinite Stream of 1s
        val ones: Stream[Int] = Stream.cons(1, ones)

        // generalize ones slightly to the function constant, which returns an infinite Stream of a given value:
        def constant[A](a: A): Stream[A] = {
            lazy val tail: Stream[A] = Cons(() => a, () => tail)
            tail
        }

        // Write a function that generates an infinite stream of integers, starting
        // from n, then n + 1, n + 2, and so on.
        def from(n: Int): Stream[Int] = cons(n, from(n + 1))

        // a function fibs that generates the infinite stream of Fibonacci numbers: 0, 1, 1, 2, 3, 5, 8...
        val fibs: Stream[Int] = {
            def go(f0: Int, f1: Int): Stream[Int] = cons(f0, go(f1, f0 + f1))
            go(0, 1)
        }

        // write a more general stream-building function: unfold which takes an initial state,
        // and a function for building both the next state and the next value in the stream to be generated:
        def unfold[A, S](z: S)(f: S => Option[(A, S)]): Stream[A] = f(z) match {
            case Some((h, s)) => cons(h, unfold(s)(f))
            case None => empty
        }

        val fibsViaUnfold: Stream[Int] = unfold((0, 1)) { case (f0, f1) => Some((f0, (f1, f0 + f1)))}

        def fromViaUnfold(n: Int): Stream[Int] = unfold(n)(n => Some((n, n + 1)))

        def constantViaUnfold[A](a: A): Stream[A] = unfold(a)(_ => Some((a, a)))

        val onesViaUnfold: Stream[Int] = unfold(1)(_ => Some((1, 1)))
    }

    def main(args: Array[String]): Unit = {
        val s = Stream(1, 2, 3)
        assert(s.toList == List(1, 2, 3))
        assert(s.take(2).toList == List(1, 2))

        val s1 = Stream(1, 2, 3, 4, 5)
        assert(s1.drop(2).toList == List(3, 4, 5))
        assert(s1.takeWhile((x: Int) => x < 3).toList == List(1, 2))
        assert(s1.takeWhile((x: Int) => x < 0).toList == Nil)

        val s2 = Stream("a", "b", "c")
        assert(!s1.forAll((x: Int) => x % 2 == 0))
        assert(s2.forAll((x: String) => x.nonEmpty))

        val startingPoint = Stream(1, 2, 3, 4).map(_ + 10).filter(_ % 2 == 0).toList
        val step1 = Stream.cons(1 , Stream(2, 3, 4).map(_ + 10)).filter(_ % 2 == 0).toList
        val step2 = Stream(2, 4).map(_ + 10).filter(_ % 2 == 0).toList
        val step3 = Stream.cons(12, Stream(4).map(_ + 10)).filter(_ % 2 == 0).toList
        val step4 = 12 :: Stream(3, 4).map(_ + 10).filter(_ % 2 == 0).toList
        val step5 = 12 :: Stream.cons(3, Stream(4).map(_ + 10)).filter(_ % 2 == 0).toList
        val step6 = 12 :: Stream(4).map(_ + 10).filter(_ % 2 == 0).toList
        val step7 = 12 :: Stream.cons(14, Stream[Int]().map(_ + 10)).filter(_ % 2 == 0).toList
        val step8 = 12 :: 14 :: Stream[Int]().map(_ + 10).filter(_ % 2 == 0).toList
        val finalStep = 12 :: 14 :: List()
        assert(startingPoint == step1)
        assert(step1 == step2)
        assert(step2 == step3)
        assert(step3 == step4)
        assert(step4 == step5)
        assert(step5 == step6)
        assert(step6 == step7)
        assert(step7 == step8)
        assert(step8 == finalStep)

        assert(Stream.ones.take(5).toList == List(1, 1, 1, 1, 1))
        assert(Stream.ones.exists(_ % 2 != 0))
        assert(Stream.ones.map(_ + 1).exists(_ % 2 == 0))
        assert(!Stream.ones.forAll(_ != 1))

        assert(Stream.from(100).take(5).toList == List(100, 101, 102, 103, 104))

        assert(Stream.fibs.take(7).toList == List(0, 1, 1, 2, 3, 5, 8))
        assert(Stream.fibsViaUnfold.take(7).toList == List(0, 1, 1, 2, 3, 5, 8))
        assert(Stream.fromViaUnfold(100).take(5).toList == List(100, 101, 102, 103, 104))
        assert(Stream.onesViaUnfold.take(10).toList == List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1))
        assert(s1.takeViaUnfold(5).toList == List(1, 2, 3, 4, 5))

        assert(s.tails.toList.map(_.toList) == List(List(1, 2, 3), List(2, 3), List(3), List()))

        assert(Stream(1, 2, 3).scanRight(0)(_ + _).toList == List(6, 5, 3, 0))
    }
}
