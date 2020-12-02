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
package org.olat.repository.handlers;

import java.io.File;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.ui.DocEditorStandaloneOpenController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.fileresource.FileResourceManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

/**
 * 
 * Initial date: 25 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocumentEditorDelegate implements WebDocumentCreateDelegate, WebDocumentEditDelegate {

	private DocumentEditorDelegateType type;

	public DocumentEditorDelegate(DocumentEditorDelegateType type) {
		this.type = type;
	}
	
	@Override
	public String getCreateLabelI18nKey() {
		return type.getCreateLabelI18nKey();
	}
	
	@Override
	public boolean supportCreate(Identity identity, Roles roles) {
		return canEdit(identity, roles);
	}

	private boolean canEdit(Identity identity, Roles roles) {
		DocEditorService docEditorService = CoreSpringFactory.getImpl(DocEditorService.class);
		return docEditorService.hasEditor(identity, roles, type.getSuffix(), Mode.EDIT, true, false);
	}
	
	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description,
			Object createObject, Organisation organisation, Locale locale) {
		OLATResource resource = OLATResourceManager.getInstance().createAndPersistOLATResourceInstance(type.getOLATResourceable());
		File fResourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(resource).getBasefile();
		String filename = FileUtils.cleanFilename(displayname.toLowerCase()) + "." + type.getSuffix();
		File target = new File(fResourceFileroot, filename);
		
		VFSLeaf vfsLeaf = new LocalFileImpl(target);
		VFSManager.copyContent(type.getContent(locale), vfsLeaf, initialAuthor);

		RepositoryEntry re = CoreSpringFactory.getImpl(RepositoryService.class).create(initialAuthor, null, "", displayname,
				description, resource, RepositoryEntryStatusEnum.preparation, organisation);
		DBFactory.getInstance().commit();
		return re;
	}
	
	@Override
	public EditionSupport supportsEdit(Identity identity, Roles roles) {
		return canEdit(identity, roles)? EditionSupport.yes: EditionSupport.no;
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl,
			TooledStackedPanel toolbar) {
		OLATResource resource = re.getOlatResource();
		VFSContainer fResourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(resource);
		
		LocalFileImpl document = null;
		for(VFSItem item:fResourceFileroot.getItems(new VFSSystemItemFilter())) {
			if(item instanceof VFSLeaf && item instanceof LocalImpl) {
				document = (LocalFileImpl)item;
			}
		}
		
		if (document == null) {
			throw new AssertException("Web document not found! " + re);
		}
		DocEditorConfigs docEditorConfigs = DocEditorConfigs.builder()
				.withMode(Mode.EDIT)
				.build(document);
		return new DocEditorStandaloneOpenController(ureq, wControl, docEditorConfigs);
	}
	
}
