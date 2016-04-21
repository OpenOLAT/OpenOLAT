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
package org.olat.modules.video.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.model.VideoQualityVersion;
import org.olat.modules.video.ui.VideoQualityTableModel.QualityTableCols;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * table to show the different available qualityversions of a video ressource 
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoQualityTableFormController extends FormBasicController {

	private FlexiTableElement tableEl;
	private VideoQualityTableModel tableModel;
	private FormLink viewButton;
	private CloseableModalController cmc;

	@Autowired
	private VideoManager videoManager;
	private OLATResource videoResource;
	private RepositoryEntry videoEntry;


	public VideoQualityTableFormController(UserRequest ureq, WindowControl wControl, RepositoryEntry videoEntry) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.videoResource = videoEntry.getOlatResource();
		this.videoEntry = videoEntry;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer generalCont = FormLayoutContainer.createVerticalFormLayout("general", getTranslator());
		generalCont.setFormTitle(translate("tab.video.qualityConfig"));
		generalCont.setRootForm(mainForm);
		generalCont.setFormContextHelp("Video Tracks");
		formLayout.add(generalCont);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, QualityTableCols.type.i18nKey(), QualityTableCols.type.ordinal(), true, QualityTableCols.type.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, QualityTableCols.dimension.i18nKey(), QualityTableCols.dimension.ordinal(), true, QualityTableCols.dimension.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, QualityTableCols.size.i18nKey(), QualityTableCols.size.ordinal(), true, QualityTableCols.size.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, QualityTableCols.format.i18nKey(), QualityTableCols.format.ordinal(), true, QualityTableCols.format.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, QualityTableCols.view.i18nKey(), QualityTableCols.view.ordinal(), true, QualityTableCols.view.name()));
		tableModel = new VideoQualityTableModel(columnsModel, getTranslator());

		List<QualityTableRow> rows = new ArrayList<QualityTableRow>();
		Size origSize = videoManager.getVideoSize(videoResource);
		
		viewButton = uifactory.addFormLink("view", "viewQuality", "quality.view", "qulaity.view", null, Link.LINK);
		rows.add(new QualityTableRow("original", origSize.getWidth() +"x"+ origSize.getHeight(),  FileUtils.byteCountToDisplaySize(videoManager.getVideoFile(videoResource).length()), "mp4",viewButton));
		
		List<VideoQualityVersion> versions = videoManager.getQualityVersions(videoResource);
		for(VideoQualityVersion version:versions){
			viewButton = uifactory.addFormLink(version.getType(), "viewQuality", "quality.view", "qulaity.view", null, Link.LINK);
			rows.add(new QualityTableRow(version.getType(), version.getDimension().getWidth() +"x"+ version.getDimension().getHeight(),  version.getFileSize(), version.getFormat(),viewButton));
		}
		
		tableModel.setObjects(rows);
		tableEl = uifactory.addTableElement(getWindowControl(), "qualities", tableModel, getTranslator(), generalCont);
		tableEl.setCustomizeColumns(false);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == viewButton){
			
			VideoDisplayController videoDispController = new VideoDisplayController(ureq, getWindowControl(), videoEntry, true);
			cmc = new CloseableModalController(getWindowControl(), "close", videoDispController.getInitialComponent());
			cmc.activate();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

}
