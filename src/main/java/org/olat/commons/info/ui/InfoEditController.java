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

package org.olat.commons.info.ui;

import java.util.Collection;
import java.util.Date;

import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * <P>
 * Initial Date:  24 aug. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoEditController extends FormBasicController {
	
	private InfoMessage messageToEdit;
	private final InfoEditFormController editForm;
	
	@Autowired
	private InfoMessageFrontendManager infoFrontendManager;

	public InfoEditController(UserRequest ureq, WindowControl wControl, InfoMessage messageToEdit) {
		super(ureq, wControl, "edit");
		this.messageToEdit = messageToEdit;
		editForm = new InfoEditFormController(ureq, wControl, mainForm, false, messageToEdit);
		listenTo(editForm);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer editCont = editForm.getInitialFormItem();
		flc.add("edit", editCont);

		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		editCont.add(buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("submit", buttonLayout);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		return editForm.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		messageToEdit = editForm.getInfoMessage();
		messageToEdit.setModificationDate(new Date());
		messageToEdit.setModifier(getIdentity());
		infoFrontendManager.sendInfoMessage(messageToEdit, null, null, getIdentity(), null);
		
		Collection<String> pathToDelete = editForm.getAttachmentPathToDelete();
		infoFrontendManager.deleteAttachments(pathToDelete);
		
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.INFO_MESSAGE_UPDATED, getClass(),
				LoggingResourceable.wrap(messageToEdit.getOLATResourceable(), OlatResourceableType.infoMessage));

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}