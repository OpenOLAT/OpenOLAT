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
package org.olat.course.certificate.model;

import java.io.Serializable;

import org.olat.course.certificate.CertificateTemplate;

/**
 * 
 * Initial date: 19.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificateWorkUnit implements Serializable {

	private static final long serialVersionUID = 4462884019283948487L;
	
	private Float score;
	private Float maxScore;
	private Boolean passed;
	private Double completion;
	private Long templateKey;
	private Long printTemplateKey;
	private boolean printTemplate;
	private Long certificateKey;
	private Long doerKey;
	private String grade;
	private CertificateConfig config;
	
	public CertificateWorkUnit() {
		//
	}
	
	public CertificateWorkUnit(Long certificateKey, Long templateKey, boolean printTemplate, Long printTemplateKey,
			Float score, Float maxScore, Boolean passed, Double completion, CertificateConfig config, Long doerKey) {
		this.score = score;
		this.maxScore = maxScore;
		this.passed = passed;
		this.completion = completion;
		this.config = config;
		this.templateKey = templateKey;
		this.printTemplate = printTemplate;
		this.printTemplateKey = printTemplateKey;
		this.certificateKey = certificateKey;
		this.doerKey = doerKey;
	}
	
	public static CertificateWorkUnit valueOf(CertificateTemplate template, boolean printTemplateEnabled, CertificateTemplate printTemplate,
			Float score, Float maxScore, Boolean passed, Double completion, String grade, CertificateConfig config, Long doerKey) {
		
		CertificateWorkUnit workUnit = new CertificateWorkUnit();
		if(template != null) {
			workUnit.setTemplateKey(template.getKey());
		}
		workUnit.setPrintTemplate(printTemplateEnabled);
		if(printTemplate != null) {
			workUnit.setPrintTemplateKey(printTemplate.getKey());
		}
		workUnit.setScore(score);
		workUnit.setMaxScore(maxScore);
		workUnit.setPassed(passed);
		workUnit.setCompletion(completion);
		workUnit.setConfig(config);
		workUnit.setGrade(grade);
		workUnit.setDoerKey(doerKey);
		
		return workUnit;
	}

	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	public Float getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(Float maxScore) {
		this.maxScore = maxScore;
	}

	public Boolean getPassed() {
		return passed;
	}

	public void setPassed(Boolean passed) {
		this.passed = passed;
	}

	public Double getCompletion() {
		return completion;
	}

	public void setCompletion(Double completion) {
		this.completion = completion;
	}

	public CertificateConfig getConfig() {
		return config;
	}

	public void setConfig(CertificateConfig config) {
		this.config = config;
	}

	public Long getTemplateKey() {
		return templateKey;
	}

	public void setTemplateKey(Long templateKey) {
		this.templateKey = templateKey;
	}

	public boolean isPrintTemplate() {
		return printTemplate;
	}

	public void setPrintTemplate(boolean printTemplate) {
		this.printTemplate = printTemplate;
	}

	public Long getPrintTemplateKey() {
		return printTemplateKey;
	}

	public void setPrintTemplateKey(Long printTemplateKey) {
		this.printTemplateKey = printTemplateKey;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}
	
	public Long getDoerKey() {
		return doerKey;
	}

	public void setDoerKey(Long doerKey) {
		this.doerKey = doerKey;
	}

	@Override
	public int hashCode() {
		return certificateKey == null ? 87580 : certificateKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CertificateWorkUnit work) {
			return certificateKey != null && certificateKey.equals(work.certificateKey);
		}
		return false;
	}
}