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

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.nodes.ta.DropboxController;
import org.olat.course.nodes.ta.ReturnboxController;
import org.olat.course.nodes.ta.SolutionController;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.CourseNodeDocument;
import org.olat.search.service.indexer.FolderIndexer;
import org.olat.search.service.indexer.FolderIndexerAccess;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Indexer for TA (task) course-node.
 * @author Christian Guretzki
 */
public class TACourseNodeIndexer extends FolderIndexer implements CourseNodeIndexer {
	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public final static String TYPE_TASK        = "type.course.node.ta.task";
	public final static String TYPE_DROPBOX     = "type.course.node.ta.dropbox";
	public final static String TYPE_RETURNBOX   = "type.course.node.ta.returnbox";
	public final static String TYPE_SOLUTIONBOX = "type.course.node.ta.solutionbox";

	private final static String SUPPORTED_TYPE_NAME = "org.olat.course.nodes.TACourseNode";

	@Override
	public void doIndex(SearchResourceContext repositoryResourceContext, ICourse course, CourseNode courseNode, OlatFullIndexer indexWriter) throws IOException,InterruptedException  {
		SearchResourceContext courseNodeResourceContext = createSearchResourceContext(repositoryResourceContext, courseNode, null);
		Document nodeDocument = CourseNodeDocument.createDocument(courseNodeResourceContext, courseNode);
		indexWriter.addDocument(nodeDocument);
		
		// Index Task
		File fTaskfolder = new File(FolderConfig.getCanonicalRoot() + TACourseNode.getTaskFolderPathRelToFolderRoot(course.getCourseEnvironment(), courseNode));
		VFSContainer taskRootContainer = new LocalFolderImpl(fTaskfolder);
		courseNodeResourceContext.setDocumentType(TYPE_TASK);
		doIndexVFSContainer(courseNodeResourceContext, taskRootContainer, indexWriter, "", FolderIndexerAccess.FULL_ACCESS);
		
		// Index Dropbox
		String dropboxFilePath = FolderConfig.getCanonicalRoot() + DropboxController.getDropboxPathRelToFolderRoot(course.getCourseEnvironment(), courseNode);
		File fDropboxFolder = new File(dropboxFilePath);
		VFSContainer dropboxRootContainer = new LocalFolderImpl(fDropboxFolder);
		courseNodeResourceContext.setDocumentType(TYPE_DROPBOX);
		doIndexVFSContainer(courseNodeResourceContext, dropboxRootContainer, indexWriter, "", FolderIndexerAccess.FULL_ACCESS);
		
		// Index Returnbox
		String returnboxFilePath = FolderConfig.getCanonicalRoot() + ReturnboxController.getReturnboxPathRelToFolderRoot(course.getCourseEnvironment(), courseNode);
		File fResturnboxFolder = new File(returnboxFilePath);
		VFSContainer returnboxRootContainer = new LocalFolderImpl(fResturnboxFolder);
		courseNodeResourceContext.setDocumentType(TYPE_RETURNBOX);
		doIndexVFSContainer(courseNodeResourceContext, returnboxRootContainer, indexWriter, "", FolderIndexerAccess.FULL_ACCESS);
		
		// Index Solutionbox
		String solutionFilePath = FolderConfig.getCanonicalRoot() + SolutionController.getSolutionPathRelToFolderRoot(course.getCourseEnvironment(), courseNode);
		File fSolutionFolder = new File(solutionFilePath);
		VFSContainer solutionRootContainer = new LocalFolderImpl(fSolutionFolder);
		courseNodeResourceContext.setDocumentType(TYPE_SOLUTIONBOX);
		doIndexVFSContainer(courseNodeResourceContext, solutionRootContainer, indexWriter, "", FolderIndexerAccess.FULL_ACCESS);
	}

	@Override
	public String getSupportedTypeName() {
		return SUPPORTED_TYPE_NAME;
	}
	
	
	@Override
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		if(roles.isGuestOnly()) {
			return false;
		}
		return super.checkAccess(contextEntry, businessControl, identity, roles);
	}
}
