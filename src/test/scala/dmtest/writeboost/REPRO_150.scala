package dmtest.writeboost

import java.io.File
import java.nio.file.{Paths, Path, Files}

import dmtest._
import dmtest.stack._

class REPRO_150 extends DMTestSuite {
  // not reproduced
  ignore("with filesystem") {
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
            Shell.runScript {
              s"""
                  cd ${mnt};
                  cat ./* > /dev/null;
                  cd -;
              """.stripMargin
            }
            Kernel.dropCaches
            s.status
            Shell.runScript {
              s"""
                  cd ${mnt};
                  sha1sum ./* > /tmp/sha1sum_new.out;
                  cd -;
              """.stripMargin
            }
            s.status
          }
        }
      }
    }
  }

  test("with device file") {
    slowDevice(Sector.M(100)) { backing =>
      fastDevice(Sector.M(10)) { caching =>
        Shell.runScript { s"""scrub -pcustom\"DATA\" -S ${backing.bdev.path}""" }
        Shell.runScript { s"""scrub -pcustom\"CACHE\" -S ${caching.bdev.path}""" }

        val baseline = TempFile.alloc()
        Shell.runScript { s"dd if=${backing.bdev.path} of=${baseline} bs=1M" }

        Writeboost.sweepCaches(caching)
        Shell("sync")

        val o1 = TempFile.alloc()
        val o2 = TempFile.alloc()
        val o3 = TempFile.alloc()
        Writeboost.Table(backing, caching, Map("write_around_mode" -> 1, "read_cache_threshold" -> 127)).create { s =>
          Shell(s"dd if=${s.bdev.path} of=${o1} bs=1M")
          Kernel.dropCaches
          Shell(s"dd if=${s.bdev.path} of=${o2} bs=1M")
          Kernel.dropCaches
          Shell(s"dd if=${s.bdev.path} of=${o3} bs=1M")
        }

        val h_baseline = Shell.sync(s"sha1sum ${baseline}")
        val h_o1 = Shell.sync(s"sha1sum ${o1}")
        assert(h_o1 === h_baseline)
        val h_o2 = Shell.sync(s"sha1sum ${o2}")
        assert(h_o2 === h_baseline)
        val h_o3 = Shell.sync(s"sha1sum ${o3}")
        assert(h_o3 === h_baseline)
      }
    }
  }
}
