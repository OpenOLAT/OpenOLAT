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
package org.olat.modules.certificationprogram.model;

import org.olat.modules.certificationprogram.CertificationProgramRef;

/**
 * 
 * Initial date: 3 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramMemberSearchParameters {

	private Type type;
	private OrderBy orderBy;
	private boolean orderAsc;
	private CertificationProgramRef certificationProgram;
	
	public CertificationProgramMemberSearchParameters(CertificationProgramRef certificationProgram) {
		this.certificationProgram = certificationProgram;
	}

	public CertificationProgramRef getCertificationProgram() {
		return certificationProgram;
	}

	public void setCertificationProgram(CertificationProgramRef certificationProgram) {
		this.certificationProgram = certificationProgram;
	}
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public OrderBy getOrderBy() {
		return orderBy;
	}

	public boolean isOrderAsc() {
		return orderAsc;
	}

	public void setOrderBy(OrderBy orderBy, boolean orderAsc) {
		this.orderBy = orderBy;
		this.orderAsc = orderAsc;
	}

	public enum Type {
		CERTIFIED,
		CERTIFYING,
		REMOVED
	}
	
	public enum OrderBy {
		NEXTRECERTIFICATIONDATE,
		CREATIONDATE
	}

}
