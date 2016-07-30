package dmtest

import java.nio.file.Files

object DMState {
  case class Table(start: Sector, len: Sector, target: String, args: Array[String])
  case class Status(start: Sector, len: Sector, target: String, args: Array[String])
}

class DMState(val name: String) {
  import DMState._
  def create() = Shell(s"dmsetup create ${name} --notable")
  def remove() = Shell(s"dmsetup remove ${name}")
  def reload(table: String) = {
    logger.debug(s"reload: table=${table}")
    val f = TempFile.text(table)
    try {
      Shell(s"dmsetup reload ${name} ${f}")
    } finally {
      Files.deleteIfExists(f)
    }
  }
  def suspend() = Shell(s"dmsetup suspend ${name}")
  def resume() = Shell(s"dmsetup resume ${name}")
  def table: Table = {
    val line = Shell(s"dmsetup table ${name}").split(" ")
    Table(Sector(line(0).toLong), Sector(line(1).toLong), line(2), line.drop(3))
  }
  def status(): Status = {
    val line = Shell(s"dmsetup status ${name}").split(" ")
    Status(Sector(line(0).toLong), Sector(line(1).toLong), line(2), line.drop(3))
  }
  def message(msg: String) = Shell(s"dmsetup message ${name} 0 ${msg}")
}
