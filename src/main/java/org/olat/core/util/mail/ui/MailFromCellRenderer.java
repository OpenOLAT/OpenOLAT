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
package org.olat.core.util.mail.ui;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.user.UserManager;

/**
 * 
 * Description:<br>
 * Render the from with link to visiting card or group name or email
 * 
 * <P>
 * Initial Date:  7 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MailFromCellRenderer implements CustomCellRenderer {

	private static final AtomicInteger counter = new AtomicInteger(100);
	
	private final Translator translator;
	private VelocityContainer container;
	private final Controller listeningController;
	private final UserManager userManager;
	
	public MailFromCellRenderer(Controller listeningController, VelocityContainer container, Translator translator) {
		this.listeningController = listeningController;
		this.container = container;
		this.translator = translator;
		userManager = CoreSpringFactory.getImpl(UserManager.class);
	}

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		if(val instanceof Identity identity) {
			String fullName = userManager.getUserDisplayName(identity);
			if(renderer == null) {
				sb.appendHtmlEscaped(fullName);
			} else {
				Link link = LinkFactory.createLink("bp_" + counter.incrementAndGet(), container, listeningController);
				link.setCustomDisplayText(StringHelper.escapeHtml(fullName));
				link.setUserObject("[Identity:" + identity.getKey() + "]");
				URLBuilder ubu = renderer.getUrlBuilder().createCopyFor(link);
				RenderResult renderResult = new RenderResult();
				link.getHTMLRendererSingleton().render(renderer, sb, link, ubu, translator, renderResult, null);
				link.setDirty(false);
			}
		} else if (val instanceof String string) {
			sb.append("<span>").appendHtmlEscaped(string).append("</span>");
		}
	}
}
