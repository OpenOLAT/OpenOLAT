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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.util.httpclient.filter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HttpCoreContext;
import org.junit.Assert;
import org.junit.Test;

/**
 * Checks the last line of defense of the SSRF mitigation, the interceptor which
 * validates the target host of a request which was not resolved by OpenOlat itself.
 *
 * Initial date: 13 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class FilteringHostRequestInterceptorTest {

	private static final InetAddressFilter externalOnly = InetAddressFilter.externalAddresses();

	private final FilteringHostRequestInterceptor interceptor
		= new FilteringHostRequestInterceptor(externalOnly, Set.of());

	@Test
	public void literalInternalIpv4AddressesAreRejected() {
		String[] addresses = new String[] {
				"127.0.0.1", "10.1.2.3", "172.16.5.5", "192.168.1.1",
				"169.254.169.254",	// Cloud metadata service
				"100.64.1.1"
			};
		for(String address:addresses) {
			HttpHost target = new HttpHost(address);
			Assert.assertThrows(address, FilteredHostException.class, () -> process(interceptor, target));
		}
	}

	@Test
	public void literalInternalIpv6AddressesAreRejected() {
		String[] addresses = new String[] { "::1", "[::1]", "fd00::1", "[fe80::1]" };
		for(String address:addresses) {
			HttpHost target = new HttpHost(address);
			Assert.assertThrows(address, FilteredHostException.class, () -> process(interceptor, target));
		}
	}

	@Test
	public void literalPublicAddressesAreAccepted() throws IOException {
		String[] addresses = new String[] { "8.8.8.8", "1.1.1.1", "2606:4700:4700::1111", "[2606:4700:4700::1111]" };
		for(String address:addresses) {
			process(interceptor, new HttpHost(address));
		}
	}

	/**
	 * Host names are deliberately not resolved here, the {@link FilteringDnsResolver}
	 * is the one which checks them right before the socket is opened.
	 */
	@Test
	public void hostNamesAreLeftToTheDnsResolver() throws IOException {
		process(interceptor, new HttpHost("localhost"));
		process(interceptor, new HttpHost("internal.example.org"));
	}

	/**
	 * Documents a limitation of the interceptor: only the canonical notations are
	 * recognized as literal addresses by {@code InetAddressUtils}. The following
	 * notations all point to the loopback or to this network but are treated as host
	 * names here and are only rejected later by the {@link FilteringDnsResolver}.
	 */
	@Test
	public void nonCanonicalLiteralAddressesAreLeftToTheDnsResolver() throws IOException {
		String[] addresses = new String[] {
				"0.0.0.0",				// This network
				"2130706433",			// Decimal 127.0.0.1
				"127.1",				// Short form of 127.0.0.1
				"0177.0.0.1",			// Octal
				"::ffff:127.0.0.1"		// IPv4 mapped IPv6
			};
		for(String address:addresses) {
			process(interceptor, new HttpHost(address));
		}
	}

	/**
	 * A host which was already resolved, typically because the route was built from an
	 * {@link InetAddress}, carries its address and can be checked here.
	 */
	@Test
	public void resolvedHostWithInternalAddressIsRejected() throws UnknownHostException {
		HttpHost target = new HttpHost(InetAddress.getByName("10.0.0.1"), "internal.example.org", -1, "http");
		FilteredHostException e = Assert.assertThrows(FilteredHostException.class, () -> process(interceptor, target));
		Assert.assertEquals("internal.example.org", e.getHost());
		Assert.assertEquals("10.0.0.1", e.getAddress().getHostAddress());
	}

	@Test
	public void resolvedHostWithPublicAddressIsAccepted() throws IOException {
		HttpHost target = new HttpHost(InetAddress.getByName("8.8.8.8"), "public.example.org", -1, "https");
		process(interceptor, target);
	}

	@Test
	public void httpAndHttpsSchemesAreAccepted() throws IOException {
		process(interceptor, new HttpHost("8.8.8.8", 80, "http"));
		process(interceptor, new HttpHost("8.8.8.8", 443, "https"));
		process(interceptor, new HttpHost("8.8.8.8", 443, "HTTPS"));
	}

	@Test
	public void otherSchemesAreRejected() {
		String[] schemes = new String[] { "file", "ftp", "gopher", "jar" };
		for(String scheme:schemes) {
			HttpHost target = new HttpHost("public.example.org", -1, scheme);
			Assert.assertThrows(scheme, FilteredHostException.class, () -> process(interceptor, target));
		}
	}

	/**
	 * The scheme is validated before the host is exempted, an exemption must not open
	 * the door to the other protocols.
	 */
	@Test
	public void exemptedHostDoesNotBypassTheSchemeCheck() {
		FilteringHostRequestInterceptor exempting
			= new FilteringHostRequestInterceptor(externalOnly, Set.of("trust.example.org"));
		HttpHost target = new HttpHost("trust.example.org", -1, "file");
		Assert.assertThrows(FilteredHostException.class, () -> process(exempting, target));
	}

	@Test
	public void exemptedHostIsAccepted() throws IOException {
		FilteringHostRequestInterceptor exempting
			= new FilteringHostRequestInterceptor(externalOnly, Set.of("trust.example.org"));
		process(exempting, new HttpHost(InetAddress.getByName("10.0.0.1"), "trust.example.org", -1, "http"));

		HttpHost target = new HttpHost(InetAddress.getByName("10.0.0.1"), "other.example.org", -1, "http");
		Assert.assertThrows(FilteredHostException.class, () -> process(exempting, target));
	}

	/**
	 * Host names are compared case insensitively, the ones from the configuration as
	 * well as the ones of the requests.
	 */
	@Test
	public void exemptedHostIsCaseInsensitive() throws IOException {
		FilteringHostRequestInterceptor exempting
			= new FilteringHostRequestInterceptor(externalOnly, Set.of("Trust.Example.Org"));
		process(exempting, new HttpHost(InetAddress.getByName("10.0.0.1"), "TRUST.EXAMPLE.ORG", -1, "http"));
	}

	@Test
	public void exemptedLiteralAddressIsAccepted() throws IOException {
		FilteringHostRequestInterceptor exempting
			= new FilteringHostRequestInterceptor(externalOnly, Set.of("127.0.0.1"));
		process(exempting, new HttpHost("127.0.0.1"));

		Assert.assertThrows(FilteredHostException.class, () -> process(exempting, new HttpHost("10.0.0.1")));
	}

	/**
	 * The addresses of the allow list are accepted although they are not public.
	 */
	@Test
	public void allowedAddressesAreAccepted() throws IOException {
		FilteringHostRequestInterceptor allowing
			= new FilteringHostRequestInterceptor(externalOnly.or("10.0.0.0/8"), Set.of());
		process(allowing, new HttpHost("10.1.2.3"));

		Assert.assertThrows(FilteredHostException.class, () -> process(allowing, new HttpHost("192.168.1.1")));
	}

	/**
	 * Without a target host there is nothing to validate, the request is handled further
	 * down the stack.
	 */
	@Test
	public void missingTargetHostIsIgnored() throws IOException {
		HttpRequest request = new HttpGet("http://public.example.org/feed.xml");
		interceptor.process(request, HttpCoreContext.create());
	}

	private void process(FilteringHostRequestInterceptor hostInterceptor, HttpHost target) throws IOException {
		HttpCoreContext context = HttpCoreContext.create();
		context.setTargetHost(target);
		HttpRequest request = new HttpGet("/feed.xml");
		hostInterceptor.process(request, context);
	}

}
