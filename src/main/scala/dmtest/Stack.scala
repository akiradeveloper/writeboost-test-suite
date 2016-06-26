package dmtest

import java.nio.file.Path

trait Stack {
  protected def path: Path // e.g. /dev/sdb

  def bdev: BlockDevice = BlockDevice(path)

  // TODO should use counting because same stack can be nested (although quite uncommon)
  // stack1 { s =>
  //   s {
  //   }
  // }
  private var locked = false
  def lock(): Unit = { locked = true }
  def unlock(): Unit = { locked = false }

  // lock is a mechanism to manage the lifetime of stacks.
  // stack1 {
  //   stack2(stack1) {
  //   } // stack1 isn't removed because it's protected
  // } // stack1 is removed here
  def apply[A](f: this.type => A): A = {
    val resource: this.type = this
    lock
    try {
      f(resource)
    } finally {
      unlock
      purge
    }
  }
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
  protected def subStacks: Iterable[Stack] = Iterable.empty

  final def purge(): Unit = {
    if (locked) {
      logger.debug(s"${path} is locked")
      return
    }
    terminate()
    subStacks.foreach(_.purge())
  }
}
