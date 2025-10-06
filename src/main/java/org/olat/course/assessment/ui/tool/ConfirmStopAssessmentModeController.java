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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
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
	
	private static int LIST_SIZE = 5;
	
	private AssessmentMode mode;

	private FormLink extraTimeLink;
	private FormLink moreParticipantsLink;
	private FormLink disadvantageCompensationsLink;
	private MultipleSelectionElement withExtraTimeEl;
	private MultipleSelectionElement withDisadvantagesEl;
	private MultipleSelectionElement pullRunningSessionsEl;
	
	private MoreIdentitiesController moreIdentitiesCtrl;
	private CloseableCalloutWindowController calloutCtrl;

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
				.toList();
		
		List<AssessmentRunningTestSessionInfos> runningSessions = getAssessmentTestSessionsRunning(assessedIdentityKeys, nodeList);
		boolean withExtraTime = !extensionTime || mode.getEndStatus() == null
				|| mode.getEndStatus() == EndStatus.withoutBoth || mode.getEndStatus() == EndStatus.withoutExtraTime;
		List<Long> runningIdentityKeysWithExtraTime = withExtraTime
				? runningSessions.stream()
						.filter(infos -> infos.extraTime() != null && infos.extraTime().intValue() > 0)
						.map(AssessmentRunningTestSessionInfos::identityKey)
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
		if(!runningIdentityKeysWithExtraTime.isEmpty() || !disadvantageCompensations.isEmpty()) {
			int numOf= runningIdentityKeysWithExtraTime.size() + disadvantageCompensations.size();
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
		
		String page = velocity_root + "/confirm_stop_participants.html";
		FormLayoutContainer customLayout = uifactory.addCustomFormLayout("participants", "confirm.participants", page, formLayout);
		
		initListOfIdentities(customLayout, assessedIdentityKeys);
		moreParticipantsLink = initMoreIdentities(customLayout, assessedIdentityKeys);
	}
	
	private void initExtraTime(FormItemContainer formLayout, List<Long> runningIdentityKeysWithExtraTime) {
		if(runningIdentityKeysWithExtraTime.isEmpty()) return;
		
		String page = velocity_root + "/confirm_stop_check_participants.html";
		FormLayoutContainer customLayout = uifactory.addCustomFormLayout("extra.time", "confirm.extra.time", page, formLayout);
		String label = "<span><i class='o_icon o_icon-fw o_icon_extra_time'> </i> " + translate("confirm.extra.time") + "</span>";
		customLayout.setLabel(label, null, false);
		
		customLayout.setLabel(label, null, false);
		
		SelectionValues keyValues = new SelectionValues();
		String optionKey = runningIdentityKeysWithExtraTime.size() == 1
				? "confirm.stop.text.exam"
				: "confirm.stop.text.exams";
		keyValues.add(SelectionValues.entry("with", translate(optionKey, Integer.toString(runningIdentityKeysWithExtraTime.size()))));
		withExtraTimeEl = uifactory.addCheckboxesHorizontal("withExtraTime", null, customLayout,
				keyValues.keys(), keyValues.values());
		
		initListOfIdentities(customLayout, runningIdentityKeysWithExtraTime);
		extraTimeLink = initMoreIdentities(customLayout, runningIdentityKeysWithExtraTime);
	}
	
	private void initDisadvantageCompensations(FormItemContainer formLayout, List<Long> disadvantageCompensationIdentitiesKeys) {
		if(disadvantageCompensationIdentitiesKeys.isEmpty()) return;
		
		String page = velocity_root + "/confirm_stop_check_participants.html";
		FormLayoutContainer customLayout = uifactory.addCustomFormLayout("confirm.disadvantage.compensations", "confirm.disadvantage.compensations", page, formLayout);
		String label = "<span><i class='o_icon o_icon-fw o_icon_disadvantage_compensation'> </i> " + translate("confirm.disadvantage.compensations") + "</span>";
		customLayout.setLabel(label, null, false);
		
		SelectionValues keyValues = new SelectionValues();
		String optionKey = disadvantageCompensationIdentitiesKeys.size() == 1
				? "confirm.stop.text.exam"
				: "confirm.stop.text.exams";
		keyValues.add(SelectionValues.entry("with", translate(optionKey, Integer.toString(disadvantageCompensationIdentitiesKeys.size()))));
		withDisadvantagesEl = uifactory.addCheckboxesHorizontal("withDisadvantages", null, customLayout,
				keyValues.keys(), keyValues.values());
		
		initListOfIdentities(customLayout, disadvantageCompensationIdentitiesKeys);
		disadvantageCompensationsLink = initMoreIdentities(customLayout, disadvantageCompensationIdentitiesKeys);
	}
	
	private void initListOfIdentities(FormLayoutContainer formLayout, List<Long> identitiesKeys) {
		List<Long> firstIdentityKeys;
		boolean split = identitiesKeys.size() > LIST_SIZE;
		if(split) {
			firstIdentityKeys = identitiesKeys.subList(0, LIST_SIZE);
		} else {
			firstIdentityKeys = identitiesKeys;
		}
		
		List<Identity> identities = securityManager.loadIdentityByKeys(firstIdentityKeys);
		List<String> moreParticipants = identities.stream()
				.map(id -> userManager.getUserDisplayName(id))
				.toList();
		formLayout.contextPut("participants", moreParticipants);
	}

	private FormLink initMoreIdentities(FormLayoutContainer formLayout, List<Long> identitiesKeys) {
		if(identitiesKeys.size() <= LIST_SIZE) return null;
		
		int additionalParticipants = identitiesKeys.size() - LIST_SIZE;
		String i18nKey = additionalParticipants <= 1 ? "confirm.participants.more" : "confirm.participants.more.plural";
		String link = translate(i18nKey, Integer.toString(additionalParticipants));
		FormLink moreLink = uifactory.addFormLink("more.participants", link, null, formLayout, Link.LINK | Link.NONTRANSLATED);
		List<Long> moreParticipantsKeys = identitiesKeys.subList(LIST_SIZE, identitiesKeys.size());
		moreLink.setUserObject(new IdentitiesKeys(moreParticipantsKeys));
		return moreLink;
	}
	
	private List<AssessmentRunningTestSessionInfos> getAssessmentTestSessionsRunning(List<Long> assessedIdentityKeys, List<String> nodeList) {
		List<IdentityRef> identities = assessedIdentityKeys.stream()
				.map(IdentityRefImpl::new)
				.collect(Collectors.toList());
		return qti21Service.getRunningAssessmentTestSession(mode.getRepositoryEntry(), nodeList, identities);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(moreIdentitiesCtrl == source || calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(moreIdentitiesCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		moreIdentitiesCtrl = null;
		calloutCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(extraTimeLink == source && extraTimeLink.getUserObject() instanceof IdentitiesKeys keys) {
			doOpenIdentitiesCallout(ureq, extraTimeLink, keys.identitiesKeys());
		} else if(moreParticipantsLink == source && moreParticipantsLink.getUserObject() instanceof IdentitiesKeys keys) {
			doOpenIdentitiesCallout(ureq, moreParticipantsLink, keys.identitiesKeys());
		} else if(disadvantageCompensationsLink == source && disadvantageCompensationsLink.getUserObject() instanceof IdentitiesKeys keys) {
			doOpenIdentitiesCallout(ureq, disadvantageCompensationsLink, keys.identitiesKeys());
		}
		super.formInnerEvent(ureq, source, event);
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
	
	private void doOpenIdentitiesCallout(UserRequest ureq, FormLink link, List<Long> identitiesKeys) {
		List<String> names = securityManager.loadIdentityByKeys(identitiesKeys).stream()
				.map(id -> userManager.getUserDisplayName(id))
				.toList();
		
		moreIdentitiesCtrl = new MoreIdentitiesController(ureq, getWindowControl(), names);
		listenTo(moreIdentitiesCtrl);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				moreIdentitiesCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private record IdentitiesKeys(List<Long> identitiesKeys) {
		//
	}
	
	private class MoreIdentitiesController extends BasicController {
		
		public MoreIdentitiesController(UserRequest ureq, WindowControl wControl, List<String> names) {
			super(ureq, wControl);
			
			VelocityContainer mainVC = createVelocityContainer("confirm_more_participants");
			mainVC.contextPut("names", names);
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			//
		}
	}
}
