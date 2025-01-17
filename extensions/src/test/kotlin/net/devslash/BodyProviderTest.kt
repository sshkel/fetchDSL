package net.devslash

import net.devslash.util.getCall
import net.devslash.util.requestDataFromList
import org.junit.Assert.assertEquals
import org.junit.Test

internal class BodyProviderTest {

  @Test
  fun testEmpty() {
    val provider = getBodyProvider(
      getCall(), requestDataFromList(listOf("b", "d"))
    )

    assertEquals(EmptyBodyProvider::class, provider::class)
  }

  @Test
  fun testWithBody() {
    val provider = getBodyProvider(
      getCall(HttpBody(null, null, mapOf("a" to listOf("b"), "c" to listOf("d")), formIdentity, null, null, null, null)),
      requestDataFromList()
    )

    assertEquals(mapOf("a" to listOf("b"), "c" to listOf("d")), (provider as FormBody).get())
  }

  @Test
  fun testBodyWithReplaceableValues() {
    val provider = getBodyProvider(
      getCall(HttpBody("a=!1!&c=!2!", indexValueMapper, null, null, null, null, null, null)),
      requestDataFromList(listOf("b", "d"))
    )
    assertEquals("a=b&c=d", (provider as BasicBodyProvider).get())
  }

  @Test
  fun testParamsWithReplacement() {
    val provider = getBodyProvider(
      getCall(HttpBody(null, null, mapOf("a" to listOf("!1!"), "c" to listOf("!2!")), formIndexed, null, null, null, null)),
      requestDataFromList(listOf("b", "d"))
    )

    assertEquals(mapOf("a" to listOf("b"), "c" to listOf("d")), (provider as FormBody).get())
  }
}

