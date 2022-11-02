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
package org.olat.core.commons.modules.bc.commands;

import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.modules.bc.components.ListRenderer;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.video.ui.VideoAudioPlayerController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;

import java.util.List;

/**
 * Initial date: 2022-11-02<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CmdViewAudioVideo extends BasicController implements FolderCommand {
	private CloseableModalController cmc;
	private VideoAudioPlayerController videoAudioPlayerController;
	private int status = FolderCommandStatus.STATUS_SUCCESS;
	private VFSItem currentItem;

	protected CmdViewAudioVideo(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
	}

	@Override
	public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator translator) {
		String pos = ureq.getParameter(ListRenderer.PARAM_VIEW_AUDIO_VIDEO);
		if (!StringHelper.containsNonWhitespace(pos)) {
			status = FolderCommandStatus.STATUS_FAILED;
			getWindowControl().setError(translator.translate("failed"));
			return null;
		}

		status = FolderCommandHelper.sanityCheck(wControl, folderComponent);
		if (status == FolderCommandStatus.STATUS_SUCCESS) {
			int index = Integer.parseInt(pos);
			List<VFSItem> children = folderComponent.getCurrentContainerChildren();
			if (index >= 0 && index < children.size()) {
				currentItem = folderComponent.getCurrentContainerChildren().get(index);
				status = FolderCommandHelper.sanityCheck2(wControl, folderComponent, currentItem);
			} else {
				status = FolderCommandStatus.STATUS_FAILED;
				getWindowControl().setError(translator.translate("failed"));
				return null;
			}
		}
		if (status == FolderCommandStatus.STATUS_FAILED) {
			return null;
		}

		status = FolderCommandHelper.fileEditSanityCheck(currentItem);
		if (status == FolderCommandStatus.STATUS_FAILED) {
			logWarn("VFSItem is not a file and can't be viewed: " + folderComponent.getCurrentContainerPath() + "/" + currentItem.getName(),null);
			getWindowControl().setError(translator.translate("failed"));
			return null;
		}

		if (!(currentItem instanceof VFSLeaf)) {
			status = FolderCommandStatus.STATUS_FAILED;
			getWindowControl().setError(translator.translate("failed"));
			return null;
		}

		VFSLeaf vfsLeaf = (VFSLeaf) currentItem;
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.withMode(DocEditor.Mode.VIEW)
				.build(vfsLeaf);
		videoAudioPlayerController = new VideoAudioPlayerController(ureq, getWindowControl(), configs, null);
		String title = translator.translate("av.play");
		cmc = new CloseableModalController(getWindowControl(), "close",
				videoAudioPlayerController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();

		return null;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public boolean runsModal() {
		return true;
	}

	@Override
	public String getModalTitle() {
		return null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (cmc == source) {
			cleanUp();
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(videoAudioPlayerController);
		removeAsListenerAndDispose(cmc);
		videoAudioPlayerController = null;
		cmc = null;
	}
}
