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

package org.olat.core.commons.services.search.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.search.ResultDocument;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;

/**
 * Description:<br>

 * 
 * <P>
 * Initial Date:  8 dec. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public interface SearchServiceUIFactory {

	public SearchController createInputController(UserRequest ureq, WindowControl wControl, DisplayOption displayOption, Form mainForm);
	
	public SearchController createSearchController(UserRequest ureq, WindowControl wControl);
	
	public ResultController createController(UserRequest ureq, WindowControl wControl, Form mainForm, ResultDocument document);
	
	public String getBusinessPathLabel(String token, List<String> allTokens, Locale locale);
	
	public enum DisplayOption {
		BUTTON,//only search symbol
		BUTTON_WITH_LABEL,//search symbol with label
		STANDARD,//text field and search symbol
		STANDARD_TEXT,//text field and search BUTTON
	}
}
