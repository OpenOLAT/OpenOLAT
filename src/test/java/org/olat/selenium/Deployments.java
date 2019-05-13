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
package org.olat.selenium;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.olat.core.logging.Tracing;
import org.olat.test.ArquillianDeployments;

import com.dumbster.smtp.SimpleSmtpServer;

/**
 * 
 * Initial date: 12 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@ArquillianSuiteDeployment
public class Deployments {
	
	private static final Logger log = Tracing.createLoggerFor(Deployments.class);

	private static SimpleSmtpServer dumbster;
	static {
		try {
			dumbster = SimpleSmtpServer.start(SimpleSmtpServer.AUTO_SMTP_PORT);
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		Map<String,String> mailSettings = new HashMap<>();
		if(dumbster != null) {
			mailSettings.put("smtp.port", String.valueOf(dumbster.getPort()));
			mailSettings.put("smtp.host", "localhost");
			log.info("Simple smtp server started on port: " + dumbster.getPort());
		}
		return ArquillianDeployments.createDeployment(mailSettings);
	}
	
	@After
	public void afterTest() {
		if(dumbster != null) {
			dumbster.reset();
		}
	}
	
	protected SimpleSmtpServer getSmtpServer() {
		return dumbster;
	}
}
