package dmtest

import scala.sys.process._

object Kernel {
  def dropCaches = Shell("echo 3" #> "/proc/sys/vm/drop_caches")
}
