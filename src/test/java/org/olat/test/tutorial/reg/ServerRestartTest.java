package org.olat.test.tutorial.reg;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.OlatLoginHelper;
import org.olat.test.util.selenium.SeleniumManager;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

import com.thoughtworks.selenium.Selenium;

/**
 * This class tests 10x a singlevm server start and 10x a cluster server start
 * .
 * @author eglis
 */
public class ServerRestartTest extends BaseSeleneseTestCase {
	
	public void testRestarts() throws Exception {
		for(int i=0; i<5; i++) {
			Context context = Context.setupContext(getFullName(), SetupType.CLEAN_AND_RESTARTED_TWO_NODE_CLUSTER);
			Selenium s = context.createSeleniumAndLogin();
			s.close();
			s.stop();
			Context.tearDown();
		}
		for(int i=0; i<5; i++) {
			Context context = Context.setupContext(getFullName(), SetupType.CLEAN_AND_RESTARTED_SINGLE_VM);
			Selenium s = context.createSeleniumAndLogin();
			s.close();
			s.stop();
			Context.tearDown();
		}
		Context.setupContext(getFullName(), SetupType.SINGLE_VM);
	}
}
