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
package org.olat.modules.coach.ui;

import java.util.List;

import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.RelationRole;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.RoleSecurityCallback;
import org.olat.modules.coach.model.ParticipantStatisticsEntry;
import org.olat.modules.coach.model.SearchParticipantsStatisticsParams;
import org.olat.modules.coach.security.RoleSecurityCallbackFactory;
import org.olat.user.ui.role.RelationRolesAndRightsUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 25 May 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com
 */
public class UserRelationListController extends AbstractParticipantsListController {

    private final RelationRole relationRole;

	private final boolean canViewReservations;
	private final boolean canViewCoursesAndCurriculum;
	private final boolean canViewCourseProgressAndStatus;
	private final RoleSecurityCallback securityCallback;
    
    @Autowired
    private CoachingService coachingService;
    @Autowired
    private IdentityRelationshipService identityRelationshipService;

    public UserRelationListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, RelationRole relationRole) {
        super(ureq, wControl, stackPanel, "rr-" + relationRole.getKey());

        this.relationRole = identityRelationshipService.getRole(relationRole.getKey());
        securityCallback = RoleSecurityCallbackFactory.create(this.relationRole.getRights());
        canViewReservations = securityCallback.canViewPendingCourseBookings();
        canViewCoursesAndCurriculum = securityCallback.canViewCoursesAndCurriculum();
        canViewCourseProgressAndStatus = securityCallback.canViewCourseProgressAndStatus();

        super.initForm(ureq);
        loadModel();
    }  

    @Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initTableForm(formLayout, ureq, canViewReservations, canViewCoursesAndCurriculum, canViewCourseProgressAndStatus, false);
	}

    @Override
	protected List<ParticipantStatisticsEntry> loadStatistics() {
		SearchParticipantsStatisticsParams searchParams = SearchParticipantsStatisticsParams.as(getIdentity(), relationRole);
		searchParams
			.withOrganisations(organisationsEnabled)
			.withReservations(canViewReservations)
			.withCourseCompletion(canViewCourseProgressAndStatus)
			.withCourseStatus(canViewCourseProgressAndStatus);
		return coachingService.getParticipantsStatistics(searchParams, userPropertyHandlers, getLocale());
    }

    @Override
    protected UserOverviewController createParticipantOverview(UserRequest ureq, ParticipantStatisticsEntry statisticsEntry) {
        Identity user = securityManager.loadIdentityByKey(statisticsEntry.getIdentityKey());
        OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, user.getKey());
        WindowControl bwControl = addToHistory(ureq, ores, null);

        int index = tableModel.getObjects().indexOf(statisticsEntry);
        String roleTranslation = RelationRolesAndRightsUIFactory.getTranslatedContraRole(relationRole, getLocale());
        return new UserOverviewController(ureq, bwControl, stackPanel, statisticsEntry, user, index, tableModel.getRowCount(), roleTranslation, securityCallback);
    }
}
