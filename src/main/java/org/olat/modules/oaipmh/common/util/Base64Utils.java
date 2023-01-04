package org.olat.modules.oaipmh.common.util;

import org.apache.commons.codec.binary.Base64;

public class Base64Utils {
    public static String encode(String input) {
        return new String(Base64.encodeBase64(input.getBytes()));
    }

    public static String decode(String input) {
        return new String(Base64.decodeBase64(input.getBytes()));
    }
}
