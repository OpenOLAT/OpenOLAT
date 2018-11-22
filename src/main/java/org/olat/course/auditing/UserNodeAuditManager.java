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

import org.olat.core.id.Identity;
import org.olat.course.nodes.CourseNode;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.Role;

/**
 * Description:
 * <BR>
 * The audit manager provides logging functionality for user node logging.
 * <p>
 * There used to be two variants of logging of which one is now
 * handled in the ThreadLocalUserActivityLogger - check there for further
 * details.
 * <p>
 * The user node logs:
 * This is the users personal log that is used in some nodes to guarantee
 * transparent changes of user data like passed or score attributes. Ths has
 * access to this log via browser on the node that provides this log. 
 * <P>
 * 
 * Initial Date: Dec 1, 2004
 * @author gnaegi
 */
public interface UserNodeAuditManager {

	/**
	 * the log identifyer is used as name in the properties table, do not change
	 * it to another value!!
	 */
	public static final String LOG_IDENTIFYER = "LOG";

	/**
	 * @param node The current course node
	 * @return true if this node has user logs, false otherwhise
	 */
	public boolean hasUserNodeLogs(CourseNode node);

	/**
	 * @param courseNode The course node
	 * @param identity The identity
	 * @return The user log or null if no log available
	 */
	public String getUserNodeLog(CourseNode courseNode, Identity identity);
	
	/**
	 * 
	 * @param courseNode The course node
	 * @param businessGroup The business group
	 * @return The group log or null if not available
	 */
	public String getUserNodeLog(CourseNode courseNode, BusinessGroup businessGroup);

	/**
	 * Append a log message to the personal course node log
	 * 
	 * @param courseNode
	 * @param identity The user who initiated the action that triggered the log
	 *          entry
	 * @param assessedIdentity The user who is affected by the change
	 * @param logText The text to log
	 * @param by The role of the acting user (optional)
	 */
	public void appendToUserNodeLog(CourseNode courseNode, Identity identity, Identity assessedIdentity, String logText, Role by);
	
	/**
	 * 
	 * @param courseNode
	 * @param identity
	 * @param assessedIdentity
	 * @param assessedGroup
	 * @param logText
	 */
	public void appendToUserNodeLog(CourseNode courseNode, Identity identity, BusinessGroup assessedGroup, String logText, Role by);

}