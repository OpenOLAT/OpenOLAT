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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.handler.DefaultReportProvider;
import org.olat.modules.forms.handler.EvaluationFormReportHandler;
import org.olat.modules.forms.handler.EvaluationFormReportProvider;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;

/**
 * 
 * Initial date: 04.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormReportController extends FormBasicController {
	
	private final Form form;
	private final List<? extends EvaluationFormSessionRef> sessions;
	private final EvaluationFormReportProvider provider;
	private final ReportHelper reportHelper;
	
	private final Component header;
	private final List<ReportFragment> fragments = new ArrayList<>();
	
	public EvaluationFormReportController(UserRequest ureq, WindowControl wControl, Form form,
			List<? extends EvaluationFormSessionRef> sessions) {
			this(ureq, wControl, form, sessions, null, null, null);
	}
	
	public EvaluationFormReportController(UserRequest ureq, WindowControl wControl, Form form,
			List<? extends EvaluationFormSessionRef> sessions, EvaluationFormReportProvider provider) {
		this(ureq, wControl, form, sessions, provider, null, null);
	}
	
	public EvaluationFormReportController(UserRequest ureq, WindowControl wControl, Form form,
			List<? extends EvaluationFormSessionRef> sessions, EvaluationFormReportProvider provider, ReportHelper reportHelper) {
		this(ureq, wControl, form, sessions, provider, reportHelper, null);
	}

	public EvaluationFormReportController(UserRequest ureq, WindowControl wControl, Form form,
			List<? extends EvaluationFormSessionRef> sessions, EvaluationFormReportProvider provider, ReportHelper reportHelper,
			Component header) {
		super(ureq, wControl, "report");
		this.form = form;
		this.sessions = sessions;
		this.provider = provider != null? provider: new DefaultReportProvider();
		this.reportHelper = reportHelper != null? reportHelper: ReportHelper.builder(getLocale()).build();
		this.header = header;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (header != null) {
			flc.put("header", header);
		}
		for (AbstractElement element: form.getElements()) {
			EvaluationFormReportHandler reportHandler = provider.getReportHandler(element);
			if (reportHandler != null) {
				Component component = reportHandler.getReportComponent(ureq, getWindowControl(), element, sessions,
						reportHelper);
				String cmpId = "cpt-" + CodeHelper.getRAMUniqueID();
				flc.put(cmpId, component);
				fragments.add(new ReportFragment(reportHandler.getType(), cmpId));
			}
		}
		flc.contextPut("fragments", fragments);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public static final class ReportFragment {

		private final String type;
		private final String componentName;
		
		public ReportFragment(String type, String componentName) {
			this.type = type;
			this.componentName = componentName;
		}
		
		public String getCssClass() {
			return "o_ed_".concat(type);
		}
		
		public String getComponentName() {
			return componentName;
		}
	}

}
