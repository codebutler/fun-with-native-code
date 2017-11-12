import com.sun.jna.*

interface TestLib: Library {
  fun say_hello(name: String)
}

val lib = Native.loadLibrary("test", TestLib::class.java)
lib.say_hello("Droidcon")
