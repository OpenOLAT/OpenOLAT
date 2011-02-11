
/**
 *
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 *
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package de.bps.security;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.olat.core.configuration.Initializable;
import org.olat.core.logging.Tracing;

public class SSLConfigurationModule implements Initializable {

	private static String keyStoreFile;
	private static String keyStorePass;
	private static String keyStoreType;
	private static String trustStoreFile;
	private static String trustStorePass;
	private static String trustStoreType;

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
		System.setProperty("javax.net.ssl.trustStore", SSLConfigurationModule.getTrustStoreFile());
		System.setProperty("javax.net.ssl.trustStorePassword", SSLConfigurationModule.getTrustStorePass());
		System.setProperty("javax.net.ssl.keyStore", SSLConfigurationModule.getKeyStoreFile());
		System.setProperty("javax.net.ssl.keyStorePassword", SSLConfigurationModule.getKeyStorePass());
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
			Tracing.createLoggerFor(SSLConfigurationModule.class).error("Error while initializing the keystore", e);
			e.printStackTrace();
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
			Tracing.createLoggerFor(SSLConfigurationModule.class).error("Error while initializing the truststore", e);
			return null;
		}
	}
}
