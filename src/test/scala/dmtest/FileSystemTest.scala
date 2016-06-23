package dmtest

import java.nio.ByteBuffer
import java.nio.file.Files

import dmtest.stack.Loopback
import org.scalatest.FunSuite

class FileSystemTest extends FunSuite {
  test("mkfs.xfs") {
    Loopback.S(Sector.M(1)) { s =>
      FileSystem.XFS(s) { mp =>
        val f = mp.resolve("a")
        Files.createFile(f)
      }
    }
  }
}
