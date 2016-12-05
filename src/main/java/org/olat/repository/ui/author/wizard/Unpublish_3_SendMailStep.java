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
package org.olat.repository.ui.author.wizard;

import org.apache.velocity.VelocityContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailTemplateForm;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;

/**
 * 
 * Form to send the notification e-mails
 * 
 * Initial date: 29.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Unpublish_3_SendMailStep extends BasicStep {
	
	private final RepositoryEntry entry;
	
	public Unpublish_3_SendMailStep(UserRequest ureq, RepositoryEntry entry) {
		super(ureq);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		setNextStep(Step.NOSTEP);
		setI18nTitleAndDescr("close.ressource.step3", "close.ressource.step3");
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, false, true);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new SendMailStepController(ureq, wControl, form, runContext, entry);
	}
	
	public class SendMailStepController extends StepFormBasicController {
		private MailTemplate mailTemplate;
		private MailTemplateForm templateForm;
		
		public SendMailStepController(UserRequest ureq, WindowControl wControl,
				Form rootForm, StepsRunContext runContext, RepositoryEntry entry) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
			setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
			
			String courseTitle = "'" + entry.getDisplayname() + "'";
			mailTemplate = createMailTemplate(
					translate("wizard.step3.mail.subject", new String[] { courseTitle }),
					translate("wizard.step3.mail.body",
					new String[] {
							courseTitle,
							getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, null) + " "
									+ getIdentity().getUser().getProperty(UserConstants.LASTNAME, null)
					}));

			templateForm = new MailTemplateForm(ureq, wControl, mailTemplate, false, rootForm);
			initForm(ureq);
		}
		
		private MailTemplate createMailTemplate(String subject, String body) {		
			return new MailTemplate(subject, body, null) {
				@Override
				public void putVariablesInMailContext(VelocityContext context, Identity identity) {
					// nothing to do
				}
			};
		}
		
		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			formLayout.add(templateForm.getInitialFormItem());
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void formOK(UserRequest ureq) {
			if(templateForm.sendMailSwitchEnabled()) {
				templateForm.updateTemplateFromForm(mailTemplate);
				addToRunContext("mailTemplate", mailTemplate);
			}
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
	}
}