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
package org.olat.course.assessment.ui.tool;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
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
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.EndStatus;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.jpa.AssessmentRunningTestSessionInfos;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ConfirmStopAssessmentModeController extends FormBasicController {
	
	private AssessmentMode mode;
	private MultipleSelectionElement withExtraTimeEl;
	private MultipleSelectionElement withDisadvantagesEl;
	private MultipleSelectionElement pullRunningSessionsEl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private QTI21Service qti21Service;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private AssessmentModeManager assessmentModeManager;
	@Autowired
	private AssessmentModeCoordinationService assessmentModeCoordinationService;
	
	public ConfirmStopAssessmentModeController(UserRequest ureq, WindowControl wControl, AssessmentMode mode) {
		super(ureq, wControl);
		this.mode = mode;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_assessment_mode_stop");
		
		List<String> nodeList = mode.getElementAsList();
		mode = assessmentModeManager.getAssessmentModeById(mode.getKey());
		List<Long> assessedIdentityKeys = new ArrayList<>(assessmentModeManager.getAssessedIdentityKeys(mode));
		boolean extensionTime = assessmentModeCoordinationService.isDisadvantageCompensationExtensionTime(mode);
		
		boolean withDisadvantageCompensation = !extensionTime || mode.getEndStatus() == null
				|| mode.getEndStatus() == EndStatus.withoutBoth || mode.getEndStatus() == EndStatus.withoutDisadvantage;
		List<DisadvantageCompensation> disadvantageCompensations = withDisadvantageCompensation
				? assessmentModeCoordinationService.getDisadvantageCompensations(mode)
				: List.of();
		List<Long> disadvantageCompensationIdentitiesKeys = disadvantageCompensations.stream()
				.map(comp -> comp.getIdentity().getKey())
				.filter(key -> assessedIdentityKeys.contains(key))
				.distinct()
				.toList();
		
		List<AssessmentRunningTestSessionInfos> runningSessions = getAssessmentTestSessionsRunning(assessedIdentityKeys, nodeList);
		boolean withExtraTime = !extensionTime || mode.getEndStatus() == null
				|| mode.getEndStatus() == EndStatus.withoutBoth || mode.getEndStatus() == EndStatus.withoutExtraTime;
		List<Long> runningIdentityKeysWithExtraTime = withExtraTime
				? runningSessions.stream()
						.filter(infos -> infos.extraTime() != null && infos.extraTime().intValue() > 0)
						.map(AssessmentRunningTestSessionInfos::identityKey)
						.filter(key -> assessedIdentityKeys.contains(key))
						.distinct()
						.toList()
				: List.of();
		
		String msgStop = translate("confirm.stop.text", StringHelper.escapeHtml(mode.getName()));
		if(!assessedIdentityKeys.isEmpty()) {
			int numOfParticipants = extensionTime
					? disadvantageCompensationIdentitiesKeys.size() + runningIdentityKeysWithExtraTime.size()
					: assessedIdentityKeys.size();
			String key = assessedIdentityKeys.size() == 1
					? "confirm.stop.text.participant"
					: "confirm.stop.text.participants";
			msgStop += " " + translate(key, Integer.toString(numOfParticipants));
		}
		if(!runningIdentityKeysWithExtraTime.isEmpty() || !disadvantageCompensationIdentitiesKeys.isEmpty()) {
			int numOf= runningIdentityKeysWithExtraTime.size() + disadvantageCompensationIdentitiesKeys.size();
			String key = numOf == 1
					? "confirm.stop.text.disadvantage.participant"
					: "confirm.stop.text.disadvantage.participants";
			msgStop += " " + translate(key, Integer.toString(numOf));
		}
		setFormTranslatedWarning(msgStop);
		
		// If already in time extension, participants without extra time or disadvantage compensation are already ended
		if(!extensionTime) {
			assessedIdentityKeys.removeAll(runningIdentityKeysWithExtraTime);
			assessedIdentityKeys.removeAll(disadvantageCompensationIdentitiesKeys);
			initParticipants(formLayout, assessedIdentityKeys);
		}
		initExtraTime(formLayout, runningIdentityKeysWithExtraTime);
		initDisadvantageCompensations(formLayout, disadvantageCompensationIdentitiesKeys);
		
		// Show pull tests only if running tests
		if(!runningSessions.isEmpty()) {
			SelectionValues keyValues = new SelectionValues();
			keyValues.add(SelectionValues.entry("with", translate("confirm.stop.pull.running.sessions.option")));
			pullRunningSessionsEl = uifactory.addCheckboxesHorizontal("runningSessions", "confirm.stop.pull.running.sessions", formLayout,
					keyValues.keys(), keyValues.values());
			pullRunningSessionsEl.select(keyValues.keys()[0], true);
		}
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("stop", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void initParticipants(FormItemContainer formLayout, List<Long> assessedIdentityKeys) {
		if(assessedIdentityKeys.isEmpty()) return;
		
		List<String> participants = getListOfIdentities(assessedIdentityKeys);
		uifactory.addStaticListElement("participants.with", "confirm.participants", participants, formLayout);
	}
	
	private void initExtraTime(FormItemContainer formLayout, List<Long> runningIdentityKeysWithExtraTime) {
		if(runningIdentityKeysWithExtraTime.isEmpty()) return;

		String page = velocity_root + "/check_with_list.html";
		FormLayoutContainer customCont = uifactory.addCustomFormLayout("withExtraTime", "confirm.disadvantage.compensations", page, formLayout);
		String label = "<span><i class='o_icon o_icon-fw o_icon_extra_time'> </i> " + translate("confirm.extra.time") + "</span>";
		customCont.setLabel(label, null, false);
		
		SelectionValues keyValues = new SelectionValues();
		String optionKey = runningIdentityKeysWithExtraTime.size() == 1
				? "confirm.stop.text.exam"
				: "confirm.stop.text.exams";
		keyValues.add(SelectionValues.entry("with", translate(optionKey, Integer.toString(runningIdentityKeysWithExtraTime.size()))));
		withExtraTimeEl = uifactory.addCheckboxesHorizontal("withChecks", "confirm.extra.time", customCont,
				keyValues.keys(), keyValues.values());
		withExtraTimeEl.setElementCssClass("o_assessment_mode_check");
		
		List<String> participants = getListOfIdentities(runningIdentityKeysWithExtraTime);
		uifactory.addStaticListElement("participants.list", null, participants, customCont);
	}
	
	private void initDisadvantageCompensations(FormItemContainer formLayout, List<Long> disadvantageCompensationIdentitiesKeys) {
		if(disadvantageCompensationIdentitiesKeys.isEmpty()) return;
		
		String page = velocity_root + "/check_with_list.html";
		FormLayoutContainer customCont = uifactory.addCustomFormLayout("withDisadvantages", "confirm.disadvantage.compensations", page, formLayout);
		String label = "<span><i class='o_icon o_icon-fw o_icon_disadvantage_compensation'> </i> " + translate("confirm.disadvantage.compensations") + "</span>";
		customCont.setLabel(label, null, false);
		
		SelectionValues keyValues = new SelectionValues();
		String optionKey = disadvantageCompensationIdentitiesKeys.size() == 1
				? "confirm.stop.text.exam"
				: "confirm.stop.text.exams";
		keyValues.add(SelectionValues.entry("with", translate(optionKey, Integer.toString(disadvantageCompensationIdentitiesKeys.size()))));
		withDisadvantagesEl = uifactory.addCheckboxesHorizontal("withChecks", null, customCont,
				keyValues.keys(), keyValues.values());
		withDisadvantagesEl.setElementCssClass("o_assessment_mode_check");
		
		List<String> participants = getListOfIdentities(disadvantageCompensationIdentitiesKeys);
		uifactory.addStaticListElement("participants.list", null, participants, customCont);
	}
	
	private List<String> getListOfIdentities(List<Long> identitiesKeys) {
		final Collator collator = Collator.getInstance(getLocale());
		final List<Identity> identities = securityManager.loadIdentityByKeys(identitiesKeys);
		return identities.stream()
				.map(id -> userManager.getUserDisplayName(id))
				.sorted((s1, s2) -> collator.compare(s1, s2))
				.toList();
	}
	
	private List<AssessmentRunningTestSessionInfos> getAssessmentTestSessionsRunning(List<Long> assessedIdentityKeys, List<String> nodeList) {
		List<IdentityRef> identities = assessedIdentityKeys.stream()
				.map(IdentityRefImpl::new)
				.collect(Collectors.toList());
		return qti21Service.getRunningAssessmentTestSession(mode.getRepositoryEntry(), nodeList, identities);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		AssessmentMode reloadedMode = assessmentModeManager.getAssessmentModeById(mode.getKey());
		boolean pullTests = pullRunningSessionsEl != null && pullRunningSessionsEl.isAtLeastSelected(1);
		boolean withExtraTime = withExtraTimeEl == null || withExtraTimeEl.isAtLeastSelected(1);
		boolean withDisadvantaged = withDisadvantagesEl == null || withDisadvantagesEl.isAtLeastSelected(1);
		assessmentModeCoordinationService.stopAssessment(reloadedMode, pullTests, withExtraTime, withDisadvantaged, getIdentity());
		dbInstance.commit();
		getLogger().info(Tracing.M_AUDIT, "Stop assessment mode : {} ({}) pull tests: {}, with disadvantaged: {}", reloadedMode.getName(), reloadedMode.getKey(), pullTests, withDisadvantaged);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
