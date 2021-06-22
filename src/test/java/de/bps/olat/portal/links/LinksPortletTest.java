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
package de.bps.olat.portal.links;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 22 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LinksPortletTest {
	
	@Test
	public void readPortletLinks() throws URISyntaxException {
		URL xmlUrl = LinksPortletTest.class.getResource("portlet.xml");
		File xmlFile = new File(xmlUrl.toURI());
		
		Map<String,PortletInstitution> institutions = LinksPortlet.readConfiguration(xmlFile);
		Assert.assertNotNull(institutions);
		PortletInstitution institution = institutions.get("*");
		Assert.assertNotNull(institution);
	}

}
