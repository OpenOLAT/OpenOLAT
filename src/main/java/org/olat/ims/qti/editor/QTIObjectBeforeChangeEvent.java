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
*/

package org.olat.ims.qti.editor;

import org.olat.core.gui.control.Event;

/**
 * Initial Date:  Nov 28, 2005 <br>
 *
 * @author patrick
 */
public class QTIObjectBeforeChangeEvent extends Event {

	private static final long serialVersionUID = -8628375483709082132L;
	private String content;
	private String id;

	/**
	 * @param command
	 */
	public QTIObjectBeforeChangeEvent() {
		super("qtiObjBeforeChangeEvent");
	}
	/**
	 * initialize this event object, previous values are lost. may be used to recycle an object in loop.
	 * @param id
	 * @param content
	 */
	public void init(String id, String content) {
		this.id=id;
		this.content=content;
	}
	/**
	 * @return Returns the content.
	 */
	public String getContent() {
		return content;
	}
	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}
}
