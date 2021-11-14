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
package org.olat.core.commons.services.doceditor.ui;

import java.util.List;
import java.util.Optional;

import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfig;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorConfigs.Config;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Mar 2019<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocEditorController extends BasicController implements Activateable2 {

	private static final String PROPERTY_CATEGOTY = "doc.editor";

	private VelocityContainer mainVC;
	private Controller editorCtrl;

	private DocEditorConfigs configs;
	private Access access;

	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private PropertyManager propertyManager;
	private DataTransferConfirmationController dataTransferConfirmationCtrl;

	public DocEditorController(UserRequest ureq, WindowControl wControl, Access access, DocEditorConfigs configs) {
		super(ureq, wControl);
		this.configs = configs;
		this.access = access;
		
		mainVC = createVelocityContainer("editor_main");
		Config config = configs.getConfig(DocEditorConfig.TYPE);
		if (config instanceof DocEditorConfig) {
			DocEditorConfig docEditorConfig = (DocEditorConfig)config;
			mainVC.contextPut("cssClass", docEditorConfig.getCssClass());
		}
		
		Optional<DocEditor> editor = docEditorService.getEditor(access.getEditorType());
		if (editor.isPresent()) {
			doOpenEditor(ureq, editor.get());
		} 
		
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(editorCtrl instanceof Activateable2) {
			((Activateable2)editorCtrl).activate(ureq, entries, state);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == editorCtrl) {
			fireEvent(ureq, event);
		} else if (source == dataTransferConfirmationCtrl && Event.DONE_EVENT.equals(event)) {
			DocEditor editor = dataTransferConfirmationCtrl.getEditor();
			doDataTransferConfirmed(editor);
			doOpenEditor(ureq, editor);
		}
		super.event(ureq, source, event);
	}

	private void doOpenEditor(UserRequest ureq, DocEditor editor) {
		removeAsListenerAndDispose(dataTransferConfirmationCtrl);
		removeAsListenerAndDispose(editorCtrl);

		if (editorCtrl != null || dataTransferConfirmationCtrl != null) {
			mainVC.remove("editor");
		}

		if (isDataTransferConfirmed(editor)) {
			editorCtrl = editor.getRunController(ureq, getWindowControl(), getIdentity(), configs, access);
			listenTo(editorCtrl);
			mainVC.put("editor", editorCtrl.getInitialComponent());
		} else {
			dataTransferConfirmationCtrl = new DataTransferConfirmationController(ureq, getWindowControl(), editor);
			listenTo(dataTransferConfirmationCtrl);
			mainVC.put("editor", dataTransferConfirmationCtrl.getInitialComponent());
		}
	}

	private boolean isDataTransferConfirmed(DocEditor editor) {
		if (editor.isDataTransferConfirmationEnabled()) {
			Property property = findUserProperty(editor);
			if (property == null || isNotConfirmedYet(property)) {
				return false;
			}
		}
		return true;
	}

	private boolean isNotConfirmedYet(Property property) {
		return !Boolean.valueOf(property.getStringValue());
	}

	private void doDataTransferConfirmed(DocEditor editor) {
		Property property = findUserProperty(editor);
		if (property == null) {
			property = propertyManager.createUserPropertyInstance(getIdentity(), PROPERTY_CATEGOTY,
					getDataTransferPropertyName(editor), null, null, Boolean.TRUE.toString(), null);
			propertyManager.saveProperty(property);
		}
	}

	private Property findUserProperty(DocEditor editor) {
		List<Property> properties = propertyManager.findProperties(getIdentity(), null, null, PROPERTY_CATEGOTY, getDataTransferPropertyName(editor));
		if (properties.isEmpty()) {
			return null;
		}
		
		// Clean up if property was accidently stored more than once.
		if (properties.size() > 1) {
			for (int i = 1; i < properties.size(); i++) {
				Property property = properties.get(i);
				propertyManager.deleteProperty(property);
			}
		}
		
		return properties.get(0);
	}

	private String getDataTransferPropertyName(DocEditor editor) {
		return editor.getType() + ".data.transfer.accepted";
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
