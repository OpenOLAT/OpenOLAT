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
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.conn.DnsResolver;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * A {@link DnsResolver} which rejects hosts resolving to an address which is not
 * accepted by the configured {@link InetAddressFilter}.
 * <p>
 * This is the hook Apache HttpClient uses right before opening the socket
 * ({@code DefaultHttpClientConnectionOperator} connects to exactly the addresses
 * returned here), which means the check cannot be circumvented by DNS rebinding and
 * is applied again to every hop of a redirect chain.
 * <p>
 * A host is rejected as soon as <em>one</em> of its addresses is filtered out, so that
 * a host with both a public and a private A record cannot be used to reach the
 * internal network.
 *
 * Initial date: 11 Aug 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class FilteringDnsResolver implements DnsResolver {

	private static final Logger log = Tracing.createLoggerFor(FilteringDnsResolver.class);

	private final DnsResolver delegate;
	private final InetAddressFilter filter;
	private final Set<String> exemptedHosts;

	public FilteringDnsResolver(InetAddressFilter filter, Collection<String> exemptedHosts) {
		this(SystemDefaultDnsResolver.INSTANCE, filter, exemptedHosts);
	}

	public FilteringDnsResolver(DnsResolver delegate, InetAddressFilter filter, Collection<String> exemptedHosts) {
		this.delegate = delegate;
		this.filter = filter;
		this.exemptedHosts = exemptedHosts == null
				? Set.of()
				: exemptedHosts.stream()
					.map(host -> host.toLowerCase(Locale.ROOT))
					.collect(Collectors.toSet());
	}

	@Override
	public InetAddress[] resolve(String host) throws UnknownHostException {
		InetAddress[] addresses = delegate.resolve(host);
		if(host != null && exemptedHosts.contains(host.toLowerCase(Locale.ROOT))) {
			return addresses;
		}
		if(addresses == null || addresses.length == 0) {
			throw new UnknownHostException(host);
		}

		for(InetAddress address:addresses) {
			if(!filter.matches(address)) {
				log.warn("Blocked outgoing request to {} which resolves to the filtered address {}",
						host, address.getHostAddress());
				throw new FilteredHostException(host, address);
			}
		}
		return addresses;
	}

}
