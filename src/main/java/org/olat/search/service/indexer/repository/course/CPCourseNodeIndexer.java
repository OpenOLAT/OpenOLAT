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
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.cp.CPEditController;
import org.olat.fileresource.FileResourceManager;
import org.olat.repository.RepositoryEntry;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.CourseNodeDocument;
import org.olat.search.service.indexer.FolderIndexer;
import org.olat.search.service.indexer.FolderIndexerAccess;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Indexer for BC (content-package) course-node.
 * @author Christian Guretzki
 */
public class CPCourseNodeIndexer extends FolderIndexer implements CourseNodeIndexer {

	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public static final String TYPE = "type.course.node.cp";

	private static final String SUPPORTED_TYPE_NAME = "org.olat.course.nodes.CPCourseNode";
	
	@Override
	public void doIndex(SearchResourceContext repositoryResourceContext, ICourse course, CourseNode courseNode, OlatFullIndexer indexWriter)
	throws IOException,InterruptedException  {
		SearchResourceContext courseNodeResourceContext = createSearchResourceContext(repositoryResourceContext, courseNode, TYPE);
		Document document = CourseNodeDocument.createDocument(courseNodeResourceContext, courseNode);
		indexWriter.addDocument(document);
		
	    RepositoryEntry re = CPEditController.getCPReference(courseNode.getModuleConfiguration(), false);
	    if(re != null) {
	    	File cpRoot = FileResourceManager.getInstance().unzipFileResource(re.getOlatResource());
	    	if(cpRoot != null) {
	    		VFSContainer rootContainer = new LocalFolderImpl(cpRoot);
	    		doIndexVFSContainer(courseNodeResourceContext, rootContainer, indexWriter, "", FolderIndexerAccess.FULL_ACCESS);
	    	}
	    }
	}

	@Override
	public String getSupportedTypeName() {
		return SUPPORTED_TYPE_NAME;
	}
}
