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
package org.olat.modules.selectus.ui.application;

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
 * Initial date: 14.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationMemoController extends FormBasicController {
	
	private TextElement memoEl;
	
	private Application application;
	private final boolean canEditMemo;
	
	public ApplicationMemoController(UserRequest ureq, WindowControl wControl, Form rootForm, Application application, boolean canEditMemo) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}
		this.application = application;
		this.canEditMemo = canEditMemo;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("edit.application.memo.infos");
		
		String memo = application.getMemo();
		memoEl = uifactory.addTextAreaElement("memoarea", "edit.application.memo", 4000, 8, 72, true, false, false, memo, formLayout);
		memoEl.setEnabled(canEditMemo);
		if(!StringHelper.containsNonWhitespace(memo)) {
			memoEl.setFocus(true);
		}
	}
	
	protected Application commitChanges(Application app) {
		this.application = app;
		app.setMemo(memoEl.getValue());
		return app;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		commitChanges(application);
		fireEvent(ureq, Event.DONE_EVENT);
	}
}