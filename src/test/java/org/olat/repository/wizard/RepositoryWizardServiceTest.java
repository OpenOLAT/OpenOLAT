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
package org.olat.repository.wizard;

import static org.olat.test.JunitTestHelper.random;

import java.util.Set;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryWizardServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private TaxonomyService taxonomyService;
	
	@Autowired
	private RepositoryWizardService sut;
	
	@Test
	public void shouldUpdateEntryByMetaInfo() {
		Taxonomy taxonomy = taxonomyService.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel level1 = taxonomyService.createTaxonomyLevel(random(), random(), null, null, null, null, taxonomy);
		TaxonomyLevel level2 = taxonomyService.createTaxonomyLevel(random(), random(), null, null, null, null, taxonomy);
		TaxonomyLevel level3 = taxonomyService.createTaxonomyLevel(random(), random(), null, null, null, null, taxonomy);
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		// Init values
		InfoMetadata infoMetadata = new InfoMetadata();
		String displayName = random();
		infoMetadata.setDisplayName(displayName);
		String externalRef = random();
		infoMetadata.setExternalRef(externalRef);
		String description = random();
		infoMetadata.setDescription(description);
		String authors = random();
		infoMetadata.setAuthors(authors);
		infoMetadata.setTaxonomyLevelRefs(Set.of(level1));
		entry = sut.updateRepositoryEntry(entry, infoMetadata);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(entry.getDisplayname()).as("Init displayName").isEqualTo(displayName);
		softly.assertThat(entry.getExternalRef()).as("Init externalRef").isEqualTo(externalRef);
		softly.assertThat(entry.getDescription()).as("Init description").isEqualTo(description);
		softly.assertThat(entry.getAuthors()).as("Init authors").isEqualTo(authors);
		softly.assertThat(repositoryService.getTaxonomy(entry)).as("Init taxonomyLevels").containsExactlyInAnyOrder(level1);
		
		// Update nulls -> no change
		infoMetadata = new InfoMetadata();
		entry = sut.updateRepositoryEntry(entry, infoMetadata);
		dbInstance.commitAndCloseSession();
		
		softly.assertThat(entry.getDisplayname()).as("Null displayName").isEqualTo(displayName);
		softly.assertThat(entry.getExternalRef()).as("Null externalRef").isEqualTo(externalRef);
		softly.assertThat(entry.getDescription()).as("Null description").isEqualTo(description);
		softly.assertThat(entry.getAuthors()).as("Null authors").isEqualTo(authors);
		softly.assertThat(repositoryService.getTaxonomy(entry)).as("Null taxonomyLevels").containsExactlyInAnyOrder(level1);
		
		// Update values
		infoMetadata = new InfoMetadata();
		displayName = random();
		infoMetadata.setDisplayName(displayName);
		externalRef = random();
		infoMetadata.setExternalRef(externalRef);
		description = random();
		infoMetadata.setDescription(description);
		authors = random();
		infoMetadata.setAuthors(authors);
		infoMetadata.setTaxonomyLevelRefs(Set.of(level2, level3));
		entry = sut.updateRepositoryEntry(entry, infoMetadata);
		dbInstance.commitAndCloseSession();
		
		softly.assertThat(entry.getDisplayname()).as("Update displayName").isEqualTo(displayName);
		softly.assertThat(entry.getExternalRef()).as("Update externalRef").isEqualTo(externalRef);
		softly.assertThat(entry.getDescription()).as("Update description").isEqualTo(description);
		softly.assertThat(entry.getAuthors()).as("Update authors").isEqualTo(authors);
		softly.assertThat(repositoryService.getTaxonomy(entry)).as("Update taxonomyLevels").containsExactlyInAnyOrder(level2, level3);
		
		softly.assertAll();
	}

}
