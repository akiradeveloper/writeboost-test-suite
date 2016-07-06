import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

package object dmtest {
  val logger = Logger(LoggerFactory.getLogger("dmtest"))

  def reportTime[A](label: String)(f: => A): A = {
    val s = System.currentTimeMillis()
    val res = f
    val e = System.currentTimeMillis()
    logger.info(s"${label}: elapsed time=${e-s}[ms]")
    res
  }
}
