/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.ims.lti13.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.ims.lti.LTIDisplayOptions;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 22 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="ltitooldeployment")
@Table(name="o_lti_tool_deployment")
public class LTI13ToolDeploymentImpl implements LTI13ToolDeployment, Persistable {

	private static final long serialVersionUID = -6482256605058165492L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

    @Column(name="l_deployment_id", nullable=false, insertable=true, updatable=false)
	private String deploymentId;
    
    @Column(name="l_context_id", nullable=false, insertable=true, updatable=false)
	private String contextId;
    
    @Column(name="l_target_url", nullable=true, insertable=true, updatable=true)
	private String targetUrl;
    
    @Column(name="l_send_attributes", nullable=true, insertable=true, updatable=true)
	private String sendAttributes;
    @Column(name="l_send_custom_attributes", nullable=true, insertable=true, updatable=true)
	private String sendCustomAttributes;

    @Column(name="l_author_roles", nullable=true, insertable=true, updatable=true)
	private String authorRoles;
    @Column(name="l_coach_roles", nullable=true, insertable=true, updatable=true)
	private String coachRoles;
    @Column(name="l_participant_roles", nullable=true, insertable=true, updatable=true)
	private String participantRoles;
    
    @Column(name="l_assessable", nullable=true, insertable=true, updatable=true)
    private boolean assessable;
    
    @Column(name="l_nrps", nullable=true, insertable=true, updatable=true)
    private boolean nameAndRolesProvisioningServices;

    @Column(name="l_display", nullable=true, insertable=true, updatable=true)
    private String display;
    @Column(name="l_display_height", nullable=true, insertable=true, updatable=true)
    private String displayHeight;
    @Column(name="l_display_width", nullable=true, insertable=true, updatable=true)
    private String displayWidth;
    @Column(name="l_skip_launch_page", nullable=true, insertable=true, updatable=true)
    private boolean skipLaunchPage;
	
	@ManyToOne(targetEntity=LTI13ToolImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_tool_id", nullable=false, insertable=true, updatable=false)
	private LTI13Tool tool;
	
	@ManyToOne(targetEntity=RepositoryEntry.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_entry_id", nullable=true, insertable=true, updatable=false)
	private RepositoryEntry entry;
	@Column(name="l_sub_ident", nullable=true, insertable=true, updatable=false)
	private String subIdent;
	
	@ManyToOne(targetEntity=BusinessGroupImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_group_id", nullable=true, insertable=true, updatable=false)
	private BusinessGroup businessGroup;
	

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
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	@Override
	public String getContextId() {
		return contextId;
	}

	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

	@Override
	public String getTargetUrl() {
		return targetUrl;
	}

	@Override
	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	public String getSendAttributes() {
		return sendAttributes;
	}

	public void setSendAttributes(String sendAttributes) {
		this.sendAttributes = sendAttributes;
	}

	@Override
	public List<String> getSendUserAttributesList() {
		return toList(getSendAttributes());
	}

	@Override
	public void setSendUserAttributesList(List<String> attributes) {
		setSendAttributes(toString(attributes));
	}

	@Override
	public String getSendCustomAttributes() {
		return sendCustomAttributes;
	}

	@Override
	public void setSendCustomAttributes(String sendCustomAttributes) {
		this.sendCustomAttributes = sendCustomAttributes;
	}

	private static final List<String> toList(String val) {
		if(StringHelper.containsNonWhitespace(val)) {
			String[] attrs = val.split("[,]");
			return Arrays.<String>asList(attrs);
		}
		return new ArrayList<>();
	}
	
	private static final String toString(List<String> list) {
		if(list == null || list.isEmpty()) {
			return null;
		}
		return String.join(",", list);
	}

	@Override
	public String getAuthorRoles() {
		return authorRoles;
	}

	public void setAuthorRoles(String authorRoles) {
		this.authorRoles = authorRoles;
	}

	@Override
	public List<String> getAuthorRolesList() {
		return toList(getAuthorRoles());
	}

	@Override
	public void setAuthorRolesList(List<String> roles) {
		setAuthorRoles(toString(roles));
	}

	@Override
	public String getCoachRoles() {
		return coachRoles;
	}

	public void setCoachRoles(String coachRoles) {
		this.coachRoles = coachRoles;
	}
	
	@Override
	public List<String> getCoachRolesList() {
		return toList(getCoachRoles());
	}

	@Override
	public void setCoachRolesList(List<String> roles) {
		setCoachRoles(toString(roles));
	}

	@Override
	public String getParticipantRoles() {
		return participantRoles;
	}

	public void setParticipantRoles(String participantRoles) {
		this.participantRoles = participantRoles;
	}
	
	@Override
	public List<String> getParticipantRolesList() {
		return toList(getParticipantRoles());
	}

	@Override
	public void setParticipantRolesList(List<String> roles) {
		setParticipantRoles(toString(roles));
	}

	@Override
	public boolean isAssessable() {
		return assessable;
	}

	@Override
	public void setAssessable(boolean assessable) {
		this.assessable = assessable;
	}
	
	@Override
	public boolean isNameAndRolesProvisioningServicesEnabled() {
		return nameAndRolesProvisioningServices;
	}

	public void setNameAndRolesProvisioningServicesEnabled(boolean nameAndRolesProvisioningServices) {
		this.nameAndRolesProvisioningServices = nameAndRolesProvisioningServices;
	}

	@Override
	public String getDisplay() {
		return display;
	}

	@Override
	public void setDisplay(String display) {
		this.display = display;
	}

	@Override
	public LTIDisplayOptions getDisplayOptions() {
		return StringHelper.containsNonWhitespace(display) ? LTIDisplayOptions.valueOf(display) : LTIDisplayOptions.iframe;
	}

	@Override
	public void setDisplayOptions(LTIDisplayOptions option) {
		setDisplay(option == null ? null : option.name());	
	}

	@Override
	public String getDisplayHeight() {
		return displayHeight;
	}

	@Override
	public void setDisplayHeight(String height) {
		this.displayHeight = height;
	}

	@Override
	public String getDisplayWidth() {
		return displayWidth;
	}

	@Override
	public void setDisplayWidth(String width) {
		this.displayWidth = width;
	}

	@Override
	public boolean isSkipLaunchPage() {
		return skipLaunchPage;
	}

	@Override
	public void setSkipLaunchPage(boolean skipLaunchPage) {
		this.skipLaunchPage = skipLaunchPage;
	}

	@Override
	public LTI13Tool getTool() {
		return tool;
	}

	public void setTool(LTI13Tool tool) {
		this.tool = tool;
	}
	
	@Override
	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}

	@Override
	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}
	
	@Override
	public BusinessGroup getBusinessGroup() {
		return businessGroup;
	}

	public void setBusinessGroup(BusinessGroup businessGroup) {
		this.businessGroup = businessGroup;
	}

	@Override
	public int hashCode() {
		return key == null ? 265379 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof LTI13ToolDeploymentImpl) {
			LTI13ToolDeploymentImpl deployment = (LTI13ToolDeploymentImpl)obj;
			return getKey() != null && getKey().equals(deployment.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
