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
package org.olat.core.commons.services.doceditor;

import static org.olat.core.commons.services.doceditor.ContentProviderFactory.empty;
import static org.olat.core.commons.services.doceditor.ContentProviderFactory.emptyDocx;
import static org.olat.core.commons.services.doceditor.ContentProviderFactory.emptyDrawio;
import static org.olat.core.commons.services.doceditor.ContentProviderFactory.emptyDrawiowb;
import static org.olat.core.commons.services.doceditor.ContentProviderFactory.emptyPptx;
import static org.olat.core.commons.services.doceditor.ContentProviderFactory.emptyXlsx;
import static org.olat.core.commons.services.doceditor.ContentProviderFactory.emptyXml;
import static org.olat.core.commons.services.doceditor.DocEditor.Mode.EDIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.doceditor.ui.CreateDocumentController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 19 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocTemplates {
	
	private static final String SUFFIX_TXT = "txt";
	private static final String SUFFIX_HTML = "html";
	private static final String SUFFIX_CSS = "css";
	private static final String SUFFIX_XML = "xml";
	private static final String SUFFIX_DOCX = "docx";
	private static final String SUFFIX_XLSX = "xlsx";
	private static final String SUFFIX_PPTX = "pptx";
	private static final String SUFFIX_DRAWIO = "drawio";
	private static final String SUFFIX_DRAWIOWB = "dwb";
	
	private final List<DocTemplate> docTemplates;

	private DocTemplates(List<DocTemplate> docTemplates) {
		this.docTemplates = docTemplates;
	}
	
	public List<DocTemplate> getTemplates() {
		return docTemplates;
	}

	public boolean isEmpty() {
		return docTemplates.isEmpty();
	}
	
	public static Builder editables(Identity identity, Roles roles, Locale locale, boolean metadataAvailable) {
		Builder builder = new Builder(locale);
		
		DocEditorService docEditorService = CoreSpringFactory.getImpl(DocEditorService.class);
		
		if (docEditorService.hasEditor(identity, roles, SUFFIX_DOCX, EDIT, metadataAvailable, false)) {
			builder.addDocx();
		}
		if (docEditorService.hasEditor(identity, roles, SUFFIX_XLSX, EDIT, metadataAvailable, false)) {
			builder.addXlsx();
		}
		if (docEditorService.hasEditor(identity, roles, SUFFIX_PPTX, EDIT, metadataAvailable, false)) {
			builder.addPptx();
		}
		if (docEditorService.hasEditor(identity, roles, SUFFIX_DRAWIO, EDIT, metadataAvailable, false)) {
			builder.addDrawio();
		}
		if (docEditorService.hasEditor(identity, roles, SUFFIX_DRAWIOWB, EDIT, metadataAvailable, false)) {
			builder.addDrawiowb();
		}
		if (docEditorService.hasEditor(identity, roles, SUFFIX_TXT, EDIT, metadataAvailable, false)) {
			builder.addTxt();
		}
		if (docEditorService.hasEditor(identity, roles, SUFFIX_HTML, EDIT, metadataAvailable, false)) {
			builder.addHtml();
		}
		if (docEditorService.hasEditor(identity, roles, SUFFIX_CSS, EDIT, metadataAvailable, false)) {
			builder.addCss();
		}
		if (docEditorService.hasEditor(identity, roles, SUFFIX_XML, EDIT, metadataAvailable, false)) {
			builder.addXml();
		}
		
		return builder;
	}
	
	public static Builder editablesOffice(Identity identity, Roles roles, Locale locale, boolean metadataAvailable) {
		Builder builder = new Builder(locale);
		
		DocEditorService docEditorService = CoreSpringFactory.getImpl(DocEditorService.class);
		
		if (docEditorService.hasEditor(identity, roles, SUFFIX_DOCX, EDIT, metadataAvailable, false)) {
			builder.addDocx();
		}
		if (docEditorService.hasEditor(identity, roles, SUFFIX_XLSX, EDIT, metadataAvailable, false)) {
			builder.addXlsx();
		}
		if (docEditorService.hasEditor(identity, roles, SUFFIX_PPTX, EDIT, metadataAvailable, false)) {
			builder.addPptx();
		}
		
		return builder;
	}

	public static Builder builder(Locale locale) {
		return new Builder(locale);
	}

	public static final class Builder {
		private final Translator translator;
		private List<DocTemplate> docTemplates = new ArrayList<>();

		private Builder(Locale locale) {
			this.translator = Util.createPackageTranslator(CreateDocumentController.class, locale);
		}
		
		public Builder addTxt() {
			docTemplates.add(DocTemplate.of(SUFFIX_TXT, translate("doc.type.txt"), empty()));
			return this;
		}
		
		public Builder addHtml() {
			docTemplates.add(DocTemplate.of(SUFFIX_HTML, translate("doc.type.html"), empty()));
			return this;
		}
		
		public Builder addCss() {
			docTemplates.add(DocTemplate.of(SUFFIX_CSS, translate("doc.type.css"), empty()));
			return this;
		}
		
		public Builder addXml() {
			docTemplates.add(DocTemplate.of(SUFFIX_XML, translate("doc.type.xml"), emptyXml()));
			return this;
		}
		
		public Builder addDocx() {
			docTemplates.add(DocTemplate.of(SUFFIX_DOCX, translate("doc.type.docx"), emptyDocx()));
			return this;
		}
		
		public Builder addXlsx() {
			docTemplates.add(DocTemplate.of(SUFFIX_XLSX, translate("doc.type.xlsx"), emptyXlsx()));
			return this;
		}
		
		public Builder addPptx() {
			docTemplates.add(DocTemplate.of(SUFFIX_PPTX, translate("doc.type.pptx"), emptyPptx()));
			return this;
		}
		
		public Builder addDrawio() {
			docTemplates.add(DocTemplate.of(SUFFIX_DRAWIO, translate("doc.type.drawio"), emptyDrawio()));
			return this;
		}
		
		public Builder addDrawiowb() {
			docTemplates.add(DocTemplate.of(SUFFIX_DRAWIOWB, translate("doc.type.drawiowb"), emptyDrawiowb()));
			return this;
		}

		public Builder addFileType(DocTemplate docTemplate) {
			this.docTemplates.add(docTemplate);
			return this;
		}

		private String translate(String i18nKey) {
			return translator.translate(i18nKey);
		}

		public DocTemplates build() {
			return new DocTemplates(new ArrayList<>(docTemplates));
		}
	}
	
}
