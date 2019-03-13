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
import org.olat.core.gui.components.stack.TooledStackedPanel;
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
	private final FilterController filterCtrl;
	private final ToolComponents toolComponents;
	
	private Analysis2ColController colsCtrl;
	
	private Boolean showFilter;

	public AnalysisSessionSelectionController(UserRequest ureq, WindowControl wControl, Form form, DataStorage storage,
			SessionFilter filter, ReportHelper reportHelper, TooledStackedPanel stackPanel, FilterController filterCtrl,
			ToolComponents toolComponents) {
		super(ureq, wControl, form, storage, filter, reportHelper, null);
		setTranslator(Util.createPackageTranslator(AbstractSessionSelectionController.class, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		this.filterCtrl = filterCtrl;
		this.toolComponents = toolComponents;
		initForm(ureq);
	}

	@Override
	public void pushController(UserRequest ureq, String breadcrumbName, EvaluationFormExecutionController theExecutionCtrl) {
		filterCtrl.setReadOnly(true);
		colsCtrl = new Analysis2ColController(ureq, getWindowControl(), theExecutionCtrl, filterCtrl);
		listenTo(colsCtrl);

		stackPanel.pushController(breadcrumbName, colsCtrl);
		
		colsCtrl.setShowFilter(showFilter);
		toolComponents.setPrintVisibility(false);
		toolComponents.setPrintPopupVisibility(false);
		toolComponents.setPdfVisibility(false);
		toolComponents.setExportVisibility(false);
	}

	public void setShowFilter(Boolean show) {
		this.showFilter = show;
		if (colsCtrl != null) {
			colsCtrl.setShowFilter(show);
		}	
	}

}
