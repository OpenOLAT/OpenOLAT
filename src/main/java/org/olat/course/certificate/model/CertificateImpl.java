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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

/**
 * 
 * Initial date: 21.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="certificate")
@Table(name="o_cer_certificate")
public class CertificateImpl extends AbstractCertificate {

	private static final long serialVersionUID = 2360631986446191873L;

	@Column(name="c_archived_resource_id", nullable=false, insertable=true, updatable=false)
	private Long archivedResourceKey;
	
	@ManyToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_olatresource", nullable=false, insertable=true, updatable=true)
	private OLATResource olatResource;

	@Override
	public Long getArchivedResourceKey() {
		return archivedResourceKey;
	}

	public void setArchivedResourceKey(Long archivedResourceKey) {
		this.archivedResourceKey = archivedResourceKey;
	}

	public OLATResource getOlatResource() {
		return olatResource;
	}

	public void setOlatResource(OLATResource olatResource) {
		this.olatResource = olatResource;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CertificateImpl) {
			CertificateImpl cert = (CertificateImpl)obj;
			return getKey() != null && getKey().equals(cert.getKey());
		}
		return false;
	}
}