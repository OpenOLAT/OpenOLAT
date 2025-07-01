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
package org.olat.repository.manager;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.util.DateUtils;
import org.olat.repository.model.UsersMembershipsEntry;
import org.olat.repository.ui.report.UsersMembershipsReport;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class UsersMembershipsReportQueryTest extends OlatTestCase {
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private UsersMembershipsReportQuery reportQuery;
	
	/**
	 * The test only check the syntax of the query.
	 */
	@Test
	public void searchCheckSyntax() {
		Date to = DateUtils.getEndOfDay(new Date());
		Date from = DateUtils.addDays(to, -365);
		List<GroupRoles> roles = List.of(GroupRoles.coach, GroupRoles.participant);
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(UsersMembershipsReport.USER_PROPS_IDENTIFIER, true);
		
		List<UsersMembershipsEntry> entries = reportQuery.search(from, to, roles, userPropertyHandlers, Locale.ENGLISH, 0, 1000);
		Assert.assertNotNull(entries);
	}

}
