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

import org.olat.core.commons.persistence.PersistentObject;

/**
 * Hibernate class representing a <i>log line</i> - 
 * a row in the user activity logging table.
 * <p>
 * Initial Date:  20.10.2009 <br>
 * @author Stefan
 */
public class LoggingObject extends PersistentObject {
	private static final long serialVersionUID = -7960024949707705523L;

	// technical fields
	private String sourceClass;
	
	// session and user fields
	private String sessionId;
	private long userId;
	
	// action fields
	private String actionCrudType;
	private String actionVerb;
	private String actionObject;
	private Boolean resourceAdminAction;
	private long simpleDuration;
	
	// scope fields
	private String businessPath;
	private String greatGrandParentResType;
	private String greatGrandParentResId;
	private String greatGrandParentResName;
	private String grandParentResType;
	private String grandParentResId;
	private String grandParentResName;
	private String parentResType;
	private String parentResId;
	private String parentResName;
	private String targetResType;
	private String targetResId;
	private String targetResName;
	
	public LoggingObject(){
	// for hibernate	
	}
	
	@Override
	public String toString() {
		return "LoggingObject["+
			actionVerb+" "+actionObject+" by identity "+userId+" by class "+sourceClass+
			","+(resourceAdminAction ? "isAdminAction" : "isUserAction")+
			", businessPath="+businessPath+
			", greatGrandParentResType="+greatGrandParentResType+
			", greatGrandParentResId="+greatGrandParentResId+
			", greatGrandParentResName="+greatGrandParentResName+
			", grandParentResType="+grandParentResType+
			", grandParentResId="+grandParentResId+
			", grandParentResName="+grandParentResName+
			", parentResType="+parentResType+
			", parentResId="+parentResId+
			", parentResName="+parentResName+
			", targetResType="+targetResType+
			", targetResId="+targetResId+
			", targetResName="+targetResName;
	}
	
	/**
	 * Creates a new LoggingObject with a few of the mandatory fields passed to.
	 * <p>
	 * Note that this method does parameter validity checks - hence it
	 * may throw IllegalArgumentExceptions if it doesn't like your input.
	 * <p>
	 * @param sessionId the id of the session - which is directly stored to the database
	 * @param identityKey The identity primary key
	 * @param actionCrudType the crudAction type
	 * @param action - the actual log message
	 */
	public LoggingObject(String sessionId, Long identityKey, String actionCrudType, String actionVerb, String actionObject) {
		if (sessionId==null) {
			throw new IllegalArgumentException("sessionId must not be null");
		}
		if (identityKey==null || LogModule.isLogAnonymous()) {
			throw new IllegalArgumentException("identity key or name must not be null");
		}
		if (actionCrudType==null || actionCrudType.length()==0) {
			throw new IllegalArgumentException("action must not be null or empty");
		}
		if (actionCrudType.length()>1) {
			throw new IllegalArgumentException("actionCrudType must be of length 1");
		}
		if (actionVerb==null || actionVerb.length()==0) {
			throw new IllegalArgumentException("actionVerb must not be null or empty");
		}
		if (actionObject==null || actionObject.length()==0) {
			throw new IllegalArgumentException("actionObject must not be null or empty");
		}
		this.sessionId=sessionId;
		this.sourceClass=sourceClass==null ? null : sourceClass.getClass().getName();
		this.actionCrudType=actionCrudType;
		this.actionVerb=actionVerb;
		this.actionObject=actionObject;
		this.userId = identityKey;
	}

	/**
	 * Convenience method to set the three greatGrandParent
	 * resource properties
	 * @param the LoggingResourceable which should be stored 
	 * in the greatGrandParent field
	 */
	public void setGreatGrandParentResourceInfo(ILoggingResourceable r) {
		setGreatGrandParentResType(r.getType());
		setGreatGrandParentResId(r.getId());
		setGreatGrandParentResName(r.getName());
	}
	
	/**
	 * Convenience method to set the three grandParent
	 * resource properties
	 * @param the LoggingResourceable which should be stored 
	 * in the grandParent field
	 */
	public void setGrandParentResourceInfo(ILoggingResourceable r) {
		setGrandParentResType(r.getType());
		setGrandParentResId(r.getId());
		setGrandParentResName(r.getName());
	}
	
	/**
	 * Convenience method to set the three parent
	 * resource properties
	 * @param the LoggingResourceable which should be stored 
	 * in the parent field
	 */
	public void setParentResourceInfo(ILoggingResourceable r) {
		setParentResType(r.getType());
		setParentResId(r.getId());
		setParentResName(r.getName());
	}
	
	/**
	 * Convenience method to set the three target
	 * resource properties
	 * @param the LoggingResourceable which should be stored 
	 * in the targetResource field
	 */
	public void setTargetResourceInfo(ILoggingResourceable r) {
		setTargetResType(r.getType());
		setTargetResId(r.getId());
		setTargetResName(r.getName());
	}
	
//
// Following are generated setters and getters
//

	public final String getSourceClass() {
		return sourceClass;
	}

	public final void setSourceClass(String sourceClass) {
		this.sourceClass = sourceClass;
	}

	public final String getSessionId() {
		return sessionId;
	}

	public final void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public final long getUserId() {
		return userId;
	}

	public final void setUserId(long userId) {
		this.userId = userId;
	}

	public final String getActionCrudType() {
		return actionCrudType;
	}
	
	public final String getActionCrudTypeVerbose() {
		if ("c".equals(actionCrudType)) {
			return "create";
		} else if ("r".equals(actionCrudType)) {
			return "retrieve";
		} else if ("u".equals(actionCrudType)) {
			return "update";
		} else if ("d".equals(actionCrudType)) {
			return "delete";
		} else if ("e".equals(actionCrudType)) {
			return "exit";
		} else {
			// fallback, non verbose
			return actionCrudType;
		}
	}

	public final void setActionCrudType(String actionCrudType) {
		if (actionCrudType.length()>1) {
			throw new IllegalArgumentException("actionCrudType must be of length 1");
		}
		this.actionCrudType = actionCrudType;
	}

	public final String getActionVerb() {
		return actionVerb;
	}

	public final void setActionVerb(String actionverb) {
		this.actionVerb = actionverb;
	}

	public final String getActionObject() {
		return actionObject;
	}

	public final void setActionObject(String actionobject) {
		this.actionObject = actionobject;
	}

	public final Boolean getResourceAdminAction() {
		return resourceAdminAction;
	}

	public final void setResourceAdminAction(Boolean resourceAdminAction) {
		this.resourceAdminAction = resourceAdminAction;
	}

	public final long getSimpleDuration() {
		return simpleDuration;
	}

	public final void setSimpleDuration(long duration) {
		this.simpleDuration = duration;
	}

	public final String getBusinessPath() {
		return businessPath;
	}

	public final void setBusinessPath(String businessPath) {
		this.businessPath = businessPath;
	}

	public final String getGreatGrandParentResType() {
		return greatGrandParentResType;
	}

	public final void setGreatGrandParentResType(String greatGrandParentResType) {
		this.greatGrandParentResType = greatGrandParentResType;
	}

	public final String getGreatGrandParentResId() {
		return greatGrandParentResId;
	}

	public final void setGreatGrandParentResId(String greatGrandParentResId) {
		this.greatGrandParentResId = greatGrandParentResId;
	}

	public final String getGreatGrandParentResName() {
		return greatGrandParentResName;
	}

	public final void setGreatGrandParentResName(String greatGrandParentResName) {
		this.greatGrandParentResName = greatGrandParentResName;
	}

	public final String getGrandParentResType() {
		return grandParentResType;
	}

	public final void setGrandParentResType(String grandParentResType) {
		this.grandParentResType = grandParentResType;
	}

	public final String getGrandParentResId() {
		return grandParentResId;
	}

	public final void setGrandParentResId(String grandParentResId) {
		this.grandParentResId = grandParentResId;
	}

	public final String getGrandParentResName() {
		return grandParentResName;
	}

	public final void setGrandParentResName(String grandParentResName) {
		this.grandParentResName = grandParentResName;
	}

	public final String getParentResType() {
		return parentResType;
	}

	public final void setParentResType(String parentResType) {
		this.parentResType = parentResType;
	}

	public final String getParentResId() {
		return parentResId;
	}

	public final void setParentResId(String parentResId) {
		this.parentResId = parentResId;
	}

	public final String getParentResName() {
		return parentResName;
	}

	public final void setParentResName(String parentResName) {
		this.parentResName = parentResName;
	}

	public final String getTargetResType() {
		return targetResType;
	}

	public final void setTargetResType(String targetResType) {
		this.targetResType = targetResType;
	}

	public final String getTargetResId() {
		return targetResId;
	}

	public final void setTargetResId(String targetResId) {
		this.targetResId = targetResId;
	}

	public final String getTargetResName() {
		return targetResName;
	}

	public final void setTargetResName(String targetResName) {
		this.targetResName = targetResName;
	}
}
