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
package org.olat.modules.certificationprogram;

import java.math.BigDecimal;
import java.util.Set;

import org.olat.basesecurity.Group;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.modules.certificationprogram.ui.component.Duration;
import org.olat.modules.certificationprogram.ui.component.DurationType;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 25 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public interface CertificationProgram extends CertificationProgramRef, OLATResourceable, CreateInfo, ModifiedInfo {
	
	String getIdentifier();
	
	void setIdentifier(String identifier);
	
	String getDisplayName();
	
	void setDisplayName(String displayName);
	
	String getDescription();
	
	void setDescription(String description);
	
	CertificationProgramStatusEnum getStatus();

	void setStatus(CertificationProgramStatusEnum status);
	
	RecertificationMode getRecertificationMode();

	void setRecertificationMode(RecertificationMode recertificationMode);
	
	boolean isValidityEnabled();

	void setValidityEnabled(boolean validityEnabled);

	int getValidityTimelapse();

	void setValidityTimelapse(int validityTimelapse);

	DurationType getValidityTimelapseUnit();

	void setValidityTimelapseUnit(DurationType validityTimelapseUnit);
	
	Duration getValidityTimelapseDuration();
	
	boolean isRecertificationEnabled();

	void setRecertificationEnabled(boolean recertificationEnabled);

	boolean isRecertificationWindowEnabled();

	void setRecertificationWindowEnabled(boolean recertificationWindowEnabled);

	int getRecertificationWindow();

	void setRecertificationWindow(int recertificationWindow);
	
	DurationType getRecertificationWindowUnit();

	void setRecertificationWindowUnit(DurationType recertificationWindowUnit);
	
	Duration getRecertificationWindowDuration();
	
	boolean hasCreditPoints();
	
	BigDecimal getCreditPoints();
	
	void setCreditPoints(BigDecimal points);
	
	CreditPointSystem getCreditPointSystem();

	void setCreditPointSystem(CreditPointSystem creditPointSystem);
	
	
	String getCertificateCustom1();

	void setCertificateCustom1(String certificateCustom1);

	String getCertificateCustom2();

	void setCertificateCustom2(String certificateCustom2);

	String getCertificateCustom3();

	void setCertificateCustom3(String certificateCustom3);
	
	CertificateTemplate getTemplate();

	void setTemplate(CertificateTemplate template);
	
	
	Group getGroup();
	
	Set<CertificationProgramToOrganisation> getOrganisations();
	
	OLATResource getResource();
	
	
}
