package dmtest.stack

import dmtest._

object Writeboost {
  object Table {
    def parse(gTable: DMState.Table): Table = ???
  }
  case class Table(backingDev: Stack, cacheDev: Stack) extends DMTable[Writeboost] {
    override def f: (DMStack) => Writeboost = ???
    override def line: String = ???
  }
  object Status {
    def parse(gStatus: DMState.Status): Status = ???
  }
  case class Status()
}
case class Writeboost(delegate: DMStack, table: Writeboost.Table) extends DMStackDecorator[Writeboost] {
  override def subsidiaries = Seq(table.backingDev, table.cacheDev)
}
