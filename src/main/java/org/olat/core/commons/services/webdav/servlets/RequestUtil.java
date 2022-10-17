/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.core.commons.services.webdav.servlets;


import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

import jakarta.servlet.http.Cookie;


/**
 * General purpose request parsing and encoding utility methods.
 *
 * @author Craig R. McClanahan
 * @author Tim Tye
 */

public final class RequestUtil {


    /**
     * The DateFormat to use for generating readable dates in cookies.
     */
    private static SimpleDateFormat format =
        new SimpleDateFormat(" EEEE, dd-MMM-yy kk:mm:ss zz");

    static {
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }


    /**
     * Encode a cookie as per RFC 2109.  The resulting string can be used
     * as the value for a <code>Set-Cookie</code> header.
     *
     * @param cookie The cookie to encode.
     * @return A string following RFC 2109.
     */
    public static String encodeCookie(Cookie cookie) {

        StringBuilder buf = new StringBuilder( cookie.getName() );
        buf.append("=");
        buf.append(cookie.getValue());

        if (cookie.getComment() != null) {
            buf.append("; Comment=\"");
            buf.append(cookie.getComment());
            buf.append("\"");
        }

        if (cookie.getDomain() != null) {
            buf.append("; Domain=\"");
            buf.append(cookie.getDomain());
            buf.append("\"");
        }

        if (cookie.getMaxAge() >= 0) {
            buf.append("; Max-Age=\"");
            buf.append(cookie.getMaxAge());
            buf.append("\"");
        }

        if (cookie.getPath() != null) {
            buf.append("; Path=\"");
            buf.append(cookie.getPath());
            buf.append("\"");
        }

        if (cookie.getSecure()) {
            buf.append("; Secure");
        }

        if (cookie.getVersion() > 0) {
            buf.append("; Version=\"");
            buf.append(cookie.getVersion());
            buf.append("\"");
        }

        return (buf.toString());
    }


    /**
     * Filter the specified message string for characters that are sensitive
     * in HTML.  This avoids potential attacks caused by including JavaScript
     * codes in the request URL that is often reported in error messages.
     *
     * @param message The message string to be filtered
     */
    public static String filter(String message) {

        if (message == null)
            return (null);

        char content[] = new char[message.length()];
        message.getChars(0, message.length(), content, 0);
        StringBuilder result = new StringBuilder(content.length + 50);
        for (int i = 0; i < content.length; i++) {
            switch (content[i]) {
            case '<':
                result.append("&lt;");
                break;
            case '>':
                result.append("&gt;");
                break;
            case '&':
                result.append("&amp;");
                break;
            case '"':
                result.append("&quot;");
                break;
            default:
                result.append(content[i]);
            }
        }
        return (result.toString());

    }


    /**
     * Normalize a relative URI path that may have relative values ("/./",
     * "/../", and so on ) it it.  <strong>WARNING</strong> - This method is
     * useful only for normalizing application-generated paths.  It does not
     * try to perform security checks for malicious input.
     *
     * @param path Relative path to be normalized
     */
    public static String normalize(String path) {

        if (path == null)
            return null;

        // Create a place for the normalized path
        String normalized = path;

        if (normalized.equals("/."))
            return "/";

        // Add a leading "/" if necessary
        if (!normalized.startsWith("/"))
            normalized = "/" + normalized;

        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
                normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
                normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return (null);  // Trying to go outside our context
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) +
                normalized.substring(index + 3);
        }

        // Return the normalized path that we have completed
        return (normalized);

    }


    /**
     * Parse the character encoding from the specified content type header.
     * If the content type is null, or there is no explicit character encoding,
     * <code>null</code> is returned.
     *
     * @param contentType a content type header
     */
    public static String parseCharacterEncoding(String contentType) {

        if (contentType == null)
            return (null);
        int start = contentType.indexOf("charset=");
        if (start < 0)
            return (null);
        String encoding = contentType.substring(start + 8);
        int end = encoding.indexOf(';');
        if (end >= 0)
            encoding = encoding.substring(0, end);
        encoding = encoding.trim();
        if ((encoding.length() > 2) && (encoding.startsWith("\""))
            && (encoding.endsWith("\"")))
            encoding = encoding.substring(1, encoding.length() - 1);
        return (encoding.trim());

    }


    /**
     * Append request parameters from the specified String to the specified
     * Map.  It is presumed that the specified Map is not accessed from any
     * other thread, so no synchronization is performed.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>:  URL decoding is performed
     * individually on the parsed name and value elements, rather than on
     * the entire query string ahead of time, to properly deal with the case
     * where the name or value includes an encoded "=" or "&" character
     * that would otherwise be interpreted as a delimiter.
     *
     * @param map Map that accumulates the resulting parameters
     * @param data Input string containing request parameters
     * @param urlParameters true if we're parsing parameters on the URL
     *
     * @exception IllegalArgumentException if the data is malformed
     */
    public static void parseParameters(Map<String,String[]> map, String data, String encoding)
        throws UnsupportedEncodingException {

        if ((data != null) && (data.length() > 0)) {
            parseParameters(map, data.getBytes(encoding), encoding);
        }
    }


    /**
     * Decode and return the specified URL-encoded String.
     * When the byte array is converted to a string, the system default
     * character encoding is used...  This may be different than some other
     * servers.
     *
     * @param str The url-encoded string
     *
     * @exception IllegalArgumentException if a '%' character is not followed
     * by a valid 2-digit hexadecimal number
     */
    public static String URLDecode(String str) {

        return URLDecode(str, null);

    }


    /**
     * Decode and return the specified URL-encoded String.
     *
     * @param str The url-encoded string
     * @param enc The encoding to use; if null, the default encoding is used
     * @exception IllegalArgumentException if a '%' character is not followed
     * by a valid 2-digit hexadecimal number
     */
    public static String URLDecode(String str, String enc) {
        return (str == null) ? null : URLDecode(str.getBytes(), enc);
    }


    /**
     * Decode and return the specified URL-encoded byte array.
     *
     * @param bytes The url-encoded byte array
     * @exception IllegalArgumentException if a '%' character is not followed
     * by a valid 2-digit hexadecimal number
     */
    public static String URLDecode(byte[] bytes) {
        return URLDecode(bytes, null);
    }


    /**
     * Decode and return the specified URL-encoded byte array.
     *
     * @param bytes The url-encoded byte array
     * @param enc The encoding to use; if null, the default encoding is used
     * @exception IllegalArgumentException if a '%' character is not followed
     * by a valid 2-digit hexadecimal number
     */
    public static String URLDecode(byte[] bytes, String enc) {

        if (bytes == null)
            return (null);

        int len = bytes.length;
        int ix = 0;
        int ox = 0;
        while (ix < len) {
            byte b = bytes[ix++];     // Get byte to test
            if (b == '+') {
                b = (byte)' ';
            } else if (b == '%') {
                b = (byte) ((convertHexDigit(bytes[ix++]) << 4)
                            + convertHexDigit(bytes[ix++]));
            }
            bytes[ox++] = b;
        }
        if (enc != null) {
            try {
                return new String(bytes, 0, ox, enc);
            } catch (Exception e) {} // will return String with default encodeing
        }
        return new String(bytes, 0, ox);

    }


    /**
     * Convert a byte character value to hexidecimal digit value.
     *
     * @param b the character value byte
     */
    private static byte convertHexDigit( byte b ) {
        if ((b >= '0') && (b <= '9')) return (byte)(b - '0');
        if ((b >= 'a') && (b <= 'f')) return (byte)(b - 'a' + 10);
        if ((b >= 'A') && (b <= 'F')) return (byte)(b - 'A' + 10);
        return 0;
    }


    /**
     * Put name value pair in map.
     *
     * @param b the character value byte
     *
     * Put name and value pair in map.  When name already exist, add value
     * to array of values.
     */
    private static void putMapEntry( Map<String,String[]> map, String name, String value) {
        String[] newValues = null;
        String[] oldValues = map.get(name);
        if (oldValues == null) {
            newValues = new String[1];
            newValues[0] = value;
        } else {
            newValues = new String[oldValues.length + 1];
            System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
            newValues[oldValues.length] = value;
        }
        map.put(name, newValues);
    }


    /**
     * Append request parameters from the specified String to the specified
     * Map.  It is presumed that the specified Map is not accessed from any
     * other thread, so no synchronization is performed.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>:  URL decoding is performed
     * individually on the parsed name and value elements, rather than on
     * the entire query string ahead of time, to properly deal with the case
     * where the name or value includes an encoded "=" or "&" character
     * that would otherwise be interpreted as a delimiter.
     *
     * NOTE: byte array data is modified by this method.  Caller beware.
     *
     * @param map Map that accumulates the resulting parameters
     * @param data Input string containing request parameters
     * @param encoding Encoding to use for converting hex
     *
     * @exception UnsupportedEncodingException if the data is malformed
     */
    public static void parseParameters(Map<String,String[]> map, byte[] data, String encoding)
        throws UnsupportedEncodingException {

        if (data != null && data.length > 0) {
            int    ix = 0;
            int    ox = 0;
            String key = null;
            String value = null;
            while (ix < data.length) {
                byte c = data[ix++];
                switch ((char) c) {
                case '&':
                    value = new String(data, 0, ox, encoding);
                    if (key != null) {
                        putMapEntry(map, key, value);
                        key = null;
                    }
                    ox = 0;
                    break;
                case '=':
                    key = new String(data, 0, ox, encoding);
                    ox = 0;
                    break;
                case '+':
                    data[ox++] = (byte)' ';
                    break;
                case '%':
                    data[ox++] = (byte)((convertHexDigit(data[ix++]) << 4)
                                    + convertHexDigit(data[ix++]));
                    break;
                default:
                    data[ox++] = c;
                }
            }
            //The last value does not end in '&'.  So save it now.
            if (key != null) {
                value = new String(data, 0, ox, encoding);
                putMapEntry(map, key, value);
            }
        }

    }

    /**
     * This method remove some special characters as space, &gt;, &lt;,
     * slash .. but preserve Umlauts. () are acceptable
     * @return
     */
    public static String normalizeFilename(String filename) {
    	StringBuilder sb = new StringBuilder(filename.length());
    	char[] chars = filename.toCharArray();
    	int numOfChars = chars.length;
    	for(int i=0; i<numOfChars; i++) {
    		char ch = chars[i];
    		switch(ch) {
    			case '/': sb.append('_'); break;
    			case '\\': sb.append('_'); break;
    			case '?': sb.append('_'); break;
    			case '<': sb.append('_'); break;
    			case '>': sb.append('_'); break;
    			case '%': sb.append('_'); break;
    			case '"': sb.append('_'); break;
    			case '\'': sb.append('_'); break;
    			case ':': sb.append('_'); break;
    			case '*': sb.append('_'); break;
    			case '&': sb.append('_'); break;
    			case '.': sb.append('_'); break;
    			default: sb.append(ch);
    		}
    	}
    	String normalized = Normalizer.normalize(sb, Normalizer.Form.NFC);
    	return normalized.trim();
    }
}

