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
package org.olat.modules.ceditor;

import java.util.List;

/**
 * 
 * Initial date: 04.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface PageEditorProvider extends PageProvider {
	
	public List<PageElementHandler> getCreateHandlers();
	
	public List<PageLayoutHandler> getCreateLayoutHandlers();
	
	public int indexOf(PageElement element);
	
	public PageElement appendPageElement(PageElement element);
	
	public PageElement appendPageElementAt(PageElement element, int index);
	
	public boolean isRemoveConfirmation(PageElement element);

	public String getRemoveConfirmationI18nKey();
	
	public void removePageElement(PageElement element);

	public void moveUpPageElement(PageElement element);
	
	public void moveDownPageElement(PageElement element);
	
	/**
	 * Move the specified element before the sibling element.
	 * @param element
	 * @param sibling The reference object
	 */
	public void movePageElement(PageElement elementToMove, PageElement sibling, boolean after);
	
	public String getImportButtonKey();
}
