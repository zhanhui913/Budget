//
// Created by Zhan Yap on 2016-12-16.
//

#include <jni.h>

extern "C" {
    JNIEXPORT jstring JNICALL
    Java_com_zhan_budget_MyApplication_invokeNativeFunction(JNIEnv *env, jobject instance) {
        return env->NewStringUTF("07fbfe7f7fb9fe5f799ab21bca5fb6014677643a314a46bee0e0a83eb0cf6642");
    }
}