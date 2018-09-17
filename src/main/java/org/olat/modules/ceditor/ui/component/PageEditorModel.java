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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.control.Controller;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.model.ContainerElement;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.ui.model.EditorFragment;

/**
 * 
 * Initial date: 14 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageEditorModel {

	private final List<EditorFragment> fragments;
	
	public PageEditorModel(List<EditorFragment> fragments) {
		this.fragments = new ArrayList<>(fragments);
	}
	
	/**
	 * @return A copy of the model list.
	 */
	public List<EditorFragment> getFragments() {
		return new ArrayList<>(fragments);
	}
	
	public int indexOf(EditorFragment fragment) {
		return fragments.indexOf(fragment);
	}
	
	public int size() {
		return fragments.size();
	}
	
	public void add(int index, EditorFragment fragment) {
		fragments.add(index, fragment);
	}
	
	public boolean add(EditorFragment fragment) {
		return fragments.add(fragment);
	}
	
	public boolean remove(EditorFragment fragment) {
		return fragments.remove(fragment);
	}
	
	public boolean contains(EditorFragment fragment) {
		return fragments.contains(fragment);
	}
	
	public EditorFragment getFragmentByCmpId(String cmpId) {
		if(!StringHelper.containsNonWhitespace(cmpId)) return null;
		
		for(EditorFragment fragment:fragments) {
			if(fragment.getCmpId().equals(cmpId)) {
				return fragment;
			}
		}
		return null;
	}
	
	public String getContainerOfFragmentCmpId(String fragmentCmpId) {
		if(fragmentCmpId == null) return null;
		
		EditorFragment fragment = getFragmentByCmpId(fragmentCmpId);
		if(fragment == null) return null;
		
		String elementId = fragment.getElementId();
		for(EditorFragment f:fragments) {
			if(f.getPageElement() instanceof ContainerElement) {
				ContainerElement container = (ContainerElement)f.getPageElement();
				ContainerSettings settings = container.getContainerSettings();
				if(settings.getAllElementIds().contains(elementId)) {
					return f.getComponentName();
				}
			}
		}
		return null;
	}
	
	public EditorFragment getEditedFragment() {
		EditorFragment editedFragment = null;
		for(EditorFragment fragment:fragments) {
			if(fragment.isEditMode()) {
				editedFragment = fragment;
			}
		}
		return editedFragment;
	}
	
	public boolean isContainerized(EditorFragment fragment) {
		return getContainerOfFragmentCmpId(fragment.getComponentName()) != null;
	}
	
	public boolean isEditorPartController(Controller source) {
		for(EditorFragment fragment:fragments) {
			if(fragment.getEditorPart() == source) {
				return true;
			}
		}
		return false;
	}
	
	public EditorFragment getEditorFragment(Controller source) {
		for(EditorFragment fragment:fragments) {
			if(fragment.getEditorPart() == source) {
				return fragment;
			}
		}
		return null;
	}

}
