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

import java.io.IOException;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.modules.ceditor.CloneElementHandler;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.ceditor.InteractiveAddPageElementHandler;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.model.ImageElement;
import org.olat.modules.ceditor.model.StandardMediaRenderingHints;
import org.olat.modules.ceditor.model.StoredData;
import org.olat.modules.ceditor.ui.ImageInspectorController;
import org.olat.modules.ceditor.ui.ImageRunController;
import org.olat.modules.ceditor.ui.PageRunComponent;
import org.olat.modules.ceditor.ui.PageRunControllerElement;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.FileStoredData;
import org.olat.modules.forms.model.xml.Image;
import org.olat.modules.forms.ui.ImageUploadController;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.model.EvaluationFormComponentElement;
import org.olat.modules.forms.ui.model.EvaluationFormControllerReportElement;
import org.olat.modules.forms.ui.model.EvaluationFormExecutionElement;
import org.olat.modules.forms.ui.model.EvaluationFormReportElement;
import org.olat.modules.forms.ui.model.ExecutionIdentity;

/**
 * 
 * Initial date: 21 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImageHandler implements EvaluationFormElementHandler, PageElementStore<ImageElement>,
		InteractiveAddPageElementHandler, CloneElementHandler, EvaluationFormReportHandler {

	private static final Logger log = Tracing.createLoggerFor(ImageHandler.class);
	
	private final DataStorage dataStorage;
	
	public ImageHandler(DataStorage dataStorage) {
		this.dataStorage = dataStorage;
	}

	@Override
	public String getType() {
		return "formimage";
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_image";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.content;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element,
			RenderingHints options) {
		if(element instanceof ImageElement imageElement) {
			Controller ctrl = new ImageRunController(ureq, wControl, dataStorage, imageElement, options);
			return new PageRunControllerElement(ctrl);
		}
		return new PageRunComponent(new Panel("empty"));
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof ImageElement imageElement) {
			return new ImageRunController(ureq, wControl, dataStorage, imageElement, new StandardMediaRenderingHints(false));
		}
		return null;
	}
	
	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof ImageElement imageElement) {
			return new ImageInspectorController(ureq, wControl, imageElement, this, "inspector.image");
		}
		return null;
	}
	
	@Override
	public EvaluationFormReportElement getReportElement(UserRequest ureq, WindowControl windowControl,
			PageElement element, SessionFilter filter, ReportHelper reportHelper) {
		if (element instanceof ImageElement imageElement) {
			Controller ctrl = new ImageRunController(ureq, windowControl, dataStorage, imageElement, new StandardMediaRenderingHints(false));
			return new EvaluationFormControllerReportElement(ctrl);
		}
		return null;
	}
	
	@Override
	public PageElementAddController getAddPageElementController(UserRequest ureq, WindowControl wControl, AddSettings settings) {
		return new ImageUploadController(ureq, wControl, this, dataStorage);
	}

	@Override
	public PageElement clonePageElement(PageElement element) {
		if (element instanceof Image image) {
			Image clone = new Image();
			clone.setId(UUID.randomUUID().toString());
			clone.setContent(image.getContent());
			clone.setLayoutOptions(image.getLayoutOptions());
			try {
				StoredData clonedStoreData = new FileStoredData();
				clonedStoreData = dataStorage.copy(image.getStoredData(), clonedStoreData);
				clone.setStoredData(clonedStoreData);
				return clone;
			} catch (IOException e) {
				log.error("", e);
			}
		}
		return null;
	}

	@Override
	public ImageElement savePageElement(ImageElement element) {
		return element;
	}

	@Override
	public EvaluationFormExecutionElement getExecutionElement(UserRequest ureq, WindowControl wControl, Form rootForm,
			PageElement element, ExecutionIdentity executionIdentity) {
		return new EvaluationFormComponentElement(getContent(ureq, wControl, element, new StandardMediaRenderingHints(false)));
	}
	
}
