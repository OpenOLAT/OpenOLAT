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
package org.olat.core.gui.control.generic.iframe;

import java.io.Serializable;

import org.olat.core.util.vfs.VFSItem;

/**
 * 
 * @author srosse
 *
 */
public class SerializableIFrameDeliveryMapper extends IFrameDeliveryMapper implements Serializable {

	private static final long serialVersionUID = 8710796223152048613L;
	
	public SerializableIFrameDeliveryMapper() {
		//for XStream
	}
	
	public SerializableIFrameDeliveryMapper(VFSItem rootDir, boolean rawContent, boolean enableTextmarking,
			String frameId, String customCssURL, String themeBaseUri, String customHeaderContent) {
		super(rootDir, rawContent, enableTextmarking, frameId, customCssURL, themeBaseUri, customHeaderContent);
	}
}
