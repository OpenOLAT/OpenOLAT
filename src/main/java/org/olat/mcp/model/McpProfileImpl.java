/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.mcp.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Persistable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.mcp.McpProfile;
import org.olat.mcp.McpProfileType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The JPA implementation of an MCP profile. The tools are stored as a JSON
 * array of strings in a text column, the organisation roles as a
 * comma-separated list of role names. The identifier and the type cannot
 * change after the creation.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="mcpprofile")
@Table(name="o_mcp_profile")
public class McpProfileImpl implements McpProfile, Persistable {

	private static final long serialVersionUID = 2026100203L;

	private static final Logger log = Tracing.createLoggerFor(McpProfileImpl.class);
	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
	private static final TypeReference<List<String>> TOOLS_TYPE = new TypeReference<>() { /* */ };

	public static final int IDENTIFIER_MAX_LENGTH = 64;
	public static final int NAME_MAX_LENGTH = 255;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="m_identifier", nullable=false, insertable=true, updatable=false)
	private String identifier;
	@Column(name="m_name", nullable=false, insertable=true, updatable=true)
	private String name;
	@Column(name="m_description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="m_enabled", nullable=false, insertable=true, updatable=true)
	private boolean enabled;
	@Enumerated(EnumType.STRING)
	@Column(name="m_type", nullable=false, insertable=true, updatable=false)
	private McpProfileType type;
	@Column(name="m_tools", nullable=true, insertable=true, updatable=true)
	private String toolsJson;
	@Column(name="m_org_roles", nullable=true, insertable=true, updatable=true)
	private String organisationRolesString;
	@Column(name="m_ratelimit_rpm", nullable=true, insertable=true, updatable=true)
	private Integer rateLimitRequestsPerMinute;

	/** Parsed value of the tools column, built on the first access */
	@Transient
	private transient Set<String> toolsCache;

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
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public McpProfileType getType() {
		return type;
	}

	public void setType(McpProfileType type) {
		this.type = type;
	}

	public String getToolsJson() {
		return toolsJson;
	}

	public void setToolsJson(String toolsJson) {
		this.toolsJson = toolsJson;
		this.toolsCache = null;
	}

	@Override
	public Set<String> getTools() {
		Set<String> tools = toolsCache;
		if(tools == null) {
			tools = Collections.unmodifiableSortedSet(parseTools(toolsJson));
			toolsCache = tools;
		}
		return tools;
	}

	/**
	 * Stores the tools sorted by name. Null and blank names are dropped,
	 * the names are trimmed.
	 */
	@Override
	public void setTools(Set<String> tools) {
		SortedSet<String> cleaned = new TreeSet<>();
		if(tools != null) {
			for(String tool:tools) {
				if(StringHelper.containsNonWhitespace(tool)) {
					cleaned.add(tool.trim());
				}
			}
		}
		toolsJson = writeTools(cleaned);
		toolsCache = Collections.unmodifiableSortedSet(cleaned);
	}

	public String getOrganisationRolesString() {
		return organisationRolesString;
	}

	public void setOrganisationRolesString(String organisationRolesString) {
		this.organisationRolesString = organisationRolesString;
	}

	/**
	 * Unknown role names in the column are ignored. The list follows the
	 * order of the enumeration {@link OrganisationRoles}.
	 */
	@Override
	public List<OrganisationRoles> getOrganisationRoles() {
		if(!StringHelper.containsNonWhitespace(organisationRolesString)) {
			return List.of();
		}
		EnumSet<OrganisationRoles> roles = EnumSet.noneOf(OrganisationRoles.class);
		for(String role:organisationRolesString.split(",")) {
			String trimmed = role.trim();
			if(OrganisationRoles.isValue(trimmed)) {
				roles.add(OrganisationRoles.valueOf(trimmed));
			}
		}
		return List.copyOf(roles);
	}

	@Override
	public void setOrganisationRoles(List<OrganisationRoles> roles) {
		organisationRolesString = toRolesString(roles);
	}

	@Override
	public Integer getRateLimitRequestsPerMinute() {
		return rateLimitRequestsPerMinute;
	}

	@Override
	public void setRateLimitRequestsPerMinute(Integer requestsPerMinute) {
		this.rateLimitRequestsPerMinute = requestsPerMinute;
	}

	/**
	 * @param roles A list of roles, can be null
	 * @return The comma-separated names without duplicates in the order of the enumeration, null if empty
	 */
	public static String toRolesString(Collection<OrganisationRoles> roles) {
		if(roles == null || roles.isEmpty()) {
			return null;
		}
		EnumSet<OrganisationRoles> uniqueRoles = EnumSet.noneOf(OrganisationRoles.class);
		for(OrganisationRoles role:roles) {
			if(role != null) {
				uniqueRoles.add(role);
			}
		}
		if(uniqueRoles.isEmpty()) {
			return null;
		}
		List<String> names = new ArrayList<>(uniqueRoles.size());
		for(OrganisationRoles role:uniqueRoles) {
			names.add(role.name());
		}
		return String.join(",", names);
	}

	private static SortedSet<String> parseTools(String json) {
		SortedSet<String> tools = new TreeSet<>();
		if(StringHelper.containsNonWhitespace(json)) {
			try {
				List<String> names = JSON_MAPPER.readValue(json, TOOLS_TYPE);
				if(names != null) {
					for(String name:names) {
						if(StringHelper.containsNonWhitespace(name)) {
							tools.add(name);
						}
					}
				}
			} catch (JsonProcessingException e) {
				log.error("Cannot parse the tools of an MCP profile: {}", json, e);
			}
		}
		return tools;
	}

	private static String writeTools(Collection<String> tools) {
		try {
			return JSON_MAPPER.writeValueAsString(tools);
		} catch (JsonProcessingException e) {
			// a list of strings is always serializable
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int hashCode() {
		return key == null ? 862351 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof McpProfileImpl profile) {
			return key != null && key.equals(profile.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public String toString() {
		return "McpProfile[key=" + key + ", identifier=" + identifier + ", type=" + type + ", enabled=" + enabled + "]";
	}
}
