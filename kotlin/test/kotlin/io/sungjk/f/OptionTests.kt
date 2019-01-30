package io.sungjk.f

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Created by jeremy on 08/27/2018.
 */
class OptionTests {

    @Test
    fun testIsEmpty() {
        val some: Option<Int> = Some(42)
        val none: Option<Int> = None
        assertEquals(some.isEmpty(), false)
        assertEquals(none.isEmpty(), true)
    }

    @Test
    fun testIsDefined() {
        val some: Option<Int> = Some(42)
        val none: Option<Int> = None
        assertEquals(some.isDefined(), true)
        assertEquals(none.isDefined(), false)
    }

    @Test
    fun testMap() {
        val some: Option<Int> = Some(42)
        val none: Option<Int> = None
        assertEquals(some.map { it + 1 }, Some(43))
        assertEquals(none.map { it + 1 }, None)
    }

    @Test
    fun flatMap() {
        val some: Option<Int> = Some(42)
        val none: Option<Int> = None
        assertEquals(some.flatMap { Some(it + 1) }, Some(43))
        assertEquals(some.flatMap { None }, None)
        assertEquals(none.flatMap { Some(it + 1) }, None)
    }

    @Test
    fun testGet() {
        val some: Option<Int> = Some(42)
        assertEquals(some.get(), 42)

        val none : Option<Int> = None
        when (none) {
            is Some<Int> -> fail()
            is None -> assertEquals(none, None)
        }
    }

    @Test
    fun testOrElse() {
        val some: Option<Int> = Some(42)
        val none: Option<Int> = None
        assertEquals(some.orElse { Some(11) }.get(), 42)
        assertEquals(none.orElse { Some(11) }.get(), 11)
    }


    @Test
    fun testGetOrElse() {
        val some: Option<Int> = Some(42)
        val none: Option<Int> = None
        assertEquals(some.getOrElse { 11 }, 42)
        assertEquals(none.getOrElse { 11 }, 11)
    }

}
