package org.olat.ims.qti21.ui.assessment;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.ims.qti21.ui.QTI21RuntimeController;

/**
 * 
 * Initial date: 28 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ValidationToolController extends BasicController {
	
	private CloseableModalController cmc;
	private ValidationXmlSignatureController validationCtrl;
	
	private final Link validateButton;
	
	public QTI21ValidationToolController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(QTI21RuntimeController.class, ureq.getLocale()));
		
		validateButton = LinkFactory.createButton("validate.xml.signature", null, this);
		validateButton.setIconLeftCSS("o_icon o_icon-fw o_icon_correction");
		validateButton.setTranslator(getTranslator());
		putInitialPanel(validateButton);
		getInitialComponent().setSpanAsDomReplaceable(true); // override to wrap panel as span to not break link layout 
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(validateButton == source) {
			doValidate(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(validationCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(validationCtrl);
		removeAsListenerAndDispose(cmc);
		validationCtrl = null;
		cmc = null;
	}

	private void doValidate(UserRequest ureq) {
		if(validationCtrl != null) return;
		
		validationCtrl = new ValidationXmlSignatureController(ureq, getWindowControl());
		listenTo(validationCtrl);
		cmc = new CloseableModalController(getWindowControl(), "close", validationCtrl.getInitialComponent(),
				true, translate("validate.xml.signature"));
		cmc.activate();
		listenTo(cmc);
	}
}
