package org.olat.repository.ui.author;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Martin Schraner
 */
@Component
public class RepositoryEditDescriptionControllerFactoryImpl implements RepositoryEditDescriptionControllerFactory {

	private final UserManager userManager;
	private final RepositoryService repositoryService;
	private final RepositoryManager repositoryManager;
	private final RepositoryEntryLifecycleDAO lifecycleDao;
	private final RepositoryHandlerFactory repositoryHandlerFactory;

	@Autowired
	public RepositoryEditDescriptionControllerFactoryImpl(UserManager userManager,
														  RepositoryService repositoryService,
														  RepositoryManager repositoryManager,
														  RepositoryEntryLifecycleDAO lifecycleDao,
														  RepositoryHandlerFactory repositoryHandlerFactory) {
		this.userManager = userManager;
		this.repositoryService = repositoryService;
		this.repositoryManager = repositoryManager;
		this.lifecycleDao = lifecycleDao;
		this.repositoryHandlerFactory = repositoryHandlerFactory;
	}

	@Override
	public RepositoryEditDescriptionController create(UserRequest userRequest,
													  WindowControl windowControl,
													  RepositoryEntry repositoryEntry) {
		return new RepositoryEditDescriptionController(
				userRequest,
				windowControl,
				repositoryEntry
		);
	}

}
