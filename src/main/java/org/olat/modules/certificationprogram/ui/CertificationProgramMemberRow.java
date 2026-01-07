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
package org.olat.modules.certificationprogram.ui;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.course.certificate.Certificate;
import org.olat.modules.certificationprogram.ui.component.NextRecertificationInDays;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 3 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramMemberRow extends UserPropertiesRow {

	private CertificationProgramMemberDetailsController detailsCtrl;

	private final CreditPointWallet wallet;
	private final Certificate certificate;
	private final CertificationStatus certificateStatus;
	private final CertificationIdentityStatus identityStatus;
	private final NextRecertificationInDays nextRecertification;
	
	public CertificationProgramMemberRow(Identity member, Certificate certificate,
			NextRecertificationInDays nextRecertification, CertificationStatus certificateStatus,
			CertificationIdentityStatus identityStatus, CreditPointWallet wallet,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(member, userPropertyHandlers, locale);
		this.wallet = wallet;
		this.certificate = certificate;
		this.identityStatus = identityStatus;
		this.certificateStatus = certificateStatus;
		this.nextRecertification = nextRecertification;
	}
	
	public CertificationStatus getCertificateStatus() {
		return certificateStatus;
	}
	
	public CertificationIdentityStatus getIdentityStatus() {
		return identityStatus;
	}
	
	public long getRecertificationCount() {
		return certificate == null || certificate.getRecertificationCount() == null
				? 0l
				: certificate.getRecertificationCount().longValue();
	}

	public Date getNextRecertificationDate() {
		return certificate == null
				? null
				: certificate.getNextRecertificationDate();
	}
	
	public Date getRevocationDate() {
		return certificate != null && certificateStatus == CertificationStatus.REVOKED
				? certificate.getRevocationDate()
				: null;
	}
	
	public NextRecertificationInDays getNextRecertification() {
		return nextRecertification;
	}
	
	public BigDecimal getWalletBalance() {
		return wallet == null
				? BigDecimal.ZERO
				: wallet.getBalance();
	}
	
	public boolean isDetailsControllerAvailable() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().isVisible();
		}
		return false;
	}

	public CertificationProgramMemberDetailsController getDetailsController() {
		return detailsCtrl;
	}
	
	public String getDetailsControllerName() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().getComponent().getComponentName();
		}
		return null;
	}
	
	public void setDetailsController(CertificationProgramMemberDetailsController detailsCtrl) {
		this.detailsCtrl = detailsCtrl;
	}
}
