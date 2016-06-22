package dmtest

object Sector {
  def K(n: Long) = Sector(2 * n)
  def M(n: Long) = Sector((2<<10) * n)
  def G(n: Long) = Sector((2<<20) * n)
}

case class Sector(unwrap: Long) {
  override def toString = unwrap.toString
  def toB: Long = unwrap << 9
}
