package fis

/**
  * Created by jeremy on 03/07/2019.
  */
object ex6 {
    trait RNG {
        def nextInt: (Int, RNG)
    }

    object RNG {
        case class Simple(seed: Long) extends RNG {
            override def nextInt: (Int, RNG) = {
                val newSeed = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL
                val nextRNG = Simple(newSeed)
                val n = (newSeed >>> 16).toInt
                (n, nextRNG)
            }
        }

        type Rand[+A] = RNG => (A, RNG)

        val int: Rand[Int] = _.nextInt

        def unit[A](a: A): Rand[A] = rng => (a, rng)

        def map[A, B](s: Rand[A])(f: A => B): Rand[B] = rng => {
            val (a, rng2) = s(rng)
            (f(a), rng2)
        }

        // write a function that uses RNG.nextInt to generate a random integer between 0 and Int.maxValue,
        // making sure to handle the corner case when nextInt returns Int.MinValue,
        // which doesn't have a non-negative counterpart
        def nonNegativeInt(rng: RNG): (Int, RNG) = {
            val (i, r) = rng.nextInt
            (if (i < 0) -(i + 1) else i, r)
        }

        // write a function to generate a Double between 0 and 1, excluding 1.
        // Note that we use Int.MaxValue to divide a random positive integer
        def double(rng: RNG): (Double, RNG) = {
            val (i, r) = nonNegativeInt(rng)
            (i / (Int.MaxValue.toDouble + 1), r)
        }

        // write functions that generate tuples of random values,
        // i.e.: an (Int, Double) pair, a (Double, Int) pair, and a (Double, Double, Double) 3-tuple
        def intDouble(rng: RNG): ((Int, Double), RNG) = {
            val (i, r1) = rng.nextInt
            val (d, r2) = double(r1)
            ((i, d), r2)
        }

        def doubleInt(rng: RNG): ((Double, Int), RNG) = {
            val ((i, d), r) = intDouble(rng)
            ((d, i), r)
        }

        def double3(rng: RNG): ((Double, Double, Double), RNG) = {
            val (d1, r1) = double(rng)
            val (d2, r2) = double(r1)
            val (d3, r3) = double(r2)
            ((d1, d2, d3), r3)
        }

        // write a function to generate a list of random integers
        def ints(count: Int)(rng: RNG): (List[Int], RNG) =
            if (count == 0) (List(), rng) else {
                val (x, r1) = rng.nextInt
                val (xs, r2) = ints(count - 1)(r1)
                (x :: xs, r2)
            }

        // implement map2 which takes two actions, ra and rb,
        // and a binary function f for combining their results, and returns a new action that combines them
        def map2[A, B, C](ra: Rand[A], rb: Rand[B])(f: (A, B) => C): Rand[C] = rng => {
            val (a, r1) = ra(rng)
            val (b, r2) = rb(r1)
            (f(a, b), r2)
        }

        // implement sequence for combining a List of transitions into a single transition
        def sequence[A](fs: List[Rand[A]]): Rand[List[A]] =
            fs.foldRight(unit(List[A]()))((f, acc) => map2(f, acc)(_ :: _))

        def flatMap[A, B](f: Rand[A])(g: A => Rand[B]): Rand[B] = rng => {
            val (a, r1) = f(rng)
            g(a)(r1)
        }

        def nonNegativeLessThan(n: Int): Rand[Int] = {
            flatMap(nonNegativeInt) { i =>
                val mod = i % n
                if (i + (n - 1) - mod >= 0) unit(mod) else nonNegativeLessThan(n)
            }
        }

        def _map[A, B](s: Rand[A])(f: A => B): Rand[B] =
            flatMap(s)(a => unit(f(a)))

        def _map2[A, B, C](ra: Rand[A], rb: Rand[B])(f: (A, B) => C): Rand[C] =
            flatMap(ra)(a => map(rb)(b => f(a, b)))

        def rollDie: Rand[Int] = map(nonNegativeLessThan(6))(_ + 0)
    }


    case class State[S, +A](run: S => (A, S)) {
        def map[B](f: A => B): State[S, B] = flatMap(a => State.unit(f(a)))

        def map2[B, C](sb: State[S, B])(f: (A, B) => C): State[S, C] = flatMap(a => sb.map(b => f(a, b)))

        def flatMap[B](f: A => State[S, B]): State[S, B] = State(s => {
            val (a, s1) = run(s)
            f(a).run(s1)
        })
    }

    object State {
        def unit[S, A](a: A): State[S, A] = State(s => (a, s))

        def sequence[S, A](sas: List[State[S, A]]): State[S, List[A]] =
            sas.foldRight(unit[S, List[A]](List()))((f, acc) => f.map2(acc)(_ :: _))

        def modify[S](f: S => S): State[S, Unit] = for {
            s <- get
            _ <- set(f(s))
        } yield ()

        def get[S]: State[S, S] = State(s => (s, s))

        def set[S](s: S): State[S, Unit] = State(_ => ((), s))
    }


    sealed trait Input
    case object Coin extends Input
    case object Turn extends Input
    // The machine has two inputs: you can insert a coin, or you can dispense candy by turning the knob.
    // It can be in one of two states: locked or unlocked.
    // It also tracks how many candies are left and how many coins it contains.
    case class Machine(locked: Boolean, candies: Int, coins: Int) {
        def interact(input: Input): Machine = input match {
            case _ if candies == 0 => this
            case Coin if locked => Machine(false, candies, coins + 1)
            case Turn if !locked => Machine(true, candies - 1, coins)
            case _ => this
        }
    }
    // - Inserting a coin into a locked machine will unlock it if there’s still any candy left.
    // - Turning the knob on an unlocked machine will cause it to dispense one candy and return to a locked state.
    // - Turning the knob on a locked machine or inserting a coin into an unlocked machine has no effect.
    // - A machine that’s out of candy ignores any kind of input.
    object Candy {
        // same with `interact` of Machine
        def update: Input => Machine => Machine =
            (i: Input) =>
                (s: Machine) =>
                    (i, s) match {
                        case (_, Machine(_, 0, _)) => s
                        case (Coin, Machine(false, _, _)) => s
                        case (Turn, Machine(true, _, _)) => s
                        case (Coin, Machine(true, candy, coin)) => Machine(false, candy, coin + 1)
                        case (Turn, Machine(false, candy, coin)) => Machine(true, candy - 1, coin)
                    }

        // operate the machine based on a list of inputs and return the number of coins and candies left in the machine.
        def simulateMachine(inputs: List[Input]): State[Machine, (Int, Int)] =
            for {
                _ <- State.sequence(inputs map (State.modify[Machine] _ compose update))
                s <- State.get
            } yield (s.coins, s.candies)
    }

    def main(args: Array[String]): Unit = {
        import RNG._

        val rng = Simple(42)
        val (result1, rng1) = nonNegativeInt(rng)
        val result2 = nonNegativeInt(rng1)._1
        assert(result1 >= 0)
        assert(result2 >= 0)
        assert(result1 != result2)

        val (double1, drng1) = double(rng)
        val double2 = double(drng1)._1
        assert(double1.toInt >= 0)
        assert(double2.toInt >= 0)
        assert(double1 != double2)

        val (list1, irng1) = ints(5)(Simple(47))
        val list2 = ints(5)(irng1)._1
        assert(list1.size == 5)
        assert(list1.headOption != list2)

        val rdouble: Rand[Double] = map(nonNegativeInt)(_ / (Int.MaxValue.toDouble + 1))
        val (ddouble1, drng2) = rdouble(rng)
        val ddouble2 = rdouble(drng2)._1
        assert(ddouble1.toInt >= 0)
        assert(ddouble2.toInt >= 0)
        assert(ddouble1 != ddouble2)

        val (nresult1, nrng1) = nonNegativeLessThan(10)(Simple(42))
        val nresult2 = nonNegativeLessThan(10)(nrng1)._1
        assert(nresult1 >= 0)
        assert(nresult1 < 10)
        assert(nresult2 >= 0)
        assert(nresult2  < 10)
        assert(nresult1 != nresult2)

        val (dice1, rrng1) = rollDie(Simple(42))
        val dice2 = rollDie(rrng1)._1
        assert(dice1 > 0)
        assert(dice1 < 6)
        assert(dice2 > 0)
        assert(dice2 < 6)
        assert(dice1 != dice2)


        import Candy._
        val inputCoin = List(Coin)
        val inputTurn = List(Turn)
        // Inserting a coin into a locked machine will cause it to unlock if there’s any candy left.
        val machine1 = Machine(true, 1, 0)
        assert(!simulateMachine(inputCoin).run(machine1)._2.locked)

        // Turning the knob on an unlocked machine will cause it to dispense candy and become locked.
        val machine2 = Machine(false, 1, 1)
        val m2Result = simulateMachine(inputTurn).run(machine2)
        assert(m2Result._2.locked)
        assert(m2Result._2.candies == 0)

        // Turning the knob on a locked machine or inserting a coin into an unlocked machine does nothing.
        assert(simulateMachine(inputTurn).run(machine1)._2.locked == machine1.locked)
        assert(simulateMachine(inputCoin).run(machine2)._2.locked == machine2.locked)

        // A machine that’s out of candy ignores all inputs.
        val machine3 = Machine(true, 0, 1)
        assert(simulateMachine(inputTurn).run(machine3)._2.locked == machine3.locked)
        assert(simulateMachine(inputCoin).run(machine3)._2.locked == machine3.locked)
    }
}
