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
package org.olat.modules.qpool.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.id.Identity;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.ui.QuestionItemsSource;

/**
 * 
 * Initial date: 24.11.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ReviewProcessSecurityCallbackTest {
	
	@Mock
	private Identity identityMock;
	@Mock
	private 	QuestionItem itemMock;
	@Mock
	private QuestionItemsSource questionItemSourceMock;
	@Mock
	private QuestionPoolModule qPoolModule;
	@Mock
	private QPoolSPI qPoolSPIMock;
	@Mock
	private QPoolService qPoolServiceMock;
	
	@InjectMocks
	private ProcesslessSecurityCallback sut;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(qPoolSPIMock.isTypeEditable()).thenReturn(true);

		when(qPoolModule.getQuestionPoolProvider(any())).thenReturn(qPoolSPIMock);
		
		sut.setIdentity(identityMock);
		sut.setItem(itemMock);
		sut.setEditable(false);
		sut.setQuestionItemSource(questionItemSourceMock);
	}

	@Test
	public void shouldCanEditQuestionIfIsAuthor() {
		when(qPoolServiceMock.isAuthor(itemMock, identityMock)).thenReturn(true);
		
		boolean canEditQuestion = sut.canEditQuestion();
		
		assertThat(canEditQuestion).isTrue();
	}

	@Test
	public void shouldCanEditQuestionIfIsEditable() {
		sut.setEditable(true);
		
		boolean canEditQuestion = sut.canEditQuestion();
		
		assertThat(canEditQuestion).isTrue();
	}
	
	@Test
	public void shouldNotCanEdtQuestionIfQuestionTypeIsNotEditable() {
		when(qPoolServiceMock.isAuthor(itemMock, identityMock)).thenReturn(true);
		when(qPoolSPIMock.isTypeEditable()).thenReturn(false);
		
		boolean canEditQuestion = sut.canEditQuestion();
		
		assertThat(canEditQuestion).isFalse();
	}
	
	@Test
	public void shouldNotCanEditQuestionIfRandomIdentity() {
		boolean canEditQuestion = sut.canEditQuestion();
		
		assertThat(canEditQuestion).isFalse();
	}
	
	@Test
	public void shouldCanEditMetadataIfIsAuthor() {
		when(qPoolServiceMock.isAuthor(itemMock, identityMock)).thenReturn(true);
		
		boolean canEditMetadata = sut.canEditMetadata();
		
		assertThat(canEditMetadata).isTrue();
	}

	@Test
	public void shouldCanEditMetadataIfIsEditable() {
		sut.setEditable(true);
		
		boolean canEditMetadata = sut.canEditMetadata();
		
		assertThat(canEditMetadata).isTrue();
	}
	
	@Test
	public void shouldCanEdtQuestionIfQuestionTypeIsNotEditable() {
		when(qPoolServiceMock.isAuthor(itemMock, identityMock)).thenReturn(true);
		when(qPoolSPIMock.isTypeEditable()).thenReturn(false);
		
		boolean canEditMetadata = sut.canEditMetadata();
		
		assertThat(canEditMetadata).isTrue();
	}
	
	@Test
	public void shouldNotCanEditMetadataIfRandomIdentity() {
		boolean canEditMetadata = sut.canEditMetadata();
		
		assertThat(canEditMetadata).isFalse();
	}
	
	@Test
	public void shouldCanDeleteQuestionIfAllowedInQuestionDataSource() {
		when(questionItemSourceMock.isDeleteEnabled()).thenReturn(true);
		
		boolean canDelete = sut.canDelete();

		assertThat(canDelete).isTrue();
	}
	
	@Test
	public void shouldNotCanDeleteIfNotAllowedInQuestionDataSource() {
		when(questionItemSourceMock.isDeleteEnabled()).thenReturn(false);
		
		boolean canDelete = sut.canDelete();

		assertThat(canDelete).isFalse();	
	}
	
	@Test
	public void shouldNotCanDeleteIfNoQuestionDataSource() {
		sut.setQuestionItemSource(null);
		
		boolean canDelete = sut.canDelete();

		assertThat(canDelete).isFalse();
	}

}
