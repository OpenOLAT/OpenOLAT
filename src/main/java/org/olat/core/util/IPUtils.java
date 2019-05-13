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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.util;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * 
 * Thanks: https://gist.github.com/madan712/6651967
 * 
 * It's based of the InetAddresses class from guava too and
 * prevent a DNS lookup of java.net.InetAddress
 * 
 * Initial date: 18.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IPUtils {
	
	private static final Logger log = Tracing.createLoggerFor(IPUtils.class);
	
	public static long ipToLong(byte[] octets) {
		long result = 0;
		for (byte octet : octets) {
			result <<= 8;
			result |= octet & 0xff;
		}
		return result;
	}
	
	public static boolean isValidRange(String ipWithMask, String address) {
		boolean allOk = false;
		ipWithMask = ipWithMask.trim();
		int maskIndex = ipWithMask.indexOf('/');
		if(maskIndex > 0) {
			long bits = Long.parseLong(ipWithMask.substring(maskIndex + 1));
			long subnet = ipToLong(textToNumericFormatV4(ipWithMask.substring(0, maskIndex)));
			long ip = ipToLong(textToNumericFormatV4(address));
			
			long mask = -1 << (32 - bits);
			if ((subnet & mask) == (ip & mask)) {
				allOk = true;
			}
		}
		return allOk;
	}
 
	/**
	 * 
	 * @param ipStart
	 * @param ipEnd
	 * @param ipToCheck
	 * @return
	 */
	public static boolean isValidRange(String ipStart, String ipEnd, String ipToCheck) {
		try {
			ipStart = ipStart.trim();
			ipEnd = ipEnd.trim();
			ipToCheck = ipToCheck.trim();
			long ipLo = ipToLong(textToNumericFormatV4(ipStart));
			long ipHi = ipToLong(textToNumericFormatV4(ipEnd));
			long ipToTest = ipToLong(textToNumericFormatV4(ipToCheck));
			return (ipToTest >= ipLo && ipToTest <= ipHi);
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	private static byte[] textToNumericFormatV4(String ipString) {
		String[] address = ipString.split("\\.", 5);
		if (address.length != 4) {
			return null;
		}

		byte[] bytes = new byte[4];
		try {
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = parseOctet(address[i]);
			}
		} catch (NumberFormatException ex) {
			return null;
		}

		return bytes;
	}

	private static byte parseOctet(String ipPart) {
		// Note: we already verified that this string contains only hex digits.
		int octet = Integer.parseInt(ipPart);
		// Disallow leading zeroes, because no clear standard exists on
		// whether these should be interpreted as decimal or octal.
		if (octet > 255 || (ipPart.startsWith("0") && ipPart.length() > 1)) {
			throw new NumberFormatException();
		}
		return (byte) octet;
	}
}

