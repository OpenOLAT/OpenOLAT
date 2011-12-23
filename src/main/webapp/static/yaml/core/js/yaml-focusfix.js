/**
 * "Yet Another Multicolumn Layout" - (X)HTML/CSS Framework
 *
 * (en) Workaround for IE8 und Webkit browsers to fix focus problems when using skiplinks
 * (de) Workaround für IE8 und Webkit browser, um den Focus zu korrigieren, bei Verwendung von Skiplinks
 *
 * @note			inspired by Paul Ratcliffe's article 
 *					http://www.communis.co.uk/blog/2009-06-02-skip-links-chrome-safari-and-added-wai-aria
 *                  Many thanks to Mathias Schäfer (http://molily.de/) for his code improvements
 *
 * @copyright       Copyright 2005-2011, Dirk Jesse
 * @license         CC-A 2.0 (http://creativecommons.org/licenses/by/2.0/),
 *                  YAML-C (http://www.yaml.de/en/license/license-conditions.html)
 * @link            http://www.yaml.de
 * @package         yaml
 * @version         3.3.1
 * @revision        $Revision: 501 $
 * @lastmodified    $Date: 2011-06-18 17:27:44 +0200 (Sa, 18 Jun 2011) $
 */
 
(function () {
	var YAML_focusFix = { 
		skipClass : 'skip',
		
		init : function () {
			var userAgent = navigator.userAgent.toLowerCase();
			var	is_webkit = userAgent.indexOf('webkit') > -1;
			var	is_ie = userAgent.indexOf('msie') > -1;
			
			if (is_webkit || is_ie) {
				var body = document.body,
					handler = YAML_focusFix.click;
				if (body.addEventListener) {
					body.addEventListener('click', handler, false);
				} else if (body.attachEvent) {
					body.attachEvent('onclick', handler);
				}
			}
		},
		
		trim : function (str) {
			return str.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
		},
		
		click : function (e) {
			e = e || window.event;
			var target = e.target || e.srcElement;
			var a = target.className.split(' ');
			
			for (var i=0; i < a.length; i++) {
				var cls = YAML_focusFix.trim(a[i]);
				if ( cls === YAML_focusFix.skipClass) {
					YAML_focusFix.focus(target);
					break;			
				}
			}
		},
		
		focus : function (link) {		
			if (link.href) {
				var href = link.href,
					id = href.substr(href.indexOf('#') + 1),
					target = document.getElementById(id);
				if (target) {
					target.setAttribute("tabindex", "-1");
					target.focus();
				}				
			}
		}
	};
	YAML_focusFix.init();
})();