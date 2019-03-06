package fis

import scala.util.{Success, Try}

/**
  * Created by jeremy on 03/05/2019.
  */
object ex4 {
    sealed trait Option[+A] {
        def map[B](f: A => B): Option[B] = this match {
            case None => None
            case Some(a) => Some(f(a))
        }

        def flatMap[B](f: A => Option[B]): Option[B] = this match {
            case None => None
            case Some(a) => f(a)
        }

        def flatMap2[B](f: A => Option[B]): Option[B] = map(f) getOrElse None

        def getOrElse[B >: A](defaultValue: => B): B = this match {
            case None => defaultValue
            case Some(a) => a
        }

        def orElse[B >: A](ob: => Option[B]): Option[B] = this map (Some(_)) getOrElse ob

        def filter(f: A => Boolean): Option[A] = this match {
            case Some(a) if f(a) => this
            case _ => None
        }
    }
    case class Some[+A](get: A) extends Option[A]
    case object None extends Option[Nothing]
    object Option {
        def map2[A, B, C](a: Option[A], b: Option[B])(f: (A, B) => C): Option[C] =
            a flatMap { aa => b map { bb => f(aa, bb) }}

        def sequence[A](a: List[Option[A]]): Option[List[A]] = a match {
            case Nil => Some(Nil)
            case h :: t => h flatMap { hh => sequence(t) map { hh :: _ }}
        }

        def traverse[A, B](a: List[A])(f: A => Option[B]): Option[List[B]] = a match {
            case Nil => Some(Nil)
            case h :: t => map2(f(h), traverse(t)(f))(_ :: _)
        }

        def traverse_1[A, B](a: List[A])(f: A => Option[B]): Option[List[B]] =
            a.foldRight[Option[List[B]]](Some(Nil))((h, t) => map2(f(h), t)(_ :: _))

        def sequenceViaTraverse[A](a: List[Option[A]]): Option[List[A]] = traverse(a)(x => x)
    }


    sealed trait Either[+E, +A] {
        def map[B](f: A => B): Either[E, B] = this match {
            case Left(l) => Left(l)
            case Right(r) => Right(f(r))
        }

        def flatMap[EE >: E, B](f: A => Either[EE, B]): Either[EE, B] = this match {
            case Left(l) => Left(l)
            case Right(r) => f(r)
        }

        def orElse[EE >: E, AA >: A](b: => Either[EE, AA]): Either[EE, AA] = this match {
            case Left(_) => b
            case Right(r) => Right(r)
        }

        def map2[EE >: E, B, C](b: Either[EE, B])(f: (A, B) => C): Either[EE, C] =
            for {
                a <- this
                b1 <- b
            } yield f(a, b1)
    }
    case class Left[+E](value: E) extends Either[E, Nothing]
    case class Right[+A](value: A) extends Either[Nothing, A]
    object Either {
        def traverse[E, A, B](es: List[A])(f: A => Either[E, B]): Either[E, List[B]] = es match {
            case Nil => Right(Nil)
            case h :: t => (f(h) map2 traverse(t)(f))(_ :: _)
        }

        def sequence[E, A](es: List[Either[E, A]]): Either[E, List[A]] = traverse(es)(x => x)
    }


    trait Partial[+A, +B]
    case class PErrors[+A](get: Seq[A]) extends Partial[A, Nothing]
    case class PSuccess[+B](get: B) extends Partial[Nothing, B]


    def main(args: Array[String]): Unit = {
        case class Employee(name: String, department: String, manager: Option[String])
        def lookupByName(name: String): Option[Employee] = name match {
            case "Joe" => Some(Employee("Joe", "Finances", Some("Julie")))
            case "Mary" => Some(Employee("Mary", "IT", None))
            case "Izumi" => Some(Employee("Izumi", "IT", Some("Mary")))
            case _ => None
        }

        def getDepartment: Option[Employee] => Option[String] = _.map { _.department }
        assert(getDepartment(lookupByName("Joe")) == Some("Finances"))
        assert(getDepartment(lookupByName("Mary")) == Some("IT"))
        assert(getDepartment(lookupByName("Foo")) == None)

        def getManager: Option[Employee] => Option[String] = _.flatMap { _.manager }
        assert(getManager(lookupByName("Joe")) == Some("Julie"))
        assert(getManager(lookupByName("Mary")) == None)
        assert(getManager(lookupByName("Foo")) == None)

        def getManager2(employee: Option[Employee]): Option[String] = employee.flatMap(_.manager)
        assert(getManager2(lookupByName("Joe")).orElse(Some("Mr. CEO")) == Some("Julie"))
        assert(getManager2(lookupByName("Mary")).orElse(Some("Mr. CEO")) == Some("Mr. CEO"))
        assert(getManager2(lookupByName("Foo")).orElse(Some("Mr. CEO")) == Some("Mr. CEO"))

        assert(lookupByName("Joe").filter(_.department != "IT") == Some(Employee("Joe", "Finances", Some("Julie"))))
        assert(lookupByName("Mary").filter(_.department != "IT") == None)
        assert(lookupByName("Foo").filter(_.department != "IT") == None)

        assert(Option.sequence(List(Some(1), Some(2), Some(3))) == Some(List(1, 2, 3)))
        assert(Option.sequence(List(Some(1), Some(2), None)) == None)

        val list1 = List("1", "2", "3")
        val list2 = List("I", "II", "III", "IV")
        def parseInt(a: String): Option[Int] = Try(a.toInt) match {
            case Success(r) => Some(r)
            case _ => None
        }
        assert(Option.traverse(list1)(i => parseInt(i)) == Some(List(1, 2, 3)))
        assert(Option.traverse(list2)(i => parseInt(i)) == None)


        def lookupByNameViaEither(name: String): Either[String, Employee] = name match {
            case "Joe" => Right(Employee("Joe", "Finances", Some("Julie")))
            case "Mary" => Right(Employee("Mary", "IT", None))
            case "Izumi" => Right(Employee("Izumi", "IT", Some("Mary")))
            case _ => Left("Employee not found")
        }

        def getDepartment3: Either[String, Employee] => Either[String, String] = _.map { _.department }
        assert(getDepartment3(lookupByNameViaEither("Joe")) == Right("Finances"))
        assert(getDepartment3(lookupByNameViaEither("Mary")) == Right("IT"))
        assert(getDepartment3(lookupByNameViaEither("Foo")) == Left("Employee not found"))

        def getManager3(employee: Either[String, Employee]): Either[String, String] = employee.flatMap { e =>
            e.manager match {
                case Some(m) => Right(m)
                case _ => Left("Manager not found")
            }
        }
        assert(getManager3(lookupByNameViaEither("Joe")) == Right("Julie"))
        assert(getManager3(lookupByNameViaEither("Mary")) == Left("Manager not found"))
        assert(getManager3(lookupByNameViaEither("Foo")) == Left("Employee not found"))

        assert(getManager3(lookupByNameViaEither("Joe")).orElse(Right("Mr. CEO")) == Right("Julie"))
        assert(getManager3(lookupByNameViaEither("Mary")).orElse(Right("Mr. CEO")) == Right("Mr. CEO"))
        assert(getManager3(lookupByNameViaEither("Foo")).orElse(Right("Mr. CEO")) == Right("Mr. CEO"))


        def employeesShareDepartment(employeeA: Employee, employeeB: Employee) = employeeA.department == employeeB.department
        assert(lookupByNameViaEither("Joe").map2(lookupByNameViaEither("Mary"))(employeesShareDepartment) == Right(false))
        assert(lookupByNameViaEither("Mary").map2(lookupByNameViaEither("Izumi"))(employeesShareDepartment) == Right(true))
        assert(lookupByNameViaEither("Foo").map2(lookupByNameViaEither("Izumi"))(employeesShareDepartment) == Left("Employee not found"))


        val employees = List("Joe", "Mary")
        val employeesAndOutsources = employees :+ "Foo"
        assert(Either.traverse(employees)(lookupByNameViaEither) == Right(List(Employee("Joe", "Finances", Some("Julie")), Employee("Mary", "IT", None))))
        assert(Either.traverse(employeesAndOutsources)(lookupByNameViaEither) == Left("Employee not found"))


        val employees2 = List(lookupByNameViaEither("Joe"), lookupByNameViaEither("Mary"))
        val employeesAndOutsources2 = employees2 :+ lookupByNameViaEither("Foo")
        assert(Either.sequence(employees2) == Right(List(Employee("Joe", "Finances", Some("Julie")), Employee("Mary", "IT", None))))
        assert(Either.sequence(employeesAndOutsources2) == Left("Employee not found"))
    }
}
