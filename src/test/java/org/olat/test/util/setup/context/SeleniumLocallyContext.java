package org.olat.test.util.setup.context;

import java.io.File;

import org.olat.test.util.setup.SetupType;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CodepointClientFactory;
import org.olat.testutils.codepoints.client.CommunicationException;

/**
 * Context for running selenium tests in eclipse. <br/> 
 * 
 * 
 * @author eglis
 *
 */
public class SeleniumLocallyContext extends Context {

	public SeleniumLocallyContext() {
		initContext();
	}

	@Override
	protected boolean supportsSetupType(SetupType setupType) {
		return true;
	}
	
	@Override
	protected void doSetupContext(SetupType setupType) {
		System.out.println("Test run assumes SetupType: "+setupType.name());
		
		String withCodepoints = getConfigProperty("withCodepoints");
		if("true".equals(withCodepoints)) {
		  assertCodepointServerReady(1);
		}
	}
	
	@Override
	protected void doTearDown() {
		System.out.println("Closing seleniums...");
		seleniumManager_.closeSeleniums();
		System.out.println("Done closing seleniums.");
	}

	@Override
	public CodepointClient doCreateCodepointClient(int nodeId) throws Exception {
		return CodepointClientFactory.createCodepointClient(getConfigProperty("jmsBrokerUrl"), getConfigProperty("instanceId")+"-"+nodeId);
	}

	@Override
	public String provideFileRemotely(File localFile) {
		if (!localFile.exists()) {
			throw new AssertionError("File not found: "+localFile);
		}
		return localFile.getAbsolutePath();
	}
	
	protected void assertCodepointServerReady(int nodeId) {
		try{
			final long start = System.currentTimeMillis();
			String jmsBrokerUrl = getConfigProperty("jmsBrokerUrl");
			String[] ids = CodepointClientFactory.listCodepointServerNodeIds(jmsBrokerUrl);
			for (int i = 0; i < ids.length; i++) {
				System.out.println("[SeleniumLocallyContext] DISCOVERED NODE_ID: "+ids[i]);
			}
			System.out.println("[SeleniumLocallyContext] Discovery took "+(System.currentTimeMillis()-start)/1000+"s");
		} catch(Exception e) {
			System.out.println("[SeleniumLocallyContext] Exception while doing listCodepointServerNodeIds: "+e);
			e.printStackTrace(System.out);
		}
		System.out.println("[SeleniumLocallyContext] asserting that the CodepointServer with nodeId="+nodeId+" is ready...");
		CodepointClient codepointClient = createCodepointClient(nodeId);
		try {
			codepointClient.listAllCodepoints();
		} catch (CommunicationException e) {
			e.printStackTrace(System.out);
			throw new AssertionError("Got a CommunicationException while making sure the CodepointServer was running in nodeId="+nodeId+" (exception="+e+")");
		}
		codepointClient.close();
		System.out.println("[SeleniumLocallyContext] asserting that the CodepointServer with nodeId="+nodeId+" succeeded!");
	}
	
	/*@BeforeClass*/
	public void restartSeleniumServer() throws AssertionError {
		//nothing to implement - the selenium RC server is started manually when running tests using this context
		/*try {
			SeleniumServer server = new SeleniumServer();			
			server.stop();
			server.start();
			Thread.sleep(15000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

}
