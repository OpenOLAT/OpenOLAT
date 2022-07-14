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
package org.olat.course.config.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.repository.RepositoryEntry;


/**
 * 
 * Initial date: 11 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseInvitationsSettingsController extends FormBasicController {
	
	private MultipleSelectionElement invitationEnableEl;
	
	private final boolean editable;
	private RepositoryEntry entry;
	private CourseConfig courseConfig;
	
	CourseInvitationsSettingsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, ICourse course, boolean editable) {
		super(ureq, wControl);
		this.entry = entry;
		this.editable = editable;
		courseConfig = course.getCourseEnvironment().getCourseConfig().clone();

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		setFormTitle("invitations.settings.title");
		setFormDescription("invitations.settings.descr");
		
		SelectionValues onKeyValues = new SelectionValues();
		onKeyValues.add(SelectionValues.entry("on", translate("on")));
		
		invitationEnableEl = uifactory.addCheckboxesHorizontal("course.invitation.enable", "course.invitation.enable", formLayout,
				onKeyValues.keys(), onKeyValues.values());
		invitationEnableEl.setEnabled(editable);
		if(courseConfig.isInvitationByOwnersWithAuthorRightsEnabled()) {
			invitationEnableEl.select("on", true);
		}
		
		uifactory.addFormSubmitButton("save", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		OLATResourceable courseOres = entry.getOlatResource();
		if(CourseFactory.isCourseEditSessionOpen(courseOres.getResourceableId())) {
			showWarning("error.editoralreadylocked", new String[] { "???" });
			return;
		}
		
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		courseConfig = course.getCourseEnvironment().getCourseConfig();
		courseConfig.setInvitationByOwnersWithAuthorRightsEnabled(invitationEnableEl.isAtLeastSelected(1));
		
		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
}
