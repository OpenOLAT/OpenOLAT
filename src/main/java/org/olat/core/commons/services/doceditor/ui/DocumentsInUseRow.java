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
package org.olat.core.commons.services.doceditor.ui;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 26 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocumentsInUseRow extends UserPropertiesRow {
	
	private final Access access;

	public DocumentsInUseRow(Access access, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(access.getIdentity(), userPropertyHandlers, locale);
		this.access = access;
	}

	public String getFilename() {
		return access.getMetadata() != null? access.getMetadata().getFilename(): null;
	}

	public String getApp() {
		return access.getEditorType();
	}

	public Mode getMode() {
		return access.getMode();
	}

	public Date getOpened() {
		return access.getCreationDate();
	}
	
	public Date getEditStartDate() {
		return access.getEditStartDate();
	}

}
