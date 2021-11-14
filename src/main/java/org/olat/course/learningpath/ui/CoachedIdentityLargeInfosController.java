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
package org.olat.course.learningpath.ui;

import java.text.Collator;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.admin.user.UserShortDescription;
import org.olat.admin.user.UserShortDescription.Builder;
import org.olat.admin.user.UserShortDescription.Rows;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.user.DisplayPortraitController;

/**
 * 
 * Initial date: 2 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CoachedIdentityLargeInfosController extends BasicController {

	private final VelocityContainer mainVC;
	private final DisplayPortraitController portraitCtr;
	private final UserShortDescription userShortDescrCtr;
	
	public CoachedIdentityLargeInfosController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachedCourseEnv) {
		super(ureq, wControl);
		Identity coachedIdentity = coachedCourseEnv.getIdentityEnvironment().getIdentity();
		mainVC = createVelocityContainer("user_infos_large");
		mainVC.contextPut("user", coachedIdentity.getUser());

		portraitCtr = new DisplayPortraitController(ureq, getWindowControl(), coachedIdentity, true, true);
		mainVC.put("portrait", portraitCtr.getInitialComponent());
		listenTo(portraitCtr);
		
		List<BusinessGroup> participantGroups = coachedCourseEnv.getCourseEnvironment().getCourseGroupManager()
				.getParticipatingBusinessGroups(coachedIdentity);
		final Collator collator = Collator.getInstance(getLocale());
		Collections.sort(participantGroups, (a, b) -> collator.compare(a.getName(), b.getName()));
		Builder rowsBuilder = Rows.builder();
		if (!participantGroups.isEmpty()) {
			String groupNames = participantGroups.stream()
					.map(BusinessGroup::getName)
					.collect(Collectors.joining(", "));
			rowsBuilder.addRow(translate("participant.groups.title"), groupNames);
		}
		Rows additionalRows = rowsBuilder.build();
		userShortDescrCtr = new UserShortDescription(ureq, getWindowControl(), coachedIdentity, additionalRows);
		mainVC.put("userShortDescription", userShortDescrCtr.getInitialComponent());
		listenTo(userShortDescrCtr);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
