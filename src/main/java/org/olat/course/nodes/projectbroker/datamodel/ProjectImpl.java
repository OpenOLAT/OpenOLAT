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

package org.olat.course.nodes.projectbroker.datamodel;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupImpl;
import org.olat.basesecurity.manager.SecurityGroupDAO;
import org.olat.commons.lifecycle.LifeCycleEntry;
import org.olat.commons.lifecycle.LifeCycleManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.BusinessGroupService;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;


/**
 * 
 * @author guretzki
 */
@Entity(name="pbrokerproject")
@Table(name="o_projectbroker_project")
public class ProjectImpl implements CreateInfo, Persistable, Project {

	private static final Logger log = Tracing.createLoggerFor(ProjectImpl.class);
	
	private static final long serialVersionUID = 1L;

	private static final String CUSTOMFIELD_KEY = "customfield_";

	private static final String EVENT_START = "event_start";
	private static final String EVENT_END   = "event_end";

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
	@Column(name="project_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Version
	private int version = 0;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Column(name="title", nullable=true, insertable=true, updatable=true)
	private String title;
	@Column(name="description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="state", nullable=true, insertable=true, updatable=true)
	private String state;
	@Column(name="maxmembers", nullable=true, insertable=true, updatable=true)
	private int maxMembers;
	@Column(name="attachmentfilename", nullable=false, insertable=true, updatable=true)
	private String attachmentFileName;
	@Column(name="mailnotificationenabled", nullable=false, insertable=true, updatable=true)
	private boolean mailNotificationEnabled;
	
	@ManyToOne(targetEntity=ProjectBrokerImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="projectbroker_fk", nullable=false, insertable=true, updatable=false)
	private ProjectBroker projectBroker;
	@ManyToOne(targetEntity=SecurityGroupImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="candidategroup_fk", nullable=false, insertable=true, updatable=false)
	private SecurityGroup candidateGroup;
	@ManyToOne(targetEntity=BusinessGroupImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="projectgroup_fk", nullable=false, insertable=true, updatable=false)
	private BusinessGroup projectGroup; 
	
	@ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name = "o_projectbroker_customfields", 
      joinColumns = {@JoinColumn(name = "fk_project_id", referencedColumnName = "project_id")})
    @MapKeyColumn(name = "propname")
    @Column(name = "propvalue")
	private Map<String, String> customfields;
		
	/**
	 * Default constructor needs by hibernate
	 */
	public ProjectImpl () {
		//
	}
	
	/**
	 * Used to create a new project. Do not call directly, use ProjectBrokerManager.createProjectFor(..)
	 * Default value : state = Project.STATE_NOT_ASSIGNED, maxMembers = Project.MAX_MEMBERS_UNLIMITED, mailNotificationEnabled = true,
	 * remarks = "", attachmentFileName = ""
	 * @param title
	 * @param description
	 * @param state
	 * @param maxMembers
	 * @param remarks
	 * @param projectGroup
	 * @param attachmentFileName
	 * @param projectBroker
	 */
	public ProjectImpl(String title, String description, BusinessGroup projectGroup, ProjectBroker projectBroker, SecurityGroup candidateGroup) {
		this.title = title;
		this.description = description;
		this.state = Project.STATE_NOT_ASSIGNED;
		this.maxMembers = Project.MAX_MEMBERS_UNLIMITED;
		this.projectGroup = projectGroup;
		this.attachmentFileName = "";
		this.mailNotificationEnabled = true;
		this.projectBroker = projectBroker;
		this.candidateGroup = candidateGroup;
		setCreationDate(new Date());
	}
	
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

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getAttachmentFileName() {
		return attachmentFileName;
	}

	/**
	 * 
	 * @return List of Identity objects
	 */
	public List<Identity> getProjectLeaders() {
		return CoreSpringFactory.getImpl(BusinessGroupService.class).getMembers(getProjectGroup(), GroupRoles.coach.name());
	}

	/**
	 * 
	 * @return List of Identity objects
	 */
	@Override
	public SecurityGroup getCandidateGroup() {
		return candidateGroup;
	}

	@Override
	public String getState() {
		return state;
	}

	@Override
	public int getSelectedPlaces() {
		int numOfParticipants = CoreSpringFactory.getImpl(BusinessGroupService.class).countMembers(getProjectGroup(), GroupRoles.participant.name());
		int numOfCandidates = CoreSpringFactory.getImpl(SecurityGroupDAO.class).countIdentitiesOfSecurityGroup(getCandidateGroup());
		int numOfReservations = CoreSpringFactory.getImpl(ACReservationDAO.class).countReservations(getProjectGroup().getResource());
		return numOfParticipants + numOfCandidates + numOfReservations;                     
	}

	@Override
	public int getMaxMembers() {
		return maxMembers;
	}

	@Override
	public BusinessGroup getProjectGroup() {
		return projectGroup;
	}	

	@Override
	public ProjectBroker getProjectBroker() {
		return projectBroker;
	}

	/**
	 * Do not use this method to access the customfields. 
	 * Hibernate Getter.
	 * 
	 * @return Map containing the raw properties data
	 */
	public Map<String, String> getCustomfields() {
		if (customfields == null) {
			setCustomfields(new HashMap<>());
		}
		return customfields;
	}
	
	/**
	 * Hibernate setter
	 * @param fields
	 */
	public void setCustomfields(Map<String, String> customfields) {
		this.customfields = customfields;
	}

	//////////
	// SETTER
	//////////
	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setState(String state) {
		this.state = state;
	}

	@Override
	public void setMaxMembers(int maxMembers) {
		this.maxMembers = maxMembers;
	}


	public void setProjectGroup(BusinessGroup projectGroup) {
		this.projectGroup = projectGroup;
	}

	public void setCandidateGroup(SecurityGroup candidateGroup) {
		this.candidateGroup = candidateGroup;
	}

	public void setAttachmentFileName(String attachmentFileName) {
		this.attachmentFileName = attachmentFileName;
	}


	public void setProjectBroker(ProjectBroker projectBroker) {
		this.projectBroker = projectBroker;
	}

	@Override
	public void setAttachedFileName(String attachmentFileName) {
		this.attachmentFileName = attachmentFileName;
	}

	@Override
	public int getCustomFieldSize() {
		return getCustomfields().size();
	}

	@Override
	public String getCustomFieldValue(int index) {
		String value = getCustomfields().get(CUSTOMFIELD_KEY + index);
		if (value != null ) {
			return value;
		}
		return "";
	}

	@Override
	public void setCustomFieldValue(int index, String value) {
		log.debug("setValue index={} : {}", index, value);
		getCustomfields().put(CUSTOMFIELD_KEY + index, value);
	}

	@Override
	public ProjectEvent getProjectEvent(Project.EventType eventType) {
		LifeCycleManager lifeCycleManager = LifeCycleManager.createInstanceFor(this);
		LifeCycleEntry startLifeCycleEntry = lifeCycleManager.lookupLifeCycleEntry(eventType.toString(), EVENT_START);
		LifeCycleEntry endLifeCycleEntry = lifeCycleManager.lookupLifeCycleEntry(eventType.toString(), EVENT_END);
		Date startDate = null;
		if (startLifeCycleEntry != null) {
			startDate = startLifeCycleEntry.getLcTimestamp();
		}
		Date endDate = null;
		if (endLifeCycleEntry != null) {
			endDate = endLifeCycleEntry.getLcTimestamp();
		}
		
		ProjectEvent projectEvent = new ProjectEvent(eventType, startDate , endDate);
		log.debug("getProjectEvent projectEvent={}", projectEvent);
		return projectEvent;
	}

	@Override
	public void setProjectEvent(ProjectEvent projectEvent ) {
		LifeCycleManager lifeCycleManager = LifeCycleManager.createInstanceFor(this);
		log.debug("setProjectEvent projectEvent={}", projectEvent);
		if (projectEvent.getStartDate() != null) {
			lifeCycleManager.markTimestampFor(projectEvent.getStartDate(), projectEvent.getEventType().toString(), EVENT_START);
		} else {
			lifeCycleManager.deleteTimestampFor(projectEvent.getEventType().toString(), EVENT_START);
			log.debug("{} delete timestamp for {}", EVENT_START, projectEvent.getEventType());
		}
		if (projectEvent.getEndDate() != null) {
			lifeCycleManager.markTimestampFor(projectEvent.getEndDate(), projectEvent.getEventType().toString(), EVENT_END);
		} else {
			lifeCycleManager.deleteTimestampFor(projectEvent.getEventType().toString(), EVENT_END);
			log.debug("{} delete timestamp for {}", EVENT_END, projectEvent.getEventType());
		}

	}

	@Override
	public boolean isMailNotificationEnabled() {
		return mailNotificationEnabled;
	}

	/**
	 * Hibernate setter
	 * @param mailNotificationEnabled
	 */
	@Override
	public void setMailNotificationEnabled(boolean mailNotificationEnabled) {
		this.mailNotificationEnabled = mailNotificationEnabled;
	}
	

	@Override
	public String toString() {
		return "Project [title=" + getTitle() + ", description=" + getDescription() + ", state=" + getState() + "] " + super.toString();
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 82301 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof Project project) {
			return getKey() != null && getKey().equals(project.getKey());	
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
