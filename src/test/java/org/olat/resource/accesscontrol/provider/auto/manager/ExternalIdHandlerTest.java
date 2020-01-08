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
package org.olat.resource.accesscontrol.provider.auto.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;

/**
 *
 * Initial date: 15.08.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExternalIdHandlerTest {

	@Mock
	private RepositoryService repositoryServiceMock;
	@Mock
	private RepositoryEntry entryMock;

	@InjectMocks
	private ExternalIdHandler sut;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldDelegateTheSearchToRepositoryService() {
		String externalId = "EXT-123";

		sut.find(externalId);

		verify(repositoryServiceMock).loadRepositoryEntriesByExternalId(externalId);
	}

	@Test
	public void shouldReturnTheExternalIdFromRepositoryEntry() {
		String externalId = "1234";
		when(entryMock.getExternalId()).thenReturn(externalId);
		
		Set<String> values = sut.getRepositoryEntryValue(entryMock);
		
		assertThat(values.iterator().next()).isEqualTo(externalId);
	}

}
