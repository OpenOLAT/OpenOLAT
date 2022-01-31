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
package org.olat.core.util.xml;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.thoughtworks.xstream.security.ForbiddenClassException;

/**
 * 
 * Initial date: 18 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class XStreamHelperTest {
	
	@Test
	public void readXmlMapAllowed() throws URISyntaxException {
		URL url = XStreamHelperTest.class.getResource("xstream_map_strings.xml");
		File file = new File(url.toURI());
		Object obj = XStreamHelper.createXStreamInstance().fromXML(file);
		Assert.assertNotNull(obj);
	}
	
	@Test
	public void readXmlMapDbObjectsAllowed() throws URISyntaxException {
		URL url = XStreamHelperTest.class.getResource("xstream_map_strings.xml");
		File file = new File(url.toURI());
		Object obj = XStreamHelper.createXStreamInstanceForDBObjects().fromXML(file);
		Assert.assertNotNull(obj);
	}

	@Test(expected = ForbiddenClassException.class)
	public void readXmlMapNotAllowed() throws URISyntaxException {
		URL url = XStreamHelperTest.class.getResource("xstream_map_alien.xml");
		File file = new File(url.toURI());
		XStreamHelper.createXStreamInstance().fromXML(file);
	}
	
	@Test(expected = ForbiddenClassException.class)
	public void readXmlMapDbObjectsNotAllowed() throws URISyntaxException {
		URL url = XStreamHelperTest.class.getResource("xstream_map_alien.xml");
		File file = new File(url.toURI());
		XStreamHelper.createXStreamInstanceForDBObjects().fromXML(file);
	}
}
