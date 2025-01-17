package net.devslash.outputs

import net.devslash.*
import net.devslash.ReplacingString
import java.io.File

class WriteFile(
  private val fileName: String,
  private val out: OutputFormat = DefaultOutput()
) : BasicOutput {
  private val lock = Object()

  override fun accept(req: HttpRequest, resp: HttpResponse, data: RequestData) {
    synchronized(lock) {
      val f = File(data.visit(ReplacingString(fileName)))
      val output = out.accept(resp, data)
      if (output != null) {
        f.writeBytes(output)
      }
    }
  }
}
