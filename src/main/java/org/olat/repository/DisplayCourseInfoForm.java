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

package org.olat.repository;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.instantMessaging.InstantMessagingModule;


/**
 *
 */
public class DisplayCourseInfoForm extends FormBasicController {

	private SelectionElement chatIsOn;
	private SelectionElement efficencyStatement;
	private SelectionElement calendar;

	private CourseConfig cc;
	
	
	public DisplayCourseInfoForm(UserRequest ureq, WindowControl wControl, ICourse course) {
		super(ureq, wControl);
		cc = course.getCourseEnvironment().getCourseConfig();
		initForm (ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("details.summaryprop");
		setFormContextHelp("org.olat.repository","rep-meta-infoCourse.html","help.hover.rep.detail");
		
		chatIsOn = uifactory.addCheckboxesVertical("chatIsOn", "chkbx.chat.onoff", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		chatIsOn.select("xx", cc.isChatEnabled());
		chatIsOn.setVisible(CoreSpringFactory.getImpl(InstantMessagingModule.class).isEnabled() && CourseModule.isCourseChatEnabled());
		
		uifactory.addStaticTextElement(
				"layout", "form.layout.cssfile",
				cc.hasCustomCourseCSS() ? cc.getCssLayoutRef() : translate("form.layout.setsystemcss"),
				formLayout
		);
		
		String name;
		String softkey = cc.getSharedFolderSoftkey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(softkey, false);
		if (re == null) {
			cc.setSharedFolderSoftkey(CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY);
			name = translate("sf.notconfigured");
		} else {
			name = re.getDisplayname();
		}
		
		uifactory.addStaticTextElement(
				"sfolder", "sf.resourcetitle",
				cc.hasCustomSharedFolder() ? name : translate("sf.notconfigured"),
				formLayout
		);
		
		efficencyStatement = uifactory.addCheckboxesVertical("efficencyStatement", "chkbx.efficency.onoff", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		efficencyStatement.select("xx", cc.isEfficencyStatementEnabled());

		calendar = uifactory.addCheckboxesVertical("calendar", "chkbx.calendar.onoff", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		calendar.select("xx", cc.isCalendarEnabled());
		
		String glossName;
		String glossSoftKey = cc.getGlossarySoftKey();
		re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(glossSoftKey, false);
		if (re == null) {
			glossName = translate("glossary.no.glossary");
		} else {
			glossName = re.getDisplayname();
		}
		
		uifactory.addStaticTextElement(
				"glossary", "glossary.isconfigured",
				cc.hasGlossary() ? glossName : translate("glossary.no.glossary"),
				formLayout
		);
		
		flc.setEnabled(false);
	}

	@Override
	protected void doDispose() {
		//
	}
}
