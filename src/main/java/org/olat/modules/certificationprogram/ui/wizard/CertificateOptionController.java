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
package org.olat.modules.certificationprogram.ui.wizard;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.modules.certificationprogram.CertificationProgram;

/**
 * 
 * Initial date: 11 déc. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificateOptionController extends StepFormBasicController {
	
	private DateChooser issuedDateEl;
	private StaticTextElement validUntilEl;
	
	private final AddProgramMembersContext membersContext;
	private final CertificationProgram certificationProgram;
	
	public CertificateOptionController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, AddProgramMembersContext membersContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		this.membersContext = membersContext;
		certificationProgram = membersContext.getProgram();
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("certificate.option.infos");
		formLayout.setElementCssClass("o_certification_issued_date_step");
		
		Date now = ureq.getRequestTimestamp();
		issuedDateEl = uifactory.addDateChooser("certificate.issue.date", "certificate.issue.date", now, formLayout);
		if(certificationProgram.isValidityEnabled()) {
			issuedDateEl.addActionListener(FormEvent.ONCHANGE);
		}
		
		validUntilEl = uifactory.addStaticTextElement("certificate.valid.until", "", formLayout);
		validUntilEl.setVisible(certificationProgram.isValidityEnabled());
	}
	
	private void updateUI() {
		if(certificationProgram.isValidityEnabled()) {
			Date now = issuedDateEl.getDate();
			Date date = certificationProgram.getValidityTimelapseUnit()
					.toDate(now, certificationProgram.getValidityTimelapse());
			String until = Formatter.getInstance(getLocale()).formatDate(date);
			validUntilEl.setValue(until);
		}
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(issuedDateEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		issuedDateEl.clearError();
		if(issuedDateEl.getDate() == null) {
			issuedDateEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else  {
			Date date = issuedDateEl.getDate();
			if(date.compareTo(DateUtils.getEndOfDay(ureq.getRequestTimestamp())) >= 0) {
				issuedDateEl.setErrorKey("error.date.in.future");
				allOk &= false;
			} else if(certificationProgram.isValidityEnabled()) {
				Date limit = certificationProgram.getValidityTimelapseUnit()
						.toDate(ureq.getRequestTimestamp(), -certificationProgram.getValidityTimelapse());
				if(date.compareTo(DateUtils.getStartOfDay(limit)) < 0) {
					issuedDateEl.setErrorKey("error.date.in.past");
					allOk &= false;	
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formNext(UserRequest ureq) {
		membersContext.setIssuedDate(issuedDateEl.getDate());
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
