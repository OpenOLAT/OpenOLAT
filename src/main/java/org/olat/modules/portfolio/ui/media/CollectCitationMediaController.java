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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.modules.bc.meta.MetaInfoController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.core.gui.components.textboxlist.TextBoxItemImpl;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.ui.AddElementInfos;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.CitationSourceType;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.handler.CitationHandler;
import org.olat.modules.portfolio.manager.MetadataXStream;
import org.olat.modules.portfolio.model.CitationXml;
import org.olat.modules.portfolio.model.MediaPart;
import org.olat.modules.portfolio.ui.PortfolioHomeController;
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
	private RichTextElement descriptionEl, textEl;
	private TextBoxListElement categoriesEl;
	
	private SingleSelection sourceTypeEl;
	private TextElement urlEl, sourceEl, languageEl;
	private TextElement creatorsEl, placeEl, publisherEl;
	private DateChooser publicationDateEl, lastVisitDateEl;
	private TextElement editorEl, editionEl, volumeEl, seriesEl, publicationTitleEl, isbnEl, issueEl, pagesEl, institutionEl;
	
	private CitationXml citation;
	private Media mediaReference;
	private List<TextBoxItem> categories = new ArrayList<>();
	
	private final String businessPath;
	private AddElementInfos userObject;
	
	@Autowired
	private CitationHandler citationHandler;
	@Autowired
	private PortfolioService portfolioService;

	public CollectCitationMediaController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MetaInfoController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(PortfolioHomeController.class, getLocale(), getTranslator()));
		businessPath = "[HomeSite:" + getIdentity().getKey() + "][PortfolioV2:0][MediaCenter:0]";
		initForm(ureq);
		updateCitationFieldsVisibility();
	}
	
	public CollectCitationMediaController(UserRequest ureq, WindowControl wControl, Media media) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MetaInfoController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(PortfolioHomeController.class, getLocale(), getTranslator()));
		businessPath = media.getBusinessPath();
		mediaReference = media;
		
		if(StringHelper.containsNonWhitespace(mediaReference.getMetadataXml())) {
			citation = (CitationXml)MetadataXStream.get().fromXML(mediaReference.getMetadataXml());
		}
		
		List<Category> categoryList = portfolioService.getCategories(media);
		for(Category category:categoryList) {
			categories.add(new TextBoxItemImpl(category.getName(), category.getName()));
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
		MediaPart part = new MediaPart();
		part.setMedia(mediaReference);
		return part;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_pf_collect_citation_form");
		
		String title = mediaReference == null ? null : mediaReference.getTitle();
		titleEl = uifactory.addTextElement("artefact.title", "artefact.title", 255, title, formLayout);
		titleEl.setElementCssClass("o_sel_pf_collect_title");
		titleEl.setMandatory(true);
		
		String desc = mediaReference == null ? null : mediaReference.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("artefact.descr", "artefact.descr", desc, 6, 60, formLayout, getWindowControl());
		descriptionEl.getEditorConfiguration().setPathInStatusBar(false);
		
		String text = mediaReference == null ? null : mediaReference.getContent();
		textEl = uifactory.addRichTextElementForStringData("citation", "citation", text, 10, 6, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		textEl.setElementCssClass("o_sel_pf_collect_citation");
		
		categoriesEl = uifactory.addTextBoxListElement("categories", "categories", "categories.hint", categories, formLayout, getTranslator());
		categoriesEl.setHelpText(translate("categories.hint"));
		categoriesEl.setElementCssClass("o_sel_ep_tagsinput");
		categoriesEl.setAllowDuplicates(false);
		
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
		
		Date collectDate = mediaReference == null ? new Date() : mediaReference.getCollectionDate();
		String date = Formatter.getInstance(getLocale()).formatDate(collectDate);
		uifactory.addStaticTextElement("artefact.collect.date", "artefact.collect.date", date, formLayout);

		if(StringHelper.containsNonWhitespace(businessPath)) {
			String link = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
			uifactory.addStaticTextElement("artefact.collect.link", "artefact.collect.link", link, formLayout);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", "save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
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
			titleEl.setErrorKey("form.legende.mandatory", null);
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
			mediaReference = citationHandler.createMedia(title, description, content, businessPath, getIdentity());
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

		mediaReference = portfolioService.updateMedia(mediaReference);

		List<String> updatedCategories = categoriesEl.getValueList();
		portfolioService.updateCategories(mediaReference, updatedCategories);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
