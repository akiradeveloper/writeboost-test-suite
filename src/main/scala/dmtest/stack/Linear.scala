package dmtest.stack

import java.nio.file.Path

import dmtest._

object Linear {
  case class T(backing: Stack, start: Sector, len: Sector) extends DMTable[S] {
    override def f: (DMStack) => S = ???
    override def line: String = ???
  }
  case class S(delegate: DMStack, table: T) extends DMStackDecorator[S] {
    override def subsidiaries = Seq(table.backing)
    override def terminate(): Unit = ???

    def start = table.start
    def len = table.len
  }
}
