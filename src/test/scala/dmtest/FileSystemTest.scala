package dmtest

import java.nio.ByteBuffer
import java.nio.file.Files

import dmtest.fs.{EXT4, XFS}
import dmtest.stack.Loopback
import org.scalatest.FunSuite

class FileSystemTest extends FunSuite {
  test("mkfs.xfs") {
    Loopback.S(Sector.M(16)) { s =>
      XFS.format(s)
      XFS.Mount(s) { mp =>
        val f = mp.resolve("a")
        Files.createFile(f)
      }
    }
  }
  test("mkfs.ext4") {
    Loopback.S(Sector.M(16)) { s =>
      EXT4.format(s)
      EXT4.Mount(s) { mp =>
        val f = mp.resolve("b")
        Files.createFile(f)
      }
    }
  }
}
