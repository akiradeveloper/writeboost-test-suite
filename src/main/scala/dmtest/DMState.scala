package dmtest

import java.io.{FileWriter, File}

class DMState(val name: String) {
  def create() = Shell(s"dmsetup create ${name} --notable")
  def remove() = Shell(s"dmsetup remove ${name}")
  def reload(table: String) = Shell(s"dmsetup reload ${name} ${TempFile(table)}")
  def suspend() = Shell(s"dmsetup suspend ${name}")
  def resume() = Shell(s"dmsetup resume ${name}")
  def table() = Shell(s"dmsetup table ${name}")
}
