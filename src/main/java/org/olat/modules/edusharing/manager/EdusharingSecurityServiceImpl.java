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

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.olat.modules.edusharing.EdusharingException;
import org.olat.modules.edusharing.EdusharingModule;
import org.olat.modules.edusharing.EdusharingSecurityService;
import org.olat.modules.edusharing.EdusharingSignature;
import org.olat.modules.edusharing.model.EdusharingSignatureImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 4 Dec 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EdusharingSecurityServiceImpl implements EdusharingSecurityService {

	private static final String ALGORITHM_KEY = "RSA";
	private static final String ALGORITHM_SIGNATURE = "SHA1withRSA";
	// This algorithm depends in the crypto library used by edu-sharing.
	// As of today edu-sharing uses SunJCE.
	private static final String ALGORITHM_ENCRYPTION = "RSA/ECB/PKCS1Padding";
	
	@Autowired
	private EdusharingModule edusharinModule;
	
	@Override
	public EdusharingSignature createSignature() throws EdusharingException {
		String soapAppId = edusharinModule.getAppId();
		String timeStamp = new Long(System.currentTimeMillis()).toString();
		String signData = soapAppId + timeStamp;
		
		PrivateKey privateKey = edusharinModule.getSoapKeys().getPrivate();
		byte[] signatur = sign(privateKey, signData);
		signatur = new Base64().encode(signatur);
		return new EdusharingSignatureImpl(soapAppId, timeStamp, signData, new String(signatur));
	}
	
	@Override
	public byte[] sign(PrivateKey privateKey, String data) throws EdusharingException {
		try {
			Signature dsa = Signature.getInstance(ALGORITHM_SIGNATURE);
			dsa.initSign(privateKey);
			dsa.update(data.getBytes());
			byte[] realSig = dsa.sign();
			return realSig;
		} catch (Exception e) {
			throw new EdusharingException(e);
		}
	}
	
	@Override
	public boolean verify(PublicKey publicKey, byte[] realSig, String data) throws EdusharingException {
		try {
			Signature dsa = Signature.getInstance(ALGORITHM_SIGNATURE);
			dsa.initVerify(publicKey);
			dsa.update(data.getBytes());
			return dsa.verify(realSig);
		} catch (Exception e) {
			throw new EdusharingException(e);
		}
	}
	
	@Override
	public String encrypt(PublicKey publicKey, String plain) throws EdusharingException {
		try {
			byte[] plainBytes = plain.getBytes();
			Cipher cipher = Cipher.getInstance(ALGORITHM_ENCRYPTION);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] encryptedBytes = cipher.doFinal(plainBytes);
			return Base64.encodeBase64String(encryptedBytes);
		} catch (Exception e) {
			throw new EdusharingException(e);
		}
	}
	
	@Override
	public String decrypt(PrivateKey privateKey, String encrypted) throws EdusharingException {
		 try {
			byte[] encryptedBytes = Base64.decodeBase64(encrypted.getBytes());
			Cipher chiper = Cipher.getInstance(ALGORITHM_ENCRYPTION);
			chiper.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] plainBytes = chiper.doFinal(encryptedBytes);
			return new String(plainBytes, "UTF-8");
		 } catch (Exception e) {
			throw new EdusharingException(e);
		 }
	}
	
	@Override
	public KeyPair generateKeys() throws EdusharingException {
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_KEY);
			keyPairGenerator.initialize(2048);
			KeyPair pair = keyPairGenerator.genKeyPair();
			return pair;
		} catch (Exception e) {
			throw new EdusharingException(e);
		}
	}
	
	@Override
	public String getPublicKey(KeyPair kp) {
		byte[] encoded = kp.getPublic().getEncoded();
		Base64 base64 = new Base64();
		byte[] encode = base64.encode(encoded);
		String key = new String(encode);
		
		return new StringBuilder()
				.append("-----BEGIN PUBLIC KEY-----\n")
				.append(key)
				.append("-----END PUBLIC KEY-----")
				.toString();
	}
	
	@Override
	public PublicKey toPublicKey(String publicKey) throws EdusharingException {
		try {
			String cleanedKey = publicKey.replace("-----BEGIN PUBLIC KEY-----", "");
			cleanedKey = cleanedKey.replace("\n", "");
			cleanedKey = cleanedKey.replace("-----END PUBLIC KEY-----", "");

			Base64 b64 = new Base64();
			byte[] decoded = b64.decode(cleanedKey.getBytes());

			X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
			KeyFactory kf = KeyFactory.getInstance(ALGORITHM_KEY);
			return kf.generatePublic(spec);
		} catch (Exception e) {
			throw new EdusharingException(e);
		}
	}
	
	@Override
	public String getPrivateKey(KeyPair kp) {
		byte[] encoded = kp.getPrivate().getEncoded();
		Base64 base64 = new Base64();
		byte[] encode = base64.encode(encoded);
		String key = new String(encode);
		
		return new StringBuilder()
				.append("-----BEGIN PRIVATE KEY-----\n")
				.append(key)
				.append("-----END PRIVATE KEY-----")
				.toString();
	}
	
	@Override
	public PrivateKey toPrivateKey(String privateKey) throws EdusharingException {
		try {
			String cleanedKey = privateKey.replace("-----BEGIN PRIVATE KEY-----", "");
			cleanedKey = cleanedKey.replace("\n", "");
			cleanedKey = cleanedKey.replace("-----END PRIVATE KEY-----", "");
			cleanedKey = cleanedKey.trim();
			
			Base64 b64 = new Base64();
			byte[] decoded = b64.decode(cleanedKey.getBytes());

			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
			KeyFactory kf = KeyFactory.getInstance(ALGORITHM_KEY);
			return kf.generatePrivate(spec);
		} catch (Exception e) {
			throw new EdusharingException(e);
		}
	}

}
