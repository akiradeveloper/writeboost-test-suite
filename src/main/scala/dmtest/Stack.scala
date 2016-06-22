package dmtest

trait Stack {
  def path: String // e.g. /dev/sdb
  def dev = BlockDevice(path)
  def withDev[A](f: BlockDevice => A): A = {
    val res = f(dev)
    purge
    res
  }
  def exists: Boolean = dev.size.unwrap > 0

  protected def terminate(): Unit
  protected def subsidiaries: Iterable[Stack] = Iterable.empty

  private var _lock = false
  def lock(): Unit = { _lock = true }
  def unlock(): Unit = { _lock = false }

  final def purge(): Unit = {
    if (_lock) {
      // TODO log
      return
    }
    terminate()
    subsidiaries.foreach(_.purge())
  }
}
