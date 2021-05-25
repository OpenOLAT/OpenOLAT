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
package org.olat.modules.lecture.ui.coach;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.AggregatedLectureBlocksStatistics;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.modules.lecture.model.LectureBlockRefImpl;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.coach.LecturesListDataModel.StatsCols;
import org.olat.modules.lecture.ui.component.PercentCellRenderer;
import org.olat.modules.lecture.ui.event.SelectLectureBlockEvent;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesListController extends FormBasicController {
	
	public static final int USER_PROPS_OFFSET = 500;
	
	private FormLink exportButton;
	private FlexiTableElement tableEl;
	private LecturesListDataModel tableModel;
	
	private final boolean showExport;
	private final boolean showRepositoryEntry;
	
	private int count = 0;
	private final String propsIdentifier;
	private final boolean absenceNoticeEnabled;
	private final boolean authorizedAbsenceEnabled;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private List<LectureBlockIdentityStatistics> statistics;
	
	private LectureBlocksCallout lectureBlocksListCtrl;
	private CloseableCalloutWindowController lectureBlocksListCalloutCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	
	public LecturesListController(UserRequest ureq, WindowControl wControl,
			List<LectureBlockIdentityStatistics> statistics,
			List<UserPropertyHandler> userPropertyHandlers, String propsIdentifier,
			boolean showRepositoryEntry, boolean showExport) {
		super(ureq, wControl, "lectures_coaching", Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.statistics = statistics;
		this.propsIdentifier = propsIdentifier;
		this.showExport = showExport;
		this.showRepositoryEntry = showRepositoryEntry;
		this.userPropertyHandlers = userPropertyHandlers;
		absenceNoticeEnabled = lectureModule.isAbsenceNoticeEnabled();
		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(showExport) {
			exportButton = uifactory.addFormLink("export", formLayout, Link.BUTTON);
			exportButton.setIconLeftCSS("o_icon o_icon_download");
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, StatsCols.id));

		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(propsIdentifier, userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, null,
					true, userPropertyHandler.i18nColumnDescriptorLabelKey()));
		}

		if(showRepositoryEntry) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.externalRef, "open.course"));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.entry, "open.course"));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.plannedLectures));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.attendedLectures));
		if(authorizedAbsenceEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.unauthorizedAbsenceLectures));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.authorizedAbsenceLectures));
			if(absenceNoticeEnabled) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.dispensedLectures));
			}
		} else {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.absentLectures));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.currentRate, new PercentCellRenderer()));
		
		tableModel = new LecturesListDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setFooter(true);
	}
	
	public void reloadModel(List<LectureBlockIdentityStatistics> statistics) {
		this.statistics = new ArrayList<>(statistics);
		loadModel();
		tableEl.reset(false, false, true);
	}
	
	private void loadModel() {
		AggregatedLectureBlocksStatistics total = lectureService.aggregatedStatistics(statistics);
		List<LectureBlockIdentityStatisticsRow> rows = statistics.stream()
				.map(this::forgeRow)
				.collect(Collectors.toList());
		tableModel.setObjects(rows, total);
	}
	
	private LectureBlockIdentityStatisticsRow forgeRow(LectureBlockIdentityStatistics stats) {
		LectureBlockIdentityStatisticsRow row = new LectureBlockIdentityStatisticsRow(stats);
		if(authorizedAbsenceEnabled && stats.getTotalAbsentLectures() > 0l) {
			String val = Long.toString(stats.getTotalAbsentLectures());
			FormLink link = uifactory.addFormLink("unauthorized_" + (count++), "unauthorized", val, null, flc, Link.LINK | Link.NONTRANSLATED);
			row.setUnauthorizedLink(link);
			link.setUserObject(row);
		}
		return row;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == lectureBlocksListCtrl) {
			lectureBlocksListCalloutCtrl.deactivate();
			cleanUp();
		} else if(source == lectureBlocksListCalloutCtrl) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(lectureBlocksListCalloutCtrl);
		removeAsListenerAndDispose(lectureBlocksListCtrl);
		lectureBlocksListCalloutCtrl = null;
		lectureBlocksListCtrl = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == exportButton) {
			doExportStatistics(ureq);
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("open.course".equals(cmd)) {
					LectureBlockIdentityStatisticsRow row = tableModel.getObject(se.getIndex());
					doOpenCourseLectures(ureq, row.getStatistics());
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("unauthorized".equals(link.getCmd()) && link.getUserObject() instanceof LectureBlockIdentityStatisticsRow) {
				doOpenUnauthorized(ureq, ((LectureBlockIdentityStatisticsRow)link.getUserObject()).getStatistics(), link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doExportStatistics(UserRequest ureq) {
		LecturesStatisticsExport export = new LecturesStatisticsExport(statistics, null, null, userPropertyHandlers, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(export);
	}
	
	private void doOpenCourseLectures(UserRequest ureq, LectureBlockIdentityStatistics row) {
		Long repoKey = row.getRepoKey();
		String businessPath = "[RepositoryEntry:" + repoKey + "][Lectures:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doSelectLectureBlock(UserRequest ureq, LectureBlock lectureBlock) {
		fireEvent(ureq, new SelectLectureBlockEvent(lectureBlock));
	}
	
	private void doOpenUnauthorized(UserRequest ureq, LectureBlockIdentityStatistics row, FormLink link) {
		removeAsListenerAndDispose(lectureBlocksListCalloutCtrl);
		removeAsListenerAndDispose(lectureBlocksListCtrl);
		
		List<Long> lectureBlockKeys = row.getAbsentLectureBlocks();
		if(lectureBlockKeys.isEmpty()) {
			// don nothing
		} else if(lectureBlockKeys.size() == 1) {
			LectureBlock lectureBlock = lectureService
					.getLectureBlock(new LectureBlockRefImpl(lectureBlockKeys.get(0)));
			doSelectLectureBlock(ureq, lectureBlock);
		} else {
			List<LectureBlock> lectureBlocks = lectureService.getLectureBlocks(lectureBlockKeys);
			lectureBlocksListCtrl = new LectureBlocksCallout(ureq, getWindowControl(), lectureBlocks);
			listenTo(lectureBlocksListCtrl);
		
			lectureBlocksListCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					lectureBlocksListCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(lectureBlocksListCalloutCtrl);
			lectureBlocksListCalloutCtrl.activate();
		}
	}
	
	public class LectureBlocksCallout extends BasicController {
		
		private final Formatter formatter;
		
		public LectureBlocksCallout(UserRequest ureq, WindowControl wControl, List<LectureBlock> lectureBlocks) {
			super(ureq, wControl);
			
			formatter = Formatter.getInstance(getLocale());
			
			VelocityContainer mainVC = createVelocityContainer("lectureblocks_callout");
			List<String> linkNames = new ArrayList<>();
			for(LectureBlock lectureBlock:lectureBlocks) {
				String name = generateName(lectureBlock);
				String cmpId = "lb_" + count++;
				Link link = LinkFactory.createLink(cmpId, cmpId, "select_block", name, getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
				link.setIconLeftCSS("o_icon o_icon_lecture");
				link.setUserObject(lectureBlock);
				linkNames.add(link.getComponentName());
			}
			mainVC.contextPut("lectureBlocks", linkNames);
			
			putInitialPanel(mainVC);
		}
		
		private String generateName(LectureBlock lectureBlock) {
			StringBuilder sb = new StringBuilder(64);
			String title = lectureBlock.getTitle();
			if(StringHelper.containsNonWhitespace(title)) {
				sb.append(title);
			}
			
			String date = formatter.formatDate(lectureBlock.getStartDate());
			String startTime = formatter.formatTimeShort(lectureBlock.getStartDate());
			String endTime = formatter.formatTimeShort(lectureBlock.getEndDate());
			sb.append(" ").append(date)
			  .append(" ").append(startTime)
			  .append(" - ").append(endTime);
			
			return sb.toString();
		}

		@Override
		protected void doDispose() {
			//
		}
		
		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(source instanceof Link && ((Link)source).getUserObject() instanceof LectureBlock) {
				Link link = (Link)source;
				close();
				doSelectLectureBlock(ureq, (LectureBlock)link.getUserObject());
			}
		}
		
		private void close() {
			lectureBlocksListCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}
