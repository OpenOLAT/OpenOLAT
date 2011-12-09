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

package org.olat.ims.qti.editor.beecom.objects;

import org.olat.core.util.CodeHelper;

/**
 * @author rkulow
 */
public class Response {

	private String ident = null;
	private Material content = new Material();
	private boolean correct = false;
	private float points = 0;

	public Response() {
		ident = "" + CodeHelper.getRAMUniqueID();
	}

	/**
	 * Returns the content.
	 * 
	 * @return String
	 */
	public Material getContent() {
		return content;
	}

	/**
	 * Returns the ident.
	 * 
	 * @return String
	 */
	public String getIdent() {
		return ident;
	}

	/**
	 * Sets the content.
	 * 
	 * @param content The content to set
	 */
	public void setContent(Material content) {
		this.content = content;
	}

	/**
	 * Sets the ident.
	 * 
	 * @param ident The ident to set
	 */
	public void setIdent(String ident) {
		this.ident = ident;
	}

	/**
	 * Returns the correct.
	 * 
	 * @return boolean
	 */
	public boolean isCorrect() {
		return correct;
	}

	/**
	 * Sets the correct.
	 * 
	 * @param correct The correct to set
	 */
	public void setCorrect(boolean correct) {
		this.correct = correct;
	}

	/**
	 * @return
	 */
	public float getPoints() {
		return points;
	}

	/**
	 * @param f
	 */
	public void setPoints(float f) {
		points = f;
	}

	public void setPoints(String s) {
		if (s == null) return;
		try {
			points = Float.parseFloat(s);
		} catch (Exception e) {
			//
		}
	}
}