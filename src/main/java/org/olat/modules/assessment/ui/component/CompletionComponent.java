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
package org.olat.modules.assessment.ui.component;

import java.util.Locale;

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.assessment.ui.AssessedIdentityListController;

/**
 * 
 * Initial date: 22 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CompletionComponent extends FormBaseComponentImpl {
	
	private static final ComponentRenderer RENDERER = new CompletionRenderer();
	
	private final CompletionItem completionItem;
	
	private Double completion = 0.0;
	private boolean ended = false;
	private final Translator completionTranslator;
	
	public CompletionComponent(String name, CompletionItem completionItem, Locale locale) {
		super(name);
		this.completionItem = completionItem;
		setDomReplacementWrapperRequired(false);
		completionTranslator = Util.createPackageTranslator(AssessedIdentityListController.class, locale);
		setTranslator(completionTranslator);
	}

	public boolean isEnded() {
		return ended;
	}

	public void setEnded(boolean ended) {
		this.ended = ended;
		setDirty(true);
	}

	public Double getCompletion() {
		return completion;
	}

	public void setCompletion(Double completion) {
		this.completion = completion;
		setDirty(true);
	}

	public CompletionItem getCompletionItem() {
		return completionItem;
	}

	public CompletionItem getFormItem() {
		return completionItem;
	}
	
	public Translator getCompletionTranslator() {
		return completionTranslator;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

}
