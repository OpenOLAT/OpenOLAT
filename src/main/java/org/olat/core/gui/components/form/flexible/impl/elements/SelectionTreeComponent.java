/**
 * 
 */
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.Map;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.translator.Translator;

/**
 * @author patrickb
 *
 */
class SelectionTreeComponent extends FormBaseComponentImpl {

	private SelectionElement selectionElement;
	private TreeModel treeModel;
	private Map<String, Component> subComponents;
	private final static ComponentRenderer RENDERER = new SelectionTreeComponentRenderer(); 

	/**
	 * @param name
	 */
	public SelectionTreeComponent(String name, Translator translator, SelectionElement selectionElement, TreeModel treeModel) {
		super(name, translator);
		this.selectionElement = selectionElement;
		this.treeModel = treeModel;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.Component#getHTMLRendererSingleton()
	 */
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	SelectionElement getSelectionElement() {
		return selectionElement;
	}

	TreeModel getTreeModel() {
		return treeModel;
	}

	/**
	 * @param checkboxitems
	 */
	protected void setComponents(Map<String, Component> checkboxitems) {
		this.subComponents = checkboxitems;
	}

	protected Map<String, Component> getSubComponents(){
		return this.subComponents;
	}
	
}
