/**
 * <a href="https://www.openolat.org">
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
package org.olat.core.util.crypto;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * Authenticated encryption of short secrets with AES-256-GCM. Use it to protect
 * credentials which must be readable again later, typically tokens saved in the
 * database. Prefer this over {@link org.olat.core.util.Encoder.Algorithm#aes}
 * which uses an unauthenticated mode and a passphrase hard coded in the source.
 * <p>
 * The encrypted value is prefixed with {@link #PREFIX} to make the format
 * recognizable and to leave room for a future algorithm. Values without the
 * prefix are rejected, they are never interpreted as clear text.
 * <p>
 * The additional authenticated data (AAD) is not encrypted but the value cannot
 * be decrypted without it. Bind the secret to its owner, the primary key of the
 * row for example, and a value copied to an other row becomes worthless.
 * <p>
 * Initial date: 1 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AesGcmCipher {
	
	private static final Logger log = Tracing.createLoggerFor(AesGcmCipher.class);
	
	public static final String PREFIX = "{aesgcm1}";
	
	private static final String ALGORITHM = "AES";
	private static final String TRANSFORMATION = "AES/GCM/NoPadding";
	/** AES-256 only, a shorter key is refused. */
	private static final int KEY_LENGTH = 32;
	/** 12 bytes is the recommended nonce length for GCM. */
	private static final int IV_LENGTH = 12;
	private static final int TAG_LENGTH_BITS = 128;
	
	private static final SecureRandom random = new SecureRandom();
	
	private AesGcmCipher() {
		//
	}
	
	/**
	 * @param base64Key A base64 encoded key of exactly 32 bytes
	 * @return The key or null if the specified value is not a valid AES-256 key
	 */
	public static SecretKey keyFromBase64(String base64Key) {
		if(!StringHelper.containsNonWhitespace(base64Key)) {
			return null;
		}
		
		byte[] keyBytes;
		try {
			keyBytes = Base64.getDecoder().decode(base64Key.trim());
		} catch (IllegalArgumentException e) {
			log.error("Key is not base64 encoded");
			return null;
		}
		
		if(keyBytes.length != KEY_LENGTH) {
			log.error("Key has {} bytes, AES-256 needs exactly {} bytes", keyBytes.length, KEY_LENGTH);
			Arrays.fill(keyBytes, (byte)0);
			return null;
		}
		
		SecretKey key = new SecretKeySpec(keyBytes, ALGORITHM);
		Arrays.fill(keyBytes, (byte)0);
		return key;
	}
	
	/**
	 * @return A new base64 encoded AES-256 key, to help an administrator to configure one
	 */
	public static String generateKeyBase64() {
		byte[] keyBytes = new byte[KEY_LENGTH];
		random.nextBytes(keyBytes);
		String base64Key = Base64.getEncoder().encodeToString(keyBytes);
		Arrays.fill(keyBytes, (byte)0);
		return base64Key;
	}
	
	/**
	 * @param key The key, mandatory
	 * @param plainText The value to protect
	 * @param aad The additional authenticated data, mandatory to decrypt the value again
	 * @return The prefixed and base64 encoded value or null if the value cannot be encrypted
	 */
	public static String encrypt(SecretKey key, String plainText, String aad) {
		if(key == null || plainText == null) {
			return null;
		}
		
		try {
			byte[] iv = new byte[IV_LENGTH];
			random.nextBytes(iv);
			
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
			updateAad(cipher, aad);
			byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
			
			byte[] encrypted = new byte[iv.length + cipherText.length];
			System.arraycopy(iv, 0, encrypted, 0, iv.length);
			System.arraycopy(cipherText, 0, encrypted, iv.length, cipherText.length);
			return PREFIX.concat(Base64.getEncoder().encodeToString(encrypted));
		} catch (Exception e) {
			// Never log the value itself
			log.error("Cannot encrypt value with AES-GCM", e);
			return null;
		}
	}
	
	/**
	 * @param key The key, mandatory
	 * @param encrypted A value produced by {@link #encrypt(SecretKey, String, String)}
	 * @param aad The additional authenticated data used to encrypt the value
	 * @return The clear text value or null if the value cannot be decrypted
	 */
	public static String decrypt(SecretKey key, String encrypted, String aad) {
		if(key == null || !StringHelper.containsNonWhitespace(encrypted)) {
			return null;
		}
		if(!encrypted.startsWith(PREFIX)) {
			log.error("Value is not encrypted with AES-GCM, it will not be used");
			return null;
		}
		
		try {
			byte[] bytes = Base64.getDecoder().decode(encrypted.substring(PREFIX.length()));
			if(bytes.length <= IV_LENGTH) {
				log.error("Encrypted value is too short to hold an initialization vector and a tag");
				return null;
			}
			
			GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, bytes, 0, IV_LENGTH);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, key, spec);
			updateAad(cipher, aad);
			byte[] plainText = cipher.doFinal(bytes, IV_LENGTH, bytes.length - IV_LENGTH);
			return new String(plainText, StandardCharsets.UTF_8);
		} catch (AEADBadTagException e) {
			// A wrong key, a wrong AAD and a tampered value are not distinguishable by design,
			// the stack trace of the exception would not tell more than the message
			log.error("Cannot decrypt value with AES-GCM, key, authenticated data or value are not valid");
			return null;
		} catch (Exception e) {
			log.error("Cannot decrypt value with AES-GCM", e);
			return null;
		}
	}
	
	private static void updateAad(Cipher cipher, String aad) {
		if(StringHelper.containsNonWhitespace(aad)) {
			cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
		}
	}
}
