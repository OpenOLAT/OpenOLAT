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
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.EvaluationFormView;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AnalysisController extends BasicController {
	
	enum Presentation {REPORT, HEAT_MAP};

	private VelocityContainer mainVC;
	private Controller filterCtrl;
	private FilterableController presentationCtrl;
	private final QualitySecurityCallback secCallback;
	private final TooledStackedPanel stackPanel;
	
	private final EvaluationFormView formView;
	private final Form form;
	private AnalysisSearchParameter searchParams;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private RepositoryService repositoryService;
	
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
		
		searchParams = new AnalysisSearchParameter();
		searchParams.setFormEntryRef(() -> formView.getFormEntryKey());
		filterCtrl= new FilterController(ureq, wControl, searchParams);
		listenTo(filterCtrl);
		mainVC.put("filter", filterCtrl.getInitialComponent());
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (stackPanel == source && stackPanel.getLastController() == this && event instanceof PopEvent) {
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
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void doDispose() {
		removeAsListenerAndDispose(presentationCtrl);
		removeAsListenerAndDispose(filterCtrl);
		presentationCtrl = null;
		filterCtrl = null;
		if (stackPanel != null) {
			stackPanel.removeListener(this);
		}
	}

	public void setPresentation(UserRequest ureq, Presentation presentation) {
		removeAsListenerAndDispose(presentationCtrl);
		presentationCtrl = null;
		
		switch (presentation) {
			case REPORT:
				presentationCtrl = new AnalysisReportController(ureq, getWindowControl(), form);
				break;
			case HEAT_MAP:
				presentationCtrl = new HeatMapController(ureq, getWindowControl(), secCallback, stackPanel);
				break;
			default:
				presentationCtrl = new AnalysisReportController(ureq, getWindowControl(), form);
				break;
		}
		listenTo(presentationCtrl);
		presentationCtrl.onFilter(ureq, searchParams);
		mainVC.put("presentation", presentationCtrl.getInitialComponent());
		mainVC.setDirty(true);
	}

}
