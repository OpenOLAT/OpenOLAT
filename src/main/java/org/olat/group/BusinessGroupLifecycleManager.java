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
package org.olat.group;

import java.util.Date;
import java.util.Set;

import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 9 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface BusinessGroupLifecycleManager {
	
	public Identity getInactivatedBy(BusinessGroupRef businessGroup);
	
	public Identity getSoftDeletedBy(BusinessGroupRef businessGroup);
	
	
	public Date getInactivationDate(BusinessGroupLifecycle businessGroup);
	
	public Date getInactivationEmailDate(BusinessGroupLifecycle businessGroup);
	
	/**
	 * 
	 * @param businessGroup The business group
	 * @return Number of days used of the delay
	 */
	public long getInactivationResponseDelayUsed(BusinessGroup businessGroup);
	
	public Date getSoftDeleteDate(BusinessGroupLifecycle businessGroup);
	
	public Date getSoftDeleteEmailDate(BusinessGroup businessGroup);
	
	public long getSoftDeleteResponseDelayUsed(BusinessGroup businessGroup);
	
	public Date getDefinitiveDeleteDate(BusinessGroupLifecycle businessGroup);
	
	public void inactivateAutomaticallyBusinessGroups(Set<BusinessGroup> vetoed);
	
	/**
	 * The business group need to meet several conditions to be inactivated, last usage
	 * must be greater than the number of days without a visit of one of its members,
	 * the email must have been sent the number of days specified by the configuration,
	 * not be in the reactivation period and not be in the veto list.
	 * 
	 * @param vetoed A list of business group which are excluded of the inactivation
	 */
	public void inactivateBusinessGroupsAfterResponseTime(Set<BusinessGroup> vetoed);
	
	public void softDeleteAutomaticallyBusinessGroups(Set<BusinessGroup> vetoed);
	
	public void softDeleteBusinessGroupsAfterResponseTime(Set<BusinessGroup> vetoed);

	public void definitivelyDeleteBusinessGroups(Set<BusinessGroup> vetoed);
	

	/**
	 * Change the status of the business group. Add an entry in log table.
	 * 
	 * @param businessGroup The business group
	 * @param inactivatedBy The user which does the action
	 * @param withMail Send notification per E-mail
	 * @return Mail results
	 */
	public BusinessGroup inactivateBusinessGroup(BusinessGroup businessGroup, Identity doer, boolean withMail);
	
	/**
	 * 
	 * @param businessGroup The business group
	 * @return The merged business group
	 */
	public BusinessGroup sendInactivationEmail(BusinessGroup businessGroup);
	
	/**
	 * Change the status to active after the business group was inactivated or soft deleted.
	 * 
	 * @param businessGroup The business group
	 * @return The merged business group
	 */
	public BusinessGroup reactivateBusinessGroup(BusinessGroup businessGroup, Identity doer, boolean asGroupOwner);
	
	/**
	 * 
	 * @param businessGroup
	 * @return
	 */
	public BusinessGroup sendDeleteSoftlyEmail(BusinessGroup businessGroup);
	
	/**
	 * Change the status of the business group to deleted. Add an entry in log table.
	 * 
	 * @param businessGroup The business group
	 * @param deletedBy The user which does the action
	 * @param withMail Send notification per E-mail
	 * @return The merged business group
	 */
	public BusinessGroup deleteBusinessGroupSoftly(BusinessGroup businessGroup, Identity deletedBy, boolean withMail);
	
	/**
	 * Delete definitively a business group from the database with all data.
	 * 
	 * @param businessGroup The business group
	 * @param deletedBy The user which does the action
	 * @param withMail Send a notification per E-mail
	 */
	public void deleteBusinessGroup(BusinessGroup businessGroup, Identity deletedBy, boolean withMail);
	
	/**
	 * change the status of the business group and take care of the  life cycle.
	 * 
	 * @param businessGroup The business group
	 * @param status The new status
	 * @param doer The user which do the action
	 * @param asGroupOwner If the user is the actually owner of the group, some worklfow differs
	 * @return The merged business group
	 */
	public BusinessGroup changeBusinessGroupStatus(BusinessGroup businessGroup, BusinessGroupStatusEnum status, Identity doer, boolean asGroupOwner);
	

}
