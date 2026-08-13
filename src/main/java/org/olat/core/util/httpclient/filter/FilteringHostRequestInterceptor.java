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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * Validates the target host of a request before it is sent.
 * <p>
 * The {@link FilteringDnsResolver} is the primary defense, but it is only consulted
 * when OpenOlat resolves the host itself. This interceptor covers the two cases where
 * this does not happen:
 * <ul>
 * <li>the request goes through a forward proxy, which resolves the target host on our
 * behalf</li>
 * <li>the target is a literal IP address carried in an already resolved
 * {@link HttpHost}</li>
 * </ul>
 * Only literal IP addresses are checked here. Host names are left to the DNS resolver
 * because resolving them a second time would be both redundant and racy.
 *
 * Initial date: 11 Aug 2026<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FilteringHostRequestInterceptor implements HttpRequestInterceptor {

	private static final Logger log = Tracing.createLoggerFor(FilteringHostRequestInterceptor.class);

	private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

	private final InetAddressFilter filter;
	private final Set<String> exemptedHosts;

	public FilteringHostRequestInterceptor(InetAddressFilter filter, Collection<String> exemptedHosts) {
		this.filter = filter;
		this.exemptedHosts = exemptedHosts == null
				? Set.of()
				: exemptedHosts.stream()
					.map(host -> host.toLowerCase(Locale.ROOT))
					.collect(Collectors.toSet());
	}

	@Override
	public void process(HttpRequest request, HttpContext context) throws IOException {
		HttpHost target = HttpCoreContext.adapt(context).getTargetHost();
		if(target == null) {
			return;
		}

		String scheme = target.getSchemeName();
		if(scheme != null && !ALLOWED_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT))) {
			throw new FilteredHostException(target.toHostString(), null);
		}
		
		if(target.getHostName() != null && exemptedHosts.contains(target.getHostName().toLowerCase(Locale.ROOT))) {
			return;
		}

		InetAddress address = target.getAddress();
		if(address == null) {
			address = literalAddress(target.getHostName());
		}
		if(address != null && !filter.matches(address)) {
			log.warn("Blocked outgoing request to the filtered address {}", address.getHostAddress());
			throw new FilteredHostException(target.getHostName(), address);
		}
	}

	/**
	 * @param hostName The host name of the target
	 * @return The address if the host name is a literal IP address, null if it is a name
	 * which has to be resolved
	 */
	private InetAddress literalAddress(String hostName) {
		if(hostName == null) {
			return null;
		}

		String candidate = hostName;
		if(candidate.startsWith("[") && candidate.endsWith("]")) {
			candidate = candidate.substring(1, candidate.length() - 1);
		}
		if(!InetAddressUtils.isIPv4Address(candidate) && !InetAddressUtils.isIPv6Address(candidate)) {
			return null;
		}

		try {
			return InetAddress.getByName(candidate);
		} catch (UnknownHostException e) {
			return null;// Cannot happen with a literal address
		}
	}

}
