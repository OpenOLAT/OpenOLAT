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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.olat.core.logging.AssertException;


/**
 * Description:
 * 
 * @author Sabina Jeger
 */
public class Encoder {
	private static final String HASH_ALGORITHM = "MD5";

	/**
	 * The MD5 helper object for this class.
	 */
	public static final MD5Encoder md5Encoder = new MD5Encoder();

	/**
	 * encrypt the supplied argument with md5.
	 * 
	 * @param s
	 * @return MD5 encrypted string
	 */
	public static String encrypt(String s) {
		byte[] inbytes = s.getBytes();
		try {
			MessageDigest md5Helper = MessageDigest.getInstance(HASH_ALGORITHM);
			byte[] outbytes = md5Helper.digest(inbytes);
			String out = md5Encoder.encode(outbytes);
			return out;
		} catch (NoSuchAlgorithmException e) {
			throw new AssertException("Cannot load MD5 Message Digest ," + HASH_ALGORITHM + " not supported");
		}
	}

	/**
	 * encrypt the first argument and show the result on the console
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String result = encrypt(args[0]);
		System.out.println("MD5-Hash of " + args[0] + ": " + result);
	}

}