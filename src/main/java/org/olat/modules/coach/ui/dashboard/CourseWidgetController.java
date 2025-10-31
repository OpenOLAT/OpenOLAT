/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.coach.ui.dashboard;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.indicators.IndicatorsFactory;
import org.olat.core.gui.components.indicators.IndicatorsItem;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dashboard.TableWidgetController;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.CoursesStatisticsRuntimeTypesGroup;
import org.olat.modules.coach.ui.CourseListController;
import org.olat.modules.coach.ui.component.CompletionCellRenderer;
import org.olat.modules.coach.ui.component.SuccessStatusCellRenderer;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Oct 30, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CourseWidgetController extends TableWidgetController implements FlexiTableComponentDelegate {

	private static final String CMD_OPEN = "open";
	
	private IndicatorsItem indicatorsEl;
	private CourseTableModel dataModel;
	private FlexiTableElement tableEl;
	private CompletionCellRenderer completionCellRenderer;
	private SuccessStatusCellRenderer successStatusCellRenderer;
	private FormLink indicatorAllLink;
	private FormLink indicatorMarkedLink;
	private FormLink indicatorPublishedLink;
	private FormLink indicatorCoachPublishedLink;
	private FormLink showAllLink;

	private final MapperKey mapperThumbnailKey;
	
	@Autowired
	private MarkManager markManager;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private RepositoryManager repositoryManager;

	public CourseWidgetController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CourseListController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		mapperThumbnailKey = mapperService.register(null, "repositoryentryImage", new RepositoryEntryImageMapper(210, 140));
		completionCellRenderer = new CompletionCellRenderer(getTranslator());
		successStatusCellRenderer = new SuccessStatusCellRenderer();
		
		initForm(ureq);
	}

	@Override
	protected String getTitle() {
		return "<i class=\"o_icon o_CourseModule_icon\"> </i> " + translate("course.as.coach");
	}

	@Override
	protected String createIndicators(FormLayoutContainer widgetCont) {
		indicatorsEl = IndicatorsFactory.createItem("indicators", widgetCont);
		
		indicatorAllLink = IndicatorsFactory.createIndicatorFormLink("courses", CMD_OPEN, "", "", widgetCont);
		setUrl(indicatorAllLink, "[CoachSite:0][Courses:0][coach:0][All:0]");
		indicatorsEl.setKeyIndicator(indicatorAllLink);
		
		indicatorMarkedLink = IndicatorsFactory.createIndicatorFormLink("marked", CMD_OPEN, "", "", widgetCont);
		setUrl(indicatorMarkedLink, "[CoachSite:0][Courses:0][coach:0][Bookmarks:0]");
		
		indicatorPublishedLink = IndicatorsFactory.createIndicatorFormLink("relevant", CMD_OPEN, "", "", widgetCont);
		setUrl(indicatorPublishedLink, "[CoachSite:0][Courses:0][coach:0][Published:0]");
		
		indicatorCoachPublishedLink = IndicatorsFactory.createIndicatorFormLink("access", CMD_OPEN, "", "", widgetCont);
		setUrl(indicatorCoachPublishedLink, "[CoachSite:0][Courses:0][coach:0][AccessForCoach:0]");
		
		List<FormItem> focusIndicators = List.of(indicatorMarkedLink, indicatorPublishedLink, indicatorCoachPublishedLink);
		indicatorsEl.setFocusIndicatorsItems(focusIndicators);
		
		return indicatorsEl.getComponent().getComponentName();
	}

	@Override
	protected String createTable(FormLayoutContainer widgetCont) {
		// No table view => No columns needed.
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		dataModel = new CourseTableModel(tableColumnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 5, true, getTranslator(), widgetCont);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		
		tableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer rowVC = createVelocityContainer("course_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		
		return tableEl.getComponent().getComponentName();
	}
	
	@Override
	public boolean isRowClickEnabled() {
		return true;
	}

	@Override
	protected String createShowAll(FormLayoutContainer widgetCont) {
		showAllLink = createShowAllLink(widgetCont);
		setUrl(showAllLink, "[CoachSite:0][Courses:0][coach:0][All:0]");
		return showAllLink.getComponent().getComponentName();
	}
	
	public void reload() {
		Set<Long> markedKeys = markManager.getMarks(getIdentity(), List.of("RepositoryEntry")).stream()
				.map(Mark::getOLATResourceable)
				.map(OLATResourceable::getResourceableId)
				.collect(Collectors.toSet());
		
		List<CourseStatEntry> courseStatistics = coachingService.getCoursesStatistics(getIdentity(), GroupRoles.coach,
				CoursesStatisticsRuntimeTypesGroup.standaloneAndCurricular);
		
		updateIndicators(courseStatistics, markedKeys);
		updateTable(courseStatistics, markedKeys);
	}

	private void updateIndicators(List<CourseStatEntry> courseStatistics, Set<Long> markedKeys) {
		indicatorAllLink.setI18nKey(IndicatorsFactory.createLinkText(
				translate("course.courses"),
				String.valueOf(courseStatistics.size())));
		
		int numMarked = 0;
		int numPublished = 0;
		int numCoachPublished = 0;
		for (CourseStatEntry entry : courseStatistics) {
			if (markedKeys.contains(entry.getRepoKey())) {
				numMarked++;
			}
			if (RepositoryEntryStatusEnum.published == entry.getRepoStatus()) {
				numPublished++;
			}
			if (RepositoryEntryStatusEnum.coachpublished == entry.getRepoStatus()) {
				numCoachPublished++;
			}
		}
		
		indicatorMarkedLink.setI18nKey(IndicatorsFactory.createLinkText(
				"<i class=\"o_icon o_icon_bookmark\"></i> " + translate("search.mark"),
				String.valueOf(numMarked)));
		indicatorPublishedLink.setI18nKey(IndicatorsFactory.createLinkText(
				translate("filter.published"),
				String.valueOf(numPublished)));
		indicatorCoachPublishedLink.setI18nKey(IndicatorsFactory.createLinkText(
				"<i class=\"o_icon o_icon_coach\"></i> " + translate("filter.access.for.coach"),
				String.valueOf(numCoachPublished)));
	}

	private void setUrl(FormLink link, String businessPath) {
		link.setUserObject(businessPath);
		String url = BusinessControlFactory.getInstance().getRelativeURLFromBusinessPathString(businessPath);
		link.setUrl(url);
	}

	private void updateTable(List<CourseStatEntry> courseStatistics, Set<Long> markedKeys) {
		List<CourseRow> rows = courseStatistics.stream()
				.filter(statsEntry -> markedKeys.contains(statsEntry.getRepoKey()))
				.sorted(Comparator.comparing(CourseStatEntry::getLastVisit, Comparator.nullsFirst(Date::compareTo).reversed()))
				.limit(5)
				.map(this::toRow)
				.toList();
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private CourseRow toRow(CourseStatEntry entry) {
		CourseRow row = new CourseRow();
		row.setKey(entry.getRepoKey());
		row.setExternalRef(entry.getRepoExternalRef());
		row.setDisplayName(entry.getRepoDisplayName());
		
		VFSLeaf image = repositoryManager.getImage(entry.getRepoKey(), OresHelper.createOLATResourceableInstance("CourseModule", entry.getResourceId()));
		if(image != null) {
			row.setThumbnailRelPath(RepositoryEntryImageMapper.getImageUrl(mapperThumbnailKey.getUrl(), image));
		}
		
		if ("CourseModule".equals(entry.getResourceTypeName())) {
			StringOutput target = new StringOutput();
			completionCellRenderer.render(null, target, entry.getAverageCompletion(), 0, null, null, getTranslator());
			row.setCompletion(target.toString());
		
			target = new StringOutput();
			successStatusCellRenderer.render(null, target, entry.getSuccessStatus(), 0, null, null, getTranslator());
			row.setSuccessStatus(target.toString());
		}
		
		return row;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent se) {
				doOpenCourse(ureq, se.getIndex());
			}
		} else if (source == showAllLink) {
			if (showAllLink.getUserObject() instanceof String businessPath) {
				doOpen(ureq, businessPath);
			}
		} else if (source instanceof FormLink link) {
			if (CMD_OPEN.equals(link.getCmd())) {
				if (link.getUserObject() instanceof String businessPath) {
					doOpen(ureq, businessPath);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenCourse(UserRequest ureq, int index) {
		CourseRow row = dataModel.getObject(index);
		doOpen(ureq, "[RepositoryEntry:" + row.getKey() + "]");
	}
	
	private void doOpen(UserRequest ureq, String businessPath) {
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

}
