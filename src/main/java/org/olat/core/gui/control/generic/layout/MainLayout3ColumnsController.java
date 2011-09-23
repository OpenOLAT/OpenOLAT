package org.olat.core.gui.control.generic.layout;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;

public interface MainLayout3ColumnsController extends MainLayoutController {

	/**
	 * Add a controller to this layout controller that should be cleaned up when
	 * this layout controller is diposed. In most scenarios you should hold a
	 * reference to the content controllers that controll the col1, col2 or col3,
	 * but in rare cases this is not the case and you have no local reference to
	 * your controller. You can then use this method to add your controller. At
	 * the dispose time of the layout controller your controller will be disposed
	 * as well.
	 * 
	 * @param toBedisposedControllerOnDispose
	 */
	public abstract void addDisposableChildController(Controller toBedisposedControllerOnDispose);

	/**
	 * Add a css class to the #b_main wrapper div, e.g. for special background
	 * formatting
	 * 
	 * @param cssClass
	 */
	public abstract void addCssClassToMain(String cssClass);

	/**
	 * Remove a CSS class from the #b_main wrapper div
	 * @param cssClass
	 */
	public abstract void removeCssClassFromMain(String cssClass);

	/**
	 * Temporarily hide the column 1 withour removing the component
	 * @param hide The column to hide
	 */
	public abstract void hideCol1(boolean hide);

	/**
	 * Temporarily hide the column 2 withour removing the component
	 * @param hide The column to hide
	 */
	public abstract void hideCol2(boolean hide);

	/**
	 * Temporarily hide the column 3 withour removing the component
	 * @param hide The column to hide
	 */
	public abstract void hideCol3(boolean hide);

	/**
	 * Set a new component to this column or null to not use this column
	 * @param col1Component
	 */
	public abstract void setCol1(Component col1Component);

	/**
	 * Set a new component to this column or null to not use this column
	 * @param col2Component
	 */
	public abstract void setCol2(Component col2Component);

	/**
	 * Set a new component to this column or null to not use this column
	 * @param col3Component
	 */
	public abstract void setCol3(Component col3Component);

}