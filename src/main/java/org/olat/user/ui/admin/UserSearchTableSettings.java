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
package org.olat.user.ui.admin;

/**
 * 
 * Initial date: 2 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSearchTableSettings {
	
	private final boolean vCard;
	private final boolean bulkMail;
	private final boolean bulkDelete;
	private final boolean bulkOrganisationMove;
	private final boolean statusFilter;
	
	private UserSearchTableSettings(boolean vCard, boolean bulkMail, boolean bulkOrganisationMove, boolean bulkDelete, boolean statusFilter) {
		this.vCard = vCard;
		this.bulkMail = bulkMail;
		this.bulkDelete = bulkDelete;
		this.bulkOrganisationMove = bulkOrganisationMove;
		this.statusFilter = statusFilter;
	}
	
	public static UserSearchTableSettings none() {
		return new UserSearchTableSettings(false, false, false, false, false);
	}
	
	public static UserSearchTableSettings withVCard(boolean bulkMail, boolean bulkOrganisationMove, boolean bulkDelete, boolean statusFilter) {
		return new UserSearchTableSettings(false, bulkMail, bulkOrganisationMove, bulkDelete, statusFilter);
	}
	
	public boolean isVCard() {
		return vCard;
	}
	
	public boolean isBulkMail() {
		return bulkMail;
	}
	
	public boolean isBulkOrganisationMove() {
		return bulkOrganisationMove;
	}
	
	public boolean isBulkDelete() {
		return bulkDelete;
	}
	
	public boolean isStatusFilter() {
		return statusFilter;
	}
}
