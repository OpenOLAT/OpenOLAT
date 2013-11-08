/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.olat.core.util.httpclient;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

/**
 * This socket factory will create ssl socket that accepts self signed
 * certificate
 * 
 * @author olamy
 * @version $Id: EasySSLSocketFactory.java 765355 2009-04-15 20:59:07Z evenisse
 *          $
 * @since 1.2.3
 */
public class EasySSLSocketFactory implements ConnectionSocketFactory {

	private SSLContext sslcontext = null;

	private static SSLContext createEasySSLContext() throws IOException {
		try {
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new TrustManager[] { new EasyX509TrustManager(null) }, null);
			return context;
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	private SSLContext getSSLContext() throws IOException {
		if (sslcontext == null) {
			sslcontext = createEasySSLContext();
		}
		return sslcontext;
	}

	/**
	 * @see org.apache.http.conn.scheme.SocketFactory#createSocket()
	 */
	public Socket createSocket() throws IOException {
		return getSSLContext().getSocketFactory().createSocket();
	}

	// -------------------------------------------------------------------
	// javadoc in org.apache.http.conn.scheme.SocketFactory says :
	// Both Object.equals() and Object.hashCode() must be overridden
	// for the correct operation of some connection managers
	// -------------------------------------------------------------------
	@Override
	public boolean equals(Object obj) {
		return ((obj != null) && obj.getClass().equals(
				EasySSLSocketFactory.class));
	}
	
	@Override
	public int hashCode() {
		return EasySSLSocketFactory.class.hashCode();
	}

	@Override
	public Socket createSocket(HttpContext context) throws IOException {
		return createSocket();
	}

	@Override
	public Socket connectSocket(int connectTimeout, Socket sock, HttpHost host,
			InetSocketAddress remoteAddress, InetSocketAddress localAddress,
			HttpContext context) throws IOException {

		SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock : createSocket());

		int localPort = 0;
		if (localAddress != null) {
			// we need to bind explicitly
			if (localAddress.getPort() < 0) {
				localPort = 0; // indicates "any"
			}
			InetSocketAddress isa = new InetSocketAddress(localAddress.getAddress(), localPort);
			sslsock.bind(isa);
		}

		sslsock.connect(remoteAddress, 10000);
		sslsock.setSoTimeout(10000);
		return sslsock;
	}
}