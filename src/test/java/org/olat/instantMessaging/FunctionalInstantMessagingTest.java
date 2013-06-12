/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.instantMessaging;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang.ArrayUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.test.ArquillianDeployments;
import org.olat.user.restapi.UserVO;
import org.olat.collaboration.CollaborationTools;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.util.FunctionalGroupsSiteUtil;
import org.olat.util.FunctionalGroupsSiteUtil.MembersConfiguration;
import org.olat.util.FunctionalInstantMessagingUtil;
import org.olat.util.FunctionalUtil;
import org.olat.util.FunctionalVOUtil;
import org.olat.util.browser.Student1;
import org.olat.util.browser.Student2;
import org.olat.util.browser.Student3;
import org.olat.util.browser.Tutor1;

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
@Ignore
@RunWith(Arquillian.class)
public class FunctionalInstantMessagingTest {
	
	public final static List<Dialog> GROUP_CHAT_DIALOG = new ArrayList<Dialog>();
	public final static String[] GROUP_CHAT_MESSAGE = new String[]{
		null,
		null,
		null,
		"Hello world!",
		"Clear sky.",
	};

	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ArquillianDeployments.createDeployment();
	}

	@ArquillianResource
	URL deploymentUrl;

	static FunctionalUtil functionalUtil;
	static FunctionalGroupsSiteUtil functionalGroupsSiteUtil;
	static FunctionalInstantMessagingUtil functionalInstantMessagingUtil;

	static FunctionalVOUtil functionalVOUtil;
	
	static boolean initialized = false;
	
	@Before
	public void setup() throws IOException, URISyntaxException{
		if(!initialized){
			functionalUtil = new FunctionalUtil();
			functionalUtil.setDeploymentUrl(deploymentUrl.toString());
			functionalGroupsSiteUtil = new FunctionalGroupsSiteUtil(functionalUtil);
			functionalInstantMessagingUtil = new FunctionalInstantMessagingUtil(functionalUtil);
			
			functionalVOUtil = new FunctionalVOUtil(functionalUtil.getUsername(), functionalUtil.getPassword());
			
			initialized = true;
		}
	}

	@Ignore
	@Test
	@RunAsClient
	public void checkGroupChat(@Tutor1 Selenium tutor0, @Student1 Selenium student0, @Student2 Selenium student1, @Student3 Selenium student2)
			throws IOException, URISyntaxException
	{
		/*
		 * Prerequisites
		 */
		/* create users and group */
		List<UserVO> tutor = functionalVOUtil.createTestAuthors(deploymentUrl, 1);
		List<UserVO> user = functionalVOUtil.createTestUsers(deploymentUrl, 3);
		
		List<GroupVO> group = functionalVOUtil.createTestCourseGroups(deploymentUrl, 1);
		
		/* set visibility */
		functionalVOUtil.setGroupConfiguration(deploymentUrl, group.get(0),
				new String[]{CollaborationTools.TOOL_CHAT},
				false, false,
				false, false,
				false, false);
		
		/* add users to group */
		functionalVOUtil.addOwnerToGroup(deploymentUrl, group.get(0), tutor.get(0));
		
		for(int i = 0; i < 2; i++){
			functionalVOUtil.addParticipantToGroup(deploymentUrl, group.get(0), user.get(i));
		}
		
		/*
		 * Content
		 */
		functionalUtil.login(tutor0, tutor.get(0).getLogin(), tutor.get(0).getPassword(), true);
		
		/* message #0 */
		Dialog dialog = new Dialog(student0, group.get(0), GROUP_CHAT_MESSAGE[0]);
		Dialog.Action action = dialog.new LoginAction(user.get(0));
		dialog.getPreProcessor().add(action);
		action = dialog.new OnlineContactsAction(0);
		dialog.getPreProcessor().add(action);
		action = dialog.new OfflineContactsAction(0);
		dialog.getPreProcessor().add(action);
		action = dialog.new UsersAction(0);
		dialog.getPreProcessor().add(action);
		action = dialog.new LogoutAction();
		GROUP_CHAT_DIALOG.add(dialog);
		
		/* message #1 */
		dialog = new Dialog(student1, group.get(0), GROUP_CHAT_MESSAGE[1]);
		action = dialog.new ModifySettingsAction(tutor0, new FunctionalGroupsSiteUtil.MembersConfiguration[]{
				MembersConfiguration.CAN_SEE_COACHES,
		});
		dialog.getPreProcessor().add(action);
		action = dialog.new LoginAction(user.get(0));
		dialog.getPreProcessor().add(action);
		action = dialog.new OnlineContactsAction(1);
		dialog.getPreProcessor().add(action);
		action = dialog.new CheckUserAction(tutor.get(0).getFirstName(), tutor.get(0).getLastName());
		dialog.getPreProcessor().add(action);
		action = dialog.new OfflineContactsAction(0);
		dialog.getPreProcessor().add(action);
		action = dialog.new LogoutAction();
		GROUP_CHAT_DIALOG.add(dialog);
		
		/* message #2 */
		dialog = new Dialog(student0, group.get(0), GROUP_CHAT_MESSAGE[2]);
		//TODO:JK implement me
		
		/* message #3 */
		dialog = new Dialog(student0, group.get(0), GROUP_CHAT_MESSAGE[3]);
		//TODO:JK implement me
		
		/* message #4 */
		dialog = new Dialog(student0, group.get(0), GROUP_CHAT_MESSAGE[4]);
		action = dialog.new OnlineContactsAction(0);
		dialog.getPreProcessor().add(action);
		GROUP_CHAT_DIALOG.add(dialog);

		//TODO:JK implement me
		
		/* chat */
		for(Dialog current: GROUP_CHAT_DIALOG){
			Assert.assertTrue(current.chat());
		}
	}
	
	public class Dialog{
		private Selenium browser;
		
		private Object conversationPartner;
		
		private String message;
		
		private List<Action> preProcessor = new ArrayList<Action>();
		private List<Action> postProcessor = new ArrayList<Action>();
	
		public Dialog(Selenium browser, Object conversationPartner, String message){
			this.browser = browser;
			this.conversationPartner = conversationPartner;
			this.message = message;
		}
		
		public boolean performPreProcessing(){
			for(Action current: preProcessor){
				if(!current.process(this)){
					return(false);
				}
			}
			
			return(true);
		}
		
		public boolean performPostProcessing(){
			for(Action current: postProcessor){
				if(!current.process(this)){
					return(false);
				}
			}
			
			return(true);
		}
		
		public boolean chat(){
			if(!performPreProcessing()){
				return(false);
			}
			
			if(conversationPartner instanceof UserVO){
				if(!functionalInstantMessagingUtil.sendMessageToUser(browser,
						((UserVO) conversationPartner).getFirstName(), ((UserVO) conversationPartner).getLastName(),
						message)){
					return(false);
				}
			}else if(conversationPartner instanceof GroupVO){
				if(!functionalInstantMessagingUtil.sendMessageToGroup(browser,
						((GroupVO) conversationPartner).getName(),
						message)){
					return(false);
				}
			}
			
			if(performPostProcessing()){
				return(false);
			}
			
			return(true);
		}
		
		public Selenium getBrowser() {
			return browser;
		}

		public void setBrowser(Selenium browser) {
			this.browser = browser;
		}

		public Object getConversationPartner() {
			return conversationPartner;
		}

		public void setConversationPartner(Object conversationPartner) {
			this.conversationPartner = conversationPartner;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public List<Action> getPreProcessor() {
			return preProcessor;
		}

		public void setPreProcessor(List<Action> preProcessor) {
			this.preProcessor = preProcessor;
		}

		public List<Action> getPostProcessor() {
			return postProcessor;
		}

		public void setPostProcessor(List<Action> postProcessor) {
			this.postProcessor = postProcessor;
		}

		public abstract class Action{
			public abstract boolean process(Dialog dialog);
		}
		
		public class UsersAction extends Action{
			private int count;
			
			public UsersAction(int count){
				this.count = count;
			}
			
			@Override
			public boolean process(Dialog dialog) {
				int users = functionalInstantMessagingUtil.onlineContactCount(getBrowser());
				
				if(users == count){
					return(true);
				}else{
					return(false);
				}
			}
		}
		
		public class LoginAction extends Action {
			private UserVO user;
			
			public LoginAction(UserVO user){
				this.user = user;
			}
			
			@Override
			public boolean process(Dialog dialog) {
				if(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true)){
					return(true);
				}else{
					return(false);
				}
			}
			
		}
		
		public class LogoutAction extends Action {
			public LogoutAction(){
			}
			
			@Override
			public boolean process(Dialog dialog) {
				if(functionalUtil.logout(browser)){
					return(true);
				}else{
					return(false);
				}
			}
			
		}
		
		public class AvailableUsersAction extends Action{
			private int count;
			
			public AvailableUsersAction(int count){
				this.count = count;
			}
			
			@Override
			public boolean process(Dialog dialog) {
				int users = functionalInstantMessagingUtil.onlineContactCount(browser);
				
				if(users == count){
					return(true);
				}else{
					return(false);
				}
			}
		}
		
		public class OnlineContactsAction extends Action{
			private int count;
			
			public OnlineContactsAction(int count){
				this.count = count;
			}
			
			@Override
			public boolean process(Dialog dialog) {
				int users = functionalUtil.retrieveUserCount(browser);
				
				if(users == count){
					return(true);
				}else{
					return(false);
				}
			}
		}
		
		public class OfflineContactsAction extends Action{
			private int count;
			
			public OfflineContactsAction(int count){
				this.count = count;
			}
			
			@Override
			public boolean process(Dialog dialog) {
				List<String> contacts = functionalInstantMessagingUtil.findOfflineContacts(browser);
				
				if(contacts.size() == count){
					return(true);
				}else{
					return(false);
				}
			}
		}
		
		public class VerifyMessageAction extends Action {
			String message[];

			public VerifyMessageAction(String message[]){
				this.message = message;
			}
			
			@Override
			public boolean process(Dialog dialog) {
				for(String current: message){
					if(!functionalInstantMessagingUtil.waitForPageToLoadMessage(browser, current)){
						return(false);
					}
				}
				
				return(true);
			}
		}
		
		public class ModifySettingsAction extends Action {
			private Selenium tutor;
			private FunctionalGroupsSiteUtil.MembersConfiguration[] config;
			
			public ModifySettingsAction(Selenium tutor, FunctionalGroupsSiteUtil.MembersConfiguration[] config){
				this.tutor = tutor;
				this.config = config;
			}
			
			@Override
			public boolean process(Dialog dialog) {
				functionalGroupsSiteUtil.applyMembersConfiguration(tutor, config);
				
				return(true);
			}
		}
		
		public class CheckUserAction extends Action {
			private String firstname;
			private String surname;
			
			public CheckUserAction(String firstname, String surname){
				this.firstname = firstname;
				this.surname = surname;
			}
			
			@Override
			public boolean process(Dialog dialog) {
				String name = surname + ", " + firstname;
				
				if(ArrayUtils.contains(functionalInstantMessagingUtil.findOnlineContacts(browser).toArray(), name)){
					return(true);
				}else{
					return(false);
				}
			}
		}
	}
}

