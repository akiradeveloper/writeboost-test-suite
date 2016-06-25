package dmtest.stack

import dmtest.{Stack, DMStackDecorator, DMStack, DMTable}

object Writeboost {
  case class T(backingDev: Stack, cacheDev: Stack) extends DMTable[S] {
    override def f: (DMStack) => S = ???
    override def line: String = ???
  }
  case class S(delegate: DMStack, table: T) extends DMStackDecorator[S] {
    override def subsidiaries = Seq(table.backingDev, table.cacheDev)
  }
}
