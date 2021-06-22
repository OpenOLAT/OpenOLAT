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
package org.olat.modules.portfolio.manager;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.olat.modules.portfolio.Citation;
import org.olat.modules.portfolio.CitationSourceType;
import org.olat.modules.portfolio.model.CitationXml;

/**
 * 
 * Initial date: 22 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MetadataXStreamTest {
	
	@Test
	public void readCitationXml() throws URISyntaxException {
		URL citationUrl = MetadataXStreamTest.class.getResource("citation.xml");
		File citationFile = new File(citationUrl.toURI());
		Citation citation = (Citation)MetadataXStream.get().fromXML(citationFile);
		Assert.assertNotNull(citation);
		Assert.assertEquals("SBN-3458794958", citation.getIsbn());
		Assert.assertEquals("Volumen 23", citation.getVolume());
	}
	
	@Test
	public void writeReadCitationXml() throws URISyntaxException {
		CitationXml citation = new CitationXml();
		citation.setLastVisit(new Date());
		citation.setEdition("First edition");
		citation.setItemType(CitationSourceType.film);

		String xml = MetadataXStream.get().toXML(citation);
		Citation reloaded = (Citation)MetadataXStream.get().fromXML(xml);
		
		Assert.assertNotNull(reloaded);
		Assert.assertEquals("First edition", reloaded.getEdition());
		Assert.assertEquals(CitationSourceType.film, reloaded.getItemType());
	}
}
