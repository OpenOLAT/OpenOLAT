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
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.ui.AbstractSessionSelectionController;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.quality.analysis.ui.AnalysisController.ToolComponents;

/**
 * 
 * Initial date: 13 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AnalysisSessionSelectionController extends AbstractSessionSelectionController {

	private final TooledStackedPanel stackPanel;
	private final ToolComponents toolComponents;
	
	private EvaluationFormExecutionController theExecutionCtrl;

	public AnalysisSessionSelectionController(UserRequest ureq, WindowControl wControl, Form form, DataStorage storage,
			SessionFilter filter, ReportHelper reportHelper, TooledStackedPanel stackPanel, ToolComponents toolComponents) {
		super(ureq, wControl, form, storage, filter, reportHelper, null);
		setTranslator(Util.createPackageTranslator(AbstractSessionSelectionController.class, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		this.stackPanel.addListener(this);
		this.toolComponents = toolComponents;
		initToolComponents();
		initForm(ureq);
	}

	@Override
	public void pushController(UserRequest ureq, String breadcrumbName, EvaluationFormExecutionController theExecutionCtrl) {
		this.theExecutionCtrl = theExecutionCtrl;
		listenTo(theExecutionCtrl);
		
		stackPanel.pushController(breadcrumbName, theExecutionCtrl);
		
		initSingleSessionToolComponents();
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == stackPanel) {
			if (event instanceof PopEvent) {
				PopEvent popEvent = (PopEvent)event;
				if (popEvent.getController() == theExecutionCtrl) {
					initToolComponents();
				}
			}
		}
		super.event(ureq, source, event);
	}

	private void initToolComponents() {
		toolComponents.setPrintVisibility(true);
		toolComponents.setPrintPopupVisibility(false);
		toolComponents.setPdfVisibility(true);
		toolComponents.setExportVisibility(true);
		toolComponents.setFilterVisibility(true);
	}

	private void initSingleSessionToolComponents() {
		toolComponents.setPrintVisibility(false);
		toolComponents.setPrintPopupVisibility(false);
		toolComponents.setPdfVisibility(false);
		toolComponents.setExportVisibility(false);
		toolComponents.setFilterVisibility(false);
	}

	@Override
	protected void doDispose() {
		if (stackPanel != null) {
			stackPanel.removeListener(this);
		}
		super.doDispose();
	}

}
