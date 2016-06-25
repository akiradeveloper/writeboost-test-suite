package dmtest

import java.io.{FileWriter, File}
import java.nio.file.Path

object TempFile {
  def apply(f: => String): Path = {
    val fn = s"/tmp/${RandName.alloc}"
    val file = new File(fn); file.deleteOnExit()
    val writer = new FileWriter(fn)
    writer.write(f)
    writer.close()
    file.toPath
  }
}
