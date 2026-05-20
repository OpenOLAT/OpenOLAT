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
import java.util.List;
import java.util.stream.Collectors;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramToOrganisation;

/**
 * 
 * Initial date: 19 mai 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "certificationProgramVO")
public class CertificationProgramVO {

	private Long key;
	private String identifier;
	private String displayName;
	private String description;
	private String status;

	private boolean validityEnabled;
	private int validityTimelapse;
	private String validityTimelapseUnit;

	private boolean recertificationEnabled;
	private int recertificationWindow;
	private String recertificationWindowUnit;

	private BigDecimal creditPoints;
	private Long creditPointSystemKey;
	private Long templateKey;
	private List<Long> organisationKeys;

	public CertificationProgramVO() {
		// for JAX-RS
	}

	public static CertificationProgramVO valueOf(CertificationProgram program) {
		CertificationProgramVO vo = new CertificationProgramVO();
		vo.setKey(program.getKey());
		vo.setIdentifier(program.getIdentifier());
		vo.setDisplayName(program.getDisplayName());
		vo.setDescription(program.getDescription());
		vo.setStatus(program.getStatus() == null ? null : program.getStatus().name());
		vo.setValidityEnabled(program.isValidityEnabled());
		vo.setValidityTimelapse(program.getValidityTimelapse());
		vo.setValidityTimelapseUnit(program.getValidityTimelapseUnit() == null ? null : program.getValidityTimelapseUnit().name());
		vo.setRecertificationEnabled(program.isRecertificationEnabled());
		vo.setRecertificationWindow(program.getRecertificationWindow());
		vo.setRecertificationWindowUnit(program.getRecertificationWindowUnit() == null ? null : program.getRecertificationWindowUnit().name());
		vo.setCreditPoints(program.getCreditPoints());
		vo.setCreditPointSystemKey(program.getCreditPointSystem() == null ? null : program.getCreditPointSystem().getKey());
		vo.setTemplateKey(program.getTemplate() == null ? null : program.getTemplate().getKey());
		if(program.getOrganisations() != null) {
			List<Long> orgKeys = program.getOrganisations().stream()
					.map(CertificationProgramToOrganisation::getOrganisation)
					.map(org -> org.getKey())
					.collect(Collectors.toList());
			vo.setOrganisationKeys(orgKeys);
		}
		return vo;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isValidityEnabled() {
		return validityEnabled;
	}

	public void setValidityEnabled(boolean validityEnabled) {
		this.validityEnabled = validityEnabled;
	}

	public int getValidityTimelapse() {
		return validityTimelapse;
	}

	public void setValidityTimelapse(int validityTimelapse) {
		this.validityTimelapse = validityTimelapse;
	}

	public String getValidityTimelapseUnit() {
		return validityTimelapseUnit;
	}

	public void setValidityTimelapseUnit(String validityTimelapseUnit) {
		this.validityTimelapseUnit = validityTimelapseUnit;
	}

	public boolean isRecertificationEnabled() {
		return recertificationEnabled;
	}

	public void setRecertificationEnabled(boolean recertificationEnabled) {
		this.recertificationEnabled = recertificationEnabled;
	}

	public int getRecertificationWindow() {
		return recertificationWindow;
	}

	public void setRecertificationWindow(int recertificationWindow) {
		this.recertificationWindow = recertificationWindow;
	}

	public String getRecertificationWindowUnit() {
		return recertificationWindowUnit;
	}

	public void setRecertificationWindowUnit(String recertificationWindowUnit) {
		this.recertificationWindowUnit = recertificationWindowUnit;
	}

	public BigDecimal getCreditPoints() {
		return creditPoints;
	}

	public void setCreditPoints(BigDecimal creditPoints) {
		this.creditPoints = creditPoints;
	}

	public Long getCreditPointSystemKey() {
		return creditPointSystemKey;
	}

	public void setCreditPointSystemKey(Long creditPointSystemKey) {
		this.creditPointSystemKey = creditPointSystemKey;
	}

	public Long getTemplateKey() {
		return templateKey;
	}

	public void setTemplateKey(Long templateKey) {
		this.templateKey = templateKey;
	}

	public List<Long> getOrganisationKeys() {
		return organisationKeys;
	}

	public void setOrganisationKeys(List<Long> organisationKeys) {
		this.organisationKeys = organisationKeys;
	}

	@Override
	public int hashCode() {
		return key == null ? 3867 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CertificationProgramVO other) {
			return key != null && key.equals(other.key);
		}
		return false;
	}
}
