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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorConfig;
import org.olat.modules.quality.manager.QualityTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityGeneratorConfigDAOTest extends OlatTestCase {
	
	private static final String IDENTIFIER = "identifier";
	private static final String VALUE = "value";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;

	@Autowired
	private QualityGeneratorConfigDAO sut;

	@Before
	public void cleanUp() {
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from qualitygeneratorconfig")
				.executeUpdate();
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void shouldCreateConfig() {
		QualityGenerator generator = qualityTestHelper.createGenerator();
		dbInstance.commitAndCloseSession();
		
		QualityGeneratorConfig config = sut.create(generator, IDENTIFIER, VALUE);
		dbInstance.commitAndCloseSession();
		
		assertThat(config).isNotNull();
		assertThat(config.getCreationDate()).isNotNull();
		assertThat(config.getLastModified()).isNotNull();
		assertThat(config.getIdentifier()).isEqualTo(IDENTIFIER);
		assertThat(config.getValue()).isEqualTo(VALUE);
	}

	@Test
	public void shouldLoadByGenerator() {
		QualityGenerator generator = qualityTestHelper.createGenerator();
		QualityGeneratorConfig config1 = sut.create(generator, IDENTIFIER, VALUE);
		QualityGeneratorConfig config2 = sut.create(generator, IDENTIFIER, VALUE);
		QualityGenerator otherGenerator = qualityTestHelper.createGenerator();
		QualityGeneratorConfig otherConfig = sut.create(otherGenerator, IDENTIFIER, VALUE);
		dbInstance.commitAndCloseSession();
		
		List<QualityGeneratorConfig> configs = sut.loadByGenerator(generator);

		assertThat(configs)
				.containsExactlyInAnyOrder(config1, config2)
				.doesNotContain(otherConfig);
	}
	
	@Test
	public void shouldUpdateConfig() {
		QualityGenerator generator = qualityTestHelper.createGenerator();
		QualityGeneratorConfig config = sut.create(generator, IDENTIFIER, VALUE);
		dbInstance.commitAndCloseSession();
		
		String changedValue = "new value";
		config.setValue(changedValue);
		sut.save(config);
		dbInstance.commitAndCloseSession();
		
		List<QualityGeneratorConfig> configs = sut.loadByGenerator(generator);
		QualityGeneratorConfig reloadedConfig = configs.get(0);
		assertThat(reloadedConfig.getValue()).isEqualTo(changedValue);
	}
	
	@Test
	public void shouldDeleteConfig() {
		QualityGenerator generator = qualityTestHelper.createGenerator();
		QualityGeneratorConfig config = sut.create(generator, IDENTIFIER, VALUE);
		dbInstance.commitAndCloseSession();
		
		sut.delete(config);
		dbInstance.commitAndCloseSession();
		
		List<QualityGeneratorConfig> configs = sut.loadByGenerator(generator);
		assertThat(configs).hasSize(0);
	}

	@Test
	public void shouldDeleteByGenerator() {
		QualityGenerator generator = qualityTestHelper.createGenerator();
		sut.create(generator, IDENTIFIER, VALUE);
		sut.create(generator, IDENTIFIER, VALUE);
		QualityGenerator otherGenerator = qualityTestHelper.createGenerator();
		QualityGeneratorConfig otherConfig = sut.create(otherGenerator, IDENTIFIER, VALUE);
		dbInstance.commitAndCloseSession();
		
		sut.deleteAll(generator);
		
		List<QualityGeneratorConfig> configs = sut.loadByGenerator(generator);
		assertThat(configs).isEmpty();
		
		List<QualityGeneratorConfig> otherConfigs = sut.loadByGenerator(otherGenerator);
		assertThat(otherConfigs).containsExactly(otherConfig);
	}

}
