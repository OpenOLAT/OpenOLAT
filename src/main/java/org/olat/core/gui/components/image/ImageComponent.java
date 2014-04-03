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
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.apache.commons.io.IOUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.image.Size;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class ImageComponent extends AbstractComponent {
	private static final ComponentRenderer RENDERER = new ImageRenderer();
	private static final OLog log = Tracing.createLoggerFor(ImageComponent.class);
	
	private MediaResource mediaResource;

	private Size realSize;
	private Size scaledSize;
	private float scalingFactor;
	private boolean cropSelectionEnabled = false;

	/**
	 * @param name
	 */
	public ImageComponent(String name) {
		super(name);
	}

	/**
	 * @see org.olat.core.gui.components.Component#dispatchRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String ri = ureq.getParameter("ri");
		if("1".equals(ri)) {
			// our tasks now: deliver the descriptor to the picture we want to display
			// and which made our nice buddy, the renderer, embedded into html
			ureq.getDispatchResult().setResultingMediaResource(mediaResource);
		}
	}

	public boolean isCropSelectionEnabled() {
		return cropSelectionEnabled;
	}
	
	public void setCropSelectionEnabled(boolean enable) {
		cropSelectionEnabled = enable;
	}

	/**
	 * @return Long
	 */
	public Size getScaledSize() {
		return scaledSize;
	}
	
	public Size getRealSize() {
		return realSize;
	}
	
	public float getScalingFactor() {
		return scalingFactor;
	}

	/**
	 * Sets the image to be delivered. The image can be
	 * delivered several times. Don't set a resource which
	 * can be only send once. 
	 * 
	 * @param mediaResource
	 */
	public void setMediaResource(MediaResource mediaResource) {
		setDirty(true);
		this.mediaResource = mediaResource;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		if(isCropSelectionEnabled()) {
			vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/jquery/imagecrop/jquery.imagecrop.js");
		}
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

		try {
			String type = mediaResource.getContentType();
			String suffix = getSuffix(type);

			Size size = null;
			if(StringHelper.containsNonWhitespace(suffix)) {
				size = getImageSize(suffix);
			}
			if(size == null) {
				size = getImageSizeFallback();
			}
			if(size == null) {
				return;
			}

			int realWidth = size.getWidth();
			int realHeight = size.getHeight();
			
			// calculate scaling factor
			scalingFactor = 1f;
			if (realWidth > maxWidth) {
				float scalingWidth = 1f / realWidth * maxWidth;
				scalingFactor = (scalingWidth <  scalingFactor ? scalingWidth : scalingFactor);
			}
			if (realHeight > maxHeight) {
				float scalingHeight = 1f / realHeight * maxHeight;
				scalingFactor = (scalingHeight < scalingFactor ? scalingHeight : scalingFactor);
			}
			realSize = new Size(realWidth, realHeight, false);
			scaledSize = new Size(Math.round(realWidth * scalingFactor), Math.round(realHeight * scalingFactor), false);
			
			setDirty(true);
		} catch (Exception e) {
			// log error, don't do anything else
			log.error("Problem while setting image size to fit " + maxWidth + "x" + maxHeight + " for resource::" + mediaResource, e);
		} 
	}
	
	private String getSuffix(String contentType) {
		contentType = contentType.toLowerCase();
		if(contentType.indexOf("jpg") >= 0 || contentType.indexOf("jpeg") >= 0) {
			return "jpg";
		}
		if(contentType.indexOf("gif") >= 0) {
			return "gif";
		}
		if(contentType.indexOf("png") >= 0) {
			return "png";
		}
		return null;
	}
	
	private Size getImageSizeFallback() {
		BufferedInputStream fileStrean = null;
		BufferedImage imageSrc = null;
		try {
			fileStrean = new BufferedInputStream(mediaResource.getInputStream());
			imageSrc = ImageIO.read(fileStrean);
			if (imageSrc == null) {
				// happens with faulty Java implementation, e.g. on MacOSX
				return null;
			}
			double realWidth = imageSrc.getWidth();
			double realHeight = imageSrc.getHeight();
			return new Size((int)realWidth, (int)realHeight, 0, 0, false);
		} catch (IOException e) {
			// log error, don't do anything else
			log.error("Problem while setting image size to fit for resource::" + mediaResource, e);
			return null;
		} finally {
			IOUtils.closeQuietly(fileStrean);
			if (imageSrc != null) {
				imageSrc.flush();
			}
		}
	}
	
	private Size getImageSize(String suffix) {
		Size result = null;
		Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
		if (iter.hasNext()) {
			ImageReader reader = iter.next();
			try {
				ImageInputStream stream = new MemoryCacheImageInputStream(mediaResource.getInputStream());
				reader.setInput(stream);
				int readerMinIndex = reader.getMinIndex();
				int width = reader.getWidth(readerMinIndex);
				int height = reader.getHeight(readerMinIndex);
				result = new Size(width, height, 0, 0, false);
			} catch (IOException e) {
				log.error(e.getMessage());
			} finally {
				reader.dispose();
			}
		} else {
			log.error("No reader found for given format: " + suffix);
		}
		return result;
	}
}