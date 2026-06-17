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
package org.olat.modules.selectus.ui.comment;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.ui.PositionController;

/**
 * 
 * Initial date: 3 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationCommitteeCommentController extends FormBasicController {
	
	private TextElement commentEl;
	
	private Application application;
	private final boolean canEditCommitteeComment;
	
	public ApplicationCommitteeCommentController(UserRequest ureq, WindowControl wControl, Form rootForm, Application application, boolean canEditCommitteeComment) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}
		this.application = application;
		this.canEditCommitteeComment = canEditCommitteeComment;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("edit.application.committee.comment.infos");
		
		String comment = application.getCommitteeComment();
		commentEl = uifactory.addTextAreaElement("commentarea", "edit.application.committee.comment", 4000, 8, 72, true, false, false, comment, formLayout);
		commentEl.setEnabled(canEditCommitteeComment);
		if(!StringHelper.containsNonWhitespace(comment)) {
			commentEl.setFocus(true);
		}
	}
	
	public Application commitChanges(Application app) {
		this.application = app;
		app.setCommitteeComment(commentEl.getValue());
		return app;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		commitChanges(application);
		fireEvent(ureq, Event.DONE_EVENT);
	}
}