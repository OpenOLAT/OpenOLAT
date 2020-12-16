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

package org.olat.core.logging.activity;

import java.lang.reflect.Field;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * LoggingActions for generic stuff re Learning Resources/RepositoryEntries
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
public class LearningResourceLoggingAction extends BaseLoggingAction {
	
	private static final Logger log = Tracing.createLoggerFor(LearningResourceLoggingAction.class);

	private static final ResourceableTypeList LEARNING_RESOURCE_OPEN_CLOSE_LIST = 
		new ResourceableTypeList().
		addMandatory(OlatResourceableType.wiki).
		or().addMandatory(OlatResourceableType.businessGroup, OlatResourceableType.wiki).
		or().addMandatory(OlatResourceableType.genRepoEntry).
		// this is the case when you add/remove/open a shared folder
		or().addMandatory(OlatResourceableType.genRepoEntry, StringResourceableType.bcFile).
		or().addMandatory(StringResourceableType.scormResource).
		or().addMandatory(OlatResourceableType.cp, StringResourceableType.cpNode).
		or().addMandatory(OlatResourceableType.cp).
		// this is the case when you open the wiki as a course node
		or().addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.genRepoEntry).
		or().addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.wiki).
		// this is the case when you open a scorm resource as a course node
		or().addMandatory(OlatResourceableType.course, OlatResourceableType.node, StringResourceableType.scormResource).
		// this is the case when you open a QTI resource as a course node
		or().addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.iq).
		// this is another case of closing a resource
		or().addMandatory(OlatResourceableType.course, OlatResourceableType.node);
	public static final ILoggingAction LEARNING_RESOURCE_OPEN = 
		new LearningResourceLoggingAction(ActionType.statistic, CrudAction.retrieve, ActionVerb.open, ActionObject.resource).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction LEARNING_RESOURCE_CLOSE = 
		new LearningResourceLoggingAction(ActionType.statistic, CrudAction.exit, ActionVerb.close, ActionObject.resource).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction LEARNING_RESOURCE_CREATE = 
		new LearningResourceLoggingAction(ActionType.admin, CrudAction.create, ActionVerb.add, ActionObject.resource).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction LEARNING_RESOURCE_TRASH = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.delete, ActionVerb.trash, ActionObject.resource).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction LEARNING_RESOURCE_DEACTIVATE = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.delete, ActionVerb.deactivate, ActionObject.resource).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction LEARNING_RESOURCE_RESTORE = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.restore, ActionObject.resource).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction LEARNING_RESOURCE_DELETE = 
		new LearningResourceLoggingAction(ActionType.admin, CrudAction.delete, ActionVerb.remove, ActionObject.resource).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction LEARNING_RESOURCE_UPDATE = 
		new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.edit, ActionObject.resource).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES = 
		new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.edit, ActionObject.course).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_IM_ENABLED = 
		new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.chat).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_IM_DISABLED = 
		new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.chat).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_COURSESEARCH_ENABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.search).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_COURSESEARCH_DISABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.search).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_PARTICIPANTLIST_ENABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.participantList).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_PARTICIPANTLIST_DISABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.participantList).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_PARTICIPANTINFO_ENABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.infomessage).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_PARTICIPANTINFO_DISABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.infomessage).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_EMAIL_ENABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.mail).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_EMAIL_DISABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.mail).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_TEAMS_ENABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.teams).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_TEAMS_DISABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.teams).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_BIGBLUEBUTTON_ENABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.bigbluebutton).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_BIGBLUEBUTTON_DISABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.bigbluebutton).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_BLOG_ENABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.blog).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_BLOG_DISABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.blog).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_WIKI_ENABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.wiki).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_WIKI_DISABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.wiki).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_FORUM_ENABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.forum).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_FORUM_DISABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.forum).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_DOCUMENTS_ENABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.folder).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_DOCUMENTS_DISABLED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.folder).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_GLOSSARY_ENABLED = 
		new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.glossar).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_GLOSSARY_DISABLED = 
		new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.glossar).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_CALENDAR_ENABLED = 
		new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.calendar).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_CALENDAR_DISABLED = 
		new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.calendar).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_EFFICIENCY_STATEMENT_ENABLED = 
		new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.efficency).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_EFFICIENCY_STATEMENT_DISABLED = 
		new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.efficency).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_SHARED_FOLDER_REMOVED = 
		new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.sharedfolder).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_SHARED_FOLDER_ADDED = 
		new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.sharedfolder).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_COURSELAYOUT_DEFAULT_ADDED = 
		new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.layout).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_COURSELAYOUT_CUSTOM_ADDED = 
		new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.layout).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_COMPLETION_TYPE_NUMBEr_OF_NODES = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.completionType).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_COMPLETION_TYPE_DURATION = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.completionType).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	public static final ILoggingAction REPOSITORY_ENTRY_PROPERTIES_COMPLETION_TYPE_NONE = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.completionType).setTypeList(LEARNING_RESOURCE_OPEN_CLOSE_LIST);
	
	// lecture block
	public static final ILoggingAction LECTURE_BLOCK_CREATED = 
			new LearningResourceLoggingAction(ActionType.admin, CrudAction.create, ActionVerb.add, ActionObject.lectures).setTypeList(
					new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.lectureBlock));
	public static final ILoggingAction LECTURE_BLOCK_EDITED = 
			new CourseLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.edit, ActionObject.lectures).setTypeList(
					new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.lectureBlock));
	public static final ILoggingAction LECTURE_BLOCK_DELETED = 
			new CourseLoggingAction(ActionType.admin, CrudAction.delete, ActionVerb.remove, ActionObject.lectures).setTypeList(
					new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.lectureBlock));
	
	public static final ILoggingAction LECTURE_BLOCK_ROLL_CALL_STARTED = 
			new CourseLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.launch, ActionObject.lecturesRollcall).setTypeList(
					new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.lectureBlock));
	public static final ILoggingAction LECTURE_BLOCK_ROLL_CALL_CLOSED = 
			new CourseLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.close, ActionObject.lecturesRollcall).setTypeList(
					new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.lectureBlock));
	public static final ILoggingAction LECTURE_BLOCK_ROLL_CALL_CANCELLED = 
			new CourseLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.cancel, ActionObject.lecturesRollcall).setTypeList(
					new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.lectureBlock));
	public static final ILoggingAction LECTURE_BLOCK_ROLL_CALL_REOPENED = 
			new CourseLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.reopen, ActionObject.lecturesRollcall).setTypeList(
					new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.lectureBlock));

	/**
	 * This static constructor's only use is to set the javaFieldIdForDebug
	 * on all of the LoggingActions defined in this class.
	 * <p>
	 * This is used to simplify debugging - as it allows to issue (technical) log
	 * statements where the name of the LoggingAction Field is written.
	 */
	static {
		Field[] fields = LearningResourceLoggingAction.class.getDeclaredFields();
		if (fields!=null) {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.getType()==LearningResourceLoggingAction.class) {
					try {
						LearningResourceLoggingAction aLoggingAction = (LearningResourceLoggingAction)field.get(null);
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
	 * @see BaseLoggingAction#BaseLoggingAction(boolean, CrudAction, ActionVerb, String)
	 * @deprecated
	 */
	@Deprecated
	LearningResourceLoggingAction(ActionType resourceActionType, CrudAction action, ActionVerb actionVerb, String actionObject) {
		super(resourceActionType, action, actionVerb, actionObject);
	}
	
	/**
	 * Simple wrapper calling super<init>
	 * @see BaseLoggingAction#BaseLoggingAction(boolean, CrudAction, ActionVerb, ActionObject)
	 */
	LearningResourceLoggingAction(ActionType resourceActionType, CrudAction action, ActionVerb actionVerb, ActionObject actionObject) {
		super(resourceActionType, action, actionVerb, actionObject.name());
	}
	
}
