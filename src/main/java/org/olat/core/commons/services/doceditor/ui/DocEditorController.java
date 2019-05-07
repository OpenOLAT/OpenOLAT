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

import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallback;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSLeaf;
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
public class DocEditorController extends BasicController {

	private static final String PROPERTY_CATEGOTY = "doc.editor";

	private VelocityContainer mainVC;
	private DocEditorConfigController configCtrl;
	private Controller editorCtrl;

	private final VFSLeaf vfsLeaf;
	private final DocEditorSecurityCallback secCallback;
	private final DocEditorConfigs configs;

	@Autowired
	private PropertyManager propertyManager;
	private DataTransferConfirmationController dataTransferConfirmationCtrl;

	public DocEditorController(UserRequest ureq, WindowControl wControl, VFSLeaf vfsLeaf,
			DocEditorSecurityCallback secCallback, DocEditorConfigs configs) {
		this(ureq, wControl, vfsLeaf, secCallback, configs, null);
	}

	public DocEditorController(UserRequest ureq, WindowControl wControl, VFSLeaf vfsLeaf,
			DocEditorSecurityCallback secCallback, DocEditorConfigs configs, String cssClass) {
		super(ureq, wControl);
		this.vfsLeaf = vfsLeaf;
		this.secCallback = secCallback;
		this.configs = configs;

		mainVC = createVelocityContainer("editor_main");
		mainVC.contextPut("cssClass", cssClass);

		configCtrl = new DocEditorConfigController(ureq, wControl, vfsLeaf, secCallback);
		listenTo(configCtrl);
		mainVC.put("config", configCtrl.getInitialComponent());
		configCtrl.activate(ureq, null, null);

		putInitialPanel(mainVC);
	}

	public VFSLeaf getVfsLeaf() {
		return vfsLeaf;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == configCtrl) {
			if (event instanceof DocEditorSelectionEvent) {
				DocEditorSelectionEvent esEvent = (DocEditorSelectionEvent) event;
				DocEditor editor = esEvent.getEditor();
				doOpenEditor(ureq, editor);
			} else if (event == Event.DONE_EVENT) {
				fireEvent(ureq, event);
			}
		} else if (source == editorCtrl) {
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
			editorCtrl = editor.getRunController(ureq, getWindowControl(), getIdentity(), vfsLeaf, secCallback,
					configs);
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
			Property property = propertyManager.findUserProperty(getIdentity(), PROPERTY_CATEGOTY,
					getDataTransferPropertyName(editor));
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
		Property property = propertyManager.createUserPropertyInstance(getIdentity(), PROPERTY_CATEGOTY,
				getDataTransferPropertyName(editor), null, null, Boolean.TRUE.toString(), null);
		propertyManager.saveProperty(property);
	}

	private String getDataTransferPropertyName(DocEditor editor) {
		return editor.getType() + ".data.transfer.accepted";
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
