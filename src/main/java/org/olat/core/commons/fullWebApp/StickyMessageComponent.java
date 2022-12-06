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
package org.olat.core.commons.fullWebApp;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.control.ScreenMode;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * The goal of this wrapper is the paranoidally prevent
 * concurrent modification exception of the main map
 * of our velocity container;
 * 
 * Initial date: 08.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class StickyMessageComponent extends AbstractComponent {
	
	private static final StickyMessageRenderer RENDERER = new StickyMessageRenderer();
	
	private String text;
	private Component delegateComponent;
	
	private final ScreenMode screenMode;
	
	public StickyMessageComponent(String name, ScreenMode screenMode) {
		super(name);
		this.screenMode = screenMode;
	}

	public boolean isMessages() {
		return (delegateComponent != null && delegateComponent.isVisible())
				|| StringHelper.containsNonWhitespace(text);
	}
	
	public ScreenMode getScreenMode() {
		return screenMode;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		if((this.text == null && text != null)
			|| (this.text != null && text == null)
			|| (this.text != null && !this.text.equals(text))) {
			this.text = text; 
			setDirty(true);
		}
	}

	public Component getDelegateComponent() {
		return delegateComponent;
	}

	public void setDelegateComponent(Component component) {
		if(this.delegateComponent != component) {
			delegateComponent = component;
			setDirty(true);
		}
	}

	@Override
	public boolean isDirty() {
		return (delegateComponent != null && delegateComponent.isDirty()) || super.isDirty();
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	private static class StickyMessageRenderer extends DefaultComponentRenderer {

		@Override
		public void renderComponent(Renderer renderer, StringOutput sb, Component source,
				URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
			
			StickyMessageComponent cmp = (StickyMessageComponent)source;
			if(cmp.isMessages()) {
				sb.append("<div id='o_msg_sticky' class='o_scrollblock clearfix")
				  .append(" o_msg_sticky_fullscreen", cmp.getScreenMode().isFullScreen())
				  .append("'><i class='o_icon o_icon_info_msg'> </i> ");
				
				Component delegate = cmp.getDelegateComponent();
				if(delegate != null && delegate.isVisible()) {
					renderer.render(delegate, sb, args);
				} else if(StringHelper.containsNonWhitespace(cmp.getText())) {
					sb.append(cmp.getText());
				}	
				sb.append("</div>");
			}
		}
	}
}
