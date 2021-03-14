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
package org.olat.core.util.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Enumeration;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * 
 * Initial date: 16 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CryptoUtil {

	/**
	 * 
	 * @param certificate
	 * @return
	 * @throws Exception
	 */
	public static X509CertificatePrivateKeyPair getX509CertificatePrivateKeyPairPfx(File certificate, String password) throws Exception {
		KeyStore keyStore;
		if(certificate.getName().equals("cacerts")) {
			keyStore = KeyStore.getInstance("JKS");
		} else  {
			keyStore = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME);
		}
		keyStore.load(new FileInputStream(certificate), password.toCharArray());

		PrivateKey privateKey = null;
        X509Certificate x509Cert = null;
		
		Enumeration<String> aliases = keyStore.aliases();
		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();
			Certificate cert = keyStore.getCertificate(alias);
			if (cert instanceof X509Certificate) {
				x509Cert = (X509Certificate)cert;
				Key key = keyStore.getKey(alias, null);
				if(key instanceof PrivateKey) {
					privateKey = (PrivateKey)key;
				}
				break;
			}
		}

		return new X509CertificatePrivateKeyPair(x509Cert, privateKey);
	}
	
	public static PublicKey string2PublicKey(String keyString) {
		if ( keyString == null ) return null;
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");

			keyString = stripPKCS8(keyString);
			X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(keyString));
			return kf.generatePublic(keySpecX509);
		} catch (IllegalArgumentException | InvalidKeySpecException e) {
			return null;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static PrivateKey string2PrivateKey(String keyString) {
		if ( keyString == null ) return null;
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");

			keyString = stripPKCS8(keyString);
			PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyString.getBytes()));
			return kf.generatePrivate(keySpecPKCS8);
		} catch (IllegalArgumentException | InvalidKeySpecException e) {
			return null;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String stripPKCS8(String input) {
		if (input == null) {
			return input;
		}
		if (!input.startsWith("-----BEGIN")) {
			return input;
		}
		String[] lines = input.split("\n");
		String retval = "";
		for (String line : lines) {
			if (line.startsWith("----")) {
				continue;
			}
			retval = retval + line.trim();
		}
		return retval;
	}
	
	public static String getKeyBase64(Key key) {
		byte[] encodeArray = key.getEncoded();
		return base64Encode(encodeArray);
	}
	
	public static String getPublicEncoded(Key key) {
		return getPublicEncoded(getKeyBase64(key));
	}
	
	public static String getPublicEncoded(String keyStr) {
		// Don't double convert
		if(keyStr.startsWith("-----BEGIN ")) return keyStr;

		return "-----BEGIN PUBLIC KEY-----\n"
				+ breakKeyIntoLines(keyStr)
				+ "\n-----END PUBLIC KEY-----\n";
	}

	public static String getPrivateEncoded(Key key) {
		return getPrivateEncoded(getKeyBase64(key));
	}
	
	public static String getPrivateEncoded(String keyStr) {
		// Don't double convert
		if(keyStr.startsWith("-----BEGIN ")) return keyStr;

		return "-----BEGIN PRIVATE KEY-----\n"
				+ breakKeyIntoLines(keyStr)
				+ "\n-----END PRIVATE KEY-----\n";
	}
	
	public static String base64Encode(byte[] input) {
		Base64.Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(input);
	}
	
	public static String breakKeyIntoLines(String rawkey) {
		int len = 65;
		StringBuilder ret = new StringBuilder();

		String trimmed = rawkey.trim();
		for (int i = 0; i < trimmed.length(); i += len) {
			int end = i + len;
			if (ret.length() > 0) {
				ret.append("\n");
			}
			if (end > trimmed.length()) {
				end = trimmed.length();
			}
			ret.append(trimmed.substring(i, end));
		}
		return ret.toString();
	}
}
