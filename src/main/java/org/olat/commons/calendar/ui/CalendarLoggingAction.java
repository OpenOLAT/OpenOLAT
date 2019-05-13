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

package org.olat.commons.calendar.ui;

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
 * 
 * Description:<br>
 * LoggingActions used by calendars.
 * <P>
 * PLEASE BE CAREFUL WHEN EDITING IN HERE.
 * <P>
 * Especially when modifying the ResourceableTypeList - which is a exercise
 * where we try to predict/document/define which ResourceableTypes will later on
 * - at runtime - be available to the IUserActivityLogger.log() method.
 * <P>
 * The names of the LoggingAction should be self-describing.
 * <P>
 * Initial Date:  17.06.2010 <br>
 * @author bja
 */
public class CalendarLoggingAction extends BaseLoggingAction {
	
	private static final Logger log = Tracing.createLoggerFor(CalendarLoggingAction.class);
	
	private static final ResourceableTypeList CALENDAR_READ_RESOURCES = new ResourceableTypeList().
	// this one is a calendar in a calendar-node in a course
	addMandatory(OlatResourceableType.course, OlatResourceableType.node, StringResourceableType.targetIdentity, OlatResourceableType.calendar).
	// this one is a course calendar
	or().addMandatory(OlatResourceableType.course, StringResourceableType.targetIdentity, OlatResourceableType.calendar).
	// this one is a group calendar
	or().addMandatory(OlatResourceableType.businessGroup, StringResourceableType.targetIdentity, OlatResourceableType.calendar).
	// this one is a personal calendar
	or().addMandatory(StringResourceableType.targetIdentity, StringResourceableType.calendar);
	
	public static final ILoggingAction CALENDAR_ENTRY_MODIFIED = new CalendarLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.edit,
			ActionObject.calendar).setTypeList(CALENDAR_READ_RESOURCES);
	public static final ILoggingAction CALENDAR_ENTRY_CREATED = new CalendarLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.add,
			ActionObject.calendar).setTypeList(CALENDAR_READ_RESOURCES);


	/**
	 * This static constructor's only use is to set the javaFieldIdForDebug on all
	 * of the LoggingActions defined in this class.
	 * <p>
	 * This is used to simplify debugging - as it allows to issue (technical) log
	 * statements where the name of the LoggingAction Field is written.
	 */
	static {
		Field[] fields = CalendarLoggingAction.class.getDeclaredFields();
		if (fields != null) {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.getType() == CalendarLoggingAction.class) {
					try {
						CalendarLoggingAction aLoggingAction = (CalendarLoggingAction) field.get(null);
						aLoggingAction.setJavaFieldIdForDebug(field.getName());
					} catch (IllegalArgumentException | IllegalAccessException e) {
						log.error("", e);
					}
				}
			}
		}
	}

	CalendarLoggingAction(ActionType resourceActionType, CrudAction action, ActionVerb actionVerb, ActionObject actionObject) {
		super(resourceActionType, action, actionVerb, actionObject.name());
	}
	
}
