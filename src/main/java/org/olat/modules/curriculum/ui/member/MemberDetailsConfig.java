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
package org.olat.modules.curriculum.ui.member;

import java.util.List;

import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.user.UserInfoProfileConfig;

/**
 * 
 * Initial date: 10 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public record MemberDetailsConfig(UserInfoProfileConfig profileConfig, List<CurriculumRoles> alwaysVisibleRoles,
		boolean withEdit, boolean withAcceptDecline, boolean withHistory,
		boolean withActivityColumns, boolean withConfirmationColumns,
		boolean withOrders, boolean withOrdersDetails, boolean canEditOrder, boolean showImplementation) {
	//
}
