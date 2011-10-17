package org.olat.test.tutorial.testng.dependencies;

import org.testng.annotations.Test;

public class ClassC {

	@Test(groups = {"dependencies"} )
	public void methodC() {
		System.out.println("methodC");
	}
}
