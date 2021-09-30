package org.olat.modules.immunityproof.ui.event;

import org.olat.core.gui.control.Event;
import org.olat.modules.immunityproof.ImmunityProofContext;

public class ImmunityProofFoundEvent extends Event {

	private static final long serialVersionUID = 237293728920L;

	private final ImmunityProofContext context;

	public ImmunityProofFoundEvent(ImmunityProofContext context) {
		super("immunity_proof_added");

		this.context = context;
	}

	public ImmunityProofContext getContext() {
		return context;
	}

}
