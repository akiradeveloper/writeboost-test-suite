package dmtest

import java.nio.file.Files

import dmtest.fs.{EXT4, XFS}
import dmtest.stack.Memory
import org.scalatest.FunSuite

class FileSystemTest extends DMTestSuite {
  test("mkfs.xfs") {
    Memory(Sector.M(32)) { s =>
      XFS.format(s)
      XFS.Mount(s) { mp =>
        val f = mp.resolve("a")
        Files.createFile(f)
      }
    }
  }
  test("mkfs.ext4") {
    Memory(Sector.M(16)) { s =>
      EXT4.format(s)
      EXT4.Mount(s) { mp =>
        val f = mp.resolve("b")
        Files.createFile(f)
      }
    }
  }
  test("mkfs (inmem)") {
    Memory(Sector.M(32)) { s =>
      XFS.format(s)
      XFS.Mount(s) { mp =>
        val f = mp.resolve("a")
        Files.createFile(f)
      }
    }
  }
}
