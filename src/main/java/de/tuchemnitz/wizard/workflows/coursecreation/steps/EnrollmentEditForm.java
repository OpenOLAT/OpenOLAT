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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.rules.RulesFactory;
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
	private final List<String> elements = new ArrayList<String>();
	private String[] keys;
	private String[] values;
	private final CourseCreationConfiguration courseConfig;

	private String SUBSCRIBER_COUNT = "25";

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
	protected void doDispose() {
		// nothing to dispose
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer formButtons = FormLayoutContainer.createButtonLayout("formButtons", this.getTranslator());

		// create group count textbox
		groupCount = uifactory.addTextElement("groupCount", "en.groupcount", 3, courseConfig.getGroupCount().toString(), formLayout);
		groupCount.setLabel("en.groupcount", null);
		groupCount.setErrorKey("cce.enrollment.error.groupcount", null);
		groupCount.setRegexMatchCheck("\\d*", "cce.enrollment.error.groupcount");
		groupCount.showError(false);
		

		if (courseConfig.getSubscriberCount() != null) subscriberCount = uifactory.addTextElement("subscriberCount",
				"en.subscribercount", 3, courseConfig.getSubscriberCount().toString(), formLayout);
		else subscriberCount = uifactory.addTextElement("subscriberCount", "en.subscribercount", 3, SUBSCRIBER_COUNT, formLayout);

		subscriberCount.setErrorKey("cce.enrollment.error.subscribercount", null);
		subscriberCount.showError(false);

		
		keys = new String[] { "ison" };
		values = new String[] { "" };

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

		if (!accessLimit.isSelected(0)) courseElements.setVisible(false);

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

		// rules to hide / unhide
		Set<FormItem> targets = new HashSet<FormItem>();
		targets.add(courseElements);
		RulesFactory.createHideRule(accessLimit, null, targets, formLayout);
		RulesFactory.createShowRule(accessLimit, "ison", targets, formLayout);

		// Buttons
		formLayout.add(formButtons);
		uifactory.addFormSubmitButton("okButton", formButtons);
		uifactory.addFormCancelButton("cancelButton", formButtons, ureq, getWindowControl());
	}
	
	
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
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
		
		return allOk & super.validateFormLogic(ureq);
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
		courseConfig.setEnableAccessLimit(accessLimit.getSelectedKeys().size() == 1);
		courseConfig.setEnableAclSinglePage(courseElements.getSelectedKeys().contains(translate("cce.informationpage")));
		courseConfig.setEnableAclContactForm(courseElements.getSelectedKeys().contains(translate("cce.contactform")));
		courseConfig.setEnableAclDownloadFolder(courseElements.getSelectedKeys().contains(translate("cce.downloadfolder")));
		courseConfig.setEnableAclForum(courseElements.getSelectedKeys().contains(translate("cce.forum")));
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

}
