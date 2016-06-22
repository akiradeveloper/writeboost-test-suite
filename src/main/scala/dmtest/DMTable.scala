package dmtest

trait DMTable[S <: DMStackDecorator[S]] {
  def f: DMStack => S
  def line: String
}
