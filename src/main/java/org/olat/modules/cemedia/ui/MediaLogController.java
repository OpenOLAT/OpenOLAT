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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.ui.MediaLogTableModel.MediaLogCols;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaLogController extends FormBasicController {
	
	private MediaLogTableModel model;
	private FlexiTableElement tableEl;
	
	private Media media;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private MediaService mediaService;
	
	public MediaLogController(UserRequest ureq, WindowControl wControl, Form mainForm, Media media) {
		super(ureq, wControl, LAYOUT_CUSTOM, "media_logs", mainForm);
		this.media = media;
		initForm(ureq);
	}
	
	public int size() {
		return model == null ? 0 : model.getRowCount();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaLogCols.author));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaLogCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaLogCols.comment));
		
		model = new MediaLogTableModel(columnsModel, getLocale());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 25, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
	}
	
	protected void loadModel() {
		List<MediaLog> logs = mediaService.getMediaLogs(media);
		List<MediaLogRow> rows = new ArrayList<>(logs.size());
		for(MediaLog mLog:logs) {
			String fullName = mLog.getIdentity() == null
					? null : userManager.getUserDisplayName(mLog.getIdentity());
			String action = translate("log.action." + mLog.getAction().name().toLowerCase());
			rows.add(new MediaLogRow(mLog, fullName, action));
		}
		model.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
