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
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterSearchParams;
import org.olat.modules.catalog.model.CatalogFilterImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogFilterDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private CatalogFilterDAO sut;
	
	@Test
	public void shouldCreateCatalogFilter() {
		String type = random();
		CatalogFilter catalogFilter = sut.create(type);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(((CatalogFilterImpl)catalogFilter).getKey()).isNotNull();
		softly.assertThat(((CatalogFilterImpl)catalogFilter).getCreationDate()).isNotNull();
		softly.assertThat(((CatalogFilterImpl)catalogFilter).getLastModified()).isNotNull();
		softly.assertThat(catalogFilter.getType()).isEqualTo(type);
		softly.assertThat(catalogFilter.getSortOrder()).isNotNull();
		softly.assertThat(catalogFilter.isEnabled()).isTrue();
		softly.assertThat(catalogFilter.isDefaultVisible()).isTrue();
		softly.assertThat(catalogFilter.getConfig()).isNull();
		softly.assertAll();
	}
	
	@Test
	public void shouldGetNextSortOrder() {
		CatalogFilter catalogFilter = sut.create(random());
		dbInstance.commitAndCloseSession();
		
		int nextSortOrder = sut.getNextSortOrder();
		
		assertThat(nextSortOrder).isEqualTo(catalogFilter.getSortOrder() + 1);
	}
	
	@Test
	public void shouldSaveCatalogFilter() {
		CatalogFilter catalogFilter = sut.create(random());
		dbInstance.commitAndCloseSession();
		
		catalogFilter.setSortOrder(3);
		catalogFilter.setEnabled(false);
		catalogFilter.setDefaultVisible(false);
		String config = random();
		catalogFilter.setConfig(config);
		catalogFilter = sut.save(catalogFilter);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(catalogFilter.getSortOrder()).isEqualTo(3);
		softly.assertThat(catalogFilter.isEnabled()).isFalse();
		softly.assertThat(catalogFilter.isDefaultVisible()).isFalse();
		softly.assertThat(catalogFilter.getConfig()).isEqualTo(config);
		softly.assertAll();
	}
	
	@Test
	public void shouldDeleteCatalogFilter() {
		CatalogFilter catalogFilter = sut.create(random());
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.load(new CatalogFilterSearchParams())).contains(catalogFilter);
		
		sut.delete(catalogFilter);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.load(new CatalogFilterSearchParams())).doesNotContain(catalogFilter);
	}
	
	@Test
	public void shouldLoadByKey() {
		CatalogFilter catalogFilter = sut.create(random());
		dbInstance.commitAndCloseSession();
		
		CatalogFilter reloaded = sut.loadByKey(catalogFilter);
		
		assertThat(reloaded).isEqualTo(catalogFilter);
	}
	
	@Test
	public void shouldLoadBySortOrder() {
		CatalogFilter catalogFilter = sut.create(random());
		dbInstance.commitAndCloseSession();
		
		CatalogFilter reloaded = sut.loadBySortOrder(catalogFilter.getSortOrder());
		
		assertThat(reloaded).isEqualTo(catalogFilter);
	}
	
	@Test
	public void shouldLoadFilterByEnabled() {
		CatalogFilter catalogFilter1 = sut.create(random());
		catalogFilter1.setEnabled(true);
		catalogFilter1 = sut.save(catalogFilter1);
		CatalogFilter catalogFilter2 = sut.create(random());
		catalogFilter2.setEnabled(true);
		catalogFilter2 = sut.save(catalogFilter2);
		CatalogFilter catalogFilter3 = sut.create(random());
		catalogFilter3.setEnabled(false);
		catalogFilter3 = sut.save(catalogFilter3);
		dbInstance.commitAndCloseSession();
		
		CatalogFilterSearchParams searchParams = new CatalogFilterSearchParams();
		searchParams.setEnabled(Boolean.TRUE);
		List<CatalogFilter> catalogFilters = sut.load(searchParams);
		
		assertThat(catalogFilters)
				.contains(catalogFilter1, catalogFilter2)
				.doesNotContain(catalogFilter3);
	}

}
