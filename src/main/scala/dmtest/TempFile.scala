package dmtest

import java.io.{FileWriter, File}
import java.nio.file.{Files, Paths, Path}
import java.util.concurrent.atomic.AtomicLong

object TempFile {
  val POOLDIR = Paths.get("/mnt/dmtest")
  def mount(): Unit = {
    if (!Files.exists(POOLDIR)) {
      Files.createDirectory(POOLDIR)
    }
    Shell(s"mount -t ramfs -o size=256m ramfs ${POOLDIR}")
  }
  def umount(): Unit = {
    Shell(s"ls ${POOLDIR}")
    Shell(s"umount ${POOLDIR}")
    Files.deleteIfExists(POOLDIR)
  }
  private val i = new AtomicLong(0)
  def alloc(): Path =  {
    val p = Paths.get(s"${POOLDIR}/${i.getAndIncrement()}")
    p
  }
  def apply[A](fn: Path => A): A = {
    val p = alloc()
    try {
      fn(p)
    } finally {
      Files.deleteIfExists(p)
    }
  }
  def text(mkString: => String): Path = {
    val p = alloc()
    val writer = new FileWriter(p.toFile)
    try {
      writer.write(mkString)
    } finally {
      writer.close()
    }
    p
  }
}
