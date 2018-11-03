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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.modules.quality.QualityReportAccessReference.of;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityReportAccess;
import org.olat.modules.quality.QualityReportAccess.EmailTrigger;
import org.olat.modules.quality.QualityReportAccess.Type;
import org.olat.modules.quality.QualityReportAccessSearchParams;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityReportAccessDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private QualityReportAccessDAO sut;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}
	
	@Test
	public void shouldCreateReportAccessForDataCollection() {
		QualityDataCollectionRef dataCollectionRef = qualityTestHelper.createDataCollection();
		QualityReportAccess.Type type = QualityReportAccess.Type.GroupRoles;
		String role = null;
		dbInstance.commitAndCloseSession();
		
		QualityReportAccess access = sut.create(of(dataCollectionRef), type, role);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(access).isNotNull();
		softly.assertThat(access.getCreationDate()).isNotNull();
		softly.assertThat(access.getLastModified()).isNotNull();
		softly.assertThat(access.getType()).isEqualTo(type);
		softly.assertThat(access.isOnline()).isFalse();
		softly.assertThat(access.getEmailTrigger()).isEqualTo(QualityReportAccess.EmailTrigger.never);
		softly.assertAll();
	}
	
	@Test
	public void shouldCreateReportAccessForGenerator() {
		QualityGeneratorRef generatorRef = qualityTestHelper.createGenerator();
		QualityReportAccess.Type type = QualityReportAccess.Type.GroupRoles;
		String role = "name";
		dbInstance.commitAndCloseSession();
		
		QualityReportAccess access = sut.create(of(generatorRef), type, role);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(access).isNotNull();
		softly.assertThat(access.getCreationDate()).isNotNull();
		softly.assertThat(access.getLastModified()).isNotNull();
		softly.assertThat(access.getType()).isEqualTo(type);
		softly.assertThat(access.getRole()).isEqualTo(role);
		softly.assertThat(access.isOnline()).isFalse();
		softly.assertThat(access.getEmailTrigger()).isEqualTo(QualityReportAccess.EmailTrigger.never);
		softly.assertAll();
	}
	
	@Test
	public void shouldCopyReportAccess() {
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		QualityGeneratorRef generatorRef = qualityTestHelper.createGenerator();
		QualityReportAccess.Type type = QualityReportAccess.Type.GroupRoles;
		String role = "name";
		boolean online = true;
		EmailTrigger emailTrigger = QualityReportAccess.EmailTrigger.always;
		QualityReportAccess reportAccess = sut.create(of(generatorRef), type, role);
		reportAccess.setOnline(online);
		reportAccess.setEmailTrigger(emailTrigger);
		sut.save(reportAccess);
		dbInstance.commitAndCloseSession();
		
		QualityReportAccess copy = sut.copy(of(dataCollection), reportAccess);

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(copy).isNotNull();
		softly.assertThat(copy.getCreationDate()).isNotNull();
		softly.assertThat(copy.getLastModified()).isNotNull();
		softly.assertThat(copy.getType()).isEqualTo(type);
		softly.assertThat(copy.getRole()).isEqualTo(role);
		softly.assertThat(copy.isOnline()).isEqualTo(online);
		softly.assertThat(copy.getEmailTrigger()).isEqualTo(emailTrigger);
		softly.assertAll();
	}
	
	@Test
	public void shouldSaveReportAccess() {
		QualityGeneratorRef generatorRef = qualityTestHelper.createGenerator();
		QualityReportAccess.Type type = QualityReportAccess.Type.GroupRoles;
		QualityReportAccess access = sut.create(of(generatorRef), type, null);
		dbInstance.commitAndCloseSession();
		
		EmailTrigger trigger = EmailTrigger.insufficient;
		access.setEmailTrigger(trigger);
		access.setOnline(true);
		access = sut.save(access);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(access).isNotNull();
		softly.assertThat(access.getCreationDate()).isNotNull();
		softly.assertThat(access.getLastModified()).isNotNull();
		softly.assertThat(access.getType()).isEqualTo(type);
		softly.assertThat(access.getEmailTrigger()).isEqualTo(trigger);
		softly.assertThat(access.isOnline()).isTrue();
		softly.assertAll();
	}
	
	@Test
	public void shouldDeleteByDataCollection() {
		QualityDataCollectionRef dc = qualityTestHelper.createDataCollection();
		sut.create(of(dc), QualityReportAccess.Type.TopicIdentity, null);
		sut.create(of(dc), QualityReportAccess.Type.CurriculumRoles, null);
		QualityDataCollectionRef dcOther = qualityTestHelper.createDataCollection();
		QualityReportAccess reportAccessOther = sut.create(of(dcOther), QualityReportAccess.Type.TopicIdentity, null);
		dbInstance.commitAndCloseSession();
		
		sut.deleteReportAccesses(of(dc));
		dbInstance.commitAndCloseSession();
		
		QualityReportAccessSearchParams searchParams = new QualityReportAccessSearchParams();
		searchParams.setReference(of(dc));
		List<QualityReportAccess> accesses = sut.load(searchParams);
		assertThat(accesses).isEmpty();
		
		searchParams.setReference(of(dcOther));
		List<QualityReportAccess> accessesOther = sut.load(searchParams);
		assertThat(accessesOther).contains(reportAccessOther);
	}
	
	@Test
	public void shouldDeleteByGenerator() {
		QualityGenerator generator = qualityTestHelper.createGenerator();
		sut.create(of(generator), QualityReportAccess.Type.TopicIdentity, null);
		sut.create(of(generator), QualityReportAccess.Type.CurriculumRoles, null);
		QualityGenerator generatorOther = qualityTestHelper.createGenerator();
		QualityReportAccess reportAccessOther = sut.create(of(generatorOther), QualityReportAccess.Type.TopicIdentity, null);
		dbInstance.commitAndCloseSession();
		
		sut.deleteReportAccesses(of(generator));
		dbInstance.commitAndCloseSession();
		
		QualityReportAccessSearchParams searchParams = new QualityReportAccessSearchParams();
		searchParams.setReference(of(generator));
		List<QualityReportAccess> accesses = sut.load(searchParams);
		assertThat(accesses).isEmpty();
		
		searchParams.setReference(of(generatorOther));
		List<QualityReportAccess> accessesOther = sut.load(searchParams);
		assertThat(accessesOther).contains(reportAccessOther);
	}
	
	@Test
	public void shouldFilterByDataCollection() {
		QualityDataCollectionRef dc = qualityTestHelper.createDataCollection();
		QualityReportAccess access1 = sut.create(of(dc), QualityReportAccess.Type.TopicIdentity, null);
		QualityReportAccess access2 = sut.create(of(dc), QualityReportAccess.Type.CurriculumRoles, null);
		QualityDataCollectionRef dcOther = qualityTestHelper.createDataCollection();
		QualityReportAccess accessOther = sut.create(of(dcOther), QualityReportAccess.Type.TopicIdentity, null);
		dbInstance.commitAndCloseSession();
		
		QualityReportAccessSearchParams searchParams = new QualityReportAccessSearchParams();
		searchParams.setReference(of(dc));
		List<QualityReportAccess> accesses = sut.load(searchParams);
		
		assertThat(accesses)
				.containsExactlyInAnyOrder(access1, access2)
				.doesNotContain(accessOther);
	}
	
	@Test
	public void shouldFilterByGenerator() {
		QualityGenerator generator = qualityTestHelper.createGenerator();
		QualityReportAccess access1 = sut.create(of(generator), QualityReportAccess.Type.TopicIdentity, null);
		QualityReportAccess access2 = sut.create(of(generator), QualityReportAccess.Type.CurriculumRoles, null);
		QualityGenerator generatorOther = qualityTestHelper.createGenerator();
		QualityReportAccess accessOther = sut.create(of(generatorOther), QualityReportAccess.Type.TopicIdentity, null);
		dbInstance.commitAndCloseSession();
		
		QualityReportAccessSearchParams searchParams = new QualityReportAccessSearchParams();
		searchParams.setReference(of(generator));
		List<QualityReportAccess> accesses = sut.load(searchParams);
		
		assertThat(accesses)
				.containsExactlyInAnyOrder(access1, access2)
				.doesNotContain(accessOther);
	}
	
	@Test
	public void shouldLoadReceiversForGroupRole() {
		Identity reportViewerEntry1 = JunitTestHelper.createAndPersistIdentityAsRndUser("course1");
		Identity reportViewerEntry2 = JunitTestHelper.createAndPersistIdentityAsRndUser("course1");
		Identity reportViewerOtherRole = JunitTestHelper.createAndPersistIdentityAsRndUser("other-role");
		Identity reportViewerNoParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("no-participant");
		GroupRoles reportViewerRole = GroupRoles.owner;
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("executer");
		GroupRoles executorRole = GroupRoles.participant;
		QualityDataCollection dc = qualityTestHelper.createDataCollection();
		// Everything fulfilled: Data collection has participant of the course
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addRole(reportViewerEntry1, entry1, reportViewerRole.name());
		repositoryService.addRole(executor, entry1, executorRole.name());
		List<EvaluationFormParticipation> participationsNotFinished = qualityService.addParticipations(dc, singletonList(executor));
		qualityService.createContextBuilder(dc, participationsNotFinished.get(0), entry1, executorRole).build();
		// Everything fulfilled: Data collection has participant of a second course
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addRole(reportViewerEntry2, entry2, reportViewerRole.name());
		repositoryService.addRole(executor, entry2, executorRole.name());
		List<EvaluationFormParticipation> participations2 = qualityService.addParticipations(dc, singletonList(executor));
		qualityService.createContextBuilder(dc, participations2.get(0), entry2, executorRole).build();
		// Report viewer has other course role
		repositoryService.addRole(reportViewerOtherRole, entry1, GroupRoles.participant.name());
		// Report viewer is member of course with no participation
		RepositoryEntry entryNoParticipation = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addRole(reportViewerNoParticipant, entryNoParticipation, reportViewerRole.name());
		repositoryService.addRole(executor, entryNoParticipation, executorRole.name());
		
		QualityReportAccess reportAccess = qualityService.createReportAccess(of(dc), Type.GroupRoles, reportViewerRole.name());
		dbInstance.commitAndCloseSession();
		
		List<Identity> receivers = sut.loadReceivers(reportAccess);
		
		assertThat(receivers)
				.containsExactlyInAnyOrder(reportViewerEntry1, reportViewerEntry2)
				.doesNotContain(reportViewerOtherRole, reportViewerNoParticipant);
	}
	
}
