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
package org.olat.basesecurity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

public class MediaServerModuleTest extends OlatTestCase {

	@Autowired
	private MediaServerModule mediaServerModule;

	private MediaServerMode originalMode;
	private boolean originalYouTubeEnabled;
	private Set<String> originalCustomServerIds;

	@Before
	public void setUp() {
		originalMode = mediaServerModule.getMediaServerMode();
		originalYouTubeEnabled = mediaServerModule.isMediaServerEnabled(MediaServerModule.YOUTUBE_KEY);
		originalCustomServerIds = mediaServerModule.getCustomMediaServers().stream()
				.map(MediaServer::getId).collect(Collectors.toSet());

		mediaServerModule.setMediaServerMode(MediaServerMode.configure);
		mediaServerModule.setMediaServerEnabled(MediaServerModule.YOUTUBE_KEY, true);
		waitMessageAreConsumed();
	}

	@After
	public void tearDown() {
		List<MediaServer> currentServers = new ArrayList<>(mediaServerModule.getCustomMediaServers());
		for (MediaServer server : currentServers) {
			if (!originalCustomServerIds.contains(server.getId())) {
				mediaServerModule.deleteCustomMediaServer(server);
			}
		}
		mediaServerModule.setMediaServerMode(originalMode);
		mediaServerModule.setMediaServerEnabled(MediaServerModule.YOUTUBE_KEY, originalYouTubeEnabled);
		waitMessageAreConsumed();
	}

	@Test
	public void isRestrictedDomain_ssrfBypassViaUserinfo() {
		Assert.assertTrue(mediaServerModule.isRestrictedDomain("https://youtu.be@127.0.0.1:6666"));
	}

	@Test
	public void isRestrictedDomain_normalYouTubeUrl_notRestricted() {
		Assert.assertFalse(mediaServerModule.isRestrictedDomain("https://youtu.be/watch?v=dQw4w9WgXcQ"));
	}

	@Test
	public void isRestrictedDomain_youTubeRoot_notRestricted() {
		Assert.assertFalse(mediaServerModule.isRestrictedDomain("https://youtu.be"));
	}

	@Test
	public void isRestrictedDomain_evilYouTubeDomain_isRestricted() {
		Assert.assertTrue(mediaServerModule.isRestrictedDomain("https://evilyoutu.be"));
	}

	@Test
	public void isRestrictedDomain_youTubeSuffixAttack_isRestricted() {
		Assert.assertTrue(mediaServerModule.isRestrictedDomain("https://youtu.be.attacker.tld"));
	}

	@Test
	public void isRestrictedDomain_internalIp_isRestricted() {
		Assert.assertTrue(mediaServerModule.isRestrictedDomain("https://127.0.0.1"));
	}

	@Test
	public void isRestrictedDomain_localhost_isRestricted() {
		Assert.assertTrue(mediaServerModule.isRestrictedDomain("http://localhost"));
	}

	@Test
	public void isRestrictedDomain_fileScheme_isRestricted() {
		Assert.assertTrue(mediaServerModule.isRestrictedDomain("file:///etc/passwd"));
	}

	@Test
	public void isRestrictedDomain_gopherScheme_isRestricted() {
		Assert.assertTrue(mediaServerModule.isRestrictedDomain("gopher://attacker.tld"));
	}

	@Test
	public void isRestrictedDomain_malformedUrl_isRestricted() {
		Assert.assertTrue(mediaServerModule.isRestrictedDomain("not a url"));
	}

	@Test
	public void isRestrictedDomain_customDomainSubdomain_notRestricted() {
		mediaServerModule.updateCustomMediaServer(newMediaServer("frentix.com"));
		waitMessageAreConsumed();
		
		Assert.assertFalse(mediaServerModule.isRestrictedDomain("https://foo.frentix.com"));
	}

	@Test
	public void isRestrictedDomain_customDomainSuffixAttack_isRestricted() {
		mediaServerModule.updateCustomMediaServer(newMediaServer("frentix.com"));
		waitMessageAreConsumed();
		
		Assert.assertTrue(mediaServerModule.isRestrictedDomain("https://frentix.com.attacker.tld"));
	}

	@Test
	public void isRestrictedDomain_customDomainExactMatch_notRestricted() {
		mediaServerModule.updateCustomMediaServer(newMediaServer("frentix.com"));
		waitMessageAreConsumed();

		Assert.assertFalse(mediaServerModule.isRestrictedDomain("https://frentix.com"));
	}

	private MediaServer newMediaServer(String domain) {
		MediaServer server = new MediaServer();
		server.setId(UUID.randomUUID().toString());
		server.setName(domain);
		server.setDomain(domain);
		return server;
	}
}
