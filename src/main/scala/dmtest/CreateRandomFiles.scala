package dmtest

import java.io.{FileOutputStream, FileWriter}
import java.nio.file.{StandardOpenOption, Files, Path}

class CreateRandomFiles(dir: Path) {
  var numDirectories = 1
  var numFiles = 1
  var numSize = 1
  def create(): Unit = {
    for (i <- 0 until numDirectories) {
      val dirPath = dir.resolve(i.toString)
      Files.createDirectory(dirPath)
      for (j <- 0 until numFiles) {
        val filePath = dirPath.resolve(j.toString)
        val buf = DataBuffer.random(numSize)
        val out = new FileOutputStream(filePath.toFile)
        out.write(buf.refArray)
        out.close()
      }
    }
  }
}
