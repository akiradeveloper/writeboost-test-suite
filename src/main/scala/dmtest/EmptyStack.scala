package dmtest

case class EmptyStack() extends DMStack {
  val dm = new DMState(name = RandName.alloc)

  dm.create()
}
