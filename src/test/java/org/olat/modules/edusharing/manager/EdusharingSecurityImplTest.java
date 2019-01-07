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
package org.olat.modules.edusharing.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.Test;

/**
 * 
 * Initial date: 4 Dec 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdusharingSecurityImplTest {
	
	EdusharingSecurityServiceImpl sut = new EdusharingSecurityServiceImpl();

	@Test
	public void shouldVerifyTrue() throws Exception {
		KeyPair keys = sut.generateKeys();
		String data = "123";

		byte[] signed = sut.sign(keys.getPrivate(), data);
		boolean verify = sut.verify(keys.getPublic(), signed, data);
		
		assertThat(verify).isTrue();
	}
	
	@Test
	public void shouldVerifyFalse() throws Exception {
		KeyPair keys = sut.generateKeys();
		String data = "123";
		String other = "ABC";
		
		byte[] signed = sut.sign(keys.getPrivate(), data);
		boolean verify = sut.verify(keys.getPublic(), signed, other);
		
		assertThat(verify).isFalse();
	}
	
	@Test
	public void shouldSerializeAndDeserializeKeys() throws GeneralSecurityException {
		String data = "123";
		KeyPair keys = sut.generateKeys();
		String privateKeyString = sut.getPrivateKey(keys);
		PrivateKey privateKey = sut.toPrivateKey(privateKeyString);
		String publicKeyString = sut.getPublicKey(keys);
		PublicKey publicKey = sut.toPublicKey(publicKeyString);
		
		byte[] signed = sut.sign(privateKey, data);
		boolean verify = sut.verify(publicKey, signed, data);
		
		assertThat(verify).isTrue();
	}
	
	@Test
	public void shouldEncryptAndDecrypt() throws NoSuchAlgorithmException {
		String plain = "plaintext";
		KeyPair keys = sut.generateKeys();
		
		String encrypted = sut.encrypt(keys.getPublic(), plain);
		String decrypted = sut.decrypt(keys.getPrivate(), encrypted);
		
		assertThat(decrypted).isEqualTo(plain);
	}


}
