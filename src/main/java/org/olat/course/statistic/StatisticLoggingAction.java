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
package org.olat.course.statistic;

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

public class StatisticLoggingAction extends BaseLoggingAction {
	
	private static final Logger log = Tracing.createLoggerFor(StatisticLoggingAction.class);

	public static final ILoggingAction VIEW_NODE_STATISTIC = 
		new StatisticLoggingAction(ActionType.admin, CrudAction.retrieve, ActionVerb.view, ActionObject.statistic).
		setTypeList(new ResourceableTypeList().
				addMandatory(OlatResourceableType.course, StringResourceableType.statisticManager, StringResourceableType.statisticType, OlatResourceableType.node));

	public static final ILoggingAction VIEW_TOTAL_OF_NODES_STATISTIC = 
		new StatisticLoggingAction(ActionType.admin, CrudAction.retrieve, ActionVerb.view, ActionObject.statistic).
		setTypeList(new ResourceableTypeList().
				addMandatory(OlatResourceableType.course, StringResourceableType.statisticManager, StringResourceableType.statisticType));

	public static final ILoggingAction VIEW_TOTAL_BY_VALUE_STATISTIC = 
		new StatisticLoggingAction(ActionType.admin, CrudAction.retrieve, ActionVerb.view, ActionObject.statistic).
			setTypeList(new ResourceableTypeList().
					addMandatory(OlatResourceableType.course, StringResourceableType.statisticManager, StringResourceableType.statisticType, StringResourceableType.statisticColumn));

	public static final ILoggingAction VIEW_TOTAL_TOTAL_STATISTIC = 
		new StatisticLoggingAction(ActionType.admin, CrudAction.retrieve, ActionVerb.view, ActionObject.statistic).
			setTypeList(new ResourceableTypeList().
					addMandatory(OlatResourceableType.course, StringResourceableType.statisticManager, StringResourceableType.statisticType));


	/**
	 * This static constructor's only use is to set the javaFieldIdForDebug
	 * on all of the LoggingActions defined in this class.
	 * <p>
	 * This is used to simplify debugging - as it allows to issue (technical) log
	 * statements where the name of the LoggingAction Field is written.
	 */
	static {
		Field[] fields = StatisticLoggingAction.class.getDeclaredFields();
		if (fields!=null) {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.getType()==StatisticLoggingAction.class) {
					try {
						StatisticLoggingAction aLoggingAction = (StatisticLoggingAction)field.get(null);
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
	StatisticLoggingAction(ActionType resourceActionType, CrudAction action, ActionVerb actionVerb, ActionObject actionObject) {
		super(resourceActionType, action, actionVerb, actionObject.name());
	}

}
