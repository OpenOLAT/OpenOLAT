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
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;

/**
 * 
 * Initial date: 20 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DocumentDirectoryController extends BasicController implements Activateable2  {
	
	private final VelocityContainer mainVC;
	private FolderRunController folderCtrl;
	
	public DocumentDirectoryController(UserRequest ureq, WindowControl wControl,
			VFSContainer documents, String name) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("document_directory");
		mainVC.contextPut("iconCssClass", "o_icon_taxonomy_templates");
		mainVC.contextPut("displayName", name);

		String rootName = translate("document.pool.templates");
		VFSContainer namedContainer = new NamedContainerImpl(rootName, documents);
		folderCtrl = new FolderRunController(namedContainer, true, true, true, ureq, getWindowControl());
		mainVC.put("folder", folderCtrl.getInitialComponent());

		putInitialPanel(mainVC);
	}
	
	public void setAdditionalResourceURL(String additionalUrl) {
		folderCtrl.setResourceURL("[DocumentPool:0]" + additionalUrl);
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
