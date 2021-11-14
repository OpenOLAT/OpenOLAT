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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAppealStatus;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditAppealController extends FormBasicController {

	private TextElement reasonEl;
	private SingleSelection statusEl;
	
	private final LectureBlockRollCall preselectedRollCall;
	private final List<LectureBlockRollCall> rollCalls;
	
	@Autowired
	private LectureService lectureService;
	
	public EditAppealController(UserRequest ureq, WindowControl wControl, LectureBlockRollCall rollCall) {
		super(ureq, wControl);
		this.preselectedRollCall = rollCall;
		this.rollCalls = Collections.singletonList(rollCall);
		initForm(ureq);
	}
	
	public EditAppealController(UserRequest ureq, WindowControl wControl, List<LectureBlockRollCall> rollCalls) {
		super(ureq, wControl);
		this.preselectedRollCall = null;
		this.rollCalls = new ArrayList<>(rollCalls);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("appeal.form.explain");
		
		String reason = preselectedRollCall == null ? null : preselectedRollCall.getAppealStatusReason();
		reasonEl = uifactory.addTextAreaElement("reason", 12, 60, reason, formLayout);
		
		String[] statusKeys = new String[] {
			LectureBlockAppealStatus.pending.name(),
			LectureBlockAppealStatus.approved.name(),
			LectureBlockAppealStatus.rejected.name(),
		};
		String[] statusValues = new String[] {
			translate("appeal.pending"), translate("appeal.approved"), translate("appeal.rejected")
		};
		statusEl = uifactory.addRadiosVertical("appeal.status", "appeal.status", formLayout, statusKeys, statusValues);
		if(preselectedRollCall != null && preselectedRollCall.getAppealStatus() != null) {
			statusEl.select(preselectedRollCall.getAppealStatus().name(), true);
		} else {
			statusEl.select(statusKeys[0], true);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		statusEl.clearError();
		reasonEl.clearError();
		if(!statusEl.isOneSelected()) {
			statusEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(LectureBlockAppealStatus.pending.name().equals(statusEl.getSelectedKey())
				&& !StringHelper.containsNonWhitespace(reasonEl.getValue())) {
			reasonEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;	
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for(LectureBlockRollCall rollCall:rollCalls) {
			rollCall = lectureService.getRollCall(rollCall);
			LectureBlock lectureBlock = rollCall.getLectureBlock();
			Identity assessedIdentity = rollCall.getIdentity();
			RepositoryEntry entry = lectureBlock.getEntry();
			String before = lectureService.toAuditXml(rollCall);
			
			rollCall.setAppealStatusReason(reasonEl.getValue());
			rollCall.setAppealStatus(LectureBlockAppealStatus.valueOf(statusEl.getSelectedKey()));
			rollCall = lectureService.updateRollCall(rollCall);
	
			lectureService.auditLog(LectureBlockAuditLog.Action.updateRollCall, before, lectureService.toAuditXml(rollCall),
					reasonEl.getValue(), lectureBlock, rollCall, entry, assessedIdentity, getIdentity());
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
