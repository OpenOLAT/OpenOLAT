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
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.impl.elements.AddRemoveElementImpl.AddRemoveMode;

/**
 * Initial date: Nov 5, 2020<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public interface AddRemoveElement extends FormItemCollection, FormItem {
	
	/**
	 * @return The "add" link
	 */
	public FormLink getAddLink();
	
	/**	
	 * @return The "remove" link
	 */
	public FormLink getRemoveLink();
	
	/**
	 * @return True if "add" was selected, elsewise return false
	 */
	public boolean isAddSelected();
	
	/**
	 * @return True if "remove" was selected, elsewise return false
	 */
	public boolean isRemoveSelected();
	
	/**
	 * @return True if neither "add" nor "remove" was selected, elsewise return false
	 */
	public boolean isNoneSelected();
	
	/**
	 * @return True if "add" is selected, false if "remove" is selected, null if none is selcted
	 */
	public Boolean getSelection();
	
	/**
	 * Set the selected button. 
	 * 		-> true:	"add" is selected
	 * 		-> false: 	"remove" is selected
	 * 		-> null: 	nothing is selected
	 * 
	 * @param selection
	 */
	public void setSelection(Boolean selection);
	
	/**
	 * Select "add"
	 */
	public void selectAdd();
	
	/**
	 * Select "remove"
	 */
	public void selectRemove();
	
	/**
	 * Set whether a text next to the button should be shown. 
	 * Default is false. Default texts are "Add" and "Remove", translated to the user's language.
	 * 
	 * @param showText
	 */
	public void setShowText(boolean showText);
	
	/**
	 * @return True if a text is shown next to the buttons, otherwise false.
	 */
	public boolean isTextShown();
	
	/**
	 * Set a custom css class which is applied, when the add-component is active (clicked).
	 * Used to set other color and styles. 
	 * Is only applied, if DisplayMode.ADD_REMOVE is set!
	 * 
	 * @param cssClass
	 */
	public void setAddActiveCssClass(String cssClass);
	
	/**
	 * @return Get the css class which will be applied, when the add-component is active (clicked).
	 */
	public String getAddActiveCssClass();
	
	/**
	 * Set a custom css class which is applied, when the remove-component is active (clicked).
	 * Used to set other color and styles.
	 * Is only applied, if DisplayMode.ADD_REMOVE is set!
	 * 
	 * @param cssClass
	 */
	public void setRemoveActiveCssClass(String cssClass);
	
	/**
	 * @return Get the css class which will be applied, when the remove-component is active (clicked).
	 */
	public String getRemoveActiveCssClass();
	
	/**
	 * Set the text, which is shown next to the add-icon. 
	 * Also apply setShowText(true);
	 * 
	 * @param text
	 */
	public void setAddText(String text);
	
	/**
	 * @return The text which is shown next to the add icon
	 */
	public String getAddText();
	
	/**
	 * Set a title, which is shown when hovering over the component. 
	 * Also used as aria-label. 
	 * Not translated! Provide an already translated string.
	 * @param title
	 */
	public void setAddTitle(String title);
	
	/**
	 * @return Title and aria-label
	 */
	public String getAddTitle();
	
	/**
	 * Set the text, which is shown next to the remove-icon. 
	 * Also apply setShowText(true);
	 * 
	 * @param text
	 */
	public void setRemoveText(String text);
	
	/**
	 * @return The text which is shown next to the remove icon
	 */
	public String getRemoveText();
	
	/**
	 * Set a title, which is shown when hovering over the component. 
	 * Also used as aria-label. 
	 * Not translated! Provide an already translated string.
	 * @param title
	 */
	public void setRemoveTitle(String title);
	
	/**
	 * @return Title and aria-label
	 */
	public String getRemoveTitle();
	
	/**
	 * Set the icon which is used on the add component.
	 * The default is a +. Just enter the icon, "o_icon o_icon_fw" will be applied automatically.
	 * 
	 * @param icon
	 */
	public void setAddIcon(String icon);
	
	/**
	 * @return The icon which is used on the add component
	 */
	public String getAddIcon();
	
	/**
	 * Set the icon which is used on the remove component.
	 * The default is a +. Just enter the icon, "o_icon o_icon_fw" will be applied automatically.
	 * 
	 * @param icon
	 */
	public void setRemoveIcon(String icon);
	
	/**
	 * @return The icon which is used on the remove component
	 */
	public String getRemoveIcon();
	
	/**
	 * Set the display mode to: 
	 * 		- ADD_REMOVE: Buttons to add and remove will be shown
	 * 		- ADD_ONLY: A single check box will be shown, use selectAdd() or isAddSelected()
	 * 		- REMOVE_ONLY: A single check box will be shown, use selectRemove() or isRemoveSelected()
	 * @param displayMode
	 */
	public void setAddRemoveMode(AddRemoveMode displayMode);
	
	/**
	 * @return The current display mode
	 */
	public AddRemoveMode getDisplayMode();
	
	/**
	 * Enable or disable only the "add" button
	 * 
	 * @param addEnabled
	 */
	public void setAddEnabled(boolean addEnabled);
	
	/**
	 * @return True if the "add" button enabled, otherwise false 
	 */
	public boolean isAddEnabled();
	
	/**
	 * Enable or disable only the "remove" button
	 * 
	 * @param addEnabled
	 */
	public void setRemoveEnabled(boolean removeEnabled);
	
	/**
	 * @return True if the "remove" button enabled, otherwise false 
	 */
	public boolean isRemoveEnabled();
}
