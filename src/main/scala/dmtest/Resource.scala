package dmtest

// experimental
object Resource {
  def apply[ResourceT, ResultT]
    (acquire: => ResourceT)
    (release: ResourceT => Unit)
    (f: ResourceT => ResultT): ResultT = {
    val resource =acquire
    try {
      f(resource)
    } finally {
      release(resource)
    }
  }
}
