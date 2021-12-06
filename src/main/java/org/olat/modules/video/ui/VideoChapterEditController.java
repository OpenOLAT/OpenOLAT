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
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.util.StringHelper;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.ui.VideoChapterTableModel.ChapterTableCols;
import org.olat.modules.video.ui.event.VideoEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Class VideoChapterEditController. 
 * Initial Date: 02.11.2016
 * @autor fkiefer fabian.kiefer@frentix.com
 * this class displays a VideoDisplayController and a table of chapters
 * chapters are saved in a textfile at the same location as the video resource
 */
public class VideoChapterEditController extends BasicController {

	private CloseableModalController cmc;
	private ChapterEditController chapterEditCtr;
	private VideoChaptersController chaptersEditCtrl;
	private VideoDisplayController videoDisplayCtr;
	

	private long durationInSeconds;
	private String currentTimeCode;
	private RepositoryEntry entry;
	private final String videoElementId;
	private final SimpleDateFormat displayDateFormat;

	@Autowired
	private VideoManager videoManager;
	
	public VideoChapterEditController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		this.entry = entry;
		displayDateFormat = new SimpleDateFormat("HH:mm:ss");
		displayDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		VelocityContainer mainVC = createVelocityContainer("video_chapter_editor");
		//video preview
		VideoDisplayOptions displayOptions = VideoDisplayOptions.disabled();
		displayOptions.setAlwaysShowControls(true);
		displayOptions.setAuthorMode(true);
		videoDisplayCtr = new VideoDisplayController(ureq, getWindowControl(), entry, null, null, displayOptions);
		videoDisplayCtr.setTimeUpdateListener(true);
		listenTo(videoDisplayCtr);	
		videoDisplayCtr.reloadVideo(ureq);
		mainVC.put("video", videoDisplayCtr.getInitialComponent());
		videoElementId = videoDisplayCtr.getVideoElementId();
		
		//chapters editor
		chaptersEditCtrl = new VideoChaptersController(ureq, getWindowControl());
		listenTo(chaptersEditCtrl);
		mainVC.put("chapters", chaptersEditCtrl.getInitialComponent());

		durationInSeconds = VideoHelper.durationInSeconds(entry, videoDisplayCtr);
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == videoDisplayCtr) {
			if (event instanceof VideoEvent) {
				VideoEvent videoEvent = (VideoEvent) event;
				currentTimeCode = videoEvent.getTimeCode();
				if(StringHelper.containsNonWhitespace(videoEvent.getDuration()) && !"NaN".equals(videoEvent.getDuration())) {
					try {
						durationInSeconds = Math.round(Double.parseDouble(videoEvent.getDuration()));
					} catch (NumberFormatException e) {
						//don't panic
					}
				}
			}
		}
	}

	private class VideoChaptersController extends FormBasicController {

		private FormLink addChapterEl;
		private FlexiTableElement chapterTable;
		private VideoChapterTableModel tableModel;

		public VideoChaptersController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, "video_chapter");
			initForm(ureq);
			loadTableModel();
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			FlexiTableColumnModel chapterTableModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			chapterTableModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChapterTableCols.chapterName));
			chapterTableModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChapterTableCols.intervals));
			chapterTableModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
			chapterTableModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
			tableModel = new VideoChapterTableModel(chapterTableModel);
	
			chapterTable = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(), formLayout);
			chapterTable.setCustomizeColumns(false);
			chapterTable.setNumOfRowsEnabled(false);
			
			FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add(buttonGroupLayout);
			addChapterEl = uifactory.addFormLink("video.chapter.add", buttonGroupLayout, Link.BUTTON);
		}
	
		private void loadTableModel() {
			List<VideoChapterTableRow> chapters = videoManager.loadChapters(entry.getOlatResource());
			if(chapters == null) {
				tableModel.setObjects(new ArrayList<>());
			} else {
				tableModel.setObjects(chapters);
			}
			chapterTable.reset(true, true, true);		
		}

		@Override
		public void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(source == chapterTable) {
				if(event instanceof SelectionEvent) {
					SelectionEvent se = (SelectionEvent)event;
					se.getIndex();
					VideoChapterTableRow currentObject = tableModel.getObject(se.getIndex());
					if ("delete".equals(se.getCommand())){
						doDelete(currentObject);					
					} else if ("edit".equals(se.getCommand())){
						doEditChapter(ureq, currentObject, true);
					} 
				}
			} else if (addChapterEl == source) {
				long currentLong = currentTimeCode != null ? Math.round(Double.parseDouble(currentTimeCode)) * 1000l : 0l;
				Date currentDate = new Date(currentLong);
				VideoChapterTableRow vctr = new VideoChapterTableRow(translate("video.chapter.new"), 
						displayDateFormat.format(currentDate), currentDate, currentDate);
				doEditChapter(ureq, vctr, false);
			}
		}
		
		@Override
		protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
			//avoid reload of html template, to prevent videoreload
		}
	
		@Override
		public void event(UserRequest ureq, Controller source, Event event) {
			if (source == chapterEditCtr){
				if (event == Event.DONE_EVENT) {
					doAddOrUpdateChapter(ureq, chapterEditCtr.getVideoChapterTableRow());
				} 
				cmc.deactivate();
				cleanUp();
			} else if (source == cmc) {
				cleanUp();
			} 
			super.event(ureq, source, event);
		}
		
		private void cleanUp(){
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(chapterEditCtr);
			cmc = null;
			chapterEditCtr = null;
		}
		
		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
		
		private void doDelete(VideoChapterTableRow toRemove) {
			List<VideoChapterTableRow> chapters = tableModel.getObjects();
			chapters.remove(toRemove);	
			organizeChapters(chapters);
			tableModel.setObjects(chapters);
			chapterTable.reset(true, true, true);	
			saveChapters();
		}
		
		private void saveChapters() {
			List<VideoChapterTableRow> chapters = tableModel.getObjects();
			videoManager.saveChapters(chapters, entry.getOlatResource());
		}
		
		private void moveCurrentTime(Date time) {
			long timeInSeconds = time.getTime() / 1000l;

			String elementId = "o_so_vid" + videoElementId;
			StringBuilder sb = new StringBuilder();
			sb.append("try {\n")
			  .append(" var player = jQuery('#").append(elementId).append("').data('player');\n")
			  .append(" var loaded = jQuery('#").append(elementId).append("').data('playerloaded');\n")
			  .append(" if(loaded) {\n")
			  .append("  player.pause();\n")
			  .append("  player.setCurrentTime(").append(timeInSeconds).append(");\n")
			  .append(" } else {")
			  .append("  var metaListener = function(e) {\n")
			  .append("   player.setCurrentTime(").append(timeInSeconds).append(");\n")
			  .append("   player.pause();\n")
			  .append("   player.media.removeEventListener(metaListener);\n")
			  .append("  };\n")
			  .append("  player.play();")
			  .append("  player.media.addEventListener('loadedmetadata', metaListener);")
			  .append(" }")
			  .append("} catch(e) {\n")
			  .append("  if(window.console) console.log(e);\n")
			  .append("}");

			getWindowControl().getWindowBackOffice().sendCommandTo(new JSCommand(sb.toString()));
		}
	
		private void doEditChapter(UserRequest ureq, VideoChapterTableRow videoChapterTableRow, boolean chapterExists) {
			if(guardModalController(chapterEditCtr)) return;

			chapterEditCtr = new ChapterEditController(ureq, getWindowControl(), videoChapterTableRow, 
					chapterExists, tableModel.getObjects(), durationInSeconds); 
			listenTo(chapterEditCtr);
	
			cmc = new CloseableModalController(getWindowControl(), translate("close"), chapterEditCtr.getInitialComponent(), 
					true, translate("video.chapter." + (chapterExists ? "edit" : "new")));
			listenTo(cmc);
			cmc.activate();
		}
		
		private void doAddOrUpdateChapter(UserRequest ureq, VideoChapterTableRow row){
			//only add if object does not yet exist
			List<VideoChapterTableRow> chapters = new ArrayList<>(tableModel.getObjects());
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
			organizeChapters(chapters);
			tableModel.setObjects(chapters);
			chapterTable.reset(true, true, true);
			saveChapters();
			moveCurrentTime(row.getBegin());
			addToHistory(ureq, this);
		}
		
		/**
		 * Set end of previous chapter to beginning of the following.
		 * 
		 * @param chapters The list of chapters to reorganize
		 */
		private void organizeChapters(List<VideoChapterTableRow> chapters) {			
			for (int i = 1; i <= chapters.size(); i++) {
				VideoChapterTableRow previousChapter = chapters.get(i-1);
				if (i != chapters.size()) {
					VideoChapterTableRow currentChapter = chapters.get(i);
					previousChapter.setEnd(currentChapter.getBegin());				
				} else {
					// duration may hold no value, backup ask videoManager
					if (durationInSeconds <= 0) {
						Date endOfMovie = new Date(videoManager.getVideoDuration(entry.getOlatResource()));
						previousChapter.setEnd(endOfMovie);
					} else {
						previousChapter.setEnd(new Date(durationInSeconds * 1000l));					
					}
				}
			}
		}
	}
}