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
*/
package org.olat.test.tutorial.testng.parallel;

import junit.framework.TestCase;

import org.testng.annotations.Test;

public class TestParallel extends TestCase {

	@Test
	public void testMethod1() throws Exception {
		System.out.println("method1...");
		Thread.sleep(5000);
		System.out.println("end method1.");
	}

	@Test
	public void testMethod2() throws Exception {
		System.out.println("method2...");
		Thread.sleep(5000);
		System.out.println("end method2.");
	}

	@Test
	public void testMethod3() throws Exception {
		System.out.println("method3...");
		Thread.sleep(5000);
		System.out.println("end method3.");
	}

	@Test
	public void testMethod4() throws Exception {
		System.out.println("method4...");
		Thread.sleep(5000);
		System.out.println("end method4.");
	}
	
}
