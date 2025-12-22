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
package org.olat.modules.certificationprogram.ui.wizard;

import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.certificationprogram.CertificationProgram;

/**
 * 
 * Initial date: 11 déc. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AddProgramMembersContext {
	
	private final List<Long> currentMembers;
	private final CertificationProgram program;
	
	private List<Identity> searchedIdentities;
	private List<UserToCertify> selectedIdentities;
	private List<Identity> identitiesToCertify;
	
	private Date issuedDate;
	
	public AddProgramMembersContext(List<Long> currentMembers, CertificationProgram program) {
		this.currentMembers = currentMembers;
		this.program = program;
	}

	public List<Long> getCurrentMembers() {
		return currentMembers;
	}

	public CertificationProgram getProgram() {
		return program;
	}

	public List<Identity> getSearchedIdentities() {
		return searchedIdentities;
	}

	public void setSearchedIdentities(List<Identity> searchedIdentities) {
		this.searchedIdentities = searchedIdentities;
	}

	public List<UserToCertify> getSelectedIdentities() {
		return selectedIdentities;
	}

	public void setSelectedIdentities(List<UserToCertify> selectedIdentities) {
		this.selectedIdentities = selectedIdentities;
	}

	public List<Identity> getIdentitiesToCertify() {
		return identitiesToCertify;
	}

	public void setIdentitiesToCertify(List<Identity> identitiesToCertify) {
		this.identitiesToCertify = identitiesToCertify;
	}

	public Date getIssuedDate() {
		return issuedDate;
	}

	public void setIssuedDate(Date issuedDate) {
		this.issuedDate = issuedDate;
	}
}
