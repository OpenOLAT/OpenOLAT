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

import java.util.Locale;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.CloneElementHandler;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementRenderingHints;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.model.ContainerElement;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.ui.ContainerEditorController;
import org.olat.modules.ceditor.ui.PageRunComponent;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.Container;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.model.EvaluationFormComponentElement;
import org.olat.modules.forms.ui.model.EvaluationFormComponentReportElement;
import org.olat.modules.forms.ui.model.EvaluationFormExecutionElement;
import org.olat.modules.forms.ui.model.EvaluationFormReportElement;
import org.olat.modules.forms.ui.model.ExecutionIdentity;

/**
 * 
 * Initial date: 17 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContainerHandler implements EvaluationFormElementHandler, PageElementStore<ContainerElement>,
		SimpleAddPageElementHandler, CloneElementHandler, EvaluationFormReportHandler {

	private final Controller ruleLinkController;

	public ContainerHandler(Controller ruleLinkController) {
		this.ruleLinkController = ruleLinkController;
	}

	@Override
	public String getType() {
		return "formcontainer";
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_container";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.layout;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, PageElementRenderingHints options) {
		// rendering is done by the page component
		Component dummyCmp = new Panel("");
		return new PageRunComponent(dummyCmp);
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof ContainerElement) {
			return new ContainerEditorController(ureq, wControl, (ContainerElement)element, this, ruleLinkController);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		Container container = new Container();
		container.setId(UUID.randomUUID().toString());
		return container;
	}

	@Override
	public PageElement clonePageElement(PageElement element) {
		if (element instanceof Container) {
			Container container = (Container)element;
			Container clone = new Container();
			clone.setId(UUID.randomUUID().toString());
			ContainerSettings containerSettings = container.getContainerSettings();
			// We do not clone the elements of the container. They are cloned in the controller.
			containerSettings.getColumns().forEach(column -> column.getElementIds().clear());
			String settingsXml = ContentEditorXStream.toXml(containerSettings);
			clone.setLayoutOptions(settingsXml);
			return clone;
		}
		return null;
	}

	@Override
	public ContainerElement savePageElement(ContainerElement element) {
		return element;
	}

	@Override
	public EvaluationFormExecutionElement getExecutionElement(UserRequest ureq, WindowControl wControl, Form rootForm, PageElement element, ExecutionIdentity executionIdentity) {
		PageRunElement runElement = getContent(ureq, wControl, element, null);
		if (runElement != null) {
			return new EvaluationFormComponentElement(runElement);
		}
		return null;
	}
	
	@Override
	public EvaluationFormReportElement getReportElement(UserRequest ureq, WindowControl windowControl,
			PageElement element, SessionFilter filter, ReportHelper reportHelper) {
		return new EvaluationFormComponentReportElement(new Panel(""));
	}

}
