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
package org.olat.modules.ceditor.ui.model;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.model.ContainerElement;

/**
 * 
 * Initial date: 11 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditorFragment {

	private PageElement element;
	private final PageElementHandler handler;

	private final String cmpId;
	private Controller editorPart;
	
	private Link saveLink;
	private Link deleteLink;
	private Link moveUpLink;
	private Link moveDownLink;
	private Link addElementAboveLink;
	private Link addElementBelowLink;

	public EditorFragment(PageElement element, PageElementHandler handler, String cmpId, Controller editorPart) {
		this.element = element;
		this.handler = handler;
		this.cmpId = cmpId;
		this.editorPart = editorPart;
	}
	
	public String getCmpId() {
		return cmpId;
	}
	
	public boolean isDroppable() {
		return !(element instanceof ContainerElement);
	}
	
	public boolean isDraggable() {
		return true;
	}
	
	public String getElementId() {
		return element.getId();
	}

	public PageElement getPageElement() {
		return element;
	}
	
	public void setPageElement(PageElement element) {
		this.element = element;
	}
	
	public Component getComponent() {
		return editorPart.getInitialComponent();
	}

	public String getComponentName() {
		return getCmpId();
	}
	
	public Controller getEditorPart() {
		return editorPart;
	}
	
	public Link getAddElementAboveLink() {
		return addElementAboveLink;
	}

	public void setAddElementAboveLink(Link addElementAboveLink) {
		this.addElementAboveLink = addElementAboveLink;
	}

	public Link getAddElementBelowLink() {
		return addElementBelowLink;
	}

	public void setAddElementBelowLink(Link addElementBelowLink) {
		this.addElementBelowLink = addElementBelowLink;
	}

	public Link getSaveLink() {
		return saveLink;
	}

	public void setSaveLink(Link saveLink) {
		this.saveLink = saveLink;
	}

	public Link getDeleteLink() {
		return deleteLink;
	}

	public void setDeleteLink(Link deleteLink) {
		this.deleteLink = deleteLink;
	}

	public Link getMoveUpLink() {
		return moveUpLink;
	}

	public void setMoveUpLink(Link moveUpLink) {
		this.moveUpLink = moveUpLink;
	}

	public Link getMoveDownLink() {
		return moveDownLink;
	}

	public void setMoveDownLink(Link moveDownLink) {
		this.moveDownLink = moveDownLink;
	}
	
	public String getType() {
		return handler.getType();
	}
	
	public String getTypeCssClass() {
		return handler.getIconCssClass();
	}

	public PageElementHandler getHandler() {
		return handler;
	}

	@Override
	public int hashCode() {
		return element.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof EditorFragment) {
			EditorFragment eFragment = (EditorFragment)obj;
			return element != null && element.equals(eFragment.getPageElement());
		}
		return false;
	}
}
