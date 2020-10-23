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
package org.olat.course.nodes.iq;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.nodes.CourseNode;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.modules.dcompensation.DisadvantageCompensationAuditLog.Action;
import org.olat.modules.dcompensation.DisadvantageCompensationService;
import org.olat.modules.dcompensation.DisadvantageCompensationStatusEnum;
import org.olat.modules.dcompensation.ui.UserDisadvantageCompensationEditController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmCompensationExtraTimeController  extends FormBasicController {
	
	private DateChooser approvalEl;
	private TextElement extraTimeEl;
	private TextElement approvedByEl;
	
	private final String subIdent;
	private final String subIdentName;
	private final RepositoryEntry entry;
	private final Identity assessedIdentity;
	private AssessmentTestSession lastSession;
	private DisadvantageCompensation compensation;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private DisadvantageCompensationService disadvantageCompensationService;
	
	public ConfirmCompensationExtraTimeController(UserRequest ureq, WindowControl wControl,
			Identity assessedIdentity, RepositoryEntry entry, CourseNode courseNode, AssessmentTestSession lastSession) {
		super(ureq, wControl, Util.createPackageTranslator(UserDisadvantageCompensationEditController.class, ureq.getLocale()));
		this.entry = entry;
		subIdent = courseNode.getIdent();
		subIdentName = courseNode.getShortTitle();
		this.lastSession = lastSession;
		this.assessedIdentity = assessedIdentity;
		
		// Prefer an active compensation
		List<DisadvantageCompensation> compensations = disadvantageCompensationService.getDisadvantageCompensations(assessedIdentity, entry, subIdent);
		if(!compensations.isEmpty()) {
			for(DisadvantageCompensation c:compensations) {
				if(c.getStatusEnum() == DisadvantageCompensationStatusEnum.active) {
					compensation = c;
				}
			}
			if(compensation == null) {
				compensation = compensations.get(0);
			}
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String approvedBy = compensation == null ? null : compensation.getApprovedBy();
		approvedByEl = uifactory.addTextElement("edit.approved.by", 255, approvedBy, formLayout);
		approvedByEl.setMandatory(true);
		
		Date approval = compensation == null ? null : compensation.getApproval();
		approvalEl = uifactory.addDateChooser("edit.approval.date", approval, formLayout);
		approvalEl.setMandatory(true);
		
		int extraTime = 0;
		if(lastSession != null && lastSession.getCompensationExtraTime() != null ) {
			extraTime = lastSession.getCompensationExtraTime().intValue();
		} else if(compensation != null && compensation.getExtraTime() != null
				&& compensation.getStatusEnum() == DisadvantageCompensationStatusEnum.active) {
			extraTime = compensation.getExtraTime();
		}
		String maxExtraTime = extraTime == 0 ? "" : Integer.toString(extraTime / 60);
		extraTimeEl = uifactory.addTextElement("edit.extra.time", "edit.extra.time", 5, maxExtraTime, formLayout);
		extraTimeEl.setDisplaySize(5);
		extraTimeEl.setDomReplacementWrapperRequired(false);
		extraTimeEl.setMandatory(true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("extra.time", buttonsCont);
	}
	
	/**
	 * 
	 * @return The extra time in seconds.
	 */
	public int getExtraTime() {
		String val = extraTimeEl.getValue();
		int inMinute = Integer.parseInt(val);
		return inMinute * 60;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		approvedByEl.clearError();
		if(!StringHelper.containsNonWhitespace(approvedByEl.getValue())) {
			approvedByEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		approvalEl.clearError();
		if(approvalEl.getDate() == null) {
			approvalEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		extraTimeEl.clearError();
		if(StringHelper.containsNonWhitespace(extraTimeEl.getValue())) {
			try {
				int time = Integer.parseInt(extraTimeEl.getValue());
				if(time <= 0) {
					allOk &= false;
					extraTimeEl.setErrorKey("form.error.nointeger", null);
				}
			} catch(Exception e) {
				extraTimeEl.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		} else {
			extraTimeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		int extraTime = getExtraTime();
		Date approval = approvalEl.getDate();
		String approvedBy = approvedByEl.getValue();
		
		if(lastSession != null) {
			qtiService.compensationExtraTimeAssessmentTestSession(lastSession, extraTime, getIdentity());
		}
		
		if(compensation == null) {
			compensation = disadvantageCompensationService.createDisadvantageCompensation(assessedIdentity,
					extraTime, approvedBy, approval, getIdentity(), entry, subIdent, subIdentName);
			String afterXml = disadvantageCompensationService.toXml(compensation);
			disadvantageCompensationService.auditLog(Action.create, null, afterXml, compensation, getIdentity());
		} else {
			String beforeXml = disadvantageCompensationService.toXml(compensation);
			
			compensation.setApproval(approval);
			compensation.setExtraTime(extraTime);
			compensation.setApprovedBy(approvedBy);
			compensation.setStatusEnum(DisadvantageCompensationStatusEnum.active);
			compensation = disadvantageCompensationService.updateDisadvantageCompensation(compensation);
			
			String afterXml = disadvantageCompensationService.toXml(compensation);
			disadvantageCompensationService.auditLog(Action.update, beforeXml, afterXml, compensation, getIdentity());
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
