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
* <p>
*/  

package org.olat.group;

import java.lang.reflect.Field;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
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
 * LoggingActions used by the group module.
 * <P>
 * PLEASE BE CAREFUL WHEN EDITING IN HERE.
 * <p>
 * Especially when modifying the ResourceableTypeList - which is 
 * a exercise where we try to predict/document/define which ResourceableTypes
 * will later on - at runtime - be available to the IUserActivityLogger.log() method.
 * <p>
 * The names of the LoggingAction should be self-describing.
 * <p>
 * Initial Date:  20.10.2009 <br>
 * @author Stefan
 */
public class GroupLoggingAction extends BaseLoggingAction {
	
	private static final Logger log = Tracing.createLoggerFor(GroupLoggingAction.class);

	private static final ResourceableTypeList GROUP_ACTION_RESOURCEABLE_TYPE_LIST =
		// first one is used for enrollment where targetIdentity is the user him/herself
		new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.businessGroup).
		// this is the course-group admin case
		or().addMandatory(OlatResourceableType.course, OlatResourceableType.businessGroup, StringResourceableType.targetIdentity).
		// this is the enrollment case
		or().addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.businessGroup, StringResourceableType.targetIdentity).
		// this is the standalone group admin case
		or().addMandatory(OlatResourceableType.businessGroup, StringResourceableType.targetIdentity).
		// this is the repository detail -> benutzer verwalten case
		or().addMandatory(OlatResourceableType.course, StringResourceableType.targetIdentity).
		// the user import case with adding to group in same step
		or().addMandatory(OlatResourceableType.businessGroup).addOptional(OlatResourceableType.businessGroup).addOptional(StringResourceableType.targetIdentity);
	
	public static final ILoggingAction GROUP_OPEN = 
		new GroupLoggingAction(ActionType.tracking, CrudAction.retrieve, ActionVerb.open, ActionObject.group).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.businessGroup).addOptional(OlatResourceableType.course));
	
	public static final ILoggingAction GROUP_CLOSED = 
		new GroupLoggingAction(ActionType.tracking, CrudAction.exit, ActionVerb.close, ActionObject.group).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.businessGroup).addOptional(OlatResourceableType.course).
				or().addMandatory(OlatResourceableType.businessGroup).addOptional(StringResourceableType.targetIdentity, OlatResourceableType.sharedFolder, OlatResourceableType.genRepoEntry));
	
	/** logging action: group owner has been added to group * */
	public static final ILoggingAction GROUP_OWNER_ADDED = 
		new GroupLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.add, ActionObject.owner).setTypeList(GROUP_ACTION_RESOURCEABLE_TYPE_LIST);

	/** logging action: group participant has been added to group * */
	public static final ILoggingAction GROUP_PARTICIPANT_ADDED = 
		new GroupLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.add, ActionObject.participant).setTypeList(GROUP_ACTION_RESOURCEABLE_TYPE_LIST);

	/** activity identitfyer: group owner has been removed from group * */
	public static final ILoggingAction GROUP_OWNER_REMOVED = 
		new GroupLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.remove, ActionObject.owner).setTypeList(GROUP_ACTION_RESOURCEABLE_TYPE_LIST);
	
	/** activity identitfyer: group participant has been removed from group * */
	public static final ILoggingAction GROUP_PARTICIPANT_REMOVED = 
		new GroupLoggingAction(ActionType.tracking, CrudAction.delete, ActionVerb.remove, ActionObject.participant).setTypeList(GROUP_ACTION_RESOURCEABLE_TYPE_LIST);

	/** logging action: somebody has been added to waiting-list * */
	public static final ILoggingAction GROUP_TO_WAITING_LIST_ADDED = 
		new GroupLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.add, ActionObject.waitingperson).setTypeList(GROUP_ACTION_RESOURCEABLE_TYPE_LIST);
	
	/** logging action: somebody has been removed from waiting-list * */
	public static final ILoggingAction GROUP_FROM_WAITING_LIST_REMOVED = 
		new GroupLoggingAction(ActionType.tracking,	CrudAction.delete, ActionVerb.remove, ActionObject.waitingperson).setTypeList(GROUP_ACTION_RESOURCEABLE_TYPE_LIST);

	/** activity identitfyer: group participant has been removed from group * */
	public static final ILoggingAction GROUP_MEMBER_REMOVED = 
		new GroupLoggingAction(ActionType.tracking, CrudAction.delete, ActionVerb.remove, ActionObject.participant).setTypeList(GROUP_ACTION_RESOURCEABLE_TYPE_LIST);


	/** activity identitfyer: group configuration has been changed * */
	public static final ILoggingAction GROUP_CONFIGURATION_CHANGED = 
		new GroupLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.edit, ActionObject.group).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.businessGroup).
					or().addMandatory(OlatResourceableType.businessGroup));
	
	
	/** activity identitfyer: learning area changed **/
	public static final ILoggingAction GROUP_AREA_UPDATED = 
		new GroupLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.edit, ActionObject.tools).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.businessGroup, StringResourceableType.bgArea));
	/** activity identitfyer: learning area changed - it just got empty **/
	public static final ILoggingAction GROUP_AREA_UPDATED_EMPTY = 
		new GroupLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.edit, ActionObject.toolsempty).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.businessGroup));
	
	/** activity identitfyer: group right updated **/
	public static final ILoggingAction GROUP_RIGHT_UPDATED = 
		new GroupLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.edit, ActionObject.grouparea);
	/** activity identitfyer: group right updated - it just got empty **/
	public static final ILoggingAction GROUP_RIGHT_UPDATED_EMPTY = 
		new GroupLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.edit, ActionObject.groupareaempty);
	
	

	public static final ILoggingAction GROUPMANAGEMENT_CLOSE = 
		new GroupLoggingAction(ActionType.admin, CrudAction.retrieve, ActionVerb.exit, ActionObject.groupmanagement).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.bgContext));
	
	public static final ILoggingAction GROUPMANAGEMENT_START = 
		new GroupLoggingAction(ActionType.admin, CrudAction.retrieve, ActionVerb.launch, ActionObject.groupmanagement).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.bgContext));

	public static final ILoggingAction BGAREA_UPDATED_NOW_EMPTY = 
		new GroupLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.edit, ActionObject.rightsempty);
	public static final ILoggingAction BGAREA_UPDATED_MEMBER_GROUP = 
		new GroupLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.edit, ActionObject.rights);
	
	/** activity identitfyer: group has been copied * */
	public static final ILoggingAction BG_GROUP_COPIED = 
		new GroupLoggingAction(ActionType.admin, CrudAction.create, ActionVerb.copy, ActionObject.group);
	
	/** activity identitfyer: new group has been created * */
	public static final ILoggingAction GROUP_CREATED = 
		new GroupLoggingAction(ActionType.admin, CrudAction.create, ActionVerb.add,	ActionObject.group).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.businessGroup));
	/** activity identitfyer: group has been inactivated * */
	public static final ILoggingAction GROUP_INACTIVATED = 
		new GroupLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.deactivate, ActionObject.group).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.businessGroup));
	/** activity identitfyer: group has been reactivated (after inactivation or soft delete) * */
	public static final ILoggingAction GROUP_REACTIVATED = 
		new GroupLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.deactivate, ActionObject.group).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.businessGroup));
	/** activity identifier: group has been reactivated (after inactivation or soft delete) * */
	public static final ILoggingAction GROUP_TRASHED = 
			new GroupLoggingAction(ActionType.admin, CrudAction.delete, ActionVerb.trash, ActionObject.group).setTypeList(
					new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.businessGroup));
	/** activity identitfyer: group has been deleted * */
	public static final ILoggingAction GROUP_DELETED = 
		new GroupLoggingAction(ActionType.admin, CrudAction.delete, ActionVerb.remove, ActionObject.group).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.businessGroup));
	
	
	
	/** activity identitfyer: new learning area has been created * */
	public static final ILoggingAction AREA_CREATED = 
		new GroupLoggingAction(ActionType.admin, CrudAction.create, ActionVerb.add, ActionObject.grouparea).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, StringResourceableType.bgArea));
	/** activity identitfyer: learning area has been deleted * */
	public static final ILoggingAction AREA_DELETED = 
		new GroupLoggingAction(ActionType.admin, CrudAction.delete, ActionVerb.remove, ActionObject.grouparea).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, StringResourceableType.bgArea));
	

	
	/**
	 * This static constructor's only use is to set the javaFieldIdForDebug
	 * on all of the LoggingActions defined in this class.
	 * <p>
	 * This is used to simplify debugging - as it allows to issue (technical) log
	 * statements where the name of the LoggingAction Field is written.
	 */
	static {
		Field[] fields = GroupLoggingAction.class.getDeclaredFields();
		if (fields!=null) {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.getType()==GroupLoggingAction.class) {
					try {
						GroupLoggingAction aLoggingAction = (GroupLoggingAction)field.get(null);
						aLoggingAction.setJavaFieldIdForDebug(field.getName());
					} catch (IllegalArgumentException | IllegalAccessException e) {
						log.error("", e);
					}
				}
			}
		}
	}
	
	/**
	 * Simple wrapper calling super<init>
	 * @see BaseLoggingAction#BaseLoggingAction(ActionType, CrudAction, ActionVerb, String)
	 */
	GroupLoggingAction(ActionType resourceActionType, CrudAction action, ActionVerb actionVerb, ActionObject actionObject) {
		super(resourceActionType, action, actionVerb, actionObject.name());
	}
	
}
