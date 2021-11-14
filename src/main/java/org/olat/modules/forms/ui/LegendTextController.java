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
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.ui.ReportHelper.Legend;
import org.olat.modules.forms.ui.model.LegendTextDataSource;
import org.olat.modules.forms.ui.model.SessionText;

/**
 * 
 * Initial date: 07.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LegendTextController extends BasicController {
	
	private final VelocityContainer mainVC;
	private final LegendTextDataSource dataSource;
	private final ReportHelper reportHelper;
	private final OWASPAntiSamyXSSFilter xssFilter = new OWASPAntiSamyXSSFilter();

	public LegendTextController(UserRequest ureq, WindowControl wControl, LegendTextDataSource dataSource,
			ReportHelper reportHelper) {
		super(ureq, wControl);
		this.dataSource = dataSource;
		this.reportHelper = reportHelper;
		
		mainVC = createVelocityContainer("legend_text");
		List<LegendTextWrapper> wrappers = createWrappers();
		mainVC.contextPut("wrappers", wrappers);
		putInitialPanel(mainVC);
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
		content = xssFilter.filter(content);
		content = Formatter.stripTabsAndReturns(content).toString();
		return new LegendTextWrapper(legend.getName(), legend.getColor(), content);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
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
