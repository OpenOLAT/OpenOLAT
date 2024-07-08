/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.sharepoint.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.OAuth2Tokens;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.sharepoint.manager.SharePointDAO;
import org.olat.modules.sharepoint.model.MicrosoftDrive;
import org.olat.modules.sharepoint.model.MicrosoftSite;
import org.olat.modules.sharepoint.model.SiteAndDriveConfiguration;
import org.olat.modules.sharepoint.ui.SitesAndDrivesTreeTableModel.SitesAndDrivesCols;
import org.olat.modules.teams.manager.MicrosoftGraphDAO;
import org.springframework.beans.factory.annotation.Autowired;

import com.azure.core.credential.TokenCredential;

/**
 * 
 * Initial date: 8 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharePointSearchSitesAndDrivesController extends FormBasicController {

	private FlexiTableElement tableEl;
	private FormLink addSitesAndDrivesButton;
	private SitesAndDrivesTreeTableModel tableModel;
	
	@Autowired
	private SharePointDAO sharePointDao;
	@Autowired
	private MicrosoftGraphDAO microsoftGraphDao;
	
	public SharePointSearchSitesAndDrivesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "sites_drives_chooser");
		
		initForm(ureq);
	}
	
	public List<SiteAndDriveConfiguration> getSelectedSiteAndDrives() {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<SiteAndDriveConfiguration> configurationList = new ArrayList<>();
		for(Integer selectedIndex:selectedIndexes) {
			SiteAndDriveRow row = tableModel.getObject(selectedIndex.intValue());
			SiteAndDriveConfiguration config = row.toConfiguration();
			configurationList.add(config);
		}
		return configurationList;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		FlexiCellRenderer nodeRenderer = new TreeNodeFlexiCellRenderer(new SiteAndDriveNameRenderer());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SitesAndDrivesCols.name, nodeRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SitesAndDrivesCols.id));
		
		tableModel = new SitesAndDrivesTreeTableModel(columnsModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "points", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		
		addSitesAndDrivesButton = uifactory.addFormLink("add.sites.and.drives", formLayout, Link.BUTTON);
	}
	
	private void loadModel(UserRequest ureq) {
		String searchString = tableEl.getQuickSearchString();
		
		OAuth2Tokens tokens = ureq.getUserSession().getOAuth2Tokens();
		TokenCredential tokenProvider = microsoftGraphDao.getTokenProvider(tokens);
		List<MicrosoftSite> sites = sharePointDao.getSites(tokenProvider, searchString);
		
		List<SiteAndDriveRow> rows = new ArrayList<>();
		for(MicrosoftSite site:sites) {
			SiteAndDriveRow siteRow = new SiteAndDriveRow(site);
			rows.add(siteRow);
			getLogger().debug("Site: {} ({})", site.name(), site.id());

			List<MicrosoftDrive> drives = sharePointDao.getDrives(site.id(), tokenProvider);
			for(MicrosoftDrive drive:drives) {
				SiteAndDriveRow driveRow = new SiteAndDriveRow(site, drive);
				driveRow.setParent(siteRow);
				rows.add(driveRow);
				getLogger().debug("Drive: {} ({})", drive.name(), drive.id());
			}
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addSitesAndDrivesButton == source) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		loadModel(ureq);
	}
}
