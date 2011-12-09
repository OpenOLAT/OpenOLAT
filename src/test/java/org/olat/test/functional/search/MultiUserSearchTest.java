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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 
package org.olat.test.functional.search;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

import com.thoughtworks.selenium.Selenium;

/**
 * Search service test with several users and several search queries. <p>
 * 
 * THIS TEST ONLY RUNS AGAINST AN OLAT WITH SEARCH ACTIVATED. <br/>
 * 
 * Test setup: <br/>
 * It needs a property file (see searchTest.properties) with the following data: <br/>
 * - user names - it is assumed that the users are already defined in OLAT <br/>
 * - search query pool <br/>
 * - max. number of searches before logout: maxSearches <br/>
 * - testSpanMs - max running time of the test <br/>
 * - timeToWaitBetweenSearches <br/>
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class MultiUserSearchTest extends BaseSeleneseTestCase {
	
	private final String PROPERTIES_FILE_NAME = "org/olat/test/functional/search/searchTest.properties";
	
	private List<String> userNamesList = new ArrayList<String>();	
    
  private List<String> searchQueryList = new ArrayList<String>();
  private Iterator<String> queryIterator; 
    
  private final String SERVICE_NOT_AVAILABLE_MSG = "This service is temporarily unavailable.";
  private final String RESULTS_FOUND = "Results 1";
  private final String NO_RESULTS_FOUND = "No results found.";
  
  private String timeToWaitBetweenSearches = "30000";  
  private int maxSearches = 5; //max. number of searches before logout
  private long testSpanMs;
  private long startTimeStampMs;
 
    

	public void setUp() throws Exception { 
		//no need to setup the context
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);	
		getTestData();
		startTimeStampMs = System.currentTimeMillis();
	}
	
	/**
	 * Search in loop for the testSpanMs.
	 * @throws Exception
	 */
	public void testSearch() throws Exception {
		while(System.currentTimeMillis()-startTimeStampMs<testSpanMs) {
			searchForOneLogin();
		}		
	}
		
	/**
	 * Tests the search instance with a configurable:
	 * - number of users
	 * - search query pool
	 * - number of searches per user - between login and logout
	 * - timeToWaitBetweenSearches
	 * 
	 * @throws Exception
	 */
	public void searchForOneLogin() throws Exception {
		System.out.println("testSearch STARTED");
				
		List<Selenium> seleniumList = new ArrayList<Selenium>();
		//create a selenium instance for each user, and let the users sent the first search request
		for(String username : userNamesList) {	
			String standardPassword = Context.getContext().getStandardAdminOlatLoginInfos(1).getPassword();
			//TODO:LD: create user with different roles
			Context.getContext().createuserIfNotExists(1, username, standardPassword, true, false, false, false, false);
			//we assume that all tests users have the same login, and the user already exists in the target olat instance (default users)
			Selenium selenium_ = createSeleniumAndLogin(username, standardPassword); 
			seleniumList.add(selenium_);
						
			//search after login
			String luceneQuery = getNextSearchQuery();
			selenium_.type("ui=search::topnavSearchInput()", luceneQuery);
			selenium_.click("ui=search::topnavSubmitSearch()");
			selenium_.waitForPageToLoad(timeToWaitBetweenSearches);	
			assertTrue(evaluateSearchResult(selenium_, luceneQuery));
		}		
		int searchCounter = 1; //once already searched 
		
		while(searchCounter<=maxSearches) {
		  //iterate over the seleniumList - and trigger searches via the search form
		  for(Selenium selenium_ : seleniumList) {	
		  	String luceneQuery = getNextSearchQuery();
			  selenium_.type("ui=search::searchFormInput()", luceneQuery);
			  selenium_.click("ui=search::searchButton()");
			  selenium_.waitForPageToLoad(timeToWaitBetweenSearches);			
			  assertTrue(evaluateSearchResult(selenium_, luceneQuery));
		  }
		  searchCounter++;
		}
		
		//logout all users
		for(Selenium selenium_ : seleniumList) {				
			selenium_.click("ui=tabs::logOut()");
			selenium_.waitForPageToLoad("30000");
			selenium_.close();		
			selenium_.stop();
		}
		
		System.out.println("testSearch ENDED");
	}
	
	/**
	 * Get initial test data.
	 *
	 */
	private void getTestData() {
		//input data - read it from property file 
		File propertiesFile = WorkflowHelper.locateFile(PROPERTIES_FILE_NAME);
		if(propertiesFile.exists()) {
			System.out.println("Read test data from properties file:");
			//read properties
			Properties properties = new Properties();
			try {
				FileInputStream fis = new FileInputStream(propertiesFile);
				properties.load(fis);
				Iterator keyIterator = properties.keySet().iterator();
				while(keyIterator.hasNext()) {
					String currentKey = (String)keyIterator.next();
					String currentValue = (String)properties.get(currentKey);
					if(currentKey.equals("testSpanMs")) {						
						testSpanMs = Long.parseLong(currentValue);
						System.out.println("testSpanMs: " + currentValue);
					} else if(currentKey.startsWith("user")) {
						userNamesList.add(currentValue);
						System.out.println("user: " + currentValue);
					} else if(currentKey.startsWith("query")) {
						searchQueryList.add(currentValue);
						System.out.println("searchQuery: " + currentValue);
					} else if (currentKey.equals("timeToWaitBetweenSearches")) {
						timeToWaitBetweenSearches = currentValue;
						System.out.println("timeToWaitBetweenSearches: " + timeToWaitBetweenSearches); 
					} else if(currentKey.equals("maxSearches")) {
						maxSearches = Integer.parseInt(currentValue);
						System.out.println("maxSearches: " + maxSearches);
					}
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			System.out.println("dummy test data");
		  //testSpanMs = 60*60*1000; //1h
		  testSpanMs = 2*60*1000; //1h
		
		  String[] userNames = {"test", "test2", "test3"};
		  //String[] userNames = {"test"};
		  String [] searchQueries = {"test", "olat", "Demo", "course"};
		
		  for(int i=0; i<userNames.length; i++) {
			  userNamesList.add(userNames[i]);			
		  }
		
		  for(int i=0; i<searchQueries.length; i++) {
			  searchQueryList.add(searchQueries[i]);			
		  }
		}
		System.out.println("userNamesList: " + userNamesList);
		System.out.println("searchQueryList: " + searchQueryList);
	}
	
	/**
	 * Iterates over the query list over and over again.
	 * @return
	 */
	private String getNextSearchQuery() {
		if(queryIterator==null || !queryIterator.hasNext()) {
		  queryIterator = searchQueryList.iterator();
		}
		if(queryIterator.hasNext()) {			
			String searchQuery = queryIterator.next();
			System.out.println("getNextSearchQuery: " + searchQuery);
			return searchQuery;
		}
		//should never return null
		return null;
	}
	
	/**
	 * 
	 * @param username
	 * @param passwort
	 * @return Returns a selenium instance for the input username.
	 * @throws Exception
	 */
	private Selenium createSeleniumAndLogin(String username, String passwort) throws Exception {
		OlatLoginInfos defaultOlatLoginInfos = Context.getContext().getStandardStudentOlatLoginInfos(1);
    //OlatLoginInfos olatLoginInfos1 = Context.getContext().createuserIfNotExists(1, username, psw, true, false, false, false, false);
		OlatLoginInfos olatLoginInfos1 = new OlatLoginInfos(defaultOlatLoginInfos.getSeleniumHostname(),
				defaultOlatLoginInfos.getSeleniumBrowserId(),
				defaultOlatLoginInfos.getFullOlatServerUrl(),
				username, passwort);
		Selenium selenium1 = Context.getContext().createSeleniumAndLogin(olatLoginInfos1);
		return selenium1;
	}
	
	/**
	 * We consider that an successful search should find at least a result entry.
	 * If no result found or service not available - this is regarded as an invalid search result.
	 * @param selenium_
	 */
	private boolean evaluateSearchResult(Selenium selenium_, String luceneQuery) {
		boolean noResults = selenium_.isTextPresent(NO_RESULTS_FOUND);
		boolean resultsFound = selenium_.isTextPresent(RESULTS_FOUND);
		boolean serviceNotAvailable = selenium_.isTextPresent(SERVICE_NOT_AVAILABLE_MSG);
		boolean searchResultsFound = !serviceNotAvailable && (resultsFound && !noResults );
		System.out.println("searchResultsFound: " + searchResultsFound + " for query: " + luceneQuery);
		System.out.println("serviceNotAvailable: " + serviceNotAvailable);
		System.out.println("resultsFound: " + resultsFound);
		System.out.println("noResults: " + noResults);
		return searchResultsFound;
	}
	
}
