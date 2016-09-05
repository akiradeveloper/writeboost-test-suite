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
          val script = new File(getClass.getClassLoader.getResource("REPRO_150_script.sh").getFile).toPath
          Files.copy(script, scriptDir.resolve("script.sh"))
          Shell.at(scriptDir) { "sh script.sh" }
          Shell.at(scriptDir) { "ls"}
        }
      }
    }
  }
}
