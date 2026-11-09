/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.util.httpclient;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Initial date: 17 janv. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ConnectionUtilities {
	
	private static final Logger log = LoggerFactory.getLogger(ConnectionUtilities.class);
	private static final SecureRandom boundariesGenerator = new SecureRandom();
    
	private static final int RADIX = 16;
    private static final BitSet URLENCODER = new BitSet(256);
    private static final BitSet UNRESERVED = new BitSet(256);
    
    static {
        // unreserved chars
        // alpha characters
        for (int i = 'a'; i <= 'z'; i++) {
            UNRESERVED.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            UNRESERVED.set(i);
        }
        // numeric characters
        for (int i = '0'; i <= '9'; i++) {
            UNRESERVED.set(i);
        }
        UNRESERVED.set('_'); // these are the charactes of the "mark" list
        UNRESERVED.set('-');
        UNRESERVED.set('.');
        UNRESERVED.set('*');
        URLENCODER.or(UNRESERVED); // skip remaining unreserved characters
    }
	
	public static final String IMAGE_JPEG = "image/jpeg";
	public static final String IMAGE_PNG = "image/png";
	public static final String IMAGE_BMP = "image/bmp";
	
	public static final void consume(HttpResponse<InputStream> response) {
		if(response != null) {
			try(InputStream body=response.body()) {
				byte[] buffer = new byte[4096];
				while (body.read(buffer, 0, buffer.length) != -1) {
		    		//consume
				}
			} catch (Exception e) {
				log.error("Consume", e);
			}
		}
	}
	
	public static final String toString(HttpResponse<InputStream> response) {
		if(response == null) return null;
		
		try(InputStream body=response.body();
				InputStreamReader inputReader = new InputStreamReader(body, StandardCharsets.UTF_8);
				BufferedReader bReader = new BufferedReader(inputReader)) {
			return bReader
					.lines()
					.collect(Collectors.joining());
		} catch (Exception e) {
			log.error("UTF8 Stream to string", e);
			return null;
		}
	}
	
	public static final String toString(HttpResponse<InputStream> response, String encoding) {
		try(InputStream body=response.body();
				InputStreamReader inputReader = new InputStreamReader(body, encoding);
				BufferedReader bReader = new BufferedReader(inputReader)) {
			return bReader
					.lines()
					.collect(Collectors.joining());
		} catch (Exception e) {
			log.error("{} Stream to string", encoding, e);
			return null;
		}
	}

	public static final byte[] toByteArray(HttpResponse<InputStream> response) {
		try(InputStream body=response.body();
				ByteArrayOutputStream inputReader = new ByteArrayOutputStream()) {
            final byte[] tmp = new byte[8096];
            int l;
            while((l = body.read(tmp)) != -1) {
            	inputReader.write(tmp, 0, l);
            }
            return inputReader.toByteArray();
		} catch (Exception e) {
			log.error("Stream to string", e);
			return null;
		}
	}
	
	public static final String getBoundary() {
		return new BigInteger(256, boundariesGenerator).toString();
	}
	
	public static BodyPublisher ofMimeMultipartData(String filename, String mimeType, byte[] data, List<NameValuePair> nameValueList, String boundary) {
		final Charset charset = StandardCharsets.UTF_8;
		List<byte[]> byteArrays = new ArrayList<>();
		byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=")
				.getBytes(StandardCharsets.UTF_8);
	
		// filename
		if(filename != null) {
			byteArrays.add(separator);
			byteArrays.add(("\"filename\"\r\n\r\n" + filename + "\r\n").getBytes(charset));
		}
		
		// pairs
		if(nameValueList != null) {
			for(NameValuePair nameValue:nameValueList) {
				byteArrays.add(separator);
				byteArrays.add(("\"" + nameValue.name() + "\"\r\n\r\n" + nameValue.value() + "\r\n").getBytes(charset));
			}
		}
		
		// file
		if(data != null) {
			byteArrays.add(separator);
        	byteArrays.add(("\"file\"; filename=\"" + filename + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").getBytes(charset));
        	byteArrays.add(data);
        	byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
		}

		byteArrays.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
		return BodyPublishers.ofByteArrays(byteArrays);
	}
	
    public static String urlEncode(List<NameValuePair> parameters, Charset charset) {
        return urlEncode(parameters, '&', charset);
    }
    
	public static String urlEncode(List<NameValuePair> parameters, char parameterSeparator, Charset charset) {
		StringBuilder result = new StringBuilder();
		for (NameValuePair parameter : parameters) {
			String encodedName = encodeFormFields(parameter.name(), charset);
			String encodedValue = encodeFormFields(parameter.value(), charset);
			if (result.length() > 0) {
				result.append(parameterSeparator);
			}
			result.append(encodedName);
			if (encodedValue != null) {
				result.append("=");
				result.append(encodedValue);
			}
		}
		return result.toString();
	}
    
    private static String encodeFormFields(String content, Charset charset) {
        if (content == null) {
            return null;
        }
        return urlEncode(content, charset, URLENCODER, true);
    }
	
    private static String urlEncode(String content, Charset charset, BitSet safechars, boolean blankAsPlus) {
        if (content == null) {
            return null;
        }
        final StringBuilder buf = new StringBuilder();
        final ByteBuffer bb = charset.encode(content);
        while (bb.hasRemaining()) {
            final int b = bb.get() & 0xff;
            if (safechars.get(b)) {
                buf.append((char) b);
            } else if (blankAsPlus && b == ' ') {
                buf.append('+');
            } else {
                buf.append("%");
                final char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, RADIX));
                final char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, RADIX));
                buf.append(hex1);
                buf.append(hex2);
            }
        }
        return buf.toString();
    }
    
    public record NameValuePair(String name, String value) {
    	//
    }
}
