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

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.util.Encoder.Algorithm;

/**
 * 
 * Initial date: 23.08.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EncoderTest {
	
	@Test
	public void testCompatibility() {
		//the openolat password as saved on our database
		String openolat = "c14c4d01c090a065eaa619ca92f8cbc0";

		String hashedOpenolat_1 = Encoder.md5("openolat", null);
		Assert.assertEquals(openolat, hashedOpenolat_1);
		
		String encryptedOpenolat_1 = Encoder.md5hash("openolat");
		Assert.assertEquals(openolat, encryptedOpenolat_1);
		
		String encryptedOpenolat_2 = Encoder.encrypt("openolat", null, Algorithm.md5);
		Assert.assertEquals(openolat, encryptedOpenolat_2);
	}
	
	/**
	 * Dummy test which check that the salts are not always equals.
	 */
	@Test
	public void testSalt() {
		Set<String> history = new HashSet<String>();
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
	public void testPBKDF2() {
		String password = "openolat#19";
		String salt = "lji1/YS8rEwv/ML0JUV2OQ==";
		String hash = "kUUaki27mueSEkrFeAFQoLfs1k8=";

		String shaOutput = Encoder.encrypt(password, salt, Algorithm.pbkdf2);
		Assert.assertEquals(hash, shaOutput);
	}
}
