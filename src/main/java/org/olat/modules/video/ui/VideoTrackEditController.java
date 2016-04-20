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
import java.util.HashMap;
import java.util.Map;

import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
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
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.video.manager.VideoManager;
import org.olat.modules.video.models.VideoTracksTableModel;
import org.olat.modules.video.models.VideoTracksTableModel.TrackTableCols;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * 
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoTrackEditController extends FormBasicController {
	protected FormUIFactory uifactory = FormUIFactory.getInstance();

	private FlexiTableElement tableEl;
	private VideoTracksTableModel tableModel;
	private FormLink addButton;
	VideoTrackUploadForm trackUploadForm;
	VideoPosterUploadForm posterUploadForm;
	VideoPosterSelectionForm posterSelectionForm;
	CloseableModalController cmc;

	private Map<String, TrackTableRow> rows;

	@Autowired
	private VideoManager videoManager;
	private OLATResource videoResource;

	public VideoTrackEditController(UserRequest ureq, WindowControl wControl, OLATResource videoResource) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.videoResource = videoResource;
		initForm(ureq);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer generalCont = FormLayoutContainer.createVerticalFormLayout("general", getTranslator());
		generalCont.setFormTitle(translate("tab.video.trackConfig"));
		generalCont.setRootForm(mainForm);
		generalCont.setFormContextHelp("Video Tracks");
		formLayout.add(generalCont);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, TrackTableCols.file.i18nKey(), TrackTableCols.file.ordinal(), true, TrackTableCols.file.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TrackTableCols.language.i18nKey(), TrackTableCols.language.ordinal(), true, TrackTableCols.language.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, TrackTableCols.delete.i18nKey(), TrackTableCols.delete.ordinal(),false, TrackTableCols.delete.name()));
		tableModel = new VideoTracksTableModel(columnsModel, getTranslator());

		tableEl = uifactory.addTableElement(getWindowControl(), "tracks", tableModel, getTranslator(), generalCont);
		tableEl.setCustomizeColumns(false);
		HashMap<String, VFSLeaf> tracks = videoManager.getAllTracks(videoResource);
		rows = new HashMap<String,TrackTableRow>(tracks.size());
		if (!tracks.isEmpty()) {
			for (Map.Entry<String, VFSLeaf> entry : tracks.entrySet()) {
				FormLink delButton = uifactory.addFormLink(entry.getKey(), "deleteTrack", "track.delete", "track.delete", null, Link.BUTTON);
				rows.put(entry.getKey(), new TrackTableRow(entry.getKey(), entry.getValue(), delButton));

			}
			tableModel.setObjects(new ArrayList<TrackTableRow>(rows.values()));
		}
//		tableEl.setVisible(!videoManager.getAllTracks(videoResource).isEmpty());
		tableEl.setVisible(true);

		addButton = uifactory.addFormLink("add.track", generalCont, Link.BUTTON);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {

		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {

	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (addButton == source) {
			doAddTrack(ureq);
		}else if (source.getComponent() instanceof Link){
			String lang = rows.get(source.getName()).getLanguage();
			videoManager.removeTrack(videoResource, lang);
			rows.remove(rows.get(source.getName()).getLanguage());
			tableModel.setObjects(new ArrayList<TrackTableRow>(rows.values()));
			tableEl.reset();
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == trackUploadForm){
			videoManager.addTrack(videoResource, trackUploadForm.getLang(),(VFSLeaf) ((FolderEvent) event).getItem());
			rows.put(trackUploadForm.getLang(), new TrackTableRow(trackUploadForm.getLang(), (VFSLeaf) ((FolderEvent) event).getItem(), uifactory.addFormLink(trackUploadForm.getLang(),"deleteTrack", "track.delete", "track.delete", null, Link.BUTTON)));
			tableModel.setObjects(new ArrayList<TrackTableRow>(rows.values()));
			tableEl.reset();
			tableEl.setVisible(true);
			tableEl.setEnabled(true);
			cmc.deactivate();
		}
		else if(event.getCommand() == "CLOSE_MODAL_EVENT"){
			cmc.deactivate();
		}
	}


	private void doAddTrack(UserRequest ureq) {
		trackUploadForm = new VideoTrackUploadForm(ureq, getWindowControl(), videoResource);
		listenTo(trackUploadForm);
		cmc = new CloseableModalController(getWindowControl(), "close", trackUploadForm.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}

}
