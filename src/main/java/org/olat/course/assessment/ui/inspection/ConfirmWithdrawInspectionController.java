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
package org.olat.course.assessment.ui.inspection;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.AssessmentInspectionService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmWithdrawInspectionController extends FormBasicController {

	private TextElement commentEl;
	
	private List<AssessmentInspection> inspectionList;
	
	@Autowired
	private AssessmentInspectionService inspectionService;
	
	public ConfirmWithdrawInspectionController(UserRequest ureq, WindowControl wControl, List<AssessmentInspection> inspectionList) {
		super(ureq, wControl, "confirm_withdraw_inspections");
		this.inspectionList = inspectionList;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			String msgI18n = inspectionList.size() == 1 ? "confirm.bulk.withdraw.singular" : "confirm.bulk.withdraw.plural";
			String msg = translate(msgI18n, Integer.toString(inspectionList.size()));
			layoutCont.contextPut("msg", msg);
		}
		
		commentEl = uifactory.addTextAreaElement("comment", "comment", 4000, 4, 60, false, false, false, "", formLayout);
		commentEl.setMandatory(true);
		commentEl.setMaxLength(4000);
		
		uifactory.addFormSubmitButton("bulk.withdraw", "bulk.withdraw", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		commentEl.clearError();
		if(!StringHelper.containsNonWhitespace(commentEl.getValue())) {
			commentEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(commentEl.getValue().length() > 4000) {
			commentEl.setErrorKey("form.error.toolong", "4000");
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String comment = commentEl.getValue();
		for(AssessmentInspection inspection:inspectionList) {
			inspectionService.withdrawInspection(inspection, comment, getIdentity());
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}