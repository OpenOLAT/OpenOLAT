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
