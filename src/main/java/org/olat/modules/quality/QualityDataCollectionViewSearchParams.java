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
package org.olat.modules.quality;

import java.util.Collection;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.OrganisationRef;

/**
 * 
 * Initial date: 07.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityDataCollectionViewSearchParams {
	
	private QualityDataCollectionRef dataCollectionRef;
	private Collection<? extends OrganisationRef> organsationRefs;
	private IdentityRef reportAccessIdentity;

	public QualityDataCollectionRef getDataCollectionRef() {
		return dataCollectionRef;
	}

	public void setDataCollectionRef(QualityDataCollectionRef dataCollectionRef) {
		this.dataCollectionRef = dataCollectionRef;
	}

	public Collection<? extends OrganisationRef> getOrgansationRefs() {
		return organsationRefs;
	}

	public void setOrgansationRefs(Collection<? extends OrganisationRef> organsationRefs) {
		this.organsationRefs = organsationRefs;
	}

	public IdentityRef getReportAccessIdentity() {
		return reportAccessIdentity;
	}

	public void setReportAccessIdentity(IdentityRef identityRef) {
		this.reportAccessIdentity = identityRef;
	}

}
