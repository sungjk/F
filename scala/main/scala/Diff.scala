/**
  * Created by Jeremy on 2019-08-22.
  */
trait Diff[CC[_], T] {
    def patch(coll: CC[T]): CC[T]

    def map[U](f: T => U): Diff[CC, U]
}

trait Diffable[CC[_]] {
    def diff[T](left: CC[T], right: CC[T]): Diff[CC, T]

    def empty[T]: CC[T]
}

object Diffable {
    private case class SetDiff[T](add: Set[T], remove: Set[T]) extends Diff[Set, T] {
        override def patch(coll: Set[T]): Set[T] =
            coll ++ add -- remove

        override def map[U](f: T => U): Diff[Set, U] =
            SetDiff(add.map(f), remove.map(f))

        override def toString: String = s"Diff(+$add, -$remove)"
    }

    private case class SeqDiff[T](limit: Int, insert: Map[Int, T]) extends Diff[Seq, T] {
        override def patch(coll: Seq[T]): Seq[T] = {
            val out = new Array[Any](limit)
            coll.copyToArray(out, 0, limit)

            for ((i, v) <- insert)
                out(i) = v

            out.toSeq.asInstanceOf[Seq[T]]
        }

        override def map[U](f: T => U): Diff[Seq, U] =
            SeqDiff(limit, insert.map { case (k, v) => (k, f(v)) })
    }

    implicit val ofSet: Diffable[Set] = new Diffable[Set] {
        override def diff[T](left: Set[T], right: Set[T]): Diff[Set, T] =
            SetDiff(right -- left, left -- right)

        override def empty[T]: Set[T] =
            Set.empty
    }

    implicit val ofSeq: Diffable[Seq] = new Diffable[Seq] {
        override def diff[T](left: Seq[T], right: Seq[T]): Diff[Seq, T] =
            if (left.length < right.length) {
                val SeqDiff(_, insert) = diff(left, right.take(left.length))
                SeqDiff(right.length, insert ++ ((left.length until right.length) map { i =>
                    i -> right(i)
                }))
            } else if (left.length > right.length) {
                diff(left.take(right.length), right)
            } else {
                val insert = for (((x, y), i) <- left.zip(right).zipWithIndex if x != y)
                    yield i -> y
                SeqDiff(left.length, insert.toMap)
            }

        override def empty[T]: Seq[T] = Seq.empty
    }

    def diff[CC[_]: Diffable, T](left: CC[T], right: CC[T]): Diff[CC, T] =
        implicitly[Diffable[CC]].diff(left, right)

    def empty[CC[_]: Diffable, T]: CC[T] =
        implicitly[Diffable[CC]].empty
}
