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

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.modules.bc.meta.MetaInfoController;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.AddElementInfos;
import org.olat.modules.cemedia.CitationSourceType;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaModule;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.handler.CitationHandler;
import org.olat.modules.cemedia.manager.MetadataXStream;
import org.olat.modules.cemedia.model.CitationXml;
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
 * itemType
 * creators, edition, volume, series
 * 
 * webpage: authorString, title, url<br>
 * book: authorString, title, volume, series, edition, place, publisher, date, url<br>
 * journalArticle: authorString, title, publicationTitle, issue, date, pages, url<br>
 * report: title, place, institution, date, url
 * film: authorString, title, date, url
 * 
 * Initial date: 18.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CollectCitationMediaController extends FormBasicController implements PageElementAddController {
	
	private TextElement titleEl;
	private TagSelection tagsEl;
	private RichTextElement textEl;
	private RichTextElement descriptionEl;
	private TaxonomyLevelSelection taxonomyLevelEl;
	
	private SingleSelection sourceTypeEl;
	private TextElement urlEl;
	private TextElement sourceEl;
	private TextElement languageEl;
	private TextElement creatorsEl;
	private TextElement placeEl;
	private TextElement publisherEl;
	private DateChooser publicationDateEl;
	private DateChooser lastVisitDateEl;
	private TextElement editorEl;
	private TextElement editionEl;
	private TextElement volumeEl;
	private TextElement seriesEl;
	private TextElement publicationTitleEl;
	private TextElement isbnEl;
	private TextElement issueEl;
	private TextElement pagesEl;
	private TextElement institutionEl;
	
	private CitationXml citation;
	private Media mediaReference;
	private MediaVersion mediaVersion;
	private final boolean metadataOnly;
	
	private final String businessPath;
	private AddElementInfos userObject;

	private MediaRelationsController relationsCtrl;
	
	@Autowired
	private MediaModule mediaModule;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private CitationHandler citationHandler;
	@Autowired
	private TaxonomyService taxonomyService;

	public CollectCitationMediaController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(MediaCenterController.class, ureq.getLocale(),
				Util.createPackageTranslator(MetaInfoController.class, ureq.getLocale(),
						Util.createPackageTranslator(TaxonomyUIFactory.class, ureq.getLocale()))));
		businessPath = "[HomeSite:" + getIdentity().getKey() + "][PortfolioV2:0][MediaCenter:0]";
		metadataOnly = false;
		
		relationsCtrl = new MediaRelationsController(ureq, getWindowControl(), mainForm, null, true, true);
		relationsCtrl.setOpenClose(false);
		listenTo(relationsCtrl);
		
		initForm(ureq);
		updateCitationFieldsVisibility();
	}
	
	public CollectCitationMediaController(UserRequest ureq, WindowControl wControl, Media media, boolean metadataOnly) {
		super(ureq, wControl, Util.createPackageTranslator(MediaCenterController.class, ureq.getLocale(),
				Util.createPackageTranslator(MetaInfoController.class, ureq.getLocale(),
						Util.createPackageTranslator(TaxonomyUIFactory.class, ureq.getLocale()))));
		businessPath = media.getBusinessPath();
		this.metadataOnly = metadataOnly;
		mediaReference = media;
		mediaVersion = media.getVersions().get(0);
		
		if(StringHelper.containsNonWhitespace(mediaReference.getMetadataXml())) {
			citation = (CitationXml)MetadataXStream.get().fromXML(mediaReference.getMetadataXml());
		}
		initForm(ureq);
		updateCitationFieldsVisibility();
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
		formLayout.setElementCssClass("o_sel_ce_collect_citation_form");
		initMetadataForm(formLayout, ureq);
		
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
	
	private void initMetadataForm(FormItemContainer formLayout, UserRequest ureq) {
		String title = mediaReference == null ? null : mediaReference.getTitle();
		titleEl = uifactory.addTextElement("artefact.title", "artefact.title", 255, title, formLayout);
		titleEl.setElementCssClass("o_sel_pf_collect_title");
		titleEl.setMandatory(true);
		
		String desc = mediaReference == null ? null : mediaReference.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("artefact.descr", "artefact.descr", desc, 4, -1, formLayout, getWindowControl());
		descriptionEl.getEditorConfiguration().setPathInStatusBar(false);
		descriptionEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		
		String text = mediaVersion == null ? null : mediaVersion.getContent();
		textEl = uifactory.addRichTextElementForStringData("citation", "citation", text, 10, 6, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		textEl.setElementCssClass("o_sel_pf_collect_citation");
		textEl.setVisible(!metadataOnly);

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
		
		String[] typeKeys = new String[CitationSourceType.values().length];
		String[] typeValues = new String[CitationSourceType.values().length];
		int i = 0;
		for(CitationSourceType type : CitationSourceType.values()) {
			typeKeys[i] = type.name();
			typeValues[i++] = translate("mf.sourceType." + type.name());
		}
		
		sourceTypeEl = uifactory.addDropdownSingleselect("mf.sourceType", formLayout, typeKeys, typeValues, null);
		sourceTypeEl.addActionListener(FormEvent.ONCHANGE);
		String sourceType = (citation != null && citation.getItemType() != null
				? citation.getItemType().name() : CitationSourceType.book.name());
		for(String typeKey:typeKeys) {
			if(typeKey.equals(sourceType)) {
				sourceTypeEl.select(typeKey, true);
			}
		}
		
		initMetadataForm(formLayout);
		initCitationForm(formLayout);

		String link = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
		StaticTextElement linkEl = uifactory.addStaticTextElement("artefact.collect.link", "artefact.collect.link", link, formLayout);
		linkEl.setVisible(!metadataOnly && MediaUIHelper.showBusinessPath(businessPath));
	}

	protected void initMetadataForm(FormItemContainer formLayout) {
		String creators = (mediaReference != null ? mediaReference.getCreators() : null);
		creatorsEl = uifactory.addTextElement("creator", "mf.creator", -1, creators, formLayout);
		
		String place = (mediaReference != null ? mediaReference.getPlace() : null);
		placeEl = uifactory.addTextElement("place", "mf.city", -1, place, formLayout);

		String publisherVal = (mediaReference != null ? mediaReference.getPublisher() : null);
		publisherEl = uifactory.addTextElement("publisher", "mf.publisher", -1, publisherVal, formLayout);

		String url = (mediaReference != null ? mediaReference.getUrl() : null);
		urlEl = uifactory.addTextElement("url", "mf.url", -1, url, formLayout);

		String source = (mediaReference != null ? mediaReference.getSource() : null);
		sourceEl = uifactory.addTextElement("source", "mf.source", -1, source, formLayout);

		String language = (mediaReference != null ? mediaReference.getLanguage() : null);
		languageEl = uifactory.addTextElement("language", "mf.language", -1, language, formLayout);

		Date pubDate = (mediaReference != null ? mediaReference.getPublicationDate() : null);
		publicationDateEl = uifactory.addDateChooser("publicationDate", "mf.publicationDate", pubDate, formLayout);
	}

	protected void initCitationForm(FormItemContainer formLayout) {
		
		String editor = (citation != null ? citation.getEditor() : null);
		editorEl = uifactory.addTextElement("editor", "mf.editor", -1, editor, formLayout);
		
		String edition = (citation != null ? citation.getEdition() : null);
		editionEl = uifactory.addTextElement("edition", "mf.edition", -1, edition, formLayout);
		String volume = (citation != null ? citation.getVolume() : null);
		volumeEl = uifactory.addTextElement("volume", "mf.volume", -1, volume, formLayout);
		String series = (citation != null ? citation.getSeries() : null);
		seriesEl = uifactory.addTextElement("series", "mf.series", -1, series, formLayout);
		
		String isbn = (citation != null ? citation.getIsbn() : null);
		isbnEl = uifactory.addTextElement("isbn", "mf.isbn", -1, isbn, formLayout);
		
		String publicationTitle = (citation != null ? citation.getPublicationTitle() : null);
		publicationTitleEl = uifactory.addTextElement("publicationTitle", "mf.publicationTitle", -1, publicationTitle, formLayout);
		String issue = (citation != null ? citation.getIssue() : null);
		issueEl = uifactory.addTextElement("issue", "mf.issue", -1, issue, formLayout);
		String pages = (citation != null ? citation.getPages() : null);
		pagesEl = uifactory.addTextElement("pages", "mf.pages", -1, pages, formLayout);

		String institution = (citation != null ? citation.getInstitution() : null);
		institutionEl = uifactory.addTextElement("institution", "mf.institution", -1, institution, formLayout);
		
		Date visitDate = (citation != null ? citation.getLastVisit() : null);
		lastVisitDateEl = uifactory.addDateChooser("lastVisitDate", "mf.lastVisitDate", visitDate, formLayout);
	}
	
	/**
	 * <ul>
	 * 	<li>webpage: authorString, title, url. last visit</li>
	 * 	<li>book: authorString, title, volume, series, edition, editor, isbn, place, publisher, date, url</li>
	 * 	<li>journalArticle: authorString, title, publicationTitle, issue, date, pages, url</li>
	 * 	<li>report: title, place, institution, date, url</li>
	 * 	<li>film: authorString, title, date, url</li>
	 * </ul>
	 */
	private void updateCitationFieldsVisibility() {
		CitationSourceType sourceType = CitationSourceType.valueOf(sourceTypeEl.getSelectedKey());
		editorEl.setVisible(sourceType == CitationSourceType.book);
		editionEl.setVisible(sourceType == CitationSourceType.book);
		volumeEl.setVisible(sourceType == CitationSourceType.book);
		seriesEl.setVisible(sourceType == CitationSourceType.book);
		isbnEl.setVisible(sourceType == CitationSourceType.book);
		publicationTitleEl.setVisible(sourceType == CitationSourceType.journalArticle);
		issueEl.setVisible(sourceType == CitationSourceType.journalArticle);
		pagesEl.setVisible(sourceType == CitationSourceType.journalArticle || sourceType == CitationSourceType.book);
		institutionEl.setVisible(sourceType == CitationSourceType.report);
		lastVisitDateEl.setVisible(sourceType == CitationSourceType.webpage);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if (titleEl.isEmpty()) {
			titleEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == sourceTypeEl) {
			updateCitationFieldsVisibility();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(mediaReference == null) {
			String title = titleEl.getValue();
			String description = descriptionEl.getValue();
			String content = textEl.getValue();
			mediaReference = citationHandler.createMedia(title, description, null, content, businessPath, getIdentity(), MediaLog.Action.CREATED);
		} else if(metadataOnly) {
			mediaReference.setTitle(titleEl.getValue());
			mediaReference.setDescription(descriptionEl.getValue());
		} else {
			mediaReference.setTitle(titleEl.getValue());
			mediaReference.setDescription(descriptionEl.getValue());
			
			MediaVersion version = mediaReference.getVersions().get(0);
			version.setContent(textEl.getValue());
			mediaService.updateMediaVersion(version);
		}

		citation = new CitationXml();
		citation.setEdition(editionEl.getValue());
		citation.setInstitution(institutionEl.getValue());
		citation.setIssue(issueEl.getValue());
		CitationSourceType sourceType = CitationSourceType.valueOf(sourceTypeEl.getSelectedKey());
		citation.setItemType(sourceType);
		citation.setPages(pagesEl.getValue());
		citation.setPublicationTitle(publicationTitleEl.getValue());
		citation.setSeries(seriesEl.getValue());
		citation.setVolume(volumeEl.getValue());
		citation.setEditor(editorEl.getValue());
		citation.setIsbn(isbnEl.getValue());
		citation.setLastVisit(lastVisitDateEl.getDate());
		mediaReference.setMetadataXml(MetadataXStream.get().toXML(citation));
		
		// dublin core
		mediaReference.setCreators(creatorsEl.getValue());
		mediaReference.setPlace(placeEl.getValue());
		mediaReference.setPublisher(publisherEl.getValue());
		mediaReference.setPublicationDate(publicationDateEl.getDate());
		mediaReference.setUrl(urlEl.getValue());
		mediaReference.setSource(sourceEl.getValue());
		mediaReference.setLanguage(languageEl.getValue());

		mediaReference = mediaService.updateMedia(mediaReference);

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
