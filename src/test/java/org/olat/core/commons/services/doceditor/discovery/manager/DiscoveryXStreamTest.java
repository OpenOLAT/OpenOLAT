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
package org.olat.core.commons.services.doceditor.discovery.manager;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.services.doceditor.discovery.Action;
import org.olat.core.commons.services.doceditor.discovery.App;
import org.olat.core.commons.services.doceditor.discovery.Discovery;
import org.olat.core.commons.services.doceditor.discovery.NetZone;
import org.olat.core.commons.services.doceditor.discovery.ProofKey;

/**
 * 
 * Initial date: 1 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DiscoveryXStreamTest {
	
	@Test
	public void shouldConvertMicrosoftDiscoveryFile() throws Exception {
		URL url = DiscoveryXStreamTest.class.getResource("discovery_microsoft.xml");
		Path resPath = Paths.get(url.toURI());
		String xml = new String(Files.readAllBytes(resPath), "UTF8");
		
		Discovery discovery = DiscoveryXStream.fromXml(xml, Discovery.class);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(discovery).isNotNull();
		ProofKey proofKey = discovery.getProofKey();
		softly.assertThat(proofKey.getOldValue()).isNotNull();
		softly.assertThat(proofKey.getOldModulus()).isNotNull();
		softly.assertThat(proofKey.getOldExponent()).isNotNull();
		softly.assertThat(proofKey.getValue()).isNotNull();
		softly.assertThat(proofKey.getModulus()).isNotNull();
		softly.assertThat(proofKey.getExponent()).isNotNull();
		
		NetZone netZone = discovery.getNetZones().get(0);
		softly.assertThat(netZone.getName()).isEqualTo("external-https");
		
		App app = netZone.getApps().stream()
				.filter(a -> a.getName().equals("Excel"))
				.findFirst().get();
		softly.assertThat(app.getName()).isNotNull();
		softly.assertThat(app.getFavIconUrl()).isNotNull();
		softly.assertThat(app.getCheckLicense()).isNotNull();
		
		Action action = app.getActions().stream()
				.filter(a -> "view".equals(a.getName()) && "csv".equals(a.getExt()))
				.findFirst().get();
		softly.assertThat(action.getName()).isNotNull();
		softly.assertThat(action.getExt()).isNotNull();
		softly.assertThat(action.getUrlSrc()).isNotNull();
		softly.assertThat(action.getRequires()).isNull();
		softly.assertThat(action.getTargetExt()).isNull();
		
		action = app.getActions().stream()
				.filter(a -> "convert".equals(a.getName()) && "csv".equals(a.getExt()))
				.findFirst().get();
		softly.assertThat(action.getRequires()).isNotNull();
		softly.assertThat(action.getTargetExt()).isNotNull();
		
		softly.assertAll();
	}

}
