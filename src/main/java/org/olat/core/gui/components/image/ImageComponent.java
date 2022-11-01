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

import java.io.File;
import java.util.Collections;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class ImageComponent extends AbstractComponent implements Disposable {
	private static final ComponentRenderer RENDERER = new ImageRenderer();
	private static final Logger log = Tracing.createLoggerFor(ImageComponent.class);
	
	private VFSLeaf media;
	private String mimeType;

	private String alt;
	private String cssClasses;
	private final MapperKey mapperUrl;
	private final VFSMediaMapper mapper;

	// optional in case of video: poster image
	private VFSLeaf poster;
	private MapperKey posterMapperUrl;
	private VFSMediaMapper posterMapper;

	private Size realSize;
	private Size scaledSize;
	private float scalingFactor;
	private boolean divImageWrapper = true;
	private boolean cropSelectionEnabled = false;
	private boolean preventBrowserCaching = true;
	
	private final MapperService mapperService;

	/**
	 * 
	 * @param usess The user session
	 * @param name The name of the component
	 */
	public ImageComponent(UserSession usess, String name) {
		super(name);
		mapper = new VFSMediaMapper();
		String mapperId = UUID.randomUUID().toString();
		mapperService = CoreSpringFactory.getImpl(MapperService.class);
		mapperUrl = mapperService.register(usess, mapperId, mapper);
		// optional poster frame for videos
		posterMapper = new VFSMediaMapper();
		posterMapperUrl = mapperService.register(usess, mapperId + "-poster", posterMapper);		
		// renderer provides own DOM ID
		setDomReplacementWrapperRequired(false);
	}

	public String getAlt() {
		return alt;
	}

	public void setAlt(String alt) {
		this.alt = alt;
	}

	public boolean isPreventBrowserCaching() {
		return preventBrowserCaching;
	}

	public void setPreventBrowserCaching(boolean preventBrowserCaching) {
		this.preventBrowserCaching = preventBrowserCaching;
	}

	public boolean isDivImageWrapper() {
		return divImageWrapper;
	}

	public void setDivImageWrapper(boolean divImageWrapper) {
		this.divImageWrapper = divImageWrapper;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	public boolean isCropSelectionEnabled() {
		return cropSelectionEnabled;
	}
	
	public void setCropSelectionEnabled(boolean enable) {
		cropSelectionEnabled = enable;
	}

	public String getCssClasses() {
		return cssClasses;
	}

	public void setCssClasses(String cssClasses) {
		this.cssClasses = cssClasses;
	}

	/**
	 * @return Long
	 */
	public Size getScaledSize() {
		return scaledSize;
	}
	
	public Size getRealSize() {
		if(realSize == null) {
			String suffix = getSuffix(getMimeType());
			if(StringHelper.containsNonWhitespace(suffix)) {
				if(suffix.equalsIgnoreCase("jpg") || suffix.equalsIgnoreCase("png") || suffix.equalsIgnoreCase("jpeg")  || suffix.equalsIgnoreCase("gif")) {
					realSize = CoreSpringFactory.getImpl(ImageService.class).getSize(media, suffix);
				} else if(suffix.equalsIgnoreCase("mp4") || suffix.equalsIgnoreCase("m4v") || suffix.equalsIgnoreCase("flv"))  {
					realSize = CoreSpringFactory.getImpl(MovieService.class).getSize(media, suffix);
				}
			}
		}
		return realSize;
	}
	
	public float getScalingFactor() {
		return scalingFactor;
	}
	
	public VFSLeaf getMedia() {
		return media;
	}

	public VFSLeaf getPoster() {
		return poster;
	}
	
	@Override
	public void dispose() {
		if(mapper != null) {
			mapperService.cleanUp(Collections.<MapperKey>singletonList(mapperUrl));
		}
		if(posterMapper != null) {
			mapperService.cleanUp(Collections.<MapperKey>singletonList(posterMapperUrl));
		}
	}

	/**
	 * Sets the image to be delivered. The image can be
	 * delivered several times. Don't set a resource which
	 * can be only send once. 
	 * 
	 * @param mediaResource
	 */
	public void setMedia(VFSLeaf media) {
		setDirty(true);
		this.media = media;
		this.mimeType = null;
		mapper.setMediaFile(media);
		realSize = null;
	}
	
	public void setMedia(VFSLeaf media, String mimeType) {
		setDirty(true);
		this.media = media;
		this.mimeType = mimeType;
		mapper.setMediaFile(media);
		realSize = null;
	}
	
	public void setMedia(File mediaFile) {
		setDirty(true);
		setMedia(new LocalFileImpl(mediaFile));
	}

	public void setPoster(VFSLeaf poster) {
		setDirty(true);
		this.poster = poster;
		posterMapper.setMediaFile(poster);
	}
	
	public String getMapperUrl() {
		return mapperUrl.getUrl();
	}

	public String getPosterMapperUrl() {
		return posterMapperUrl.getUrl();
	}
	
	public String getMimeType() {
		if(mimeType != null) {
			return mimeType;
		}
		if(media == null) {
			return null;
		}
		return WebappHelper.getMimeType(media.getName());
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		if(isCropSelectionEnabled()) {
			vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/jquery/cropper/cropper.min.js");
		}
		
		String type = getMimeType();
		if(type != null && type.startsWith("video/")) {
			// Preload media element
			vr.getJsAndCSSAdder().addRequiredStaticJsFile("movie/mediaelementjs/mediaelement-and-player.min.js");
			String css = StaticMediaDispatcher.getStaticURI("movie/mediaelementjs/mediaelementplayer.min.css");
			vr.getJsAndCSSAdder().addRequiredCSSPath(css, false, 1);
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
		if (media == null || !media.exists()) {
			scalingFactor = Float.NaN;
			realSize = null;
			scaledSize = null;
			return;
		}

		try {
			Size size = getRealSize();
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
			log.error("Problem while setting image size to fit {}x{} for resource::{}", maxWidth, maxHeight, media, e);
		} 
	}
	
	protected String getSuffix(String contentType) {
		if(!StringHelper.containsNonWhitespace(contentType)) return null;
		
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
		if(contentType.indexOf("png") >= 0) {
			return "png";
		}
		if(contentType.indexOf("m4v") >= 0) {
			return "m4v";
		}
		if(contentType.indexOf("mp4") >= 0) {
			return "mp4";
		}
		if(contentType.indexOf("webm") >= 0) {
			return "webm";
		}
		if(contentType.indexOf("webp") >= 0) {
			return "webp";
		}
		if(contentType.indexOf("flv") >= 0) {
			return "flv";
		}
		return null;
	}
	
}