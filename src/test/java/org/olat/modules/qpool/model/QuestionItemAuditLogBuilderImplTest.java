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
package org.olat.modules.qpool.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.id.Identity;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLog;

/**
 * 
 * Initial date: 22.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QuestionItemAuditLogBuilderImplTest {

	private static final Long AUTHOR_KEY = 123l;
	private static final QuestionItemAuditLog.Action ACTION = QuestionItemAuditLog.Action.CREATE_QUESTION_ITEM_NEW;
	private static final Long QITEM_KEY = 234l;
	private static final String MESSAGE = "message";
	private static final String ITEM_XML = "xml";
	private static final String LICENSE_XML = "license";
	
	@Mock
	private Identity authorMock;
	@Mock
	private QuestionItem qitemMock;
	@Mock
	private QPoolService qpoolServiceMock;
	@Mock
	private LicenseService licenseServiceMock;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		when(authorMock.getKey()).thenReturn(AUTHOR_KEY);
		when(qitemMock.getKey()).thenReturn(QITEM_KEY);
		when(qpoolServiceMock.toAuditXml(any())).thenReturn(ITEM_XML);
		when(licenseServiceMock.toXml(any())).thenReturn(LICENSE_XML);
	}
	
	@Test
	public void shouldBuildMinimalAuditLog() {
		QuestionItemAuditLog auditLog = new QuestionItemAuditLogBuilderImpl(qpoolServiceMock, licenseServiceMock,
				authorMock, ACTION).create();
		
		assertThat(auditLog.getAuthorKey()).isEqualTo(AUTHOR_KEY);
		assertThat(auditLog.getAction()).isEqualTo(ACTION.name());
		assertThat(auditLog.getQuestionItemKey()).isNull();
		assertThat(auditLog.getBefore()).isNull();
		assertThat(auditLog.getAfter()).isNull();
		assertThat(auditLog.getLicenseBefore()).isNull();
		assertThat(auditLog.getLicenseAfter()).isNull();
		assertThat(auditLog.getMessage()).isNull();
	}
	
	@Test
	public void shouldBuildAuditLogWithAllAttributes() {
		QuestionItemAuditLog auditLog = new QuestionItemAuditLogBuilderImpl(qpoolServiceMock, licenseServiceMock, authorMock, ACTION)
				.withBefore(qitemMock)
				.withAfter(qitemMock)
				.withMessage(MESSAGE)
				.create();
		
		assertThat(auditLog.getAuthorKey()).isEqualTo(AUTHOR_KEY);
		assertThat(auditLog.getAction()).isEqualTo(ACTION.name());
		assertThat(auditLog.getQuestionItemKey()).isEqualTo(QITEM_KEY);
		assertThat(auditLog.getBefore()).isNotNull();
		assertThat(auditLog.getAfter()).isNotNull();
		assertThat(auditLog.getLicenseBefore()).isNotNull();
		assertThat(auditLog.getLicenseAfter()).isNotNull();
		assertThat(auditLog.getMessage()).isEqualTo(MESSAGE);
	}
}
