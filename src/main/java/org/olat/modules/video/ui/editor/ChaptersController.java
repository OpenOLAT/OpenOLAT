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
package org.olat.modules.video.ui.editor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.ui.ChapterEditController;
import org.olat.modules.video.ui.VideoChapterTableRow;
import org.olat.modules.video.ui.VideoSettingsController;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2022-11-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ChaptersController extends FormBasicController {
	public static final Event RELOAD_CHAPTERS_EVENT = new Event("video.edit.reload.chapters");
	private static final String EDIT_ACTION = "edit";
	private static final String SELECT_ACTION = "select";
	private static final String TOOLS_COMMAND = "tools";

	private FormLink addChapterButton;
	private FlexiTableElement chapterTable;
	private ChapterTableModel tableModel;
	private final RepositoryEntry repositoryEntry;
	private CloseableModalController cmc;
	private ChapterEditController chapterEditController;

	@Autowired
	private VideoManager videoManager;

	private final long durationInSeconds;
	private String currentTimeCode;
	private final Translator videoTranslator;
	private CloseableCalloutWindowController ccwc;
	private ToolsController toolsController;

	public ChaptersController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
							  long durationInSeconds) {
		super(ureq, wControl, "chapters");
 		this.repositoryEntry = repositoryEntry;
		this.durationInSeconds = durationInSeconds;
		videoTranslator = Util.createPackageTranslator(VideoSettingsController.class, ureq.getLocale());
		initForm(ureq);
		loadTableModel();
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(chapterEditController);
		removeAsListenerAndDispose(toolsController);
		cmc = null;
		ccwc = null;
		chapterEditController = null;
		toolsController = null;
	}

	private void loadTableModel() {
		List<ChapterTableRow> chapters = videoManager.loadChapters(repositoryEntry.getOlatResource()).stream()
				.map(this::mapChapterRow).toList();
		tableModel.setObjects(chapters);
		chapterTable.reset(true, true, true);
	}

	private ChapterTableRow mapChapterRow(VideoChapterTableRow videoChapterTableRow) {
		ChapterTableRow chapterTableRow = new ChapterTableRow(videoChapterTableRow);
		addToolLink(chapterTableRow);
		return chapterTableRow;
	}

	private void addToolLink(ChapterTableRow chapterTableRow) {
		String toolId = "tool_" + chapterTableRow.hashCode();
		FormLink toolLink = (FormLink) flc.getFormComponent(toolId);
		if (toolLink == null) {
			toolLink = uifactory.addFormLink(toolId, TOOLS_COMMAND, "", chapterTable,
					Link.LINK | Link.NONTRANSLATED);
			toolLink.setTranslator(getTranslator());
			toolLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolLink.setTitle(translate("table.header.chapter.tools"));
		}
		toolLink.setUserObject(chapterTableRow);
		chapterTableRow.setToolLink(toolLink);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addChapterButton = uifactory.addFormLink("addChapter", "form.chapter.add",
				"form.chapter.add", formLayout, Link.BUTTON);

		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChapterTableModel.ChapterTableCols.start,
				SELECT_ACTION));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChapterTableModel.ChapterTableCols.text));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ChapterTableModel.ChapterTableCols.edit.i18nHeaderKey(),
				translate(ChapterTableModel.ChapterTableCols.edit.i18nHeaderKey()), EDIT_ACTION));
		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(
				ChapterTableModel.ChapterTableCols.tools.i18nHeaderKey(),
				ChapterTableModel.ChapterTableCols.tools.ordinal()
				);
		toolsColumn.setIconHeader("o_icon o_icon_actions o_icon-fws o_icon-lg");
		columnModel.addFlexiColumnModel(toolsColumn);

		tableModel = new ChapterTableModel(columnModel);

		chapterTable = uifactory.addTableElement(getWindowControl(), "chapters", tableModel,
				getTranslator(), formLayout);
		chapterTable.setCustomizeColumns(false);
		chapterTable.setNumOfRowsEnabled(false);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (chapterEditController == source) {
			if (event == Event.DONE_EVENT) {
				doAddOrUpdateChapter(ureq, chapterEditController.getVideoChapterTableRow());
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		} else if (ccwc == source) {
			cleanUp();
		} else if (toolsController == source) {
			if (ToolsController.DELETE_EVENT.getCommand().equals(event.getCommand())) {
				doDelete(ureq, toolsController.getChapterTableRow());
			}
			ccwc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void doDelete(UserRequest ureq, ChapterTableRow chapterTableRow) {
		List<ChapterTableRow> chapters = new ArrayList<>(tableModel.getObjects());
		chapters.remove(chapterTableRow);
		updateChapters(ureq, chapters);
	}

	private void doAddOrUpdateChapter(UserRequest ureq, VideoChapterTableRow videoChapterTableRow) {
		List<ChapterTableRow> chapters = new ArrayList<>(tableModel.getObjects());
		ChapterTableRow chapterTableRow = mapChapterRow(videoChapterTableRow);
		if (!chapters.contains(chapterTableRow)){
			chapters.add(chapterTableRow);
		}
		updateChapters(ureq, chapters);
	}

	private void updateChapters(UserRequest ureq, List<ChapterTableRow> chapters) {
		sortAndAlignChapters(chapters);
		tableModel.setObjects(chapters);
		chapterTable.reset(true, true, true);
		List<VideoChapterTableRow> rows = chapters.stream().map(ChapterTableRow::getVideoChapterTableRow).toList();
		videoManager.saveChapters(rows, repositoryEntry.getOlatResource());
		reloadChapters(ureq);
	}

	private void reloadChapters(UserRequest ureq) {
		fireEvent(ureq, RELOAD_CHAPTERS_EVENT);
	}


	private void sortAndAlignChapters(List<ChapterTableRow> chapters) {
		chapters.sort(Comparator.comparing(ChapterTableRow::getBegin));
		for (int i = 1; i <= chapters.size(); i++) {
			if (i < chapters.size()) {
				chapters.get(i - 1).setEnd(chapters.get(i).getBegin());
			} else {
				chapters.get(i - 1).setEnd(new Date(durationInSeconds * 1000));
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (event instanceof SelectionEvent selectionEvent) {
			ChapterTableRow row = tableModel.getObject(selectionEvent.getIndex());
			if (EDIT_ACTION.equals(selectionEvent.getCommand())) {
				doEdit(ureq, row.getVideoChapterTableRow(), true);
			} else if (SELECT_ACTION.equals(selectionEvent.getCommand())) {
				doSelect(ureq, row);
			}
		} else if (addChapterButton == source) {
			long timeInMs = currentTimeCode != null ? Math.round(Double.parseDouble(currentTimeCode)) * 1000L : 0L;
			Date currentDate = new Date(timeInMs);
			VideoChapterTableRow videoChapterTableRow = new VideoChapterTableRow(
					videoTranslator.translate("video.chapter.new"),
					TimelineModel.durationString(timeInMs),
					currentDate,
					currentDate
			);
			doEdit(ureq, videoChapterTableRow, false);
		} else if (source instanceof FormLink formLink && TOOLS_COMMAND.equals(formLink.getCmd()) &&
				formLink.getUserObject() instanceof ChapterTableRow chapterTableRow) {
			doOpenTools(ureq, chapterTableRow);
		}
	}

	private void doSelect(UserRequest ureq, ChapterTableRow row) {
		fireEvent(ureq, new ChapterSelectedEvent(TimelineDataSource.generateChapterId(row.getVideoChapterTableRow()),
				row.getBegin().getTime()));
	}

	private void doEdit(UserRequest ureq, VideoChapterTableRow videoChapterTableRow, boolean chapterExists) {
		if (guardModalController(chapterEditController)) {
			return;
		}

		List<VideoChapterTableRow> videoChapterTableRows = tableModel.getObjects().stream()
				.map(ChapterTableRow::getVideoChapterTableRow).toList();
		chapterEditController = new ChapterEditController(ureq, getWindowControl(),	videoChapterTableRow, chapterExists,
				videoChapterTableRows, durationInSeconds);
		listenTo(chapterEditController);

		String title = videoTranslator.translate("video.chapter." + (chapterExists ? "edit" : "new"));
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				chapterEditController.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpenTools(UserRequest ureq, ChapterTableRow chapterTableRow) {
		toolsController = new ToolsController(ureq, getWindowControl(), chapterTableRow);
		listenTo(toolsController);
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(), toolsController.getInitialComponent(),
				chapterTableRow.getToolLink().getFormDispatchId(), "", true, "");
		listenTo(ccwc);
		ccwc.activate();
	}

	@Override
	protected void formOK(UserRequest ureq) {

	}

	public void setCurrentTimeCode(String currentTimeCode) {
		this.currentTimeCode = currentTimeCode;
	}

	public void handleDeleted() {
		loadTableModel();
	}

	private static class ToolsController extends BasicController {
		private static final Event DELETE_EVENT = new Event("delete");
		private final Link deleteLink;
		private final ChapterTableRow chapterTableRow;

		protected ToolsController(UserRequest ureq, WindowControl wControl, ChapterTableRow chapterTableRow) {
			super(ureq, wControl);

			this.chapterTableRow = chapterTableRow;

			VelocityContainer mainVC = createVelocityContainer("table_row_tools");
			deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this,
					Link.LINK);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete");
			mainVC.put("delete", deleteLink);

			putInitialPanel(mainVC);
		}

		public ChapterTableRow getChapterTableRow() {
			return chapterTableRow;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if (deleteLink == source) {
				fireEvent(ureq, DELETE_EVENT);
			}
		}
	}
}
