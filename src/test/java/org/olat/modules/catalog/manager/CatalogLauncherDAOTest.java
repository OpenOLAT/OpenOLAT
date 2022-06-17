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
package org.olat.modules.catalog.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.miniRandom;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherSearchParams;
import org.olat.modules.catalog.model.CatalogLauncherImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogLauncherDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private CatalogLauncherDAO sut;
	
	@Test
	public void shouldCreateCatalogLauncher() {
		String type = random();
		String identifier = miniRandom();
		CatalogLauncher catalogLauncher = sut.create(type, identifier);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(((CatalogLauncherImpl)catalogLauncher).getKey()).isNotNull();
		softly.assertThat(((CatalogLauncherImpl)catalogLauncher).getCreationDate()).isNotNull();
		softly.assertThat(((CatalogLauncherImpl)catalogLauncher).getLastModified()).isNotNull();
		softly.assertThat(catalogLauncher.getType()).isEqualTo(type);
		softly.assertThat(catalogLauncher.getIdentifier()).isEqualTo(identifier);
		softly.assertThat(catalogLauncher.getSortOrder()).isNotNull();
		softly.assertThat(catalogLauncher.isEnabled()).isTrue();
		softly.assertThat(catalogLauncher.getConfig()).isNull();
		softly.assertAll();
	}
	
	@Test
	public void shouldGetNextSortOrder() {
		CatalogLauncher catalogLauncher = sut.create(random(), miniRandom());
		dbInstance.commitAndCloseSession();
		
		int nextSortOrder = sut.getNextSortOrder();
		
		assertThat(nextSortOrder).isEqualTo(catalogLauncher.getSortOrder() + 1);
	}
	
	@Test
	public void shouldSaveCatalogLauncher() {
		CatalogLauncher catalogLauncher = sut.create(random(), miniRandom());
		dbInstance.commitAndCloseSession();
		
		catalogLauncher.setSortOrder(3);
		catalogLauncher.setEnabled(false);
		String config = random();
		catalogLauncher.setConfig(config);
		catalogLauncher = sut.save(catalogLauncher);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(catalogLauncher.getSortOrder()).isEqualTo(3);
		softly.assertThat(catalogLauncher.isEnabled()).isFalse();
		softly.assertThat(catalogLauncher.getConfig()).isEqualTo(config);
		softly.assertAll();
	}
	
	@Test
	public void shouldDeleteCatalogLauncher() {
		CatalogLauncher catalogLauncher = sut.create(random(), miniRandom());
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.load(new CatalogLauncherSearchParams())).contains(catalogLauncher);
		
		sut.delete(catalogLauncher);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.load(new CatalogLauncherSearchParams())).doesNotContain(catalogLauncher);
	}
	
	@Test
	public void shouldLoadByKey() {
		CatalogLauncher catalogLauncher = sut.create(random(), miniRandom());
		dbInstance.commitAndCloseSession();
		
		CatalogLauncher reloaded = sut.loadByKey(catalogLauncher);
		
		assertThat(reloaded).isEqualTo(catalogLauncher);
	}
	
	@Test
	public void shouldLoadBySortOrder() {
		CatalogLauncher catalogLauncher = sut.create(random(), miniRandom());
		dbInstance.commitAndCloseSession();
		
		CatalogLauncher reloaded = sut.loadBySortOrder(catalogLauncher.getSortOrder());
		
		assertThat(reloaded).isEqualTo(catalogLauncher);
	}
	
	@Test
	public void shouldLoadLauncherByEnabled() {
		CatalogLauncher catalogLauncher1 = sut.create(random(), miniRandom());
		catalogLauncher1.setEnabled(true);
		catalogLauncher1 = sut.save(catalogLauncher1);
		CatalogLauncher catalogLauncher2 = sut.create(random(), miniRandom());
		catalogLauncher2.setEnabled(true);
		catalogLauncher2 = sut.save(catalogLauncher2);
		CatalogLauncher catalogLauncher3 = sut.create(random(), miniRandom());
		catalogLauncher3.setEnabled(false);
		catalogLauncher3 = sut.save(catalogLauncher3);
		dbInstance.commitAndCloseSession();
		
		CatalogLauncherSearchParams searchParams = new CatalogLauncherSearchParams();
		searchParams.setEnabled(Boolean.TRUE);
		List<CatalogLauncher> catalogLaunchers = sut.load(searchParams);
		
		assertThat(catalogLaunchers)
				.contains(catalogLauncher1, catalogLauncher2)
				.doesNotContain(catalogLauncher3);
	}

}
