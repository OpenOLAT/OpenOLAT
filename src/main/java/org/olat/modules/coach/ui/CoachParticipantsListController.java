/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.coach.ui;

import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.coach.model.ParticipantStatisticsEntry;
import org.olat.modules.coach.model.SearchParticipantsStatisticsParams;

/**
 * 
 * Initial date: 13 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CoachParticipantsListController extends AbstractParticipantsListController {
	
	private final GroupRoles role;
	
	public CoachParticipantsListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, GroupRoles role) {
		super(ureq, wControl, stackPanel, role.name());
		this.role = role;
		
		initForm(ureq);
		loadModel();
		loadOrganisationsFilterFromModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initTableForm(formLayout, ureq, false, true, true, false);
	}
	
	@Override
	protected List<ParticipantStatisticsEntry> loadStatistics() {
		SearchParticipantsStatisticsParams params = SearchParticipantsStatisticsParams.as(getIdentity(), role);
		params
			.withOrganisations(organisationsEnabled)
			.withReservations(false)
			.withCourseCompletion(true)
			.withCourseStatus(true);
		return coachingService.getParticipantsStatistics(params, userPropertyHandlers, getLocale());
	}
	
	@Override
	protected NextPreviousController createParticipantOverview(UserRequest ureq, ParticipantStatisticsEntry studentStat) {
		Identity student = securityManager.loadIdentityByKey(studentStat.getIdentityKey());
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, student.getKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		
		int index = tableModel.getObjects().indexOf(studentStat);
		return new StudentCoursesController(ureq, bwControl, stackPanel, studentStat, student, index, tableModel.getRowCount(), false);
	}
}
