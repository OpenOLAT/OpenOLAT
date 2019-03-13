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
package org.olat.modules.forms.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.Form;

/**
 * 
 * Initial date: 23.05.2018<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormSessionSelectionController extends AbstractSessionSelectionController implements BreadcrumbPanelAware {

	private BreadcrumbPanel stackPanel;

	public EvaluationFormSessionSelectionController(UserRequest ureq, WindowControl wControl, Form form, DataStorage storage,
			SessionFilter filter, ReportHelper reportHelper, Component formHeader) {
		super(ureq, wControl, form, storage, filter, reportHelper, formHeader);
		initForm(ureq);
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
	}

	@Override
	public void pushController(UserRequest ureq, String breadcrumbName, EvaluationFormExecutionController theExecutionCtrl) {
		stackPanel.pushController(breadcrumbName, theExecutionCtrl);
	}

}
