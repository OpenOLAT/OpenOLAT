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
import java.util.Set;

import org.apache.http.conn.DnsResolver;
import org.junit.Assert;
import org.junit.Test;

/**
 * Checks the SSRF mitigation of the HTTP clients.
 *
 * Initial date: 11 Aug 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class FilteringDnsResolverTest {

	private static final InetAddressFilter externalOnly = InetAddressFilter.externalAddresses();
	
	@Test
	public void internalAddressesAreFiltered() throws UnknownHostException {
		String[] addresses = new String[] {
				"127.0.0.1", "::1", "::ffff:127.0.0.1", "2130706433",
				"10.1.2.3", "172.16.5.5", "192.168.1.1",
				"169.254.169.254",	// Cloud metadata service
				"0.0.0.0", "100.64.1.1", "224.0.0.1",
				"fd00::1", "fe80::1",
				"64:ff9b::7f00:1"	// NAT64 mapped loopback
			};
		for(String address:addresses) {
			Assert.assertFalse(address, externalOnly.matches(InetAddress.getByName(address)));
		}
	}

	@Test
	public void publicAddressesAreAccepted() throws UnknownHostException {
		String[] addresses = new String[] { "8.8.8.8", "1.1.1.1", "2606:4700:4700::1111" };
		for(String address:addresses) {
			Assert.assertTrue(address, externalOnly.matches(InetAddress.getByName(address)));
		}
	}

	@Test
	public void allowedAddressesAreAccepted() throws UnknownHostException {
		InetAddressFilter filter = InetAddressFilter.externalAddresses().or("10.0.0.0/8");
		Assert.assertTrue(filter.matches(InetAddress.getByName("10.1.2.3")));
		Assert.assertFalse(filter.matches(InetAddress.getByName("127.0.0.1")));
		Assert.assertFalse(filter.matches(InetAddress.getByName("192.168.1.1")));
	}

	@Test
	public void resolverRejectsInternalHost() {
		DnsResolver resolver = new FilteringDnsResolver(dns("10.0.0.1"), externalOnly, Set.of());
		Assert.assertThrows(FilteredHostException.class, () -> resolver.resolve("internal.example.org"));
	}

	@Test
	public void resolverAcceptsPublicHost() throws UnknownHostException {
		DnsResolver resolver = new FilteringDnsResolver(dns("8.8.8.8"), externalOnly, Set.of());
		Assert.assertEquals(1, resolver.resolve("public.example.org").length);
	}

	/**
	 * A host with a public and an internal address must not be used to reach the
	 * internal network.
	 */
	@Test
	public void resolverRejectsPartiallyInternalHost() {
		DnsResolver resolver = new FilteringDnsResolver(dns("8.8.8.8", "127.0.0.1"), externalOnly, Set.of());
		Assert.assertThrows(FilteredHostException.class, () -> resolver.resolve("mixed.example.org"));
	}

	/**
	 * The configured proxy is typically an internal host, requests are routed to it and
	 * not to the final target.
	 */
	@Test
	public void resolverAcceptsExemptedProxy() throws UnknownHostException {
		DnsResolver resolver = new FilteringDnsResolver(dns("10.0.0.1"), externalOnly, Set.of("proxy.local"));
		Assert.assertEquals(1, resolver.resolve("proxy.local").length);
		Assert.assertThrows(FilteredHostException.class, () -> resolver.resolve("internal.example.org"));
	}

	private DnsResolver dns(String... addresses) {
		return host -> {
			InetAddress[] resolved = new InetAddress[addresses.length];
			for(int i=addresses.length; i-->0; ) {
				resolved[i] = InetAddress.getByName(addresses[i]);
			}
			return resolved;
		};
	}

}
