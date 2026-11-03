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
package org.olat.course.disclaimer.restapi;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.course.disclaimer.model.CourseDisclaimerSettings;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The disclaimer configuration of a course.
 *
 * Initial date: 2 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "courseDisclaimerVO")
public class CourseDisclaimerVO {

	@Schema(description = "The terms of use. If null on update, the stored terms of use are not changed.")
	private CourseDisclaimerTextVO termsOfUse;
	@Schema(description = "The data protection declaration. If null on update, the stored declaration is not changed.")
	private CourseDisclaimerTextVO dataProtection;

	@Schema(description = "Number of consents of the course (read only)", accessMode = Schema.AccessMode.READ_ONLY)
	private Long consentCount;
	@Schema(description = "Course disclaimers are enabled in the system configuration (read only)", accessMode = Schema.AccessMode.READ_ONLY)
	private Boolean moduleEnabled;

	public CourseDisclaimerVO() {
		//make JAXB happy
	}

	public static CourseDisclaimerVO valueOf(CourseDisclaimerSettings settings, Long consentCount, boolean moduleEnabled) {
		CourseDisclaimerVO vo = new CourseDisclaimerVO();
		vo.setTermsOfUse(CourseDisclaimerTextVO.valueOf(settings.getTermsOfUse()));
		vo.setDataProtection(CourseDisclaimerTextVO.valueOf(settings.getDataProtection()));
		vo.setConsentCount(consentCount);
		vo.setModuleEnabled(Boolean.valueOf(moduleEnabled));
		return vo;
	}

	public CourseDisclaimerTextVO getTermsOfUse() {
		return termsOfUse;
	}

	public void setTermsOfUse(CourseDisclaimerTextVO termsOfUse) {
		this.termsOfUse = termsOfUse;
	}

	public CourseDisclaimerTextVO getDataProtection() {
		return dataProtection;
	}

	public void setDataProtection(CourseDisclaimerTextVO dataProtection) {
		this.dataProtection = dataProtection;
	}

	public Long getConsentCount() {
		return consentCount;
	}

	public void setConsentCount(Long consentCount) {
		this.consentCount = consentCount;
	}

	public Boolean getModuleEnabled() {
		return moduleEnabled;
	}

	public void setModuleEnabled(Boolean moduleEnabled) {
		this.moduleEnabled = moduleEnabled;
	}
}
