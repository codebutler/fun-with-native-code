# $ python test.py
# Hello Droidcon!

from ctypes import *

lib = cdll.LoadLibrary("libtest.dylib")
lib.say_hello("Droidcon")
