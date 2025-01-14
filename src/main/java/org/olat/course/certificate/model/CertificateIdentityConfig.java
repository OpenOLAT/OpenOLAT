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
package org.olat.course.certificate.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;

/**
 * Initial date: 2025-01-13<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CertificateIdentityConfig {
	private Certificate certificate;
	private Long identityKey;
	private Long entryKey;
	private List<String> identityProps;
	private RepositoryEntryCertificateConfiguration certificateConfig;

	public void setCertificate(Certificate certificate) {
		this.certificate = certificate;
	}

	public Certificate getCertificate() {
		return certificate;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setEntryKey(Long entryKey) {
		this.entryKey = entryKey;
	}

	public Long getEntryKey() {
		return entryKey;
	}

	public void setIdentityProp(int index, String value) {
		if (identityProps == null) {
			identityProps = new ArrayList<>();
		}
		while (identityProps.size() < index) {
			identityProps.add(null);
		}
		if (index < identityProps.size()) {
			identityProps.set(index, value);
		} else {
			identityProps.add(value);
		}
	}
	
	public String getIdentityProp(int index) {
		if (identityProps == null || index >= identityProps.size()) {
			return "";
		}
		return identityProps.get(index);
	}

	public void setConfig(RepositoryEntryCertificateConfiguration certificateConfig) {
		this.certificateConfig = certificateConfig;
	}

	public RepositoryEntryCertificateConfiguration getConfig() {
		return certificateConfig;
	}
}
