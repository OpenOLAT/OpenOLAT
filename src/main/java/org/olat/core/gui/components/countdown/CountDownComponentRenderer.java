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
package org.olat.core.gui.components.countdown;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 08.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class CountDownComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		
		CountDownComponent cmp = (CountDownComponent)source;
		sb.append("<span id='o_c").append(cmp.getDispatchID()).append("'>");
		
		Long countDown = cmp.getCountDown();
		cmp.setCurrentRenderedTime(countDown);
		if(countDown != null) {
			String i18nKey = cmp.getI18nKey();
			if(countDown.intValue() == 0 && cmp.getI18nKeyZero() != null) {
				i18nKey = cmp.getI18nKeyZero();
			} else if(countDown.intValue() <= 1 && cmp.getI18nKeySingular() != null) {
				i18nKey = cmp.getI18nKeySingular();
			}
			
			if(i18nKey == null) {
				sb.append(countDown);
			} else {
				sb.append(translator.translate(i18nKey, countDown.toString()));
			}
		}
		sb.append("</span>");
	}
}
