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
package org.olat.user;

import java.util.Date;
import java.util.Set;

import org.olat.core.id.Identity;
import org.olat.core.id.IdentityLifecycle;

/**
 * 
 * Initial date: 4 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface UserLifecycleManager {
	

	public long getDaysUntilDeactivation(IdentityLifecycle identity, Date referenceDate);
	
	public Date getDateUntilDeactivation(IdentityLifecycle identity);
	
	public Date getDateUntilDeactivation(IdentityLifecycle identity, Date login);
	
	public long getDaysUntilDeletion(IdentityLifecycle identity, Date referenceDate);

	public Date getDateUntilDeletion(IdentityLifecycle identity);
	
	/**
	 * Check if there are identities which expiration dates are .
	 * 
	 * @param vetoed Build a list to ignore.
	 */
	public void expiredIdentities(Set<Identity> vetoed);
	
	/**
	 * Check if there are identities to deactivate.
	 * 
	 * @param vetoed Build a list to ignore.
	 */
	public void inactivateIdentities(Set<Identity> vetoed);
	
	public void deleteIdentities(Set<Identity> vetoed);
	
	/**
	 * Delete all user-data in registered deleteable resources.
	 * 
	 * @param identity The identity to delete
	 * @param doer The identity which want to delete someone (optional)
	 * @return true: delete was successful; false: delete could not finish
	 */
	public boolean deleteIdentity(Identity identity, Identity doer);
	
	
	public boolean updatePlannedInactivationDates();

}
