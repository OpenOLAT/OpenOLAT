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
package org.olat.course.assessment.ui.mode;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.model.AssessmentModeManagedFlag;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 janv. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeEditSafeExamBrowserController extends AbstractEditSafeExamBrowserController {

	private CloseableModalController cmc;
	private DialogBoxController confirmCtrl;
	
	private RepositoryEntry entry;
	private AssessmentMode assessmentMode;
	private final OLATResourceable courseOres;

	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	@Autowired
	private AssessmentModeCoordinationService modeCoordinationService;
	
	public AssessmentModeEditSafeExamBrowserController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, AssessmentMode assessmentMode) {
		super(ureq, wControl, assessmentMode);
		this.entry = entry;
		
		courseOres = OresHelper.clone(entry.getOlatResource());
		if(assessmentMode.getKey() == null) {
			this.assessmentMode = assessmentMode;
		} else {
			this.assessmentMode = assessmentModeMgr.getAssessmentModeById(assessmentMode.getKey());
		}
		initForm(ureq);
		updateUI();
	}
	
	public AssessmentMode getAssessmentMode() {
		return assessmentMode;
	}
	
	@Override
	protected boolean isEditable() {
		Status status = assessmentMode.getStatus();
		boolean editable = (status == null || status == Status.none);
		return editable && !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.safeexambrowser);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);

		Status status = assessmentMode.getStatus();
		safeExamBrowserKeyEl.setEnabled(status != Status.end
				&& !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.safeexambrowser));
		safeExamBrowserHintEl.setEnabled(status != Status.end
				&& !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.safeexambrowser));
		
		FormLayoutContainer buttonsWrapperCont = uifactory.addDefaultFormLayout("buttonsWrapper", null, formLayout);
		FormLayoutContainer buttonCont = uifactory.addButtonsFormLayout("buttons", null, buttonsWrapperCont);
		if(status != Status.end && !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.safeexambrowser)) {
			uifactory.addFormSubmitButton("save", buttonCont);
		}
	}
	
	protected void initSafeExamBrowserForm(FormLayoutContainer enableCont) {
		enableCont.setElementCssClass("o_sel_assessment_mode_edit_form");
		setFormContextHelp("manual_user/learningresources/Assessment_mode/");
		setFormDescription("form.mode.description");
		
		ICourse course = CourseFactory.loadCourse(courseOres);
		if(StringHelper.containsNonWhitespace(assessmentMode.getStartElement())) {
			CourseNode startElement = course.getRunStructure().getNode(assessmentMode.getStartElement());
			if(startElement == null) {
				setFormWarning("warning.missing.start.element");
			}
		}
		
		if(StringHelper.containsNonWhitespace(assessmentMode.getElementList())) {
			String elements = assessmentMode.getElementList();
			for(String element:elements.split(",")) {
				CourseNode node = course.getRunStructure().getNode(element);
				if(node == null) {
					setFormWarning("warning.missing.element");
				}
			}
		}
		
		super.initSafeExamBrowserForm(enableCont);

		downloadConfigEl.setEnabled(!AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.safeexambrowser));
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				save(ureq, true);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		safeExamBrowserKeyEl.clearError();
		if(safeExamBrowserEl.isAtLeastSelected(1) && this.typeOfUseEl.isKeySelected("keys")) {
			String value = safeExamBrowserKeyEl.getValue();
			if(!StringHelper.containsNonWhitespace(value)) {
				safeExamBrowserKeyEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if(value.length() > safeExamBrowserKeyEl.getMaxLength()) {
				safeExamBrowserKeyEl.setErrorKey("form.error.toolong", Integer.toString(safeExamBrowserKeyEl.getMaxLength()));
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(assessmentMode.getKey() != null) {
			assessmentMode = assessmentModeMgr.getAssessmentModeById(assessmentMode.getKey());
		}
		
		Status currentStatus = assessmentMode.getStatus();
		if(assessmentMode.isManualBeginEnd()) {
			//manual start don't change the status of the assessment
			save(ureq, false);
		} else {
			Status nextStatus = modeCoordinationService.evaluateStatus(assessmentMode.getBegin(), assessmentMode.getLeadTime(),
					assessmentMode.getEnd(), assessmentMode.getFollowupTime());
			if(currentStatus == nextStatus) {
				save(ureq, true);
			} else {
				String title = translate("confirm.status.change.title");
	
				String text;
				switch(nextStatus) {
					case none: text = translate("confirm.status.change.none"); break;
					case leadtime: text = translate("confirm.status.change.leadtime"); break;
					case assessment: text = translate("confirm.status.change.assessment"); break;
					case followup: text = translate("confirm.status.change.followup"); break;
					case end: text = translate("confirm.status.change.end"); break;
					default: text = "ERROR";
				}
				confirmCtrl = activateOkCancelDialog(ureq, title, text, confirmCtrl);
			}
		}
	}
	
	private void save(UserRequest ureq, boolean forceStatus) {
		if(assessmentMode.getKey() != null) {
			assessmentMode = assessmentModeMgr.getAssessmentModeById(assessmentMode.getKey());
		}

		commit(assessmentMode);
	
		//mode need to be persisted for the following relations
		if(assessmentMode.getKey() == null) {
			assessmentMode = assessmentModeMgr.persist(assessmentMode);
		}

		assessmentMode = assessmentModeMgr.merge(assessmentMode, forceStatus);
		fireEvent(ureq, Event.CHANGED_EVENT);
		
		// Always update config key
		safeExamBrowserConfigKeyEl.setValue(assessmentMode.getSafeExamBrowserConfigPListKey());
		
		ChangeAssessmentModeEvent changedEvent = new ChangeAssessmentModeEvent(assessmentMode, entry);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.fireEventToListenersOf(changedEvent, ChangeAssessmentModeEvent.ASSESSMENT_MODE_ORES);
	}
}