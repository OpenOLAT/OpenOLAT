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

package org.olat.search.service.indexer.repository.course;


import java.io.IOException;

import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * 
 * @author Christian Guretzki
 */
public interface CourseNodeIndexer {
	
	public void doIndex(SearchResourceContext searchResourceContext, ICourse course, CourseNode node, OlatFullIndexer indexWriter) throws IOException,InterruptedException;

	public String getSupportedTypeName();
	
	/**
	 * Check access for certain business-control (resourceUrl) and user with roles.
	 * @param contextEntry
	 * @param businessControl
	 * @param identity
	 * @param roles
	 * @return
	 */
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles);
}
