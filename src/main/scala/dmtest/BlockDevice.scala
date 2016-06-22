package dmtest

case class BlockDevice(path: String) {
  def size: Sector = Sector(Shell.run(s"blockdev --getsize ${path}").toLong)
  override def toString = path
}
