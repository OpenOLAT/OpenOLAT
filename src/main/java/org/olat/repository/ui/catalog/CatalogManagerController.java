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
package org.olat.repository.ui.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.CatalogEntry;
import org.olat.repository.manager.CatalogManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogManagerController extends BasicController implements Activateable2 {
	
	private final TooledStackedPanel toolbarPanel;
	private CatalogNodeManagerController catalogCtrl;
	
	@Autowired
	private CatalogManager catalogManager;
	
	public CatalogManagerController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		toolbarPanel = new TooledStackedPanel("categoriesStackPanel", getTranslator(), this);
		toolbarPanel.setInvisibleCrumb(0); // show root level
		toolbarPanel.setShowCloseLink(false, false);
		putInitialPanel(toolbarPanel);

		List<CatalogEntry> rootNodes = catalogManager.getRootCatalogEntries();
		if(rootNodes.size() == 1) {
			CatalogEntry root = rootNodes.get(0);
			catalogCtrl = new CatalogNodeManagerController(ureq, getWindowControl(), getWindowControl(), root, toolbarPanel, false);
			listenTo(catalogCtrl);
			toolbarPanel.pushController(root.getShortTitle(), catalogCtrl);
			catalogCtrl.initToolbar();
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if("CatalogEntry".equalsIgnoreCase(type)) {
			Long entryKey = entry.getOLATResourceable().getResourceableId();
			if(entryKey != null && entryKey.longValue() > 0) {
				List<ContextEntry> parentLine = new ArrayList<>();
				for(CatalogEntry node = catalogManager.getCatalogEntryByKey(entryKey); node != null && node.getParent() != null; node=node.getParent()) {
					OLATResourceable nodeRes = OresHelper.createOLATResourceableInstance("Node", node.getKey());
					ContextEntry ctxEntry = BusinessControlFactory.getInstance().createContextEntry(nodeRes);
					ctxEntry.setTransientState(new CatalogStateEntry(node));
					parentLine.add(ctxEntry);
				}
				Collections.reverse(parentLine);
				toolbarPanel.popUpToRootController(ureq);
				catalogCtrl.activate(ureq, parentLine, null);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
