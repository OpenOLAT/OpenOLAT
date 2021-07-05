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
package org.olat.modules.lecture.ui;

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.Reason;
import org.olat.modules.lecture.RollCallSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CancelRollCallConfirmationController extends FormBasicController {

	private SingleSelection effectiveEndReasonEl;
	
	private LectureBlock lectureBlock;
	private RollCallSecurityCallback secCallback;
	
	@Autowired
	private LectureService lectureService;
	
	public CancelRollCallConfirmationController(UserRequest ureq, WindowControl wControl,
			LectureBlock lectureBlock, RollCallSecurityCallback secCallback) {
		super(ureq, wControl);
		this.lectureBlock = lectureBlock;
		this.secCallback = secCallback;
		
		initForm(ureq);
	}
	
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		List<Reason> allReasons = lectureService.getAllReasons();
		Collections.sort(allReasons, new ReasonComparator());
		SelectionValues reasonKeyValues = new SelectionValues();
		for(Reason reason:allReasons) {
			if(reason.isEnabled() || reason.equals(lectureBlock.getReasonEffectiveEnd())) {
				reasonKeyValues.add(SelectionValues.entry(reason.getKey().toString(), reason.getTitle()));
			}
		}
		effectiveEndReasonEl = uifactory.addDropdownSingleselect("effective.reason", "lecture.block.effective.reason", formLayout,
				reasonKeyValues.keys(), reasonKeyValues.values(), null);
		effectiveEndReasonEl.setEnabled(secCallback.canEdit());
		boolean found = false;
		if(lectureBlock.getReasonEffectiveEnd() != null) {
			String selectedReasonKey = lectureBlock.getReasonEffectiveEnd().getKey().toString();
			for(String reasonKey:reasonKeyValues.keys()) {
				if(reasonKey.equals(selectedReasonKey)) {
					effectiveEndReasonEl.select(reasonKey, true);
					found = true;
					break;
				}
			}
		}
		if(!found) {
			if(reasonKeyValues.isEmpty()) {
				effectiveEndReasonEl.setEnabled(false);
				effectiveEndReasonEl.setVisible(false);
			} else {
				effectiveEndReasonEl.select(reasonKeyValues.keys()[0], true);
			}
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("cancel.lecture.blocks", buttonsCont);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		effectiveEndReasonEl.clearError();
		if(effectiveEndReasonEl.isEnabled() && !effectiveEndReasonEl.isOneSelected()) {
			effectiveEndReasonEl.setErrorKey("error.reason.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String before = lectureService.toAuditXml(lectureBlock);
		if(effectiveEndReasonEl.isEnabled()) {
			Long reasonKey = Long.valueOf(effectiveEndReasonEl.getSelectedKey());
			Reason selectedReason = lectureService.getReason(reasonKey);
			lectureBlock.setReasonEffectiveEnd(selectedReason);
		}
		lectureBlock = lectureService.cancel(lectureBlock);
		String after = lectureService.toAuditXml(lectureBlock);
		lectureService.auditLog(LectureBlockAuditLog.Action.cancelLectureBlock, before, after, null, lectureBlock, null, lectureBlock.getEntry(), null, getIdentity());
		fireEvent(ureq, Event.DONE_EVENT);
		
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LECTURE_BLOCK_ROLL_CALL_CANCELLED, getClass(),
				CoreLoggingResourceable.wrap(lectureBlock, OlatResourceableType.lectureBlock, lectureBlock.getTitle()));
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
