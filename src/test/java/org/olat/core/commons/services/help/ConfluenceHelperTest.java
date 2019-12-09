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
package org.olat.core.commons.services.help;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 07.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfluenceHelperTest {

	@Test
	public void getURL_confluence() {
		String url1 = ConfluenceHelper.generateSpace("10.1.1", Locale.GERMAN);
		Assert.assertNotNull(url1);
		Assert.assertTrue(url1.startsWith("/OO101DE/"));
		
		String url2 = ConfluenceHelper.generateSpace("10.1", Locale.ENGLISH);
		Assert.assertNotNull(url2);
		Assert.assertTrue(url2.startsWith("/OO101EN/"));
		
		String url3 = ConfluenceHelper.generateSpace("10.1a", Locale.ENGLISH);
		Assert.assertNotNull(url3);
		Assert.assertTrue(url3.startsWith("/OO101EN/"));
		
		String url4 = ConfluenceHelper.generateSpace("11a", Locale.ENGLISH);
		Assert.assertNotNull(url4);
		Assert.assertTrue(url4.startsWith("/OO110EN/"));
		
		String url5pre0a = ConfluenceHelper.generateSpace("15.pre.0.a", Locale.ENGLISH);
		Assert.assertNotNull(url5pre0a);
		Assert.assertTrue(url5pre0a.startsWith("/OO150EN/"));
		
		String url15pre1 = ConfluenceHelper.generateSpace("15.pre.1", Locale.ENGLISH);
		Assert.assertNotNull(url15pre1);
		Assert.assertTrue(url15pre1.startsWith("/OO150EN/"));
		
		String url15pre = ConfluenceHelper.generateSpace("15.pre", Locale.ENGLISH);
		Assert.assertNotNull(url15pre);
		Assert.assertTrue(url15pre.startsWith("/OO150EN/"));
	}
}
