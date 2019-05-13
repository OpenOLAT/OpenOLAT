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

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.Status;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.properties.Property;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.CourseNodeDocument;
import org.olat.search.service.indexer.ForumIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Indexer for FO (forum) course-node.
 * @author Christian Guretzki
 */
public class FOCourseNodeIndexer extends ForumIndexer implements CourseNodeIndexer {
	private static final Logger log = Tracing.createLoggerFor(FOCourseNodeIndexer.class);
	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public static final String TYPE = "type.course.node.forum.message";

	private static final String SUPPORTED_TYPE_NAME = "org.olat.course.nodes.FOCourseNode";

	@Override
	public void doIndex(SearchResourceContext repositoryResourceContext, ICourse course, CourseNode courseNode, OlatFullIndexer indexWriter) {
		try {
			SearchResourceContext courseNodeResourceContext = createSearchResourceContext(repositoryResourceContext, courseNode, TYPE);
			Document document = CourseNodeDocument.createDocument(courseNodeResourceContext, courseNode);
			indexWriter.addDocument(document);
			
			doIndexForum(courseNodeResourceContext, course, courseNode, indexWriter);
		} catch(Exception ex) {
			log.error("Exception indexing courseNode=" + courseNode, ex);
		}
	}

	@Override
	public String getSupportedTypeName() {
		return SUPPORTED_TYPE_NAME;
	}
	
	@Override
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		ContextEntry ce = businessControl.popLauncherContextEntry();
		if(ce == null || ce.getOLATResourceable() == null || ce.getOLATResourceable().getResourceableId() == null) {
			return true;//it's the node itself
		}
		
		Long resourceableId = ce.getOLATResourceable().getResourceableId();
		Message message = CoreSpringFactory.getImpl(ForumManager.class).loadMessage(resourceableId);
		if(message != null) {
			Message threadtop = message.getThreadtop();
			if(threadtop == null) {
				threadtop = message;
			}
			boolean isMessageHidden = Status.getStatus(threadtop.getStatusCode()).isHidden(); 
			//assumes that if is owner then is moderator so it is allowed to see the hidden forum threads
			// TODO policy owner: (LD) fix this!!! - the contextEntry is not the right context for this check
			if(isMessageHidden) {
				return false;
			}
		}
		return super.checkAccess(contextEntry, businessControl, identity, roles);	
	}

	/**
	 * Index a forum in a course.
	 * @param parentResourceContext
	 * @param course
	 * @param courseNode
	 * @param indexWriter
	 * @throws IOException
	 */
	private void doIndexForum(SearchResourceContext parentResourceContext, ICourse course, CourseNode courseNode, OlatFullIndexer indexWriter) throws IOException,InterruptedException  {
		if (log.isDebugEnabled()) log.debug("Index Course Forum...");
		ForumManager fom = CoreSpringFactory.getImpl(ForumManager.class);
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();

		Property forumKeyProperty = cpm.findCourseNodeProperty(courseNode, null, null, FOCourseNode.FORUM_KEY);
		// Check if forum-property exist
		if (forumKeyProperty != null) {
		  Long forumKey = forumKeyProperty.getLongValue();
		  Forum forum = fom.loadForum(forumKey);
		  parentResourceContext.setDocumentType(TYPE);
		  doIndexAllMessages(parentResourceContext, forum, indexWriter );
		}
	}

}
