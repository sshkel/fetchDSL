package net.devslash.pipes

import kotlinx.coroutines.runBlocking
import net.devslash.HttpResponse
import net.devslash.ListRequestData
import net.devslash.mustGet
import net.devslash.util.getBasicRequest
import net.devslash.util.requestDataFromList
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.net.URI

internal class PipeTest {

  @Test
  fun testPipeStartsEmpty() = runBlocking {
    val pipe = Pipe<String> { _, _ -> listOf(ListRequestData(listOf("A", "B"))) }

    assertThat(pipe.getDataForRequest(), nullValue())
  }

  @Test
  fun testPipeSingleCase() = runBlocking {
    val pipe = Pipe<String> { r, _ -> listOf(ListRequestData(listOf(String(r.body)))) }

    pipe.accept(
      getBasicRequest(),
      HttpResponse(URI("http://a"), 200, mapOf(), "result".toByteArray()),
      requestDataFromList(listOf())
    )

    val data = pipe.getDataForRequest()!!
    assertThat(data, not(nullValue()))
    assertThat(data.mustGet<List<String>>()[0], equalTo("result"))
    assertThat(pipe.getDataForRequest(), nullValue())
  }

  @Test
  fun testPipeCanReturnMultipleResults() = runBlocking {
    val vals = listOf("a", "b", "c")
    val pipe = Pipe<String> { _, _ -> vals.map { ListRequestData(listOf(it)) } }
    pipe.accept(
      getBasicRequest(),
      HttpResponse(URI("http://a"), 200, mapOf(), byteArrayOf()),
      requestDataFromList(listOf())
    )

    vals.forEach {
      assertThat(pipe.getDataForRequest()!!.mustGet<List<String>>()[0], equalTo(it))
    }
  }

  @Test
  fun testPipeAcceptsMultipleAndReturnsInOrder() = runBlocking {
    val pipe = Pipe<String> { r, _ -> listOf(ListRequestData(listOf(String(r.body)))) }
    pipe.accept(
      getBasicRequest(),
      HttpResponse(URI("http://a"), 200, mapOf(), "a".toByteArray()),
      requestDataFromList(listOf())
    )
    pipe.accept(
      getBasicRequest(),
      HttpResponse(URI("http://a"), 200, mapOf(), "b".toByteArray()),
      requestDataFromList(listOf())
    )
    pipe.accept(
      getBasicRequest(),
      HttpResponse(URI("http://a"), 200, mapOf(), "c".toByteArray()),
      requestDataFromList(listOf())
    )

    val values = listOf("a", "b", "c")
    values.forEach {
      val data = pipe.getDataForRequest()!!
      val repl = data.mustGet<List<String>>()
      assertThat(repl[0], equalTo(it))
    }

    assertThat(pipe.getDataForRequest(), nullValue())
  }
}
