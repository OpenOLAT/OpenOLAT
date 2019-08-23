/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.test;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.helpers.Settings;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.event.FrameworkStartupEventChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.dumbster.smtp.SimpleSmtpServer;

/**
 * Initial Date:  25.10.2002
 *
 * @author Florian Gnaegi
 * @author guido
 *
 * This class is common parent to all JUnit Use Case tests in OLAT framework integration tests. 
 */
@ContextConfiguration(loader = MockServletContextWebContextLoader.class, locations = {
	"classpath:/org/olat/_spring/mainContext.xml"
})
public abstract class OlatTestCase extends AbstractJUnit4SpringContextTests {
	private static final Logger log = Tracing.createLoggerFor(OlatTestCase.class);
	
	private static boolean postgresqlConfigured = false;
	private static boolean oracleConfigured = false;
	private static boolean started = false;
	private static SimpleSmtpServer dumbster;

	@Autowired
	protected DB dbInstance;
	
	@Rule public TestName currentTestName = new TestName();
	
	/**
	 * If you like to disable a test method for some time just add the
	 * @Ignore("not today") annotation
	 * 
	 * The normal flow is that the spring context gets loaded and befor each test method the @before will be executed and after the the method each time
	 * the @after will be executed
	 */
	
	/**
	 * @param arg0
	 */
	public OlatTestCase() {
		Settings.setJUnitTest(true);
	}
	
	@Before
	public void printBanner() {
		log.info("Method run: " + currentTestName.getMethodName() + "(" + this.getClass().getCanonicalName() + ")");
		
		if(started) {
			return;
		}
		
		try {
			dumbster = SimpleSmtpServer.start(SimpleSmtpServer.AUTO_SMTP_PORT);
			log.info("Simple smtp server started on port: " + dumbster.getPort());
			WebappHelper.setMailConfig("mailport", String.valueOf(dumbster.getPort()));
			WebappHelper.setMailConfig("mailhost", "localhost");
		} catch (IOException e) {
			log.error("", e);
		}
		
		FrameworkStartupEventChannel.fireEvent();
		
		String dbVendor = DBFactory.getInstance().getDbVendor();
		postgresqlConfigured = dbVendor != null && dbVendor.startsWith("postgres");
		oracleConfigured = dbVendor != null && dbVendor.startsWith("oracle");
		
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		printOlatLocalProperties();
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("+ OLAT configuration initialized, starting now with junit tests +");
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

		started = true;
	}
	
	@After
	public void closeConnectionAfter() {
		log.info("Method test finished: " + currentTestName.getMethodName() + "(" + this.getClass().getCanonicalName() + ")");
		try {
			DBFactory.getInstance().commitAndCloseSession();
		} catch (Exception e) {
			e.printStackTrace();

			try {
				DBFactory.getInstance().rollbackAndCloseSession();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
		if(dumbster != null) {
			dumbster.reset();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void printOlatLocalProperties() {
		Resource overwritePropertiesRes = new ClassPathResource("olat.local.properties");
		try {
			Properties overwriteProperties = new Properties();
			overwriteProperties.load(overwritePropertiesRes.getInputStream());
			Enumeration<String> propNames = (Enumeration<String>)overwriteProperties.propertyNames();
			
			System.out.println("### olat.local.properties : ###");
			while (propNames.hasMoreElements()) {
				String propName = propNames.nextElement();
				System.out.println("++" + propName + "='" + overwriteProperties.getProperty(propName) + "'");
			}
		} catch (IOException e) {
			System.err.println("Could not load properties files from classpath! Exception=" + e);
		}
		
	}
	
	protected boolean waitForCondition(final Callable<Boolean> condition, final int timeoutInMilliseconds) {
		final CountDownLatch countDown = new CountDownLatch(1);
		final AtomicBoolean result = new AtomicBoolean(false);
		
		new Thread() {
			@Override
			public void run() {
				try {
					int numOfTry = (timeoutInMilliseconds / 100) + 2;
					for(int i=0; i<numOfTry; i++) {
						Boolean test = condition.call();
						if(test != null && test.booleanValue()) {
							result.set(true);
							break;
						} else {
							result.set(false);
						}
						DBFactory.getInstance().commitAndCloseSession();
						sleep(100);
					}
				} catch (Exception e) {
					log.error("", e);
					result.set(false);
				} finally {
					DBFactory.getInstance().closeSession();
				}
				countDown.countDown();
			}
		}.start();

		try {
			countDown.await(timeoutInMilliseconds, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			log.error("", e);
		}
		return result.get();
	}
	
	protected void sleep(int milliSeconds) {
		try {
			Thread.sleep(milliSeconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected SimpleSmtpServer getSmtpServer() {
		return dumbster;
	}

	/**
	 * @return True if the test run on PostreSQL
	 */
	protected boolean isPostgresqlConfigured() {
		return postgresqlConfigured;
	}

	/**
	 * @return True if the test run on Oracle
	 */
	protected boolean isOracleConfigured() {
		return oracleConfigured;
	}
}