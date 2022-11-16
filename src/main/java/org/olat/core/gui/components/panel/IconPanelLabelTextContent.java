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
package org.olat.core.gui.components.panel;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;

/**
 * 
 * Initial date: 16 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class IconPanelLabelTextContent extends AbstractComponent {
	
	private static final ComponentRenderer RENDERER = new IconPanelLabelTextlRenderer();
	
	private List<LabelText> labelTexts;
	private String warning;

	public IconPanelLabelTextContent(String name) {
		super(name);
	}

	public List<LabelText> getLabelTexts() {
		return labelTexts;
	}

	public void setLabelTexts(List<LabelText> labelTexts) {
		this.labelTexts = labelTexts;
		setDirty(true);
	}

	public String getWarning() {
		return warning;
	}

	public void setWarning(String warning) {
		this.warning = warning;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	public static final class LabelText {
		
		private final String label;
		private final String text;
		
		public LabelText(String label, String text) {
			this.label = label;
			this.text = text;
		}
		
		public String getLabel() {
			return label;
		}
		
		public String getText() {
			return text;
		}
		
	}

}
