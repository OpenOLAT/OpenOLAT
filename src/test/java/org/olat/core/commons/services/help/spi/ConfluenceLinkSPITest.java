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
package org.olat.core.commons.services.help.spi;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.helpers.SettingsTest;

/**
 * 
 * Initial date: 07.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfluenceLinkSPITest {

	@Test
	public void getUrl() {
		// init settings to set version, required by ConfluenceLinkSPI
		SettingsTest.createHttpDefaultPortSettings();
		
		ConfluenceLinkSPI linkSPI = new ConfluenceLinkSPI();
		//Data%20Management#DataManagement-qb_import
		// Standard Case in English
		String url1 = linkSPI.getURL(Locale.ENGLISH, "Data Management");
		Assert.assertNotNull(url1);
		Assert.assertTrue(url1.endsWith("Data%20Management"));
		
		// Special handing for anchors in confluence
		String url2 = linkSPI.getURL(Locale.ENGLISH, "Data Management#qb_import");
		Assert.assertNotNull(url2);
		Assert.assertTrue(url2.endsWith("Data%20Management#DataManagement-qb_import"));
	}
}
