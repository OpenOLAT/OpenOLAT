/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.gui.components.expand;

import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;

/**
 * 
 * Initial date: Sep 3, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface FormExpandButton extends FormItem {
	
	public void setText(String text);
	
	public void setEscapeMode(EscapeMode escapeMode);

	public void setTitle(String title);

	public void setCssClass(String cssClass);

	public void setIconLeftExpandedCss(String iconLeftExpandedCss);

	public void setIconLeftCollapsedCss(String iconLeftCollapsedCss);

	public void setIconRightExpandedCss(String iconRightExpandedCss);

	public void setIconRightCollapsedCss(String iconRightCollapsedCss);

	public void setAriaLabel(String ariaLabel);
	
	public void setAriaHasPopup(String ariaHasPopup);
	
	public void setAriaControls(String ariaControls);
	
	public boolean isExpanded();

	public void setExpanded(boolean expanded);

}
