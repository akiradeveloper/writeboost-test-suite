package dmtest.stack

import java.nio.file.{Paths, Path}

import dmtest._

import scala.sys.process._

object Luks {
  case class S(backing: Stack) extends Stack {
    val keyFile = TempFile("aaaa")

    Shell(s"cryptsetup luksFormat ${backing.bdev.path} --key-file=${keyFile}")
    val name = RandName.alloc

    Shell(s"cryptsetup luksOpen ${backing.bdev.path} ${name} --key-file=${keyFile}")

    override protected def path: Path = Paths.get(s"/dev/mapper/${name}")

    override protected def terminate(): Unit = {
      Shell(s"cryptsetup luksClose ${name}")
    }
  }
}
