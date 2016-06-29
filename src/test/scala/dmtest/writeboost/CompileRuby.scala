package dmtest.writeboost

import java.nio.file.{Files, Paths, Path}

import dmtest.Shell

class CompileRuby(dir: Path) {
  val RUBY = dir.resolve(s"ruby.tar.gz")
  val RUBY_CACHED = Paths.get("ruby-2.1.1.tar.gz")
  val RUBY_LOCATION = Paths.get("http://cache.ruby-lang.org/pub/ruby/2.1/ruby-2.1.1.tar.gz")

  def downloadArchive: Unit = {
    if (!Files.exists(RUBY_CACHED)) {
      Shell(s"curl ${RUBY_LOCATION} -o ${RUBY_CACHED}")
    }
    Files.copy(RUBY_CACHED, RUBY)
  }
  def unarchive: Unit = {
    Shell.at(dir)(s"tar xvfz ${RUBY}")
  }
  def compile: Unit = {
    Shell.at(dir.resolve("ruby"))("./configure")
    Shell.at(dir.resolve("ruby"))("make")
  }
  def check: Unit = {
    Shell.at(dir.resolve("ruby"))("make test")
  }
}
