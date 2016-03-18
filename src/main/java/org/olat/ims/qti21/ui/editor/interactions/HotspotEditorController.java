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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSFormItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.interactions.HotspotAssessmentItemBuilder;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.expression.operator.Shape;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.graphic.HotspotChoice;

/**
 * 
 * Initial date: 16.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HotspotEditorController extends FormBasicController {
	
	private static final Set<String> mimeTypes = new HashSet<String>();
	static {
		mimeTypes.add("image/gif");
		mimeTypes.add("image/jpg");
		mimeTypes.add("image/jpeg");
		mimeTypes.add("image/png");
	}
	
	private TextElement titleEl;
	private RichTextElement textEl;
	private FileElement backgroundEl;
	private FormLayoutContainer hotspotsCont;
	
	private final HotspotAssessmentItemBuilder itemBuilder;
	
	private File itemFile;
	private File rootDirectory;
	private VFSContainer rootContainer;
	
	private File backgroundImage;
	private File initialBackgroundImage;
	private Map<String,HotspotWrapper> wrapperMap = new HashMap<>();
	
	private final String backgroundMapperUri;
	
	@Autowired
	private ImageService imageService;
	
	public HotspotEditorController(UserRequest ureq, WindowControl wControl, HotspotAssessmentItemBuilder itemBuilder,
			File rootDirectory, VFSContainer rootContainer, File itemFile) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemFile = itemFile;
		this.itemBuilder = itemBuilder;
		this.rootDirectory = rootDirectory;
		this.rootContainer = rootContainer;
		backgroundMapperUri = registerMapper(ureq, new BackgroundMapper(itemFile));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		titleEl = uifactory.addTextElement("title", "form.imd.title", -1, itemBuilder.getTitle(), formLayout);
		titleEl.setMandatory(true);
		
		String relativePath = rootDirectory.toPath().relativize(itemFile.toPath().getParent()).toString();
		VFSContainer itemContainer = (VFSContainer)rootContainer.resolve(relativePath);
		
		String question = itemBuilder.getQuestion();
		textEl = uifactory.addRichTextElementForStringData("desc", "form.imd.descr", question, 8, -1, true, itemContainer, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		textEl.addActionListener(FormEvent.ONCLICK);
		
		//responses
		String page = velocity_root + "/hotspots.html";
		hotspotsCont = FormLayoutContainer.createCustomFormLayout("answers", getTranslator(), page);
		hotspotsCont.setRootForm(mainForm);
		hotspotsCont.contextPut("mapperUri", backgroundMapperUri);
		JSAndCSSFormItem js = new JSAndCSSFormItem("js", new String[] { "js/jquery/openolat/jquery.drawing.js" });
		formLayout.add(js);
		formLayout.add(hotspotsCont);
		
		List<HotspotChoice> choices = itemBuilder.getHotspotChoices();
		List<HotspotWrapper> choiceWrappers = new ArrayList<>();
		for(HotspotChoice choice:choices) {
			HotspotWrapper spot = new HotspotWrapper(choice);
			choiceWrappers.add(spot);
			wrapperMap.put(spot.getIdentifier(), spot);
		}
		hotspotsCont.contextPut("hotspots", choiceWrappers);
		
		initialBackgroundImage = getCurrentBackground();
		backgroundEl = uifactory.addFileElement(getWindowControl(), "form.imd.background", "form.imd.background", formLayout);
		if(initialBackgroundImage != null) {
			backgroundEl.setInitialFile(initialBackgroundImage);
		}
		backgroundEl.addActionListener(FormEvent.ONCHANGE);
		backgroundEl.setDeleteEnabled(true);
		backgroundEl.limitToMimeType(mimeTypes, null, null);
		updateBackground();
		
		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
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
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		backgroundEl.clearError();
		if(backgroundImage == null && initialBackgroundImage == null) {
			backgroundEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk & super.validateFormLogic(ureq);
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
					}
				} else if(initialBackgroundImage != null) {
					initialBackgroundImage = null;
					backgroundEl.setInitialFile(null);
				}
				flc.setDirty(true);
			} else if (backgroundEl.isUploadSuccess()) {
				flc.setDirty(true);
				backgroundImage = backgroundEl.moveUploadFileTo(itemFile.getParentFile());
			}
			updateBackground();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateBackground() {
		File objectImg = null;
		if(backgroundImage != null) {
			objectImg = backgroundImage;
		} else if(initialBackgroundImage != null) {
			objectImg = initialBackgroundImage;
		}
		
		if(objectImg != null) {
			String filename = objectImg.getName();
			Size size = imageService.getSize(new LocalFileImpl(objectImg), null);
			hotspotsCont.contextPut("filename", filename);
			if(size != null) {
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
		} else {
			hotspotsCont.contextRemove("filename");
		}
	}
	

	
	@Override
	protected void formOK(UserRequest ureq) {
		itemBuilder.setTitle(titleEl.getValue());
		//set the question with the text entries
		String questionText = textEl.getRawValue();
		itemBuilder.setQuestion(questionText);
		
		File objectImg = null;
		if(backgroundImage != null) {
			objectImg = backgroundImage;
		} else if(initialBackgroundImage != null) {
			objectImg = initialBackgroundImage;
		}
		
		if(objectImg != null) {
			String filename = objectImg.getName();
			String mimeType = WebappHelper.getMimeType(filename);
			Size size = imageService.getSize(new LocalFileImpl(objectImg), null);
			int height = -1;
			int width = -1;
			if(size != null) {
				height = size.getHeight();
				width = size.getWidth();
			}
			itemBuilder.setBackground(filename, mimeType, height, width);
		}
		updateHotspots(ureq);
		
		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), QTI21QuestionType.hotspot));
	}
	
	private void updateHotspots(UserRequest ureq) {
		for(Enumeration<String> parameterNames = ureq.getHttpReq().getParameterNames(); parameterNames.hasMoreElements(); ) {
			String name = parameterNames.nextElement();
			String value = ureq.getHttpReq().getParameter(name);
			if(name.endsWith("_shape")) {
				String hotspotIdentifier = name.substring(0, name.length() - 6);
				HotspotWrapper spot = getOrCreateHotspots(hotspotIdentifier);
				spot.setShape(value);
			} else if(name.endsWith("_coords")) {
				String hotspotIdentifier = name.substring(0, name.length() - 7);
				HotspotWrapper spot = getOrCreateHotspots(hotspotIdentifier);
				spot.setCoords(value);
			}
		}
		
		List<HotspotWrapper> choiceWrappers = new ArrayList<>();
		for(HotspotWrapper choiceWrapper:wrapperMap.values()) {
			choiceWrappers.add(choiceWrapper);
		}
		hotspotsCont.contextPut("hotspots", choiceWrappers);
	}
	
	private HotspotWrapper getOrCreateHotspots(String hotspotIdentifier) {
		HotspotWrapper wrapper = wrapperMap.get(hotspotIdentifier);
		if(wrapper == null) {
			HotspotChoice choice = itemBuilder.createHotspotChoice(hotspotIdentifier);
			wrapper = new HotspotWrapper(choice);
			wrapperMap.put(hotspotIdentifier, wrapper);
		}
		return wrapper;
	}
	
	public static class HotspotWrapper {

		private final HotspotChoice choice;
		
		public HotspotWrapper(HotspotChoice choice) {
			this.choice = choice;
		}

		public String getIdentifier() {
			return choice.getIdentifier().toString();
		}

		public String getShape() {
			return choice.getShape().toQtiString();
		}
		
		public void setShape(String shape) {
			if("circle".equals(shape)) {
				choice.setShape(Shape.CIRCLE);
			} else if("rect".equals(shape)) {
				choice.setShape(Shape.RECT);
			} else if("poly".equals(shape)) {
				choice.setShape(Shape.POLY);
			}
		}

		public String getCoords() {
			return AssessmentItemFactory.coordsString(choice.getCoords());
		}

		public void setCoords(String coords) {
			List<Integer> coordList = AssessmentItemFactory.coordsList(coords);
			choice.setCoords(coordList);
		}
	}
	
	private static class BackgroundMapper implements Mapper {
		
		private final File itemFile;
		
		public BackgroundMapper(File itemFile) {
			this.itemFile = itemFile;
		}

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if(StringHelper.containsNonWhitespace(relPath)) {
				if(relPath.startsWith("/")) {
					relPath = relPath.substring(1);
				}
				
				File backgroundFile = new File(itemFile.getParentFile(), relPath);
				return new VFSMediaResource(new LocalFileImpl(backgroundFile));
			}
			return new NotFoundMediaResource(relPath);
		}
	}
}
