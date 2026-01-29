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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.commons.services.vfs.model.VFSThumbnailInfos;
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
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dashboard.MaxHeightScrollableDelegate;
import org.olat.core.gui.control.generic.dashboard.TableWidgetConfigPrefs;
import org.olat.core.gui.control.generic.dashboard.TableWidgetConfigProvider;
import org.olat.core.gui.control.generic.dashboard.TableWidgetController;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.CoursesStatisticsParams;
import org.olat.modules.coach.model.CoursesStatisticsRuntimeTypesGroup;
import org.olat.modules.coach.ui.CourseListController;
import org.olat.modules.coach.ui.component.CompletionCellRenderer;
import org.olat.modules.coach.ui.component.SuccessStatusCellRenderer;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Oct 30, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CourseWidgetController extends TableWidgetController
		implements TableWidgetConfigProvider, FlexiTableComponentDelegate {

	private static final String CMD_OPEN = "open";
	
	private IndicatorsItem indicatorsEl;
	private CourseTableModel dataModel;
	private FlexiTableElement tableEl;
	private CompletionCellRenderer completionCellRenderer;
	private SuccessStatusCellRenderer successStatusCellRenderer;
	private Map<String, FormLink> keyToIndicatorLink;
	private FormLink showAllLink;

	private final MapperKey mapperThumbnailKey;
	private final RepositoryEntryImageMapper mapperThumbnail;
	private SelectionValues figureValues;
	private String keyFigureKey;
	
	@Autowired
	private MarkManager markManager;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private NodeAccessService nodeAccessService;

	public CourseWidgetController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CourseListController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		mapperThumbnail = RepositoryEntryImageMapper.mapper210x140();
		mapperThumbnailKey = mapperService.register(null, RepositoryEntryImageMapper.MAPPER_ID_210_140, mapperThumbnail);
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
		
		keyToIndicatorLink = new LinkedHashMap<>();
		figureValues = new SelectionValues();
		createIndicator(widgetCont, "marked", "search.mark", "[CoachSite:0][Courses:0][coach:0][Bookmarks:0]");
		createIndicator(widgetCont, "all", "all", "[CoachSite:0][Courses:0][coach:0][All:0]");
		createIndicator(widgetCont, "relevant", "relevant", "[CoachSite:0][Courses:0][coach:0][Relevant:0]");
		createIndicator(widgetCont, "published", "filter.published", "[CoachSite:0][Courses:0][coach:0][Published:0]");
		createIndicator(widgetCont, "access", "filter.access.for.coach", "[CoachSite:0][Courses:0][coach:0][AccessForCoach:0]");
		createIndicator(widgetCont, "finished", "filter.finished", "[CoachSite:0][Courses:0][coach:0][Finished:0]");
		
		return indicatorsEl.getComponent().getComponentName();
	}

	private void createIndicator(FormLayoutContainer widgetCont, String name, String figureValueI18n, String url) {
		FormLink link = IndicatorsFactory.createIndicatorFormLink(name, CMD_OPEN, "", "", widgetCont);
		setUrl(link, url);
		keyToIndicatorLink.put(name, link);
		figureValues.add(SelectionValues.entry(name, translate(figureValueI18n)));
	}
	
	@Override
	protected TableWidgetConfigProvider getConfigProvider() {
		return this;
	}

	@Override
	public String getId() {
		return "coaching-course-widget-v2";
	}

	@Override
	public SelectionValues getFigureValues() {
		return figureValues;
	}

	@Override
	public TableWidgetConfigPrefs getDefault() {
		TableWidgetConfigPrefs prefs = new TableWidgetConfigPrefs();
		prefs.setKeyFigureKey("relevant");
		Set<String> figureKeys = Set.of("marked", "published", "access");
		prefs.setFocusFigureKeys(figureKeys);
		prefs.setNumRows(5);
		return prefs;
	}

	@Override
	public void update(TableWidgetConfigPrefs prefs) {
		keyFigureKey = prefs.getKeyFigureKey();
		
		FormLink keyIndicator = keyToIndicatorLink.get(keyFigureKey);
		indicatorsEl.setKeyIndicator(keyIndicator);
		
		List<FormItem> focusIndicators = keyToIndicatorLink.entrySet().stream()
				.filter(keyToLink -> prefs.getFocusFigureKeys().contains(keyToLink.getKey()))
				.map(Entry::getValue)
				.map(FormItem.class::cast)
				.toList();
		indicatorsEl.setFocusIndicatorsItems(focusIndicators);
		
		setTableTitle(figureValues.getValue(keyFigureKey));
		
		tableEl.setPageSize(prefs.getNumRows());
		
		reload();
	}

	@Override
	protected String createTable(FormLayoutContainer widgetCont) {
		// No table view => No columns needed.
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		dataModel = new CourseTableModel(tableColumnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 15, true, getTranslator(), widgetCont);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setShowSmallPageSize(false);
		
		tableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer rowVC = createVelocityContainer("course_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		tableEl.setCssDelegate(MaxHeightScrollableDelegate.DELEGATE);
		
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

		CoursesStatisticsParams coursesStatisticsParams = new CoursesStatisticsParams(true, false, true, false, false,
				CoursesStatisticsRuntimeTypesGroup.standaloneAndCurricular.runtimeTypes());
		List<CourseStatEntry> courseStatistics = coachingService.getCoursesStatistics(getIdentity(), GroupRoles.coach,
				coursesStatisticsParams);
		updateIndicators(courseStatistics, markedKeys);
		updateTable(courseStatistics, markedKeys);
	}

	private void updateIndicators(List<CourseStatEntry> courseStatistics, Set<Long> markedKeys) {
		keyToIndicatorLink.get("all").setI18nKey(IndicatorsFactory.createLinkText(
				translate("all"),
				String.valueOf(courseStatistics.size())));
		
		int numMarked = 0;
		int numCoachPublished = 0;
		int numPublished = 0;
		int numFinished = 0;
		for (CourseStatEntry entry : courseStatistics) {
			if (markedKeys.contains(entry.getRepoKey())) {
				numMarked++;
			}
			
			if (RepositoryEntryStatusEnum.coachpublished == entry.getRepoStatus()) {
				numCoachPublished++;
			} else if (RepositoryEntryStatusEnum.published == entry.getRepoStatus()) {
				numPublished++;
			} else if (RepositoryEntryStatusEnum.closed == entry.getRepoStatus()) {
				numFinished++;
			}
		}
		
		keyToIndicatorLink.get("relevant").setI18nKey(IndicatorsFactory.createLinkText(
				translate("relevant"),
				String.valueOf(numCoachPublished + numPublished)));
		keyToIndicatorLink.get("marked").setI18nKey(IndicatorsFactory.createLinkText(
				"<i class=\"o_icon o_course_widget_icon o_icon_bookmark\"></i> " + translate("search.mark"),
				String.valueOf(numMarked)));
		keyToIndicatorLink.get("access").setI18nKey(IndicatorsFactory.createLinkText(
				"<i class=\"o_icon o_course_widget_icon o_icon_coach\"></i> " + translate("filter.access.for.coach"),
				String.valueOf(numCoachPublished)));
		keyToIndicatorLink.get("published").setI18nKey(IndicatorsFactory.createLinkText(
				"<i class=\"o_icon o_course_widget_icon o_icon_repo_status_published\"></i> " + translate("filter.published"),
				String.valueOf(numPublished)));
		keyToIndicatorLink.get("finished").setI18nKey(IndicatorsFactory.createLinkText(
				"<i class=\"o_icon o_course_widget_icon o_icon_repo_status_closed\"></i> " + translate("filter.finished"),
				String.valueOf(numFinished)));
	}

	private void setUrl(FormLink link, String businessPath) {
		link.setUserObject(businessPath);
		String url = BusinessControlFactory.getInstance().getRelativeURLFromBusinessPathString(businessPath);
		link.setUrl(url);
	}

	private void updateTable(List<CourseStatEntry> courseStatistics, Set<Long> markedKeys) {
		Predicate<? super CourseStatEntry> filterPredicate = getFilterPredicate(markedKeys);
		List<CourseRow> rows = courseStatistics.stream()
				.filter(filterPredicate)
				.sorted(Comparator.comparing(CourseStatEntry::getLastVisit, Comparator.nullsFirst(Date::compareTo).reversed()))
				.limit(tableEl.getPageSize())
				.map(this::toRow)
				.toList();
		
		final Map<Long,VFSThumbnailInfos> thumbnails = mapperThumbnail.getResourceableThumbnails(rows);
		rows.forEach(r -> appendThumbnail(r, thumbnails));
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private Predicate<CourseStatEntry> getFilterPredicate(Set<Long> markedKeys) {
		if (keyFigureKey != null) {
			return switch (keyFigureKey) {
			case "marked" -> entry -> markedKeys.contains(entry.getRepoKey());
			case "relevant" -> entry -> RepositoryEntryStatusEnum.published == entry.getRepoStatus()
										|| RepositoryEntryStatusEnum.coachpublished == entry.getRepoStatus();
			case "published" -> entry -> RepositoryEntryStatusEnum.published == entry.getRepoStatus();
			case "access" -> entry -> RepositoryEntryStatusEnum.coachpublished == entry.getRepoStatus();
			case "finished" -> entry -> RepositoryEntryStatusEnum.closed == entry.getRepoStatus();
			default -> entry -> true;
			};
		}
		return entry -> true;
	}
	
	private CourseRow toRow(CourseStatEntry entry) {
		CourseRow row = new CourseRow();
		row.setKey(entry.getRepoKey());
		row.setDisplayName(entry.getRepoDisplayName());
		row.setExternalRef(entry.getRepoExternalRef());
		row.setResourceableId(entry.getResourceableId());
		row.setResourceableTypeName(entry.getResourceableTypeName());
		
		if (StringHelper.containsNonWhitespace(entry.getRepoTechnicalType())) {
			NodeAccessType type = NodeAccessType.of(entry.getRepoTechnicalType());
			String translatedType = ConditionNodeAccessProvider.TYPE.equals(type.getType())
					? translate("CourseModule")
					: nodeAccessService.getNodeAccessTypeName(type, getLocale());
			row.setTranslatedTechnicalType(translatedType);
		} else {
			row.setTranslatedTechnicalType(translate(entry.getResourceTypeName()));
		}
		
		if ("CourseModule".equals(entry.getResourceTypeName())) {
			StringOutput target = new StringOutput();
			completionCellRenderer.render(null, target, entry.getAverageCompletion(), 0, null, null, getTranslator());
			row.setCompletion(target.toString());
		
			target = new StringOutput();
			successStatusCellRenderer.render(null, target, entry.getSuccessStatus(), 0, null, null, getTranslator());
			row.setSuccessStatus(target.toString());
		}
		
		String url = BusinessControlFactory.getInstance().getRelativeURLFromBusinessPathString("[RepositoryEntry:" + row.getKey() + "]");
		row.setUrl(url);
		
		return row;
	}
	

	private void appendThumbnail(CourseRow row, Map<Long,VFSThumbnailInfos> thumbnails) {
		String imageUrl = mapperThumbnail.getThumbnailURL(mapperThumbnailKey.getUrl(), row.getKey(), thumbnails);
		row.setThumbnailRelPath(imageUrl);
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
