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
package org.olat.modules.docpool.ui;

import java.util.List;

import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyTreeNode;
import org.olat.modules.taxonomy.ui.component.TaxonomyVFSSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DocumentPoolLevelController extends BasicController implements Activateable2  {
	
	private final VelocityContainer mainVC;
	private FolderRunController folderCtrl;
	
	private TaxonomyLevel taxonomyLevel;
	
	@Autowired
	private TaxonomyService taxonomyService;
	
	public DocumentPoolLevelController(UserRequest ureq, WindowControl wControl,
			TaxonomyLevel level, TaxonomyTreeNode node, TaxonomyVFSSecurityCallback secCallback) {
		super(ureq, wControl);
		taxonomyLevel = taxonomyService.getTaxonomyLevel(level);
		
		if(taxonomyLevel == null) {
			mainVC = createVelocityContainer("deleted");
		} else {
			mainVC = createVelocityContainer("document_pool_level_directory");
		
			String iconCssClass;
			TaxonomyLevelType type = level.getType();
			if(type != null && StringHelper.containsNonWhitespace(type.getCssClass())) {
				iconCssClass = type.getCssClass();
			} else {
				iconCssClass = node.getIconCssClass();
			}
			mainVC.contextPut("iconCssClass", iconCssClass);
			mainVC.contextPut("displayName", StringHelper.escapeHtml(level.getDisplayName()));
			mainVC.contextPut("identifier", StringHelper.escapeHtml(level.getIdentifier()));
			
			if(node.isDocumentsLibraryEnabled() && node.isCanRead()) {
				String name = level.getDisplayName();
				VFSContainer documents = taxonomyService.getDocumentsLibrary(level);
				documents.setLocalSecurityCallback(secCallback);
				VFSContainer namedContainer = new NamedContainerImpl(name, documents);
				folderCtrl = new FolderRunController(namedContainer, true, true, true, ureq, getWindowControl());
				folderCtrl.setResourceURL("[DocumentPool:0][TaxonomyLevel:" + taxonomyLevel.getKey() + "]");
				mainVC.put("folder", folderCtrl.getInitialComponent());
			}
		}
		putInitialPanel(mainVC);
	}
	
	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		if(folderCtrl != null) {
			folderCtrl.activate(ureq, entries, state);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
