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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.accesscontrol.provider.auto.IdentifierKey;

/**
 *
 * Initial date: 14.08.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 **/
public class IdentifierHandlerTest {

	private static RepositoryEntry course;
	private static RepositoryEntry course2;

	@Mock
	private ExternalIdHandler externaHandlerMock;

	@InjectMocks
	private IdentifierHandler sut;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(externaHandlerMock.getIdentifierKey()).thenReturn(IdentifierKey.externalId);
		sut.setLoadedHandlers(List.of(externaHandlerMock));
		sut.initHandlerCache();

		course = new RepositoryEntry();
		course2 = new RepositoryEntry();
	}

	@Test
	public void shouldReturnTheRepositoryIfFoundOne() {
		List<RepositoryEntry> resources = Arrays.asList(course);
		when(externaHandlerMock.find(anyString())).thenReturn(resources);

		List<RepositoryEntry> loaded = sut.findRepositoryEntries(IdentifierKey.externalId, "EXT-123");

		assertThat(loaded.get(0)).isEqualTo(course);
	}

	@Test
	public void shouldReturnEmptyListIfNoCourseFound() {
		when(externaHandlerMock.find(anyString())).thenReturn(Collections.<RepositoryEntry>emptyList());

		List<RepositoryEntry> loaded = sut.findRepositoryEntries(IdentifierKey.externalId, "EXT-123");

		assertThat(loaded).isEmpty();
	}

	@Test
	public void shouldReturnAllIfMoreThanOneCourseFound() {
		List<RepositoryEntry> resources = Arrays.asList(course, course2);
		when(externaHandlerMock.find(anyString())).thenReturn(resources);

		List<RepositoryEntry> loaded = sut.findRepositoryEntries(IdentifierKey.externalId, "EXT-123");

		assertThat(loaded).hasSameSizeAs(resources);
	}

}
