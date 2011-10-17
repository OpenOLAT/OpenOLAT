package org.olat.test.tutorial.testng.dependencies;

import org.testng.annotations.Test;

public class ClassB {

	@Test(groups = {"dependencies"} )
	public void methodB() {
		System.out.println("MethodB");
	}
}
