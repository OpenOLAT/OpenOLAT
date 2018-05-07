package org.olat.user.ui.organisation;

import org.olat.basesecurity.manager.InstitutionNamesToOrganisationMigrator;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InstitutionMigratorController extends FormBasicController {
	
	private FormLink migrateButton;
	
	@Autowired
	private InstitutionNamesToOrganisationMigrator migrator;
	
	public InstitutionMigratorController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "migrator");
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		migrateButton = uifactory.addFormLink("migrate", formLayout, Link.BUTTON);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(migrateButton == source) {
			doMigrate(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doMigrate(UserRequest ureq) {
		migrateButton.setEnabled(false);
		migrateButton.setIconLeftCSS("o_icon o_icon_pending o_icon-spin");
		migrator.migrate();
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
