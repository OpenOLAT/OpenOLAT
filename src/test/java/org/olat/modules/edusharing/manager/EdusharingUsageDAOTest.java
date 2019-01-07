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
package org.olat.modules.edusharing.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.edusharing.EdusharingHtmlElement;
import org.olat.modules.edusharing.EdusharingUsage;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Dec 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdusharingUsageDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	
	@Autowired
	private EdusharingUsageDAO sut;

	@Before
	public void cleanUp() {
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from edusharingusage")
				.executeUpdate();
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void shouldCreateUsage() {
		OLATResource ores = JunitTestHelper.createRandomResource();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("es");
		String identifier = random();
		String objectUrl = random();
		String version = random();
		String mimeType = random();
		String mediaType = random();
		String width = "100";
		String hight = "200";
		EdusharingHtmlElement element = new EdusharingHtmlElement(identifier, objectUrl);
		element.setVersion(version);
		element.setMediaType(mediaType);
		element.setMimeType(mimeType);
		element.setWidth(width);
		element.setHight(hight);
		
		EdusharingUsage usage = sut.create(identity, element, ores);
		dbInstance.commitAndCloseSession();
		
		assertThat(usage).isNotNull();
		assertThat(usage.getCreationDate()).isNotNull();
		assertThat(usage.getLastModified()).isNotNull();
		assertThat(usage.getIdentifier()).isEqualTo(identifier);
		assertThat(usage.getObjectUrl()).isEqualTo(objectUrl);
		assertThat(usage.getVersion()).isEqualTo(version);
		assertThat(usage.getMimeType()).isEqualTo(mimeType);
		assertThat(usage.getMediaType()).isEqualTo(mediaType);
		assertThat(usage.getWidth()).isEqualTo(width);
		assertThat(usage.getHeight()).isEqualTo(hight);
	}

	@Test
	public void shouldLoadByIdentifier() {
		OLATResource ores = JunitTestHelper.createRandomResource();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("es");
		String identifier = random();
		String objectUrl = random();
		EdusharingHtmlElement element = new EdusharingHtmlElement(identifier, objectUrl);
		EdusharingUsage usage = sut.create(identity, element, ores);
		dbInstance.commitAndCloseSession();
		
		EdusharingUsage loadedUsage = sut.loadByIdentifier(identifier);
		
		assertThat(loadedUsage).isEqualTo(usage);
	}

	@Test
	public void shouldLoadByResourceable() {
		OLATResource ores = JunitTestHelper.createRandomResource();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("es");
		EdusharingHtmlElement element = new EdusharingHtmlElement(random(), random());
		EdusharingUsage usage1 = sut.create(identity, element, ores);
		EdusharingUsage usage2 = sut.create(identity, element, ores);
		OLATResource oresOther = JunitTestHelper.createRandomResource();
		EdusharingUsage usageOther = sut.create(identity, element, oresOther);
		dbInstance.commitAndCloseSession();
		
		List<EdusharingUsage> usages = sut.loadByResoureable(ores);
		
		assertThat(usages)
				.containsExactlyInAnyOrder(usage1, usage2)
				.doesNotContain(usageOther);
	}

	@Test
	public void shouldDeleteUsageByIdentifier() {
		OLATResource ores = JunitTestHelper.createRandomResource();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("es");
		String identifier = random();
		String objectUrl = random();
		EdusharingHtmlElement element = new EdusharingHtmlElement(identifier, objectUrl);
		sut.create(identity, element, ores);
		dbInstance.commitAndCloseSession();
		
		sut.delete(identifier);
		dbInstance.commitAndCloseSession();
		EdusharingUsage loadedUsage = sut.loadByIdentifier(identifier);
		
		assertThat(loadedUsage).isNull();
	}

	private String random() {
		return UUID.randomUUID().toString();
	}
}
