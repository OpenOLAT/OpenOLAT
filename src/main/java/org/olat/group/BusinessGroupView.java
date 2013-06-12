/**
 * <a href=“http://www.openolat.org“>
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
 * 2011 by frentix GmbH, http://www.frentix.com
 * <p>
**/
package org.olat.group;

import java.util.Date;

import org.olat.basesecurity.SecurityGroup;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;
import org.olat.resource.OLATResource;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface BusinessGroupView extends BusinessGroupShort, Persistable, CreateInfo, ModifiedInfo, OLATResourceable {

	/**
	 * @return The group display name (not system unique)
	 */
	public String getName();

	/**
	 * @return The group description or NULL if none set.
	 */
	public String getDescription();
	
	/**
	 * @return The associated resource
	 */
	public OLATResource getResource();
	
	
	public long getNumOfRelations();
	
	public long getNumOfOwners();
	
	public long getNumOfParticipants();
	
	/**
	 * Number of pending reservations
	 * @return
	 */
	public long getNumOfPendings();
	
	public long getNumWaiting();
	
	/**
	 * @return The number of offers linked to this group (absolute number)
	 */
	public long getNumOfOffers();
	
	/**
	 * @return The number of currently valid offers
	 */
	public long getNumOfValidOffers();

	/**
	 * The BusinessGroup has 1..n Owners acting as <i>administrators </i>.
	 * 
	 * @return the owners
	 */
	public SecurityGroup getOwnerGroup();

	/**
	 * The BusinessGroup has 0..n Partipiciants.
	 * 
	 * @return the partipiciants
	 */
	public SecurityGroup getPartipiciantGroup();

	/**
	 * The BusinessGroup has 0..n people in the waiting group.
	 * 
	 * @return the waiting group
	 */
	public SecurityGroup getWaitingGroup();

	/**
	 * @return last usage of this group
	 */
	public Date getLastUsage();

	/**
	 * @return the maximal number of participants
	 */
	public Integer getMaxParticipants();

	/**
	 * @return the minimal number of participants
	 */
	public Integer getMinParticipants();
	
	/**
	 * @return true: if the waiting list will automaticly close ranks to participant list 
	 */
	public Boolean getAutoCloseRanksEnabled();

	/**
	 * @return true: if waiting-list is enabled
	 */
	public Boolean getWaitingListEnabled();

	
}
