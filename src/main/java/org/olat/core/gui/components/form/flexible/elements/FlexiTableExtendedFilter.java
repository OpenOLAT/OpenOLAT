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
package org.olat.core.gui.components.form.flexible.elements;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 16 juil. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface FlexiTableExtendedFilter {

	/**
	 * @return The identifier of the filter
	 */
	public String getFilter();
	
	public String getLabel();
	
	/**
	 * 
	 * @param withHtml true if tags are allowed to beautify the output
	 * @return The label with the selected values.
	 */
	public String getDecoratedLabel(boolean withHtml);
	
	public String getDecoratedLabel(Object value, boolean withHtml);

	public boolean isDefaultVisible();
	
	public boolean isSelected();
	
	
	public String getValue();

	public List<String> getValues();

	public void setValue(Object val);
	
	
	public void reset();
	
	/**
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @return
	 */
	public Controller getController(UserRequest ureq, WindowControl wControl, Translator translator);
	
	public Controller getController(UserRequest ureq, WindowControl wControl, Translator translator, Object preselectedValue);

}
