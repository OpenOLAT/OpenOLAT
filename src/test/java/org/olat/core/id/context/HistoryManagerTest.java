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

import org.junit.Assert;
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
	
	
	/**
	 * Test the compatibility for old resume files with business group
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testRead_groupContext() throws IOException, URISyntaxException {
		URL xmlUrl = HistoryManagerTest.class.getResource("resume_ver81a.xml");
		assertNotNull(xmlUrl);
		File resumeXml = new File(xmlUrl.toURI());
		HistoryPoint history = historyManager.readHistory(resumeXml);
		Assert.assertNotNull(history);
	}
	
	/**
	 * Test the compatibility for old resume files
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testRead_v1() throws IOException, URISyntaxException {
		URL xmlUrl = HistoryManagerTest.class.getResource("resume_ver1.xml");
		assertNotNull(xmlUrl);
		File resumeXml = new File(xmlUrl.toURI());
		HistoryPoint history = historyManager.readHistory(resumeXml);
		Assert.assertNotNull(history);
	}
	
	/**
	 * Test the compatibility with current format
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testRead_v2() throws IOException, URISyntaxException {
		URL xmlUrl = HistoryManagerTest.class.getResource("resume_ver2.xml");
		assertNotNull(xmlUrl);
		File resumeXml = new File(xmlUrl.toURI());
		HistoryPoint history = historyManager.readHistory(resumeXml);
		Assert.assertNotNull(history);
	}
	
	/**
	 * Test the compatibility with version 8.3
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testRead_v83() throws IOException, URISyntaxException {
		URL xmlUrl = HistoryManagerTest.class.getResource("resume_ver83.xml");
		assertNotNull(xmlUrl);
		File resumeXml = new File(xmlUrl.toURI());
		HistoryPoint history = historyManager.readHistory(resumeXml);
		Assert.assertNotNull(history);
	}
	
	/**
	 * Test the compatibility with version 13 (varian a)
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testRead_v13a() throws IOException, URISyntaxException {
		URL xmlUrl = HistoryManagerTest.class.getResource("resume_ver13a.xml");
		assertNotNull(xmlUrl);
		File resumeXml = new File(xmlUrl.toURI());
		HistoryPoint history = historyManager.readHistory(resumeXml);
		Assert.assertNotNull(history);
	}
	
	/**
	 * Test the compatibility with version 13 (varian b)
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testRead_v13b() throws IOException, URISyntaxException {
		URL xmlUrl = HistoryManagerTest.class.getResource("resume_ver13b.xml");
		assertNotNull(xmlUrl);
		File resumeXml = new File(xmlUrl.toURI());
		HistoryPoint history = historyManager.readHistory(resumeXml);
		Assert.assertNotNull(history);
	}
}
