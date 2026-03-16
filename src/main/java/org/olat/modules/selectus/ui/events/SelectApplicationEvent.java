/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.events;

import java.util.List;

import org.olat.core.gui.control.Event;
import org.olat.core.id.context.ContextEntry;

import org.olat.modules.selectus.model.Application;

/**
 * 
 * Initial date: 18.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectApplicationEvent extends Event {

	private static final long serialVersionUID = 3608238449487479174L;
	private final Application application;
	private final List<ContextEntry> activation;
	
	public SelectApplicationEvent(Application application) {
		this(application, null);
	}
	
	public SelectApplicationEvent(Application application, List<ContextEntry> activation) {
		super("select-app-result");
		this.application = application;
		this.activation = activation;
	}

	public Application getApplication() {
		return application;
	}

	public List<ContextEntry> getActivation() {
		return activation;
	}
}
