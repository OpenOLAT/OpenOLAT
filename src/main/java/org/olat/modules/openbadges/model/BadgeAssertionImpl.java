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

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.BadgeClass;

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
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Initial date: 2023-06-01<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Entity(name="badgeassertion")
@Table(name="o_badge_assertion")
public class BadgeAssertionImpl implements Persistable, BadgeAssertion {

	@Serial
	private static final long serialVersionUID = -8388023500983264420L;

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

	@Enumerated(EnumType.STRING)
	@Column(name = "b_status", nullable = false, insertable = true, updatable = true)
	private BadgeAssertionStatus status;

	@Column(name = "b_recipient", nullable = false, insertable = true, updatable = true)
	private String recipientObject;

	@Column(name = "b_verification", nullable = false, insertable = true, updatable = true)
	private String verificationObject;

	@Column(name = "b_issued_on", nullable = false, insertable = true, updatable = true)
	private Date issuedOn;

	@Column(name = "b_baked_image", nullable = true, insertable = true, updatable = true)
	private String bakedImage;

	@Column(name = "b_evidence", nullable = true, insertable = true, updatable = true)
	private String evidence;

	@Column(name = "b_narrative", nullable = true, insertable = true, updatable = true)
	private String narrative;

	@Column(name = "b_expires", nullable = true, insertable = true, updatable = true)
	private Date expires;

	@Column(name = "b_revocation_reason", nullable = true, insertable = true, updatable = true)
	private String revocationReason;

	@ManyToOne(targetEntity = BadgeClassImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_badge_class", nullable = false, insertable = true, updatable = true)
	private BadgeClass badgeClass;

	@ManyToOne(targetEntity = IdentityImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_recipient", nullable = false, insertable = true, updatable = true)
	private Identity recipient;

	@ManyToOne(targetEntity = IdentityImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_awarded_by", nullable = true, insertable = true, updatable = true)
	private Identity awardedBy;

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
	public BadgeAssertionStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(BadgeAssertionStatus status) {
		this.status = status;
	}

	public String getRecipientObject() {
		return recipientObject;
	}

	public void setRecipientObject(String recipientObject) {
		this.recipientObject = recipientObject;
	}

	@Override
	public String getVerificationObject() {
		return verificationObject;
	}

	@Override
	public void setVerificationObject(String verificationObject) {
		this.verificationObject = verificationObject;
	}

	@Override
	public Date getIssuedOn() {
		return issuedOn;
	}

	@Override
	public void setIssuedOn(Date issuedOn) {
		this.issuedOn = issuedOn;
	}

	@Override
	public String getBakedImage() {
		return bakedImage;
	}

	@Override
	public void setBakedImage(String bakedImage) {
		this.bakedImage = bakedImage;
	}

	@Override
	public String getEvidence() {
		return evidence;
	}

	@Override
	public void setEvidence(String evidence) {
		this.evidence = evidence;
	}

	@Override
	public String getNarrative() {
		return narrative;
	}

	@Override
	public void setNarrative(String narrative) {
		this.narrative = narrative;
	}

	@Override
	public Date getExpires() {
		return expires;
	}

	@Override
	public void setExpires(Date expires) {
		this.expires = expires;
	}

	@Override
	public String getRevocationReason() {
		return revocationReason;
	}

	@Override
	public void setRevocationReason(String revocationReason) {
		this.revocationReason = revocationReason;
	}

	@Override
	public BadgeClass getBadgeClass() {
		return badgeClass;
	}

	@Override
	public void setBadgeClass(BadgeClass badgeClass) {
		this.badgeClass = badgeClass;
	}

	@Override
	public Identity getRecipient() {
		return recipient;
	}

	@Override
	public void setRecipient(Identity recipient) {
		this.recipient = recipient;
	}

	@Override
	public Identity getAwardedBy() {
		return awardedBy;
	}

	@Override
	public void setAwardedBy(Identity awardedBy) {
		this.awardedBy = awardedBy;
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
		if (obj instanceof BadgeAssertionImpl badgeAssertion) {
			return getKey() != null && getKey().equals(badgeAssertion.getKey());
		}
		return false;
	}
}
