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

import org.apache.hc.client5.http.RouteInfo;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.InetAddressUtils;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * Request interceptor for Apache HttpClient 5 which validates the scheme and the
 * target of every request, redirects included (SSRF mitigation).
 * <p>
 * It covers the cases which the {@link FilteringDnsResolver} doesn't see: literal
 * IP addresses, routes built from an already resolved {@link InetAddress},
 * requests sent through a proxy, where the target is resolved by the proxy, and
 * routes through a Unix domain socket.
 *
 * Initial date: 17 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
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
	public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws IOException {
		RouteInfo route = context == null ? null : HttpClientContext.castOrCreate(context).getHttpRoute();
		HttpHost routeTarget = route == null ? null : route.getTargetHost();
		// A Unix domain socket is a local endpoint which bypasses the DNS resolver
		if(route != null && route.getUnixDomainSocket() != null) {
			log.warn("Blocked outgoing request through the Unix domain socket {}", route.getUnixDomainSocket());
			throw new FilteredHostException(route.getUnixDomainSocket().toString(), null);
		}

		String scheme = request.getScheme();
		URIAuthority authority = request.getAuthority();
		if(scheme == null && routeTarget != null) {
			scheme = routeTarget.getSchemeName();
		}
		String hostName = authority != null ? authority.getHostName()
				: (routeTarget != null ? routeTarget.getHostName() : null);
		if(hostName == null) {
			return;
		}

		if(scheme != null && !ALLOWED_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT))) {
			throw new FilteredHostException(hostName, null);
		}

		if(exemptedHosts.contains(hostName.toLowerCase(Locale.ROOT))) {
			return;
		}

		InetAddress address = null;
		if(routeTarget != null && routeTarget.getAddress() != null
				&& hostName.equalsIgnoreCase(routeTarget.getHostName())) {
			address = routeTarget.getAddress();
		}
		if(address == null) {
			address = literalAddress(hostName);
		}
		if(address != null && !filter.matches(address)) {
			log.warn("Blocked outgoing request to the filtered address {}", address.getHostAddress());
			throw new FilteredHostException(hostName, address);
		}
	}

	/**
	 * @param hostName The host name of the target
	 * @return The address if the host name is a literal IP address, null if it is a name
	 * which has to be resolved
	 */
	private InetAddress literalAddress(String hostName) {
		String candidate = hostName;
		if(candidate.startsWith("[") && candidate.endsWith("]")) {
			candidate = candidate.substring(1, candidate.length() - 1);
		}
		if(!InetAddressUtils.isIPv4(candidate) && !InetAddressUtils.isIPv6(candidate)) {
			return null;
		}

		try {
			return InetAddress.getByName(candidate);
		} catch (UnknownHostException e) {
			return null;// Cannot happen with a literal address
		}
	}
}
