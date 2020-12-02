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
package org.olat.modules.fo.archiver;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.openxml.DocReference;
import org.olat.core.util.openxml.OpenXMLDocumentWriter;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumCallback;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.archiver.formatters.ForumOpenXMLFormatter;
import org.olat.modules.fo.manager.ForumManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ForumArchive {
	
	private static final Logger log = Tracing.createLoggerFor(ForumArchive.class);
	
	@Autowired
	private ForumManager forumManager;
	
	private final Locale locale;
	private final Forum forum;
	private final Long topMessageId;
	private final ForumCallback forumCallback;
	
	public ForumArchive(Forum forum, Long topMessageId, Locale locale, ForumCallback forumCallback) {
		CoreSpringFactory.autowireObject(this);
		this.forum = forum;
		this.locale = locale;
		this.topMessageId = topMessageId;
		this.forumCallback = forumCallback;
	}
	
	public void export(String name, VFSContainer exportDir)
	throws IOException {

		Map<File,DocReference> attachments = null;
		VFSLeaf forumDoc = exportDir.createChildLeaf(name);
		try(OutputStream out = forumDoc.getOutputStream(false)) {
			attachments = exportForum(out);
		} catch(IOException e) {
			log.error("", e);
		}
		
		if(attachments != null && attachments.size() > 0) {
			VFSContainer attachmentsContainer = VFSManager.getOrCreateContainer(exportDir, "attachments");
			for(Map.Entry<File,DocReference> attachmentEntry : attachments.entrySet()) {
				File attachment = attachmentEntry.getKey();
				DocReference ref = attachmentEntry.getValue();
				VFSLeaf leaf = attachmentsContainer.createChildLeaf(ref.getFilename());
				VFSManager.copyContent(attachment, leaf, null);
			}
		}
	}
	
	public void export(String name, ZipOutputStream zout)
	throws IOException {
		export(name, "", zout);
	}
	
	public void export(String name, String path, ZipOutputStream zout)
	throws IOException {
		ZipEntry test = new ZipEntry(ZipUtil.concat(path, name));
		zout.putNextEntry(test);
		Map<File,DocReference> attachments = null;
		try(ShieldOutputStream sOut = new ShieldOutputStream(zout)) {
			attachments = exportForum(sOut);
		} catch(IOException e) {
			log.error("", e);
		}
		zout.closeEntry();
		
		if(attachments != null && attachments.size() > 0) {
			for(Map.Entry<File,DocReference> attachmentEntry : attachments.entrySet()) {
				File attachment = attachmentEntry.getKey();
				DocReference ref = attachmentEntry.getValue();
				zout.putNextEntry(new ZipEntry(ZipUtil.concat(path, "attachments/" + ref.getFilename())));
				copyShielded(attachment, zout);
				zout.closeEntry();
			}
		}
	}
	
	private Map<File,DocReference> exportForum(OutputStream out) {
		try(ZipOutputStream zout = new ZipOutputStream(out)) {
			zout.setLevel(9);
			
			VFSContainer mediaContainer = forumManager.getForumContainer(forum.getKey());
			
			ForumOpenXMLFormatter openXmlFormatter = new ForumOpenXMLFormatter(mediaContainer, locale);
			if(topMessageId != null) {
				applyFormatterForOneThread(openXmlFormatter, topMessageId);
			} else {
				applyFormatter(openXmlFormatter);
			}
			
			OpenXMLDocumentWriter writer = new OpenXMLDocumentWriter();
			writer.createDocument(zout, openXmlFormatter.getOpenXMLDocument());
			return openXmlFormatter.getAttachments();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private void copyShielded(File attachment, ZipOutputStream zout) {
		try(OutputStream out = new ShieldOutputStream(zout)) {
			Files.copy(attachment.toPath(), out);
		} catch(Exception e) {
			log.error("", e);
		}
	}
	
	/**
	 * If the forumCallback is null no restriction applies to the forum archiver. 
	 * (that is it can archive all threads no matter the status)
	 * @param forumFormatter The formatter
	 * @return
	 */
	private void applyFormatter(ForumOpenXMLFormatter forumFormatter) {
		log.info("Archiving complete forum: {}", forum);
		//convert forum structure to trees
		List<MessageNode> threadTreesList = convertToThreadTrees();
		//format forum trees by using the formatter given by the callee
		formatForum(threadTreesList, forumFormatter);
	}
	
	/**
	 * It is assumed that if the caller of this method is allowed to see the forum thread
	 * starting from topMessageId, then he also has the right to archive it, so no need for a ForumCallback.
	 * @param forumFormatter The formatter
	 * @param messageId The root message id
	 */
	private void applyFormatterForOneThread(ForumOpenXMLFormatter forumFormatter, Long messageId){
		MessageNode topMessageNode = convertToThreadTree(messageId);
		formatThread(topMessageNode, forumFormatter);
	}

	/**
	 * If the forumCallback is null no filtering is executed, 
	 * else if a thread is hidden and the user doesn't have moderator rights the
	 * hidden thread is not included into the archive.
	 * 
	 * @return all top message nodes together with their children in a list
	 */
	private List<MessageNode> convertToThreadTrees() {
		List<MessageNode> topNodeList = new ArrayList<>();

		List<Message> messages = forumManager.getMessagesByForum(forum);
		for (Iterator<Message> iterTop = messages.iterator(); iterTop.hasNext();) {
			Message msg = iterTop.next();
			if (msg.getParent() == null) {
				iterTop.remove();
				MessageNode topNode = new MessageNode(msg);
				if(!topNode.isHidden()
						|| (topNode.isHidden() && (forumCallback == null || forumCallback.mayEditMessageAsModerator()))) {
					addChildren(messages, topNode);
					topNodeList.add(topNode);
				}
			}
		}
		Collections.sort(topNodeList, new MessageNodeComparator());
		return topNodeList;
	}
	
	public static class MessageNodeComparator implements Comparator<MessageNode> {
		@Override
		public int compare(final MessageNode m1, final MessageNode m2) {			
			if(m1.isSticky() && m2.isSticky()) {
				return m2.getModifiedDate().compareTo(m1.getModifiedDate()); //last first
			} else if(m1.isSticky()) {
				return -1;
			} else if(m2.isSticky()){
				return 1;
			} else {
				return m2.getModifiedDate().compareTo(m1.getModifiedDate()); //last first
			}				
		}
	}
	
	/**
	 * 
	 * @param messageId
	 * @param metaInfo
	 * @return the top message node with all its children
	 */
	private MessageNode convertToThreadTree(Long messagedId) {
		MessageNode topNode = null;
		List<Message> messages = forumManager.getThread(messagedId);
		for (Iterator<Message> iterTop = messages.iterator(); iterTop.hasNext();) {
			Message msg = iterTop.next();
			if (msg.getParent() == null) {
				iterTop.remove();
				topNode = new MessageNode(msg);
				addChildren(messages, topNode);
			}
		}
		return topNode;
	}
	
	private void addChildren(List<Message> messages, MessageNode mn){
		for(Iterator<Message> iterMsg = messages.iterator(); iterMsg.hasNext(); ) {
			Message msg = iterMsg.next();
			if (msg.getParent() != null && msg.getParent().getKey().equals(mn.getKey())){
				MessageNode childNode = new MessageNode(msg);
				mn.addChild(childNode);
				childNode.setParent(mn);
				addChildren(messages, childNode);
			}
		}
	}
	
	private void formatForum(List<MessageNode> topNodeList, ForumOpenXMLFormatter forumFormatter) { 
		forumFormatter.openForum();
		for (Iterator<MessageNode> iterTop = topNodeList.iterator(); iterTop.hasNext();){
			MessageNode mn = iterTop.next();
			//a new top thread starts, inform formatter
			forumFormatter.openThread();
			TreeVisitor tv = new TreeVisitor(forumFormatter, mn, false);
			tv.visitAll();
		}
	}
	
	private void formatThread(MessageNode mn, ForumOpenXMLFormatter forumFormatter) {
		forumFormatter.openThread();
		TreeVisitor tv = new TreeVisitor(forumFormatter, mn, false);
		tv.visitAll();
	}
}
