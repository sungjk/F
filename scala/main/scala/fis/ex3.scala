package fis

/**
  * Created by jeremy on 03/05/2019.
  */
object ex3 {
    sealed trait List[+A]
    case object Nil extends List[Nothing]
    case class Cons[+A](head: A, tail: List[A]) extends List[A]

    object List {
        def tail[A](l: List[A]): List[A] = l match {
            case Nil => sys.error("tail of empty list")
            case Cons(_, t) => t
        }

        def setHead[A](l: List[A], h: A): List[A] = l match {
            case Nil => sys.error("setHead on empty list")
            case Cons(_, t) => Cons(h, t)
        }

        def drop[A](l: List[A], n: Int): List[A] = if (n <= 0) l else l match {
            case Nil => Nil
            case Cons(_, t) => drop(t, n - 1)
        }

        def dropWhile[A](l: List[A], f: A => Boolean): List[A] = l match {
            case Cons(h, t) if f(h) => dropWhile(t, f)
            case _ => l
        }

        def init[A](l: List[A]): List[A] = l match {
            case Nil => sys.error("init of empty list")
            case Cons(_, Nil) => Nil
            case Cons(h, t) => Cons(h, init(t))
        }

        def foldLeft[A, B](l: List[A], z: B)(f: (B, A) => B): B = l match {
            case Nil => z
            case Cons(h, t) => foldLeft(t, f(z, h))(f)
        }

        def foldRight[A, B](l: List[A], z: B)(f: (A, B) => B): B = l match {
            case Nil => z
            case Cons(h, t) => foldRight(t, f(h, z))(f)
        }

        def sum3(l: List[Int]) = foldLeft(l, 0)(_ + _)
        def product3(l: List[Double]) = foldLeft(l, 1.0)(_ * _)
        def length2[A](l: List[A]): Int = foldLeft(l, 0)((acc, h) => acc + 1)

//        def reverse[A](l: List[A]): List[A] = foldLeft(l, List[A]())((acc, h) => Cons(h, acc))

        def append[A](l: List[A], r: List[A]): List[A] = foldRight(l, r)(Cons(_, _))

        def concat[A](l: List[List[A]]): List[A] = foldRight(l, Nil: List[A])(append)

        def addPairwise(a: List[Int], b: List[Int]): List[Int] = (a, b) match {
            case (Nil, _) => Nil
            case (_, Nil) => Nil
            case (Cons(h1, t1), Cons(h2, t2)) => Cons(h1 + h2, addPairwise(t1, t2))
        }

        def zipWith[A, B, C](a: List[A], b: List[B])(f: (A, B) => C): List[C] = (a, b) match {
            case (Nil, _) => Nil
            case (_, Nil) => Nil
            case (Cons(h1, t1), Cons(h2, t2)) => Cons(f(h1, h2), zipWith(t1, t2)(f))
        }

        @annotation.tailrec
        def startsWith[A](l: List[A], prefix: List[A]): Boolean = (l, prefix) match {
            case (_, Nil) => true
            case (Cons(h, t), Cons(h2, t2)) if h == h2 => startsWith(t, t2)
            case _ => false
        }

        @annotation.tailrec
        def hasSubsequence[A](sup: List[A], sub: List[A]): Boolean = sup match {
            case Nil => sub == Nil
            case _ if startsWith(sup, sub) => true
            case Cons(h, t) => hasSubsequence(t, sub)
        }
    }



    sealed trait Tree[+A]
    case class Leaf[A](value: A) extends Tree[A]
    case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]

    object Tree {
        def size[A](t: Tree[A]): Int = t match {
            case Leaf(_) => 1
            case Branch(l, r) => 1 + size(l) + size(r)
        }

        def maximum(t: Tree[Int]): Int = t match {
            case Leaf(n) => n
            case Branch(l, r) => maximum(l) max maximum(r)
        }

        def depth[A](t: Tree[A]): Int = t match {
            case Leaf(_) => 0
            case Branch(l, r) => 1 + (depth(l) max depth(r))
        }

        def map[A, B](t: Tree[A])(f: A => B): Tree[B] = t match {
            case Leaf(a) => Leaf(f(a))
            case Branch(l, r) => Branch(map(l)(f), map(r)(f))
        }

        def fold[A, B](t: Tree[A])(f: A => B)(g: (B, B) => B): B = t match {
            case Leaf(a) => f(a)
            case Branch(l, r) => g(fold(l)(f)(g), fold(r)(f)(g))
        }

        def sizeViaFold(t: Tree[Int]): Int = fold(t)(_ => 1)(1 + _ + _)

        def maximumViaFold(t: Tree[Int]): Int = fold(t)(a => a)(_ max _)

        def depthViaFold(t: Tree[Int]): Int = fold(t)(_ => 0)((d1, d2) => 1 + (d1 max d2))

        def mapViaFold[A, B](t: Tree[A])(f: A => B): Tree[B] = fold(t)(a => Leaf(f(a)): Tree[B])(Branch(_, _))
    }

    def main(args: Array[String]): Unit = {
//        assert(List.tail(List(1, 2, 3)) == List(2, 3))
//        assert(List.tail(List(1)) == Nil)
//        assert(List.setHead(List(1, 2, 3), 3) == List(3, 2, 3))
//        assert(List.setHead(List("a", "b"), "c") == List("c", "b"))
//
//        assert(List.drop(List(1, 2, 3), 1) == List(2, 3))
//        assert(List.drop(List(1, 2, 3), 0) == List(1, 2, 3))
//        assert(List.drop(List("a", "b"), 2) == Nil)
//        assert(List.drop(List(1, 2), 3) == Nil)
//        assert(List.drop(Nil, 1) == Nil)
//
//        assert(List.dropWhile(List(1, 2, 3), (x: Int) => x < 2) == List(2, 3))
//        assert(List.dropWhile(List(1, 2, 3), (x: Int) => x > 2) == List(1, 2, 3))
//        assert(List.dropWhile(List(1, 2, 3), (x: Int) => x > 0) == Nil)
//        assert(List.dropWhile(Nil, (x: Int) => x > 0) == Nil)
//
//        assert(List.init(List(1, 2, 3)) == List(1, 2))
//        assert(List.init(List(1)) == Nil)
//
//        assert(List.foldRight(Cons(1, Cons(2, Cons(3, Nil))), 0)((x, y) => x + y) == 6)
//        assert(1 + List.foldRight(Cons(2, Cons(3, Nil)), 0)((x, y) => x + y) == 6)
//        assert(1 + 2 + List.foldRight(Cons(3, Nil), 0)((x, y) => x + y) == 6)
//        assert(1 + 2 + 3 + List.foldRight(Nil , 0)((x, y) => x + y) == 6)
//        assert(1 + 2 + 3 + 0 == 6)
//
//        assert(List.foldRight(List(1, 2, 3), Nil: List[Int])(Cons(_, _)) == List(1, 2, 3))
//
//        def l = List(1, 2, 3, 4, 5)
//        def length[A](as: List[A]): Int = List.foldRight(as, 0)((_, acc) => acc + 1)
//        assert(length(l) == 5)
//
//        def listInts = List(1, 2, 3, 4, 5)
//        def listDoubles = List(1.0, 2.0, 3.0)
//        assert(List.sum3(listInts) == 15)
//        assert(List.product3(listDoubles) == 6.0)
//        assert(List.length2(listInts) == 5)

        def t = Branch(Branch(Leaf(1), Leaf(2)), Leaf(3))
        assert(Tree.size(t) == 5)
        assert(Tree.depth(t) == 2)
        assert(Tree.map(t)(_ * 2) == Branch(Branch(Leaf(2), Leaf(4)), Leaf(6)))
        assert(Tree.sizeViaFold(t) == 5)
        assert(Tree.maximumViaFold(t) == 3)
        assert(Tree.depthViaFold(t) == 2)
        assert(Tree.mapViaFold(t)(_ % 2 == 0) == Branch(Branch(Leaf(false), Leaf(true)), Leaf(false)))

    }
}
