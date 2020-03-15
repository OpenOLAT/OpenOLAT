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
package org.olat.course.auditing;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.Role;
import org.olat.properties.Property;

/**
 * Default implementation of the UserNodeAuditManager - storing
 * the user node logs in the properties table.
 * <p>
 * Note that this has an inherent problem in that the property
 * size is limited yet this class appends to that property 
 * constantly.
 * <p>
 * Initial Date:  22.10.2009 <br>
 * @author Stefan
 */
public class UserNodeAuditManagerImpl implements UserNodeAuditManager {

	protected static final String LOG_DELIMITER = "-------------------------------------------------------------------\n";
	protected static final String LOG_PREFIX_REMOVED_OLD_LOG_ENTRIES = "Removed old log entires because of limited log size\n";
	private static final SimpleDateFormat sdb = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	private final CoursePropertyManager cpm;

	public UserNodeAuditManagerImpl(ICourse course) {
		cpm = course.getCourseEnvironment().getCoursePropertyManager();
	}
	
	@Override
	public void appendToUserNodeLog(CourseNode courseNode, Identity identity, Identity assessedIdentity, String logText, Role by) {
		String text = formatMessage(identity, logText, by) ;
		cpm.appendText(courseNode, assessedIdentity, null, LOG_IDENTIFYER, text);
		
		
		/*
		Property logProperty = cpm.findCourseNodeProperty(courseNode, assessedIdentity, null, LOG_IDENTIFYER);
		if (logProperty == null) {
			logProperty = cpm.createCourseNodePropertyInstance(courseNode, assessedIdentity, null, LOG_IDENTIFYER, null, null, null, text);
			cpm.saveProperty(logProperty);
		} else {
			String newLog = logProperty.getTextValue().concat(text);
			String limitedLogContent = createLimitedLogContent(newLog, 60000);
			logProperty.setTextValue(limitedLogContent);
			cpm.updateProperty(logProperty);
		}
		*/
	}
		
	@Override
	public void appendToUserNodeLog(CourseNode courseNode, Identity identity, BusinessGroup assessedGroup, String logText, Role by) {
		String text = formatMessage(identity, logText, by) ;
		cpm.appendText(courseNode, null, assessedGroup, LOG_IDENTIFYER, text);
		/*
		Property logProperty = cpm.findCourseNodeProperty(courseNode, null, assessedGroup, LOG_IDENTIFYER);
		if (logProperty == null) {
			logProperty = cpm.createCourseNodePropertyInstance(courseNode, null, assessedGroup, LOG_IDENTIFYER, null, null, null, text);
			cpm.saveProperty(logProperty);
		} else {
			String newLog = logProperty.getTextValue().concat(text);
			String limitedLogContent = createLimitedLogContent(newLog, 60000);
			logProperty.setTextValue(limitedLogContent);
			cpm.updateProperty(logProperty);
		}
		*/
	}

	private String formatMessage(Identity identity, String logText, Role by) {
		Date now = new Date();
		String date;
		synchronized(sdb) {
			date = sdb.format(now);
		}
		StringBuilder sb = new StringBuilder(256);
		sb.append(LOG_DELIMITER)
		  .append("Date: ").append(date).append("\n");
		if(by == Role.auto) {
			sb.append("Identity: automatic");
		} else if(identity != null) {
			sb.append("Identity: ")
			  .append(identity.getUser().getFirstName())
			  .append(" ")
			  .append(identity.getUser().getLastName())
			  .append(" (")
			  .append(identity.getKey()).append(")");
			if(by == Role.coach) {
				sb.append(" (coach)");
			}
			sb.append("\n");
		}
		sb.append(logText).append("\n");
		return sb.toString();
	}

	protected String createLimitedLogContent(String logContent, int maxLength) {
		if (logContent.length() < maxLength) {
			return logContent;// nothing to limit
		}
		// too long => limit it by removing first log entries
		while (logContent.length() > maxLength) {
			int posSecongLogDelimiter = logContent.indexOf(LOG_DELIMITER, LOG_DELIMITER.length() );
			logContent = logContent.substring(posSecongLogDelimiter);
		}
		return LOG_PREFIX_REMOVED_OLD_LOG_ENTRIES + logContent;
	}

	@Override
	public boolean hasUserNodeLogs(CourseNode node) {
		int numOfProperties = cpm.countCourseNodeProperties(node, null, null, LOG_IDENTIFYER);
		return numOfProperties > 0;
	}

	@Override
	public String getUserNodeLog(CourseNode courseNode, Identity identity) {
		Property property = cpm.findCourseNodeProperty(courseNode, identity, null, LOG_IDENTIFYER);
		return property == null ? null : property.getTextValue();
	}

	@Override
	public String getUserNodeLog(CourseNode courseNode, BusinessGroup businessGroup) {
		Property property = cpm.findCourseNodeProperty(courseNode, businessGroup, LOG_IDENTIFYER);
		return property == null ? null : property.getTextValue();
	}
}
