CC = clang
CXX = clang++

all: example-python-ctypes example-csharp example-kotlin-jni example-kotlin-jna

libtest.dylib: test.c
	$(CC) -shared -o $@ $^

libtest-jni.dylib: test-jni.cpp
	$(CXX) -shared -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin/ -o $@ $^

jna.jar:
	mvn dependency:get -Dartifact=net.java.dev.jna:jna:4.5.0 -Ddest=$@

example-python-ctypes: libtest.dylib
	python example-ctypes.py

example-csharp: libtest.dylib
	csharp Test.cs

example-kotlin-jni: libtest-jni.dylib
	kotlinc -script example-jni.kts

example-kotlin-jna: libtest.dylib jna.jar
	kotlinc -cp jna.jar -script example-jna.kts

clean:
	rm -f libtest.dylib libtest-jni.dylib jna.jar
