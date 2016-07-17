package dmtest.writeboost

import java.nio.file.{Files, Paths, Path}

import dmtest.Shell

class CompileRuby(dir: Path) {
  val WD = dir.resolve("ruby-2.1.1")
  val RUBY_CACHED = Paths.get("ruby-2.1.1.tar.gz")
  def downloadArchive: Unit = {
    if (!Files.exists(RUBY_CACHED)) {
      Shell(s"curl http://cache.ruby-lang.org/pub/ruby/2.1/ruby-2.1.1.tar.gz -o ${RUBY_CACHED}")
    }
    Files.copy(RUBY_CACHED, dir.resolve(RUBY_CACHED))
  }
  def unarchive: Unit = {
    Shell.at(dir)(s"tar xvfz ${RUBY_CACHED}")
  }
  def compile: Unit = {
    Shell.at(WD)("./configure")
    Shell.at(WD)("make")
  }
  def check: Unit = {
    Shell.at(WD)("make test")
  }
}
