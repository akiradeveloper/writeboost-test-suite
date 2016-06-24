package dmtest

class DMState(val name: String) {
  def create() = Shell(s"dmsetup create ${name} --notable")
  def remove() = Shell(s"dmsetup remove ${name}")
  def reload(table: String) = Shell(s"echo ${table} | dmsetup reload ${name}")
  def suspend() = Shell(s"dmsetup suspend ${name}")
  def resume() = Shell(s"dmsetup resume ${name}")
  def table() = Shell(s"dmsetup table ${name}")
}
