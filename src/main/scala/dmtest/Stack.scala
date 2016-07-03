package dmtest

import java.nio.file.Path

trait Stack {
  protected def path: Path // e.g. /dev/sdb

  def bdev: BlockDevice = BlockDevice(path)

  def apply[A](f: this.type => A): A = {
    val resource: this.type = this
    try {
      f(resource)
    } finally {
      terminate()
    }
  }
// experimental. can't be compiled
//  def acquire: this.type = {
//    lock
//    this
//  }
//  def release = { a: this.type =>
//    unlock
//    purge
//  }
//  def apply[A](f: this.type => A): A = Resource(acquire)(release)(f)
  def exists: Boolean = bdev.size > Sector(0)

  protected def terminate(): Unit
}
