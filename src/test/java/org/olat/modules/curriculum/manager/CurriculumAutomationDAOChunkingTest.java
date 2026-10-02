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
package org.olat.modules.curriculum.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.curriculum.CurriculumAutomationConfig;
import org.olat.modules.curriculum.CurriculumAutomationExecution;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The PostgreSQL driver fails with more than 32767 parameters in a statement.
 * Not every test database has this limit, so the chunk size is verified on the bound parameters.
 *
 * Initial date: 2 Oct 2026<br>
 * @author uhensler, https://www.frentix.com
 */
public class CurriculumAutomationDAOChunkingTest {

	private static final int MAX_PARAMETERS = 32767;

	private final DB dbInstance = mock(DB.class);
	private final EntityManager em = mock(EntityManager.class);

	@Test
	public void shouldChunkKeys_whenLoadingConfigsByCurriculumElements() {
		CurriculumAutomationConfigDAO dao = new CurriculumAutomationConfigDAO();
		ReflectionTestUtils.setField(dao, "dbInstance", dbInstance);
		TypedQuery<CurriculumAutomationConfig> query = mockQuery(CurriculumAutomationConfig.class);

		List<CurriculumAutomationConfig> configs = dao.getConfigsByCurriculumElements(createElements(70_000));

		assertAllKeysBoundInChunks(query, 70_000);
		assertThat(configs).hasSizeGreaterThan(1);
	}

	@Test
	public void shouldChunkKeys_whenLoadingExecutions() {
		CurriculumAutomationExecutionDAO dao = new CurriculumAutomationExecutionDAO();
		ReflectionTestUtils.setField(dao, "dbInstance", dbInstance);
		TypedQuery<CurriculumAutomationExecution> query = mockQuery(CurriculumAutomationExecution.class);

		List<CurriculumAutomationExecution> executions = dao.getExecutions(createElements(70_000));

		assertAllKeysBoundInChunks(query, 70_000);
		assertThat(executions).hasSizeGreaterThan(1);
	}

	@SuppressWarnings("unchecked")
	private <T> TypedQuery<T> mockQuery(Class<T> type) {
		TypedQuery<T> query = mock(TypedQuery.class);
		when(dbInstance.getCurrentEntityManager()).thenReturn(em);
		when(em.createQuery(anyString(), eq(type))).thenReturn(query);
		when(query.setParameter(eq("keys"), any())).thenReturn(query);
		when(query.getResultList()).thenAnswer(invocation -> List.of(mock(type)));
		return query;
	}

	private void assertAllKeysBoundInChunks(TypedQuery<?> query, int expectedKeys) {
		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(query, atLeastOnce()).setParameter(eq("keys"), captor.capture());
		int boundKeys = 0;
		for (Object value : captor.getAllValues()) {
			int size = ((Collection<?>) value).size();
			assertThat(size).isLessThanOrEqualTo(MAX_PARAMETERS);
			boundKeys += size;
		}
		assertThat(boundKeys).isEqualTo(expectedKeys);
	}

	private List<CurriculumElement> createElements(int count) {
		List<CurriculumElement> elements = new ArrayList<>(count);
		for (long i = 0; i < count; i++) {
			CurriculumElementImpl element = new CurriculumElementImpl();
			element.setKey(i + 1);
			elements.add(element);
		}
		return elements;
	}

}
