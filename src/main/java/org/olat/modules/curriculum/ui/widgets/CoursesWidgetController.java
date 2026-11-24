/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.widgets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.vfs.model.VFSThumbnailInfos;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateVariant;
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
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dashboard.DashboardUIFactory;
import org.olat.core.gui.control.generic.dashboard.MaxHeightScrollableDelegate;
import org.olat.core.gui.control.generic.dashboard.TableWidgetController;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.ConfirmInstantiateTemplateController;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.repository.ui.author.AccessRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoursesWidgetController extends TableWidgetController implements FlexiTableComponentDelegate {
	
	private static final String CMD_OPEN = "open";
	private static final String CMD_INSTANTIATE = "instantiate";
	private static final String RESOURCES_PATH = "[Resources:0]";
	
	private FigureCardsItem figuresEl;
	private FormLink coursesLink;
	private FormLink detailsLink;
	private FlexiTableElement entriesTableEl;
	private CoursesWidgetDataModel entriesTableModel;

	private final boolean singleCourse;
	private final String businessPath;
	private final MapperKey mapperThumbnailKey;
	private final CurriculumElement curriculumElement;
	private final CurriculumSecurityCallback secCallback;
	private final RepositoryEntryImageMapper mapperThumbnail;
	
	private CloseableModalController cmc;
	private ConfirmInstantiateTemplateController confirmInstantiateCtrl;

	@Autowired
	private MapperService mapperService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private NodeAccessService nodeAccessService;
	
	public CoursesWidgetController(UserRequest ureq, WindowControl wControl, CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CurriculumComposerController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		
		mapperThumbnail = RepositoryEntryImageMapper.mapper210x140();
		mapperThumbnailKey = mapperService.register(null, RepositoryEntryImageMapper.MAPPER_ID_210_140, mapperThumbnail);

		this.secCallback = secCallback;
		this.curriculumElement = curriculumElement;
		singleCourse = curriculumElement.isSingleCourseImplementation();
		businessPath = "[CurriculumAdmin:0][Implementations:0][CurriculumElement:" + curriculumElement.getKey() + "]";

		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected String getTitle() {
		return "<i class=\"o_icon o_CourseModule_icon\"> </i> " + translate("tab.resources");
	}

	@Override
	protected String createTopArea(FormLayoutContainer widgetCont) {
		figuresEl = FigureCardsFactory.createItem("figures", widgetCont);
		coursesLink = FigureCardsFactory.createCardLink("courses", "courses", widgetCont);
		setUrl(coursesLink, RESOURCES_PATH, businessPath + RESOURCES_PATH);
		figuresEl.setCards(List.of(coursesLink));
		return figuresEl.getComponent().getComponentName();
	}
	
	@Override
	protected String createTable(FormLayoutContainer widgetCont) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		entriesTableModel = new CoursesWidgetDataModel(columnsModel);
		entriesTableEl = uifactory.addTableElement(getWindowControl(), "entriesTable", entriesTableModel, 25, false, getTranslator(), widgetCont);
		entriesTableEl.setCustomizeColumns(false);
		entriesTableEl.setNumOfRowsEnabled(false);
		entriesTableEl.setRendererType(FlexiTableRendererType.custom);
		entriesTableEl.setCssDelegate(MaxHeightScrollableDelegate.DELEGATE);
		VelocityContainer rowVC = createVelocityContainer("course_widget_row");
		rowVC.setDomReplacementWrapperRequired(false);
		entriesTableEl.setRowRenderer(rowVC, this);
		entriesTableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withVariant(EmptyStateVariant.auto)
				.withIconCss("o_CourseModule_icon")
				.withMessageTranslated(translate(singleCourse? "widget.courses.empty.single": "widget.courses.empty"))
				.build());
		return entriesTableEl.getComponent().getComponentName();
	}
	
	@Override
	protected List<String> createFooterButtons(FormLayoutContainer widgetCont) {
		detailsLink = DashboardUIFactory.createDetailsLink(widgetCont);
		setUrl(detailsLink, RESOURCES_PATH, businessPath + RESOURCES_PATH);
		return List.of(detailsLink.getComponent().getComponentName());
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		if(rowObject instanceof CourseWidgetRow entryRow) {
			List<Component> links = new ArrayList<>(2);
			links.add(entryRow.openLink().getComponent());
			if(entryRow.instantiateTemplateLink() != null) {
				links.add(entryRow.instantiateTemplateLink().getComponent());
			}
			return links;
		}
		return List.of();
	}
	
	public void loadModel() {
		List<RepositoryEntry> repositoryEntries = curriculumService.getRepositoryEntries(curriculumElement);
		List<RepositoryEntry> repositoryTemplates = singleCourse && repositoryEntries.isEmpty()
				? curriculumService.getRepositoryTemplates(curriculumElement)
				: List.of();
		final int numOfEntries = repositoryEntries.size();
		
		Map<Long,VFSThumbnailInfos> thumbnails = mapperThumbnail.getRepositoryThumbnails(repositoryEntries);
		thumbnails.putAll(mapperThumbnail.getRepositoryThumbnails(repositoryTemplates));
		
		AccessRenderer renderer = new AccessRenderer(getLocale());
		List<CourseWidgetRow> rows = new ArrayList<>();
		for(RepositoryEntry entry:repositoryEntries) {
			rows.add(forgeRow(entry, false, thumbnails, renderer));
		}
		for(RepositoryEntry template:repositoryTemplates) {
			rows.add(forgeRow(template, true, thumbnails, renderer));
		}
		entriesTableModel.setObjects(rows);
		entriesTableEl.reset(true, true, true);
		
		updateFigure(numOfEntries);
	}
	
	private void updateFigure(int numOfEntries) {
		String num = String.valueOf(numOfEntries);
		String figure = singleCourse? num + "/1": num;
		String label = translate(singleCourse || numOfEntries == 1? "widget.courses.label.single": "widget.courses.label");
		coursesLink.setI18nKey(FigureCardsFactory.createCardText("o_CourseModule_icon", null, figure, label));
		if(singleCourse) {
			setFooterText(translate("widget.courses.footer.of.one", num));
		} else {
			setFooterText(translate(numOfEntries == 1? "widget.courses.footer.single": "widget.courses.footer", num));
		}
	}
	
	private CourseWidgetRow forgeRow(RepositoryEntry entry, boolean template, Map<Long,VFSThumbnailInfos> thumbnails, AccessRenderer renderer) {
		FormLink openLink = uifactory.addFormLink("open_" + entry.getKey(), CMD_OPEN, "", entriesTableEl, Link.NONTRANSLATED);
		openLink.setElementCssClass("o_link_plain o_dashboard_widget_row_link");
		openLink.setUrl(BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString("[RepositoryEntry:" + entry.getKey() + "]"));
		openLink.setUserObject(entry);
		
		FormLink instantiateLink = null;
		if(template && secCallback.canManageCurriculumElementResources(curriculumElement)) {
			instantiateLink = uifactory.addFormLink("instantiate_" + entry.getKey(), CMD_INSTANTIATE, "instantiate.template", entriesTableEl, Link.BUTTON);
			instantiateLink.setPrimary(true);
			instantiateLink.setAriaDialogOpener();
			instantiateLink.setUserObject(entry);
		}
		
		String status = template? null: renderer.renderEntryStatus(entry);
		String executionPeriod = template? null: getExecutionPeriod(entry);
		String thumbnailUrl = mapperThumbnail.getThumbnailURL(mapperThumbnailKey.getUrl(), entry, thumbnails);
		CourseWidgetRow row = new CourseWidgetRow(entry, template, openLink, instantiateLink, thumbnailUrl, getTechnicalType(entry), status, executionPeriod);
		
		VelocityContainer linkVC = createVelocityContainer("course_row_link");
		linkVC.setDomReplacementWrapperRequired(false);
		linkVC.contextPut("row", row);
		openLink.setInnerComponent(linkVC);
		return row;
	}
	
	private String getExecutionPeriod(RepositoryEntry entry) {
		RepositoryEntryLifecycle lifecycle = entry.getLifecycle();
		Date begin = lifecycle == null? null: lifecycle.getValidFrom();
		Date end = lifecycle == null? null: lifecycle.getValidTo();
		return RepositoyUIFactory.formatExecutionPeriod(getTranslator(), begin, end, Formatter.getInstance(getLocale())::formatDateTwoDigitYear);
	}
	
	private String getTechnicalType(RepositoryEntry entry) {
		if(!StringHelper.containsNonWhitespace(entry.getTechnicalType())) {
			return null;
		}
		NodeAccessType nodeAccessType = NodeAccessType.of(entry.getTechnicalType());
		return ConditionNodeAccessProvider.TYPE.equals(nodeAccessType.getType())
				? translate("CourseModule")
				: nodeAccessService.getNodeAccessTypeName(nodeAccessType, getLocale());
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmInstantiateCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmInstantiateCtrl);
		removeAsListenerAndDispose(cmc);
		confirmInstantiateCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink link) {
			if(CMD_OPEN.equals(link.getCmd()) && link.getUserObject() instanceof RepositoryEntry entry) {
				doOpen(ureq, entry);
			} else if(CMD_INSTANTIATE.equals(link.getCmd()) && link.getUserObject() instanceof RepositoryEntry template) {
				doInstantiateTemplate(ureq, template);
			} else if(link.getUserObject() instanceof String path) {
				doActivate(ureq, path);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doActivate(UserRequest ureq, String path) {
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(path);
		fireEvent(ureq, new ActivateEvent(entries));
	}
	
	private void doOpen(UserRequest ureq, RepositoryEntry entry) {
		NewControllerFactory.getInstance().launch("[RepositoryEntry:" + entry.getKey() + "]", ureq, getWindowControl());
	}
	
	private void doInstantiateTemplate(UserRequest ureq, RepositoryEntry template) {
		confirmInstantiateCtrl = new ConfirmInstantiateTemplateController(ureq, getWindowControl(),
				curriculumElement, template);
		listenTo(confirmInstantiateCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmInstantiateCtrl.getInitialComponent(),
				true, translate("instantiate.template.title"));
		listenTo(cmc);
		cmc.activate();
	}
}
