/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.projectbroker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.course.nodes.projectbroker.datamodel.CustomField;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerManager;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerModuleConfiguration;
import org.olat.group.BusinessGroupService;

/**
 * 
 * @author guretzki
 */

public class ProjectListTableModel extends DefaultTableDataModel<Project> {
	private static final int COLUMN_COUNT = 6;
	private Identity identity;
	private Translator translator;
	private ProjectBrokerModuleConfiguration moduleConfig;
	private int numberOfCustomFieldInTable;
	private int numberOfEventInTable;
	private int nbrSelectedProjects;
	private List<Project.EventType> enabledEventList;
	private boolean isParticipantInAnyProject;
	// Array with numbers of the customfields [0...MAX_NBR_CUSTOMFIELDS] which are enabled for table-view 
	private int[] enabledCustomFieldNumbers;
	
	private final ProjectBrokerManager projectBrokerManager;
	
	/**
	 * @param owned list of projects 
	 */
	public ProjectListTableModel(List<Project> owned, Identity identity, Translator translator, ProjectBrokerModuleConfiguration moduleConfig, 
			                         int numberOfCustomFieldInTable, int numberOfEventInTable, int nbrSelectedProjects, boolean isParticipantInAnyProject) {
		super(owned);
		this.identity = identity;
		this.translator = translator;
		this.moduleConfig = moduleConfig;
		this.numberOfCustomFieldInTable = numberOfCustomFieldInTable;
		this.numberOfEventInTable = numberOfEventInTable;
		this.nbrSelectedProjects = nbrSelectedProjects;
		this.enabledEventList = getEnabledEvents(moduleConfig);
		this.isParticipantInAnyProject = isParticipantInAnyProject;
		this.enabledCustomFieldNumbers = new int[numberOfCustomFieldInTable];
		projectBrokerManager = CoreSpringFactory.getImpl(ProjectBrokerManager.class);
		// loop over all custom fields
		int index = 0;
		int customFiledIndex = 0;
		for (Iterator<CustomField> iterator = moduleConfig.getCustomFields().iterator(); iterator.hasNext();) {
			CustomField customField = iterator.next();
			if (customField.isTableViewEnabled()) {
				enabledCustomFieldNumbers[index++] = customFiledIndex;			
			}
			customFiledIndex++;
		}
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		return COLUMN_COUNT + numberOfCustomFieldInTable + numberOfEventInTable;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int col) {
		Project project = objects.get(row);
		if (col == 0) {
			String name = project.getTitle();
			return name;
		} else if (col == 1) {
			// get identity_date list sorted by AddedDate
		  List<Identity> identities = CoreSpringFactory.getImpl(BusinessGroupService.class)
					.getMembers(project.getProjectGroup(), GroupRoles.coach.name());
			if (identities.isEmpty()) {
				return List.of();
			} else {
				// return all proj-leaders
				List<Identity> allIdents = new ArrayList<>();
				for (Identity idobj : identities) {
					allIdents.add(idobj);
				}
				return allIdents;
			}
		} else if (col == (numberOfCustomFieldInTable + numberOfEventInTable + 2)) {
			return projectBrokerManager.getStateFor(project,identity,moduleConfig);
		} else if (col == (numberOfCustomFieldInTable + numberOfEventInTable + 3)) { // num. of slots
			StringBuilder buf = new StringBuilder();
			buf.append(project.getSelectedPlaces());
			if (project.getMaxMembers() != Project.MAX_MEMBERS_UNLIMITED) {
				buf.append(" ");
				buf.append(translator.translate("projectlist.numbers.delimiter"));
				buf.append(" ");
				buf.append(project.getMaxMembers());
			}
			return buf.toString();
		}	else if (col == (numberOfCustomFieldInTable + numberOfEventInTable + 4)) { // enroll
			return projectBrokerManager.canBeProjectSelectedBy(identity, project, moduleConfig, nbrSelectedProjects, isParticipantInAnyProject);
		} else if (col == (numberOfCustomFieldInTable + numberOfEventInTable + 5)) { // cancel enrollment
			return projectBrokerManager.canBeCancelEnrollmentBy(identity,project,moduleConfig);
		} else if ( (col == 2) && (numberOfCustomFieldInTable > 0) ) {
			return project.getCustomFieldValue(enabledCustomFieldNumbers[0]);
		} else if ( (col == 3) && (numberOfCustomFieldInTable > 1) ) {
			return project.getCustomFieldValue(enabledCustomFieldNumbers[1]);
		} else if ( (col == 4) && (numberOfCustomFieldInTable > 2) ) {
			return project.getCustomFieldValue(enabledCustomFieldNumbers[2]);
		} else if ( (col == 5) && (numberOfCustomFieldInTable > 3) ) {
			return project.getCustomFieldValue(enabledCustomFieldNumbers[3]);
		} else if ( (col == 6) && (numberOfCustomFieldInTable > 4) ) {
			return project.getCustomFieldValue(enabledCustomFieldNumbers[4]);
		} else if ( col == (2 + numberOfCustomFieldInTable) ) {
			return project.getProjectEvent(enabledEventList.get(0));
		} else if ( col == (3 + numberOfCustomFieldInTable) ) {
			return project.getProjectEvent(enabledEventList.get(1));
		} else if ( col == (4 + numberOfCustomFieldInTable) ) {
			return project.getProjectEvent(enabledEventList.get(2));
		} else {
			return "ERROR";
		}
	}

	private List<Project.EventType> getEnabledEvents(ProjectBrokerModuleConfiguration moduleConfig) {
		List<Project.EventType> enabledEventList = new ArrayList<>();
		for (Project.EventType eventType : Project.EventType.values()) {
			if (moduleConfig.isProjectEventEnabled(eventType) && moduleConfig.isProjectEventTableViewEnabled(eventType)) {
				enabledEventList.add(eventType);
			}
		}
		return enabledEventList;
	}

	/**
	 * @param owned
	 */
	public void setEntries(List<Project> owned) {
		this.objects = owned;
	}

	/**
	 * @param row
	 * @return the project at the given row
	 */
	public Project getProjectAt(int row) {
		return objects.get(row);
	}

	public Object createCopyWithEmptyList() {
		ProjectListTableModel copy = new ProjectListTableModel(new ArrayList<Project>(), identity, translator, moduleConfig, numberOfCustomFieldInTable, numberOfEventInTable, nbrSelectedProjects, isParticipantInAnyProject);
		return copy;
	}

}