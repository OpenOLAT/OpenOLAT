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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 * @author Felix Jost
 *
 */
public class ForumManagerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	public UserManager userManager;
	@Autowired
	public ForumManager forumManager;
	
	@Test
	public void testGetThread() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-4");
		Forum fo = forumManager.addAForum();
		dbInstance.commit();

		Message message = forumManager.createMessage(fo, id, false);
		message.setTitle("stufe 0: subject 0");
		message.setBody("body/n dep 0");
		forumManager.addTopMessage(message);
		dbInstance.commit();

		Long messageTopThread = message.getKey();
		List<Message> threadMessageList = forumManager.getThread(messageTopThread);
		Assert.assertEquals("Not the right number of messages for this forum", 1, threadMessageList.size());
		
		// lookup for a none existing thread
		List<Message> noneThreadMessageList = forumManager.getThread(1234l);
		Assert.assertEquals("Not the right number of messages for this forum", 0, noneThreadMessageList.size());
	}

	@Test
	public void testCreateAndGetMessages_loadForumID() throws Exception {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-2");
		Forum fo = forumManager.addAForum();
		dbInstance.commit();

		Message topMessage = forumManager.createMessage(fo, id1, false);
		topMessage.setTitle("stufe 0: subject 0");
		topMessage.setBody("body/n dep 0");
		forumManager.addTopMessage(topMessage);
		dbInstance.commit();

		Message reply = forumManager.createMessage(fo, id2, false);
		reply.setTitle("stufe 0: subject 0");
		reply.setBody("body/n dep 0");
		forumManager.replyToMessage(reply, topMessage);
		dbInstance.commitAndCloseSession();

		//load the forum
		Forum forum = forumManager.loadForum(fo.getKey());
		List<Message> messageList = forumManager.getMessagesByForum(forum);
		Assert.assertNotNull(messageList);			
		for(Message msg: messageList) {
			Assert.assertNotNull(msg);
		}
		
		Assert.assertEquals("Not the right number of messages for this forum", 2, messageList.size());
	}
	
	@Test
	public void testCountMessagesByForumID() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-2");
		Forum fo = forumManager.addAForum();
		dbInstance.commit();

		Message topMessage = forumManager.createMessage(fo, id1, false);
		topMessage.setTitle("stufe 0: subject 0");
		topMessage.setBody("body/n dep 0");
		forumManager.addTopMessage(topMessage);
		dbInstance.commit();

		Message reply = forumManager.createMessage(fo, id2, false);
		reply.setTitle("stufe 1: subject 0");
		reply.setBody("body/n dep 0");
		forumManager.replyToMessage(reply, topMessage);
		dbInstance.commit();
		
		Message reply2 = forumManager.createMessage(fo, id1, false);
		reply2.setTitle("stufe 1: subject 0");
		reply2.setBody("body/n dep 0");
		forumManager.replyToMessage(reply2, reply);
		dbInstance.commit();
		
		int numOfMessages = forumManager.countMessagesByForumID(fo.getKey());
		Assert.assertEquals("Not the right number of messages for this forum", 3, numOfMessages);
	}
	
	@Test
	public void testGetNewMessageInfo() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-5");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-6");
		Forum fo = forumManager.addAForum();
		dbInstance.commit();

		Message topMessage = forumManager.createMessage(fo, id1, false);
		topMessage.setTitle("New message 1");
		topMessage.setBody("The newest stuff");
		forumManager.addTopMessage(topMessage);
		dbInstance.commit();

		Message reply = forumManager.createMessage(fo, id2, false);
		reply.setTitle("New message 2");
		reply.setBody("The more newest stuff");
		forumManager.replyToMessage(reply, topMessage);
		dbInstance.commit();

		sleep(1500);//we must ensure a lap of 1 second
		
		//check the newest messages, limit now
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		List<Message> newestMessages = forumManager.getNewMessageInfo(fo.getKey(), cal.getTime());
		Assert.assertEquals(0, newestMessages.size());
		
		//check the newest messages, limit one hour in past
		cal.add(Calendar.HOUR_OF_DAY, - 1);
		List<Message> olderLastMessages = forumManager.getNewMessageInfo(fo.getKey(), cal.getTime());
		Assert.assertEquals(2, olderLastMessages.size());
	}
	
	@Test
	public void testDeleteMessageTree() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-5");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-6");
		Forum fo = forumManager.addAForum();
		dbInstance.commit();

		Message topMessage = forumManager.createMessage(fo, id1, false);
		topMessage.setTitle("Future deleted message 1");
		topMessage.setBody("Future deleted  stuff");
		forumManager.addTopMessage(topMessage);
		dbInstance.commit();

		Message reply = forumManager.createMessage(fo, id2, false);
		reply.setTitle("Future deleted 2");
		reply.setBody("Future deleted  stuff");
		forumManager.replyToMessage(reply, topMessage);
		dbInstance.commit();
		
		Message reply2 = forumManager.createMessage(fo, id1, false);
		reply2.setTitle("Future deleted 3");
		reply2.setBody("Future deleted  stuff");
		forumManager.replyToMessage(reply2, reply);
		dbInstance.commit();
		
		//delete a message
		forumManager.deleteMessageTree(fo.getKey(), reply2);
		dbInstance.commitAndCloseSession();
		
		//delete a top message
		forumManager.deleteMessageTree(fo.getKey(), topMessage);
		dbInstance.commitAndCloseSession();
	}
	
	@Ignore @Test
	public void testDeleteForum() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-7");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-8");
		Forum fo = forumManager.addAForum();
		dbInstance.commit();

		Message topMessage = forumManager.createMessage(fo, id1, false);
		topMessage.setTitle("Future deleted forum part. 1");
		topMessage.setBody("Future deleted  stuff");
		forumManager.addTopMessage(topMessage);
		dbInstance.commit();

		Message reply = forumManager.createMessage(fo, id2, false);
		reply.setTitle("Future deleted forum part. 2");
		reply.setBody("Future deleted  stuff");
		forumManager.replyToMessage(reply, topMessage);
		dbInstance.commit();
		
		Message reply2 = forumManager.createMessage(fo, id1, false);
		reply2.setTitle("Future deleted forum part. 3");
		reply2.setBody("Future deleted  stuff");
		forumManager.replyToMessage(reply2, reply);
		dbInstance.commitAndCloseSession();

		//delete the forum
		forumManager.deleteForum(fo.getKey());
		dbInstance.commit();
	}
}
