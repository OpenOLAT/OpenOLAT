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

import java.util.Base64;

import javax.crypto.SecretKey;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 1 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AesGcmCipherTest {
	
	private static final String AAD = "8f14e45f-ea8d-4a6b-9a1c-2d3e4f5a6b7c";
	private static final String SECRET = "0.AXkAaBcDeFgHiJkLmNoPqRsTuVwXyZ-refresh-token-value";
	
	@Test
	public void encryptDecrypt() {
		SecretKey key = AesGcmCipher.keyFromBase64(AesGcmCipher.generateKeyBase64());
		
		String encrypted = AesGcmCipher.encrypt(key, SECRET, AAD);
		Assert.assertNotNull(encrypted);
		Assert.assertTrue(encrypted.startsWith(AesGcmCipher.PREFIX));
		Assert.assertFalse(encrypted.contains(SECRET));
		
		Assert.assertEquals(SECRET, AesGcmCipher.decrypt(key, encrypted, AAD));
	}
	
	@Test
	public void encryptWithoutAad() {
		SecretKey key = AesGcmCipher.keyFromBase64(AesGcmCipher.generateKeyBase64());
		
		String encrypted = AesGcmCipher.encrypt(key, SECRET, null);
		Assert.assertEquals(SECRET, AesGcmCipher.decrypt(key, encrypted, null));
	}
	
	@Test
	public void encryptRandomizedByInitializationVector() {
		SecretKey key = AesGcmCipher.keyFromBase64(AesGcmCipher.generateKeyBase64());
		
		String first = AesGcmCipher.encrypt(key, SECRET, AAD);
		String second = AesGcmCipher.encrypt(key, SECRET, AAD);
		Assert.assertNotEquals(first, second);
		Assert.assertEquals(SECRET, AesGcmCipher.decrypt(key, first, AAD));
		Assert.assertEquals(SECRET, AesGcmCipher.decrypt(key, second, AAD));
	}
	
	@Test
	public void decryptWithWrongAad() {
		SecretKey key = AesGcmCipher.keyFromBase64(AesGcmCipher.generateKeyBase64());
		
		String encrypted = AesGcmCipher.encrypt(key, SECRET, AAD);
		Assert.assertNull(AesGcmCipher.decrypt(key, encrypted, "an-other-organizer"));
		Assert.assertNull(AesGcmCipher.decrypt(key, encrypted, null));
	}
	
	@Test
	public void decryptWithWrongKey() {
		SecretKey key = AesGcmCipher.keyFromBase64(AesGcmCipher.generateKeyBase64());
		SecretKey otherKey = AesGcmCipher.keyFromBase64(AesGcmCipher.generateKeyBase64());
		
		String encrypted = AesGcmCipher.encrypt(key, SECRET, AAD);
		Assert.assertNull(AesGcmCipher.decrypt(otherKey, encrypted, AAD));
	}
	
	@Test
	public void decryptTamperedValue() {
		SecretKey key = AesGcmCipher.keyFromBase64(AesGcmCipher.generateKeyBase64());
		String encrypted = AesGcmCipher.encrypt(key, SECRET, AAD);
		
		byte[] bytes = Base64.getDecoder().decode(encrypted.substring(AesGcmCipher.PREFIX.length()));
		bytes[bytes.length - 1] ^= 0x01;
		String tampered = AesGcmCipher.PREFIX.concat(Base64.getEncoder().encodeToString(bytes));
		
		Assert.assertNull(AesGcmCipher.decrypt(key, tampered, AAD));
	}
	
	@Test
	public void decryptClearTextIsRejected() {
		SecretKey key = AesGcmCipher.keyFromBase64(AesGcmCipher.generateKeyBase64());
		
		Assert.assertNull(AesGcmCipher.decrypt(key, SECRET, AAD));
		Assert.assertNull(AesGcmCipher.decrypt(key, AesGcmCipher.PREFIX, AAD));
		Assert.assertNull(AesGcmCipher.decrypt(key, AesGcmCipher.PREFIX.concat("not-base64-$$$"), AAD));
	}
	
	@Test
	public void keyFromBase64Validation() {
		Assert.assertNotNull(AesGcmCipher.keyFromBase64(AesGcmCipher.generateKeyBase64()));
		// 16 bytes, AES-128 is refused
		Assert.assertNull(AesGcmCipher.keyFromBase64(Base64.getEncoder().encodeToString(new byte[16])));
		Assert.assertNull(AesGcmCipher.keyFromBase64("not base64 at all $$$"));
		Assert.assertNull(AesGcmCipher.keyFromBase64(""));
		Assert.assertNull(AesGcmCipher.keyFromBase64(null));
	}
	
	@Test
	public void nullSafety() {
		SecretKey key = AesGcmCipher.keyFromBase64(AesGcmCipher.generateKeyBase64());
		
		Assert.assertNull(AesGcmCipher.encrypt(key, null, AAD));
		Assert.assertNull(AesGcmCipher.encrypt(null, SECRET, AAD));
		Assert.assertNull(AesGcmCipher.decrypt(key, null, AAD));
		Assert.assertNull(AesGcmCipher.decrypt(null, "anything", AAD));
	}
}
