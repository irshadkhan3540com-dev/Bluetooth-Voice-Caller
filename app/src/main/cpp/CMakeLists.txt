# एंड्रॉइड कंपाइलर का मिनिमम वर्जन सेट करना
cmake_minimum_required(VERSION 3.22.1)

# हमारे C++ प्रोजेक्ट का नाम
project("btcaller")

# 1. native-lib.cpp को कंपाइल करके एक शेयर लाइब्रेरी (Shared Library) बनाना
add_library(
    btcaller
    SHARED
    native-lib.cpp
)

# 2. एंड्रॉइड की डिबग लॉगिंग (log) लाइब्रेरी को खोजना ताकि हम C++ के लॉग्स देख सकें
find_library(
    log-lib
    log
)

# 3. हमारी btcaller लाइब्रेरी को एंड्रॉइड की log लाइब्रेरी के साथ जोड़ना (Link करना)
target_link_libraries(
    btcaller
    ${log-lib}
)

