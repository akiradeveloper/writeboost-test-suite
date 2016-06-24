package dmtest

import java.nio.file.Path

trait Stack {
  def path: Path // e.g. /dev/sdb

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
    lock
    val res = f(this)
    unlock

    purge
    res
  }
  def exists: Boolean = BlockDevice(path).size > Sector(0)

  protected def terminate(): Unit
  protected def subsidiaries: Iterable[Stack] = Iterable.empty

  final def purge(): Unit = {
    if (locked) {
      logger.debug(s"${path} is locked")
      return
    }
    terminate()
    subsidiaries.foreach(_.purge())
  }
}
