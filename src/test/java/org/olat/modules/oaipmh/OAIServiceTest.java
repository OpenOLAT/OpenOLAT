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
package org.olat.modules.oaipmh;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.xml.XMLFactories;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import io.smallrye.common.constraint.Assert;

/**
 * 
 * Initial date: 5 juin 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OAIServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OAIService oaiService;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	
	@Test
	public void handleOAIRequest()
	throws UnsupportedEncodingException, ParserConfigurationException, SAXException, IOException {
		Identity admin = JunitTestHelper.getDefaultAuthor();
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(admin);
		entry.setEntryStatus(RepositoryEntryStatusEnum.published);
		entry.setCanIndexMetadata(true);
		String name = "Meta-" + UUID.randomUUID().toString();
		entry.setDisplayname(name);
		entry = repositoryEntryDao.updateAndCommit(entry);
		
		LicenseType licenseType = licenseService.loadLicenseTypeByName("CC BY");	
		ResourceLicense license = licenseService.loadOrCreateLicense(entry.getOlatResource());
		license.setLicenseType(licenseType);
		licenseService.update(license);
		dbInstance.commitAndCloseSession();
		
		String requestVerbParam = "listRecords";
		String requestIdentifierParam = null;
		String requestMetadataPrefixParameter = "oai_dc";

		MediaResource mr = oaiService.handleOAIRequest(requestVerbParam, requestIdentifierParam,
						requestMetadataPrefixParameter, null, null, null, null);
		
		String content = FileUtils.load(mr.getInputStream(), StandardCharsets.UTF_8.name());
		Assert.assertTrue(content.contains("<dc:rights>CC BY</dc:rights>"));
		Assert.assertTrue(content.contains("<dc:publisher>OpenOLAT</dc:publisher>"));
		Assert.assertTrue(content.contains("<dc:title>" + name +"</dc:title>"));
		
		// Check that we can read the output as XML without errors
		SAXParser saxParser = XMLFactories.newSAXParser();
		DefaultHandler2 myHandler = new DefaultHandler2();
		saxParser.setProperty("http://xml.org/sax/properties/lexical-handler", myHandler);
		try (InputStream in = new ByteArrayInputStream(content.getBytes())) {
			saxParser.parse(in, myHandler);
		} catch(IOException e) {
			throw e;
		}
	}
}
