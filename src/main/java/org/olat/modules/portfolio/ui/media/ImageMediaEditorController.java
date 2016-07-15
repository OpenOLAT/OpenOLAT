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
package org.olat.modules.portfolio.ui.media;

import java.io.File;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.image.ImageFormItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.MediaPart;
import org.olat.modules.portfolio.ui.editor.PageElementEditorController;
import org.olat.modules.portfolio.ui.editor.event.ChangePartEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImageMediaEditorController extends FormBasicController implements PageElementEditorController {
	
	private static final String[] alignmentKeys = new String[]{ "right", "left", "center" };
	
	private SingleSelection alignmentEl;
	
	private MediaPart mediaPart;
	private boolean editMode;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public ImageMediaEditorController(UserRequest ureq, WindowControl wControl, MediaPart mediaPart) {
		super(ureq, wControl, "editor_image");
		this.mediaPart = mediaPart;
		initForm(ureq);
	}
	
	@Override
	public boolean isEditMode() {
		return editMode;
	}

	@Override
	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
		alignmentEl.setVisible(editMode);
		flc.getFormItemComponent().contextPut("editMode", new Boolean(editMode));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Media media = mediaPart.getMedia();
		File mediaDir = new File(FolderConfig.getCanonicalRoot(), media.getStoragePath());
		File mediaFile = new File(mediaDir, media.getRootFilename());
		ImageFormItem imageEl = new ImageFormItem(ureq.getUserSession(), "image");
		imageEl.setMedia(mediaFile);
		formLayout.add("image", imageEl);
		
		alignmentEl = uifactory.addDropdownSingleselect("alignment", null, formLayout, alignmentKeys, alignmentKeys, null);
		alignmentEl.addActionListener(FormEvent.ONCHANGE);
		if(StringHelper.containsNonWhitespace(mediaPart.getLayoutOptions())) {
			for(String alignmentKey:alignmentKeys) {
				if(mediaPart.getLayoutOptions().contains(alignmentKey)) {
					alignmentEl.select(alignmentKey, true);
				}
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(alignmentEl == source) {
			String alignment = alignmentEl.getSelectedKey();
			mediaPart.setLayoutOptions(alignment);
			mediaPart = portfolioService.updatePart(mediaPart);
			fireEvent(ureq, new ChangePartEvent(mediaPart));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
}
