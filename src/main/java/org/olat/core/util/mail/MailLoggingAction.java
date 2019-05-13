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

package org.olat.core.util.mail;

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
 * Description:<br>
 * LoggingActions used by mail formulars.
 * <P>
 * PLEASE BE CAREFUL WHEN EDITING IN HERE.
 * <P>
 * Especially when modifying the ResourceableTypeList - which is a exercise
 * where we try to predict/document/define which ResourceableTypes will later on
 * - at runtime - be available to the IUserActivityLogger.log() method.
 * <P>
 * The names of the LoggingAction should be self-describing.
 * <P>
 * Initial Date:  22.06.2010 <br>
 * @author bja
 */
public class MailLoggingAction extends BaseLoggingAction {
	
	private static final Logger log = Tracing.createLoggerFor(MailLoggingAction.class);

	private static final ResourceableTypeList MAIL_RESOURCES = new ResourceableTypeList().
	// this one is a mail formular in a contact-node in a course
	addMandatory(OlatResourceableType.course, OlatResourceableType.node).
	// this one is a mail formular in a business group
	or().addMandatory(OlatResourceableType.businessGroup, OlatResourceableType.course);
	
	public static final ILoggingAction MAIL_SENT = new MailLoggingAction(ActionType.tracking, CrudAction.retrieve, ActionVerb.perform,
			ActionObject.mail).setTypeList(MAIL_RESOURCES);
	
	/**
	 * This static constructor's only use is to set the javaFieldIdForDebug on all
	 * of the LoggingActions defined in this class.
	 * <p>
	 * This is used to simplify debugging - as it allows to issue (technical) log
	 * statements where the name of the LoggingAction Field is written.
	 */
	static {
		Field[] fields = MailLoggingAction.class.getDeclaredFields();
		if (fields != null) {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.getType() == MailLoggingAction.class) {
					try {
						MailLoggingAction aLoggingAction = (MailLoggingAction) field.get(null);
						aLoggingAction.setJavaFieldIdForDebug(field.getName());
					} catch (IllegalArgumentException | IllegalAccessException e) {
						log.error("", e);
					}
				}
			}
		}
	}
	
	protected MailLoggingAction(ActionType resourceActionType,
			CrudAction action, ActionVerb actionVerb, ActionObject actionObject) {
		super(resourceActionType, action, actionVerb, actionObject.name());
	}

}
