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
package org.olat.test.tutorial.singlenode.testng;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Test(groups = {"sequential"} )
public class SimpleTutorialTest {

	@BeforeClass
	public void beforeClass() {
		System.out.println("SimpleTutorialTest.beforeClass");
	}
	
	@BeforeTest
	public void beforeTest() {
		System.out.println("SimpleTutorialTest.beforeTest");
	}
	
	@BeforeMethod
	public void beforeMethod() {
		System.out.println("SimpleTutorialTest.beforeMethod");
	}
	
	@Test
	public void theSimpleTutorialTest() {
		System.out.println("SimpleTutorialTest.theTest");
	}
	
	@AfterMethod
	public void afterMethod() {
		System.out.println("SimpleTutorialTest.afterMethod");
	}
	
	@AfterTest
	public void afterTest() {
		System.out.println("SimpleTutorialTest.afterTest");
	}
	
	@AfterClass
	public void afterClass() {
		System.out.println("SimpleTutorialTest.afterClass");
	}
}
