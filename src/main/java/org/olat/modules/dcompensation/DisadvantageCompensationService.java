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
package org.olat.modules.dcompensation;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.modules.dcompensation.DisadvantageCompensationAuditLog.Action;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 22 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface DisadvantageCompensationService {
	
	public DisadvantageCompensation createDisadvantageCompensation(Identity identity,
			Integer extraTime, String approvedBy, Date approval, Identity creator, RepositoryEntry entry,
			String subIdent, String subIdentName);
	
	public DisadvantageCompensation updateDisadvantageCompensation(DisadvantageCompensation compensation);
	
	public List<DisadvantageCompensation> getDisadvantageCompensations(IdentityRef identity);
	
	public List<DisadvantageCompensation> getDisadvantageCompensations(RepositoryEntryRef entry, String subIdent);
	
	public List<DisadvantageCompensation> getDisadvantageCompensations(IdentityRef identity, RepositoryEntryRef entry, String subIdent);
	
	/**
	 * 
	 * @param identity The disadvantaged user
	 * @param entry The course
	 * @param subIdent The course element identifier
	 * @return The first active compensation
	 */
	public DisadvantageCompensation getActiveDisadvantageCompensation(IdentityRef identity, RepositoryEntryRef entry, String subIdent);
	
	public boolean isActiveDisadvantageCompensation(IdentityRef identity, RepositoryEntryRef entry, List<String> subIdents);
	
	/**
	 * @param entry The course
	 * @param subIdents A list of course elements (optional, null means all)
	 * @return A list of users with compensation for disadvantages.
	 */
	public List<IdentityRef> getActiveDisadvantagedUsers(RepositoryEntryRef entry, List<String> subIdents);
	
	
	public String toXml(DisadvantageCompensation compensation);
	
	public void auditLog(Action action, String before, String after, DisadvantageCompensation compensation, IdentityRef doer);
	
	public List<DisadvantageCompensationAuditLog> getAuditLogs(IdentityRef identity, RepositoryEntryRef entry, String subIdent);

}
