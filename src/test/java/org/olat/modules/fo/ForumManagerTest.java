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

package org.olat.modules.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;

/**
 * @author Felix Jost
 */

public class ForumManagerTest extends OlatTestCase {

	private static OLog log = Tracing.createLoggerFor(ForumManagerTest.class); 

	public Identity u1;
	public Identity u2;
	public Identity u3;
	public UserManager um1;
	
	public ForumManager fm1;
	public Forum fo;
	
  private Message message1, m3;
  

	/**
	 * SetUp is called before each test
	 */
	@Before public void setup(){
		// create some users with user manager
		try{		
			log.info("setUp start ------------------------");
			
			um1 = UserManager.getInstance();
			//um1.resetSession(sess);
			u1 = JunitTestHelper.createAndPersistIdentityAsUser("felix");
			u2 = JunitTestHelper.createAndPersistIdentityAsUser("migros");
			u3 = JunitTestHelper.createAndPersistIdentityAsUser("salat");
			
			fm1 = ForumManager.getInstance();
			fo = fm1.addAForum();
			
			message1 = new MessageImpl();
			message1.setTitle("stufe 0: subject 0");
			message1.setBody("body/n dep 0");
			
			Message m2 = new MessageImpl();
			m2.setTitle("stufe 0: subject 1");
			m2.setBody("body 2 /n dep 0");
			
			m3 = new MessageImpl();
			m3.setTitle("stufe 1: subject 2");
			m3.setBody("body 21 /n dep 1");
			
			Message m4 = new MessageImpl();
			m4.setTitle("stufe 1: subject 3");
			m4.setBody("body 211 /n dep 2");
			
			fm1.addTopMessage(u1, fo, message1);
			fm1.addTopMessage(u2, fo, m2);
			
			fm1.replyToMessage(m3, u3, m2);
			fm1.replyToMessage(m4, u1, m3);			
			
			for (int i=0; i<10;i++) {
				Message m = new MessageImpl();
				m.setTitle("Title" + i);
				m.setBody("Body" + i);
				fm1.replyToMessage(m,u1,m4);
			}
			log.info("setUp done ------------------------");
		}
		catch(Exception e){
		 	log.error("Exception in setUp(): "+e);	
		}
	}
	
	@Test
	public void testGetMessagesByForumID() throws Exception {
		log.debug("Start testGetMessagesByForumID()");
		
		ForumManager foma = ForumManager.getInstance();
		long start = System.currentTimeMillis();
		Forum forum = foma.loadForum(fo.getKey());
		List<Message> messageList = foma.getMessagesByForum(forum);
		long stop = System.currentTimeMillis();		
		assertNotNull(messageList);			
		log.debug("time:"+(stop-start));
		Iterator<Message> it = messageList.iterator();
		while (it.hasNext()) {
			Object o =  it.next();
			log.debug("object:"+o);
			Message msg = (Message)o;
			log.debug("msg:"+msg.getTitle());
		}
		assertEquals("Not the right number of messages for this forum",14,messageList.size());
	}
	
	@Test public void testCountMessagesByForumID() {
		log.debug("Start testCountMessagesByForumID()");
		ForumManager foma = ForumManager.getInstance();
		assertEquals("Not the right number of messages for this forum",14,foma.countMessagesByForumID(fo.getKey()).intValue());
	}

	@Test public void testGetThread() {
		log.debug("Start testGetThread()");
		ForumManager foma = ForumManager.getInstance();
		Long msgidTopThread = message1.getKey();
		List<Message> threadMessageList = foma.getThread(msgidTopThread);
		log.debug("threadMessageList.size()=" + threadMessageList.size());
		assertEquals("Not the right number of messages for this forum",1,threadMessageList.size());
		// lookup for 
		Long notExistingTopThread = new Long(1234);
		threadMessageList = foma.getThread(notExistingTopThread);
		log.debug("threadMessageList.size()=" + threadMessageList.size());
		assertEquals("Not the right number of messages for this forum",0,threadMessageList.size());
	
	}
	
	@Test public void testGetNewMessageInfo() {
		log.debug("Start testGetNewMessageInfo()");
		ForumManager foma = ForumManager.getInstance();
		
		sleep(1500);//we must ensure a lap of 1 second
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		List<Message> msgList = foma.getNewMessageInfo(fo.getKey(), cal.getTime());
		assertEquals(0, msgList.size());
		cal.add(Calendar.HOUR_OF_DAY, - 1);
		msgList = foma.getNewMessageInfo(fo.getKey(), cal.getTime());
		assertEquals(14, msgList.size());
	}
	
	@Test public void testDeleteMessageTree() {
		log.debug("Start testDeleteMessageTree()");
		ForumManager foma = ForumManager.getInstance();
		foma.deleteMessageTree(fo.getKey(), m3); // throws Exception when failed		
	}
	
	@Test public void testDeleteForum() {
		log.debug("Start testDeleteForum()");
		ForumManager foma = ForumManager.getInstance();
		foma.deleteForum(fo.getKey()); // throws Exception when failed
	}
    
}
