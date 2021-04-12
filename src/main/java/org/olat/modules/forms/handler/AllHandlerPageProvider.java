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

import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageProvider;
import org.olat.modules.forms.model.xml.Form;

/**
 * 
 * Initial date: 20.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AllHandlerPageProvider implements PageProvider {
	
	private final Form form;
	private final List<EvaluationFormElementHandler> handlers = new ArrayList<>();
	
	public AllHandlerPageProvider(Form form, DataStorage storage) {
		this.form = form;
		
		handlers.add(new TitleHandler());
		handlers.add(new SpacerHandler());
		handlers.add(new HTMLRawHandler());
		handlers.add(new HTMLParagraphHandler());
		handlers.add(new ImageHandler(storage));
		handlers.add(new TableHandler());
		handlers.add(new RubricHandler(false, false));
		handlers.add(new TextInputHandler(false));
		handlers.add(new FileUploadHandler(false));
		handlers.add(new SingleChoiceHandler(false));
		handlers.add(new MultipleChoiceHandler(false));
		handlers.add(new DisclaimerHandler(false));
		handlers.add(new SessionInformationsHandler(false));
		handlers.add(new ContainerHandler(null));
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
