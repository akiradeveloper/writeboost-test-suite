package dmtest

case class EmptyStack() extends DMStack {
  val dm = new DMState(name = RandName.alloc)
  override protected def subsidiaries: Iterable[Stack] = Seq.empty

  dm.create()
}
