package dmtest

import java.io.{FileWriter, File}

class DMState(val name: String) {
  def create() = Shell(s"dmsetup create ${name} --notable")
  def remove() = Shell(s"dmsetup remove ${name}")
  def reload(table: String) = {
    val fn = s"/tmp/${RandName.alloc}"
    val f = new File(fn); f.deleteOnExit()
    val writer = new FileWriter(fn)
    writer.write(table)
    writer.close()

    Shell(s"dmsetup reload ${name} ${fn}")
  }
  def suspend() = Shell(s"dmsetup suspend ${name}")
  def resume() = Shell(s"dmsetup resume ${name}")
  def table() = Shell(s"dmsetup table ${name}")
}
