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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 2007 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.test;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.frentix.spring.test.HelloWorld;

/**
 * <h3>Description:</h3>
 * Test if the spring loading mechanism works
 * <p>
 * Initial Date: 04.05.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
@ContextConfiguration(locations = {"classpath*:/serviceconfig/com/frentix/spring/test/_spring/olatextconfig.xml"})
public class SpringTest extends AbstractJUnit4SpringContextTests {
	
	@Autowired
	HelloWorld helloWorld;

	@Test
	public void testClassAvailable() {
		try {
			com.frentix.spring.test.HelloWorld.class.getName();
			assertTrue(true);
		} catch (Exception e) {
			fail("Could not find HelloWorld class - jar not loaded");
		}
	}

	@Test
	public void testSpringLoadClass() {
		try {
			assertTrue(helloWorld.toString().equals("Hello World"));
		} catch (Exception e) {
			fail(e.getMessage());
		}		
	}
	
	@Test
	public void testSpringLoadConfigManuallyAsClasspathResource() {
		try {
			Resource resource = new ClassPathResource("/serviceconfig/com/frentix/spring/test/_spring/olatextconfig.xml");
			assertTrue(resource != null && resource.exists());
			// -> config is here, so loading mechanism in CoreSpringFactory must be broken if tests above fail
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testSpringLoadSingleResource() {
		try {
			Resource resource = applicationContext.getResource("/com/frentix/spring/test/testresource.png");
			assertTrue(resource != null && resource.exists());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testSpringLoadMultipleResourcesWithWildcard() {
		try {
			Resource[] resources = applicationContext.getResources("/com/frentix/spring/test/*.png");
			assertTrue(resources != null && resources.length == 2);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	
}
