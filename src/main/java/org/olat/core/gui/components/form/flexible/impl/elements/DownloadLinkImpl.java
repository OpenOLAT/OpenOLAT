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
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DownloadComponent;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 06.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DownloadLinkImpl extends FormItemImpl implements DownloadLink {
	
	private final DownloadComponent downloadCmp;
	
	public DownloadLinkImpl(String name) {
		super(name);
		downloadCmp = new DownloadComponent(name, this);
	}

	@Override
	public String getLinkText() {
		return downloadCmp.getLinkText();
	}

	@Override
	public void setLinkText(String linkText) {
		downloadCmp.setLinkText(linkText);
	}

	@Override
	public void setLinkToolTip(String linkToolTip) {
		downloadCmp.setLinkToolTip(linkToolTip);
	}

	@Override
	public void setIconLeftCSS(String iconCSS) {
		downloadCmp.setLinkCssIconClass(iconCSS);
	}

	@Override
	public void setDownloadItem(File downloadItem) {
		downloadCmp.setDownloadItem(downloadItem);
	}

	@Override
	public void setDownloadItem(VFSLeaf downloadItem) {
		downloadCmp.setDownloadItem(downloadItem, true);
	}

	@Override
	public void setDownloadMedia(MediaResource mediaResource) {
		downloadCmp.setMediaResource(mediaResource);
	}

	@Override
	protected Component getFormItemComponent() {
		return downloadCmp;
	}
	
	@Override
	public void doDispatchFormRequest(UserRequest ureq) {
		downloadCmp.doDownload(ureq);
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
