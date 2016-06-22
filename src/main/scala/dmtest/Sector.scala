package dmtest

object Sector {
  def K(n: Long) = Sector(2 * n)
  def M(n: Long) = Sector((2<<10) * n)
  def G(n: Long) = Sector((2<<20) * n)
}

case class Sector(unwrap: Long) extends Ordered[Sector] {
  override def toString = unwrap.toString
  def toB: Long = unwrap << 9
  def +(that: Sector) = Sector(unwrap + that.unwrap)
  def -(that: Sector) = Sector(unwrap - that.unwrap)
  override def compare(that: Sector): Int = (unwrap - that.unwrap).toInt
}
