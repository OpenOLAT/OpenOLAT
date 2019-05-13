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

package org.olat.course.nodes.projectbroker.service;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.projectbroker.ProjectBrokerControllerFactory;
import org.olat.course.nodes.projectbroker.datamodel.CustomField;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.datamodel.ProjectEvent;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.group.BusinessGroupService;

/**
 * @author Christian Guretzki
 */
public class ProjectBrokerExportGenerator {
	private static final Logger log = Tracing.createLoggerFor(ProjectBrokerExportGenerator.class);
  
  private static final String END_OF_LINE = "\t\n";
  private static final String TABLE_DELIMITER = "\t";
  
	/**
	 * The results from assessable nodes are written to one row per user into an excel-sheet. An
     * assessable node will only appear if it is producing at least one of the
     * following variables: score, passed, attempts, comments.
	 * 
	 * @param identities
	 * @param myNodes
	 * @param course
	 * @param locale
	 * @return String
	 */
	public static String createCourseResultsOverviewTable(CourseNode courseNode, ICourse course, Locale locale) {
		Translator translator = Util.createPackageTranslator(ProjectBrokerControllerFactory.class, locale);
		StringBuilder table = new StringBuilder();
		ProjectBrokerModuleConfiguration moduleConfig = new ProjectBrokerModuleConfiguration(courseNode.getModuleConfiguration());
		
		ProjectBrokerManager projectBrokerManager = CoreSpringFactory.getImpl(ProjectBrokerManager.class);

		// load project-list
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		Long projectBrokerId = projectBrokerManager.getProjectBrokerId(cpm, courseNode);
		if (projectBrokerId != null) {
			List<Project> projects = projectBrokerManager.getProjectListBy(projectBrokerId);				
			// build table-header
			table.append( createHeaderLine(translator, moduleConfig)) ;			
			// loop over all projects
			for (Project project : projects) {
				table.append( createProjectDataLine(translator, project, moduleConfig, translator) );
			}
		} else {
			log.debug("projectBrokerId is null, courseNode=" + courseNode + " , course=" + course);
		}
		return table.toString();
	}

	private static String createProjectDataLine(Translator t, Project project, ProjectBrokerModuleConfiguration moduleConfig, Translator translator) {
		StringBuilder line = new StringBuilder();

		line.append(project.getTitle());
		line.append(TABLE_DELIMITER);
		// loop over project leaders
		StringBuilder projectLeader = new StringBuilder();
		boolean firstElement = true;
		for (Identity identity : project.getProjectLeaders()) {
			if (!firstElement) {
				projectLeader.append(" , ");
			}
			String last = identity.getUser().getProperty(UserConstants.LASTNAME, t.getLocale());
			String first= identity.getUser().getProperty(UserConstants.FIRSTNAME, t.getLocale());
			projectLeader.append(first);
			projectLeader.append(" ");
			projectLeader.append(last);
			firstElement = false;
		}
		line.append(projectLeader.toString());
		line.append(TABLE_DELIMITER);

		line.append(t.translate(project.getState()));
		line.append(TABLE_DELIMITER);
		// loop over customfileds
		for (int customFieldIndex=0; customFieldIndex<moduleConfig.getCustomFields().size(); customFieldIndex++) {
			String value = project.getCustomFieldValue(customFieldIndex);
			line.append(value);
			line.append(TABLE_DELIMITER);
		}

		line.append(project.getSelectedPlaces());
		line.append(TABLE_DELIMITER);
		// loop over all events
		for (Project.EventType eventType : Project.EventType.values()) {
			if (moduleConfig.isProjectEventEnabled(eventType) ) {
				ProjectEvent projectEvent = project.getProjectEvent(eventType);
				if (projectEvent.getStartDate() != null) {
					line.append(translator.translate("export.event.start.prefix"));
					line.append(" ");
					line.append(projectEvent.getFormattedStartDate());
					line.append(" ");
				}
				if (projectEvent.getEndDate() != null) {
					line.append(translator.translate("export.event.end.prefix"));
					line.append(" ");
					line.append(projectEvent.getFormattedEndDate());
				}
				line.append(TABLE_DELIMITER);
			}
		}		
		// loop over all paricipants
		
		StringBuilder participants = new StringBuilder();
		boolean firstParticipants = true;
		List<Identity> participantList = CoreSpringFactory.getImpl(BusinessGroupService.class)
				.getMembers(project.getProjectGroup(), GroupRoles.participant.name());
		for (Identity identity : participantList) {
			if (!firstParticipants) {
				participants.append(" , ");
			}
			String last = identity.getUser().getProperty(UserConstants.LASTNAME, t.getLocale());
			String first= identity.getUser().getProperty(UserConstants.FIRSTNAME, t.getLocale());
			participants.append(first);
			participants.append(" ");
			participants.append(last);
			firstParticipants = false;
		}
		line.append(participants.toString());
		line.append(TABLE_DELIMITER);
		line.append(project.getKey().toString());
		line.append(END_OF_LINE);
		return line.toString();
	}

	private static String createHeaderLine(Translator t, ProjectBrokerModuleConfiguration moduleConfig) {
		StringBuilder line = new StringBuilder();
		line.append(t.translate("export.header.title"));
		line.append(TABLE_DELIMITER);
		line.append(t.translate("export.header.projectleaders"));
		line.append(TABLE_DELIMITER);
		line.append(t.translate("export.header.projectstate"));
		line.append(TABLE_DELIMITER);
		// loop over enable customfileds
		for (Iterator<CustomField> iterator =  moduleConfig.getCustomFields().iterator(); iterator.hasNext();) {
			line.append(iterator.next().getName());
			line.append(TABLE_DELIMITER);
		}	
		line.append(t.translate("export.header.selectedplaces"));
		line.append(TABLE_DELIMITER);
		// loop over events
		for (Project.EventType eventType : Project.EventType.values()) {
			if (moduleConfig.isProjectEventEnabled(eventType) ) {
				line.append(t.translate(eventType.getI18nKey()));
				line.append(TABLE_DELIMITER);
			}
		}		
		line.append(t.translate("export.header.projectpaticipants"));
		line.append(TABLE_DELIMITER);
		line.append(t.translate("export.header.projectid"));
		line.append(END_OF_LINE);
		return line.toString();
	}
    
}
