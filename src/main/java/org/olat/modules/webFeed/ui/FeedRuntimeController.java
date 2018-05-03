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

import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.Spacer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.fileresource.FileResourceManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * The runtime add quoty management and delivery options.
 * 
 * Initial date: 15.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FeedRuntimeController extends RepositoryEntryRuntimeController {
	
	private Link quotaLink;

	@Autowired
	private QuotaManager quotaManager;
	
	public FeedRuntimeController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry re, RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);
	}

	@Override
	protected void initSettingsTools(Dropdown settingsDropdown) {
		super.initSettingsTools(settingsDropdown);
		if (reSecurity.isEntryAdmin()
				&& quotaManager.hasQuotaEditRights(getIdentity(), roles, getOrganisations())) {
			settingsDropdown.addComponent(new Spacer(""));
			quotaLink = LinkFactory.createToolLink("quota", translate("tab.quota.edit"), this, "o_sel_repo_quota");
			quotaLink.setIconLeftCSS("o_icon o_icon-fw o_icon_quota");
			settingsDropdown.addComponent(quotaLink);
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		entries = removeRepositoryEntry(entries);
		if(entries != null && !entries.isEmpty()) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Quota".equalsIgnoreCase(type)) {
				doQuota(ureq);
			}
		}
		super.activate(ureq, entries, state);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(quotaLink == source) {
			doQuota(ureq);
		} else {
			super.event(ureq, source, event);
		}
	}
	
	private void doQuota(UserRequest ureq) {
		if (quotaManager.hasQuotaEditRights(ureq.getIdentity(), roles, getOrganisations())) {
			RepositoryEntry entry = getRepositoryEntry();
			OlatRootFolderImpl feedRoot = FileResourceManager.getInstance().getFileResourceRootImpl(entry.getOlatResource());
			WindowControl bwControl = getSubWindowControl("Quota");
			Controller quotaCtrl = quotaManager.getQuotaEditorInstance(ureq, addToHistory(ureq, bwControl), feedRoot.getRelPath(), getOrganisations());
			pushController(ureq, translate("tab.quota.edit"), quotaCtrl);
			setActiveTool(quotaLink);
		}
	}
}