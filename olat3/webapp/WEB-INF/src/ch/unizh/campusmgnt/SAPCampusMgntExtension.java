package ch.unizh.campusmgnt;

import java.util.Locale;

import org.olat.core.extensions.AbstractExtension;
import org.olat.core.extensions.Extension;
import org.olat.core.extensions.ExtensionElement;
import org.olat.core.extensions.action.ActionExtension;
import org.olat.core.extensions.helpers.ExtensionElements;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.archiver.ArchiverMainController;

import ch.unizh.campusmgnt.controller.CampusManagementController;


public class SAPCampusMgntExtension extends AbstractExtension implements Extension {

	private ExtensionElements elements = new ExtensionElements();
	public String mapPath; // to be accessed by controllers of a sub-package (only for pdf or such, not for images!)

	/**
	 * must be public for spring framework
	 */
	public SAPCampusMgntExtension() {

		elements.putExtensionElement(ArchiverMainController.class.getName(), new ActionExtension() {
			public String getDescription(Locale loc) {
				Translator trans = Util.createPackageTranslator(this.getClass(), loc);
				return trans.translate("tool.description");
			}

			public String getActionText(Locale loc) {
				Translator trans = Util.createPackageTranslator(this.getClass(), loc);
				return trans.translate("tool.actiontext");
			}

			public Controller createController(UserRequest ureq, WindowControl wControl, Object arg) {
				if (arg instanceof ICourse) {
					ICourse course = (ICourse) arg;
					Controller ctr = new CampusManagementController(ureq, wControl, course);
					return ctr;
				} else throw new AssertException("SAPCampusMgntExtension needs a ICourse as the argument parameter: arg = "+arg);
			}
		});
	}

	/**
	 * @see org.olat.core.extensions.Extension#getExtensionFor(java.lang.Class)
	 */
	public ExtensionElement getExtensionFor(String extensionPoint) {
		return elements.getExtensionElement(extensionPoint);
	}
	
}
