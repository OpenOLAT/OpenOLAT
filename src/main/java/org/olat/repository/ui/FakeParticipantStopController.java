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
package org.olat.repository.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.ButtonSize;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.run.scoring.ResetCourseDataHelper;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;

/**
 * 
 * Initial date: 8 Sep 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FakeParticipantStopController extends BasicController {
	
	public static final Event STOP_EVENT = new Event("stop");

	private Link stopButton;
	private Link resetDataButton;
	private final Dropdown moreDropdown;
	
	private final RepositoryEntry courseEntry;

	public FakeParticipantStopController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		this.courseEntry = courseEntry;
		
		VelocityContainer mainVC = createVelocityContainer("fake_participant_stop");
		
		stopButton = LinkFactory.createButtonSmall("fake.participant.stop", mainVC, this);
		
		moreDropdown = new Dropdown("fake.participant.more", null, false, getTranslator());
		moreDropdown.setCarretIconCSS("o_icon o_icon_commands");
		moreDropdown.setOrientation(DropdownOrientation.right);
		moreDropdown.setEmbbeded(true);
		moreDropdown.setButton(true);
		moreDropdown.setButtonSize(ButtonSize.small);
		mainVC.put("fake.participant.more", moreDropdown);
		
		resetDataButton = LinkFactory.createToolLink("reset.data", translate("reset.data"), this, "o_icon_reset_data");
		moreDropdown.addComponent(resetDataButton);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == stopButton) {
			fireEvent(ureq, STOP_EVENT);
		} else if(source == resetDataButton) {
			doResetData();
		}
	}

	private void doResetData() {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		new ResetCourseDataHelper(course.getCourseEnvironment())
			.resetCourse(getIdentity(), getIdentity(), Role.coach);
		showInfo("info.fake.user.course.reseted");
	}
}
