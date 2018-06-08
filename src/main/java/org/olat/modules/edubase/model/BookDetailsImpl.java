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
package org.olat.modules.edubase.model;

import org.olat.core.util.StringHelper;
import org.olat.modules.edubase.BookDetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * Initial date: 21.08.2017<br>
 *
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class BookDetailsImpl implements BookDetails {

	private String coverUrl;
	private String documentCoverUrl;
	private String authors;
	private String documentAuthors;
	private String title;
	private String documentTitle;
	private String subtitle;
	private String documentSubtitle;
	private String publisherName;
	private String documentPublisherName;
	private String edition;
	private String documentEdition;
	private String numberOfPages;
	private String documentNumberOfPages;
	private String isbn;
	private String documentIsbn;
	private String description1;
	private String documentDescription1;

	@Override
	public String getCoverUrl() {
		return StringHelper.containsNonWhitespace(coverUrl) ? coverUrl : documentCoverUrl;
	}

	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}

	public void setDocumentCoverUrl(String documentCoverUrl) {
		this.documentCoverUrl = documentCoverUrl;
	}

	@Override
	public String getAuthors() {
		return StringHelper.containsNonWhitespace(authors)? authors: documentAuthors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public void setDocumentAuthors(String documentAuthors) {
		this.documentAuthors = documentAuthors;
	}

	@Override
	public String getTitle() {
		return StringHelper.containsNonWhitespace(title) ? title : documentTitle;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
	}

	@Override
	public String getSubtitle() {
		return StringHelper.containsNonWhitespace(subtitle) ? subtitle : documentSubtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public void setDocumentSubtitle(String documentSubtitle) {
		this.documentSubtitle = documentSubtitle;
	}

	@Override
	public String getPublisher() {
		return StringHelper.containsNonWhitespace(publisherName) ? publisherName : documentPublisherName;
	}

	public void setPublisherName(String publisher) {
		this.publisherName = publisher;
	}

	public void setDocumentPublisherName(String documentPublisher) {
		this.documentPublisherName = documentPublisher;
	}

	@Override
	public String getEdition() {
		return StringHelper.containsNonWhitespace(edition) ? edition : documentEdition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public void setDocumentEdition(String documentEdition) {
		this.documentEdition = documentEdition;
	}

	@Override
	public String getNumberOfPages() {
		return StringHelper.containsNonWhitespace(numberOfPages) ? numberOfPages : documentNumberOfPages;
	}

	public void setNumberOfPages(String numberOfPages) {
		this.numberOfPages = numberOfPages;
	}

	public void setDocumentNumberOfPages(String documentNumberOfPages) {
		this.documentNumberOfPages = documentNumberOfPages;
	}

	@Override
	public String getIsbn() {
		return StringHelper.containsNonWhitespace(isbn)? isbn: documentIsbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public void setDocumentIsbn(String documentIsbn) {
		this.documentIsbn = documentIsbn;
	}

	@Override
	public String getDescription() {
		return StringHelper.containsNonWhitespace(description1)? description1: documentDescription1;
	}

	public void setDescription1(String description1) {
		this.description1 = description1;
	}

	public void setDocumentDescription1(String documentDescription1) {
		this.documentDescription1 = documentDescription1;
	}


}
