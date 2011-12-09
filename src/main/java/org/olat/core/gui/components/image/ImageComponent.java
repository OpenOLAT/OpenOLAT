/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.gui.components.image;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class ImageComponent extends Component {
	private static final ComponentRenderer RENDERER = new ImageRenderer();
	private static final OLog log = Tracing.createLoggerFor(ImageComponent.class);
	
	private MediaResource mediaResource;
	private Long width, height;

	/**
	 * @param name
	 */
	public ImageComponent(String name) {
		super(name);
	}

	/**
	 * @see org.olat.core.gui.components.Component#dispatchRequest(org.olat.core.gui.UserRequest)
	 */
	protected void doDispatchRequest(UserRequest ureq) {
		// our tasks now: deliver the descriptor to the picture we want to display
		// and which made our nice buddy, the renderer, embedded into html
		MediaResource mr = mediaResource; // FIXME:fj: clone this, since mr not made
																			// to deliver repeatedly
		ureq.getDispatchResult().setResultingMediaResource(mr);
	}

	/**
	 * @return Long
	 */
	public Long getHeight() {
		return height;
	}

	/**
	 * @return Long
	 */
	public Long getWidth() {
		return width;
	}

	/**
	 * Sets the height.
	 * 
	 * @param height The height to set
	 */
	public void setHeight(Long height) {
		setDirty(true);
		this.height = height;
	}

	/**
	 * Sets the width.
	 * 
	 * @param width The width to set
	 */
	public void setWidth(Long width) {
		setDirty(true);
		this.width = width;
	}

	/**
	 * sets the image to be delivered
	 * 
	 * @param mediaResource
	 */
	public void setMediaResource(MediaResource mediaResource) {
		setDirty(true);
		this.mediaResource = mediaResource;
	}

	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	
	/**
	 * Call this method to display the image within a given box of width and
	 * height. The method does NOT manipulate the image itself, it does only
	 * adjust the images width and height tag. <br />
	 * The image will made displayed smaller, it will not enlarge the image since
	 * this always looks bad. The scaling is done in a way to get an image that is
	 * smaller than the maxWidth or smaller than the maxHeight, depending on whith
	 * of the sizes produce a smaller scaling factor. <br />
	 * To scale an image on the filesystem to another width and height, use the
	 * ImageHelper.scaleImage() method.
	 * 
	 * @param maxWidth
	 * @param maxHeight
	 */
	public void setMaxWithAndHeightToFitWithin(int maxWidth, int maxHeight) {
		if (mediaResource == null || mediaResource.getInputStream() == null) {
			throw new AssertException("Set media resource to a valid value befor calling scaleToFit::" + mediaResource);
		}
		BufferedInputStream fileStrean = null;
		BufferedImage imageSrc = null;
		try {
			fileStrean = new BufferedInputStream(mediaResource.getInputStream());
			imageSrc = ImageIO.read(fileStrean);
			if (imageSrc == null) {
				// happens with faulty Java implementation, e.g. on MacOSX
				return;
			}
			double realWidth = imageSrc.getWidth();
			double realHeight = imageSrc.getHeight();
			// calculate scaling factor
			double scalingFactor = 1;
			if (realWidth > maxWidth) {
				double scalingWidth = 1 / realWidth * maxWidth;
				scalingFactor = ( scalingWidth <  scalingFactor ? scalingWidth : scalingFactor);
			}
			if (realHeight > maxHeight) {
				double scalingHeight = 1 / realHeight * maxHeight;
				scalingFactor = ( scalingHeight < scalingFactor ? scalingHeight : scalingFactor);
			}
			setHeight(new Long( Math.round(realHeight * scalingFactor)));
			setWidth(new Long( Math.round(realWidth * scalingFactor)));
		} catch (IOException e) {
			// log error, don't do anything else
			log.error("Problem while setting image size to fit " + maxWidth + "x" + maxHeight + " for resource::" + mediaResource, e);
		} finally {
			// release all resources
			if (fileStrean != null) {
				try {
					fileStrean.close();
				} catch (IOException e) {
					log.error("Problem while closing file stream for resource::" + mediaResource, e);
				}
			}
			if (imageSrc != null) {
				imageSrc.flush();
			}
		}
	}
}