#include <jni.h>
#include <cstdint>
#include "macho_inspector.h"
extern "C" JNIEXPORT jstring JNICALL Java_com_raptorkillz_projectdreambuilder_MainActivity_nativeInspectMachO(JNIEnv* env,jobject,jbyteArray a) {
 if (!a) return env->NewStringUTF("No binary header supplied."); jsize n=env->GetArrayLength(a); jbyte* p=env->GetByteArrayElements(a,nullptr); if (!p) return env->NewStringUTF("Could not access binary header.");
 auto info=dreamcompat::inspect_macho_header(reinterpret_cast<const uint8_t*>(p),static_cast<size_t>(n)); env->ReleaseByteArrayElements(a,p,JNI_ABORT); return env->NewStringUTF(info.summary.c_str());
}
