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
package org.olat.modules.taxonomy.ui;

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
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.taxonomy.Taxonomy;

/**
 * 
 * Initial date: 20 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyDirectoryController extends BasicController implements Activateable2  {
	
	private final VelocityContainer mainVC;
	private FolderRunController folderCtrl;

	
	public TaxonomyDirectoryController(UserRequest ureq, WindowControl wControl,
			Taxonomy taxonomy, VFSContainer documents, String name) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("taxonomy_directory");
		mainVC.contextPut("iconCssClass", "o_icon_taxonomy_templates");
		mainVC.contextPut("displayName", name);

		VFSContainer namedContainer = new NamedContainerImpl("Templates", documents);
		folderCtrl = new FolderRunController(namedContainer, true, true, true, ureq, getWindowControl());
		folderCtrl.setResourceURL("[Taxonomy:" + taxonomy.getKey() + "]");
		mainVC.put("folder", folderCtrl.getInitialComponent());

		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
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
