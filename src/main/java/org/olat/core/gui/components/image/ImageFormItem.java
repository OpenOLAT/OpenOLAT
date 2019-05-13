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

import java.io.File;

import org.olat.core.commons.services.image.Crop;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 10.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImageFormItem extends FormItemImpl {
	
	private static final Logger log = Tracing.createLoggerFor(ImageFormItem.class);

	private final ImageComponent imageComponent;
	
	private Crop cropSelection;
	
	public ImageFormItem(UserSession usess, String name) {
		super(name);
		imageComponent = new ImageComponent(usess, name + "-cmp");
	}

	@Override
	protected ImageComponent getFormItemComponent() {
		return imageComponent;
	}
	
	public void setMedia(VFSLeaf mediaResource) {
		imageComponent.setMedia(mediaResource);
	}
	
	public void setMedia(VFSLeaf mediaResource, String mimeType) {
		imageComponent.setMedia(mediaResource, mimeType);
	}
	
	public void setMedia(File mediaResource) {
		imageComponent.setMedia(mediaResource);
	}
	
	public void setMaxWithAndHeightToFitWithin(int maxWidth, int maxHeight) {
		imageComponent.setMaxWithAndHeightToFitWithin(maxWidth, maxHeight);
	}
	
	public boolean isCropSelectionEnabled() {
		return imageComponent.isCropSelectionEnabled();
	}
	
	public void setCropSelectionEnabled(boolean enable) {
		imageComponent.setCropSelectionEnabled(enable);
	}
	
	public Crop getCropSelection() {
		if(cropSelection == null) return null;
		float scalingFactor = imageComponent.getScalingFactor();
		if(scalingFactor <= 0f ||  Float.isNaN(scalingFactor)) return null;
		
		int x = cropSelection.getX();
		int y = cropSelection.getY();
		int width = cropSelection.getWidth();
		int height = cropSelection.getHeight();
		//log.info("At: " + x + " : " + y + " size: " + width + " x" + height);
		
		Crop realCropSelection = new Crop();
		realCropSelection.setWidth(width);
		realCropSelection.setHeight(height);
		realCropSelection.setX(x);
		realCropSelection.setY(y);
		return realCropSelection;
	}

	@Override
	protected void rootFormAvailable() {
		//
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {	
		String imgId = "o_img" + imageComponent.getDispatchID();
		String x = getRootForm().getRequestParameter(imgId + "_x");
		String y = getRootForm().getRequestParameter(imgId + "_y");
		String w = getRootForm().getRequestParameter(imgId + "_w");
		String h = getRootForm().getRequestParameter(imgId + "_h");
		
		if(StringHelper.isLong(x) && StringHelper.isLong(y) && StringHelper.isLong(w) &&StringHelper.isLong(h)) {
			try {
				Crop c = new Crop();
				c.setX(Integer.parseInt(x));
				c.setY(Integer.parseInt(y));
				c.setWidth(Integer.parseInt(w));
				c.setHeight(Integer.parseInt(h));
				cropSelection = c;
			} catch (NumberFormatException e) {
				log.warn("", e);
				cropSelection = null;
			}
		} else {
			cropSelection = null;
		}
	}

	@Override
	public void reset() {
		//
	}
}
