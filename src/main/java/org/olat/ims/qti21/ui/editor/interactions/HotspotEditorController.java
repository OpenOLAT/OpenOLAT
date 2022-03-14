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
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSFormItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.helpers.Settings;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.ConsumableBoolean;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Constants.HotspotLayouts;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.interactions.HotspotAssessmentItemBuilder;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.ims.qti21.ui.components.FlowFormItem;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;
import org.olat.ims.qti21.ui.editor.interactions.HotspotExtendedEditorController.SpotWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.expression.operator.Shape;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.graphic.HotspotChoice;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;

/**
 * 
 * Initial date: 16.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HotspotEditorController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	private static final Set<String> mimeTypes = new HashSet<>();
	static {
		mimeTypes.add("image/gif");
		mimeTypes.add("image/jpg");
		mimeTypes.add("image/jpeg");
		mimeTypes.add("image/png");
	}
	
	private TextElement titleEl;
	private RichTextElement textEl;
	private FileElement backgroundEl;
	private SingleSelection resizeEl;
	private FormLink newRectButton;
	private FormLink newCircleButton;
	private FormLink extendedEditButton;
	private SingleSelection cardinalityEl;
	private FormLayoutContainer hotspotsCont;
	private MultipleSelectionElement responsiveEl;
	private MultipleSelectionElement correctHotspotsEl;
	private SingleSelection layoutEl;
	private MultipleSelectionElement shadowEl;
	
	private final boolean readOnly;
	private final boolean restrictedEdit;
	private final HotspotAssessmentItemBuilder itemBuilder;
	
	private File itemFile;
	private File rootDirectory;
	private VFSContainer rootContainer;
	
	private File backgroundImage;
	private File initialBackgroundImage;
	
	private List<HotspotWrapper> choiceWrappers = new ArrayList<>();
	
	private final String mapperUri;
	private final String backgroundMapperUri;
	
	private CloseableModalController cmc;
	private HotspotExtendedEditorController extendedEditorCtrl;
	
	@Autowired
	private ImageService imageService;
	
	public HotspotEditorController(UserRequest ureq, WindowControl wControl, HotspotAssessmentItemBuilder itemBuilder,
			File rootDirectory, VFSContainer rootContainer, File itemFile, boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl, LAYOUT_DEFAULT_2_10);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemFile = itemFile;
		this.itemBuilder = itemBuilder;
		this.rootDirectory = rootDirectory;
		this.rootContainer = rootContainer;
		this.readOnly = readOnly;
		this.restrictedEdit = restrictedEdit;
		
		mapperUri = registerCacheableMapper(null, "HotspotEditorController::" + CodeHelper.getRAMUniqueID(),
				new ResourcesMapper(itemFile.toURI(), rootDirectory));
		backgroundMapperUri = registerMapper(ureq, new BackgroundMapper(itemFile));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("Configure test questions");
		
		titleEl = uifactory.addTextElement("title", "form.imd.title", -1, itemBuilder.getTitle(), formLayout);
		titleEl.setElementCssClass("o_sel_assessment_item_title");
		titleEl.setMandatory(true);
		titleEl.setEnabled(!readOnly);
		
		String relativePath = rootDirectory.toPath().relativize(itemFile.toPath().getParent()).toString();
		VFSContainer itemContainer = (VFSContainer)rootContainer.resolve(relativePath);
		
		String question = itemBuilder.getQuestion();
		textEl = uifactory.addRichTextElementForQTI21("desc", "form.imd.descr", question, 8, -1, itemContainer,
				formLayout, ureq.getUserSession(), getWindowControl());
		textEl.addActionListener(FormEvent.ONCLICK);
		textEl.setEnabled(!readOnly);
		textEl.setVisible(!readOnly);
		if(readOnly) {
			FlowFormItem textReadOnlyEl = new FlowFormItem("descro", itemFile);
			textReadOnlyEl.setLabel("form.imd.descr", null);
			textReadOnlyEl.setBlocks(itemBuilder.getQuestionBlocks());
			textReadOnlyEl.setMapperUri(mapperUri);
			formLayout.add(textReadOnlyEl);
		}
		
		String[] cardinalityKeys = new String[] { Cardinality.SINGLE.name(), Cardinality.MULTIPLE.name() };
		String[] cardinalityValues = new String[] { translate(Cardinality.SINGLE.name()), translate(Cardinality.MULTIPLE.name()) };
		cardinalityEl = uifactory.addRadiosHorizontal("form.imd.cardinality", formLayout, cardinalityKeys, cardinalityValues);
		cardinalityEl.setElementCssClass("o_sel_assessment_item_cardinality");
		cardinalityEl.setEnabled(!restrictedEdit && !readOnly);
		if(itemBuilder.isSingleChoice()) {
			cardinalityEl.select(cardinalityKeys[0], true);
		} else {
			cardinalityEl.select(cardinalityKeys[1], true);
		}
		
		responsiveEl = uifactory.addCheckboxesHorizontal("form.imd.responsive", formLayout, onKeys, new String[] {""});
		responsiveEl.setHelpText(translate("form.imd.responsive.hint"));
		responsiveEl.setEnabled(!restrictedEdit && !readOnly);
		if(itemBuilder.isResponsive()) {
			responsiveEl.select(onKeys[0], true);
		}
		
		initialBackgroundImage = getCurrentBackground();
		backgroundEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "form.imd.background", "form.imd.background", formLayout);
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

		//responses
		String page = velocity_root + "/hotspots.html";
		hotspotsCont = FormLayoutContainer.createCustomFormLayout("answers", getTranslator(), page);
		hotspotsCont.getFormItemComponent().addListener(this);
		hotspotsCont.setLabel("new.spots", null);
		hotspotsCont.setRootForm(mainForm);
		hotspotsCont.contextPut("mapperUri", backgroundMapperUri);
		hotspotsCont.contextPut("restrictedEdit", restrictedEdit || readOnly);
		hotspotsCont.contextPut("focusOnEditor", new ConsumableBoolean(false));
		JSAndCSSFormItem js = new JSAndCSSFormItem("js", new String[] {
				(Settings.isDebuging() ? "js/interactjs/interact.js" : "js/interactjs/interact.min.js"),		
				"js/jquery/openolat/jquery.drawing.v2.js"
			});
		formLayout.add(js);
		formLayout.add(hotspotsCont);
		
		newCircleButton = uifactory.addFormLink("new.circle", "new.circle", null, hotspotsCont, Link.BUTTON);
		newCircleButton.setIconLeftCSS("o_icon o_icon-lg o_icon_circle");
		newCircleButton.setVisible(!restrictedEdit && !readOnly);
		newRectButton = uifactory.addFormLink("new.rectangle", "new.rectangle", null, hotspotsCont, Link.BUTTON);
		newRectButton.setIconLeftCSS("o_icon o_icon-lg o_icon_rectangle");
		newRectButton.setVisible(!restrictedEdit && !readOnly);
		extendedEditButton = uifactory.addFormLink("extended.edit.hotspot", "extended.edit.hotspot", null, hotspotsCont, Link.BUTTON);
		extendedEditButton.setIconLeftCSS("o_icon o_icon-lg o_icon_edit");
		extendedEditButton.setVisible(!restrictedEdit && !readOnly);
		
		updateBackground();

		String[] emptyKeys = new String[0];
		correctHotspotsEl = uifactory.addCheckboxesHorizontal("form.imd.correct.spots", formLayout, emptyKeys, emptyKeys);
		correctHotspotsEl.setElementCssClass("o_sel_assessment_item_correct_spots");
		correctHotspotsEl.setEnabled(!restrictedEdit && !readOnly);
		correctHotspotsEl.addActionListener(FormEvent.ONCHANGE);
		rebuildWrappersAndCorrectSelection();
		
		HotspotLayouts[] layouts = HotspotLayouts.values();
		String[] layoutKeys = new String[layouts.length];
		String[] layoutValues = new String[layouts.length];
		for(int i=layouts.length; i-->0; ) {
			layoutKeys[i] = layouts[i].cssClass();
			layoutValues[i] =  translate("hotspot.layout." + layouts[i].name());
		}
		layoutEl = uifactory.addDropdownSingleselect("hotspot.layout", "hotspot.layout", formLayout, layoutKeys, layoutValues, null);
		layoutEl.addActionListener(FormEvent.ONCHANGE);
		layoutEl.setEnabled(!readOnly);
		boolean found = false;
		for(int i=layoutKeys.length; i-->0; ) {
			if(itemBuilder.hasHotspotInteractionClass(layoutKeys[i])) {
				layoutEl.select(layoutKeys[i], true);
				found = true;
			}
		}
		if(!found) {
			layoutEl.select(layoutKeys[0], true);
		}

		shadowEl = uifactory.addCheckboxesHorizontal("hotspot.layout.shadow", "hotspot.layout.shadow", formLayout,
				onKeys, new String[] { "" });
		shadowEl.setEnabled(!readOnly);
		if(!itemBuilder.hasHotspotInteractionClass(QTI21Constants.CSS_HOTSPOT_DISABLE_SHADOW)) {
			shadowEl.select(onKeys[0], true);
		}
		updateLayoutCssClass();

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setElementCssClass("o_sel_hotspots_save");
		buttonsContainer.setRootForm(mainForm);
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
		
		updateHotspots(ureq);
		
		backgroundEl.clearError();
		if(backgroundImage == null && initialBackgroundImage == null) {
			backgroundEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else {
			List<ValidationStatus> status = new ArrayList<>();
			backgroundEl.validate(status);
			allOk &= status.isEmpty();
		}

		correctHotspotsEl.clearError();
		if(!restrictedEdit && !readOnly) {
			if(correctHotspotsEl.getSelectedKeys().isEmpty()) {
				correctHotspotsEl.setErrorKey("error.need.correct.answer", null);
				allOk &= false;
			}
		}
		
		cardinalityEl.clearError();
		if(cardinalityEl.isSelected(0) && correctHotspotsEl.getSelectedKeys().size() > 1) {
			cardinalityEl.setErrorKey("error.cardinality.answer", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(extendedEditorCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doTransfert(extendedEditorCtrl.getSpots());
				markDirty();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(extendedEditorCtrl);
		removeAsListenerAndDispose(cmc);
		extendedEditorCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(newCircleButton == source) {
			createHotspotChoice(Shape.CIRCLE, "60,60,25");
			updateHotspots(ureq);
		} else if(newRectButton == source) {
			createHotspotChoice(Shape.RECT, "50,50,100,100");
			updateHotspots(ureq);
		} else if(extendedEditButton == source) {
			updateHotspots(ureq);
			doOpenExtendedEditor(ureq);
		} else if(backgroundEl == source) {
			// upload in item directory;
			if(FileElementEvent.DELETE.equals(event.getCommand())) {
				if(backgroundEl.getUploadFile() != null && backgroundEl.getUploadFile() != backgroundEl.getInitialFile()) {
					backgroundEl.reset();
					if(initialBackgroundImage != null) {
						backgroundEl.setInitialFile(initialBackgroundImage);
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
					Size size = imageService.getSize(new LocalFileImpl(backgroundImage), null);
					optimizeResizeEl(size, true);
				}
			}
			Size backgroundSize = updateBackground();
			updateHotspots(ureq);
			updateHotspotsPosition(backgroundSize);
		} else if(correctHotspotsEl == source) {
			updateHotspots(ureq);
			doCorrectAnswers(correctHotspotsEl.getSelectedKeys());
			flc.setDirty(true);
		} else if(layoutEl == source) {
			updateHotspots(ureq);
			updateLayoutCssClass();
		} else if(hotspotsCont == source) {
			updateHotspots(ureq);
			String deleteHotspot = ureq.getParameter("delete-hotspot");
			if(StringHelper.containsNonWhitespace(deleteHotspot)) {
				doDeleteHotspot(deleteHotspot);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doTransfert(List<SpotWrapper> wrappers) {
		Map<String,HotspotWrapper> wrapperMap = new HashMap<>();
		for(HotspotWrapper wrapper:choiceWrappers) {
			wrapperMap.put(wrapper.getIdentifier(), wrapper);
		}
		
		for(SpotWrapper wrapper:wrappers) {
			HotspotWrapper choiceWrapper = wrapperMap.get(wrapper.getIdentifier());
			if(choiceWrapper != null) {
				choiceWrapper.setCoords(wrapper.getCoords());
				wrapperMap.remove(wrapper.getIdentifier());
			} else {
				itemBuilder.createHotspotChoice(Identifier.assumedLegal(wrapper.getIdentifier()),
						Shape.parseShape(wrapper.getShape()), wrapper.getCoords());	
			}
		}
		
		for(HotspotWrapper wrapper:wrapperMap.values()) {
			HotspotChoice choiceToDelete = itemBuilder.getHotspotChoice(wrapper.getIdentifier());
			if(choiceToDelete != null) {
				itemBuilder.deleteHotspotChoice(choiceToDelete);
			}
		}
		
		rebuildWrappersAndCorrectSelection();

		flc.setDirty(true);
	}
	
	private void doOpenExtendedEditor(UserRequest ureq) {
		String layoutCssClass;
		if(layoutEl.isOneSelected() && StringHelper.containsNonWhitespace(layoutEl.getSelectedKey())) {
			String selectedLayout = layoutEl.getSelectedKey();
			layoutCssClass = "o_qti_" + selectedLayout;
		} else {
			layoutCssClass = "o_qti_hotspot-standard";
		}
		
		File objectImg = getObjectImage();
		extendedEditorCtrl = new HotspotExtendedEditorController(ureq, getWindowControl(), itemFile, objectImg, choiceWrappers, layoutCssClass);
		listenTo(extendedEditorCtrl);
		
		String title = translate("extended.edit.hotspot.title");
		cmc = new CloseableModalController(getWindowControl(), "close", extendedEditorCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDeleteHotspot(String hotspotId) {
		if(restrictedEdit || readOnly) return;
		
		HotspotChoice choiceToDelete = itemBuilder.getHotspotChoice(hotspotId);
		if(choiceToDelete != null) {
			itemBuilder.deleteHotspotChoice(choiceToDelete);
			rebuildWrappersAndCorrectSelection();
		}
	}
	
	private void createHotspotChoice(Shape shape, String coords) {
		Identifier identifier = IdentifierGenerator.newNumberAsIdentifier("hc");
		itemBuilder.createHotspotChoice(identifier, shape, coords);
		rebuildWrappersAndCorrectSelection();

		hotspotsCont.contextPut("focusOnEditor", new ConsumableBoolean(true));
	}
	
	private void rebuildWrappersAndCorrectSelection() {
		choiceWrappers.clear();
		
		List<HotspotChoice> choices = itemBuilder.getHotspotChoices();
		SelectionValues keyValues = new SelectionValues();
		for(int i=0; i<choices.size(); i++) {
			HotspotChoice choice = choices.get(i);
			keyValues.add(SelectionValues.entry(choice.getIdentifier().toString(), translate("position.hotspot", Integer.toString(i + 1))));
			choiceWrappers.add(new HotspotWrapper(choice, itemBuilder));
		}
		correctHotspotsEl.setKeysAndValues(keyValues.keys(), keyValues.values());
		for(int i=0; i<choices.size(); i++) {
			HotspotChoice choice = choices.get(i);
			if(itemBuilder.isCorrect(choice)) {
				correctHotspotsEl.select(choice.getIdentifier().toString(), true);
			}
		}
		hotspotsCont.contextPut("hotspots", choiceWrappers);
	}
	
	private void doCorrectAnswers(Collection<String> correctResponseIds) {
		List<HotspotChoice> choices = itemBuilder.getHotspotChoices();
		itemBuilder.clearCorrectAnswers();
		for(int i=0; i<choices.size(); i++) {
			HotspotChoice choice = choices.get(i);
			boolean correct = correctResponseIds.contains(choice.getIdentifier().toString());
			itemBuilder.setCorrect(choice, correct);
		}
	}
	
	private void updateLayoutCssClass() {
		if(layoutEl.isOneSelected()) {
			String selectedLayout = layoutEl.getSelectedKey();
			if(StringHelper.containsNonWhitespace(selectedLayout)) {
				hotspotsCont.contextPut("layoutCssClass","o_qti_" +  selectedLayout);
			} else {
				hotspotsCont.contextPut("layoutCssClass", "o_qti_hotspot-standard");
			}
		} else {
			hotspotsCont.contextPut("layoutCssClass", "o_qti_hotspot-standard");
		}
	}
	
	private File getObjectImage() {
		File objectImg = null;
		if(backgroundImage != null) {
			objectImg = backgroundImage;
		} else if(initialBackgroundImage != null) {
			objectImg = initialBackgroundImage;
		}
		return objectImg;
	}
	
	private Size updateBackground() {
		Size size = null;
		File objectImg = getObjectImage();
		if(objectImg != null) {
			String relativePath = itemFile.getParentFile().toPath().relativize(objectImg.toPath()).toString();
			size = imageService.getSize(new LocalFileImpl(objectImg), null);
			hotspotsCont.contextPut("filename", relativePath);
			setBackgroundSize(size);
		} else {
			hotspotsCont.contextRemove("filename");
		}
		return size;
	}

	private void setBackgroundSize(Size size) {
		if(size == null) {
			hotspotsCont.contextRemove("height");
			hotspotsCont.contextRemove("width");
		} else {
			if(size.getHeight() > 0) {
				hotspotsCont.contextPut("height", Integer.toString(size.getHeight()));
			} else {
				hotspotsCont.contextRemove("height");
			}
			if(size.getWidth() > 0) {
				hotspotsCont.contextPut("width", Integer.toString(size.getWidth()));
			} else {
				hotspotsCont.contextRemove("width");
			}
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		if(readOnly) return;
		
		updateHotspots(ureq);
		
		itemBuilder.setTitle(titleEl.getValue());
		//set the question with the text entries
		String questionText = textEl.getRawValue();
		itemBuilder.setQuestion(questionText);
		itemBuilder.setResponsive(responsiveEl.isAtLeastSelected(1));
		
		File objectImg = null;
		if(backgroundImage != null) {
			objectImg = backgroundImage;
		} else if(initialBackgroundImage != null) {
			objectImg = initialBackgroundImage;
		}
		
		doCorrectAnswers(correctHotspotsEl.getSelectedKeys());
		
		if(cardinalityEl.isOneSelected()) {
			String selectedCardinality = cardinalityEl.getSelectedKey();
			itemBuilder.setCardinality(Cardinality.valueOf(selectedCardinality));
		}
		
		boolean updateHotspot = true;
		
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
					setBackgroundSize(size);
					scaleHotspot(currentSize, size);
					optimizeResizeEl(size, false);
					updateHotspot = false;
				}
			}

			int height = -1;
			int width = -1;
			if(size != null) {
				height = size.getHeight();
				width = size.getWidth();
			}

			String relPath = itemFile.getParentFile().toPath().relativize(objectImg.toPath()).toString();
			itemBuilder.setBackground(relPath, mimeType, height, width);
		}
		
		if(updateHotspot) {
			updateHotspots(ureq);
		}
		
		if(layoutEl.isOneSelected()) {
			String selectedLayout = layoutEl.getSelectedKey();
			for(HotspotLayouts layout:HotspotLayouts.values()) {
				itemBuilder.removeHotspotInteractionClass(layout.cssClass());
			}
			itemBuilder.addHotspotInteractionClass(selectedLayout);
		}
		
		if(shadowEl.isAtLeastSelected(1)) {
			itemBuilder.removeHotspotInteractionClass(QTI21Constants.CSS_HOTSPOT_DISABLE_SHADOW);
		} else {
			itemBuilder.addHotspotInteractionClass(QTI21Constants.CSS_HOTSPOT_DISABLE_SHADOW);
		}
		
		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), QTI21QuestionType.hotspot));
	}
	
	private void optimizeResizeEl(Size size, boolean selectSize) {
		if(size == null) return;
		
		List<String> keys = new ArrayList<>();
		List<String> values = new ArrayList<>();

		String selectedSize = null;
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
	
	private void updateHotspots(UserRequest ureq) {
		Map<String,HotspotWrapper> wrapperMap = new HashMap<>();
		for(HotspotWrapper wrapper:choiceWrappers) {
			wrapperMap.put(wrapper.getIdentifier(), wrapper);
		}
		
		for(Enumeration<String> parameterNames = ureq.getHttpReq().getParameterNames(); parameterNames.hasMoreElements(); ) {
			String name = parameterNames.nextElement();
			String value = ureq.getHttpReq().getParameter(name);
			if(name.endsWith("_shape")) {
				String hotspotIdentifier = name.substring(0, name.length() - 6);
				HotspotWrapper spot = wrapperMap.get(hotspotIdentifier);
				if(spot != null) {
					spot.setShape(value);
				}
			} else if(name.endsWith("_coords")) {
				String hotspotIdentifier = name.substring(0, name.length() - 7);
				HotspotWrapper spot = wrapperMap.get(hotspotIdentifier);
				if(spot != null) {
					spot.setCoords(value);
				}
			}
		}
	}
	
	private void scaleHotspot(Size oldSize, Size newSize) {
		if(oldSize == null || newSize == null || choiceWrappers.isEmpty()) return;
		int oldWidth = oldSize.getWidth();
		int newWidth = newSize.getWidth();
		int oldHeight = oldSize.getHeight();
		int newHeight = newSize.getHeight();
		if(oldWidth <= 0 || oldHeight <= 0 || newWidth <= 0 || newHeight <= 0) return;
		
		double widthFactor = ((double)oldWidth / newWidth);
		double heightFactor = ((double)oldHeight / newHeight);
		
		for(HotspotWrapper wrapper:choiceWrappers) {
			HotspotChoice choice = wrapper.getChoice();
			if(choice != null) {
				if(Shape.CIRCLE.equals(choice.getShape())) {
					scaleCircle(choice.getCoords(), widthFactor, heightFactor);
				} else if(Shape.RECT.equals(choice.getShape())) {
					scaleRect(choice.getCoords(), widthFactor, heightFactor);
				}
			}
		}
	}
	
	private void scaleCircle(List<Integer> coords, double widthFactor, double heightFactor) {
		if(coords.size() != 3) return;
		
		int centerX = coords.get(0);
		int centerY = coords.get(1);
		int radius = coords.get(2);
		
		if(centerX > 0) {
			coords.set(0, (int)Math.round(centerX / widthFactor));
		}
		if(centerY > 0) {
			coords.set(1, (int)Math.round(centerY / heightFactor));
		}
		if(radius > 0) {
			coords.set(2, (int)Math.round(radius / widthFactor));
		}
	}
	
	private void scaleRect(List<Integer> coords, double widthFactor, double heightFactor) {
		if(coords.size() != 4) return;
		
		int leftX = coords.get(0);
		int topY = coords.get(1);
		int rightX = coords.get(2);
		int bottomY = coords.get(3);

		if(leftX > 0) {
			coords.set(0, (int)Math.round(leftX / widthFactor));
		}
		if(topY > 0) {
			coords.set(1, (int)Math.round(topY / heightFactor));
		}
		if(rightX > 0) {
			coords.set(2, (int)Math.round(rightX / widthFactor));
		}
		if(bottomY > 0) {
			coords.set(3, (int)Math.round(bottomY / heightFactor));
		}
	}
	
	/**
	 * If the image is too small, translate the hotspots to match
	 * approximatively the new image.
	 * 
	 * @param backgroundSize
	 */
	private void updateHotspotsPosition(Size backgroundSize) {
		if(backgroundSize == null || choiceWrappers.isEmpty()) return;
		int width = backgroundSize.getWidth();
		int height = backgroundSize.getHeight();
		if(width <= 0 || height <= 0) return;
		
		for(HotspotWrapper wrapper:choiceWrappers) {
			HotspotChoice choice = wrapper.getChoice();
			if(choice != null) {
				if(Shape.CIRCLE.equals(choice.getShape())) {
					translateCircle(choice.getCoords(), width, height);
				} else if(Shape.RECT.equals(choice.getShape())) {
					translateRect(choice.getCoords(), width, height);
				}
			}
		}
	}

	private void translateCircle(List<Integer> coords, int width, int height) {
		if(coords.size() != 3) return;
		
		int centerX = coords.get(0);
		int centerY = coords.get(1);
		int radius = coords.get(2);
		
		int translateX = 0;
		int translateY = 0;
		if(centerX > width) {
			translateX = centerX - width;
			if((width - translateX) < radius) {
				translateX = width - radius;
			}
		}
		if(centerY > height) {
			translateY = centerY - height;
			if((height - translateY) < radius) {
				translateY = height - radius;
			}
		}
		if(translateX > 0) {
			coords.set(0, (centerX - translateX));
		}
		if(translateY > 0) {
			coords.set(1, (centerY - translateY));
		}
	}
	
	private void translateRect(List<Integer> coords, int width, int height) {
		if(coords.size() != 4) return;
		
		int leftX = coords.get(0);
		int topY = coords.get(1);
		int rightX = coords.get(2);
		int bottomY = coords.get(3);
		
		int translateX = 0;
		int translateY = 0;
		if(rightX > width) {
			translateX = rightX - width;
			if(translateX > leftX) {
				translateX = leftX;
			}
		}
		if(bottomY > height) {
			translateY = Math.min(topY, bottomY - height);
			if(translateY > topY) {
				translateY = topY;
			}
		}
		if(translateX > 0) {
			coords.set(0, (leftX - translateX));
			coords.set(2, (rightX - translateX));
		}
		if(translateY > 0) {
			coords.set(1, (topY - translateY));
			coords.set(3, (bottomY - translateY));
		}
	}
}
