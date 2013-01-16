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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.collaboration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.ui.events.KalendarModifiedEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.group.BusinessGroup;
import org.olat.instantMessaging.InstantMessagingModule;

/**
 * Description: <BR>
 * Administrative controller which allows configuration of collaboration tools.
 * <P>
 * Initial Date: Aug 23, 2004 
 * @author patrick
 */

public class CollaborationToolsSettingsController extends BasicController {

	private VelocityContainer vc_collabtools;
	private ChoiceOfToolsForm cots;
	private NewsFormController newsController;
	private CalendarToolSettingsController calendarForm;
	private FolderToolSettingsController folderForm;

	boolean lastCalendarEnabledState;
	private Controller quotaCtr;
	private BusinessGroup businessGroup;

	/**
	 * @param ureq
	 * @param tools
	 */
	public CollaborationToolsSettingsController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl);
		this.businessGroup = businessGroup;
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		
		vc_collabtools = createVelocityContainer ("collaborationtools");

		cots = new ChoiceOfToolsForm (ureq, wControl, collabTools);
		listenTo(cots);
		vc_collabtools.put("choiceOfTools", cots.getInitialComponent());
		
		if (collabTools.isToolEnabled(CollaborationTools.TOOL_NEWS)) {
			addNewsTool(ureq);
		} else {
			vc_collabtools.contextPut("newsToolEnabled", Boolean.FALSE);
		}
		
		if (ureq.getUserSession().getRoles().isOLATAdmin()) {
			vc_collabtools.contextPut("isOlatAdmin", Boolean.TRUE);
			quotaCtr = QuotaManager.getInstance().getQuotaEditorInstance(ureq, getWindowControl(), collabTools.getFolderRelPath(), false);
			listenTo(quotaCtr);
		} else {
			vc_collabtools.contextPut("isOlatAdmin", Boolean.FALSE);
		}

		// update calendar form: only show when enabled
		if (collabTools.isToolEnabled(CollaborationTools.TOOL_CALENDAR)) {
			lastCalendarEnabledState = true;
			vc_collabtools.contextPut("calendarToolEnabled", Boolean.TRUE);
			int iCalendarAccess = CollaborationTools.CALENDAR_ACCESS_OWNERS;
			Long lCalendarAccess = collabTools.lookupCalendarAccess();
			if (lCalendarAccess != null) iCalendarAccess = lCalendarAccess.intValue();
			calendarForm = new CalendarToolSettingsController(ureq, getWindowControl(), iCalendarAccess);
			listenTo(calendarForm);
			
			vc_collabtools.put("calendarform", calendarForm.getInitialComponent());
		} else {
			lastCalendarEnabledState = false;
			vc_collabtools.contextPut("calendarToolEnabled", Boolean.FALSE);
		}
		
		// update quota form: only show when enabled
		if (collabTools.isToolEnabled(CollaborationTools.TOOL_FOLDER)) {
			vc_collabtools.contextPut("folderToolEnabled", Boolean.TRUE);
			//fxdiff VCRP-8: collaboration tools folder access control
			if(ureq.getUserSession().getRoles().isOLATAdmin()) {
				vc_collabtools.put("quota", quotaCtr.getInitialComponent());
			}
			vc_collabtools.contextPut("folderToolEnabled", Boolean.TRUE);
			if(folderForm != null) {
				removeAsListenerAndDispose(folderForm);
			}
			Long lFolderAccess = collabTools.lookupFolderAccess();
			int access = lFolderAccess == null ? CollaborationTools.FOLDER_ACCESS_ALL : lFolderAccess.intValue();
			folderForm = new FolderToolSettingsController(ureq, getWindowControl(), access);
			listenTo(folderForm);
			vc_collabtools.put("folderform", folderForm.getInitialComponent());
		} else {
			vc_collabtools.contextPut("folderToolEnabled", Boolean.FALSE);
		}
		
		putInitialPanel(vc_collabtools);
	}

	private void addNewsTool(UserRequest ureq) {
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		String newsValue = collabTools.lookupNews();
		
		if (newsController != null) {
			removeAsListenerAndDispose(newsController);
		}
		newsController = new NewsFormController(ureq, getWindowControl(), (newsValue == null ? "" : newsValue));
		listenTo(newsController);
		
		vc_collabtools.contextPut("newsToolEnabled", Boolean.TRUE);
		vc_collabtools.put("newsform", newsController.getInitialComponent());
	}

	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		
		if (source == cots && event.getCommand().equals("ONCHANGE")) {
			
			Set<String> set = cots.getSelected();
			for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
				// usually one should check which one changed but here
				// it is okay to set all of them because ctsm has a cache
				// and writes only when really necessary.
				collabTools.setToolEnabled(CollaborationTools.TOOLS[i], set.contains(""+i));	
			}
			//reload tools after a change
			collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
			
			fireEvent(ureq, Event.CHANGED_EVENT);			
			
			// update news form: only show when enabled
			if (collabTools.isToolEnabled(CollaborationTools.TOOL_NEWS)) {
				addNewsTool(ureq);
			} else {
				vc_collabtools.contextPut("newsToolEnabled", Boolean.FALSE);
			}
			
			// update calendar form: only show when enabled
			boolean newCalendarEnabledState = collabTools.isToolEnabled(CollaborationTools.TOOL_CALENDAR);
			if (newCalendarEnabledState != lastCalendarEnabledState) {
				if (newCalendarEnabledState) {
					vc_collabtools.contextPut("calendarToolEnabled", Boolean.TRUE);
					int iCalendarAccess = CollaborationTools.CALENDAR_ACCESS_OWNERS;
					Long lCalendarAccess = collabTools.lookupCalendarAccess();
					if (lCalendarAccess != null) iCalendarAccess = lCalendarAccess.intValue();
					if (calendarForm != null) {
						this.removeAsListenerAndDispose(calendarForm);
					}
					calendarForm = new CalendarToolSettingsController(ureq, getWindowControl(), iCalendarAccess);
					listenTo(calendarForm);
					vc_collabtools.put("calendarform", calendarForm.getInitialComponent());

				} else {
					
					vc_collabtools.contextPut("calendarToolEnabled", Boolean.FALSE);

					// notify calendar components to refresh their calendars
					CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(
							new KalendarModifiedEvent(), OresHelper.lookupType(CalendarManager.class)
					);
				}
				lastCalendarEnabledState = newCalendarEnabledState;
			}

			// update quota form: only show when enabled
			if (collabTools.isToolEnabled(CollaborationTools.TOOL_FOLDER)) {
				vc_collabtools.contextPut("folderToolEnabled", Boolean.TRUE);
				//fxdiff VCRP-8: collaboration tools folder access control
				if(folderForm != null) {
					removeAsListenerAndDispose(folderForm);
				}
				Long lFolderAccess = collabTools.lookupFolderAccess();
				int access = lFolderAccess == null ? CollaborationTools.FOLDER_ACCESS_ALL : lFolderAccess.intValue();
				folderForm = new FolderToolSettingsController(ureq, getWindowControl(), access);
				listenTo(folderForm);
				vc_collabtools.put("folderform", folderForm.getInitialComponent());
				if (ureq.getUserSession().getRoles().isOLATAdmin()) {
					vc_collabtools.put("quota", quotaCtr.getInitialComponent());
				}
			} else {
				vc_collabtools.contextPut("folderToolEnabled", Boolean.FALSE);
			}
			
		} else if (source == this.newsController) {
			if (event.equals(Event.DONE_EVENT)) {
				String news = this.newsController.getNewsValue();
				collabTools.saveNews(news);
			}
			
		} else if (source == this.calendarForm) {	
			collabTools.saveCalendarAccess(new Long(calendarForm.getCalendarAccess()));
			// notify calendar components to refresh their calendars
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(
					new KalendarModifiedEvent(), OresHelper.lookupType(CalendarManager.class)
			);
		//fxdiff VCRP-8: collaboration tools folder access control
		} else if (source == folderForm) {
			collabTools.saveFolderAccess(new Long(folderForm.getFolderAccess()));
		}
	}


	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
}

class ChoiceOfToolsForm extends FormBasicController {

	CollaborationTools cts;
	MultipleSelectionElement ms;
	
	List <String>theKeys   = new ArrayList<String>();
	List <String>theValues = new ArrayList<String>();
	
	public ChoiceOfToolsForm(UserRequest ureq, WindowControl wControl, CollaborationTools cts) {
		super(ureq, wControl);
		this.cts = cts;
		
		for (int i=0; i<CollaborationTools.TOOLS.length; i++) {
			String k = CollaborationTools.TOOLS[i];
			if (k.equals(CollaborationTools.TOOL_CHAT) && !CoreSpringFactory.getImpl(InstantMessagingModule.class).isEnabled()) {
				continue;
			}
			theKeys.add(""+i);
			theValues.add(translate("collabtools.named."+CollaborationTools.TOOLS[i]));
		}
		
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		ms = uifactory.addCheckboxesVertical(
				"selection", formLayout, 
				theKeys.toArray(new String[theKeys.size()]),
				theValues.toArray(new String[theValues.size()]),
				null, 1
		);
		for (int i=0; i<CollaborationTools.TOOLS.length; i++) {
			ms.select(""+i, cts.isToolEnabled(CollaborationTools.TOOLS[i]));
		}
		ms.addActionListener(listener, FormEvent.ONCLICK);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == ms && event.getCommand().equals("ONCLICK")) {
			fireEvent(ureq, new Event("ONCHANGE"));
		}
	}
	
	protected Set<String> getSelected() {
		return ms.getSelectedKeys();
	}
	
	@Override
	protected void doDispose() {
		//
	}
}