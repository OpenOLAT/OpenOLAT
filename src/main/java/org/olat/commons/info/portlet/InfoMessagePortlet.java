package org.olat.commons.info.portlet;

import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortlet;
import org.olat.core.gui.control.generic.portal.Portlet;
import org.olat.core.gui.control.generic.portal.PortletToolController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for InfoMessagePortlet
 * 
 * <P>
 * Initial Date:  27 juil. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoMessagePortlet extends AbstractPortlet {

	private InfoMessagePortletRunController runCtrl;

	@Override
	public String getTitle() {
		return getTranslator().translate("portlet.title");
	}

	@Override
	public String getDescription() {
		return getTranslator().translate("portlet.title");
	}

	@Override
	public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map portletConfig) {
		Translator translator = Util.createPackageTranslator(InfoMessagePortlet.class, ureq.getLocale());
		Portlet p = new InfoMessagePortlet();
		p.setName(getName());			
		p.setTranslator(translator);
		return p;
	}

	@Override
	public Component getInitialRunComponent(WindowControl wControl, UserRequest ureq) {
		if(runCtrl != null) runCtrl.dispose();
		runCtrl = new InfoMessagePortletRunController(wControl, ureq, getTranslator(), getName());
		return runCtrl.getInitialComponent();
	}
	
	@Override
	public void dispose() {
		disposeRunComponent();
	}

	@Override
	public void disposeRunComponent() {
		if (this.runCtrl != null) {
			this.runCtrl.dispose();
			this.runCtrl = null;
		}
	}

	@Override
	public String getCssClass() {
		return "o_portlet_infomessages";
	}

	@Override
	public PortletToolController getTools(UserRequest ureq, WindowControl wControl) {
		if (runCtrl == null ) {
			runCtrl = new InfoMessagePortletRunController(wControl, ureq, getTranslator(), getName());
		}
	  return runCtrl.createSortingTool(ureq, wControl);
	}
}
