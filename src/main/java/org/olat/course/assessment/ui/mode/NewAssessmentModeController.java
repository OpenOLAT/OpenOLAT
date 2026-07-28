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

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 juin 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class NewAssessmentModeController extends FormBasicController {

	private SingleSelection startModeEl;
	private IntegerElement leadTimeEl;
	private IntegerElement followupTimeEl;
	private DateChooser beginEl;
	private DateChooser endEl;
	private TextElement nameEl;
	
	private final RepositoryEntry entry;
	private AssessmentMode assessmentMode;

	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	
	public NewAssessmentModeController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.entry = entry;
		
		initForm(ureq);
	}
	
	public AssessmentMode getAssessmentMode() {
		return assessmentMode;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_new_assessment_mode_form");
		
		nameEl = uifactory.addTextElement("mode.name", "mode.name", 255, "", formLayout);
		nameEl.setElementCssClass("o_sel_assessment_mode_name");
		nameEl.setMandatory(true);
		
		FormLayoutContainer datesCont = uifactory.addTwoColumnsFormLayout("dates", null, formLayout);
		beginEl = uifactory.addDateChooser("mode.begin", null, datesCont);
		beginEl.setElementCssClass("o_sel_assessment_mode_begin");
		beginEl.setDateChooserTimeEnabled(true);
		beginEl.setValidDateCheck("form.error.date");
		beginEl.setMandatory(true);
		
		endEl = uifactory.addDateChooser("mode.end", null, datesCont);
		endEl.setElementCssClass("o_sel_assessment_mode_end");
		endEl.setDateChooserTimeEnabled(true);
		endEl.setValidDateCheck("form.error.date");
		endEl.setDefaultValue(beginEl);
		endEl.setMandatory(true);
		beginEl.setPushDateValueTo(endEl);

		leadTimeEl = uifactory.addIntegerElement("mode.leadTime", 0, datesCont);
		leadTimeEl.setElementCssClass("o_sel_assessment_mode_leadtime o_form_number");
		leadTimeEl.setExampleKey("mode.minutes", null);
		leadTimeEl.setDisplaySize(3);
		leadTimeEl.setInlineValidationOn(true);
		
		followupTimeEl = uifactory.addIntegerElement("mode.followupTime", 0, datesCont);
		followupTimeEl.setElementCssClass("o_sel_assessment_mode_followuptime o_form_number");
		followupTimeEl.setExampleKey("mode.minutes", null);
		followupTimeEl.setDisplaySize(3);
		
		SelectionValues startModePK = new SelectionValues();
		startModePK.add(SelectionValues.entry("manual", translate("mode.beginend.manual")));
		startModePK.add(SelectionValues.entry("automatic", translate("mode.beginend.automatic")));
		startModeEl = uifactory.addCardSingleSelectHorizontal("mode.beginend", "mode.beginend", formLayout, startModePK);
		startModeEl.setElementCssClass("o_sel_assessment_mode_start_mode");
		startModeEl.select("manual", true);

		FormLayoutContainer buttonCont = uifactory.addButtonsFormLayout("button", null, formLayout);
		uifactory.addFormSubmitButton("create", buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		nameEl.clearError();
		if(StringHelper.containsNonWhitespace(nameEl.getValue())) {
			if(nameEl.getValue().length() > 255) {
				nameEl.setErrorKey("form.error.toolong", new String[] { "255" });
				allOk &= false;
			}
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
		assessmentMode = assessmentModeMgr.createAssessmentMode(entry);
		
		assessmentMode.setTargetAudience(AssessmentMode.Target.course);
		assessmentMode.setName(nameEl.getValue());
		assessmentMode.setBegin(beginEl.getDate());
		assessmentMode.setLeadTime(Math.max(leadTimeEl.getIntValue(), 0));
		assessmentMode.setEnd(endEl.getDate());
		assessmentMode.setFollowupTime(Math.max(followupTimeEl.getIntValue(), 0));
		assessmentMode.setManualBeginEnd(startModeEl.isOneSelected() && startModeEl.isSelected(0));
		assessmentMode = assessmentModeMgr.merge(assessmentMode, false, getIdentity());

		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
