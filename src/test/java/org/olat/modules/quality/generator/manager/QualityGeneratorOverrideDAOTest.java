/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.quality.generator.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.generator.GeneratorOverrideSearchParams;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorOverride;
import org.olat.modules.quality.generator.model.QualityGeneratorOverrideImpl;
import org.olat.modules.quality.manager.QualityTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 Dec 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityGeneratorOverrideDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper helper;
	@Autowired
	private QualityGeneratorDAO generatorDao;
	
	@Autowired
	private QualityGeneratorOverrideDAO sut;
	
	@Test
	public void shouldCreateOverride() {
		QualityGenerator generator = createGenerator();
		
		String identifier = random();
		Long generatorProviderKey = Long.valueOf(33);
		QualityGeneratorOverride override = sut.create(identifier, generator, generatorProviderKey);
		dbInstance.commitAndCloseSession();
		
		assertThat(override).isNotNull();
		assertThat(override.getKey()).isNotNull();
		assertThat(((QualityGeneratorOverrideImpl)override).getCreationDate()).isNotNull();
		assertThat(((QualityGeneratorOverrideImpl)override).getLastModified()).isNotNull();
		assertThat(override.getIdentifier()).isEqualTo(identifier);
		assertThat(override.getGenerator().getKey()).isEqualTo(generator.getKey());
		assertThat(override.getGeneratorProviderKey()).isEqualTo(generatorProviderKey);
	}
	
	@Test
	public void shouldUpdate() {
		QualityGeneratorOverride override = sut.create(random(), createGenerator(), null);
		dbInstance.commitAndCloseSession();
		
		Date start = new Date();
		override.setStart(start);
		QualityDataCollection dataCollection = helper.createDataCollection();
		override.setDataCollection(dataCollection);
		sut.save(override);
		dbInstance.commitAndCloseSession();
		
		override = sut.load(override.getIdentifier());
		
		assertThat(override.getStart()).isCloseTo(start, 2000);
		assertThat(override.getDataCollection().getKey()).isEqualTo(dataCollection.getKey());
	}

	@Test
	public void shouldDelete() {
		QualityGeneratorOverride override = sut.create(random(), createGenerator(), null);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.load(override.getIdentifier())).isNotNull();
		
		sut.delete(override.getIdentifier());
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.load(override.getIdentifier())).isNull();
	}
	
	@Test
	public void shouldFilter_generatorKeys() {
		QualityGenerator generator1 = createGenerator();
		QualityGenerator generator2 = createGenerator();
		QualityGenerator generator3 = createGenerator();
		QualityGeneratorOverride override11 = sut.create(random(), generator1, null);
		QualityGeneratorOverride override12 = sut.create(random(), generator1, null);
		QualityGeneratorOverride override21 = sut.create(random(), generator2, null);
		sut.create(random(), generator3, null);
		dbInstance.commitAndCloseSession();
		
		GeneratorOverrideSearchParams searchParams = new GeneratorOverrideSearchParams();
		searchParams.setGenerators(List.of(generator1, generator2));
		List<QualityGeneratorOverride> overrides = sut.load(searchParams);
		
		assertThat(overrides).containsExactlyInAnyOrder(override11, override12, override21);
	}
	
	@Test
	public void shouldFilter_dataCollectionCreated() {
		QualityGenerator generator = createGenerator();
		QualityGeneratorOverride override1 = sut.create(random(), generator, null);
		override1.setDataCollection(helper.createDataCollection());
		sut.save(override1);
		QualityGeneratorOverride override2 = sut.create(random(), generator, null);
		override2.setDataCollection(helper.createDataCollection());
		sut.save(override2);
		QualityGeneratorOverride override3 = sut.create(random(), generator, null);
		dbInstance.commitAndCloseSession();
		
		GeneratorOverrideSearchParams searchParams = new GeneratorOverrideSearchParams();
		searchParams.setGenerator(generator);
		searchParams.setDataCollectionCreated(null);
		assertThat(sut.load(searchParams)).containsExactlyInAnyOrder(override1, override2, override3);
		
		searchParams.setDataCollectionCreated(Boolean.TRUE);
		assertThat(sut.load(searchParams)).containsExactlyInAnyOrder(override1, override2);
		
		searchParams.setDataCollectionCreated(Boolean.FALSE);
		assertThat(sut.load(searchParams)).containsExactlyInAnyOrder(override3);
	}

	private QualityGenerator createGenerator() {
		QualityGenerator generator = generatorDao.create(random());
		dbInstance.commitAndCloseSession();
		return generator;
	}

}
