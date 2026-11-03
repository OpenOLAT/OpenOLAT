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

import org.olat.course.disclaimer.model.CourseDisclaimerSettings.Disclaimer;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One text of the course disclaimer: the terms of use or the data protection declaration.
 *
 * Initial date: 2 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "courseDisclaimerTextVO")
public class CourseDisclaimerTextVO {

	@Schema(description = "The participants must accept this text to enter the course. If null on update, the stored value is not changed.")
	private Boolean enabled;
	@Schema(description = "Mandatory if enabled, max. 255 characters")
	private String title;
	@Schema(description = "HTML, mandatory if enabled, max. 30'000 characters")
	private String terms;
	@Schema(description = "Label of the first checkbox, mandatory if enabled, max. 255 characters")
	private String label1;
	@Schema(description = "Label of the second checkbox, optional, max. 255 characters")
	private String label2;

	public CourseDisclaimerTextVO() {
		//make JAXB happy
	}

	public static CourseDisclaimerTextVO valueOf(Disclaimer disclaimer) {
		CourseDisclaimerTextVO vo = new CourseDisclaimerTextVO();
		vo.setEnabled(Boolean.valueOf(disclaimer.isEnabled()));
		vo.setTitle(disclaimer.getTitle());
		vo.setTerms(disclaimer.getTerms());
		vo.setLabel1(disclaimer.getLabel1());
		vo.setLabel2(disclaimer.getLabel2());
		return vo;
	}

	/**
	 * @param storedDisclaimer The disclaimer currently saved in the course
	 * @return The disclaimer with the values of this VO, if enabled is null the
	 * 		value of the stored disclaimer is used
	 */
	public Disclaimer toDisclaimer(Disclaimer storedDisclaimer) {
		boolean enable = enabled == null ? storedDisclaimer.isEnabled() : enabled.booleanValue();
		return new Disclaimer(enable, title, terms, label1, label2);
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTerms() {
		return terms;
	}

	public void setTerms(String terms) {
		this.terms = terms;
	}

	public String getLabel1() {
		return label1;
	}

	public void setLabel1(String label1) {
		this.label1 = label1;
	}

	public String getLabel2() {
		return label2;
	}

	public void setLabel2(String label2) {
		this.label2 = label2;
	}
}
