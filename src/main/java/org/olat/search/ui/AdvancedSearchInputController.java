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
package org.olat.search.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.StringHelper;
import org.olat.search.model.AbstractOlatDocument;
import org.olat.search.service.SearchMetadataFieldsProvider;
import org.olat.search.service.document.file.ExcelDocument;
import org.olat.search.service.document.file.HtmlDocument;
import org.olat.search.service.document.file.OpenDocument;
import org.olat.search.service.document.file.PdfDocument;
import org.olat.search.service.document.file.PowerPointDocument;
import org.olat.search.service.document.file.TextDocument;
import org.olat.search.service.document.file.UnkownDocument;
import org.olat.search.service.document.file.WordDocument;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Controller for the advanced search
 * 
 * <P>
 * Initial Date:  4 dec. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class AdvancedSearchInputController extends FormBasicController {
	
	private static final Logger log = Tracing.createLoggerFor(AdvancedSearchInputController.class);
	
	private DateFormat format = new SimpleDateFormat("yyyyMMdd");
	
	private static final String HTML_TYPES = HtmlDocument.FILE_TYPE;
	private static final String WORD_TYPES = WordDocument.FILE_TYPE;
	private static final String CALC_TYPES = ExcelDocument.FILE_TYPE + " " + OpenDocument.SPEADSHEET_FILE_TYPE + " "
			+ OpenDocument.FORMULA_FILE_TYPE;
	private static final String PRESENTATION_TYPES = PowerPointDocument.FILE_TYPE + " " + OpenDocument.PRESENTATION_FILE_TYPE;
	private static final String PDF_TYPES = PdfDocument.FILE_TYPE;
	private static final String WIKI_TYPES = "type.*.wiki";
	private static final String FORUM_TYPES = "type.*.forum.message";
	private static final String COURSE_TYPES = "type.course.node*";
	private static final String BLOG_PODCAST_TYPES = "type.*.podcast type.*.blog type.repository.entry.*.PODCAST type.repository.entry.*.BLOG";
	private static final String GROUP_TYPES = "type.group";
	private static final String USER_TYPES = "type.identity";
	private static final String PORTFOLIO_TYPES = "type.db.EP*Map* type.group.EP*Map*";
	private static final String OTHER_TYPES = TextDocument.FILE_TYPE + " " + OpenDocument.FORMULA_FILE_TYPE + " " + OpenDocument.GRAPHIC_FILE_TYPE + " " 
		+ UnkownDocument.UNKOWN_TYPE + " " + OpenDocument.TEXT_FILE_TYPE;
	
	private final List<DocumentInfo> documentInfos = new ArrayList<>();
	 
	private FormLink searchButton;
	private TextElement searchInput;
	private TextElement authorQuery;
	private TextElement titleQuery;
	private TextElement descriptionQuery;
	private DateChooser createdDate;
	private DateChooser modifiedDate;
	private MultipleSelectionElement licenseQuery;
	private TextElement metadataQuery;
	private SingleSelection metadataType;
	private SingleSelection contextSelection;
	private MultipleSelectionElement documentTypeQuery;
	
	private boolean resourceContextEnable = true;
	private Set<String> selectedDocumentTypes = new HashSet<>();
	
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	
	public AdvancedSearchInputController(UserRequest ureq, WindowControl wControl, Form mainForm) {
		super(ureq, wControl, -1, null, mainForm);
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchInput = uifactory.addTextElement("search_input", "search.title", 255, "", formLayout);
		authorQuery = uifactory.addTextElement("search_author", "form.search.label.author", 255, "", formLayout); 
		titleQuery = uifactory.addTextElement("search_title", "form.search.label.title", 255, "", formLayout);
		descriptionQuery = uifactory.addTextElement("search_description", "form.search.label.description", 255, "", formLayout);
		createdDate = uifactory.addDateChooser("search_creation", "form.search.label.created.date", null, formLayout);
		modifiedDate = uifactory.addDateChooser("search_modification", "form.search.label.modified.date", null, formLayout);

		//document types
		initDocumentTypesKeysAndValues();
		String[] documentTypeKeys = new String[documentInfos.size()];
		String[] documentTypeValues = new String[documentInfos.size()];
		
		int j=0;
		for(DocumentInfo documentType : documentInfos) {
			documentTypeKeys[j] = documentType.getKey();
			documentTypeValues[j++] = documentType.getValue();
		}
		documentTypeQuery = uifactory.addCheckboxesDropdown("doc_type", "form.search.label.documenttype", formLayout, documentTypeKeys, documentTypeValues);
		documentTypeQuery.setNonSelectedText(translate("drop.down.no.selection"));
		
		//licenses
		List<LicenseType> activeLicenseTypes = licenseService.loadLicenseTypes();
		activeLicenseTypes.removeIf(licenseService::isNoLicense);
		Collections.sort(activeLicenseTypes);
		
		String[] licenseTypeKeys = new String[activeLicenseTypes.size()];
		String[] licenseTypeValues = new String[activeLicenseTypes.size()];
		int counter = 0;
		for (LicenseType licenseType: activeLicenseTypes) {
			licenseTypeKeys[counter] = String.valueOf(licenseType.getKey());
			licenseTypeValues[counter] = LicenseUIFactory.translate(licenseType, getLocale());
			counter++;
		}
		licenseQuery = uifactory.addCheckboxesDropdown("search_license", "form.search.label.license", formLayout, licenseTypeKeys, licenseTypeValues);
		licenseQuery.setNonSelectedText(translate("drop.down.no.selection"));
		if (!licenseModule.isAnyHandlerEnabled()) {
			licenseQuery.setVisible(false);
		}

		//metadatas
		SearchMetadataFieldsProvider metadataProvider = (SearchMetadataFieldsProvider) CoreSpringFactory.getBean("SearchMetadataFieldsProvider");
		// The metadata key selection, e.g DC.language for doublin core language metadata
		List<String> metaDataList = metadataProvider.getAdvancedSearchableFields();
		if (metaDataList.size() > 0) {
			String[] metaDataFields = ArrayHelper.toArray(metaDataList);
			String[] metaDataFieldsTranslated = new String[metaDataFields.length];
			Translator metaTranslator = metadataProvider.createFieldsTranslator(getLocale());
			for (int i=0; i < metaDataFields.length; i++) {
				String key = metaDataFields[i];
				metaDataFieldsTranslated[i] = key + " (" + metaTranslator.translate(key) + ")";
			}
			metadataType = uifactory.addDropdownSingleselect("metadata_type", "form.search.label.metadatatype", formLayout, metaDataFields, metaDataFieldsTranslated, null);
			metadataQuery = uifactory.addTextElement("metadata_query", null, 255, "", formLayout);
		}
		
		contextSelection = uifactory.addRadiosHorizontal("context", "form.search.label.context", formLayout, new String[0], new String[0]);
		searchButton = uifactory.addFormLink("search", formLayout, Link.BUTTON_SMALL);
		searchButton.setEnabled(true);
	}

	public boolean isResourceContextEnable() {
		return resourceContextEnable;
	}

	public void setResourceContextEnable(boolean resourceContextEnable) {
		if(contextSelection.isVisible() != resourceContextEnable) {
			contextSelection.setVisible(resourceContextEnable);
		}
		this.resourceContextEnable = resourceContextEnable;
	}
	
	/**
	 * Initialize drop-down value and key array.
	 */
	private void initDocumentTypesKeysAndValues() {
		documentInfos.clear();
		documentInfos.add(new DocumentInfo("html", translate("type.file.html"), null, HTML_TYPES));
		documentInfos.add(new DocumentInfo("word", translate("type.file.word"), null, WORD_TYPES));
		documentInfos.add(new DocumentInfo("table", translate("type.file.table"), null, CALC_TYPES));
		documentInfos.add(new DocumentInfo("powerpoint", translate("type.file.presentation"), null, PRESENTATION_TYPES));
		documentInfos.add(new DocumentInfo("pdf", translate("type.file.pdf"), null, PDF_TYPES));
		documentInfos.add(new DocumentInfo("wiki", translate("area.wikis"), WIKI_TYPES, null));
		documentInfos.add(new DocumentInfo("forum", translate("area.forums"), FORUM_TYPES, null));
		documentInfos.add(new DocumentInfo("course", translate("area.courses"), COURSE_TYPES, null));
		documentInfos.add(new DocumentInfo("blog", translate("area.blogs"), BLOG_PODCAST_TYPES, null));
		documentInfos.add(new DocumentInfo("group", translate("area.groups"), GROUP_TYPES, null));
		documentInfos.add(new DocumentInfo("user", translate("area.users"), USER_TYPES, null));
		documentInfos.add(new DocumentInfo("portfolio", translate("area.portfolio"), PORTFOLIO_TYPES, null));
		documentInfos.add(new DocumentInfo("others", translate("type.file.others"), null, OTHER_TYPES));
	}
	
	public String getSearchString() {
		return searchInput.getValue();
	}
	
	public void setSearchString(String searchString) {
		searchInput.setValue(searchString);
	}
	
	public String getContext() {
		if(contextSelection.isOneSelected()) {
			return contextSelection.getSelectedKey();
		}
		return null;
	}
	
	public void setContextKeysAndValues(String[] keys, String[] values) {
		contextSelection.setKeysAndValues(keys, values, null);
		if(keys.length > 0) {
			contextSelection.select(keys[keys.length - 1], true);
		}
	}
	
	public void load() {
		if(!selectedDocumentTypes.isEmpty()) {
			for(String selected : selectedDocumentTypes) {
				documentTypeQuery.select(selected, true);
			}
		}
	}
	
	public void unload() {
		selectedDocumentTypes.clear();
		selectedDocumentTypes.addAll(documentTypeQuery.getSelectedKeys());
	}
	
	public List<String> getQueryStrings() {
		List<String> queries = new ArrayList<>();

		if (StringHelper.containsNonWhitespace(authorQuery.getValue())) {
			appendAnd(queries, AbstractOlatDocument.AUTHOR_FIELD_NAME, ":(", authorQuery.getValue(), ") ");
		}
		if(!documentTypeQuery.getSelectedKeys().isEmpty()) {
			buildDocumentTypeQuery(queries);	
		}
		if (!licenseQuery.getSelectedKeys().isEmpty()) {
			buildLicenseTypeQuery(queries);
		}
		if (StringHelper.containsNonWhitespace(titleQuery.getValue())) {
			appendAnd(queries, AbstractOlatDocument.TITLE_FIELD_NAME, ":(", titleQuery.getValue(), ") ");
		}
		if (StringHelper.containsNonWhitespace(descriptionQuery.getValue())) {
			appendAnd(queries, AbstractOlatDocument.DESCRIPTION_FIELD_NAME, ":(", descriptionQuery.getValue(), ") ");
		}
		if (StringHelper.containsNonWhitespace(createdDate.getValue())) {
			Date creationDate = createdDate.getDate();
			if(creationDate != null) {
				appendAnd(queries, AbstractOlatDocument.CREATED_FIELD_NAME, ":(", format.format(creationDate), ") ");
			}
		}
		if (StringHelper.containsNonWhitespace(modifiedDate.getValue())) {
			Date modificationDate = modifiedDate.getDate();
			if(modificationDate != null) {
				appendAnd(queries, AbstractOlatDocument.CHANGED_FIELD_NAME, ":(", format.format(modificationDate), ") ");
			}
		}
		//Check for null on metadata element since it might not be configured and initialized
		if (metadataQuery != null && StringHelper.containsNonWhitespace(metadataQuery.getValue())) {
			appendAnd(queries, metadataType.getSelectedKey(), ":(", metadataQuery.getValue(), ") ");
		}
		if (log.isDebugEnabled()) log.debug("Advanced query=" + queries);
		return queries;
	}

	/**
	 * Append 'AND' operation if buf is not empty.
	 * @param buf
	 */
	private void appendAnd(List<String> queries, String... strings) {
		StringBuilder query = new StringBuilder();
		for(String string:strings) {
			query.append(string);
		}
		
		if(query.length() > 0) {
			queries.add(query.toString());
		}
	}
	
	public boolean isDocumentTypesSelected() {
		return documentTypeQuery.isMultiselect();
	}
	
	private void buildDocumentTypeQuery(List<String> queries) {
		Collection<String> selectDocTypes = documentTypeQuery.getSelectedKeys();
		if(selectDocTypes.size() == documentInfos.size() || selectDocTypes.isEmpty()) {
			//all selected -> no constraints of the type
			return;
		}
		
		List<String> docTypes = new ArrayList<>();
		List<String> fTypes = new ArrayList<>();
		for(String selectedocType:selectDocTypes) {
			for(DocumentInfo info:documentInfos) {
				if(selectedocType.equals(info.getKey())) {
					if(info.hasDocumentType()) {
						docTypes.add(info.getDocumentType());
					}
					if(info.hasFileType()) {
						fTypes.add(info.getFileType());
					}
				}
			}
		}
		
		StringBuilder buf = new StringBuilder();
		buf.append('(');
		if(!docTypes.isEmpty()) {
			buf.append(AbstractOlatDocument.DOCUMENTTYPE_FIELD_NAME);
			buf.append(":(");
			for(String docType:docTypes) {
				buf.append(docType).append(' ');
			}
		  buf.append(") ");
		}
		
		if(!fTypes.isEmpty()) {
			if(!docTypes.isEmpty()) {
				buf.append(' ');//don't need OR
			}
			
			buf.append(AbstractOlatDocument.FILETYPE_FIELD_NAME);
			buf.append(":(");
			for(String fileType:fTypes) {
				buf.append(fileType).append(' ');
			}
			buf.append(")");
		}
		buf.append(") ");
		
		if(buf.length() > 4) {
			queries.add(buf.toString());
		}
	}
	
	private void buildLicenseTypeQuery(List<String> queries) {
		Collection<String> selectedLicTypes = licenseQuery.getSelectedKeys();
		if(selectedLicTypes.size() == licenseQuery.getKeys().size() || selectedLicTypes.isEmpty()) {
			//all selected -> no constraints of the type
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		sb.append(AbstractOlatDocument.LICENSE_TYPE_FIELD_NAME);
		sb.append(":(");
		for(String licenseTypeKey: selectedLicTypes) {
			sb.append(licenseTypeKey).append(' ');
		}
		sb.append(")) ");
		
		if (sb.length() > 6) {
			queries.add(sb.toString());
		}
	}
	
	public void getSearchProperties(Properties props) {
		setSearchProperty(props, "aq", authorQuery.getValue());
		setSearchProperty(props, "tq", titleQuery.getValue());
		setSearchProperty(props, "dq", descriptionQuery.getValue());
		setSearchProperty(props, "cd", createdDate.getValue());
		setSearchProperty(props, "md", modifiedDate.getValue());
		setSearchProperty(props, "mtdq", metadataQuery.getValue());

		if(metadataType.isOneSelected()) {
			props.setProperty("mtdt", metadataType.getSelectedKey());
		} else {
			props.remove("mtdt");
		}
		
		Collection<String> selectedKeys = documentTypeQuery.getSelectedKeys();
		StringBuilder sb = new StringBuilder();
		for(String selectedKey:selectedKeys) {
			sb.append(selectedKey).append('|');
		}
		props.setProperty("dtypes", sb.toString());
		
		Collection<String> licenseTypeKeys = licenseQuery.getSelectedKeys();
		StringBuilder licSb = new StringBuilder();
		for (String licenseTypeKey: licenseTypeKeys) {
			licSb.append(licenseTypeKey).append('|');
		}
		props.setProperty("lictypes", licSb.toString());
	}
	
	private void setSearchProperty(Properties props, String key, String value) {
		if(StringHelper.containsNonWhitespace(value)) {
			props.setProperty(key, value);
		} else {
			props.remove(key);
		}
	}
	
	public void setSearchProperties(Properties props) {
		String aq = props.getProperty("aq");
		if(StringHelper.containsNonWhitespace(aq)) {
			authorQuery.setValue(aq);
		}
		
		String tq = props.getProperty("tq");
		if(StringHelper.containsNonWhitespace(tq)) {
			titleQuery.setValue(tq);
		}
		
		String dq = props.getProperty("dq");
		if(StringHelper.containsNonWhitespace(aq)) {
			descriptionQuery.setValue(dq);
		}
		
		String cd = props.getProperty("cd");
		if(StringHelper.containsNonWhitespace(cd)) {
			createdDate.setValue(cd);
		}
		
		String md = props.getProperty("md");
		if(StringHelper.containsNonWhitespace(md)) {
			modifiedDate.setValue(md);
		}
		
		String mtdq = props.getProperty("mtdq");
		if (StringHelper.containsNonWhitespace(mtdq)) {
			metadataQuery.setValue(mtdq);
	  }
		
		String mtdt = props.getProperty("mtdt");
		if(StringHelper.containsNonWhitespace(mtdt)) {
			metadataType.select(mtdt, true);
		}
		
		String dtypes = props.getProperty("dtypes");
		if(StringHelper.containsNonWhitespace(dtypes)) {
			selectedDocumentTypes.clear();
			for(DocumentInfo documentInfo:documentInfos) {
				boolean selected = dtypes.indexOf(documentInfo.getKey()) >= 0;
				documentTypeQuery.select(documentInfo.getKey(), selected);
				if(selected) {
					selectedDocumentTypes.add(documentInfo.getKey());
				}
			}
		}

		String lictypes = props.getProperty("lictypes");
		if(StringHelper.containsNonWhitespace(lictypes)) {
			for (String licenseTypeKey: licenseQuery.getKeys()) {
				boolean selected = lictypes.indexOf(licenseTypeKey) >= 0;
				licenseQuery.select(licenseTypeKey, selected);
			}
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	public FormLink getSearchButton() {
		return searchButton;
	}
	
	public FormItem getFormItem() {
		return flc;
	}
	
	public class DocumentInfo {
		public String key;
		public String value;
		public String fileType;
		public String documentType;
		
		public DocumentInfo(String key, String value, String docType, String fType) {
			this.key = key;
			this.value = value;
			this.fileType = fType;
			this.documentType = docType;
		}
		
		public String getKey() {
			return key;
		}
		
		public String getValue() {
			return value;
		}
		
		public boolean hasDocumentType() {
			return StringHelper.containsNonWhitespace(documentType);
		}
		
		public String getDocumentType() {
			return documentType;
		}
		
		public boolean hasFileType() {
			return StringHelper.containsNonWhitespace(fileType);
		}
		
		public String getFileType() {
			return fileType;
		}
	}
}