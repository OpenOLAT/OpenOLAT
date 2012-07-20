package org.olat.group.ui.main;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.layout.GenericMainController;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupMainController extends GenericMainController implements Activateable2 {

	public BusinessGroupMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		init(ureq);
		getMenuTree().setRootVisible(false);
		addCssClassToMain("o_groups");
	}

	/**
	 * @see org.olat.core.gui.control.generic.layout.GenericMainController#handleOwnMenuTreeEvent(java.lang.Object,
	 *      org.olat.core.gui.UserRequest)
	 */
	@Override
	protected Controller handleOwnMenuTreeEvent(Object uobject, UserRequest ureq) {
		return null;
	}

	@Override
	public void activate(UserRequest ureq, String viewIdentifier) {
		super.activate(ureq, viewIdentifier);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		super.activate(ureq, entries, state);
	}
}