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
package org.olat.modules.dcompensation.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.modules.dcompensation.DisadvantageCompensationAuditLog;
import org.olat.modules.dcompensation.DisadvantageCompensationAuditLog.Action;
import org.olat.modules.dcompensation.DisadvantageCompensationService;
import org.olat.modules.dcompensation.model.DisadvantageCompensationImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DisadvantageCompensationServiceImpl implements DisadvantageCompensationService {
	
	@Autowired
	private DisadvantageCompensationDAO disadvantageCompensationDao;
	@Autowired
	private DisadvantageCompensationAuditLogDAO disadvantageCompensationAuditLogDao;
	
	@Override
	public DisadvantageCompensation createDisadvantageCompensation(Identity identity,
			Integer extraTime, String approvedBy, Date approval, Identity creator,
			RepositoryEntry entry, String subIdent, String subIdentName) {
		return disadvantageCompensationDao.createDisadvantageCompensation(identity,
				extraTime, approvedBy, approval, creator, entry, subIdent, subIdentName);
	}

	@Override
	public DisadvantageCompensation updateDisadvantageCompensation(DisadvantageCompensation compensation) {
		return disadvantageCompensationDao.updateDisadvantageCompensation(compensation);
	}
	
	/**
	 * Fetch all repository informations (resource, statistics...)
	 */
	@Override
	public List<DisadvantageCompensation> getDisadvantageCompensations(IdentityRef identity) {
		return disadvantageCompensationDao.getDisadvantageCompensations(identity);
	}
	
	@Override
	public List<DisadvantageCompensation> getActiveDisadvantageCompensations(RepositoryEntryRef entry, String subIdent) {
		return disadvantageCompensationDao.getActiveDisadvantageCompensations(entry, subIdent);
	}

	@Override
	public List<DisadvantageCompensation> getDisadvantageCompensations(IdentityRef identity, RepositoryEntryRef entry, String subIdent) {
		return disadvantageCompensationDao.getDisadvantageCompensations(identity, entry, subIdent);
	}

	@Override
	public DisadvantageCompensation getActiveDisadvantageCompensation(IdentityRef identity, RepositoryEntryRef entry, String subIdent) {
		return disadvantageCompensationDao.getActiveDisadvantageCompensation(identity, entry, subIdent);
	}

	@Override
	public boolean isActiveDisadvantageCompensation(IdentityRef identity, RepositoryEntryRef entry, List<String> subIdents) {
		return disadvantageCompensationDao.isActiveDisadvantagedUser(identity, entry, subIdents);
	}

	@Override
	public List<IdentityRef> getActiveDisadvantagedUsers(RepositoryEntryRef entry, List<String> subIdents) {
		return disadvantageCompensationDao.getActiveDisadvantagedUsers(entry, subIdents);
	}

	@Override
	public String toXml(DisadvantageCompensation compensation) {
		return disadvantageCompensationAuditLogDao.toXml((DisadvantageCompensationImpl)compensation);
	}

	@Override
	public void auditLog(Action action, String before, String after, DisadvantageCompensation compensation, IdentityRef doer) {
		disadvantageCompensationAuditLogDao.create(action.name(), before, after, compensation, doer);
	}

	@Override
	public List<DisadvantageCompensationAuditLog> getAuditLogs(IdentityRef identity, RepositoryEntryRef entry, String subIdent) {
		return disadvantageCompensationAuditLogDao.getAuditLogs(identity, entry, subIdent);
	}
}
