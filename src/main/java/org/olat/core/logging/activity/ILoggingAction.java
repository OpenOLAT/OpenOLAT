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
 * An ILoggingAction is used to summarize all information required to
 * reflect an occurrance of logging in a particular place in a workflow.
 * <p>
 * The ILoggingAction therefore contains the following properties, which are
 * described here shortly:
 * <ul>
 *  <li>resourceAdminAction: specifies the visibility of this logging action
 *      later in the reporting: true for 'only visible to resource admins'
 *      false for 'visible for both resource admins and a normal user'
 *  <li>crudAction: this is a means to allow simplified SQL statements on the logging
 *      table later by specifying - at log-runtime-time - whether this logging action
 *      is about retrieving something, updating, deleting or creating.
 *      Not always can an action easily be associated to one of them - in which case
 *      it is best to default back to retrieve</li>
 *  <li>logMessage: the actual log message. Please follow the standard way of composing
 *      a log message - which is something like LEARNING_RESOURCE_OPEN</li>
 *  <li>typeListDefinition: this is an instance of ResourceableTypeList which defines
 *      what ILoggingResourceableTypes are expected and allowed to be stored to the
 *      DB along side this log message. It also serves as a way to document 
 *      possible occurrances</li>
 * </ul>
 * Furthermore there is a property called javaFieldIdForDebug - which is only used for
 * debugging (as the name suggests). In the default implementation (LoggingAction) 
 * it is set to the actual java filed name - allowing quicker debugging.
 * <p>
 * Initial Date:  20.10.2009 <br>
 * @author Stefan
 */
public interface ILoggingAction {

	/**
	 * Returns whether this logging action is only visible to a resource admin
	 * (true) or also to a user (false)
	 * @return whether this logging action is only for the eyes of 
	 * a resource admin (true) or also for users (false)
	 */
	public ActionType getResourceActionType();

	/**
	 * Returns the type of action this logging action represents -
	 * which is one of Create, Retrieve, Update or Delete.
	 * @return the crudAction of this logging action
	 */
	CrudAction getCrudAction();

	/**
	 * Returns the actionVerb of this logging action.
	 * <p>
	 * @return the actionVerb of this logging action
	 */
	ActionVerb getActionVerb();
	
	/**
	 * Returns the actionObject of this logging action.
	 * <p>
	 * Note that this is not an enum like ActionVerb
	 * since it is distributed amongst the ILoggingAction
	 * implementors - who themselves are very encouraged
	 * to use an enum underneath though and use name() on 
	 * that!
	 * <p>
	 * @return the actionObject of this logging action
	 */
	String getActionObject();
	
	/**
	 * Returns the ResourceableTypeList - which is a list
	 * of ILoggingResourceableTypes defining what is expected
	 * to be stored to the DB alongside this logging action
	 * @return the ResourceableTypeList which defines what
	 * is expected to be stored alongside this logging action
	 */
	ResourceableTypeList getTypeListDefinition();

	/**
	 * This is for DEBUG only - it is usually mapped to the
	 * name of the static field and issued in technical logging
	 * when the ResourceableType-Check fails
	 * @return the name of the (static) java filed which holds
	 * this logging action - for DEBUG only, so don't worry
	 */
	String getJavaFieldIdForDebug();

}
