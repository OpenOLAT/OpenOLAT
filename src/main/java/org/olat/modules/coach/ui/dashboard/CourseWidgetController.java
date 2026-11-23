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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.vfs.model.VFSThumbnailInfos;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.figurecards.FigureCardsFactory;
import org.olat.core.gui.components.figurecards.FigureCardsItem;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelFormat;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.progressbar.ProgressBarCellRenderer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutOrientation;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.dashboard.DashboardUIFactory;
import org.olat.core.gui.control.generic.dashboard.MaxHeightScrollableDelegate;
import org.olat.core.gui.control.generic.dashboard.TableWidgetConfigPrefs;
import org.olat.core.gui.control.generic.dashboard.TableWidgetConfigProvider;
import org.olat.core.gui.control.generic.dashboard.TableWidgetController;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.modules.coach.CoachingRoles;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.CoachingSecurity;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.CoursesSummary;
import org.olat.modules.coach.model.CoursesSummaryKeyFigures;
import org.olat.modules.coach.model.ParticipantStatisticsEntry.SuccessStatus;
import org.olat.modules.coach.ui.CourseListController;
import org.olat.modules.coach.ui.component.SuccessStatusCellRenderer;
import org.olat.modules.coach.ui.component.SuccessStatusCellRenderer.SuccessStatusPercents;
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
	private static final String CMD_PARTICIPANTS = "participants";
	
	private FigureCardsItem figuresEl;
	private CourseTableModel dataModel;
	private FlexiTableElement tableEl;
	private ProgressBarCellRenderer completionCellRenderer;
	private SuccessStatusCellRenderer successStatusCellRenderer;
	private Map<String, FormLink> keyToFigureCard;
	private FormLink showAllLink;
	
	private CloseableCalloutWindowController calloutCtrl;

	private final CoachingSecurity coachingSec;
	private final MapperKey mapperThumbnailKey;
	private final RepositoryEntryImageMapper mapperThumbnail;
	private SelectionValues figureValues;
	private String keyFigureKey;
	
	@Autowired
	private MapperService mapperService;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private NodeAccessService nodeAccessService;

	public CourseWidgetController(UserRequest ureq, WindowControl wControl, CoachingSecurity coachingSec) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CourseListController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.coachingSec = coachingSec;
		mapperThumbnail = RepositoryEntryImageMapper.mapper210x140();
		mapperThumbnailKey = mapperService.register(null, RepositoryEntryImageMapper.MAPPER_ID_210_140, mapperThumbnail);
		completionCellRenderer = new ProgressBarCellRenderer(RenderStyle.horizontal, RenderSize.small, LabelFormat.none, BarColor.primary);
		successStatusCellRenderer = new SuccessStatusCellRenderer(getLocale(), false);
		
		initForm(ureq);
	}

	@Override
	protected String getTitle() {
		return "<i class=\"o_icon o_CourseModule_icon\"> </i> " + translate("courses");
	}

	@Override
	protected String createTopArea(FormLayoutContainer widgetCont) {
		figuresEl = FigureCardsFactory.createItem("figures", widgetCont);
		
		keyToFigureCard = new LinkedHashMap<>();
		figureValues = new SelectionValues();
		createIndicator(widgetCont, CoursesSummaryKeyFigures.marked.name(), "search.mark", "[CoachSite:0][Courses:0][owner:0][Bookmarks:0]");
		createIndicator(widgetCont, CoursesSummaryKeyFigures.all.name(), "all", "[CoachSite:0][Courses:0][owner:0][All:0]");
		createIndicator(widgetCont, CoursesSummaryKeyFigures.relevant.name(), "relevant", "[CoachSite:0][Courses:0][owner:0][Relevant:0]");
		createIndicator(widgetCont, CoursesSummaryKeyFigures.published.name(), "filter.published", "[CoachSite:0][Courses:0][owner:0][Published:0]");
		createIndicator(widgetCont, CoursesSummaryKeyFigures.access.name(), "filter.access.for.coach", "[CoachSite:0][Courses:0][owner:0][AccessForCoach:0]");
		createIndicator(widgetCont, CoursesSummaryKeyFigures.finished.name(), "filter.finished", "[CoachSite:0][Courses:0][owner:0][Finished:0]");
		
		return figuresEl.getComponent().getComponentName();
	}

	private void createIndicator(FormLayoutContainer widgetCont, String name, String figureValueI18n, String url) {
		FormLink link = FigureCardsFactory.createCardLink(name, CMD_OPEN, widgetCont);
		setUrl(link, url);
		keyToFigureCard.put(name, link);
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
		prefs.setNumRows(10);
		return prefs;
	}

	@Override
	public void update(TableWidgetConfigPrefs prefs) {
		keyFigureKey = prefs.getKeyFigureKey();
		figuresEl.setCards(getFigureCards(keyToFigureCard, prefs));
		setEmptyState(tableEl, "o_CourseModule_icon", translate("widget.course.objects"), keyFigureKey);
		
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
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(2);
		if (rowObject instanceof CourseRow courseRow) {
			if (courseRow.getTitleLink() != null) {
				cmps.add(courseRow.getTitleLink().getComponent());
			}
			if (courseRow.getParticipantsLink() != null) {
				cmps.add(courseRow.getParticipantsLink().getComponent());
			}
		}
		return cmps;
	}

	@Override
	protected List<String> createFooterButtons(FormLayoutContainer widgetCont) {
		showAllLink = DashboardUIFactory.createShowAllLink(widgetCont);
		setUrl(showAllLink, "[CoachSite:0][Courses:0][owner:0][All:0]");
		return List.of(showAllLink.getComponent().getComponentName());
	}
	
	public void reload() {
		List<CoachingRoles> roles = coachingSec.getCoachingRoles();
		CoursesSummaryKeyFigures keyFigure = CoursesSummaryKeyFigures.secureValueOf(keyFigureKey);
		CoursesSummary coursesSummary = coachingService.getCoursesSummaryStatistics(getIdentity(), roles,
				keyFigure, tableEl.getPageSize());
		updateIndicators(coursesSummary);
		updateTable(coursesSummary, keyFigure);
	}

	private void updateIndicators(CoursesSummary coursesSummary) {
		keyToFigureCard.get("all").setI18nKey(FigureCardsFactory.createCardText(null, null,
				String.valueOf(coursesSummary.numCourses()), translate("all")));

		int numRelevant = coursesSummary.numCoachPublished() + coursesSummary.numPublished();
		keyToFigureCard.get("relevant").setI18nKey(FigureCardsFactory.createCardText(null, null,
				String.valueOf(numRelevant), translate("relevant")));
		keyToFigureCard.get("marked").setI18nKey(FigureCardsFactory.createCardText("o_course_widget_icon o_icon_bookmark", null,
				String.valueOf(coursesSummary.numMarked()), translate("search.mark")));
		keyToFigureCard.get("access").setI18nKey(FigureCardsFactory.createCardText("o_course_widget_icon o_icon_coach", null,
				String.valueOf(coursesSummary.numCoachPublished()), translate("filter.access.for.coach")));
		keyToFigureCard.get("published").setI18nKey(FigureCardsFactory.createCardText("o_course_widget_icon o_icon_repo_status_published", null,
				String.valueOf(coursesSummary.numPublished()), translate("filter.published")));
		keyToFigureCard.get("finished").setI18nKey(FigureCardsFactory.createCardText("o_course_widget_icon o_icon_repo_status_closed", null,
				String.valueOf(coursesSummary.numFinished()), translate("filter.finished")));
	}

	private void updateTable(CoursesSummary coursesSummary, CoursesSummaryKeyFigures keyFigure) {
		List<CourseRow> rows = coursesSummary.courseStatistics().stream()
				.map(this::toRow)
				.toList();
		
		final Map<Long,VFSThumbnailInfos> thumbnails = mapperThumbnail.getResourceableThumbnails(rows);
		rows.forEach(r -> appendThumbnail(r, thumbnails));
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
		setFooterText(rows.size(), coursesSummary.numOf(keyFigure), getFigureLabel(keyFigureKey));
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
		
			if(withSuccessStatus(entry)) {
				target = new StringOutput();
				successStatusCellRenderer.render(null, target, entry.getSuccessStatus(), 0, null, null, getTranslator());
				row.setSuccessStatus(target.toString());
			}
			
			String participants = String.valueOf(entry.getParticipants());
			FormLink participantsLink = uifactory.addFormLink("participants_" + row.getKey(), CMD_PARTICIPANTS, participants, tableEl, Link.NONTRANSLATED);
			participantsLink.setIconLeftCSS("o_icon o_icon_group");
			participantsLink.setAriaLabel(translate("success.status.participants", participants));
			participantsLink.setAriaDialogOpener();
			participantsLink.setUserObject(entry.getSuccessStatus());
			participantsLink.setEnabled(withSuccessStatus(entry));
			row.setParticipantsLink(participantsLink);
		}
		
		VelocityContainer linkVC = createVelocityContainer("course_row_link");
		linkVC.setDomReplacementWrapperRequired(false);
		linkVC.contextPut("row", row);
		
		FormLink titleLink = uifactory.addFormLink("title_" + row.getKey(), CMD_OPEN, "", tableEl, Link.NONTRANSLATED);
		titleLink.setInnerComponent(linkVC);
		titleLink.setElementCssClass("o_link_plain o_row_select");
		setUrl(titleLink, "[RepositoryEntry:" + row.getKey() + "]");
		row.setTitleLink(titleLink);
		
		String url = BusinessControlFactory.getInstance().getRelativeURLFromBusinessPathString("[RepositoryEntry:" + row.getKey() + "]");
		row.setUrl(url);
		
		return row;
	}
	
	private boolean withSuccessStatus(CourseStatEntry entry) {
		Mode passedMode = entry.getPassedMode();
		return passedMode != null && passedMode != Mode.none;
	}

	private void appendThumbnail(CourseRow row, Map<Long,VFSThumbnailInfos> thumbnails) {
		String imageUrl = mapperThumbnail.getThumbnailURL(mapperThumbnailKey.getUrl(), row.getKey(), thumbnails);
		row.setThumbnailRelPath(imageUrl);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(calloutCtrl);
		calloutCtrl = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == showAllLink) {
			if (showAllLink.getUserObject() instanceof String businessPath) {
				doOpen(ureq, businessPath);
			}
		} else if (source instanceof FormLink link) {
			if (CMD_OPEN.equals(link.getCmd())) {
				if (link.getUserObject() instanceof String businessPath) {
					doOpen(ureq, businessPath);
				}
			} else if (CMD_PARTICIPANTS.equals(link.getCmd())) {
				if (link.getUserObject() instanceof SuccessStatus successStatus) {
					doOpenSuccessStatus(ureq, successStatus, link);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenSuccessStatus(UserRequest ureq, SuccessStatus successStatus, FormLink link) {
		cleanUp();
		
		SuccessStatusPercents percents = SuccessStatusCellRenderer.getPercents(successStatus);
		List<SuccessStatusItem> items = new ArrayList<>(3);
		items.add(new SuccessStatusItem("o_coaching_success_passed", translate("success.status.callout.value",
				translate("passed.true"), String.valueOf(successStatus.numPassed()), String.valueOf(percents.passed()))));
		items.add(new SuccessStatusItem("o_coaching_success_failed", translate("success.status.callout.value",
				translate("passed.false"), String.valueOf(successStatus.numFailed()), String.valueOf(percents.failed()))));
		items.add(new SuccessStatusItem("o_coaching_success_undefined", translate("success.status.callout.value",
				translate("passed.undefined"), String.valueOf(successStatus.numUndefined()), String.valueOf(percents.undefined()))));
		
		VelocityContainer calloutVC = createVelocityContainer("success_status_callout");
		calloutVC.contextPut("items", items);
		
		CalloutSettings settings = new CalloutSettings(true, CalloutOrientation.bottom, true, null);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), calloutVC, link, "", true, "", settings);
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doOpen(UserRequest ureq, String businessPath) {
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	public record SuccessStatusItem(String css, String text) {}
	
}
