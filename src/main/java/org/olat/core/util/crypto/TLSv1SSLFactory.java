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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 18 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TLSv1SSLFactory extends SSLSocketFactory {
	
	private static final Logger log = Tracing.createLoggerFor(TLSv1SSLFactory.class);
	
	private SSLSocketFactory factoryDelegate;
	private static TLSv1SSLFactory tlsv1Factory;
	
	public TLSv1SSLFactory() {
		try {
			factoryDelegate = SSLContext.getDefault().getSocketFactory();
		} catch (NoSuchAlgorithmException e) {
			log.error("", e);
		}
	}
	
	public static synchronized SocketFactory getDefault() {
		if(tlsv1Factory == null) {
			tlsv1Factory = new TLSv1SSLFactory();
		}
		return tlsv1Factory;
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return factoryDelegate.getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return factoryDelegate.getSupportedCipherSuites();
	}

	@Override
	public Socket createSocket(Socket s, InputStream consumed, boolean autoClose) throws IOException {
		return decorateSSLSocket(factoryDelegate.createSocket(s, consumed, autoClose));
	}

	@Override
	public Socket createSocket() throws IOException {
		return decorateSSLSocket(factoryDelegate.createSocket());
	}

	@Override
	public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
		return decorateSSLSocket(factoryDelegate.createSocket(s, host, port, autoClose));
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		return decorateSSLSocket(factoryDelegate.createSocket(host, port));
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
	throws IOException {
		return decorateSSLSocket(factoryDelegate.createSocket(host, port, localHost, localPort));
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		return decorateSSLSocket(factoryDelegate.createSocket(host, port));
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
	throws IOException {
		return decorateSSLSocket(factoryDelegate.createSocket(address, port, localAddress, localPort));
	}
	
	private Socket decorateSSLSocket(Socket socket) {
		if(socket instanceof SSLSocket) {
			SSLSocket sslSocket = (SSLSocket)socket;
			sslSocket.setEnabledProtocols(new String[] { "TLSv1" });
		}
		return socket;
	}
}
