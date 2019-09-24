package org.olat.repository.ui.list;

import org.olat.core.commons.persistence.DB;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryLifeCycleChangeControllerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
@Configuration
public class RepositoryEntryBeanFactory {

	private final DB dbInstance;

	private final RepositoryManager repositoryManager;

	private final RepositoryModule repositoryModule;

	protected final RepositoryService repositoryService;

	private final MapperService mapperService;

	@Autowired
	public RepositoryEntryBeanFactory(DB dbInstance,
									  RepositoryManager repositoryManager,
									  RepositoryModule repositoryModule,
									  RepositoryService repositoryService,
									  MapperService mapperService) {
		this.dbInstance = dbInstance;
		this.repositoryManager = repositoryManager;
		this.repositoryModule = repositoryModule;
		this.repositoryService = repositoryService;
		this.mapperService = mapperService;
	}

	@Bean(name={"row_1"})
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	@SuppressWarnings("SpringJavaAutowiringInspection")
	protected VelocityContainer createRow1VelocityContainer(BasicController caller) {
		VelocityContainer result = new VelocityContainer(null,
				"vc_" + "row_1",
				Util.getPackageVelocityRoot(RepositoryEntryBeanFactory.class) + "/row_1.html",
				Util.createPackageTranslator(caller.getClass(), caller.getLocale()),
				caller
		);
		result.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		return result;
	}

	@Bean(name={"repositoryEntryRowFactory"})
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	@SuppressWarnings("SpringJavaAutowiringInspection")
	protected RepositoryEntryRowsFactory createRepositoryEntryRowFactory(UserRequest userRequest) {
		return new RepositoryEntryRowsFactoryImpl(repositoryManager, repositoryModule, mapperService, userRequest);
	}

	@Bean(name={"repositoryEntryLifeCycleChangeControllerFactory"})
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	@SuppressWarnings("SpringJavaAutowiringInspection")
	protected RepositoryEntryLifeCycleChangeControllerFactory repositoryEntryLifeCycleChangeControllerFactory(UserRequest userRequest,
																											  WindowControl windowControl,
																											  RepositoryEntrySecurity repositoryEntrySecurity,
																											  RepositoryHandler repositoryHandler) {
		return new RepositoryEntryLifeCycleChangeControllerFactory(userRequest, windowControl, repositoryEntrySecurity, repositoryHandler);
	}
}
