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
package org.olat.admin.security;

import java.util.Arrays;

import org.olat.basesecurity.MediaServer;
import org.olat.basesecurity.MediaServerMode;
import org.olat.basesecurity.MediaServerModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-09-27<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class MediaServerController extends FormBasicController {
	private static final String CMD_EDIT = "edit";
	private static final String CMD_DELETE = "delete";

	private SingleSelection modeEl;
	private MultipleSelectionElement mediaServersEl;
	private FormLink addButton;
	private FlexiTableElement tableEl;
	private MediaServerModel tableModel;
	private EditMediaServerController editController;
	private CloseableModalController cmc;
	private DialogBoxController deleteDialogController;

	@Autowired
	private MediaServerModule mediaServerModule;

	public MediaServerController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);

		initForm(ureq);
		loadModel();
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer topCont = FormLayoutContainer.createDefaultFormLayout("topCont", getTranslator());
		formLayout.add(topCont);
		topCont.setFormTitle(translate("media.server.title"));

		FormLayoutContainer middleCont = FormLayoutContainer.createDefaultFormLayout("middleCont", getTranslator());
		middleCont.setElementCssClass("o_sel_media_server_form");
		formLayout.add(middleCont);

		SelectionValues modeKV = new SelectionValues();
		Arrays.stream(MediaServerMode.values())
				.map(MediaServerMode::name)
				.map(key -> new SelectionValues.SelectionValue(key, translate("media.server.mode." + key)))
				.forEach(modeKV::add);

		modeEl = uifactory.addRadiosVertical("media.server.mode", middleCont, modeKV.keys(), modeKV.values());
		modeEl.addActionListener(FormEvent.ONCHANGE);
		MediaServerMode mode = mediaServerModule.getMediaServerMode();
		if (mode != null) {
			modeEl.select(mode.name(), true);
		} else {
			modeEl.select(MediaServerMode.allowAll.name(), true);
		}

		SelectionValues mediaServersKV = new SelectionValues();
		mediaServersKV.add(SelectionValues.entry(MediaServerModule.YOUTUBE_KEY, MediaServerModule.YOUTUBE_NAME));
		mediaServersKV.add(SelectionValues.entry(MediaServerModule.VIMEO_KEY, MediaServerModule.VIMEO_NAME));
		mediaServersEl = uifactory.addCheckboxesVertical("media.servers", middleCont, mediaServersKV.keys(),
				mediaServersKV.values(), 1);

		addButton = uifactory.addFormLink("media.servers.add", middleCont, Link.BUTTON);
		addButton.setElementCssClass("o_sel_add_domain");
		addButton.setLabel("media.servers.custom", null);
		addButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaServerModel.MediaServerCol.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaServerModel.MediaServerCol.domain));
		DefaultFlexiColumnModel editCol = new DefaultFlexiColumnModel(MediaServerModel.MediaServerCol.edit.i18nHeaderKey(),
				MediaServerModel.MediaServerCol.edit.ordinal(), CMD_EDIT,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate(MediaServerModel.MediaServerCol.edit.i18nHeaderKey()), CMD_EDIT), null));
		columnsModel.addFlexiColumnModel(editCol);
		DefaultFlexiColumnModel deleteCol = new DefaultFlexiColumnModel(MediaServerModel.MediaServerCol.delete.i18nHeaderKey(),
				MediaServerModel.MediaServerCol.delete.ordinal(), CMD_DELETE,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate(MediaServerModel.MediaServerCol.delete.i18nHeaderKey()), CMD_DELETE), null));
		columnsModel.addFlexiColumnModel(deleteCol);

		tableModel = new MediaServerModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20,
				false, getTranslator(), middleCont);
		tableEl.setCustomizeColumns(false);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createDefaultFormLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	private void loadModel() {
		tableModel.setObjects(mediaServerModule.getCustomMediaServers().stream().map(this::toRow).toList());
		tableEl.reset();
	}

	private MediaServerRow toRow(MediaServer mediaServer) {
		return new MediaServerRow(mediaServer.getId(), mediaServer.getName(), mediaServer.getDomain());
	}

	private void updateUI() {
		boolean mediaServersVisible = modeEl.isOneSelected() && MediaServerMode.configure.name().equals(modeEl.getSelectedKey());
		mediaServersEl.setVisible(mediaServersVisible);
		addButton.setVisible(mediaServersVisible);
		tableEl.setVisible(mediaServersVisible);
		if (mediaServersVisible) {
			mediaServersEl.select(MediaServerModule.YOUTUBE_KEY, mediaServerModule.isMediaServerEnabled(MediaServerModule.YOUTUBE_KEY));
			mediaServersEl.select(MediaServerModule.VIMEO_KEY, mediaServerModule.isMediaServerEnabled(MediaServerModule.VIMEO_KEY));
			tableEl.setVisible(tableModel.getRowCount() > 0);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (modeEl == source) {
			updateUI();
		} else if (addButton == source) {
			doAdd(ureq);
		} else if (tableEl == source) {
			if (event instanceof SelectionEvent selectionEvent) {
				String command = selectionEvent.getCommand();
				MediaServerRow row = tableModel.getObject(selectionEvent.getIndex());
				MediaServer mediaServer = mediaServerModule.getCustomMediaServers().stream()
						.filter(m -> row != null && row.id().equals(m.getId())).findFirst().orElse(null);
				if (CMD_EDIT.equals(command)) {
					doEdit(ureq, mediaServer);
				} else if (CMD_DELETE.equals(command)) {
					doConfirmDelete(ureq, mediaServer);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (editController == source) {
			if (event == Event.DONE_EVENT) {
				mediaServerModule.updateCustomMediaServer(editController.getMediaServer());
				loadModel();
				updateUI();

			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		} else if (deleteDialogController == source) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				if (deleteDialogController.getUserObject() instanceof MediaServer mediaServer) {
					mediaServerModule.deleteCustomMediaServer(mediaServer);
					loadModel();
					updateUI();
				}
			}
		}

		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(editController);
		removeAsListenerAndDispose(cmc);
		editController = null;
		cmc = null;
	}

	private void doEdit(UserRequest ureq, MediaServer mediaServer) {
		editController = new EditMediaServerController(ureq, getWindowControl(), mediaServer);
		listenTo(editController);

		String title = translate("media.server.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirmDelete(UserRequest ureq, MediaServer mediaServer) {
		if (mediaServer == null) {
			return;
		}
		String text = translate("media.server.delete.confirm", new String[] { mediaServer.getName() });
		deleteDialogController = activateYesNoDialog(ureq, translate("media.server.delete"), text, deleteDialogController);
		deleteDialogController.setUserObject(mediaServer);
	}

	private void doAdd(UserRequest ureq) {
		editController = new EditMediaServerController(ureq, getWindowControl(), null);
		listenTo(editController);

		String title = translate("media.servers.add");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (modeEl.isOneSelected()) {
			MediaServerMode mode = MediaServerMode.valueOf(modeEl.getSelectedKey());
			mediaServerModule.setMediaServerMode(mode);
		}
		if (MediaServerMode.configure.equals(mediaServerModule.getMediaServerMode())) {
			mediaServerModule.setMediaServerEnabled(MediaServerModule.YOUTUBE_KEY, mediaServersEl.isKeySelected(MediaServerModule.YOUTUBE_KEY));
			mediaServerModule.setMediaServerEnabled(MediaServerModule.VIMEO_KEY, mediaServersEl.isKeySelected(MediaServerModule.VIMEO_KEY));
		}
	}
}
