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
package org.olat.core.commons.services.doceditor.onlyoffice.model;

import org.olat.core.commons.services.doceditor.onlyoffice.Permissions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 
 * Initial date: 22 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@JsonInclude(Include.NON_NULL)
public class PermissionsImpl implements Permissions {

	private boolean comment;
	private boolean download;
	private boolean edit;
	private boolean print;
	private boolean follForms;
	private boolean review;
	
	@Override
	public boolean getComment() {
		return comment;
	}

	public void setComment(boolean comment) {
		this.comment = comment;
	}

	@Override
	public boolean getDownload() {
		return download;
	}

	public void setDownload(boolean download) {
		this.download = download;
	}

	@Override
	public boolean getEdit() {
		return edit;
	}
	
	public void setEdit(boolean edit) {
		this.edit = edit;
	}

	@Override
	public boolean getPrint() {
		return print;
	}

	public void setPrint(boolean print) {
		this.print = print;
	}

	@Override
	public boolean getFollForms() {
		return follForms;
	}

	public void setFollForms(boolean follForms) {
		this.follForms = follForms;
	}

	@Override
	public boolean getReview() {
		return review;
	}

	public void setReview(boolean review) {
		this.review = review;
	}
}
