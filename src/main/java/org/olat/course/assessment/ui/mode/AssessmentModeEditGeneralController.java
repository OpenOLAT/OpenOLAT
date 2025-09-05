/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.mode;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.DateUtils;
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
 * Initial date: 24 janv. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AssessmentModeEditGeneralController extends FormBasicController {

	private static final String[] startModeKeys = new String[] { "automatic", "manual" };

	private SingleSelection startModeEl;
	private IntegerElement leadTimeEl;
	private IntegerElement followupTimeEl;
	private DateChooser beginEl;
	private DateChooser endEl;
	private TextElement nameEl;
	private RichTextElement descriptionEl;

	private CloseableModalController cmc;
	private DialogBoxController confirmCtrl;
	
	private final RepositoryEntry entry;
	private AssessmentMode assessmentMode;
	private final OLATResourceable courseOres;
	
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	@Autowired
	private AssessmentModeCoordinationService modeCoordinationService;
	
	public AssessmentModeEditGeneralController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, AssessmentMode assessmentMode) {
		super(ureq, wControl);
		this.entry = entry;
		courseOres = OresHelper.clone(entry.getOlatResource());
		if(assessmentMode.getKey() == null) {
			this.assessmentMode = assessmentMode;
		} else {
			this.assessmentMode = assessmentModeMgr.getAssessmentModeById(assessmentMode.getKey());
		}
		initForm(ureq);
	}
	
	protected AssessmentMode getAssessmentMode() {
		return assessmentMode;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_assessment_mode_edit_form");
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
		
		Status status = assessmentMode.getStatus();
		String name = assessmentMode.getName();
		nameEl = uifactory.addTextElement("mode.name", "mode.name", 255, name, formLayout);
		nameEl.setElementCssClass("o_sel_assessment_mode_name");
		nameEl.setMandatory(true);
		nameEl.setEnabled(status != Status.followup && status != Status.end
				&& !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.name));
		
		String desc = assessmentMode.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringData("mode.description", "mode.description",
				desc, 6, -1, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		descriptionEl.getEditorConfiguration().setPathInStatusBar(false);
		descriptionEl.setEnabled(status != Status.followup && status != Status.end
				&& !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.description));
		
		beginEl = uifactory.addDateChooser("mode.begin", assessmentMode.getBegin(), formLayout);
		beginEl.setElementCssClass("o_sel_assessment_mode_begin");
		beginEl.setDateChooserTimeEnabled(true);
		beginEl.setValidDateCheck("form.error.date");
		beginEl.setMandatory(true);
		beginEl.setEnabled((status == Status.none || status == Status.leadtime)
				&& !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.begin));
		
		
		int leadTime = assessmentMode.getLeadTime();
		if(leadTime < 0) {
			leadTime = 0;
		}
		leadTimeEl = uifactory.addIntegerElement("mode.leadTime", leadTime, formLayout);
		leadTimeEl.setElementCssClass("o_sel_assessment_mode_leadtime");
		leadTimeEl.setDisplaySize(3);
		leadTimeEl.setEnabled((status == Status.none || status == Status.leadtime)
				&& !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.leadTime));
		leadTimeEl.setInlineValidationOn(true);
		
		endEl = uifactory.addDateChooser("mode.end", assessmentMode.getEnd(), formLayout);
		endEl.setElementCssClass("o_sel_assessment_mode_end");
		endEl.setDateChooserTimeEnabled(true);
		endEl.setValidDateCheck("form.error.date");
		endEl.setDefaultValue(beginEl);
		endEl.setMandatory(true);
		endEl.setEnabled(status != Status.end && !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.end));
		beginEl.setPushDateValueTo(endEl);
		
		int followupTime = assessmentMode.getFollowupTime();
		if(followupTime < 0) {
			followupTime = 0;
		}
		followupTimeEl = uifactory.addIntegerElement("mode.followupTime", followupTime, formLayout);
		followupTimeEl.setElementCssClass("o_sel_assessment_mode_followuptime");
		followupTimeEl.setDisplaySize(3);
		followupTimeEl.setEnabled(status != Status.end
				&& !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.followupTime));
		
		String[] startModeValues = new String[] {
				translate("mode.beginend.automatic"), translate("mode.beginend.manual")
		};
		startModeEl = uifactory.addDropdownSingleselect("mode.beginend", formLayout, startModeKeys, startModeValues, null);
		startModeEl.setElementCssClass("o_sel_assessment_mode_start_mode");
		if(assessmentMode.isManualBeginEnd()) {
			startModeEl.select(startModeKeys[1], true);
		} else {
			startModeEl.select(startModeKeys[0], true);
		}
		startModeEl.setEnabled(status != Status.end
				&& !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.mode));
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("button", getTranslator());
		formLayout.add(buttonCont);
		if(status != Status.end && !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.general)) {
			uifactory.addFormSubmitButton("save", buttonCont);
		}
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		// Only update end date if needed, not the whole container
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
	protected boolean validateFormItem(UserRequest ureq, FormItem item) {
		boolean ok = super.validateFormItem(ureq, item);

		if (item == leadTimeEl) {
			leadTimeEl.clearWarning();
			try {
				if (leadTimeEl.getIntValue() >= 30) {
					leadTimeEl.setWarningKey("form.lead.time.long", String.valueOf(leadTimeEl.getIntValue()));
				}
			} catch (NumberFormatException e) {
				leadTimeEl.setErrorKey("integer.element.int.error");
			}
		}

		return ok;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		nameEl.clearError();
		if(StringHelper.containsNonWhitespace(nameEl.getValue())) {
			//too long
		} else {
			nameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		endEl.clearError();
		beginEl.clearError();
		if(beginEl.getDate() == null) {
			beginEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(!validateFormItem(ureq, beginEl)) {
			allOk &= false;
		} else if(beginEl.isEnabled() && !validateFutureDate(ureq, beginEl)) {
			allOk &= false;
		}
		if(endEl.getDate() == null) {
			endEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(!validateFormItem(ureq, endEl)) {
			allOk &= false;
		}
		
		if(beginEl.getDate() != null && endEl.getDate() != null
				&& beginEl.getDate().compareTo(endEl.getDate()) >= 0) {
			beginEl.setErrorKey("error.begin.after.end");
			endEl.setErrorKey("error.begin.after.end");
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateFutureDate(UserRequest ureq, DateChooser el) {
		boolean allOk = true;
		
		if(el.getDate() != null) {
			Date d = DateUtils.roundToMinute(el.getDate());
			Date u = DateUtils.roundToMinute(ureq.getRequestTimestamp());
			if(d.compareTo(u) <= 0) {
				el.setErrorKey("error.future.date");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Date begin = beginEl.getDate();
		Date end = endEl.getDate();
		int followupTime = followupTimeEl.getIntValue();
		int leadTime = leadTimeEl.getIntValue();

		Status currentStatus = assessmentMode.getStatus();
		if(startModeEl.isOneSelected() && startModeEl.isSelected(1)) {
			//manual start don't change the status of the assessment
			save(ureq, false);
		} else {
			Status nextStatus = modeCoordinationService.evaluateStatus(begin, leadTime, end, followupTime);
			if(currentStatus == nextStatus) {
				save(ureq, true);
			} else {
				String title = translate("confirm.status.change.title");
	
				String text = switch (nextStatus) {
					case none -> translate("confirm.status.change.none");
					case leadtime -> translate("confirm.status.change.leadtime");
					case assessment -> translate("confirm.status.change.assessment");
					case followup -> translate("confirm.status.change.followup");
					case end -> translate("confirm.status.change.end");
					default -> "ERROR";
				};
				confirmCtrl = activateOkCancelDialog(ureq, title, text, confirmCtrl);
			}
		}
	}
	
	private void save(UserRequest ureq, boolean forceStatus) {
		if(assessmentMode.getKey() != null) {
			assessmentMode = assessmentModeMgr.getAssessmentModeById(assessmentMode.getKey());
		} else if(assessmentMode.getTargetAudience() == null) {
			// setup default access group
			assessmentMode.setTargetAudience(AssessmentMode.Target.course);
		}

		assessmentMode.setName(nameEl.getValue());
		assessmentMode.setDescription(descriptionEl.getValue());
		
		assessmentMode.setBegin(beginEl.getDate());
		assessmentMode.setLeadTime(Math.max(leadTimeEl.getIntValue(), 0));
		
		assessmentMode.setEnd(endEl.getDate());
		assessmentMode.setFollowupTime(Math.max(followupTimeEl.getIntValue(), 0));

		assessmentMode.setManualBeginEnd(startModeEl.isOneSelected() && startModeEl.isSelected(1));

		//mode need to be persisted for the following relations
		if(assessmentMode.getKey() == null) {
			assessmentMode = assessmentModeMgr.persist(assessmentMode);
		}

		assessmentMode = assessmentModeMgr.merge(assessmentMode, forceStatus);
		fireEvent(ureq, Event.CHANGED_EVENT);
		
		ChangeAssessmentModeEvent changedEvent = new ChangeAssessmentModeEvent(assessmentMode, entry);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.fireEventToListenersOf(changedEvent, ChangeAssessmentModeEvent.ASSESSMENT_MODE_ORES);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}