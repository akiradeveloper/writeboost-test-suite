package dmtest

trait DMStackDecorator[S <: DMStackDecorator[S]] extends DMStack {
  def table: DMTable[S]
  def delegate: DMStack
  override def dm = delegate.dm
  override def lock = delegate.lock
  override def unlock = delegate.unlock

  dm.reload(table.line)
}
