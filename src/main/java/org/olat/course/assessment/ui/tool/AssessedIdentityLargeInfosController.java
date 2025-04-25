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
package org.olat.course.assessment.ui.tool;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.modules.dcompensation.DisadvantageCompensationService;
import org.olat.user.UserPropertiesInfoController;
import org.olat.user.UserPropertiesInfoController.Builder;
import org.olat.user.UserPropertiesInfoController.LabelValues;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedIdentityLargeInfosController extends BasicController {

	private final VelocityContainer mainVC;
	
	@Autowired
	private DisadvantageCompensationService disadvantageCompensationService;
	
	public AssessedIdentityLargeInfosController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity) {
		this(ureq, wControl, assessedIdentity, (CourseEnvironment)null, null);
	}
	
	public AssessedIdentityLargeInfosController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity,
			ICourse course, CourseNode courseNode) {
		this(ureq, wControl, assessedIdentity, (course == null ? null : course.getCourseEnvironment()), courseNode);
	}
	
	public AssessedIdentityLargeInfosController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity,
			CourseEnvironment courseEnv, CourseNode courseNode) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("user_infos_large");

		Builder lvBuilder = LabelValues.builder();
		if(courseNode != null) {
			DisadvantageCompensation compensation = disadvantageCompensationService.getActiveDisadvantageCompensation(assessedIdentity,
					courseEnv.getCourseGroupManager().getCourseEntry(), courseNode.getIdent());
			if(compensation != null && compensation.getExtraTime() != null) {
				int extraTimeInMinutes = compensation.getExtraTime().intValue() / 60;
				lvBuilder.add(translate("compensation.label"), translate("compensation.value", Integer.toString(extraTimeInMinutes)));
			}
		}
		List<BusinessGroup> participantGroups = courseEnv != null
				? courseEnv.getCourseGroupManager().getParticipatingBusinessGroups(assessedIdentity)
				: new ArrayList<>(1);
		final Collator collator = Collator.getInstance(getLocale());
		Collections.sort(participantGroups, (a, b) -> collator.compare(a.getName(), b.getName()));
		if (!participantGroups.isEmpty()) {
			String groupNames = participantGroups.stream()
					.map(BusinessGroup::getName)
					.map(StringHelper::escapeHtml)
					.collect(Collectors.joining(", "));
			lvBuilder.add(translate("participantgroups.title"), groupNames);
		}
		
		UserPropertiesInfoController userInfoCtr = new UserPropertiesInfoController(ureq, getWindowControl(),
				assessedIdentity, null, lvBuilder.build());
		mainVC.put("userInfo", userInfoCtr.getInitialComponent());
		listenTo(userInfoCtr);
		
		putInitialPanel(mainVC);	
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
