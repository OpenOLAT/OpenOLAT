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
package org.olat.modules.webFeed.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.fileresource.FileResourceManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoryEntrySettingsController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  The settings add quota management.
 *  
 * Initial date: 30 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FeedSettingsController extends RepositoryEntrySettingsController {
	
	private Link quotaLink;
	
	private Controller quotaCtrl;
	
	@Autowired
	private QuotaManager quotaManager;

	public FeedSettingsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, RepositoryEntry entry) {
		super(ureq, wControl, stackPanel, entry);
	}
	
	@Override
	protected void initOptions() {
		super.initOptions();
		if (quotaManager.hasQuotaEditRights(getIdentity(), roles, getOrganisations())) {
			quotaLink = LinkFactory.createToolLink("quota", translate("tab.quota.edit"), this);
			quotaLink.setElementCssClass("o_sel_repo_quota");
			buttonsGroup.addButton(quotaLink, false);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		super.activate(ureq, entries, state);
		
		if(entries != null && !entries.isEmpty()) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Quota".equalsIgnoreCase(type)) {
				doOpenQuota(ureq);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(quotaCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				doOpenQuota(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(quotaLink == source) {
			doOpenQuota(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();
		removeAsListenerAndDispose(quotaCtrl);
		quotaCtrl = null;
	}

	private void doOpenQuota(UserRequest ureq) {
		if (quotaManager.hasQuotaEditRights(ureq.getIdentity(), roles, getOrganisations())) {
			VFSContainer feedRoot = FileResourceManager.getInstance().getFileResourceRootImpl(entry.getOlatResource());
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Quota"), null);
			if(readOnly) {
				quotaCtrl = quotaManager.getQuotaViewInstance(ureq, swControl, feedRoot.getRelPath());
			} else {
				quotaCtrl = quotaManager.getQuotaEditorInstance(ureq, addToHistory(ureq, swControl), feedRoot.getRelPath(), true, false);
			}
			mainPanel.setContent(quotaCtrl.getInitialComponent());
			buttonsGroup.setSelectedButton(quotaLink);
		}
	}
}
