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
package org.olat.modules.cemedia.ui.medias;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.olat.core.commons.modules.bc.meta.MetaInfoController;
import org.olat.core.commons.services.doceditor.ContentProvider;
import org.olat.core.commons.services.doceditor.ContentProviderFactory;
import org.olat.core.commons.services.doceditor.ui.CreateDocumentController;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.AddElementInfos;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaModule;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.handler.DrawioHandler;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.MediaRelationsController;
import org.olat.modules.cemedia.ui.MediaUIHelper;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelSelection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.04.2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CreateDrawioMediaController extends FormBasicController implements PageElementAddController {
	
	private TextElement titleEl;
	private TagSelection tagsEl;
	private RichTextElement descriptionEl;
	private TaxonomyLevelSelection taxonomyLevelEl;
	private TextElement fileNameEl;
	private TextElement altTextEl;

	private Media mediaReference;
	
	private final String businessPath;
	private AddElementInfos userObject;
	private final boolean formatSvg = true;
	
	private MediaRelationsController relationsCtrl;
	
	@Autowired
	private DrawioHandler drawioHandler;
	@Autowired
	private MediaModule mediaModule;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private TaxonomyService taxonomyService;

	public CreateDrawioMediaController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(MediaCenterController.class, ureq.getLocale(),
				Util.createPackageTranslator(MetaInfoController.class, ureq.getLocale(),
				Util.createPackageTranslator(CreateDocumentController.class, ureq.getLocale(),
				Util.createPackageTranslator(TaxonomyUIFactory.class, ureq.getLocale())))));
		businessPath = "[HomeSite:" + getIdentity().getKey() + "][PortfolioV2:0][MediaCenter:0]";
		
		relationsCtrl = new MediaRelationsController(ureq, getWindowControl(), mainForm, null, true, true);
		relationsCtrl.setOpenClose(false);
		listenTo(relationsCtrl);
		
		initForm(ureq);
	}
	
	public Media getMediaReference() {
		return mediaReference;
	}
	
	@Override
	public AddElementInfos getUserObject() {
		return userObject;
	}

	@Override
	public void setUserObject(AddElementInfos userObject) {
		this.userObject = userObject;
	}

	@Override
	public PageElement getPageElement() {
		return MediaPart.valueOf(getIdentity(), mediaReference);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_pf_create_drawio_form");
		initMetadataForm(formLayout);
		
		if(relationsCtrl != null) {
			FormItem relationsItem = relationsCtrl.getInitialFormItem();
			relationsItem.setFormLayout("0_12");
			formLayout.add(relationsItem);
		}

		FormLayoutContainer buttonsCont = uifactory.addInlineFormLayout("buttons", null, formLayout);
		if(relationsCtrl != null) {
			buttonsCont.setFormLayout("0_12");
		}
		uifactory.addFormSubmitButton("save", "save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void initMetadataForm(FormItemContainer formLayout) {
		titleEl = uifactory.addTextElement("artefact.title", "artefact.title", 255, "", formLayout);
		titleEl.setElementCssClass("o_sel_pf_collect_title");
		titleEl.setMandatory(true);
		
		fileNameEl = uifactory.addTextElement("create.doc.name", -1, "", formLayout);
		fileNameEl.setDisplaySize(100);
		fileNameEl.setMandatory(true);
		
		List<TagInfo> tagsInfos = mediaService.getTagInfos(mediaReference, getIdentity(), false);
		tagsEl = uifactory.addTagSelection("tags", "tags", formLayout, getWindowControl(), tagsInfos);
		tagsEl.setHelpText(translate("categories.hint"));
		tagsEl.setElementCssClass("o_sel_ep_tagsinput");
		
		List<TaxonomyLevel> levels = mediaService.getTaxonomyLevels(mediaReference);
		Set<TaxonomyLevel> availableTaxonomyLevels = taxonomyService.getTaxonomyLevelsAsSet(mediaModule.getTaxonomyRefs());
		taxonomyLevelEl = uifactory.addTaxonomyLevelSelection("taxonomy.levels", "taxonomy.levels", formLayout,
				getWindowControl(), availableTaxonomyLevels);
		taxonomyLevelEl.setDisplayNameHeader(translate("table.header.taxonomy"));
		taxonomyLevelEl.setSelection(levels);
		
		String desc = mediaReference == null ? null : mediaReference.getTitle();
		descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("artefact.descr", "artefact.descr", desc, 4, -1, formLayout, getWindowControl());
		descriptionEl.getEditorConfiguration().setPathInStatusBar(false);
		descriptionEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		
		String altText = mediaReference == null ? null : mediaReference.getAltText();
		altTextEl = uifactory.addTextElement("artefact.alt.text", "artefact.alt.text", 1000, altText, formLayout);
		
		String link = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
		StaticTextElement linkEl =uifactory.addStaticTextElement("artefact.collect.link", "artefact.collect.link", link, formLayout);
		linkEl.setVisible(MediaUIHelper.showBusinessPath(businessPath));
		
		String jsPage = Util.getPackageVelocityRoot(CreateDocumentController.class) + "/new_filename_js.html";
		FormLayoutContainer jsCont = FormLayoutContainer.createCustomFormLayout("js", getTranslator(), jsPage);
		jsCont.contextPut("titleId", titleEl.getFormDispatchId());
		jsCont.contextPut("filetypeDefaultSuffix", getSuffix());
		jsCont.contextPut("filenameId", fileNameEl.getFormDispatchId());
		formLayout.add(jsCont);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if (titleEl.isEmpty()) {
			titleEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		String fileName = fileNameEl.getValue();
		fileNameEl.clearError();
		if (!StringHelper.containsNonWhitespace(fileName)) {
			fileNameEl.setErrorKey("form.mandatory.hover");
			allOk = false;
		} else {
			// update in GUI so user sees how we optimized
			fileNameEl.setValue(fileName);
			if (invalidFilenName(fileName)) {
				fileNameEl.setErrorKey("create.file.name.notvalid");
				allOk = false;
			}
		}

		return allOk;
	}
	
	private boolean invalidFilenName(String fileName) {
		return !FileUtils.validateFilename(fileName);
	}
	
	private String getFileName() {
		String fileName = fileNameEl.getValue();
		return fileName.endsWith("." + getSuffix())
				? fileName
				: fileName + "." + getSuffix();
	}
	
	private String getSuffix() {
		return formatSvg? "svg": "png";
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String fileName = getFileName();
		File tempDir = new File(WebappHelper.getTmpDir(), "pf" + UUID.randomUUID());
		tempDir.mkdirs();
		File tempFile = new File(tempDir, fileName);
		VFSLeaf vfsLeaf = new LocalFileImpl(tempFile);
		ContentProvider contentProvider =  formatSvg? ContentProviderFactory.emptyDrawioSvg(): ContentProviderFactory.emptyPng();
		VFSManager.copyContent(contentProvider.getContent(getLocale()), vfsLeaf, getIdentity());
		
		String title = titleEl.getValue();
		String altText = altTextEl.getValue();
		String description = descriptionEl.getValue();
		String mimeType = WebappHelper.getMimeType(fileName);
		UploadMedia mObject = new UploadMedia(tempFile, fileName, mimeType);
		mediaReference = drawioHandler.createMedia(title, description, altText, mObject, businessPath, getIdentity(), MediaLog.Action.CREATED);
		FileUtils.deleteFile(tempFile);
		FileUtils.deleteFile(tempDir);
		
		List<String> updatedTags = tagsEl.getDisplayNames();
		mediaService.updateTags(getIdentity(), mediaReference, updatedTags);
		
		Set<TaxonomyLevelRef> updatedLevels = taxonomyLevelEl.getSelection();
		mediaService.updateTaxonomyLevels(mediaReference, updatedLevels);
		
		if(relationsCtrl != null) {
			relationsCtrl.saveRelations(mediaReference);
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
