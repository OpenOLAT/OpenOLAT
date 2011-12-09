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

package org.olat.core.commons.services.search.ui;

import org.olat.core.commons.services.search.ResultDocument;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.control.Controller;

public interface SearchController extends Controller {
	
	public String getSearchString();
	
	public void setSearchString(String searchString);
	
	public String getParentContext();

	public void setParentContext(String parentContext);

	public String getDocumentType();

	public void setDocumentType(String documentType);

	public String getResourceUrl();
	
	public void setResourceUrl(String resourceUrl);
	
	public boolean isResourceContextEnable();

	public void setResourceContextEnable(boolean resourceContextEnable);
	

	public FormItem getFormItem();
	
	public void gotoSearchResult(UserRequest ureq, ResultDocument document);
}
