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
package org.olat.test.util.setup.context;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.AssertionFailedError;

import org.olat.test.util.selenium.OlatLoginHelper;
import org.olat.test.util.selenium.SeleniumManager;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CodepointRef;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.thoughtworks.selenium.Selenium;

/**
 * Setups the context for running selenium tests. <br/>
 * Configurable via customcontext.xml if any available, else via defaultcontext.xml
 * 
 * @author eglis
 *
 */
public abstract class Context {
	
    public static InheritableThreadLocal<Context> currentContext=new InheritableThreadLocal<Context>();
	
	private static Map<SetupType,Context> contexts_;
	protected final static String log4JConfigFilenameKey = "log4jConfigFilename";
	protected boolean cleanupBeforeTest = true; //should be read from context?
	public static String FILE_RESOURCES_PATH = "org/olat/test/file_resources/";
	public static String DEMO_COURSE_NAME_1 = "OLAT: Demo course";
	public static String DEMO_COURSE_NAME_2 = "OLAT: Demo course";
	public static String DEMO_COURSE_NAME_3 = "OLAT: Demokurs Einschreibung";
	

	static {
		System.out.println(new Date());
		System.out.println("INITIALIZING SPRING WITH contexts.xml FOR SELENIUMTESTS' CONTEXT...");
		contexts_ = new HashMap<SetupType,Context>();
		new ClassPathXmlApplicationContext(
				new String[] {
						"classpath*:customcontext.xml"
				});
		if (contexts_.size()==0) {
			new ClassPathXmlApplicationContext(
					new String[] {
							"classpath*:defaultcontext.xml"
					});
		}
		System.out.println("DONE.");
	}

	/**
	 * The SeleniumManager is used for creating and closing Selenium instances.
	 * The abstract Context base class sets up this instance before the Context
	 * is used for creating selenium instances the first time.
	 */
	protected SeleniumManager seleniumManager_;

	private List<CodepointClient> codepointClients_ = new LinkedList<CodepointClient>();;
	
	private Map<String,String> config_ = new HashMap<String,String>();

	private SetupType setupType_;
	
	protected void initContext() {
		SetupType[] setupTypes = SetupType.values();
		for (int i = 0; i < setupTypes.length; i++) {
			SetupType setupType = setupTypes[i];
			if (supportsSetupType(setupType)) {
				if (contexts_.containsKey(setupType)) {
					throw new IllegalArgumentException("There is already a Context defined for SetupType "+setupType.name());
				}
				contexts_.put(setupType, this);
			}
		}
		setConfigProperty("browserId", "*chrome");
	}
	
	protected abstract boolean supportsSetupType(SetupType setupType);
	
	private static Context getContext(SetupType setupType) {
		Context c = contexts_.get(setupType);
		if (c==null) {
			throw new IllegalStateException("No Context found for SetupType "+setupType.name());
		}
		return c;
	}
	
	public SetupType getSetupType() {
		return setupType_;
	}
	
	public boolean isSingleVMSetupType() {
		return getSetupType().isSingleVm();
	}
	
	public void setConfig(Map<String, String> config) {
		Iterator<Map.Entry<String, String>> it = config.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, String> entry = it.next();
			config_.put(entry.getKey(), entry.getValue());
		}
	}
	
	public String getConfigProperty(String key) {
		if (config_==null) {
			throw new IllegalStateException("config_ must not be null.");
		}
		return config_.get(key);
	}
	
	public void setConfigProperty(String key, String value) {
		if (config_==null) {
			throw new IllegalStateException("config_ must not be null.");
		}
		config_.put(key, value);
	}

	public static Context setupContext(String testName, SetupType setupType) {
		if (currentContext.get()!=null) {
			throw new IllegalStateException("Context is already setup. You must do a context.tearDown() first before doing another Context.setupContext Mate!");
		}
		Context c = getContext(setupType);
		if (c==null) {
			throw new IllegalStateException("Context not configured. contexts.xml in classpath and configured correctly?");
		}
		currentContext.set(c);
    	System.out.println("============================");
    	System.out.println("Context.setupContext   START("+testName+","+setupType.name()+")");
    	System.out.println("seleniumHost     = "+c.getConfigProperty("seleniumHost"));
    	System.out.println("browserId        = "+c.getConfigProperty("browserId"));
    	System.out.println("withCodepoints     = "+c.getConfigProperty("withCodepoints"));
    	System.out.println("jmsBrokerUrl     = "+c.getConfigProperty("jmsBrokerUrl"));
    	System.out.println("instanceId       = "+c.getConfigProperty("instanceId"));
    	
    	if (setupType.isSingleVm()) {
    		// then we have exactly 1 url
    		System.out.println("singleVmOlatUrl  = "+c.getConfigProperty("singleVmOlatUrl"));
    	} else {
    		// then we have multiple. unclear how many, so lets just loop
    		int nodeId = 1;
    		while(c.getConfigProperty("multiVmOlatUrl"+nodeId)!=null) {
    			System.out.println("multiVmOlatUrl"+nodeId+"  = "+c.getConfigProperty("multiVmOlatUrl"+nodeId));
            	nodeId++;
    		}
    	}
    	System.out.println("admin            = "+c.getConfigProperty("adminUsername")+"/"+c.getConfigProperty("adminPassword"));
    	System.out.println("author           = "+c.getConfigProperty("authorUsername")+"/"+c.getConfigProperty("authorPassword"));
    	System.out.println("student          = "+c.getConfigProperty("studentUsername")+"/"+c.getConfigProperty("studentPassword"));
    	System.out.println("guest            = "+c.getConfigProperty("guestUsername")+"/"+c.getConfigProperty("guestPassword"));
    	//............
    	System.out.println("log4JConfigFilename = "+c.getConfigProperty(log4JConfigFilenameKey));
    	
    	
		c.seleniumManager_ = new SeleniumManager(testName);
		c.setupType_ = setupType;
		c.doSetupContext(setupType);
		
    	System.out.println("Context.setupContext     END("+testName+","+setupType.name()+")");
    	System.out.println("============================");
		// and return the context itself
		return c;
	}
	
	public static Context getContext() {
		Context c = currentContext.get();
		if (c==null) {
			throw new IllegalStateException("Context not configured. contexts.xml in classpath and configured correctly? And, did you call Context.setupContext() ?");
		}
		return c;
	}
	
	/**
	 * Try to find out if it is a KnownIssueException somewhere in the olat.log - 
	 * and if so, don't report it as the original failure but as a known issue so we immediately 
	 * know from the test result that it is a known issue.
	 * <br/>
	 * Returns by default false, that is "Do not mask the test failure if ERROR encountered in olat.log".
	 * <br/>
	 * It never returns true, but it throws a AssertionFailedError if a "Known Issue" encountered.
	 * 
	 * @param th
	 * @return
	 * @throws Exception
	 */
	public static boolean maskTestFailureOrError(Throwable th) throws Exception {
		Context c = currentContext.get();
		if (c==null) {
			throw new IllegalStateException("Context not configured. contexts.xml in classpath and configured correctly? And, did you call Context.setupContext() ?");
		}
		if (c.doMaskTestFailureOrError(th)) {
			return true;
		}
		if (th.getMessage().contains("INST-MSG-author")) {
			throw new AssertionFailedError("Known Issue 3841 encountered.");
		}
		if (c.seleniumManager_.anySeleniumBrowserHasKnownIssue3857()) {
			throw new AssertionFailedError("Known Issue 3857 encountered. (\"Please do not use the `Reload` or `Back` button of your browser.\" encountered in one of the browsers)");
		}
		return false;
	}
	
	protected boolean doMaskTestFailureOrError(Throwable th) throws Exception {
		return false;
	}
	
	public static void tearDown() {
    	System.out.println("======== TEARDOWNSTART =====");
		try{
			staticDoTearDown();
		} catch(Error er) {
			er.printStackTrace(System.out);
			throw er;
		} catch(RuntimeException re) {
			re.printStackTrace(System.out);
			throw re;
		} finally {
	    	System.out.println("======= TEARDOWNFINALLY ====");
		}
	}
	
	private final static void staticDoTearDown() {
		Context c = currentContext.get();
		if (c==null) {
			throw new IllegalStateException("Context not configured. contexts.xml in classpath and configured correctly? And, did you call Context.setupContext() ?");
		}
    	System.out.println("============================");
    	System.out.println("Context.tearDown()     START");

    	c.doTearDown();

    	System.out.println("----------------------------");
    	System.out.println("Closing CodepointClients....");
    	int cnt = 0;
    	for (Iterator<CodepointClient> it = c.codepointClients_.iterator(); it.hasNext();) {
    		System.out.println("CodepointClient["+cnt+"] START");
			CodepointClient codepointClient = it.next();
			try{
	    		System.out.println("CodepointClient["+cnt+"] fetching all codepoints for debug...");
				List<CodepointRef> codepoints = codepointClient.listAllCodepoints();
	    		System.out.println("CodepointClient["+cnt+"] going through all codepoints now...");
				for (Iterator<CodepointRef> it2 = codepoints.iterator(); it2.hasNext();) {
					CodepointRef codepointRef = it2.next();
					System.out.println("[Codepoint-"+codepointClient+"]: "+codepointRef.getId()+", hitCount="+codepointRef.getHitCount());
				}
	    		System.out.println("CodepointClient["+cnt+"] done with going through all codepoints.");
			} catch(AssertionError ae) {
				// ok, be silent here
			} catch(Exception e) {
				System.out.println("Exception in list codepoint: "+e);
				e.printStackTrace(System.out);
			}
    		System.out.println("CodepointClient["+cnt+"] closing codepointclient now...");
			codepointClient.close();
    		System.out.println("CodepointClient["+cnt+"] END");
    		cnt++;
		}
    	System.out.println("Closed CodepointClients.");
    	System.out.println("----------------------------");
    	
    	currentContext.set(null);
    	
    	System.out.println("Context.tearDown()       END");
    	System.out.println("============================");
	}
	
	protected abstract void doSetupContext(SetupType setupType);

	protected abstract void doTearDown();
	
	/**
	public void deleteAllLearningResourcesFromMyAuthors() {
		WorkflowHelper.deleteAllLearningResourcesFromAuthor(getStandardAdminOlatLoginInfos(1).getUsername());
		WorkflowHelper.deleteAllLearningResourcesFromAuthor(getStandardAuthorOlatLoginInfos(1).getUsername());
		WorkflowHelper.deleteAllLearningResourcesFromAuthor(getStandardStudentOlatLoginInfos(1).getUsername());
		WorkflowHelper.deleteAllLearningResourcesFromAuthor(getStandardGuestOlatLoginInfos(1).getUsername());
	}**/
	
	public Selenium createSelenium() {
		return createSelenium(getStandardAdminOlatLoginInfos(1));
	}
	
	public Selenium createSelenium(int nodeId) {
		return createSelenium(getStandardAdminOlatLoginInfos(nodeId));
	}
	
	public Selenium createSelenium(OlatLoginInfos loginInfos) {
		Selenium selenium = seleniumManager_.createSelenium(loginInfos);
		return selenium;
	}
	
	public Selenium createSeleniumAndLogin() {
		return createSeleniumAndLogin(getStandardAdminOlatLoginInfos(1));
	}
	
	public Selenium createSeleniumAndLogin(OlatLoginInfos loginInfos) {
		Selenium selenium = seleniumManager_.createSelenium(loginInfos);
		
		OlatLoginHelper.olatLogin(selenium, loginInfos);
		
		// make sure the login worked
		if (!"OLAT - Home".equals(selenium.getTitle())) {
			throw new AssertionError("createSeleniumAndLogin failed - expected to be logged in now and seeing 'OLAT - Home' - but instead I'm on this page: "+selenium.getTitle());
		}
		return selenium;
	}
	
	/**
	 * Creates selenium, login and return an OLAT abstraction instance. 
	 * @param loginInfos
	 * @return THE OLAT ABSTRACTION entry point
	 */
	public OLATWorkflowHelper getOLATWorkflowHelper(OlatLoginInfos loginInfos) {
		Selenium selenium = seleniumManager_.createSelenium(loginInfos);
		
		OlatLoginHelper.olatLogin(selenium, loginInfos);
		
		// make sure the login worked
		if (!"OLAT - Home".equals(selenium.getTitle())) {
			throw new AssertionError("createSeleniumAndLogin failed - expected to be logged in now and seeing 'OLAT - Home' - but instead I'm on this page: "+selenium.getTitle());
		}
		return new OLATWorkflowHelper(selenium);
	}
		
	/**
	 * Create user
	 * @param nodeId
	 * @param username
	 * @param password
	 * @param isSystemUser
	 * @param userManagementRole
	 * @param groupManagementRole
	 * @param authorRole
	 * @param systemAdminRole
	 * @return
	 */
	public OlatLoginInfos createuserIfNotExists(int nodeId,
			String username, String password,
			boolean isSystemUser,
			boolean userManagementRole, boolean groupManagementRole, boolean authorRole, boolean systemAdminRole) {
		
		try {
			return WorkflowHelper.createUserIfNotExists(getStandardAdminOlatLoginInfos(nodeId), 
					username, password, 
					isSystemUser, 
					userManagementRole, groupManagementRole, authorRole, systemAdminRole);
		} catch (InterruptedException e) {
			e.printStackTrace(System.out);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Create user by login as admin with the given name and the standard password.
	 * @param nodeId
	 * @param username
	 * @param isSystemUser
	 * @param userManagementRole
	 * @param groupManagementRole
	 * @param authorRole
	 * @param systemAdminRole
	 * @return
	 */
	public OlatLoginInfos createuserIfNotExists(
			int nodeId,
			String username,
			boolean isSystemUser,
			boolean userManagementRole, boolean groupManagementRole, boolean authorRole, boolean systemAdminRole) {
		return createuserIfNotExists(nodeId, username, getStandardPassword(), isSystemUser, userManagementRole, groupManagementRole, authorRole, systemAdminRole);
	}
	
	public CodepointClient createCodepointClient(int nodeId) {
		CodepointClient codepointClient;
		try {
			codepointClient = doCreateCodepointClient(nodeId);
		} catch (Exception e) {
			e.printStackTrace(System.out);
			throw new RuntimeException(e);
		}
		codepointClients_.add(codepointClient);
		return codepointClient;
	}
	
	/**
	 * Creates a codepoint client for the server node with the given id.
	 * 
	 * @param nodeId
	 * @return
	 * @throws Exception
	 */
	protected abstract CodepointClient doCreateCodepointClient(int nodeId) throws Exception;
	
	public OlatLoginInfos getStandardAdminOlatLoginInfos() {
		if (!isSingleVMSetupType()) {
			throw new IllegalStateException("In Cluster mode you need to specify the nodeId of this login");
		}
		return getStandardAdminOlatLoginInfos(1);
	}
	
	public OlatLoginInfos getStandardAuthorOlatLoginInfos() {
		if (!isSingleVMSetupType()) {
			throw new IllegalStateException("In Cluster mode you need to specify the nodeId of this login");
		}
		return getStandardAuthorOlatLoginInfos(1);
	}
	
	public OlatLoginInfos getStandardStudentOlatLoginInfos() {
		return getStandardStudentOlatLoginInfos(1);
	}
	
	public OlatLoginInfos getStandardGuestOlatLoginInfos() {
		return getStandardGuestOlatLoginInfos(1);
	}
	
	public OlatLoginInfos getStandardAdminOlatLoginInfos(int nodeId) {
		return createLoginInfos(nodeId, getConfigProperty("adminUsername"), getConfigProperty("adminPassword"));
	}

	public OlatLoginInfos getStandardAuthorOlatLoginInfos(int nodeId) {
		return createLoginInfos(nodeId, getConfigProperty("authorUsername"), getConfigProperty("authorPassword"));
	}

	public OlatLoginInfos getStandardStudentOlatLoginInfos(int nodeId) {
		return createLoginInfos(nodeId, getConfigProperty("studentUsername"), getConfigProperty("studentPassword"));
	}

	public OlatLoginInfos getStandardGuestOlatLoginInfos(int nodeId) {
		return createLoginInfos(nodeId, getConfigProperty("guestUsername"), getConfigProperty("guestPassword"));
	}

	protected String getOlatUrl(int nodeId) {
		if (isSingleVMSetupType()) {
			if (nodeId!=1) {
				throw new IllegalArgumentException("Cannot refer to nodeId other than 1 while having SetupType set to SINGLE VM");
			}
			return getConfigProperty("singleVmOlatUrl");
		} else {
			return getConfigProperty("multiVmOlatUrl"+nodeId);
		}
	}
	
	protected OlatLoginInfos createLoginInfos(int nodeId, String username, String password) {
		String olatUrl = getOlatUrl(nodeId);
		try{
			return new OlatLoginInfos(
					getConfigProperty("seleniumHost"), 
					getConfigProperty("browserId"), 
					olatUrl,
					username,
					password);
		} catch(MalformedURLException e) {
			throw new RuntimeException("MalformedURLException ("+olatUrl+") when creating admin login infos: "+e);
		}
	}
	
	/**
	 * Beware: this does not check if the user data is valid!!!
	 * @param nodeId
	 * @param username
	 * @param password
	 * @return Returns an OlatLoginInfos for the username, password, and nodeId.
	 */
	public OlatLoginInfos getOlatLoginInfo(int nodeId, String username, String password) {
		//TODO:LD: see is user data check needed (e.g. admin login and check if username exists and change password to the given one)
		return createLoginInfos(nodeId, username, password);
	}
	
	/**
	 * Creates a OlatLoginInfos object using the context info and the standard password.
	 * @param nodeId
	 * @param username
	 * @return
	 */
	public OlatLoginInfos getOlatLoginInfo(int nodeId, String username) {
		//TODO:LD: see is user data check needed (e.g. admin login and check if username exists and change password to the given one)
		return createLoginInfos(nodeId, username, getStandardStudentOlatLoginInfos(1).getPassword());
	}
	
	/**
	 * Convention: all test users could use a standard password, and we assume that this is the default student password.
	 * @return
	 */
	private String getStandardPassword() {
	  return getStandardStudentOlatLoginInfos(1).getPassword();
	}
	
	/**
	 * Copies the localFile to the seleniumHost location. 
	 * 
	 * @param localFile
	 * @return
	 */
	public abstract String provideFileRemotely(File localFile);
	
	/**
	 * Restarts Selenium RC server.
	 * 
	 * @throws AssertionError
	 */
	protected abstract void restartSeleniumServer() throws AssertionError;
	

	
}
