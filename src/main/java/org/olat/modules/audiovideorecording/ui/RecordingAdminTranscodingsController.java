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
package org.olat.modules.audiovideorecording.ui;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Initial date: 2022-10-25<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RecordingAdminTranscodingsController extends FormBasicController {
	private TranscodingTableModel tableModel;
	private FlexiTableElement tableEl;
	private FormLink refreshButton;

	@Autowired
	private VFSTranscodingService vfsTranscodingService;

	public RecordingAdminTranscodingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "transcodings");
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingTableCols.id));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingTableCols.fileName));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingTableCols.creationDate));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingTableCols.status));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TranscodingTableCols.action));
		tableModel = new TranscodingTableModel(columnModel, getLocale());

		tableEl = uifactory.addTableElement(getWindowControl(), "transcodings", tableModel, getTranslator(), flc);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);

		refreshButton = uifactory.addFormLink("button.refresh", flc, Link.BUTTON);
		refreshButton.setIconLeftCSS("o_icon o_icon_refresh o_icon-fw");
	}

	private void loadModel() {
		List<VFSMetadata> metadatas = vfsTranscodingService.getMetadatasWithUnresolvedTranscodingStatus();
		List<TranscodingTableRow> rows = metadatas.stream().map(this::mapToTableRow).collect(Collectors.toList());
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private TranscodingTableRow mapToTableRow(VFSMetadata metadata) {
		TranscodingTableRow row = new TranscodingTableRow(getTranslator(), metadata.getKey().toString(),
				metadata.getFilename(), metadata.getFileSize(), metadata.getCreationDate(),
				metadata.getTranscodingStatus());

		Integer transcodingStatus = metadata.getTranscodingStatus();
		if (transcodingStatus == VFSMetadata.TRANSCODING_STATUS_ERROR ||
				transcodingStatus == VFSMetadata.TRANSCODING_STATUS_TIMEOUT) {
			FormLink retranscodeLink = uifactory.addFormLink("retranscode_" + metadata.getKey(),
					"retranscode", "button.retry", "button.retry", flc, Link.LINK);
			retranscodeLink.setUserObject(metadata);
			retranscodeLink.setIconLeftCSS("o_icon o_icon_refresh o_icon-fw");
			row.setRetranscodeLink(retranscodeLink);
		}

		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == refreshButton) {
			loadModel();
		} else if (source instanceof FormLink && ((FormLink) source).getCmd().equals("retranscode")) {
			FormLink link = (FormLink) source;
			VFSMetadata metadata = (VFSMetadata) link.getUserObject();
			vfsTranscodingService.setStatus(metadata, VFSMetadata.TRANSCODING_STATUS_WAITING);
			loadModel();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
	}
}
