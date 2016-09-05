package dmtest.writeboost

import java.io.File
import java.nio.file.{Paths, Path, Files}

import dmtest._
import dmtest.stack._

class REPRO_150 extends DMTestSuite {
  test("read-caching works") {
    slowDevice(Sector.G(10)) { backing =>
      fastDevice(Sector.G(1)) { caching =>
        fs.XFS.format(backing)
        fs.XFS.Mount(backing) { mnt =>
          val scriptDir = mnt.resolve("scripts")
          Files.createDirectories(scriptDir)
          val scriptResource = new File(getClass.getClassLoader.getResource("REPRO_150_script.sh").getFile).toPath
          val script = scriptDir.resolve("script.sh")
          Files.copy(scriptResource, script)
          Shell.at(scriptDir) { "sh script.sh" }
          // delete script/ otherwise later sha1sum fails
          Files.delete(script)
          Files.delete(scriptDir)

          Shell("sync")
        }

        Writeboost.sweepCaches(caching)
        Shell("sync")
        Writeboost.Table(backing, caching, Map("read_cache_threshold" -> 127)).create { s =>
          s.status
          fs.XFS.Mount(s) { mnt =>
            Shell.at(mnt) { "cat *" } // stage
            Kernel.dropCaches
            s.status
            Shell.at(mnt) { "sha1sum *" } // re-read
            s.status
          }
        }
      }
    }
  }
}
