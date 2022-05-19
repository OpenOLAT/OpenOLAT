package org.olat.modules.qpool.ui.admin;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.nodes.practice.PracticeResource;
import org.olat.course.nodes.practice.PracticeService;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.ui.QuestionsController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeletePoolController extends FormBasicController {
	
	private final Pool pool;
	
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private PracticeService practiceService;
	
	public ConfirmDeletePoolController(UserRequest ureq, WindowControl wControl, Pool pool) {
		super(ureq, wControl, "confirm_delete_pool", Util.createPackageTranslator(QuestionsController.class, ureq.getLocale()));
		this.pool = pool;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("message", translate("delete.pool.confirm", pool.getName()));
			
			List<PracticeResource> resources = practiceService.getResources(pool);
			if(resources.size() == 1) {
				layoutCont.contextPut("practice", translate("delete.pool.confirm.practice.singular", Integer.toString(resources.size())));	
			} else if(resources.size() > 1) {
				layoutCont.contextPut("practice", translate("delete.pool.confirm.practice.plural", Integer.toString(resources.size())));
				
			}
		}
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		FormSubmit deleteButton = uifactory.addFormSubmitButton("delete", "delete.pool", formLayout);
		deleteButton.setElementCssClass("btn-danger");
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		qpoolService.deletePool(pool);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}
