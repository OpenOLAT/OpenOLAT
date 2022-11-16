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
*/ 

package org.olat.core.gui.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.delegating.DelegatingComponent;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.control.winmgr.WindowBackOfficeImpl;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 * responsible for rendering the &lt;link rel.. and &lt;script src=... tags in
 * the html header.<br>
 * we do not need remove methods, since in ajax-mode, any change will lead to a
 * page reload.
 * <P>
 * Initial Date:  04.05.2006 <br>
 *
 * @author Felix Jost
 */
public class JSAndCSSAdderImpl implements JSAndCSSAdder, ComponentRenderer {
	
	private static final Logger log = Tracing.createLoggerFor(JSAndCSSAdderImpl.class);

	private DelegatingComponent dc;

	private List<String> curCssList = new ArrayList<>();
	private List<String> prevCssList = new ArrayList<>();
	private Collection<String> curCssForceSet = new ArrayList<>(3);
	private Collection<String> prevCssForceSet = new ArrayList<>(3);

	private Set<String> allCssKeepSet = new HashSet<>();
	private Set<String> allJsKeepSet = new HashSet<>();
	
	private List<String> curJsList = new ArrayList<>();
	private List<String> prevJsList = new ArrayList<>();

	
	private Set<String> cssToAdd;  // the css to add for the next round
	private Set<String> cssToRemove;  // the css to remove for the next round
	private List<String> jsToAdd; // the js to add for the next round
	
	private List<String> cssToRender; // the css's to render
	private List<String> jsToRender; // the js's to render
	
	// FIXME:fj: make the rawset deprecated; all raw includes can be replaced by a css or js include; the js calls can be moved to the velocity files.
	// for QTIEditormaincontroller / Displaycontroller -> Autocomplete files which need are dynamic files to be included -> 
	// simplest sol would be: get the content of the file (in utf-8) and put it into <script> tags of the appropriate velocitycontainer.
	private Collection<String> curRawSet = new ArrayList<>(2);
	private Collection<String> oldRawSet = new ArrayList<>(2);
	
	private static final int MINIMAL_REFRESHINTERVAL = 1000;//in [ms] 
	private int refreshInterval = -1;
	private final WindowBackOfficeImpl wboImpl;

	private Map<String,String> jsPathToJsFileName = new HashMap<>();
	private Map<String,String> jsPathToEvalBeforeAJAXAddJsCode = new HashMap<>();
	private Map<String,String> jsPathToEvalFileEncoding = new HashMap<>();
	
	private static final String ENCODING_DEFAULT = "utf-8";

	private Map<String, String> cssPathToId = new HashMap<>();
	private Map<String, Integer> cssPathToIndex = new HashMap<>();
	private final Comparator<String> cssIndexComparator = (css1, css2) -> {
		int index1 = cssPathToIndex.get(css1);
		int index2 = cssPathToIndex.get(css2);
		return (index1 - index2);
	};
	private int cssCounter = 0;

	private boolean requiresFullPageRefresh = false;
	
	public JSAndCSSAdderImpl(WindowBackOfficeImpl wboImpl) {
		this.wboImpl = wboImpl;
		dc = new DelegatingComponent("jsAndCssAdderDeleComp", this);
		dc.setDomReplaceable(false);
		cssToRender = curCssList;
		jsToRender = curJsList;
	}
	
	@Override
	public void addRequiredStaticJsFile(String jsFileName) {
		addRequiredJsFile(jsFileName, ENCODING_DEFAULT, null);
	}
	
	@Override
	public void addRequiredStaticJsFile(String jsFileName, String fileEncoding, String preAJAXAddJsCode) {
		addRequiredJsFile(jsFileName, fileEncoding, preAJAXAddJsCode);
	}

	private void addRequiredJsFile(String jsFileName, String fileEncoding, String AJAXAddJsCode) {
		try(StringOutput sb = new StringOutput(50)) {
			String jsPath;
			if(jsFileName.startsWith("http:") || jsFileName.startsWith("https:") || jsFileName.startsWith("//")) {
				jsPath = jsFileName;
			} else {
				Renderer.renderStaticURI(sb, jsFileName);
				jsPath = sb.toString();
			}
	
			if (!curJsList.contains(jsPath)) {
				curJsList.add(jsPath);
				jsPathToJsFileName.put(jsPath, jsFileName);
				if (StringHelper.containsNonWhitespace(AJAXAddJsCode)) {
					jsPathToEvalBeforeAJAXAddJsCode.put(jsPath, AJAXAddJsCode);
				}
				if (fileEncoding != null) {
					jsPathToEvalFileEncoding.put(jsPath, fileEncoding);
				} else {
					jsPathToEvalFileEncoding.put(jsPath, ENCODING_DEFAULT);
				}
			}
		} catch(IOException e) {
			log.error("", e);
		}		
	}

	@Override
	public void addStaticCSSPath(String cssPath) {
		addRequiredCSSPath(cssPath, false, JSAndCSSAdder.CSS_INDEX_BEFORE_THEME);
	}

	@Override
	public void addRequiredCSSPath(String cssPath, boolean forceRemove, Integer cssLoadIndex) {
		if (!curCssList.contains(cssPath)) {
			String id = cssPathToId.get(cssPath);
			if (id == null) { // no html id for this stylesheet import yet -> create one
				cssPathToId.put(cssPath, "o_css"+(++cssCounter));
			}
			curCssList.add(cssPath);
			if (forceRemove) {
				curCssForceSet.add(cssPath);
			}
			if(cssLoadIndex == null) {
				cssLoadIndex = JSAndCSSAdder.CSS_INDEX_BEFORE_THEME;
			}
			cssPathToIndex.put(cssPath, cssLoadIndex);
			// sort css after index
			Collections.sort(curCssList, cssIndexComparator);
		}
	}
	
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
	@Override
	public void requireFullPageRefresh() {
		requiresFullPageRefresh = true;
	}
	
	@Override
	public boolean finishAndCheckChange() {
		// ----- find out whether there are any freshly added or removed css classes. -----
		// create new sets since we need to keep the original list untouched 
		// (e.g. needed for rendering when doing a browser full page reload, or when in non-ajax-mode)
		Set<String> curCss = new HashSet<>(curCssList);
		Set<String> prevCss = new HashSet<>(prevCssList);
		curCss.removeAll(prevCssList); // the current minus the previous ones = the new ones to be added
		curCss.removeAll(allCssKeepSet); // but take those away which were used earlier and didn't need to be removed
		prevCss.removeAll(curCssList); // the previous minus the current ones = the ones not needed anymore = to be deleted
		prevCss.retainAll(prevCssForceSet); // only keep those css in the remove collection which really need to be removed (flagged as forceremove) 
		cssToAdd = curCss;
		cssToRemove = prevCss;
		// ----- find out whether there are new js libs to be added. -----
		// it doesn't make sense to require a removal of js libs (js libs should never interfere which each other by design) -
		// therefore we only have to take care about new ones to be added.
		List<String> curJs = new ArrayList<>(curJsList);
		curJs.removeAll(allJsKeepSet); // the current minus the previously added ones = the new ones to be added
		jsToAdd = curJs;

		//System.out.println("---- css-add:\n"+cssToAdd);
		//System.out.println("---- css-remove:\n"+cssToRemove);
		//System.out.println("---- js-add:\n"+jsToAdd);
		
		// raw set -> deprecated, see comments at variable declaration
		boolean wasRawChanged = false;
		if (curRawSet.size() != oldRawSet.size()) {
			wasRawChanged = true;
		} else {
			// same size, but could still be different:
			wasRawChanged = !curRawSet.containsAll(oldRawSet);
		}

		
		// ----- end-of-calculations: make the cur to the prev for the next add-round -----
		// css
		List<String> tmpCss = prevCssList;
		prevCssList = curCssList;
		cssToRender = curCssList;
		tmpCss.clear();
		curCssList = tmpCss;
		
		// remember which non-remove-force css entries have once already been added
		allCssKeepSet.addAll(cssToAdd);
		allCssKeepSet.removeAll(curCssForceSet);
		
		// change current cssFrceSet and clear it for the next validate process
		Collection<String> forceTmp = prevCssForceSet;
		prevCssForceSet = curCssForceSet;
		curCssForceSet = forceTmp;
		curCssForceSet.clear();
		
		// js
		allJsKeepSet.addAll(jsToAdd);
		
		List<String> jsTmp = prevJsList;
		jsTmp.clear();
		prevJsList = curJsList;
		curJsList = jsTmp;
		jsToRender = prevJsList;
		// raw set -> deprecated, see comments at variable declaration
		Collection<String> tmp = oldRawSet;
		oldRawSet = curRawSet;
		curRawSet = tmp;
		curRawSet.clear();		
		
		// set and reset update/refresh intervall for ajax polling
		wboImpl.setRequiredRefreshInterval(refreshInterval);
		refreshInterval = -1;

		boolean fullPageRefresh = requiresFullPageRefresh;
		requiresFullPageRefresh = false;
		
		return wasRawChanged || fullPageRefresh;
	}

	public Component getJsCssRawHtmlHeader() {
		return dc;
	}

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		// The render argument is used to indicate rendering before and after themes loading
		if (args == null || args.length != 1) {
			throw new AssertException("Programming error: can't render JSAndCSSAdder without 'pre-theme' or 'post-thee' render argument");
		}
		boolean postThemeRendering = args[0].equals("post-theme") ? true : false;
		
		// clear the added-since-last-fullpagerefresh, since we are doing a full page refresh here (only then is the <head> part here rerendered.)
		// this aims to minimize the number of js and css "imports" in the html head when using the non-ajax-mode (only those imports really needed are listed)
		allCssKeepSet.clear();
		allJsKeepSet.clear();
		
		// JS scripts are rendered when in pre-theme rendering phase
		if (!postThemeRendering) {
			for (Iterator<String> it_js = jsToRender.iterator(); it_js.hasNext();) {
				String jsExpr = it_js.next();
				sb.append("<script src=\"").append(jsExpr).append("\"></script>\n");
			}
		}
		
		// sort css files
		for (Iterator<String> it_css = cssToRender.iterator(); it_css.hasNext();) {
			String cssExpr = it_css.next();
			// render post-theme css when in post-theme rendering phase and pre-theme
			// css when in pre-them rendering phase. List is sorted after index
			int cssIndex = cssPathToIndex.get(cssExpr);
			if ((postThemeRendering && cssIndex > JSAndCSSAdder.CSS_INDEX_THEME)
					|| (!postThemeRendering && cssIndex < JSAndCSSAdder.CSS_INDEX_THEME)) {
				String acssId = cssPathToId.get(cssExpr);
				// use media=all to load always and use @media screen/print within the stylesheet
				sb.append("<link id=\"").append(acssId).append("\" rel=\"StyleSheet\" href=\"").append(cssExpr).append("\" media=\"all\" />\n");
			}
		}
		
		if (postThemeRendering) {
			// Render raw header after theme. See also OLAT-4262
			for (Iterator<String> it_raw = oldRawSet.iterator(); it_raw.hasNext();) {
				String rawE = it_raw.next();
				sb.append("\n").append(rawE);
			}			
		}
	}

	@Override
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderingState rstate) {
		//
	}

	@Override
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		//
	}

	@Override
	public void addRequiredRawHeader(Class<?> baseClass, String rawHeader) {
		curRawSet.add(rawHeader);
	}

	@Override
	public void setRequiredRefreshInterval(Class<?> baseClass, int refreshIntervall) {
		if(refreshIntervall < MINIMAL_REFRESHINTERVAL){
			throw new AssertException("Poll refresh intervall is smaller then defined MINIMAL value " + MINIMAL_REFRESHINTERVAL);
		}
		// idea: baseClass for later de-prioritising by configuration
		if (this.refreshInterval == -1 || refreshIntervall < this.refreshInterval) {
			this.refreshInterval = refreshIntervall;
		} // else we already have a request that requires a higher frequency of updates, we will take that one
	}
	
	
	public Command extractJSCSSCommand() {
		try {			
			JSONObject root = new JSONObject();
			
			//css to add
			JSONArray cssAdd = new JSONArray();
			root.put("cssadd", cssAdd);
			for (String addCss : cssToAdd) {
				// the id and the whole relative css path, e.g. /g/4/my.css
				JSONObject styleinfo = new JSONObject();
				String cssId = cssPathToId.get(addCss);
				styleinfo.put("id", cssId);
				styleinfo.put("url", addCss);
				// on js level only pre and post theme rendering supported
				styleinfo.put("pt", cssPathToIndex.get(addCss) > JSAndCSSAdder.CSS_INDEX_THEME ? true : false);
				cssAdd.put(styleinfo); 
			}
			
			//css to remove
			JSONArray cssRemove = new JSONArray();
			root.put("cssrm", cssRemove);
			for (String removeCss : cssToRemove) {
				// the id and the whole relative css path, e.g. /g/4/my.css
				JSONObject styleinfo = new JSONObject();
				String cssId = cssPathToId.get(removeCss);
				styleinfo.put("id", cssId);
				styleinfo.put("url", removeCss);
				cssRemove.put(styleinfo); 
			}
			
			//jsToAdd
			JSONArray jsAdd = new JSONArray();
			root.put("jsadd", jsAdd);
			for (String addJs : jsToAdd) {
				// load file with correct encoding. OLAT files are all UTF-8, but some
				// libraries like TinyMCE are ISO-88591. The window.execScript() in IE
				// can fail when the string has the wrong encoding (IE error 8002010)
				String fileEncoding = jsPathToEvalFileEncoding.get(addJs);
				JSONObject fileInfo = new JSONObject();
				fileInfo.put("url", addJs);
				fileInfo.put("enc", fileEncoding);
				// add code to be executed before the js code is inserted
				if (jsPathToEvalBeforeAJAXAddJsCode.containsKey(addJs)) {
					fileInfo.put("before", jsPathToEvalBeforeAJAXAddJsCode.get(addJs));					
				}
				jsAdd.put(fileInfo);
			}
			Command com = CommandFactory.createJSCSSCommand();
			com.setSubJSON(root);
			return com;
		} catch (JSONException e) {
			throw new AssertException("wrong data put into json object", e);
		}
	}

}
