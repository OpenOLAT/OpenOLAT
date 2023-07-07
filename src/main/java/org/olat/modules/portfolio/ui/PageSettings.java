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
package org.olat.modules.portfolio.ui;

import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 17 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageSettings {
	
	private boolean withTaxonomy;
	private boolean withCategories;
	private boolean withBookmarks;
	private boolean withTitle;
	private boolean withImportContent;
	private MetadataHeader metadataHeader;
	private RepositoryEntry baseRepositoryEntry;
	
	private PageSettings() {
		//
	}
	
	public static PageSettings full(RepositoryEntry baseRepositoryEntry) {
		PageSettings settings = new PageSettings();
		settings.setWithCategories(true);
		settings.setWithTaxonomy(true);
		settings.setWithBookmarks(true);
		settings.setWithImportContent(true);
		settings.setMetadataHeader(MetadataHeader.FULL);
		settings.setBaseRepositoryEntry(baseRepositoryEntry);
		return settings;
	}
	
	public static PageSettings reduced(RepositoryEntry baseRepositoryEntry, boolean withTitle, boolean withImportContent) {
		PageSettings settings = new PageSettings();
		settings.setWithCategories(false);
		settings.setWithTaxonomy(false);
		settings.setWithBookmarks(false);
		settings.setWithTitle(withTitle);
		settings.setWithImportContent(withImportContent);
		settings.setMetadataHeader(MetadataHeader.REDUCED);
		settings.setBaseRepositoryEntry(baseRepositoryEntry);
		return settings;
	}
	
	public static PageSettings noHeader(RepositoryEntry baseRepositoryEntry) {
		PageSettings settings = new PageSettings();
		settings.setWithCategories(false);
		settings.setWithTaxonomy(false);
		settings.setWithBookmarks(false);
		settings.setWithTitle(false);
		settings.setWithImportContent(false);
		settings.setMetadataHeader(MetadataHeader.NONE);
		settings.setBaseRepositoryEntry(baseRepositoryEntry);
		return settings;
	}
	
	public boolean isWithTaxonomy() {
		return withTaxonomy;
	}
	
	public void setWithTaxonomy(boolean withTaxonomy) {
		this.withTaxonomy = withTaxonomy;
	}

	public boolean isWithCategories() {
		return withCategories;
	}

	public void setWithCategories(boolean withCategories) {
		this.withCategories = withCategories;
	}

	public boolean isWithBookmarks() {
		return withBookmarks;
	}

	public void setWithBookmarks(boolean withBookmarks) {
		this.withBookmarks = withBookmarks;
	}

	public boolean isWithTitle() {
		return withTitle;
	}

	public void setWithTitle(boolean withTitle) {
		this.withTitle = withTitle;
	}

	public MetadataHeader getMetadataHeader() {
		return metadataHeader;
	}

	public void setMetadataHeader(MetadataHeader metadataHeader) {
		this.metadataHeader = metadataHeader;
	}

	public boolean isWithImportContent() {
		return withImportContent;
	}

	public void setWithImportContent(boolean withImportContent) {
		this.withImportContent = withImportContent;
	}
	
	public RepositoryEntry getBaseRepositoryEntry() {
		return baseRepositoryEntry;
	}

	public void setBaseRepositoryEntry(RepositoryEntry baseRepositoryEntry) {
		this.baseRepositoryEntry = baseRepositoryEntry;
	}



	public enum MetadataHeader {
		FULL,
		REDUCED,
		NONE
	}
}
