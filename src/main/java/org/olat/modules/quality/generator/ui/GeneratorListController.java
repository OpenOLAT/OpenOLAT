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
package org.olat.modules.quality.generator.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Organisation;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.ui.BooleanCSSCellRenderer;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.modules.quality.generator.QualityGeneratorSearchParams;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.QualityGeneratorView;
import org.olat.modules.quality.generator.ui.GeneratorDataModel.GeneratorCols;
import org.olat.modules.quality.ui.security.MainSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GeneratorListController extends FormBasicController implements TooledController, Activateable2 {
	
	private static final String CMD_EDIT = "edit";
	private static final Comparator<? super QualityGeneratorView> CREATION_DATE_DESC = 
			(g1, g2) -> g2.getCreationDate().compareTo(g1.getCreationDate());

	private final TooledStackedPanel stackPanel;
	private Link createGeneratorLink;
	private FlexiTableElement tableEl;
	private GeneratorDataModel dataModel;
	
	private CloseableModalController cmc;
	private GeneratorCreationController creationCtrl;
	private GeneratorController generatorCtrl;
	private GeneratorDeleteConfirmationController deleteConfirmationCtrl;
	
	private final MainSecurityCallback secCallback;
	
	@Autowired
	private QualityGeneratorService generatorService;
	@Autowired
	private QualityService qualityService;

	public GeneratorListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			MainSecurityCallback secCallback) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.secCallback = secCallback;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		FlexiCellRenderer enabledRenderer = new BooleanCSSCellRenderer(getTranslator(),
				"o_icon o_icon-lg o_icon_qual_gen_enabled", "o_icon o_icon-lg o_icon_qual_gen_disabled",
				"generator.enabled.hover", "generator.disabled.hover");
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GeneratorCols.enabled, enabledRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GeneratorCols.title, CMD_EDIT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GeneratorCols.providerName));
		DefaultFlexiColumnModel numDataCollectionsColumn = new DefaultFlexiColumnModel(GeneratorCols.numberDataCollections);
		numDataCollectionsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		numDataCollectionsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(numDataCollectionsColumn);
		
		dataModel = new GeneratorDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "quality-generator");
		tableEl.setEmptyTableMessageKey("generator.empty.table");
		loadModel();
	}

	private void loadModel() {
		QualityGeneratorSearchParams searchParams = new QualityGeneratorSearchParams();
		searchParams.setOrganisationRefs(secCallback.getViewGeneratorOrganisationRefs());
		List<QualityGeneratorView> generators = generatorService.loadGenerators(searchParams);
		generators.sort(CREATION_DATE_DESC);
		List<GeneratorRow> rows = new ArrayList<>(generators.size());
		for (QualityGeneratorView generator: generators) {
			String providerName = generatorService.getProviderDisplayName(generator, getLocale());
			GeneratorRow row = new GeneratorRow(generator, providerName);
			rows.add(row);
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	public void initTools() {
		stackPanel.removeAllTools();
		if (secCallback.canCreateGenerators()) {
			createGeneratorLink = LinkFactory.createToolLink("generator.create", translate("generator.create"), this);
			createGeneratorLink.setIconLeftCSS("o_icon o_icon-lg o_icon_qual_gen_create");
			stackPanel.addTool(createGeneratorLink, Align.left);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if (QualityGenerator.RESOURCEABLE_TYPE_NAME.equals(type)) {
			Long key = entry.getOLATResourceable().getResourceableId();
			GeneratorRow row = dataModel.getObjectByKey(key);
			if (row == null) {
				loadModel();
				row = dataModel.getObjectByKey(key);
				if (row != null) {
					doEditGenerator(ureq, row.getGeneratorRef());
					int index = dataModel.getObjects().indexOf(row);
					if (index >= 1 && tableEl.getPageSize() > 1) {
						int page = index / tableEl.getPageSize();
						tableEl.setPage(page);
					}
				} else {
					tableEl.reset();
					showInfo("generator.forbidden");
				}
			} else {
				doEditGenerator(ureq, row.getGeneratorRef());
			}
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl && event instanceof SelectionEvent) {
			SelectionEvent se = (SelectionEvent)event;
			String cmd = se.getCommand();
			GeneratorRow row = dataModel.getObject(se.getIndex());
			if (CMD_EDIT.equals(cmd)) {
				doEditGenerator(ureq, row.getGeneratorRef());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (createGeneratorLink == source) {
			doSelectGeneratorProvider(ureq);
		} else if (stackPanel == source && event instanceof PopEvent && stackPanel.getLastController() == this) {
			initTools();
			loadModel();
		} 
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == creationCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				doCreateGenerator(ureq, creationCtrl.getProviderType(), creationCtrl.getTitle());
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == generatorCtrl && event instanceof GeneratorEvent) {
			GeneratorEvent gEvent = (GeneratorEvent) event;
			GeneratorEvent.Action action = gEvent.getAction();
			if (GeneratorEvent.Action.DELETE.equals(action)) {
				QualityGenerator generator = gEvent.getGenerator();
				doConfirmDeleteGenerator(ureq, generator);
			}
		} else if (source == deleteConfirmationCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				QualityGenerator generator = deleteConfirmationCtrl.getGenerator();
				doDeleteGenerator(generator);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(creationCtrl);
		removeAsListenerAndDispose(cmc);
		deleteConfirmationCtrl = null;
		creationCtrl = null;
		cmc = null;
	}

	private void doSelectGeneratorProvider(UserRequest ureq) {
		creationCtrl = new GeneratorCreationController(ureq, getWindowControl());
		listenTo(creationCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), creationCtrl.getInitialComponent(),
				true, translate("generator.create.title"));
		cmc.activate();
	}

	private void doCreateGenerator(UserRequest ureq, String providerType, String title) {
		List<Organisation> organisations = qualityService.getDefaultOrganisations(getIdentity());
		QualityGenerator generator = generatorService.createGenerator(providerType, organisations);
		if (StringHelper.containsNonWhitespace(title)) {
			generator.setTitle(title);
			generator = generatorService.updateGenerator(generator);
		}
		doEditGenerator(ureq, generator);
	}
	
	private void doEditGenerator(UserRequest ureq, QualityGeneratorRef generatorRef) {
		QualityGenerator generator = generatorService.loadGenerator(generatorRef);
		doEditGenerator(ureq, generator);
	}

	private void doEditGenerator(UserRequest ureq, QualityGenerator generator) {
		WindowControl bwControl = addToHistory(ureq, generator, null);
		generatorCtrl = new GeneratorController(ureq, bwControl, stackPanel, generator);
		listenTo(generatorCtrl);
		String title = generator.getTitle();
		String formattedTitle = StringHelper.containsNonWhitespace(title)
				? Formatter.truncate(title, 50)
				: translate("generator.title.empty");
		stackPanel.pushController(formattedTitle, generatorCtrl);
		generatorCtrl.activate(ureq, null, null);
	}
	
	private void doConfirmDeleteGenerator(UserRequest ureq, QualityGenerator generator) {
		deleteConfirmationCtrl = new GeneratorDeleteConfirmationController(ureq, getWindowControl(), generator);
		listenTo(deleteConfirmationCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				deleteConfirmationCtrl.getInitialComponent(), true, translate("generator.delete.confirm.title"));
		cmc.activate();
		listenTo(cmc);
	}

	private void doDeleteGenerator(QualityGenerator generator) {
		long numberDataCollections = generatorService.getNumberOfDataCollections(generator);
		if (numberDataCollections == 0) {
			generatorService.deleteGenerator(generator);
			stackPanel.popUpToController(this);
			loadModel();
			initTools();
		} else {
			showInfo("generator.delete.has.data.collections");
			generatorCtrl.initTools();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		if (stackPanel != null) {
			stackPanel.removeListener(this);
		}
	}

}
