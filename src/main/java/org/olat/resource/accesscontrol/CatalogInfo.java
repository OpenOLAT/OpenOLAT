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
package org.olat.resource.accesscontrol;

import java.util.function.Predicate;

public class CatalogInfo {
	
	public static final CatalogInfo UNSUPPORTED = new CatalogInfo(false, false, null, null, null, null);
	
	private final boolean catalogSupported;
	private final boolean showDetails;
	private final String details;
	private final Predicate<Offer> catalogVisibility;
	private final String editBusinessPath;
	private final String editLabel;
	
	public CatalogInfo(boolean catalogSupported, boolean showDetails, String details,
			Predicate<Offer> catalogVisibility, String editBusinessPath, String editLabel) {
		this.catalogSupported = catalogSupported;
		this.showDetails = showDetails;
		this.details = details;
		this.catalogVisibility = catalogVisibility;
		this.editBusinessPath = editBusinessPath;
		this.editLabel = editLabel;
	}

	public boolean isCatalogSupported() {
		return catalogSupported;
	}

	public boolean isShowDetails() {
		return showDetails;
	}

	public String getDetails() {
		return details;
	}

	public Predicate<Offer> getCatalogVisibility() {
		return catalogVisibility;
	}

	public String getEditBusinessPath() {
		return editBusinessPath;
	}

	public String getEditLabel() {
		return editLabel;
	}
	
}