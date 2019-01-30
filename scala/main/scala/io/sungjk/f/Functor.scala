package io.sungjk.f

// Functors: uniform action over a parameterized type, generalizing the map function on lists.
trait Functor[F[_]] {
    def map[A, B](fa: F[A])(f: A => B): F[B]

    // List로 받으면 unzip 같은 기능
    def distribute[A, B](fab: F[(A, B)]): (F[A], F[B]) = (map(fab)(_._1), map(fab)(_._2))

    def codistribute[A, B](e: Either[F[A], F[B]]): F[Either[A, B]] = e match {
        case Left(fa) => map(fa)(Left(_))
        case Right(fb) => map(fb)(Right(_))
    }

    // TODO lift 정의하기
}

object Functor {
    val listFunctor: Functor[List] = new Functor[List] {
        override def map[A, B](fa: List[A])(f: A => B): List[B] = fa map f
    }

    val optionFunctor: Functor[Option] = new Functor[Option] {
        override def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa match {
            case None => None
            case Some(a) => Some(f(a))
        }
    }
}
