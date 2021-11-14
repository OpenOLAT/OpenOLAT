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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.forms.Limit;
import org.olat.modules.forms.ui.model.LegendTextDataSource;
import org.olat.modules.forms.ui.model.TextInputLegendTextDataSource;

/**
 * 
 * Initial date: 10.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TextInputLegendTextController extends BasicController {
	
	private final VelocityContainer mainVC;
	private Link downloadLink;
	
	private Controller legendTextCtrl;
	
	private final LegendTextDataSource dataSource;
	private final ReportHelper reportHelper;

	public TextInputLegendTextController(UserRequest ureq, WindowControl wControl, TextInputLegendTextDataSource dataSource,
			ReportHelper reportHelper) {
		super(ureq, wControl);
		this.dataSource = dataSource;
		this.reportHelper = reportHelper;
		
		mainVC = createVelocityContainer("text_input_legend_text");

		Long showCount = dataSource.getResponsesCount();
		if (showCount > 0) {
			Long showAll = dataSource.getResponsesCount(Limit.all());
			if (showAll > showCount) {
				mainVC.contextPut("downloadInfo", translate("textinput.download.info", new String[] { showCount.toString(), showAll.toString() }));
			}
			downloadLink = LinkFactory.createLink("textinput.download.link", mainVC, this);
		} else {
			mainVC.contextPut("noText", translate("textinput.no.text"));
		}
		
		legendTextCtrl = new LegendTextFixedController(ureq, wControl, dataSource, reportHelper);
		mainVC.put("legend.text", legendTextCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == downloadLink) {
			doExport(ureq);
		}
	}
	
	private void doExport(UserRequest ureq) {
		String name = "survey_text";
		LegendTextExcelExport export = new LegendTextExcelExport(dataSource, reportHelper, getTranslator(), name);
		ureq.getDispatchResult().setResultingMediaResource(export.createMediaResource());
	}

}
