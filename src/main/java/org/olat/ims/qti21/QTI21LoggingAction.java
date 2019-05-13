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

package org.olat.ims.qti21;

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
 * 
 * Initial date: 30.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21LoggingAction extends BaseLoggingAction {
	
	private static final Logger log = Tracing.createLoggerFor(QTI21LoggingAction.class);

	// the following is a user clicking within a test
	public static final ILoggingAction QTI_START_IN_COURSE = 
		new QTI21LoggingAction(ActionType.statistic, CrudAction.update, ActionVerb.launch, ActionObject.test).setTypeList(
				new ResourceableTypeList().
					addMandatory(OlatResourceableType.course, OlatResourceableType.node)
		);
	
	public static final ILoggingAction QTI_CLOSE_IN_COURSE = 
		new QTI21LoggingAction(ActionType.statistic, CrudAction.update, ActionVerb.close , ActionObject.test).setTypeList(
				new ResourceableTypeList().
					addMandatory(OlatResourceableType.course, OlatResourceableType.node)
		);
	
	public static final ILoggingAction QTI_RESET_IN_COURSE = 
			new QTI21LoggingAction(ActionType.statistic, CrudAction.update, ActionVerb.reset, ActionObject.test).setTypeList(
					new ResourceableTypeList().
						addMandatory(OlatResourceableType.course, OlatResourceableType.node)
			);
	
	public static final ILoggingAction QTI_REOPEN_IN_COURSE = 
			new QTI21LoggingAction(ActionType.statistic, CrudAction.update, ActionVerb.reopen , ActionObject.test).setTypeList(
					new ResourceableTypeList().
						addMandatory(OlatResourceableType.course, OlatResourceableType.node)
			);
	
	public static final ILoggingAction QTI_START_AS_RESOURCE = 
			new QTI21LoggingAction(ActionType.statistic, CrudAction.update, ActionVerb.launch, ActionObject.test).setTypeList(
					new ResourceableTypeList().
						addMandatory(OlatResourceableType.test)
			);
		
	public static final ILoggingAction QTI_CLOSE_AS_RESOURCE = 
		new QTI21LoggingAction(ActionType.statistic, CrudAction.update, ActionVerb.close , ActionObject.test).setTypeList(
				new ResourceableTypeList().
					addMandatory(OlatResourceableType.test)
		);
	
	public static final ILoggingAction QTI_EDIT_RESOURCE = 
			new QTI21LoggingAction(ActionType.statistic, CrudAction.update, ActionVerb.edit , ActionObject.test).setTypeList(
					new ResourceableTypeList().
						addMandatory(OlatResourceableType.test)
			);

	
	/**
	 * This static constructor's only use is to set the javaFieldIdForDebug
	 * on all of the LoggingActions defined in this class.
	 * <p>
	 * This is used to simplify debugging - as it allows to issue (technical) log
	 * statements where the name of the LoggingAction Field is written.
	 */
	static {
		Field[] fields = QTI21LoggingAction.class.getDeclaredFields();
		if (fields!=null) {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.getType()==QTI21LoggingAction.class) {
					try {
						QTI21LoggingAction aLoggingAction = (QTI21LoggingAction)field.get(null);
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
	QTI21LoggingAction(ActionType resourceActionType, CrudAction action, ActionVerb actionVerb, ActionObject actionObject) {
		super(resourceActionType, action, actionVerb, actionObject.name());
	}
	
}
