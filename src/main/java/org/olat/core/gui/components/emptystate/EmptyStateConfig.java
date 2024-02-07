/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.emptystate;

/**
 * 
 * Initial date: 22 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface EmptyStateConfig {
	
	static EmptyStateConfigBuilder builder() {
		return new EmptyStateConfigBuilder();
	}

	public String getIconCss();

	public String getIndicatorIconCss();
	
	public String getMessageI18nKey();

	public String[] getMessageI18nArgs();
	
	public String getMessageTranslated();

	public String getHintI18nKey();

	public String[] getHintI18nArgs();

	/**
	 * if translation happens before passing the value, retrieve it and don't translate it afterwards
	 * @return translated hint
	 */
	public String getHintTranslated();

	/**
	 * retrieve i18n key for the description
	 * @return i18n key for the description
	 */
	public String getDescI18nKey();

	/**
	 * should be used alongside with getDescI18nKey()
	 * @return i18n args for the description
	 */
	public String[] getDescI18nArgs();

	/**
	 * if translation happens before passing the value, retrieve it and don't translate it afterwards
	 * @return translated description
	 */
	public String getDescTranslated();

	public String getButtonI18nKey();
	
	public String getSecondaryButtonI18nKey();

}
