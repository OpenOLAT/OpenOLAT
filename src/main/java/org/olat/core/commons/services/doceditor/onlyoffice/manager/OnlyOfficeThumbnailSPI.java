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
package org.olat.core.commons.services.doceditor.onlyoffice.manager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeConversionService;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule;
import org.olat.core.commons.services.thumbnail.CannotGenerateThumbnailException;
import org.olat.core.commons.services.thumbnail.FinalSize;
import org.olat.core.commons.services.thumbnail.ThumbnailSPI;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 4 Sep 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OnlyOfficeThumbnailSPI implements ThumbnailSPI {

	// https://api.onlyoffice.com/editors/conversionapi#text-matrix
	private static final List<String> PNG_OUTPUT_EXTENSION = Arrays.asList(
			// Text document file formats
			"doc", "docm", "docx", "dot", "dotm", "dotx", "epub", "fodt", "html", "mht", "odt", "ott",
			//"pdf", // PDFToThumbnail
			"rtf", "txt",
			// Spreadsheet file formats
			"xps", "csv", "fods", "ods", "ots", "xls", "xlsm", "xlsx", "xlt", "xltm", "xltx",
			// Presentation file formats
			"fodp", "odp", "otp", "pot", "potm", "potx", "pps", "ppsm", "ppsx", "ppt", "pptm", "pptx");
	
	@Autowired
	private OnlyOfficeModule onlyOfficeModule;
	@Autowired
	private OnlyOfficeConversionService onlyOfficeConversionService;
	
	@Override
	public List<String> getExtensions() {
		if (onlyOfficeModule.isEnabled() && onlyOfficeModule.isThumbnailsEnabled()) {
			return PNG_OUTPUT_EXTENSION;
		}
		return Collections.emptyList();
	}

	@Override
	public FinalSize generateThumbnail(VFSLeaf file, VFSLeaf thumbnailFile, int maxWidth, int maxHeight, boolean fill)
			throws CannotGenerateThumbnailException {
		return onlyOfficeConversionService.createThumbnail(file, thumbnailFile, maxWidth, maxHeight);
	}

}
