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
package org.olat.core.commons.services.csp.manager;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.csp.CSPLog;
import org.olat.core.commons.services.csp.CSPManager;
import org.olat.core.commons.services.csp.model.CSPReport;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CSPManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CSPManager cspManager;
	
	@Test
	public void createLog() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("scp-1");
		CSPReport report = new CSPReport();
		report.setBlockedUri("blocked");
		report.setColumnNumber("12");
		report.setDisposition("enforce");
		report.setDocumentUri("doc-uri");
		report.setEffectiveDirective("effective");
		report.setLineNumber("132");
		report.setOriginalPolicy("original");
		report.setReferrer("refererer");
		report.setScriptSample("script");
		report.setSourceFile("source");
		report.setStatusCode("status-code");
		report.setViolatedDirective("directive");
		
		CSPLog log = cspManager.log(report, identity);
		dbInstance.commit();
		Assert.assertNotNull(log);
	}
	

	@Test
	public void cleanup() {
		cspManager.cleanup();
		dbInstance.commit();
	}

}
