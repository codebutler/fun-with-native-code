#include <stdio.h>
#include <jni.h>

extern "C" {
  void say_hello(const char* name) {
    printf("Hello %s!\n", name);
  }

  JNIEXPORT void JNICALL Java_Example_1jni_sayHello(JNIEnv *env, jclass cls, jstring name) {
    const char *nativeHello = env->GetStringUTFChars(name, JNI_FALSE);
    say_hello(nativeHello);
    env->ReleaseStringUTFChars(name, nativeHello);
  }
}
