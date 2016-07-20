package dmtest

import dmtest.stack._
import dmtest.fs._

class CreateRandomFilesTest extends DMTestSuite {
  test("no error") {
    Memory(Sector.M(128)) { s =>
      EXT4.format(s)
      EXT4.Mount(s) { mp =>
        val crf = new CreateRandomFiles(mp)
        crf.numDirectories = 10
        crf.numFiles = 30
        crf.fileSize = 5
        crf.create()
      }
    }
  }
}
