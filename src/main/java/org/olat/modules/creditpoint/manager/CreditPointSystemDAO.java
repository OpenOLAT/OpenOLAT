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
package org.olat.modules.creditpoint.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.modules.creditpoint.CreditPointExpirationType;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointSystemStatus;
import org.olat.modules.creditpoint.model.CreditPointSystemImpl;
import org.olat.modules.creditpoint.model.CreditPointSystemInfos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * 
 * Initial date: 3 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CreditPointSystemDAO {
	
	@Autowired
	private DB dbInstance;
	
	public CreditPointSystem createSystem(String name, String label,
			Integer defaultExpiration, CreditPointExpirationType defaultExpirationType) {
		CreditPointSystemImpl system = new CreditPointSystemImpl();
		system.setCreationDate(new Date());
		system.setLastModified(system.getCreationDate());
		system.setName(name);
		system.setLabel(label);
		system.setDefaultExpiration(defaultExpiration);
		system.setDefaultExpirationUnit(defaultExpirationType);
		system.setStatus(CreditPointSystemStatus.active);
		dbInstance.getCurrentEntityManager().persist(system);
		return system;
	}
	
	public CreditPointSystem updateSystem(CreditPointSystem system) {
		((CreditPointSystemImpl)system).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(system);
	}
	
	public List<CreditPointSystem> loadCreditPointSystems() {
		String query = "select sys from creditpointsystem sys";
		return dbInstance.getCurrentEntityManager().createQuery(query, CreditPointSystem.class)
				.getResultList();
	}
	
	public CreditPointSystem loadCreditPointSystem(Long systemKey) {
		String query = "select sys from creditpointsystem sys where sys.key=:systemKey";
		List<CreditPointSystem> systems = dbInstance.getCurrentEntityManager().createQuery(query, CreditPointSystem.class)
				.setParameter("systemKey", systemKey)
				.getResultList();
		return systems == null || systems.isEmpty() ? null : systems.get(0);
	}
	
	public List<CreditPointSystemInfos> loadCreditPointSystemsWithInfos() {
		String query = """
			select sys,
			(select count(wallet.key) from creditpointwallet as wallet
			  where wallet.creditPointSystem.key=sys.key
			) as numOfWallets
			from creditpointsystem sys""";
		
		List<Object[]> rawObjectsList = dbInstance.getCurrentEntityManager().createQuery(query, Object[].class)
				.getResultList();
		List<CreditPointSystemInfos> infos = new ArrayList<>(rawObjectsList.size());
		for(Object[] rawObjects:rawObjectsList) {
			CreditPointSystem system = (CreditPointSystem)rawObjects[0];
			long usage = PersistenceHelper.extractLong(rawObjects, 1);
			infos.add(new CreditPointSystemInfos(system, usage));
		}
		return infos;
	}
}
