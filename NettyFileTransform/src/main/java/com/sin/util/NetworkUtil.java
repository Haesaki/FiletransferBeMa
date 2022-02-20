package com.sin.util;

import java.util.regex.Pattern;

public class NetworkUtil {
    public static boolean validIPV4Address(String ip) {
        if(ip.contains("\\.")){
            String chunkIPv4 = "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])";
            Pattern pattenIPv4 =
                    Pattern.compile("^(" + chunkIPv4 + "\\.){3}" + chunkIPv4 + "$");
            return pattenIPv4.matcher(ip).matches();
        }
        return false;
    }
}
