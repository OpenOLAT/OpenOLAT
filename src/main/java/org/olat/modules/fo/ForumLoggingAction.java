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

package org.olat.modules.fo;

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

/**
 * LoggingActions used by the Forum
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
public class ForumLoggingAction extends BaseLoggingAction {
	
	private static final Logger log = Tracing.createLoggerFor(ForumLoggingAction.class);

	public static final ILoggingAction FORUM_MESSAGE_LIST = 
		new ForumLoggingAction(ActionType.tracking, CrudAction.retrieve, ActionVerb.view, ActionObject.forumthread).setTypeList(
				new ResourceableTypeList().
				// this one is the a forum-node in a course
				addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.forum).
				// this one is a forum in a standalone wiki
					or().addMandatory(OlatResourceableType.genRepoEntry, OlatResourceableType.forum).
					// this one is the wiki-forum in a group
					or().addMandatory(OlatResourceableType.businessGroup, OlatResourceableType.wiki, OlatResourceableType.forum));

	private static final ResourceableTypeList FORUM_THREAD_READ_RESOURCES = 
			new ResourceableTypeList().
				// this one is a message in a forum-node in a course
				addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.forum, OlatResourceableType.forumMessage).
				// this one is a message in a forum of a standalone wiki
				or().addMandatory(OlatResourceableType.genRepoEntry, OlatResourceableType.forum, OlatResourceableType.forumMessage);

	public static final ILoggingAction FORUM_THREAD_READ = 
		new ForumLoggingAction(ActionType.statistic, CrudAction.retrieve, ActionVerb.view, ActionObject.forummessage).setTypeList(FORUM_THREAD_READ_RESOURCES);
	
	public static final ILoggingAction FORUM_THREAD_HIDE = 
		new ForumLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.hide, ActionObject.forumthread).setTypeList(FORUM_THREAD_READ_RESOURCES);
	
	public static final ILoggingAction FORUM_THREAD_SHOW = 
		new ForumLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.view, ActionObject.forumthread).setTypeList(FORUM_THREAD_READ_RESOURCES);
	
	public static final ILoggingAction FORUM_THREAD_CLOSE = 
		new ForumLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.close, ActionObject.forumthread).setTypeList(FORUM_THREAD_READ_RESOURCES);
	
	public static final ILoggingAction FORUM_THREAD_REOPEN = 
		new ForumLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.open, ActionObject.forumthread).setTypeList(FORUM_THREAD_READ_RESOURCES);
	
	public static final ILoggingAction FORUM_THREAD_SPLIT = 
		new ForumLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.edit, ActionObject.forumthread).
		setTypeList(FORUM_THREAD_READ_RESOURCES);
	
	public static final ILoggingAction FORUM_THREAD_DELETE = 
		new ForumLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.remove, ActionObject.forumthread).setTypeList(FORUM_THREAD_READ_RESOURCES);

	public static final ILoggingAction FORUM_MESSAGE_DELETE = 
		new ForumLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.remove, ActionObject.forummessage).setTypeList(FORUM_THREAD_READ_RESOURCES);
	
	public static final ILoggingAction FORUM_MESSAGE_EDIT = 
		new ForumLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.edit, ActionObject.forummessage).setTypeList(FORUM_THREAD_READ_RESOURCES);
	
	public static final ILoggingAction FORUM_MESSAGE_CREATE = 
		new ForumLoggingAction(ActionType.statistic, CrudAction.update, ActionVerb.add, ActionObject.forummessage).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.forum).
				or().addMandatory(OlatResourceableType.wiki, OlatResourceableType.forum));
	
	public static final ILoggingAction FORUM_REPLY_MESSAGE_CREATE = 
		new ForumLoggingAction(ActionType.statistic, CrudAction.update, ActionVerb.add, ActionObject.forummessage).setTypeList(FORUM_THREAD_READ_RESOURCES);

	public static final ILoggingAction FORUM_MESSAGE_READ = 
		new ForumLoggingAction(ActionType.statistic, CrudAction.retrieve, ActionVerb.view, ActionObject.forummessage).setTypeList(FORUM_THREAD_READ_RESOURCES);
	
	public static final ILoggingAction FORUM_MESSAGE_MOVE = 
		new ForumLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.move, ActionObject.forummessage).
		setTypeList(FORUM_THREAD_READ_RESOURCES);
	
	/**
	 * This static constructor's only use is to set the javaFieldIdForDebug
	 * on all of the LoggingActions defined in this class.
	 * <p>
	 * This is used to simplify debugging - as it allows to issue (technical) log
	 * statements where the name of the LoggingAction Field is written.
	 */
	static {
		Field[] fields = ForumLoggingAction.class.getDeclaredFields();
		if (fields!=null) {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.getType()==ForumLoggingAction.class) {
					try {
						ForumLoggingAction aLoggingAction = (ForumLoggingAction)field.get(null);
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
	ForumLoggingAction(ActionType resourceActionType, CrudAction action, ActionVerb actionVerb, ActionObject actionObject) {
		super(resourceActionType, action, actionVerb, actionObject.name());
	}
	
}
