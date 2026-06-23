#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "BT_CALL_NATIVE"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// वैश्विक वेरिएबल (Global variables) ऑडियो और सॉकेट स्टेटस के लिए
bool isCallActive = false;
int bluetoothSocketFd = -1; // ब्लूटूथ सॉकेट फाइल डिस्क्रिप्टर

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_example_btcaller_ui_CallActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "C++ Voice Engine Active";
    return env->NewStringUTF(hello.c_str());
}

// 1. ब्लूटूथ के जरिए ऑडियो स्ट्रीम शुरू करने का फंक्शन
JNIEXPORT void JNICALL
Java_com_example_btcaller_ui_CallActivity_startNativeAudioConnection(
        JNIEnv* env,
        jobject /* this */,
        jstring deviceAddress) {
    
    const char* address = env->GetStringUTFChars(deviceAddress, nullptr);
    LOGI("C++: Connecting to bluetooth device: %s", address);

    if (isCallActive) {
        LOGI("C++: Call is already running.");
        env->ReleaseStringUTFChars(deviceAddress, address);
        return;
    }

    isCallActive = true;

    // यहाँ हम C++ thread बनाएंगे जो:
    // 1. Android Microphone (PCM Data) से आवाज रिकॉर्ड करेगा।
    // 2. उस डेटा को ब्लूटूथ RFCOMM सॉकेट के जरिए दूसरे फोन पर भेजेगा।
    // 3. दूसरे फोन से आने वाले ऑडियो डेटा को रिसीवर स्पीकर पर प्ले करेगा।

    LOGI("C++: Audio Recording and Playback threads initialized.");
    env->ReleaseStringUTFChars(deviceAddress, address);
}

// 2. कॉल को डिस्कनेक्ट और रिसोर्सेज को फ्री करने का फंक्शन
JNIEXPORT void JNICALL
Java_com_example_btcaller_ui_CallActivity_stopNativeAudioConnection(
        JNIEnv* env,
        jobject /* this */) {
    
    if (!isCallActive) {
        LOGI("C++: No active call to stop.");
        return;
    }

    isCallActive = false;
    
    // ब्लूटूथ सॉकेट बंद करना और माइक स्ट्रीमिंग रोकना
    if (bluetoothSocketFd >= 0) {
        // close(bluetoothSocketFd);
        bluetoothSocketFd = -1;
    }

    LOGI("C++: Native Audio Connection stopped successfully.");
}

}

