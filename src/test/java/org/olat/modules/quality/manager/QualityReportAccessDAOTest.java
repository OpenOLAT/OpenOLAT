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
import static org.olat.test.JunitTestHelper.random;

import java.util.Collections;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.RelationRight;
import org.olat.basesecurity.RelationRole;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.RolesByOrganisation;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityDataCollectionStatus;
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
	private CurriculumService curriculumService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private IdentityRelationshipService identityRelationshipService;
	@Autowired
	private BaseSecurityManager baseSecurityManager;
	
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
	public void shouldSetGroup() {
		QualityGeneratorRef generatorRef = qualityTestHelper.createGenerator();
		QualityReportAccess.Type type = QualityReportAccess.Type.GroupRoles;
		QualityReportAccess access = sut.create(of(generatorRef), type, null);
		dbInstance.commitAndCloseSession();
		
		Group group = groupDao.createGroup();
		access = sut.setGroup(access, group);
		
		assertThat(access.getGroup()).isEqualTo(group);
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
		sut.create(of(dc), QualityReportAccess.Type.ReportMember, null);
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
		sut.create(of(generator), QualityReportAccess.Type.ReportMember, null);
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
	public void shouldDeleteUnappropriate() {
		QualityDataCollectionRef dc = qualityTestHelper.createDataCollection();
		QualityReportAccess raAlways = sut.create(of(dc), QualityReportAccess.Type.TopicIdentity, null);
		raAlways.setEmailTrigger(EmailTrigger.insufficient);
		sut.save(raAlways);
		QualityReportAccess raOnline = sut.create(of(dc), QualityReportAccess.Type.TopicIdentity, null);
		raOnline.setOnline(true);
		sut.save(raOnline);
		QualityReportAccess raReportMember = sut.create(of(dc), QualityReportAccess.Type.ReportMember, null);
		raReportMember.setOnline(false);
		raReportMember.setEmailTrigger(EmailTrigger.never);
		sut.save(raReportMember);
		QualityReportAccess raUnappropriated = sut.create(of(dc), QualityReportAccess.Type.TopicIdentity, null);
		raUnappropriated.setOnline(false);
		raUnappropriated.setEmailTrigger(EmailTrigger.never);
		sut.save(raUnappropriated);
		QualityDataCollectionRef dcOther = qualityTestHelper.createDataCollection();
		QualityReportAccess raUnappropriatedOther = sut.create(of(dcOther), QualityReportAccess.Type.TopicIdentity, null);
		raUnappropriatedOther.setOnline(false);
		raUnappropriatedOther.setEmailTrigger(EmailTrigger.never);
		sut.save(raUnappropriatedOther);
		dbInstance.commitAndCloseSession();
		
		sut.deleteUnappropriated(of(dc));
		dbInstance.commitAndCloseSession();
		
		QualityReportAccessSearchParams searchParams = new QualityReportAccessSearchParams();
		searchParams.setReference(of(dc));
		List<QualityReportAccess> accesses = sut.load(searchParams);
		assertThat(accesses)
				.containsExactlyInAnyOrder(raOnline, raAlways, raReportMember)
				.doesNotContain(raUnappropriated);
		
		searchParams.setReference(of(dcOther));
		List<QualityReportAccess> accessesOther = sut.load(searchParams);
		assertThat(accessesOther).containsExactly(raUnappropriatedOther);
	}
	
	@Test
	public void shouldFilterByDataCollection() {
		QualityDataCollectionRef dc = qualityTestHelper.createDataCollection();
		QualityReportAccess access1 = sut.create(of(dc), QualityReportAccess.Type.TopicIdentity, null);
		QualityReportAccess access2 = sut.create(of(dc), QualityReportAccess.Type.ReportMember, null);
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
		QualityReportAccess access2 = sut.create(of(generator), QualityReportAccess.Type.ReportMember, null);
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
	public void shouldFilterByType() {
		Type type = QualityReportAccess.Type.TopicIdentity;
		QualityGenerator generator = qualityTestHelper.createGenerator();
		QualityReportAccess access1 = sut.create(of(generator), type, null);
		QualityReportAccess access2 = sut.create(of(generator), type, null);
		QualityGenerator generatorOther = qualityTestHelper.createGenerator();
		Type otherType = QualityReportAccess.Type.Participants;
		QualityReportAccess accessOther = sut.create(of(generatorOther), otherType, null);
		dbInstance.commitAndCloseSession();
		
		QualityReportAccessSearchParams searchParams = new QualityReportAccessSearchParams();
		searchParams.setType(type);
		List<QualityReportAccess> accesses = sut.load(searchParams);
		
		assertThat(accesses)
				.containsExactlyInAnyOrder(access1, access2)
				.doesNotContain(accessOther);
	}
	
	@Test
	public void shouldLoadReceiversForDoneParticipations() {
		Identity executorFinished = JunitTestHelper.createAndPersistIdentityAsRndUser("p1");
		Identity executorNotFinished = JunitTestHelper.createAndPersistIdentityAsRndUser("p2");
		Identity executorOther = JunitTestHelper.createAndPersistIdentityAsRndUser("p3");
		QualityDataCollection dc = qualityTestHelper.createDataCollection();
		// participated and session finished
		EvaluationFormParticipation participationFinished = qualityService.addParticipations(dc, singletonList(executorFinished)).get(0);
		EvaluationFormSession session = evaluationFormManager.createSession(participationFinished);
		evaluationFormManager.finishSession(session);
		// participation but session not finished
		EvaluationFormParticipation participationNotFinished = qualityService.addParticipations(dc, singletonList(executorNotFinished)).get(0);
		evaluationFormManager.createSession(participationNotFinished);
		// participation to an other data collection
		QualityDataCollection dcOther = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation participationOther = qualityService.addParticipations(dcOther, singletonList(executorOther)).get(0);
		EvaluationFormSession sessionOther = evaluationFormManager.createSession(participationOther);
		evaluationFormManager.finishSession(sessionOther);
		
		QualityReportAccess reportAccess = qualityService.createReportAccess(of(dc), Type.Participants, EvaluationFormParticipationStatus.done.name());
		dbInstance.commitAndCloseSession();
		
		List<Identity> receivers = sut.loadRecipients(reportAccess);
		
		assertThat(receivers)
				.containsExactlyInAnyOrder(executorFinished)
				.doesNotContain(executorNotFinished, executorOther);
	}
	
	@Test
	public void shouldLoadReceiversForAllParticipations() {
		Identity executorFinished = JunitTestHelper.createAndPersistIdentityAsRndUser("p1");
		Identity executorNotFinished = JunitTestHelper.createAndPersistIdentityAsRndUser("p2");
		Identity executorOther = JunitTestHelper.createAndPersistIdentityAsRndUser("p3");
		QualityDataCollection dc = qualityTestHelper.createDataCollection();
		// participated and session finished
		EvaluationFormParticipation participationFinished = qualityService.addParticipations(dc, singletonList(executorFinished)).get(0);
		EvaluationFormSession session = evaluationFormManager.createSession(participationFinished);
		evaluationFormManager.finishSession(session);
		// participation but session not finished
		EvaluationFormParticipation participationNotFinished = qualityService.addParticipations(dc, singletonList(executorNotFinished)).get(0);
		evaluationFormManager.createSession(participationNotFinished);
		// participation to an other data collection
		QualityDataCollection dcOther = qualityTestHelper.createDataCollection();
		EvaluationFormParticipation participationOther = qualityService.addParticipations(dcOther, singletonList(executorOther)).get(0);
		EvaluationFormSession sessionOther = evaluationFormManager.createSession(participationOther);
		evaluationFormManager.finishSession(sessionOther);
		
		QualityReportAccess reportAccess = qualityService.createReportAccess(of(dc), Type.Participants, null);
		dbInstance.commitAndCloseSession();
		
		List<Identity> receivers = sut.loadRecipients(reportAccess);
		
		assertThat(receivers)
				.containsExactlyInAnyOrder(executorFinished, executorNotFinished)
				.doesNotContain(executorOther);
	}
	
	@Test
	public void shouldLoadReceiversForGroupRole() {
		Identity reportViewerEntry1 = JunitTestHelper.createAndPersistIdentityAsRndUser("course1");
		Identity reportViewerEntry2 = JunitTestHelper.createAndPersistIdentityAsRndUser("course1");
		Identity reportViewerElement = JunitTestHelper.createAndPersistIdentityAsRndUser("curriculumElement1");
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
		// Report Viewer is member of the curriculum element
		CurriculumElement element = qualityTestHelper.createCurriculumElement();
		curriculumService.addMember(element, reportViewerElement, CurriculumRoles.owner);
		curriculumService.addMember(element, executor, CurriculumRoles.participant);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc, singletonList(executor));
		qualityService.createContextBuilder(dc, participations.get(0), element, CurriculumRoles.participant).build();
		
		QualityReportAccess reportAccess = qualityService.createReportAccess(of(dc), Type.GroupRoles, reportViewerRole.name());
		dbInstance.commitAndCloseSession();
		
		List<Identity> receivers = sut.loadRecipients(reportAccess);
		
		assertThat(receivers)
				.containsExactlyInAnyOrder(reportViewerEntry1, reportViewerEntry2, reportViewerElement)
				.doesNotContain(reportViewerOtherRole, reportViewerNoParticipant);
	}
	
	@Test
	public void shouldLoadReceiversForTopicIdentity() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsUser("c1");
		Identity coachOther = JunitTestHelper.createAndPersistIdentityAsUser("c2");
		// Everything fulfilled
		QualityDataCollection dc = qualityTestHelper.createDataCollection();
		dc.setTopicIdentity(coach);
		qualityService.updateDataCollection(dc);
		// Data collection with other coach
		QualityDataCollection dcOther = qualityTestHelper.createDataCollection();
		dcOther.setTopicIdentity(coachOther);
		qualityService.updateDataCollection(dcOther);
		
		QualityReportAccess reportAccess = qualityService.createReportAccess(of(dc), Type.TopicIdentity, null);
		dbInstance.commitAndCloseSession();
		
		List<Identity> receivers = sut.loadRecipients(reportAccess);
		
		assertThat(receivers)
				.containsExactlyInAnyOrder(coach)
				.doesNotContain(coachOther);
	}
	@Test
	public void shouldLoadReceiversForReportMembers() {
		Identity member = JunitTestHelper.createAndPersistIdentityAsUser("m1");
		Identity memberOther = JunitTestHelper.createAndPersistIdentityAsUser("m2");
		QualityDataCollection dc = qualityTestHelper.createDataCollection();
		qualityService.addReportMember(of(dc), member);
		QualityDataCollection dcOther = qualityTestHelper.createDataCollection();
		qualityService.addReportMember(of(dcOther), memberOther);
		dbInstance.commitAndCloseSession();
		
		QualityReportAccess reportAccess = qualityService.loadMembersReportAccess(of(dc));
		List<Identity> receivers = sut.loadRecipients(reportAccess);
		
		assertThat(receivers)
				.containsExactlyInAnyOrder(member)
				.doesNotContain(memberOther);
	}
	
	@Test
	public void shouldLoadReceviersForRelationRoles() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsUser("c");
		Identity relSource = JunitTestHelper.createAndPersistIdentityAsUser("rt");
		Identity relSourceOtherRole = JunitTestHelper.createAndPersistIdentityAsUser("rtOtherRole");
		Identity relSourceOtherRight = JunitTestHelper.createAndPersistIdentityAsUser("rtOtherRight");
		Identity coachOther = JunitTestHelper.createAndPersistIdentityAsUser("cOther");
		List<RelationRight> rights = identityRelationshipService.getAvailableRights();
		RelationRight right = rights.get(0);
		RelationRight rightOther = rights.get(1);
		RelationRole role = identityRelationshipService.createRole(random(), singletonList(right));
		RelationRole roleOtherRole = identityRelationshipService.createRole(random(), singletonList(right));
		RelationRole roleOtherRight = identityRelationshipService.createRole(random(), singletonList(rightOther));
		
		// Everything fulfilled
		QualityDataCollection dc = qualityTestHelper.createDataCollection();
		dc.setTopicIdentity(coach);
		qualityService.updateDataCollection(dc);
		identityRelationshipService.addRelation(relSource, coach, role, null, null);
		
		// other right
		identityRelationshipService.addRelation(relSourceOtherRight, coach, roleOtherRight, null, null);
		
		// other role
		identityRelationshipService.addRelation(relSourceOtherRole, coach, roleOtherRole, null, null);
		
		// other data collection
		QualityDataCollection dcOther = qualityTestHelper.createDataCollection();
		dcOther.setTopicIdentity(coach);
		qualityService.updateDataCollection(dcOther);
		
		QualityReportAccess reportAccess = qualityService.createReportAccess(of(dc), Type.RelationRole, role.getKey().toString());
		dbInstance.commitAndCloseSession();
		
		List<Identity> receivers = sut.loadRecipients(reportAccess);
		
		assertThat(receivers)
				.containsExactlyInAnyOrder(
						relSource)
				.doesNotContain(
						relSourceOtherRight,
						relSourceOtherRole,
						coachOther);
	}
	
	@Test
	public void shouldLoadReceviersForLearnResourceManagers() {
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndUser("e1");
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		Organisation organisation11 = qualityTestHelper.createOrganisation(organisation1);
		Organisation organisation12 = qualityTestHelper.createOrganisation(organisation1);
		
		// learn resource manager
		Identity learnresourceManager = JunitTestHelper.createAndPersistIdentityAsRndUser("lrm1");
		RolesByOrganisation roles = new RolesByOrganisation(organisation11, singletonList(OrganisationRoles.learnresourcemanager));
		baseSecurityManager.updateRoles(null, learnresourceManager, roles);
		
		// learn resource manager of parent organisation
		Identity learnresourceManagerParent = JunitTestHelper.createAndPersistIdentityAsRndUser("lrm1P");
		RolesByOrganisation rolesParent = new RolesByOrganisation(organisation1, singletonList(OrganisationRoles.learnresourcemanager));
		baseSecurityManager.updateRoles(null, learnresourceManagerParent, rolesParent);
		
		// learn of other organisation branch
		Identity learnresourceManagerOther = JunitTestHelper.createAndPersistIdentityAsRndUser("lrm2");
		RolesByOrganisation rolesOther = new RolesByOrganisation(organisation12, singletonList(OrganisationRoles.learnresourcemanager));
		baseSecurityManager.updateRoles(null, learnresourceManagerOther, rolesOther);
		
		// user manager
		Identity userManager = JunitTestHelper.createAndPersistIdentityAsRndUser("um");
		RolesByOrganisation umRoles = new RolesByOrganisation(organisation11, singletonList(OrganisationRoles.usermanager));
		baseSecurityManager.updateRoles(null, userManager, umRoles);
		dbInstance.commitAndCloseSession();
		
		QualityDataCollection dc = qualityTestHelper.createDataCollection();
		dc = qualityService.updateDataCollectionStatus(dc, QualityDataCollectionStatus.FINISHED);
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryService.addOrganisation(entry, organisation11);
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dc, Collections.singletonList(executor));
		qualityService.createContextBuilder(dc, participations.get(0), entry, GroupRoles.participant).build();
		dbInstance.commitAndCloseSession();
		
		QualityReportAccess reportAccess = qualityService.createReportAccess(of(dc), Type.LearnResourceManager, null);
		dbInstance.commitAndCloseSession();
		
		List<Identity> receivers = sut.loadRecipients(reportAccess);
		
		assertThat(receivers)
				.containsExactlyInAnyOrder(
						learnresourceManager,
						learnresourceManagerParent)
				.doesNotContain(
						learnresourceManagerOther,
						userManager);
	}

}
