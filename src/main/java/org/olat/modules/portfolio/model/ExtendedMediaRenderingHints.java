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
package org.olat.modules.portfolio.model;

import org.olat.modules.ceditor.PageElementRenderingHints;
import org.olat.modules.portfolio.MediaRenderingHints;

/**
 * 
 * Initial date: 24 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExtendedMediaRenderingHints implements MediaRenderingHints, PageElementRenderingHints {

	private final boolean toPdf;
	private final boolean onePage;
	private final boolean extendedMetadata;
	
	private ExtendedMediaRenderingHints(boolean toPdf, boolean onePage, boolean extendedMetadata) {
		this.toPdf = toPdf;
		this.onePage = onePage;
		this.extendedMetadata = extendedMetadata;
	}

	@Override
	public boolean isToPdf() {
		return toPdf;
	}

	@Override
	public boolean isOnePage() {
		return onePage;
	}

	@Override
	public boolean isExtendedMetadata() {
		return extendedMetadata;
	}
	
	public static ExtendedMediaRenderingHints toPdf() {
		return new ExtendedMediaRenderingHints(true, true, true);
	}
	
	public static ExtendedMediaRenderingHints toPrint() {
		return new ExtendedMediaRenderingHints(false, true, true);
	}
	
	public static ExtendedMediaRenderingHints valueOf(MediaRenderingHints hints) {
		boolean hintPdf = hints.isToPdf();
		boolean hintPage = true;
		boolean hintMeta = hints.isExtendedMetadata();
		if(hints instanceof PageElementRenderingHints) {
			hintPage = ((PageElementRenderingHints)hints).isOnePage();
		}
		return new ExtendedMediaRenderingHints(hintPdf, hintPage, hintMeta);
	}
}
