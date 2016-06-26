package dmtest

import scala.sys.process._

object Kernel {
  def drop_caches = Shell("echo 3" #> "/proc/sys/vm/drop_caches")
}
