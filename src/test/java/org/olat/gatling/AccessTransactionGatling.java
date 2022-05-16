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
package org.olat.gatling;

import java.util.List;

import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.manager.ACMethodDAO;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.FreeAccessMethod;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Generate a lot of transactions.
 * 
 * Initial date: 29.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AccessTransactionGatling extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private ACMethodDAO acMethodManager;
	@Autowired
	private BaseSecurity securityManager;
	
	
	@Test
	public void generateDatas() {
		
		//pick up a method
		List<AccessMethod> methods = acMethodManager.getAvailableMethodsByType(FreeAccessMethod.class);
		AccessMethod method = methods.get(0);
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select v from repositoryentry as v ")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where ores.resName='CourseModule' and v.status ").in(RepositoryEntryStatusEnum.preparationToClosed());
		
		List<RepositoryEntry> courses= dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.getResultList();
		
		List<Identity> loadIdentities = securityManager
				.getVisibleIdentitiesByPowerSearch(null, null, false, null, null, null, null, 0, 40000);
		
		for(RepositoryEntry course:courses) {
			try {
				List<Offer> offers = acService.findOfferByResource(course.getOlatResource(), true, null, null);
				if(offers.isEmpty()) {
					OLATResource randomOres = course.getOlatResource();
					Offer offer = acService.createOffer(randomOres, "Free " + course.getDisplayname());
					offer.setAutoBooking(true);
					OfferAccess link = acService.createOfferAccess(offer, method);
					offer = acService.save(offer);
					acService.saveOfferAccess(link);
					dbInstance.commit();

					int fromIndex = (int)(Math.random() * loadIdentities.size() - 1);
					int length = (int)(Math.random() * 200);
					int toIndex = Math.min(loadIdentities.size() - 1, fromIndex + length);

					List<Identity> identities = loadIdentities.subList(fromIndex, toIndex);
					for(Identity identity:identities) {
						acService.isAccessible(course, identity, Boolean.FALSE, false, true);
						dbInstance.commit();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				dbInstance.commitAndCloseSession();
			}
		}
	}
}
