package dmtest

import java.nio.file.Paths

trait DMStack extends Stack {
  def dm: DMState
  override def path = Paths.get(s"/dev/mapper/${dm.name}")
  def reload[S <: DMStackDecorator[S]](table: DMTable[S]): S = {
    val res = table.f(this)
    dm.suspend()
    dm.resume()
    res
  }
  def terminate = dm.remove()
}
