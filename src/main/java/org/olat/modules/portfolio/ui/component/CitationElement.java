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
package org.olat.modules.portfolio.ui.component;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.modules.ceditor.model.DublinCoreMetadata;
import org.olat.modules.portfolio.Citation;

/**
 * 
 * Initial date: 13.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CitationElement extends FormItemImpl {
	
	private final CitationComponent component;
	
	public CitationElement(String name) {
		super(name);
		component = new CitationComponent(name.concat("_CMP"), this);
	}
	
	public Citation getCitation() {
		return component.getCitation();
	}

	public void setCitation(Citation citation) {
		component.setCitation(citation);
	}

	public DublinCoreMetadata getDublinCoreMetadata() {
		return component.getDublinCoreMetadata();
	}

	public void setDublinCoreMetadata(DublinCoreMetadata dublinCoreMetadata) {
		component.setDublinCoreMetadata(dublinCoreMetadata);
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		//
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}

	@Override
	public void reset() {
		//
	}
}
