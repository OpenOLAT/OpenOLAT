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
package org.olat.core.util.http;

import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.HandshakeRequest;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 9 oct. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OriginValidator {
	
	private final Set<String> allowedHosts; // exact hosts, lower case
	private final Set<String> allowedSuffixes; // ".example.com" for *.example.com
	private final boolean requireHttps;
	
	public OriginValidator(Collection<String> domains, boolean requireHttps) {
		this.requireHttps = requireHttps;
		Set<String> hosts = new HashSet<>();
		Set<String> suffixes = new HashSet<>();
		for(String domain:domains) {
			String d = domain.trim().toLowerCase(Locale.ROOT);
			if(d.startsWith("*.")) {
				suffixes.add("." + IDN.toASCII(d.substring(2)).toLowerCase(Locale.ROOT)); // keep the leading .
			} else if(!d.isEmpty()) {
				hosts.add(IDN.toASCII(d).toLowerCase(Locale.ROOT));
			}
		}
		allowedHosts = Set.copyOf(hosts);
		allowedSuffixes = Set.copyOf(suffixes);
	}

	public boolean isAllowed(HttpServletRequest request) {
		String origin = request.getHeader("Origin");
		if(origin == null) {
			// Fallback for same-origin GETs / older browsers
			origin = request.getHeader("Referer");
		}
		return isAllowed(origin);
	}
	
	public boolean isAllowed(HandshakeRequest request) {
		List<String> origins = request.getHeaders().get("Origin");
		return origins == null || origins.isEmpty()
				? false
				: isAllowed(origins.get(0));
	}
	
	public boolean isAllowed(String origin) {
		if(!StringHelper.containsNonWhitespace(origin)) {
			return false;
		}
		try {
			URI uri = new URI(origin.trim());
			String scheme = uri.getScheme();
			String host = uri.getHost();
			if(scheme == null || host == null || uri.getRawUserInfo() != null) {
				return false; // reject "https://good.com@evil.com
			}
			scheme = scheme.toLowerCase(Locale.ROOT);
			if(!"https".equals(scheme) && (requireHttps || !"http".equals(scheme))) {
				return false;
			}
			host = IDN.toASCII(host).toLowerCase(Locale.ROOT);
			if(host.endsWith(".")) {
				host = host.substring(0, host.length() - 1);
			}
			if(allowedHosts.contains(host)) {
				return true;
			}
			for(String suffix:allowedSuffixes) {
				if(host.endsWith(suffix)) {
					return true;
				}
			}
			return false;
		} catch (URISyntaxException | IllegalArgumentException e) {
			return false;
		}
	}
}
