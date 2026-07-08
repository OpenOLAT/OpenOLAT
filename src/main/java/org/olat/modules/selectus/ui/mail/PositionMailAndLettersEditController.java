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
package org.olat.modules.selectus.ui.mail;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionMailTemplate;
import org.olat.modules.selectus.ui.mail.PositionMailTemplateRow.Type;

/**
 * 
 * Initial date: 5 avr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionMailAndLettersEditController extends BasicController {
	
	private TabbedPane tabPane;
	private VelocityContainer mainVC;
	
	private Position position;
	
	private PositionLetterEditController letterEditCtrl;
	private PositionMailTemplateEditController editTemplateCtrl;
	private MailTemplateSingleLanguageEditController editPositionTemplateCtrl;
	private MailTemplateMultiLanguageEditController editPositionMLTemplateCtrl;
	
	@Autowired
	private RecruitingModule recruitingModule;
	
	public PositionMailAndLettersEditController(UserRequest ureq, WindowControl wControl, Position position,
			PositionMailTemplateRow row, Type type, String templateName, boolean withLetter) {
		super(ureq, wControl);
		this.position = position;
		
		tabPane = new TabbedPane("emailLetterTabbedPane", getLocale());
		mainVC = createVelocityContainer("mail_letter");
		mainVC.put("tabPane", tabPane);
		
		if(type == Type.custom || type == Type.system) {
			editTemplateCtrl = new PositionMailTemplateEditController(ureq, getWindowControl(), position, row, type, templateName);
			listenTo(editTemplateCtrl);
			tabPane.addTab(translate("tab.email"), editTemplateCtrl);
			mainVC.put("cmp", editTemplateCtrl.getInitialComponent());
		} else if(type == Type.committeeReminder || type == Type.feedback
				|| type == Type.expert || type == Type.referee || type == Type.comparativeExpert
				|| type == Type.confirmationSubmissionExpert || type == Type.confirmationSubmissionReferee || type == Type.confirmationSubmissionComparativeExpert) {
			editPositionTemplateCtrl = new MailTemplateSingleLanguageEditController(ureq, getWindowControl(), position, row);
			listenTo(editPositionTemplateCtrl);
			tabPane.addTab(translate("tab.email"), editPositionTemplateCtrl);
			mainVC.put("cmp", editPositionTemplateCtrl.getInitialComponent());
		} else if(type == Type.confirmationApplication || type == Type.confirmationApplicationWithRefereeManagement || type == Type.confirmationApplicationDuplicate) {
			editPositionMLTemplateCtrl = new MailTemplateMultiLanguageEditController(ureq, getWindowControl(), position, row);
			listenTo(editPositionMLTemplateCtrl);
			tabPane.addTab(translate("tab.email"), editPositionMLTemplateCtrl);
			mainVC.put("cmp", editPositionMLTemplateCtrl.getInitialComponent());
		}
		
		if(recruitingModule.isMailLetterEnabled() && withLetter) {
			letterEditCtrl = new PositionLetterEditController(ureq, wControl, position, row, type, templateName);
			listenTo(letterEditCtrl);
			tabPane.addTab(translate("tab.letter"), letterEditCtrl);
		} else {
			tabPane.setVisible(false);
		}

		putInitialPanel(mainVC);
		
	}
	
	public Position getPosition() {
		return position;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event == Event.CANCELLED_EVENT
				|| (!tabPane.isVisible() && (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT))) {
			if(source == editTemplateCtrl) {
				position = editTemplateCtrl.getPosition();
			} else if(source == editPositionTemplateCtrl) {
				position = editPositionTemplateCtrl.getPosition();
			} else if(source == editPositionMLTemplateCtrl) {
				position = editPositionMLTemplateCtrl.getPosition();
			}
			fireEvent(ureq, event);
		} else if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
			if(source == editTemplateCtrl) {
				PositionMailTemplate mailTemplate = editTemplateCtrl.getUpdatedTemplate();
				if(letterEditCtrl != null) {
					letterEditCtrl.updateMailTemplate(editTemplateCtrl.getTemplateName(), mailTemplate);
				}
			}
		} else if(event instanceof OpenVariablesEvent) {
			doOpenVariables(ureq);
		}
	}

	private void doOpenVariables(UserRequest ureq) {
		if(editTemplateCtrl != null) {
			editTemplateCtrl.doOpenVariables(ureq);
		} else if(editPositionTemplateCtrl != null) {
			editPositionTemplateCtrl.doOpenVariables(ureq);
		} else if(editPositionMLTemplateCtrl != null) {
			editPositionMLTemplateCtrl.doOpenVariables(ureq);
		}
	}
}
