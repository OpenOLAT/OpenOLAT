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
package org.olat.core.gui.components.figurecards;

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;

/**
 *
 * Initial date: Oct 27, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FigureCardsComponent extends FormBaseComponentImpl implements ComponentCollection {
	
	private static final ComponentRenderer RENDERER = new FigureCardsRenderer();
	
	private final FigureCardsItem element;
	private List<Component> cards = List.of();
	
	public FigureCardsComponent(FigureCardsItem element) {
		super(element.getFormItemId(), element.getName());
		this.element = element;
	}
	
	@Override
	public FormItem getFormItem() {
		return element;
	}
	
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	@Override
	public Component getComponent(String name) {
		return cards.stream().filter(card -> card.getComponentName().equals(name)).findFirst().orElse(null);
	}
	
	@Override
	public Iterable<Component> getComponents() {
		return cards;
	}
	
	public List<Component> getCards() {
		return cards;
	}
	
	public void setCards(List<Component> cards) {
		this.cards = cards;
		setDirty(true);
	}
	
}
