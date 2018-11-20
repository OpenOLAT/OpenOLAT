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
package org.olat.core.configuration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.test.OlatTestCase;

/**
 * 
 * Initial date: 20 Nov 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EDConfigurationTest extends OlatTestCase {

	private static final SecureRandom random = new SecureRandom();
	
	@Test
	public void writeReadString() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		SecretKey key = generateKey("dfz46GH38723BGbg");
		Cipher cipher = Cipher.getInstance("AES/CTR/NOPADDING", "BC");
		cipher.init(Cipher.DECRYPT_MODE, key, random);
		CipherOutputStream cout =  new CipherOutputStream(out, cipher);
		
		cout.write("Hello world".getBytes("8859_1"));
		cout.flush();
		cout.close();
		
		String encrypted = out.toString("8859_1");
		
		ByteArrayInputStream in = new ByteArrayInputStream(encrypted.getBytes("8859_1"));
		Cipher rcipher = Cipher.getInstance("AES/CTR/NOPADDING", "BC");
		rcipher.init(Cipher.DECRYPT_MODE, key, random);
		CipherInputStream cin =  new CipherInputStream(in, rcipher);
		String decrypt = IOUtils.toString(cin, "8859_1");
		Assert.assertEquals("Hello world", decrypt);
	}
  
	private static SecretKey generateKey(String passphrase) throws Exception {
		String salt = "Something I want to say about something but cannot do without  salt.";
		PBEKeySpec keySpec = new PBEKeySpec(passphrase.toCharArray(), salt.getBytes(), 2000, 128);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWITHSHA256AND128BITAES-CBC-BC");
		return keyFactory.generateSecret(keySpec);
	}
}
