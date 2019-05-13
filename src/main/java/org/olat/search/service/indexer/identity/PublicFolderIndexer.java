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
package org.olat.search.service.indexer.identity;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.indexer.FolderIndexer;
import org.olat.search.service.indexer.FolderIndexerAccess;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * <h3>Description:</h3>
 * <p>
 * The identity indexer indexes the users public folder
 * <p>
 * Initial Date: 21.08.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class PublicFolderIndexer extends FolderIndexer {

	private static final Logger log = Tracing.createLoggerFor(PublicFolderIndexer.class);
	
	public static final String TYPE = "type.identity.publicfolder";
	public static final OLATResourceable BUSINESS_CONTROL_TYPE = OresHelper.createOLATResourceableTypeWithoutCheck("userfolder");

	@Override
	public String getSupportedTypeName() {
		return Identity.class.getSimpleName();
	}

	@Override
	public void doIndex(SearchResourceContext parentResourceContext, Object parentObject, OlatFullIndexer indexWriter) {
		try {
			// get public folder for user
			Identity identity = (Identity) parentObject;
			VFSContainer rootContainer = VFSManager.olatRootContainer(FolderConfig.getUserHome(identity.getName()) + "/public", null);
			if (!rootContainer.exists()) return;
			// build new resource context
			SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
			searchResourceContext.setParentContextName(identity.getName());
			searchResourceContext.setBusinessControlFor(BUSINESS_CONTROL_TYPE);
			searchResourceContext.setDocumentType(TYPE);
			// now index the folder
			doIndexVFSContainer(searchResourceContext, rootContainer, indexWriter, "", FolderIndexerAccess.FULL_ACCESS);
		} catch (Exception ex) {
			log.warn("Exception while indexing public folder of identity::" + parentObject.toString() + ". Skipping this user, try next one.",
							ex);
		}
		if (log.isDebugEnabled()) log.debug("PublicFolder finished for user::" + parentObject.toString());
	}
}
