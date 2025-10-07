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
package org.olat.ims.qti21.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.nodes.iq.QTI21AssessmentRunController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroupService;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.DigitalSignatureOptions;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.assessment.Role;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.modules.dcompensation.DisadvantageCompensationService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21RetrieveTestsController extends FormBasicController {

	private MultipleSelectionElement withExtraTimeEl;
	private MultipleSelectionElement withDisadvantagesEl;
	
	private IQTESTCourseNode courseNode;
	private RepositoryEntry assessedEntry;
	private final List<Identity> identities;
	private final List<AssessmentTestSession> sessions;
	private final Set<Long> identityKeysWithCompensations;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private InstantMessagingService imService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private DisadvantageCompensationService disadvantageCompensationService;
	
	public QTI21RetrieveTestsController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv,
			AssessmentToolOptions asOptions, IQTESTCourseNode courseNode) {
		super(ureq, wControl);

		this.courseNode = courseNode;
		identities = getIdentities(asOptions, courseEnv);
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		sessions = qtiService
				.getRunningAssessmentTestSession(courseEntry, courseNode.getIdent(), courseNode.getReferencedRepositoryEntry());
		identityKeysWithCompensations = getDisadvanatgeCompensationsOfSessions(courseEntry, sessions);
		
		initForm(ureq);
	}
	
	public QTI21RetrieveTestsController(UserRequest ureq, WindowControl wControl,
			AssessmentTestSession session, IQTESTCourseNode courseNode) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		identities = Collections.singletonList(session.getIdentity());
		sessions = Collections.singletonList(session);
		identityKeysWithCompensations = getDisadvanatgeCompensationsOfSessions(session.getRepositoryEntry(), sessions);
		
		initForm(ureq);
	}
	
	public QTI21RetrieveTestsController(UserRequest ureq, WindowControl wControl, RepositoryEntry  assessedEntry,
			AssessmentToolOptions asOptions) {
		super(ureq, wControl);
		this.assessedEntry = assessedEntry;
		identities = getIdentities(asOptions, null);
		sessions = qtiService.getRunningAssessmentTestSession(assessedEntry, null, assessedEntry);
		identityKeysWithCompensations = Collections.emptySet();
		
		initForm(ureq);
	}
	
	private Set<Long> getDisadvanatgeCompensationsOfSessions(RepositoryEntry courseEntry, List<AssessmentTestSession> sessionsList) {
		if(courseNode == null) return Collections.emptySet();
		
		Set<Long> identityKeys = sessionsList.stream()
				.map(session -> session.getIdentity().getKey())
				.collect(Collectors.toSet());
		
		List<DisadvantageCompensation> allCompensations = disadvantageCompensationService
				.getActiveDisadvantageCompensations(courseEntry, courseNode.getIdent());
		return allCompensations.stream()
				.filter(compensation -> identityKeys.contains(compensation.getIdentity().getKey()))
				.map(compensation -> compensation.getIdentity().getKey())
				.collect(Collectors.toSet());
	}
	
	private List<Identity> getIdentities(AssessmentToolOptions asOptions, CourseEnvironment courseEnv) {
		List<Identity> identityList;
		if(asOptions.getGroup() == null && asOptions.getIdentities() == null) {
			if(courseEnv != null) {
				identityList = ScoreAccountingHelper.loadUsers(courseEnv);
			} else {
				identityList = repositoryService.getMembers(assessedEntry, RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.participant.name());
			}
		} else if (asOptions.getIdentities() != null) {
			identityList = asOptions.getIdentities();
		} else {
			identityList = businessGroupService.getMembers(asOptions.getGroup());
		}
		return identityList;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<Identity> extraTimeList = new ArrayList<>();
		List<Identity> assessedIdentitiesList = new ArrayList<>();
		List<Identity> disadvantageCompensationList = new ArrayList<>();
		Set<Identity> deduplicateSet = new HashSet<>();
		for(AssessmentTestSession session:sessions) {
			Identity assessedIdentity = session.getIdentity();
			if(identities.contains(assessedIdentity) && !deduplicateSet.contains(assessedIdentity)) {
				if(identityKeysWithCompensations.contains(assessedIdentity.getKey())) {
					disadvantageCompensationList.add(assessedIdentity);
				} else if(session.getExtraTime() != null && session.getExtraTime().intValue() > 0) {
					extraTimeList.add(assessedIdentity);
				} else {
					assessedIdentitiesList.add(assessedIdentity);
				}
				deduplicateSet.add(assessedIdentity);
			}
		}
		
		String msg;
		if(sessions.isEmpty()) {
			msg = translate("retrievetest.nothing.todo");
		} else {
			String i18nKey = sessions.size() == 1
					? "confirm.pull.text.test"
					: "confirm.pull.text.tests";
			msg = translate(i18nKey, Integer.toString(sessions.size()));
		}
		if(!extraTimeList.isEmpty() || !disadvantageCompensationList.isEmpty()) {
			int numOf= extraTimeList.size() + disadvantageCompensationList.size();
			String key = numOf == 1
					? "confirm.pull.text.disadvantage.participant"
					: "confirm.pull.text.disadvantage.participants";
			msg += " " + translate(key, Integer.toString(numOf));
		}
		setFormTranslatedWarning(msg);
		
		initParticipants(formLayout, assessedIdentitiesList);
		initExtraTime(formLayout, extraTimeList);
		initDisadvantageCompensations(formLayout, disadvantageCompensationList);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("menu.retrieve.tests.title", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void initParticipants(FormItemContainer formLayout, List<Identity> assessedIdentityKeys) {
		if(assessedIdentityKeys.isEmpty()) return;
		
		List<String> participants = getListOfIdentities(assessedIdentityKeys);
		uifactory.addStaticListElement("participants.with", "confirm.pull.participants", participants, formLayout);
	}
	
	private void initExtraTime(FormItemContainer formLayout, List<Identity> runningIdentitiesWithExtraTime) {
		if(runningIdentitiesWithExtraTime.isEmpty()) return;
		
		SelectionValues keyValues = new SelectionValues();
		String optionKey = runningIdentitiesWithExtraTime.size() == 1
				? "confirm.stop.text.test"
				: "confirm.stop.text.tests";
		keyValues.add(SelectionValues.entry("with", translate(optionKey, Integer.toString(runningIdentitiesWithExtraTime.size()))));
		withExtraTimeEl = uifactory.addCheckboxesHorizontal("withExtraTime", null, formLayout,
				keyValues.keys(), keyValues.values());
		String label = "<span><i class='o_icon o_icon-fw o_icon_extra_time'> </i> " + translate("confirm.extra.time") + "</span>";
		withExtraTimeEl.setLabel(label, null, false);
		withExtraTimeEl.setElementCssClass("o_assessment_mode_check");
		
		List<String> participants = getListOfIdentities(runningIdentitiesWithExtraTime);
		uifactory.addStaticListElement("participants.with.extra", null, participants, formLayout);
	}
	
	private void initDisadvantageCompensations(FormItemContainer formLayout, List<Identity> disadvantageCompensationIdentities) {
		if(disadvantageCompensationIdentities.isEmpty()) return;
	
		SelectionValues keyValues = new SelectionValues();
		String optionKey = disadvantageCompensationIdentities.size() == 1
				? "confirm.stop.text.test"
				: "confirm.stop.text.tests";
		keyValues.add(SelectionValues.entry("with", translate(optionKey, Integer.toString(disadvantageCompensationIdentities.size()))));
		withDisadvantagesEl = uifactory.addCheckboxesHorizontal("withDisadvantages", null, formLayout,
				keyValues.keys(), keyValues.values());
		String label = "<span><i class='o_icon o_icon-fw o_icon_disadvantage_compensation'> </i> " + translate("confirm.disadvantage.compensations") + "</span>";
		withDisadvantagesEl.setLabel(label, null, false);
		withDisadvantagesEl.setElementCssClass("o_assessment_mode_check");
		
		List<String> participants = getListOfIdentities(disadvantageCompensationIdentities);
		uifactory.addStaticListElement("participants.with.compensations", null, participants, formLayout);
	}
	
	private List<String> getListOfIdentities(List<Identity> identities) {
		return identities.stream()
				.map(id -> userManager.getUserDisplayName(id))
				.toList();
	}
	

	@Override
	protected void formOK(UserRequest ureq) {
		boolean withExtraTime = withExtraTimeEl == null || withExtraTimeEl.isAtLeastSelected(1);
		boolean withCompensations = withDisadvantagesEl == null || withDisadvantagesEl.isAtLeastSelected(1);
	
		for(AssessmentTestSession session:sessions) {
			boolean compensation = identityKeysWithCompensations.contains(session.getIdentity().getKey());
			if(compensation) {
				if(withCompensations) {
					doRetrieveTest(session);
				}
			} else if(session.getExtraTime() != null && session.getExtraTime().intValue() > 0) {
				if(withExtraTime) {
					doRetrieveTest(session);
				}
			} else {
				doRetrieveTest(session);
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void doRetrieveTest(AssessmentTestSession session) {
		session = qtiService.getAssessmentTestSession(session.getKey());
		session = qtiService.pullSession(session, getSignatureOptions(session), getIdentity());
		if(courseNode != null) {
			RepositoryEntry courseEntry = session.getRepositoryEntry();
			CourseEnvironment courseEnv = CourseFactory.loadCourse(courseEntry).getCourseEnvironment();
			UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
					.createAndInitUserCourseEnvironment(session.getIdentity(), courseEnv);
			courseNode.pullAssessmentTestSession(session, assessedUserCourseEnv, getIdentity(), Role.coach, getLocale());
			
			// End chat
			String channel = session.getIdentity() == null ? session.getAnonymousIdentifier() : session.getIdentity().getKey().toString();
			imService.endChannel(getIdentity(), courseEntry.getOlatResource(), courseNode.getIdent(), channel);
		}
		dbInstance.commitAndCloseSession();
	}
	
	private DigitalSignatureOptions getSignatureOptions(AssessmentTestSession session) {
		if(courseNode == null) return null;
		
		RepositoryEntry testEntry = session.getTestEntry();
		RepositoryEntry courseEntry = session.getRepositoryEntry();
		QTI21DeliveryOptions deliveryOptions = qtiService.getDeliveryOptions(testEntry);
		
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		boolean digitalSignature = config.getBooleanSafe(IQEditController.CONFIG_DIGITAL_SIGNATURE,
			deliveryOptions.isDigitalSignature());
		boolean sendMail = config.getBooleanSafe(IQEditController.CONFIG_DIGITAL_SIGNATURE_SEND_MAIL,
			deliveryOptions.isDigitalSignatureMail());

		DigitalSignatureOptions options = new DigitalSignatureOptions(digitalSignature, sendMail, courseEntry, testEntry);
		if(digitalSignature) {
			CourseEnvironment courseEnv = CourseFactory.loadCourse(courseEntry).getCourseEnvironment();
			QTI21AssessmentRunController.decorateCourseConfirmation(session, options, courseEnv, courseNode, testEntry, null, getLocale());
		}
		return options;
	}
}