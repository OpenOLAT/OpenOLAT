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
package org.olat.course.nodes.portfolio;

import java.util.Date;

import org.olat.core.gui.components.widget.Widget;
import org.olat.core.gui.components.widget.WidgetRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.portfolio.Binder;

/**
 * 
 * Initial date: 16 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class PortfolioWidget extends Widget {
	
	private static final WidgetRenderer RENDERER = new PortfolioWidgetRenderer();
	
	private String binderTitle;
	private Date copyDate;
	private Date returnDate;

	public PortfolioWidget(String name, Translator translator) {
		super(name, translator);
	}

	@Override
	protected WidgetRenderer getWidgetRenderer() {
		return RENDERER;
	}
	
	@Override
	public String getElementCssClass() {
		return "o_widget_main_binder " + (super.getElementCssClass() != null? super.getElementCssClass(): "");
	}

	public void setBinder(Binder binder) {
		binderTitle = binder.getTitle();
		copyDate = binder.getCopyDate();
		returnDate = binder.getReturnDate();
	}

	public String getBinderTitle() {
		return binderTitle;
	}

	public Date getCopyDate() {
		return copyDate;
	}

	public Date getReturnDate() {
		return returnDate;
	}

}
