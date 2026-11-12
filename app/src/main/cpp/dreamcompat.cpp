#include <jni.h>
#include <cstdint>
#include "macho_inspector.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_raptorkillz_projectdreambuilder_MainActivity_nativeInspectMachO(
    JNIEnv* env, jobject, jbyteArray array) {
    if (array == nullptr) {
        return env->NewStringUTF("No binary header supplied.");
    }

    const jsize length = env->GetArrayLength(array);
    jbyte* bytes = env->GetByteArrayElements(array, nullptr);
    if (bytes == nullptr) {
        return env->NewStringUTF("Could not access binary header.");
    }

    const auto info = dreamcompat::inspect_macho_header(
        reinterpret_cast<const uint8_t*>(bytes), static_cast<size_t>(length));
    env->ReleaseByteArrayElements(array, bytes, JNI_ABORT);
    return env->NewStringUTF(info.summary.c_str());
}
