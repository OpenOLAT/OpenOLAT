/* base.js: some utility functions to complement jQuery library
	Copyright (c) 2012 Fernando Serrano Carpena
	fernando@vitaminew.com
	http://www.vitaminew.com/basejs
*/

/*
  Part 1:
  Class, version 2.7
  Copyright (c) 2006, 2007, 2008, Alex Arnell <alex@twologic.com>

  Redistribution and use in source and binary forms, with or without modification, are
  permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice, this list
	of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice, this
	list of conditions and the following disclaimer in the documentation and/or other
	materials provided with the distribution.
  * Neither the name of typicalnoise.com nor the names of its contributors may be
	used to endorse or promote products derived from this software without specific prior
	written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
  THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
  OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

/*
	Class definition improved by Fernando Serrano Carpena to support C++ typed dynamic casting.
	// Default method, defined by Alex Arnell:
	var MyClass = Class.extend.apply(parent,{ class definition })

	// New method:
	Class.create("MyClass",parent,{ class definition })

	// Improvements
	// 	- automatic namespace creation:
	Class.create("myns.MyClass",myns.Parent, { class definition })

	//	- Dynamic casting:
	var instance = dynamic_cast("myns.MyClass",anObject);

	// You can also use:
	var MyClass = Class.create({ class definition })
	// or
	var MyClass = Class.create(parent, { class definition })
*/

var ClassBase = (function() {
  var __extending = {};

  return {
	extend: function(parent, def) {
	  if (arguments.length == 1) { def = parent; parent = null; }
	  var func = function() {
		if (arguments[0] ==  __extending) { return; }
		if (!this.initialize) {
			this.initialize = function() {}
		}
		this.initialize.apply(this, arguments);
	  };
	  if (typeof(parent) == 'function') {
		func.prototype = new parent( __extending);
	  }
	  var mixins = [];
	  if (def && def.include) {
		if (def.include.reverse) {
		  // methods defined in later mixins should override prior
		  mixins = mixins.concat(def.include.reverse());
		} else {
		  mixins.push(def.include);
		}
		delete def.include; // clean syntax sugar
	  }
	  if (def) ClassBase.inherit(func.prototype, def);
	  for (var i = 0; (mixin = mixins[i]); i++) {
		ClassBase.mixin(func.prototype, mixin);
	  }
	  return func;
	},
	mixin: function (dest, src, clobber) {
	  clobber = clobber || false;
	  if (typeof(src) != 'undefined' && src !== null) {
		for (var prop in src) {
		  if (clobber || (!dest[prop] && typeof(src[prop]) == 'function')) {
			dest[prop] = src[prop];
		  }
		}
	  }
	  return dest;
	},
	inherit: function(dest, src, fname) {
	  if (arguments.length == 3) {
		var ancestor = dest[fname], descendent = src[fname], method = descendent;
		descendent = function() {
		  var ref = this.parent; this.parent = ancestor;
		  var result = method.apply(this, arguments);
		  ref ? this.parent = ref : delete this.parent;
		  return result;
		};
		// mask the underlying method
		descendent.valueOf = function() { return method; };
		descendent.toString = function() { return method.toString(); };
		dest[fname] = descendent;
	  } else {
		for (var prop in src) {
		  if (dest[prop] && typeof(src[prop]) == 'function') {
			ClassBase.inherit(dest, src, prop);
		  } else {
			dest[prop] = src[prop];
		  }
		}
	  }
	  return dest;
	},
	singleton: function() {
	  var args = arguments;
	  if (args.length == 2 && args[0].getInstance) {
		var klass = args[0].getInstance(__extending);
		// we're extending a singleton swap it out for it's class
		if (klass) { args[0] = klass; }
	  }

	  return (function(args){
		// store instance and class in private variables
		var instance = false;
		var klass = ClassBase.extend.apply(args.callee, args);
		return {
		  getInstance: function () {
			if (arguments[0] == __extending) return klass;
			if (instance) return instance;
			return (instance = new klass());
		  }
		};
	  })(args);
	}
  };
})();

// New method
ClassBase.create = function() {
  return ClassBase.extend.apply(this, arguments);
};

function Class_createNamespace(nsAndClass) {
	var nsArray = nsAndClass.split(".");
	nsArray.pop();	// Remove the class name
	var ns = null;
	if (nsArray.length>0) {
		for (var i=0;i<nsArray.length;++i) {
			var name = nsArray[i];
			if (ns) {
				if (ns[name]==undefined) {
					ns[name] = {}
					ns = ns[name];
				}
				else {
					ns = ns[name];
				}
			}
			else {
				if (window[name]==undefined) {
					window[name] = {}
					ns = window[name];
				}
				else {
					ns = window[name];
				}
			}
		}
	}

	if (ns) {
		return ns;
	}
	else {
		return window;
	}
}

function Class_getClassName(nsAndClass) {
	return nsAndClass.split(".").pop();
}

function Class(a,b,c) {
	if (typeof(a)=='object' && !b) {
		return ClassBase.create(a);
	}
	else if (typeof(a)=='function' && typeof(b)=='object' && !c) {
		return ClassBase.create(a,b);
	}
	else if (typeof(a)=='string' && typeof(b)=='object' && !c) {
		// a es el nombre de la clase con su NS, y b es la definición
		var ns = Class_createNamespace(a);
		var cn = Class_getClassName(a);
		b[a] = true;
		ns[cn] = ClassBase.create(b);
		return ns[cn];
	}
	else if (typeof(a)=='string' && typeof(b)=='function' && typeof(c)=='object') {
		// a es el nombre de la clase con su NS, b es el padre y c es la definición
		var ns = Class_createNamespace(a);
		var cn = Class_getClassName(a);
		c[a] = true;
		ns[cn] = ClassBase.create(b,c);
		return ns[cn];
	}
}

Class.create = function() {
	return ClassBase.extend.apply(this, arguments);
}

function dynamic_cast(type,object) {
	return (object && object[type]) ? object:null;
}

Class.fromString = function(str) {
  var arr = str.split(".");

  var fn = (window || this);
  for (var i = 0, len = arr.length; i < len; i++) {
    fn = fn[arr[i]];
  }

  if (typeof fn !== "function") {
    throw new Error("function not found");
  }

  return fn;
};


/* Part 2: javascript extension */
Array.prototype.contains = function(obj) {
	var i = this.length;
	while (i--) {
		if (this[i] == obj) {
			return true;
		}
	}
	return false;
}

Array.prototype.remove = function(from, to) {
  var rest = this.slice((to || from) + 1 || this.length);
  this.length = from < 0 ? this.length + from : from;
  return this.push.apply(this, rest);
};

String.prototype.trim = function () {
	return this.replace(/^\s*/, "").replace(/\s*$/, "");
}

/* Part 3: base.js library */
var base = {};

Class ("base.UserAgent",{
	system:{},
	browser:{},

	// ie11: "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; .NET4.0E; .NET4.0C; .NET CLR 3.5.30729; .NET CLR 2.0.50727; .NET CLR 3.0.30729; rv:11.0) like Gecko"
	// ie10:

	initialize:function(userAgentString) {
		if (!userAgentString) {
			userAgentString = navigator.userAgent;
		}
		this.parseOperatingSystem(userAgentString);
		this.parseBrowser(userAgentString);
	},

	parseOperatingSystem:function(userAgentString) {
		this.system.MacOS = /Macintosh/.test(userAgentString);
		this.system.Windows = /Windows/.test(userAgentString);
		this.system.iPhone = /iPhone/.test(userAgentString);
		this.system.iPodTouch = /iPod/.test(userAgentString);
		this.system.iPad = /iPad/.test(userAgentString);
		this.system.iOS = this.system.iPhone || this.system.iPad || this.system.iPodTouch;
		this.system.Android = /Android/.test(userAgentString);
		this.system.Linux = (this.system.Android) ? false:/Linux/.test(userAgentString);

		if (this.system.MacOS) {
			this.system.OSName = "Mac OS X";
			this.parseMacOSVersion(userAgentString);
		}
		else if (this.system.Windows) {
			this.system.OSName = "Windows";
			this.parseWindowsVersion(userAgentString);
		}
		else if (this.system.Linux) {
			this.system.OSName = "Linux";
			this.parseLinuxVersion(userAgentString);
		}
		else if (this.system.iOS) {
			this.system.OSName = "iOS";
			this.parseIOSVersion(userAgentString);
		}
		else if (this.system.Android) {
			this.system.OSName = "Android";
			this.parseAndroidVersion(userAgentString);
		}
	},

	parseBrowser:function(userAgentString) {
		// Safari: Version/X.X.X Safari/XXX
		// Chrome: Chrome/XX.X.XX.XX Safari/XXX
		// Opera: Opera/X.XX
		// Firefox: Gecko/XXXXXX Firefox/XX.XX.XX
		// Explorer: MSIE X.X
		this.browser.Version = {};
		this.browser.Safari = /Version\/([\d\.]+) Safari\//.test(userAgentString);
		if (this.browser.Safari) {
			this.browser.Name = "Safari";
			this.browser.Vendor = "Apple";
			this.browser.Version.versionString = RegExp.$1;
		}

		this.browser.Chrome = /Chrome\/([\d\.]+) Safari\//.test(userAgentString) ||
							  /Chrome\/([\d\.]+) Electron\//.test(userAgentString);
		if (this.browser.Chrome) {
			this.browser.Name = "Chrome";
			this.browser.Vendor = "Google";
			this.browser.Version.versionString = RegExp.$1;
		}

		this.browser.Opera = /Opera\/[\d\.]+/.test(userAgentString);
		if (this.browser.Opera) {
			this.browser.Name = "Opera";
			this.browser.Vendor = "Opera Software";
			var versionString = /Version\/([\d\.]+)/.test(userAgentString);
			this.browser.Version.versionString = RegExp.$1;
		}

		this.browser.Firefox = /Gecko\/[\d\.]+ Firefox\/([\d\.]+)/.test(userAgentString);
		if (this.browser.Firefox) {
			this.browser.Name = "Firefox";
			this.browser.Vendor = "Mozilla Foundation";
			this.browser.Version.versionString = RegExp.$1;
		}

		this.browser.Edge = /Edge\/(.*)/.test(userAgentString);
		if (this.browser.Edge) {
			var result = /Edge\/(.*)/.exec(userAgentString);
			this.browser.Name = "Edge";
			this.browser.Chrome = false;
			this.browser.Vendor = "Microsoft";
			this.browser.Version.versionString = result[1];
		} 

		this.browser.Explorer = /MSIE ([\d\.]+)/.test(userAgentString);
		if (!this.browser.Explorer) {
			var re = /\Mozilla\/5.0 \(([^)]+)\) like Gecko/
			var matches = re.exec(userAgentString);
			if (matches) {
				re = /rv:(.*)/
				var version = re.exec(matches[1]);
				this.browser.Explorer = true;
				this.browser.Name = "Internet Explorer";
				this.browser.Vendor = "Microsoft";
				if (version) {
					this.browser.Version.versionString = version[1];
				}
				else {
					this.browser.Version.versionString = "unknown";
				}
			}
		}
		else {
			this.browser.Name = "Internet Explorer";
			this.browser.Vendor = "Microsoft";
			this.browser.Version.versionString = RegExp.$1;
		}

		if (this.system.iOS) {
			this.browser.IsMobileVersion = true;
			this.browser.MobileSafari = /Version\/([\d\.]+) Mobile/.test(userAgentString);
			if (this.browser.MobileSafari) {
				this.browser.Name = "Mobile Safari";
				this.browser.Vendor = "Apple";
				this.browser.Version.versionString = RegExp.$1;
			}
			this.browser.Android = false;
		}
		else if (this.system.Android) {
			this.browser.IsMobileVersion = true;
			this.browser.Android = /Version\/([\d\.]+) Mobile/.test(userAgentString);
			if (this.browser.MobileSafari) {
				this.browser.Name = "Android Browser";
				this.browser.Vendor = "Google";
				this.browser.Version.versionString = RegExp.$1;
			}
			else {
				this.browser.Chrome = /Chrome\/([\d\.]+)/.test(userAgentString);
				this.browser.Name = "Chrome";
				this.browser.Vendor = "Google";
				this.browser.Version.versionString = RegExp.$1;
			}

			this.browser.Safari = false;
		}
		else {
			this.browser.IsMobileVersion = false;
		}

		this.parseBrowserVersion(userAgentString);
	},

	parseBrowserVersion:function(userAgentString) {
		if (/([\d]+)\.([\d]+)\.*([\d]*)/.test(this.browser.Version.versionString)) {
			this.browser.Version.major = Number(RegExp.$1);
			this.browser.Version.minor = Number(RegExp.$2);
			this.browser.Version.revision = (RegExp.$3) ? Number(RegExp.$3):0;
		}
	},

	parseMacOSVersion:function(userAgentString) {
		var versionString = (/Mac OS X (\d+_\d+_*\d*)/.test(userAgentString)) ? RegExp.$1:'';
		this.system.Version = {};
		// Safari/Chrome
		if (versionString!='') {
			if (/(\d+)_(\d+)_*(\d*)/.test(versionString)) {
				this.system.Version.major = Number(RegExp.$1);
				this.system.Version.minor = Number(RegExp.$2);
				this.system.Version.revision = (RegExp.$3) ? Number(RegExp.$3):0;
			}
		}
		// Firefox/Opera
		else {
			versionString = (/Mac OS X (\d+\.\d+\.*\d*)/.test(userAgentString)) ? RegExp.$1:'Unknown';
			if (/(\d+)\.(\d+)\.*(\d*)/.test(versionString)) {
				this.system.Version.major = Number(RegExp.$1);
				this.system.Version.minor = Number(RegExp.$2);
				this.system.Version.revision = (RegExp.$3) ? Number(RegExp.$3):0;
			}
		}
		if (!this.system.Version.major) {
			this.system.Version.major = 0;
			this.system.Version.minor = 0;
			this.system.Version.revision = 0;
		}
		this.system.Version.stringValue = this.system.Version.major + '.' + this.system.Version.minor + '.' + this.system.Version.revision;
		switch (this.system.Version.minor) {
			case 0:
				this.system.Version.name = "Cheetah";
				break;
			case 1:
				this.system.Version.name = "Puma";
				break;
			case 2:
				this.system.Version.name = "Jaguar";
				break;
			case 3:
				this.system.Version.name = "Panther";
				break;
			case 4:
				this.system.Version.name = "Tiger";
				break;
			case 5:
				this.system.Version.name = "Leopard";
				break;
			case 6:
				this.system.Version.name = "Snow Leopard";
				break;
			case 7:
				this.system.Version.name = "Lion";
				break;
			case 8:
				this.system.Version.name = "Mountain Lion";
				break;
		}
	},

	parseWindowsVersion:function(userAgentString) {
		this.system.Version = {};
		if (/NT (\d+)\.(\d*)/.test(userAgentString)) {
			this.system.Version.major = Number(RegExp.$1);
			this.system.Version.minor = Number(RegExp.$2);
			this.system.Version.revision = 0;	// Solo por compatibilidad
			this.system.Version.stringValue = "NT " + this.system.Version.major + "." + this.system.Version.minor;
			var major = this.system.Version.major;
			var minor = this.system.Version.minor;
			var name = 'undefined';
			if (major==5) {
				if (minor==0) this.system.Version.name = '2000';
				else this.system.Version.name = 'XP';
			}
			else if (major==6) {
				if (minor==0) this.system.Version.name = 'Vista';
				else if (minor==1) this.system.Version.name = '7';
				else if (minor==2) this.system.Version.name = '8';
			}
		}
		else {
			this.system.Version.major = 0;
			this.system.Version.minor = 0;
			this.system.Version.name = "Unknown";
			this.system.Version.stringValue = "Unknown";
		}
	},

	parseLinuxVersion:function(userAgentString) {
		// Muchos navegadores no proporcionan información sobre la distribución de linux... no se puede hacer mucho más que esto
		this.system.Version = {};
		this.system.Version.major = 0;
		this.system.Version.minor = 0;
		this.system.Version.revision = 0;
		this.system.Version.name = "";
		this.system.Version.stringValue = "Unknown distribution";
	},

	parseIOSVersion:function(userAgentString) {
		this.system.Version = {};
		if (/iPhone OS (\d+)_(\d+)_*(\d*)/i.test(userAgentString) || /iPad; CPU OS (\d+)_(\d+)_*(\d*)/i.test(userAgentString)) {
			this.system.Version.major = Number(RegExp.$1);
			this.system.Version.minor = Number(RegExp.$2);
			this.system.Version.revision = (RegExp.$3) ? Number(RegExp.$3):0;
			this.system.Version.stringValue = this.system.Version.major + "." + this.system.Version.minor + '.' + this.system.Version.revision;
			this.system.Version.name = "iOS";
		}
		else {
			this.system.Version.major = 0;
			this.system.Version.minor = 0;
			this.system.Version.name = "Unknown";
			this.system.Version.stringValue = "Unknown";
		}
	},

	parseAndroidVersion:function(userAgentString) {
		this.system.Version = {};
		if (/Android (\d+)\.(\d+)\.*(\d*)/.test(userAgentString)) {
			this.system.Version.major = Number(RegExp.$1);
			this.system.Version.minor = Number(RegExp.$2);
			this.system.Version.revision = (RegExp.$3) ? Number(RegExp.$3):0;
			this.system.Version.stringValue = this.system.Version.major + "." + this.system.Version.minor + '.' + this.system.Version.revision;
		}
		else {
			this.system.Version.major = 0;
			this.system.Version.minor = 0;
			this.system.Version.revision = 0;
		}
		if (/Build\/([a-zA-Z]+)/.test(userAgentString)) {
			this.system.Version.name = RegExp.$1;
		}
		else {
			this.system.Version.name = "Unknown version";
		}
		this.system.Version.stringValue = this.system.Version.major + "." + this.system.Version.minor + '.' + this.system.Version.revision;
	},

	getInfoString:function() {
		return navigator.userAgent;
	}
});

base.userAgent = new base.UserAgent();

Class ("base.TimerManager", {
	timerArray:new Array(),
	lastId:0,

	setupTimer:function(timer,time) {
		this.lastId++;
		timer.timerId = this.lastId;
		timer.timeout = time;
		this.timerArray[this.lastId] = timer;
		timer.jsTimerId = setTimeout("timerManager.executeTimerCallback(" + this.lastId + ")",time);
	},

	executeTimerCallback:function(timerId) {
		var timer = this.timerArray[timerId];
		if (timer && timer.callback) {
			timer.callback(timer,timer.params);
		}
		if (timer.repeat) {
			timer.jsTimerId = setTimeout("timerManager.executeTimerCallback(" + timer.timerId + ")",timer.timeout);
		}
	}
});

base.timerManager = new base.TimerManager();

Class ("base.Timer",{
	timerId:0,
	callback:null,
	params:null,
	jsTimerId:0,
	repeat:false,
	timeout:0,

	initialize:function(callback,time,params) {
		this.callback = callback;
		this.params = params;
		base.timerManager.setupTimer(this,time);
	},

	cancel:function() {
		clearTimeout(this.jsTimerId);
	}
});

// Deprecated
var TimerManager = base.TimerManager
var timerManager = base.timerManager
var Timer = base.Timer;

base.Timer.sleep = function(milliseconds) {
  var start = new Date().getTime();
  for (var i = 0; i < 1e7; i++) {
	if ((new Date().getTime() - start) > milliseconds){
	  break;
	}
  }
}

base.cookies = {
	set:function(name,value) {
		document.cookie = name + "=" + value;
	},

	get:function(name) {
		var i,x,y,ARRcookies=document.cookie.split(";");
		for (i=0;i<ARRcookies.length;i++) {
			x=ARRcookies[i].substr(0,ARRcookies[i].indexOf("="));
			y=ARRcookies[i].substr(ARRcookies[i].indexOf("=")+1);
			x=x.replace(/^\s+|\s+$/g,"");
			if (x==name) {
				return unescape(y);
			}
		}
	}
};

base.hashParams = {
	extractUrl:function() {
		var urlOnly = window.location.href;
		if (urlOnly.lastIndexOf('#')>=0) {
			urlOnly = urlOnly.substr(0,urlOnly.lastIndexOf('#'));
		}
		return urlOnly;
	},

	extractParams:function() {
		var params = window.location.href;
		if (params.lastIndexOf('#')>=0) {
			params = params.substr(window.location.href.lastIndexOf('#'));
		}
		else {
			params = "";
		}
		return params;
	},

	clear:function() {
		window.location.href = this.extractUrl() + '#';
	},

	unset:function(key) {
		var url = location.href;
		var lastIndex = url.lastIndexOf('#');
		var urlOnly = this.extractUrl();
		if (lastIndex>=0 && lastIndex+1<url.length) {
			var newParams = "";
			var params = url.substr(url.lastIndexOf('#')+1);
			params = params.split('&');
			for (var i=0;i<params.length;++i) {
				var current = params[i];
				var keyValue = current.split('=');
				if ((keyValue.length>=2 && keyValue[0]!=key) || keyValue.length<2) {
					if (newParams=="") newParams += '#';
					else newParams += "&";
					newParams += current;
				}
			}
			if (newParams=="") newParams = "#";
			location.href = urlOnly + newParams;
		}
	},

	set:function(key,value) {
		if (key && value) {
			this.unset(key);
			var url = this.extractUrl();
			var params = this.extractParams();
			var result = url;
			if (params.length==0) {
				result += '#' + key + '=' + value;
			}
			else if (params.length==1) {
				result +=  params + key + '=' + value;
			}
			else {
				result +=  params + "&" + key + '=' + value;
			}
			location.href = result;
		}
	},

	get:function(key) {
		var url = location.href;
		var index = url.indexOf("#");
		if (index==-1) return "";
		index = url.indexOf(key,index) + key.length;
		if (url.charAt(index)=="=") {
			var result = url.indexOf("&",index);
			if (result==-1) {
				result = url.length;
			}
			return url.substring(index + 1, result);
		}
		return "";
	}
}

base.parameters = {
    list:null,

	parse:function() {
		if (!this.list) {
			var url = window.location.href;
			this.list = {};
			
			if (/(http|https|file)?:\/\/([a-z0-9.\-_\/\~:]*\?)([a-z0-9.\/\-_\%\=\&]*)\#*/i.test(url)) {
				var params = RegExp.$3;
				var paramArray = params.split('&');
				this.list = {};
				for (var i=0; i<paramArray.length;++i) {
					var keyValue = paramArray[i].split('=');
					var key = keyValue[0];
					var value = keyValue.length==2 ? keyValue[1]:'';
					this.list[key] = value;
				}
			}
		}
	},

	get:function(parameter) {
        if (this.list==null) {
            this.parse();
        }
		return this.list[parameter];
	},

	extractUrl:function() {
		var urlOnly = base.hashParams.extractUrl();
		if (urlOnly.lastIndexOf('?')>=0) {
			urlOnly = urlOnly.substr(0,urlOnly.lastIndexOf('?'));
		}
		return urlOnly;
	},

	extractParams:function() {
		// Primero quitar los parámetros hash
		var urlAndParams = base.hashParams.extractUrl();
		var params = urlAndParams;
		if (params.lastIndexOf('?')>=0) {
			params = params.substr(window.location.href.lastIndexOf('?'));
		}
		else {
			params = "";
		}
		return params;
	},

	// Pasa los parámetros de la URL a hash. Esta acción recargará la página
	toHash:function() {
		var urlOnly = this.extractUrl();
		var hashParameters = base.hashParams.extractParams();
		var parameters = this.extractParams();
		var newHashParams = "";
		var result = urlOnly;
		if (parameters.length==0 || parameters.length==1) {
			result += hashParameters;
		}
		else {
			parameters = parameters.substr(1);
			parameters = parameters.split('&');
			for (var i=0;i<parameters.length;++i) {
				keyValue = parameters[i].split('=');
				if (keyValue.length>=2 && base.hashParams.get(keyValue[0])=='') {
					if (hashParameters=="" && newHashParams=="") newHashParams += '#';
					else newHashParams += '&';
					newHashParams += keyValue[0] + '=' + keyValue[1];
				}
			}
			result += hashParameters + newHashParams;
		}
		if (location.href!=result) {
			location.href = result;
		}
	}
}

base.require = function(libraryName) {
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = libraryName;
    document.getElementsByTagName('head')[0].appendChild(script);
}

base.importStylesheet = function(stylesheetFile) {
	var link = document.createElement('link');
	link.setAttribute("rel","stylesheet");
	link.setAttribute("href",stylesheetFile);
	link.setAttribute("type","text/css");
	link.setAttribute("media","screen");
	document.getElementsByTagName('head')[0].appendChild(link);
}

// Deprecated
base.dom = {
	createElement: function(type,params,attributes) {
		if (!params) params = {};
		var elem = document.createElement(type);
		if (params.className) elem.className = params.className;
		if (params.id) elem.id = params.id;
		if (params.style) $(elem).css(params.style);
		if (params.innerHTML) elem.innerHTML = params.innerHTML;

		if (attributes) {
			for (var key in attributes) {
				var value = attributes[key];
				if (value===undefined) { value = "" }
				if (typeof(value)=='string') {
					value.trim();
				}

				elem.setAttribute(key,value);
			}
		}

		return elem;
	},

	fontSize:function(domElement) {
		var size = {size:0,units:'px'}
		var measure = $(domElement).css('font-size');
		if (/(\d+\.*\d*)(\D+)/.test(measure)) {
			size.size = RegExp.$1;
			size.units = RegExp.$2;
		}
		return size;
	},

	// Convert all measures in px into %, inside domNode.
	toPercent:function(domNode) {
		var baseSize = {w:$(domNode).width(),h:$(domNode).height()};

		for (var i=0;i<domNode.children.length;++i) {
			var child = domNode.children[i];
			var nodeSize = {x:$(child).position().left,y:$(child).position().top,w:$(child).width(),h:$(child).height()};

			child.originalSize = {fontSize:this.fontSize(child)}
			child.originalSize.x = nodeSize.x;
			child.originalSize.y = nodeSize.y;
			child.originalSize.w = nodeSize.w;
			child.originalSize.h = nodeSize.h;

			nodeSize.x = nodeSize.x * 100 / baseSize.w;
			nodeSize.y = nodeSize.y * 100 / baseSize.h;
			nodeSize.w = nodeSize.w * 100 / baseSize.w;
			nodeSize.h = nodeSize.h * 100 / baseSize.h;
			child.style.left	= nodeSize.x + '%';
			child.style.top		= nodeSize.y + '%';
			child.style.width	= nodeSize.w + '%';
			child.style.height	= nodeSize.h + '%';
			//console.log(nodeSize);
		}
	},

	prepareToScaleTexts:function(domNode) {
		for (var i=0;i<domNode.children.length;++i) {
			var child = domNode.children[i];
			var nodeSize = {x:$(child).position().left,y:$(child).position().top,w:$(child).width(),h:$(child).height()};

			child.originalSize = {fontSize:this.fontSize(child)}
			child.originalSize.x = nodeSize.x;
			child.originalSize.y = nodeSize.y;
			child.originalSize.w = nodeSize.w;
			child.originalSize.h = nodeSize.h;
		}
	},

	scaleTexts:function(domNode) {
		for (var i=0;i<domNode.children.length;++i) {
			var child = domNode.children[i];
			var nodeSize = {x:$(child).position().left,
							y:$(child).position().top,
							w:$(child).width(),
							h:$(child).height()};

			var originalSize = child.originalSize;

			if (!originalSize) {
				base.log.debug("base.dom.scaleTexts(): domNode could not be scaled. Original element size not found.");
				return;
			}

			var scaleFactor = nodeSize.w / originalSize.w;
			var fontSize = originalSize.fontSize.size * scaleFactor;
			child.style.fontSize = fontSize + originalSize.fontSize.units;
		}
	},

	proportionalSize: function(domElement,width,height,animate) {
		var parent = domElement.parentNode;
		var parentSize = {w:$(parent).width(),h:$(parent).height()};
		var parentRatio = parentSize.w/parentSize.h;
		var childRatio = width / height;
		var finalWidth = parentSize.w;
		var finalHeight = parentSize.h;
		var marginTop = "";

		// DEBUG: coloreamos el fondo para ver que pasa
		//domElement.style.backgroundColor = "blue";
		if (parentRatio>childRatio) {
			finalWidth = finalHeight * childRatio;
		}
		else {
		 	finalHeight = finalWidth / childRatio;
		 	var margin = (parentSize.h - finalHeight) / 2;
		 	marginTop = margin + "px";
		}

		if (animate) {
			$(domElement).animate({'width':finalWidth + 'px','height':finalHeight + 'px',marginTop:marginTop});
		}
		else {
			$(domElement).css({'width':finalWidth + 'px','height':finalHeight + 'px',marginTop:marginTop});
		}
	}
}

Class ("base.Log", {
    _currentLevel:0,

    initialize:function() {
        var logLevelParam = base.parameters.get("logLevel");
        logLevelParam = logLevelParam ? logLevelParam:base.hashParams.get("logLevel");
        logLevelParam = logLevelParam.toLowerCase();
        switch (logLevelParam) {
            case "error":
                this.setLevel(base.Log.kLevelError);
                break;
            case "warning":
                this.setLevel(base.Log.kLevelWarning);
                break;
            case "debug":
                this.setLevel(base.Log.kLevelDebug);
                break;
            case "log":
                this.setLevel(base.Log.kLevelLog);
                break;
        }
    },

    logMessage:function(level,message) {
        var prefix = "";
        if (typeof(level)=="string") {
            message = level;
        }
        else if (level>=base.Log.kLevelError && level<=base.Log.kLevelLog) {
            switch (level) {
                case base.Log.kLevelError:
                    prefix = "ERROR: ";
                    break;
                case base.Log.kLevelWarning:
                    prefix = "WARNING: ";
                    break;
                case base.Log.kLevelDebug:
                    prefix = "DEBUG: ";
                    break;
                case base.Log.kLevelLog:
                    prefix = "LOG: ";
                    break;
            }
        }

        if (this._currentLevel>=level && console.log) {
            console.log(prefix + message);
        }
    },

    error:function(message) {
        this.logMessage(base.Log.kLevelError, message);
    },

    warning:function(message) {
        this.logMessage(base.Log.kLevelWarning, message);
    },

    debug:function(message) {
        this.logMessage(base.Log.kLevelDebug, message);
    },

    log:function(message) {
        this.logMessage(base.Log.kLevelLog, message);
    },

    setLevel:function(level) {
        this._currentLevel = level;
    }
});

base.Log.kLevelError    = 1;
base.Log.kLevelWarning  = 2;
base.Log.kLevelDebug    = 3;
base.Log.kLevelLog      = 4;

base.log = new base.Log();

// Deprecated
base.debug = {
    log:function(message) {
        base.log.warning("Use of deprecated function base.debug.log()");
        base.log.debug(message);
    }
}

// This class requires jquery
base.ajax = {
	// onSuccess/onFail(data,type,returnCode,rawData)
	send:function(type,params,onSuccess,onFail) {
		this.assertParams(params);

		var ajaxObj = jQuery.ajax({
			url:params.url,
			data:params.params,
			type:type
		});

		if (typeof(onSuccess)=='function') {
			ajaxObj.done(function(data,textStatus,jqXHR) {
				var contentType = jqXHR.getResponseHeader('content-type')
				onSuccess(data,contentType,jqXHR.status,jqXHR.responseText);
			});
		}

		if (typeof(onFail)=='function') {
			ajaxObj.fail(function(jqXHR,textStatus,error) {
				onFail(textStatus + ' : ' + error,'text/plain',jqXHR.status,jqXHR.responseText);
			});
		}
	},

	assertParams:function(params) {
		if (!params.url) throw new Error("base.ajax.send: url parameter not found");
		if (!params.params) params.params = {}
	}
}

base.ajax["get"] = function(params,onSuccess,onFail) {
	base.ajax.send('get',params,onSuccess,onFail);
}

base.ajax["post"] = function(params,onSuccess,onFail) {
	base.ajax.send('post',params,onSuccess,onFail);
}

base.ajax["put"] = function(params,onSuccess,onFail) {
	base.ajax.send('put',params,onSuccess,onFail);
}

base.ajax["delete"] = function(params,onSuccess,onFail) {
	base.ajax.send('delete',params,onSuccess,onFail);
}

Class ("base.AsyncLoaderCallback", {
	name:"",
	prevCb:null,
	nextCb:null,
	loader:null,

	initialize:function(name) {
		this.name = name;
	},

	load:function(onSuccess,onError) {
		base.debug.log("loading " + this.name);
		onSuccess();
		// If error: onError()
	}
});

Class ("base.AjaxCallback", base.AsyncLoaderCallback,{
	params:null,
	type:'get',

	data:null,
	mimeType:null,
	statusCode:null,
	rawData:null,

	getParams:function() {
		return this.params;
	},

	willLoad:function(callback) {

	},

	didLoadSuccess:function(callback) {
		return true;
	},

	didLoadFail:function(callback) {
		return false;
	},

	initialize:function(params,type) {
		this.name = "ajaxCallback";
		if (type) this.type = type;
		if (typeof(params)=='string') this.params = {url:params}
		else if (typeof(params)=='object') this.params = params;
		else this.params = {}
	},

	load:function(onSuccess,onError) {
		var This = this;
		if (typeof(this.willLoad)=='function') this.willLoad(this);
		base.ajax.send(this.type,this.getParams(),
			function(data,type,code,rawData) {
				var status = true;
				This.data = data;
				This.mimeType = type;
				This.statusCode = code;
				This.rawData = rawData;
				if (typeof(This.didLoadSuccess)=='function') status = This.didLoadSuccess(This);
				if (status) onSuccess();
				else onError();
			},
			function(data,type,code,rawData) {
				var status = false;
				This.data = data;
				This.mimeType = type;
				This.statusCode = code;
				This.rawData = rawData;
				if (typeof(This.didLoadFail)=='function') status = This.didLoadFail(This);
				if (status) onSuccess();
				else onError();
			});
	}
});

Class ("base.JSONCallback", base.AjaxCallback, {
	initialize:function(params,type) { this.parent(params,type); },

	didLoadSuccess:function(callback) {
		if (typeof(callback.data)=='object') return true;

		try {
			callback.data = JSON.parse(callback.data);
			return true;
		}
		catch (e) {
			callback.data = {error:"Unexpected data format",data:callback.data}
			return false;
		}
	}
});

Class ("base.DictionaryCallback", base.AjaxCallback,{
	initialize:function(dictionaryUrl) { this.parent({url:dictionaryUrl}); },

	getParams:function() {
		var lang = base.dictionary.currentLanguage();
		this.params.url = this.params.url + '_' + lang + '.json';
		return this.params;
	},

	didLoadSuccess:function(callback) {
		if (typeof(callback.data)=="string") {
			try {
				callback.data = JSON.parse(callback.data);
			}
			catch (e) {
				return false;
			}
		}
		base.dictionary.addDictionary(callback.data);
		return true;
	},

	didLoadFail:function(callback) {
		return true;
	}
})

Class ("base.AsyncLoader", {
	firstCb:null,
	lastCb:null,
	callbackArray:null,
	generatedId:0,

	continueOnError:false,
	errorCallbacks:null,

	currentCb:null,

	clearError:function() {
		this.errorCallbacks = [];
	},

	initialize:function() {
		this.callbackArray = {};
		this.errorCallbacks = [];
		this.generatedId = 0;
	},

	addCallback:function(cb,name) {
		if (!name) {
			name = "callback_" + this.generatedId++;
		}
		cb.__cbName__ = name;
		this.callbackArray[name] = cb;
		if (!this.firstCb) {
			this.firstCb = cb;
			this.currentCb = cb;
		}
		cb.prevCb = this.lastCb;
		if (this.lastCb) this.lastCb.nextCb = cb;
		this.lastCb = cb;
		cb.loader = this;
		return cb;
	},

	getCallback:function(name) {
		return this.callbackArray[name];
	},

	load:function(onSuccess,onError) {
		var This = this;
		if (this.currentCb) {
			this.currentCb.load(function() {
				This.onComplete(This.currentCb,This.currentCb.__cbName__,true);
				This.currentCb = This.currentCb.nextCb;
				This.load(onSuccess,onError);
			},
			function() {
				This.onComplete(This.currentCb,This.currentCb.__cbName__,false);
				if (This.continueOnError) {
					This.errorCallbacks.push(This.currentCb);
					This.currentCb = This.currentCb.nextCb;
					This.load(onSuccess,onError);
				}
				else if (typeof(onError)=='function') {
					onError();
				}
			});
		}
		else if (typeof(onSuccess)=='function') {
			onSuccess();
		}
	},

	onComplete:function(callback,cbName,status) {

	}
});

Class ("base.Dictionary", {
	dictionary:{},

	initialize:function() {

	},

	addDictionary:function(dict) {
		for (var key in dict) {
			this.dictionary[key] = dict[key];
		}
	},

	translate:function(key) {
		var value = base.dictionary.dictionary[key];
		if (value) return value;
		else return key;
	},

	currentLanguage:function() {
		var lang = navigator.language || window.navigator.userLanguage;
		return lang.substr(0, 2).toLowerCase();
	}
});

base.dictionary = new base.Dictionary();

base.types = {
	parseNumber:function(num) {
		return parseFloat(num);
	},

	parseBoolean:function(num) {
		return /^true$/i.test(num) || /^1$/i.test(num) || /^yes$/.test(num);
	},

	parseDictionary:function(num) {
		try {
			return JSON.parse(num);
		}
		catch (e) {
			return null;
		}
	}
}

base.uuid = function() {
	return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {var r = Math.random()*16|0,v=c=='x'?r:r&0x3|0x8;return v.toString(16);});
}

base.imgBase64 = function(url) {
	var img = new Image();
	img.onload = function(event) {
		var canvas = document.createElement('canvas');
		var ctx = canvas.getContext('2d');
		canvas.width = event.srcElement.width;
		canvas.height = event.srcElement.height;
		ctx.drawImage(event.srcElement,0,0);
		var dataUrl = canvas.toDataURL('image/png');
		event.srcElement.onload = null;
		event.srcElement.src = dataUrl;
		canvas = null;
	};
	img.src = url;
	return img;
}
