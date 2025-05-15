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

import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.RoleSecurityCallback;
import org.olat.modules.coach.model.ParticipantStatisticsEntry;
import org.olat.modules.coach.model.SearchParticipantsStatisticsParams;
import org.olat.modules.coach.security.RoleSecurityCallbackFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OrganisationListController extends AbstractParticipantsListController {
	
    
	private final List<Organisation> organisations;
    private final OrganisationRoles organisationRole;
	
	private final boolean canViewReservations;
	private final boolean canViewCoursesAndCurriculum;
	private final boolean canViewCourseProgressAndStatus;
	private final RoleSecurityCallback securityCallback;
	
    @Autowired
    private CoachingService coachingService;
    @Autowired
    private OrganisationService organisationService;

    public OrganisationListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
    		OrganisationRoles role, String initialTab) {
        super(ureq, wControl, stackPanel, role.name());

        organisationRole = role;
        organisations = organisationService.getOrganisations(getIdentity(), role);
        
		Organisation organisation = (organisations == null || organisations.isEmpty()) ? null : organisations.get(0);
		securityCallback = RoleSecurityCallbackFactory.create(organisationService.getGrantedOrganisationRights(organisation, organisationRole));
        canViewReservations = securityCallback.canViewPendingCourseBookings();
        canViewCoursesAndCurriculum = securityCallback.canViewCoursesAndCurriculum();
        canViewCourseProgressAndStatus = securityCallback.canViewCourseProgressAndStatus();

        initForm(ureq);
        loadModel();
        setFilterOrganisations(organisations);
        if(tableEl.getSelectedFilterTab() == null || !tableEl.getSelectedFilterTab().getId().equals(initialTab)) {
        	tableEl.setSelectedFilterTab(ureq, tableEl.getFilterTabById(initialTab));
        	tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
        	tableEl.reset(true, true, true);
        }
    }  
    
    @Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initTableForm(formLayout, ureq, canViewReservations, canViewCoursesAndCurriculum, canViewCourseProgressAndStatus, true);
    }

    @Override
	protected List<ParticipantStatisticsEntry> loadStatistics() {
    	if(organisations.isEmpty()) {
    		return List.of();
    	}
    	SearchParticipantsStatisticsParams searchParams = SearchParticipantsStatisticsParams.as(organisations);
    	searchParams
    		.withOrganisations(true)
    		.withReservations(canViewReservations)
			.withCourseCompletion(canViewCourseProgressAndStatus)
			.withCourseStatus(canViewCourseProgressAndStatus);
    	return coachingService.getParticipantsStatistics(searchParams, userPropertyHandlers, getLocale());
    }

    @Override
    protected UserOverviewController createParticipantOverview(UserRequest ureq, ParticipantStatisticsEntry statisticsEntry) {
		Identity identity = securityManager.loadIdentityByKey(statisticsEntry.getIdentityKey());
		if (!allowedToManageUser(identity)) {
			showWarning("error.select.no.permission");
			return null;
		}

        OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, identity.getKey());
        WindowControl bwControl = addToHistory(ureq, ores, null);
        
        int index = tableModel.getObjects().indexOf(statisticsEntry);
        return new UserOverviewController(ureq, bwControl, stackPanel, statisticsEntry, identity, index, tableModel.getRowCount(), null, securityCallback);
    }

	private boolean allowedToManageUser(Identity user) {
		Roles userRoles = securityManager.getRoles(user);

		if (userRoles.isSystemAdmin() || userRoles.isAdministrator() || userRoles.isPrincipal() || userRoles.isManager()) {
			return false;
		}

		if (!userRoles.hasRole(OrganisationRoles.user)) {
			return false;
		}
		
		if (userRoles.isInvitee() || userRoles.hasRole(OrganisationRoles.guest)) {
			return false;
		}

		return true;
	}
}
