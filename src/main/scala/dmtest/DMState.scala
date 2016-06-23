package dmtest

class DMState(val name: String) {
  def create() = Shell(s"dmsetup create ${name}")
  def remove() = Shell(s"dmsetup remove ${name}")
  def reload(table: String) = Shell(s"dmsetup reload ${name} ${table}")
  def suspend() = Shell(s"dmsetup suspend ${name}")
  def resume() = Shell(s"dmsetup resume ${name}")
}
