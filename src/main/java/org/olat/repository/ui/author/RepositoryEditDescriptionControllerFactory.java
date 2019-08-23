package org.olat.repository.ui.author;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.RepositoryEntry;

/**
 * @author Martin Schraner
 */
public interface RepositoryEditDescriptionControllerFactory {

	RepositoryEditDescriptionController create(UserRequest userRequest,
											   WindowControl windowControl,
											   RepositoryEntry repositoryEntry);
}
