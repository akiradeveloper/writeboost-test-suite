package dmtest.stack

import java.nio.file.Path

import dmtest._

object Linear {
  case class T(backing: Stack, start: Sector, len: Sector) extends DMTable[S] {
    override def f: (DMStack) => S = (st: DMStack) => S(st, this)
    override def line: String = s"0 ${len} linear ${backing.bdev.path} 0"
  }
  case class S(delegate: DMStack, table: T) extends DMStackDecorator[S] {
    override def subsidiaries = Seq(table.backing)
    def start = table.start
    def len = table.len
  }
}
