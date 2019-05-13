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

package org.olat.core.commons.modules.bc;

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
 * LoggingActions around Folders
 * <P>
 * PLEASE BE CAREFUL WHEN EDITING IN HERE.
 * <p>
 * Especially when modifying the ResourceableTypeList - which is 
 * a exercise where we try to predict/document/define which ResourceableTypes
 * will later on - at runtime - be available to the IUserActivityLogger.log() method.
 * <p>
 * The names of the LoggingAction should be self-describing.
 * <p>
 * Initial Date:  23.11.2009 <br>
 * @author bja
 */
public class FolderLoggingAction extends BaseLoggingAction {
	
	private static final Logger log = Tracing.createLoggerFor(FolderLoggingAction.class);
	
	public static final ILoggingAction FILE_CREATE = new FolderLoggingAction(
			ActionType.tracking, CrudAction.create, ActionVerb.add,
			ActionObject.file).setTypeList(new ResourceableTypeList()
			.addMandatory(StringResourceableType.bcFile).or().addMandatory(
					OlatResourceableType.course, OlatResourceableType.node,
					StringResourceableType.bcFile));
	
	public static final ILoggingAction FILE_UPLOADED = new FolderLoggingAction(
			ActionType.tracking, CrudAction.create, ActionVerb.add,
			ActionObject.file).setTypeList(new ResourceableTypeList()
			.addMandatory(StringResourceableType.uploadFile).or().addMandatory(
					OlatResourceableType.course, OlatResourceableType.node,
					StringResourceableType.bcFile));

	public static final ILoggingAction FOLDER_CREATE = new FolderLoggingAction(
			ActionType.tracking, CrudAction.create, ActionVerb.add,
			ActionObject.folder).setTypeList(new ResourceableTypeList()
			.addMandatory(StringResourceableType.bcFile).or().addMandatory(
					OlatResourceableType.course, OlatResourceableType.node,
					StringResourceableType.bcFile));

	public static final ILoggingAction FILE_DELETE = new FolderLoggingAction(
			ActionType.tracking, CrudAction.delete, ActionVerb.remove,
			ActionObject.file).setTypeList(new ResourceableTypeList()
			.addMandatory(StringResourceableType.bcFile).or().addMandatory(
					OlatResourceableType.course, OlatResourceableType.node,
					StringResourceableType.bcFile));

	public static final ILoggingAction FILE_EDIT = new FolderLoggingAction(
			ActionType.tracking, CrudAction.update, ActionVerb.edit,
			ActionObject.file).setTypeList(new ResourceableTypeList()
			.addMandatory(StringResourceableType.bcFile).or().addMandatory(
					OlatResourceableType.course, OlatResourceableType.node,
					StringResourceableType.bcFile));

	public static final ILoggingAction EDIT_QUOTA = new FolderLoggingAction(
			ActionType.tracking, CrudAction.update, ActionVerb.edit,
			ActionObject.quota)
			.setTypeList(new ResourceableTypeList().addOptional(
					OlatResourceableType.course, OlatResourceableType.node));

	public static final ILoggingAction BC_FILE_READ = new FolderLoggingAction(
			ActionType.statistic, CrudAction.retrieve, ActionVerb.open,
			ActionObject.file).setTypeList(new ResourceableTypeList()
			// optional genRepoEntry and businessGroup added as seen on live, e.g.:
			// at org.olat.core.commons.modules.bc.commands.CmdServeResource.execute(CmdServeResource.java:128) at org.olat.core.commons.modules.bc.FolderRunController.event(FolderRunController.java:287) at org.olat.core.gui.control.DefaultController.dispatchEvent(DefaultController.java:195) at org.olat.core.gui.components.Component$1.run(Component.java:167) at org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller.runWithUserActivityLogger(ThreadLocalUserActivityLoggerInstaller.java:84) at org.olat.core.gui.components.Component.fireEvent(Component.java:164) at org.olat.core.commons.modules.bc.components.FolderComponent.doDispatchRequest(FolderComponent.java:114) at org.olat.core.gui.components.Component.dispatchRequest(Component.java:124) at org.olat.core.gui.components.Window.doDispatchToComponent(Window.java:1136)
			.addMandatory(StringResourceableType.bcFile).or().addMandatory(
					OlatResourceableType.course, StringResourceableType.bcFile).addOptional(OlatResourceableType.genRepoEntry,OlatResourceableType.businessGroup)
			.or().addMandatory(OlatResourceableType.course,
					OlatResourceableType.node, StringResourceableType.bcFile).addOptional(OlatResourceableType.genRepoEntry,OlatResourceableType.businessGroup));
	
	public static final ILoggingAction BC_FOLDER_READ = new FolderLoggingAction(
			ActionType.statistic, CrudAction.retrieve, ActionVerb.open,
			ActionObject.folder).setTypeList(new ResourceableTypeList()
			// optional genRepoEntry and businessGroup added as seen on live, e.g.:
			// at org.olat.core.commons.modules.bc.commands.CmdServeResource.execute(CmdServeResource.java:128) at org.olat.core.commons.modules.bc.FolderRunController.event(FolderRunController.java:287) at org.olat.core.gui.control.DefaultController.dispatchEvent(DefaultController.java:195) at org.olat.core.gui.components.Component$1.run(Component.java:167) at org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller.runWithUserActivityLogger(ThreadLocalUserActivityLoggerInstaller.java:84) at org.olat.core.gui.components.Component.fireEvent(Component.java:164) at org.olat.core.commons.modules.bc.components.FolderComponent.doDispatchRequest(FolderComponent.java:114) at org.olat.core.gui.components.Component.dispatchRequest(Component.java:124) at org.olat.core.gui.components.Window.doDispatchToComponent(Window.java:1136)
			.addMandatory(StringResourceableType.bcFile).or().addMandatory(
					OlatResourceableType.course, StringResourceableType.bcFile).addOptional(OlatResourceableType.genRepoEntry,OlatResourceableType.businessGroup)
			.or().addMandatory(OlatResourceableType.course,
					OlatResourceableType.node, StringResourceableType.bcFile).addOptional(OlatResourceableType.genRepoEntry,OlatResourceableType.businessGroup));
		
	public static final ILoggingAction FILE_COPIED = new FolderLoggingAction(
			ActionType.tracking, CrudAction.create, ActionVerb.copy,
			ActionObject.file).setTypeList(new ResourceableTypeList()
			.addMandatory(StringResourceableType.bcFile, StringResourceableType.bcFile).or().addMandatory(
					OlatResourceableType.course, OlatResourceableType.node,
					StringResourceableType.bcFile, StringResourceableType.bcFile));
	
	public static final ILoggingAction FILE_MOVED = new FolderLoggingAction(
			ActionType.tracking, CrudAction.create, ActionVerb.move,
			ActionObject.file).setTypeList(new ResourceableTypeList()
			.addMandatory(StringResourceableType.bcFile, StringResourceableType.bcFile).or().addMandatory(
					OlatResourceableType.course, OlatResourceableType.node,
					StringResourceableType.bcFile, StringResourceableType.bcFile));

	/**
	 * This static constructor's only use is to set the javaFieldIdForDebug
	 * on all of the LoggingActions defined in this class.
	 * <p>
	 * This is used to simplify debugging - as it allows to issue (technical) log
	 * statements where the name of the LoggingAction Field is written.
	 */
	static {
		Field[] fields = FolderLoggingAction.class.getDeclaredFields();
		if (fields!=null) {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.getType()==FolderLoggingAction.class) {
					try {
						FolderLoggingAction aLoggingAction = (FolderLoggingAction)field.get(null);
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
	FolderLoggingAction(ActionType resourceActionType, CrudAction action, ActionVerb actionVerb, ActionObject actionObject) {
		super(resourceActionType, action, actionVerb, actionObject.name());
	}
	
}

