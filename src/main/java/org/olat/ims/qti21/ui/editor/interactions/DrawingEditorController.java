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
package org.olat.ims.qti21.ui.editor.interactions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.interactions.DrawingAssessmentItemBuilder;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.ims.qti21.ui.components.FlowFormItem;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DrawingEditorController extends FormBasicController {
	
	private static final Set<String> mimeTypes = new HashSet<>();
	static {
		mimeTypes.add("image/gif");
		mimeTypes.add("image/jpg");
		mimeTypes.add("image/jpeg");
		mimeTypes.add("image/png");
	}
	
	private TextElement titleEl;
	private RichTextElement textEl;
	private SingleSelection resizeEl;
	private FileElement backgroundEl;
	
	private final File itemFile;
	private final File rootDirectory;
	private final VFSContainer rootContainer;
	
	private File backgroundImage;
	private File initialBackgroundImage;
	
	private final boolean readOnly;
	private final boolean restrictedEdit;
	private final String mapperUri;
	private final DrawingAssessmentItemBuilder itemBuilder;

	@Autowired
	private ImageService imageService;
	
	public DrawingEditorController(UserRequest ureq, WindowControl wControl, DrawingAssessmentItemBuilder itemBuilder,
			File rootDirectory, VFSContainer rootContainer, File itemFile, boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemFile = itemFile;
		this.itemBuilder = itemBuilder;
		this.rootDirectory = rootDirectory;
		this.rootContainer = rootContainer;
		this.readOnly = readOnly;
		this.restrictedEdit = restrictedEdit;
		
		mapperUri = registerCacheableMapper(null, "DrawingEditorController::" + CodeHelper.getRAMUniqueID(),
				new ResourcesMapper(itemFile.toURI(), rootDirectory));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("manual_user/tests/Configure_test_questions/");
		
		titleEl = uifactory.addTextElement("title", "form.imd.title", -1, itemBuilder.getTitle(), formLayout);
		titleEl.setElementCssClass("o_sel_assessment_item_title");
		titleEl.setMandatory(true);
		titleEl.setEnabled(!readOnly);
		
		String relativePath = rootDirectory.toPath().relativize(itemFile.toPath().getParent()).toString();
		VFSContainer itemContainer = (VFSContainer)rootContainer.resolve(relativePath);

		String description = itemBuilder.getQuestion();
		textEl = uifactory.addRichTextElementForQTI21("desc", "form.imd.descr", description, 12, -1, itemContainer,
				formLayout, ureq.getUserSession(), getWindowControl());
		textEl.setElementCssClass("o_sel_assessment_item_question");
		textEl.setEnabled(!readOnly);
		textEl.setVisible(!readOnly);
		if(readOnly) {
			FlowFormItem textReadOnlyEl = new FlowFormItem("descro", itemFile);
			textReadOnlyEl.setLabel("form.imd.descr", null);
			textReadOnlyEl.setBlocks(itemBuilder.getQuestionBlocks());
			textReadOnlyEl.setMapperUri(mapperUri);
			formLayout.add(textReadOnlyEl);
		}
		
		initialBackgroundImage = getCurrentBackground();
		backgroundEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "form.imd.background", "form.imd.background", formLayout);
		backgroundEl.setPreview(ureq.getUserSession(), true);
		backgroundEl.setEnabled(!restrictedEdit && !readOnly);
		if(initialBackgroundImage != null) {
			backgroundEl.setInitialFile(initialBackgroundImage);
		}
		backgroundEl.addActionListener(FormEvent.ONCHANGE);
		backgroundEl.setDeleteEnabled(true);
		backgroundEl.limitToMimeType(mimeTypes, "error.mimetype", new String[]{ mimeTypes.toString() });
		
		String[] resizeKeys = new String[] { "no" };
		String[] resizeValues = new String[] { translate("form.imd.background.resize.no") };
		resizeEl = uifactory.addRadiosHorizontal("form.imd.background.resize", formLayout, resizeKeys, resizeValues);
		resizeEl.setVisible(false);
		resizeEl.setEnabled(!readOnly);
		if(initialBackgroundImage != null) {
			Size size = imageService.getSize(new LocalFileImpl(initialBackgroundImage), null);
			optimizeResizeEl(size, false);
		}

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		buttonsContainer.setElementCssClass("o_sel_lob_save");
		buttonsContainer.setVisible(!readOnly);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
	}
	
	private File getCurrentBackground() {
		if(StringHelper.containsNonWhitespace(itemBuilder.getBackground())) {
			File itemDirectory = itemFile.getParentFile();
			Path backgroundPath = itemDirectory.toPath().resolve(itemBuilder.getBackground());
			if(Files.exists(backgroundPath)) {
				return backgroundPath.toFile();
			}
		}
		return null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		backgroundEl.clearError();
		if(backgroundImage == null && initialBackgroundImage == null) {
			backgroundEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else {
			List<ValidationStatus> status = new ArrayList<>();
			backgroundEl.validate(status);
			allOk &= status.isEmpty();
		}

		return allOk;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(backgroundEl == source) {
			//upload in itemDirectory;
			if(FileElementEvent.DELETE.equals(event.getCommand())) {
				if(backgroundEl.getUploadFile() != null && backgroundEl.getUploadFile() != backgroundEl.getInitialFile()) {
					backgroundEl.reset();
					if(initialBackgroundImage != null) {
						backgroundEl.setInitialFile(initialBackgroundImage);
						backgroundImage = null;
					} else {
						backgroundEl.setInitialFile(null);
						backgroundImage = null;
					}
				} else if(initialBackgroundImage != null) {
					initialBackgroundImage = null;
					backgroundEl.setInitialFile(null);
				}
				flc.setDirty(true);
			} else if (backgroundEl.isUploadSuccess()) {
				List<ValidationStatus> status = new ArrayList<>();
				backgroundEl.validate(status);
				if(status.isEmpty()) {
					flc.setDirty(true);
					String uniqueFilename = itemBuilder
							.checkFilename(backgroundEl.getUploadFileName(), itemBuilder.getBackground(), itemFile.getParentFile());
					backgroundEl.setUploadFileName(uniqueFilename);
					backgroundImage = backgroundEl.moveUploadFileTo(itemFile.getParentFile());
					backgroundEl.setInitialFile(backgroundImage);
					Size size = imageService.getSize(new LocalFileImpl(backgroundImage), null);
					optimizeResizeEl(size, true);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(readOnly) return;
		//title
		itemBuilder.setTitle(titleEl.getValue());
		
		File objectImg = null;
		if(backgroundImage != null) {
			objectImg = backgroundImage;
		} else if(initialBackgroundImage != null) {
			objectImg = initialBackgroundImage;
		}
		
		if(objectImg != null) {
			String filename = objectImg.getName();
			String mimeType = WebappHelper.getMimeType(filename);
			Size currentSize = imageService.getSize(new LocalFileImpl(objectImg), null);
			
			Size size = currentSize;
			if(resizeEl.isVisible() && !resizeEl.isSelected(0)) {
				int maxSize = Integer.parseInt(resizeEl.getSelectedKey());
				if(maxSize < currentSize.getHeight() || maxSize < currentSize.getWidth()) {
					String extension = FileUtils.getFileSuffix(filename);
					size = imageService.scaleImage(objectImg, extension, objectImg, maxSize, maxSize, false);
					optimizeResizeEl(size, false);
				}
			}
			
			int height = -1;
			int width = -1;
			if(size != null) {
				height = size.getHeight();
				width = size.getWidth();
			}
			itemBuilder.setBackground(filename, mimeType, height, width);
		}

		//question
		String questionText = textEl.getRawValue();
		itemBuilder.setQuestion(questionText);
		
		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), QTI21QuestionType.drawing));
	}
	
	private void optimizeResizeEl(Size size, boolean selectSize) {
		List<String> keys = new ArrayList<>();
		List<String> values = new ArrayList<>();

		String selectedSize = null;
		if(size != null) {
			for(BackgroundSize availableSize:BackgroundSize.values()) {
				int proposedSize = availableSize.size();
				if(proposedSize <= size.getHeight() || proposedSize <= size.getWidth()) {
					String s = Integer.toString(availableSize.size());
					keys.add(s);
					values.add(s + " x " + s);
					if((proposedSize == size.getHeight() && proposedSize >= size.getWidth())
							|| (proposedSize == size.getWidth() && proposedSize >= size.getHeight())) {
						selectedSize = s;
					}
				}
			}
		}
		if(selectedSize == null) {
			keys.add(0, "no");
			values.add(0, translate("form.imd.background.resize.no"));
		}
		resizeEl.setKeysAndValues(keys.toArray(new String[keys.size()]), values.toArray(new String[values.size()]), null);

		if(keys.size() == 1) {
			resizeEl.select(keys.get(0), true);
			resizeEl.setVisible(false);
		} else {
			if(selectedSize != null) {
				resizeEl.select(selectedSize, true);
			} else if(selectSize && keys.size() > 1 && keys.get(1).equals(Integer.toString(BackgroundSize.s1024.size()))) {
				resizeEl.select(Integer.toString(BackgroundSize.s1024.size()), true);
			} else {
				resizeEl.select(keys.get(0), true);
			}
			resizeEl.setVisible(true);
		}
	}
}