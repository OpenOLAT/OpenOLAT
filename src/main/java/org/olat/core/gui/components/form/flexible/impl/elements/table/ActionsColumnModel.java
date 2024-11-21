/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;

/**
 * 
 * Initial date: Nov 14, 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ActionsColumnModel extends StickyActionColumnModel {

	public ActionsColumnModel(FlexiColumnDef def) {
		super(def);
		setIconHeader("o_icon o_icon-fws o_icon-lg o_icon_actions");
		setAlwaysVisible(true);
		setExportable(false);
	}
	
	/*
	 * Convenience method to create a link for the actions column.
	 */
	public static final FormLink createLink(FormUIFactory uifactory, Translator translator) {
		return createLink(uifactory, translator, "tools");
	}
	
	/*
	 * Convenience method to create a link for the actions column.
	 */
	public static final FormLink createLink(FormUIFactory uifactory, Translator translator, String cmd) {
		FormLink toolsLink = uifactory.addFormLink("tools_" + CodeHelper.getRAMUniqueID(), cmd, "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon-fws o_icon-lg o_icon_actions");
		toolsLink.setTitle(translator.translate("action.more"));
		return toolsLink;
	}

}
