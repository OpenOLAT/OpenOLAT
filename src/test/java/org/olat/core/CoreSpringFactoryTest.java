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
package org.olat.core;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.logging.Tracing;
import org.olat.test.OlatTestCase;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CoreSpringFactoryTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(CoreSpringFactoryTest.class);
	
	@Test
	public void testGetImpl() {
		long start = System.currentTimeMillis();
		for(int i=0; i<1000; i++) {
			BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
			Assert.assertNotNull(securityManager);
		}
		log.info("Get bean by impl takes (ms): " + (System.currentTimeMillis() - start));
		
		long start2 = System.currentTimeMillis();
		for(int i=0; i<1000; i++) {
			BaseSecurity securityManager = (BaseSecurity)CoreSpringFactory.getBean("baseSecurityManager");
			Assert.assertNotNull(securityManager);
		}
		log.info("Get by by ID takes (ms): " + (System.currentTimeMillis() - start2));	
	}
}
