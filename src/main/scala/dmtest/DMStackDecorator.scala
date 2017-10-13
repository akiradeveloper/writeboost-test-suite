package dmtest

trait DMStackDecorator[S <: DMStackDecorator[S]] extends DMStack {
  def table: DMTable[S]
  def delegate: DMStack
  def onReloaded(): Unit = {}
  override def dm = delegate.dm

  dm.reload(table.line)
  onReloaded()
}
