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
import java.nio.file.Path;
import java.util.Set;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Checks the request interceptor of the SSRF mitigation for Apache HttpClient 5,
 * which validates the target host of a request not resolved by OpenOlat itself.
 *
 * Initial date: 17 nov. 2026<br>
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
				"100.64.1.1",
				"0.0.0.0"			// This network, a literal for HttpClient 5 (not for HttpClient 4)
			};
		for(String address:addresses) {
			HttpHost target = host(address);
			Assert.assertThrows(address, FilteredHostException.class, () -> process(interceptor, target));
		}
	}

	@Test
	public void literalInternalIpv6AddressesAreRejected() {
		String[] addresses = new String[] { "::1", "[::1]", "fd00::1", "[fe80::1]" };
		for(String address:addresses) {
			HttpHost target = host(address);
			Assert.assertThrows(address, FilteredHostException.class, () -> process(interceptor, target));
		}
	}

	@Test
	public void literalPublicAddressesAreAccepted() throws IOException {
		String[] addresses = new String[] { "8.8.8.8", "1.1.1.1", "2606:4700:4700::1111", "[2606:4700:4700::1111]" };
		for(String address:addresses) {
			process(interceptor, host(address));
		}
	}

	/**
	 * Host names are deliberately not resolved here, the {@link FilteringDnsResolver}
	 * is the one which checks them right before the socket is opened.
	 */
	@Test
	public void hostNamesAreLeftToTheDnsResolver() throws IOException {
		process(interceptor, host("localhost"));
		process(interceptor, host("internal.example.org"));
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
				"2130706433",			// Decimal 127.0.0.1
				"127.1",				// Short form of 127.0.0.1
				"0177.0.0.1",			// Octal
				"::ffff:127.0.0.1"		// IPv4 mapped IPv6
			};
		for(String address:addresses) {
			process(interceptor, host(address));
		}
	}

	/**
	 * A route which was built from an {@link InetAddress} carries its address
	 * and can be checked here.
	 */
	@Test
	public void resolvedRouteWithInternalAddressIsRejected() throws UnknownHostException {
		HttpHost target = new HttpHost("http", InetAddress.getByName("10.0.0.1"), "internal.example.org", 80);
		FilteredHostException e = Assert.assertThrows(FilteredHostException.class, () -> process(interceptor, target));
		Assert.assertEquals("internal.example.org", e.getHost());
		Assert.assertEquals("10.0.0.1", e.getAddress().getHostAddress());
	}

	@Test
	public void resolvedRouteWithPublicAddressIsAccepted() throws IOException {
		HttpHost target = new HttpHost("https", InetAddress.getByName("8.8.8.8"), "public.example.org", 443);
		process(interceptor, target);
	}

	@Test
	public void httpAndHttpsSchemesAreAccepted() throws IOException {
		process(interceptor, new HttpHost("http", "8.8.8.8", 80));
		process(interceptor, new HttpHost("https", "8.8.8.8", 443));
		process(interceptor, new HttpHost("HTTPS", "8.8.8.8", 443));
	}

	@Test
	public void otherSchemesAreRejected() {
		String[] schemes = new String[] { "file", "ftp", "gopher", "jar" };
		for(String scheme:schemes) {
			HttpHost target = new HttpHost(scheme, "public.example.org", 80);
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
		HttpHost target = new HttpHost("file", "trust.example.org", 80);
		Assert.assertThrows(FilteredHostException.class, () -> process(exempting, target));
	}

	@Test
	public void exemptedHostIsAcceptedCaseInsensitive() throws IOException {
		FilteringHostRequestInterceptor exempting
			= new FilteringHostRequestInterceptor(externalOnly, Set.of("Trust.Example.Org"));
		process(exempting, new HttpHost("http", InetAddress.getByName("10.0.0.1"), "TRUST.EXAMPLE.ORG", 80));

		HttpHost target = new HttpHost("http", InetAddress.getByName("10.0.0.1"), "other.example.org", 80);
		Assert.assertThrows(FilteredHostException.class, () -> process(exempting, target));
	}

	@Test
	public void exemptedLiteralAddressIsAccepted() throws IOException {
		FilteringHostRequestInterceptor exempting
			= new FilteringHostRequestInterceptor(externalOnly, Set.of("127.0.0.1"));
		process(exempting, host("127.0.0.1"));

		Assert.assertThrows(FilteredHostException.class, () -> process(exempting, host("10.0.0.1")));
	}

	@Test
	public void allowedAddressesAreAccepted() throws IOException {
		FilteringHostRequestInterceptor allowing
			= new FilteringHostRequestInterceptor(externalOnly.or("10.0.0.0/8"), Set.of());
		process(allowing, host("10.1.2.3"));

		Assert.assertThrows(FilteredHostException.class, () -> process(allowing, host("192.168.1.1")));
	}

	/**
	 * HttpClient 5 can connect through a Unix domain socket, a local endpoint
	 * which bypasses the DNS resolver.
	 */
	@Test
	public void unixDomainSocketIsRejected() {
		HttpHost target = new HttpHost("http", "public.example.org", 80);
		HttpClientContext context = HttpClientContext.create();
		context.setRoute(new HttpRoute(target, Path.of("/var/run/docker.sock")));
		HttpRequest request = new BasicHttpRequest("GET", target, "/containers/json");
		Assert.assertThrows(FilteredHostException.class, () -> interceptor.process(request, null, context));
	}

	/**
	 * Without a target host there is nothing to validate, the request is handled further
	 * down the stack.
	 */
	@Test
	public void missingTargetHostIsIgnored() throws IOException {
		HttpRequest request = new BasicHttpRequest("GET", "/feed.xml");
		interceptor.process(request, null, HttpClientContext.create());
	}

	private static HttpHost host(String hostName) {
		return new HttpHost("http", hostName, 80);
	}

	/**
	 * Simulates the state of the request when the interceptors are called: the
	 * request has a scheme and an authority, the context holds the route.
	 */
	private void process(FilteringHostRequestInterceptor hostInterceptor, HttpHost target) throws IOException {
		HttpClientContext context = HttpClientContext.create();
		context.setRoute(new HttpRoute(target));
		HttpRequest request = new BasicHttpRequest("GET", target, "/feed.xml");
		hostInterceptor.process(request, null, context);
	}
}
