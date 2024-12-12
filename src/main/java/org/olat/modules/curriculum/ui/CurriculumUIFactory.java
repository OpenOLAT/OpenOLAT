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
package org.olat.modules.curriculum.ui;

import org.olat.basesecurity.GroupMembershipStatus;

/**
 * 
 * Initial date: 12 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumUIFactory {
	
	private CurriculumUIFactory() {
		//
	}
	
	public static final String getMembershipLabelCssClass(GroupMembershipStatus status) {
		return switch(status) {
			case reservation -> "o_gmembership_status_reservation";
			case active -> "o_gmembership_status_active";
			case cancel -> "o_gmembership_status_cancel";
			case cancelWithFee -> "o_gmembership_status_cancelwithfee";
			case declined -> "o_gmembership_status_declined";
			case resourceDeleted ->"o_gmembership_status_resourcedeleted";
			case finished ->"o_gmembership_status_finished";
			case removed -> "o_gmembership_status_removed";
			default -> null;
		};
	}
	
	public static final String getMembershipIconCssClass(GroupMembershipStatus status) {
		return switch(status) {
			case reservation -> "o_membership_status_pending";
			case active -> "o_membership_status_active";
			case cancel -> "o_membership_status_cancel";
			case cancelWithFee -> "o_membership_status_cancelwithfee";
			case declined -> "o_membership_status_declined";
			case resourceDeleted -> "o_membership_status_resourcedeleted";
			case finished -> "o_membership_status_finished";
			case removed -> "o_membership_status_removed";
			default -> null;
		};
	}

}
