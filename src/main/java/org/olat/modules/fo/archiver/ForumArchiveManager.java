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
* <p>
*/ 

package org.olat.modules.fo.archiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumCallback;
import org.olat.modules.fo.ForumHelper;
import org.olat.modules.fo.ForumManager;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.MessageNode;
import org.olat.modules.fo.archiver.formatters.ForumFormatter;

/**
 *          Initial Date: Nov 11, 2005 <br>
 * @author Alexander Schneider
 */

public class ForumArchiveManager  extends BasicManager {
	private static ForumArchiveManager instance = new ForumArchiveManager();
	
	//TODO spring config
	private ForumArchiveManager() {
		// private since singleton
	}
	
	/**
	 * @return the singleton
	 */
	public static ForumArchiveManager getInstance() {
		return instance;
	}
	
	/**
	 * If the forumCallback is null no restriction applies to the forum archiver. 
	 * (that is it can archive all threads no matter the status)
	 * @param forumFormatter
	 * @param forumId
	 * @param forumCallback
	 * @return
	 */
	public String applyFormatter(ForumFormatter forumFormatter, long forumId, ForumCallback forumCallback){
		Tracing.logInfo("Archiving complete forum: "+forumId, ForumArchiveManager.class);
		Map metaInfo = new HashMap();
		metaInfo.put(ForumFormatter.MANDATORY_METAINFO_KEY ,new Long(forumId));
		//convert forum structure to trees
		List threadTreesList = convertToThreadTrees(forumId, forumCallback);
		//format forum trees by using the formatter given by the callee
		return formatForum(threadTreesList, forumFormatter, metaInfo);
	}
	/**
	 * It is assumed that if the caller of this method is allowed to see the forum thread
	 * starting from topMessageId, then he also has the right to archive it, so no need for a ForumCallback.
	 * @param forumFormatter
	 * @param forumId
	 * @param topMessageId
	 * @return the message thread as String formatted
	 */
	public String applyFormatterForOneThread(ForumFormatter forumFormatter, long forumId, long topMessageId){
		Tracing.logInfo("Archiving forum.thread: "+forumId+"."+topMessageId, ForumArchiveManager.class);
		Map metaInfo = new HashMap();
		metaInfo.put(ForumFormatter.MANDATORY_METAINFO_KEY ,new Long(forumId));
		MessageNode topMessageNode = convertToThreadTree(topMessageId);
		return formatThread(topMessageNode, forumFormatter, metaInfo);
	}

	/**
	 * If the forumCallback is null no filtering is executed, 
	 * else if a thread is hidden and the user doesn't have moderator rights the
	 * hidden thread is not included into the archive.
	 * @param forumId
	 * @param metaInfo
	 * @return all top message nodes together with their children in a list
	 */
	private List convertToThreadTrees(long forumId, ForumCallback forumCallback){
		List messages;
		List topNodeList = new ArrayList();
		ForumManager fm = ForumManager.getInstance();
		Long l = new Long(forumId);
		Forum f = fm.loadForum(l);
		messages = fm.getMessagesByForum(f);
		
		for (Iterator iterTop = messages.iterator(); iterTop.hasNext();) {
			Message msg = (Message) iterTop.next();
			if (msg.getParent() == null) {
				iterTop.remove();
				MessageNode topNode = new MessageNode(msg);
				if(topNode.isHidden() && (forumCallback==null || (forumCallback!=null && forumCallback.mayEditMessageAsModerator()))) {
					addChildren(messages, topNode);
					topNodeList.add(topNode);
				}	else if(!topNode.isHidden()) {
					addChildren(messages, topNode);
					topNodeList.add(topNode);
				}
			}
		}	
		return getMessagesSorted(topNodeList);
	}
	
  /**
   * Sorts the input list by adding the sticky messages first.
   * @param topNodeList
   * @return the sorted list.
   */	
	private List getMessagesSorted(List<Message> topNodeList) { 
		 Comparator messageNodeComparator = ForumHelper.getMessageNodeComparator();
		 Collections.sort(topNodeList, messageNodeComparator);
		 return topNodeList;
	}
	
	/**
	 * 
	 * @param messageId
	 * @param metaInfo
	 * @return the top message node with all its children
	 */
	private MessageNode convertToThreadTree(long topMessageId){
		List messages;
		MessageNode topNode = null;
		ForumManager fm = ForumManager.getInstance();
		Long l = new Long(topMessageId);
		messages = fm.getThread(l);
		for (Iterator iterTop = messages.iterator(); iterTop.hasNext();) {
			Message msg = (Message) iterTop.next();
			if (msg.getParent() == null) {
				iterTop.remove();
				topNode = new MessageNode(msg);
				addChildren(messages, topNode);
			}
		}
		return topNode;
	}
	
	private void addChildren(List messages, MessageNode mn){
		for(Iterator iterMsg = messages.iterator();iterMsg.hasNext();){
			Message msg = (Message) iterMsg.next();
			if ((msg.getParent() != null) && (msg.getParent().getKey() == mn.getKey())){
				MessageNode childNode = new MessageNode(msg);
				mn.addChild(childNode);
				//FIXME:as:c next line is not necessary
				childNode.setParent(mn);
				addChildren(messages, childNode);
			}
		}
	}
	
	/**
	 * 
	 * @param topNodeList
	 * @param forumFormatter
	 * @param metaInfo
	 * @return
	 */
	private String formatForum(List topNodeList, ForumFormatter forumFormatter, Map metaInfo){ 
		forumFormatter.setForumMetaInformation(metaInfo);
		StringBuilder formattedForum = new StringBuilder();
		forumFormatter.openForum();
		for (Iterator iterTop = topNodeList.iterator(); iterTop.hasNext();){
			MessageNode mn = (MessageNode) iterTop.next();
			//a new top thread starts, inform formatter
			forumFormatter.openThread();
			TreeVisitor tv = new TreeVisitor(forumFormatter, mn, false);
			tv.visitAll();
			//commit
			formattedForum.append(forumFormatter.closeThread());
		}
		return formattedForum.append(forumFormatter.closeForum().toString()).toString();
	}
	
	/**
	 * 
	 * @param mn
	 * @param forumFormatter
	 * @param metaInfo
	 * @return
	 */
	private String formatThread(MessageNode mn, ForumFormatter forumFormatter, Map metaInfo){
		forumFormatter.setForumMetaInformation(metaInfo);
		StringBuilder formattedThread = new StringBuilder();
		forumFormatter.openThread();
		TreeVisitor tv = new TreeVisitor(forumFormatter, mn, false);
		tv.visitAll();
		return formattedThread.append(formattedThread.append(forumFormatter.closeThread())).toString();
	}
	
}
