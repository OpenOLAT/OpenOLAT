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
package org.olat.modules.curriculum.ui.widgets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.core.commons.services.vfs.model.VFSThumbnailInfos;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.indicators.IndicatorsFactory;
import org.olat.core.gui.components.indicators.IndicatorsItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutOrientation;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.dashboard.MaxHeightScrollableDelegate;
import org.olat.core.gui.control.generic.dashboard.TableWidgetConfigPrefs;
import org.olat.core.gui.control.generic.dashboard.TableWidgetConfigProvider;
import org.olat.core.gui.control.generic.dashboard.TableWidgetController;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumElementDetailsController;
import org.olat.modules.curriculum.ui.CurriculumElementImageMapper;
import org.olat.modules.curriculum.ui.CurriculumStructureCalloutController;
import org.olat.modules.curriculum.ui.component.CurriculumStatusCellRenderer;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.modules.curriculum.ui.event.SelectCurriculumElementRowEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 22, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ImplementationWidgetController extends TableWidgetController
		implements TableWidgetConfigProvider, FlexiTableComponentDelegate {
	
	private static final EnumSet<CurriculumElementStatus> RELEVANT_STATUS = EnumSet.of(
			CurriculumElementStatus.preparation, 
			CurriculumElementStatus.provisional, 
			CurriculumElementStatus.confirmed,
			CurriculumElementStatus.active);
	private static final String CMD_OPEN = "open";
	private static final String CMD_STRUCTURE = "structure";

	private IndicatorsItem indicatorsEl;
	private ImplementationTableModel dataModel;
	private FlexiTableElement tableEl;
	private Map<String, FormLink> keyToIndicatorLink;
	private FormLink showAllLink;
	
	private CloseableCalloutWindowController calloutCtrl;
	private CurriculumStructureCalloutController curriculumStructureCalloutCtrl;
	
	private final Formatter formatter;
	private final CurriculumElementImageMapper mapperThumbnail;
	private final MapperKey mapperThumbnailKey;
	private final CurriculumRef curriculum;
	private final CurriculumSecurityCallback secCallback;
	private final String curriculumBusinessPath;
	private SelectionValues figureValues;
	private String keyFigureKey;
	
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private MapperService mapperService;

	public ImplementationWidgetController(UserRequest ureq, WindowControl wControl, CurriculumRef curriculum, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CurriculumElementDetailsController.class, ureq.getLocale(), getTranslator()));
		this.curriculum = curriculum;
		this.secCallback = secCallback;
		formatter = Formatter.getInstance(getLocale());
		mapperThumbnail = CurriculumElementImageMapper.mapper210x140();
		mapperThumbnailKey = mapperService.register(null, CurriculumElementImageMapper.MAPPER_ID_210_140, mapperThumbnail);
		curriculumBusinessPath = "[CurriculumAdmin:0][Curriculums:0][Curriculum:" + curriculum.getKey() + "]";
		
		initForm(ureq);
	}

	@Override
	protected String getTitle() {
		return "<i class=\"o_icon o_icon_curriculum_implementations\"> </i> " + translate("curriculum.implementations");
	}

	@Override
	protected String createIndicators(FormLayoutContainer widgetCont) {
		indicatorsEl = IndicatorsFactory.createItem("indicators", widgetCont);
		
		keyToIndicatorLink = new LinkedHashMap<>();
		figureValues = new SelectionValues();
		createIndicator(widgetCont, "all", "all", "[Implementations:0][All:0]");
		createIndicator(widgetCont, "relevant", "relevant", "[Implementations:0][Relevant:0]");
		createIndicator(widgetCont, CurriculumElementStatus.preparation);
		createIndicator(widgetCont, CurriculumElementStatus.provisional);
		createIndicator(widgetCont, CurriculumElementStatus.confirmed);
		createIndicator(widgetCont, CurriculumElementStatus.cancelled);
		createIndicator(widgetCont, CurriculumElementStatus.finished);
		
		return indicatorsEl.getComponent().getComponentName();
	}
	
	private void createIndicator(FormLayoutContainer widgetCont, CurriculumElementStatus status) {
		String statusName = status.name();
		String businessPath = "[Implementations:0][" + statusName + ":0]";
		createIndicator(widgetCont, status.name(), "status." + statusName, businessPath);
	}
	
	private void createIndicator(FormLayoutContainer widgetCont, String name, String figureValueI18n, String businessPath) {
		FormLink link = IndicatorsFactory.createIndicatorFormLink(name, CMD_OPEN, "", "", widgetCont);
		setUrl(link, businessPath, curriculumBusinessPath + businessPath);
		keyToIndicatorLink.put(name, link);
		figureValues.add(SelectionValues.entry(name, translate(figureValueI18n)));
	}
	
	@Override
	protected TableWidgetConfigProvider getConfigProvider() {
		return this;
	}

	@Override
	public String getId() {
		return "implementation-widget-v1-" + curriculum.getKey();
	}

	@Override
	public SelectionValues getFigureValues() {
		return figureValues;
	}

	@Override
	public TableWidgetConfigPrefs getDefault() {
		TableWidgetConfigPrefs prefs = new TableWidgetConfigPrefs();
		prefs.setKeyFigureKey("relevant");
		Set<String> figureKeys = Set.of(
				CurriculumElementStatus.preparation.name(),
				CurriculumElementStatus.provisional.name(),
				CurriculumElementStatus.confirmed.name());
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
		
		dataModel = new ImplementationTableModel(tableColumnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 15, true, getTranslator(), widgetCont);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setShowSmallPageSize(false);
		
		tableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer rowVC = createVelocityContainer("implementation_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		tableEl.setCssDelegate(MaxHeightScrollableDelegate.DELEGATE);
		
		return tableEl.getComponent().getComponentName();
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(2);
		if (rowObject instanceof ImplementationRow imlementationRow) {
			if (imlementationRow.getTitleLink() != null) {
				cmps.add(imlementationRow.getTitleLink().getComponent());
			}
			if (imlementationRow.getStructureLink() != null) {
				cmps.add(imlementationRow.getStructureLink().getComponent());
			}
		}
		return cmps;
	}

	@Override
	protected String createShowAll(FormLayoutContainer widgetCont) {
		showAllLink = createShowAllLink(widgetCont);
		setUrl(showAllLink, "[Implementations:0][All:0]", curriculumBusinessPath + "[Implementations:0][All:0]");
		return showAllLink.getComponent().getComponentName();
	}

	private void reload() {
		List<CurriculumElement> curriculumElements = curriculumService.getCurriculumElementsByCurriculums(List.of(curriculum)).stream()
				.filter(element -> element.getParent() == null)
				.toList();
		
		updateIndicators(curriculumElements);
		updateTable(curriculumElements);
	}
	
	private void updateIndicators(List<CurriculumElement> curriculumElements) {
		Map<CurriculumElementStatus, Long> statusToCount = curriculumElements.stream()
				.collect(Collectors.groupingBy(CurriculumElement::getElementStatus, Collectors.counting()));
		
		updateStausIndicatorLink(CurriculumElementStatus.preparation, statusToCount);
		updateStausIndicatorLink(CurriculumElementStatus.provisional, statusToCount);
		updateStausIndicatorLink(CurriculumElementStatus.confirmed, statusToCount);
		updateStausIndicatorLink(CurriculumElementStatus.cancelled, statusToCount);
		updateStausIndicatorLink(CurriculumElementStatus.finished, statusToCount);
		
		long allCount = 0;
		long relevantCount = 0;
		for (CurriculumElementStatus elementStatus : CurriculumElementStatus.values()) {
			Long count = statusToCount.getOrDefault(elementStatus, Long.valueOf(0));
			allCount += count;
			if (RELEVANT_STATUS.contains(elementStatus)) {
				relevantCount += count;
			}
		}
		keyToIndicatorLink.get("all").setI18nKey(IndicatorsFactory.createLinkText(
				translate("all"),
				String.valueOf(allCount)));
		keyToIndicatorLink.get("relevant").setI18nKey(IndicatorsFactory.createLinkText(
				translate("relevant"),
				String.valueOf(relevantCount)));
	}
	
	private void updateStausIndicatorLink(CurriculumElementStatus status, Map<CurriculumElementStatus, Long> statusToCount) {
		String statusName = status.name();
		keyToIndicatorLink.get(statusName).setI18nKey(
				IndicatorsFactory.createLinkText(
						"<i class=\"o_icon o_curriculum_widget_icon o_icon_curriculum_status_" + statusName + "\"></i> " + translate("status." + statusName),
						String.valueOf(statusToCount.getOrDefault(status, Long.valueOf(0)))));
	}

	private void updateTable(List<CurriculumElement> curriculumElements) {
		Predicate<? super CurriculumElement> filterPredicate = getFilterPredicate();
		List<ImplementationRow> rows = curriculumElements.stream()
				.filter(filterPredicate)
				.sorted(Comparator.comparing(CurriculumElement::getBeginDate, Comparator.nullsLast(Date::compareTo)))
				.limit(tableEl.getPageSize())
				.map(this::toRow)
				.toList();

		final Map<Long,VFSThumbnailInfos> thumbnails = mapperThumbnail.getResourceableThumbnails(rows);
		rows.forEach(r -> appendThumbnail(r, thumbnails));
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private Predicate<CurriculumElement> getFilterPredicate() {
		if (StringHelper.containsNonWhitespace(keyFigureKey)) {
			if ("relevant".equals(keyFigureKey)) {
				return element -> RELEVANT_STATUS.contains(element.getElementStatus());
			}
			
			if (CurriculumElementStatus.isValueOf(keyFigureKey)) {
				CurriculumElementStatus status = CurriculumElementStatus.valueOf(keyFigureKey);
				return element -> status == element.getElementStatus();
			}
		}
		return element -> true;
	}
	
	private ImplementationRow toRow(CurriculumElement curriculumElement) {
		ImplementationRow row = new ImplementationRow();
		row.setKey(curriculumElement.getKey());
		row.setDisplayName(curriculumElement.getDisplayName());
		row.setExternalRef(curriculumElement.getIdentifier());
		row.setResourceableId(curriculumElement.getResource().getResourceableId());
		row.setResourceableTypeName(curriculumElement.getResource().getResourceableTypeName());
		
		StringOutput statusTarget = new StringOutput();
		CurriculumStatusCellRenderer.getStatus(statusTarget, "o_labeled_light", curriculumElement.getElementStatus(), getTranslator());
		row.setStatus(statusTarget.toString());
		row.setTranslatedTechnicalType(curriculumElement.getType().getDisplayName());
		
		String executionPeriod = "";
		if (curriculumElement.getBeginDate() != null) {
			executionPeriod = formatter.formatDate(curriculumElement.getBeginDate());
			if (curriculumElement.getEndDate() != null) {
				executionPeriod += " - " + formatter.formatDate(curriculumElement.getEndDate());
			}
		}
		row.setExecutionPeriod(executionPeriod);
		
		forgeLinks(row, curriculumElement);
		
		return row;
	}

	private void forgeLinks(ImplementationRow row, CurriculumElement curriculumElement) {
		String path = getBusinessPath(curriculumElement, "Overview");
		String pathFull = curriculumBusinessPath + path;
		
		FormLink titleLink = uifactory.addFormLink("title_" + row.getKey(), CMD_OPEN, "", tableEl, Link.NONTRANSLATED);
		titleLink.setEscapeMode(EscapeMode.html);
		titleLink.setI18nKey(curriculumElement.getDisplayName());
		titleLink.setElementCssClass("o_link_plain o_row_select");
		titleLink.setUserObject(path);
		setUrl(titleLink, path, pathFull);
		row.setTitleLink(titleLink);
		
		FormLink structureLink = null;
		CurriculumElementType type = curriculumElement.getType();
		if (type == null || !type.isSingleElement()) {
			structureLink = uifactory.addFormLink("structure_" + row.getKey(), CMD_STRUCTURE, "", tableEl, Link.NONTRANSLATED);
			structureLink.setIconLeftCSS("o_icon o_icon-lg o_icon_structure");
			structureLink.setTitle(translate("action.structure"));
			structureLink.setUserObject(curriculumElement);
			row.setStructureLink(structureLink);
		}
	}
	
	private String getBusinessPath(CurriculumElement curriculumElement, String tab) {
		return "[Implementations:0][CurriculumElement:" + curriculumElement.getKey() + "]["+ tab + ":0]";
	}
	
	private void setUrl(FormLink link, String businessPath, String businessPathFull) {
		link.setUserObject(businessPath);
		String url = BusinessControlFactory.getInstance().getRelativeURLFromBusinessPathString(businessPathFull);
		link.setUrl(url);
	}
	
	private void appendThumbnail(ImplementationRow row, Map<Long, VFSThumbnailInfos> thumbnails) {
		String imageUrl = mapperThumbnail.getThumbnailURL(mapperThumbnailKey.getUrl(), row.getKey(), thumbnails);
		row.setThumbnailRelPath(imageUrl);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		 if (curriculumStructureCalloutCtrl == source) {
			calloutCtrl.deactivate();
			cleanUp();
			if (event instanceof SelectCurriculumElementRowEvent scee) {
				doOpenStructure(ureq, scee.getCurriculumElement());
			}
		} else if (calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(curriculumStructureCalloutCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		curriculumStructureCalloutCtrl = null;
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
			} else if (CMD_STRUCTURE.equals(link.getCmd())) {
				if (link.getUserObject() instanceof CurriculumElement curriculumElement) {
					doOpenStructureOverview(ureq, curriculumElement, link);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpen(UserRequest ureq, String businessPath) {
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
		fireEvent(ureq, new ActivateEvent(entries));
	}
	
	private void doOpenStructureOverview(UserRequest ureq, CurriculumElement curriculumElement, FormLink link) {
		curriculumStructureCalloutCtrl = new CurriculumStructureCalloutController(ureq, getWindowControl(),
				curriculumElement, null, false, secCallback);
		listenTo(curriculumStructureCalloutCtrl);
		
		CalloutSettings settings = new CalloutSettings(true, CalloutOrientation.bottom, true,  null);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				curriculumStructureCalloutCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "", settings);
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doOpenStructure(UserRequest ureq, CurriculumElement curriculumElement) {
		if(curriculumElement == null) return;
		
		String businessPath = getBusinessPath(curriculumElement, "Structure");
		doOpen(ureq, businessPath);
	}

}
