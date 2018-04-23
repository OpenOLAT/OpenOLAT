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

import java.util.ArrayList;
import java.util.List;

import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.portfolio.ui.editor.PageElement;
import org.olat.modules.portfolio.ui.editor.PageProvider;

/**
 * 
 * Initial date: 20.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AllHandlerPageProvider implements PageProvider {
	
	private final Form form;
	private final List<EvaluationFormElementHandler> handlers = new ArrayList<>();
	
	public AllHandlerPageProvider(Form form) {
		this.form = form;
		
		handlers.add(new TitleHandler());
		handlers.add(new SpacerHandler());
		handlers.add(new HTMLRawHandler());
		handlers.add(new RubricHandler(true));
		handlers.add(new TextInputHandler());
		handlers.add(new FileUploadHandler());
		handlers.add(new SingleChoiceHandler());
		handlers.add(new MultipleChoiceHandler());
	}

	@Override
	public List<? extends PageElement> getElements() {
		return form.getElements();
	}

	@Override
	public List<EvaluationFormElementHandler> getAvailableHandlers() {
		return handlers;
	}
}
