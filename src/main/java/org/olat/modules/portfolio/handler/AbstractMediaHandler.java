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
package org.olat.modules.portfolio.handler;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.MediaInformations;
import org.olat.modules.portfolio.MediaLight;
import org.olat.modules.portfolio.model.MediaPart;
import org.olat.modules.portfolio.ui.editor.PageRunControllerElement;
import org.olat.modules.portfolio.ui.editor.PageElement;
import org.olat.modules.portfolio.ui.editor.PageElementHandler;
import org.olat.modules.portfolio.ui.editor.PageRunElement;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractMediaHandler implements MediaHandler, PageElementHandler {
	
	private final String type;
	
	public AbstractMediaHandler(String type) {
		this.type = type;
	}

	@Override
	public String getType() {
		return type;
	}
	
	@Override
	public String getIconCssClass(MediaLight media) {
		return getIconCssClass();
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof Media) {
			return new PageRunControllerElement(getMediaController(ureq, wControl, (Media)element));
		}
		if(element instanceof MediaPart) {
			MediaPart mediaPart = (MediaPart)element;
			return new PageRunControllerElement(getMediaController(ureq, wControl, mediaPart.getMedia()));
		}
		return null;
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof Media) {
			return getMediaController(ureq, wControl, (Media)element);
		}
		if(element instanceof MediaPart) {
			MediaPart mediaPart = (MediaPart)element;
			return getMediaController(ureq, wControl, mediaPart.getMedia());
		}
		return null;
	}

	public final class Informations implements MediaInformations {
		
		private final String title;
		private final String description;
		
		public Informations(String title, String description) {
			this.title = title;
			this.description = description;
		}
		
		@Override
		public String getType() {
			return AbstractMediaHandler.this.getType();
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public String getDescription() {
			return description;
		}
	}
}
