package dmtest

import java.nio.file.Paths

trait DMStack extends Stack {
  def dm: DMState
  override def path = Paths.get(s"/dev/mapper/${dm.name}")
  def reload[S <: DMStackDecorator[S]](table: DMTable[S]): S = {
    dm.suspend()
    val res = table.f(this) // does reload on creating the new stack
    dm.resume()
    assert(exists)
    res
  }
  def terminate = dm.remove()
}
