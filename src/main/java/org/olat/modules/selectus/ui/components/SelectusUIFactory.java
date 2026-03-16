package org.olat.modules.selectus.ui.components;

import java.util.Collections;

import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.TextElement;

/**
 * 
 */
public class SelectusUIFactory {
	
	public SelectusUIFactory() {
		//
	}
	
	public static final ReflectionStaticElement addReflectionStaticText(String name, String i18nLabel, TextElement elementToCopy, FormItemContainer formLayout) {
		ReflectionStaticElementImpl reflectEl = new ReflectionStaticElementImpl(name);
		if(elementToCopy != null) {
			reflectEl.setTextElements(Collections.singletonList(elementToCopy));
		}
		FormUIFactory.setLabelIfNotNull(i18nLabel, reflectEl);
		if(formLayout != null) {
			formLayout.add(reflectEl);
		}
		return reflectEl;
	}

}
