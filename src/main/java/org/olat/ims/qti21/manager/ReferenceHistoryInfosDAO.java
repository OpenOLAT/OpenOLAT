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
package org.olat.ims.qti21.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.ims.qti21.model.ReferenceHistoryWithInfos;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 4 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class ReferenceHistoryInfosDAO {
	
	@Autowired
	private DB dbInstance;
	
	public List<ReferenceHistoryWithInfos> getReferenceHistoryWithInfos(RepositoryEntry courseEntry, String subIdent) {
		String sb = """
				select testEntry, hist.creationDate, ident,
				(select count(testSession.key) from qtiassessmenttestsession as testSession
				 where testSession.testEntry.key=testEntry.key and testSession.repositoryEntry.key=:sourceEntryKey and testSession.subIdent=:subIdent
				 and testSession.authorMode=false
				) as runs
				from referenceshistory hist
				inner join hist.target as target
				inner join repositoryentry as testEntry on (target.key=testEntry.olatResource.key)
				inner join fetch testEntry.olatResource as tres
				left join hist.identity as ident
				left join fetch ident.user as identUser
				where hist.source.key=:sourceKey and hist.userdata=:subIdent
				order by hist.creationDate desc""";
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
			.createQuery(sb, Object[].class)
			.setParameter("sourceKey", courseEntry.getOlatResource().getKey())
			.setParameter("sourceEntryKey", courseEntry.getKey())
			.setParameter("subIdent", subIdent)
			.getResultList();
		List<ReferenceHistoryWithInfos> infos = new ArrayList<>();
		for(Object[] arr:objects) {
			RepositoryEntry entry = (RepositoryEntry)arr[0];
			Date date = (Date)arr[1];
			Identity doer = (Identity)arr[2];
			long runs = PersistenceHelper.extractLong(arr, 3);
			infos.add(new ReferenceHistoryWithInfos(entry, date, doer, runs));
		}
		return infos;
	}
}
