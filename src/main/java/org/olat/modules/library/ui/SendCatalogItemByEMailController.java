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
package org.olat.modules.library.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.modules.bc.meta.MetaInfoController;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ui.SendDocumentsByEMailController;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.library.LibraryManager;
import org.olat.modules.library.model.CatalogItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * <p>
 * Initial Date:  7 feb. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class SendCatalogItemByEMailController extends SendDocumentsByEMailController {
	
	private final CatalogItem catalogItem;
	
	@Autowired
	private LibraryManager libraryManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public SendCatalogItemByEMailController(UserRequest ureq, WindowControl wControl, CatalogItem catalogItem) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MetaInfoController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(SendDocumentsByEMailController.class, getLocale(), getTranslator()));
		this.catalogItem = catalogItem;
		
		VFSContainer container = libraryManager.getSharedFolder();
		List<VFSLeaf> leafs = new ArrayList<>();
		leafs.add((VFSLeaf)vfsRepositoryService.getItemFor(catalogItem.getMetaInfo()));
		setFiles(container, leafs);
	}
	
	@Override
	protected void appendBusinessPath(VFSContainer rootContainer, VFSLeaf file, StringBuilder sb) {
		String uri = catalogItem.getAbsolutePathUUID();
		appendMetadata("mf.url", uri, sb);
	}
}