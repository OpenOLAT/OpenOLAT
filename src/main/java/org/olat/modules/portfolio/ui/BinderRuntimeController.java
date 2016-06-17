package org.olat.modules.portfolio.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController;

/**
 * 
 * Initial date: 17.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderRuntimeController extends RepositoryEntryRuntimeController {
	
	public BinderRuntimeController(UserRequest ureq, WindowControl wControl, RepositoryEntry re,
			RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);
	}
	
	

}
