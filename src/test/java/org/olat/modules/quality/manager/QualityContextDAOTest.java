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
package org.olat.modules.quality.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.repository.RepositoryEntry;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityContextDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	
	@Autowired
	private QualityContextDAO sut;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}
	
	@Test
	public void shouldCreateContext() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation evaluationFormParticipation = qualityTestHelper.createParticipation();
		RepositoryEntry repositoryEntry = qualityTestHelper.createRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		QualityContext context = sut.createContext(dataCollection, evaluationFormParticipation, repositoryEntry);
		
		assertThat(context).isNotNull();
		assertThat(context.getKey()).isNotNull();
		assertThat(context.getCreationDate()).isNotNull();
		assertThat(context.getLastModified()).isNotNull();
		assertThat(context.getDataCollection()).isEqualTo(dataCollection);
		assertThat(context.getEvaluationFormParticipation()).isEqualTo(evaluationFormParticipation);
		assertThat(context.getRepositoryEntry()).isEqualTo(repositoryEntry);
	}
	
	@Test
	public void shouldLoadByKey() {
		QualityContext context = qualityTestHelper.createContext();
		dbInstance.commitAndCloseSession();
		
		QualityContext reloadedContext = sut.loadByKey(context);
		
		assertThat(reloadedContext).isEqualTo(context);
	}
	
	@Test
	public void shouldLoadByParticipationAndRepositoryEntry() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation evaluationFormParticipation = qualityTestHelper.createParticipation();
		RepositoryEntry repositoryEntry = qualityTestHelper.createRepositoryEntry();
		QualityContext context = sut.createContext(dataCollection, evaluationFormParticipation, repositoryEntry);
		dbInstance.commitAndCloseSession();
		
		List<QualityContext> reloadedContext = sut.loadByParticipationAndRepositoryEntry(evaluationFormParticipation,
				repositoryEntry);
		
		assertThat(reloadedContext.get(0)).isEqualTo(context);
	}
	
	@Test
	public void shouldDeleteContext() {
		QualityContext context = qualityTestHelper.createContext();
		dbInstance.commitAndCloseSession();
		
		sut.deleteContext(context);
		dbInstance.commitAndCloseSession();
		
		QualityContext reloadedContext = sut.loadByKey(context);
		assertThat(reloadedContext).isNull();
	}

}
