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

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.core.helpers.SettingsTest;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 07.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfluenceLinkSPITest {
	
	private static final Logger log = Tracing.createLoggerFor(ConfluenceLinkSPITest.class);
	
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
	
	@Test @Ignore
	public void getTranslatedUrl() {
		// init settings to set version, required by ConfluenceLinkSPI
		SettingsTest.createHttpDefaultPortSettings();
		
		ConfluenceLinkSPI linkSPI = new ConfluenceLinkSPI();
		// Standard Case in German - same as in english
		String url = linkSPI.getURL(Locale.GERMAN, "Data Management");
		Assert.assertNotNull(url);
		Assert.assertTrue(url.endsWith("Data%20Management"));
		
		// Special handing for anchors in confluence
		// Here some magic is needed since the CustomWare Redirection Plugin
		// plugin we use in Confluence can not redirec links with anchors. The
		// anchor is deleted.
		// We have to translate this here
		// First time it won't return the translated link as it does the translation asynchronously in a separate thread to not block the UI
		String notTranslatedUrl = linkSPI.getURL(Locale.GERMAN, "Data Management#qb_import");
		Assert.assertNotNull(notTranslatedUrl);
		Assert.assertTrue(notTranslatedUrl.endsWith("Data%20Management#DataManagement-qb_import"));
		// Wait 5secs and try it again, should be translated now
		boolean found = false;
		for(int i=0; i<100; i++) {
			String translatedUrl = linkSPI.getURL(Locale.GERMAN, "Data Management#qb_import");
			if(translatedUrl != null && translatedUrl.endsWith("Handhabung%20der%20Daten#HandhabungderDaten-qb_import")) {
				found = true;
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					log.error("", e);
				}
			}
		}
		Assert.assertTrue("German translation cannot be found after 10s", found);
	}
}
