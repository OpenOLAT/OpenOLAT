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

package org.olat.search.service.indexer;

import java.io.IOException;

import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.search.service.SearchResourceContext;

/**
 * Common interface for all indexers. Used to go over the whole data-structre during indexing. 
 * Used to check access-control for result-list. 
 * 
 * @author Christian Guretzki
 */
public interface Indexer extends IndexerAccessSecurityCallback {

	/**
	 * Do index the part of an indexer.
	 * @param searchResourceContext Current search-resource-context is used to build resouceUrl of an index-element.
	 * @param indexerObject         Index parent object. Each indexer must cast to the known type e.g. BusinessGroup.
	 * @param indexer               Callback object to add documents to the index.
	 * @throws IOException          Can not write index file.
	 * @throws InterruptedException Indexing is interrupted
	 */
	void doIndex(SearchResourceContext searchResourceContext, Object parentObject, OlatFullIndexer indexer) throws IOException,InterruptedException;
	
	/**
	 * Check access for certain business-control (resourceUrl) and user with roles.
	 * @param contextEntry
	 * @param businessControl
	 * @param identity
	 * @param roles
	 * @return
	 */
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles);
	
	/**
	 * @return The indexer supports this type name. E.g. ORES_TOOLFOLDER.getResourceableTypeName()
	 */
	public String getSupportedTypeName();
}
