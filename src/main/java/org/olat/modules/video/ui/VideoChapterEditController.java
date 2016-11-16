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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.ui.VideoChapterTableModel.ChapterTableCols;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Class VideoChapterEditController. 
 * Initial Date: 02.11.2016
 * @autor fkiefer fabian.kiefer@frentix.com
 * this class displays a VideoDisplayController and a table of chapters
 * chapters are saved in a textfile at the same location as the video resource
 */
public class VideoChapterEditController extends FormBasicController {
	
	private RepositoryEntry entry;
	private VelocityContainer mainVC;
	private VideoChapterTableModel tableModel;
	private VideoDisplayController videoDisplayCtr;
	private FlexiTableElement chapterTable;
	
	private static final SimpleDateFormat displayDateFormat = new SimpleDateFormat("HH:mm:ss");

	private List<VideoChapterTableRow> chapters;

	@Autowired
	private VideoManager videoManager;
	
	private CloseableModalController cmc;
	private ChapterEditController chapterEditCtr;
	
	private String currentTimeCode;
	private String duration;
	private FormLink addChapterEl;
	
	public VideoChapterEditController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, "video_chapter");
		displayDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		chapters = new ArrayList<>();
		this.entry = entry;
		initForm(ureq);
		videoManager.loadChapters(chapters, entry.getOlatResource());
		loadTableModel();
	}

	@Override
	protected void doDispose() {

	}


	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		videoDisplayCtr = new VideoDisplayController(ureq, getWindowControl(), entry, false, false, false, false, null, false, false, null);
		listenTo(videoDisplayCtr);	
		videoDisplayCtr.reloadVideo(ureq);
		
		FlexiTableColumnModel chapterTableModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		chapterTableModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChapterTableCols.chapterName));
		chapterTableModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChapterTableCols.intervals));
		chapterTableModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
		chapterTableModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		tableModel = new VideoChapterTableModel(chapterTableModel, getTranslator());

		chapterTable = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(), formLayout);
		chapterTable.setCustomizeColumns(false);
		chapterTable.setNumOfRowsEnabled(false);
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonGroupLayout);
		addChapterEl = uifactory.addFormLink("video.chapter.add", buttonGroupLayout, Link.BUTTON);
		
		mainVC = ((FormLayoutContainer) formLayout).getFormItemComponent();
		mainVC.put("video", videoDisplayCtr.getInitialComponent());
	}

	private void loadTableModel() {	
		if (chapters != null)tableModel.setObjects(chapters);
		chapterTable.reset(true, true, true);		
	}
	
	private void doDelete (UserRequest ureq, Object toRemove) {				
		if (chapters != null){
			chapters.remove(toRemove);	
			organizeChapters();
			tableModel.setObjects(chapters);
		}
		chapterTable.reset(true, true, true);	
		saveChapters(ureq);
	}
	
	@Override
	public void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {

		if(source == chapterTable) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				se.getIndex();
				Object currentObject = tableModel.getObject(se.getIndex());
				if ("delete".equals(se.getCommand())){
					doDelete(ureq, currentObject);					
				} else if ("edit".equals(se.getCommand())){
					VideoChapterTableRow vctr = chapters.get(chapters.indexOf(currentObject));
					doOpenCallout(ureq, vctr, true);
				} 
			}
			
		} else if (addChapterEl == source) {
			long currentLong = currentTimeCode != null ? (long) Float.parseFloat(currentTimeCode) * 1000 : 0;
			Date currentDate = new Date(currentLong);
			VideoChapterTableRow vctr = new VideoChapterTableRow(translate("video.chapter.new"), 
					displayDateFormat.format(currentDate), currentDate, currentDate);
			doOpenCallout(ureq, vctr, false);
		}
		
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		super.event(ureq, source, event);
	}
	

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == videoDisplayCtr) {
			if (event instanceof VideoEvent) {
				VideoEvent videoEvent = (VideoEvent) event;
				String timeCode = videoEvent.getTimeCode();
				currentTimeCode = timeCode;
				duration = videoEvent.getDuration();
			}
		} else if (source == chapterEditCtr){
			if (event == Event.DONE_EVENT) {
				doAddOrUpdateChapter(ureq, chapterEditCtr.getVideoChapterTableRow());
				videoDisplayCtr.reloadVideo(ureq);
			} 
			cmc.deactivate();
			cleanUpCMC();
		} else if (source == cmc) {
			cleanUpCMC();
		} 
		super.event(ureq, source, event);
	}
	
	private void cleanUpCMC(){
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(chapterEditCtr);
		cmc = null;
		chapterEditCtr = null;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		// only formInnerEvent()
	}
	
	private void saveChapters(UserRequest ureq) {
		videoManager.saveChapters(chapters, entry.getOlatResource());
		videoDisplayCtr.reloadVideo(ureq);
	}; 


	private void doOpenCallout(UserRequest ureq, VideoChapterTableRow videoChapterTableRow, boolean chapterExists) {
		
		chapterEditCtr = new ChapterEditController(ureq, getWindowControl(), videoChapterTableRow, 
				chapterExists, chapters, duration); 
		listenTo(chapterEditCtr);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), chapterEditCtr.getInitialComponent(), 
				true, translate("video.chapter." + (chapterExists ? "edit" : "new")));
		listenTo(cmc);
		
		cmc.activate();
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//avoid reload of html template, to prevent videoreload
	}
	
	private void doAddOrUpdateChapter(UserRequest ureq, VideoChapterTableRow row){
		//only add if object does not yet exist
		if (!chapters.contains(row)){
			chapters.add(row);
		}
		//sort chapters by begin time
		Collections.sort(chapters, new Comparator<VideoChapterTableRow>(){
			@Override
			public int compare(VideoChapterTableRow o1, VideoChapterTableRow o2) {
				return o1.getBegin().compareTo(o2.getBegin());
			}			
		});		
		organizeChapters();
		loadTableModel();
		saveChapters(ureq);
	}
	
	private void organizeChapters () {
		//set end of previous chapter to beginning of the following				
		for (int i = 1; i <= chapters.size(); i++) {
			VideoChapterTableRow previousChapter = chapters.get(i-1);
			if (i != chapters.size()) {
				VideoChapterTableRow currentChapter = chapters.get(i);
				previousChapter.setEnd(currentChapter.getBegin());				
			} else {
				long durationLong = duration != null ? (long) Float.parseFloat(duration) * 1000 : 0;
				// duration may hold no value, backup ask videoManager
				if (durationLong == 0){
					Date endOfMovie = new Date(videoManager.getVideoDuration(entry.getOlatResource()));
					previousChapter.setEnd(endOfMovie);
				}else{
					Date durationDate = new Date(durationLong);
					previousChapter.setEnd(durationDate);					
				}
				
			}
		}
	}

}
