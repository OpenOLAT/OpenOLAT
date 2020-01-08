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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.resource.accesscontrol.AccessControlModule;

/**
 *
 * Initial date: 15.08.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExternalRefHandlerTest {

	@Mock
	private AccessControlModule accessModulControlMock;
	@Mock
	private RepositoryService repositoryServiceMock;
	@Mock
	private RepositoryEntry entryMock;

	@InjectMocks
	private ExternalRefHandler sut;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldDelegateTheSearchToRepositoryService() {
		when(accessModulControlMock.getAutoExternalRefDelimiter()).thenReturn("");
		String externalRef = "EXT-123";

		sut.find(externalRef);

		verify(repositoryServiceMock).loadRepositoryEntriesByExternalRef(externalRef);
	}
	
	@Test
	public void shouldFilterExactMatchingExternalRef() {
		when(accessModulControlMock.getAutoExternalRefDelimiter()).thenReturn(",");
		
		String externalRef = "EXT-123";
		List<RepositoryEntry> entries = new ArrayList<>();
		RepositoryEntry re1 = new RepositoryEntry();
		re1.setExternalRef("EXT-123");
		entries.add(re1);
		RepositoryEntry re2 = new RepositoryEntry();
		re2.setExternalRef("EXT-123,abc");
		entries.add(re2);
		RepositoryEntry re3 = new RepositoryEntry();
		re3.setExternalRef("abc,EXT-123");
		entries.add(re3);
		RepositoryEntry re4 = new RepositoryEntry();
		re4.setExternalRef("EXT-1234");
		entries.add(re4);
		RepositoryEntry re5 = new RepositoryEntry();
		re5.setExternalRef("EXT-123,oo,EXT-123");
		entries.add(re5);
		when(repositoryServiceMock.loadRepositoryEntriesLikeExternalRef(externalRef))
				.thenReturn(entries);
		
		List<RepositoryEntry> foundEntries = sut.find(externalRef);
		
		assertThat(foundEntries)
				.containsExactlyInAnyOrder(re1, re2, re3, re5)
				.doesNotContain(re4);
	}
	
	@Test
	public void shouldReturnTheExternalRefFromRepositoryEntry() {
		when(accessModulControlMock.getAutoExternalRefDelimiter()).thenReturn("");
		String externalRef = "1234";
		when(entryMock.getExternalRef()).thenReturn(externalRef);

		Set<String> values = sut.getRepositoryEntryValue(entryMock);

		assertThat(values.iterator().next()).isEqualTo(externalRef);
	}
	
	@Test
	public void shouldReturnSplitedExternalRefsFromRepositoryEntry() {
		when(accessModulControlMock.getAutoExternalRefDelimiter()).thenReturn(",");
		String externalRef = "1234,1234,123,12,1.1";
		when(entryMock.getExternalRef()).thenReturn(externalRef);
		
		Set<String> values = sut.getRepositoryEntryValue(entryMock);
		
		List<String> valueList = new ArrayList<>(values);
		assertThat(valueList).containsExactlyInAnyOrder("1234","123","12","1.1");
	}

}
