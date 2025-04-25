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
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.user.UserPropertiesInfoController;
import org.olat.user.UserPropertiesInfoController.Builder;
import org.olat.user.UserPropertiesInfoController.LabelValues;

/**
 * 
 * Initial date: 2 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CoachedIdentityLargeInfosController extends BasicController {

	public CoachedIdentityLargeInfosController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachedCourseEnv) {
		super(ureq, wControl);
		Identity coachedIdentity = coachedCourseEnv.getIdentityEnvironment().getIdentity();
		
		Builder lvBuilder = LabelValues.builder();
		List<BusinessGroup> participantGroups = coachedCourseEnv.getCourseEnvironment().getCourseGroupManager()
				.getParticipatingBusinessGroups(coachedIdentity);
		if (!participantGroups.isEmpty()) {
			Collator collator = Collator.getInstance(getLocale());
			String groupNames = participantGroups.stream()
					.map(BusinessGroup::getName)
					.map(StringHelper::escapeHtml)
					.sorted((a, b) -> collator.compare(a, b))
					.collect(Collectors.joining(", "));
			lvBuilder.add(translate("participant.groups.title"), groupNames);
		}
		
		UserPropertiesInfoController userInfoCtr = new UserPropertiesInfoController(ureq, getWindowControl(),
				coachedIdentity, null, lvBuilder.build());
		listenTo(userInfoCtr);
		
		putInitialPanel(userInfoCtr.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
