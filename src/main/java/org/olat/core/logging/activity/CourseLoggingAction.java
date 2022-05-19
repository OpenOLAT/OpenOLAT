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
 * LoggingActions around Course - such as course browsing, but also course editor
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
public class CourseLoggingAction extends BaseLoggingAction {
	
	private static final Logger log = Tracing.createLoggerFor(CourseLoggingAction.class);

	public static final ILoggingAction COURSE_BROWSE_GOTO_NODE = 
		new CourseLoggingAction(ActionType.statistic, CrudAction.retrieve, ActionVerb.open, ActionObject.gotonode);
	
	public static final ILoggingAction NODE_SINGLEPAGE_GET_FILE = 
		new CourseLoggingAction(ActionType.statistic, CrudAction.retrieve, ActionVerb.open, ActionObject.spgetfile);

	public static final ILoggingAction CP_GET_FILE = 
		new CourseLoggingAction(ActionType.statistic, CrudAction.retrieve, ActionVerb.open, ActionObject.cpgetfile).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.node, StringResourceableType.cpNode).
					or().addMandatory(OlatResourceableType.cp, StringResourceableType.cpNode));

	public static final ILoggingAction ST_GOTO_NODE = 
		new CourseLoggingAction(ActionType.statistic, CrudAction.retrieve, ActionVerb.open, ActionObject.gotonode).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.node, StringResourceableType.nodeId).
					or().addMandatory(OlatResourceableType.genRepoEntry,OlatResourceableType.node,OlatResourceableType.businessGroup,OlatResourceableType.course,StringResourceableType.nodeId));
	
	public static final ILoggingAction FILE_UPLOADED = 
		new CourseLoggingAction(ActionType.statistic, CrudAction.create, ActionVerb.add, ActionObject.file).
			setTypeList(new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.node, StringResourceableType.uploadFile));

	public static final ILoggingAction DIALOG_ELEMENT_FILE_UPLOADED = 
		new CourseLoggingAction(ActionType.admin, CrudAction.create, ActionVerb.add, ActionObject.file).
		setTypeList(new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.node, StringResourceableType.uploadFile));

	public static final ILoggingAction DIALOG_ELEMENT_FILE_DELETED = 
		new CourseLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.node).
		setTypeList(new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.node, StringResourceableType.uploadFile));
	
	public static final ILoggingAction DIALOG_ELEMENT_FILE_DOWNLOADED = 
		new CourseLoggingAction(ActionType.admin, CrudAction.retrieve, ActionVerb.view, ActionObject.file).
		setTypeList(new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.node, StringResourceableType.uploadFile));
	
	public static final ILoggingAction CHECKLIST_ELEMENT_CHECKPOINT_UPDATED = 
		new CourseLoggingAction(ActionType.statistic, CrudAction.update, ActionVerb.edit, ActionObject.checkpoint).
		setTypeList(new ResourceableTypeList().addMandatory(OlatResourceableType.course, 
				OlatResourceableType.node, StringResourceableType.checklist, StringResourceableType.checkpoint));
	
	public static final ILoggingAction CHECKLIST_CHECK_UPDATED = 
			new CourseLoggingAction(ActionType.statistic, CrudAction.update, ActionVerb.edit, ActionObject.check).
			setTypeList(new ResourceableTypeList().addMandatory(OlatResourceableType.course, 
					OlatResourceableType.node, StringResourceableType.checkbox));
	
	public static final ILoggingAction CHECKLIST_CHECKBOX_CREATED = 
			new CourseLoggingAction(ActionType.statistic, CrudAction.create, ActionVerb.add, ActionObject.checkbox).
			setTypeList(new ResourceableTypeList().addMandatory(OlatResourceableType.course, 
					OlatResourceableType.node, StringResourceableType.checkbox));
	
	public static final ILoggingAction CHECKLIST_CHECKBOX_UPDATED = 
			new CourseLoggingAction(ActionType.statistic, CrudAction.update, ActionVerb.edit, ActionObject.checkbox).
			setTypeList(new ResourceableTypeList().addMandatory(OlatResourceableType.course, 
					OlatResourceableType.node, StringResourceableType.checkbox));
	
	public static final ILoggingAction CHECKLIST_CHECKBOX_DELETED = 
			new CourseLoggingAction(ActionType.statistic, CrudAction.delete, ActionVerb.remove, ActionObject.checkbox).
			setTypeList(new ResourceableTypeList().addMandatory(OlatResourceableType.course, 
					OlatResourceableType.node, StringResourceableType.checkbox));			
	
	public static final ILoggingAction INFO_MESSAGE_CREATED = 
			new CourseLoggingAction(ActionType.statistic, CrudAction.create, ActionVerb.add, ActionObject.infomessage).
			setTypeList(new ResourceableTypeList().addMandatory(OlatResourceableType.course, 
					OlatResourceableType.node, OlatResourceableType.infoMessage));
	
	public static final ILoggingAction INFO_MESSAGE_UPDATED = 
			new CourseLoggingAction(ActionType.statistic, CrudAction.update, ActionVerb.edit, ActionObject.infomessage).
			setTypeList(new ResourceableTypeList().addMandatory(OlatResourceableType.course, 
					OlatResourceableType.node, OlatResourceableType.infoMessage));
	
	public static final ILoggingAction INFO_MESSAGE_DELETED = 
			new CourseLoggingAction(ActionType.statistic, CrudAction.delete, ActionVerb.remove, ActionObject.infomessage).
			setTypeList(new ResourceableTypeList().addMandatory(OlatResourceableType.course, 
					OlatResourceableType.node, OlatResourceableType.infoMessage));
	
	public static final ILoggingAction COURSE_ENTERING = 
		new CourseLoggingAction(ActionType.statistic, CrudAction.retrieve, ActionVerb.launch, ActionObject.course).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course)
					.addOptional(OlatResourceableType.node, StringResourceableType.targetIdentity));
	
	public static final ILoggingAction COURSE_LEAVING = 
		new CourseLoggingAction(ActionType.statistic, CrudAction.exit, ActionVerb.exit, ActionObject.course).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course).
					or().addMandatory(OlatResourceableType.course).addOptional(OlatResourceableType.node).
					or().addMandatory(OlatResourceableType.course, OlatResourceableType.genRepoEntry).addOptional(OlatResourceableType.businessGroup).
					or().addMandatory(OlatResourceableType.genRepoEntry, StringResourceableType.targetIdentity).addOptional(OlatResourceableType.businessGroup).addOptional(OlatResourceableType.sharedFolder).addOptional(OlatResourceableType.course).
					or().addMandatory(OlatResourceableType.course, StringResourceableType.targetIdentity));
	
	public static final ILoggingAction COURSE_NAVIGATION_NODE_ACCESS = 
		new CourseLoggingAction(ActionType.statistic, CrudAction.retrieve, ActionVerb.launch, ActionObject.node).setTypeList(
				// the first one represents the normal course navigation
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.node).addOptional(OlatResourceableType.businessGroup).
				// the second one is navigating in a CP
					or().addMandatory(OlatResourceableType.course, OlatResourceableType.node, StringResourceableType.cpNode).addOptional(OlatResourceableType.businessGroup).
					// the third one is the wiki in a course case
					or().addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.wiki).addOptional(OlatResourceableType.businessGroup).
					// the fourth one is navigating in a forum
					or().addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.forum).addOptional(OlatResourceableType.businessGroup).
					// OLAT-4653 & LogAnalyzer Pattern 386: LoggingAction reported an inconsistency
					or().addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.genRepoEntry).addOptional(OlatResourceableType.businessGroup));
	public static final ILoggingAction COURSE_NAVIGATION_NODE_NO_ACCESS = 
		new CourseLoggingAction(ActionType.statistic, CrudAction.retrieve, ActionVerb.denied, ActionObject.node).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.node));
	
	public static final ILoggingAction COURSE_EDITOR_OPEN = 
		new CourseLoggingAction(ActionType.admin, CrudAction.retrieve, ActionVerb.launch, ActionObject.editor).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course)
						.addOptional(OlatResourceableType.node));
	
	public static final ILoggingAction COURSE_EDITOR_CLOSE = 
		new CourseLoggingAction(ActionType.admin, CrudAction.retrieve, ActionVerb.exit, ActionObject.editor).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course)
						.addOptional(OlatResourceableType.node));

	public static final ILoggingAction COURSE_EDITOR_NODE_MOVED = 
		new CourseLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.move, ActionObject.node).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.node));
	public static final ILoggingAction COURSE_EDITOR_NODE_COPIED = 
		new CourseLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.copy, ActionObject.node).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.node));
	public static final ILoggingAction COURSE_EDITOR_NODE_DELETED = 
		new CourseLoggingAction(ActionType.admin, CrudAction.delete, ActionVerb.remove, ActionObject.node).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.node));
	public static final ILoggingAction COURSE_EDITOR_NODE_EDITED = 
		new CourseLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.edit, ActionObject.node).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.node));
	public static final ILoggingAction COURSE_EDITOR_NODE_CREATED = 
		new CourseLoggingAction(ActionType.admin, CrudAction.create, ActionVerb.add, ActionObject.node).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.node));
	public static final ILoggingAction COURSE_EDITOR_NODE_RESTORED = 
		new CourseLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.node).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.node));
	
	public static final ILoggingAction COURSE_EDITOR_PUBLISHED = 
		new CourseLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.edit, ActionObject.publisher).setTypeList(
				new ResourceableTypeList().addMandatory(OlatResourceableType.course, OlatResourceableType.node)
						.addOptional(OlatResourceableType.node));
	
	
	public static final ILoggingAction COURES_DISCLAIMER_ACCEPTED = 
			new CourseLoggingAction(ActionType.tracking, CrudAction.create, ActionVerb.add, ActionObject.disclaimerConsent).setTypeList(
			new ResourceableTypeList().addMandatory(OlatResourceableType.course));
	public static final ILoggingAction COURES_DISCLAIMER_REJECTED = 
			new CourseLoggingAction(ActionType.tracking, CrudAction.delete, ActionVerb.remove, ActionObject.disclaimerConsent).setTypeList(
			new ResourceableTypeList().addMandatory(OlatResourceableType.course));

	

	/**
	 * This static constructor's only use is to set the javaFieldIdForDebug
	 * on all of the LoggingActions defined in this class.
	 * <p>
	 * This is used to simplify debugging - as it allows to issue (technical) log
	 * statements where the name of the LoggingAction Field is written.
	 */
	static {
		Field[] fields = CourseLoggingAction.class.getDeclaredFields();
		if (fields!=null) {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.getType()==CourseLoggingAction.class) {
					try {
						CourseLoggingAction aLoggingAction = (CourseLoggingAction)field.get(null);
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
	CourseLoggingAction(ActionType resourceActionType, CrudAction action, ActionVerb actionVerb, ActionObject actionObject) {
		super(resourceActionType, action, actionVerb, actionObject.name());
	}
	
}
