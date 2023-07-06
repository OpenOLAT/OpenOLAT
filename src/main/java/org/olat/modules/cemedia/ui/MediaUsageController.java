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
package org.olat.modules.cemedia.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.model.MediaUsageWithStatus;
import org.olat.modules.cemedia.ui.MediaUsageTableModel.MediaUsageCols;
import org.olat.modules.cemedia.ui.component.MediaResourceCellRenderer;
import org.olat.modules.cemedia.ui.component.MediaStatusCellRenderer;
import org.olat.modules.cemedia.ui.component.MediaUseCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaUsageController extends FormBasicController {

	private FlexiTableElement tableEl;
	private MediaUsageTableModel model;
	
	private Media media;

	@Autowired
	private MediaService mediaService;
	
	public MediaUsageController(UserRequest ureq, WindowControl wControl, Media media) {
		super(ureq, wControl, "media_usage");
		this.media = media;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaUsageCols.use, "select-page",
				new MediaUseCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaUsageCols.resource, "select-resource",
				new MediaResourceCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaUsageCols.usedBy));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaUsageCols.version));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaUsageCols.status,
				new MediaStatusCellRenderer(getTranslator())));
	
		model = new MediaUsageTableModel(columnsModel, getTranslator(), getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 25, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmptyTableMessageKey("table.sEmptyTable");
	}
	
	public void reload() {
		loadModel();
	}
	
	private void loadModel() {
		List<MediaUsageWithStatus> mediaUsageList = mediaService.getMediaUsageWithStatus(media);
		List<MediaUsageRow> rows = new ArrayList<>();
		for(MediaUsageWithStatus usedIn:mediaUsageList) {
			rows.add(MediaUsageRow.valueOf(usedIn));
		}
		
		model.setObjects(rows);
		tableEl.reset(true, true, true);
	}	

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				MediaUsageRow row = model.getObject(se.getIndex());
				if("select-page".equals(cmd)) {
					MediaUIHelper.open(ureq, getWindowControl(), row.getBinderKey(), row.getPageKey(),
							row.getRepositoryEntryKey(), row.getSubIdent());
				} else if("select-resource".equals(cmd) && row.getRepositoryEntryKey() != null) {
					MediaUIHelper.open(ureq, getWindowControl(), null, null,
							row.getRepositoryEntryKey(), row.getSubIdent());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
