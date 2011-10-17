package org.olat.test.tutorial.singlenode.testng;

import junit.framework.TestCase;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Test(groups = {"sequential"} )
public class SimpleTutorialJUnitTest extends TestCase {

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
	public void theSimpleTutorialJUnitTest() {
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
