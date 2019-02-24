import Either
import Left
import Right
import flatMap
import map
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Created by jeremy on 08/17/2018.
 */
class EitherTests {

    @Test
    fun testLeft() {
        val l: Either<Int, Int> = Left(42)
        val expected = when (l) {
            is Left -> l.value - 1
            is Right -> l.value + 1
        }

        assertEquals(expected, 42 - 1)
    }

    @Test
    fun testRight() {
        val r: Either<Int, Int> = Right(42)
        val expected = when (r) {
            is Left -> r.value - 1
            is Right -> r.value + 1
        }

        assertEquals(expected, 42 + 1)
    }

    @Test
    fun testMap() {
        val l: Either<Int, Int> = Left(42)
        val r: Either<Int, Int> = Right(42)

        assertEquals(l.map { it - 1 }, Left(42))
        assertEquals(r.map { it + 1 }, Right(43))
    }

    @Test
    fun testFlatMap() {
        val l: Either<Int, Int> = Left(42)
        val r: Either<Int, Int> = Right(42)

        assertEquals(l.flatMap { Right(it - 1) }, Left(42))
        assertEquals(r.flatMap { Right(it + 1) }, Right(43))
    }

}
