/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package ch.unizh.portal.zsuz;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerWithTemplate;

/**
 * Description:<br>
 * TODO: patrickb Class Description for ZsuzStep01Form
 * 
 * <P>
 * Initial Date:  19.06.2008 <br>
 * @author patrickb
 */
public class ZsuzStep01Form extends StepFormBasicController implements StepFormController {

	public ZsuzStep01Form(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext, int layout,
			String customLayoutPageName) {
		super(ureq, control, rootForm, runContext, layout, customLayoutPageName);
		setBasePackage(this.getClass());
		flc.setTranslator(getTranslator());
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// TODO Auto-generated method stub

	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		// inform surrounding Step runner to proceed
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);

	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer, org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	@SuppressWarnings("unused")
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		MailTemplate template = (MailTemplate)getFromRunContext("mailtemplate");
		Identity replyto = (Identity)getFromRunContext("replyto");
		String[] subjectAndBody = MailerWithTemplate.getInstance().previewSubjectAndBody(ureq.getIdentity(), null, null, template, replyto);
		//add disabled textelements.
		String email = getIdentity().getUser().getProperty(UserConstants.EMAIL, getLocale());
		uifactory.addStaticTextElement("form.howtoproceed", null, translate("form.howtoproceed", email), formLayout);
		uifactory.addStaticExampleText("form.subject", subjectAndBody[0],formLayout);
		uifactory.addStaticExampleText("form.email", subjectAndBody[1].replaceAll("\n", "<br>"),formLayout);
	}

}
