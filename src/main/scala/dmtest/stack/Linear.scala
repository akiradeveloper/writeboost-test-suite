package dmtest.stack

import java.nio.file.Path

import dmtest._

object Linear {
  case class Table(backing: Stack, start: Sector, len: Sector) extends DMTable[Linear] {
    override def f: (DMStack) => Linear = (st: DMStack) => Linear(st, this)
    override def line: String = s"0 ${len} linear ${backing.bdev.path} 0"
  }
}
case class Linear(delegate: DMStack, table: Linear.Table) extends DMStackDecorator[Linear] {
  override def subStacks = Seq(table.backing)
  def start = table.start
  def len = table.len
}
