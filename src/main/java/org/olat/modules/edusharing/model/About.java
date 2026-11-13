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
package org.olat.modules.edusharing.model;

import org.olat.modules.edusharing.EdusharingSecurityService;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * Initial date: 13 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class About {
	
	private AboutVersion version;
	private String[] signatureAlgorithms;
	
	public AboutVersion getVersion() {
		return version;
	}
	
	public void setVersion(AboutVersion version) {
		this.version = version;
	}
	
	public String[] getSignatureAlgorithms() {
		return signatureAlgorithms;
	}
	
	public void setSignatureAlgorithms(String[] signatureAlgorithms) {
		this.signatureAlgorithms = signatureAlgorithms;
	}
	
	public static String getSignatureAlgorithm(About about) {
		return about != null && about.getSignatureAlgorithms() != null && about.getSignatureAlgorithms().length > 0
				? EdusharingSecurityService.SIGNATURE_SHA512
				: EdusharingSecurityService.SIGNATURE_SHA1;
	}
	
}
