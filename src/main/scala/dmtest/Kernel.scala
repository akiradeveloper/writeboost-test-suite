package dmtest

import scala.sys.process._

object Kernel {
  def dropCaches = Shell("sysctl -w vm.drop_caches=3")
}
