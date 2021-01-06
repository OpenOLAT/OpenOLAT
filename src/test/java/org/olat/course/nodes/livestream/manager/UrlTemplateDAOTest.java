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
package org.olat.course.nodes.livestream.manager;


import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.course.nodes.livestream.model.UrlTemplate;
import org.olat.course.nodes.livestream.model.UrlTemplateImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UrlTemplateDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private UrlTemplateDAO sut;

	@Test
	public void shouldCreateUrlTemplate() {
		String name = random();
		
		UrlTemplate urlTemplate = sut.create(name);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(urlTemplate.getKey()).isNotNull();
		softly.assertThat(((UrlTemplateImpl) urlTemplate).getCreationDate()).isNotNull();
		softly.assertThat(((UrlTemplateImpl) urlTemplate).getLastModified()).isNotNull();
		softly.assertThat(urlTemplate.getName()).isEqualTo(name);
		softly.assertAll();
	}

	@Test
	public void shouldUpdateUrlTemplate() {
		UrlTemplate urlTemplate = sut.create(random());
		dbInstance.commitAndCloseSession();
		
		String name = random();
		urlTemplate.setName(name);
		String url1 = random();
		urlTemplate.setUrl1(url1);
		String url2 = random();
		urlTemplate.setUrl2(url2);
		urlTemplate = sut.update(urlTemplate);
		
		UrlTemplate reloaded = sut.loadByKey(urlTemplate.getKey());
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(reloaded.getName()).isEqualTo(name);
		softly.assertThat(reloaded.getUrl1()).isEqualTo(url1);
		softly.assertThat(reloaded.getUrl2()).isEqualTo(url2);
		softly.assertAll();
	}
	
	@Test
	public void shouldLoadAll() {
		sut.create(random());
		dbInstance.commitAndCloseSession();
		
		List<UrlTemplate> all = sut.loadAll();
		
		assertThat(all).hasSizeGreaterThan(0);
	}
	
	@Test
	public void shouldLoadTemplateUrlByKey() {
		UrlTemplate urlTemplate = sut.create(random());
		dbInstance.commitAndCloseSession();
		
		UrlTemplate reloaded = sut.loadByKey(urlTemplate.getKey());
		
		assertThat(reloaded).isEqualTo(urlTemplate);
	}
	
	@Test
	public void shouldDelete() {
		UrlTemplate urlTemplate = sut.create(random());
		dbInstance.commitAndCloseSession();
		
		sut.delete(urlTemplate);
		
		UrlTemplate reloaded = sut.loadByKey(urlTemplate.getKey());
		assertThat(reloaded).isNull();
	}



}
