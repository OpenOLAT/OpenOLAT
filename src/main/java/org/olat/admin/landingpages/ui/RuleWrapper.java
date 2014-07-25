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
package org.olat.admin.landingpages.ui;

import org.olat.admin.landingpages.model.Rule;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;

/**
 * 
 * Initial date: 15.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RuleWrapper {

	private final Rule rule;
	private int position;

	private SingleSelection roleEl;
	private SingleSelection attrNameEl;
	private TextElement attrValueEl;
	private TextElement landingPageEl;
	private FormLink landingPageChooser;
	
	public RuleWrapper(Rule rule) {
		this.rule = rule;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Rule getRule() {
		return rule;
	}

	public SingleSelection getRoleEl() {
		return roleEl;
	}

	public void setRoleEl(SingleSelection roleEl) {
		this.roleEl = roleEl;
	}

	public SingleSelection getAttrNameEl() {
		return attrNameEl;
	}

	public void setAttrNameEl(SingleSelection attrNameEl) {
		this.attrNameEl = attrNameEl;
	}

	public TextElement getAttrValueEl() {
		return attrValueEl;
	}

	public void setAttrValueEl(TextElement attrValueEl) {
		this.attrValueEl = attrValueEl;
	}

	public TextElement getLandingPageEl() {
		return landingPageEl;
	}

	public void setLandingPageEl(TextElement landingPageEl) {
		this.landingPageEl = landingPageEl;
	}
	
	public FormLink getLandingPageChooser() {
		return landingPageChooser;
	}

	public void setLandingPageChooser(FormLink landingPageChooser) {
		this.landingPageChooser = landingPageChooser;
	}

	public Rule save() {
		Rule sRule = new Rule();
		if(roleEl.isOneSelected()) {
			sRule.setRole(roleEl.getSelectedKey());
		}
		if(attrNameEl.isOneSelected()) {
			sRule.setUserAttributeKey(attrNameEl.getSelectedKey());
			sRule.setUserAttributeValue(attrValueEl.getValue());
		}
		sRule.setLandingPath(landingPageEl.getValue());
		return sRule;
	}
}
