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
package org.olat.modules.teams;

import org.olat.core.logging.activity.ActionObject;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.ActionVerb;
import org.olat.core.logging.activity.BaseLoggingAction;
import org.olat.core.logging.activity.CrudAction;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ResourceableTypeList;
import org.olat.core.logging.activity.StringResourceableType;

/**
 * 
 * Initial date: 31 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class TeamsLoggingAction extends BaseLoggingAction {
	
	public static final ILoggingAction TEAMS_MEETING_START = 
			new TeamsLoggingAction(ActionType.statistic, CrudAction.create, ActionVerb.start, ActionObject.teams).setTypeList(
					new ResourceableTypeList().
					// this one is a message in a meeting in a course
					addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.teams).
					// this one is a message in a meeting in a group
					or().addMandatory(OlatResourceableType.businessGroup, OlatResourceableType.teams).addMandatory(StringResourceableType.targetIdentity));
	
	public static final ILoggingAction TEAMS_MEETING_JOIN = 
			new TeamsLoggingAction(ActionType.statistic, CrudAction.retrieve, ActionVerb.join, ActionObject.teams).setTypeList(
					new ResourceableTypeList().
					// this one is a message in a meeting in a course
					addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.teams).
					// this one is a message in a meeting in a group
					or().addMandatory(OlatResourceableType.businessGroup, OlatResourceableType.teams).addMandatory(StringResourceableType.targetIdentity));
	
	public static final ILoggingAction TEAMS_RECORDING_PUBLISH = 
			new TeamsLoggingAction(ActionType.statistic, CrudAction.update, ActionVerb.publish, ActionObject.teams).setTypeList(
					new ResourceableTypeList().
					// this one is a message in a meeting in a course
					addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.teams).
					// this one is a message in a meeting in a group
					or().addMandatory(OlatResourceableType.businessGroup, OlatResourceableType.teams).addMandatory(StringResourceableType.targetIdentity));
	
	public static final ILoggingAction TEAMS_RECORDING_DELETE = 
			new TeamsLoggingAction(ActionType.statistic, CrudAction.delete, ActionVerb.remove, ActionObject.teams).setTypeList(
					new ResourceableTypeList().
					// this one is a message in a meeting in a course
					addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.teams).
					// this one is a message in a meeting in a group
					or().addMandatory(OlatResourceableType.businessGroup, OlatResourceableType.teams).addMandatory(StringResourceableType.targetIdentity));

	/**
	 * Simple wrapper calling super<init>
	 * @see BaseLoggingAction#BaseLoggingAction(ActionType, CrudAction, ActionVerb, String)
	 */
	TeamsLoggingAction(ActionType resourceActionType, CrudAction action, ActionVerb actionVerb, ActionObject actionObject) {
		super(resourceActionType, action, actionVerb, actionObject.name());
	}
}
