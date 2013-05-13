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
package org.olat.core.gui.components.image;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.media.MediaResource;

/**
 * 
 * Initial date: 10.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImageFormItem extends FormItemImpl {

	private final ImageComponent imageComponent;
	
	public ImageFormItem(String name) {
		super(name);
		imageComponent = new ImageComponent(name + "-cmp");
	}

	@Override
	protected ImageComponent getFormItemComponent() {
		return imageComponent;
	}
	
	public void setMediaResource(MediaResource mediaResource) {
		imageComponent.setMediaResource(mediaResource);
	}
	
	public void setMaxWithAndHeightToFitWithin(int maxWidth, int maxHeight) {
		imageComponent.setMaxWithAndHeightToFitWithin(maxWidth, maxHeight);
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
