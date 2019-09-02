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
package org.olat.course.archiver;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.UserSession;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * 
 * Initial date: 2 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FormatConfigHelperTest extends OlatTestCase {
	
	
	@Autowired
	private DB dbInstance;
	
	@Test
	public void readWritePreferences() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("archive-1");
		UserSession usess = new UserSession();
		SyntheticUserRequest ureq = new SyntheticUserRequest(id, Locale.ENGLISH, usess);
		usess.setIdentity(id);
		usess.setRoles(Roles.userRoles());
		usess.reloadPreferences();
		
		FormatConfigHelper.updateExportFormat(ureq, true, true, false, false, true);
		dbInstance.commitAndCloseSession();
		
		// mimic an other session
		UserSession readUsess = new UserSession();
		SyntheticUserRequest readUreq = new SyntheticUserRequest(id, Locale.ENGLISH, readUsess);
		readUsess.setIdentity(id);
		readUsess.setRoles(Roles.userRoles());
		readUsess.reloadPreferences();
		ExportFormat exportFormat = FormatConfigHelper.loadExportFormat(readUreq);
		Assert.assertTrue(exportFormat.isResponseCols());
		Assert.assertTrue(exportFormat.isPositionsOfResponsesCol());
		Assert.assertFalse(exportFormat.isPointCol());
		Assert.assertFalse(exportFormat.isTimeCols());
		Assert.assertTrue(exportFormat.isCommentCol());
	}

}
