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
package org.olat.modules.openbadges.model;

import java.io.Serial;
import java.util.Date;

import org.olat.core.id.Persistable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.BadgeOrganization;
import org.olat.modules.openbadges.BadgeVerification;
import org.olat.modules.openbadges.OpenBadgesFactory;
import org.olat.modules.openbadges.criteria.BadgeCriteria;
import org.olat.modules.openbadges.criteria.BadgeCriteriaXStream;
import org.olat.modules.openbadges.v2.Profile;
import org.olat.repository.RepositoryEntry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * Initial date: 2023-05-30<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Entity(name="badgeclass")
@Table(name="o_badge_class")
public class BadgeClassImpl implements Persistable, BadgeClass {

	private static final Logger log = Tracing.createLoggerFor(BadgeClassImpl.class);

	@Serial
	private static final long serialVersionUID = 4628879504742724536L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, insertable = true, updatable = false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationdate", nullable = false, insertable = true, updatable = false)
	private Date creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "lastmodified", nullable = false, insertable = true, updatable = true)
	private Date lastModified;

	@Column(name = "b_uuid", nullable = false, insertable = true, updatable = false)
	private String uuid;

	@Column(name = "b_root_id", nullable = true, insertable = true, updatable = true)
	private String rootId;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "b_status", nullable = false, insertable = true, updatable = true)
	private BadgeClassStatus status;

	@Column(name = "b_version", nullable = false, insertable = true, updatable = true)
	private String version;

	@Column(name = "b_language", nullable = true, insertable = true, updatable = true)
	private String language;

	@Column(name = "b_image", nullable = false, insertable = true, updatable = true)
	private String image;

	@Column(name = "b_name", nullable = false, insertable = true, updatable = true)
	private String name;

	@Column(name = "b_description", nullable = false, insertable = true, updatable = true)
	private String description;

	@Column(name = "b_criteria", nullable = true, insertable = true, updatable = true)
	private String criteria;

	@Column(name = "b_salt", nullable = false, insertable = true, updatable = false)
	private String salt;

	@Column(name = "b_issuer", nullable = false, insertable = true, updatable = true)
	private String issuer;

	@Column(name="b_validity_enabled", nullable = true, insertable = true, updatable = true)
	private boolean validityEnabled;

	@Column(name="b_validity_timelapse", nullable = true, insertable = true, updatable = true)
	private int validityTimelapse;

	@Enumerated(EnumType.STRING)
	@Column(name="b_validity_timelapse_unit", nullable = true, insertable = true, updatable = true)
	private BadgeClassTimeUnit validityTimelapseUnit;

	@Enumerated(EnumType.STRING)
	@Column(name="b_version_type", nullable = true, insertable = true, updatable = true)
	private BadgeClassVersionType versionType;
	
	@Enumerated(EnumType.STRING)
	@Column(name="b_verification_method", nullable = true, insertable = true, updatable = true)
	private BadgeVerification verificationMethod;
	
	@Column(name="b_private_key", nullable = true, insertable = true, updatable = true)
	private String privateKey;
	
	@Column(name="b_public_key", nullable = true, insertable = true, updatable = true)
	private String publicKey;
	
	@ManyToOne(targetEntity = RepositoryEntry.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_entry", nullable = true, insertable = true, updatable = true)
	private RepositoryEntry entry;

	@ManyToOne(targetEntity = BadgeOrganizationImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_badge_organization", nullable = true, insertable = true, updatable = true)
	private BadgeOrganization badgeOrganization;

	@OneToOne(targetEntity = BadgeClassImpl.class, fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "fk_previous_version", nullable = true, insertable = true, updatable = true)
	private BadgeClass previousVersion;
	
	@OneToOne(targetEntity = BadgeClassImpl.class, fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "fk_next_version", nullable = true, insertable = true, updatable = true)
	private BadgeClass nextVersion;

	public BadgeClassImpl() {
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
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String getRootId() {
		return rootId;
	}

	@Override
	public void setRootId(String rootId) {
		this.rootId = rootId;
	}

	@Override
	public BadgeClassStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(BadgeClassStatus status) {
		this.status = status;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public String getVersionWithScan() {
		return StringHelper.xssScan(getVersion());
	}

	@Override
	public String getVersionDisplayString() {
		if (getVersionType() == null) {
			return OpenBadgesFactory.getDefaultVersion();
		}
		return getVersion();
	}

	@Override
	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public BadgeVerification getVerificationMethod() {
		return verificationMethod;
	}

	@Override
	public void setVerificationMethod(BadgeVerification verificationMethod) {
		this.verificationMethod = verificationMethod;
	}

	@Override
	public String getPrivateKey() {
		return privateKey;
	}

	@Override
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	@Override
	public String getPublicKey() {
		return publicKey;
	}

	@Override
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	@Override
	public String getLanguage() {
		return language;
	}

	@Override
	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public String getImage() {
		return image;
	}

	@Override
	public void setImage(String image) {
		this.image = image;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getNameWithScan() {
		return StringHelper.xssScan(getName());
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setNameWithScan(String name) {
		setName(StringHelper.unescapeHtml(FilterFactory.getHtmlTagsFilter().filter(StringHelper.xssScan(name))));
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getDescriptionWithScan() {
		return StringHelper.xssScan(getDescription());
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setDescriptionWithScan(String description) {
		setDescription(StringHelper.unescapeHtml(FilterFactory.getHtmlTagsFilter().filter(StringHelper.xssScan(description))));
	}

	@Override
	public String getCriteria() {
		return criteria;
	}

	@Override
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	@Override
	public String getSalt() {
		return salt;
	}

	@Override
	public void setSalt(String salt) {
		this.salt = salt;
	}

	@Override
	public String getIssuer() {
		return issuer;
	}

	@Override
	public String getIssuerDisplayString() {
		if (!StringHelper.containsNonWhitespace(issuer)) {
			return "";
		}
		try {
			Profile profile = new Profile(new JSONObject(issuer));
			String name = profile.getNameWithScan();
			if (StringHelper.containsNonWhitespace(name)) {
				return name;
			}
		} catch (Exception e) {
			log.error(e);
		}

		return "";
	}

	@Override
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	@Override
	public boolean isValidityEnabled() {
		return validityEnabled;
	}

	@Override
	public void setValidityEnabled(boolean validityEnabled) {
		this.validityEnabled = validityEnabled;
	}

	@Override
	public int getValidityTimelapse() {
		return validityTimelapse;
	}

	@Override
	public void setValidityTimelapse(int validityTimelapse) {
		this.validityTimelapse = validityTimelapse;
	}

	@Override
	public BadgeClassTimeUnit getValidityTimelapseUnit() {
		return validityTimelapseUnit;
	}

	@Override
	public void setValidityTimelapseUnit(BadgeClassTimeUnit validityTimelapseUnit) {
		this.validityTimelapseUnit = validityTimelapseUnit;
	}

	@Override
	public BadgeClassVersionType getVersionType() {
		return versionType;
	}

	@Override
	public void setVersionType(BadgeClassVersionType versionType) {
		this.versionType = versionType;
	}

	@Override
	public RepositoryEntry getEntry() {
		return entry;
	}

	@Override
	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}

	@Override
	public BadgeOrganization getBadgeOrganization() {
		return badgeOrganization;
	}

	@Override
	public void setBadgeOrganization(BadgeOrganization badgeOrganization) {
		this.badgeOrganization = badgeOrganization;
	}

	@Override
	public BadgeClass getPreviousVersion() {
		return previousVersion;
	}

	@Override
	public void setPreviousVersion(BadgeClass previousVersion) {
		this.previousVersion = previousVersion;
	}

	@Override
	public boolean hasPreviousVersion() {
		return previousVersion != null;
	}

	@Override
	public BadgeClass getNextVersion() {
		return nextVersion;
	}

	@Override
	public void setNextVersion(BadgeClass nextVersion) {
		this.nextVersion = nextVersion;
	}

	@Override
	public void prepareForEntryReset(RepositoryEntry entry) {
		if (StringHelper.containsNonWhitespace(getCriteria())) {
			BadgeCriteria criteria = BadgeCriteriaXStream.fromXml(getCriteria());
			criteria.prepareForEntryReset(entry);
			setCriteria(BadgeCriteriaXStream.toXml(criteria));
		}
	}

	@Override
	public int upgradeBadgeDependencyConditions() {
		if (StringHelper.containsNonWhitespace(getCriteria())) {
			BadgeCriteria criteria = BadgeCriteriaXStream.fromXml(getCriteria());
			int updateCount = criteria.upgradeBadgeDependencyConditions();
			setCriteria(BadgeCriteriaXStream.toXml(criteria));
			return updateCount;
		}
		return 0;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof BadgeClassImpl badgeClass) {
			return getKey() != null && getKey().equals(badgeClass.getKey());
		}
		return false;
	}
}
