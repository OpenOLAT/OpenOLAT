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

package org.olat.modules.portfolio;

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
 * Initial date: 30.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class  PortfolioLoggingAction extends BaseLoggingAction {
	
	private static final Logger log = Tracing.createLoggerFor(PortfolioLoggingAction.class);

	/**
	 * Allow any resourceable type before portfolio [*][*]...[portfolio]
	 */
	private static final ResourceableTypeList RESOURCEABLE_LIST = 
		new ResourceableTypeList()
	   .addMandatory(StringResourceableType.anyBefore, OlatResourceableType.portfolio);

	public static final ILoggingAction PORTFOLIO_MEDIA_ADDED = 
		new PortfolioLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.add, ActionObject.portfoliomedia).setTypeList(RESOURCEABLE_LIST);
	public static final ILoggingAction PORTFOLIO_MEDIA_REMOVED = 
		new PortfolioLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.portfoliomedia).setTypeList(RESOURCEABLE_LIST);
	public static final ILoggingAction PORTFOLIO_MEDIA_SELECTED = 
		new PortfolioLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.perform, ActionObject.portfoliomedia).setTypeList(RESOURCEABLE_LIST);
	
	
	public static final ILoggingAction PORTFOLIO_TASK_STARTED = 
		new PortfolioLoggingAction(ActionType.admin, CrudAction.retrieve, ActionVerb.open, ActionObject.portfoliotask).setTypeList(RESOURCEABLE_LIST);
	public static final ILoggingAction PORTFOLIO_TASK_FINISHED = 
		new PortfolioLoggingAction(ActionType.admin, CrudAction.create, ActionVerb.perform, ActionObject.portfoliotask).setTypeList(RESOURCEABLE_LIST);

	
	public static final ILoggingAction PORTFOLIO_ASSIGNMENT_STARTED = 
			new PortfolioLoggingAction(ActionType.admin, CrudAction.retrieve, ActionVerb.open, ActionObject.portfoliotask).setTypeList(RESOURCEABLE_LIST);

	public static final ILoggingAction PORTFOLIO_SECTION_CLOSE = 
			new PortfolioLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.close, ActionObject.portfoliosection).setTypeList(RESOURCEABLE_LIST);
	public static final ILoggingAction PORTFOLIO_SECTION_REOPEN = 
			new PortfolioLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.reopen, ActionObject.portfoliosection).setTypeList(RESOURCEABLE_LIST);

	
	public static final ILoggingAction PORTFOLIO_BINDER_CREATED = 
		new PortfolioLoggingAction(ActionType.admin, CrudAction.create, ActionVerb.add, ActionObject.portfoliomap).setTypeList(RESOURCEABLE_LIST);
	public static final ILoggingAction PORTFOLIO_BINDER_REMOVED = 
		new PortfolioLoggingAction(ActionType.admin, CrudAction.update, ActionVerb.remove, ActionObject.portfoliomap).setTypeList(RESOURCEABLE_LIST);

	
	
	
	/**
	 * This static constructor's only use is to set the javaFieldIdForDebug
	 * on all of the LoggingActions defined in this class.
	 * <p>
	 * This is used to simplify debugging - as it allows to issue (technical) log
	 * statements where the name of the LoggingAction Field is written.
	 */
	static {
		Field[] fields = PortfolioLoggingAction.class.getDeclaredFields();
		if (fields!=null) {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.getType()==PortfolioLoggingAction.class) {
					try {
						PortfolioLoggingAction aLoggingAction = (PortfolioLoggingAction)field.get(null);
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
	PortfolioLoggingAction(ActionType resourceActionType, CrudAction action, ActionVerb actionVerb, ActionObject actionObject) {
		super(resourceActionType, action, actionVerb, actionObject.name());
	}
	
}
