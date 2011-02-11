package org.olat.course.auditing;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.properties.CoursePropertyManager;
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
public class UserNodeAuditManagerImpl extends UserNodeAuditManager {

	protected static final String LOG_DELIMITER = "-------------------------------------------------------------------\n";
	protected static final String LOG_PREFIX_REMOVED_OLD_LOG_ENTRIES = "Removed old log entires because of limited log size\n";
	private final OLATResourceable ores;

	public UserNodeAuditManagerImpl(ICourse course) {
		ores = course;
	}
	
	/**
	 * @see org.olat.course.auditing.AuditManager#appendToUserNodeLog(org.olat.course.nodes.CourseNode,
	 *      org.olat.core.id.Identity, org.olat.core.id.Identity,
	 *      java.lang.String)
	 */
	public void appendToUserNodeLog(CourseNode courseNode, Identity identity, Identity assessedIdentity, String logText) {
		ICourse course = CourseFactory.loadCourse(ores);
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		// Forma log message
		Date now = new Date();
		SimpleDateFormat sdb = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String date = sdb.format(now);
		StringBuilder sb = new StringBuilder();
		sb.append(LOG_DELIMITER);
		sb.append("Date: ").append(date).append("\n");
		sb.append("User: ").append(identity.getName()).append("\n");
		sb.append(logText).append("\n");
		Property logProperty = cpm.findCourseNodeProperty(courseNode, assessedIdentity, null, LOG_IDENTIFYER);
		if (logProperty == null) {
			logProperty = cpm.createCourseNodePropertyInstance(courseNode, assessedIdentity, null, LOG_IDENTIFYER, null, null, null, sb.toString());
			cpm.saveProperty(logProperty);
		} else {
			String newLog = logProperty.getTextValue() + sb.toString();
			String limitedLogContent = createLimitedLogContent(newLog,60000);
			logProperty.setTextValue(limitedLogContent);
			cpm.updateProperty(logProperty);
		}

	}

	protected String createLimitedLogContent(String logContent, int maxLength) {
		if (logContent.length() < maxLength) {
			return logContent.toString();// nothing to limit
		}
		// too long => limit it by removing first log entries
		while (logContent.length() > maxLength) {
			int posSecongLogDelimiter = logContent.indexOf(LOG_DELIMITER, LOG_DELIMITER.length() );
			logContent = logContent.substring(posSecongLogDelimiter);
		}
		return LOG_PREFIX_REMOVED_OLD_LOG_ENTRIES + logContent;
	}

	/**
	 * @see org.olat.course.auditing.AuditManager#hasUserNodeLogs(org.olat.course.nodes.CourseNode)
	 */
	public boolean hasUserNodeLogs(CourseNode node) {
		ICourse course = CourseFactory.loadCourse(ores);
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		List<?> logsProperties = cpm.listCourseNodeProperties(node, null, null, LOG_IDENTIFYER);
		return logsProperties.size() == 0 ? false : true;
	}

	/**
	 * @see org.olat.course.auditing.AuditManager#getUserNodeLog(org.olat.course.nodes.CourseNode,
	 *      org.olat.core.id.Identity)
	 */
	public String getUserNodeLog(CourseNode courseNode, Identity identity) {
		ICourse course = CourseFactory.loadCourse(ores);
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		Property property = cpm.findCourseNodeProperty(courseNode, identity, null, LOG_IDENTIFYER);
		if (property == null) return null;
		String result = property.getTextValue();
		return result;
	}


}
