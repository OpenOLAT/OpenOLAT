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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;

/**
 * Description:<br>
 * The DisplayOrDownloadFileComponent offers a way to open a javascript popup
 * window and open a resource in this window. Note that this might be blocked by
 * the browser!
 * <p>
 * In case the browser can display the content, it will display it in the
 * browser. Otherwhise, it will start downloading the file.
 * <p>
 * A filedownload is only triggered once. If the component becomes visible a
 * second time, the filedownload will not be initiated again and again. A second
 * filedowload can be triggered by using the triggerFileDownload() method.
 * 
 * <P>
 * Initial Date: 05.11.2009 <br>
 * 
 * @author gnaegi
 */
public class DisplayOrDownloadComponent extends AbstractComponent {
	private static final ComponentRenderer RENDERER = new DisplayOrDownloadComponentRenderer();
	private String fileUrl = null;

	/**
	 * Constructor. Use the triggerFileDownload() method to set the file URL later
	 * to another URL
	 * 
	 * @param name the component name
	 * @param fileUrl a valid URL that points to a file to be downloaded. Note
	 *          that this controller does only open the URL in a new window which
	 *          triggers the download or display of the file. The dispatching of
	 *          the file is up to the caller of this method. NULL if not known at
	 *          the moment. Use triggerFileDownload() in such cases later on.
	 */

	public DisplayOrDownloadComponent(String name, String fileUrl) {
		super(name);
		this.fileUrl = fileUrl;
	}

	/**
	 * @see org.olat.core.gui.components.Component#doDispatchRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
	// nothing to dispatch
	}

	/**
	 * @see org.olat.core.gui.components.Component#getHTMLRendererSingleton()
	 */
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	/**
	 * Get the configured URL to be downloaded and mark the URL as consumed (set
	 * to null).
	 * 
	 * @return The URL or NULL if already consumed
	 */
	String consumeFileUrl() {
		String url = fileUrl;
		fileUrl = null;
		return url;
	}

	/**
	 * Set the file URL of the file that should be downloaded automatically. This
	 * method can optionally be used to the constructor when a) the fileURL is not
	 * yet known at construction time or b) you want to use this controller
	 * repeatedly to download different files.
	 * 
	 * @param fileUrl a valid URL that points to a file to be downloaded. Note
	 *          that this controller does only open the URL in a new window which
	 *          triggers the download or display of the file. The dispatching of
	 *          the file is up to the caller of this method.
	 */
	public void triggerFileDownload(String fileUrl) {
		this.fileUrl = fileUrl;
	}

}
