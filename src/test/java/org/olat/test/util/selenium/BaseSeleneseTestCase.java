package org.olat.test.util.selenium;

import org.olat.test.util.setup.context.Context;
import org.testng.annotations.AfterMethod;

import com.thoughtworks.selenium.SeleneseTestCase;

/**
 * Base class for running Selenium tests in OLAT - all test cases
 * should extend this class.</P>
 * The environment of a test is defined over the a Context or derived class of it, and the corresponding spring context configuration.
 * 
 * @author Stefan
 */
public class BaseSeleneseTestCase extends SeleneseTestCase {

	public String getFullName() {
		return getClass().getCanonicalName()+"."+getName();
	}
	
    @Override
    public void setUp() throws Exception {
    	// don't do anything by default - including not calling super.setUp()
    }
    
    /**
     * Hooks for subclasses who wanted to overwrite tearDown() - but
     * the BaseSeleneseTestCase requires tearDown() to be called in
     * any case - thus enforces this pattern of
     * try{
     *   doTearDown();
     * } finally {
     *   // do the framework.tearDown stuff anyway
     * }
     * @throws Exception
     */
    public void doTearDown() throws Exception {
    	// nothing by default
    }
    
    @Override
    @AfterMethod(alwaysRun=true)
    public final void tearDown() throws Exception {
    	try{
    		checkForVerificationErrors();
    		doTearDown();
    	} finally {
    		Context.tearDown();
    	}

    	// don't call super.tearDown() by default - since we instantiate multiple selenium's here
    	// and the default 'selenium' instance variable might not be initialized - thus 
    	// a NullPointerException will occur in this case when we call super.tearDown() here.
    }

    @Override
	protected void runTest() throws Throwable {
		try{
			super.runTest();
			
			
		} catch(Throwable th) {
			// OLAT-3653: in case of a failure, try to find out if it is a KnownIssueException somewhere in the 
			// olat.log - and if so, don't report it as the original failure but as a known issue so we immediately
			// know from the test result that it is a known issue
			if (!Context.maskTestFailureOrError(th)) {
				throw th;
			}
		}
		cleanUpAfterRun();
	}
    
    /**
     * Clean up code to perform after the successful run.
     * 
     */
  protected void cleanUpAfterRun() {
    // nothing by default
  }
  
}
