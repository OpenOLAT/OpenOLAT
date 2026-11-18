/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.util.httpclient.filter;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * DNS resolver for Apache HttpClient 5 which rejects the host names resolving
 * to a filtered address (SSRF mitigation).
 * <p>
 * The resolver is set on the connection manager and the addresses it returns are
 * the ones the client connects to. Filtering here is therefore immune to DNS
 * rebinding and covers every hop of a redirect chain.
 * <p>
 * HttpClient 5 calls {@link #resolve(String, int)} to open a connection. The
 * default implementation of the interface delegates to {@link #resolve(String)},
 * but both are overridden to not depend on it.
 *
 * Initial date: 17 nov. 2026<br>
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
		if(isExempted(host)) {
			return addresses;
		}
		if(addresses == null || addresses.length == 0) {
			throw new UnknownHostException(host);
		}

		for(InetAddress address:addresses) {
			check(host, address);
		}
		return addresses;
	}

	@Override
	public List<InetSocketAddress> resolve(String host, int port) throws UnknownHostException {
		List<InetSocketAddress> socketAddresses = delegate.resolve(host, port);
		if(isExempted(host)) {
			return socketAddresses;
		}
		if(socketAddresses == null || socketAddresses.isEmpty()) {
			throw new UnknownHostException(host);
		}

		List<InetSocketAddress> checked = new ArrayList<>(socketAddresses.size());
		for(InetSocketAddress socketAddress:socketAddresses) {
			// An unresolved address would be resolved later by the socket, unchecked
			if(socketAddress.isUnresolved() || socketAddress.getAddress() == null) {
				throw new UnknownHostException(host);
			}
			check(host, socketAddress.getAddress());
			checked.add(socketAddress);
		}
		return checked;
	}

	@Override
	public String resolveCanonicalHostname(String host) throws UnknownHostException {
		return delegate.resolveCanonicalHostname(host);
	}

	private boolean isExempted(String host) {
		return host != null && exemptedHosts.contains(host.toLowerCase(Locale.ROOT));
	}

	private void check(String host, InetAddress address) throws FilteredHostException {
		if(!filter.matches(address)) {
			log.warn("Blocked outgoing request to {} which resolves to the filtered address {}",
					host, address.getHostAddress());
			throw new FilteredHostException(host, address);
		}
	}
}
