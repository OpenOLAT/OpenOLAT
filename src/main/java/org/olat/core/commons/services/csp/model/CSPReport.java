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
package org.olat.core.commons.services.csp.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The mapping class used by Jackson to parse
 * the report.
 * 
 * Initial date: 19 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@JsonIgnoreType(true)
@XmlAccessorType(XmlAccessType.FIELD)
public class CSPReport {
	
	@XmlAttribute(name = "blocked-uri")
	@com.fasterxml.jackson.annotation.JsonProperty("blocked-uri")
	private String blockedUri;
	@XmlAttribute(name = "disposition")
	@JsonProperty("disposition")
	private String disposition;
	@XmlAttribute(name = "document-uri")
	@JsonProperty("document-uri")
	private String documentUri;
	@XmlAttribute(name = "effective-directive")
	@JsonProperty("effective-directive")
	private String effectiveDirective;
	@XmlAttribute(name = "original-policy")
	@JsonProperty("original-policy")
	private String originalPolicy;
	@XmlAttribute(name = "referrer")
	@JsonProperty("referrer")
	private String referrer;
	@XmlAttribute(name = "script-sample")
	@JsonProperty("script-sample")
	private String scriptSample;
	@XmlAttribute(name = "status-code")
	@JsonProperty("status-code")
	private String statusCode;
	@XmlAttribute(name = "violated-directive")
	@JsonProperty("violated-directive")
	private String violatedDirective;
	@XmlAttribute(name = "source-file")
	@JsonProperty("source-file")
	private String sourceFile;
	@XmlAttribute(name = "line-number")
	@JsonProperty("line-number")
	private String lineNumber;
	@XmlAttribute(name = "column-number")
	@JsonProperty("column-number")
	private String columnNumber;
	
	public String getBlockedUri() {
		return blockedUri;
	}
	
	public void setBlockedUri(String blockedUri) {
		this.blockedUri = blockedUri;
	}
	
	public String getDisposition() {
		return disposition;
	}
	
	public void setDisposition(String disposition) {
		this.disposition = disposition;
	}
	
	public String getDocumentUri() {
		return documentUri;
	}
	
	public void setDocumentUri(String documentUri) {
		this.documentUri = documentUri;
	}
	
	public String getEffectiveDirective() {
		return effectiveDirective;
	}
	
	public void setEffectiveDirective(String effectiveDirective) {
		this.effectiveDirective = effectiveDirective;
	}
	
	public String getOriginalPolicy() {
		return originalPolicy;
	}
	
	public void setOriginalPolicy(String originalPolicy) {
		this.originalPolicy = originalPolicy;
	}
	
	public String getReferrer() {
		return referrer;
	}
	
	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}
	
	public String getScriptSample() {
		return scriptSample;
	}
	
	public void setScriptSample(String scriptSample) {
		this.scriptSample = scriptSample;
	}
	
	public String getStatusCode() {
		return statusCode;
	}
	
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	
	public String getViolatedDirective() {
		return violatedDirective;
	}
	
	public void setViolatedDirective(String violatedDirective) {
		this.violatedDirective = violatedDirective;
	}

	public String getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getColumnNumber() {
		return columnNumber;
	}

	public void setColumnNumber(String columnNumber) {
		this.columnNumber = columnNumber;
	}

	public String getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}


	

}
