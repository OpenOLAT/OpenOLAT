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
package org.olat.modules.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.miniRandom;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogV2ServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationService organisationService;
	
	@Autowired
	private CatalogV2Service sut;
	
	@Test
	public void shouldLauncherMoveUp() {
		CatalogLauncher catalogLauncher1 = sut.createCatalogLauncher(random(), miniRandom());
		CatalogLauncher catalogLauncher2 = sut.createCatalogLauncher(random(), miniRandom());
		dbInstance.commitAndCloseSession();
		
		sut.doMove(catalogLauncher2, true);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getCatalogLauncher(catalogLauncher1).getSortOrder()).isEqualTo(catalogLauncher2.getSortOrder());
		softly.assertThat(sut.getCatalogLauncher(catalogLauncher2).getSortOrder()).isEqualTo(catalogLauncher1.getSortOrder());
		softly.assertAll();
	}
	
	@Test
	public void shouldLauncherMoveDown() {
		CatalogLauncher catalogLauncher1 = sut.createCatalogLauncher(random(), miniRandom());
		CatalogLauncher catalogLauncher2 = sut.createCatalogLauncher(random(), miniRandom());
		dbInstance.commitAndCloseSession();
		
		sut.doMove(catalogLauncher1, false);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getCatalogLauncher(catalogLauncher1).getSortOrder()).isEqualTo(catalogLauncher2.getSortOrder());
		softly.assertThat(sut.getCatalogLauncher(catalogLauncher2).getSortOrder()).isEqualTo(catalogLauncher1.getSortOrder());
		softly.assertAll();
	}
	
	@Test
	public void shouldLauncherNotMoveUpTopmost() {
		CatalogLauncherSearchParams searchParams = new CatalogLauncherSearchParams();
		CatalogLauncher topmost = sut.getCatalogLaunchers(searchParams).stream()
				.sorted()
				.findFirst().get();
		
		sut.doMove(topmost, true);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.getCatalogLauncher(topmost).getSortOrder()).isEqualTo(topmost.getSortOrder());
	}
	
	@Test
	public void shoulLauncherdNotMoveDownLowermost() {
		CatalogLauncher lowermost = sut.createCatalogLauncher(random(), miniRandom());
		dbInstance.commitAndCloseSession();
		
		sut.doMove(lowermost, false);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.getCatalogLauncher(lowermost).getSortOrder()).isEqualTo(lowermost.getSortOrder());
	}
	
	@Test
	public void shouldUpdateLauncherOrganisations() {
		CatalogLauncher launcher = sut.createCatalogLauncher(random(), miniRandom());
		dbInstance.commitAndCloseSession();
		
		// No organisations
		sut.updateLauncherOrganisations(launcher, null);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.getCatalogLauncherOrganisations(launcher)).isEmpty();
		
		// Add two organisations
		Organisation organisation1 = organisationService.createOrganisation(random(), null, random(), null, null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, random(), null, null);
		sut.updateLauncherOrganisations(launcher, List.of(organisation1, organisation2));
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.getCatalogLauncherOrganisations(launcher)).containsExactlyInAnyOrder(organisation1, organisation2);
		
		// Remove one organisation, add two new organisations
		Organisation organisation3 = organisationService.createOrganisation(random(), null, random(), null, null);
		Organisation organisation4 = organisationService.createOrganisation(random(), null, random(), null, null);
		sut.updateLauncherOrganisations(launcher, List.of(organisation2, organisation3, organisation4));
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.getCatalogLauncherOrganisations(launcher)).containsExactlyInAnyOrder(organisation2, organisation3, organisation4);
		
		// Delete all organisations
		sut.updateLauncherOrganisations(launcher, null);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.getCatalogLauncherOrganisations(launcher)).isEmpty();
	}
	
	@Test
	public void shouldSyncLauncherOrganisationsOnDeletionOfOrganisation() {
		CatalogLauncher launcher = sut.createCatalogLauncher(random(), miniRandom());
		launcher.setEnabled(true);
		sut.update(launcher);
		dbInstance.commitAndCloseSession();
		
		// Add two organisations
		Organisation organisation1 = organisationService.createOrganisation(random(), null, random(), null, null);
		Organisation organisation2 = organisationService.createOrganisation(random(), null, random(), null, null);
		sut.updateLauncherOrganisations(launcher, List.of(organisation1, organisation2));
		dbInstance.commitAndCloseSession();
		
		// Replace one organisation
		Organisation organisation3 = organisationService.createOrganisation(random(), null, random(), null, null);
		organisationService.deleteOrganisation(organisation2, organisation3);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.getCatalogLauncherOrganisations(launcher)).containsExactlyInAnyOrder(organisation1, organisation3);
		assertThat(sut.getCatalogLauncher(launcher).isEnabled()).isTrue();
		
		// Delete all organisations
		organisationService.deleteOrganisation(organisation1, null);
		organisationService.deleteOrganisation(organisation3, null);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.getCatalogLauncherOrganisations(launcher)).isEmpty();
		assertThat(sut.getCatalogLauncher(launcher).isEnabled()).isFalse();
	}
	
	@Test
	public void shouldFilterMoveUp() {
		CatalogFilter catalogFilter1 = sut.createCatalogFilter(random());
		CatalogFilter catalogFilter2 = sut.createCatalogFilter(random());
		dbInstance.commitAndCloseSession();
		
		sut.doMove(catalogFilter2, true);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getCatalogFilter(catalogFilter1).getSortOrder()).isEqualTo(catalogFilter2.getSortOrder());
		softly.assertThat(sut.getCatalogFilter(catalogFilter2).getSortOrder()).isEqualTo(catalogFilter1.getSortOrder());
		softly.assertAll();
	}
	
	@Test
	public void shouldFilterMoveDown() {
		CatalogFilter catalogFilter1 = sut.createCatalogFilter(random());
		CatalogFilter catalogFilter2 = sut.createCatalogFilter(random());
		dbInstance.commitAndCloseSession();
		
		sut.doMove(catalogFilter1, false);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getCatalogFilter(catalogFilter1).getSortOrder()).isEqualTo(catalogFilter2.getSortOrder());
		softly.assertThat(sut.getCatalogFilter(catalogFilter2).getSortOrder()).isEqualTo(catalogFilter1.getSortOrder());
		softly.assertAll();
	}
	
	@Test
	public void shouldFilterNotMoveUpTopmost() {
		CatalogFilterSearchParams searchParams = new CatalogFilterSearchParams();
		CatalogFilter topmost = sut.getCatalogFilters(searchParams).stream()
				.sorted()
				.findFirst().get();
		
		sut.doMove(topmost, true);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.getCatalogFilter(topmost).getSortOrder()).isEqualTo(topmost.getSortOrder());
	}
	
	@Test
	public void shoulFilterdNotMoveDownLowermost() {
		CatalogFilter lowermost = sut.createCatalogFilter(random());
		dbInstance.commitAndCloseSession();
		
		sut.doMove(lowermost, false);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.getCatalogFilter(lowermost).getSortOrder()).isEqualTo(lowermost.getSortOrder());
	}

}
