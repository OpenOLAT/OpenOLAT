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
package org.olat.upgrade.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.ims.lti.LTIDisplayOptions;

/**
 * 
 * Initial date: 22 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="upgradetooldeployment")
@Table(name="o_lti_tool_deployment")
public class UpgradeLTI13ToolDeployment implements Persistable {

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

    @Column(name="l_deployment_id", nullable=false, insertable=false, updatable=false)
	private String deploymentId;
    
    @Column(name="l_context_id", nullable=false, insertable=false, updatable=false)
	private String contextId;
    
    @Column(name="l_target_url", nullable=true, insertable=false, updatable=false)
	private String targetUrl;
    
    @Column(name="l_deployment_resource_id", nullable=true, insertable=false, updatable=false)
	private String deploymentResourceId;
    
    @Column(name="l_send_attributes", nullable=true, insertable=false, updatable=false)
	private String sendAttributes;
    @Column(name="l_send_custom_attributes", nullable=true, insertable=false, updatable=false)
	private String sendCustomAttributes;

    @Column(name="l_author_roles", nullable=true, insertable=false, updatable=false)
	private String authorRoles;
    @Column(name="l_coach_roles", nullable=true, insertable=false, updatable=false)
	private String coachRoles;
    @Column(name="l_participant_roles", nullable=true, insertable=false, updatable=false)
	private String participantRoles;
    
    @Column(name="l_assessable", nullable=true, insertable=false, updatable=false)
    private boolean assessable;
    
    @Column(name="l_nrps", nullable=true, insertable=false, updatable=false)
    private boolean nameAndRolesProvisioningServices;

    @Column(name="l_display", nullable=true, insertable=false, updatable=false)
    private String display;
    @Column(name="l_display_height", nullable=true, insertable=false, updatable=false)
    private String displayHeight;
    @Column(name="l_display_width", nullable=true, insertable=false, updatable=false)
    private String displayWidth;
    @Column(name="l_skip_launch_page", nullable=true, insertable=false, updatable=false)
    private boolean skipLaunchPage;
	
	@Column(name="fk_tool_id", nullable=false, insertable=false, updatable=false)
	private Long toolKey;
	@Column(name="fk_entry_id", nullable=true, insertable=false, updatable=false)
	private Long entryKey;
	@Column(name="l_sub_ident", nullable=true, insertable=false, updatable=false)
	private String subIdent;
	@Column(name="fk_group_id", nullable=true, insertable=false, updatable=false)
	private Long businessGroupKey;
	

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	public String getContextId() {
		return contextId;
	}

	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	public String getDeploymentResourceId() {
		return deploymentResourceId;
	}

	public void setDeploymentResourceId(String deploymentResourceId) {
		this.deploymentResourceId = deploymentResourceId;
	}

	public String getSendAttributes() {
		return sendAttributes;
	}

	public void setSendAttributes(String sendAttributes) {
		this.sendAttributes = sendAttributes;
	}

	public List<String> getSendUserAttributesList() {
		return toList(getSendAttributes());
	}

	public void setSendUserAttributesList(List<String> attributes) {
		setSendAttributes(toString(attributes));
	}

	public String getSendCustomAttributes() {
		return sendCustomAttributes;
	}

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

	public String getAuthorRoles() {
		return authorRoles;
	}

	public void setAuthorRoles(String authorRoles) {
		this.authorRoles = authorRoles;
	}

	public List<String> getAuthorRolesList() {
		return toList(getAuthorRoles());
	}

	public void setAuthorRolesList(List<String> roles) {
		setAuthorRoles(toString(roles));
	}

	public String getCoachRoles() {
		return coachRoles;
	}

	public void setCoachRoles(String coachRoles) {
		this.coachRoles = coachRoles;
	}
	
	public List<String> getCoachRolesList() {
		return toList(getCoachRoles());
	}

	public void setCoachRolesList(List<String> roles) {
		setCoachRoles(toString(roles));
	}

	public String getParticipantRoles() {
		return participantRoles;
	}

	public void setParticipantRoles(String participantRoles) {
		this.participantRoles = participantRoles;
	}
	
	public List<String> getParticipantRolesList() {
		return toList(getParticipantRoles());
	}

	public void setParticipantRolesList(List<String> roles) {
		setParticipantRoles(toString(roles));
	}

	public boolean isAssessable() {
		return assessable;
	}

	public void setAssessable(boolean assessable) {
		this.assessable = assessable;
	}
	
	public boolean isNameAndRolesProvisioningServicesEnabled() {
		return nameAndRolesProvisioningServices;
	}

	public void setNameAndRolesProvisioningServicesEnabled(boolean nameAndRolesProvisioningServices) {
		this.nameAndRolesProvisioningServices = nameAndRolesProvisioningServices;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public LTIDisplayOptions getDisplayOptions() {
		return StringHelper.containsNonWhitespace(display) ? LTIDisplayOptions.valueOf(display) : LTIDisplayOptions.iframe;
	}

	public void setDisplayOptions(LTIDisplayOptions option) {
		setDisplay(option == null ? null : option.name());	
	}

	public String getDisplayHeight() {
		return displayHeight;
	}

	public void setDisplayHeight(String height) {
		this.displayHeight = height;
	}

	public String getDisplayWidth() {
		return displayWidth;
	}

	public void setDisplayWidth(String width) {
		this.displayWidth = width;
	}

	public boolean isSkipLaunchPage() {
		return skipLaunchPage;
	}

	public void setSkipLaunchPage(boolean skipLaunchPage) {
		this.skipLaunchPage = skipLaunchPage;
	}

	public Long getToolKey() {
		return toolKey;
	}

	public void setToolKey(Long toolKey) {
		this.toolKey = toolKey;
	}

	public Long getEntryKey() {
		return entryKey;
	}

	public void setEntryKey(Long entryKey) {
		this.entryKey = entryKey;
	}

	public Long getBusinessGroupKey() {
		return businessGroupKey;
	}

	public void setBusinessGroupKey(Long businessGroupKey) {
		this.businessGroupKey = businessGroupKey;
	}

	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
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
		if(obj instanceof UpgradeLTI13ToolDeployment) {
			UpgradeLTI13ToolDeployment deployment = (UpgradeLTI13ToolDeployment)obj;
			return getKey() != null && getKey().equals(deployment.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
