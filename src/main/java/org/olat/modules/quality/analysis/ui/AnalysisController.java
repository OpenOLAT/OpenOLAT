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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.prefs.Preferences;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.ui.ReportSegment;
import org.olat.modules.forms.ui.ReportSegmentEvent;
import org.olat.modules.qpool.ui.QuestionItemDetailsController;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.AvailableAttributes;
import org.olat.modules.quality.analysis.EvaluationFormView;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AnalysisController extends BasicController implements TooledController {
	
	enum Presentation {REPORT, HEAT_MAP};
	
	private static final String GUIPREF_KEY_SHOW_FILTERS = "show.filters";

	private Link showFilterLink;
	private Link hideFilterLink;

	private VelocityContainer mainVC;
	private Controller filterCtrl;
	private FilterableController presentationCtrl;
	private final QualitySecurityCallback secCallback;
	private final TooledStackedPanel stackPanel;
	
	private final EvaluationFormView formView;
	private final Form form;
	private final AvailableAttributes availableAttributes;
	private AnalysisSearchParameter searchParams;
	private Boolean showFilters;
	private ReportSegment currentSegment;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private QualityAnalysisService analysisService;

	protected AnalysisController(UserRequest ureq, WindowControl wControl, QualitySecurityCallback secCallback,
			TooledStackedPanel stackPanel, EvaluationFormView formView) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.formView = formView;
		RepositoryEntry formEntry = repositoryService.loadByKey(formView.getFormEntryKey());
		this.form = evaluationFormManager.loadForm(formEntry);
		mainVC = createVelocityContainer("analysis");
		putInitialPanel(mainVC);
		
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		showFilters = (Boolean) guiPrefs.get(QuestionItemDetailsController.class, GUIPREF_KEY_SHOW_FILTERS);
		
		searchParams = new AnalysisSearchParameter();
		searchParams.setFormEntryRef(() -> formView.getFormEntryKey());
		availableAttributes = analysisService.getAvailableAttributes(searchParams);
		filterCtrl= new FilterController(ureq, wControl, form, searchParams, availableAttributes);
		listenTo(filterCtrl);
		mainVC.put("filter", filterCtrl.getInitialComponent());
	}

	@Override
	public void initTools() {
		showFilterLink = LinkFactory.createToolLink("filter.show", translate("filter.show"), this);
		showFilterLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_ana_show_filter");
		hideFilterLink = LinkFactory.createToolLink("filter.hide", translate("filter.hide"), this);
		hideFilterLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_ana_hide_filter");
		if (showFilters == null || showFilters) {
			doShowFilter();
		} else {
			doHideFilter();
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == showFilterLink) {
			doShowFilter(ureq);
		} else if (source == hideFilterLink) {
			doHideFilter(ureq);
		} else if (stackPanel == source && stackPanel.getLastController() == this && event instanceof PopEvent) {
			PopEvent popEvent = (PopEvent) event;
			if (popEvent.isClose()) {
				stackPanel.popController(this);
			} else {
				setPresentation(ureq, Presentation.REPORT);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == filterCtrl && event instanceof AnalysisFilterEvent) {
			AnalysisFilterEvent filterEvent = (AnalysisFilterEvent) event;
			searchParams = filterEvent.getSearchParams();
			presentationCtrl.onFilter(ureq, searchParams);
		} else if (source == presentationCtrl && event instanceof ReportSegmentEvent) {
			// Save current segment between analysis segment changes
			ReportSegmentEvent rsEvent = (ReportSegmentEvent) event;
			currentSegment = rsEvent.getSegment();
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void doDispose() {
		if (stackPanel != null) {
			stackPanel.removeListener(this);
		}
	}

	public void setPresentation(UserRequest ureq, Presentation presentation) {
		removeAsListenerAndDispose(presentationCtrl);
		presentationCtrl = null;
		
		switch (presentation) {
		case REPORT:
			presentationCtrl = new AnalysisReportController(ureq, getWindowControl(), form, formView.getFormTitle(),
					currentSegment);
			break;
		case HEAT_MAP:
			presentationCtrl = new HeatMapController(ureq, getWindowControl(), form, availableAttributes);
			break;
		default:
			presentationCtrl = new AnalysisReportController(ureq, getWindowControl(), form, formView.getFormTitle(),
					currentSegment);
			break;
		}
		listenTo(presentationCtrl);
		presentationCtrl.onFilter(ureq, searchParams);
		mainVC.put("presentation", presentationCtrl.getInitialComponent());
		mainVC.setDirty(true);
	}
	
	private void doShowFilter(UserRequest ureq) {
		doShowFilter();
		doPutFiltersSwitch(ureq, Boolean.TRUE);
	}

	private void doShowFilter() {
		stackPanel.addTool(hideFilterLink, Align.right);
		stackPanel.removeTool(showFilterLink);
		mainVC.contextPut("filterSwitch", Boolean.TRUE);
	}
	
	private void doHideFilter(UserRequest ureq) {
		doHideFilter();
		doPutFiltersSwitch(ureq, Boolean.FALSE);
	}

	private void doHideFilter() {
		stackPanel.addTool(showFilterLink, Align.right);
		stackPanel.removeTool(hideFilterLink);
		mainVC.contextPut("filterSwitch", Boolean.FALSE);
	}
	
	private void doPutFiltersSwitch(UserRequest ureq, Boolean show) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.putAndSave(QuestionItemDetailsController.class, GUIPREF_KEY_SHOW_FILTERS, show);
		}
	}

}
