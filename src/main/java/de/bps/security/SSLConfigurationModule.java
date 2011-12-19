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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.security;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

public class SSLConfigurationModule {
	
	private static final OLog log = Tracing.createLoggerFor(SSLConfigurationModule.class);

	private static String keyStoreFile;
	private static String keyStorePass;
	private static String keyStoreType;
	private static String trustStoreFile;
	private static String trustStorePass;
	private static String trustStoreType;
	private boolean enableSsl;


	public boolean isEnableSsl() {
		return enableSsl;
	}

	public void setEnableSsl(boolean enableSsl) {
		this.enableSsl = enableSsl;
	}

	/**
	 * @param keyStoreFile The keyStoreFile to set.
	 */
	public void setKeyStoreFile(String keyStoreFile) {
		SSLConfigurationModule.keyStoreFile = keyStoreFile;
	}

	/**
	 * @param keyStorePass The keyStorePass to set.
	 */
	public void setKeyStorePass(String keyStorePass) {
		SSLConfigurationModule.keyStorePass = keyStorePass;
	}

	/**
	 * @param keyStoreType The keyStoreType to set.
	 */
	public void setKeyStoreType(String keyStoreType) {
		SSLConfigurationModule.keyStoreType = keyStoreType;
	}

	/**
	 * @param trustStoreFile The trustStoreFile to set.
	 */
	public void setTrustStoreFile(String trustStoreFile) {
		SSLConfigurationModule.trustStoreFile = trustStoreFile;
	}

	/**
	 * @param trustStorePass The trustStorePass to set.
	 */
	public void setTrustStorePass(String trustStorePass) {
		SSLConfigurationModule.trustStorePass = trustStorePass;
	}

	/**
	 * @param trustStoreType The trustStoreType to set.
	 */
	public void setTrustStoreType(String trustStoreType) {
		SSLConfigurationModule.trustStoreType = trustStoreType;
	}

	public SSLConfigurationModule() {
		super();
	}

	public static String getKeyStoreFile() {
		return keyStoreFile;
	}

	public static String getKeyStorePass() {
		return keyStorePass;
	}

	public static String getKeyStoreType() {
		return keyStoreType;
	}

	public static String getTrustStoreFile() {
		return trustStoreFile;
	}

	public static String getTrustStorePass() {
		return trustStorePass;
	}

	public static String getTrustStoreType() {
		return trustStoreType;
	}

	/**
	 * 
	 * @see org.olat.core.configuration.Initializable#init()
	 */
	public void init() {
		if(isEnableSsl()) {
			System.setProperty("javax.net.ssl.trustStore", SSLConfigurationModule.getTrustStoreFile());
			System.setProperty("javax.net.ssl.trustStorePassword", SSLConfigurationModule.getTrustStorePass());
			System.setProperty("javax.net.ssl.keyStore", SSLConfigurationModule.getKeyStoreFile());
			System.setProperty("javax.net.ssl.keyStorePassword", SSLConfigurationModule.getKeyStorePass());
			log.info("Overwrite the standard javax.net.ssl settings with custom ones!");
		}
	}

	public static KeyManager[] getKeyManagers() {
		try {
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			FileInputStream kStream = new FileInputStream(keyStoreFile);
			keyStore.load(kStream, keyStorePass.toCharArray());
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, keyStorePass.toCharArray());
			return keyManagerFactory.getKeyManagers();
		} catch (Exception e) {
			log.error("Error while initializing the keystore", e);
			return null;
		}
	}

	public static TrustManager[] getTrustManagers() {
		try {
			KeyStore trustStore = KeyStore.getInstance(trustStoreType);
			FileInputStream tStream = new FileInputStream(trustStoreFile);
			trustStore.load(tStream, trustStorePass.toCharArray());
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
			trustManagerFactory.init(trustStore);
			return trustManagerFactory.getTrustManagers();
		} catch (Exception e) {
			log.error("Error while initializing the truststore", e);
			return null;
		}
	}
}
