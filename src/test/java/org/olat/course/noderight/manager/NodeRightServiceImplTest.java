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
package org.olat.course.noderight.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Persistable;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.util.DateUtils;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.config.CourseConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.folder.CourseContainerOptions;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.noderight.NodeRight;
import org.olat.course.noderight.NodeRight.EditMode;
import org.olat.course.noderight.NodeRightGrant;
import org.olat.course.noderight.NodeRightGrant.NodeRightRole;
import org.olat.course.noderight.NodeRightType;
import org.olat.course.noderight.NodeRightTypeBuilder;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.userview.CourseReadOnlyDetails;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.group.area.BGArea;
import org.olat.group.model.BusinessGroupRefImpl;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;

/**
 * 
 * Initial date: 2 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NodeRightServiceImplTest {
	
	private NodeRightServiceImpl sut = new NodeRightServiceImpl();
	
	@Test
	public void shouldGetDefaultRight() {
		NodeRightType rightType = NodeRightTypeBuilder.ofIdentifier("test").build();
		ModuleConfiguration moduleConfig = new ModuleConfiguration();
		
		NodeRight right = sut.getRight(moduleConfig, rightType);
		
		assertThat(right).isNotNull().extracting(NodeRight::getTypeIdentifier).isEqualTo("test");
	}
	
	@Test
	public void shouldSetAndGetRight() {
		NodeRightType rightType = NodeRightTypeBuilder.ofIdentifier("test").build();
		ModuleConfiguration moduleConfig = new ModuleConfiguration();
		NodeRight right = sut.getRight(moduleConfig, rightType);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(right.getEditMode()).as("Initial edit mode").isEqualTo(EditMode.regular);
		
		right.setEditMode(EditMode.advanced);
		sut.setRight(moduleConfig, right);
		
		right = sut.getRight(moduleConfig, rightType);
		softly.assertThat(right.getTypeIdentifier()).as("Reloaded type").isEqualTo("test");
		softly.assertThat(right.getEditMode()).as("Reloaded edit mode").isEqualTo(EditMode.advanced);
		softly.assertAll();
	}

	@Test
	public void shouldCreateRoleGrant() {
		NodeRightGrant grant = sut.createGrant(NodeRightRole.coach);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(grant.getRole()).isEqualTo(NodeRightRole.coach);
		softly.assertThat(grant.getIdentityRef()).isNull();
		softly.assertThat(grant.getBusinessGroupRef()).isNull();
		softly.assertThat(grant.getStart()).isNull();
		softly.assertThat(grant.getEnd()).isNull();
		softly.assertAll();
	}
	
	@Test
	public void shouldCreateIdentityGrant() {
		NodeRightGrant grant = sut.createGrant(new IdentityRefImpl(Long.valueOf(2)));
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(grant.getRole()).isNull();
		softly.assertThat(grant.getIdentityRef().getKey()).isEqualTo(2);
		softly.assertThat(grant.getBusinessGroupRef()).isNull();
		softly.assertThat(grant.getStart()).isNull();
		softly.assertThat(grant.getEnd()).isNull();
		softly.assertAll();
	}
	
	@Test
	public void shouldCreateBusinessGroupGrant() {
		NodeRightGrant grant = sut.createGrant(new BusinessGroupRefImpl(Long.valueOf(3)));
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(grant.getRole()).isNull();
		softly.assertThat(grant.getIdentityRef()).isNull();
		softly.assertThat(grant.getBusinessGroupRef().getKey()).isEqualTo(3);
		softly.assertThat(grant.getStart()).isNull();
		softly.assertThat(grant.getEnd()).isNull();
		softly.assertAll();
	}

	@Test
	public void shouldClone() {
		String identifier = random();
		NodeRightType rightType = NodeRightTypeBuilder.ofIdentifier(identifier).build();
		ModuleConfiguration moduleConfig = new ModuleConfiguration();
		NodeRight right = sut.getRight(moduleConfig, rightType);
		EditMode editMode = EditMode.advanced;
		right.setEditMode(editMode);
		NodeRightGrant roleGrant = sut.createGrant(NodeRightRole.guest);
		right.getGrants().add(roleGrant);
		NodeRightGrant identityGrant = sut.createGrant(new IdentityRefImpl(Long.valueOf(9)));
		right.getGrants().add(identityGrant);
		NodeRightGrant grant = sut.createGrant(NodeRightRole.owner, () -> Long.valueOf(8), () -> Long.valueOf(21));
		Date start = new Date();
		grant.setStart(start);
		Date end = DateUtils.addHours(start, 1);
		grant.setEnd(end);
		right.getGrants().add(grant);
		
		NodeRight clone = sut.clone(right);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(clone).isNotSameAs(right);
		softly.assertThat(clone.getTypeIdentifier()).isEqualTo(identifier);
		softly.assertThat(clone.getEditMode()).isEqualTo(editMode);
		softly.assertThat(clone.getGrants()).hasSize(3);
		
		NodeRightGrant clonedGrant = clone.getGrants().stream()
				.filter(g -> g.getIdentityRef() != null && g.getIdentityRef().getKey() == Long.valueOf(8))
				.findFirst().get();
		softly.assertThat(clonedGrant.getRole()).isEqualTo(grant.getRole());
		softly.assertThat(clonedGrant.getIdentityRef().getKey()).isEqualTo(grant.getIdentityRef().getKey());
		softly.assertThat(clonedGrant.getBusinessGroupRef().getKey()).isEqualTo(grant.getBusinessGroupRef().getKey());
		softly.assertThat(clonedGrant.getStart()).isEqualTo(grant.getStart());
		softly.assertThat(clonedGrant.getEnd()).isEqualTo(grant.getEnd());
		softly.assertAll();
	}
	
	@Test
	public void shouldAddNoGrantDuplicates() {
		NodeRightType rightType = NodeRightTypeBuilder.ofIdentifier(random()).build();
		ModuleConfiguration moduleConfig = new ModuleConfiguration();
		NodeRight right = sut.getRight(moduleConfig, rightType);
		NodeRightGrant grant = sut.createGrant(NodeRightRole.guest);
		sut.addGrants(right, Collections.singletonList(grant));
		
		// Add grants a second time
		sut.addGrants(right, Collections.singletonList(grant));
		
		assertThat(right.getGrants()).hasSize(1);
	}
	
	@Test
	public void shouldCheckIfGrantsAreSame() {
		Date date = new GregorianCalendar(2020, 1, 1).getTime();
		Date start1 = new GregorianCalendar(2020, 2, 3).getTime();
		Date start2 = new GregorianCalendar(2020, 2, 4).getTime();
		Date end1 = new GregorianCalendar(2020, 3, 10).getTime();
		Date end2 = new GregorianCalendar(2020, 3, 11).getTime();
		
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, null, null), roleGrant(NodeRightRole.coach, null, null))).isTrue();
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, date, null), roleGrant(NodeRightRole.coach, date, null))).isTrue();
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, null, date), roleGrant(NodeRightRole.coach, null, date))).isTrue();
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, start1, end1), roleGrant(NodeRightRole.coach, start1, end1))).isTrue();
		// 1 date
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, date, null), roleGrant(NodeRightRole.coach, null, null))).isFalse();
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, null, date), roleGrant(NodeRightRole.coach, null, null))).isFalse();
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, null, null), roleGrant(NodeRightRole.coach, date, null))).isFalse();
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, null, null), roleGrant(NodeRightRole.coach, null, date))).isFalse();
		// 2 date
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, date, date), roleGrant(NodeRightRole.coach, null, null))).isFalse();
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, date, null), roleGrant(NodeRightRole.coach, null, date))).isFalse();
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, null, date), roleGrant(NodeRightRole.coach, date, null))).isFalse();
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, null, null), roleGrant(NodeRightRole.coach, date, date))).isFalse();
		// 3 date
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, date, date), roleGrant(NodeRightRole.coach, date, null))).isFalse();
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, date, date), roleGrant(NodeRightRole.coach, null, date))).isFalse();
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, date, null), roleGrant(NodeRightRole.coach, date, date))).isFalse();
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, null, date), roleGrant(NodeRightRole.coach, date, date))).isFalse();
		
		// same start, different end
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, start1, end1), roleGrant(NodeRightRole.coach, start1, null))).isFalse();
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, start1, end1), roleGrant(NodeRightRole.coach, start1, end2))).isFalse();
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, start1, null), roleGrant(NodeRightRole.coach, start1, end2))).isFalse();
		
		// same end, different end
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, start1, end1), roleGrant(NodeRightRole.coach, null, end1))).isFalse();
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, start1, end1), roleGrant(NodeRightRole.coach, start2, end1))).isFalse();
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, null, end1), roleGrant(NodeRightRole.coach, start2, end1))).isFalse();
		
		// different dates 
		assertThat(sut.isSame(roleGrant(NodeRightRole.coach, start1, end1), roleGrant(NodeRightRole.coach, start2, end2))).isFalse();
		
		// compare role
		assertThat(sut.isSame(sut.createGrant(NodeRightRole.coach), sut.createGrant(NodeRightRole.owner))).isFalse();
		assertThat(sut.isSame(sut.createGrant(NodeRightRole.coach), sut.createGrant((IdentityRef)() -> Long.valueOf(1)))).isFalse();
		assertThat(sut.isSame(sut.createGrant(NodeRightRole.coach), sut.createGrant((BusinessGroupRef)() -> Long.valueOf(1)))).isFalse();
		
		// compare identity
		assertThat(sut.isSame(sut.createGrant((IdentityRef)() -> Long.valueOf(1)), sut.createGrant((IdentityRef)() -> Long.valueOf(2)))).isFalse();
		assertThat(sut.isSame(sut.createGrant((IdentityRef)() -> Long.valueOf(1)), sut.createGrant(NodeRightRole.coach))).isFalse();
		assertThat(sut.isSame(sut.createGrant((IdentityRef)() -> Long.valueOf(1)), sut.createGrant((BusinessGroupRef)() -> Long.valueOf(1)))).isFalse();
		
		// compare business group
		assertThat(sut.isSame(sut.createGrant((BusinessGroupRef)() -> Long.valueOf(1)), sut.createGrant((BusinessGroupRef)() -> Long.valueOf(2)))).isFalse();
		assertThat(sut.isSame(sut.createGrant((BusinessGroupRef)() -> Long.valueOf(1)), sut.createGrant(NodeRightRole.coach))).isFalse();
		assertThat(sut.isSame(sut.createGrant((BusinessGroupRef)() -> Long.valueOf(1)), sut.createGrant((IdentityRef)() -> Long.valueOf(1)))).isFalse();
	}
	
	private NodeRightGrant roleGrant(NodeRightRole role, Date start, Date end) {
		NodeRightGrant grant = sut.createGrant(role);
		grant.setStart(start);
		grant.setEnd(end);
		return grant;
	}
	
	@Test
	public void shouldSetRoleGrants() {
		NodeRightType rightType = NodeRightTypeBuilder.ofIdentifier("test").build();
		ModuleConfiguration moduleConfig = new ModuleConfiguration();
		NodeRight right = sut.getRight(moduleConfig, rightType);
		NodeRightGrant identityGrant = sut.createGrant(new IdentityRefImpl(Long.valueOf(9)));
		right.getGrants().add(identityGrant);
		NodeRightGrant groupGrant = sut.createGrant(new BusinessGroupRefImpl(Long.valueOf(6)));
		right.getGrants().add(groupGrant);
		NodeRightGrant roleGrant = sut.createGrant(NodeRightRole.owner);
		Date start = new Date();
		roleGrant.setStart(start);
		Date end = DateUtils.addHours(start, 1);
		roleGrant.setEnd(end);
		right.getGrants().add(roleGrant);
		
		sut.setRoleGrants(right, List.of(NodeRightRole.owner, NodeRightRole.coach));
		
		SoftAssertions softly = new SoftAssertions();
		Collection<NodeRightGrant> grants = right.getGrants();
		softly.assertThat(grants).hasSize(2);
		
		NodeRightGrant ownerGrant = grants.stream().filter(grant -> grant.getRole() == NodeRightRole.owner).findFirst().get();
		softly.assertThat(ownerGrant.getStart()).isNull();
		softly.assertThat(ownerGrant.getEnd()).isNull();
		
		NodeRightGrant coachGrant = grants.stream().filter(grant -> grant.getRole() == NodeRightRole.coach).findFirst().get();
		softly.assertThat(coachGrant.getStart()).isNull();
		softly.assertThat(coachGrant.getEnd()).isNull();
		
		softly.assertAll();
	}

	@Test
	public void shouldSetRegularEditMode() {
		NodeRightType rightType = NodeRightTypeBuilder.ofIdentifier("test").build();
		ModuleConfiguration moduleConfig = new ModuleConfiguration();
		NodeRight right = sut.getRight(moduleConfig, rightType);
		right.setEditMode(EditMode.advanced);
		NodeRightGrant identityGrant = sut.createGrant(new IdentityRefImpl(Long.valueOf(9)));
		right.getGrants().add(identityGrant);
		NodeRightGrant groupGrant = sut.createGrant(new BusinessGroupRefImpl(Long.valueOf(6)));
		right.getGrants().add(groupGrant);
		NodeRightGrant roleGrantWithDate = sut.createGrant(NodeRightRole.owner);
		Date start = new Date();
		roleGrantWithDate.setStart(start);
		Date end = DateUtils.addHours(start, 1);
		roleGrantWithDate.setEnd(end);
		right.getGrants().add(roleGrantWithDate);
		NodeRightGrant roleGrantWithoutDate = sut.createGrant(NodeRightRole.owner);
		right.getGrants().add(roleGrantWithoutDate);
		
		sut.setEditMode(right, EditMode.regular);
		
		SoftAssertions softly = new SoftAssertions();
		assertThat(right.getEditMode()).isEqualTo(EditMode.regular);
		assertThat(right.getGrants()).hasSize(1);
		
		NodeRightGrant grant = right.getGrants().stream().findFirst().get();
		assertThat(grant.getRole()).isEqualTo(NodeRightRole.owner);
		assertThat(grant.getEnd()).isNull();
		assertThat(grant.getStart()).isNull();
		softly.assertAll();
	}
	
	@Test
	public void shouldSetAdvancedEditMode() {
		NodeRightType rightType = NodeRightTypeBuilder.ofIdentifier("test").build();
		ModuleConfiguration moduleConfig = new ModuleConfiguration();
		NodeRight right = sut.getRight(moduleConfig, rightType);
		right.setEditMode(EditMode.regular);
		NodeRightGrant roleGrant = sut.createGrant(NodeRightRole.guest);
		right.getGrants().add(roleGrant);
		NodeRightGrant identityGrant = sut.createGrant(new IdentityRefImpl(Long.valueOf(9)));
		right.getGrants().add(identityGrant);
		NodeRightGrant grant = sut.createGrant(NodeRightRole.coach, () -> Long.valueOf(8), () -> Long.valueOf(21));
		Date start = new Date();
		grant.setStart(start);
		Date end = DateUtils.addHours(start, 1);
		grant.setEnd(end);
		right.getGrants().add(grant);
		
		sut.setEditMode(right, EditMode.advanced);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(right.getEditMode()).isEqualTo(EditMode.advanced);
		softly.assertThat(right.getGrants()).hasSize(3);
		
		NodeRightGrant keepGrant = right.getGrants().stream()
				.filter(g -> g.getIdentityRef() != null && g.getIdentityRef().getKey() == 8L)
				.findFirst().get();
		softly.assertThat(keepGrant.getRole()).isEqualTo(grant.getRole());
		softly.assertThat(keepGrant.getIdentityRef().getKey()).isEqualTo(grant.getIdentityRef().getKey());
		softly.assertThat(keepGrant.getBusinessGroupRef().getKey()).isEqualTo(grant.getBusinessGroupRef().getKey());
		softly.assertThat(keepGrant.getStart()).isEqualTo(grant.getStart());
		softly.assertThat(keepGrant.getEnd()).isEqualTo(grant.getEnd());
		softly.assertAll();
	}
	
	@Test
	public void shouldCheckIfGrantedToRoleOwner() {
		NodeRightType rightType = NodeRightTypeBuilder.ofIdentifier(random()).build();
		ModuleConfiguration moduleConfig = new ModuleConfiguration();
		NodeRight right = sut.getRight(moduleConfig, rightType);
		NodeRightGrant roleGrant = sut.createGrant(NodeRightRole.owner);
		right.getGrants().add(roleGrant);
		NodeRightGrant identityGrant = sut.createGrant(new IdentityRefImpl(Long.valueOf(9)));
		right.getGrants().add(identityGrant);
		
		UserCourseEnvironment owner = new TestUserCourseEnvironment(true, false, false, false, null, null, false);
		UserCourseEnvironment coach = new TestUserCourseEnvironment(false, true, false, false, null, null, false);
		UserCourseEnvironment participant = new TestUserCourseEnvironment(false, false, true, false, null, null, false);
		UserCourseEnvironment guest = new TestUserCourseEnvironment(false, false, false, true, null, null, false);
		UserCourseEnvironment all = new TestUserCourseEnvironment(true, true, true, true, Long.valueOf(1), Long.valueOf(2), true);
		UserCourseEnvironment identity = new TestUserCourseEnvironment(false, false, false, false, Long.valueOf(1), null, false);
		UserCourseEnvironment group = new TestUserCourseEnvironment(false, false, false, false, null, Long.valueOf(2), true);
		UserCourseEnvironment groupNotInCourse = new TestUserCourseEnvironment(false, false, false, false, null, Long.valueOf(3), true);
		UserCourseEnvironment identityNotInGroup = new TestUserCourseEnvironment(false, false, false, false, null, Long.valueOf(2), false);
		UserCourseEnvironment nothing = new TestUserCourseEnvironment(false, false, false, false, null, null, false);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.isGranted(right, owner)).as("Granted to owner").isTrue();
		softly.assertThat(sut.isGranted(right, coach)).as("Granted to coach").isFalse();
		softly.assertThat(sut.isGranted(right, participant)).as("Granted to participant").isFalse();
		softly.assertThat(sut.isGranted(right, guest)).as("Granted to guest").isFalse();
		softly.assertThat(sut.isGranted(right, all)).as("Granted to all").isTrue();
		softly.assertThat(sut.isGranted(right, identity)).as("Granted to identity").isFalse();
		softly.assertThat(sut.isGranted(right, group)).as("Granted to group").isFalse();
		softly.assertThat(sut.isGranted(right, groupNotInCourse)).as("Granted to group not in course").isFalse();
		softly.assertThat(sut.isGranted(right, identityNotInGroup)).as("Granted to identiy not in group").isFalse();
		softly.assertThat(sut.isGranted(right, nothing)).as("Granted to nothing").isFalse();
		softly.assertAll();
	}
	
	@Test
	public void shouldCheckIfGrantedToRoleCoach() {
		NodeRightType rightType = NodeRightTypeBuilder.ofIdentifier(random()).build();
		ModuleConfiguration moduleConfig = new ModuleConfiguration();
		NodeRight right = sut.getRight(moduleConfig, rightType);
		NodeRightGrant roleGrant = sut.createGrant(NodeRightRole.coach);
		right.getGrants().add(roleGrant);
		NodeRightGrant identityGrant = sut.createGrant(new IdentityRefImpl(Long.valueOf(9)));
		right.getGrants().add(identityGrant);
		
		UserCourseEnvironment owner = new TestUserCourseEnvironment(true, false, false, false, null, null, false);
		UserCourseEnvironment coach = new TestUserCourseEnvironment(false, true, false, false, null, null, false);
		UserCourseEnvironment participant = new TestUserCourseEnvironment(false, false, true, false, null, null, false);
		UserCourseEnvironment guest = new TestUserCourseEnvironment(false, false, false, true, null, null, false);
		UserCourseEnvironment all = new TestUserCourseEnvironment(true, true, true, true, Long.valueOf(1), Long.valueOf(2), true);
		UserCourseEnvironment identity = new TestUserCourseEnvironment(false, false, false, false, Long.valueOf(1), null, false);
		UserCourseEnvironment group = new TestUserCourseEnvironment(false, false, false, false, null, Long.valueOf(2), true);
		UserCourseEnvironment groupNotInCourse = new TestUserCourseEnvironment(false, false, false, false, null, Long.valueOf(3), true);
		UserCourseEnvironment identityNotInGroup = new TestUserCourseEnvironment(false, false, false, false, null, Long.valueOf(2), false);
		UserCourseEnvironment nothing = new TestUserCourseEnvironment(false, false, false, false, null, null, false);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.isGranted(right, owner)).as("Granted to owner").isFalse();
		softly.assertThat(sut.isGranted(right, coach)).as("Granted to coach").isTrue();
		softly.assertThat(sut.isGranted(right, participant)).as("Granted to participant").isFalse();
		softly.assertThat(sut.isGranted(right, guest)).as("Granted to guest").isFalse();
		softly.assertThat(sut.isGranted(right, all)).as("Granted to all").isTrue();
		softly.assertThat(sut.isGranted(right, identity)).as("Granted to identity").isFalse();
		softly.assertThat(sut.isGranted(right, group)).as("Granted to group").isFalse();
		softly.assertThat(sut.isGranted(right, groupNotInCourse)).as("Granted to group not in course").isFalse();
		softly.assertThat(sut.isGranted(right, identityNotInGroup)).as("Granted to identiy not in group").isFalse();
		softly.assertThat(sut.isGranted(right, nothing)).as("Granted to nothing").isFalse();
		softly.assertAll();
	}
	
	@Test
	public void shouldCheckIfGrantedToRoleParticipant() {
		NodeRightType rightType = NodeRightTypeBuilder.ofIdentifier(random()).build();
		ModuleConfiguration moduleConfig = new ModuleConfiguration();
		NodeRight right = sut.getRight(moduleConfig, rightType);
		NodeRightGrant roleGrant = sut.createGrant(NodeRightRole.participant);
		right.getGrants().add(roleGrant);
		NodeRightGrant identityGrant = sut.createGrant(new IdentityRefImpl(Long.valueOf(9)));
		right.getGrants().add(identityGrant);
		
		UserCourseEnvironment owner = new TestUserCourseEnvironment(true, false, false, false, null, null, false);
		UserCourseEnvironment coach = new TestUserCourseEnvironment(false, true, false, false, null, null, false);
		UserCourseEnvironment participant = new TestUserCourseEnvironment(false, false, true, false, null, null, false);
		UserCourseEnvironment guest = new TestUserCourseEnvironment(false, false, false, true, null, null, false);
		UserCourseEnvironment all = new TestUserCourseEnvironment(true, true, true, true, Long.valueOf(1), Long.valueOf(2), true);
		UserCourseEnvironment identity = new TestUserCourseEnvironment(false, false, false, false, Long.valueOf(1), null, false);
		UserCourseEnvironment group = new TestUserCourseEnvironment(false, false, false, false, null, Long.valueOf(2), true);
		UserCourseEnvironment groupNotInCourse = new TestUserCourseEnvironment(false, false, false, false, null, Long.valueOf(3), true);
		UserCourseEnvironment identityNotInGroup = new TestUserCourseEnvironment(false, false, false, false, null, Long.valueOf(2), false);
		UserCourseEnvironment nothing = new TestUserCourseEnvironment(false, false, false, false, null, null, false);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.isGranted(right, owner)).as("Granted to owner").isFalse();
		softly.assertThat(sut.isGranted(right, coach)).as("Granted to coach").isFalse();
		softly.assertThat(sut.isGranted(right, participant)).as("Granted to participant").isTrue();
		softly.assertThat(sut.isGranted(right, guest)).as("Granted to guest").isFalse();
		softly.assertThat(sut.isGranted(right, all)).as("Granted to all").isTrue();
		softly.assertThat(sut.isGranted(right, identity)).as("Granted to identity").isFalse();
		softly.assertThat(sut.isGranted(right, group)).as("Granted to group").isFalse();
		softly.assertThat(sut.isGranted(right, groupNotInCourse)).as("Granted to group not in course").isFalse();
		softly.assertThat(sut.isGranted(right, identityNotInGroup)).as("Granted to identiy not in group").isFalse();
		softly.assertThat(sut.isGranted(right, nothing)).as("Granted to nothing").isFalse();
		softly.assertAll();
	}
	
	@Test
	public void shouldCheckIfGrantedToRoleGuest() {
		NodeRightType rightType = NodeRightTypeBuilder.ofIdentifier(random()).build();
		ModuleConfiguration moduleConfig = new ModuleConfiguration();
		NodeRight right = sut.getRight(moduleConfig, rightType);
		NodeRightGrant roleGrant = sut.createGrant(NodeRightRole.guest);
		right.getGrants().add(roleGrant);
		NodeRightGrant identityGrant = sut.createGrant(new IdentityRefImpl(Long.valueOf(9)));
		right.getGrants().add(identityGrant);
		
		UserCourseEnvironment owner = new TestUserCourseEnvironment(true, false, false, false, null, null, false);
		UserCourseEnvironment coach = new TestUserCourseEnvironment(false, true, false, false, null, null, false);
		UserCourseEnvironment participant = new TestUserCourseEnvironment(false, false, true, false, null, null, false);
		UserCourseEnvironment guest = new TestUserCourseEnvironment(false, false, false, true, null, null, false);
		UserCourseEnvironment all = new TestUserCourseEnvironment(true, true, true, true, Long.valueOf(1), Long.valueOf(2), true);
		UserCourseEnvironment identity = new TestUserCourseEnvironment(false, false, false, false, Long.valueOf(1), null, false);
		UserCourseEnvironment group = new TestUserCourseEnvironment(false, false, false, false, null, Long.valueOf(2), true);
		UserCourseEnvironment groupNotInCourse = new TestUserCourseEnvironment(false, false, false, false, null, Long.valueOf(3), true);
		UserCourseEnvironment identityNotInGroup = new TestUserCourseEnvironment(false, false, false, false, null, Long.valueOf(2), false);
		UserCourseEnvironment nothing = new TestUserCourseEnvironment(false, false, false, false, null, null, false);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.isGranted(right, owner)).as("Granted to owner").isFalse();
		softly.assertThat(sut.isGranted(right, coach)).as("Granted to coach").isFalse();
		softly.assertThat(sut.isGranted(right, participant)).as("Granted to participant").isFalse();
		softly.assertThat(sut.isGranted(right, guest)).as("Granted to guest").isTrue();
		softly.assertThat(sut.isGranted(right, all)).as("Granted to all").isTrue();
		softly.assertThat(sut.isGranted(right, identity)).as("Granted to identity").isFalse();
		softly.assertThat(sut.isGranted(right, group)).as("Granted to group").isFalse();
		softly.assertThat(sut.isGranted(right, groupNotInCourse)).as("Granted to group not in course").isFalse();
		softly.assertThat(sut.isGranted(right, identityNotInGroup)).as("Granted to identiy not in group").isFalse();
		softly.assertThat(sut.isGranted(right, nothing)).as("Granted to nothing").isFalse();
		softly.assertAll();
	}
	
	@Test
	public void shouldCheckIfGrantedToIdentity() {
		NodeRightType rightType = NodeRightTypeBuilder.ofIdentifier(random()).build();
		ModuleConfiguration moduleConfig = new ModuleConfiguration();
		NodeRight right = sut.getRight(moduleConfig, rightType);
		NodeRightGrant identityGrant = sut.createGrant(new IdentityRefImpl(Long.valueOf(1)));
		right.getGrants().add(identityGrant);
		
		UserCourseEnvironment owner = new TestUserCourseEnvironment(true, false, false, false, null, null, false);
		UserCourseEnvironment coach = new TestUserCourseEnvironment(false, true, false, false, null, null, false);
		UserCourseEnvironment participant = new TestUserCourseEnvironment(false, false, true, false, null, null, false);
		UserCourseEnvironment guest = new TestUserCourseEnvironment(false, false, false, true, null, null, false);
		UserCourseEnvironment all = new TestUserCourseEnvironment(true, true, true, true, Long.valueOf(1), Long.valueOf(2), true);
		UserCourseEnvironment identity = new TestUserCourseEnvironment(false, false, false, false, Long.valueOf(1), null, false);
		UserCourseEnvironment group = new TestUserCourseEnvironment(false, false, false, false, null, Long.valueOf(2), true);
		UserCourseEnvironment groupNotInCourse = new TestUserCourseEnvironment(false, false, false, false, null, Long.valueOf(3), true);
		UserCourseEnvironment identityNotInGroup = new TestUserCourseEnvironment(false, false, false, false, null, Long.valueOf(2), false);
		UserCourseEnvironment nothing = new TestUserCourseEnvironment(false, false, false, false, null, null, false);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.isGranted(right, owner)).as("Granted to owner").isFalse();
		softly.assertThat(sut.isGranted(right, coach)).as("Granted to coach").isFalse();
		softly.assertThat(sut.isGranted(right, participant)).as("Granted to participant").isFalse();
		softly.assertThat(sut.isGranted(right, guest)).as("Granted to guest").isFalse();
		softly.assertThat(sut.isGranted(right, all)).as("Granted to all").isTrue();
		softly.assertThat(sut.isGranted(right, identity)).as("Granted to identity").isTrue();
		softly.assertThat(sut.isGranted(right, group)).as("Granted to group").isFalse();
		softly.assertThat(sut.isGranted(right, groupNotInCourse)).as("Granted to group not in course").isFalse();
		softly.assertThat(sut.isGranted(right, identityNotInGroup)).as("Granted to identiy not in group").isFalse();
		softly.assertThat(sut.isGranted(right, nothing)).as("Granted to nothing").isFalse();
		softly.assertAll();
	}
	
	@Test
	public void shouldCheckIfGrantedToBusinessGroup() {
		NodeRightType rightType = NodeRightTypeBuilder.ofIdentifier(random()).build();
		ModuleConfiguration moduleConfig = new ModuleConfiguration();
		NodeRight right = sut.getRight(moduleConfig, rightType);
		NodeRightGrant grant = sut.createGrant(new BusinessGroupRefImpl(Long.valueOf(2)));
		right.getGrants().add(grant);
		
		UserCourseEnvironment owner = new TestUserCourseEnvironment(true, false, false, false, null, null, false);
		UserCourseEnvironment coach = new TestUserCourseEnvironment(false, true, false, false, null, null, false);
		UserCourseEnvironment participant = new TestUserCourseEnvironment(false, false, true, false, null, null, false);
		UserCourseEnvironment guest = new TestUserCourseEnvironment(false, false, false, true, null, null, false);
		UserCourseEnvironment all = new TestUserCourseEnvironment(true, true, true, true, Long.valueOf(1), Long.valueOf(2), true);
		UserCourseEnvironment identity = new TestUserCourseEnvironment(false, false, false, false, Long.valueOf(1), null, false);
		UserCourseEnvironment group = new TestUserCourseEnvironment(false, false, false, false, null, Long.valueOf(2), true);
		UserCourseEnvironment groupNotInCourse = new TestUserCourseEnvironment(false, false, false, false, null, Long.valueOf(3), true);
		UserCourseEnvironment identityNotInGroup = new TestUserCourseEnvironment(false, false, false, false, null, Long.valueOf(2), false);
		UserCourseEnvironment nothing = new TestUserCourseEnvironment(false, false, false, false, null, null, false);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.isGranted(right, owner)).as("Granted to owner").isFalse();
		softly.assertThat(sut.isGranted(right, coach)).as("Granted to coach").isFalse();
		softly.assertThat(sut.isGranted(right, participant)).as("Granted to participant").isFalse();
		softly.assertThat(sut.isGranted(right, guest)).as("Granted to guest").isFalse();
		softly.assertThat(sut.isGranted(right, all)).as("Granted to all").isTrue();
		softly.assertThat(sut.isGranted(right, identity)).as("Granted to identity").isFalse();
		softly.assertThat(sut.isGranted(right, group)).as("Granted to group").isTrue();
		softly.assertThat(sut.isGranted(right, groupNotInCourse)).as("Granted to group not in course").isFalse();
		softly.assertThat(sut.isGranted(right, identityNotInGroup)).as("Granted to identiy not in group").isFalse();
		softly.assertThat(sut.isGranted(right, nothing)).as("Granted to nothing").isFalse();
		softly.assertAll();
	}
	
	@Test
	public void shouldCheckIfGrantedIsInTime() {
		UserCourseEnvironment owner = new TestUserCourseEnvironment(true, false, false, false, null, null, false);
		
		String type = JunitTestHelper.random();
		NodeRightType rightType = NodeRightTypeBuilder.ofIdentifier(type).build();
		ModuleConfiguration moduleConfig = new ModuleConfiguration();
		NodeRight right = sut.getRight(moduleConfig, rightType);
		NodeRightGrant grant = sut.createGrant(NodeRightRole.owner);
		right.getGrants().add(grant);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.isGranted(right, owner)).as("No date limits").isTrue();
		
		Date now = new Date();
		grant.setStart(DateUtils.addHours(now, 1));
		softly.assertThat(sut.isGranted(right, owner)).as("Start in future").isFalse();
		grant.setStart(DateUtils.addHours(now, -1));
		softly.assertThat(sut.isGranted(right, owner)).as("Start in past").isTrue();
		grant.setStart(null);
		
		grant.setEnd(DateUtils.addHours(now, 1));
		softly.assertThat(sut.isGranted(right, owner)).as("End in future").isTrue();
		grant.setEnd(DateUtils.addHours(now, -1));
		softly.assertThat(sut.isGranted(right, owner)).as("End in past").isFalse();
		
		grant.setStart(DateUtils.addHours(now, -1));
		grant.setEnd(DateUtils.addHours(now, 1));
		softly.assertThat(sut.isGranted(right, owner)).as("In range").isTrue();
		
		softly.assertAll();
	}
	
	@Test
	public void shouldCheckIfGrantedMultipleGrants() {
		UserCourseEnvironment owner = new TestUserCourseEnvironment(true, false, false, false, null, null, false);
		
		String type = JunitTestHelper.random();
		NodeRightType rightType = NodeRightTypeBuilder.ofIdentifier(type).build();
		ModuleConfiguration moduleConfig = new ModuleConfiguration();
		NodeRight right = sut.getRight(moduleConfig, rightType);
		NodeRightGrant grantOutOfRange = sut.createGrant(NodeRightRole.owner);
		grantOutOfRange.setStart(DateUtils.addHours(new Date(), 1));
		right.getGrants().add(grantOutOfRange);
		NodeRightGrant grantNoRange = sut.createGrant(NodeRightRole.owner);
		right.getGrants().add(grantNoRange);
		
		assertThat(sut.isGranted(right, owner)).isTrue();
	}
	
	private static final class TestUserCourseEnvironment implements UserCourseEnvironment {
		
		private final boolean admin;
		private final boolean coach;
		private final boolean participant;
		private final IdentityEnvironment identityEnvironment;
		private final CourseEnvironment courseEnvironment;

		private TestUserCourseEnvironment(boolean admin, boolean coach, boolean participant, boolean guest,
				Long identityKey, Long businessGroupKey, boolean inGroup) {
			this.admin = admin;
			this.coach = coach;
			this.participant = participant;
			this.identityEnvironment = new IdentityEnvironment(
					new IdentityMock(identityKey), 
					guest? Roles.guestRoles(): Roles.userRoles());
			this.courseEnvironment = new CourseEnvironmentMock(new CourseGroupManagerMock(businessGroupKey, inGroup));
		}
		
		@Override
		public CourseEnvironment getCourseEnvironment() {
			return courseEnvironment;
		}

		@Override
		public CourseEditorEnv getCourseEditorEnv() {
			return null;
		}

		@Override
		public ConditionInterpreter getConditionInterpreter() {
			return null;
		}

		@Override
		public IdentityEnvironment getIdentityEnvironment() {
			return identityEnvironment;
		}

		@Override
		public WindowControl getWindowControl() {
			return null;
		}

		@Override
		public ScoreAccounting getScoreAccounting() {
			return null;
		}

		@Override
		public boolean isAdmin() {
			return admin;
		}

		@Override
		public boolean isCoach() {
			return coach;
		}

		@Override
		public boolean isParticipant() {
			return participant;
		}

		@Override
		public boolean isMemberParticipant() {
			return false;
		}

		@Override
		public boolean isIdentityInCourseGroup(Long groupKey) {
			return false;
		}

		@Override
		public boolean isInOrganisation(String organisationIdentifier, OrganisationRoles... roles) {
			return false;
		}

		@Override
		public List<BusinessGroup> getParticipatingGroups() {
			return null;
		}

		@Override
		public List<BusinessGroup> getWaitingLists() {
			return null;
		}

		@Override
		public List<BusinessGroup> getCoachedGroups() {
			return null;
		}

		@Override
		public List<CurriculumElement> getCoachedCurriculumElements() {
			return null;
		}

		@Override
		public boolean isAdministratorOfAnyCourse() {
			return false;
		}

		@Override
		public boolean isCoachOfAnyCourse() {
			return false;
		}

		@Override
		public boolean isParticipantOfAnyCourse() {
			return false;
		}

		@Override
		public RepositoryEntryLifecycle getLifecycle() {
			return null;
		}

		@Override
		public boolean isCourseReadOnly() {
			return false;
		}

		@Override
		public CourseReadOnlyDetails getCourseReadOnlyDetails() {
			return new CourseReadOnlyDetails(Boolean.FALSE, Boolean.FALSE);
		}

		@Override
		public boolean hasEfficiencyStatementOrCertificate(boolean update) {
			return false;
		}
		
		@Override
		public List<String> getUsernames() {
			return List.of();
		}
		
		private class IdentityMock implements Identity {

			private static final long serialVersionUID = -39929795824681088L;
			
			private final Long key;

			private IdentityMock(Long key) {
				this.key = key;
			}

			@Override
			public Long getKey() {
				return key;
			}

			@Override
			public Date getCreationDate() {
				return null;
			}

			@Override
			public Date getInactivationDate() {
				return null;
			}

			@Override
			public Date getReactivationDate() {
				return null;
			}

			@Override
			public Date getDeletionEmailDate() {
				return null;
			}

			@Override
			public boolean equalsByPersistableKey(Persistable persistable) {
				return false;
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public String getExternalId() {
				return null;
			}

			@Override
			public User getUser() {
				return null;
			}

			@Override
			public Integer getStatus() {
				return null;
			}

			@Override
			public Date getLastLogin() {
				return null;
			}

			@Override
			public Date getExpirationDate() {
				return null;
			}

		}
		
	}

	private static class CourseEnvironmentMock implements CourseEnvironment {
		
		private final CourseGroupManagerMock courseGroupManagerMock;

		public CourseEnvironmentMock(CourseGroupManagerMock courseGroupManagerMock) {
			this.courseGroupManagerMock = courseGroupManagerMock;
		}

		@Override
		public long getCurrentTimeMillis() {
			return 0;
		}

		@Override
		public boolean isPreview() {
			return false;
		}
		
		@Override
		public ICourse updateCourse() {
			return null;
		}

		@Override
		public long getLastPublicationTimestamp() {
			return -1;
		}

		@Override
		public CourseGroupManager getCourseGroupManager() {
			return courseGroupManagerMock;
		}

		@Override
		public Long getCourseResourceableId() {
			return null;
		}

		@Override
		public void updateCourseEntry(RepositoryEntry courseEntry) {
		
		}
		
		@Override
		public VFSContainer getCourseFolderContainer() {
			return null;
		}

		@Override
		public VFSContainer getCourseFolderContainer(CourseContainerOptions options) {
			return null;
		}

		@Override
		public LocalFolderImpl getCourseBaseContainer() {
			return null;
		}

		@Override
		public Structure getRunStructure() {
			return null;
		}

		@Override
		public String getCourseTitle() {
			return null;
		}

		@Override
		public CourseConfig getCourseConfig() {
			return null;
		}

		@Override
		public CoursePropertyManager getCoursePropertyManager() {
			return null;
		}

		@Override
		public AssessmentManager getAssessmentManager() {
			return null;
		}

		@Override
		public UserNodeAuditManager getAuditManager() {
			return null;
		}
		
	}
	
	private static final class CourseGroupManagerMock implements CourseGroupManager {

		private final List<BusinessGroup> allBusinessGroups;
		private final boolean inGroup;

		public CourseGroupManagerMock(Long businessGroupKey, boolean inGroup) {
			if (businessGroupKey != null) {
				BusinessGroup businessGroupmock = new BusinessGroupMock(businessGroupKey);
				this.allBusinessGroups = Collections.singletonList(businessGroupmock);
			} else {
				this.allBusinessGroups = Collections.emptyList();
			}
			this.inGroup = inGroup;
		}

		@Override
		public OLATResource getCourseResource() {
			return null;
		}

		@Override
		public RepositoryEntry getCourseEntry() {
			return null;
		}

		@Override
		public boolean isNotificationsAllowed() {
			return false;
		}

		@Override
		public boolean hasRight(Identity identity, String courseRight, GroupRoles role) {
			return false;
		}

		@Override
		public List<String> getRights(Identity identity, GroupRoles role) {
			return null;
		}

		@Override
		public boolean isIdentityInGroup(Identity identity, Long groupKey) {
			return inGroup;
		}

		@Override
		public boolean isBusinessGroupFull(Long groupKey) {
			return false;
		}

		@Override
		public boolean isIdentityInLearningArea(Identity identity, Long areaKey) {
			return false;
		}

		@Override
		public boolean isIdentityCourseCoach(Identity identity) {
			return false;
		}

		@Override
		public boolean isIdentityCourseAdministrator(Identity identity) {
			return false;
		}

		@Override
		public boolean isIdentityCourseParticipant(Identity identity) {
			return false;
		}

		@Override
		public boolean isIdentityAnyCourseCoach(Identity identity) {
			return false;
		}

		@Override
		public boolean isIdentityAnyCourseAdministrator(Identity identity) {
			return false;
		}

		@Override
		public boolean isIdentityAnyCourseParticipant(Identity identity) {
			return false;
		}

		@Override
		public boolean isIdentityInOrganisation(IdentityRef identity, String organisationIdentifier,
				OrganisationRoles... roles) {
			return false;
		}

		@Override
		public boolean hasBusinessGroups() {
			return false;
		}

		@Override
		public List<BusinessGroup> getAllBusinessGroups() {
			return allBusinessGroups;
		}

		@Override
		public boolean existGroup(String nameOrKey) {
			return false;
		}

		@Override
		public List<BusinessGroup> getOwnedBusinessGroups(Identity identity) {
			return null;
		}

		@Override
		public List<BusinessGroup> getParticipatingBusinessGroups(Identity identity) {
			return null;
		}

		@Override
		public List<CurriculumElement> getAllCurriculumElements() {
			return null;
		}

		@Override
		public List<CurriculumElement> getCoachedCurriculumElements(Identity identity) {
			return null;
		}

		@Override
		public boolean hasAreas() {
			return false;
		}

		@Override
		public List<BGArea> getAllAreas() {
			return null;
		}

		@Override
		public boolean existArea(String nameOrKey) {
			return false;
		}

		@Override
		public void deleteCourseGroupmanagement() {
			
		}

		@Override
		public List<Integer> getNumberOfMembersFromGroups(List<BusinessGroup> groups) {
			return null;
		}

		@Override
		public List<String> getUniqueBusinessGroupNames() {
			return null;
		}

		@Override
		public List<String> getUniqueAreaNames() {
			return null;
		}

		@Override
		public void exportCourseBusinessGroups(File fExportDirectory, CourseEnvironmentMapper env) {
			
		}

		@Override
		public CourseEnvironmentMapper getBusinessGroupEnvironment() {
			return null;
		}

		@Override
		public CourseEnvironmentMapper importCourseBusinessGroups(File fImportDirectory) {
			return null;
		}

		@Override
		public void archiveCourseGroups(File exportDirectory) {
			
		}

		@Override
		public List<Identity> getCoachesFromBusinessGroups() {
			return null;
		}

		@Override
		public List<Identity> getCoachesFromBusinessGroups(List<Long> groupKeys) {
			return null;
		}

		@Override
		public List<Identity> getCoaches() {
			return null;
		}

		@Override
		public List<Identity> getCoachesFromAreas() {
			return null;
		}

		@Override
		public List<Identity> getCoachesFromAreas(List<Long> areaKeys) {
			return null;
		}

		@Override
		public List<Identity> getCoachesFromCurriculumElements() {
			return null;
		}

		@Override
		public List<Identity> getCoachesFromCurriculumElements(List<Long> curriculumElementKeys) {
			return null;
		}

		@Override
		public List<Identity> getParticipantsFromBusinessGroups() {
			return null;
		}

		@Override
		public List<Identity> getParticipantsFromBusinessGroups(List<Long> groupKeys) {
			return null;
		}

		@Override
		public List<Identity> getParticipants() {
			return null;
		}

		@Override
		public List<Identity> getParticipantsFromAreas() {
			return null;
		}

		@Override
		public List<Identity> getParticipantsFromAreas(List<Long> areaKeys) {
			return null;
		}

		@Override
		public List<Identity> getParticipantsFromCurriculumElements() {
			return null;
		}

		@Override
		public List<Identity> getParticipantsFromCurriculumElements(List<Long> curriculumElementKeys) {
			return null;
		}

		@Override
		public List<BusinessGroup> getWaitingListGroups(Identity identity) {
			return null;
		}
		
	}
	
	public static final class BusinessGroupMock implements BusinessGroup {

		private static final long serialVersionUID = -3673696437941537988L;
		
		private final Long key;
		private final String name;

		public BusinessGroupMock(Long key) {
			this(key, null);
		}
		
		public BusinessGroupMock(Long key, String name) {
			this.key = key;
			this.name = name;
		}

		@Override
		public Long getKey() {
			return key;
		}

		@Override
		public String getResourceableTypeName() {
			return null;
		}

		@Override
		public Long getResourceableId() {
			return null;
		}

		@Override
		public String getTechnicalType() {
			return BusinessGroup.BUSINESS_TYPE;
		}

		@Override
		public BusinessGroupStatusEnum getGroupStatus() {
			return BusinessGroupStatusEnum.active;
		}

		@Override
		public void setGroupStatus(BusinessGroupStatusEnum status) {
			//
		}

		@Override
		public boolean equalsByPersistableKey(Persistable persistable) {
			return false;
		}

		@Override
		public Date getCreationDate() {
			return null;
		}

		@Override
		public Date getLastModified() {
			return null;
		}

		@Override
		public void setLastModified(Date date) {
			
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setName(String name) {
			
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public void setDescription(String description) {
			//
		}

		@Override
		public String getExternalId() {
			return null;
		}

		@Override
		public void setExternalId(String externalId) {
			//
		}

		@Override
		public BusinessGroupManagedFlag[] getManagedFlags() {
			return null;
		}

		@Override
		public String getManagedFlagsString() {
			return null;
		}

		@Override
		public void setManagedFlagsString(String flags) {
			//
		}

		@Override
		public void setLastUsage(Date lastUsage) {
			
		}

		@Override
		public OLATResource getResource() {
			return null;
		}

		@Override
		public Group getBaseGroup() {
			return null;
		}

		@Override
		public Date getLastUsage() {
			return null;
		}

		@Override
		public boolean isOwnersVisibleIntern() {
			return false;
		}

		@Override
		public void setOwnersVisibleIntern(boolean visible) {
			
		}

		@Override
		public boolean isParticipantsVisibleIntern() {
			return false;
		}

		@Override
		public void setParticipantsVisibleIntern(boolean visible) {
			
		}

		@Override
		public boolean isWaitingListVisibleIntern() {
			return false;
		}

		@Override
		public void setWaitingListVisibleIntern(boolean visible) {
			
		}

		@Override
		public boolean isOwnersVisiblePublic() {
			return false;
		}

		@Override
		public void setOwnersVisiblePublic(boolean visible) {
			
		}

		@Override
		public boolean isParticipantsVisiblePublic() {
			return false;
		}

		@Override
		public void setParticipantsVisiblePublic(boolean visible) {
			
		}

		@Override
		public boolean isWaitingListVisiblePublic() {
			return false;
		}

		@Override
		public void setWaitingListVisiblePublic(boolean visible) {
			
		}

		@Override
		public boolean isDownloadMembersLists() {
			return false;
		}

		@Override
		public void setDownloadMembersLists(boolean downloadMembersLists) {
			
		}

		@Override
		public boolean isAllowToLeave() {
			return false;
		}

		@Override
		public void setAllowToLeave(boolean allow) {
			
		}

		@Override
		public Integer getMaxParticipants() {
			return null;
		}

		@Override
		public void setMaxParticipants(Integer maxParticipants) {
			
		}

		@Override
		public Integer getMinParticipants() {
			return null;
		}

		@Override
		public void setMinParticipants(Integer minParticipants) {
			
		}

		@Override
		public Boolean getAutoCloseRanksEnabled() {
			return null;
		}

		@Override
		public void setAutoCloseRanksEnabled(Boolean autoCloseRanksEnabled) {
			
		}

		@Override
		public Boolean getWaitingListEnabled() {
			return null;
		}

		@Override
		public void setWaitingListEnabled(Boolean waitingListEnabled) {
			
		}
		
	}

}
