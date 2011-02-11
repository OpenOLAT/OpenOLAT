/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.core.commons.services.thumbnail.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.olat.core.commons.services.thumbnail.CannotGenerateThumbnailException;
import org.olat.core.commons.services.thumbnail.FinalSize;
import org.olat.core.commons.services.thumbnail.ThumbnailSPI;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.ImageHelper;
import org.olat.core.util.ImageHelper.Size;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Description:<br>
 * Generate a thumbnail from the first page of a PDF
 * 
 * <P>
 * Initial Date:  30 mar. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class PDFToThumbnail implements ThumbnailSPI {

	private static final OLog log = Tracing.createLoggerFor(PDFToThumbnail.class);
	
	private List<String> extensions = Collections.singletonList("pdf");
	
	@Override
	public List<String> getExtensions() {
		return extensions;
	}

	@Override
	public FinalSize generateThumbnail(VFSLeaf pdfFile, VFSLeaf thumbnailFile, int maxWidth, int maxHeight) throws CannotGenerateThumbnailException {
		InputStream in = null;
		PDDocument document = null;
		try {
			in = pdfFile.getInputStream();
			document = PDDocument.load(in);
			if (document.isEncrypted()) {
				try {
					document.decrypt("");
				} catch (Exception e) {
					log.info("PDF document is encrypted: " + pdfFile);
					throw new CannotGenerateThumbnailException("PDF document is encrypted: " + pdfFile);
				}
			}
			List pages = document.getDocumentCatalog().getAllPages();
			PDPage page = (PDPage) pages.get(0);
			BufferedImage image = page.convertToImage(BufferedImage.TYPE_INT_BGR, 72);
			Size size = ImageHelper.scaleImage(image, thumbnailFile, maxWidth, maxHeight);
			return new FinalSize(size.getWidth(), size.getWidth());

		} catch (CannotGenerateThumbnailException e) {
			throw e;
		} catch (Exception e) {
			log.warn("Unable to create image from pdf file.", e);
			throw new CannotGenerateThumbnailException(e);
		} finally {
			FileUtils.closeSafely(in);
			if (document != null) {
				try {
					document.close();
				} catch (IOException e) {
					//only a try, fail silently
				}
			}
		}
	}
}
