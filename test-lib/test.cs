using System.Runtime.InteropServices;

static class LibTest {
  [DllImport("libtest.dylib")]
  public static extern void say_hello(string name);
}

LibTest.say_hello("Droidcon")
