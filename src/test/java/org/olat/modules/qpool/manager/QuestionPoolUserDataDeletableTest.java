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
package org.olat.modules.qpool.manager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.id.Identity;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLogBuilder;
import org.olat.modules.qpool.QuestionPoolModule;

/**
 * 
 * Initial date: 11.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QuestionPoolUserDataDeletableTest {

	@Mock
	private QuestionPoolModule qpoolModuleMock;
	@Mock
	private QPoolService qpoolServiceMock;
	@Mock
	private QuestionItemDAO questionItemDaoMock;
	@Mock
	private QuestionItemAuditLogBuilder auditLogBuilderMock;
	
	@Mock
	private Identity identityDummy;
	@Mock
	private List<QuestionItem> itemsDummy;
	private int numberOfItems = 20;
	private String newDeletedUserName;
	
	@InjectMocks
	private QuestionPoolUserDataDeletable sut;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		itemsDummy = Stream.generate(() -> mock(QuestionItem.class))
				.limit(numberOfItems)
				.collect(Collectors.toList());
		when(questionItemDaoMock.getItemsWithOneAuthor(identityDummy)).thenReturn(itemsDummy);
		
		when(qpoolServiceMock.createAuditLogBuilder(any(), any())).thenReturn(auditLogBuilderMock);
	}
	
	@Test
	public void shouldDeleteQuestionsOfUserIfEnabled() {
		when(qpoolModuleMock.isDeleteQuestionsWithoutAuthor()).thenReturn(true);

		sut.deleteUserData(identityDummy, newDeletedUserName);
		
		verify(qpoolServiceMock).deleteItems(itemsDummy);
	}

	@Test
	public void shouldNotDeleteQuestionsOfUserIfNotEnabled() {
		when(qpoolModuleMock.isDeleteQuestionsWithoutAuthor()).thenReturn(false);

		sut.deleteUserData(identityDummy, newDeletedUserName);
		
		verify(qpoolServiceMock, never()).deleteItems(itemsDummy);
	}
}
