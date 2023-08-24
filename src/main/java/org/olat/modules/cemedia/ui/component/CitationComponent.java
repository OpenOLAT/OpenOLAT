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
package org.olat.modules.cemedia.ui.component;

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.modules.ceditor.model.DublinCoreMetadata;
import org.olat.modules.cemedia.Citation;

/**
 * 
 * Initial date: 21.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CitationComponent extends FormBaseComponentImpl {
	
	private static final ComponentRenderer RENDERER = new CitationComponentRenderer();
	
	private Citation citation;
	private DublinCoreMetadata dublinCoreMetadata;
	private final CitationElement element;
	
	public CitationComponent(String name) {
		this(name, null);
	}
	
	public CitationComponent(String name, CitationElement element) {
		super(name);
		this.element = element;
	}
	
	@Override
	public FormItem getFormItem() {
		return element;
	}

	public Citation getCitation() {
		return citation;
	}

	public void setCitation(Citation citation) {
		this.citation = citation;
	}

	public DublinCoreMetadata getDublinCoreMetadata() {
		return dublinCoreMetadata;
	}

	public void setDublinCoreMetadata(DublinCoreMetadata dublinCoreMetadata) {
		this.dublinCoreMetadata = dublinCoreMetadata;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
