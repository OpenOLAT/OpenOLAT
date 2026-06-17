/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.certificationprogram.restapi;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.course.certificate.Certificate;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberWithInfos;
import org.olat.modules.certificationprogram.ui.CertificationStatus;
import org.olat.modules.creditpoint.CreditPointWallet;

/**
 * 
 * Initial date: 20 mai 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "certificationProgramMemberVO")
public class CertificationProgramMemberVO {

	private Long identityKey;
	private Long certificateKey;
	private Date issuedDate;
	private Date nextRecertificationDate;
	/** active, expired, recertification */
	private String status;
	private boolean recertificationWindowOpen;
	private BigDecimal creditPointsAwarded;
	private String externalId;

	public CertificationProgramMemberVO() {
		// for JAX-RS
	}

	public static CertificationProgramMemberVO valueOf(CertificationProgramMemberWithInfos memberWithInfos, Date referenceDate) {
		Certificate certificate = memberWithInfos.certificate();
		CreditPointWallet wallet = memberWithInfos.wallet();
		return valueOf(certificate, wallet, referenceDate);
	}
	
	public static CertificationProgramMemberVO valueOf(Certificate certificate, CreditPointWallet wallet, Date referenceDate) {
		CertificationStatus certStatus = CertificationStatus.evaluate(certificate, referenceDate);

		CertificationProgramMemberVO vo = new CertificationProgramMemberVO();
		vo.setIdentityKey(certificate.getIdentity().getKey());
		vo.setCertificateKey(certificate.getKey());
		vo.setIssuedDate(certificate.getCreationDate());
		vo.setNextRecertificationDate(certificate.getNextRecertificationDate());
		vo.setStatus(toRestStatus(certStatus));
		vo.setRecertificationWindowOpen(certStatus == CertificationStatus.EXPIRED_RENEWABLE);
		vo.setExternalId(certificate.getExternalId());
		if(wallet != null) {
			vo.setCreditPointsAwarded(wallet.getBalance());
		}
		return vo;
	}

	private static String toRestStatus(CertificationStatus certStatus) {
		return switch(certStatus) {
			case VALID -> "active";
			case EXPIRED_RENEWABLE -> "recertification";
			default -> "expired";
		};
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public Long getCertificateKey() {
		return certificateKey;
	}

	public void setCertificateKey(Long certificateKey) {
		this.certificateKey = certificateKey;
	}

	public Date getIssuedDate() {
		return issuedDate;
	}

	public void setIssuedDate(Date issuedDate) {
		this.issuedDate = issuedDate;
	}

	public Date getNextRecertificationDate() {
		return nextRecertificationDate;
	}

	public void setNextRecertificationDate(Date nextRecertificationDate) {
		this.nextRecertificationDate = nextRecertificationDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isRecertificationWindowOpen() {
		return recertificationWindowOpen;
	}

	public void setRecertificationWindowOpen(boolean recertificationWindowOpen) {
		this.recertificationWindowOpen = recertificationWindowOpen;
	}

	public BigDecimal getCreditPointsAwarded() {
		return creditPointsAwarded;
	}

	public void setCreditPointsAwarded(BigDecimal creditPointsAwarded) {
		this.creditPointsAwarded = creditPointsAwarded;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public int hashCode() {
		return certificateKey == null ? 8273 : certificateKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj instanceof CertificationProgramMemberVO other) {
			return certificateKey != null && certificateKey.equals(other.certificateKey);
		}
		return false;
	}
}
