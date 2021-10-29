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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.fo.model.ForumThread;
import org.olat.modules.fo.model.ForumUserStatistics;
import org.olat.modules.fo.model.MessageImpl;
import org.olat.modules.fo.model.PseudonymStatistics;
import org.olat.modules.fo.ui.MessagePeekview;
import org.olat.repository.RepositoryEntry;
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
	@Autowired
	public BaseSecurity securityManager;
	
	@Test
	public void addAForum() {
		Forum fo = forumManager.addAForum();
		dbInstance.commitAndCloseSession();
		
		fo = forumManager.loadForum(fo.getKey());
		
		Assert.assertNotNull(fo);
	}
	
	@Test
	public void addAForumForOlatResourceable() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Forum fo = forumManager.addAForum(repositoryEntry);
		dbInstance.commitAndCloseSession();
		
		fo = forumManager.loadForum(repositoryEntry);
		
		Assert.assertNotNull(fo);
	}
	
	@Test
	public void getThread() {
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
	public void createAndGetMessages_loadForumID() throws Exception {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-2");
		Forum fo = forumManager.addAForum();
		dbInstance.commit();

		Message topMessage = forumManager.createMessage(fo, id1, false);
		topMessage.setTitle("Create and get message");
		topMessage.setBody("Create and get message");
		forumManager.addTopMessage(topMessage);
		dbInstance.commit();

		Message reply = forumManager.createMessage(fo, id2, false);
		reply.setTitle("Re: Create and get message");
		reply.setBody("Create and get message");
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
	public void getForumThreads() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-2");
		Forum forum = forumManager.addAForum();
		dbInstance.commit();
		
		Message thread1 = forumManager.createMessage(forum, id1, false);
		thread1.setTitle("Get forum threads");
		thread1.setBody("Get forum threads");
		forumManager.addTopMessage(thread1);
		dbInstance.commit();

		Message reply = forumManager.createMessage(forum, id2, false);
		reply.setTitle("Re: Get forum threads");
		reply.setBody("Get forum threads");
		forumManager.replyToMessage(reply, thread1);
		dbInstance.commitAndCloseSession();
		
		Message thread2 = forumManager.createMessage(forum, id1, false);
		thread2.setTitle("More on get forum threads");
		thread2.setBody("More on get forum threads");
		forumManager.addTopMessage(thread2);
		dbInstance.commit();
		
		List<ForumThread> forumThreads = forumManager.getForumThreads(forum, id1);
		Assert.assertNotNull(forumThreads);
		Assert.assertEquals(2, forumThreads.size());
		
		ForumThread forumThread1 = null;
		ForumThread forumThread2 = null;
		for(ForumThread forumThread:forumThreads) {
			if(forumThread.getKey().equals(thread1.getKey())) {
				forumThread1 = forumThread;
			} else if(forumThread.getKey().equals(thread2.getKey())) {
				forumThread2 = forumThread;
			}
		}
		
		Assert.assertNotNull(forumThread1);
		Assert.assertNotNull(forumThread2);
	}
	
	@Test
	public void getForumUserStatistics() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-2");
		Forum forum = forumManager.addAForum();
		dbInstance.commit();
		
		Message thread1 = forumManager.createMessage(forum, id1, false);
		thread1.setTitle("Get forum user statistics");
		thread1.setBody("Get forum user statistics");
		forumManager.addTopMessage(thread1);
		dbInstance.commit();

		Message reply = forumManager.createMessage(forum, id2, false);
		reply.setTitle("Re: Get forum user statistics");
		reply.setBody("Get forum user statistics and other usefull stuff to we need");
		forumManager.replyToMessage(reply, thread1);
		dbInstance.commitAndCloseSession();
		
		List<ForumUserStatistics> userStatistics = forumManager.getForumUserStatistics(forum);
		Assert.assertNotNull(userStatistics);
		Assert.assertEquals(2, userStatistics.size());

		ForumUserStatistics userStatistic1 = null;
		ForumUserStatistics userStatistic2 = null;
		for(ForumUserStatistics userStatistic:userStatistics) {
			if(userStatistic.getIdentity().getKey().equals(id1.getKey())) {
				userStatistic1 = userStatistic;
			} else if(userStatistic.getIdentity().getKey().equals(id2.getKey())) {
				userStatistic2 = userStatistic;
			}
		}
		
		Assert.assertNotNull(userStatistic1);
		Assert.assertNotNull(userStatistic2);
		
		//stats user 1
		Assert.assertEquals(1, userStatistic1.getNumOfThreads());
		Assert.assertEquals(0, userStatistic1.getNumOfReplies());
		Assert.assertTrue(userStatistic1.getNumOfWords() > 1);
		Assert.assertTrue(userStatistic1.getNumOfCharacters() > 1);
		
		//stats user 2
		Assert.assertEquals(0, userStatistic2.getNumOfThreads());
		Assert.assertEquals(1, userStatistic2.getNumOfReplies());
		Assert.assertTrue(userStatistic2.getNumOfWords() > 1);
		Assert.assertTrue(userStatistic2.getNumOfCharacters() > 1);
	}
	
	@Test
	public void getLightMessagesByForum() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-2");
		Forum forum = forumManager.addAForum();
		dbInstance.commit();
		
		Message thread1 = forumManager.createMessage(forum, id1, false);
		thread1.setTitle("Get messages light by forum");
		thread1.setBody("Get messages light by forum");
		forumManager.addTopMessage(thread1);
		dbInstance.commit();
		
		Message reply = forumManager.createMessage(forum, id2, false);
		reply.setTitle("Re: Get messages light by forum");
		reply.setBody("Get messages light by forum and other usefull stuff we need");
		forumManager.replyToMessage(reply, thread1);
		dbInstance.commitAndCloseSession();
		
		//load and check the messages
		List<MessageLight> messages = forumManager.getLightMessagesByForum(forum);
		dbInstance.commitAndCloseSession();
		
		MessageLight message1 = null;
		MessageLight message2 = null;
		for(MessageLight message:messages) {
			if(message.getKey().equals(thread1.getKey())) {
				message1 = message;
			} else if(message.getKey().equals(reply.getKey())) {
				message2 = message;
			}
		}
		
		//check thread
		Assert.assertNotNull(message1);
		Assert.assertEquals(thread1.getKey(), message1.getKey());
		Assert.assertEquals(thread1.getTitle(), message1.getTitle());
		Assert.assertEquals(thread1.getBody(), message1.getBody());
		Assert.assertEquals(thread1.getCreator(), id1);
		Assert.assertNull(message1.getThreadtop());
		
		//check reply
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(message2);
		Assert.assertEquals(reply.getKey(), message2.getKey());
		Assert.assertEquals(reply.getTitle(), message2.getTitle());
		Assert.assertEquals(reply.getBody(), message2.getBody());
		Assert.assertEquals(reply.getCreator(), id2);
		Assert.assertNotNull(message2.getThreadtop());
		Assert.assertEquals(thread1.getKey(), message2.getThreadtop().getKey());
	}
	
	@Test
	public void getLightMessagesByThread() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-2");
		Forum forum = forumManager.addAForum();
		dbInstance.commit();
		
		Message thread1 = forumManager.createMessage(forum, id1, false);
		thread1.setTitle("Get messages light by thread");
		thread1.setBody("Get messages light by thread");
		forumManager.addTopMessage(thread1);
		dbInstance.commit();
		
		Message reply = forumManager.createMessage(forum, id2, false);
		reply.setTitle("Re: Get messages light by thread");
		reply.setBody("Get messages light by thread and other usefull stuff we need");
		forumManager.replyToMessage(reply, thread1);
		dbInstance.commitAndCloseSession();
		
		//load and check the messages
		List<MessageLight> messages = forumManager.getLightMessagesByThread(forum, thread1);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(messages);
		Assert.assertEquals(1, messages.size());
		MessageLight message = messages.get(0);
		Assert.assertNotNull(message);
		Assert.assertEquals(reply.getKey(), message.getKey());
		Assert.assertEquals(reply.getTitle(), message.getTitle());
		Assert.assertEquals(reply.getBody(), message.getBody());
		Assert.assertEquals(reply.getCreator(), id2);
		Assert.assertNotNull(message.getThreadtop());
		Assert.assertEquals(thread1.getKey(), message.getThreadtop().getKey());
	}
	
	@Test
	public void getLightMessagesOfGuests() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity guest2 =  securityManager.getAndUpdateAnonymousUserForLanguage(Locale.ENGLISH);
		Forum forum = forumManager.addAForum();
		dbInstance.commit();
		
		Message thread1 = forumManager.createMessage(forum, id1, false);
		thread1.setTitle("Get messages light of guests");
		thread1.setBody("Get messages light of guests");
		forumManager.addTopMessage(thread1);
		dbInstance.commit();
		
		Message reply = forumManager.createMessage(forum, guest2, true);
		reply.setTitle("Re: Get messages light of guests");
		reply.setBody("Get messages light of guests and other usefull stuff we need");
		reply.setPseudonym("Guest pseudo 1289");
		forumManager.replyToMessage(reply, thread1);
		dbInstance.commitAndCloseSession();
		
		//load and check the messages
		List<MessageLight> messages = forumManager.getLightMessagesOfGuests(forum);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(messages);
		Assert.assertEquals(1, messages.size());
		MessageLight message = messages.get(0);
		Assert.assertNotNull(message);
		Assert.assertEquals(reply.getKey(), message.getKey());
		Assert.assertEquals(reply.getTitle(), message.getTitle());
		Assert.assertEquals(reply.getBody(), message.getBody());
		Assert.assertEquals("Guest pseudo 1289", message.getPseudonym());
		Assert.assertNull(message.getCreator());
		Assert.assertTrue(message.isGuest());
		Assert.assertNotNull(message.getThreadtop());
		Assert.assertEquals(thread1.getKey(), message.getThreadtop().getKey());
	}
	
	@Test
	public void getLightMessagesByUser() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity id2 =  JunitTestHelper.createAndPersistIdentityAsRndUser("fo-2");
		Identity id3 =  JunitTestHelper.createAndPersistIdentityAsRndUser("fo-3");
		Forum forum = forumManager.addAForum();
		dbInstance.commit();
		
		Message thread1 = forumManager.createMessage(forum, id1, false);
		thread1.setTitle("Get messages light by user");
		thread1.setBody("Get messages light by user");
		forumManager.addTopMessage(thread1);
		dbInstance.commit();
		
		Message reply = forumManager.createMessage(forum, id2, false);
		reply.setTitle("Re: Get messages light by user");
		reply.setBody("Get messages light by user and other usefull stuff we need");
		forumManager.replyToMessage(reply, thread1);
		dbInstance.commitAndCloseSession();
		
		Message replyPseudo = forumManager.createMessage(forum, id3, false);
		replyPseudo.setTitle("Re: Get messages light by user");
		replyPseudo.setBody("Get messages light by user and other usefull stuff we need");
		replyPseudo.setPseudonym("Id pseudo 3476");
		forumManager.replyToMessage(replyPseudo, thread1);
		dbInstance.commitAndCloseSession();
		
		//load and check the messages of first user
		List<MessageLight> messagesOfUser1 = forumManager.getLightMessagesByUser(forum, id1);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(messagesOfUser1);
		Assert.assertEquals(1, messagesOfUser1.size());
		MessageLight messageOfUser1 = messagesOfUser1.get(0);
		Assert.assertNotNull(messageOfUser1);
		Assert.assertEquals(thread1.getKey(), messageOfUser1.getKey());
		Assert.assertEquals(thread1.getTitle(), messageOfUser1.getTitle());
		Assert.assertEquals(thread1.getBody(), messageOfUser1.getBody());
		Assert.assertNotNull(messageOfUser1.getCreator());
		Assert.assertEquals(id1, messageOfUser1.getCreator());
		Assert.assertFalse(messageOfUser1.isGuest());
		Assert.assertNull(messageOfUser1.getThreadtop());
		
		//load and check the messages of second user
		List<MessageLight> messagesOfUser2 = forumManager.getLightMessagesByUser(forum, id2);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(messagesOfUser2);
		Assert.assertEquals(1, messagesOfUser2.size());
		MessageLight messageOfUser2 = messagesOfUser2.get(0);
		Assert.assertNotNull(messageOfUser2);
		Assert.assertEquals(reply.getKey(), messageOfUser2.getKey());
		Assert.assertEquals(reply.getTitle(), messageOfUser2.getTitle());
		Assert.assertEquals(reply.getBody(), messageOfUser2.getBody());
		Assert.assertNotNull(messageOfUser2.getCreator());
		Assert.assertEquals(id2, messageOfUser2.getCreator());
		Assert.assertFalse(messageOfUser2.isGuest());
		Assert.assertNotNull(messageOfUser2.getThreadtop());
		Assert.assertEquals(thread1.getKey(), messageOfUser2.getThreadtop().getKey());
		
		//load and check the messages of third user which use a pseudo
		List<MessageLight> messagesOfUser3 = forumManager.getLightMessagesByUser(forum, id3);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(messagesOfUser3);
		Assert.assertTrue(messagesOfUser3.isEmpty());
	}
	
	@Test
	public void getLightMessagesByUserUnderPseudo() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity id2 =  JunitTestHelper.createAndPersistIdentityAsRndUser("fo-2");
		Forum forum = forumManager.addAForum();
		dbInstance.commit();
		
		Message thread1 = forumManager.createMessage(forum, id1, false);
		thread1.setTitle("Get messages light by user with pseudo");
		thread1.setBody("Get messages light by user with pseudo");
		forumManager.addTopMessage(thread1);
		dbInstance.commit();
		
		Message reply = forumManager.createMessage(forum, id2, false);
		reply.setTitle("Re: Get messages light by user with pseudo");
		reply.setBody("Get messages light by user and other usefull stuff we need");
		forumManager.replyToMessage(reply, thread1);
		dbInstance.commitAndCloseSession();
		
		Message replyPseudo = forumManager.createMessage(forum, id2, false);
		replyPseudo.setTitle("Re: Get messages light by user with pseudo");
		replyPseudo.setBody("Get messages light by user and other usefull stuff we need");
		String pseudo = "Id pseudo " + UUID.randomUUID();
		replyPseudo.setPseudonym(pseudo);
		forumManager.replyToMessage(replyPseudo, thread1);
		dbInstance.commitAndCloseSession();
		
		//load and check the messages of user with pseudo
		List<MessageLight> messagesOfUser2 = forumManager.getLightMessagesByUserUnderPseudo(forum, id2, pseudo);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(messagesOfUser2);
		Assert.assertEquals(1, messagesOfUser2.size());
		MessageLight messageUnderPseudo = messagesOfUser2.get(0);
		Assert.assertNotNull(messageUnderPseudo);
		Assert.assertEquals(replyPseudo.getKey(), messageUnderPseudo.getKey());
		Assert.assertEquals(replyPseudo.getTitle(), messageUnderPseudo.getTitle());
		Assert.assertEquals(replyPseudo.getBody(), messageUnderPseudo.getBody());
		Assert.assertNotNull(messageUnderPseudo.getCreator());
		Assert.assertEquals(id2, messageUnderPseudo.getCreator());
		Assert.assertFalse(messageUnderPseudo.isGuest());
		Assert.assertNotNull(messageUnderPseudo.getThreadtop());
		Assert.assertEquals(thread1.getKey(), messageUnderPseudo.getThreadtop().getKey());
	}
	
	@Test
	public void getMessageById() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Forum forum = forumManager.addAForum();
		dbInstance.commit();
		
		Message message = forumManager.createMessage(forum, id, false);
		message.setTitle("Get message by id");
		message.setBody("Get message by id");
		forumManager.addTopMessage(message);
		dbInstance.commit();
		
		//load the message by id
		Message loadedMessage = forumManager.getMessageById(message.getKey());
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(loadedMessage);
		Assert.assertEquals(message.getKey(), loadedMessage.getKey());
		Assert.assertEquals(message.getTitle(), loadedMessage.getTitle());
		Assert.assertEquals(message.getBody(), loadedMessage.getBody());
		Assert.assertNotNull(loadedMessage.getCreator());
		Assert.assertEquals(id, loadedMessage.getCreator());
		Assert.assertFalse(loadedMessage.isGuest());
		Assert.assertNull(loadedMessage.getThreadtop());
	}
	
	@Test
	public void getPeekviewMessages() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity id2 =  JunitTestHelper.createAndPersistIdentityAsRndUser("fo-2");
		Forum forum = forumManager.addAForum();
		dbInstance.commit();
		
		Message thread1 = forumManager.createMessage(forum, id1, false);
		thread1.setTitle("Get peekview messages");
		thread1.setBody("Get peekview messages");
		forumManager.addTopMessage(thread1);
		dbInstance.commit();
		
		Message reply = forumManager.createMessage(forum, id2, false);
		reply.setTitle("Re: Get peekview messages");
		reply.setBody("Get peekview messages");
		forumManager.replyToMessage(reply, thread1);
		dbInstance.commitAndCloseSession();
		
		Message replyPseudo = forumManager.createMessage(forum, id2, false);
		replyPseudo.setTitle("Re: Get peekview messages with pseudo");
		replyPseudo.setBody("Get peekview messages and other usefull stuff we need");
		String pseudo = "Id pseudo " + UUID.randomUUID();
		replyPseudo.setPseudonym(pseudo);
		forumManager.replyToMessage(replyPseudo, thread1);
		dbInstance.commitAndCloseSession();
		
		//load the peekview
		List<MessagePeekview> peekViews = forumManager.getPeekviewMessages(forum, 2);
		Assert.assertNotNull(peekViews);
		Assert.assertEquals(2, peekViews.size());
		
		int found = 0;
		for(MessagePeekview peekView:peekViews) {
			if(peekView.getKey().equals(thread1.getKey())
					|| peekView.getKey().equals(reply.getKey())
					|| peekView.getKey().equals(replyPseudo.getKey())) {
				found++;
			}
		}
		Assert.assertEquals(2, found);
	}
	
	@Test
	public void getPseudonym() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity id2 =  JunitTestHelper.createAndPersistIdentityAsRndUser("fo-2");
		Forum forum = forumManager.addAForum();
		dbInstance.commit();
		
		Message thread1 = forumManager.createMessage(forum, id1, false);
		thread1.setTitle("Get pseudonym");
		thread1.setBody("Get pseudonym");
		forumManager.addTopMessage(thread1);
		dbInstance.commit();
	
		Message replyPseudo = forumManager.createMessage(forum, id2, false);
		replyPseudo.setTitle("Re: Get pseudonym");
		replyPseudo.setBody("Get pseudonym in forum and other usefull stuff we need");
		String pseudo = "Id pseudo " + UUID.randomUUID();
		replyPseudo.setPseudonym(pseudo);
		forumManager.replyToMessage(replyPseudo, thread1);
		dbInstance.commitAndCloseSession();
		
		// get pseudonym of id 2
		String alias2 = forumManager.getPseudonym(forum, id2);
		Assert.assertEquals(pseudo, alias2);
		
		// get pseudonym of id 1
		String alias1 = forumManager.getPseudonym(forum, id1);
		Assert.assertNull(alias1);
	}
	
	@Test
	public void readMessages() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-2");
		Forum forum = forumManager.addAForum();
		dbInstance.commit();
		
		Message thread1 = forumManager.createMessage(forum, id1, false);
		thread1.setTitle("Read messages workflow");
		thread1.setBody("Read messages workflow");
		forumManager.addTopMessage(thread1);
		dbInstance.commit();
	
		Message replyPseudo = forumManager.createMessage(forum, id1, false);
		replyPseudo.setTitle("Re: Read messages workflow");
		replyPseudo.setBody("Read messages workflow and other usefull stuff we need");
		String pseudo = "Id pseudo " + UUID.randomUUID();
		replyPseudo.setPseudonym(pseudo);
		forumManager.replyToMessage(replyPseudo, thread1);
		dbInstance.commitAndCloseSession();

		Message reply = forumManager.createMessage(forum, id2, false);
		reply.setTitle("Re: Read messages workflow");
		reply.setBody("Read messages workflow and other usefull stuff we need");
		forumManager.replyToMessage(reply, thread1);
		dbInstance.commitAndCloseSession();
		
		//mark thread1 as read
		forumManager.markAsRead(id1, forum, thread1);
		dbInstance.commitAndCloseSession();
		
		//load read set and check for id1
		Set<Long> readSet = forumManager.getReadSet(id1, forum);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(readSet);
		Assert.assertEquals(1, readSet.size());
		Assert.assertTrue(readSet.contains(thread1.getKey()));
		
		//mark thread1 as read
		forumManager.markAsRead(id2, forum, reply);
		forumManager.markAsRead(id2, forum, replyPseudo);
		dbInstance.commitAndCloseSession();
		
		//load read set and check for id2
		Set<Long> readSet2 = forumManager.getReadSet(id2, forum);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(readSet2);
		Assert.assertEquals(2, readSet2.size());
		Assert.assertTrue(readSet2.contains(reply.getKey()));
		Assert.assertTrue(readSet2.contains(replyPseudo.getKey()));
	}
	
	@Test
	public void updateMessage() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Forum fo = forumManager.addAForum();
		dbInstance.commit();

		Message topMessage = forumManager.createMessage(fo, id, false);
		topMessage.setTitle("Message counter");
		topMessage.setBody("Message counter");
		forumManager.addTopMessage(topMessage);
		dbInstance.commit();
		
		//update message
		topMessage.setBody("Message counter and other stuff");
		Message updatedMessage = forumManager.updateMessage(topMessage, true);
		Assert.assertNotNull(updatedMessage);
		Assert.assertEquals(topMessage.getKey(), updatedMessage.getKey());
		Assert.assertEquals("Message counter", updatedMessage.getTitle());
		Assert.assertEquals("Message counter and other stuff", updatedMessage.getBody());
		Assert.assertNotNull(updatedMessage.getNumOfCharacters());
		Assert.assertEquals(27, updatedMessage.getNumOfCharacters().intValue());
		Assert.assertNotNull(updatedMessage.getNumOfWords());
		Assert.assertEquals(5, updatedMessage.getNumOfWords().intValue());
	}
	
	@Test
	public void countMessagesByForumID() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-2");
		Forum fo = forumManager.addAForum();
		dbInstance.commit();

		Message topMessage = forumManager.createMessage(fo, id1, false);
		topMessage.setTitle("Messages count by forum");
		topMessage.setBody("Messages count by forum");
		forumManager.addTopMessage(topMessage);
		dbInstance.commit();

		Message reply = forumManager.createMessage(fo, id2, false);
		reply.setTitle("Re: Messages count by forum");
		reply.setBody("Messages count by forum");
		forumManager.replyToMessage(reply, topMessage);
		dbInstance.commit();
		
		Message reply2 = forumManager.createMessage(fo, id1, false);
		reply2.setTitle("Re: Re: Messages count by forum");
		reply2.setBody("Messages count by forum");
		forumManager.replyToMessage(reply2, reply);
		dbInstance.commit();
		
		int numOfMessages = forumManager.countMessagesByForumID(fo.getKey());
		Assert.assertEquals("Not the right number of messages for this forum", 3, numOfMessages);
	}

	@Test
	public void countThreadsByForumID() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-2");
		Forum fo = forumManager.addAForum();
		dbInstance.commit();

		Message topMessage = forumManager.createMessage(fo, id1, false);
		topMessage.setTitle("Threads count by forum");
		topMessage.setBody("Threads count by forum");
		forumManager.addTopMessage(topMessage);
		dbInstance.commit();

		Message reply = forumManager.createMessage(fo, id2, false);
		reply.setTitle("Re: Threads count by forum");
		reply.setBody("Threads count by forum");
		forumManager.replyToMessage(reply, topMessage);
		dbInstance.commit();
		
		Message topMessage2 = forumManager.createMessage(fo, id2, false);
		topMessage2.setTitle("More on threads count by forum");
		topMessage2.setBody("More on threads count by forum");
		forumManager.addTopMessage(topMessage2);
		dbInstance.commit();
		
		int numOfThreads = forumManager.countThreadsByForumID(fo.getKey());
		Assert.assertEquals("Not the right number of threads for this forum", 2, numOfThreads);
	}
	
	@Test
	public void getNewMessageInfo() {
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
	public void moveMessage() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-2");
		Identity guest3 =  securityManager.getAndUpdateAnonymousUserForLanguage(Locale.ENGLISH);
		Forum fo = forumManager.addAForum();
		dbInstance.commit();
		
		// thread
		// -> message
		// -> -> message to move
		// -> -> -> message child 1
		// -> -> -> -> message child 1.1
		// -> -> -> message child 2
		// -> -> message staying

		Message topMessage = forumManager.createMessage(fo, id1, false);
		topMessage.setTitle("Thread move message");
		topMessage.setBody("Thread move message");
		forumManager.addTopMessage(topMessage);
		dbInstance.commit();

		Message message = forumManager.createMessage(fo, id2, false);
		message.setTitle("Re: Thread move message");
		message.setBody("Thread move message");
		forumManager.replyToMessage(message, topMessage);
		dbInstance.commit();
		
		Message messageToMove = forumManager.createMessage(fo, id2, false);
		messageToMove.setTitle("Message to move");
		messageToMove.setBody("Thread move message");
		forumManager.replyToMessage(messageToMove, message);
		dbInstance.commit();
		
		Message messageToMove_1 = forumManager.createMessage(fo, id2, false);
		messageToMove_1.setTitle("Re: Message to move 1");
		messageToMove_1.setBody("Thread move message");
		forumManager.replyToMessage(messageToMove_1, messageToMove);
		dbInstance.commit();
		
		Message messageToMove_1_1 = forumManager.createMessage(fo, guest3, true);
		messageToMove_1_1.setTitle("Re: Message to move 1");
		messageToMove_1_1.setBody("Thread move message");
		forumManager.replyToMessage(messageToMove_1_1, messageToMove_1);
		dbInstance.commit();
		
		Message messageToMove_2 = forumManager.createMessage(fo, id2, false);
		messageToMove_2.setTitle("Re: Message to move 2");
		messageToMove_2.setBody("Thread move message");
		forumManager.replyToMessage(messageToMove_2, messageToMove);
		dbInstance.commit();
		
		Message messageToStay = forumManager.createMessage(fo, id2, false);
		messageToStay.setTitle("Message to stay");
		messageToStay.setBody("Thread move message");
		forumManager.replyToMessage(messageToStay, message);
		dbInstance.commit();
			
		Message targetThread = forumManager.createMessage(fo, id2, false);
		targetThread.setTitle("Target thread");
		targetThread.setBody("Target thread");
		forumManager.addTopMessage(targetThread);
		dbInstance.commit();
		
		Message targetMessage = forumManager.createMessage(fo, id2, false);
		targetMessage.setTitle("Message to stay");
		targetMessage.setBody("Thread move message");
		forumManager.replyToMessage(targetMessage, targetThread);
		dbInstance.commit();
		
		//move the message
		Message movedMessage = forumManager.moveMessage(messageToMove, targetMessage, null);
		dbInstance.commitAndCloseSession();
		
		//check target thread
		List<Message> targetMessages = forumManager.getThread(targetThread.getKey());
		Assert.assertEquals(3, targetMessages.size());
		Assert.assertTrue(targetMessages.contains(targetThread));
		Assert.assertTrue(targetMessages.contains(targetMessage));
		Assert.assertTrue(targetMessages.contains(movedMessage));
		
		//check thread and parent of the target thread
		Message reloadedTargetThread = forumManager.getMessageById(targetThread.getKey());
		Assert.assertNull(reloadedTargetThread.getThreadtop());
		Assert.assertNull(reloadedTargetThread.getParent());

		Message reloadedTargetMessage = forumManager.getMessageById(targetMessage.getKey());
		Assert.assertEquals(targetThread, reloadedTargetMessage.getThreadtop());
		Assert.assertEquals(targetThread, reloadedTargetMessage.getParent());

		Message reloadedMovedMessage = forumManager.getMessageById(movedMessage.getKey());
		Assert.assertEquals(targetThread, reloadedMovedMessage.getThreadtop());
		Assert.assertEquals(targetMessage, reloadedMovedMessage.getParent());
		
		//check original thread
		List<Message> originMessages = forumManager.getThread(topMessage.getKey());
		Assert.assertEquals(6, originMessages.size());
		Assert.assertTrue(originMessages.contains(topMessage));
		Assert.assertTrue(originMessages.contains(message));
		Assert.assertTrue(originMessages.contains(messageToStay));
		Assert.assertTrue(originMessages.contains(messageToMove_1));
		Assert.assertTrue(originMessages.contains(messageToMove_1_1));
		Assert.assertTrue(originMessages.contains(messageToMove_2));
		Assert.assertFalse(originMessages.contains(movedMessage));
		Assert.assertFalse(originMessages.contains(messageToMove));
		
		// thread
		// -> message
		// -> -> message child 1
		// -> -> -> message child 1.1
		// -> -> message child 2
		// -> -> message staying
		
		//check thread and parent of the target thread
		Message reloadedTopMessage = forumManager.getMessageById(topMessage.getKey());
		Assert.assertNull(reloadedTopMessage.getThreadtop());
		Assert.assertNull(reloadedTopMessage.getParent());
		
		Message reloadedMessage = forumManager.getMessageById(message.getKey());
		Assert.assertEquals(topMessage, reloadedMessage.getThreadtop());
		Assert.assertEquals(topMessage, reloadedMessage.getParent());

		Message reloadedMessageToMove_1 = forumManager.getMessageById(messageToMove_1.getKey());
		Assert.assertEquals(topMessage, reloadedMessageToMove_1.getThreadtop());
		Assert.assertEquals(message, reloadedMessageToMove_1.getParent());
		
		Message reloadedMessageToMove_1_1 = forumManager.getMessageById(messageToMove_1_1.getKey());
		Assert.assertEquals(topMessage, reloadedMessageToMove_1_1.getThreadtop());
		Assert.assertEquals(messageToMove_1, reloadedMessageToMove_1_1.getParent());
		
		Message reloadedMessageToMove_2 = forumManager.getMessageById(messageToMove_1.getKey());
		Assert.assertEquals(topMessage, reloadedMessageToMove_2.getThreadtop());
		Assert.assertEquals(message, reloadedMessageToMove_2.getParent());
		
		Message reloadedMessageToStay = forumManager.getMessageById(messageToStay.getKey());
		Assert.assertEquals(topMessage, reloadedMessageToStay.getThreadtop());
		Assert.assertEquals(message, reloadedMessageToStay.getParent());
	}
	
	@Test
	public void moveMessageToAnotherForum() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-3");
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-4");
		Identity guest3 =  securityManager.getAndUpdateAnonymousUserForLanguage(Locale.ENGLISH);
		Forum fo1 = forumManager.addAForum();
		dbInstance.commit();
		Forum fo2 = forumManager.addAForum();
		dbInstance.commit();
		
		//Forum 1
		Message topMessage = forumManager.createMessage(fo1, id1, false);
		topMessage.setTitle("Thread move message");
		topMessage.setBody("Thread move message");
		forumManager.addTopMessage(topMessage);
		dbInstance.commit();

		Message message = forumManager.createMessage(fo1, id2, false);
		message.setTitle("Re: Thread move message");
		message.setBody("Thread move message");
		forumManager.replyToMessage(message, topMessage);
		dbInstance.commit();
		
		Message messageToMove = forumManager.createMessage(fo1, id3, false);
		messageToMove.setTitle("Message to move");
		messageToMove.setBody("Thread move message");
		forumManager.replyToMessage(messageToMove, message);
		dbInstance.commit();
		
		Message messageToMove_1 = forumManager.createMessage(fo1, id4, false);
		messageToMove_1.setTitle("Re: Message to move 1");
		messageToMove_1.setBody("Thread move message");
		forumManager.replyToMessage(messageToMove_1, messageToMove);
		dbInstance.commit();
		
		Message messageToMove_1_1 = forumManager.createMessage(fo1, guest3, true);
		messageToMove_1_1.setTitle("Re: Message to move 1");
		messageToMove_1_1.setBody("Thread move message");
		forumManager.replyToMessage(messageToMove_1_1, messageToMove_1);
		dbInstance.commit();
		
		//Forum 2
		Message targetThread = forumManager.createMessage(fo2, id1, false);
		targetThread.setTitle("Thread move message Forum2");
		targetThread.setBody("Thread move message Forum2");
		forumManager.addTopMessage(targetThread);
		dbInstance.commit();

		Message message_f2 = forumManager.createMessage(fo2, id2, false);
		message_f2.setTitle("Re: Thread move message Forum2");
		message_f2.setBody("Thread move message Forum2");
		forumManager.replyToMessage(message_f2, targetThread);
		dbInstance.commit();
		
		Message messageToMove_f2 = forumManager.createMessage(fo2, id3, false);
		messageToMove_f2.setTitle("Message to move Forum2");
		messageToMove_f2.setBody("Thread move message Forum2");
		forumManager.replyToMessage(messageToMove_f2, targetThread);
		dbInstance.commit();
		
		Message targetMessage = forumManager.createMessage(fo2, id4, false);
		targetMessage.setTitle("Re: Message to move 1 Forum2 Target");
		targetMessage.setBody("Thread move message Forum2");
		forumManager.replyToMessage(targetMessage, messageToMove_f2);
		dbInstance.commit();
		
		Message messageToMove_1_1_f2 = forumManager.createMessage(fo2, guest3, true);
		messageToMove_1_1_f2.setTitle("Re: Message to move 1 Forum2");
		messageToMove_1_1_f2.setBody("Thread move message Forum2");
		forumManager.replyToMessage(messageToMove_1_1_f2, targetMessage);
		dbInstance.commit();
		
		//move the message
		Message movedMessage = forumManager.moveMessageToAnotherForum(messageToMove, fo2, targetMessage, null);
		List<Message> children = forumManager.getMessageChildren(movedMessage);
		
		//check target thread
		List<Message> targetMessages = forumManager.getThread(targetThread.getKey());
		Assert.assertEquals(8, targetMessages.size());
		Assert.assertTrue(targetMessages.contains(targetThread));
		Assert.assertTrue(targetMessages.contains(targetMessage));
		Assert.assertTrue(targetMessages.contains(movedMessage));
		Assert.assertTrue(targetMessages.contains(children.get(0)));

		
		// check original thread
		List<Message> originMessages = forumManager.getThread(topMessage.getKey());
		Assert.assertEquals(2, originMessages.size());
		Assert.assertFalse(originMessages.contains(messageToMove));
		Assert.assertTrue(originMessages.contains(topMessage));
		Assert.assertTrue(originMessages.contains(message));
		Assert.assertFalse(originMessages.contains(messageToMove_1));
		Assert.assertFalse(originMessages.contains(messageToMove_1_1));
		
		//check thread and parent of the target thread
		Message reloadedTopMessage = forumManager.getMessageById(targetThread.getKey());
		Assert.assertNull(reloadedTopMessage.getThreadtop());
		Assert.assertNull(reloadedTopMessage.getParent());	
	}
	
	@Test
	public void createOrAppendThreadInAnotherForum() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-3");
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-4");
		Identity guest3 =  securityManager.getAndUpdateAnonymousUserForLanguage(Locale.ENGLISH);
		Forum fo1 = forumManager.addAForum();
		dbInstance.commit();
		Forum fo2 = forumManager.addAForum();
		dbInstance.commit();
		
		//Forum 1
		Message topMessage = forumManager.createMessage(fo1, id1, false);
		topMessage.setTitle("0Thread move message");
		topMessage.setBody("Thread move message");
		forumManager.addTopMessage(topMessage);
		dbInstance.commit();

		Message message = forumManager.createMessage(fo1, id2, false);
		message.setTitle("1Re: Thread move message");
		message.setBody("Thread move message");
		forumManager.replyToMessage(message, topMessage);
		dbInstance.commit();
		
		Message messageToMove = forumManager.createMessage(fo1, id3, false);
		messageToMove.setTitle("2Message to move");
		messageToMove.setBody("Thread move message");
		forumManager.replyToMessage(messageToMove, message);
		dbInstance.commit();
		
		Message messageToMove_1 = forumManager.createMessage(fo1, id4, false);
		messageToMove_1.setTitle("3Re: Message to move 1");
		messageToMove_1.setBody("Thread move message");
		forumManager.replyToMessage(messageToMove_1, messageToMove);
		dbInstance.commit();
		
		Message messageToMove_1_1 = forumManager.createMessage(fo1, guest3, true);
		messageToMove_1_1.setTitle("4Re: Message to move 1");
		messageToMove_1_1.setBody("Thread move message");
		forumManager.replyToMessage(messageToMove_1_1, messageToMove_1);
		dbInstance.commit();
		
		// Another Thread
		Message topMsg = forumManager.createMessage(fo1, id1, false);
		topMsg.setTitle("Top move message");
		topMsg.setBody("Top move message");
		forumManager.addTopMessage(topMsg);
		dbInstance.commit();

		Message msg = forumManager.createMessage(fo1, id4, false);
		msg.setTitle("Re: Thread move message");
		msg.setBody("Thread move message");
		forumManager.replyToMessage(msg, topMsg);
		dbInstance.commit();
		
		//Forum 2
		Message targetThread = forumManager.createMessage(fo2, id1, false);
		targetThread.setTitle("5Thread move message Forum2");
		targetThread.setBody("Thread move message Forum2");
		forumManager.addTopMessage(targetThread);
		dbInstance.commit();

		Message message_f2 = forumManager.createMessage(fo2, id2, false);
		message_f2.setTitle("6Re: Thread move message Forum2");
		message_f2.setBody("Thread move message Forum2");
		forumManager.replyToMessage(message_f2, targetThread);
		dbInstance.commit();
		
		Message messageToMove_f2 = forumManager.createMessage(fo2, id3, false);
		messageToMove_f2.setTitle("7Message to move Forum2");
		messageToMove_f2.setBody("Thread move message Forum2");
		forumManager.replyToMessage(messageToMove_f2, targetThread);
		dbInstance.commit();
		
		Message targetMessage = forumManager.createMessage(fo2, id4, false);
		targetMessage.setTitle("8Re: Message to move 1 Forum2 Target");
		targetMessage.setBody("Thread move message Forum2");
		forumManager.replyToMessage(targetMessage, messageToMove_f2);
		dbInstance.commit();
		
		Message messageToMove_1_1_f2 = forumManager.createMessage(fo2, guest3, true);
		messageToMove_1_1_f2.setTitle("9Re: Message to move 1 Forum2");
		messageToMove_1_1_f2.setBody("Thread move message Forum2");
		forumManager.replyToMessage(messageToMove_1_1_f2, targetMessage);
		dbInstance.commit();
		
		// move thread to forum as new thread
		Message newthread = forumManager.createOrAppendThreadInAnotherForum(topMsg, fo2, null, null);
		
		// check if newthread is in another forum
		Assert.assertEquals(fo2, newthread.getForum());

		// move thread to another forum to append to another thread
		Message movedthread = forumManager.createOrAppendThreadInAnotherForum(topMessage, fo2, targetThread, null);
		
		//check target thread
		List<Message> targetMessages = forumManager.getThread(targetThread.getKey(), 0, -1, Message.OrderBy.title, true); 
		Assert.assertEquals(10, targetMessages.size());
		Assert.assertTrue(targetMessages.contains(targetThread));
		Assert.assertTrue(targetMessages.contains(targetMessage));
		Assert.assertTrue(targetMessages.contains(movedthread));
		
		// check if hierarchy is consistent 
		Message fo1messagetomove_1 = forumManager.getMessageById(targetMessages.get(3).getKey());
		Message fo1messagetomove_1_1 = forumManager.getMessageById(targetMessages.get(4).getKey());
		Assert.assertEquals(fo1messagetomove_1, fo1messagetomove_1_1.getParent());
		
		Message fo2targetMessage = forumManager.getMessageById(targetMessage.getKey());
		Message fo2messageToMove_1_1_f2 = forumManager.getMessageById(messageToMove_1_1_f2.getKey());
		Assert.assertEquals(fo2targetMessage, fo2messageToMove_1_1_f2.getParent());
		int index3 = targetMessages.indexOf(targetMessage);
		int index4 = targetMessages.indexOf(messageToMove_1_1_f2);				
		Assert.assertEquals(targetMessages.get(index3), targetMessages.get(index4).getParent());
		
	}	
	
	
	@Test
	public void splitMessage() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-2");
		Identity guest3 =  securityManager.getAndUpdateAnonymousUserForLanguage(Locale.ENGLISH);
		Forum fo = forumManager.addAForum();
		dbInstance.commit();
		
		// thread
		// -> message
		// -> -> message to split
		// -> -> -> message child 1
		// -> -> -> -> message child 1.1
		// -> -> -> message child 2
		// -> -> message staying

		Message topMessage = forumManager.createMessage(fo, id1, false);
		topMessage.setTitle("Thread split message");
		topMessage.setBody("Thread split message");
		forumManager.addTopMessage(topMessage);
		dbInstance.commit();

		Message message = forumManager.createMessage(fo, id2, false);
		message.setTitle("Re: Thread split message");
		message.setBody("Thread split message");
		forumManager.replyToMessage(message, topMessage);
		dbInstance.commit();
		
		Message messageToSplit = forumManager.createMessage(fo, id2, false);
		messageToSplit.setTitle("Message to split");
		messageToSplit.setBody("Thread split message");
		forumManager.replyToMessage(messageToSplit, message);
		dbInstance.commit();
		
		Message messageToSplit_1 = forumManager.createMessage(fo, id2, false);
		messageToSplit_1.setTitle("Re: Message to split 1");
		messageToSplit_1.setBody("Thread split message");
		forumManager.replyToMessage(messageToSplit_1, messageToSplit);
		dbInstance.commit();
		
		Message messageToSplit_1_1 = forumManager.createMessage(fo, guest3, true);
		messageToSplit_1_1.setTitle("Re: Re: Message to split 1");
		messageToSplit_1_1.setBody("Thread split message");
		forumManager.replyToMessage(messageToSplit_1_1, messageToSplit_1);
		dbInstance.commit();
		
		Message messageToSplit_2 = forumManager.createMessage(fo, id2, false);
		messageToSplit_2.setTitle("Re: Message to split 2");
		messageToSplit_2.setBody("Thread split message");
		forumManager.replyToMessage(messageToSplit_2, messageToSplit);
		dbInstance.commit();
		
		Message messageToStay = forumManager.createMessage(fo, id2, false);
		messageToStay.setTitle("Message to stay");
		messageToStay.setBody("Thread split message");
		forumManager.replyToMessage(messageToStay, message);
		dbInstance.commit();
		
		//move the message
		Message splitedMessage = forumManager.splitThread(messageToSplit);
		dbInstance.commitAndCloseSession();
		
		//check the original thread
		// thread
		// -> message
		// -> -> message staying
		List<Message> originalMessages = forumManager.getThread(topMessage.getKey());
		Assert.assertEquals(3, originalMessages.size());
		Assert.assertTrue(originalMessages.contains(topMessage));
		Assert.assertTrue(originalMessages.contains(message));
		Assert.assertTrue(originalMessages.contains(messageToStay));
		
		//check thread and parent of the target thread
		Message reloadedTopMessage = forumManager.getMessageById(topMessage.getKey());
		Assert.assertNull(reloadedTopMessage.getThreadtop());
		Assert.assertNull(reloadedTopMessage.getParent());

		Message reloadedMessage = forumManager.getMessageById(message.getKey());
		Assert.assertEquals(topMessage, reloadedMessage.getThreadtop());
		Assert.assertEquals(topMessage, reloadedMessage.getParent());

		Message reloadedMessageToStay = forumManager.getMessageById(messageToStay.getKey());
		Assert.assertEquals(topMessage, reloadedMessageToStay.getThreadtop());
		Assert.assertEquals(message, reloadedMessageToStay.getParent());
		
		//check original thread
		// message to split
		// -> message child 1
		// -> -> message child 1.1
		// -> message child 2
		List<Message> splitedMessages = forumManager.getThread(splitedMessage.getKey());
		Assert.assertEquals(4, splitedMessages.size());
		Assert.assertTrue(splitedMessages.contains(splitedMessage));
		Assert.assertTrue(splitedMessages.contains(messageToSplit_1));
		Assert.assertTrue(splitedMessages.contains(messageToSplit_1_1));
		Assert.assertTrue(splitedMessages.contains(messageToSplit_2));

		//check thread and parent of the splited thread
		
		Message reloadedmessageToSplit = forumManager.getMessageById(messageToSplit.getKey());
		Assert.assertNull(reloadedmessageToSplit.getThreadtop());
		Assert.assertNull(reloadedmessageToSplit.getParent());

		Message reloadedMessageToSplit_1 = forumManager.getMessageById(messageToSplit_1.getKey());
		Assert.assertEquals(messageToSplit, reloadedMessageToSplit_1.getThreadtop());
		Assert.assertEquals(messageToSplit, reloadedMessageToSplit_1.getParent());
		
		Message reloadedMessageToSplit_1_1 = forumManager.getMessageById(messageToSplit_1_1.getKey());
		Assert.assertEquals(messageToSplit, reloadedMessageToSplit_1_1.getThreadtop());
		Assert.assertEquals(messageToSplit_1, reloadedMessageToSplit_1_1.getParent());
		
		Message reloadedMessageToSplit_2 = forumManager.getMessageById(messageToSplit_1.getKey());
		Assert.assertEquals(messageToSplit, reloadedMessageToSplit_2.getThreadtop());
		Assert.assertEquals(messageToSplit, reloadedMessageToSplit_2.getParent());
	}

	/**
	 * The test doesn't test directly the method but check
	 * if the loading mechanism in the method work in 2 different
	 * cases and if it accept already deleted messages
	 * 
	 */
	@Test
	public void deleteMessagePropertiesTree_checkFindBehavior() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-10");
		Forum fo = forumManager.addAForum();
		dbInstance.commit();

		Message topMessage = forumManager.createMessage(fo, id1, false);
		topMessage.setTitle("Future deleted message 1");
		topMessage.setBody("Future deleted  stuff");
		forumManager.addTopMessage(topMessage);
		dbInstance.commit();
		
		//reload
		Message reloadedMessage = dbInstance.getCurrentEntityManager().find(MessageImpl.class, topMessage.getKey());
		Assert.assertNotNull(reloadedMessage);
		//reload inexistent message
		Message inexistentMessage = dbInstance.getCurrentEntityManager().find(MessageImpl.class, -23l);
		Assert.assertNull(inexistentMessage);
		
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void deleteMessageTree() {
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
	
	@Test
	public void deleteMessageTree_withMarks() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-15");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-16");
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
		
		//mark as read
		forumManager.markAsRead(id1, fo, topMessage);
		forumManager.markAsRead(id1, fo, reply);
		forumManager.markAsRead(id1, fo, reply2);
		forumManager.markAsRead(id2, fo, topMessage);
		forumManager.markAsRead(id2, fo, reply);
		forumManager.markAsRead(id2, fo, reply2);
		dbInstance.commitAndCloseSession();
		
		//delete a message
		forumManager.deleteMessageTree(fo.getKey(), reply2);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void deleteForum() {
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
	
	@Test
	public void deleteForum_bigForumWithMarks() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-17");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-18");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-19");
		Forum fo = forumManager.addAForum();
		dbInstance.commit();
		
		List<Message> messages = new ArrayList<>();
		for(int i=0; i<5; i++) {
			Message topMessage = forumManager.createMessage(fo, id1, false);
			topMessage.setTitle("Future deleted forum part. " + i);
			topMessage.setBody("Future deleted  stuff");
			forumManager.addTopMessage(topMessage);
			messages.add(topMessage);
			dbInstance.commit();
			
			for(int j=0; j<3; j++) {
				Message reply = forumManager.createMessage(fo, id2, false);
				reply.setTitle("Future deleted forum part. " + i + "." + j);
				reply.setBody("Future deleted  stuff");
				forumManager.replyToMessage(reply, topMessage);
				messages.add(reply);
				dbInstance.commit();

				for(int k=0; k<3; k++) {
					Message reply2 = forumManager.createMessage(fo, id3, false);
					reply2.setTitle("Future deleted forum part. " + i + "." + j + "." + k);
					reply2.setBody("Future deleted  stuff");
					forumManager.replyToMessage(reply2, reply);
					messages.add(reply2);
					dbInstance.commitAndCloseSession();
				}
			}
		}
		
		for(Message message:messages) {
			forumManager.markAsRead(id1, fo, message);
			forumManager.markAsRead(id2, fo, message);
			forumManager.markAsRead(id3, fo, message);
		}
		dbInstance.commitAndCloseSession();

		//delete the forum
		forumManager.deleteForum(fo.getKey());
		dbInstance.commit();
	}
	
	@Test
	public void mergeForums() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-9");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-10");
		
		Forum masterForum = forumManager.addAForum();
		dbInstance.commit();
		
		Message topMessage = forumManager.createMessage(masterForum, id1, false);
		topMessage.setTitle("Future deleted forum part. 1");
		topMessage.setBody("Future deleted  stuff");
		forumManager.addTopMessage(topMessage);
		dbInstance.commit();

		Message reply = forumManager.createMessage(masterForum, id2, false);
		reply.setTitle("Future deleted forum part. 2");
		reply.setBody("Future deleted  stuff");
		forumManager.replyToMessage(reply, topMessage);
		dbInstance.commit();
		
		//forum to merge in master
		Forum altForum = forumManager.addAForum();
		dbInstance.commit();
		
		Message topAltMessage = forumManager.createMessage(altForum, id1, false);
		topAltMessage.setTitle("Future deleted forum part. 1");
		topAltMessage.setBody("Future deleted  stuff");
		forumManager.addTopMessage(topAltMessage);
		dbInstance.commit();

		Message replyAlt = forumManager.createMessage(altForum, id2, false);
		replyAlt.setTitle("Future deleted forum part. 2");
		replyAlt.setBody("Future deleted  stuff");
		forumManager.replyToMessage(replyAlt, topAltMessage);
		dbInstance.commitAndCloseSession();
		
		//merge
		List<Long> forumsToMerge = new ArrayList<>();
		forumsToMerge.add(altForum.getKey());
		int changed = forumManager.mergeForums(masterForum.getKey(), forumsToMerge);
		dbInstance.commit();
		Assert.assertEquals(2, changed);
		
		//check that the merged forum is empty
		List<MessageLight> altMessages = forumManager.getLightMessagesByForum(altForum);
		Assert.assertNotNull(altMessages);
		Assert.assertTrue(altMessages.isEmpty());
		
		//check that the target forum has all messages
		List<Message> messages = forumManager.getMessagesByForum(masterForum);
		Assert.assertNotNull(messages);
		Assert.assertEquals(4, messages.size());
		//paranoia check
		Assert.assertTrue(messages.contains(topMessage));
		Assert.assertTrue(messages.contains(reply));
		Assert.assertTrue(messages.contains(topAltMessage));
		Assert.assertTrue(messages.contains(replyAlt));
		for(Message message:messages) {
			Assert.assertEquals(message.getForum(), masterForum);
		}
	}
	
	@Test
	public void createProtectedPseudonym() {
		String pseudonym = UUID.randomUUID().toString();
		String password = "secret"; 
		
		Pseudonym protectedPseudo = forumManager.createProtectedPseudonym(pseudonym, password);
		dbInstance.commit();
		
		Assert.assertNotNull(protectedPseudo);
		Assert.assertNotNull(protectedPseudo.getKey());
		Assert.assertNotNull(protectedPseudo.getCreationDate());
		Assert.assertNotNull(protectedPseudo.getCredential());
		Assert.assertNotNull(protectedPseudo.getSalt());
		Assert.assertNotNull(protectedPseudo.getAlgorithm());
		Assert.assertEquals(pseudonym, protectedPseudo.getPseudonym());
	}
	
	@Test
	public void loadProtectedPseudonym() {
		String pseudonym = UUID.randomUUID().toString();
		String password = "secret"; 
		
		Pseudonym protectedPseudo = forumManager.createProtectedPseudonym(pseudonym, password);
		dbInstance.commitAndCloseSession();
		
		//load and check the content
		Pseudonym reloadedPseudo = forumManager.getPseudonymByKey(protectedPseudo.getKey());
		Assert.assertNotNull(reloadedPseudo);
		Assert.assertNotNull(reloadedPseudo.getKey());
		Assert.assertNotNull(reloadedPseudo.getCreationDate());
		Assert.assertNotNull(reloadedPseudo.getCredential());
		Assert.assertNotNull(reloadedPseudo.getSalt());
		Assert.assertNotNull(reloadedPseudo.getAlgorithm());
		Assert.assertEquals(pseudonym, reloadedPseudo.getPseudonym());
	}
	
	@Test
	public void getPseudonyms() {
		String pseudonym = UUID.randomUUID().toString();
		String password = "secret"; 
		
		Pseudonym protectedPseudo = forumManager.createProtectedPseudonym(pseudonym, password);
		dbInstance.commitAndCloseSession();
		
		//load and check the content
		List<Pseudonym> thePseudo = forumManager.getPseudonyms(pseudonym);
		Assert.assertNotNull(thePseudo);
		Assert.assertEquals(1, thePseudo.size());
		Assert.assertTrue(thePseudo.contains(protectedPseudo));
		
		//negative tests
		List<Pseudonym> noPseudo = forumManager.getPseudonyms(UUID.randomUUID().toString() + "break");
		Assert.assertNotNull(noPseudo);
		Assert.assertTrue(noPseudo.isEmpty());
	}
	
	@Test
	public void authenticatePseudonym() {
		String pseudonym = UUID.randomUUID().toString();
		String password = "thesecret"; 
		forumManager.createProtectedPseudonym(pseudonym, password);
		dbInstance.commitAndCloseSession();
		
		//load 
		List<Pseudonym> thePseudos = forumManager.getPseudonyms(pseudonym);
		Assert.assertNotNull(thePseudos);
		Assert.assertEquals(1, thePseudos.size());
		Pseudonym thePseudo = thePseudos.get(0);
		
		//check authentication
		boolean ok = forumManager.authenticatePseudonym(thePseudo, password);
		Assert.assertTrue(ok);
		//negative tests
		boolean notOk = forumManager.authenticatePseudonym(thePseudo, "12345");
		Assert.assertFalse(notOk);
	}
	
	@Test
	public void isPseudonymProtected() {
		//create a reference
		String pseudonym = UUID.randomUUID().toString();
		String password = "thesecret"; 
		forumManager.createProtectedPseudonym(pseudonym, password);
		dbInstance.commitAndCloseSession();
		
		//load 
		boolean protectedYes = forumManager.isPseudonymProtected(pseudonym);
		Assert.assertTrue(protectedYes);
		//negative tests
		boolean protectedNo = forumManager.isPseudonymProtected("12345");
		Assert.assertFalse(protectedNo);
	}
	
	@Test
	public void isPseudonymInUseInForums() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-1");
		Forum forum = forumManager.addAForum();
		dbInstance.commit();
		
		Message thread = forumManager.createMessage(forum, id1, false);
		thread.setTitle("Get pseudonym");
		thread.setBody("Get pseudonym");
		String pseudo = "Id pseudo " + UUID.randomUUID();
		thread.setPseudonym(pseudo);
		forumManager.addTopMessage(thread);
		dbInstance.commit();

		//load 
		boolean protectedYes = forumManager.isPseudonymInUseInForums(pseudo);
		Assert.assertTrue(protectedYes);
		//negative tests
		boolean protectedNo = forumManager.isPseudonymInUseInForums("12345");
		Assert.assertFalse(protectedNo);
	}
	
	@Test
	public void getPseudonymStatistics() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-9");
		Forum forum = forumManager.addAForum();
		dbInstance.commit();
		
		Message thread = forumManager.createMessage(forum, id, false);
		thread.setTitle("Message with pseudonym");
		thread.setBody("Message with pseudonym");
		String pseudo = "Id pseudo " + UUID.randomUUID();
		thread.setPseudonym(pseudo);
		forumManager.addTopMessage(thread);
		Pseudonym protectedPseudo = forumManager.createProtectedPseudonym(pseudo, "secret");
		dbInstance.commit();
		
		//check statistics
		List<PseudonymStatistics> stats = forumManager.getPseudonymStatistics(null);
		Assert.assertNotNull(stats);
		Assert.assertTrue(stats.size() > 0);
		PseudonymStatistics thePseudoStats = null;
		for(PseudonymStatistics stat:stats) {
			if(pseudo.equals(stat.getPseudonym())) {
				thePseudoStats = stat;
			}
		}
		Assert.assertNotNull(thePseudoStats);
		Assert.assertEquals(protectedPseudo.getKey(), thePseudoStats.getKey());
		Assert.assertEquals(Long.valueOf(1l), thePseudoStats.getNumOfMessages());
		
		//check with search
	}
	
	@Test
	public void getPseudonymStatistics_searchString() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("fo-10");
		Forum forum = forumManager.addAForum();
		dbInstance.commit();
		
		Message thread = forumManager.createMessage(forum, id, false);
		thread.setTitle("Message with pseudonym");
		thread.setBody("Message with pseudonym");
		String pseudo = "Search string pseudo " + UUID.randomUUID();
		thread.setPseudonym(pseudo);
		forumManager.addTopMessage(thread);
		Pseudonym protectedPseudo = forumManager.createProtectedPseudonym(pseudo, "secret");
		dbInstance.commit();
		
		//check statistics with search
		List<PseudonymStatistics> stats = forumManager.getPseudonymStatistics("Search string");
		Assert.assertNotNull(stats);
		Assert.assertTrue(stats.size() > 0);
		PseudonymStatistics thePseudoStats = null;
		for(PseudonymStatistics stat:stats) {
			if(pseudo.equals(stat.getPseudonym())) {
				thePseudoStats = stat;
			}
		}
		Assert.assertNotNull(thePseudoStats);
		Assert.assertEquals(protectedPseudo.getKey(), thePseudoStats.getKey());
		Assert.assertEquals(Long.valueOf(1l), thePseudoStats.getNumOfMessages());
		
		//check negative
		List<PseudonymStatistics> emptyStats = forumManager.getPseudonymStatistics("This string is never a pseudo");
		Assert.assertNotNull(emptyStats);
		Assert.assertTrue(emptyStats.isEmpty());
	}
}
