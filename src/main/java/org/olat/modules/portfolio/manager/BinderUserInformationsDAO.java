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
package org.olat.modules.portfolio.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.BinderUserInformations;
import org.olat.modules.portfolio.model.BinderUserInfosImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 16 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BinderUserInformationsDAO {
	
	private static final OLog log = Tracing.createLoggerFor(BinderUserInformationsDAO.class);

	@Autowired
	private DB dbInstance;
	
	/**
	 * Update (or create if not exists) the course informations for a user. To creation
	 * of the user infos is made with doInSync.
	 * 
	 * @param binder The binder
	 * @param identity The identity
	 */
	public void updateBinderUserInformations(final Binder binder, final Identity identity) {
		int updatedRows = lowLevelUpdate(binder, identity);
		dbInstance.commit();//to make it quick
		if(updatedRows == 0) {
			OLATResourceable lockRes = OresHelper.createOLATResourceableInstance("BinderLaunchDate::Identity", identity.getKey());
			CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(lockRes, () -> {
				try {
					int retryUpdatedRows = lowLevelUpdate(binder, identity);
					if(retryUpdatedRows == 0) {
						createAndPersistUserInfos(binder, identity);
					}
				} catch (Exception e) {
					log.error("Cannot update binder informations for: " + identity + " from " + identity, e);
				}
			});
		}
	}
	
	/**
	 * Update or create the user informations for the specified binder and
	 * identity. This method need to be called within a doInSync.
	 * 
	 * @param binder
	 * @param identity
	 */
	public void updateBinderUserInformationsInSync(final Binder binder, final Identity identity) {
		int updatedRows = lowLevelUpdate(binder, identity);
		dbInstance.commit();//to make it quick
		if(updatedRows == 0) {
			createAndPersistUserInfos(binder, identity);
		}
	}
	
	private void createAndPersistUserInfos(Binder binder, Identity identity) {
		BinderUserInfosImpl infos = new BinderUserInfosImpl();
		infos.setIdentity(identity);
		infos.setCreationDate(new Date());
		infos.setInitialLaunch(infos.getCreationDate());
		infos.setLastModified(infos.getCreationDate());
		infos.setRecentLaunch(infos.getCreationDate());
		infos.setVisit(1);
		infos.setBinder(binder);
		dbInstance.getCurrentEntityManager().persist(infos);
	}
	
	/**
	 * Execute the update statement
	 * @param courseResource
	 * @param identity
	 * @return
	 */
	protected int lowLevelUpdate(BinderRef binder, IdentityRef identity) {
		return dbInstance.getCurrentEntityManager().createNamedQuery("updateBinderLaunchDates")
			.setParameter("identityKey", identity.getKey())
			.setParameter("binderKey", binder.getKey())
			.setParameter("now", new Date())
			.executeUpdate();
	}
	
	public BinderUserInformations getBinderUserInfos(BinderRef binder, IdentityRef identity) {
		List<BinderUserInformations> infos = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadBinderUserInfosByBinderAndIdentity", BinderUserInformations.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("binderKey", binder.getKey())
				.getResultList();
		return infos != null && !infos.isEmpty() ? infos.get(0) : null;
	}
	
	public int deleteBinderUserInfos(BinderRef binder) {
		String delQuery = "delete from pfbinderuserinfos uinfos where uinfos.binder.key=:binderKey";
		return dbInstance.getCurrentEntityManager().createQuery(delQuery)
				.setParameter("binderKey", binder.getKey())
				.executeUpdate();
	}
}
