/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.gui.control;


/**
 * Description:<br>
 * 
 * <P>
 * Initial Date:  04.05.2006 <br>
 *
 * @author Felix Jost
 */
public abstract class JSAndCSSAdder {
	public static final int CSS_INDEX_BEFORE_THEME = 25;
	// everything that must be loaded before the theme: 1 - 49
	public static final int CSS_INDEX_THEME = 50;
	// everything that must be loaded after the theme: 51 - 99
	public static final int CSS_INDEX_AFTER_THEME = 65;

	/**
	 * adds a js file to be served in the html header to the list of required
	 * js-files. e.g. addRequiredJsFile(MyController.class, "js/myscript.js")
	 * where the file myscript.js must be under
	 * "location of MyController.class"/_static/js
	 * <p>
	 * In full page refresh mode, the library will be included to the page
	 * header using the standard script tags.
	 * <p>
	 * In AJAX mode, the library is will read the file from disk, transfer the
	 * content via the AJAX channel and then eval() the content in the window.
	 * <p>
	 * In case that your script needs some values to be initialized before the
	 * eval() happens, use the other addRequiredJsFile() for this purpose
	 * 
	 * @param baseClass
	 * @param jsFileName
	 */
	public abstract void addRequiredJsFile(Class baseClass, String jsFileName);

	/**
	 * adds a js file to be served in the html header to the list of required
	 * js-files. e.g. addRequiredJsFile(MyController.class, "js/myscript.js")
	 * where the file myscript.js must be under
	 * "location of MyController.class"/_static/js
	 * <p>
	 * In full page refresh mode, the library will be included to the page header
	 * using the standard script tags.
	 * <p>
	 * In AJAX mode, the library is will read the file from disk, transfer the
	 * content via the AJAX channel and then eval() the content in the window. The
	 * fileEncoding is used to read the file from disk.
	 * <p>
	 * In case that your script needs some values to be initialized before the
	 * eval() happens, use the other addRequiredJsFile() for this purpose
	 * 
	 * @param baseClass
	 * @param jsFileName
	 * @param fileEncoding the encoding of the js file or NULL to use the default
	 *          encoding (utf-8)
	 */
	public abstract  void addRequiredJsFile(Class baseClass, String jsFileName, String fileEncoding);

	/**
	 * adds a js file to be served in the html header to the list of required
	 * js-files. e.g. addRequiredJsFile(MyController.class, "js/myscript.js")
	 * where the file myscript.js must be under
	 * "location of MyController.class"/_static/js
	 * <p>
	 * In full page refresh mode, the library will be included to the page header
	 * using the standard script tags.
	 * <p>
	 * In AJAX mode, the library is will first eval() the content of the supplied
	 * string evalBeforeDomReplacementJsCode, then read the file from disk,
	 * transfere the content via the AJAX channel and then eval() the content in
	 * the window.The fileEncoding is used to read the file from disk.
	 * 
	 * @param baseClass The class where the script is localted
	 * @param jsFileName The script path, e.g. js/myscript.js
	 * @param fileEncoding the encoding of the js file or NULL to use the default
	 *          encoding (utf-8)
	 * @param preAJAXAddJsCode A string containing some values to be eval()-ed
	 *          right before the script itself is eval()-ed
	 */
	public abstract  void addRequiredJsFile(Class baseClass, String jsFileName, String fileEncoding, String preAJAXAddJsCode);

	/**
	 * 
	 * @param baseClass
	 * @param cssFileName
	 * @param forceRemove normally, once added css files will not be removed
	 *          anymore. However, if your css overrides default settings (e.g.
	 *          when you have a preview css), this css must be removed as soon as
	 *          the validate method does not require it anymore. (e.g. when you
	 *          leave the course edit mode)
	 * @param cssIndex position of the css in relation of the position of the
	 *          theme. Use JSAndCSSAdder.CSS_INDEX_* variables to set this
	 */
	public abstract  void addRequiredCSSFile(Class baseClass, String cssFileName, boolean forceRemove, int cssLoadIndex);
	
	/**
	 * This method will add the CSS file before the theme. use the other method to change this behaviour
	 * @param baseClass
	 * @param cssFileName
	 * @param forceRemove normally, once added css files will not be removed
	 *          anymore. However, if your css overrides default settings (e.g.
	 *          when you have a preview css), this css must be removed as soon as
	 *          the validate method does not require it anymore. (e.g. when you
	 *          leave the course edit mode)
	 */
	public abstract  void addRequiredCSSFile(Class baseClass, String cssFileName, boolean forceRemove);

	/**
	 * 
	 * used rather rarely, e.g. when you have a css with content which is dynamic
	 * (e.g. a custom course css in olat).<br>
	 * Use a Mapper/MapperRegistry to obtain a cssPath <br>
	 * Normally (for fixed css) use
	 * 
	 * @see addRequiredCSSFile(Class baseClass, String cssFileName, boolean
	 *      forceRemove);
	 * 
	 * @param cssPath the path to the css, e.g. /olat/m/1001/mycss.css
	 * 
	 * @param forceRemove normally, once added css files will not be removed
	 *          anymore. However, if your css overrides default settings (e.g.
	 *          when you have a preview css), this css must be removed as soon as
	 *          the validate method does not require it anymore. (e.g. when you
	 *          leave the course edit mode)
	 * @param cssIndex position of the css in relation of the position of the
	 *          theme. Use JSAndCSSAdder.CSS_INDEX_* variables to set this
	 */
	public abstract  void addRequiredCSSPath(String cssPath, boolean forceRemove, int cssLoadIndex);
	
	/**
	 * 
	 * @return true if there has been a new (never been added before) (or a deleted css which has been marked as to-be-removed) required css or js file's base since the previous call to this method, false otherwise (no new js or css libs needed)
	 * 
	 */
	public abstract  boolean finishAndCheckChange();
	

	/**
	 * @param baseClass
	 * @param rawHeader
	 */
	public abstract  void addRequiredRawHeader(Class baseClass, String rawHeader);

	/**
	 *
	 * @param refreshIntervall the time 
	 * in miliseconds after which (in ajax mode) a refresh of the screen is needed. use small times with caution, since it generates server load. after the given time, a poll (comet, push, hanging get for future release) is issued to the server to collect the dirty components and rerender them. when more than one interval is requested in one validation phase (=on one browser window), then the minimum is taken.	
	 */
	public abstract  void setRequiredRefreshInterval(Class baseClass, int refreshIntervall);

	/**
	 * 
	 * requires that a full page reload takes places.	
	 * sometimes eval'ing a more complex js lib (such as tiny mce) directly into global context does not work (timing issues?)
	 * this should be used only rarely when complex js is executed and has errors in it, 
	 * since a full page refresh is slower than a ajax call.
	 * <br>
	 * when a component is validated (last cycle before rendering), and a full page refresh is required, then a full page request command is 
	 * sent via JSON to the browser which then executes it using document.location.replace(...). Since this step involves two calls (JSON+reload),
	 * this is slower than a normal full page click (aka known as non-ajax mode). 
	 * 
	 */
	public abstract  void requireFullPageRefresh();

	/**
	 * Get the mapper path for this class and file name, e.g. to use in a
	 * preAJAXAddJsCode call when adding a JS script that needs to load other
	 * scripts as well.
	 * 
	 * @param baseClass
	 * @param fileName
	 *            the name of the file or NULL to get the base path.
	 * @return A string that represents the path of the JS file as it is used on
	 *         a full page refresh. If fileName is null, the base path wihout
	 *         trailing slash is returned.
	 */
	public abstract  String getMappedPathFor(Class baseClass, String fileName);

	
	
	
}
