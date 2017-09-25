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

import java.io.Serializable;

import org.olat.modules.edubase.BookSection;

/**
 *
 * Initial date: 23.06.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class BookSectionImpl implements BookSection, Serializable {

	private static final long serialVersionUID = 6590799780069735569L;

	private String bookId;
	private String title;
	private Integer pageFrom;
	private Integer pageTo;
	private String description;
	private String coverUrl;
	private Integer position;

	@Override
	public String getBookId() {
		return bookId;
	}

	@Override
	public void setBookId(String bookId) {
		this.bookId = bookId;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public Integer getPageFrom() {
		return pageFrom;
	}

	@Override
	public void setPageFrom(Integer pageFrom) {
		this.pageFrom = pageFrom;
	}

	@Override
	public Integer getPageTo() {
		return pageTo;
	}

	@Override
	public void setPageTo(Integer pageTo) {
		this.pageTo = pageTo;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getCoverUrl() {
		return coverUrl;
	}

	@Override
	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}

	@Override
	public Integer getPosition() {
		return position;
	}

	@Override
	public void setPosition(Integer position) {
		this.position = position;
	}
}
