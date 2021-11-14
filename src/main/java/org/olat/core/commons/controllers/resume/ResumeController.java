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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.core.commons.controllers.resume;

import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.controllers.resume.ResumeSessionController.Redirect;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.HistoryManager;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * Resume to the last business path. The controller use the preferences
 * of the user to resume automatically, ask to resume with a popup window
 * or ignore the feature completely.
 * <p>
 * Initial Date:  12 jan. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class ResumeController extends FormBasicController {

	private FormSubmit okButton;
	private FormLink noButton, landingButton;
	
	private String[] askagain_keys = new String[]{"askagain_k"};
	private MultipleSelectionElement askagainCheckbox;
	
	private final Redirect redirect;
	
	@Autowired
	private HistoryManager historyManager;
	
	public ResumeController(UserRequest ureq, WindowControl wControl, Redirect redirect) {
		super(ureq, wControl);
		this.redirect = redirect;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		askagainCheckbox = uifactory.addCheckboxesHorizontal("askagain",null, formLayout, askagain_keys,  new String[]{translate("askagain.label")});
		
		// Button layout
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		okButton = uifactory.addFormSubmitButton("submit", "resume.button", buttonLayout);
		okButton.setElementCssClass("o_sel_resume_yes");
		okButton.setFocus(true);
		landingButton = uifactory.addFormLink("landing", "resume.button.landing", null, buttonLayout, Link.BUTTON);
		landingButton.setElementCssClass("o_sel_resume_landing");
		landingButton.setVisible(StringHelper.containsNonWhitespace(redirect.getLandingPage()));
		noButton = uifactory.addFormLink("cancel", "resume.button.cancel", null, buttonLayout, Link.BUTTON);
		noButton.setElementCssClass("o_sel_resume_cancel");
		noButton.setVisible(!StringHelper.containsNonWhitespace(redirect.getLandingPage()));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source.equals(noButton)){
			savePreferences(ureq, "none");			
			fireEvent (ureq, new Event("no"));
		} else if(source.equals(landingButton)){
			savePreferences(ureq, "none");		
			fireEvent (ureq, new Event("landing"));
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		savePreferences(ureq, "auto");		
		fireEvent (ureq, Event.DONE_EVENT);
		
		HistoryPoint historyEntry = historyManager.readHistoryPoint(ureq.getIdentity());
		if(historyEntry != null && StringHelper.containsNonWhitespace(historyEntry.getBusinessPath())) {
			List<ContextEntry> cloneCes = BusinessControlFactory.getInstance().cloneContextEntries(historyEntry.getEntries());
			BusinessControl bc = BusinessControlFactory.getInstance().createFromContextEntries(cloneCes);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			try {
				//make the resume secure. If something fail, don't generate a red screen
				NewControllerFactory.getInstance().launch(ureq, bwControl);
			} catch (Exception e) {
				logError("Error while resumging", e);
			}
		}
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		savePreferences(ureq, "none");
		fireEvent (ureq, Event.CANCELLED_EVENT);
	}
	
	private void savePreferences(UserRequest ureq, String val) {
		// check if checkbox (dont askagain) is checked
		if(askagainCheckbox.isSelected(0)){
			ureq.getUserSession().getGuiPreferences().commit(WindowManager.class, "resume-prefs", val);
		}
	}
}
