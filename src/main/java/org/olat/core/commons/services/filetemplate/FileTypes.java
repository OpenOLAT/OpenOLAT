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
package org.olat.core.commons.services.filetemplate;

import static org.olat.core.commons.services.filetemplate.ContentProviderFactory.empty;
import static org.olat.core.commons.services.filetemplate.ContentProviderFactory.emptyDocx;
import static org.olat.core.commons.services.filetemplate.ContentProviderFactory.emptyXlsx;
import static org.olat.core.commons.services.vfs.VFSLeafEditor.Mode.EDIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.filetemplate.ui.CreateFileController;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 19 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FileTypes {
	
	private final List<FileType> fileTypes;

	private FileTypes(List<FileType> fileTypes) {
		this.fileTypes = fileTypes;
	}
	
	public List<FileType> getFileTypes() {
		return fileTypes;
	}
	
	public static Builder editables(Locale locale) {
		Builder builder = new Builder(locale);
		
		VFSRepositoryService vfsService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
		
		if (vfsService.hasEditor("txt", EDIT)) {
			builder.addTxt();
		}
		if (vfsService.hasEditor("html", EDIT)) {
			builder.addHtml();
		}
		if (vfsService.hasEditor("css", EDIT)) {
			builder.addCss();
		}
		if (vfsService.hasEditor("docx", EDIT)) {
			builder.addDocx();
		}
		if (vfsService.hasEditor("xlsx", EDIT)) {
			builder.addXlsx();
		}
			
		return builder;
	}

	public static Builder builder(Locale locale) {
		return new Builder(locale);
	}

	public static final class Builder {
		private final Translator translator;
		private List<FileType> fileTypes = new ArrayList<>();

		private Builder(Locale locale) {
			this.translator = Util.createPackageTranslator(CreateFileController.class, locale);
		}
		
		public Builder addCss() {
			fileTypes.add(FileType.of("css", translate("file.type.css"), empty()));
			return this;
		}
		
		public Builder addDocx() {
			fileTypes.add(FileType.of("docx", translate("file.type.docx"), emptyDocx()));
			return this;
		}
		
		public Builder addHtml() {
			fileTypes.add(FileType.of("html", translate("file.type.html"), empty()));
			return this;
		}
		
		public Builder addTxt() {
			fileTypes.add(FileType.of("txt", translate("file.type.txt"), empty()));
			return this;
		}
		
		public Builder addXlsx() {
			fileTypes.add(FileType.of("xlsx", translate("file.type.xlsx"), emptyXlsx()));
			return this;
		}

		public Builder addFileType(FileType fileType) {
			this.fileTypes.add(fileType);
			return this;
		}

		private String translate(String i18nKey) {
			return translator.translate(i18nKey);
		}

		public FileTypes build() {
			return new FileTypes(new ArrayList<>(fileTypes));
		}
	}
	
}
