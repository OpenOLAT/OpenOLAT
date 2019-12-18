package org.olat.core.commons.services.vfs.ui;

import java.util.Locale;

import org.apache.logging.log4j.Level;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

public class VFSUIFactory {
	private static final String FILE_INFO_TRANS = "fileinfo.";
	
	private VFSUIFactory() {}
	
	public static String translateFileType(String fileType, Locale locale) {
		Translator translator = Util.createPackageTranslator(VFSUIFactory.class, locale);
		String i18nKey = FILE_INFO_TRANS + "type." + fileType.toLowerCase();
		String translation = translator.translate(i18nKey, null, Level.OFF);
		if(i18nKey.equals(translation) || translation.length() > 256) {
			translation = fileType;
		}
		return translation;
	}
	
	public static String translateFileCategory(String fileCategory, Locale locale) {
		Translator translator = Util.createPackageTranslator(VFSUIFactory.class, locale);
		String i18nKey = FILE_INFO_TRANS + "category." + fileCategory.toLowerCase();
		String translation = translator.translate(i18nKey, null, Level.OFF);
		if(i18nKey.equals(translation) || translation.length() > 256) {
			translation = fileCategory;
		}
		return translation;
	}
}
