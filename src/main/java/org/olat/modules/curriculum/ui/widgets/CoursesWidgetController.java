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
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.vfs.model.VFSThumbnailInfos;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.EmptyPanelItem;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.ConfirmInstantiateTemplateController;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.widgets.CoursesWidgetDataModel.EntriesCols;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.repository.ui.author.AccessRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoursesWidgetController extends FormBasicController implements FlexiTableComponentDelegate {
	
	private static final String CMD_OPEN = "open";
	private static final String CMD_INSTANTIATE = "instantiate";
	
	private EmptyPanelItem emptyList;
	private FlexiTableElement entriesTableEl;
	private CoursesWidgetDataModel entriesTableModel;

	private final MapperKey mapperThumbnailKey;
	private final CurriculumElement curriculumElement;
	private final RepositoryEntryImageMapper mapperThumbnail;
	
	private CloseableModalController cmc;
	private ConfirmInstantiateTemplateController confirmInstantiateCtrl;

	@Autowired
	private MapperService mapperService;
	@Autowired
	private CurriculumService curriculumService;
	
	public CoursesWidgetController(UserRequest ureq, WindowControl wControl, CurriculumElement curriculumElement) {
		super(ureq, wControl, "courses_widget", Util.createPackageTranslator(CurriculumComposerController.class, ureq.getLocale()));
		mapperThumbnail = RepositoryEntryImageMapper.mapper210x140();
		mapperThumbnailKey = mapperService.register(null, RepositoryEntryImageMapper.MAPPER_ID_210_140, mapperThumbnail);
		
		this.curriculumElement = curriculumElement;
		
		initForm(ureq);
		loadModel();
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		if(rowObject instanceof CourseWidgetRow entryRow) {
			List<Component> links = new ArrayList<>(2);
			if(entryRow.getOpenLink() != null) {
				links.add(entryRow.getOpenLink().getComponent());
			}
			if(entryRow.getInstantiateTemplateLink() != null) {
				links.add(entryRow.getInstantiateTemplateLink().getComponent());
			}
			return links;
		}
		return List.of();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		emptyList = uifactory.addEmptyPanel("course.empty", null, formLayout);
		emptyList.setTitle(translate("curriculum.no.course.assigned.title"));
		emptyList.setIconCssClass("o_icon o_icon-lg o_CourseModule_icon");

		initFormTable(formLayout);
	}
	
	private void initFormTable(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, EntriesCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EntriesCols.displayName, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EntriesCols.externalRef));
		
		entriesTableModel = new CoursesWidgetDataModel(columnsModel);
		entriesTableEl = uifactory.addTableElement(getWindowControl(), "entriesTable", entriesTableModel, 25, false, getTranslator(), formLayout);
		entriesTableEl.setCustomizeColumns(false);
		entriesTableEl.setNumOfRowsEnabled(false);
		entriesTableEl.setSelection(true, false, false);
		entriesTableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		entriesTableEl.setRendererType(FlexiTableRendererType.custom);
		entriesTableEl.setNumOfRowsEnabled(false);
		entriesTableEl.setCssDelegate(new EntriesDelegate());
		
		VelocityContainer row = new VelocityContainer(null, "vc_row1", velocity_root + "/entry_1.html",
				getTranslator(), this);
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		entriesTableEl.setRowRenderer(row, this);
	}
	
	public void loadModel() {
		List<RepositoryEntry> repositoryEntries = curriculumService.getRepositoryEntries(curriculumElement);
		List<RepositoryEntry> repositoryTemplates = curriculumService.getRepositoryTemplates(curriculumElement);
		final int numOfEntries = repositoryEntries.size();
		
		Map<Long,VFSThumbnailInfos> thumbnails = mapperThumbnail.getRepositoryThumbnails(repositoryEntries);
		thumbnails.putAll(mapperThumbnail.getRepositoryThumbnails(repositoryTemplates));
		
		AccessRenderer renderer = new AccessRenderer(getLocale());
		List<CourseWidgetRow> rows = new ArrayList<>();
		for(RepositoryEntry entry:repositoryEntries) {
			rows.add(forgeRow(entry, false, numOfEntries, thumbnails, renderer));
		}
		for(RepositoryEntry template:repositoryTemplates) {
			rows.add(forgeRow(template, true, numOfEntries, thumbnails, renderer));
		}
		entriesTableModel.setObjects(rows);
		entriesTableEl.reset(true, true, true);
		
		boolean empty = rows.isEmpty();
		entriesTableEl.setVisible(!empty);
		emptyList.setVisible(empty);
	}
	
	private CourseWidgetRow forgeRow(RepositoryEntry entry, boolean template, int numOfEntries, Map<Long,VFSThumbnailInfos> thumbnails, AccessRenderer renderer) {
		String displayName = StringHelper.escapeHtml(entry.getDisplayname());
		FormLink openLink = uifactory.addFormLink("open_" + entry.getKey(), "open", displayName, null, flc, Link.NONTRANSLATED);
		final String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString("[RepositoryEntry:" + entry.getKey() + "]");
		openLink.setUrl(url);
		
		FormLink instantiateLink = null;
		if(template && numOfEntries == 0) {
			instantiateLink = uifactory.addFormLink("instantiate_" + entry.getKey(), "instantiate", "instantiate.template", null, flc, Link.BUTTON);
			instantiateLink.setElementCssClass("btn btn-primary");
			instantiateLink.setUserObject(entry);
		}
		
		String status = renderer.renderEntryStatus(entry);
		String thumbnailUrl = mapperThumbnail.getThumbnailURL(mapperThumbnailKey.getUrl(), entry, thumbnails);
		CourseWidgetRow row = new CourseWidgetRow(entry, template, openLink, instantiateLink, url, thumbnailUrl, status);
		openLink.setUserObject(row);
		return row;
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
		removeAsListenerAndDispose(cmc);
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink link) {
			if(CMD_OPEN.equals(link.getCmd())
					&& link.getUserObject() instanceof CourseWidgetRow row) {
				doOpen(ureq, row);
			} else if(CMD_INSTANTIATE.equals(link.getCmd())
					&& link.getUserObject() instanceof RepositoryEntry template) {
				doInstantiateTemplate(ureq, template);
			}
		} else if(source instanceof FormLayoutContainer) {
			String entryKey = ureq.getParameter("select_entry");
			if(StringHelper.isLong(entryKey)) {
				doOpen(ureq, Long.valueOf(entryKey));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpen(UserRequest ureq, CourseWidgetRow row) {
		doOpen(ureq, row.getKey());
	}
	
	private void doOpen(UserRequest ureq, Long entryKey) {
		String businessPath = "[RepositoryEntry:" + entryKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
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
	
	private static class EntriesDelegate implements FlexiTableCssDelegate {

		@Override
		public String getWrapperCssClass(FlexiTableRendererType type) {
			return null;
		}

		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			return "o_cards";
		}

		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return null;
		}
	}
}
