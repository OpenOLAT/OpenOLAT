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
package org.olat.core.gui.components.download;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;

/**
 * Description:<br>
 * The download component displays a link which when pressed triggers a file
 * download in a new window.
 * 
 * <P>
 * Initial Date: 09.12.2009 <br>
 * 
 * @author gnaegi
 */
public class DownloadComponent extends AbstractComponent {
	private static final ComponentRenderer RENDERER = new DownloadComponentRenderer();
	private MediaResource mediaResource;
	private String linkText;
	private String linkToolTip;
	private String linkCssIconClass;
	private DownloadLink delegate;
	
	/**
	 * Constructor for flexi element (or you need to set all properties yourself)
	 * @param name
	 */
	public DownloadComponent(String name, DownloadLink delegate) {
		super(name);
		this.delegate = delegate;
	}

	/**
	 * Constructor to create a download component that will use the file name as
	 * display text and the appropriate file icon
	 * 
	 * @param name
	 * @param downloadItem
	 */
	public DownloadComponent(String name, VFSLeaf downloadItem) {
		this(name, downloadItem, true, downloadItem.getName(), null,
				getCssIconClass(downloadItem.getName()));
	}

	public DownloadComponent(String name, File downloadItem, String linkText, String linkToolTip, String linkCssIconClass) {
		super(name);
		setDownloadItem(downloadItem);
		setLinkText(linkText);
		setLinkToolTip(linkToolTip);
		setLinkCssIconClass(linkCssIconClass);
	}

	/**
	 * Detailed constructor 
	 * 
	 * @param name
	 *            The component name
	 * @param downloadFile
	 *            The VFS item to be downloaded
	 * @param linkText
	 *            an optional link text
	 * @param linkToolTip
	 *            an optional tool tip (hover text over link)
	 * @param linkCssIconClass
	 *            an optional css icon class. Note that o_icon
	 *            will be added when this argument is used. Use the render
	 *            argument when you want to provide additional CSS classes.
	 */
	public DownloadComponent(String name, VFSLeaf downloadItem, boolean forceDownload,
			String linkText, String linkToolTip, String linkCssIconClass) {
		super(name);
		setDownloadItem(downloadItem, forceDownload);
		setLinkText(linkText);
		setLinkToolTip(linkToolTip);
		setLinkCssIconClass(linkCssIconClass);
		// renderer puts dispatch ID in a tag
		this.setDomReplacementWrapperRequired(false);
	}
	
	/**
	 * @param name The component name
	 * @param downloadItem The resource to download
	 * @param linkText An optional link text
	 * @param linkToolTip An optional tool tip
	 * @param linkCssIconClass An optional icon class
	 */
	public DownloadComponent(String name, MediaResource downloadItem, String linkText,
			String linkToolTip, String linkCssIconClass) {
		super(name);
		setMediaResource(downloadItem);
		setLinkText(linkText);
		setLinkToolTip(linkToolTip);
		setLinkCssIconClass(linkCssIconClass);
		// renderer puts dispatch ID in a tag
		this.setDomReplacementWrapperRequired(false);
	}
	
	public DownloadLink getFormItem() {
		return delegate;
	}

	/**
	 * @param downloadItem
	 *            the VFS item to download
	 */
	public void setDownloadItem(VFSLeaf downloadItem, boolean forceDownload) {
		if (downloadItem == null) {
			mediaResource = null;
			setLinkCssIconClass(null);
		} else {
			String css = CSSHelper.createFiletypeIconCssClassFor(downloadItem.getName());
			setLinkCssIconClass("o_icon o_icon-fw " + css);
			
			VFSMediaResource mResource = new VFSMediaResource(downloadItem);
			if(forceDownload) {
				mResource.setDownloadable(forceDownload);
			}
			mediaResource = mResource;
		}
		setDirty(true);
	}
	
	public void setDownloadItem(File downloadItem) {
		if (downloadItem == null) {
			mediaResource = null;
			setLinkCssIconClass(null);
		} else {
			String css = CSSHelper.createFiletypeIconCssClassFor(downloadItem.getName());
			setLinkCssIconClass("o_icon o_icon-fw " + css);
			
			mediaResource = new FileMediaResource(downloadItem);
		}
		setDirty(true);
	}
	
	public void setMediaResource(MediaResource mediaResource) {
		this.mediaResource = mediaResource;
	}

	/**
	 * Package scope getter method for file download media resource
	 * 
	 * @return
	 */
	MediaResource getDownloadMediaResoruce() {
		return mediaResource;
	}

	/**
	 * @return The optional link text or NULL to only display an icon
	 */
	public String getLinkText() {
		return linkText;
	}

	/**
	 * @param linkText
	 */
	public void setLinkText(String linkText) {
		this.linkText = linkText;
		this.setDirty(true);
	}

	/**
	 * @return The optional link tooltip or NULL if not available
	 */
	public String getLinkToolTip() {
		return linkToolTip;
	}

	/**
	 * @param linkToolTip
	 *            The optional link tooltip or NULL if not available
	 */
	public void setLinkToolTip(String linkToolTip) {
		this.linkToolTip = linkToolTip;
		this.setDirty(true);
	}

	/**
	 * @return The link icon css class or NULL if no css should be used
	 */
	public String getLinkCssIconClass() {
		return linkCssIconClass;
	}

	/**
	 * @param linkCssIconClass
	 *            The link icon css class or NULL if no css should be used. Note
	 *            that o_icon will be added when this argument
	 *            is used. Use the render argument when you want to provide
	 *            additional CSS classes.
	 */
	public void setLinkCssIconClass(String linkCssIconClass) {
		this.linkCssIconClass = linkCssIconClass;
		this.setDirty(true);
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		doDownload(ureq);
	}
	
	public void doDownload(UserRequest ureq) {
		if (mediaResource != null) {
			ureq.getDispatchResult().setResultingMediaResource(mediaResource);
			setDirty(false);
		}
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	/**
	 * Helper method to create the css class for the given file type from
	 * brasato.css
	 * 
	 * @param fileName
	 * @return
	 */
	private static String getCssIconClass(String fileName) {
		int typePos = fileName.lastIndexOf(".");
		if (typePos > 0) {
			return "o_filetype_" + fileName.substring(typePos + 1);
		}
		return CSSHelper.createFiletypeIconCssClassFor(fileName);
	}

}
