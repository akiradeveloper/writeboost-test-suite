package dmtest.writeboost

import java.nio.file.{Paths, Files, Path}

import dmtest.Shell

object GitExtract {
  case class GitTree(root: Path) {
    require(Files.exists(root))
    require(Files.exists(root.resolve(".git")))
    def mkClone(dest: Path): GitTree = {
      Shell(s"git clone ${root} ${dest}")
      GitTree(dest)
    }
    def checkout(tag: String): Unit = {
      Shell.at(root)(s"git checkout ${tag}")
    }
  }
  val TAGS = Seq("v2.6.12", "v2.6.13", "v2.6.14", "v2.6.15", "v2.6.16", "v2.6.17")
}
case class GitExtract(mp: Path) {
  import GitExtract._
  val orig = GitTree(Paths.get("./linux-root"))
  val root = orig.mkClone(mp.resolve("./linux"))
  def extract(tag: String): Unit = {
    root.checkout(tag)
  }
}
