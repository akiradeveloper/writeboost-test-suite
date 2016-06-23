package dmtest

class DMState(val name: String) {
  def create() = Shell.run(s"dmsetup create ${name}")
  def reload(table: String) = (s"dmsetup reload ${name} ${table}")
}
