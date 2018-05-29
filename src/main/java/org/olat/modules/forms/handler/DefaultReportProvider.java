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
package org.olat.modules.forms.handler;

import java.util.HashMap;
import java.util.Map;

import org.olat.modules.forms.model.xml.FileUpload;
import org.olat.modules.forms.model.xml.HTMLRaw;
import org.olat.modules.forms.model.xml.MultipleChoice;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.SessionInformations;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.model.xml.Spacer;
import org.olat.modules.forms.model.xml.TextInput;
import org.olat.modules.forms.model.xml.Title;
import org.olat.modules.portfolio.ui.editor.PageElement;

/**
 * 
 * Initial date: 04.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DefaultReportProvider implements EvaluationFormReportProvider {
	
	private final Map<String, EvaluationFormReportHandler> handlers = new HashMap<>();
	
	public DefaultReportProvider() {
		handlers.put(Title.TYPE, new TitleHandler());
		handlers.put(Spacer.TYPE, new SpacerHandler());
		handlers.put(HTMLRaw.TYPE, new HTMLRawHandler());
		handlers.put(Rubric.TYPE, new RubricBarChartsHandler());
		handlers.put(TextInput.TYPE, new TextInputLegendTextHandler());
		handlers.put(FileUpload.TYPE, new FileUploadListingHandler());
		handlers.put(SingleChoice.TYPE, new SingleChoiceBarChartHandler());
		handlers.put(MultipleChoice.TYPE, new MultipleChoiceBarChartHandler());
		handlers.put(SessionInformations.TYPE, new SessionInformationsStatisticHandler());
	}

	@Override
	public EvaluationFormReportHandler getReportHandler(PageElement element) {
		return handlers.get(element.getType());
	}
	
	public void put(String elementType, EvaluationFormReportHandler handler) {
		handlers.put(elementType, handler);
	}

}
