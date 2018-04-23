package org.olat.core.commons.services.csp.manager;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
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
		
		cspManager.log(report, identity);
		dbInstance.commit();
	}
	

	@Test
	public void cleanup() {
		cspManager.cleanup();
		dbInstance.commit();
	}

}
