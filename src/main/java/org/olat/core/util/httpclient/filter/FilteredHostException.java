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
package org.olat.core.util.httpclient.filter;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Thrown when a host was rejected by an {@link InetAddressFilter}, typically because
 * it resolves to an address which is not allowed to be called (SSRF mitigation).
 * <p>
 * Extends {@link UnknownHostException} so that it can be thrown from a
 * {@link org.apache.http.conn.DnsResolver} and travels through the Apache HttpClient
 * stack like any other resolution failure.
 *
 * Initial date: 11 Aug 2026<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FilteredHostException extends UnknownHostException {

	private static final long serialVersionUID = 6172390125476721903L;

	private final String host;
	private final InetAddress address;

	public FilteredHostException(String host, InetAddress address) {
		super("Host " + host + " is not allowed to be called"
				+ (address == null ? "" : " (resolved to " + address.getHostAddress() + ")"));
		this.host = host;
		this.address = address;
	}

	/**
	 * @return The host name (or literal IP) of the rejected request
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return The rejected address, can be null
	 */
	public InetAddress getAddress() {
		return address;
	}

}
