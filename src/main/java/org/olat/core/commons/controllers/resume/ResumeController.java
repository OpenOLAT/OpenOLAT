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
import org.olat.admin.landingpages.LandingPagesModule;
import org.olat.admin.landingpages.model.Rules;
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
import org.olat.core.id.context.HistoryModule;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.prefs.Preferences;
import org.olat.login.SupportsAfterLoginInterceptor;
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
public class ResumeController extends FormBasicController implements SupportsAfterLoginInterceptor {

	private FormSubmit okButton;
	private FormLink noButton, landingButton;
	
	private String[] askagain_keys = new String[]{"askagain_k"};
	private MultipleSelectionElement askagainCheckbox;
	
	@Autowired
	private LandingPagesModule lpModule;
	@Autowired
	private HistoryModule historyModule;
	@Autowired
	private HistoryManager historyManager;
	
	public ResumeController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
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
		landingButton = uifactory.addFormLink("landing", "resume.button.landing", null, buttonLayout, Link.BUTTON);
		landingButton.setElementCssClass("o_sel_resume_landing");
		noButton = uifactory.addFormLink("cancel", "resume.button.cancel", null, buttonLayout, Link.BUTTON);
		noButton.setElementCssClass("o_sel_resume_cancel");
	}
	
	@Override
	public boolean isInterceptionRequired(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();

		boolean interception = false;
		if(isREST(ureq)) {
			//do nothing
		} else if(!historyModule.isResumeEnabled()) {
			String bc = getLandingBC(ureq);
			redirect(ureq, bc);
		} else if(usess.getRoles().isGuestOnly()) {
			String bc = getLandingBC(ureq);
			redirect(ureq, bc);
		} else {
			Preferences prefs =  usess.getGuiPreferences();
			String resumePrefs = (String)prefs.get(WindowManager.class, "resume-prefs");
			if(!StringHelper.containsNonWhitespace(resumePrefs)) {
				resumePrefs = historyModule.getResumeDefaultSetting();
			}

			if("none".equals(resumePrefs)) {
				String bc = getLandingBC(ureq);
				redirect(ureq, bc);
			} else if ("auto".equals(resumePrefs)) {
				HistoryPoint historyEntry = HistoryManager.getInstance().readHistoryPoint(ureq.getIdentity());
				if(historyEntry != null && StringHelper.containsNonWhitespace(historyEntry.getBusinessPath())) {
					List<ContextEntry> cloneCes = BusinessControlFactory.getInstance().cloneContextEntries(historyEntry.getEntries());
					String bc = BusinessControlFactory.getInstance().getAsRestPart(cloneCes, true);
					redirect(ureq, bc);
				} else {
					String bc = getLandingBC(ureq);
					redirect(ureq, bc);
				}
			} else if ("ondemand".equals(resumePrefs)) {
				HistoryPoint historyEntry = historyManager.readHistoryPoint(ureq.getIdentity());
				if(historyEntry != null && StringHelper.containsNonWhitespace(historyEntry.getBusinessPath())) {
					interception = true;
					
					String bc = getLandingBC(ureq);
					if(StringHelper.containsNonWhitespace(bc)) {
						noButton.setVisible(false);
					} else {
						landingButton.setVisible(false);
					}
				} else {
					String bc = getLandingBC(ureq);
					redirect(ureq, bc);
				}
			}
		}
		return interception;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source.equals(noButton)){
			flc.setDirty(true);
			formResetted(ureq);
			formCancelled(ureq);
		} else if(source.equals(landingButton)){
			savePreferences(ureq, "none");		
			fireEvent (ureq, Event.DONE_EVENT);
			String bc = getLandingBC(ureq);
			launch(ureq, bc);
		}
	}
	
	private boolean isREST(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		if(usess.getEntry("AuthDispatcher:businessPath") != null) return true;
		if(usess.getEntry("AuthDispatcher:entryUrl") != null) return true;
		return false;
	}

	@Override
	protected void doDispose() {
		//
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
	
	/**
	 * Search first in the user preferences, after in rules
	 * @param ureq
	 * @return
	 */
	private String getLandingBC(UserRequest ureq) {
		Preferences prefs =  ureq.getUserSession().getGuiPreferences();
		String landingPage = (String)prefs.get(WindowManager.class, "landing-page");
		if(StringHelper.containsNonWhitespace(landingPage)) {
			String path = Rules.cleanUpLandingPath(landingPage);
			if(StringHelper.containsNonWhitespace(path)) {
				return BusinessControlFactory.getInstance().formatFromURI(path);
			}
		}
		return lpModule.getRules().match(ureq.getUserSession());
	}
	
	private void launch(UserRequest ureq, String businessPath) {
		if(StringHelper.containsNonWhitespace(businessPath)) {
			try {
				//make the resume secure. If something fail, don't generate a red screen
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			} catch (Exception e) {
				logError("Error while resuming", e);
			}
		}
	}
	
	private void redirect(UserRequest ureq, String businessPath) {
		if(StringHelper.containsNonWhitespace(businessPath)) {
			try {
				ureq.getUserSession().putEntry("redirect-bc", businessPath);
			} catch (Exception e) {
				logError("Error while resuming", e);
			}
		}
	}
	
	private void savePreferences(UserRequest ureq, String val) {
		// check if checkbox (dont askagain) is checked
		if(askagainCheckbox.isSelected(0)){
			Preferences	prefs = ureq.getUserSession().getGuiPreferences();
			prefs.put(WindowManager.class, "resume-prefs", val);
			prefs.save();
		}
	}
}
