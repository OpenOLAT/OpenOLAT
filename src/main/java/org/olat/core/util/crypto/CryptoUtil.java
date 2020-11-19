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
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
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
}
