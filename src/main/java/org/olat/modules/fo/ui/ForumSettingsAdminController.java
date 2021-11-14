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
package org.olat.modules.fo.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ForumSettingsAdminController extends FormBasicController {
	
	private static final String[] anonymousPostingKeys = new String[]{ "on" };
	private static final String[] defaultKeys = new String[]{ "enabled", "disabled" };
	
	private MultipleSelectionElement anonymousPostingEl;
	private SingleSelection defaultCourseEl, defaultMessageEl;
	
	@Autowired
	private ForumModule forumModule;
	
	public ForumSettingsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_DEFAULT_6_6);
		setTranslator(Util.createPackageTranslator(Forum.class, getLocale(), getTranslator()));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.title");
		
		String[] anonymousPostingValues = new String[]{ "" };
		anonymousPostingEl = uifactory.addCheckboxesHorizontal("anonymous.posting", "anonymous.posting", formLayout,
				anonymousPostingKeys, anonymousPostingValues);
		if(forumModule.isAnonymousPostingWithPseudonymEnabled()) {
			anonymousPostingEl.select(anonymousPostingKeys[0], true);
		}
		anonymousPostingEl.addActionListener(FormEvent.ONCHANGE);
		
		String[] defaultValues = new String[]{
				translate("anonymous.default.enabled"), translate("anonymous.default.disabled")
		};
		defaultCourseEl = uifactory.addRadiosHorizontal("anonymous.course.default", "anonymous.course.default", formLayout,
				defaultKeys, defaultValues);
		if(forumModule.isPseudonymForCourseEnabledByDefault()) {
			defaultCourseEl.select(defaultKeys[0], true);
		} else {
			defaultCourseEl.select(defaultKeys[1], true);
		}
		defaultCourseEl.addActionListener(FormEvent.ONCHANGE);
		
		
		defaultMessageEl = uifactory.addRadiosHorizontal("anonymous.message.default", "anonymous.message.default", formLayout,
				defaultKeys, defaultValues);
		defaultMessageEl.setHelpText(translate("anonymous.message.default.hint"));
		if(forumModule.isPseudonymForMessageEnabledByDefault()) {
			defaultMessageEl.select(defaultKeys[0], true);
		} else {
			defaultMessageEl.select(defaultKeys[1], true);
		}
		defaultMessageEl.addActionListener(FormEvent.ONCHANGE);

		updateUI();
	}
	
	private void updateUI() {
		defaultCourseEl.setVisible(anonymousPostingEl.isAtLeastSelected(1));
		defaultMessageEl.setVisible(anonymousPostingEl.isAtLeastSelected(1));
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(anonymousPostingEl == source) {
			forumModule.setAnonymousPostingWithPseudonymEnabled(anonymousPostingEl.isAtLeastSelected(1));
			updateUI();
		} else if(defaultCourseEl == source) {
			boolean enabled = defaultCourseEl.isOneSelected() && defaultCourseEl.isSelected(0);
			forumModule.setPseudonymForCourseEnabledByDefault(enabled);
		} else if(defaultMessageEl == source) {
			boolean enabled = defaultMessageEl.isOneSelected() && defaultMessageEl.isSelected(0);
			forumModule.setPseudonymForMessageEnabledByDefault(enabled);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
