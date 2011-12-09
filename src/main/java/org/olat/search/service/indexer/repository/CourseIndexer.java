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

package org.olat.search.service.indexer.repository;

import java.io.IOException;
import java.util.List;

import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.navigation.NavigationHandler;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.TreeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatus;
import org.olat.repository.RepositoryManager;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.indexer.Indexer;
import org.olat.search.service.indexer.OlatFullIndexer;
import org.olat.search.service.indexer.repository.course.CourseNodeIndexer;
import org.olat.search.service.indexer.repository.course.CourseNodeIndexerFactory;

/**
 * Index a hole course.
 * @author Christian Guretzki
 */
public class CourseIndexer implements Indexer {
	private static final OLog log = Tracing.createLoggerFor(CourseIndexer.class);
	
	public final static String TYPE = "type.repository.entry.CourseModule"; 
	
	private RepositoryManager repositoryManager;
	
	public CourseIndexer() {
		repositoryManager = RepositoryManager.getInstance();
	}
	
	/**
	 * 
	 */
	public String getSupportedTypeName() {	
		return CourseModule.getCourseTypeName(); 
	}
	
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsDownload()
	 */

	public void doIndex(SearchResourceContext parentResourceContext, Object parentObject, OlatFullIndexer indexWriter) {
		RepositoryEntry repositoryEntry = (RepositoryEntry) parentObject;
		if (log.isDebug()) log.debug("Analyse Course... repositoryEntry=" + repositoryEntry);
		try {
			RepositoryEntryStatus status = RepositoryManager.getInstance().createRepositoryEntryStatus(repositoryEntry.getStatusCode());
			if(status.isClosed()) {
				if(log.isDebug()) log.debug("Course not indexed because it's closed: repositoryEntry=" + repositoryEntry);
				return;
			}

			ICourse course = CourseFactory.loadCourse(repositoryEntry.getOlatResource());
			// course.getCourseTitle(); // do not index title => index root-node
			parentResourceContext.setParentContextType(TYPE);
			parentResourceContext.setParentContextName(course.getCourseTitle());
			doIndexCourse( parentResourceContext, course,  course.getRunStructure().getRootNode(), indexWriter);			
		} catch (Exception ex) {
			log.warn("Can not index repositoryEntry=" + repositoryEntry,ex);
		}
	}

	public void doIndexCourse(SearchResourceContext repositoryResourceContext, ICourse course, CourseNode courseNode, OlatFullIndexer indexWriter) throws IOException,InterruptedException  {
		// loop over all child nodes
		int childCount = courseNode.getChildCount();
		for (int i=0;i<childCount; i++) {
			INode childCourseNode = courseNode.getChildAt(i);
			if (childCourseNode instanceof CourseNode) {
				if (log.isDebug()) log.debug("Analyse CourseNode child ... childCourseNode=" + childCourseNode);
  			// go further with resource
  			CourseNodeIndexer courseNodeIndexer = CourseNodeIndexerFactory.getInstance().getCourseNodeIndexer( (CourseNode)childCourseNode);
  			if (courseNodeIndexer != null) {
  				if (log.isDebug()) log.debug("courseNodeIndexer=" + courseNodeIndexer);
   				try {
						courseNodeIndexer.doIndex(repositoryResourceContext, course, (CourseNode)childCourseNode, indexWriter);
					} catch (Exception e) {
						log.warn("Can not index course node=" + childCourseNode.getIdent(), e);
					}
  			} else {
  				if (log.isDebug()) log.debug("No CourseNodeIndexer for " + childCourseNode);				
  		    // go further, index my child nodes
  				doIndexCourse(repositoryResourceContext, course, (CourseNode)childCourseNode, indexWriter);
  			}
			} else {
				if (log.isDebug()) log.debug("ChildNode is no CourseNode, " + childCourseNode);
			}
		}
	}

	/**
	 * Bean setter method used by spring. 
	 * @param indexerList
	 */
	public void setIndexerList(List<CourseNodeIndexer> indexerList) {
		if (indexerList == null)
			throw new AssertException("null value for indexerList not allowed.");

		try {
			for (CourseNodeIndexer courseNodeIndexer : indexerList) {
				CourseNodeIndexerFactory.getInstance().registerIndexer(courseNodeIndexer);
				if (log.isDebug()) log.debug("Adding indexer from configuraton: ");
			} 
		}	catch (ClassCastException cce) {
				throw new StartupException("Configured indexer is not of type RepositoryEntryIndexer", cce);
		}
	}

	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		ContextEntry bcContextEntry = businessControl.popLauncherContextEntry();
		if (bcContextEntry == null) {
			// no context-entry anymore, the repository entry itself is the context entry, 
			// not a course node of course we have access to the course metadata
			return true;
		}
		if (log.isDebug()) log.debug("Start identity=" + identity + "  roles=" + roles);
		Long repositoryKey = contextEntry.getOLATResourceable().getResourceableId();
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(repositoryKey);
		if (log.isDebug()) log.debug("repositoryEntry=" + repositoryEntry );

		Long nodeId = bcContextEntry.getOLATResourceable().getResourceableId();
		if (log.isDebug()) log.debug("nodeId=" + nodeId );
		
		ICourse course = CourseFactory.loadCourse(repositoryEntry.getOlatResource());
		IdentityEnvironment ienv = new IdentityEnvironment();
		ienv.setIdentity(identity);
		ienv.setRoles(roles);
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
		if (log.isDebug()) log.debug("userCourseEnv=" + userCourseEnv + "ienv=" + ienv );
		
		CourseNode rootCn = userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();

		String nodeIdS = nodeId.toString();
		CourseNode courseNode = course.getRunStructure().getNode(nodeIdS);
		if (log.isDebug()) log.debug("courseNode=" + courseNode );
		
		TreeEvaluation treeEval = new TreeEvaluation();
		NodeEvaluation rootNodeEval = rootCn.eval(userCourseEnv.getConditionInterpreter(), treeEval);
		if (log.isDebug()) log.debug("rootNodeEval=" + rootNodeEval );

		TreeNode newCalledTreeNode = treeEval.getCorrespondingTreeNode(courseNode);
		if (newCalledTreeNode == null) {
			// TreeNode no longer visible
			return false;
		}
		// go further
		NodeEvaluation nodeEval = (NodeEvaluation) newCalledTreeNode.getUserObject();
		if (log.isDebug()) log.debug("nodeEval=" + nodeEval );
		if (nodeEval.getCourseNode() != courseNode) throw new AssertException("error in structure");
		if (!nodeEval.isVisible()) throw new AssertException("node eval not visible!!");
		if (log.isDebug()) log.debug("call mayAccessWholeTreeUp..." );
		boolean mayAccessWholeTreeUp = NavigationHandler.mayAccessWholeTreeUp(nodeEval);	
		if (log.isDebug()) log.debug("call mayAccessWholeTreeUp=" + mayAccessWholeTreeUp );
		
		if (mayAccessWholeTreeUp) {
			CourseNodeIndexer courseNodeIndexer = CourseNodeIndexerFactory.getInstance().getCourseNodeIndexer(courseNode);
			return courseNodeIndexer.checkAccess(bcContextEntry, businessControl, identity, roles);		
		} else {
  		return false;
		}
	}

}
