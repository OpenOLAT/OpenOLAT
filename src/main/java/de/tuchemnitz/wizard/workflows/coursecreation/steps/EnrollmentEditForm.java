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
* <p>
* Initial code contributed and copyrighted by<br>
* Technische Universitaet Chemnitz Lehrstuhl Technische Informatik<br>
* <br>
* Author Marcel Karras (toka@freebits.de)<br>
* Author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)<br>
* Author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
*/

package de.tuchemnitz.wizard.workflows.coursecreation.steps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

import de.tuchemnitz.wizard.workflows.coursecreation.CourseCreationHelper;
import de.tuchemnitz.wizard.workflows.coursecreation.model.CourseCreationConfiguration;

/**
 * 
 * Description:<br>
 * Simple configuration dialog for enrollment.
 * 
 * <P>
 * Initial Date: 21.01.2010 <br>
 * 
 * @author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)
 * @author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
 */
public class EnrollmentEditForm extends FormBasicController {

	// limit access to selected course elements
	private MultipleSelectionElement accessLimit;
	// access config chooser for course elements
	private MultipleSelectionElement courseElements;
	// number of groups
	private TextElement groupCount;
	// number of members per group
	private TextElement subscriberCount;
	// list of course elements used for access limitations
	private final List<String> elements = new ArrayList<>();
	private String[] keys = new String[] { "ison" };
	private String[] values = new String[] { "" };
	private final CourseCreationConfiguration courseConfig;

	/**
	 * Standard constructor
	 * @param ureq
	 * @param wControl
	 * @param courseCOnfig
	 */
	protected EnrollmentEditForm(UserRequest ureq, WindowControl wControl, CourseCreationConfiguration courseCOnfig) {
		super(ureq, wControl);
		this.courseConfig = courseCOnfig;
		super.setTranslator(Util.createPackageTranslator(CourseCreationHelper.class, ureq.getLocale()));

		initConfig();
		initForm(ureq);
		listenTo(this);
	}

	/**
	 * Constructs the configuration of the form.
	 */
	private void initConfig() {
		elements.clear();
		// prepare the list of course elements for the form
		if (courseConfig.isCreateSinglePage()) elements.add(translate("cce.informationpage"));
		if (courseConfig.isCreateDownloadFolder()) elements.add(translate("cce.downloadfolder"));
		if (courseConfig.isCreateForum()) elements.add(translate("cce.forum"));
		if (courseConfig.isCreateContactForm()) elements.add(translate("cce.contactform"));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("coursecreation.enrollment.shortDescription");
		
		FormLayoutContainer formButtons = FormLayoutContainer.createButtonLayout("formButtons", this.getTranslator());

		// create group count textbox
		groupCount = uifactory.addTextElement("groupCount", "en.groupcount", 3, courseConfig.getGroupCount().toString(), formLayout);
		groupCount.setLabel("en.groupcount", null);
		groupCount.setErrorKey("cce.enrollment.error.groupcount", null);
		groupCount.setRegexMatchCheck("\\d*", "cce.enrollment.error.groupcount");
		groupCount.showError(false);
		
		String subCount = courseConfig.getSubscriberCount() == null ? "25" : courseConfig.getSubscriberCount().toString();
		subscriberCount = uifactory.addTextElement("subscriberCount", "en.subscribercount",
					3, subCount, formLayout);
		subscriberCount.setErrorKey("cce.enrollment.error.subscribercount", null);
		subscriberCount.showError(false);

		accessLimit = uifactory.addCheckboxesVertical("accessLimit", formLayout, keys, values, 1);
		accessLimit.setLabel("en.accesscheckbox", null);
		accessLimit.select("ison", courseConfig.getEnableAccessLimit());
		accessLimit.addActionListener(FormEvent.ONCLICK);

		keys = new String[elements.size()];
		values = new String[elements.size()];
		for (int i = 0; i < elements.size(); i++) {
			keys[i] = elements.get(i);
		}
		for (int i = 0; i < elements.size(); i++) {
			values[i] = elements.get(i);
		}

		courseElements = uifactory.addCheckboxesVertical("courseElements", formLayout, keys, values, 1);
		courseElements.setLabel("en.selectelements", null);
		courseElements.setVisible(accessLimit.isSelected(0));

		// copy elements list into an array
		for (int i = 0; i < elements.size(); i++) {
			keys[i] = elements.get(i);
			if (keys[i].equals(translate("cce.informationpage"))) {
				courseElements.select(keys[i], courseConfig.isEnableAclSinglePage());
			} else if (keys[i].equals(translate("cce.downloadfolder"))) {
				courseElements.select(keys[i], courseConfig.isEnableAclDownloadFolder());
			} else if (keys[i].equals(translate("cce.forum"))) {
				courseElements.select(keys[i], courseConfig.isEnableAclForum());
			} else if (keys[i].equals(translate("cce.contactform"))) {
				courseElements.select(keys[i], courseConfig.isEnableAclContactForm());
			}
		}

		// Buttons
		formLayout.add(formButtons);
		uifactory.addFormCancelButton("cancelButton", formButtons, ureq, getWindowControl());
		uifactory.addFormSubmitButton("okButton", formButtons);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		String groupCountStr = groupCount.getValue();
		if(StringHelper.containsNonWhitespace(groupCountStr)) {
			try {
				Integer.parseInt(groupCountStr);
			} catch (NumberFormatException e) {
				groupCount.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		} else {
			groupCount.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Integer groupCountInt = new Integer(groupCount.getValue());
		// minimum of one group
		if (groupCountInt <= 0) {
			groupCountInt = 1;
		}
		courseConfig.setGroupCount(groupCountInt);
		String s = subscriberCount.getValue().trim();
		if (s.length() > 0) {
			courseConfig.setSubscriberCount(new Integer(s));
		}
		courseConfig.setEnableAccessLimit(accessLimit.isAtLeastSelected(1));
		Collection<String> selectedCourseElements = courseElements.getSelectedKeys();
		courseConfig.setEnableAclSinglePage(selectedCourseElements.contains(translate("cce.informationpage")));
		courseConfig.setEnableAclContactForm(selectedCourseElements.contains(translate("cce.contactform")));
		courseConfig.setEnableAclDownloadFolder(selectedCourseElements.contains(translate("cce.downloadfolder")));
		courseConfig.setEnableAclForum(selectedCourseElements.contains(translate("cce.forum")));
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(accessLimit == source) {
			courseElements.setVisible(accessLimit.isAtLeastSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}