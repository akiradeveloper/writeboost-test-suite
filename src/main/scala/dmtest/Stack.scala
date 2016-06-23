package dmtest

import java.nio.file.Path

trait Stack {
  def path: Path // e.g. /dev/sdb

  private var _lock = false
  def lock(): Unit = { _lock = true }
  def unlock(): Unit = { _lock = false }

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
    if (_lock) {
      // TODO log
      return
    }
    terminate()
    subsidiaries.foreach(_.purge())
  }
}
