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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.ui.ReportHelper.Legend;
import org.olat.modules.forms.ui.model.LegendTextDataSource;
import org.olat.modules.forms.ui.model.SessionText;

/**
 * 
 * Initial date: 09.11.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LegendTextFixedController extends FormBasicController {

	private final LegendTextDataSource dataSource;
	private final ReportHelper reportHelper;

	public LegendTextFixedController(UserRequest ureq, WindowControl wControl, LegendTextDataSource dataSource,
			ReportHelper reportHelper) {
		super(ureq, wControl, "legend_text_fixed");
		this.dataSource = dataSource;
		this.reportHelper = reportHelper;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<LegendTextWrapper> wrappers = createWrappers();
		flc.contextPut("wrappers", wrappers);
	}
	
	private List<LegendTextWrapper> createWrappers() {
		List<LegendTextWrapper> wrappers = new ArrayList<>();
		List<SessionText> responses = dataSource.getResponses();
		for (SessionText response: responses) {
			LegendTextWrapper wrapper = createWrapper(response);
			wrappers.add(wrapper);
		}
		return wrappers;
	}
	
	private LegendTextWrapper createWrapper(SessionText response) {
		EvaluationFormSession session = response.getSession();
		Legend legend = reportHelper.getLegend(session);
		String content = response.getText();
		String name = "textinput_" + CodeHelper.getRAMUniqueID();
		TextAreaElement contentEl = uifactory.addTextAreaElement(name, null, 56000, -1, 72, false, true, content, flc);
		contentEl.setEnabled(false);
		return new LegendTextWrapper(legend.getName(), legend.getColor(), name);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public final static class LegendTextWrapper {
		
		private final String name;
		private final String color;
		private final String content;
		
		public LegendTextWrapper(String name, String color, String content) {
			this.name = name;
			this.color = color;
			this.content = content;
		}
		
		public String getName() {
			return name;
		}

		public String getColor() {
			return color;
		}

		public String getContent() {
			return content;
		}
	}

}
