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

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.HistoryManager;
import org.olat.core.id.context.HistoryModule;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.prefs.Preferences;
import org.olat.login.SupportsAfterLoginInterceptor;

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

	//the cancel button ("Nein")
	private FormLink bttNo;
	
	private String[] askagain_keys = new String[]{"askagain_k"};
	private MultipleSelectionElement askagainCheckbox;
	
	public ResumeController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		askagainCheckbox = uifactory.addCheckboxesHorizontal("askagain",null, formLayout, askagain_keys,  new String[]{translate("askagain.label")}, null);
		
		// Button layout
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", "resume.button", buttonLayout);
		bttNo = uifactory.addFormLink("cancel","resume.button.cancel", "", buttonLayout, Link.BUTTON);
//		FormCancel bttCancel =  uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	@Override
	public boolean isInterceptionRequired(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		boolean disabled = isDisabled(ureq);
		if(disabled) return false;//rest url, do not resume
		
		Preferences prefs =  usess.getGuiPreferences();
		String resumePrefs = (String)prefs.get(WindowManager.class, "resume-prefs");
		if(!StringHelper.containsNonWhitespace(resumePrefs)) {
			HistoryModule historyModule = (HistoryModule)CoreSpringFactory.getBean("historyModule");
			resumePrefs = historyModule.getResumeDefaultSetting();
		}
		if("none".equals(resumePrefs)) {
				return false;
		} else if ("auto".equals(resumePrefs)) {
			HistoryPoint historyEntry = HistoryManager.getInstance().readHistoryPoint(ureq.getIdentity());
			if(historyEntry != null && StringHelper.containsNonWhitespace(historyEntry.getBusinessPath())) {
				BusinessControl bc = BusinessControlFactory.getInstance().createFromContextEntries(historyEntry.getEntries());
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
				try {
					//make the resume secure. If something fail, don't generate a red screen
					NewControllerFactory.getInstance().launch(ureq, bwControl);
				} catch (Exception e) {
					logError("Error while resuming", e);
				}
			}
			return false;
		} else if ("ondemand".equals(resumePrefs)) {
			HistoryPoint historyEntry = HistoryManager.getInstance().readHistoryPoint(ureq.getIdentity());
			return historyEntry != null &&  StringHelper.containsNonWhitespace(historyEntry.getBusinessPath());
		}
		return false;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source.equals(bttNo)){
			this.flc.setDirty(true);
			formResetted(ureq);
			formCancelled(ureq);
		}
	}

	/**
	 * Resume function is disabled if the module say it's disable, or
	 * for REST URL and Jump'in URL
	 * @param ureq
	 * @return
	 */
	private boolean isDisabled(UserRequest ureq) {
		HistoryModule historyModule = (HistoryModule)CoreSpringFactory.getBean("historyModule");
		if(!historyModule.isResumeEnabled()) return true;
		UserSession usess = ureq.getUserSession();
		if(usess.getRoles().isGuestOnly()) return true;
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
		// check if checkbox (dont askagain) is checked
		if(askagainCheckbox.isSelected(0)){
			Preferences	prefs = ureq.getUserSession().getGuiPreferences();
			prefs.put(WindowManager.class, "resume-prefs","auto");
			prefs.save();
		}
				
		fireEvent (ureq, Event.DONE_EVENT);
		
		HistoryPoint historyEntry = HistoryManager.getInstance().readHistoryPoint(ureq.getIdentity());
		if(historyEntry != null && StringHelper.containsNonWhitespace(historyEntry.getBusinessPath())) {
			BusinessControl bc = BusinessControlFactory.getInstance().createFromContextEntries(historyEntry.getEntries());
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
		// check if checkbox (dont askagain) is checked
		if(askagainCheckbox.isSelected(0)){
			Preferences	prefs = ureq.getUserSession().getGuiPreferences();
			prefs.put(WindowManager.class, "resume-prefs","none");
			prefs.save();
		}
		fireEvent (ureq, Event.CANCELLED_EVENT);
	}
}
