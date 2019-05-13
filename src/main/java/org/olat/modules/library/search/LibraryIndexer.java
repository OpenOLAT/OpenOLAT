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
package org.olat.modules.library.search;

import java.io.IOException;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.library.LibraryManager;
import org.olat.modules.library.site.LibrarySite;
import org.olat.modules.library.ui.LibraryMainController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.indexer.FolderIndexer;
import org.olat.search.service.indexer.FolderIndexerAccess;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * 
 * <h3>Description:</h3> Index the library as a folder (same as shared folder
 * indexer)
 * <p>
 * Initial Date: 18.08.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */

public class LibraryIndexer extends FolderIndexer {

	private static final Logger log = Tracing.createLoggerFor(LibraryIndexer.class);
	private static final OLATResourceable TYPE = OresHelper.createOLATResourceableTypeWithoutCheck(LibrarySite.class.getSimpleName());
	private boolean restrictAccessToOwnerGroup = false;
	private String restrictAccessToRole = null;

	@Override
	public String getSupportedTypeName() {
		return TYPE.getResourceableTypeName();
	}

	@Override
	public void doIndex(SearchResourceContext parentResourceContext, Object parentObject, OlatFullIndexer indexWriter) throws IOException,
			InterruptedException {

		VFSContainer container = CoreSpringFactory.getImpl(LibraryManager.class).getSharedFolder();
		if (container == null) return;
		if (log.isDebugEnabled()) log.debug("Index Library Folder...");

		SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
		searchResourceContext.setParentContextType(TYPE.getResourceableTypeName());
		Translator trans = Util.createPackageTranslator(LibraryMainController.class, I18nModule.getDefaultLocale());
		searchResourceContext.setParentContextName(trans.translate("library.title"));

		searchResourceContext.setBusinessControlFor(TYPE); // to match the list of
		// indexer
		doIndexVFSContainer(searchResourceContext, container, indexWriter, "", FolderIndexerAccess.FULL_ACCESS);
	}

	@Override
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		boolean ok;
		if (restrictAccessToOwnerGroup) {
			RepositoryEntry re = CoreSpringFactory.getImpl(LibraryManager.class).getCatalogRepoEntry();
			if (CoreSpringFactory.getImpl(RepositoryService.class).hasRole(identity, re, GroupRoles.owner.name())) {
				ok = true;
			} else {
				ok = false;
			}
		} else if (restrictAccessToRole != null) {
			if (restrictAccessToRole.equals("administrator") && roles.isAdministrator()) {
				ok = true;
			} else if (restrictAccessToRole.equals("groupmanager") && roles.isGroupManager()) {
				ok = true;
			} else if (restrictAccessToRole.equals("usermanager") && roles.isUserManager()) {
				ok = true;
			} else if (restrictAccessToRole.equals("author") && roles.isAuthor()) {
				ok = true;
			} else {
				ok = false;
			}
		} else {
			ok = true;
		}
		return ok && super.checkAccess(contextEntry, businessControl, identity, roles);
	}

	// [used by spring] see serviceconfig/search/
	public void setRestrictAccessToOwnerGroup(boolean restrict) {
		this.restrictAccessToOwnerGroup = restrict;
	}

	// [used by spring] see serviceconfig/search/
	public void setRestrictAccessToRole(String role) {
		this.restrictAccessToRole = role.toLowerCase();
	}

}
