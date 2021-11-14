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
package org.olat.modules.quality.analysis.ui;

import java.util.ArrayList;
import java.util.Comparator;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.quality.analysis.AnalysisPresentation;
import org.olat.modules.quality.analysis.EvaluationFormView;
import org.olat.modules.quality.analysis.EvaluationFormViewSearchParams;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.olat.modules.quality.analysis.ui.AnalysisDataModel.AnalysisCols;
import org.olat.modules.quality.ui.security.MainSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 03.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AnalysisListController extends FormBasicController implements FlexiTableComponentDelegate, Activateable2 {

	private static final String ORES_TYPE_FORM = "form";
	private static final String ORES_TYPE_PRESENTATION = "presentation";
	private static final String CMD_OPEN = "open";
	private static final Comparator<? super EvaluationFormView> CREATED_DESC = 
			(f1, f2) -> f2.getLatestDataCollectionFinishedDate().compareTo(f1.getLatestDataCollectionFinishedDate());
	
	private final TooledStackedPanel stackPanel;
	private final MainSecurityCallback secCallback;
	private FlexiTableElement tableEl;
	private AnalysisDataModel dataModel;
	
	private AnalysisController analysisCtrl;
	
	private int counter = 0;
	private AnalysisPresentation presentation;

	@Autowired
	private QualityAnalysisService analysisService;
	
	public AnalysisListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			MainSecurityCallback secCallback) {
		super(ureq, wControl, "analysis_list");
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.secCallback = secCallback;
		initForm(ureq);
	}
	
	public static OLATResourceable getOlatResourceable(AnalysisPresentation presentation) {
		return presentation.getKey() != null
				? OresHelper.createOLATResourceableInstance(ORES_TYPE_PRESENTATION, presentation.getKey())
				: OresHelper.createOLATResourceableInstance(ORES_TYPE_FORM, presentation.getFormEntry().getKey());
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AnalysisCols.formTitle, CMD_OPEN));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AnalysisCols.formCreated));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AnalysisCols.soonest));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AnalysisCols.latest));
		DefaultFlexiColumnModel numDataCollectionsColumn = new DefaultFlexiColumnModel(AnalysisCols.numberDataCollections);
		numDataCollectionsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		numDataCollectionsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(numDataCollectionsColumn);;
		DefaultFlexiColumnModel numParticipationsColumn = new DefaultFlexiColumnModel(AnalysisCols.numberParticipations);
		numParticipationsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		numParticipationsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(numParticipationsColumn);
		
		dataModel = new AnalysisDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 8, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setElementCssClass("o_qual_ana_table");
		tableEl.setSearchEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "quality-analysis");
		tableEl.setEmptyTableMessageKey("analysis.table.empty");
		
		VelocityContainer row = createVelocityContainer("analysis_row");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
		tableEl.setCssDelegate(new AnalysisCssDelegate());
		
		loadModel();
	}
	
	private void loadModel() {
		EvaluationFormViewSearchParams searchParams = new EvaluationFormViewSearchParams();
		searchParams.setOrganisationRefs(secCallback.getViewAnalysisOrganisationRefs());
		List<EvaluationFormView> forms = analysisService.loadEvaluationForms(searchParams);
		forms.sort(CREATED_DESC);
		List<AnalysisRow> rows = new ArrayList<>(forms.size());
		for (EvaluationFormView form: forms) {
			AnalysisRow row = forgeRow(form);
			rows.add(row);
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private AnalysisRow forgeRow(EvaluationFormView form) {
		String openLinkId = "open_" + (++counter);
		FormLink openLink = uifactory.addFormLink(openLinkId, CMD_OPEN, "analysis.table.open", null, flc, Link.LINK);
		openLink.setElementCssClass("o_qual_ana_open_link");
		openLink.setIconRightCSS("o_icon o_icon_start");

		AnalysisRow row = new AnalysisRow(form, openLink);
		openLink.setUserObject(row);
		return row;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		AnalysisRow elRow = dataModel.getObject(row);
		List<Component> components = new ArrayList<>(1);
		if (elRow.getOpenLink() != null) {
			components.add(elRow.getOpenLink().getComponent());
		}
		return components;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if (ORES_TYPE_FORM.equalsIgnoreCase(type)) {
			Long formEntryKey = entries.get(0).getOLATResourceable().getResourceableId();
			AnalysisRow row = dataModel.getObjectByFormEntryKey(formEntryKey);
			if (row != null) {
				presentation = createPresentation(row);
				doOpenAnalysis(ureq);
			}
		} else if (ORES_TYPE_PRESENTATION.equalsIgnoreCase(type)) {
			Long presentationKey = entries.get(0).getOLATResourceable().getResourceableId();
			doOpenPresentation(ureq, presentationKey);
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (stackPanel == source && stackPanel.getLastController() == analysisCtrl && event instanceof PopEvent) {
			PopEvent popEvent = (PopEvent) event;
			if (!popEvent.isClose()) {
				stackPanel.popController(analysisCtrl);
				doOpenAnalysis(ureq);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl && event instanceof SelectionEvent) {
			SelectionEvent se = (SelectionEvent)event;
			String cmd = se.getCommand();
			AnalysisRow row = dataModel.getObject(se.getIndex());
			presentation = createPresentation(row);
			if (CMD_OPEN.equals(cmd)) {
				doOpenAnalysis(ureq);
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if(CMD_OPEN.equals(link.getCmd())) {
				AnalysisRow row = (AnalysisRow)link.getUserObject();
				presentation = createPresentation(row);
				doOpenAnalysis(ureq);
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}
	
	private AnalysisPresentation createPresentation(EvaluationFormView formView) {
		return analysisService.createPresentation(formView.getFormEntry(), secCallback.getViewAnalysisOrganisationRefs());
	}

	private void doOpenPresentation(UserRequest ureq, Long presentationKey) {
		presentation = analysisService.loadPresentationByKey(() -> presentationKey);
		if (presentation != null) {
			doOpenAnalysis(ureq);
		}
	}

	private void doOpenAnalysis(UserRequest ureq) {
		OLATResourceable ores = getOlatResourceable(presentation);
		WindowControl bwControl = addToHistory(ureq, ores, null);
		analysisCtrl = new AnalysisController(ureq, bwControl, secCallback, stackPanel, presentation);
		listenTo(analysisCtrl);
		String title = getBreadcrumbTitle(presentation);
		stackPanel.pushController(title, analysisCtrl);
		analysisCtrl.activate(ureq, null, null);
	}
	
	private String getBreadcrumbTitle(AnalysisPresentation presentation) {
		return presentation.getKey() != null
				? presentation.getName()
				: presentation.getFormEntry().getDisplayname();
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
        super.doDispose();
	}
	
	private static class AnalysisCssDelegate extends DefaultFlexiTableCssDelegate {
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_qual_ana_row";
		}
	}

}
