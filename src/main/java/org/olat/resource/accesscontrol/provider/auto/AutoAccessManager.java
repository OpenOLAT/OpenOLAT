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
package org.olat.resource.accesscontrol.provider.auto;

import java.util.Collection;

import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;

/**
 * Manager for the automatic access control provider. This provider allows to
 * save proposed assignments of users to a course by a third party service like
 * Shibboleth, REST, LDAP etc. The module effectively assigns the user to a
 * course if the course for the saved key is available.
 *
 * Initial date: 14.08.2017<br>
 *
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface AutoAccessManager {

	/**
	 * Create a pending advance order for the combination of every identifier
	 * key and every single identifier value in the input. You can specify an
	 * parser to split the raw input value in singles values.
	 *
	 * @param input
	 */
	public void createAdvanceOrders(AdvanceOrderInput input);
	
	public Collection<AdvanceOrder> loadAdvanceOrders(AdvanceOrderSearchParams searchParams);

	/**
	 * Load all pending advance orders for a ReposiotyEntry.
	 *
	 * @param resourse
	 */
	public Collection<AdvanceOrder> loadPendingAdvanceOrders(RepositoryEntry entry);

	/**
	 * Delete an advance order.
	 *
	 * @param advanceOrder
	 */
	public void deleteAdvanceOrder(AdvanceOrder advanceOrder);

	/**
	 * Delete all advance order of a user.
	 *
	 * @param identity
	 */
	public void deleteAdvanceOrders(Identity identity);

	/**
	 * Load all pending advance orders for the identity and try to grant access.
	 *
	 * @param identity
	 */
	public void grantAccessToCourse(Identity identity);

	/**
	 * Load all pending advance orders for the RepositoryEntry and try to grant access.
	 *
	 * @param repositoryEntry
	 */
	public void grantAccess(RepositoryEntry entry);

	/**
	 * Try to grant access to a resource for the identity in the pending advance
	 * orders. If the resource is found and the access is granted, the status of the
	 * advance order is set to done. If no resource for the identifier key and
	 * value is found, the advance order remains pending.
	 *
	 * @param advanceOrders
	 */
	public void grantAccess(Collection<AdvanceOrder> advancedOrders);

}