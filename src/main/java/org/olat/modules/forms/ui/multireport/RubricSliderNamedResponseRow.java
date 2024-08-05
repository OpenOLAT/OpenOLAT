/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.forms.ui.multireport;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;

/**
 * 
 * Initial date: 31 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RubricSliderNamedResponseRow {
	
	private final String user;
	private final boolean noResponse;
	
	private String comment;
	private Component ratingComponent;
	private SingleSelection noResponseComponent;
	
	public RubricSliderNamedResponseRow(String user, boolean noResponse) {
		this.user = user;
		this.noResponse = noResponse;
	}
	
	public String getUser() {
		return user;
	}
	
	public boolean isNoResponse() {
		return noResponse;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public Component getRatingComponent() {
		return ratingComponent;
	}
	
	public void setRatingComponent(Component component) {
		this.ratingComponent = component;
	}

	public SingleSelection getNoResponseComponent() {
		return noResponseComponent;
	}

	public void setNoResponseComponent(SingleSelection noResponseComponent) {
		this.noResponseComponent = noResponseComponent;
	}
}
