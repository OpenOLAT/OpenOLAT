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

import java.util.Date;

import org.olat.modules.portfolio.Citation;
import org.olat.modules.portfolio.CitationSourceType;

/**
 * For XStream
 * 
 * Initial date: 21.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CitationXml implements Citation {
	
	private CitationSourceType itemType;
	
	private String edition;
	private String editor;
	private String volume;
	private String series;
	
	private String publicationTitle;
	private String issue;
	private String pages;

	private String institution;
	
	private String isbn;
	
	private Date lastVisit;

	@Override
	public CitationSourceType getItemType() {
		return itemType;
	}

	public void setItemType(CitationSourceType itemType) {
		this.itemType = itemType;
	}

	@Override
	public String getEdition() {
		return edition;
	}

	@Override
	public void setEdition(String edition) {
		this.edition = edition;
	}

	@Override
	public String getEditor() {
		return editor;
	}

	@Override
	public void setEditor(String editor) {
		this.editor = editor;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	@Override
	public String getVolume() {
		return volume;
	}

	@Override
	public void setVolume(String volume) {
		this.volume = volume;
	}

	@Override
	public String getSeries() {
		return series;
	}

	@Override
	public void setSeries(String series) {
		this.series = series;
	}

	@Override
	public String getPublicationTitle() {
		return publicationTitle;
	}

	@Override
	public void setPublicationTitle(String publicationTitle) {
		this.publicationTitle = publicationTitle;
	}

	@Override
	public String getIssue() {
		return issue;
	}

	@Override
	public void setIssue(String issue) {
		this.issue = issue;
	}

	@Override
	public String getPages() {
		return pages;
	}

	@Override
	public void setPages(String pages) {
		this.pages = pages;
	}

	@Override
	public String getInstitution() {
		return institution;
	}

	@Override
	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public Date getLastVisit() {
		return lastVisit;
	}

	public void setLastVisit(Date lastVisit) {
		this.lastVisit = lastVisit;
	}
	
	

}
