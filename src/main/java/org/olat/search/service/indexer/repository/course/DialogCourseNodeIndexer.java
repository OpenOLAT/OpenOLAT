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
*/

package org.olat.search.service.indexer.repository.course;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSLeafFilter;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.dialog.DialogElement;
import org.olat.course.nodes.dialog.DialogElementsManager;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.Status;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.repository.RepositoryEntry;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.CourseNodeDocument;
import org.olat.search.service.document.ForumMessageDocument;
import org.olat.search.service.document.file.DocumentAccessException;
import org.olat.search.service.document.file.FileDocumentFactory;
import org.olat.search.service.indexer.DefaultIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Indexer for dialog course-node.
 * @author Christian Guretzki
 */
public class DialogCourseNodeIndexer extends DefaultIndexer implements CourseNodeIndexer {
	
	private static final Logger log = Tracing.createLoggerFor(DialogCourseNodeIndexer.class);
	
	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public static final String TYPE_MESSAGE = "type.course.node.dialog.forum.message";
	public static final String TYPE_FILE    = "type.course.node.dialog.file";

	private static final String SUPPORTED_TYPE_NAME = "org.olat.course.nodes.DialogCourseNode";
	
	@Override
	public void doIndex(SearchResourceContext searchResourceContext, Object parentObject, OlatFullIndexer indexer)
			throws IOException, InterruptedException {
		//
	}

	@Override
	public void doIndex(SearchResourceContext repositoryResourceContext, ICourse course, CourseNode courseNode, OlatFullIndexer indexWriter) throws IOException,InterruptedException  {
		SearchResourceContext courseNodeResourceContext = createSearchResourceContext(repositoryResourceContext, courseNode, null);
		Document document = CourseNodeDocument.createDocument(courseNodeResourceContext, courseNode);
		indexWriter.addDocument(document);
		
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		DialogElementsManager dialogElmsMgr = CoreSpringFactory.getImpl(DialogElementsManager.class);
		List<DialogElement> elements = dialogElmsMgr.getDialogElements(entry, courseNode.getIdent());
		for (DialogElement element:elements) {
			Forum forum = element.getForum();
			doIndexAllMessages(courseNodeResourceContext, forum, indexWriter );
			doIndexFile(element, courseNodeResourceContext, indexWriter);
		}
	}

	/**
	 * Index a file of dialog-module.
	 * @param filename
	 * @param forumKey
	 * @param leafResourceContext
	 * @param indexWriter
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void doIndexFile(DialogElement element, SearchResourceContext leafResourceContext, OlatFullIndexer indexWriter)
	throws IOException,InterruptedException {
		DialogElementsManager dialogElmsMgr = CoreSpringFactory.getImpl(DialogElementsManager.class);
		VFSContainer dialogContainer = dialogElmsMgr.getDialogContainer(element);
		VFSLeaf leaf = (VFSLeaf) dialogContainer.getItems(new VFSLeafFilter()).get(0);
		if (log.isDebugEnabled()) log.debug("Analyse VFSLeaf=" + leaf.getName());
		try {
			if (CoreSpringFactory.getImpl(FileDocumentFactory.class).isFileSupported(leaf)) {
				leafResourceContext.setFilePath(element.getFilename());
				leafResourceContext.setDocumentType(TYPE_FILE);
				
				Document document = CoreSpringFactory.getImpl(FileDocumentFactory.class).createDocument(leafResourceContext, leaf);
				indexWriter.addDocument(document);
			} else {
				if (log.isDebugEnabled()) log.debug("Documenttype not supported. file=" + leaf.getName());
			}
		} catch (DocumentAccessException e) {
			if (log.isDebugEnabled()) log.debug("Can not access document." + e.getMessage());
		} catch (IOException ioEx) {
			log.warn("IOException: Can not index leaf=" + leaf.getName(), ioEx);
		} catch (InterruptedException iex) {
			throw new InterruptedException(iex.getMessage());
		} catch (Exception ex) {
			log.warn("Exception: Can not index leaf=" + leaf.getName(), ex);
		}
	}

	private void doIndexAllMessages(SearchResourceContext parentResourceContext, Forum forum, OlatFullIndexer indexWriter) throws IOException,InterruptedException {
		// loop over all messages of a forum
		List<Message> messages = CoreSpringFactory.getImpl(ForumManager.class).getMessagesByForum(forum);
		for(Message message:messages){
			SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
			searchResourceContext.setBusinessControlFor(message);
			searchResourceContext.setDocumentType(TYPE_MESSAGE);
			Document document = ForumMessageDocument.createDocument(searchResourceContext, message);
		  indexWriter.addDocument(document);
		}
	}

	@Override
	public String getSupportedTypeName() {
		return SUPPORTED_TYPE_NAME;
	}
	
	@Override
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles)  {
		ContextEntry ce = businessControl.popLauncherContextEntry();
		if(ce == null || ce.getOLATResourceable() == null || ce.getOLATResourceable().getResourceableId() == null) {
			return true;// it's the node itself
		}
		
		OLATResourceable ores = ce.getOLATResourceable();
		if(log.isDebugEnabled()) log.debug("OLATResourceable={}", ores);
		if (ores.getResourceableTypeName().startsWith("path=")) {
			// => it is a file element, typeName format: 'path=/test1/test2/readme.txt'
			return true;
		} else if (ores.getResourceableTypeName().equals(OresHelper.calculateTypeName(Message.class))) {
			// it is message => check message access
			Long resourceableId = ores.getResourceableId();
			Message message = CoreSpringFactory.getImpl(ForumManager.class).loadMessage(resourceableId);
			Message threadtop = message.getThreadtop();
			if(threadtop==null) {
				threadtop = message;
			}
			boolean isMessageHidden = Status.getStatus(threadtop.getStatusCode()).isHidden(); 
			//assumes that if is owner then is moderator so it is allowed to see the hidden forum threads	
			if(isMessageHidden) {
				return false;
			}		
			return true;
		} else {
			log.warn("In DialogCourseNode unkown OLATResourceable={}", ores);
			return false;
		}
	}
}
