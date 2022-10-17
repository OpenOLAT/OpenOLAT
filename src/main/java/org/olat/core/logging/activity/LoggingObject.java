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

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;

/**
 * Hibernate class representing a <i>log line</i> - 
 * a row in the user activity logging table.
 * <p>
 * Initial Date:  20.10.2009 <br>
 * @author Stefan
 */
@Entity(name="loggingobject")
@Table(name="o_loggingtable")
public class LoggingObject implements CreateInfo, Persistable {
	private static final long serialVersionUID = -7960024949707705523L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="log_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	// technical fields
	@Column(name="sourceclass", nullable=true, insertable=true, updatable=true)
	private String sourceClass;
	
	// session and user fields
	@Column(name="sessionid", nullable=true, insertable=true, updatable=true)
	private String sessionId;
	@Column(name="user_id", nullable=true, insertable=true, updatable=true)
	private long userId;
	
	// action fields
	@Column(name="actioncrudtype", nullable=true, insertable=true, updatable=true)
	private String actionCrudType;
	@Column(name="actionverb", nullable=true, insertable=true, updatable=true)
	private String actionVerb;
	@Column(name="actionobject", nullable=true, insertable=true, updatable=true)
	private String actionObject;
	@Column(name="resourceadminaction", nullable=true, insertable=true, updatable=true)
	private Boolean resourceAdminAction;
	@Column(name="simpleduration", nullable=true, insertable=true, updatable=true)
	private long simpleDuration;
	
	// scope fields
	@Column(name="businesspath", nullable=true, insertable=true, updatable=true)
	private String businessPath;
	@Column(name="greatgrandparentrestype", nullable=true, insertable=true, updatable=true)
	private String greatGrandParentResType;
	@Column(name="greatgrandparentresid", nullable=true, insertable=true, updatable=true)
	private String greatGrandParentResId;
	@Column(name="greatgrandparentresname", nullable=true, insertable=true, updatable=true)
	private String greatGrandParentResName;
	@Column(name="grandparentrestype", nullable=true, insertable=true, updatable=true)
	private String grandParentResType;
	@Column(name="grandparentresid", nullable=true, insertable=true, updatable=true)
	private String grandParentResId;
	@Column(name="grandparentresname", nullable=true, insertable=true, updatable=true)
	private String grandParentResName;
	@Column(name="parentrestype", nullable=true, insertable=true, updatable=true)
	private String parentResType;
	@Column(name="parentresid", nullable=true, insertable=true, updatable=true)
	private String parentResId;
	@Column(name="parentresname", nullable=true, insertable=true, updatable=true)
	private String parentResName;
	@Column(name="targetrestype", nullable=true, insertable=true, updatable=true)
	private String targetResType;
	@Column(name="targetresid", nullable=true, insertable=true, updatable=true)
	private String targetResId;
	@Column(name="targetresname", nullable=true, insertable=true, updatable=true)
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

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}
	
	public String getSourceClass() {
		return sourceClass;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public void setSourceClass(String sourceClass) {
		this.sourceClass = sourceClass;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getActionCrudType() {
		return actionCrudType;
	}
	
	public String getActionCrudTypeVerbose() {
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

	public void setActionCrudType(String actionCrudType) {
		if (actionCrudType.length()>1) {
			throw new IllegalArgumentException("actionCrudType must be of length 1");
		}
		this.actionCrudType = actionCrudType;
	}

	public String getActionVerb() {
		return actionVerb;
	}

	public void setActionVerb(String actionverb) {
		this.actionVerb = actionverb;
	}

	public String getActionObject() {
		return actionObject;
	}

	public void setActionObject(String actionobject) {
		this.actionObject = actionobject;
	}

	public Boolean getResourceAdminAction() {
		return resourceAdminAction;
	}

	public void setResourceAdminAction(Boolean resourceAdminAction) {
		this.resourceAdminAction = resourceAdminAction;
	}

	public long getSimpleDuration() {
		return simpleDuration;
	}

	public void setSimpleDuration(long duration) {
		this.simpleDuration = duration;
	}

	public String getBusinessPath() {
		return businessPath;
	}

	public void setBusinessPath(String businessPath) {
		this.businessPath = businessPath;
	}

	public String getGreatGrandParentResType() {
		return greatGrandParentResType;
	}

	public void setGreatGrandParentResType(String greatGrandParentResType) {
		this.greatGrandParentResType = greatGrandParentResType;
	}

	public String getGreatGrandParentResId() {
		return greatGrandParentResId;
	}

	public void setGreatGrandParentResId(String greatGrandParentResId) {
		this.greatGrandParentResId = greatGrandParentResId;
	}

	public String getGreatGrandParentResName() {
		return greatGrandParentResName;
	}

	public void setGreatGrandParentResName(String greatGrandParentResName) {
		this.greatGrandParentResName = greatGrandParentResName;
	}

	public String getGrandParentResType() {
		return grandParentResType;
	}

	public void setGrandParentResType(String grandParentResType) {
		this.grandParentResType = grandParentResType;
	}

	public String getGrandParentResId() {
		return grandParentResId;
	}

	public void setGrandParentResId(String grandParentResId) {
		this.grandParentResId = grandParentResId;
	}

	public String getGrandParentResName() {
		return grandParentResName;
	}

	public void setGrandParentResName(String grandParentResName) {
		this.grandParentResName = grandParentResName;
	}

	public String getParentResType() {
		return parentResType;
	}

	public void setParentResType(String parentResType) {
		this.parentResType = parentResType;
	}

	public String getParentResId() {
		return parentResId;
	}

	public void setParentResId(String parentResId) {
		this.parentResId = parentResId;
	}

	public String getParentResName() {
		return parentResName;
	}

	public void setParentResName(String parentResName) {
		this.parentResName = parentResName;
	}

	public String getTargetResType() {
		return targetResType;
	}

	public void setTargetResType(String targetResType) {
		this.targetResType = targetResType;
	}

	public String getTargetResId() {
		return targetResId;
	}

	public void setTargetResId(String targetResId) {
		this.targetResId = targetResId;
	}

	public String getTargetResName() {
		return targetResName;
	}

	public void setTargetResName(String targetResName) {
		this.targetResName = targetResName;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? -9265 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof LoggingObject) {
			LoggingObject logObject = (LoggingObject)obj;
			return getKey() != null && getKey().equals(logObject.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
