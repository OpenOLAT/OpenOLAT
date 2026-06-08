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
package org.olat.modules.selectus.ui.rejection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.model.mail.EmailVariables;

/**
 * 
 * Initial date: 19.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReviewForEmailController extends AbstractEmailController {
	
	public ReviewForEmailController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form,
			EmailVariables emailVar, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, runContext, form, emailVar.getSelectedApps(), emailVar.getMailLog(), emailVar.getRatings(),
				emailVar.getCommittee(), emailVar.getPosition(), emailVar.isShowDecisions(), secCallback);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("wizard.mail.overview.description");
		super.initForm(formLayout, listener, ureq);
		tableEl.setElementCssClass("o_sel_rejection_mail_to_overview");
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}