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


/**
 * Base implementation for ILoggingAction - the recommended super class for
 * all implementations for ILoggingAction (but not mandatory)
 * <P>
 * Things this class implements:
 * <ul>
 *  <li>Contains the actual properties with setters and getters</li>
 *  <li>Contains a constructor which needs to be given the mandatory properties</li>
 *  <li>Note that typeList is mandatory yet is set later via a setTypeList call -
 *      this is due to the way the LoggingAction definition is done</li>
 * </ul>
 * Initial Date:  20.10.2009 <br>
 * @author Stefan
 */
public class BaseLoggingAction implements ILoggingAction {
	
	/** the resourceAdminAction property - directly propagates to the DB later **/
	private final ActionType resourceActionType_;
	
	/** the crudAction (create, retrieve, update, delete) action type - directly propagates
	 * to the DB later
	 */
	private final CrudAction action_;
	
	/** the verb of the actual logging action - one of the global enum ActionVerb **/
	private final ActionVerb actionVerb_;
	
	/** the object of the actual logging action - subclasses are encouraged to map an enum to this field **/
	private final String actionObject_;
	
	/** optional: name of the field in which this instance is held - for DEBUGging only **/
	private String javaId_;
	
	/** list of optional and mandatory ILoggingResourceableTypes for this logging action **/
	private ResourceableTypeList typeList_;
	
	/**
	 * Constructs a new LoggingAction class.
	 * <p>
	 * Note that this is usually done via a static field - i.e. instances of 
	 * LoggingActions are not recommended to be instanciated dynamically!
	 * 
	 * @param resourceAdminAction whether or not this logging action is for the eyes of
	 * an admin only (true) or also for users (false)
	 * @param action one of the four crudAction types
	 * @param logMessage the actual log message - should follow the default pattern (e.g. LEARNING_RESOURCE_OPEN)
	 */
	protected BaseLoggingAction(ActionType resourceActionType, CrudAction action, ActionVerb actionVerb, String actionObject) {
		resourceActionType_ = resourceActionType;
		if (action==null) {
			throw new IllegalArgumentException("action must not be null");
		}
		action_ = action;
		if (actionVerb==null) {
			throw new IllegalArgumentException("actionVerb must not be null");
		}
		actionVerb_ = actionVerb;
		if (actionObject==null || actionObject.length()==0) {
			throw new IllegalArgumentException("actionObject must not be null or empty");
		}
		actionObject_ = actionObject;
	}
	
	/**
	 * Sets the ResourceableTypeList for this logging action.
	 * <p>
	 * Note that this can only be set once and should not be null!
	 * Also note that even though this is a mandatory field it is not passed to via
	 * the constructor - this in order to simplify and beautify the way LoggingActions are created
	 * @param typeList not null ResourceableTypeList for this action
	 * @return this (to allow chaining)
	 */
	protected BaseLoggingAction setTypeList(ResourceableTypeList typeList) {
		if (typeList_!=null) {
			throw new IllegalStateException("typeList already set");
		} else if (typeList==null) {
			throw new IllegalArgumentException("typeList must not be null");
		}
		typeList_ = typeList;
		return this;
	}
	
	public ResourceableTypeList getTypeListDefinition() {
		return typeList_;
	}
	
	/** 
	 * Returns the resourceAdminAction property - directly propagates to the DB later
	 * @return the resourceAdminAction property - directly propagates to the DB later
	 **/
	public ActionType getResourceActionType() {
		return resourceActionType_;
	}

	/** 
	 * Returns the crudAction (create, retrieve, update, delete) action type - directly propagates
	 * to the DB later
	 * @return the crudAction (create, retrieve, update, delete) action type - directly propagates
	 * to the DB later
	 */
	public CrudAction getCrudAction() {
		return action_;
	}

	/** 
	 * Returns the actionVerb of the logging action
	 * @return the actionVerb of the logging action
	 **/ 
	public ActionVerb getActionVerb() {
		return actionVerb_;
	}
	
	/**
	 * Returns the actionObject of the logging action
	 * @return the actionObject of the logging action
	 */
	public String getActionObject() {
		return actionObject_;
	}
	
	/** 
	 * Returns the optional name of the field in which this instance is held - for DEBUGging only
	 * @return the optional name of the field in which this instance is held - for DEBUGging only
	 **/
	public String getJavaFieldIdForDebug() {
		return javaId_;
	}

	/** 
	 * Sets the optional name of the field in which this instance is held - for DEBUGging only -
	 * this field id will be issued when there are problems with this ILoggingAction's
	 * ResourceableTypeList later
	 * @param javaId the java field id - or anything else which helps in your DEBUGging case -
	 * this field id will be issued when there are problems with this ILoggingAction's
	 * ResourceableTypeList later
	 **/
	public void setJavaFieldIdForDebug(String javaId) {
		javaId_ = javaId;
	}
}
