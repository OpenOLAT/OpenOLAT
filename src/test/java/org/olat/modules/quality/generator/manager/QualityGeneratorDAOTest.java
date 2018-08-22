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
package org.olat.modules.quality.generator.manager;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.modules.quality.generator.QualityGeneratorSearchParams;
import org.olat.modules.quality.generator.QualityGeneratorView;
import org.olat.modules.quality.manager.QualityTestHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityGeneratorDAOTest extends OlatTestCase {
	
	private static final String PROVIDER_TYPE = "provider-type";

	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	
	@Autowired
	private QualityGeneratorDAO sut;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}
	
	@Test
	public void shouldCreateGenerator() {
		QualityGenerator generator = sut.create(PROVIDER_TYPE);
		dbInstance.commitAndCloseSession();
		
		assertThat(generator).isNotNull();
		assertThat(generator.getKey()).isNotNull();
		assertThat(generator.getCreationDate()).isNotNull();
		assertThat(generator.getLastModified()).isNotNull();
		assertThat(generator.getType()).isEqualTo(PROVIDER_TYPE);
		assertThat(generator.isEnabled()).isFalse();
	}
	
	@Test
	public void shouldLoadByKey() {
		QualityGenerator createdGenerator = sut.create(PROVIDER_TYPE);
		dbInstance.commitAndCloseSession();
		
		QualityGenerator generator = sut.loadByKey(createdGenerator);
		
		assertThat(generator).isNotNull();
		assertThat(generator.getKey()).isNotNull();
		assertThat(generator.getCreationDate()).isNotNull();
		assertThat(generator.getLastModified()).isNotNull();
		assertThat(generator.getType()).isEqualTo(PROVIDER_TYPE);
		assertThat(generator.isEnabled()).isFalse();
	}
	
	@Test
	public void shouldLoadEnabledGenerators() {
		QualityGenerator generator1 = sut.create(PROVIDER_TYPE);
		generator1.setEnabled(true);
		sut.save(generator1);
		QualityGenerator generator2 = sut.create(PROVIDER_TYPE);
		generator2.setEnabled(true);
		sut.save(generator2);
		QualityGenerator otherGenerator = sut.create(PROVIDER_TYPE);
		otherGenerator.setEnabled(false);
		sut.save(otherGenerator);
		dbInstance.commitAndCloseSession();
		
		List<QualityGenerator> enabledGenerators = sut.loadEnabledGenerators();
		
		assertThat(enabledGenerators)
				.containsExactlyInAnyOrder(generator1, generator2)
				.doesNotContain(otherGenerator);
	}

	
	@Test
	public void shouldSaveGenerator() {
		QualityGenerator generator = sut.create(PROVIDER_TYPE);
		dbInstance.commitAndCloseSession();
		
		String title = "T";
		RepositoryEntry formEntry = qualityTestHelper.createRepositoryEntry();
		generator.setTitle(title);
		generator.setFormEntry(formEntry);
		generator = sut.save(generator);
		dbInstance.commitAndCloseSession();
		
		assertThat(generator.getTitle()).isEqualTo(title);
		assertThat(generator.getFormEntry()).isEqualTo(formEntry);
	}

	@Test
	public void shouldDeleteGenerator() {
		QualityGenerator generator = sut.create(PROVIDER_TYPE);
		dbInstance.commitAndCloseSession();
		
		sut.delete(generator);
		dbInstance.commitAndCloseSession();

		QualityGenerator reloadedGenerator = sut.loadByKey(generator);
		assertThat(reloadedGenerator).isNull();
	}
	
	@Test
	public void shouldLoadGeneratorView() {
		qualityTestHelper.createGenerator();
		dbInstance.commitAndCloseSession();
		
		QualityGeneratorSearchParams searchParams = new QualityGeneratorSearchParams();
		List<QualityGeneratorView> generators = sut.load(searchParams);
		
		QualityGeneratorView generatorView = generators.get(0);
		
		assertThat(generatorView).isNotNull();
		assertThat(generatorView.getKey()).isNotNull();
		assertThat(generatorView.getCreationDate()).isNotNull();
		assertThat(generatorView.getType()).isNotNull();
		assertThat(generatorView.isEnabled()).isNotNull();
		assertThat(generatorView.getNumberDataCollections()).isNotNull();
	}
	
	@Test
	public void shouldFilterGeneratorByKeys() {
		QualityGenerator generator1 = qualityTestHelper.createGenerator();
		QualityGenerator generator2 = qualityTestHelper.createGenerator();
		QualityGenerator otherGenerator = qualityTestHelper.createGenerator();
		dbInstance.commitAndCloseSession();
		
		QualityGeneratorSearchParams searchParams = new QualityGeneratorSearchParams();
		searchParams.setGeneratorRefs(asList(generator2, generator1));
		List<QualityGeneratorView> generators = sut.load(searchParams);
		
		List<Long> generatorKeys = generators.stream().map(QualityGeneratorRef::getKey).collect(toList());
		assertThat(generatorKeys)
				.containsExactlyInAnyOrder(generator1.getKey(), generator2.getKey())
				.doesNotContain(otherGenerator.getKey());
	}
	
	@Test
	public void shouldFilterGeneratorViewByOrganisations() {
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation2 = qualityTestHelper.createOrganisation();
		Organisation otherOrganisation = qualityTestHelper.createOrganisation();
		QualityGenerator generator1 = qualityTestHelper.createGenerator(singletonList(organisation1));
		QualityGenerator generator2 = qualityTestHelper.createGenerator(Arrays.asList(organisation2, organisation1));
		QualityGenerator otherGenerator = qualityTestHelper.createGenerator(singletonList(otherOrganisation));
		dbInstance.commitAndCloseSession();
		
		QualityGeneratorSearchParams searchParams = new QualityGeneratorSearchParams();
		searchParams.setOrganisationRefs(singletonList(organisation1));
		List<QualityGeneratorView> generators = sut.load(searchParams);
		
		List<Long> generatorKeys = generators.stream().map(QualityGeneratorRef::getKey).collect(toList());
		assertThat(generatorKeys)
				.containsExactlyInAnyOrder(generator1.getKey(), generator2.getKey())
				.doesNotContain(otherGenerator.getKey());
	}

}
