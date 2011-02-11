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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.gui.formelements;

/**
 * 
 * Initial Date: 06.10.2004
 * @author Felix Jost
 */
public class PopupData {
	private String textelementId, buttonaction, buttonlabelkey;
	private int popupwidth, popupheight;

	/**
	 * @param textelementId
	 * @param buttonaction
	 * @param buttonlabelkey
	 * @param popupwidth
	 * @param popupheight
	 */
	public PopupData(String textelementId, String buttonaction, String buttonlabelkey, int popupwidth, int popupheight) {
		this.textelementId = textelementId;
		this.buttonaction = buttonaction;
		this.buttonlabelkey = buttonlabelkey;
		this.popupwidth = popupwidth;
		this.popupheight = popupheight;
	}

	/**
	 * @return
	 */
	public String getButtonaction() {
		return buttonaction;
	}

	/**
	 * @param buttonaction
	 */
	public void setButtonaction(String buttonaction) {
		this.buttonaction = buttonaction;
	}

	/**
	 * @return
	 */
	public String getButtonlabelkey() {
		return buttonlabelkey;
	}

	/**
	 * @param buttonlabelkey
	 */
	public void setButtonlabelkey(String buttonlabelkey) {
		this.buttonlabelkey = buttonlabelkey;
	}

	/**
	 * @return
	 */
	public int getPopupheight() {
		return popupheight;
	}

	/**
	 * @param popupheight
	 */
	public void setPopupheight(int popupheight) {
		this.popupheight = popupheight;
	}

	/**
	 * @return
	 */
	public int getPopupwidth() {
		return popupwidth;
	}

	/**
	 * @param popupwidth
	 */
	public void setPopupwidth(int popupwidth) {
		this.popupwidth = popupwidth;
	}

	/**
	 * @return
	 */
	public String getTextelementId() {
		return textelementId;
	}

	/**
	 * @param textelementId
	 */
	public void setTextelementId(String textelementId) {
		this.textelementId = textelementId;
	}
}

