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
package org.olat.core.id.context;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class HistoryManagerTest extends OlatTestCase {

	@Autowired
	private HistoryManager historyManager;
	
	@Test
	public void xstreamBackwardsCompatibility() throws IOException, URISyntaxException {
		URL xmlUrl = HistoryManagerTest.class.getResource("resume_ver1.xml");
		assertNotNull(xmlUrl);
		File resumeXml = new File(xmlUrl.toURI());
		HistoryPoint history = historyManager.readHistory(resumeXml);
		Assert.assertNotNull(history);
	}
}
