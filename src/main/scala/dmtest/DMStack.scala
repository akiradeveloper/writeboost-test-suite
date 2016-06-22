package dmtest

trait DMStack extends Stack {
  def dm: DMState
  override def path = s"/dev/mapper/${dm.name}"
  def reload[S <: DMStackDecorator[S]](table: DMTable[S]): S = {
    table.f(this)
  }
  def terminate = {
    // dmsetup remove
  }
}
