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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.ims.qti21.QTI21Service;
import org.olat.modules.dcompensation.DisadvantageCompensationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmStopAssessmentModeController extends FormBasicController {
	
	private AssessmentMode mode;
	
	private MultipleSelectionElement withDisadvantagesEl;
	private MultipleSelectionElement pullRunningSessionsEl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Service qti21Service;
	@Autowired
	private AssessmentModeManager assessmentModeManager;
	@Autowired
	private DisadvantageCompensationService disadvantageCompensationService;
	@Autowired
	private AssessmentModeCoordinationService assessmentModeCoordinationService;
	
	public ConfirmStopAssessmentModeController(UserRequest ureq, WindowControl wControl, AssessmentMode mode) {
		super(ureq, wControl, "confirm_stop");
		this.mode = mode;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<String> nodeList = mode.getElementAsList();
		mode = assessmentModeManager.getAssessmentModeById(mode.getKey());
		Set<Long> assessedIdentityKeys = assessmentModeManager.getAssessedIdentityKeys(mode);
		boolean extensionTime = assessmentModeCoordinationService.isDisadvantageCompensationExtensionTime(mode);
		
		boolean runningSessions;
		if(extensionTime) {
			Set<Long> disadvantegCompensationAssessedIdentityKeys = getIdentitiesWithDisadvantageCompensations(assessedIdentityKeys, nodeList);
			runningSessions = hasAssessmentTestSessionsRunning(disadvantegCompensationAssessedIdentityKeys, nodeList);
			initFormExtensionTime(formLayout);
		} else {
			runningSessions = hasAssessmentTestSessionsRunning(assessedIdentityKeys, nodeList);
			initForm(formLayout, nodeList, assessedIdentityKeys);
		}
		
		if(runningSessions) {
			SelectionValues keyValues = new SelectionValues();
			keyValues.add(SelectionValues.entry("with", translate("confirm.stop.pull.running.sessions")));
			pullRunningSessionsEl = uifactory.addCheckboxesHorizontal("runningSessions", "confirm.stop.pull.running.sessions", formLayout,
					keyValues.keys(), keyValues.values());
			pullRunningSessionsEl.select(keyValues.keys()[0], true);
		}

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("stop", formLayout);
	}
	
	private void initFormExtensionTime(FormItemContainer formLayout) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String name = StringHelper.escapeHtml(mode.getName());
			layoutCont.contextPut("msg", translate("confirm.stop.final.text.details", new String[] { name }));
		}
	}
	
	private void initForm(FormItemContainer formLayout, List<String> nodeList, Set<Long> assessedIdentityKeys) {
		Set<Long> disadvantegCompensationAssessedIdentityKeys = getIdentitiesWithDisadvantageCompensations(assessedIdentityKeys, nodeList);
		int numOfDisadvantagedUsers = disadvantegCompensationAssessedIdentityKeys.size();

		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String name = StringHelper.escapeHtml(mode.getName());
			layoutCont.contextPut("msg", translate("confirm.stop.text.details", new String[] { name }));

			if(numOfDisadvantagedUsers == 1) {
				layoutCont.contextPut("compensationMsg", translate("confirm.stop.text.compensations"));
			} else if(numOfDisadvantagedUsers > 1) {
				layoutCont.contextPut("compensationMsg", translate("confirm.stop.text.compensations.plural",
						new String[] { Integer.toString(numOfDisadvantagedUsers) }));
			}
		}
		
		if(numOfDisadvantagedUsers > 0) {
			SelectionValues keyValues = new SelectionValues();
			keyValues.add(SelectionValues.entry("with", translate("confirm.stop.with.disadvantages")));
			withDisadvantagesEl = uifactory.addCheckboxesHorizontal("disadvantages", "confirm.stop.with.disadvantages", formLayout,
					keyValues.keys(), keyValues.values());
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private Set<Long> getIdentitiesWithDisadvantageCompensations(final Set<Long> assessedIdentityKeys, final List<String> nodeList) {
		final List<IdentityRef> disadvantagedIdentities = disadvantageCompensationService
				.getActiveDisadvantagedUsers(mode.getRepositoryEntry(), nodeList);
		return disadvantagedIdentities.stream()
			.filter(ref -> assessedIdentityKeys.contains(ref.getKey()))
			.map(IdentityRef::getKey)
			.collect(Collectors.toSet());
	}
	
	private boolean hasAssessmentTestSessionsRunning(Set<Long> assessedIdentityKeys, List<String> nodeList) {
		List<IdentityRef> identities = assessedIdentityKeys.stream()
				.map(IdentityRefImpl::new)
				.collect(Collectors.toList());
		return qti21Service.isRunningAssessmentTestSession(mode.getRepositoryEntry(), nodeList, identities);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		AssessmentMode reloadedMode = assessmentModeManager.getAssessmentModeById(mode.getKey());
		boolean pullTests = pullRunningSessionsEl != null && pullRunningSessionsEl.isAtLeastSelected(1);
		boolean withDisadvantaged = withDisadvantagesEl == null || withDisadvantagesEl.isAtLeastSelected(1);
		assessmentModeCoordinationService.stopAssessment(reloadedMode, pullTests, withDisadvantaged, getIdentity());
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
