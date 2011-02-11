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
package org.olat.core.gui.components.form.flexible.elements;

import java.util.List;
import java.util.Map;

import org.olat.core.gui.components.form.flexible.FormItem;

/**
 * Description:<br>
 * TODO: rhaag Class Description for TextBoxListElement
 * 
 * <P>
 * Initial Date:  27.08.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public interface TextBoxListElement extends FormItem {

	public String getValue();
	
	public List<String> getValueList();

	public void setAutoCompleteContent(List<String> tagL);

	public void setNoFormSubmit(boolean noFormSubmit);

	public void setAutoCompleteContent(Map<String, String> tagM);

}
