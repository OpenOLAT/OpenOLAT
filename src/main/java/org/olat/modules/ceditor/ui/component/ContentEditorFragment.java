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
package org.olat.modules.ceditor.ui.component;

import org.olat.core.gui.components.ComponentCollection;
import org.olat.modules.ceditor.PageElement;

/**
 * 
 * Initial date: 9 déc. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ContentEditorFragment extends ComponentCollection {
	
	public String getElementId();
	
	public PageElement getElement();
	
	public boolean isEditMode();
	
	public void setEditMode(boolean editMode);
	
	public boolean isInspectorVisible();
	
	public void setInspectorVisible(boolean visible);
	
	public boolean isCloneable();
	
	public void setCloneable(boolean cloneable);
	
	public boolean isDeleteable();
	
	public void setDeleteable(boolean enable);
	
	public boolean isMoveable();
	
	public void  setMoveable(boolean enable);
	
	public boolean isEditable();
	
}
