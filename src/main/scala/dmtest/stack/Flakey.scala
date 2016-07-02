package dmtest.stack

import dmtest.{DMStack, DMTable, DMStackDecorator, Stack}

object Flakey {
  case class Table(backing: Stack, upInterval: Int, downInterval: Int) extends DMTable[Flakey] {
    override def f: (DMStack) => Flakey = (st: DMStack) => Flakey(st, this)
    override def line: String = s"0 ${backing.bdev.size} flakey ${backing.bdev.path} 0 ${upInterval} ${downInterval}"
  }
}
case class Flakey(delegate: DMStack, table: Flakey.Table) extends DMStackDecorator[Flakey]
