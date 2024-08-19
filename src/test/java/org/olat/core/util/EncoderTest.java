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

import java.security.Security;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.services.webdav.manager.WebDAVManagerImpl;
import org.olat.core.util.Encoder.Algorithm;

/**
 * 
 * Initial date: 23.08.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EncoderTest {
	
	static {
		Security.insertProviderAt(new BouncyCastleProvider(), 1);
	}
	
	@Test
	public void testCompatibility() {
		//the openolat password as saved on our database
		String openolat = "c14c4d01c090a065eaa619ca92f8cbc0";

		String hashedOpenolat_1 = Encoder.md5("openolat", null, null);
		Assert.assertEquals(openolat, hashedOpenolat_1);
		
		String encryptedOpenolat_1 = Encoder.md5hash("openolat");
		Assert.assertEquals(openolat, encryptedOpenolat_1);
		
		String encryptedOpenolat_2 = Encoder.encrypt("openolat", null, Algorithm.md5);
		Assert.assertEquals(openolat, encryptedOpenolat_2);
	}
	
	/**
	 * The test check the use of the UTF-8 or the ISO-8859-1
	 * on the hashing algorithm used by the digest authentication.
	 */
	@Test
	public void testDigestLikeCompatibility() {
		// UTF-8 encoded of standard username
		String rawDigest = digest("myUsername@openolat.org", "de#34KL");
		String ha1_utf8 = Encoder.encrypt(rawDigest, null, Encoder.Algorithm.md5_noSalt);
		String ha1_iso = Encoder.encrypt(rawDigest, null, Encoder.Algorithm.md5_iso_8859_1);
		Assert.assertEquals(ha1_utf8, ha1_iso);
		
		// ISO-8859-1 difference with Umlaut
		String rawUmlautDigest = digest("myUsern\u00E4me@openolat.org", "de#34KL");
		String ha1_umlaut_utf8 = Encoder.encrypt(rawUmlautDigest, null, Encoder.Algorithm.md5_noSalt);
		String ha1_umlaut_iso = Encoder.encrypt(rawUmlautDigest, null, Encoder.Algorithm.md5_iso_8859_1);
		Assert.assertNotEquals(ha1_umlaut_utf8, ha1_umlaut_iso);		
	}
	
	@Test
	public void testDigestCompared() {
		String rawDigest = digest("zgc_1", "w\u20ACbdav");
		String ha1_openolat = Encoder.encrypt(rawDigest, null, Encoder.Algorithm.md5_utf_8);
		String ha1_apache = DigestUtils.md5Hex(rawDigest);
		Assert.assertEquals(ha1_apache, ha1_openolat);		
	}
	
	private String digest(String authUsername, String password) {
		return authUsername + ":" + WebDAVManagerImpl.BASIC_AUTH_REALM + ":" + password;
	}
	
	/**
	 * Dummy test which check that the salts are not always equals.
	 */
	@Test
	public void testSalt() {
		Set<String> history = new HashSet<>();
		for(int i=0; i<100; i++) {
			String salt = Encoder.getSalt();
			Assert.assertFalse(history.contains(salt));
			history.add(salt);
		}
		Assert.assertEquals(100, history.size());
	}

	@Test
	public void testSHA1() {
		String password = "openolat#19";
		String salt = "FEHq1hZYqd54/iCoboHvBQ==";
		String hash = "dkpoYSfn3uz6UcwdFhauuOFgX7Q=";
		
		String shaOutput = Encoder.encrypt(password, salt, Algorithm.sha1);
		Assert.assertEquals(hash, shaOutput);
	}
			
	@Test
	public void testSHA256() {
		String password = "openolat#19";
		String salt = "dbStjLmL2av7LObcN/69ww==";
		String hash = "+iTqibtIuur1t1IBYJ3P1i/iq6Xx2cb38wHc7LXHLHQ=";
		
		String shaOutput = Encoder.encrypt(password, salt, Algorithm.sha256);
		Assert.assertEquals(hash, shaOutput);
	}
	
	@Test
	public void testSHA512() {
		String password = "openolat#19";
		String salt = "Y4nY4VFWaiSnM6qe88ZxbQ==";
		String hash = "3+OTGOdnladWlLOY71DNnYa1YpaNmrDyefCvp/LM7d417frbjabdJnWkxK6JLOwxzJkpSfyjg3LY\nY8I0Mnq8PA==";
		
		String shaOutput = Encoder.encrypt(password, salt, Algorithm.sha512);
		Assert.assertEquals(hash, shaOutput);
	}
	
	@Test
	public void testAes() {
		String password = "openolat#19";
		String key = "6afs6873l--q3ruiah";
		String salt = "Y4nY4VFWaiSnM6qe88ZxbQ==";
		int iteration = 2000;
		
		String aesOutput = Encoder.encodeAes(password, key, salt, iteration);
		Assert.assertNotNull(aesOutput);
		String decodedPassword = Encoder.decodeAes(aesOutput, key, salt, iteration);
		Assert.assertEquals(password, decodedPassword);
	}
	
	@Test
	public void testAes_alt() {
		String password = "openolat#19";
		String salt = "Y4nY4VFWaiSnM6qe88ZxbQ==";
		
		String aesOutput = Encoder.encrypt(password, salt, Algorithm.aes);
		Assert.assertNotNull(aesOutput);
		String decodedPassword = Encoder.decrypt(aesOutput, salt, Algorithm.aes);
		Assert.assertEquals(password, decodedPassword);
	}
	
	@Test
	public void testPBKDF2() {
		String password = "openolat#19";
		String salt = "lji1/YS8rEwv/ML0JUV2OQ==";
		String hash = "kUUaki27mueSEkrFeAFQoLfs1k8=";

		String shaOutput = Encoder.encrypt(password, salt, Algorithm.pbkdf2);
		Assert.assertEquals(hash, shaOutput);
	}
	
	@Test
	public void testArgon2id() {
		String password = "openolat#19";
		String salt = "lji1/YS8rEwv/ML0JUV2OQ==";
		String hash = """
				//7Jf0qmUsfoIPSBnmqj8AGuXhoxOzX8rp6sLudAGF5Pouhx+lANNpclYWGXt0HkkuQxXeDMx68A
				mL3dKuwR1ewb56nLbzLYXP6eYalFFNKwMvdU9RmYmGrL02QzRm2qvG5uoHxw8sGncZwRpe99t3/3
				77y9SRw0IesIlui4HaY=""";

		String output = Encoder.encrypt(password, salt, Algorithm.argon2id);
		Assert.assertEquals(hash, output);
	}
	
	@Test
	public void testJailedArgon2id() {
		String password = "openolat#19";
		String salt = "lji1/YS8rEwv/ML0JUV2OQ==";
		String hash = """
				//7Jf0qmUsfoIPSBnmqj8AGuXhoxOzX8rp6sLudAGF5Pouhx+lANNpclYWGXt0HkkuQxXeDMx68A
				mL3dKuwR1ewb56nLbzLYXP6eYalFFNKwMvdU9RmYmGrL02QzRm2qvG5uoHxw8sGncZwRpe99t3/3
				77y9SRw0IesIlui4HaY=""";

		String output = Encoder.jailedArgon2id(password, salt, Algorithm.argon2id);
		Assert.assertEquals(hash, output);
	}
	
	/*
	@Test
	public void testPerformances() {
		for(int i=1; i<10; i++) {
			String password = "openolat#19" + i;
			String salt = "lji1/YS8rEwv/ML0JUV2OQ==";
			
			long start1 = System.nanoTime();
			Encoder.encrypt(password, salt, Algorithm.argon2id);
			CodeHelper.printMilliSecondTime(start1, "Argon2d m=19456 (19 MiB), t=3, p=1: ");
			
			long start2 = System.nanoTime();
			Encoder.encrypt(password, salt, Algorithm.argon2id_owasp);
			CodeHelper.printMilliSecondTime(start2, "Argon2d m=12288 (12 MiB), t=3, p=1: ");
			
			long start3 = System.nanoTime();
			Encoder.encrypt(password, salt, Algorithm.pbkdf2);
			CodeHelper.printMilliSecondTime(start3, "PBK: ");
			
			long start4 = System.nanoTime();
			Encoder.encrypt(password, salt, Algorithm.sha512);
			CodeHelper.printMilliSecondTime(start4, "SHA512: ");
			
		}
	}
	*/
}
