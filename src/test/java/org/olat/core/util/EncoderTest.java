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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
	public void concurrentMD5() {
		final CountDownLatch finishCount = new CountDownLatch(14);
		
		EncryptMD5Runner[] encrypters = new EncryptMD5Runner[]{
				new EncryptMD5Runner("openolat#19","lji1/YS8rEwv/ML0JUV2OQ==","67895d9df8d2b3ba011c29e482e89caa", finishCount),
				new EncryptMD5Runner("secret#19","zewurtuiozu","e1a9c2c4b2cdc94a1c65aa61e6a098f4", finishCount),
				new EncryptMD5Runner("secret?0","9c0ff19b-4b62-4428-8fc3-08c27d9a1328","6f15a2cce20d5ee894e62dfdbc672ba2", finishCount),
				new EncryptMD5Runner("secret?1","b07ff38f-6c1f-4c46-a274-12b1d5954f89","931611fa5f1163f81965ba81ebedf7a9", finishCount),
				new EncryptMD5Runner("secret?2","00f8dcf6-61ee-4a47-ad95-7b0055740b2c","1c144f086be2d9001ee3fe5d431a6720", finishCount),
				new EncryptMD5Runner("secret?3","32f6c9bb-b19a-422e-bb0c-51b4fd1f0196","fb0ab614ea8f448c21d58454c8683c4a", finishCount),
				new EncryptMD5Runner("secret?4","e5d993ed-83a5-4521-89e0-a83c1fb7b80d","a383d8089a3cf28f5b89c7262d9237f2", finishCount),
				new EncryptMD5Runner("secret?5","aabe7399-920d-4ba0-8d60-9d7ff52c272f","21db34962517f47da0fe2dd49c0027fd", finishCount),
				new EncryptMD5Runner("weak?secret#0","c447d1e5-15f6-4be4-a3c8-dc673a0524c7","474c42e67c23a3cddea5519ffdf82d75", finishCount),
				new EncryptMD5Runner("weak?secret#1","e1296d99-b3b4-4de6-9f9b-19fdad6e52a1","19b8d7083b0cfd1e52d647ec9ae037c8", finishCount),
				new EncryptMD5Runner("weak?secret#2","3d50300e-f53e-46c0-8cd9-fa9ea1dc4887","d7cde9946e7de02e7814952829d84ca6", finishCount),
				new EncryptMD5Runner("weak?secret#3","c0f6f257-8c57-4da3-b10b-5bf65bd2037e","970355c4d8f52905367a43121f062e83", finishCount),
				new EncryptMD5Runner("weak?secret#4","f547c7c2-e6ae-4545-945d-99d8cb415518","70abf0cf3d63c0b96b676098bd19856d", finishCount),
				new EncryptMD5Runner("weak?secret#5","b8d63092-acd5-4824-858a-200162dea348","0fa562df8d25070d9867ebeae51e24d7", finishCount),
		};
		
		for(int i=0; i<14; i++) {
			encrypters[i].start();
		}

		try {
			finishCount.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Assert.fail("Threads did not finish in 10sec");
		}
		
		for(int i=0; i<14; i++) {
			Assert.assertEquals(encrypters[i].getReference(), encrypters[i].getHash());
		}	
	}
	
	private static class EncryptMD5Runner extends Thread {
		
		private final CountDownLatch countDown;
		private final String password;
		private final String salt;
		private final String reference;
		private String hash;
		
		public EncryptMD5Runner(String password, String salt, String reference, CountDownLatch countDown) {
			this.salt = salt;
			this.password = password;
			this.reference = reference;
			this.countDown = countDown;
		}
		
		public String getReference() {
			return reference;
		}
		
		public String getHash() {
			return hash;
		}
		
		@Override
		public void run() {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			hash = Encoder.encrypt(password, salt, Algorithm.md5);
			countDown.countDown();
		}
	}
	
	@Test
	public void concurrentPBKDF2() {

		final CountDownLatch finishCount = new CountDownLatch(15);
		
		EncryptPBKDF2Runner[] encrypters = new EncryptPBKDF2Runner[]{
				new EncryptPBKDF2Runner("openolat#19", "lji1/YS8rEwv/ML0JUV2OQ==", "kUUaki27mueSEkrFeAFQoLfs1k8=", finishCount),
				new EncryptPBKDF2Runner("secret#19", "zewurtuiozu", "6ZE0hrBsbs4gNMnrSSQS6TbzpuI=", finishCount),
				new EncryptPBKDF2Runner("top-secret", "clearance", "JgRVHHHT29PKlwuhSbQVczBChkY=", finishCount),
				new EncryptPBKDF2Runner("secret?0","fb79944a-ab0e-45ad-be1c-0eb50a2f3031","xiZw1itsZGXdFO3AeUg8Du9gbSk=", finishCount),
				new EncryptPBKDF2Runner("secret?1","a78872be-5798-4120-9577-98f79f993ebc","qLtZfhh2OSGB71qOT1QoK+e3VCY=", finishCount),
				new EncryptPBKDF2Runner("secret?2","94136eb8-99ac-41ab-9bc6-a534ea390492","EnygPiljxgxQPUAo+k4kPTnSEYk=", finishCount),
				new EncryptPBKDF2Runner("secret?3","800617f4-80cf-4e92-a0fd-06ca8c83e5a3","osUdlhRDuGKhrtlDk7GqpmRC5rk=", finishCount),
				new EncryptPBKDF2Runner("secret?4","cc4aa953-1f13-4370-a803-9388a68aa868","pa/qNiKj8SMP9bTMwZ3M7kzeFuo=", finishCount),
				new EncryptPBKDF2Runner("secret?5","83634a3b-fc3d-4dc5-94c4-684063833912","ZJLaMn4xyRvieyK+xYeH4dvzI7Q=", finishCount),
				new EncryptPBKDF2Runner("more?secret#0","aaade6df-3319-416d-b405-a6299cc84970","pOV6OT1aVo6oMKJj81UL5wcnpH0=", finishCount),
				new EncryptPBKDF2Runner("more?secret#1","d7bc525f-5bf6-49f8-bc51-18606873de9f","isxOvmuPRk8jwwnAaUfKIyMDddc=", finishCount),
				new EncryptPBKDF2Runner("more?secret#2","c9638cc5-4e27-497a-8f1d-75d0937459a7","gYSvlDb/p6bxWfVGsKTpfMQR/Ik=", finishCount),
				new EncryptPBKDF2Runner("more?secret#3","69d730e1-678f-4ed0-8b98-47f098e0a200","5CPd6B0aXJB5qJoYiCe6xJZW7ug=", finishCount),
				new EncryptPBKDF2Runner("more?secret#4","c0ed1714-43b7-46c8-8816-7f72ef0b9d3b","oq1tqJxIlQiKGbluqRzCAKEoxhY=", finishCount),
				new EncryptPBKDF2Runner("more?secret#5","5e29f2e9-6bf2-4998-9d21-01f69f12533f","Asz/tXx96d+fIafd4ZSlEPHkay8=", finishCount)
		};
		
		for(int i=0; i<15; i++) {
			encrypters[i].start();
		}

		try {
			
			finishCount.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Assert.fail("Threads did not finish in 10sec");
		}
		
		for(int i=0; i<15; i++) {
			Assert.assertEquals(encrypters[i].getReference(), encrypters[i].getHash());
		}	
	}
	
	private static class EncryptPBKDF2Runner extends Thread {
		
		private final CountDownLatch countDown;
		private final String password;
		private final String salt;
		private final String reference;
		private String hash;
		
		public EncryptPBKDF2Runner(String password, String salt, String reference, CountDownLatch countDown) {
			this.salt = salt;
			this.password = password;
			this.reference = reference;
			this.countDown = countDown;
		}
		
		public String getReference() {
			return reference;
		}
		
		public String getHash() {
			return hash;
		}
		
		@Override
		public void run() {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			hash = Encoder.encrypt(password, salt, Algorithm.pbkdf2);
			countDown.countDown();
		}
	}
}
