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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.SearchCoachedIdentityParams;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.modules.coach.security.RoleSecurityCallbackFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 25 May 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com
 */
public class OrganisationListController extends AbstactCoachListController {
	
	private static final String FILTER_ORGANISATIONS = "organisations";

    @Autowired
    private CoachingService coachingService;
    @Autowired
    private OrganisationService organisationService;

    private final List<Organisation> organisations;
    private final OrganisationRoles organisationRole;

    public OrganisationListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, OrganisationRoles role) {
        super(ureq, wControl, stackPanel);

        organisationRole = role;
        organisations = organisationService.getOrganisations(getIdentity(), role);
        
        Organisation organisation = organisations.get(0);
        securityCallback = RoleSecurityCallbackFactory.create(organisationService.getGrantedOrganisationRights(organisation, organisationRole));

        initForm(ureq);
        loadModel();
    }
    
    @Override
    protected void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
    	
    	if(organisations.size() > 1) {
    		SelectionValues organisationsKV = new SelectionValues();
    		for(Organisation organisation:organisations) {
    			organisationsKV.add(SelectionValues.entry(organisation.getKey().toString(),
    					StringHelper.escapeHtml(organisation.getDisplayName())));
    		}
    		FlexiTableMultiSelectionFilter participantFilter = new FlexiTableMultiSelectionFilter(translate("filter.organisations"),
    				FILTER_ORGANISATIONS, organisationsKV, true);
    		filters.add(participantFilter);
    	}
    	
    	if(!filters.isEmpty()) {
    		tableEl.setFilters(true, filters, false, true);
    	}
    }

    @Override
    protected void loadModel() {
    	if(organisations.isEmpty()) {
    		model.setObjects(List.of());
    	} else {
        	List<StudentStatEntry> students = getUserStatistics();
        	model.setObjects(students);
        	if(StringHelper.containsNonWhitespace(tableEl.getQuickSearchString())) {
        		model.search(tableEl.getQuickSearchString());
        	}
    	}
        tableEl.reset(true, true, true);
    }

	private List<StudentStatEntry> getUserStatistics() {
		SearchCoachedIdentityParams params = new SearchCoachedIdentityParams();
		params.setOrganisations(getFilteredOrganisations(tableEl.getFilters()));
		params.setIgnoreInheritedOrgMemberships(true);
		List<StudentStatEntry> courseStats = coachingService.getUsersStatistics(params, userPropertyHandlers, getLocale());
		List<StudentStatEntry> userProperties = coachingService.getUsersByOrganization(userPropertyHandlers, getIdentity(), 
				getFilteredOrganisations(tableEl.getFilters()), organisationRole, getLocale());

		Set<Long> courseStatsIdentityKeys = courseStats.stream().map(StudentStatEntry::getIdentityKey).collect(Collectors.toSet());
		List<StudentStatEntry> result = new ArrayList<>(courseStats);
		for (StudentStatEntry studentStatEntry : userProperties) {
			if (courseStatsIdentityKeys.contains(studentStatEntry.getIdentityKey())) {
				continue;
			}
			if (studentStatEntry.getIdentityKey() == getIdentity().getKey()) {
				continue;
			}
			result.add(studentStatEntry);
		}
		return result;
	}

	private List<Organisation> getFilteredOrganisations(List<FlexiTableFilter> filters) {
    	FlexiTableFilter organisationsFilter = FlexiTableFilter.getFilter(filters, FILTER_ORGANISATIONS);
		if(organisationsFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)organisationsFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				Set<Long> keys = filterValues.stream()
						.map(Long::valueOf)
						.collect(Collectors.toSet());
				List<Organisation> filteredOrganisations = organisations.stream()
						.filter(org -> keys.contains(org.getKey()))
						.toList();
				if(!filteredOrganisations.isEmpty()) {
					return filteredOrganisations;
				}
			}
		}
		return organisations;
    }

    @Override
    protected UserOverviewController selectStudent(UserRequest ureq, StudentStatEntry studentStat) {
        Identity student = securityManager.loadIdentityByKey(studentStat.getIdentityKey());
        OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, student.getKey());
        WindowControl bwControl = addToHistory(ureq, ores, null);
        
        int index = model.getObjects().indexOf(studentStat);
        userCtrl = new UserOverviewController(ureq, bwControl, stackPanel, studentStat, student, index, model.getRowCount(), null, securityCallback);
        listenTo(userCtrl);

        String displayName = userManager.getUserDisplayName(student);
        stackPanel.pushController(displayName, userCtrl);
        return userCtrl;
    }
}
