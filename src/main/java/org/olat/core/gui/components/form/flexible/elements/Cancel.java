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

import org.olat.core.gui.components.form.flexible.FormItem;

/**
 * Description:<br>
 * The cancel button offers a way to not submit any data and to reset the form
 * 
 * <P>
 * Initial Date: 06.07.2009 <br>
 * 
 * @author gnaegi
 */
public interface Cancel extends FormItem {

	/**
	 * @param customEnabledLinkCSS
	 *            The customEnabledLinkCSS to set.
	 */
	public void setCustomEnabledLinkCSS(String customEnabledLinkCSS);

	/**
	 * Set the css that is used for the disabled link status
	 * 
	 * @param customDisabledLinkCSS
	 */
	public void setCustomDisabledLinkCSS(String customDisabledLinkCSS);

	/**
	 * Set the i18n key for the link text
	 * 
	 * @param i18n
	 */
	public void setI18nKey(String i18n);

}