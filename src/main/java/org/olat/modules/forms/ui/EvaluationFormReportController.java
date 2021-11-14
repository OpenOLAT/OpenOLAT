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
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.RubricsComparison;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.handler.DefaultReportProvider;
import org.olat.modules.forms.handler.EvaluationFormReportHandler;
import org.olat.modules.forms.handler.EvaluationFormReportProvider;
import org.olat.modules.forms.handler.RubricTableHandler;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.ui.model.EvaluationFormReportElement;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormReportController extends FormBasicController {
	
	private final Form form;
	private final SessionFilter filter;
	private final EvaluationFormReportProvider provider;
	private final ReportHelper reportHelper;
	
	private final Component header;
	private final List<ReportFragment> fragments = new ArrayList<>();
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	public EvaluationFormReportController(UserRequest ureq, WindowControl wControl, Form form, DataStorage storage, SessionFilter filter) {
			this(ureq, wControl, form, storage, filter, null, null, null);
	}
	
	public EvaluationFormReportController(UserRequest ureq, WindowControl wControl, Form form, DataStorage storage, SessionFilter filter,
			EvaluationFormReportProvider provider) {
		this(ureq, wControl, form, storage, filter, provider, null, null);
	}

	public EvaluationFormReportController(UserRequest ureq, WindowControl wControl, Form form, DataStorage storage, SessionFilter filter,
			EvaluationFormReportProvider provider, ReportHelper reportHelper) {
		this(ureq, wControl, form, storage, filter, provider, reportHelper, null);
	}

	public EvaluationFormReportController(UserRequest ureq, WindowControl wControl, Form form, DataStorage storage,
			SessionFilter filter, EvaluationFormReportProvider provider, ReportHelper reportHelper,
			Component header) {
		super(ureq, wControl, "report");
		this.form = form;
		this.filter = filter;
		this.provider = provider != null? provider: new DefaultReportProvider(storage);
		this.reportHelper = reportHelper != null? reportHelper: ReportHelper.builder(getLocale()).build();
		this.header = header;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<Rubric> rubrics = new ArrayList<>();
		if (header != null) {
			flc.put("header", header);
		}
		List<AbstractElement> elements = evaluationFormManager.getUncontainerizedElements(form);
		for (AbstractElement element: elements) {
			EvaluationFormReportHandler reportHandler = provider.getReportHandler(element);
			if (reportHandler != null) {
				EvaluationFormReportElement reportElement = reportHandler.getReportElement(ureq, getWindowControl(), element, filter,
						reportHelper);
				String cmpId = "cpt-" + CodeHelper.getRAMUniqueID();
				flc.put(cmpId, reportElement.getReportComponent());
				fragments.add(new ReportFragment(reportHandler.getType(), cmpId, reportElement));
				if (RubricTableHandler.TYPE.equals(reportHandler.getType()) && Rubric.TYPE.equals(element.getType())) {
					rubrics.add((Rubric) element);
				}
			}
		}
		flc.contextPut("fragments", fragments);
		
		// Would be better, if more generic
		Boolean rubricsIdentical = rubrics.size() > 1
				&& RubricsComparison.areIdentical(rubrics, RubricTableHandler.getAttributesColumnAlignemt());
		flc.contextPut("alignRubricsTables", rubricsIdentical);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		for (ReportFragment fragment: fragments) {
			fragment.dispose();
		}
        super.doDispose();
	}
	
	public static final class ReportFragment {

		private final String type;
		private final String componentName;
		private final EvaluationFormReportElement reportElement;
		
		public ReportFragment(String type, String componentName, EvaluationFormReportElement reportElement) {
			this.type = type;
			this.componentName = componentName;
			this.reportElement = reportElement;
		}
		
		public String getType() {
			return type;
		}

		public EvaluationFormReportElement getReportElement() {
			return reportElement;
		}

		public String getCssClass() {
			return "o_ed_".concat(type);
		}
		
		public String getComponentName() {
			return componentName;
		}
		
		public void dispose() {
			reportElement.dispose();
		}
	}

}
