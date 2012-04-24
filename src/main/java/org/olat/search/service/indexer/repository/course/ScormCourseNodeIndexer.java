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
package org.olat.search.service.indexer.repository.course;

import java.io.File;
import java.io.IOException;

import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ScormCourseNode;
import org.olat.fileresource.FileResourceManager;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.indexer.OlatFullIndexer;
import org.olat.search.service.indexer.repository.ScormRepositoryIndexer;

/**
 * 
 * Description:<br>
 * Index SCORM package in course
 * 
 * <P>
 * Initial Date:  11 dec. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class ScormCourseNodeIndexer extends ScormRepositoryIndexer implements CourseNodeIndexer {
	public final static String NODE_TYPE = "type.course.node.scorm";
	private final static String SUPPORTED_TYPE_NAME = "org.olat.course.nodes.ScormCourseNode";

	@Override
	public void doIndex(SearchResourceContext repositoryResourceContext, ICourse course, CourseNode courseNode, OlatFullIndexer indexWriter)
			throws IOException, InterruptedException {

    SearchResourceContext courseNodeResourceContext = new SearchResourceContext(repositoryResourceContext);
    courseNodeResourceContext.setBusinessControlFor(courseNode);
    courseNodeResourceContext.setDocumentType(NODE_TYPE);
    courseNodeResourceContext.setTitle(courseNode.getShortTitle());
    courseNodeResourceContext.setDescription(courseNode.getLongTitle());
		
		ScormCourseNode scormNode = (ScormCourseNode)courseNode;
		RepositoryEntry repoEntry = scormNode.getReferencedRepositoryEntry();
		OLATResource ores = repoEntry.getOlatResource();
		File cpRoot = FileResourceManager.getInstance().unzipFileResource(ores);
		
		doIndex(courseNodeResourceContext, indexWriter, cpRoot);
	}

	@Override
	public String getSupportedTypeName() {
		return SUPPORTED_TYPE_NAME;
	}
}