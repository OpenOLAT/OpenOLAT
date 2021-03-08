/*  
	Paella HTML 5 Multistream Player
	Copyright (C) 2017  Universitat Politècnica de València Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
*/

var GlobalParams = {
	video:{zIndex:1},
	background:{zIndex:0}
};


window.paella = window.paella || {};
paella.player = null;
paella.version = "6.5.1 - build: 9e04eb8";

(function buildBaseUrl() {
	if (window.paella_debug_baseUrl) {
		paella.baseUrl = window.paella_debug_baseUrl;
	}
	else {
		paella.baseUrl = location.href.replace(/[^/]*$/, '');
	}
})();

paella.events = {
	play:"paella:play",
	pause:"paella:pause",
	next:"paella:next",
	previous:"paella:previous",
	seeking:"paella:seeking",
	seeked:"paella:seeked",
	timeupdate:"paella:timeupdate",
	timeUpdate:"paella:timeupdate",
	seekTo:"paella:setseek",
	endVideo:"paella:endvideo",			// Triggered when a single video stream ends (once per video)
	ended:"paella:ended",				// Triggered when the video ends 
	seekToTime:"paella:seektotime",
	setTrim:"paella:settrim",
	setPlaybackRate:"paella:setplaybackrate",
	setVolume:'paella:setVolume',
	setComposition:'paella:setComposition',
	loadStarted:'paella:loadStarted',
	loadComplete:'paella:loadComplete',
	loadPlugins:'paella:loadPlugins',
	error:'paella:error',
	documentChanged:'paella:documentChanged',
	didSaveChanges:'paella:didsavechanges',
	controlBarWillHide:'paella:controlbarwillhide',
	controlBarDidHide:'paella:controlbardidhide',
	controlBarDidShow:'paella:controlbardidshow',
	hidePopUp:'paella:hidePopUp',
	showPopUp:'paella:showPopUp',
	enterFullscreen:'paella:enterFullscreen',
	exitFullscreen:'paella:exitFullscreen',
	resize:'paella:resize',		// params: { width:paellaPlayerContainer width, height:paellaPlayerContainer height }
	videoZoomChanged:'paella:videoZoomChanged',
	audioTagChanged:'paella:audiotagchanged',
	zoomAvailabilityChanged:'paella:zoomavailabilitychanged',
	
	qualityChanged:'paella:qualityChanged',
	singleVideoReady:'paella:singleVideoReady',
	singleVideoUnloaded:'paella:singleVideoUnloaded',
	videoReady:'paella:videoReady',
	videoUnloaded:'paella:videoUnloaded',
	
	controlBarLoaded:'paella:controlBarLoaded',	
	
	flashVideoEvent:'paella:flashVideoEvent',
	
	captionAdded: 'paella:caption:add', // Event triggered when new caption is available.
	captionsEnabled: 'paella:caption:enabled',  // Event triguered when a caption es enabled.
	captionsDisabled: 'paella:caption:disabled',  // Event triguered when a caption es disabled.
	
	profileListChanged:'paella:profilelistchanged',
	setProfile:'paella:setprofile',

	seekAvailabilityChanged:'paella:seekAvailabilityChanged',
	
	trigger:function(event,params) {
		$(document).trigger(event,params);
	},
	bind:function(event,callback) { $(document).bind(event,function(event,params) { callback(event,params);}); },
	
	setupExternalListener:function() {
		window.addEventListener("message", function(event) {
			if (event.data && event.data.event) {
				paella.events.trigger(event.data.event,event.data.params);
			}
		}, false);
	}
};

paella.events.setupExternalListener();

(() => {
    paella.utils = paella.utils || {};

    // This class requires jquery
    paella.utils.ajax = {
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
            if (!params.url) throw new Error("paella.utils.ajax.send: url parameter not found");
            if (!params.params) params.params = {}
        }
    }

    paella.utils.ajax["get"] = function(params,onSuccess,onFail) {
        paella.utils.ajax.send('get',params,onSuccess,onFail);
    }

    paella.utils.ajax["post"] = function(params,onSuccess,onFail) {
        paella.utils.ajax.send('post',params,onSuccess,onFail);
    }

    paella.utils.ajax["put"] = function(params,onSuccess,onFail) {
        paella.utils.ajax.send('put',params,onSuccess,onFail);
    }

    paella.utils.ajax["delete"] = function(params,onSuccess,onFail) {
        paella.utils.ajax.send('delete',params,onSuccess,onFail);
    }

    // TODO: AsyncLoader is deprecated and should be replaced by promises
    class AsyncLoaderCallback {
        constructor(name) {
            this.name = name;
            this.prevCb = null;
            this.nextCb = null;
            this.loader = null;
        }
    
        load(onSuccess,onError) {
            onSuccess();
            // If error: onError()
        }
    }

    paella.utils.AsyncLoaderCallback = AsyncLoaderCallback;
    
    class AjaxCallback extends paella.utils.AsyncLoaderCallback {
        getParams() {
            return this.params;
        }
    
        willLoad(callback) {
    
        }
    
        didLoadSuccess(callback) {
            return true;
        }
    
        didLoadFail(callback) {
            return false;
        }
    
        constructor(params,type) {
            super();

            this.params = null;
            this.type = 'get';
            this.data = null;
            this.mimeType = null;
            this.statusCode = null;
            this.rawData = null;
        
            
            this.name = "ajaxCallback";
            if (type) this.type = type;
            if (typeof(params)=='string') this.params = {url:params}
            else if (typeof(params)=='object') this.params = params;
            else this.params = {}
        }
    
        load(onSuccess,onError) {
            var This = this;
            if (typeof(this.willLoad)=='function') this.willLoad(this);
            paella.utils.ajax.send(this.type,this.getParams(),
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
    }

    paella.utils.AjaxCallback = AjaxCallback;
    
    class JSONCallback extends paella.utils.AjaxCallback {
        constructor(params,type) {
            super(params,type);
        }
    
        didLoadSuccess(callback) {
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
    }

    paella.utils.JSONCallback = JSONCallback;
    
    class AsyncLoader {
        
        clearError() {
            this.errorCallbacks = [];
        }
    
        constructor() {
            this.firstCb = null;
            this.lastCb = null;
            this.callbackArray = null;
            this.generatedId = 0;
            this.continueOnError = false;
            this.errorCallbacks = null;
            this.currentCb = null;
            
            this.callbackArray = {};
            this.errorCallbacks = [];
            this.generatedId = 0;
        }
    
        addCallback(cb,name) {
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
        }
    
        getCallback(name) {
            return this.callbackArray[name];
        }
    
        load(onSuccess,onError) {
            console.warn("paella.utils.AsyncLoader is deprecated. Consider to replace it with JavaScript promises.");
            
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
        }
    
        onComplete(callback,cbName,status) {
    
        }
    }

    paella.utils.AsyncLoader = AsyncLoader;

})();

(function() {
	let g_delegateCallbacks = {};
	let g_dataDelegates = [];

	class DataDelegate {
		read(context,params,onSuccess) {
			if (typeof(onSuccess)=='function') {
				onSuccess({},true);
			}
		}

		write(context,params,value,onSuccess) {
			if (typeof(onSuccess)=='function') {
				onSuccess({},true);
			}
		}

		remove(context,params,onSuccess) {
			if (typeof(onSuccess)=='function') {
				onSuccess({},true);
			}
		}
	}

	paella.DataDelegate = DataDelegate;

	paella.dataDelegates = {};

	class Data {
		get enabled() { return this._enabled; }

		get dataDelegates() { return g_dataDelegates; }
	
		constructor(config) {
			this._enabled = config.data.enabled;

			// Delegate callbacks
			let executedCallbacks = [];
			for (let context in g_delegateCallbacks) {
				let callback = g_delegateCallbacks[context];
				let DelegateClass = null;
				let delegateName = null;

				if (!executedCallbacks.some((execCallbackData) => {
					if (execCallbackData.callback==callback) {
						delegateName = execCallbackData.delegateName;
						return true;
					}
				})) {
					DelegateClass = g_delegateCallbacks[context]();
					delegateName = DelegateClass.name;
					paella.dataDelegates[delegateName] = DelegateClass;
					executedCallbacks.push({ callback:callback, delegateName:delegateName });
				}

				if (!config.data.dataDelegates[context]) {
					config.data.dataDelegates[context] = delegateName;
				}

			}

			for (var key in config.data.dataDelegates) {
				try {
					
					var delegateName = config.data.dataDelegates[key];
					var DelegateClass = paella.dataDelegates[delegateName];
					var delegateInstance = new DelegateClass();
					g_dataDelegates[key] = delegateInstance;
				
				}
				catch (e) {
					console.warn("Warning: delegate not found - " + delegateName);
				}
			}


			// Default data delegate
			if (!this.dataDelegates["default"]) {
				this.dataDelegates["default"] = new paella.dataDelegates.DefaultDataDelegate();
			}
		}
	
		read(context,key,onSuccess) {
			var del = this.getDelegate(context);
			del.read(context,key,onSuccess);
		}
	
		write(context,key,params,onSuccess) {
			var del = this.getDelegate(context);
			del.write(context,key,params,onSuccess);
		}
	
		remove(context,key,onSuccess) {
			var del = this.getDelegate(context);
			del.remove(context,key,onSuccess);
		}
	
		getDelegate(context) {
			if (this.dataDelegates[context]) return this.dataDelegates[context];
			else return this.dataDelegates["default"];
		}
	}

	paella.Data = Data;

	paella.addDataDelegate = function(context,callback) {
		if (Array.isArray(context)) {
			context.forEach((ctx) => {
				g_delegateCallbacks[ctx] = callback;
			})
		}
		else if (typeof(context)=="string") {
			g_delegateCallbacks[context] = callback;
		}
	}

})();

paella.addDataDelegate(["default","trimming"], () => {
	paella.dataDelegates.DefaultDataDelegate = class CookieDataDelegate extends paella.DataDelegate {
		serializeKey(context,params) {
			if (typeof(params)=='object') {
				params = JSON.stringify(params);
			}
			return context + '|' + params;
		}
	
		read(context,params,onSuccess) {
			var key = this.serializeKey(context,params);
			var value = paella.utils.cookies.get(key);
			try {
				value = unescape(value);
				value = JSON.parse(value);
			}
			catch (e) {}
			if (typeof(onSuccess)=='function') {
				onSuccess(value,true);
			}
		}
	
		write(context,params,value,onSuccess) {
			var key = this.serializeKey(context,params);
			if (typeof(value)=='object') {
				value = JSON.stringify(value);
			}
			value = escape(value);
			paella.utils.cookies.set(key,value);
			if(typeof(onSuccess)=='function') {
				onSuccess({},true);
			}
		}
	
		remove(context,params,onSuccess) {
			var key = this.serializeKey(context,params);
			if (typeof(value)=='object') {
				value = JSON.stringify(value);
			}
			paella.utils.cookies.set(key,'');
			if(typeof(onSuccess)=='function') {
				onSuccess({},true);
			}
		}
	}

	return paella.dataDelegates.DefaultDataDelegate;
})

// Will be initialized inmediately after loading config.json, in PaellaPlayer.onLoadConfig()
paella.data = null;
(function() {

    paella.utils = paella.utils || {};
        
    class Dictionary {
        constructor() {
            this._dictionary = {};
        }

        addDictionary(dict) {
            for (let key in dict) {
                this._dictionary[key] = dict[key];
            }
        }

        translate(key) {
            return this._dictionary[key] || key;
        }

        currentLanguage() {
            let lang = navigator.language || window.navigator.userLanguage;
            return lang.substr(0, 2).toLowerCase();
        }
    }

    paella.utils.dictionary = new Dictionary();

})();

(function() {
    class MessageBox {
		get modalContainerClassName() { return 'modalMessageContainer'; } 
		get frameClassName() { return 'frameContainer'; }
		get messageClassName() { return 'messageContainer'; }
		get errorClassName() { return 'errorContainer'; }
		
		get currentMessageBox() { return this._currentMessageBox; }
		set currentMessageBox(m) { this._currentMessageBox = m; } 
		get messageContainer() { return this._messageContainer; }
		get onClose() { return this._onClose; }
		set onClose(c) { this._onClose = c; }
	
		constructor() {
			this._messageContainer = null;
			$(window).resize((event) => this.adjustTop());
		}

		showFrame(src,params) {
			var closeButton = true;
			var onClose = null;
			if (params) {
				closeButton = params.closeButton;
				onClose = params.onClose;
			}
	
			this.doShowFrame(src,closeButton,onClose);
		}
	
		doShowFrame(src,closeButton,onClose) {
			this.onClose = onClose;
			$('#playerContainer').addClass("modalVisible");
	
			if (this.currentMessageBox) {
				this.close();
			}
	
			var modalContainer = document.createElement('div');
			modalContainer.className = this.modalContainerClassName;
			modalContainer.style.position = 'fixed';
			modalContainer.style.top = '0px';
			modalContainer.style.left = '0px';
			modalContainer.style.right = '0px';
			modalContainer.style.bottom = '0px';
			modalContainer.style.zIndex = 999999;

			var messageContainer = document.createElement('div');
			messageContainer.className = this.frameClassName;
			modalContainer.appendChild(messageContainer);
	
			var iframeContainer = document.createElement('iframe');
			iframeContainer.src = src;
			iframeContainer.setAttribute("frameborder", "0");
			iframeContainer.style.width = "100%";
			iframeContainer.style.height = "100%";
			messageContainer.appendChild(iframeContainer);
	
			if (paella.player && paella.player.isFullScreen()) {
				paella.player.mainContainer.appendChild(modalContainer);
			}else{
				$('body')[0].appendChild(modalContainer);
			}
	
			this.currentMessageBox = modalContainer;
			this._messageContainer = messageContainer;
			this.adjustTop();
	
			if (closeButton) {
				this.createCloseButton();
			}
		}
	
		showElement(domElement,params) {
			var closeButton = true;
			var onClose = null;
			var className = this.messageClassName;
			if (params) {
				className = params.className;
				closeButton = params.closeButton;
				onClose = params.onClose;
			}
	
			this.doShowElement(domElement,closeButton,className,onClose);
		}
	
		showMessage(message,params) {
			var closeButton = true;
			var onClose = null;
			var className = this.messageClassName;
			if (params) {
				className = params.className;
				closeButton = params.closeButton;
				onClose = params.onClose;
			}
	
			this.doShowMessage(message,closeButton,className,onClose);
		}
	
		doShowElement(domElement,closeButton,className,onClose) {
			this.onClose = onClose;
			$('#playerContainer').addClass("modalVisible");
	
			if (this.currentMessageBox) {
				this.close();
			}
			if (!className) className = this.messageClassName;
	
			var modalContainer = document.createElement('div');
			modalContainer.className = this.modalContainerClassName;
			modalContainer.style.position = 'fixed';
			modalContainer.style.top = '0px';
			modalContainer.style.left = '0px';
			modalContainer.style.right = '0px';
			modalContainer.style.bottom = '0px';
			modalContainer.style.zIndex = 999999;
	
			var messageContainer = document.createElement('div');
			messageContainer.className = className;
			messageContainer.appendChild(domElement);
			modalContainer.appendChild(messageContainer);
	
			$('body')[0].appendChild(modalContainer);
	
			this.currentMessageBox = modalContainer;
			this._messageContainer = messageContainer;
			this.adjustTop();
	
			if (closeButton) {
				this.createCloseButton();
			}
		}
	
		doShowMessage(message,closeButton,className,onClose) {
			this.onClose = onClose;
			$('#playerContainer').addClass("modalVisible");
	
			if (this.currentMessageBox) {
				this.close();
			}
			if (!className) className = this.messageClassName;
	
			var modalContainer = document.createElement('div');
			modalContainer.className = this.modalContainerClassName;
			modalContainer.style.position = 'fixed';
			modalContainer.style.top = '0px';
			modalContainer.style.left = '0px';
			modalContainer.style.right = '0px';
			modalContainer.style.bottom = '0px';
			modalContainer.style.zIndex = 999999;
	
			var messageContainer = document.createElement('div');
			messageContainer.className = className;
			messageContainer.innerHTML = message;
			modalContainer.appendChild(messageContainer);
	
			if (paella.player && paella.player.isFullScreen()) {
				paella.player.mainContainer.appendChild(modalContainer);
			}else{
				$('body')[0].appendChild(modalContainer);
			}
	
			this.currentMessageBox = modalContainer;
			this._messageContainer = messageContainer;
			this.adjustTop();
	
			if (closeButton) {
				this.createCloseButton();
			}
		}

		showError(message,params) {
			var closeButton = false;
			var onClose = null;
			if (params) {
				closeButton = params.closeButton;
				onClose = params.onClose;
			}
	
			this.doShowError(message,closeButton,onClose);
		}
	
		doShowError(message,closeButton,onClose) {
			this.doShowMessage(message,closeButton,this.errorClassName,onClose);
		}
	
		createCloseButton() {
			if (this._messageContainer) {
				var closeButton = document.createElement('span');
				this._messageContainer.appendChild(closeButton);
				closeButton.className = 'paella_messageContainer_closeButton icon-cancel-circle';
				$(closeButton).click((event) => this.onCloseButtonClick());
				$(window).keyup((evt) => {
					if (evt.keyCode == 27) {
						this.onCloseButtonClick();
					}
				});
		
			}
		}
		
		adjustTop() {
			if (this.currentMessageBox) {
	
				var msgHeight = $(this._messageContainer).outerHeight();
				var containerHeight = $(this.currentMessageBox).height();
	
				var top = containerHeight/2 - msgHeight/2;
				this._messageContainer.style.marginTop = top + 'px';
			}
		}
		
		close() {
			if (this.currentMessageBox && this.currentMessageBox.parentNode) {
				var msgBox = this.currentMessageBox;
				var parent = msgBox.parentNode;
				$('#playerContainer').removeClass("modalVisible");
				$(msgBox).animate({opacity:0.0},300,function() {
					parent.removeChild(msgBox);
				});
				if (this.onClose) {
					this.onClose();
				}
			}
		}
	
		onCloseButtonClick() {
			this.close();
		}
	}
	
	paella.MessageBox = MessageBox;
	paella.messageBox = new paella.MessageBox();
})();

(function() {
    paella.utils = paella.utils || {};
    
    paella.utils.cookies = {
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
    
    paella.utils.hashParams = {
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
    
    paella.utils.parameters = {
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
            var urlOnly = paella.utils.hashParams.extractUrl();
            if (urlOnly.lastIndexOf('?')>=0) {
                urlOnly = urlOnly.substr(0,urlOnly.lastIndexOf('?'));
            }
            return urlOnly;
        },
    
        extractParams:function() {
            // Primero quitar los parámetros hash
            var urlAndParams = paella.utils.hashParams.extractUrl();
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
            var hashParameters = paella.utils.hashParams.extractParams();
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
                    if (keyValue.length>=2 && paella.utils.hashParams.get(keyValue[0])=='') {
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

})();
(() => {

    paella.utils = paella.utils || {};

    function parseOperatingSystem(userAgentString) {
		this.system.MacOS = /Macintosh/.test(userAgentString);
		this.system.Windows = /Windows/.test(userAgentString);
		this.system.iPhone = /iPhone/.test(userAgentString);
		this.system.iPodTouch = /iPod/.test(userAgentString);
		this.system.iPad = /iPad/.test(userAgentString) || /FxiOS/.test(userAgentString);
		this.system.iOS = this.system.iPhone || this.system.iPad || this.system.iPodTouch;
		this.system.Android = /Android/.test(userAgentString);
		this.system.Linux = (this.system.Android) ? false:/Linux/.test(userAgentString);

		if (this.system.MacOS) {
			this.system.OSName = "Mac OS X";
			parseMacOSVersion.apply(this, [userAgentString]);
		}
		else if (this.system.Windows) {
			this.system.OSName = "Windows";
			parseWindowsVersion.apply(this, [userAgentString]);
		}
		else if (this.system.Linux) {
			this.system.OSName = "Linux";
			parseLinuxVersion.apply(this, [userAgentString]);
		}
		else if (this.system.iOS) {
			this.system.OSName = "iOS";
			parseIOSVersion.apply(this, [userAgentString]);
		}
		else if (this.system.Android) {
			this.system.OSName = "Android";
			parseAndroidVersion.apply(this, [userAgentString]);
		}
	}

	function parseBrowser(userAgentString) {
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
        
        // The attribute this.browser.Chrome will still be true, because it is the same browser after all
        this.browser.EdgeChromium = /Chrome.*Edg\/([0-9\.]+)/.test(userAgentString);
        if (this.browser.EdgeChromium) {
            this.browser.Name = "Edge Chromium";
            this.browser.Vendor = "Microsoft";
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

		let firefoxIOS = this.browser.Firefox || /FxiOS\/(\d+\.\d+)/.test(userAgentString);
		if (firefoxIOS) {
			this.browser.Firefox = true;
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

		parseBrowserVersion.apply(this, [userAgentString]);
	}

	function parseBrowserVersion(userAgentString) {
		if (/([\d]+)\.([\d]+)\.*([\d]*)/.test(this.browser.Version.versionString)) {
			this.browser.Version.major = Number(RegExp.$1);
			this.browser.Version.minor = Number(RegExp.$2);
			this.browser.Version.revision = (RegExp.$3) ? Number(RegExp.$3):0;
		}
	}

	function parseMacOSVersion(userAgentString) {
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
            case 9:
                this.system.Version.name = "Mavericks";
                break;
            case 10:
                this.system.Version.name = "Yosemite";
                break;
            case 11:
                this.system.Version.name = "El Capitan";
                break;
            case 12:
                this.system.Version.name = "Sierra";
                break;
            case 13:
                this.system.Version.name = "High Sierra";
                break;
            case 14:
                this.system.Version.name = "Mojave";
                break;
            case 15:
                this.system.Version.name = "Catalina";
                break;
		}
	}

	function parseWindowsVersion(userAgentString) {
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
            else if (major==10) {
                this.system.Version.name = "10";
            }
		}
		else {
			this.system.Version.major = 0;
			this.system.Version.minor = 0;
			this.system.Version.name = "Unknown";
			this.system.Version.stringValue = "Unknown";
		}
	}

	function parseLinuxVersion(userAgentString) {
		// Muchos navegadores no proporcionan información sobre la distribución de linux... no se puede hacer mucho más que esto
		this.system.Version = {};
		this.system.Version.major = 0;
		this.system.Version.minor = 0;
		this.system.Version.revision = 0;
		this.system.Version.name = "";
		this.system.Version.stringValue = "Unknown distribution";
	}

	function parseIOSVersion(userAgentString) {
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
	}

	function parseAndroidVersion(userAgentString) {
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
	}

	function getInfoString() {
		return navigator.userAgent;
    }
    
    class UserAgent {
		constructor(userAgentString = null) {
            if (!userAgentString) {
                userAgentString = navigator.userAgent;
            }
			this._system = {};
			this._browser = {};

            parseOperatingSystem.apply(this,[userAgentString]);
            parseBrowser.apply(this, [userAgentString]);
		}

		get system() { return this._system; }

		get browser() { return this._browser; }

        getInfoString() {
            return navigator.userAgent;
        }

        get infoString() { navigator.userAgent; }
	}

    paella.UserAgent = UserAgent;

    paella.utils.userAgent = new paella.UserAgent();
    
})();

/*  
	Paella HTML 5 Multistream Player
	Copyright (C) 2017  Universitat Politècnica de València Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
*/


// Paella Mouse Manager
///////////////////////////////////////////////////////
(() => {
	class MouseManager {	
		get targetObject() { return this._targetObject; }
		set targetObject(t) { this._targetObject = t; }

		constructor() {
			paella.events.bind('mouseup',(event) => this.up(event));
			paella.events.bind('mousemove',(event) => this.move(event));
			paella.events.bind('mouseover',(event) =>  this.over(event));
		}
	
		down(targetObject,event) {
			this.targetObject = targetObject;
			if (this.targetObject && this.targetObject.down) {
				const pageX = event.pageX || (event.changedTouches.length > 0 ? event.changedTouches[0].pageX : 0);
				const pageY = event.pageY || (event.changedTouches.length > 0 ? event.changedTouches[0].pageY : 0);
				this.targetObject.down(event,pageX,pageY);
				event.cancelBubble = true;
			}
			return false;
		}
	
		up(event) {
			if (this.targetObject && this.targetObject.up) {
				const pageX = event.pageX || (event.changedTouches.length > 0 ? event.changedTouches[0].pageX : 0);
				const pageY = event.pageY || (event.changedTouches.length > 0 ? event.changedTouches[0].pageY : 0);
				this.targetObject.up(event,pageX,pageY);
				event.cancelBubble = true;
			}
			this.targetObject = null;
			return false;
		}
	
		out(event) {
			if (this.targetObject && this.targetObject.out) {
				const pageX = event.pageX || (event.changedTouches.length > 0 ? event.changedTouches[0].pageX : 0);
				const pageY = event.pageY || (event.changedTouches.length > 0 ? event.changedTouches[0].pageY : 0);
				this.targetObject.out(event,pageX,pageY);
				event.cancelBubble = true;
			}
			return false;
		}
	
		move(event) {
			if (this.targetObject && this.targetObject.move) {
				const pageX = event.pageX || (event.changedTouches.length > 0 ? event.changedTouches[0].pageX : 0);
				const pageY = event.pageY || (event.changedTouches.length > 0 ? event.changedTouches[0].pageY : 0);
				this.targetObject.move(event,pageX,pageY);
				event.cancelBubble = true;
			}
			return false;
		}
	
		over(event) {
			if (this.targetObject && this.targetObject.over) {
				const pageX = event.pageX || (event.changedTouches.length > 0 ? event.changedTouches[0].pageX : 0);
				const pageY = event.pageY || (event.changedTouches.length > 0 ? event.changedTouches[0].pageY : 0);
				this.targetObject.over(event,pageX,pageY);
				event.cancelBubble = true;
			}
			return false;
		}
	}

	paella.MouseManager = MouseManager;
})();


// paella.utils
///////////////////////////////////////////////////////
(function initSkinDeps() {
	var link = document.createElement('link');
	link.rel = 'stylesheet';
	link.href = paella.baseUrl + 'resources/bootstrap/css/bootstrap.min.css';
	link.type = 'text/css';
	link.media = 'screen';
	link.charset = 'utf-8';
	document.head.appendChild(link);
})();

paella.utils = paella.utils || {};

paella.utils.mouseManager = new paella.MouseManager();

paella.utils.folders = {
	get: function(folder) {
		if (paella.player && paella.player.config && paella.player.config.folders && paella.player.config.folders[folder]) {
			return paella.player.config.folders[folder];	
		}
		return undefined;			
	},
	
	profiles: function() {
		return paella.baseUrl + (paella.utils.folders.get("profiles") || "config/profiles");
	},
	
	resources: function() {
		return paella.baseUrl + (paella.utils.folders.get("resources") || "resources");
	},
	
	skins: function() {
		return paella.baseUrl + (paella.utils.folders.get("skins") || paella.utils.folders.get("resources") + "/style");
	}
}
	
paella.utils.styleSheet = {
	removeById:function(id) {
		var outStyleSheet = $(document.head).find('#' + id)[0];
		if (outStyleSheet) {
			document.head.removeChild(outStyleSheet);
		}
	},
	
	remove:function(fileName) {
		var links = document.head.getElementsByTagName('link');
		for (var i =0; i<links.length; ++i) {
			if (links[i].href) {
				document.head.removeChild(links[i]);
				break;
			}
		}
	},
	
	add:function(fileName,id) {
		var link = document.createElement('link');
		link.rel = 'stylesheet';
		link.href = fileName;
		link.type = 'text/css';
		link.media = 'screen';
		link.charset = 'utf-8';
		if (id) link.id = id;
		document.head.appendChild(link);
	},
	
	swap:function(outFile,inFile) {
		this.remove(outFile);
		this.add(inFile);
	}
};
	
paella.utils.skin = {
	set:function(skinName) {
		var skinId = 'paellaSkin';
		paella.utils.styleSheet.removeById(skinId);
		paella.utils.styleSheet.add(paella.utils.folders.skins() + '/style_' + skinName + '.css');
		paella.utils.cookies.set("skin",skinName);
	},
	
	restore:function(defaultSkin) {
		var storedSkin = paella.utils.cookies.get("skin");
		if (storedSkin && storedSkin!="") {
			this.set(storedSkin);
		}
		else {
			this.set(defaultSkin);
		}
	}
};

paella.utils.timeParse = {
	timeToSeconds:function(timeString) {
		var hours = 0;
		var minutes = 0;
		var seconds =0;
		if (/([0-9]+)h/i.test(timeString)) {
			hours = parseInt(RegExp.$1) * 60 * 60;
		}
		if (/([0-9]+)m/i.test(timeString)) {
			minutes = parseInt(RegExp.$1) * 60;
		}
		if (/([0-9]+)s/i.test(timeString)) {
			seconds = parseInt(RegExp.$1);
		}
		return hours + minutes + seconds;
	},

	secondsToTime:function(seconds) {
		var hrs = ~~ (seconds / 3600);
		if (hrs<10) hrs = '0' + hrs;
		var mins = ~~ ((seconds % 3600) / 60);
		if (mins<10) mins = '0' + mins;
		var secs = Math.floor(seconds % 60);
		if (secs<10) secs = '0' + secs;
		return hrs + ':' + mins + ':' + secs;
	},
	secondsToText:function(secAgo) {
		// Seconds
		if (secAgo <= 1) {
			return paella.utils.dictionary.translate("1 second ago");
		}
		if (secAgo < 60) {
			return paella.utils.dictionary.translate("{0} seconds ago").replace(/\{0\}/g, secAgo);
		}
		// Minutes
		var minAgo = Math.round(secAgo/60);
		if (minAgo <= 1) {
			return paella.utils.dictionary.translate("1 minute ago");
		}
		if (minAgo < 60) {
			return paella.utils.dictionary.translate("{0} minutes ago").replace(/\{0\}/g, minAgo);
		}
		//Hours
		var hourAgo = Math.round(secAgo/(60*60));
		if (hourAgo <= 1) {
			return paella.utils.dictionary.translate("1 hour ago");
		}
		if (hourAgo < 24) {
			return paella.utils.dictionary.translate("{0} hours ago").replace(/\{0\}/g, hourAgo);
		}
		//Days
		var daysAgo = Math.round(secAgo/(60*60*24));
		if (daysAgo <= 1) {
			return paella.utils.dictionary.translate("1 day ago");
		}
		if (daysAgo < 24) {
			return paella.utils.dictionary.translate("{0} days ago").replace(/\{0\}/g, daysAgo);
		}
		//Months
		var monthsAgo = Math.round(secAgo/(60*60*24*30));
		if (monthsAgo <= 1) {
			return paella.utils.dictionary.translate("1 month ago");
		}
		if (monthsAgo < 12) {
			return paella.utils.dictionary.translate("{0} months ago").replace(/\{0\}/g, monthsAgo);
		}
		//Years
		var yearsAgo = Math.round(secAgo/(60*60*24*365));
		if (yearsAgo <= 1) {
			return paella.utils.dictionary.translate("1 year ago");
		}
		return paella.utils.dictionary.translate("{0} years ago").replace(/\{0\}/g, yearsAgo);
	},
	matterhornTextDateToDate: function(mhdate) {
		var d = new Date();
		d.setFullYear(parseInt(mhdate.substring(0, 4), 10));
		d.setMonth(parseInt(mhdate.substring(5, 7), 10) - 1);
		d.setDate(parseInt(mhdate.substring(8, 10), 10));
		d.setHours(parseInt(mhdate.substring(11, 13), 10));
		d.setMinutes(parseInt(mhdate.substring(14, 16), 10));
		d.setSeconds(parseInt(mhdate.substring(17, 19), 10));

		return d;
	}
};

paella.utils.objectFromString = (str) => {
	var arr = str.split(".");

	var fn = (window || this);
	for (var i = 0, len = arr.length; i < len; i++) {
		fn = fn[arr[i]];
	}

	if (typeof fn !== "function") {
		throw new Error("constructor not found");
	}

	return fn;
}

paella.utils.uuid = function() {
	return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
		let r = Math.random() * 16 | 0;
		let v = (c == 'x' ? r : r) & 0x3 | 0x8;
		return v.toString(16);
	});
};

(() => {

	class TimerManager {
		constructor() {
			this.timerArray = [];
			this.lastId = 0;
		}
	
		setupTimer(timer,time) {
			this.lastId++;
			timer.timerId = this.lastId;
			timer.timeout = time;
			this.timerArray[this.lastId] = timer;
			timer.jsTimerId = setTimeout(
				`g_timerManager.executeTimerCallback(${ this.lastId })`	
			, time);
		}
	
		executeTimerCallback(timerId) {
			var timer = this.timerArray[timerId];
			if (timer && timer.callback) {
				timer.callback(timer,timer.params);
			}
			if (timer.repeat) {
				timer.jsTimerId = setTimeout(
					`g_timerManager.executeTimerCallback(${ timer.timerId })`
				, timer.timeout);
			}
		}
	}

	window.g_timerManager = new TimerManager();

	class Timer {
		static sleep(milliseconds) {
			let start = new Date().getTime();
			for (let i = 0; i<1e7; ++i) {
				if ((new Date().getTime() - start) > milliseconds) {
					break;
				}
			}
		}

		constructor(callback,time,params) {
			this.callback = callback;
			this.params = params;
			this._repeat = false;
			g_timerManager.setupTimer(this,time);
		}

		get repeat() { return this._repeat; }
		set repeat(r) { this._repeat = r; } 

		cancel() {
			clearTimeout(this.jsTimerId);
		}
	}

	paella.utils.Timer = Timer;
})();

(() => {
	// Include scripts in header
	let g_requiredScripts = {};
	
	paella.require = function(path) {
		if (!g_requiredScripts[path]) {
			g_requiredScripts[path] = new Promise((resolve,reject) => {
				paella.utils.ajax.get({url: path}, (data) => {
					try {
						let module = {
							exports: null
						};
						let exports = null;
						eval(data);
						if (module && module.exports) {
							resolve(module.exports);
						}
						else {
							let geval = eval;
							geval(data);
							resolve();
						}
					}
					catch(err) {
						reject(err);
					}
				},
				(err) => {
					reject(err);
				});
			});
		}
		return g_requiredScripts[path];
	};

	paella.tabIndex = new (class TabIndexManager {
		constructor() {
			this._last = 1;
		}

		get next() {
			return this._last++;
		}

		get last() {
			return this._last - 1;
		}

		get tabIndexElements() {
			let result = Array.from($('[tabindex]'));

			// Sort by tabIndex
			result.sort((a,b) => {
				return a.tabIndex-b.tabIndex;
			});

			return result;
		}

		insertAfter(target,elements) {
			if (target.tabIndex==null || target.tabIndex==-1) {
				throw Error("Insert tab index: the target element does not have a valid tabindex.");
			}

			let targetIndex = -1;
			let newTabIndexElements = this.tabIndexElements;
			newTabIndexElements.some((elem,i) => {
				if (elem==target) {
					targetIndex = i;
					return true;
				}
			});
			newTabIndexElements.splice(targetIndex + 1, 0, ...elements);
			newTabIndexElements.forEach((elem,index) => {
				elem.tabIndex = index; + 1
			});
			this._last = newTabIndexElements.length;
		}

		removeTabIndex(elements) {
			Array.from(elements).forEach((e) => {
				e.removeAttribute("tabindex");
			});

			this.tabIndexElements.forEach((elem,index) => {
				elem.tabIndex = index + 1;
				this._last = elem.tabIndex + 1;
			});
		}
	})();

	paella.URL = class PaellaURL {
		constructor(urlText) {
			this._urlText = urlText;
		}

		get text() {
			return this._urlText;
		}

		get isAbsolute() {
			return new RegExp('^([a-z]+://|//)', 'i').test(this._urlText) ||
					/^\//.test(this._urlText);	// We consider that the URLs starting with / are absolute and local to this server
		}

		get isExternal() {
			let thisUrl = new URL(this.absoluteUrl);
			let localUrl = new URL(location.href);
			return thisUrl.hostname != localUrl.hostname;
		}

		get absoluteUrl() {
			let result = "";
			if (new RegExp('^([a-z]+://|//)', 'i').test(this._urlText)) {
				result = this._urlText;
			}
			else if (/^\//.test(this._urlText)) {
				result = `${ location.origin }${ this._urlText }`
			}
			else {
				let pathname = location.pathname;
				if (pathname.lastIndexOf(".")>pathname.lastIndexOf("/")) {
					pathname = pathname.substring(0,pathname.lastIndexOf("/")) + '/';
				}
				result = `${ location.origin }${ pathname }${ this._urlText }`;
			}
			result = (new URL(result)).href;
			return result;
		}

		appendPath(text) {
			if (this._urlText.endsWith("/") && text.startsWith("/")) {
				this._urlText += text.substring(1,text.length);
			}
			else if (this._urlText.endsWith("/") || text.startsWith("/")) {
				this._urlText += text;
			}
			else {
				this._urlText += "/" + text;
			}
			return this;
		}
	}
	
	class Log {
        constructor() {
			this._currentLevel = 0;
            var logLevelParam = paella.utils.parameters.get("logLevel");
            logLevelParam = logLevelParam ? logLevelParam:paella.utils.hashParams.get("logLevel");
            logLevelParam = logLevelParam.toLowerCase();
            switch (logLevelParam) {
                case "error":
                    this.setLevel(paella.log.kLevelError);
                    break;
                case "warning":
                    this.setLevel(paella.log.kLevelWarning);
                    break;
                case "debug":
                    this.setLevel(paella.log.kLevelDebug);
                    break;
                case "log":
                    this.setLevel(paella.log.kLevelLog);
                    break;
            }
        }
    
        logMessage(level,message) {
            var prefix = "";
            if (typeof(level)=="string") {
                message = level;
            }
            else if (level>=paella.log.kLevelError && level<=paella.log.kLevelLog) {
                switch (level) {
                    case paella.log.kLevelError:
                        prefix = "ERROR: ";
                        break;
                    case paella.log.kLevelWarning:
                        prefix = "WARNING: ";
                        break;
                    case paella.log.kLevelDebug:
                        prefix = "DEBUG: ";
                        break;
                    case paella.log.kLevelLog:
                        prefix = "LOG: ";
                        break;
                }
            }
    
            if (this._currentLevel>=level && console.log) {
                console.log(prefix + message);
            }
        }
    
        error(message) {
            this.logMessage(paella.log.kLevelError, message);
        }
    
        warning(message) {
            this.logMessage(paella.log.kLevelWarning, message);
        }
    
        debug(message) {
            this.logMessage(paella.log.kLevelDebug, message);
        }
    
        log(message) {
            this.logMessage(paella.log.kLevelLog, message);
        }
    
        setLevel(level) {
            this._currentLevel = level;
        }
	}
    
    Log.kLevelError    = 1;
    Log.kLevelWarning  = 2;
    Log.kLevelDebug    = 3;
    Log.kLevelLog      = 4;
    
    paella.log = new Log();	
})();

paella.AntiXSS = {
	htmlEscape: function (str) {
		return String(str)
    		.replace(/&/g, '&amp;')
    		.replace(/"/g, '&quot;')
    		.replace(/'/g, '&#39;')
    		.replace(/</g, '&lt;')
    		.replace(/>/g, '&gt;');
    	},

    htmlUnescape: function (value){
		return String(value)
			.replace(/&quot;/g, '"')
			.replace(/&#39;/g, "'")
			.replace(/&lt;/g, '<')
			.replace(/&gt;/g, '>')
			.replace(/&amp;/g, '&');
	}
};

function paella_DeferredResolved(param) {
	return new Promise((resolve) => {
		resolve(param);
	});
}

function paella_DeferredRejected(param) {
	return new Promise((resolve,reject) => {
		reject(param);
	});
}

function paella_DeferredNotImplemented () {
	return paella_DeferredRejected(new Error("not implemented"));
}

/*  
	Paella HTML 5 Multistream Player
	Copyright (C) 2017  Universitat Politècnica de València Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
*/


(() => {

	class Node {
		get identifier() { return this._identifier; }
		set identifier(id) { this._identifier = id; }
		get nodeList() { return this._nodeList; }
		get parent() { return this._parent; }
		set parent(p) { this._parent = p; } 
	
		constructor(id) {
			this._nodeList = {};
			this.identifier = id;
		}
	
		addTo(parentNode) {
			parentNode.addNode(this);
		}
	
		addNode(childNode) {
			childNode.parent = this;
			this.nodeList[childNode.identifier] = childNode;
			return childNode;
		}

		getNode(id) {
			return this.nodeList[id];
		}
		
		removeNode(childNode) {
			if (this.nodeList[childNode.identifier]) {
				delete this.nodeList[childNode.identifier];
				return true;
			}
			return false;
		}
	}

	paella.Node = Node;
	
	class DomNode extends paella.Node {
		get domElement() { return this._domElement; }
		
		get domElementType() { return this._elementType; }
		set domElementType(newType) {
			this._elementType = newType;
			let oldElement = this._domElement;
			let parent = oldElement.parentNode;
			let newElement = document.createElement(newType);
			parent.removeChild(oldElement);
			parent.appendChild(newElement);
			this._domElement = newElement;
			newElement.innerHTML = oldElement.innerHTML;
			for (let i = 0; i<oldElement.attributes.length; ++i) {
				let attr = oldElement.attributes[i];
				newElement.setAttribute(attr.name,attr.value);
			}
		}
	
		constructor(elementType,id,style) {
			super(id);
			this._elementType = elementType;
			this._domElement = document.createElement(elementType);
			this.domElement.id = id;
			if (style) this.style = style;
		}

		set style(s) { $(this.domElement).css(s); }
	
		addNode(childNode) {
			let returnValue = super.addNode(childNode);
			this.domElement.appendChild(childNode.domElement);
			return returnValue;
		}
	
		onresize() {
		}
		
		removeNode(childNode) {
			if (super.removeNode(childNode)) {
				this.domElement.removeChild(childNode.domElement);
			}
		}
	}

	paella.DomNode = DomNode;
	
	class Button extends paella.DomNode {
		get isToggle() { return this._isToggle; }
		set isToggle(t) { this._isToggle = t; } 
	
		constructor(id,className,action,isToggle) {
			var style = {};
			super('div',id,style);
			this.isToggle = isToggle;
			this.domElement.className = className;
			if (isToggle) {
				$(this.domElement).click((event) => {
					this.toggleIcon();
				});
			}
			$(this.domElement).click('click',action);
		}
	
		isToggled() {
			if (this.isToggle) {
				var element = this.domElement;
				return /([a-zA-Z0-9_]+)_active/.test(element.className);
			}
			else {
				return false;
			}
		}
	
		toggle() {
			this.toggleIcon();
		}
	
		toggleIcon() {
			var element = this.domElement;
			if (/([a-zA-Z0-9_]+)_active/.test(element.className)) {
				element.className = RegExp.$1;
			}
			else {
				element.className = element.className + '_active';
			}
		}
	
		show() {
			$(this.domElement).show();
		}
	
		hide() {
			$(this.domElement).hide();
		}
	
		visible() {
			return this.domElement.visible();
		}
	}

	paella.Button = Button;
	
})();

(function() {
    let g_profiles = [];

	paella.addProfile = function(cb) {
		cb().then((profileData) => {
			if (profileData) {
                g_profiles.push(profileData);
                if (typeof(profileData.onApply)!="function") {
                    profileData.onApply = function() { }
                }
                if (typeof(profileData.onDeactivte)!="function") {
                    profileData.onDeactivate = function() {}
                }
				paella.events.trigger(paella.events.profileListChanged, { profileData:profileData });
			}
		});
    }
    
    // Utility functions
    function hideBackground() {
        let bkgNode = this.container.getNode("videoContainerBackground");
        if (bkgNode) this.container.removeNode(bkgNode);
    }

    function showBackground(bkgData) {
        if (!bkgData) return;
        hideBackground.apply(this);
        this.backgroundData = bkgData;
        let style = {
            backgroundImage: `url(${paella.baseUrl}${paella.utils.folders.get("resources")}/style/${ bkgData.content })`,
            backgroundSize: "100% 100%",
            zIndex: bkgData.layer,
            position: 'absolute',
            left: bkgData.rect.left + "px",
            right: bkgData.rect.right + "px",
            width: "100%",
            height: "100%",
        }
        this.container.addNode(new paella.DomNode('div',"videoContainerBackground",style));
    }

    function hideAllLogos() {
        if (this.logos == undefined) return;
        for (var i=0;i<this.logos.length;++i) {
            var logoId = this.logos[i].content.replace(/\./ig,"-");
            var logo = this.container.getNode(logoId);
            $(logo.domElement).hide();
        }
    }

    function showLogos(logos) {
        this.logos = logos;
        var relativeSize = new paella.RelativeVideoSize();
        for (var i=0; i<logos.length;++i) {
            var logo = logos[i];
            var logoId = logo.content.replace(/\./ig,"-");
            var logoNode = this.container.getNode(logoId);
            var rect = logo.rect;
            if (!logoNode) {
                style = {};
                logoNode = this.container.addNode(new paella.DomNode('img',logoId,style));
                logoNode.domElement.setAttribute('src', `${paella.baseUrl}${paella.utils.folders.get("resources")}/style/${logo.content}`);
            }
            else {
                $(logoNode.domElement).show();
            }
            var percentTop = Number(relativeSize.percentVSize(rect.top)) + '%';
            var percentLeft = Number(relativeSize.percentWSize(rect.left)) + '%';
            var percentWidth = Number(relativeSize.percentWSize(rect.width)) + '%';
            var percentHeight = Number(relativeSize.percentVSize(rect.height)) + '%';
            var style = {top:percentTop,left:percentLeft,width:percentWidth,height:percentHeight,position:'absolute',zIndex:logo.zIndex};
            $(logoNode.domElement).css(style);
        }
    }

    function hideButtons() {
        if (this.buttons) {
            this.buttons.forEach((btn) => {
                this.container.removeNode(this.container.getNode(btn.id));
            });
            this.buttons = null;
        }
    }

    function showButtons(buttons,profileData) {
        hideButtons.apply(this);
        if (buttons) {
            let relativeSize = new paella.RelativeVideoSize();
            this.buttons = buttons;
            buttons.forEach((btn,index) => {
                btn.id = "button_" + index;
                let rect = btn.rect;
                let percentTop = relativeSize.percentVSize(rect.top) + '%';
                let percentLeft = relativeSize.percentWSize(rect.left) + '%';
                let percentWidth = relativeSize.percentWSize(rect.width) + '%';
                let percentHeight = relativeSize.percentVSize(rect.height) + '%';
                let url = paella.baseUrl;
                url = url.replace(/\\/ig,'/');
                let style = {
                    top:percentTop,
                    left:percentLeft,
                    width:percentWidth,
                    height:percentHeight,
                    position:'absolute',
                    zIndex:btn.layer,
                    backgroundImage: `url(${paella.baseUrl}${paella.utils.folders.get("resources")}/style/${ btn.icon })`,
                    backgroundSize: '100% 100%',
                    display: 'block'
                };
                let logoNode = this.container.addNode(new paella.DomNode('div',btn.id,style));
                logoNode.domElement.className = "paella-profile-button";
                logoNode.domElement.data = {
                    action: btn.onClick,
                    profileData: profileData
                }
                $(logoNode.domElement).click(function(evt) {
                    this.data.action.apply(this.data.profileData,[evt]);
                    evt.stopPropagation();
                    return false;
                })
            })
        }
    }

    function getClosestRect(profileData,videoDimensions) {
        var minDiff = 10;
        var re = /([0-9\.]+)\/([0-9\.]+)/;
        var result = profileData.rect[0];
        var videoAspectRatio = videoDimensions.h==0 ? 1.777777:videoDimensions.w / videoDimensions.h;
        var profileAspectRatio = 1;
        var reResult = false;
        profileData.rect.forEach(function(rect) {
            if ((reResult = re.exec(rect.aspectRatio))) {
                profileAspectRatio = Number(reResult[1]) / Number(reResult[2]);
            }
            var diff = Math.abs(profileAspectRatio - videoAspectRatio);
            if (minDiff>diff) {
                minDiff = diff;
                result = rect;
            }
        });
        return result;
    }

    function applyProfileWithJson(profileData,animate) {
        if (animate==undefined) animate = true;
        if (!profileData) return;
        let getProfile = (content) => {
            let result = null;
            
                profileData && profileData.videos.some((videoProfile) => {
                    if (videoProfile.content==content) {
                        result = videoProfile;
                    }
                    return result!=null;
                });
                return result;
            
        };

        let applyVideoRect = (profile,videoData,videoWrapper,player) => {
            let frameStrategy = this.profileFrameStrategy;
            if (frameStrategy) {
                let rect = getClosestRect(profile,videoData.res);
                let videoSize = videoData.res;
                let containerSize = { width:$(this.domElement).width(), height:$(this.domElement).height() };
                let scaleFactor = rect.width / containerSize.width;
                let scaledVideoSize = { width:videoSize.w * scaleFactor, height:videoSize.h * scaleFactor };
                rect.left = Number(rect.left);
                rect.top = Number(rect.top);
                rect.width = Number(rect.width);
                rect.height = Number(rect.height);
                rect = frameStrategy.adaptFrame(scaledVideoSize,rect);
                
                let visible = /true/i.test(profile.visible);
                rect.visible = visible;
                let layer = parseInt(profile.layer);
                videoWrapper.domElement.style.zIndex = layer;

                videoWrapper.setRect(rect,animate);
                videoWrapper.setVisible(visible,animate);

                // The disable/enable functions may not be called on main audio player
                let isMainAudioPlayer = paella.player.videoContainer.streamProvider.mainAudioPlayer==player;
                visible ? player.enable(isMainAudioPlayer) : player.disable(isMainAudioPlayer);
            }
        };
        
        profileData && profileData.onApply();
        hideAllLogos.apply(this);
        profileData && showLogos.apply(this,[profileData.logos]);
        hideBackground.apply(this);
        profileData && showBackground.apply(this,[profileData.background]);
        hideButtons.apply(this);
        profileData && showButtons.apply(this,[profileData.buttons, profileData]);
        
        this.streamProvider.videoStreams.forEach((streamData,index) => {
            let profile = getProfile(streamData.content);
            let player = this.streamProvider.videoPlayers[index];
            let videoWrapper = this.videoWrappers[index];
            if (profile) {
                player.getVideoData()
                    .then((data) => {
                        applyVideoRect(profile,data,videoWrapper,player);
                    });
            }
            else if (videoWrapper) {
                videoWrapper.setVisible(false,animate);
                player.disable(paella.player.videoContainer.streamProvider.mainAudioPlayer==player);
            }
        });
    }

    let profileReloadCount = 0;
    const maxProfileReloadCunt = 20;

	class Profiles {
        constructor() {
            paella.events.bind(paella.events.controlBarDidHide, () => this.hideButtons());
            paella.events.bind(paella.events.controlBarDidShow, () => this.showButtons());

            paella.events.bind(paella.events.profileListChanged, () => {
                if (paella.player && paella.player.videoContainer && 
                    (!this.currentProfile || this.currentProfileName!=this.currentProfile.id))
                {
                    this.setProfile(this.currentProfileName,false);
                }
            })
        }

        get profileList() { return g_profiles; }

        getDefaultProfile() {
            if (paella.player.videoContainer.masterVideo() && paella.player.videoContainer.masterVideo().defaultProfile()) {
                return paella.player.videoContainer.masterVideo().defaultProfile();
            }
            if (paella.player && paella.player.config && paella.player.config.defaultProfile) {
                return paella.player.config.defaultProfile;
            }
            return undefined;
        }

        loadProfile(profileId) {
            let result = null;
            g_profiles.some((profile) => {
                if (profile.id==profileId) {
                    result = profile;
                }
                return result;
            });
            return result;
        }

        get currentProfile() { return this.getProfile(this._currentProfileName); }

        get currentProfileName() { return this._currentProfileName; }

        setProfile(profileName,animate) {
            
            if (!profileName) {
                return false;
            }
            
            animate = paella.utils.userAgent.browser.Explorer ? false:animate;
            if (this.currentProfile) {
                this.currentProfile.onDeactivate();
            }

            if (!paella.player.videoContainer.ready) {
                return false;	// Nothing to do, the video is not loaded
            }
            else {
                let profileData = this.loadProfile(profileName) || (g_profiles.length>0 && g_profiles[0]);
                if (!profileData && g_profiles.length==0) {
                    if (profileReloadCount < maxProfileReloadCunt) {
                        profileReloadCount++;
                        // Try to load the profile again later, maybe the profiles are not loaded yet
                        setTimeout(() => {
                            this.setProfile(profileName,animate);
                        },100);
                        return false;
                    }
                    else {
                        console.error("No valid video layout profiles were found. Check that the 'content' attribute setting in 'videoSets', at config.json file, matches the 'content' property in the video manifest.");
                        return false;
                    }
                }
                else {
                    this._currentProfileName = profileName;
                    applyProfileWithJson.apply(paella.player.videoContainer,[profileData,animate]);
                    return true;
                }
            }
        }

        getProfile(profileName) {
            let result = null;
            this.profileList.some((p) => {
                if (p.id==profileName) {
                    result = p;
                    return true;
                }
            })
            return result;
        }

        placeVideos() {
            this.setProfile(this._currentProfileName,false);
        }

        hideButtons() {
            $('.paella-profile-button').hide();
        }

        showButtons() {
            $('.paella-profile-button').show();
        }
    }

    paella.profiles = new Profiles();
    
    
})();
/*  
	Paella HTML 5 Multistream Player
	Copyright (C) 2017  Universitat Politècnica de València Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
*/


(function() {
	class VideoQualityStrategy {
		static Factory() {
			var config = paella.player.config;

			try {
				var strategyClass = config.player.videoQualityStrategy;
				var ClassObject = paella.utils.objectFromString(strategyClass);
				var strategy = new ClassObject();
				if (strategy instanceof paella.VideoQualityStrategy) {
					return strategy;
				}
			}
			catch (e) {
			}
			
			return null;
		}

		getParams() {
			return paella.player.config.player.videoQualityStrategyParams || {};
		}
	
		getQualityIndex(source) {
			if (source.length>0) {
				return source[source.length-1];
			}
			else {
				return source;
			}
		}
	}

	paella.VideoQualityStrategy = VideoQualityStrategy;
	
	class BestFitVideoQualityStrategy extends paella.VideoQualityStrategy {
		getQualityIndex(source) {
			var index = source.length - 1;
	
			if (source.length>0) {
				var selected = source[0];
				var win_w = $(window).width();
				var win_h = $(window).height();
				var win_res = (win_w * win_h);
	
				if (selected.res && selected.res.w && selected.res.h) {
					var selected_res = parseInt(selected.res.w) * parseInt(selected.res.h);
					var selected_diff = Math.abs(win_res - selected_res);
	
					for (var i=0; i<source.length; ++i) {
						var res = source[i].res;
						if (res) {
							var m_res = parseInt(source[i].res.w) * parseInt(source[i].res.h);
							var m_diff = Math.abs(win_res - m_res);
	
							if (m_diff <= selected_diff){
								selected_diff = m_diff;
								index = i;
							}
						}
					}
				}
			}
	
			return index;
		}
	}

	paella.BestFitVideoQualityStrategy = BestFitVideoQualityStrategy;
	
	class LimitedBestFitVideoQualityStrategy extends paella.VideoQualityStrategy {
		getQualityIndex(source) {
			var index = source.length - 1;
			var params = this.getParams();
	
			if (source.length>0) {
				//var selected = source[0];
				var selected = null;
				var win_h = $(window).height();
				var maxRes = params.maxAutoQualityRes || 720;
				var diff = Number.MAX_VALUE;
	
				source.forEach(function(item,i) { 
					if (item.res && item.res.h<=maxRes ) {
						var itemDiff = Math.abs(win_h - item.res.h);
						if (itemDiff<diff) {
							selected = item;
							index = i;
						}
					}
				});
			}
			return index;
		}
	}

	paella.LimitedBestFitVideoQualityStrategy = LimitedBestFitVideoQualityStrategy;

	class VideoFactory {
		isStreamCompatible(streamData) {
			return false;
		}
	
		getVideoObject(id, streamData, rect) {
			return null;
		}
	}

	paella.VideoFactory = VideoFactory;
	paella.videoFactories = paella.videoFactories || {};

	paella.videoFactory = {
		_factoryList:[],
	
		initFactories:function() {
			if (paella.videoFactories) {
				var This = this;
				paella.player.config.player.methods.forEach(function(method) {
					if (method.enabled && paella.videoFactories[method.factory]) {
						This.registerFactory(new paella.videoFactories[method.factory]());
					}
				});
				this.registerFactory(new paella.videoFactories.EmptyVideoFactory());
			}
		},
	
		getVideoObject:function(id, streamData, rect) {
			if (this._factoryList.length==0) {
				this.initFactories();
			}
			var selectedFactory = null;
			if (this._factoryList.some(function(factory) {
				if (factory.isStreamCompatible(streamData)) {
					selectedFactory = factory;
					return true;
				}
			})) {
				return selectedFactory.getVideoObject(id, streamData, rect);
			}
			return null;
		},
	
		registerFactory:function(factory) {
			this._factoryList.push(factory);
		}
	};
	
	
})();


(function() {
    class AudioElementBase extends paella.DomNode {
        constructor(id,stream) {
            super('div',id);
            this._stream = stream;
            this._ready = false;
        }

        get ready() { return this._ready; } 

        get currentTimeSync() { return null; }
        get volumeSync() { return null; }
        get pausedSync() { return null; }
        get durationSync() { return null; }

        get stream() { return this._stream; }
        setAutoplay() {return Promise.reject(new Error("no such compatible video player"));}
        load() {return Promise.reject(new Error("no such compatible video player")); }
        play() { return Promise.reject(new Error("no such compatible video player")); }
        pause() { return Promise.reject(new Error("no such compatible video player")); }
        isPaused() { return Promise.reject(new Error("no such compatible video player")); }
        duration() { return Promise.reject(new Error("no such compatible video player")); }
        setCurrentTime(time) { return Promise.reject(new Error("no such compatible video player")); }
        currentTime() { return Promise.reject(new Error("no such compatible video player")); }
        setVolume(volume) { return Promise.reject(new Error("no such compatible video player")); }
        volume() { return Promise.reject(new Error("no such compatible video player")); }
        setPlaybackRate(rate) { return Promise.reject(new Error("no such compatible video player")); }
        playbackRate() { return Promise.reject(new Error("no such compatible video player")); }
        unload() { return Promise.reject(new Error("no such compatible video player")); }

        getQualities() {
            return Promise.resolve([
                {
                    index: 0,
                    res: { w: 0, h: 1 },
                    src: "",
                    toString: function() { return ""; },
                    shortLabel: function() { return ""; },
                    compare: function() { return 0; }
                }
            ]);
        }

        getCurrentQuality() { return Promise.resolve(0); }
        defaultProfile() { return null; }
        
        supportAutoplay() { return false;}
    };

    paella.AudioElementBase = AudioElementBase;
    paella.audioFactories = {};

    class AudioFactory {
        isStreamCompatible(streamData) {
            return false;
        }

        getAudioObject(id,streamData) {
            return null;
        }
    }

    paella.AudioFactory = AudioFactory;

    paella.audioFactory = {
        _factoryList:[],

        initFactories:function() {
            if (paella.audioFactories) {
                var This = this;
                paella.player.config.player.audioMethods = paella.player.config.player.audioMethods || {

                };
                paella.player.config.player.audioMethods.forEach(function(method) {
                    if (method.enabled) {
                        This.registerFactory(new paella.audioFactories[method.factory]());
                    }
                });
            }
        },

        getAudioObject:function(id, streamData) {
            if (this._factoryList.length==0) {
                this.initFactories();
            }
            var selectedFactory = null;
            if (this._factoryList.some(function(factory) {
                if (factory.isStreamCompatible(streamData)) {
                    selectedFactory = factory;
                    return true;
                }
            })) {
                return selectedFactory.getAudioObject(id, streamData);
            }
            return null;
        },

        registerFactory:function(factory) {
            this._factoryList.push(factory);
        }
    };

})();

(function() {

function checkReady(cb) {
    let This = this;
    return new Promise((resolve,reject) => {
        if (This._ready) {
            resolve(typeof(cb)=='function' ? cb():true);
        }
        else {
            function doCheck() {
                if (This.audio.readyState>=This.audio.HAVE_CURRENT_DATA) {
                    This._ready = true;
                    resolve(typeof(cb)=='function' ? cb():true);
                }
                else {
                    setTimeout(doCheck,50);
                }
            }
            doCheck();
        }
    });
}

class MultiformatAudioElement extends paella.AudioElementBase {
    constructor(id,stream) {
        super(id,stream);
        this._streamName = "audio";

        this._audio = document.createElement('audio');
        this.domElement.appendChild(this._audio);
    }

    get buffered() {
		return this.audio && this.audio.buffered;
    }
    
    get audio() { return this._audio; }

    get currentTimeSync() {
		return this.ready ? this.audio.currentTimeSync : null;
	}

	get volumeSync() {
		return this.ready ? this.audio.volumeSync : null;
	}

	get pausedSync() {
		return this.ready ? this.audio.pausedSync : null;
	}

	get durationSync() {
		return this.ready ? this.audio.durationSync : null;
	}

    setAutoplay(ap) {
        this.audio.autoplay = ap;
    }

    load() {
        var This = this;
		var sources = this._stream.sources[this._streamName];
		var stream = sources.length>0 ? sources[0]:null;
		this.audio.innerText = "";
		if (stream) {
			var sourceElem = this.audio.querySelector('source');
			if (!sourceElem) {
				sourceElem = document.createElement('source');
				this.audio.appendChild(sourceElem);
			}

			sourceElem.src = stream.src;
			if (stream.type) sourceElem.type = stream.type;
			this.audio.load();

            return checkReady.apply(this, [function() {
                return stream;
            }]);
		}
		else {
			return Promise.reject(new Error("Could not load video: invalid quality stream index"));
		}
    }

    play() {
        return checkReady.apply(this, [() => {
            this.audio.play();
        }]);
    }

    pause() {
        return checkReady.apply(this, [() => {
            this.audio.pause();
        }]);
    }

    isPaused() {
        return checkReady.apply(this,[() => {
            return this.audio.paused;
        }]);
    }

    duration() {
        return checkReady.apply(this,[() => {
            return this.audio.duration;
        }]);
    }

    setCurrentTime(time) {
        return checkReady.apply(this,[() => {
            this.audio.currentTime = time;
        }]);
    }

    currentTime() {
        return checkReady.apply(this,[() => {
            return this.audio.currentTime;
        }]);
    }

    setVolume(volume) {
        return checkReady.apply(this,[() => {
            return this.audio.volume = volume;
        }]);
    }

    volume() {
        return checkReady.apply(this,[() => {
            return this.audio.volume;
        }]);
    }

    setPlaybackRate(rate) { 
        return checkReady.apply(this,[() => {
            this.audio.playbackRate = rate;
        }]);
    }
    playbackRate() {
        return checkReady.apply(this,[() => {
            return this.audio.playbackRate;
        }]);
    }

    unload() { return Promise.resolve(); }
};

paella.MultiformatAudioElement = MultiformatAudioElement;

class MultiformatAudioFactory {
    isStreamCompatible(streamData) {
        return true;
    }

    getAudioObject(id,streamData) {
        return new paella.MultiformatAudioElement(id,streamData);
    }
}

paella.audioFactories.MultiformatAudioFactory = MultiformatAudioFactory;

})();

/*  
	Paella HTML 5 Multistream Player
	Copyright (C) 2017  Universitat Politècnica de València Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
*/

(() => {

paella.Profiles = {
	profileList: null,
	
	getDefaultProfile: function() {
		if (paella.player.videoContainer.masterVideo() && paella.player.videoContainer.masterVideo().defaultProfile()) {
			return paella.player.videoContainer.masterVideo().defaultProfile();
		}
		if (paella.player && paella.player.config && paella.player.config.defaultProfile) {
			return paella.player.config.defaultProfile;
		}
		return undefined;		
	},
	
	loadProfile:function(profileName,onSuccessFunction) {
	
		var defaultProfile  = this.getDefaultProfile();		
		this.loadProfileList(function(data){		
			var profileData;
			if(data[profileName] ){
				// Successful mapping
				profileData = data[profileName];
			} else if (data[defaultProfile]) {
				// Fallback to default profile
				profileData = data[defaultProfile];
			} else {
				// Unable to find or map defaultProfile in profiles.json
				paella.log.debug("Error loading the default profile. Check your Paella Player configuration");
				return false;
			}
			onSuccessFunction(profileData);
		});
	},

	loadProfileList:function(onSuccessFunction) {
		var thisClass = this;
		if (this.profileList == null) {
			var params = { url: paella.utils.folders.profiles() + "/profiles.json" };
	
			paella.utils.ajax.get(params,function(data,mimetype,code) {
					if (typeof(data)=="string") {
						data = JSON.parse(data);
					}
					thisClass.profileList = data;
					onSuccessFunction(thisClass.profileList);
				},
				function(data,mimetype,code) {
					paella.log.debug("Error loading video profiles. Check your Paella Player configuration");
				}
			);
		}
		else {
			onSuccessFunction(thisClass.profileList);
		}
	}
};

class RelativeVideoSize {
	get w() { return this._w || 1280; }
	set w(v) { this._w = v; }
	get h() { return this._h || 720; }
	set h(v) { this._h = v; }
	
	proportionalHeight(newWidth) {
		return Math.floor(this.h * newWidth / this.w);
	}

	proportionalWidth(newHeight) {
		return Math.floor(this.w * newHeight / this.h);
	}

	percentVSize(pxSize) {
		return pxSize * 100 / this.h;
	}

	percentWSize(pxSize) {
		return pxSize * 100 / this.w;
	}

	aspectRatio() {
		return this.w/this.h;
	}
}

paella.RelativeVideoSize = RelativeVideoSize;



class VideoRect extends paella.DomNode {
	
	constructor(id, domType, left, top, width, height) {
		super(domType,id,{});
		
		let zoomSettings = paella.player.config.player.videoZoom || {};
		let zoomEnabled = (zoomSettings.enabled!==undefined ? zoomSettings.enabled : true) && this.allowZoom();
		this.style = zoomEnabled ? {width:this._zoom + '%',height:"100%",position:'absolute'} : { width:"100%", height:"100%" };
		this._rect = null;

		let eventCapture = document.createElement('div');
		setTimeout(() => this.domElement.parentElement.appendChild(eventCapture), 10);

		eventCapture.id = id + "EventCapture";
		eventCapture.style.position = "absolute";
		eventCapture.style.top = "0px";
		eventCapture.style.left = "0px";
		eventCapture.style.right = "0px";
		eventCapture.style.bottom = "0px";
		this.eventCapture = eventCapture;

		if (zoomEnabled) {
			this._zoomAvailable = true;
			function checkZoomAvailable() {
				let minWindowSize = (paella.player.config.player &&
									paella.player.config.player.videoZoom &&
									paella.player.config.player.videoZoom.minWindowSize) || 500;

				let available = $(window).width()>=minWindowSize;
				if (this._zoomAvailable!=available) {
					this._zoomAvailable = available;
					paella.events.trigger(paella.events.zoomAvailabilityChanged, { available:available });
				}
			}
			checkZoomAvailable.apply(this);

			$(window).resize(() => {
				checkZoomAvailable.apply(this);
			});

			this._zoom = 100;
			this._mouseCenter = { x:0, y:0 };
			this._mouseDown = { x:0, y:0 };
			this._zoomOffset = { x:0, y: 0 };
			this._maxZoom = zoomSettings.max || 400;
			$(this.domElement).css({
				width: "100%",
				height: "100%",
				left:"0%",
				top: "0%"
			});

			Object.defineProperty(this,'zoom', {
				get: function() { return this._zoom; }
			});

			Object.defineProperty(this,'zoomOffset',{
				get: function() { return this._zoomOffset; }
			});

			function mousePos(evt) {
				return {
					x:evt.originalEvent.offsetX,
					y:evt.originalEvent.offsetY
				};
			}

			function wheelDelta(evt) {
				let wheel = evt.originalEvent.deltaY * (paella.utils.userAgent.Firefox ? 2:1);
				let maxWheel = 6;
				return -Math.abs(wheel) < maxWheel ? wheel : maxWheel * Math.sign(wheel);
			}

			function touchesLength(p0,p1) {
				return Math.sqrt(
					(p1.x - p0.x) * (p1.x - p0.x) +
					(p1.y - p0.y) * (p1.y - p0.y)
				);
			}

			function centerPoint(p0,p1) {
				return {
					x:(p1.x - p0.x) / 2 + p0.x,
					y:(p1.y - p0.y) / 2 + p0.y
				}
			}

			function panImage(o) {
				let center = {
					x: this._mouseCenter.x - o.x * 1.1,
					y: this._mouseCenter.y - o.y * 1.1
				};
				let videoSize = {
					w: $(this.domElement).width(),
					h: $(this.domElement).height()
				};
				let maxOffset = this._zoom - 100;
				let offset = {
					x: (center.x * maxOffset / videoSize.w) * (maxOffset / 100),
					y: (center.y * maxOffset / videoSize.h) * (maxOffset / 100)
				};
				
				if (offset.x>maxOffset) {
					offset.x = maxOffset;
				}
				else if (offset.x<0) {
					offset.x = 0;
				}
				else {
					this._mouseCenter.x = center.x;
				}
				if (offset.y>maxOffset) {
					offset.y = maxOffset;
				}
				else if (offset.y<0) {
					offset.y = 0;
				}
				else {
					this._mouseCenter.y = center.y;
				}
				$(this.domElement).css({
					left:"-" + offset.x + "%",
					top: "-" + offset.y + "%"
				});

				this._zoomOffset = {
					x: offset.x,
					y: offset.y
				};
				paella.events.trigger(paella.events.videoZoomChanged,{ video:this });
			}

			let touches = [];
			$(eventCapture).on('touchstart', (evt) => {
				if (!this.allowZoom() || !this._zoomAvailable) return;
				touches = [];
				let videoOffset = $(this.domElement).offset();
				for (let i=0; i<evt.originalEvent.targetTouches.length; ++i) {
					let touch = evt.originalEvent.targetTouches[i];
					touches.push({
						x: touch.screenX - videoOffset.left,
						y: touch.screenY - videoOffset.top
					});
				}
				if (touches.length>1) evt.preventDefault();
			});

			$(eventCapture).on('touchmove', (evt) => {
				if (!this.allowZoom() || !this._zoomAvailable) return;
				let curTouches = [];
				let videoOffset = $(this.domElement).offset();
				for (let i=0; i<evt.originalEvent.targetTouches.length; ++i) {
					let touch = evt.originalEvent.targetTouches[i];
					curTouches.push({
						x: touch.screenX - videoOffset.left,
						y: touch.screenY - videoOffset.top
					});
				}
				if (curTouches.length>1 && touches.length>1) {
					let l0 = touchesLength(touches[0],touches[1]);
					let l1 = touchesLength(curTouches[0],curTouches[1]);
					let delta = l1 - l0;
					let center = centerPoint(touches[0],touches[1]);
					this._mouseCenter = center;
					
					this._zoom += delta;
					this._zoom = this._zoom < 100 ? 100 : this._zoom;			
					this._zoom = this._zoom > this._maxZoom ? this._maxZoom : this._zoom;
					let newVideoSize = {
						w: $(this.domElement).width(),
						h: $(this.domElement).height()
					};
					let mouse = this._mouseCenter;
					$(this.domElement).css({
						width:this._zoom + '%',
						height:this._zoom + '%'
					});
					
					let maxOffset = this._zoom - 100;
					let offset = {
						x: (mouse.x * maxOffset / newVideoSize.w),
						y: (mouse.y * maxOffset / newVideoSize.h)
					};
					
					offset.x = offset.x<maxOffset ? offset.x : maxOffset;
					offset.y = offset.y<maxOffset ? offset.y : maxOffset;
					
					$(this.domElement).css({
						left:"-" + offset.x + "%",
						top: "-" + offset.y + "%"
					});

					this._zoomOffset = {
						x: offset.x,
						y: offset.y
					};
					paella.events.trigger(paella.events.videoZoomChanged,{ video:this });
					touches = curTouches;
					evt.preventDefault();
				}
				else if (curTouches.length>0) {
					let desp = {
						x: curTouches[0].x - touches[0].x,
						y: curTouches[0].y - touches[0].y,
					}

					panImage.apply(this,[desp]);
					touches = curTouches;

					evt.preventDefault();
				}
			});

			$(eventCapture).on('touchend', (evt) => {
				if (!this.allowZoom() || !this._zoomAvailable) return;
				if (touches.length>1) evt.preventDefault();
			});

			this.zoomIn = () => {
				if (this._zoom>=this._maxZoom || !this._zoomAvailable) return;
				if (!this._mouseCenter) {
					this._mouseCenter = {
						x: $(this.domElement).width() / 2,
						y: $(this.domElement).height() / 2	
					}
				}
				this._zoom += 25;
				this._zoom = this._zoom < 100 ? 100 : this._zoom;			
				this._zoom = this._zoom > this._maxZoom ? this._maxZoom : this._zoom;
				let newVideoSize = {
					w: $(this.domElement).width(),
					h: $(this.domElement).height()
				};
				let mouse = this._mouseCenter;
				$(this.domElement).css({
					width:this._zoom + '%',
					height:this._zoom + '%'
				});
				
				let maxOffset = this._zoom - 100;
				let offset = {
					x: (mouse.x * maxOffset / newVideoSize.w) * (maxOffset / 100),
					y: (mouse.y * maxOffset / newVideoSize.h) * (maxOffset / 100)
				};
				
				offset.x = offset.x<maxOffset ? offset.x : maxOffset;
				offset.y = offset.y<maxOffset ? offset.y : maxOffset;
				
				$(this.domElement).css({
					left:"-" + offset.x + "%",
					top: "-" + offset.y + "%"
				});

				this._zoomOffset = {
					x: offset.x,
					y: offset.y
				};
				paella.events.trigger(paella.events.videoZoomChanged,{ video:this });
			}

			this.zoomOut = () => {
				if (this._zoom<=100 || !this._zoomAvailable) return;
				if (!this._mouseCenter) {
					this._mouseCenter = {
						x: $(this.domElement).width() / 2,
						y: $(this.domElement).height() / 2	
					}
				}
				this._zoom -= 25;
				this._zoom = this._zoom < 100 ? 100 : this._zoom;			
				this._zoom = this._zoom > this._maxZoom ? this._maxZoom : this._zoom;
				let newVideoSize = {
					w: $(this.domElement).width(),
					h: $(this.domElement).height()
				};
				let mouse = this._mouseCenter;
				$(this.domElement).css({
					width:this._zoom + '%',
					height:this._zoom + '%'
				});
				
				let maxOffset = this._zoom - 100;
				let offset = {
					x: (mouse.x * maxOffset / newVideoSize.w) * (maxOffset / 100),
					y: (mouse.y * maxOffset / newVideoSize.h) * (maxOffset / 100)
				};
				
				offset.x = offset.x<maxOffset ? offset.x : maxOffset;
				offset.y = offset.y<maxOffset ? offset.y : maxOffset;
				
				$(this.domElement).css({
					left:"-" + offset.x + "%",
					top: "-" + offset.y + "%"
				});

				this._zoomOffset = {
					x: offset.x,
					y: offset.y
				};
				paella.events.trigger(paella.events.videoZoomChanged,{ video:this });
			}

			let altScrollMessageContainer = document.createElement('div');
			altScrollMessageContainer.className = "alt-scroll-message-container";
			altScrollMessageContainer.innerHTML = "<p>" + paella.utils.dictionary.translate("Use Alt+Scroll to zoom the video") + "</p>";
			eventCapture.appendChild(altScrollMessageContainer);
			$(altScrollMessageContainer).css({ opacity: 0.0 });
			let altScrollMessageTimer = null;
			function clearAltScrollMessage(animate = true) {
				animate ? 
					$(altScrollMessageContainer).animate({ opacity: 0.0 }) :
					$(altScrollMessageContainer).css({ opacity: 0.0 });
			}
			function showAltScrollMessage() {
				if (altScrollMessageTimer) {
					clearTimeout(altScrollMessageTimer);
					altScrollMessageTimer = null;
				}
				else {
					$(altScrollMessageContainer).css({ opacity: 1.0 });
				}
				altScrollMessageTimer = setTimeout(() => {
					clearAltScrollMessage();
					altScrollMessageTimer = null;
				}, 500);
			}

			$(eventCapture).on('mousewheel wheel',(evt) => {
				if (!this.allowZoom() || !this._zoomAvailable) return;
				if (!evt.altKey) {
					showAltScrollMessage();
					return;
				}
				else {
					clearAltScrollMessage(false);
					if (altScrollMessageTimer) {
						clearTimeout(altScrollMessageTimer);
						altScrollMessageTimer = null;
					}
				}
				let mouse = mousePos(evt);
				let wheel = wheelDelta(evt);
				if (this._zoom>=this._maxZoom && wheel>0) return;
				this._zoom += wheel;
				this._zoom = this._zoom < 100 ? 100 : this._zoom;			
				this._zoom = this._zoom > this._maxZoom ? this._maxZoom : this._zoom;
				let newVideoSize = {
					w: $(this.domElement).width(),
					h: $(this.domElement).height()
				};
				$(this.domElement).css({
					width:this._zoom + '%',
					height:this._zoom + '%'
				});
				
				let maxOffset = this._zoom - 100;
				let offset = {
					x: (mouse.x * maxOffset / newVideoSize.w) * (maxOffset / 100),
					y: (mouse.y * maxOffset / newVideoSize.h) * (maxOffset / 100)
				};
				
				offset.x = offset.x<maxOffset ? offset.x : maxOffset;
				offset.y = offset.y<maxOffset ? offset.y : maxOffset;
				
				$(this.domElement).css({
					left:"-" + offset.x + "%",
					top: "-" + offset.y + "%"
				});

				this._zoomOffset = {
					x: offset.x,
					y: offset.y
				};
				paella.events.trigger(paella.events.videoZoomChanged,{ video:this });

				this._mouseCenter = mouse;
				evt.stopPropagation();
				return false;
			});

			$(eventCapture).on('mousedown',(evt) => {
				this._mouseDown = mousePos(evt);
				this.drag = true;
			});

			$(eventCapture).on('mousemove',(evt) => {
				if (!this.allowZoom() || !this._zoomAvailable) return;
				let mouse = mousePos(evt);
				let offset = {
					x: mouse.x - this._mouseDown.x,
					y: mouse.y - this._mouseDown.y
				};

				// We have not found out why there are sometimes sudden jumps in the
				// position of the mouse cursos, so we avoid the problem
				if ((Math.abs(offset.x)>80 || Math.abs(this.y)>80) && this.drag) {
					this._mouseDown = mouse;
					return;
				}

				this.drag = evt.buttons>0;

				if (this.drag) {
					paella.player.videoContainer.disablePlayOnClick();
					panImage.apply(this,[offset]);
					this._mouseDown = mouse;
				}
			});

			$(eventCapture).on('mouseup',(evt) => {
				if (!this.allowZoom() || !this._zoomAvailable) return;
				this.drag = false;
				paella.player.videoContainer.enablePlayOnClick(1000);
			});

			$(eventCapture).on('mouseleave',(evt) => {
				this.drag = false;
			});
		}
	}

	get canvasData() {
		let canvasType = this._stream && Array.isArray(this._stream.canvas) && this._stream.canvas[0];
		let canvasData = canvasType && paella.getVideoCanvasData(this._stream.canvas[0]) || { mouseEventsSupport: false, webglSupport: false };
		return canvasData;
	}

	allowZoom() {
		return !this.canvasData.mouseEventsSupport;
	}

	setZoom(zoom,left,top,tween=0) {
		if (this.zoomAvailable()) {
			this._zoomOffset.x = left;
			this._zoomOffset.y = top;
			this._zoom = zoom;
			
			if (tween==0) {
				$(this.domElement).css({
					width:this._zoom + '%',
					height:this._zoom + '%',
					left:"-" + this._zoomOffset.x + "%",
					top: "-" + this._zoomOffset.y + "%"
				});
			}
			else {
				$(this.domElement).stop(true,false).animate({
					width:this._zoom + '%',
					height:this._zoom + '%',
					left:"-" + this._zoomOffset.x + "%",
					top: "-" + this._zoomOffset.y + "%"
				},tween,"linear");
			}

			paella.events.trigger(paella.events.videoZoomChanged,{ video:this });
		}
	}

	captureFrame() {
		return Promise.resolve(null);
	}

	supportsCaptureFrame() {
		return Promise.resolve(false);
	}

	// zoomAvailable will only return true if the zoom is enabled, the
	// video plugin supports zoom and the current video resolution is higher than
	// the current video size
	zoomAvailable() {
		return this.allowZoom() && this._zoomAvailable;
	}

	disableEventCapture() {
		this.eventCapture.style.pointerEvents = 'none';
	}

	enableEventCapture() {
		this.eventCapture.style.pointerEvents = '';
	}
}

paella.VideoRect = VideoRect;

class VideoElementBase extends paella.VideoRect {

	constructor(id,stream,containerType,left,top,width,height) {
		super(id, containerType, left, top, width, height);
		
		this._stream = stream;

		this._ready = false;
		this._autoplay = false;
		this._videoQualityStrategy = null;
		
		if (this._stream.preview) this.setPosterFrame(this._stream.preview);

		if (this.canvasData.mouseEventsSupport) {
			this.disableEventCapture();
		}
	}

	get ready() { return this._ready; }
	get stream() { return this._stream; }

	defaultProfile() {
		return null;
	}

	// Synchronous functions: returns null if the resource is not loaded. Use only if 
	// the resource is loaded.
	get currentTimeSync() { return null; }
	get volumeSync() { return null; }
	get pausedSync() { return null; }
	get durationSync() { return null; }

	// Initialization functions
	setVideoQualityStrategy(strategy) {
		this._videoQualityStrategy = strategy;
	}

	setPosterFrame(url) {
		paella.log.debug("TODO: implement setPosterFrame() function");
	}

	setAutoplay(autoplay) {
		this._autoplay = autoplay;
	}

	setMetadata(data) {
		this._metadata = data;
	}

	load() {
		return paella_DeferredNotImplemented();
	}

	supportAutoplay() {
		return true;
	}

	// Video canvas functions
	videoCanvas() {
		return Promise.reject(new Error("VideoElementBase::videoCanvas(): Not implemented in child class."));
	}

	// Multi audio functions
	supportsMultiaudio() {
		return Promise.resolve(false);
	}

	getAudioTracks() {
		return Promise.resolve([]);
	}

	setCurrentAudioTrack(trackId) {
		return Promise.resolve(false);
	}

	getCurrentAudioTrack() {
		return Promise.resolve(-1);
	}

	// Playback functions
	getVideoData() {
		return paella_DeferredNotImplemented();
	}
	
	play() {
		paella.log.debug("TODO: implement play() function in your VideoElementBase subclass");
		return paella_DeferredNotImplemented();
	}

	pause() {
		paella.log.debug("TODO: implement pause() function in your VideoElementBase subclass");
		return paella_DeferredNotImplemented();
	}

	isPaused() {
		paella.log.debug("TODO: implement isPaused() function in your VideoElementBase subclass");
		return paella_DeferredNotImplemented();
	}

	duration() {
		paella.log.debug("TODO: implement duration() function in your VideoElementBase subclass");
		return paella_DeferredNotImplemented();
	}

	setCurrentTime(time) {
		paella.log.debug("TODO: implement setCurrentTime() function in your VideoElementBase subclass");
		return paella_DeferredNotImplemented();
	}

	currentTime() {
		paella.log.debug("TODO: implement currentTime() function in your VideoElementBase subclass");
		return paella_DeferredNotImplemented();
	}

	setVolume(volume) {
		paella.log.debug("TODO: implement setVolume() function in your VideoElementBase subclass");
		return paella_DeferredNotImplemented();
	}

	volume() {
		paella.log.debug("TODO: implement volume() function in your VideoElementBase subclass");
		return paella_DeferredNotImplemented();
	}

	setPlaybackRate(rate) {
		paella.log.debug("TODO: implement setPlaybackRate() function in your VideoElementBase subclass");
		return paella_DeferredNotImplemented();
	}

	playbackRate() {
		paella.log.debug("TODO: implement playbackRate() function in your VideoElementBase subclass");
		return paella_DeferredNotImplemented();
	}

	getQualities() {
		return paella_DeferredNotImplemented();
	}

	setQuality(index) {
		return paella_DeferredNotImplemented();
	}

	getCurrentQuality() {
		return paella_DeferredNotImplemented();
	}
	
	unload() {
		this._callUnloadEvent();
		return paella_DeferredNotImplemented();
	}

	getDimensions() {
		return paella_DeferredNotImplemented();	// { width:X, height:Y }
	}

	goFullScreen() {
		return paella_DeferredNotImplemented();
	}

	freeze(){
		return paella_DeferredNotImplemented();
	}

	unFreeze(){
		return paella_DeferredNotImplemented();
	}

	disable(isMainAudioPlayer) {
		console.log("Disable video requested");
	}

	enable(isMainAudioPlayer) {
		console.log("Enable video requested");
	}

	// Utility functions
	setClassName(className) {
		this.domElement.className = className;
	}

	_callReadyEvent() {
		paella.events.trigger(paella.events.singleVideoReady, { sender:this });
	}

	_callUnloadEvent() {
		paella.events.trigger(paella.events.singleVideoUnloaded, { sender:this });
	}
}

paella.VideoElementBase = VideoElementBase;

class EmptyVideo extends paella.VideoElementBase {
	constructor(id,stream,left,top,width,height) {
		super(id,stream,'div',left,top,width,height);
	}

	// Initialization functions
	setPosterFrame(url) {}
	setAutoplay(auto) {}
	load() {return paella_DeferredRejected(new Error("no such compatible video player")); }
	play() { return paella_DeferredRejected(new Error("no such compatible video player")); }
	pause() { return paella_DeferredRejected(new Error("no such compatible video player")); }
	isPaused() { return paella_DeferredRejected(new Error("no such compatible video player")); }
	duration() { return paella_DeferredRejected(new Error("no such compatible video player")); }
	setCurrentTime(time) { return paella_DeferredRejected(new Error("no such compatible video player")); }
	currentTime() { return paella_DeferredRejected(new Error("no such compatible video player")); }
	setVolume(volume) { return paella_DeferredRejected(new Error("no such compatible video player")); }
	volume() { return paella_DeferredRejected(new Error("no such compatible video player")); }
	setPlaybackRate(rate) { return paella_DeferredRejected(new Error("no such compatible video player")); }
	playbackRate() { return paella_DeferredRejected(new Error("no such compatible video player")); }
	unFreeze() { return paella_DeferredRejected(new Error("no such compatible video player")); }
	freeze() { return paella_DeferredRejected(new Error("no such compatible video player")); }
	unload() { return paella_DeferredRejected(new Error("no such compatible video player")); }
	getDimensions() { return paella_DeferredRejected(new Error("no such compatible video player")); }
}

paella.EmptyVideo = EmptyVideo;

class EmptyVideoFactory extends paella.VideoFactory {
	isStreamCompatible(streamData) {
		return true;
	}

	getVideoObject(id, streamData, rect) {
		return new paella.EmptyVideo(id, streamData, rect.x, rect.y, rect.w, rect.h);
	}
}

paella.videoFactories.EmptyVideoFactory = EmptyVideoFactory;

class Html5Video extends paella.VideoElementBase {
	constructor(id,stream,left,top,width,height,streamName) {
		super(id,stream,'video',left,top,width,height);

		this._currentQuality = null;
		this._autoplay = false;

		this._streamName = streamName || 'mp4';
		this._playbackRate = 1;

		if (this._stream.sources[this._streamName]) {
			this._stream.sources[this._streamName].sort(function (a, b) {
				return a.res.h - b.res.h;
			});
		}

		this.video.preload = "auto";
		this.video.setAttribute("playsinline","");
		//this.video.setAttribute("tabindex","-1");

		this._configureVideoEvents(this.video);
	}

	_configureVideoEvents(videoElement) {
		function onProgress(event) {
			if (!this._ready && this.video.readyState==4) {
				this._ready = true;
				if (this._initialCurrentTipe!==undefined) {
					this.video.currentTime = this._initialCurrentTime;
					delete this._initialCurrentTime;
				}
				this._callReadyEvent();
			}
		}


		let evtCallback = (event) => { onProgress.apply(this,[event]); }

		$(this.video).bind('progress', evtCallback);
		$(this.video).bind('loadstart',evtCallback);
		$(this.video).bind('loadedmetadata',evtCallback);
		$(this.video).bind('canplay',evtCallback);
		$(this.video).bind('oncanplay',evtCallback);

		// Save current time to resume video
		$(this.video).bind('timeupdate', (evt) => {
			this._resumeCurrentTime = this.video.currentTime;
		});

		$(this.video).bind('ended',(evt) => {
			paella.events.trigger(paella.events.endVideo);
		});

		$(this.video).bind('emptied', (evt) => {
			if (this._resumeCurrentTime && !isNaN(this._resumeCurrentTime)) {
				this.video.currentTime = this._resumeCurrentTime;
			}
		});
		
		// Fix safari setQuelity bug
		if (paella.utils.userAgent.browser.Safari) {
			$(this.video).bind('canplay canplaythrough', (evt) => {
				this._resumeCurrentTime && (this.video.currentTime = this._resumeCurrentTime);
			});
		}
	}
	
	get buffered() {
		return this.video && this.video.buffered;
	}

	get video() {
		if (this.domElementType=='video') {
			return this.domElement;
		}
		else {
			this._video = this._video || document.createElement('video');
			return this._video;
		}
	}

	get ready() {
		// Fix Firefox specific issue when video reaches the end
		if (paella.utils.userAgent.browser.Firefox &&
			this.video.currentTime==this.video.duration &&
			this.video.readyState==2)
		{
			this.video.currentTime = 0;
		}

		return this.video.readyState>=3;
	}

	// Synchronous functions: returns null if the resource is not loaded. Use only if 
	// the resource is loaded.
	get currentTimeSync() {
		return this.ready ? this.video.currentTime : null;
	}

	get volumeSync() {
		return this.ready ? this.video.volume : null;
	}

	get pausedSync() {
		return this.ready ? this.video.paused : null;
	}

	get durationSync() {
		return this.ready ? this.video.duration : null;
	}

	_deferredAction(action) {
		return new Promise((resolve,reject) => {
			function processResult(actionResult) {
				if (actionResult instanceof Promise) {
					actionResult.then((p) => resolve(p))
						.catch((err) => reject(err));
				}
				else {
					resolve(actionResult);
				}
			}

			if (this.ready) {
				processResult(action());
			}
			else {
				$(this.video).bind('canplay',() => {
					processResult(action());
					$(this.video).unbind('canplay');
					$(this.video).unbind('loadedmetadata');
				});
			}
		});
	}

	_getQualityObject(index, s) {
		return {
			index: index,
			res: s.res,
			src: s.src,
			toString:function() { return this.res.w==0 ? "auto" : this.res.w + "x" + this.res.h; },
			shortLabel:function() { return this.res.w==0 ? "auto" : this.res.h + "p"; },
			compare:function(q2) { return this.res.w*this.res.h - q2.res.w*q2.res.h; }
		};
	}

	captureFrame() {
		return new Promise((resolve) => {
			resolve({
				source:this.video,
				width:this.video.videoWidth,
				height:this.video.videoHeight
			});
		});
	}

	supportsCaptureFrame() {
		return Promise.resolve(true);
	}

	// Initialization functions
	getVideoData() {
		var This = this;
		return new Promise((resolve,reject) => {
			this._deferredAction(() => {
				resolve({
					duration: This.video.duration,
					currentTime: This.video.currentTime,
					volume: This.video.volume,
					paused: This.video.paused,
					ended: This.video.ended,
					res: {
						w: This.video.videoWidth,
						h: This.video.videoHeight
					}
				});
			});
		});
	}
	
	setPosterFrame(url) {
		this._posterFrame = url;
	}

	setAutoplay(auto) {
		this._autoplay = auto;
		if (auto && this.video) {
			this.video.setAttribute("autoplay",auto);
		}
	}

	videoCanvas() {
		let canvasType = this._stream.canvas || ["video"];
		return paella.getVideoCanvas(canvasType);
	}

	webGlDidLoad() {
		return Promise.resolve();
	}

	load() {
		return new Promise((resolve,reject) => {
			var sources = this._stream.sources[this._streamName];
			if (this._currentQuality===null && this._videoQualityStrategy) {
				this._currentQuality = this._videoQualityStrategy.getQualityIndex(sources);
			}
	
			var stream = this._currentQuality<sources.length ? sources[this._currentQuality]:null;
			this.video.innerText = "";
			this.videoCanvas()
				.then((CanvasClass) => {
					let canvasInstance = new CanvasClass(stream);
					this._zoomAvailable = canvasInstance.allowZoom();

					if (window.$paella_bg && bg.app && canvasInstance instanceof bg.app.WindowController) {
						// WebGL canvas
						this.domElementType = 'canvas';
						if (stream) {

							// WebGL engine load callback
							return new Promise((webglResolve,webglReject) => {
								this.webGlDidLoad()
									.then(() => {
										this.canvasController = null;
										let mainLoop = bg.app.MainLoop.singleton;

										mainLoop.updateMode = bg.app.FrameUpdate.AUTO;
										mainLoop.canvas = this.domElement;
										mainLoop.run(canvasInstance);
										return this.loadVideoStream(canvasInstance,stream);
									})

									.then((canvas) => {
										webglResolve(canvas);
									})
									.catch((err) => webglReject(err));
							});
						}
						else {
							Promise.reject(new Error("Invalid stream data."));
						}
					}
					else {
						return this.loadVideoStream(canvasInstance,stream);
					}
				})
	
				.then((canvas) => {
					if (canvas && paella.WebGLCanvas && canvas instanceof paella.WebGLCanvas) {
						this._video = canvas.video;
						this._video.pause();
						this._configureVideoEvents(this.video);
					}
					resolve(stream);
				})
	
				.catch((err) => {
					reject(err);
				});
		});
	}

	loadVideoStream(canvasInstance,stream) {
		return canvasInstance.loadVideo(this,stream);
	}

	disable(isMainAudioPlayer) {
		//if (isMainAudioPlayer) return;
		//this._isDisabled = true;
		//this._playState = !this.video.paused;
		//this.video.pause();
	}

	enable(isMainAudioPlayer) {
		//if (isMainAudioPlayer) return;
		//this._isDisabled = false;
		//if (this._playState) {
		//	this.video.play();
		//}
	}

	getQualities() {
		return new Promise((resolve,reject) => {
			setTimeout(() => {
				var result = [];
				var sources = this._stream.sources[this._streamName];
				var index = -1;
				sources.forEach((s) => {
					index++;
					result.push(this._getQualityObject(index,s));
				});
				resolve(result);
			},10);
		});
	}

	setQuality(index) {
		return new Promise((resolve) => {
			var paused = this.video.paused;
			var sources = this._stream.sources[this._streamName];
			this._currentQuality = index<sources.length ? index:0;
			var currentTime = this.video.currentTime;
			
			let This = this;
			let onSeek = function() {
				This.unFreeze().then(() => {
					resolve();
					This.video.removeEventListener('seeked',onSeek,false);
				});
			};
	
			this.freeze()
				.then(() => {
					return this.load();
				})

				.then(() => {
					if (!paused) {
						this.play();
					}
					this.video.addEventListener('seeked',onSeek);
					this.video.currentTime = currentTime;
				});
		});
	}

	getCurrentQuality() {
		return new Promise((resolve) => {	
			resolve(this._getQualityObject(this._currentQuality,this._stream.sources[this._streamName][this._currentQuality]));
		});
	}

	play() {
		return this._deferredAction(() => {
			if (!this._isDisabled) {
				return this.video.play();
			}
			else {
				return Promise.resolve();
			}
		});
	}

	pause() {
		return this._deferredAction(() => {
			if (!this._isDisabled) {
				return this.video.pause();
			}
			else {
				return Promise.resolve();
			}
		});
	}

	isPaused() {
		return this._deferredAction(() => {
			return this.video.paused;
		});
	}

	duration() {
		return this._deferredAction(() => {
			return this.video.duration;
		});
	}

	setCurrentTime(time) {
		return this._deferredAction(() => {
			(time == 0 || time) && !isNaN(time) && (this.video.currentTime = time);
		});
	}

	currentTime() {
		return this._deferredAction(() => {
			return this.video.currentTime;
		});
	}

	setVolume(volume) {
		return this._deferredAction(() => {
			this.video.volume = volume;
			if (volume==0) {
				this.video.setAttribute("muted","muted");
				this.video.muted = true;
			}
			else {
				this.video.removeAttribute("muted");
				this.video.muted = false;
			}
		});
	}

	volume() {
		return this._deferredAction(() => {
			return this.video.volume;
		});
	}

	setPlaybackRate(rate) {
		return this._deferredAction(() => {
			this._playbackRate = rate;
			this.video.playbackRate = rate;
		});
	}

	playbackRate() {
		return this._deferredAction(() => {
			return this.video.playbackRate;
		});
	}

	supportAutoplay() {
		let macOS10_12_safari = paella.utils.userAgent.system.MacOS &&
								paella.utils.userAgent.system.Version.minor>=12 &&
								paella.utils.userAgent.browser.Safari;
		let iOS = paella.utils.userAgent.system.iOS;
		// Autoplay does not work from Chrome version 64
		let chrome_v64 =	paella.utils.userAgent.browser.Chrome &&
							paella.utils.userAgent.browser.Version.major==64;
		if (macOS10_12_safari || iOS || chrome_v64)
		{
			return false;
		}
		else {
			return true;
		}
	}

	goFullScreen() {
		return this._deferredAction(() => {
			var elem = this.video;
			if (elem.requestFullscreen) {
				elem.requestFullscreen();
			}
			else if (elem.msRequestFullscreen) {
				elem.msRequestFullscreen();
			}
			else if (elem.mozRequestFullScreen) {
				elem.mozRequestFullScreen();
			}
			else if (elem.webkitEnterFullscreen) {
				elem.webkitEnterFullscreen();
			}
		});
	}

	unFreeze(){
		return this._deferredAction(() => {
			var c = document.getElementById(this.video.id + "canvas");
			if (c) {
				$(c).remove();
			}
		});
	}
	
	freeze(){
		var This = this;
		return this._deferredAction(function() {
			var canvas = document.createElement("canvas");
			canvas.id = This.video.id + "canvas";
			canvas.className = "freezeFrame";
			canvas.width = This.video.videoWidth;
			canvas.height = This.video.videoHeight;
			canvas.style.cssText = This.video.style.cssText;
			canvas.style.zIndex = 2;

			var ctx = canvas.getContext("2d");
			ctx.drawImage(This.video, 0, 0, Math.ceil(canvas.width/16)*16, Math.ceil(canvas.height/16)*16);//Draw image
			This.video.parentElement.appendChild(canvas);
		});
	}

	unload() {
		this._callUnloadEvent();
		return paella_DeferredNotImplemented();
	}

	getDimensions() {
		return paella_DeferredNotImplemented();
	}
}

paella.Html5Video = Html5Video;

paella.Html5Video.IsAutoplaySupported = function(debug = false) {
	return new Promise((resolve) => {
		// Create video element to test autoplay
		var video = document.createElement('video');
		video.src = 'data:video/mp4;base64,AAAAIGZ0eXBtcDQyAAACAGlzb21pc28yYXZjMW1wNDEAAAAIZnJlZQAAC+htZGF0AAACqQYF//+l3EXpvebZSLeWLNgg2SPu73gyNjQgLSBjb3JlIDE1NSByMjkwMSA3ZDBmZjIyIC0gSC4yNjQvTVBFRy00IEFWQyBjb2RlYyAtIENvcHlsZWZ0IDIwMDMtMjAxOCAtIGh0dHA6Ly93d3cudmlkZW9sYW4ub3JnL3gyNjQuaHRtbCAtIG9wdGlvbnM6IGNhYmFjPTEgcmVmPTEgZGVibG9jaz0xOjA6MCBhbmFseXNlPTB4MToweDEgbWU9ZGlhIHN1Ym1lPTEgcHN5PTEgcHN5X3JkPTEuMDA6MC4wMCBtaXhlZF9yZWY9MCBtZV9yYW5nZT0xNiBjaHJvbWFfbWU9MSB0cmVsbGlzPTAgOHg4ZGN0PTAgY3FtPTAgZGVhZHpvbmU9MjEsMTEgZmFzdF9wc2tpcD0xIGNocm9tYV9xcF9vZmZzZXQ9MCB0aHJlYWRzPTEgbG9va2FoZWFkX3RocmVhZHM9MSBzbGljZWRfdGhyZWFkcz0wIG5yPTAgZGVjaW1hdGU9MSBpbnRlcmxhY2VkPTAgYmx1cmF5X2NvbXBhdD0wIGNvbnN0cmFpbmVkX2ludHJhPTAgYmZyYW1lcz0zIGJfcHlyYW1pZD0yIGJfYWRhcHQ9MSBiX2JpYXM9MCBkaXJlY3Q9MSB3ZWlnaHRiPTEgb3Blbl9nb3A9MCB3ZWlnaHRwPTEga2V5aW50PTMyMCBrZXlpbnRfbWluPTMyIHNjZW5lY3V0PTQwIGludHJhX3JlZnJlc2g9MCByYz1jcmYgbWJ0cmVlPTAgY3JmPTQwLjAgcWNvbXA9MC42MCBxcG1pbj0wIHFwbWF4PTY5IHFwc3RlcD00IGlwX3JhdGlvPTEuNDAgcGJfcmF0aW89MS4zMCBhcT0xOjEuMDAAgAAAAJBliIQD/2iscx5avG2BdVkxRtUop8zs5zVIqfxQM03W1oVb8spYPP0yjO506xIxgVQ4iSPGOtcDZBVYGqcTatSa730A8XTpnUDpUSrpyaCe/P8eLds/XenOFYMh8UIGMBwzhVOniajqOPFOmmFC20nufGOpJw81hGhgFwCO6a8acwB0P6LNhZZoRD0y2AZMQfEA0AAHAAAACEGaIRiEP5eAANAABwDSGK8UmBURhUGyINeXiuMlXvJnPVVQKigqVGy8PAuVNiWY94iJ/jL/HeT+/usIfmc/dsQ/TV87CTfXhD8C/4xCP3V+DJP8UP3iJdT8okfAuRJF8zkPgh5/J7XzGT8o9pJ+tvlST+g3uwh1330Q9qd4IbnwOQ9dexCHf8mQfVJ57wET8acsIcn6UT6p7yoP2uQ97fFAhrNARXaou2QkEJxrmP6ZBa7TiE6Uqx04OcnChy+OrfwfRWfSYRbS2wmENdDIKUQSkggeXbLb10CIHL5BPgiBydo+HEEPILBbH9zZOdw/77EbN8euVRS/ZcjbZ/D63aLDh1MTme4vfGzFjXkw9r7U8EhcddAmwXGKjo9o53+/8Rnm1rnt6yh3hLD9/htcZnjjGcW9ZQlj6DKIGrrPo/l6C6NyeVr07mB/N6VlGb5fkLBZM42iUNiIGnMJzShmmlFtEsO0mr5CMcFiJdrZQjdIxsYwpU4xlzmD2+oPtjSLVZiDh2lHDRmHubAxXMROEt0z4GkcCYCk832HaXZSM+4vZbUwJa2ysgmfAQMTEM9gxxct7h5xLdrMnHUnB2bXMO2rEnqnnjWHyFYTrzmZTjJ3N4qP+Pv5VHYzZuAa9jnrg35h5hu/Q87uewVnjbJrtcOOm6b9lltPS6n/mkxgxSyqLJVzr/bYt039aTYyhmveJTdeiG7kLfmn9bqjXfxdfZoz53RDcxw+jP7n7TtT8BsB3jUvxe7n5Gbrm9/5QzQ3cxxl9s6ojDMDg3R7Bx//b5rwuSI84z2fuP8/nPxj/wvHNccSL3n77sCEv+AUwlVzHAFkYCkDkdRIORiUg5GJSDkYlIORgKQsjI9E1d0PUP5zV31MSkvI+AAAAAtBmkMYjP/4v5j6wQDGGK/rogCQL/+rZ+PHZ8R11ITSYLDLmXtUdt5a5V+63JHBE/z0/3cCf4av6uOAGtQmr8mCvCxwSI/c7KILm624qm/Kb4fKK5P1GWvX/S84SiSuyTIfk3zVghdRlzZpLZXgddiJjKTGb43OFQCup1nyCbjWgjmOozS6mXGEDsuoVDkSR7/Q8ErEhAZqgHJ5yCxkICvpE+HztDoOSTYiiBCW6shBKQM/Aw5DdbsGWUc/3XEIhH8HXJSDU8mZDApXSSZR+4fbKiOTUHmUgYd7HOLNG544Zy3F+ZPqxMwuGkCo/HxfLXrebdQakkweTwTqUgIDlwvPC181Z5eZ7cDTV905pDXGj/KiRAk3p+hlgHPvRW35PT4b163gUGkmIl0Ds4OBn6G64lkPsnQPNFs8AtwH4PSeYoz9s5uh/jFX0tlr7f+xzN6PuDvyOmKvYhdYK5FLAOkbJ6E/r7fxRZ1g63PPgeLsfir/0iq9K7IW+eWH4ONNCdL5oyV/TSILB+ob8z1ZWUf9p50jIFh6l64geGZ785/8OQanz95/ZPwGF03PMeYdkuH6x5Q/gcx5bg2RejM+RPQ6Vg6D43WOe+1fDKbVqr9P6Y5S9fuwD56fvq62ESHISopAae8/mbMD2la4/h/K9uSYuhxZDNszxgmQmd2kQDoEU6g46KneCXN/b9b5Ez/4iQOfBj4EuXyfp8MlAlFg8P486y4HT9H680pqN9xN164m7ReXPWHU7pw7F9Pw3FEDjQrHHnM3KfE8KWrl2JyxrdR90wr+HPPrkO5v1XT88+iU5MfGOeswl1uQxhiAGn4O62zaMJmDbSrMNY4LEV/jc+TjMQJRwOsUrW8aDyVoo87t8G+Qtfm6fOy6DNK9crM2f3KQJ0YEPc5JM0eSQsjYSFkZFIWRkUgcB1El5HwAAAAIAZ5iRCX/y4AA7liudRsNCYNGAb/ocSIJGilo13xUupVcYzmaVbkEY3nch7y9pfI1qxo3V9n9Q+r84e3e7dCfx+aLdD6S8dDaqzc6eqH9onVdingfJndPc1yaRhn4JD1jsj85o/le4m9cE2W1F8unegGNvOuknfzBmz4/Us9R+kC7xW5e9Z1Z9JsGeb/z6XkKkxiNh1C3Ns5jTVxB9x1poY49zmq+xsXNh0uy75DZf0JM9Uq8ghKrZiQDyAlHf4dqw48mtmlozgUMkh7VJ7vbIW1UNI81pRTT7C3WOOa3mw0RNjAoMLjtm1+VqQBEhHw+6VBvNTwCBkyvjU+kVMA1OU8elyGQX0xTlHRM8iPGg3CO8B5AzpOm2M7J75cG3PPGc42ztXyYzat3TyZ54CyDqZi1/Mn4B6T1fhMSD0uk5lKsOHIktX1Sfud/I3Ew+McUpwm3bxVdAy7uiGeiXWbe3cMBmCruk4yW18G6dEf9prnjcT6HUZG5bBSQGYSQscX2KCZoWxWkVS0w6IkwqdVJ+Akyey/Hl0MyrcAMI6Sgq3HMn95sBcc4ZadQLT31gNKo6qyebwmyK63HlMTK40Zj3FGuboBQ3Zsg87Jf3Gg1SDlG6fRVl2+5Cc6q+0OcUNRyCfLIG157ZHTSCwD9UpZtZDLki0BCLgAAAAhBmmQYiv+BgQDyne7dSHRhSQ/D31OEhh0h14FMQDwlvgJODIIYGxb7iHQo1mvJn3hOUUli9mTrUMuuPv/W2bsX3X7l9k7jtvT/Cuf4Kmbbhn0zmtjx7GWFyjrJfyHHxs5mxuTjdr2/drXoPhh1rb2XOnE9H3BdBqm1I+K5Sd1hYCevn6PbJcDyUHpysOZeLu+VoYklOlicG52cbxZbzvVeiS4jb+qyJoL62Ox+nSrUhOkCNMf8dz5iEi+C5iYZciyXk6gmIvSJVQDNTiO2i1a6pGORhiNVWGAMBHNHyHbmWtqB9AYbSdGR5qQzHnGF9HWaHfTzIqQMNEioRwE00KEllO+UcuPFmOs0Kl9lgy1DgKSKfGaaVFc7RNrn0nOddM6OfOG51GuoJSCnOpRjIvLAMAAAAA1NfU1+Ro9v/o+AANDABwAABedtb292AAAAbG12aGQAAAAA18kDNdfJAzUAAAPoAAAAowABAAABAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAAAAGGlvZHMAAAAAEICAgAcAT////v7/AAACknRyYWsAAABcdGtoZAAAAAPXyQM118kDNQAAAAEAAAAAAAAAnwAAAAAAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAEAAAAAAOAAAACAAAAAAACRlZHRzAAAAHGVsc3QAAAAAAAAAAQAAAJ8AABZEAAEAAAAAAgptZGlhAAAAIG1kaGQAAAAA18kDNdfJAzUAAV+QAAA3qlXEAAAAAAAtaGRscgAAAAAAAAAAdmlkZQAAAAAAAAAAAAAAAFZpZGVvSGFuZGxlcgAAAAG1bWluZgAAABR2bWhkAAAAAQAAAAAAAAAAAAAAJGRpbmYAAAAcZHJlZgAAAAAAAAABAAAADHVybCAAAAABAAABdXN0YmwAAACYc3RzZAAAAAAAAAABAAAAiGF2YzEAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAOAAgAEgAAABIAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAY//8AAAAyYXZjQwFNQAr/4QAaZ01ACuyiLy+AtQYBBkAAAATAAAEsI8SJZYABAAVo74OcgAAAABhzdHRzAAAAAAAAAAEAAAAFAAALIgAAABRzdHNzAAAAAAAAAAEAAAABAAAAEXNkdHAAAAAAIBAQGBAAAAAwY3R0cwAAAAAAAAAEAAAAAgAAFkQAAAABAAAhZgAAAAEAAAsiAAAAAQAAFkQAAAAcc3RzYwAAAAAAAAABAAAAAQAAAAEAAAABAAAAKHN0c3oAAAAAAAAAAAAAAAUAAANBAAAADAAAAA8AAAAMAAAADAAAACRzdGNvAAAAAAAAAAUAAAAwAAADdQAABhAAAAjPAAAKyQAAAlp0cmFrAAAAXHRraGQAAAAD18kDNdfJAzUAAAACAAAAAAAAAKMAAAAAAAAAAAAAAAEBAAAAAAEAAAAAAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAAAAAAkZWR0cwAAABxlbHN0AAAAAAAAAAEAAABzAAAIQAABAAAAAAG+bWRpYQAAACBtZGhkAAAAANfJAzXXyQM1AACsRAAAHABVxAAAAAAAJWhkbHIAAAAAAAAAAHNvdW4AAAAAAAAAAAAAAABNb25vAAAAAXFtaW5mAAAAEHNtaGQAAAAAAAAAAAAAACRkaW5mAAAAHGRyZWYAAAAAAAAAAQAAAAx1cmwgAAAAAQAAATVzdGJsAAAAZ3N0c2QAAAAAAAAAAQAAAFdtcDRhAAAAAAAAAAEAAAAAAAAAAAACABAAAAAArEQAAAAAADNlc2RzAAAAAAOAgIAiAAIABICAgBRAFQAAAAAAAAAAAAAABYCAgAISCAaAgIABAgAAABhzdHRzAAAAAAAAAAEAAAAHAAAEAAAAABxzdHNjAAAAAAAAAAEAAAABAAAAAQAAAAEAAAAwc3RzegAAAAAAAAAAAAAABwAAAAQAAAAEAAACiwAAArAAAAHuAAABNwAAAAQAAAAsc3RjbwAAAAAAAAAHAAADcQAAA4EAAAOFAAAGHwAACNsAAArVAAAMDAAAABpzZ3BkAQAAAHJvbGwAAAACAAAAAf//AAAAHHNiZ3AAAAAAcm9sbAAAAAEAAAAHAAAAAQAAABR1ZHRhAAAADG5hbWVNb25vAAAAb3VkdGEAAABnbWV0YQAAAAAAAAAhaGRscgAAAAAAAAAAbWRpcmFwcGwAAAAAAAAAAAAAAAA6aWxzdAAAADKpdG9vAAAAKmRhdGEAAAABAAAAAEhhbmRCcmFrZSAxLjEuMiAyMDE4MDkwNTAw';
		video.load();
		//video.style.display = 'none';
		if (debug) {
			video.style = "position: fixed; top: 0px; right: 0px; z-index: 1000000;";
			document.body.appendChild(video);
		}
		else {
			video.style.display = 'none';
		}
		video.playing = false;
		video.play().then((status) => {
				resolve(true);
			})
			.catch((err) => {
				resolve(false)
			})
	})
}

class Html5VideoFactory {
	isStreamCompatible(streamData) {
		try {
			if (paella.videoFactories.Html5VideoFactory.s_instances>0 && 
				paella.utils.userAgent.system.iOS &&
				(paella.utils.userAgent.system.Version.major<=10 && paella.utils.userAgent.system.Version.minor<3))
			{
				return false;
			}
			
			for (var key in streamData.sources) {
				if (key=='mp4' || key=='mp3') return true;
			}
		}
		catch (e) {}
		return false;
	}

	getVideoObject(id, streamData, rect) {
		++paella.videoFactories.Html5VideoFactory.s_instances;
		return new paella.Html5Video(id, streamData, rect.x, rect.y, rect.w, rect.h);
	}
}

paella.videoFactories.Html5VideoFactory = Html5VideoFactory;
paella.videoFactories.Html5VideoFactory.s_instances = 0;


class ImageVideo extends paella.VideoElementBase {
	
	constructor(id,stream,left,top,width,height) {
		super(id,stream,'img',left,top,width,height);

		this._posterFrame = null;
		this._currentQuality = null;
		this._currentTime = 0;
		this._duration =  0;
		this._ended = false;
		this._playTimer = null;
		this._playbackRate = 1;

		this._frameArray = null;
		
		this._stream.sources.image.sort(function(a,b) {
			return a.res.h - b.res.h;
		});
	}

	get img() { return this.domElement; }

	get imgStream() { this._stream.sources.image[this._currentQuality]; }

	get _paused() { return this._playTimer==null; }

	_deferredAction(action) {
		return new Promise((resolve) => {
			if (this.ready) {
				resolve(action());
			}
			else {
				var resolve = () => {
					this._ready = true;
					resolve(action());
				};
				$(this.video).bind('paella:imagevideoready', resolve);
			}
		});
	}

	_getQualityObject(index, s) {
		return {
			index: index,
			res: s.res,
			src: s.src,
			toString:function() { return Number(this.res.w) + "x" + Number(this.res.h); },
			shortLabel:function() { return this.res.h + "p"; },
			compare:function(q2) { return Number(this.res.w)*Number(this.res.h) - Number(q2.res.w)*Number(q2.res.h); }
		};
	}

	_loadCurrentFrame() {
		var This = this;
		if (this._frameArray) {
			var frame = this._frameArray[0];
			this._frameArray.some(function(f) {
				if (This._currentTime<f.time) {
					return true;
				}
				else {
					frame = f.src;
				}
			});
			this.img.src = frame;
		}
	}

	// Initialization functions

	/*allowZoom:function() {
		return false;
	},*/

	getVideoData() {
		return new Promise((resolve) => {
			this._deferredAction(() => {
				let imgStream = this._stream.sources.image[this._currentQuality];
				var videoData = {
					duration: this._duration,
					currentTime: this._currentTime,
					volume: 0,
					paused: this._paused,
					ended: this._ended,
					res: {
						w: imgStream.res.w,
						h: imgStream.res.h
					}
				};
				resolve(videoData);
			});
		});
	}

	setPosterFrame(url) {
		this._posterFrame = url;
	}

	setAutoplay(auto) {
		this._autoplay = auto;
		if (auto && this.video) {
			this.video.setAttribute("autoplay",auto);
		}
	}

	load() {
		var This = this;
		var sources = this._stream.sources.image;
		if (this._currentQuality===null && this._videoQualityStrategy) {
			this._currentQuality = this._videoQualityStrategy.getQualityIndex(sources);
		}

		var stream = this._currentQuality<sources.length ? sources[this._currentQuality]:null;
		if (stream) {
			this._frameArray = [];
			for (var key in stream.frames) {
				var time = Math.floor(Number(key.replace("frame_","")));
				this._frameArray.push({ src:stream.frames[key], time:time });
			}
			this._frameArray.sort(function(a,b) {
				return a.time - b.time;
			});
			this._ready = true;
			this._currentTime = 0;
			this._duration = stream.duration;
			this._loadCurrentFrame();
			paella.events.trigger("paella:imagevideoready");
			return this._deferredAction(function() {
				return stream;
			});
		}
		else {
			return paella_DeferredRejected(new Error("Could not load video: invalid quality stream index"));
		}
	}

	supportAutoplay() {
		return true;
	}

	getQualities() {
		return new Promise((resolve) => {
			setTimeout(() => {
				var result = [];
				var sources = this._stream.sources[this._streamName];
				var index = -1;
				sources.forEach((s) => {
					index++;
					result.push(this._getQualityObject(index,s));
				});
				resolve(result);
			},10);
		});
	}

	setQuality(index) {
		return new Promise((resolve) => {
			let paused = this._paused;
			let sources = this._stream.sources.image;
			this._currentQuality = index<sources.length ? index:0;
			var currentTime = this._currentTime;
			this.load()
				.then(function() {
					this._loadCurrentFrame();
					resolve();
				});
		});
	}

	getCurrentQuality() {
		return new Promise((resolve) => {
			resolve(this._getQualityObject(this._currentQuality,this._stream.sources.image[this._currentQuality]));
		});
	}

	play() {
		let This = this;
		return this._deferredAction(() => {
			This._playTimer = new paella.utils.Timer(function() {
				This._currentTime += 0.25 * This._playbackRate;
				This._loadCurrentFrame();
			}, 250);
			This._playTimer.repeat = true;
		});
	}

	pause() {
		let This = this;
		return this._deferredAction(() => {
			This._playTimer.repeat = false;
			This._playTimer = null;
		});
	}

	isPaused() {
		return this._deferredAction(() => {
			return this._paused;
		});
	}

	duration() {
		return this._deferredAction(() => {
			return this._duration;
		});
	}

	setCurrentTime(time) {
		return this._deferredAction(() => {
			this._currentTime = time;
			this._loadCurrentFrame();
		});
	}

	currentTime() {
		return this._deferredAction(() => {
			return this._currentTime;
		});
	}

	setVolume(volume) {
		return this._deferredAction(function() {
			// No audo sources in image video
		});
	}

	volume() {
		return this._deferredAction(function() {
			// No audo sources in image video
			return 0;
		});
	}

	setPlaybackRate(rate) {
		return this._deferredAction(() => {
			this._playbackRate = rate;
		});
	}

	playbackRate() {
		return this._deferredAction(() => {
			return this._playbackRate;
		});
	}

	goFullScreen() {
		return this._deferredAction(() => {
			var elem = this.img;
			if (elem.requestFullscreen) {
				elem.requestFullscreen();
			}
			else if (elem.msRequestFullscreen) {
				elem.msRequestFullscreen();
			}
			else if (elem.mozRequestFullScreen) {
				elem.mozRequestFullScreen();
			}
			else if (elem.webkitEnterFullscreen) {
				elem.webkitEnterFullscreen();
			}
		});
	}

	unFreeze(){
		return this._deferredAction(function() {});
	}

	freeze(){
		return this._deferredAction(function() {});
	}

	unload() {
		this._callUnloadEvent();
		return paella_DeferredNotImplemented();
	}

	getDimensions() {
		return paella_DeferredNotImplemented();
	}
}

paella.ImageVideo = ImageVideo;


class ImageVideoFactory {
	isStreamCompatible(streamData) {
		try {
			for (var key in streamData.sources) {
				if (key=='image') return true;
			}
		}
		catch (e) {}
		return false;
	}

	getVideoObject(id, streamData, rect) {
		return new paella.ImageVideo(id, streamData, rect.x, rect.y, rect.w, rect.h);
	}
}

paella.videoFactories.ImageVideoFactory = ImageVideoFactory;

})();

/*  
	Paella HTML 5 Multistream Player
	Copyright (C) 2017  Universitat Politècnica de València Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
*/

(() => {

class BackgroundContainer extends paella.DomNode {
	constructor(id,image) {
		super('img',id,{position:'relative',top:'0px',left:'0px',right:'0px',bottom:'0px',zIndex:GlobalParams.background.zIndex});
		this.domElement.setAttribute('src',image);
		this.domElement.setAttribute('alt','');
		this.domElement.setAttribute('width','100%');
		this.domElement.setAttribute('height','100%');
	}

	setImage(image) {
		this.domElement.setAttribute('src',image);
	}
}

paella.BackgroundContainer = BackgroundContainer;

class VideoOverlay extends paella.DomNode {
	get size() {
		if (!this._size) {
			this._size = {w:1280,h:720};
		}
		return this._size;
	}

	constructor() {
		var style = {position:'absolute',left:'0px',right:'0px',top:'0px',bottom:'0px',overflow:'hidden',zIndex:10};
		super('div','overlayContainer',style);
		this.domElement.setAttribute("role", "main");
	}

	_generateId() {
		return Math.ceil(Date.now() * Math.random());
	}

	enableBackgroundMode() {
		this.domElement.className = 'overlayContainer background';
	}

	disableBackgroundMode() {
		this.domElement.className = 'overlayContainer';
	}

	clear() {
		this.domElement.innerText = "";
	}

	getVideoRect(index) {
		return paella.player.videoContainer.getVideoRect(index);
	}

	addText(text,rect,isDebug) {
		var textElem = document.createElement('div');
		textElem.innerText = text;
		textElem.className = "videoOverlayText";
		if (isDebug) textElem.style.backgroundColor = "red";
		return this.addElement(textElem,rect);
	}

	addElement(element,rect) {
		this.domElement.appendChild(element);
		element.style.position = 'absolute';
		element.style.left = this.getHSize(rect.left) + '%';
		element.style.top = this.getVSize(rect.top) + '%';
		element.style.width = this.getHSize(rect.width) + '%';
		element.style.height = this.getVSize(rect.height) + '%';
		return element;
	}

	getLayer(id,zindex) {
		id = id || this._generateId();
		return $(this.domElement).find("#" + id)[0] || this.addLayer(id,zindex);
	}

	addLayer(id,zindex) {
		zindex = zindex || 10;
		var element = document.createElement('div');
		element.className = "row";
		element.id = id || this._generateId();
		return this.addElement(element,{ left:0, top: 0, width:1280, height:720 });
	}

	removeLayer(id) {
		var elem = $(this.domElement).find("#" + id)[0];
		if (elem) {
			this.domElement.removeChild(elem);
		}
	}

	removeElement(element) {
		if (element) {
			try {
				this.domElement.removeChild(element);
			}
			catch (e) {
				
			}
		}
	}

	getVSize(px) {
		return px*100/this.size.h;
	}

	getHSize(px) {
		return px*100/this.size.w;
	}
}

paella.VideoOverlay = VideoOverlay;

class VideoWrapper extends paella.DomNode {
	constructor(id, left, top, width, height) {
		var relativeSize = new paella.RelativeVideoSize();
		var percentTop = relativeSize.percentVSize(top) + '%';
		var percentLeft = relativeSize.percentWSize(left) + '%';
		var percentWidth = relativeSize.percentWSize(width) + '%';
		var percentHeight = relativeSize.percentVSize(height) + '%';
		var style = {
			top: percentTop,
			left: percentLeft,
			width: percentWidth,
			height: percentHeight,
			position: 'absolute',
			zIndex: GlobalParams.video.zIndex,
			overflow: 'hidden'
		};
		super('div',id,style);
		this._rect = { left:left, top:top, width:width, height:height };
		this.domElement.className = "videoWrapper";
	}

	setRect(rect,animate) {
		this._rect = JSON.parse(JSON.stringify(rect));
		var relativeSize = new paella.RelativeVideoSize();
		var percentTop = relativeSize.percentVSize(rect.top) + '%';
		var percentLeft = relativeSize.percentWSize(rect.left) + '%';
		var percentWidth = relativeSize.percentWSize(rect.width) + '%';
		var percentHeight = relativeSize.percentVSize(rect.height) + '%';
		var style = {top:percentTop,left:percentLeft,width:percentWidth,height:percentHeight,position:'absolute'};
		if (animate) {
			this.disableClassName();
			var thisClass = this;

			$(this.domElement).animate(style,400,function(){
				thisClass.enableClassName();
				paella.events.trigger(paella.events.setComposition, { video:thisClass });
			});
			this.enableClassNameAfter(400);
		}
		else {
			$(this.domElement).css(style);
			paella.events.trigger(paella.events.setComposition, { video:this });
		}
	}

	getRect() {
		return this._rect;
	}

	disableClassName() {
		this.classNameBackup = this.domElement.className;
		this.domElement.className = "";
	}

	enableClassName() {
		this.domElement.className = this.classNameBackup;
	}

	enableClassNameAfter(millis) {
		setTimeout("$('#" + this.domElement.id + "')[0].className = '" + this.classNameBackup + "'",millis);
	}

	setVisible(visible,animate) {
		if (typeof(visible)=="string") {
			visible = /true/i.test(visible) ? true : false;
		}
		if (visible && animate) {
			$(this.domElement).show();
			$(this.domElement).animate({opacity:1.0},300);
		}
		else if (visible && !animate) {
			$(this.domElement).show();
		}
		else if (!visible && animate) {
			$(this.domElement).animate({opacity:0.0},300, () => $(this.domElement).hide());
		}
		else if (!visible && !animate) {
			$(this.domElement).hide();
		}
	}

	setLayer(layer) {
		this.domElement.style.zIndex = layer;
	}
}

paella.VideoWrapper = VideoWrapper;

paella.SeekType = {
	FULL: 1,
	BACKWARDS_ONLY: 2,
	FORWARD_ONLY: 3,
	DISABLED: 4
};

// This function is used to manage the timer to enable and disable the click and double click events
// interaction with the video container timeout.
function clearClickEventsTimeout() {
	if (this._clickEventsTimeout) {
		clearTimeout(this._clickEventsTimeout);
		this._clickEventsTimeout = null;
	}
}

class VideoContainerBase extends paella.DomNode {
	
	constructor(id) {
		var style = {position:'absolute',left:'0px',right:'0px',top:'0px',bottom:'0px',overflow:'hidden'};
		super('div',id,style);

		this._trimming = {enabled:false,start:0,end:0};
		this.timeupdateEventTimer = null;
		this.timeupdateInterval = 250;
		this.masterVideoData = null;
		this.slaveVideoData = null;
		this.currentMasterVideoData = null;
		this.currentSlaveVideoData = null;
		this._force = false;
		this._clickEventsEnabled =  true;
		this._seekDisabled =  false;
		this._seekType = paella.SeekType.FULL;
		this._seekTimeLimit = 0;
		this._attenuationEnabled = false;
		
		$(this.domElement).dblclick((evt) => {
			if (this._clickEventsEnabled) {
				paella.player.isFullScreen() ? paella.player.exitFullScreen() : paella.player.goFullScreen();
			}
		});

		let dblClickTimer = null;
		$(this.domElement).click((evt) => {
			let doClick = () => {
				if (!this._clickEventsEnabled) return;
				paella.player.videoContainer.paused()
					.then((paused) => {
						// If some player needs mouse events support, the click is ignored
						if (this.streamProvider.videoPlayers.some((p) => p.canvasData.mouseEventsSupport)) {
							return;
						}
	
						if (paused) {
							paella.player.play();
						}
						else {
							paella.player.pause();
						}
					});
			};

			// the dblClick timer prevents the single click from running when the user double clicks
			if (dblClickTimer) {
				clearTimeout(dblClickTimer);
				dblClickTimer = null;
			}
			else {
				dblClickTimer = setTimeout(() => {
					dblClickTimer = null;
					doClick();
				}, 200);
			}
			
			
		});

		this.domElement.addEventListener("touchstart",(event) => {
			if (paella.player.controls) {
				paella.player.controls.restartHideTimer();
			}
		});
	}

	set attenuationEnabled(att) {
		this._attenuationEnabled = att;

		Array.from(paella.player.videoContainer.container.domElement.children).forEach((ch) => {
			if (ch.id == "overlayContainer") {
				return;
			}
			if (att) {
				$(ch).addClass("dimmed-element");
			}
			else {
				$(ch).removeClass("dimmed-element");
			}
		});
	}

	get attenuationEnabled() {
		return this._attenuationEnabled;
	}

	set seekType(type) {
		switch (type) {
		case paella.SeekType.FULL:
		case paella.SeekType.BACKWARDS_ONLY:
		case paella.SeekType.FORWARD_ONLY:
		case paella.SeekType.DISABLED:
			this._seekType = type;
			paella.events.trigger(paella.events.seekAvailabilityChanged, {
				type: type,
				enabled: type==paella.SeekType.FULL,
				disabled: type!=paella.SeekType.FULL
			});
			break;
		default:
			throw new Error(`Invalid seekType. Allowed seek types:
				paella.SeekType.FULL
				paella.SeekType.BACKWARDS_ONLY
				paella.SeekType.FORWARD_ONLY
				paella.SeekType.DISABLED`);
		}
	}

	get seekType() { return this._seekType; }

	triggerTimeupdate() {
		var paused = 0;
		var duration = 0;
		this.paused()
			.then((p) => {
				paused = p;
				return this.duration();
			})

			.then((d) => {
				duration = d;
				return this.currentTime();
			})

			.then((currentTime) => {
				if (!paused || this._force) {
					this._seekTimeLimit = currentTime>this._seekTimeLimit ? currentTime:this._seekTimeLimit;
					this._force = false;
					paella.events.trigger(paella.events.timeupdate, {
						videoContainer: this,
						currentTime: currentTime,
						duration: duration
					});
				}
			});
	}

	startTimeupdate() {
		this.timeupdateEventTimer = new paella.utils.Timer((timer) => {
			this.triggerTimeupdate();
		}, this.timeupdateInterval);
		this.timeupdateEventTimer.repeat = true;
	}

	stopTimeupdate() {
		if (this.timeupdateEventTimer) {
			this.timeupdateEventTimer.repeat = false;
		}
		this.timeupdateEventTimer = null;
	}

	enablePlayOnClick(timeout = 0) {
		clearClickEventsTimeout.apply(this);
		if (timeout) {
			this._clickEventsTimeout = setTimeout(() => {
				this._clickEventsEnabled = true;
			}, timeout);
		}
		else {
			this._clickEventsEnabled = true;
		}
	}

	disablePlayOnClick() {
		clearClickEventsTimeout.apply(this);
		this._clickEventsEnabled = false;
	}

	isPlayOnClickEnabled() {
		return this._clickEventsEnabled;
	}

	play() {
		this.streamProvider.startVideoSync(this.audioPlayer);
		this.startTimeupdate();
		setTimeout(() => paella.events.trigger(paella.events.play), 50)
	}

	pause() {
		paella.events.trigger(paella.events.pause);
		this.stopTimeupdate();
		this.streamProvider.stopVideoSync();
	}

	seekTo(newPositionPercent) {
		return new Promise((resolve, reject) => {
			let time = 0;
			paella.player.videoContainer.currentTime()
				.then((t) => {
					time = t;
					return paella.player.videoContainer.duration()			
				})

				.then((duration) => {
					if (this._seekTimeLimit>0 && this._seekType==paella.SeekType.BACKWARDS_ONLY) {
						time = this._seekTimeLimit;
					}
					let currentPercent = time / duration * 100;
					switch (this._seekType) {
					case paella.SeekType.FULL:
						break;
					case paella.SeekType.BACKWARDS_ONLY:
						if (newPositionPercent>currentPercent) {
							reject(new Error("Warning: Seek is disabled"));
							return;
						}
						break;
					case paella.SeekType.FORWARD_ONLY:
						if (newPositionPercent<currentPercent) {
							reject(new Error("Warning: Seek is disabled"));
							return;
						}
						break;
					case paella.SeekType.DISABLED:
						reject(new Error("Warning: Seek is disabled"));
						return;
					}

					this.setCurrentPercent(newPositionPercent)
						.then((timeData) => {
							this._force = true;
							this.triggerTimeupdate();
							paella.events.trigger(paella.events.seekToTime,{ newPosition:timeData.time });
							paella.events.trigger(paella.events.seekTo,{ newPositionPercent:newPositionPercent });
							resolve();
						});
				})
		});
	}

	seekToTime(time) {
		return new Promise((resolve, reject) => {
			paella.player.videoContainer.currentTime()
				.then((currentTime) => {
					if (this._seekTimeLimit && this._seekType==paella.SeekType.BACKWARDS_ONLY) {
						currentTime = this._seekTimeLimit;
					}
					switch (this._seekType) {
					case paella.SeekType.FULL:
						break;
					case paella.SeekType.BACKWARDS_ONLY:
						if (time>currentTime) {
							reject(new Error("Warning: Seek is disabled"));
							return;
						}
						break;
					case paella.SeekType.FORWARD_ONLY:
						if (time<currentTime) {
							reject(new Error("Warning: Seek is disabled"));
							return;
						}
						break;
					case paella.SeekType.DISABLED:
						reject(new Error("Warning: Seek is disabled"));
						return;
					}

					this.setCurrentTime(time)
						.then((timeData) => {
							this._force = true;
							this.triggerTimeupdate();
							let percent = timeData.time * 100 / timeData.duration;
							paella.events.trigger(paella.events.seekToTime,{ newPosition:timeData.time });
							paella.events.trigger(paella.events.seekTo,{ newPositionPercent:percent });
						});
				});
		});
	}

	setPlaybackRate(params) {
		paella.events.trigger(paella.events.setPlaybackRate, { rate: params });
	}

	mute() {

	}

	unmute() {

	}

	setVolume(params) {
	}

	volume() {
		return 1;
	}

	trimStart() {
		return new Promise((resolve) => {
			resolve(this._trimming.start);
		});
	}

	trimEnd() {
		return new Promise((resolve) => {
			resolve(this._trimming.end);
		});
	}

	trimEnabled() {
		return new Promise((resolve) => {
			resolve(this._trimming.enabled);
		});
	}

	trimming() {
		return new Promise((resolve) => {
			resolve(this._trimming);
		});
	}

	enableTrimming() {
		this._trimming.enabled = true;
		let cap=paella.captions.getActiveCaptions()
		if(cap!==undefined) paella.plugins.captionsPlugin.buildBodyContent(cap._captions,"list");
		paella.events.trigger(paella.events.setTrim,{trimEnabled:this._trimming.enabled,trimStart:this._trimming.start,trimEnd:this._trimming.end});
	}

	disableTrimming() {
		this._trimming.enabled = false;
		let cap=paella.captions.getActiveCaptions()
		if(cap!==undefined) paella.plugins.captionsPlugin.buildBodyContent(cap._captions,"list");
		paella.events.trigger(paella.events.setTrim,{trimEnabled:this._trimming.enabled,trimStart:this._trimming.start,trimEnd:this._trimming.end});
	}

	setTrimming(start,end) {
		return new Promise((resolve) => {
			let currentTime = 0;

			this.currentTime(true)
				.then((c) => {
					currentTime = c;
					return this.duration();
				})

				.then((duration) => {
					this._trimming.start = Math.floor(start);
					this._trimming.end = Math.floor(end);
					if(this._trimming.enabled){
						if (currentTime<this._trimming.start) {
							this.setCurrentTime(0);
						}
						if (currentTime>this._trimming.end) {
							this.setCurrentTime(duration);
						}

						let cap=paella.captions.getActiveCaptions();
						if(cap!==undefined) paella.plugins.captionsPlugin.buildBodyContent(cap._captions,"list");
					}
					paella.events.trigger(paella.events.setTrim,{trimEnabled:this._trimming.enabled,trimStart:this._trimming.start,trimEnd:this._trimming.end});
					resolve();
				});
		});
	}

	setTrimmingStart(start) {
 		return this.setTrimming(start,this._trimming.end);
	}

	setTrimmingEnd(end) {
		return this.setTrimming(this._trimming.start,end);
	}

	setCurrentPercent(percent) {
		var duration = 0;
		return new Promise((resolve) => {
			this.duration()
				.then((d) => {
					duration = d;
					return this.trimming();
				})
				.then((trimming) => {
					var position = 0;
					if (trimming.enabled) {
						var start = trimming.start;
						var end = trimming.end;
						duration = end - start;
						var trimedPosition = percent * duration / 100;
						position = parseFloat(trimedPosition);
					}
					else {
						position = percent * duration / 100;
					}
					return this.setCurrentTime(position);
				})
				.then(function(timeData) {
					resolve(timeData);
				});
		});
	}

	setCurrentTime(time) {
		paella.log.debug("VideoContainerBase.setCurrentTime(" +  time + ")");
	}

	currentTime() {
		paella.log.debug("VideoContainerBase.currentTime()");
		return 0;
	}

	duration() {
		paella.log.debug("VideoContainerBase.duration()");
		return 0;
	}

	paused() {
		paella.log.debug("VideoContainerBase.paused()");
		return true;
	}

	setupVideo(onSuccess) {
		paella.log.debug("VideoContainerBase.setupVide()");
	}

	isReady() {
		paella.log.debug("VideoContainerBase.isReady()");
		return true;
	}

	onresize() { super.onresize(onresize);
	}

	ended() {
		return new Promise((resolve) => {
			let duration = 0;
			this.duration()
				.then((d) => {
					duration = d;
					return this.currentTime();
				})
				.then((currentTime) => {
					resolve(Math.floor(duration) <= Math.ceil(currentTime));
				});
		});
	}
}

paella.VideoContainerBase = VideoContainerBase;

// Profile frame strategies

class ProfileFrameStrategy {
	static Factory() {
		var config = paella.player.config;

		try {
			var strategyClass = config.player.profileFrameStrategy;
			var ClassObject = paella.utils.objectFromString(strategyClass);
			var strategy = new ClassObject();
			if (strategy instanceof paella.ProfileFrameStrategy) {
				return strategy;
			}
		}
		catch (e) {
		}
		
		return null;
	}

	valid() { return true; }

	adaptFrame(videoDimensions,frameRect) {
		return frameRect;
	}
}

paella.ProfileFrameStrategy = ProfileFrameStrategy;

class LimitedSizeProfileFrameStrategy extends ProfileFrameStrategy {
	adaptFrame(videoDimensions,frameRect) {
		if (videoDimensions.width<frameRect.width|| videoDimensions.height<frameRect.height) {
			var frameRectCopy = JSON.parse(JSON.stringify(frameRect));
			frameRectCopy.width = videoDimensions.width;
			frameRectCopy.height = videoDimensions.height;
			var diff = { w:frameRect.width - videoDimensions.width,
						h:frameRect.height - videoDimensions.height };
			frameRectCopy.top = frameRectCopy.top + diff.h/2;
			frameRectCopy.left = frameRectCopy.left + diff.w/2;
			return frameRectCopy;
		}
		return frameRect;
	}
}

paella.LimitedSizeProfileFrameStrategy = LimitedSizeProfileFrameStrategy;

function updateBuffers() {
	// Initial implementation: use the mainStream buffered property
	let mainBuffered = this.mainPlayer && this.mainPlayer.buffered;
	if (mainBuffered) {
		this._bufferedData = [];

		for (let i = 0; i<mainBuffered.length; ++i) {
			this._bufferedData.push({
				start: mainBuffered.start(i),
				end: mainBuffered.end(i)
			});
		}
	}
}

class StreamProvider {
	constructor(videoData) {
		this._mainStream = null;
		this._videoStreams = [];
		this._audioStreams = [];

		this._mainPlayer = null;
		this._audioPlayer = null;
		this._videoPlayers = [];
		this._audioPlayers = [];
		this._players = [];

		this._autoplay = paella.utils.parameters.get('autoplay')=='true' || this.isLiveStreaming;
		this._startTime = 0;

		this._bufferedData = [];
		let streamProvider = this;
		this._buffered = {
			start: function(index) {
				if (index<0 || index>=streamProvider._bufferedData.length) {
					throw new Error("Buffered index out of bounds.");
				}		
				return streamProvider._bufferedData[index].start;
			},

			end: function(index) {
				if (index<0 || index>=streamProvider._bufferedData.length) {
					throw new Error("Buffered index out of bounds.");
				}
				return streamProvider._bufferedData[index].end;
			}
		}

		Object.defineProperty(this._buffered, "length", {
			get: function() {
				return streamProvider._bufferedData.length;
			}
		});
	}

	get buffered() {
		updateBuffers.apply(this);
		return this._buffered;
	}

	init(videoData) {
		if (videoData.length==0) throw Error("Empty video data.");
		this._videoData = videoData;

		if (!this._videoData.some((stream) => { return stream.role=="master"; })) {
			this._videoData[0].role = "master";
		}

		this._videoData.forEach((stream, index) => {
			stream.type = stream.type || 'video';
			if (stream.role=='master') {
				this._mainStream = stream;
			}

			if (stream.type=='video') {
				this._videoStreams.push(stream);
			}
			else if (stream.type=='audio') {
				this._audioStreams.push(stream);
			}
		});

		if (this._videoStreams.length==0) {
			throw new Error("No video streams found. Paella Player requires at least one video stream.");
		}

		// Create video players
		let autoplay = this.autoplay;
		this._videoStreams.forEach((videoStream,index) => {
			let rect = {x:0,y:0,w:1280,h:720};
			let player = paella.videoFactory.getVideoObject(`video_${ index }`, videoStream, rect);
			player.setVideoQualityStrategy(this._qualityStrategy);
			player.setAutoplay(autoplay);

			if (videoStream==this._mainStream) {
				this._mainPlayer = player;
				this._audioPlayer = player;
			}
			else {
				player.setVolume(0);
			}

			this._videoPlayers.push(player);
			this._players.push(player);
		});

		// Create audio player
		this._audioStreams.forEach((audioStream,index) => {
			let player = paella.audioFactory.getAudioObject(`audio_${ index }`,audioStream);
			player.setAutoplay(autoplay);
			if (player) {
				this._audioPlayers.push(player);
				this._players.push(player);
			}
		});
	}

	startVideoSync(syncProviderPlayer) {
		this._syncProviderPlayer = syncProviderPlayer;
		this._audioPlayer = syncProviderPlayer; // The player that provides the synchronization is also used as main audio player.
		this.stopVideoSync();
		
		console.debug("Start sync to player:");
		console.debug(this._syncProviderPlayer);
		let maxDiff = 0.1;
		let totalTime = 0;
		let numberOfSyncs = 0;
		let syncFrequency = 0;
		let maxSyncFrequency = 0.2;
		let sync = () => {
			this._syncProviderPlayer.currentTime()
				.then((t) => {
					this.players.forEach((player) => {
						if (player!=syncProviderPlayer &&
							player.currentTimeSync!=null &&
							Math.abs(player.currentTimeSync-t)>maxDiff)
						{
							console.debug(`Sync player current time: ${ player.currentTimeSync } to time ${ t }`);
							console.debug(player);
							++numberOfSyncs;	
							player.setCurrentTime(t);

							
							if (syncFrequency>maxSyncFrequency) {
								maxDiff *= 1.5;
								console.log(`Maximum syncrhonization frequency reached. Increasing max difference syncronization time to ${maxDiff}`);
							}
						}
					});
					
				});

			totalTime += 1000;
			syncFrequency = numberOfSyncs / (totalTime / 1000);
			this._syncTimer = setTimeout(() => sync(), 1000);
		};
	
		this._syncTimer = setTimeout(() => sync(), 1000);
	}

	stopVideoSync() {
		if (this._syncTimer) {
			console.debug("Stop video sync");
			clearTimeout(this._syncTimer);
			this._syncTimer = null;
		}
	}

	loadVideos() {
		let promises = [];

		this._players.forEach((player) => {
			promises.push(player.load());
		});
		
		return Promise.all(promises);
	}

	get startTime() {
		return this._startTime;
	}

	set startTime(s) {
		this._startTime = s;
	}

	get isMonostream() {
		return this._videoStreams.length==1;
	}

	get mainStream() {
		return this._mainStream;
	}

	get videoStreams() {
		//return this._videoData;
		return this._videoStreams;
	}

	
	get audioStreams() {
		return this._audioStreams;
	}
	
	get streams() {
		return this._videoStreams.concat(this._audioStreams);
	}

	get videoPlayers() {
		return this._videoPlayers;
	}

	get audioPlayers() {
		return this._audioPlayers;
	}

	get players() {
		return this._videoPlayers.concat(this._audioPlayers);
	}

	callPlayerFunction(fnName) {
		let promises = [];
		let functionArguments = [];
		for (let i=1; i<arguments.length; ++i) {
			functionArguments.push(arguments[i]);
		}

		this.players.forEach((player) => {
			promises.push(player[fnName](...functionArguments));
		});

		return new Promise((resolve,reject) => {
			Promise.all(promises)
				.then(() => {
					if (fnName=='play' && !this._firstPlay) {
						this._firstPlay = true;
						if (this._startTime) {
							this.players.forEach((p) => p.setCurrentTime(this._startTime));
						}
					}
					resolve();
				})
				.catch((err) => {
					reject(err);
				});
		});
	}

	get mainVideoPlayer() {
		return this._mainPlayer;
	}

	get mainAudioPlayer() {
		return this._audioPlayer;
	}

	get mainPlayer() {
		return this.mainVideoPlayer || this.mainAudioPlayer;
	}

	get isLiveStreaming() {
		return paella.player.isLiveStream();
	}

	set qualityStrategy(strategy) {
		this._qualityStrategy = strategy;
		this._videoPlayers.forEach((player) => {
			player.setVideoQualityStrategy(strategy);
		})
	}

	get qualityStrategy() { return this._qualityStrategy || null; }

	get autoplay() {
		return this.supportAutoplay && this._autoplay;
	}

	set autoplay(ap) {
		if (!this.supportAutoplay || this.isLiveStreaming) return;
		this._autoplay = ap;
		if (this.videoPlayers) {
			this.videoPlayers.forEach((player) => player.setAutoplay(ap));
			this.audioPlayers.forEach((player) => player.setAutoplay(ap));
		}
	}

	get supportAutoplay() {
		return this.videoPlayers.every((player) => player.supportAutoplay());
	}
}

paella.StreamProvider = StreamProvider;

function addVideoWrapper(id,videoPlayer) {
	let wrapper = new paella.VideoWrapper(id);
	wrapper.addNode(videoPlayer);
	this.videoWrappers.push(wrapper);
	this.container.addNode(wrapper);
	return wrapper;
}

class VideoContainer extends paella.VideoContainerBase {

	get streamProvider() { return this._streamProvider; }
	get ready() { return this._ready; }
	get isMonostream() { return this._streamProvider.isMonostream; }
	get trimmingHandler() { return this._trimmingHandler; }
	get videoWrappers() { return this._videoWrappers; }
	get container() { return this._container; }
	get profileFrameStrategy() { return this._profileFrameStrategy; }
	get sourceData() { return this._sourceData; }

	constructor(id) {
		super(id);

		this._streamProvider = new paella.StreamProvider();
		this._ready = false;
		this._videoWrappers = [];

		this._container = new paella.DomNode('div','playerContainer_videoContainer_container',{position:'relative',display:'block',marginLeft:'auto',marginRight:'auto',width:'1024px',height:'567px'});
		this._container.domElement.setAttribute('role','main');
		this.addNode(this._container);

		this.overlayContainer = new paella.VideoOverlay(this.domElement);
		this.container.addNode(this.overlayContainer);

		this.setProfileFrameStrategy(paella.ProfileFrameStrategy.Factory());
		this.setVideoQualityStrategy(paella.VideoQualityStrategy.Factory());

		this._audioTag = paella.player.config.player.defaultAudioTag ||
						 paella.utils.dictionary.currentLanguage();
		this._audioPlayer = null;

		// Initial volume level
		this._volume = paella.utils.cookies.get("volume") ? Number(paella.utils.cookies.get("volume")) : 1;
		if (paella.player.startMuted)
		{
			this._volume = 0;
		}
		this._muted = false;
	}

	// Playback and status functions
	play() {
		return new Promise((resolve,reject) => {
			this.ended()
				.then((ended) => {
					if (ended) {
						this._streamProvider.startTime = 0;
						this.seekToTime(0);
					}
					else {
						this.streamProvider.startTime = this._startTime;
					}
					return this.streamProvider.callPlayerFunction('play')
				})
				.then(() => {
					super.play();
					resolve();
				})
				.catch((err) => {
					reject(err);
				});
		});
	}

	pause() {
		return new Promise((resolve,reject) => {
			this.streamProvider.callPlayerFunction('pause')
				.then(() => {
					super.pause();
					resolve();
				})
				.catch((err) => {
					reject(err);
				})
		});
	}

	setCurrentTime(time) {
		return new Promise((resolve,reject) => {
			this.trimming()
				.then((trimmingData) => {
					if (trimmingData.enabled) {
						time += trimmingData.start;
						if (time<trimmingData.start) {
							time = trimmingData.start;
						}
						if (time>trimmingData.end) {
							time = trimmingData.end;
						}
					}
					return this.streamProvider.callPlayerFunction('setCurrentTime',time);
				})
			
				.then(() => {
					return this.duration(false);
				})

				.then((duration) => {
					resolve({ time:time, duration:duration });
				})

				.catch((err) => {
					reject(err);
				})
		})
	}

	currentTime(ignoreTrimming = false) {
		return new Promise((resolve) => {
			let trimmingData = null;
			let p = ignoreTrimming ? Promise.resolve({ enabled:false }) : this.trimming();

			p.then((t) => {
				trimmingData = t;
				return this.masterVideo().currentTime();
			})

			.then((time) => {
				if (trimmingData.enabled) {
					time = time - trimmingData.start;
				}
				resolve(time)
			});
		});
	}

	setPlaybackRate(rate) {
		this.streamProvider.callPlayerFunction('setPlaybackRate',rate);
		super.setPlaybackRate(rate);
	}

	mute() {
		return new Promise((resolve) => {
			this._muted = true;
			this._audioPlayer.setVolume(0)
				.then(() => {
					paella.events.trigger(paella.events.setVolume, { master: 0 });
					resolve();
				});
		});
	}

	unmute() {
		return new Promise((resolve) => {
			this._muted = false;
			this._audioPlayer.setVolume(this._volume)
				.then(() => {
					paella.events.trigger(paella.events.setVolume, { master: this._volume });
					resolve();
				});
		});
	}

	get muted() {
		return this._muted;
	}

	setVolume(params) {
		if (typeof(params)=='object') {
			console.warn("videoContainer.setVolume(): set parameter as object is deprecated");
			return Promise.resolve();
		}
		else if (params==0) {
			return this.mute();
		}
		else {
			return new Promise((resolve,reject) => {
				paella.utils.cookies.set("volume",params);
				this._volume = params;
				this._audioPlayer.setVolume(params)
					.then(() => {
						paella.events.trigger(paella.events.setVolume, { master:params });
						resolve(params);
					})
					.catch((err) => {
						reject(err);
					});
			});
		}
	}

	volume() {
		return this._audioPlayer.volume();
	}

	duration(ignoreTrimming = false) {
		return new Promise((resolve) => {
			let trimmingData = null;
			let p = ignoreTrimming ? Promise.resolve({ enabled:false }) : this.trimming();

			p.then((t) => {
				trimmingData = t;
				return this.masterVideo().duration();
			})
			
			.then((duration) => {
				if (trimmingData.enabled) {
					duration = trimmingData.end - trimmingData.start;
				}
				resolve(duration);
			});
		})
	}

	paused() {
		return this.masterVideo().isPaused();
	}

	// Video quality functions
	getQualities() {
		return this.masterVideo().getQualities();
	}

	setQuality(index) {
		let qualities = [];
		let promises = [];
		this.streamProvider.videoPlayers.forEach((player) => {
			let playerData = {
				player:player,
				promise:player.getQualities()
			};
			qualities.push(playerData);
			promises.push(playerData.promise);
		});

		return new Promise((resolve) => {
			let resultPromises = [];
			Promise.all(promises)
				.then(() => {
					qualities.forEach((data) => {
						data.promise.then((videoQualities) => {						
							let videoQuality = videoQualities.length>index ? index:videoQualities.length - 1;
							resultPromises.push(data.player.setQuality(videoQuality));
						});
					});

					return Promise.all(resultPromises);
				})

				.then(() => {
					//setTimeout(() => {
						paella.events.trigger(paella.events.qualityChanged);
						resolve();
					//},10);
				});
		});
	}
	getCurrentQuality() {
		return this.masterVideo().getCurrentQuality();
	}

	// Current audio functions
	get audioTag() {
		return this._audioTag;
	}

	get audioPlayer() {
		return this._audioPlayer;
	}

	getAudioTags() {
		return new Promise((resolve) => {
			let lang = [];
			let p = this.streamProvider.players;
			p.forEach((player) => {
				if (player.stream.audioTag) {
					lang.push(player.stream.audioTag);
				}
			})
			resolve(lang);
		})
	}

	setAudioTag(lang) {
		this.streamProvider.stopVideoSync();
		return new Promise((resolve) => {
			let audioSet = false;
			let firstAudioPlayer = null;
			let promises = [];
			this.streamProvider.players.forEach((player) => {
				if (!firstAudioPlayer) {
					firstAudioPlayer = player;
				}

				if (!audioSet && player.stream.audioTag==lang) {
					audioSet = true;
					this._audioPlayer = player;
				}
				promises.push(player.setVolume(0));
			});

			// NOTE: The audio only streams must define a valid audio tag
			if (!audioSet && this.streamProvider.mainVideoPlayer) {
				this._audioPlayer = this.streamProvider.mainVideoPlayer;
			}
			else if (!audioSet && firstAudioPlayer) {
				this._audioPlayer = firstAudioPlayer;
			}

			Promise.all(promises).then(() => {
				return this._audioPlayer.setVolume(this._volume);
			})

			.then(() => {
				this._audioTag = this._audioPlayer.stream.audioTag;
				paella.events.trigger(paella.events.audioTagChanged);
				this.streamProvider.startVideoSync(this.audioPlayer);
				resolve();
			});
		})
	}

	setProfileFrameStrategy(strategy) {
		this._profileFrameStrategy = strategy;
	}

	setVideoQualityStrategy(strategy) {
		this.streamProvider.qualityStrategy = strategy;
	}

	autoplay() { return this.streamProvider.autoplay; }
	supportAutoplay() { return this.streamProvider.supportAutoplay; }
	setAutoplay(ap=true) {
		this.streamProvider.autoplay = ap;
		return this.streamProvider.supportAutoplay;
	}

	masterVideo() {
		return this.streamProvider.mainVideoPlayer || this.audioPlayer;
	}

	getVideoRect(videoIndex) {
		if (this.videoWrappers.length>videoIndex) {
			return this.videoWrappers[videoIndex].getRect();
		}
		else {
			throw new Error(`Video wrapper with index ${ videoIndex } not found`);
		}
	}

	setStreamData(videoData) {
		var urlParamTime = paella.utils.parameters.get("time");
		var hashParamTime = paella.utils.hashParams.get("time");
		var timeString = hashParamTime ? hashParamTime:urlParamTime ? urlParamTime:"0s";
		var startTime = paella.utils.timeParse.timeToSeconds(timeString);
		if (startTime) {
			this._startTime = startTime;
		}
		
		videoData.forEach((stream) => {
			for (var type in stream.sources) {
				let source = stream.sources[type];
				source.forEach((item) => {
					if (item.res) {
						item.res.w = Number(item.res.w);
						item.res.h = Number(item.res.h);
					}
				});
			}
		});
		this._sourceData = videoData;
		return new Promise((resolve,reject) => {
			this.streamProvider.init(videoData);

			let streamDataAudioTag = null;
			videoData.forEach((video) => {
				if (video.audioTag && streamDataAudioTag==null) {
					streamDataAudioTag = video.audioTag;
				}

				if (video.audioTag==this._audioTag) {
					streamDataAudioTag = this._audioTag;
				}
			});

			if (streamDataAudioTag!=this._audioTag && streamDataAudioTag!=null) {
				this._audioTag = streamDataAudioTag;
			}

			this.streamProvider.videoPlayers.forEach((player,index) => {
				addVideoWrapper.apply(this,['videoPlayerWrapper_' + index,player]);
				player.setAutoplay(this.autoplay());
			});

			this.streamProvider.loadVideos()
				.catch((err) => {
					reject(err)
				})

				.then(() => {
					return this.setAudioTag(this.audioTag);
				})

				.then(() => {
					let endedTimer = null;
					let setupEndEventTimer = () => {
						this.stopTimeupdate();
						if (endedTimer) {
							clearTimeout(endedTimer);
							endedTimer = null;
						}
						endedTimer = setTimeout(() => {
							paella.events.trigger(paella.events.ended);
						}, 1000);
					}

					let eventBindingObject = this.masterVideo().video || this.masterVideo().audio;
					$(eventBindingObject).bind('timeupdate', (evt) => {
						this.trimming().then((trimmingData) => {
							let current = evt.currentTarget.currentTime;
							let duration = evt.currentTarget.duration;
							if (trimmingData.enabled) {
								current -= trimmingData.start;
								duration = trimmingData.end - trimmingData.start;
							}
							if (current>=duration) {
								this.streamProvider.callPlayerFunction('pause');
								setupEndEventTimer();
							}
						})
					});
					
					paella.events.bind(paella.events.endVideo,(event) => {
						setupEndEventTimer();
					});

					this._ready = true;
					paella.events.trigger(paella.events.videoReady);
					let profileToUse = paella.utils.parameters.get('profile') ||
										paella.utils.cookies.get('profile') ||
										paella.profiles.getDefaultProfile();

					if (paella.profiles.setProfile(profileToUse, false)) {
						resolve();
					}
					else if (!paella.profiles.setProfile(paella.profiles.getDefaultProfile(), false)) {
						resolve();
					}
				});
		});
	}

	resizePortrait() {
		var width = (paella.player.isFullScreen() == true) ? $(window).width() : $(this.domElement).width();
		var relativeSize = new paella.RelativeVideoSize();
		var height = relativeSize.proportionalHeight(width);
		this.container.domElement.style.width = width + 'px';
		this.container.domElement.style.height = height + 'px';

		var containerHeight = (paella.player.isFullScreen() == true) ? $(window).height() : $(this.domElement).height();
		var newTop = containerHeight / 2 - height / 2;
		this.container.domElement.style.top = newTop + "px";
	}

	resizeLandscape() {
		var height = (paella.player.isFullScreen() == true) ? $(window).height() : $(this.domElement).height();
		var relativeSize = new paella.RelativeVideoSize();
		var width = relativeSize.proportionalWidth(height);
		this.container.domElement.style.width = width + 'px';
		this.container.domElement.style.height = height + 'px';
		this.container.domElement.style.top = '0px';
	}

	onresize() {
		super.onresize();
		var relativeSize = new paella.RelativeVideoSize();
		var aspectRatio = relativeSize.aspectRatio();
		var width = (paella.player.isFullScreen() == true) ? $(window).width() : $(this.domElement).width();
		var height = (paella.player.isFullScreen() == true) ? $(window).height() : $(this.domElement).height();
		var containerAspectRatio = width/height;

		if (containerAspectRatio>aspectRatio) {
			this.resizeLandscape();
		}
		else {
			this.resizePortrait();
		}
		//paella.profiles.setProfile(paella.player.selectedProfile,false)
	}

	// the duration and the current time are returned taking into account the trimming, for example:
	//	trimming: { enabled: true, start: 10, end: 110 } 
	//	currentTime: 0,	> the actual time is 10
	//	duration: 100 > the actual duration is (at least) 110
	getVideoData() {
		return new Promise((resolve,reject) => {
			let videoData = {
				currentTime: 0,
				volume: 0,
				muted: this.muted,
				duration: 0,
				paused: false,
				audioTag: this.audioTag,
				trimming: {
					enabled: false,
					start: 0,
					end: 0
				}
			}
			this.currentTime()
				.then((currentTime) => {
					videoData.currentTime = currentTime;
					return this.volume();
				})
				.then((v) => {
					videoData.volume = v;
					return this.duration();
				})
				.then((d) => {
					videoData.duration = d;
					return this.paused();
				})
				.then((p) => {
					videoData.paused = p;
					return this.trimming();
				})
				.then((trimming) => {
					videoData.trimming = trimming;
					resolve(videoData);
				})
				.catch((err) => reject(err));
		});
	}
}

paella.VideoContainer = VideoContainer;

})();

/*  
	Paella HTML 5 Multistream Player
	Copyright (C) 2017  Universitat Politècnica de València Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
*/


(function() {

class PluginManager {
	
	setupPlugin(plugin) {
		plugin.setup();
		this.enabledPlugins.push(plugin);
		if (eval("plugin instanceof paella.UIPlugin")) {
			plugin.checkVisibility();
		}	
	}

	checkPluginsVisibility() {	
		this.enabledPlugins.forEach(function(plugin) {		
			if (eval("plugin instanceof paella.UIPlugin")) {
				plugin.checkVisibility();
			}								
		});	
	}

	constructor() {
		this.targets = null;
		this.pluginList =  [];
		this.eventDrivenPlugins =  [];
		this.enabledPlugins =  [];
		this.doResize =  true;
		
		this.targets = {};
		paella.events.bind(paella.events.loadPlugins,(event) => {
			this.loadPlugins("paella.DeferredLoadPlugin");
		});
		
		var timer = new paella.utils.Timer(() => {
			if (paella.player && paella.player.controls && this.doResize) paella.player.controls.onresize();
		}, 1000);
		timer.repeat = true;
	}

	setTarget(pluginType,target) {
		if (target.addPlugin) {
			this.targets[pluginType] = target;
		}
	}

	getTarget(pluginType) {
		// PluginManager can handle event-driven events:
		if (pluginType=="eventDriven") {
			return this;
		}
		else {
			var target = this.targets[pluginType];
			return target;
		}
	}

	registerPlugin(plugin) {
		// Registra los plugins en una lista y los ordena
		this.importLibraries(plugin);
		this.pluginList.push(plugin);
		this.pluginList.sort(function(a,b) {
			return a.getIndex() - b.getIndex();
		});
	}

	importLibraries(plugin) {
		plugin.getDependencies().forEach(function(lib) {
			var script = document.createElement('script');
			script.type = "text/javascript";
			script.src = 'javascript/' + lib + '.js';
			document.head.appendChild(script);
		});
	}
	
	// callback => function(plugin,pluginConfig)
	loadPlugins(pluginBaseClass) {
		if (pluginBaseClass != undefined) {
			var This = this;
			this.foreach(function(plugin,config) {
				// Prevent load a plugin twice
				if (plugin.isLoaded()) return;
				if (eval("plugin instanceof " + pluginBaseClass)) {
					if (config.enabled) {
						paella.log.debug("Load plugin (" + pluginBaseClass + "): " + plugin.getName());
						plugin.config = config;							
						plugin.load(This);
					}				
				}
			});
		}
	}
	
	foreach(callback) {
		var enablePluginsByDefault = false;
		var pluginsConfig = {};
		try {
			enablePluginsByDefault = paella.player.config.plugins.enablePluginsByDefault;
		}
		catch(e){}
		try {
			pluginsConfig = paella.player.config.plugins.list;
		}
		catch(e){}
				
		this.pluginList.forEach(function(plugin){			
			var name = plugin.getName();
			var config = pluginsConfig[name];
			if (!config) {
				config = {enabled: enablePluginsByDefault};
			}
			callback(plugin, config);
		});
	}

	addPlugin(plugin) {
		// Prevent add a plugin twice
		if (plugin.__added__) return;
		plugin.__added__ = true;
		plugin.checkEnabled((isEnabled) => {
			if (plugin.type=="eventDriven" && isEnabled) {
				paella.pluginManager.setupPlugin(plugin);
				this.eventDrivenPlugins.push(plugin);
				var events = plugin.getEvents();
				var eventBind = function(event,params) {
					plugin.onEvent(event.type,params);
				};

				for (var i=0; i<events.length;++i) {
					var eventName = events[i];
					paella.events.bind(eventName, eventBind);
				}
			}
		});
	}

	getPlugin(name) {
		for (var i=0;i<this.pluginList.length;++i) {
			if (this.pluginList[i].getName()==name) return this.pluginList[i];
		}
		return null;
	}

	registerPlugins() {
		g_pluginCallbackList.forEach((pluginCallback) => {
			let PluginClass = pluginCallback();
			let pluginInstance = new PluginClass();
			if (pluginInstance.getInstanceName()) {
				paella.plugins = paella.plugins || {};
				paella.plugins[pluginInstance.getInstanceName()] = pluginInstance;
			}
			paella.pluginManager.registerPlugin(pluginInstance);
		});
	}
}

paella.PluginManager = PluginManager;

paella.pluginManager = new paella.PluginManager();

let g_pluginCallbackList = [];
paella.addPlugin = function(cb) {
	g_pluginCallbackList.push(cb);
};

	
class Plugin {
	get type() { return ""; }

	isLoaded() { return this.__loaded__; }

	getDependencies() {
		return [];
	}

	load(pluginManager) {
		if (this.__loaded__) return;
		this.__loaded__ = true;
		var target = pluginManager.getTarget(this.type);
		if (target && target.addPlugin) {
			target.addPlugin(this);
		}
	}

	getInstanceName() { return null; }

	getRootNode(id) {
		return null;
	}

	checkEnabled(onSuccess) {
		onSuccess(true);
	}

	setup() {

	}

	getIndex() {
		return 0;
	}

	getName() {
		return "";
	}
}

paella.Plugin = Plugin;
	
class FastLoadPlugin extends paella.Plugin {}
class EarlyLoadPlugin extends paella.Plugin {}
class DeferredLoadPlugin extends paella.Plugin {}

paella.FastLoadPlugin = FastLoadPlugin;
paella.EarlyLoadPlugin = EarlyLoadPlugin;
paella.DeferredLoadPlugin = DeferredLoadPlugin;

function addMenuItemTabindex(plugin) {
	if (plugin.button.tabIndex>0) {
		paella.tabIndex.insertAfter(plugin.button,plugin.menuContent.children);
	}
}

function removeMenuItemTabindexplugin(plugin) {
	if (plugin.button.tabIndex>0) {
		paella.tabIndex.removeTabIndex(plugin.menuContent.children);
	}
}

function hideContainer(identifier,container,swapFocus) {
	paella.events.trigger(paella.events.hidePopUp,{container:container});
	container.plugin.willHideContent();
	if (container.plugin.getButtonType() == paella.ButtonPlugin.type.menuButton) {
		removeMenuItemTabindexplugin(container.plugin);
	}
	$(container.element).hide();
	$(this.domElement).css({width:'0px'});
	container.button.className = container.button.className.replace(' selected','');
	this.currentContainerId = -1;
	container.plugin.didHideContent();

	if (container.plugin.getButtonType() == paella.ButtonPlugin.type.menuButton && swapFocus) {
		$(container.button).focus();
	}
}

function showContainer(identifier,container,button,swapFocus) {
	paella.events.trigger(paella.events.showPopUp,{container:container});
	container.plugin.willShowContent();
	container.button.className = container.button.className + ' selected';
	$(container.element).show();
	if (container.plugin.getButtonType() == paella.ButtonPlugin.type.menuButton) {
	 	addMenuItemTabindex(container.plugin);
	}
	let width = $(container.element).width();
	if (container.plugin.getAlignment() == 'right') {
		var right = $(button.parentElement).width() - $(button).position().left - $(button).width();
		$(this.domElement).css({width:width + 'px', right:right + 'px', left:''});				
	}
	else {
		var left = $(button).position().left;
		$(this.domElement).css({width:width + 'px', left:left + 'px', right:''});						
	}			
	this.currentContainerId = identifier;

	if (container.plugin.getButtonType() == paella.ButtonPlugin.type.menuButton &&
		container.plugin.menuContent.children.length>0 &&
		swapFocus)
	{
		$(container.plugin.menuContent.children[0]).focus();
	}
	container.plugin.didShowContent();			
}

class PopUpContainer extends paella.DomNode {

	constructor(id,className) {
		var style = {};
		super('div',id,style);

		this.containers = null;
		this.currentContainerId = -1;

		this.domElement.className = className;

		this.containers = {};
	}

	hideContainer(identifier, button, swapFocus = false) {
		var container = this.containers[identifier];
		if (container) {
			hideContainer.apply(this,[identifier,container,swapFocus]);
		}
	}

	showContainer(identifier, button, swapFocus = false) {
		var container = this.containers[identifier];
		if (container && this.currentContainerId!=identifier && this.currentContainerId!=-1) {
			var prevContainer = this.containers[this.currentContainerId];
			hideContainer.apply(this,[this.currentContainerId,prevContainer,swapFocus]);
			showContainer.apply(this,[identifier,container,button,swapFocus]);
		}
		else if (container && this.currentContainerId==identifier) {
			hideContainer.apply(this,[identifier,container,swapFocus]);
		}
		else if (container) {
			showContainer.apply(this,[identifier,container,button,swapFocus]);
		}
	}

	registerContainer(identifier,domElement,button,plugin) {
		var containerInfo = {
			identifier:identifier,
			button:button,
			element:domElement,
			plugin:plugin
		};
		this.containers[identifier] = containerInfo;
		if (plugin.closeOnMouseOut && plugin.closeOnMouseOut()) {
			let popUpId = identifier;
			let btn = button;
			$(domElement).mouseleave(function(evt) {
				paella.player.controls.playbackControl().hidePopUp(popUpId,btn);
			});
		}
		

		// this.domElement.appendChild(domElement);
		$(domElement).hide();
		button.popUpIdentifier = identifier;
		button.sourcePlugin = plugin;
		$(button).click(function(event) {
			if (!this.plugin.isPopUpOpen()) {
				paella.player.controls.playbackControl().showPopUp(this.popUpIdentifier,this,false);
			}
			else {
				paella.player.controls.playbackControl().hidePopUp(this.popUpIdentifier,this,false);
			}
		});
		$(button).keypress(function(event) {
			if ( (event.keyCode == 13) && (!this.plugin.isPopUpOpen()) ){
				if (this.plugin.isPopUpOpen()) {
					paella.player.controls.playbackControl().hidePopUp(this.popUpIdentifier,this,true);
				}
				else {
					paella.player.controls.playbackControl().showPopUp(this.popUpIdentifier,this,true);
				}
			}
			else if ( (event.keyCode == 27)){
				paella.player.controls.playbackControl().hidePopUp(this.popUpIdentifier,this,true);
			}
			event.preventDefault();
		});
		$(button).keyup(function(event) {
			event.preventDefault();
		});

		plugin.containerManager = this;
	}
}

paella.PopUpContainer = PopUpContainer;

class TimelineContainer extends paella.PopUpContainer {
	hideContainer(identifier,button) {
		var container = this.containers[identifier];
		if (container && this.currentContainerId==identifier) {
			paella.events.trigger(paella.events.hidePopUp,{container:container});
			container.plugin.willHideContent();
			$(container.element).hide();
			container.button.className = container.button.className.replace(' selected','');
			this.currentContainerId = -1;
			$(this.domElement).css({height:'0px'});
			container.plugin.didHideContent();
		}
	}

	showContainer(identifier,button) {
		var height =0;
		var container = this.containers[identifier];
		if (container && this.currentContainerId!=identifier && this.currentContainerId!=-1) {
			var prevContainer = this.containers[this.currentContainerId];
			prevContainer.button.className = prevContainer.button.className.replace(' selected','');
			container.button.className = container.button.className + ' selected';
			paella.events.trigger(paella.events.hidePopUp,{container:prevContainer});
			prevContainer.plugin.willHideContent();
			$(prevContainer.element).hide();
			prevContainer.plugin.didHideContent();
			paella.events.trigger(paella.events.showPopUp,{container:container});
			container.plugin.willShowContent();
			$(container.element).show();
			this.currentContainerId = identifier;
			height = $(container.element).height();
			$(this.domElement).css({height:height + 'px'});
			container.plugin.didShowContent();
		}
		else if (container && this.currentContainerId==identifier) {
			paella.events.trigger(paella.events.hidePopUp,{container:container});
			container.plugin.willHideContent();
			$(container.element).hide();
			container.button.className = container.button.className.replace(' selected','');
			$(this.domElement).css({height:'0px'});
			this.currentContainerId = -1;
			container.plugin.didHideContent();
		}
		else if (container) {
			paella.events.trigger(paella.events.showPopUp,{container:container});
			container.plugin.willShowContent();
			container.button.className = container.button.className + ' selected';
			$(container.element).show();
			this.currentContainerId = identifier;
			height = $(container.element).height();
			$(this.domElement).css({height:height + 'px'});
			container.plugin.didShowContent();
		}
	}
}

paella.TimelineContainer = TimelineContainer;
			
class UIPlugin extends paella.DeferredLoadPlugin {
	get ui() { return this._ui; }
	set ui(val) { this._ui = val; }
	
	checkVisibility() {
		var modes = this.config.visibleOn || [	paella.PaellaPlayer.mode.standard, 
												paella.PaellaPlayer.mode.fullscreen, 
												paella.PaellaPlayer.mode.embed ];
		
		var visible = false;
		modes.forEach(function(m){
			if (m == paella.player.getPlayerMode()) {
				visible = true;
			}
		});
		
		if (visible){
			this.showUI();
		}
		else {
			this.hideUI();
		}
	}
	
	hideUI() {
		this.ui.setAttribute('aria-hidden', 'true');
		$(this.ui).hide();
	}
	
	showUI() {
		var thisClass = this;
		paella.pluginManager.enabledPlugins.forEach(function(p) {
			if (p == thisClass) {
				thisClass.ui.setAttribute('aria-hidden', 'false');
				$(thisClass.ui).show();				
			}
		});	
	}
}

paella.UIPlugin = UIPlugin;
	
class ButtonPlugin extends paella.UIPlugin {
	get type() { return 'button'; }

	constructor() {
		super();
		this.subclass = '';
		this.container = null;
		this.containerManager = null;
		this._domElement = null;
	} 

	getAlignment() {
		return 'left';	// or right
	}

	// Returns the button subclass.
	getSubclass() {
		return "myButtonPlugin";
	}

	getIconClass() {
		return "";
	}

	addSubclass($subclass) {
		$(this.container).addClass($subclass);
	}
	
	removeSubclass($subclass) {
		$(this.container).removeClass($subclass);
	}

	action(button) {
		// Implement this if you want to do something when the user push the plugin button
	}

	getName() {
		return "ButtonPlugin";
	}

	getMinWindowSize() {
		return this.config.minWindowSize || 0;
	}

	buildContent(domElement) {
		// Override if your plugin
	}

	getMenuContent() {
		return [];
	}

	willShowContent() {
		paella.log.debug(this.getName() + " willDisplayContent");
	}

	didShowContent() {
		paella.log.debug(this.getName() + " didDisplayContent");
	}

	willHideContent() {
		paella.log.debug(this.getName() + " willHideContent");
	}

	didHideContent() {
		paella.log.debug(this.getName() + " didHideContent");
	}

	getButtonType() {
		//return paella.ButtonPlugin.type.popUpButton;
		//return paella.ButtonPlugin.type.timeLineButton;
		//return paella.ButtonPlugin.type.menuButton;
		return paella.ButtonPlugin.type.actionButton;
		
	}

	getText() {
		return "";
	}

	getAriaLabel() {
		return "";
	}
	
	setText(text) {
		this.container.innerHTML = '<span class="button-text">' + paella.AntiXSS.htmlEscape(text) + '</span>';
		if (this._i) {
			this.container.appendChild(this._i);
		}
	}

	hideButton() {
		this.hideUI();
	}

	showButton() {
		this.showUI();
	}

	// Utility functions: do not override
	changeSubclass(newSubclass) {
		this.subclass = newSubclass;
		this.container.className = this.getClassName();
	}

	changeIconClass(newClass) {
		this._i.className = 'button-icon ' + newClass;
	}

	getClassName() {
		return paella.ButtonPlugin.kClassName + ' ' + this.getAlignment() + ' ' + this.subclass;
	}

	getContainerClassName() {
		if (this.getButtonType()==paella.ButtonPlugin.type.timeLineButton) {
			return paella.ButtonPlugin.kTimeLineClassName + ' ' + this.getSubclass();
		}
		else if (this.getButtonType()==paella.ButtonPlugin.type.popUpButton) {
			return paella.ButtonPlugin.kPopUpClassName + ' ' + this.getSubclass();
		}
		else if (this.getButtonType()==paella.ButtonPlugin.type.menuButton) {
			return paella.ButtonPlugin.kPopUpClassName + ' menuContainer ' + this.getSubclass();
		}
	}

	setToolTip(message) {
		this.button.setAttribute("title", message);
		this.button.setAttribute("aria-label", message);
	}

	getDefaultToolTip() {
		return "";
	}

	isPopUpOpen() {
		return (this.button.popUpIdentifier == this.containerManager.currentContainerId);
	}

	getExpandableContent() {
		return null;
	}

	expand() {
		if (this._expand) {
			$(this._expand).show();
		}
	}

	contract() {
		if (this._expand) {
			$(this._expand).hide();
		}
	}

	static BuildPluginButton(plugin,id) {
		plugin.subclass = plugin.getSubclass();
		var elem = document.createElement('div');
		let ariaLabel = plugin.getAriaLabel() || paella.utils.dictionary.translate(plugin.config.ariaLabel) || "";
		if (ariaLabel!="") {
			elem = document.createElement('button');
		}
		elem.className = plugin.getClassName();
		elem.id = id;

		let buttonText = document.createElement('span');
		buttonText.className = "button-text";
		buttonText.innerHTML = paella.AntiXSS.htmlEscape(plugin.getText());
		buttonText.plugin = plugin;
		elem.appendChild(buttonText);
		if (ariaLabel) {
			let tabIndex = paella.tabIndex.next;
			elem.setAttribute("tabindex", tabIndex);
			elem.setAttribute("aria-label",ariaLabel);
		}	
		elem.setAttribute("alt", "");

		elem.plugin = plugin;
		plugin.button = elem;
		plugin.container = elem;
		plugin.ui = elem;
		plugin.setToolTip(plugin.getDefaultToolTip());

		let icon = document.createElement('i');
		icon.className = 'button-icon ' + plugin.getIconClass();
		icon.plugin = plugin;
		elem.appendChild(icon);
		plugin._i = icon;
			
		function onAction(self) {
			paella.userTracking.log("paella:button:action", self.plugin.getName());
			self.plugin.action(self);
		}
		
		$(elem).click(function(event) {
			onAction(this);
		});
		$(elem).keypress(function(event) {
			 onAction(this);
			 event.preventDefault();
		});

		$(elem).focus(function(event) {
			plugin.expand();
		});

		return elem;
	}

	static BuildPluginExpand(plugin,id) {
		let expandContent = plugin.getExpandableContent();
		if (expandContent) {
			let expand = document.createElement('span');
			expand.plugin = plugin;
			expand.className = 'expandable-content ' + plugin.getClassName();
			plugin._expand = expand;
			expand.appendChild(expandContent);
			$(plugin._expand).hide();
			return expand;
		}
		return null;
	}

	static BuildPluginPopUp(parent,plugin,id) {
		plugin.subclass = plugin.getSubclass();
		var elem = document.createElement('div');
		parent.appendChild(elem);
		elem.className = plugin.getContainerClassName();
		elem.id = id;
		elem.plugin = plugin;
		plugin.buildContent(elem);
		return elem;
	}

	static BuildPluginMenu(parent,plugin,id) {
		plugin.subclass = plugin.getSubclass();
		var elem = document.createElement('div');
		parent.appendChild(elem);
		elem.className = plugin.getContainerClassName();
		elem.id = id;
		elem.plugin = plugin;
		plugin.menuContent = elem;
		plugin.rebuildMenu(elem);

		return elem;
	}

	set menuContent(domElem) {
		this._domElement = domElem;
	}

	get menuContent() {
		return this._domElement;
	}

	rebuildMenu() {
		function getButtonItem(itemData,plugin) {
			var elem = document.createElement('div');
			elem.className = itemData.className +  " menuItem";
			if(itemData.default) {
				elem.className += " selected";
			}

			elem.id = itemData.id;
			elem.innerText = itemData.title;
			if (itemData.icon) {
				elem.style.backgroundImage = `url(${ itemData.icon })`;
				$(elem).addClass('icon');
			}
			elem.data = {
				itemData: itemData,
				plugin: plugin
			};

			function menuItemSelect(button,data,event) {
				data.plugin.menuItemSelected(data.itemData);
				let buttons = button.parentElement ? button.parentElement.children : [];
				for (let i=0; i<buttons.length; ++i) {
					$(buttons[i]).removeClass('selected');
				}
				$(button).addClass('selected');
			}

			$(elem).click(function(event) {
				menuItemSelect(this,this.data,event);
			});
			$(elem).keypress(function(event) {
				if (event.keyCode == 13) {
					menuItemSelect(this,this.data,event);
				}
				event.preventDefault();
			});
			$(elem).keyup(function(event) {
				if (event.keyCode == 27) {
					paella.player.controls.hidePopUp(this.data.plugin.getName(),null,true);
				}
				event.preventDefault();
			});
			return elem;
		}

		let menuContent = this.getMenuContent();
		this.menuContent.innerHTML = "";
		menuContent.forEach((menuItem) => {
			this.menuContent.appendChild(getButtonItem(menuItem,this));
		});
	}
}

paella.ButtonPlugin = ButtonPlugin;
	
paella.ButtonPlugin.alignment = {
	left:'left',
	right:'right'
};
paella.ButtonPlugin.kClassName = 'buttonPlugin';
paella.ButtonPlugin.kPopUpClassName = 'buttonPluginPopUp';
paella.ButtonPlugin.kTimeLineClassName = 'buttonTimeLine';
paella.ButtonPlugin.type = {
	actionButton:1,
	popUpButton:2,
	timeLineButton:3,
	menuButton:4
};
	
class VideoOverlayButtonPlugin extends paella.ButtonPlugin {
	get type() { return 'videoOverlayButton'; }

	// Returns the button subclass.
	getSubclass() {
		return "myVideoOverlayButtonPlugin" + " " + this.getAlignment();
	}

	action(button) {
		// Implement this if you want to do something when the user push the plugin button
	}

	getName() {
		return "VideoOverlayButtonPlugin";
	}

	get tabIndex() {
		return -1;
	}
}

paella.VideoOverlayButtonPlugin = VideoOverlayButtonPlugin;
	
	
class EventDrivenPlugin extends paella.EarlyLoadPlugin {
	get type() { return 'eventDriven'; }

	constructor() {
		super();
		var events = this.getEvents();
		for (var i = 0; i<events.length;++i) {
			var event = events[i];
			if (event==paella.events.loadStarted) {
				this.onEvent(paella.events.loadStarted);
			}
		}
	}

	getEvents() {
		return [];
	}

	onEvent(eventType,params) {
	}

	getName() {
		return "EventDrivenPlugin";
	}
}

paella.EventDrivenPlugin = EventDrivenPlugin;
	
})();

(function() {

    class VideoCanvas {
        constructor(stream) {
            this._stream = stream;
        }

        loadVideo(videoPlugin,stream) {
            return Promise.reject(new Error("Not implemented"));
        }

        allowZoom() {
            return true;
        }
    }

    paella.VideoCanvas = VideoCanvas;

    function initWebGLCanvas() {
        if (!paella.WebGLCanvas) {
        
            class WebGLCanvas extends bg.app.WindowController {
                constructor(stream) {
                    super();
                    this._stream = stream;
                }

                get stream() { return this._stream; }

                get video() { return this.texture ? this.texture.video : null; }

                get camera() { return this._camera; }

                get texture() { return this._texture; }

                loaded() {
                    return new Promise((resolve) => {
                        let checkLoaded = () => {
                            if (this.video) {
                                resolve(this);
                            }
                            else {
                                setTimeout(checkLoaded,100);
                            }
                        }
                        checkLoaded();
                    });
                }

                loadVideo(videoPlugin,stream) {
                    return Promise.reject(new Error("Not implemented"));
                }

                allowZoom() {
                    return false;
                }

                // WebGL engine functions
                registerPlugins() {
                    bg.base.Loader.RegisterPlugin(new bg.base.TextureLoaderPlugin());
                    bg.base.Loader.RegisterPlugin(new bg.base.VideoTextureLoaderPlugin());
                    bg.base.Loader.RegisterPlugin(new bg.base.VWGLBLoaderPlugin());
                }

                loadVideoTexture() {
                    return bg.base.Loader.Load(this.gl, this.stream.src);
                }

                buildVideoSurface(sceneRoot,videoTexture) {
                    let sphere = bg.scene.PrimitiveFactory.Sphere(this.gl,1,50);
                    let sphereNode = new bg.scene.Node(this.gl);
                    sphereNode.addComponent(sphere);
                    sphere.getMaterial(0).texture = videoTexture;
                    sphere.getMaterial(0).lightEmission = 0;
                    sphere.getMaterial(0).lightEmissionMaskInvert = false;
                    sphere.getMaterial(0).cullFace = false;
                    sphereNode.addComponent(new bg.scene.Transform(bg.Matrix4.Scale(1,-1,1)));
                    sceneRoot.addChild(sphereNode);
                }

                buildCamera() {
                    let cameraNode = new bg.scene.Node(this.gl,"Camera");
                    let camera = new bg.scene.Camera();
                    cameraNode.addComponent(camera);
                    cameraNode.addComponent(new bg.scene.Transform());
                    let projection = new bg.scene.OpticalProjectionStrategy();
                    projection.far = 100;
                    projection.focalLength = 55;
                    camera.projectionStrategy = projection;
                    
                    let oc = new bg.manipulation.OrbitCameraController();
                    oc.maxPitch = 90;
                    oc.minPitch = -90;
                    oc.maxDistance = 0;
                    oc.minDistance = 0;
                    this._cameraController = oc;
                    cameraNode.addComponent(oc);

                    return cameraNode;
                }

                buildScene() {
                    this._root = new bg.scene.Node(this.gl, "Root node");

                    this.registerPlugins();

                    this.loadVideoTexture()
                        .then((texture) => {
                            this._texture = texture;
                            this.buildVideoSurface(this._root,texture);
                        });

                    let lightNode = new bg.scene.Node(this.gl,"Light");
                    let light = new bg.base.Light();
                    light.ambient = bg.Color.White();
                    light.diffuse = bg.Color.Black();
                    light.specular = bg.Color.Black();
                    lightNode.addComponent(new bg.scene.Light(light));
                    this._root.addChild(lightNode);

                    let cameraNode = this.buildCamera();
                    this._camera = cameraNode.component("bg.scene.Camera");
                    this._root.addChild(cameraNode);
                }

                init() {
                    bg.Engine.Set(new bg.webgl1.Engine(this.gl));

                    this.buildScene();

                    this._renderer = bg.render.Renderer.Create(this.gl,bg.render.RenderPath.FORWARD);

                    this._inputVisitor = new bg.scene.InputVisitor();
                }

                frame(delta) {
                    if (this.texture) {
                        this.texture.update();
                    }
                    this._renderer.frame(this._root,delta);
                    this.postReshape();
                }

                display() {
                    this._renderer.display(this._root, this._camera);
                }

                reshape(width,height) {
                    this._camera.viewport = new bg.Viewport(0,0,width,height);
                    if (!this._camera.projectionStrategy) {
                        this._camera.projection.perspective(60,this._camera.viewport.aspectRatio,0.1,100);
                    }
                }

                mouseDrag(evt) {
                    this._inputVisitor.mouseDrag(this._root,evt);
                    this.postRedisplay();
                }
                
                mouseWheel(evt) {
                    this._inputVisitor.mouseWheel(this._root,evt);
                    this.postRedisplay();
                }
                
                touchMove(evt) {
                    this._inputVisitor.touchMove(this._root,evt);
                    this.postRedisplay();
                }
                
                mouseDown(evt) { this._inputVisitor.mouseDown(this._root,evt); }
                touchStar(evt) { this._inputVisitor.touchStar(this._root,evt); }
                mouseUp(evt) { this._inputVisitor.mouseUp(this._root,evt); }
                mouseMove(evt) { this._inputVisitor.mouseMove(this._root,evt); }
                mouseOut(evt) { this._inputVisitor.mouseOut(this._root,evt); }
                touchEnd(evt) { this._inputVisitor.touchEnd(this._root,evt); }
            }

            paella.WebGLCanvas = WebGLCanvas;
        }
    }

    function buildVideoCanvas(stream) {
        if (!paella.WebGLCanvas) {
            class WebGLCanvas extends bg.app.WindowController {
                constructor(stream) {
                    super();
                    this._stream = stream;
                }

                get stream() { return this._stream; }

                get video() { return this.texture ? this.texture.video : null; }

                get camera() { return this._camera; }

                get texture() { return this._texture; }

                allowZoom() {
                    return false;
                }

                loaded() {
                    return new Promise((resolve) => {
                        let checkLoaded = () => {
                            if (this.video) {
                                resolve(this);
                            }
                            else {
                                setTimeout(checkLoaded,100);
                            }
                        }
                        checkLoaded();
                    });
                }

                registerPlugins() {
                    bg.base.Loader.RegisterPlugin(new bg.base.TextureLoaderPlugin());
                    bg.base.Loader.RegisterPlugin(new bg.base.VideoTextureLoaderPlugin());
                    bg.base.Loader.RegisterPlugin(new bg.base.VWGLBLoaderPlugin());
                }

                loadVideoTexture() {
                    return bg.base.Loader.Load(this.gl, this.stream.src);
                }

                buildVideoSurface(sceneRoot,videoTexture) {
                    let sphere = bg.scene.PrimitiveFactory.Sphere(this.gl,1,50);
                    let sphereNode = new bg.scene.Node(this.gl);
                    sphereNode.addComponent(sphere);
                    sphere.getMaterial(0).texture = videoTexture;
                    sphere.getMaterial(0).lightEmission = 0;
                    sphere.getMaterial(0).lightEmissionMaskInvert = false;
                    sphere.getMaterial(0).cullFace = false;
                    sphereNode.addComponent(new bg.scene.Transform(bg.Matrix4.Scale(1,-1,1)));
                    sceneRoot.addChild(sphereNode);
                }

                buildCamera() {
                    let cameraNode = new bg.scene.Node(this.gl,"Camera");
                    let camera = new bg.scene.Camera();
                    cameraNode.addComponent(camera);
                    cameraNode.addComponent(new bg.scene.Transform());
                    let projection = new bg.scene.OpticalProjectionStrategy();
                    projection.far = 100;
                    projection.focalLength = 55;
                    camera.projectionStrategy = projection;
                    
                    let oc = new bg.manipulation.OrbitCameraController();
                    oc.maxPitch = 90;
                    oc.minPitch = -90;
                    oc.maxDistance = 0;
                    oc.minDistance = 0;
                    this._cameraController = oc;
                    cameraNode.addComponent(oc);

                    return cameraNode;
                }

                buildScene() {
                    this._root = new bg.scene.Node(this.gl, "Root node");

                    this.registerPlugins();

                    this.loadVideoTexture()
                        .then((texture) => {
                            this._texture = texture;
                            this.buildVideoSurface(this._root,texture);
                        });

                    let lightNode = new bg.scene.Node(this.gl,"Light");
                    let light = new bg.base.Light();
                    light.ambient = bg.Color.White();
                    light.diffuse = bg.Color.Black();
                    light.specular = bg.Color.Black();
                    lightNode.addComponent(new bg.scene.Light(light));
                    this._root.addChild(lightNode);

                    let cameraNode = this.buildCamera();
                    this._camera = cameraNode.component("bg.scene.Camera");
                    this._root.addChild(cameraNode);
                }

                init() {
                    bg.Engine.Set(new bg.webgl1.Engine(this.gl));

                    this.buildScene();

                    this._renderer = bg.render.Renderer.Create(this.gl,bg.render.RenderPath.FORWARD);

                    this._inputVisitor = new bg.scene.InputVisitor();
                }

                frame(delta) {
                    if (this.texture) {
                        this.texture.update();
                    }
                    this._renderer.frame(this._root,delta);
                    this.postReshape();
                }

                display() {
                    this._renderer.display(this._root, this._camera);
                }

                reshape(width,height) {
                    this._camera.viewport = new bg.Viewport(0,0,width,height);
                    if (!this._camera.projectionStrategy) {
                        this._camera.projection.perspective(60,this._camera.viewport.aspectRatio,0.1,100);
                    }
                }

                mouseDrag(evt) {
                    this._inputVisitor.mouseDrag(this._root,evt);
                    this.postRedisplay();
                }
                
                mouseWheel(evt) {
                    this._inputVisitor.mouseWheel(this._root,evt);
                    this.postRedisplay();
                }
                
                touchMove(evt) {
                    this._inputVisitor.touchMove(this._root,evt);
                    this.postRedisplay();
                }
                
                mouseDown(evt) { this._inputVisitor.mouseDown(this._root,evt); }
                touchStar(evt) { this._inputVisitor.touchStar(this._root,evt); }
                mouseUp(evt) { this._inputVisitor.mouseUp(this._root,evt); }
                mouseMove(evt) { this._inputVisitor.mouseMove(this._root,evt); }
                mouseOut(evt) { this._inputVisitor.mouseOut(this._root,evt); }
                touchEnd(evt) { this._inputVisitor.touchEnd(this._root,evt); }
            }

            paella.WebGLCanvas = WebGLCanvas;
        }

        return paella.WebGLCanvas;
    }

    let g_canvasCallbacks = {};

    paella.addCanvasPlugin = function(canvasType, webglSupport, mouseEventsSupport, canvasPluginCallback) {
        g_canvasCallbacks[canvasType] = {
            callback: canvasPluginCallback,
            webglSupport: webglSupport,
            mouseEventsSupport: mouseEventsSupport
        };
    }

    function loadWebGLDeps() {
        return new Promise((resolve) => {
            if (!window.$paella_bg) {
                paella.require(`${ paella.baseUrl }javascript/bg2e-es2015.js`)
                    .then(() => {
                        window.$paella_bg = bg;
                        buildVideoCanvas();
                       // loadWebGLDeps();
                        resolve(window.$paella_bg);
                    })
            }
            else {
                resolve(window.$paella_bg);
            }
        });
    }

    function loadCanvasPlugin(canvasType) {
        return new Promise((resolve,reject) => {
            let callbackData = g_canvasCallbacks[canvasType];
            if (callbackData) {
                (callbackData.webglSupport ? loadWebGLDeps() : Promise.resolve())
                    .then(() => {
                        resolve(callbackData.callback());
                    })

                    .catch((err) => {
                        reject(err);
                    });
            }
            else {
                reject(new Error(`No such canvas type: "${canvasType}"`));
            }
        });
    }

    paella.getVideoCanvas = function(type) {
        return new Promise((resolve,reject) => {
            let canvasData = g_canvasCallbacks[type];
            if (!canvasData) {
                reject(new Error("No such canvas type: " + type));
            }
            else {
                if (canvasData.webglSupport) {
                    loadWebGLDeps()
                        .then(() => {
                            resolve(canvasData.callback());
                        });
                }
                else {
                    resolve(canvasData.callback());
                }
            }
        })
    }

    paella.getVideoCanvasData = function(type) {
        return g_canvasCallbacks[type];
    }

    // Standard <video> canvas
    paella.addCanvasPlugin("video", false, false, () => {
        return class VideoCanvas extends paella.VideoCanvas {
            constructor(stream) {
                super(stream);
            }

            loadVideo(videoPlugin,stream,doLoadCallback = null) {
                return new Promise((resolve,reject) => {
                    doLoadCallback = doLoadCallback || function(video) {
                        return new Promise((cbResolve,cbReject) => {
                            var sourceElem = video.querySelector('source');
                            if (!sourceElem) {
                                sourceElem = document.createElement('source');
                                video.appendChild(sourceElem);
                            }
                            if (video._posterFrame) {
                                video.setAttribute("poster",video._posterFrame);
                            }
                
                            sourceElem.src = stream.src;
                            sourceElem.type = stream.type;
                            video.load();
                            video.playbackRate = videoPlugin._playbackRate || 1;
                            cbResolve();
                        })
                    };

                    doLoadCallback(videoPlugin.video)
                        .then(() => {
                            resolve(stream);
                        })
                        .catch((err) => {
                            reject(err);
                        });
                });
            }
        }
    });
    
})();

/*  
	Paella HTML 5 Multistream Player
	Copyright (C) 2017  Universitat Politècnica de València Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
*/



(function(){

class CaptionParserManager {
	addPlugin(plugin) {
		var self = this;
		var ext = plugin.ext;
		
		if ( ((Array.isArray && Array.isArray(ext)) || (ext instanceof Array)) == false) {
			ext = [ext]; 
		}
		if (ext.length == 0) {
			paella.log.debug("No extension provided by the plugin " + plugin.getName());
		}
		else {
			paella.log.debug("New captionParser added: " + plugin.getName());		
			ext.forEach(function(f){
				self._formats[f] = plugin;
			});
		}
	}

	constructor() {
		this._formats = {};
		paella.pluginManager.setTarget('captionParser', this);	
	}
}

let captionParserManager = new CaptionParserManager();

class SearchCallback extends paella.utils.AsyncLoaderCallback {
	constructor(caption, text) {
		super();
		this.name = "captionSearchCallback";
		this.caption = caption;
		this.text = text;
	}

	load(onSuccess, onError) {
		this.caption.search(this.text, (err, result) => {
			if (err) {
				onError();
			}
			else {
				this.result = result;
				onSuccess();
			}
		});
	}
}

paella.captions = {
	parsers: {},
	_captions: {},
	_activeCaption: undefined,
		
	addCaptions: function(captions) {
		var cid = captions._captionsProvider + ':' + captions._id;		
		this._captions[cid] = captions;
		paella.events.trigger(paella.events.captionAdded, cid);
	},	
		
	getAvailableLangs: function() {
		var ret = [];
		var self = this;
		Object.keys(this._captions).forEach(function(k){
			var c = self._captions[k];
			ret.push({
				id: k,
				lang: c._lang
			});
		});
		return ret;
	},
	
	getCaptions: function(cid) {	
		if (cid && this._captions[cid]) {
			return this._captions[cid];
		}
		return undefined;
	},	
	
	getActiveCaptions: function(cid) {
		return this._activeCaption;
	},
	
	setActiveCaptions: function(cid) {
		this._activeCaption = this.getCaptions(cid);
		
		if (this._activeCaption != undefined) {				
			paella.events.trigger(paella.events.captionsEnabled, cid);
		}
		else {
			paella.events.trigger(paella.events.captionsDisabled);			
		}
		
		return this._activeCaption;
	},
		
	getCaptionAtTime: function(cid, time) {
		var c = this.getCaptions(cid);		
		if (c != undefined) {
			return c.getCaptionAtTime(time);
		}
		return undefined;			
	},
	
	search: function(text, next) {
		var self = this;
		var asyncLoader = new paella.utils.AsyncLoader();
		
		this.getAvailableLangs().forEach(function(l) {			
			asyncLoader.addCallback(new SearchCallback(self.getCaptions(l.id), text));
		});
		
		asyncLoader.load(function() {
				var res = [];
				Object.keys(asyncLoader.callbackArray).forEach(function(k) {
					res = res.concat(asyncLoader.getCallback(k).result);
				});
				if (next) next(false, res);
			},
			function() {
				if (next) next(true);
			}
		);		
	}
};


class Caption {
	constructor(id, format, url, lang, next) {
		this._id = id;
		this._format = format;
		this._url = url;
		this._captions = undefined;
		this._index = undefined;
		
		if (typeof(lang) == "string") { lang = {code: lang, txt: lang}; }
		this._lang = lang;
		this._captionsProvider = "downloadCaptionsProvider";
		
		this.reloadCaptions(next);
	}
	
	canEdit(next) {
		// next(err, canEdit)
		next(false, false);
	}
	
	goToEdit() {	
	
	}
	
	reloadCaptions(next) {
		var self = this;
	
	
		jQuery.ajax({
			url: self._url,
			cache:false,
			type: 'get',
			dataType: "text",
			xhrFields: {
				withCredentials: true
			}
		})
		.then(function(dataRaw){
			var parser = captionParserManager._formats[self._format];
			if (parser == undefined) {
				paella.log.debug("Error adding captions: Format not supported!");
				if (!paella.player.videoContainer) {
					paella.log.debug("Video container is not ready, delaying parse until next reload");
					return;
				}
				paella.player.videoContainer.duration(true)
				.then((duration)=>{
					self._captions = [{
						id: 0,
		            	begin: 0,
		            	end: duration,
		            	content: paella.utils.dictionary.translate("Error! Captions format not supported.")
					}];
					if (next) { next(true); }
				});
			}
			else {
				parser.parse(dataRaw, self._lang.code, function(err, c) {
					if (!err) {
						self._captions = c;
						self._index = lunr(function () {
							var thisLunr = this;
							thisLunr.ref('id');
							thisLunr.field('content', {boost: 10});
							self._captions.forEach(function(cap){
								thisLunr.add({
									id: cap.id,
									content: cap.content,
								});
							});
						});
					}
					if (next) { next(err); }
				});
			}
		})
		.fail(function(error){
			paella.log.debug("Error loading captions: " + self._url);
				if (next) { next(true); }
		});
	}
	
	getCaptions() {
		return this._captions;	
	}
	
	getCaptionAtTime(time) {
		if (this._captions != undefined) {
			for (var i=0; i<this._captions.length; ++i) {			
				var l_cap = this._captions[i];
				if ((l_cap.begin <= time) && (l_cap.end >= time)) {
					return l_cap;
				}
			}
		}
		return undefined;		
	}
	
	getCaptionById(id) {
		if (this._captions != undefined) {
			for (var i=0; i<this._captions.length; ++i) {			
				let l_cap = this._captions[i];
				if (l_cap.id == id) {
					return l_cap;
				}
			}
		}
		return undefined;
	}
	
	search(txt, next) {
		var self = this;	
		if (this._index == undefined) {
			if (next) {
				next(true, "Error. No captions found.");
			}
		}
		else {
			var results = [];
			paella.player.videoContainer.trimming()
				.then((trimming)=>{
					this._index.search(txt).forEach(function(s){
						var c = self.getCaptionById(s.ref);
						if(trimming.enabled && (c.end<trimming.start || c.begin>trimming.end)){
							return;
						}
						results.push({time: c.begin, content: c.content, score: s.score});
					});		
					if (next) {
						next(false, results);
					}
				});
		}
	}	
}

paella.captions.Caption = Caption;

class CaptionParserPlugIn extends paella.FastLoadPlugin {
	get type() { return 'captionParser'; }
	getIndex() {return -1;}
	
	get ext() {
		if (!this._ext) {
			this._ext = [];
		}
		return this._ext;
	}

	parse(content, lang, next) {
		throw new Error('paella.CaptionParserPlugIn#parse must be overridden by subclass');
	}
}

paella.CaptionParserPlugIn = CaptionParserPlugIn;


}());

/*  
	Paella HTML 5 Multistream Player
	Copyright (C) 2017  Universitat Politècnica de València Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
*/



(function(){


var searchServiceManager = {
	_plugins: [],
	
	addPlugin: function(plugin) {
		this._plugins.push(plugin);
	},

	initialize: function() {
		paella.pluginManager.setTarget('SearchServicePlugIn', this);	
	}
};


class SearchCallback extends paella.utils.AsyncLoaderCallback {
	constructor(plugin, text) {
		super();
		this.name = "searchCallback";
		this.plugin = plugin;
		this.text = text;
	}

	load(onSuccess, onError) {
		this.plugin.search(this.text, (err, result) => {
			if (err) {
				onError();
			}
			else {
				this.result = result;
				onSuccess();
			}
		});
	}
}


paella.searchService = {
	
	search: function(text, next) {
		let asyncLoader = new paella.utils.AsyncLoader();
		
		paella.userTracking.log("paella:searchService:search", text);
		
		searchServiceManager._plugins.forEach(function(p) {
			asyncLoader.addCallback(new SearchCallback(p, text));
		});
		
		asyncLoader.load(function() {
				var res = [];
				Object.keys(asyncLoader.callbackArray).forEach(function(k) {
					res = res.concat(asyncLoader.getCallback(k).result);
				});
				if (next) next(false, res);
			},
			function() {
				if (next) next(true);
			}
		);	
	}
};


class SearchServicePlugIn extends paella.FastLoadPlugin {
	get type() { return 'SearchServicePlugIn'; }
	getIndex() {return -1;}
	
	search(text, next) {
		throw new Error('paella.SearchServicePlugIn#search must be overridden by subclass');
	}
}

paella.SearchServicePlugIn = SearchServicePlugIn;
searchServiceManager.initialize();

}());
/*  
	Paella HTML 5 Multistream Player
	Copyright (C) 2017  Universitat Politècnica de València Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
*/


(function(){


var userTrackingManager = {
	_plugins: [],

	addPlugin: function(plugin) {
		plugin.checkEnabled((isEnabled) => {
			if (isEnabled) {
				plugin.setup();
				this._plugins.push(plugin);
			}
		});
	},
	initialize: function() {
		paella.pluginManager.setTarget('userTrackingSaverPlugIn', this);
	}
};

paella.userTracking = {};
userTrackingManager.initialize();

class SaverPlugIn extends paella.FastLoadPlugin {
	get type() { return 'userTrackingSaverPlugIn'; }
	getIndex() { return -1; }
	checkEnabled(onSuccess) { onSuccess(true); }

	log(event, params) {
		throw new Error('paella.userTracking.SaverPlugIn#log must be overridden by subclass');
	}
}

paella.userTracking.SaverPlugIn = SaverPlugIn;


var evsentsToLog = {};

paella.userTracking.log = function(event, params) {
	if (evsentsToLog[event] != undefined) {
		evsentsToLog[event].cancel();
	}
	evsentsToLog[event] = new paella.utils.Timer(function(timer) {
		userTrackingManager._plugins.forEach(function(p) {
			p.log(event, params);
		});
		delete evsentsToLog[event];
	}, 500);
};



//////////////////////////////////////////////////////////
// Log automatic events
//////////////////////////////////////////////////////////
// Log simple events
[
	paella.events.play,
	paella.events.pause,
	paella.events.endVideo,
	paella.events.showEditor,
	paella.events.hideEditor,
	paella.events.enterFullscreen,
	paella.events.exitFullscreen,
	paella.events.loadComplete
].forEach(function(event){
	paella.events.bind(event, function(ev, params) {
		paella.userTracking.log(event);
	});
});

// Log show/hide PopUp
[
	paella.events.showPopUp,
	paella.events.hidePopUp]
.forEach(function(event){
	paella.events.bind(event, function(ev, params) {
		paella.userTracking.log(event, params.identifier);
	});
});

// Log captions Events
[
	// paella.events.captionAdded, 
	paella.events.captionsEnabled,
	paella.events.captionsDisabled
].forEach(function(event){
	paella.events.bind(event, function(ev, params) {
		var log;
		if (params != undefined) {
			var c = paella.captions.getCaptions(params);
			log = {id: params, lang: c._lang, url: c._url};
		}
		paella.userTracking.log(event, log);
	});
});

// Log setProfile
[
	paella.events.setProfile
].forEach(function(event){
	paella.events.bind(event, function(ev, params) {
		paella.userTracking.log(event, params.profileName);
	});
});


// Log seek events
[
	paella.events.seekTo,
	paella.events.seekToTime
].forEach(function(event){
	paella.events.bind(event, function(ev, params) {
		var log;
		try {
			JSON.stringify(params);
			log = params;
		}
		catch(e) {}

		paella.userTracking.log(event, log);
	});
});


// Log param events
[
	paella.events.setVolume,
	paella.events.resize,
	paella.events.setPlaybackRate,
	paella.events.qualityChanged
].forEach(function(event){
	paella.events.bind(event, function(ev, params) {
		var log;
		try {
			JSON.stringify(params);
			log = params;
		}
		catch(e) {}

		paella.userTracking.log(event, log);
	});
});


}());

/*  
	Paella HTML 5 Multistream Player
	Copyright (C) 2017  Universitat Politècnica de València Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
*/


(() => {

class TimeControl extends paella.DomNode {
	constructor(id) {
		super('div',id,{left:"0%"});
		this.domElement.className = 'timeControlOld';
		this.domElement.className = 'timeControl';
		//this.domElement.innerText = "0:00:00";
		var thisClass = this;
		paella.events.bind(paella.events.timeupdate,function(event,params) { thisClass.onTimeUpdate(params); });
	}

	onTimeUpdate(memo) {
		this.domElement.innerText = this.secondsToHours(parseInt(memo.currentTime));
	}

	secondsToHours(sec_numb) {
		var hours   = Math.floor(sec_numb / 3600);
		var minutes = Math.floor((sec_numb - (hours * 3600)) / 60);
		var seconds = sec_numb - (hours * 3600) - (minutes * 60);

		if (hours < 10) {hours = "0"+hours;}
		if (minutes < 10) {minutes = "0"+minutes;}
		if (seconds < 10) {seconds = "0"+seconds;}
		return hours + ':' + minutes + ':' + seconds;
	}
}

paella.TimeControl = TimeControl;

class PlaybackCanvasPlugin extends paella.DeferredLoadPlugin {
	get type() { return 'playbackCanvas'; }

	get playbackBarCanvas() { return this._playbackBarCanvas; }

	constructor() {
		super();
	}

	drawCanvas(context,width,height,videoData) {
		// videoData: {
		//		duration: fullDuration,
		//		trimming: {
		//			enabled: true | false,
		//			start: trimmingStart,
		//			end: trimmingEnd,
		//			duration: trimmedDuration | duration if trimming is not enabled
		//		}
		//	}
	}
}
paella.PlaybackCanvasPlugin = PlaybackCanvasPlugin;

class PlaybackBarCanvas {
	constructor(canvasElem) {
		this._parent = canvasElem;
		this._plugins = [];

		paella.pluginManager.setTarget('playbackCanvas', this);
	}

	addPlugin(plugin) {
		plugin._playbackBarCanvas = this;
		plugin.checkEnabled((isEnabled) => {
			if (isEnabled) {
				plugin.setup();
				this._plugins.push(plugin);
			}
		});
	}

	get parent() { return this._parent; }

	get canvas() {
		if (!this._canvas) {
			let createCanvas = (index) => {
				let result = document.createElement("canvas");
				result.className = "playerContainer_controls_playback_playbackBar_canvas layer_" + index;
				result.id = "playerContainer_controls_playback_playbackBar_canvas_" + index;
				result.width = $(this.parent).width();
				result.height = $(this.parent).height();
				return result;
			}
			this._canvas = [
				createCanvas(0),
				createCanvas(1)
			];
			$(this._parent).prepend(this._canvas[0]);
			$(this._parent).append(this._canvas[1]);
		}
		return this._canvas;
	}

	get context() {
		if (!this._context) {
			this._context = [
				this.canvas[0].getContext("2d"),
				this.canvas[1].getContext("2d")
			]
		}
		return this._context;
	}

	get width() {
		return this.canvas[0].width;
	}

	get height() {
		return this.canvas[0].height;
	}

	resize(w,h) {
		this.canvas[0].width = w;
		this.canvas[0].height = h;
		this.canvas[1].width = w;
		this.canvas[1].height = h;
		this.drawCanvas();
	}

	drawCanvas(){
		let duration = 0;
		paella.player.videoContainer.duration(true)
			.then((d) => {
				duration = d;
				return paella.player.videoContainer.trimming();
			})

			.then((trimming) => {
				let trimmedDuration = 0;
				if (trimming.enabled) {
					trimmedDuration = trimming.end - trimming.start;
				}
				let videoData = {
					duration: duration,
					trimming: {
						enabled: trimming.enabled,
						start: trimming.start,
						end: trimming.end,
						duration: trimming.enabled ? trimming.end - trimming.start : duration
					}
				}
				let ctx = this.context;
				let w = this.width;
				let h = this.height;
				this.clearCanvas();
				this._plugins.forEach((plugin) => {
					plugin.drawCanvas(ctx,w,h,videoData);
				});
			})
	}

	clearCanvas() {
		let clear = (ctx,w,h) => {
			ctx.clearRect(0, 0, w, h);
		}
		clear(this.context[0],this.width,this.height);
		clear(this.context[1],this.width,this.height);
	}
}

class PlaybackBar extends paella.DomNode {

	constructor(id) {
		var style = {};
		super('div',id,style);

		this.playbackFullId = '';
		this.updatePlayBar = true;
		this.timeControlId = '';
		this._images = null;
		this._prev = null;
		this._next = null;
		this._videoLength = null;
		this._lastSrc = null;
		this._aspectRatio = 1.777777778;
		this._hasSlides = null;
		this._imgNode = null;
		this._canvas = null;
		
		this.domElement.className = "playbackBar";
		this.domElement.setAttribute("alt", "");
		//this.domElement.setAttribute("title", "Timeline Slider");
		this.domElement.setAttribute("aria-label", "Timeline Slider");
		this.domElement.setAttribute("role", "slider");
		this.domElement.setAttribute("aria-valuemin", "0");
		this.domElement.setAttribute("aria-valuemax", "100");
		this.domElement.setAttribute("aria-valuenow", "0");
		this.domElement.setAttribute("tabindex", paella.tabIndex.next);
		$(this.domElement).keyup((event) => {
			var currentTime = 0;
			var duration = 0;
			paella.player.videoContainer.currentTime()
				.then((t) => {
					currentTime = t;
					return paella.player.videoContainer.duration();
				})

				.then((d) => {
					duration = d;
					var curr, selectedPosition;
					switch(event.keyCode) {
						case 37: //Left
							curr = 100*currentTime/duration;
							selectedPosition = curr - 5;
							paella.player.videoContainer.seekTo(selectedPosition);
							break;
						case 39: //Right
							curr = 100*currentTime/duration;
							selectedPosition = curr + 5;
							paella.player.videoContainer.seekTo(selectedPosition);
							break;
					}
				});
		});

		this.playbackFullId = id + "_full";
		this.timeControlId = id + "_timeControl";
		var playbackFull = new paella.DomNode('div',this.playbackFullId,{width:'0%'});
		playbackFull.domElement.className = "playbackBarFull";
		this.addNode(playbackFull);
		this.addNode(new paella.TimeControl(this.timeControlId));
		var thisClass = this;
		paella.events.bind(paella.events.timeupdate,function(event,params) { thisClass.onTimeUpdate(params); });
		$(this.domElement).bind('mousedown',function(event) {
			paella.utils.mouseManager.down(thisClass,event); event.stopPropagation();
		});
		$(playbackFull.domElement).bind('mousedown',function(event) {
			paella.utils.mouseManager.down(thisClass,event); event.stopPropagation();
		});
		if (!paella.utils.userAgent.browser.IsMobileVersion) {
			$(this.domElement).bind('mousemove',function(event) {
				thisClass.movePassive(event); paella.utils.mouseManager.move(event);
			});
			$(playbackFull.domElement).bind('mousemove',function(event) {
				paella.utils.mouseManager.move(event);
			});
			$(this.domElement).bind("mouseout",function(event) {
				thisClass.mouseOut(event);
			});
		}
		
		this.domElement.addEventListener('touchstart',(event) => {
			paella.utils.mouseManager.down(thisClass,event); event.stopPropagation();
		}, false);
		this.domElement.addEventListener('touchmove',(event) => {
			thisClass.movePassive(event);
			paella.utils.mouseManager.move(event);
		}, false);
		this.domElement.addEventListener('touchend',(event) => {
			paella.utils.mouseManager.up(event);
			thisClass.clearTimeOverlay();
		}, false);
	
		$(this.domElement).bind('mouseup',function(event) {
			paella.utils.mouseManager.up(event);
		});
		$(playbackFull.domElement).bind('mouseup',function(event) {
			paella.utils.mouseManager.up(event);
		});

		if (paella.player.isLiveStream()) {
			$(this.domElement).hide();
		}

		paella.events.bind(paella.events.seekAvailabilityChanged, (e,data) => {
			if (data.type!=paella.SeekType.DISABLED) {
				$(playbackFull.domElement).removeClass("disabled");
			}
			else {
				$(playbackFull.domElement).addClass("disabled");
			}
		});

		this._canvas = new PlaybackBarCanvas(this.domElement);
	}

	mouseOut(event){
		this.clearTimeOverlay();
	}

	clearTimeOverlay() {
		if(this._hasSlides) {
			$("#divTimeImageOverlay").remove();
		}
		else {
			$("#divTimeOverlay").remove();
		}
	}

	movePassive(event){
		var This = this;

		function updateTimePreview(duration,trimming) {
			// CONTROLS_BAR POSITON
			var p = $(This.domElement);
			var pos = p.offset();

			var width = p.width();
			let clientX = event.touches ? event.touches[0].clientX : event.clientX;
			var left = (clientX-pos.left);
			left = (left < 0) ? 0 : left;
			var position = left * 100 / width; // GET % OF THE STREAM

			var time = position * duration / 100;
			if (trimming.enabled) {
				time += trimming.start;
			}

			var hou = Math.floor((time - trimming.start) / 3600)%24;
			hou = ("00"+hou).slice(hou.toString().length);

			var min = Math.floor((time - trimming.start) / 60)%60;
			min = ("00"+min).slice(min.toString().length);

			var sec = Math.floor((time - trimming.start)%60);
			sec = ("00"+sec).slice(sec.toString().length);

			var timestr = (hou+":"+min+":"+sec);

			// CREATING THE OVERLAY
			if(This._hasSlides) {
				if($("#divTimeImageOverlay").length == 0)
					This.setupTimeImageOverlay(timestr,pos.top,width);
				else {
					$("#divTimeOverlay")[0].innerText = timestr; //IF CREATED, UPDATE TIME AND IMAGE
				}

				// CALL IMAGEUPDATE
				This.imageUpdate(time);
			}
			else {
				if($("#divTimeOverlay").length == 0) {
					This.setupTimeOnly(timestr,pos.top,width);
				}
				else {
					$("#divTimeOverlay")[0].innerText = timestr;
				}
			}

			// UPDATE POSITION IMAGE OVERLAY
			if (This._hasSlides) {
				var ancho = $("#divTimeImageOverlay").width();
				var posx = clientX-(ancho/2);
				if(clientX > (ancho/2 + pos.left)  &&  clientX < (pos.left+width - ancho/2) ) { // LEFT
					$("#divTimeImageOverlay").css("left",posx); // CENTER THE DIV HOVER THE MOUSE
				}
				else if(clientX < width / 2)
					$("#divTimeImageOverlay").css("left",pos.left);
				else
					$("#divTimeImageOverlay").css("left",pos.left + width - ancho);
			}

			// UPDATE POSITION TIME OVERLAY
			var ancho2 = $("#divTimeOverlay").width();
			var posx2 = clientX-(ancho2/2);
			if(clientX > ancho2/2 + pos.left  && clientX < (pos.left+width - ancho2/2) ){
				$("#divTimeOverlay").css("left",posx2); // CENTER THE DIV HOVER THE MOUSE
			}
			else if(clientX < width / 2)
				$("#divTimeOverlay").css("left",pos.left);
			else
				$("#divTimeOverlay").css("left",pos.left + width - ancho2-2);

			if(This._hasSlides) {
				$("#divTimeImageOverlay").css("bottom",$('.playbackControls').height());
			}
		}

		let duration = 0;
		paella.player.videoContainer.duration()
			.then(function(d) {
				duration = d;
				return paella.player.videoContainer.trimming();
			})
			.then(function(trimming) {
				updateTimePreview(duration,trimming);
			});
	}

	imageSetup(){
		return new Promise((resolve) => {
			paella.player.videoContainer.duration()
				.then((duration) => {
					//  BRING THE IMAGE ARRAY TO LOCAL
					this._images = {};
					var n = paella.initDelegate.initParams.videoLoader.frameList;

					if( !n || Object.keys(n).length === 0) {
						this._hasSlides = false;
						return;
					}
					else {
						this._hasSlides = true;
					}


					this._images = n; // COPY TO LOCAL
					this._videoLength = duration;

					// SORT KEYS FOR SEARCH CLOSEST
					this._keys = Object.keys(this._images);
					this._keys = this._keys.sort(function(a, b){return parseInt(a)-parseInt(b);}); // SORT FRAME NUMBERS STRINGS

					//NEXT
					this._next = 0;
					this._prev = 0;

					resolve();
				});
		});
	}

	imageUpdate(sec){
		var src = $("#imgOverlay").attr('src');
		$(this._imgNode).show();
		if(sec > this._next || sec < this._prev) {
			src = this.getPreviewImageSrc(sec);
			if (src) {
				this._lastSrc = src;
				$( "#imgOverlay" ).attr('src', src); // UPDATING IMAGE
			}
			else {
				this.hideImg();
			}
		} // RELOAD IF OUT OF INTERVAL
		else { 	
			if(src!=undefined) {
				return;
			}
			else { 
				$( "#imgOverlay" ).attr('src', this._lastSrc); 
			}// KEEP LAST IMAGE
		}
	}

	hideImg() {
		$(this._imgNode).hide();
	}

	getPreviewImageSrc(sec){
		var keys = Object.keys(this._images);

		keys.push(sec);

		keys.sort(function(a,b){
			return parseInt(a)-parseInt(b);
		});

		var n = keys.indexOf(sec)-1;
		n = (n > 0) ? n : 0;

		var i = keys[n];

		var next = keys[n+2];
		var prev = keys[n];

		next = (next==undefined) ? keys.length-1 : parseInt(next);
		this._next = next;

		prev = (prev==undefined) ? 0 : parseInt(prev);
		this._prev = prev;

		i=parseInt(i);
		if(this._images[i]){
			return this._images[i].url || this._images[i].url;
		}
		else return false;
	}

	setupTimeImageOverlay(time_str,top,width){
		var div = document.createElement("div");
		div.className = "divTimeImageOverlay";
		div.id = ("divTimeImageOverlay");

		var aux = Math.round(width/10);
		div.style.width = Math.round(aux*self._aspectRatio)+"px"; //KEEP ASPECT RATIO 4:3
		//div.style.height = Math.round(aux)+"px";

		if (this._hasSlides) {
			var img = document.createElement("img");
			img.className =  "imgOverlay";
			img.id = "imgOverlay";
			this._imgNode = img;

			div.appendChild(img);
		}


		var div2 = document.createElement("div");
		div2.className = "divTimeOverlay";
		div2.style.top = (top-20)+"px"; 
		div2.id = ("divTimeOverlay");
		div2.innerText = time_str;

		div.appendChild(div2);

		//CHILD OF CONTROLS_BAR
		$(this.domElement).parent().append(div);
	}
	
	setupTimeOnly(time_str,top,width){
		var div2 = document.createElement("div");
		div2.className = "divTimeOverlay";
		div2.style.top = (top-20)+"px"; 
		div2.id = ("divTimeOverlay");
		div2.innerText = time_str;

		//CHILD OF CONTROLS_BAR
		$(this.domElement).parent().append(div2);
	}

	playbackFull() {
		return this.getNode(this.playbackFullId);
	}

	timeControl() {
		return this.getNode(this.timeControlId);
	}

	setPlaybackPosition(percent) {
		this.playbackFull().domElement.style.width = percent + '%';
	}

	isSeeking() {
		return !this.updatePlayBar;
	}

	onTimeUpdate(memo) {
		if (this.updatePlayBar) {
			var currentTime = memo.currentTime;
			var duration = memo.duration;
			this.setPlaybackPosition(currentTime * 100 / duration);
		}
	}

	down(event,x,y) {
		this.updatePlayBar = false;
		this.move(event,x,y);
	}

	move(event,x,y) {
		var width = $(this.domElement).width();
		var selectedPosition = x - $(this.domElement).offset().left; // pixels
		if (selectedPosition<0) {
			selectedPosition = 0;
		}
		else if (selectedPosition>width) {
			selectedPosition = 100;
		}
		else {
			selectedPosition = selectedPosition * 100 / width; // percent
		}
		this.setPlaybackPosition(selectedPosition);
	}

	up(event,x,y) {
		var width = $(this.domElement).width();
		var selectedPosition = x - $(this.domElement).offset().left; // pixels
		if (selectedPosition<0) {
			selectedPosition = 0;
		}
		else if (selectedPosition>width) {
			selectedPosition = 100;
		}
		else {
			selectedPosition = selectedPosition * 100 / width; // percent
		}
		paella.player.videoContainer.seekTo(selectedPosition);
		this.updatePlayBar = true;
	}

	onresize() {
		this.imageSetup();
		let elem = $(this.domElement);
		this._canvas.resize(elem.width(),elem.height());
	}
}

paella.PlaybackBar = PlaybackBar;

class PlaybackControl extends paella.DomNode {

	addPlugin(plugin) {
		var id = 'buttonPlugin' + this.buttonPlugins.length;
		this.buttonPlugins.push(plugin);
		var button = paella.ButtonPlugin.BuildPluginButton(plugin,id);
		button.plugin = plugin;
		let expand = paella.ButtonPlugin.BuildPluginExpand(plugin,id);
		plugin.button = button;
		plugin._expandElement = expand;
		this.pluginsContainer.domElement.appendChild(button);
		if (expand) {
			let This = this;
			$(button).mouseover(function(evt) {
				evt.target.plugin.expand();
				This._expandedPlugin = evt.target.plugin;
			});
			this.pluginsContainer.domElement.appendChild(expand);
		}
		$(button).hide();
		plugin.checkEnabled((isEnabled) => {
			var parent;
			if (isEnabled) {
				$(plugin.button).show();
				paella.pluginManager.setupPlugin(plugin);

				var id = 'buttonPlugin' + this.buttonPlugins.length;
				if (plugin.getButtonType()==paella.ButtonPlugin.type.popUpButton) {
					parent = this.popUpPluginContainer.domElement;
					var popUpContent = paella.ButtonPlugin.BuildPluginPopUp(parent,plugin,id + '_container');
					this.popUpPluginContainer.registerContainer(plugin.getName(),popUpContent,button,plugin);
				}
				else if (plugin.getButtonType()==paella.ButtonPlugin.type.timeLineButton) {
					parent = this.timeLinePluginContainer.domElement;
					var timeLineContent = paella.ButtonPlugin.BuildPluginPopUp(parent, plugin,id + '_timeline');
					this.timeLinePluginContainer.registerContainer(plugin.getName(),timeLineContent,button,plugin);
				}
				else if (plugin.getButtonType()==paella.ButtonPlugin.type.menuButton) {
					parent = this.popUpPluginContainer.domElement;
					var popUpContent = paella.ButtonPlugin.BuildPluginMenu(parent,plugin,id + '_container');
					this.popUpPluginContainer.registerContainer(plugin.getName(),popUpContent,button,plugin);
				}
			}
			else {
				this.pluginsContainer.domElement.removeChild(plugin.button);
			}
		});
	}

	constructor(id) {
		var style = {};
		super('div',id,style);

		this.playbackBarId = '';
		this.pluginsContainer = null;
		this._popUpPluginContainer = null;
		this._timeLinePluginContainer = null;
		this.playbackPluginsWidth = 0;
		this.popupPluginsWidth = 0;
		this.minPlaybackBarSize = 120;
		this.playbackBarInstance = null;
		this.buttonPlugins = [];

		
		this.domElement.className = 'playbackControls';
		this.playbackBarId = id + '_playbackBar';

		var thisClass = this;
		this.pluginsContainer = new paella.DomNode('div',id + '_playbackBarPlugins');
		this.pluginsContainer.domElement.className = 'playbackBarPlugins';
		this.pluginsContainer.domElement.setAttribute("role", "toolbar");
		this.addNode(this.pluginsContainer);

		this.addNode(new paella.PlaybackBar(this.playbackBarId));

		paella.pluginManager.setTarget('button',this);

		$(window).mousemove((evt) => {
			if (this._expandedPlugin && ($(window).height() - evt.clientY)> 50) {
				this._expandedPlugin.contract();
				this._expandPlugin = null;
			}
		});
	}

	get popUpPluginContainer() {
		if (!this._popUpPluginContainer) {
			this._popUpPluginContainer = new paella.PopUpContainer(this.identifier + '_popUpPluginContainer','popUpPluginContainer');
			this.addNode(this._popUpPluginContainer);
		}
		return this._popUpPluginContainer;
	}

	get timeLinePluginContainer() {
		if (!this._timeLinePluginContainer) {
			this._timeLinePluginContainer = new paella.TimelineContainer(this.identifier + '_timelinePluginContainer','timelinePluginContainer');
			this.addNode(this._timeLinePluginContainer);
		}
		return this._timeLinePluginContainer;
	}

	showPopUp(identifier,button,swapFocus=false) {
		this.popUpPluginContainer.showContainer(identifier,button,swapFocus);
		this.timeLinePluginContainer.showContainer(identifier,button,swapFocus);
		this.hideCrossTimelinePopupButtons(identifier,this.popUpPluginContainer,this.timeLinePluginContainer,button,swapFocus);
	}

	// Hide popUpPluginContainer when a timeLinePluginContainer popup opens, and visa versa
	hideCrossTimelinePopupButtons(identifier, popupContainer, timelineContainer, button, swapFocus=true) {
		var containerToHide = null;
		if (popupContainer.containers[identifier]
			&& timelineContainer.containers[timelineContainer.currentContainerId]) {
			containerToHide = timelineContainer;
		} else if (timelineContainer.containers[identifier]
			&& popupContainer.containers[popupContainer.currentContainerId]) {
			containerToHide = popupContainer;
		}
		if (containerToHide) {
			var hideId = containerToHide.currentContainerId;
			var hidePugin = paella.pluginManager.getPlugin(hideId);
			if (hidePugin) {
				containerToHide.hideContainer(hideId,hidePugin.button,swapFocus);
			}
		}
	}

	hidePopUp(identifier,button,swapFocus=true) {
		this.popUpPluginContainer.hideContainer(identifier,button,swapFocus);
		this.timeLinePluginContainer.hideContainer(identifier,button,swapFocus);
	}

	playbackBar() {
		if (this.playbackBarInstance==null) {
			this.playbackBarInstance = this.getNode(this.playbackBarId);
		}
		return this.playbackBarInstance;
	}

	onresize() {
		var windowSize = $(this.domElement).width();
		paella.log.debug("resize playback bar (width=" + windowSize + ")");

		for (var i=0;i<this.buttonPlugins.length;++i) {
			var plugin = this.buttonPlugins[i];
			var minSize = plugin.getMinWindowSize();
			if (minSize > 0 && windowSize < minSize) {
				plugin.hideUI();
			}
			else {
				plugin.checkVisibility();
			}
		}

		this.getNode(this.playbackBarId).onresize();
	}
}

paella.PlaybackControl = PlaybackControl;

class ControlsContainer extends paella.DomNode {
	addPlugin(plugin) {
		var id = 'videoOverlayButtonPlugin' + this.buttonPlugins.length;
		this.buttonPlugins.push(plugin);
		var button = paella.ButtonPlugin.BuildPluginButton(plugin,id);
		this.videoOverlayButtons.domElement.appendChild(button);
		plugin.button = button;
		$(button).hide();
		plugin.checkEnabled(function(isEnabled) {
			if (isEnabled) {
				$(plugin.button).show();
				paella.pluginManager.setupPlugin(plugin);
			}
		});
	}

	constructor(id) {
		super('div',id);

		this.playbackControlId = '';
		this.editControlId = '';
		this.isEnabled = true;
		this.autohideTimer = null;
		this.hideControlsTimeMillis = 3000;
		this.playbackControlInstance = null;
		this.videoOverlayButtons = null;
		this.buttonPlugins = [];
		this._hidden = false;
		this._over = false;

		this.viewControlId = id + '_view';
		this.playbackControlId = id + '_playback';
		this.editControlId = id + '_editor';
		this.addNode(new paella.PlaybackControl(this.playbackControlId));
		var thisClass = this;
		paella.events.bind(paella.events.showEditor,function(event) { thisClass.onShowEditor(); });
		paella.events.bind(paella.events.hideEditor,function(event) { thisClass.onHideEditor(); });

		paella.events.bind(paella.events.play,function(event) { thisClass.onPlayEvent(); });
		paella.events.bind(paella.events.pause,function(event) { thisClass.onPauseEvent(); });
		$(document).mousemove(function(event) {
			paella.player.controls.restartHideTimer();
		});

		$(this.domElement).bind("mousemove",function(event) { thisClass._over = true; });
		$(this.domElement).bind("mouseout",function(event) { thisClass._over = false; });

		paella.events.bind(paella.events.endVideo,function(event) { thisClass.onEndVideoEvent(); });
		paella.events.bind('keydown',function(event) { thisClass.onKeyEvent(); });

		this.videoOverlayButtons = new paella.DomNode('div',id + '_videoOverlayButtonPlugins');
		this.videoOverlayButtons.domElement.className = 'videoOverlayButtonPlugins';
		this.videoOverlayButtons.domElement.setAttribute("role", "toolbar");
		this.addNode(this.videoOverlayButtons);

		paella.pluginManager.setTarget('videoOverlayButton',this);
	}

	onShowEditor() {
		var editControl = this.editControl();
		if (editControl) $(editControl.domElement).hide();
	}

	onHideEditor() {
		var editControl = this.editControl();
		if (editControl) $(editControl.domElement).show();
	}

	enterEditMode() {
		var playbackControl = this.playbackControl();
		var editControl = this.editControl();
		if (playbackControl && editControl) {
			$(playbackControl.domElement).hide();
		}
	}

	exitEditMode() {
		var playbackControl = this.playbackControl();
		var editControl = this.editControl();
		if (playbackControl && editControl) {
			$(playbackControl.domElement).show();
		}
	}

	playbackControl() {
		if (this.playbackControlInstance==null) {
			this.playbackControlInstance = this.getNode(this.playbackControlId);
		}
		return this.playbackControlInstance;
	}

	editControl() {
		return this.getNode(this.editControlId);
	}

	disable() {
		this.isEnabled = false;
		this.hide();
	}

	enable() {
		this.isEnabled = true;
		this.show();
	}

	isHidden() {
		return this._hidden;
	}

	hide() {
		var This = this;
		this._doHide = true;
		
		function hideIfNotCanceled() {
			if (This._doHide) {
				$(This.domElement).css({opacity:0.0});
				$(This.domElement).hide();
				This.domElement.setAttribute('aria-hidden', 'true');
				This._hidden = true;
				paella.events.trigger(paella.events.controlBarDidHide);
			}
		}

		paella.events.trigger(paella.events.controlBarWillHide);
		if (this._doHide) {
			if (!paella.utils.userAgent.browser.IsMobileVersion && !paella.utils.userAgent.browser.Explorer) {			
				$(this.domElement).animate({opacity:0.0},{duration:300, complete: hideIfNotCanceled});
			}
			else {
				hideIfNotCanceled();
			}		
		}
	}

	showPopUp(identifier) {
		this.playbackControl().showPopUp(identifier);
	}

	hidePopUp(identifier) {
		this.playbackControl().hidePopUp(identifier);
	}

	show() {
		if (this.isEnabled) {
			$(this.domElement).stop();
			this._doHide = false;
			this.domElement.style.opacity = 1.0;
			this.domElement.setAttribute('aria-hidden', 'false');
			this._hidden = false;
			$(this.domElement).show();
			paella.events.trigger(paella.events.controlBarDidShow);
		}
	}

	autohideTimeout() {
		var playbackBar = this.playbackControl().playbackBar();
		if (playbackBar.isSeeking() || this._over) {
			paella.player.controls.restartHideTimer();
		}
		else {
			paella.player.controls.hideControls();
		}
	}

	hideControls() {
		paella.player.videoContainer.paused()
			.then((paused) => {
				if (!paused) {
					this.hide();
				}
				else {
					this.show();
				}
			});
	}

	showControls() {
		this.show();
	}

	onPlayEvent() {
		this.restartHideTimer();
	}

	onPauseEvent() {
		this.clearAutohideTimer();
	}

	onEndVideoEvent() {
		this.show();
		this.clearAutohideTimer();
	}

	onKeyEvent() {
		this.restartHideTimer();
		paella.player.videoContainer.paused()
			.then(function(paused) {
				if (!paused) {
					paella.player.controls.restartHideTimer();
				}
			});
	}

	cancelHideBar() {
		this.restartTimerEvent();
	}

	restartTimerEvent() {
		if (this.isHidden()){
			this.showControls();
		}
		this._doHide = false;
		paella.player.videoContainer.paused((paused) => {
			if (!paused) {
				this.restartHideTimer();
			}
		});
	}

	clearAutohideTimer() {
		if (this.autohideTimer!=null) {
			this.autohideTimer.cancel();
			this.autohideTimer = null;
		}
	}

	restartHideTimer() {
		this.showControls();
		this.clearAutohideTimer();
		var thisClass = this;
		this.autohideTimer = new paella.utils.Timer(function(timer) {
			thisClass.autohideTimeout();
		},this.hideControlsTimeMillis);
	}

	onresize() {
		this.playbackControl().onresize();
	}
}

paella.ControlsContainer = ControlsContainer;


})();

/*  
	Paella HTML 5 Multistream Player
	Copyright (C) 2017  Universitat Politècnica de València Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
*/

(function() {

	class LoaderContainer extends paella.DomNode {
	
		constructor(id) {
			super('div',id,{position:'fixed',backgroundColor:'white',opacity:'0.7',top:'0px',left:'0px',right:'0px',bottom:'0px',zIndex:10000});
			this.timer = null;
			this.loader = null;
			this.loaderPosition = 0;

			this.loader = this.addNode(new paella.DomNode('i','',{
				width: "100px",
				height: "100px",
				color: "black",
				display: "block",
				marginLeft: "auto",
				marginRight: "auto",
				marginTop: "32%",
				fontSize: "100px",
			}));
			this.loader.domElement.className = "icon-spinner";
	
			paella.events.bind(paella.events.loadComplete,(event,params) => { this.loadComplete(params); });
			this.timer = new paella.utils.Timer((timer) => {
				//thisClass.loaderPosition -= 128;
				
				//thisClass.loader.domElement.style.backgroundPosition = thisClass.loaderPosition + 'px';
				this.loader.domElement.style.transform = `rotate(${ this.loaderPosition }deg`;
				this.loaderPosition+=45;
			},250);
			this.timer.repeat = true;
		}
	
		loadComplete(params) {
			$(this.domElement).hide();
			this.timer.repeat = false;
		}
	}

	paella.LoaderContainer = LoaderContainer;
	
	paella.Keys = {
		Space:32,
		Left:37,
		Up:38,
		Right:39,
		Down:40,
		A:65,B:66,C:67,D:68,E:69,F:70,G:71,H:72,I:73,J:74,K:75,L:76,M:77,N:78,O:79,P:80,Q:81,R:82,S:83,T:84,U:85,V:86,W:87,X:88,Y:89,Z:90
	};

	class KeyPlugin extends paella.FastLoadPlugin {
		get type() { return 'keyboard'; }

		onKeyPress(key) {
			console.log(key);
			return false;
		}
	}

	paella.KeyPlugin = KeyPlugin;

	let g_keyboardEventSet = false;
	class KeyManager {
		get isPlaying() { return this._isPlaying; }
		set isPlaying(p) { this._isPlaying = p; }
		
		get enabled() { return this._enabled!==undefined ? this._enabled : true; }
		set enabled(e) { this._enabled = e; }
	
		constructor() {
			this._isPlaying = false;
			var thisClass = this;
			paella.events.bind(paella.events.loadComplete,function(event,params) { thisClass.loadComplete(event,params); });
			paella.events.bind(paella.events.play,function(event) { thisClass.onPlay(); });
			paella.events.bind(paella.events.pause,function(event) { thisClass.onPause(); });

			paella.pluginManager.setTarget('keyboard',this);

			this._pluginList = []; 
			
		}

		addPlugin(plugin) {
			if (plugin.checkEnabled((e) => {
				this._pluginList.push(plugin);
				plugin.setup();
			}));
		}
	
		loadComplete(event,params) {
			if (g_keyboardEventSet) {
				return;
			}
			paella.events.bind("keyup",(event) => {
				this.keyUp(event);
			});
			g_keyboardEventSet = true;
		}
	
		onPlay() {
			this.isPlaying = true;
		}
	
		onPause() {
			this.isPlaying = false;
		}
	
		keyUp(event) {
			if (!this.enabled) return;

			this._pluginList.some((plugin) => {
				return plugin.onKeyPress(event);
			});
		}
	}

	paella.keyManager = new KeyManager();

})();


/*  
	Paella HTML 5 Multistream Player
	Copyright (C) 2017  Universitat Politècnica de València Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
*/

(() => {

	class VideoLoader {
		constructor() {
			this.metadata = {		// Video metadata
				title:"",
				duration:0
			};
			this.streams = [];		// {sources:{mp4:{src:"videourl.mp4",type:"video/mp4"},
									//			 ogg:{src:"videourl.ogv",type:"video/ogg"},
									//			 webm:{src:"videourl.webm",type:"video/webm"},
									//			 flv:{src:"videourl.flv",type:"video/x-flv"},
									//			 rtmp:{src:"rtmp://server.com/endpoint/url.loquesea",type="video/mp4 | video/x-flv"},
									//			 image:{frames:{frame_1:'frame_1.jpg',...frame_n:'frame_n.jpg'},duration:183},
									//	preview:'video_preview.jpg'}
			this.frameList = [];	// frameList[timeInstant] = { id:"frame_id", mimetype:"image/jpg", time:timeInstant, url:"image_url"}

			this.loadStatus = false;
			this.codecStatus = false;
		}
	
		getMetadata() {
			return this.metadata;
		}
	
		getVideoId() {
			return paella.initDelegate.getId();
		}
	
		getVideoUrl() {
			// This function must to return the base video URL
			return "";
		}
	
		getDataUrl() {
			// This function must to return the location of the video data file
		}
	
		loadVideo(onSuccess) {
			// This function must to:
			//	- load this.streams and this.frameList
			// 	- Check streams compatibility using this.isStreamCompatible(streamIndex)
			//	- Set this.loadStatus = true if load is Ok, or false if something gone wrong
			//	- Set this.codecStatus = true if the browser can reproduce all streams
			//	- Call onSuccess()
			onSuccess();
		}
	}

	paella.VideoLoader = VideoLoader;
	
	class AccessControl {
		canRead() {
			return paella_DeferredResolved(true);
		}
	
		canWrite() {
			return paella_DeferredResolved(false);
		}
	
		userData() {
			return paella_DeferredResolved({
				username: 'anonymous',
				name: 'Anonymous',
				avatar: paella.utils.folders.resources() + '/images/default_avatar.png',
				isAnonymous: true
			});
		}
	
		getAuthenticationUrl(callbackParams) {
			var authCallback = this._authParams.authCallbackName && window[this._authParams.authCallbackName];
			if (!authCallback && paella.player.config.auth) {
				authCallback = paella.player.config.auth.authCallbackName && window[paella.player.config.auth.authCallbackName];
			}
	
			if (typeof(authCallback)=="function") {
				return authCallback(callbackParams);
			}
			return "";
		}
	}

	paella.AccessControl = AccessControl;
	
	class PlayerBase {
		
		checkCompatibility() {
			let message = "";
			if (paella.utils.parameters.get('ignoreBrowserCheck')) {
				return true;
			}
			if (paella.utils.userAgent.browser.IsMobileVersion) return true;
			let isCompatible =	paella.utils.userAgent.browser.Chrome ||
								paella.utils.userAgent.browser.EdgeChromium ||
								paella.utils.userAgent.browser.Safari ||
								paella.utils.userAgent.browser.Firefox ||
								paella.utils.userAgent.browser.Opera ||
								paella.utils.userAgent.browser.Edge ||
								(paella.utils.userAgent.browser.Explorer && paella.utils.userAgent.browser.Version.major>=9);
			if (isCompatible) {
				return true;
			}
			else {
				var errorMessage = paella.utils.dictionary.translate("It seems that your browser is not HTML 5 compatible");
				paella.events.trigger(paella.events.error,{error:errorMessage});
				message = errorMessage + '<div style="display:block;width:470px;height:140px;margin-left:auto;margin-right:auto;font-family:Verdana,sans-sherif;font-size:12px;"><a href="http://www.google.es/chrome" style="color:#004488;float:left;margin-right:20px;"><img src="'+paella.utils.folders.resources()+'images/chrome.png" style="width:80px;height:80px" alt="Google Chrome"></img><p>Google Chrome</p></a><a href="http://windows.microsoft.com/en-US/internet-explorer/products/ie/home" style="color:#004488;float:left;margin-right:20px;"><img src="'+paella.utils.folders.resources()+'images/explorer.png" style="width:80px;height:80px" alt="Internet Explorer 9"></img><p>Internet Explorer 9</p></a><a href="http://www.apple.com/safari/" style="float:left;margin-right:20px;color:#004488"><img src="'+paella.utils.folders.resources()+'images/safari.png" style="width:80px;height:80px" alt="Safari"></img><p>Safari 5</p></a><a href="http://www.mozilla.org/firefox/" style="float:left;color:#004488"><img src="'+paella.utils.folders.resources()+'images/firefox.png" style="width:80px;height:80px" alt="Firefox"></img><p>Firefox 12</p></a></div>';
				message += '<div style="margin-top:30px;"><a id="ignoreBrowserCheckLink" href="#" onclick="window.location = window.location + \'&ignoreBrowserCheck=true\'">' + paella.utils.dictionary.translate("Continue anyway") + '</a></div>';
				paella.messageBox.showError(message,{height:'40%'});
			}
			return false;
		}
	
		constructor(playerId) {
			this.config = null;
			this.playerId = '';
			this.mainContainer = null;
			this.videoContainer = null;
			this.controls = null;
			this.accessControl = null;
	
			if (paella.utils.parameters.get('log') != undefined) {
				var log = 0;
				switch(paella.utils.parameters.get('log')) {
					case "error":
						log = paella.log.kLevelError;
						break;					
					case "warn":
						log = paella.log.kLevelWarning;
						break;					
					case "debug":
						log = paella.log.kLevelDebug;
						break;					
					case "log":
					case "true":
						log = paella.log.kLevelLog;
						break;
				}
				paella.log.setLevel(log);
			}		
				
			if (!this.checkCompatibility()) {
				paella.log.debug('It seems that your browser is not HTML 5 compatible');
			}
			else {
				paella.player = this;
				this.playerId = playerId;
				this.mainContainer = $('#' + this.playerId)[0];
				var thisClass = this;
				paella.events.bind(paella.events.loadComplete,function(event,params) { thisClass.loadComplete(event,params); });
			}
		}

		get repoUrl() { return paella.player.videoLoader._url || paella.player.config.standalone && paella.player.config.standalone.repository; }
		get videoUrl() { return paella.player.videoLoader.getVideoUrl(); }
		get dataUrl() { return paella.player.videoLoader.getDataUrl(); }
		get videoId() { return paella.initDelegate.getId(); }
		get startMuted() { return /true/.test(paella.utils.parameters.get("muted")); }
	
		loadComplete(event,params) {
	
		}
	
		get auth() {
			return {
				login: function(redirect) {
					redirect = redirect || window.location.href;
					var url = paella.initDelegate.initParams.accessControl.getAuthenticationUrl(redirect);
					if (url) {
						window.location.href = url;
					}
				},
		
				// The following functions returns promises
				canRead:function() {
					return paella.initDelegate.initParams.accessControl.canRead();
				},
		
				canWrite:function() {
					return paella.initDelegate.initParams.accessControl.canWrite();
				},
		
				userData:function() {
					return paella.initDelegate.initParams.accessControl.userData();
				}
			}
		}
	}

	paella.PlayerBase = PlayerBase;
	
	class InitDelegate {
		get initParams() {
			if (!this._initParams) {
				this._initParams = {
					configUrl:paella.baseUrl + 'config/config.json',
					dictionaryUrl:paella.baseUrl + 'localization/paella',
					accessControl:null,
					videoLoader:null,
					disableUserInterface: function() {
						return /true/i.test(paella.utils.parameters.get("disable-ui"));
					}
					// Other parameters set externally:
					//	config: json containing the configuration file
					//	loadConfig: function(defaultConfigUrl). Returns a promise with the config.json data
					//	url: attribute. Contains the repository base URL
					//	videoUrl: function. Returns the base URL of the video (example: baseUrl + videoID)
					//	dataUrl: function. Returns the full URL to get the data.json file
					//	loadVideo: Function. Returns a promise with the data.json file content
					//  disableUserInterface: Function. Returns true if the user interface should be disabled (only shows the video container)
				};
			}
			return this._initParams;
		}
	
		constructor(params) {
			if (arguments.length==2) {
				this._config = arguments[0];
			}
	
			if (params) {
				for (var key in params) {
					this.initParams[key] = params[key];
				}
			}

			if (!this.initParams.getId) {
				this.initParams.getId = function() {
					return paella.utils.parameters.get('id') || "noid";
				} 
			}
		}
	
		getId() {
			return this.initParams.getId();
		}
	
		loadDictionary() {
			return new Promise((resolve) => {
				paella.utils.ajax.get({ url:this.initParams.dictionaryUrl + "_" + paella.utils.dictionary.currentLanguage() + '.json' }, function(data,type,returnCode) {
					paella.utils.dictionary.addDictionary(data);
					resolve(data);
				},
				function(data,type,returnCode) {
					resolve();
				});
			});
		}
	
		loadConfig() {
			let loadAccessControl = (data) => {
				var AccessControlClass = paella.utils.objectFromString(data.player.accessControlClass || "paella.AccessControl");
				this.initParams.accessControl = new AccessControlClass();
			};
	
			if (this.initParams.config) {
				return new Promise((resolve) => {
					loadAccessControl(this.initParams.config);
					resolve(this.initParams.config);
				})
			}
			else if (this.initParams.loadConfig) {
				return new Promise((resolve,reject) => {
					this.initParams.loadConfig(this.initParams.configUrl)
						.then((data) => {
							loadAccessControl(data);
							resolve(data);
						})
						.catch((err) => {
							reject(err);
						});
				})
			}
			else {
				return new Promise((resolve,reject) => {
					var configUrl = this.initParams.configUrl;
					var params = {};
					params.url = configUrl;
					paella.utils.ajax.get(params,(data,type,returnCode) => {
							try {
								data = JSON.parse(data);
							}
							catch(e) {}
							loadAccessControl(data);
							resolve(data);
						},
						function(data,type,returnCode) {
							paella.messageBox.showError(paella.utils.dictionary.translate("Error! Config file not found. Please configure paella!"));
							//onSuccess({});
						});
				});
			}
		}
	}

	paella.InitDelegate = InitDelegate;
	
	window.paellaPlayer = null;
	paella.plugins = {};
	paella.plugins.events = {};
	paella.initDelegate = null;
	
})();


/*  
	Paella HTML 5 Multistream Player
	Copyright (C) 2017  Universitat Politècnica de València Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
*/

(() => {
	class PaellaPlayer extends paella.PlayerBase {
	
		getPlayerMode() {	
			if (paella.player.isFullScreen()) {
				return paella.PaellaPlayer.mode.fullscreen;
			}
			else if (window.self !== window.top) {
				return paella.PaellaPlayer.mode.embed
			}
	
			return paella.PaellaPlayer.mode.standard;
		}
	
	
		checkFullScreenCapability() {
			var fs = document.getElementById(paella.player.mainContainer.id);
			if ((fs.webkitRequestFullScreen) || (fs.mozRequestFullScreen) || (fs.msRequestFullscreen) || (fs.requestFullScreen)) {
				return true;
			}
			if (paella.utils.userAgent.browser.IsMobileVersion && paella.player.videoContainer.isMonostream) {
				return true;
			}		
			return false;
		}
	
		addFullScreenListeners() {
			var thisClass = this;
			
			var onFullScreenChangeEvent = function() {
				setTimeout(function() {
					paella.pluginManager.checkPluginsVisibility();
				}, 1000);
	
				var fs = document.getElementById(paella.player.mainContainer.id);
				
				if (paella.player.isFullScreen()) {				
					fs.style.width = '100%';
					fs.style.height = '100%';
				}
				else {
					fs.style.width = '';
					fs.style.height = '';
				}
				
				if (thisClass.isFullScreen()) {
					paella.events.trigger(paella.events.enterFullscreen);				
				}
				else{
					paella.events.trigger(paella.events.exitFullscreen);
				}			
			};
		
			if (!this.eventFullScreenListenerAdded) {
				this.eventFullScreenListenerAdded = true;
				document.addEventListener("fullscreenchange", onFullScreenChangeEvent, false);
				document.addEventListener("webkitfullscreenchange", onFullScreenChangeEvent, false);
				document.addEventListener("mozfullscreenchange", onFullScreenChangeEvent, false);	
				document.addEventListener("MSFullscreenChange", onFullScreenChangeEvent, false);
				document.addEventListener("webkitendfullscreen", onFullScreenChangeEvent, false);
			}
		}
	
		isFullScreen() {
			var webKitIsFullScreen = (document.webkitIsFullScreen === true);
			var msIsFullScreen = (document.msFullscreenElement !== undefined && document.msFullscreenElement !== null);
			var mozIsFullScreen = (document.mozFullScreen === true);
			var stdIsFullScreen = (document.fullScreenElement !== undefined && document.fullScreenElement !== null);
			
			return (webKitIsFullScreen || msIsFullScreen || mozIsFullScreen || stdIsFullScreen);
		}

		goFullScreen() {
			if (!this.isFullScreen()) {
				if (paella.utils.userAgent.system.iOS &&
					(paella.utils.userAgent.browser.Version.major<12 ||
					 !paella.utils.userAgent.system.iPad))
				{
					paella.player.videoContainer.masterVideo().goFullScreen();
				}
				else {			
					var fs = document.getElementById(paella.player.mainContainer.id);		
					if (fs.webkitRequestFullScreen) {			
						fs.webkitRequestFullScreen();
					}
					else if (fs.mozRequestFullScreen){
						fs.mozRequestFullScreen();
					}
					else if (fs.msRequestFullscreen) {
						fs.msRequestFullscreen();
					}
					else if (fs.requestFullScreen) {
						fs.requestFullScreen();
					}
				}
			}
		}
		
		exitFullScreen() {
			if (this.isFullScreen()) {			
				if (document.webkitCancelFullScreen) {
					document.webkitCancelFullScreen();
				}
				else if (document.mozCancelFullScreen) {
					document.mozCancelFullScreen();
				}
				else if (document.msExitFullscreen()) {
					document.msExitFullscreen();
				}
				else if (document.cancelFullScreen) {
					document.cancelFullScreen();
				}								
			}		
		}
	
		setProfile(profileName,animate) {
			if (paella.profiles.setProfile(profileName,animate)) {
				let profileData = paella.player.getProfile(profileName);
				if (profileData && !paella.player.videoContainer.isMonostream) {
					paella.utils.cookies.set('lastProfile', profileName);
				}
				paella.events.trigger(paella.events.setProfile,{profileName:profileName});
			}
		}
	
		getProfile(profileName) {
			return paella.profiles.getProfile(profileName);
		}
	
		constructor(playerId) {
			super(playerId);

			this.player = null;
	
			this.videoIdentifier = '';
			this.loader = null;
		
			// Video data:
			this.videoData = null;
	
			// if initialization ok
			if (this.playerId==playerId) {
				this.loadPaellaPlayer();
				var thisClass = this;
			}
		}

		get selectedProfile(){ return paella.profiles.currentProfileName; }
	
		loadPaellaPlayer() {
			var This = this;
			this.loader = new paella.LoaderContainer('paellaPlayer_loader');
			$('body')[0].appendChild(this.loader.domElement);
			paella.events.trigger(paella.events.loadStarted);
	
			paella.initDelegate.loadDictionary()
				.then(function() {
					return paella.initDelegate.loadConfig();
				})
	
				.then(function(config) {
					This.accessControl = paella.initDelegate.initParams.accessControl;
					This.videoLoader = paella.initDelegate.initParams.videoLoader;
					This.onLoadConfig(config);
					if (config.skin) {
						var skin = config.skin.default || 'dark';
						paella.utils.skin.restore(skin);
					}
				});
		}
	
		onLoadConfig(configData) {
			paella.data = new paella.Data(configData);
			paella.pluginManager.registerPlugins();
	
			this.config = configData;
			this.videoIdentifier = paella.initDelegate.getId();
	
			if (this.videoIdentifier) {
				if (this.mainContainer) {
					this.videoContainer = new paella.VideoContainer(this.playerId + "_videoContainer");
					var videoQualityStrategy = new paella.BestFitVideoQualityStrategy();
					try {
						var StrategyClass = this.config.player.videoQualityStrategy;
						var ClassObject = paella.utils.classFromString(StrategyClass);
						videoQualityStrategy = new ClassObject();
					}
					catch(e) {
						paella.log.warning("Error selecting video quality strategy: strategy not found");
					}
					this.videoContainer.setVideoQualityStrategy(videoQualityStrategy);
					
					this.mainContainer.appendChild(this.videoContainer.domElement);
				}
				$(window).resize(function(event) { paella.player.onresize(); });
				this.onload();
			}
			
			paella.pluginManager.loadPlugins("paella.FastLoadPlugin");
		}
	
		onload() {
			var thisClass = this;
			var ac = this.accessControl;
			var canRead = false;
			var userData = {};
			this.accessControl.canRead()
				.then(function(c) {
					canRead = c;
					return thisClass.accessControl.userData();
				})
	
				.then(function(d) {
					userData = d;
					if (canRead) {
						thisClass.loadVideo();
					}
					else if (userData.isAnonymous) {
						var redirectUrl = paella.initDelegate.initParams.accessControl.getAuthenticationUrl("player/?id=" + paella.player.videoIdentifier);
						var message = '<div>' + paella.utils.dictionary.translate("You are not authorized to view this resource") + '</div>';
						if (redirectUrl) {
							message += '<div class="login-link"><a href="' + redirectUrl + '">' + paella.utils.dictionary.translate("Login") + '</a></div>';
						}
						thisClass.unloadAll(message);
					}
					else {
						let errorMessage = paella.utils.dictionary.translate("You are not authorized to view this resource");
						thisClass.unloadAll(errorMessage);
						paella.events.trigger(paella.events.error,{error:errorMessage});
					}
				})
	
				.catch((error) => {
					let errorMessage = paella.utils.dictionary.translate(error);
					thisClass.unloadAll(errorMessage);
					paella.events.trigger(paella.events.error,{error:errorMessage});
				});
		}
	
		onresize() {		
			this.videoContainer.onresize();
			if (this.controls) this.controls.onresize();
	
			// Resize the layout profile
			if (this.videoContainer.ready) {
				var cookieProfile = paella.utils.cookies.get('lastProfile');
				if (cookieProfile) {
					this.setProfile(cookieProfile,false);
				}
				else {
					this.setProfile(paella.player.selectedProfile, false);
				}
			}
			
			paella.events.trigger(paella.events.resize,{width:$(this.videoContainer.domElement).width(), height:$(this.videoContainer.domElement).height()});
		}

		unloadAll(message) {
			var loaderContainer = $('#paellaPlayer_loader')[0];
			this.mainContainer.innerText = "";
			paella.messageBox.showError(message);
		}
	
		reloadVideos(masterQuality,slaveQuality) {
			if (this.videoContainer) {
				this.videoContainer.reloadVideos(masterQuality,slaveQuality);
				this.onresize();
			}
		}
	
		loadVideo() {
			if (this.videoIdentifier) {
				var This = this;
				var loader = paella.player.videoLoader;
				this.onresize();
				loader.loadVideo(() => {
					var playOnLoad = false;
					This.videoContainer.setStreamData(loader.streams)
						.then(function() {
							paella.events.trigger(paella.events.loadComplete);
							This.addFullScreenListeners();
							This.onresize();
							// If the player has been loaded using lazyLoad, the video should be
							// played as soon as it loads
							if (This.videoContainer.autoplay() || g_lazyLoadInstance!=null) {
								This.play();
							}
							else if (loader.metadata.preview) {
								This.lazyLoadContainer = new LazyThumbnailContainer(loader.metadata.preview);
								document.body.appendChild(This.lazyLoadContainer.domElement);
							}
						})
						.catch((error) => {
							console.error(error);
							let msg = error.message || "Could not load the video";
							paella.messageBox.showError(paella.utils.dictionary.translate(msg));
						});
				});
			}
		}
	
		showPlaybackBar() {
			if (!this.controls) {
				this.controls = new paella.ControlsContainer(this.playerId + '_controls');
				this.mainContainer.appendChild(this.controls.domElement);
				this.controls.onresize();
				paella.events.trigger(paella.events.loadPlugins,{pluginManager:paella.pluginManager});
	
			}
		}
	
		isLiveStream() {
			var loader = paella.initDelegate.initParams.videoLoader;
			var checkSource = function(sources,index) {
				if (sources.length>index) {
					var source = sources[index];
					for (var key in source.sources) {
						if (typeof(source.sources[key])=="object") {
							for (var i=0; i<source.sources[key].length; ++i) {
								var stream = source.sources[key][i];
								if (stream.isLiveStream) return true;
							}
						}
					}
				}
				return false;
			};
			return checkSource(loader.streams,0) || checkSource(loader.streams,1);
		}
	
		loadPreviews() {
			var streams = paella.initDelegate.initParams.videoLoader.streams;
			var slavePreviewImg = null;
	
			var masterPreviewImg = streams[0].preview;
			if (streams.length >=2) {
				slavePreviewImg = streams[1].preview;
			}
			if (masterPreviewImg) {
				var masterRect = paella.player.videoContainer.overlayContainer.getVideoRect(0);
				this.masterPreviewElem = document.createElement('img');
				this.masterPreviewElem.src = masterPreviewImg;
				paella.player.videoContainer.overlayContainer.addElement(this.masterPreviewElem,masterRect);
			}
			if (slavePreviewImg) {
				var slaveRect = paella.player.videoContainer.overlayContainer.getVideoRect(1);
				this.slavePreviewElem = document.createElement('img');
				this.slavePreviewElem.src = slavePreviewImg;
				paella.player.videoContainer.overlayContainer.addElement(this.slavePreviewElem,slaveRect);
			}
			paella.events.bind(paella.events.timeUpdate,function(event) {
				paella.player.unloadPreviews();
			});
		}
	
		unloadPreviews() {
			if (this.masterPreviewElem) {
				paella.player.videoContainer.overlayContainer.removeElement(this.masterPreviewElem);
				this.masterPreviewElem = null;
			}
			if (this.slavePreviewElem) {
				paella.player.videoContainer.overlayContainer.removeElement(this.slavePreviewElem);
				this.slavePreviewElem = null;
			}
		}
	
		loadComplete(event,params) {
			var thisClass = this;
	
			//var master = paella.player.videoContainer.masterVideo();

			paella.pluginManager.loadPlugins("paella.EarlyLoadPlugin");
			if (paella.player.videoContainer._autoplay){
				this.play();
			}		
		}
	
		play() {
			if (!this.videoContainer) {
				// play() is called from lazyLoadContainer
				this.lazyLoadContainer.destroyElements();
				this.lazyLoadContainer = null;
				this._onPlayClosure && this._onPlayClosure();
			}
			else if (this.lazyLoadContainer) {
				// play() has been called by a user interaction
				document.body.removeChild(this.lazyLoadContainer.domElement);
				this.lazyLoadContainer = null;
			}

			if (this.videoContainer) {
				return new Promise((resolve,reject) => {
					this.videoContainer.play()
						.then(() => {
							if (paella.initDelegate.initParams.disableUserInterface()) {
								resolve();
							}
							else if (!this.controls) {
								if (!this.controls) {
									this.showPlaybackBar();
									paella.events.trigger(paella.events.controlBarLoaded);
									this.controls.onresize();
								}
								resolve();
							}
						})
						.catch((err) => {
							reject(err);
						});
				});
			}
		}
	
		pause() {
			return this.videoContainer.pause();
		}
	
		playing() {
			return new Promise((resolve) => {
				this.paused()
					.then((p) => {
						resolve(!p);
					});
			});
		}
	
		paused() {
			return this.videoContainer.paused();
		}
	}
	
	paella.PaellaPlayer = PaellaPlayer;
	window.PaellaPlayer = PaellaPlayer;
	
	paella.PaellaPlayer.mode = {
		standard: 'standard',
		fullscreen: 'fullscreen',
		embed: 'embed'
	};

	class LazyThumbnailContainer extends paella.DomNode {

		static GetIconElement() {
			let container = document.createElement('div');
			container.className = "play-button-on-screen";
			container.style.width = "100%";
			container.style.height = "100%";
			container.style.pointerEvents = "none";
		
			let icon = document.createElement('div');
			icon['className'] = 'play-icon';
			container.appendChild(icon);

			return container;
		}


		constructor(src) {
			super('img','lazyLoadThumbnailContainer',{});
			let url = new paella.URL(src);
			if (!url.isAbsolute) {
				url = (new paella.URL(paella.player.repoUrl))
					.appendPath(paella.player.videoIdentifier)
					.appendPath(src);
			}
			this.domElement.src = url.absoluteUrl;
			this.domElement.alt = "";

			this.container = LazyThumbnailContainer.GetIconElement();
			if (!paella.player.videoContainer) {
				document.body.appendChild(this.container);
			}
		}

		setImage(url) {
			this.domElement.src = url;
		}

		onClick(closure) {
			this.domElement.onclick = closure;
		}

		destroyElements() {
			document.body.removeChild(this.domElement);
			document.body.removeChild(this.container);
		}
	}

	paella.LazyThumbnailContainer = LazyThumbnailContainer;

	
	let g_lazyLoadInstance = null;
	class PaellaPlayerLazy extends PaellaPlayer {
		constructor(playerId,initDelegate) {
			super(playerId,initDelegate);
			g_lazyLoadInstance = this;
		}

		set onPlay(closure) {
			this._onPlayClosure = closure;

		}

		loadComplete(event,params) {
		}

		onLoadConfig(configData) {
			//paella.data = new paella.Data(configData);
	
			this.config = configData;
			this.videoIdentifier = paella.initDelegate.getId();
	
			if (this.videoIdentifier) {
				$(window).resize(function(event) { paella.player.onresize(); });
				this.onload();
			}
		}

		loadVideo() {
			if (this.videoIdentifier) {
				var This = this;
				var loader = paella.player.videoLoader;
				this.onresize();
				loader.loadVideo(() => {
					if (!loader.metadata.preview) {
						paella.load(this.playerId,paella.loaderFunctionParams);
						g_lazyLoadInstance = null;	// Lazy load is disabled when the video has no preview
					}
					else {
						this.lazyLoadContainer = new LazyThumbnailContainer(loader.metadata.preview);
						document.body.appendChild(this.lazyLoadContainer.domElement);
						this.lazyLoadContainer.onClick(() => {
							this.lazyLoadContainer.destroyElements();
							this.lazyLoadContainer = null;
							this._onPlayClosure && this._onPlayClosure();
						});
						paella.events.trigger(paella.events.loadComplete);
					}
				});
			}
		}

		onresize() {}
	}

	paella.PaellaPlayerLazy = PaellaPlayerLazy;
	
	/* Initializer function */
	window.initPaellaEngage = function(playerId,initDelegate) {
		if (!initDelegate) {
			initDelegate = new paella.InitDelegate();
		}
		paella.initDelegate = initDelegate;
		paellaPlayer = new PaellaPlayer(playerId,paella.initDelegate);
	}
})();

/*  
	Paella HTML 5 Multistream Player
	Copyright (C) 2017  Universitat Politècnica de València Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
*/

(() => {

// Default Video Loader
//
class DefaultVideoLoader extends paella.VideoLoader {
	
	constructor(data) {
		super(data);
		this._url = null;
		if (typeof(data)=="object") {
			this._data = data;
		}
		else {
			try {
				this._data = JSON.parse(data);
			}
			catch (e) {
				this._url = data;
			}
		}
	}

	getVideoUrl() {
		if (paella.initDelegate.initParams.videoUrl) {
			return typeof(paella.initDelegate.initParams.videoUrl)=="function" ?
				paella.initDelegate.initParams.videoUrl() :
				paella.initDelegate.initParams.videoUrl;
		}
		else {
			let url = this._url || (paella.player.config.standalone && paella.player.config.standalone.repository) || '';
			return (/\/$/.test(url) ? url:url + '/') + paella.initDelegate.getId() + '/';
		}
	}

	getDataUrl() {
		if (paella.initDelegate.initParams.dataUrl) {
			return typeof(paella.initDelegate.initParams.dataUrl)=='function' ?
				paella.initDelegate.initParams.dataUrl() :
				paella.initDelegate.initParams.dataUrl;
		}
		else {
			return this.getVideoUrl() + 'data.json';
		}
	}

	loadVideo(onSuccess) {
		let loadVideoDelegate = paella.initDelegate.initParams.loadVideo;
		let url = this._url || this.getDataUrl();

		if (this._data) {
			this.loadVideoData(this._data, onSuccess);
		}
		else if (loadVideoDelegate) {
			loadVideoDelegate().then((data) => {
				this._data = data;
				this.loadVideoData(this._data, onSuccess);
			});
		}
		else if (url) {
			var This = this;
			paella.utils.ajax.get({ url:this.getDataUrl() },
				function(data,type,err) {
					if (typeof(data)=="string") {
						try {
							data = JSON.parse(data);
						}
						catch(e) {}
					}
					This._data = data;
					This.loadVideoData(This._data,onSuccess);
				},
				function(data,type,err) {
					switch (err) {
					case 401:
						paella.messageBox.showError(paella.utils.dictionary.translate("You are not logged in"));
						break;
					case 403:
						paella.messageBox.showError(paella.utils.dictionary.translate("You are not authorized to view this resource"));
						break;
					case 404:
						paella.messageBox.showError(paella.utils.dictionary.translate("The specified video identifier does not exist"));
						break;
					default:
						paella.messageBox.showError(paella.utils.dictionary.translate("Could not load the video"));
					}
				});
		}
	}

	loadVideoData(data,onSuccess) {
		var This = this;
		if (data.metadata) {
			this.metadata = data.metadata;
		}

		if (data.streams) {
			data.streams.forEach(function(stream) {
				This.loadStream(stream);
			});
		}
		if (data.frameList) {
			this.loadFrameData(data);
		}
		if (data.captions) {
			this.loadCaptions(data.captions);
		}
		if (data.blackboard) {
			this.loadBlackboard(data.streams[0],data.blackboard);
		}
		this.streams = data.streams;
		this.frameList = data.frameList;
		this.loadStatus = this.streams.length>0;
		onSuccess();
	}

	loadFrameData(data) {
		var This = this;
		if (data.frameList && data.frameList.forEach) {
			var newFrames = {};
			data.frameList.forEach(function(frame) {
				if (! /^[a-zA-Z]+:\/\//.test(frame.url) && !/^data:/.test(frame.url)) {
					frame.url = This.getVideoUrl() + frame.url;
				}
				if (frame.thumb && ! /^[a-zA-Z]+:\/\//.test(frame.thumb) && !/^data:/.test(frame.thumb)) {
					frame.thumb = This.getVideoUrl() + frame.thumb;
				}
				var id = frame.time;
				newFrames[id] = frame;

			});
			data.frameList = newFrames;
		}
	}

	loadStream(stream) {
		var This=this;
		if (stream.preview && ! /^[a-zA-Z]+:\/\//.test(stream.preview) && !/^data:/.test(stream.preview)) {
			stream.preview = This.getVideoUrl() + stream.preview;
		}

		if (!stream.sources) {
			return;
		}

		if (stream.sources.image) {
			stream.sources.image.forEach(function(image) {
				if (image.frames.forEach) {
					var newFrames = {};
					image.frames.forEach(function(frame) {
						if (frame.src && ! /^[a-zA-Z]+:\/\//.test(frame.src) && !/^data:/.test(frame.src)) {
							frame.src = This.getVideoUrl() + frame.src;
						}
						if (frame.thumb && ! /^[a-zA-Z]+:\/\//.test(frame.thumb) && !/^data:/.test(frame.thumb)) {
							frame.thumb = This.getVideoUrl() + frame.thumb;
						}
						var id = "frame_" + frame.time;
						newFrames[id] = frame.src;
					});
					image.frames = newFrames;
				}
			});
		}
		for (var type in stream.sources) {
			if (stream.sources[type]) {
				if (type != 'image') {
					var source = stream.sources[type];
					source.forEach(function (sourceItem) {
						var pattern = /^[a-zA-Z\:]+\:\/\//gi;
						if (typeof(sourceItem.src)=="string") {
							if(sourceItem.src.match(pattern) == null){
								sourceItem.src = This.getVideoUrl() + sourceItem.src;
							}
						}
						sourceItem.type = sourceItem.mimetype;
					});
				}
			}
			else {
				delete stream.sources[type];
			}
		}
	}

	loadCaptions(captions) {
		if (captions) {
			for (var i=0; i<captions.length; ++i) {
				var url = captions[i].url;

				if (! /^[a-zA-Z]+:\/\//.test(url)) {
					url = this.getVideoUrl() + url;
				}
				var c = new paella.captions.Caption(i, captions[i].format, url, {code: captions[i].lang, txt: captions[i].text});
				paella.captions.addCaptions(c);
			}
		}
	}

	loadBlackboard(stream, blackboard) {
		var This = this;
		if (!stream.sources.image) {
			stream.sources.image = [];
		}
		var imageObject = {
			count: blackboard.frames.length,
			duration: blackboard.duration,
			mimetype: blackboard.mimetype,
			res: blackboard.res,
			frames: {}
		};

		blackboard.frames.forEach(function(frame) {
			var id = "frame_" + Math.round(frame.time);
			if (!/^[a-zA-Z]+:\/\//.test(frame.src)) {
				frame.src = This.getVideoUrl() + frame.src;
			}
			imageObject.frames[id] = frame.src;
		});

		stream.sources.image.push(imageObject);
	}
}

paella.DefaultVideoLoader = DefaultVideoLoader;

class DefaultInitDelegate extends paella.InitDelegate {
}

paella.DefaultInitDelegate = DefaultInitDelegate;

function getManifestFromParameters(params) {
	let master = null;
	if (master = paella.utils.parameters.get('video')) {
		let slave = paella.utils.parameters.get('videoSlave');
		slave = slave && decodeURIComponent(slave);
		let masterPreview = paella.utils.parameters.get('preview');
		masterPreview = masterPreview && decodeURIComponent(masterPreview);
		let slavePreview = paella.utils.parameters.get('previewSlave');
		slavePreview = slavePreview && decodeURIComponent(slavePreview);
		let title = paella.utils.parameters.get('title') || "Untitled Video";
		
		let data = {
			metadata: {
				title: title
			},
			streams: [
				{
					sources: {
						mp4: [
							{
								src:decodeURIComponent(master),
								mimetype:"video/mp4",
								res:{ w:0, h:0 }
							}
						]
					},
					preview:masterPreview,
					type: "video",
					content: "presenter"
				}
			],
			frameList: []
		}

		if (slave) {
			data.streams.push({
				sources: {
					mp4: [
						{
							src:slave,
							mimetype:"video/mp4",
							res:{ w:0, h:0 }
						} 
					]
				},
				preview:slavePreview,
				type: "video",
				content: "presentation"
			});
		}

		return data;
	}
	return null;
}

/*
 *	playerContainer	Player DOM container id
 *	params.configUrl		Url to the config json file
 *	params.config			Use this configuration file
 *	params.data				Paella video data schema
 *	params.url				Repository URL
 */
paella.load = function(playerContainer, params) {
	paella.loaderFunctionParams = params;
	var auth = (params && params.auth) || {};

	// Build custom init data using url parameters
	let data = getManifestFromParameters(params);
	if (data) {
		params.data = data;
	}

	var initObjects = params;
	initObjects.videoLoader = new paella.DefaultVideoLoader(params.data || params.url);

	paella.initDelegate = new paella.DefaultInitDelegate(initObjects);
	new PaellaPlayer(playerContainer,paella.initDelegate);
};

/*
 *	playerContainer	Player DOM container id
 *	params.configUrl		Url to the config json file
 *	params.config			Use this configuration file
 *	params.data				Paella video data schema
 *	params.url				Repository URL
 *  forceLazyLoad			Use lazyLoad even if your browser does not allow automatic playback of the video
 */
paella.lazyLoad = function(playerContainer, params, forceLazyLoad = true) {
	paella.loaderFunctionParams = params;
	var auth = (params && params.auth) || {};

	// Check autoplay. If autoplay is enabled, this function must call paella.load()
	paella.Html5Video.IsAutoplaySupported()
		.then((supported) => {
			let disableUI = /true/i.test(paella.utils.parameters.get("disable-ui"));
			if ((supported || forceLazyLoad) && !disableUI) {
				// Build custom init data using url parameters
				let data = getManifestFromParameters(params);
				if (data) {
					params.data = data;
				}

				var initObjects = params;
				initObjects.videoLoader = new paella.DefaultVideoLoader(params.data || params.url);

				paella.initDelegate = new paella.DefaultInitDelegate(initObjects);
				let lazyLoad = new paella.PaellaPlayerLazy(playerContainer,paella.initDelegate);
				lazyLoad.onPlay = () => {
					$('#' + playerContainer).innerHTML = "";
					paella.load(playerContainer,params);
				};
			}
			else {
				paella.load(playerContainer,params);
			}
		});
}

})();


/*  
	Paella HTML 5 Multistream Player
	Copyright (C) 2017  Universitat Politècnica de València Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
*/

(() => {

	class RightBarPlugin extends paella.DeferredLoadPlugin {
		get type() { return 'rightBarPlugin'; }
		getName() { return "es.upv.paella.RightBarPlugin"; }
	
		buildContent(domElement) {}
	}

	paella.RightBarPlugin = RightBarPlugin;
	
	class TabBarPlugin extends paella.DeferredLoadPlugin {
		get type() { return 'tabBarPlugin'; }
		getName() { return "es.upv.paella.TabBarPlugin"; }
	
		getTabName() {
			return "New Tab";
		}
	
		action(tab) {
		}
	
		buildContent(domElement) {
		}
		
		setToolTip(message) {
			this.button.setAttribute("title", message);
			this.button.setAttribute("aria-label", message);
		}
	
		getDefaultToolTip() {
			return "";
		}
	}

	paella.TabBarPlugin = TabBarPlugin;
	
	
	class ExtendedAdapter {
		
		constructor() {
			this.rightContainer = null;
			this.bottomContainer = null;
			this.rightBarPlugins = [];
			this.tabBarPlugins = [];
			this.currentTabIndex = 0;
			this.bottomContainerTabs = null;
			this.bottomContainerContent = null;
	
			this.rightContainer = document.createElement('div');
			//this.rightContainer.id = this.settings.rightContainerId;
			this.rightContainer.className = "rightPluginContainer";
	
			this.bottomContainer = document.createElement('div');
			//this.bottomContainer.id = this.settings.bottomContainerId;
			this.bottomContainer.className = "tabsPluginContainer";
	
			var tabs = document.createElement('div');
			//tabs.id = 'bottomContainer_tabs';
			tabs.className = 'tabsLabelContainer';
			this.bottomContainerTabs = tabs;
			this.bottomContainer.appendChild(tabs);
	
			var bottomContent = document.createElement('div');
			//bottomContent.id = 'bottomContainer_content';
			bottomContent.className = 'tabsContentContainer';
			this.bottomContainerContent = bottomContent;
			this.bottomContainer.appendChild(bottomContent);
	
	
			this.initPlugins();
		}
	
		initPlugins() {
			paella.pluginManager.setTarget('rightBarPlugin', this);
			paella.pluginManager.setTarget('tabBarPlugin', this);
		}
	
		addPlugin(plugin) {
			var thisClass = this;
			plugin.checkEnabled(function(isEnabled) {
				if (isEnabled) {
					paella.pluginManager.setupPlugin(plugin);
					if (plugin.type=='rightBarPlugin') {
						thisClass.rightBarPlugins.push(plugin);
						thisClass.addRightBarPlugin(plugin);
					}
					if (plugin.type=='tabBarPlugin') {
						thisClass.tabBarPlugins.push(plugin);
						thisClass.addTabPlugin(plugin);
					}
				}
			});
		}
	
		showTab(tabIndex) {
			var i =0;
			var labels = this.bottomContainer.getElementsByClassName("tabLabel");
			var contents = this.bottomContainer.getElementsByClassName("tabContent");
		
			for (i=0; i < labels.length; ++i) {
				if (labels[i].getAttribute("tab") == tabIndex) {
					labels[i].className = "tabLabel enabled";
				}
				else {
					labels[i].className = "tabLabel disabled";
				}
			}
			
			for (i=0; i < contents.length; ++i) {
				if (contents[i].getAttribute("tab") == tabIndex) {
					contents[i].className = "tabContent enabled";
				}
				else {
					contents[i].className = "tabContent disabled";
				}
			}
		}
	
		addTabPlugin(plugin) {
			var thisClass = this;
			var tabIndex = this.currentTabIndex;
	
			// Add tab
			var tabItem = document.createElement('div');
			tabItem.setAttribute("tab", tabIndex);
			tabItem.className = "tabLabel disabled";		
			tabItem.innerText = plugin.getTabName();
			tabItem.plugin = plugin;
			$(tabItem).click(function(event) { if (/disabled/.test(this.className)) { thisClass.showTab(tabIndex); this.plugin.action(this); } });
			$(tabItem).keyup(function(event) {
				if (event.keyCode == 13) {
					if (/disabledTabItem/.test(this.className)) { thisClass.showTab(tabIndex); this.plugin.action(this); }
				}
			});
			this.bottomContainerTabs.appendChild(tabItem);
	
			// Add tab content
			var tabContent = document.createElement('div');
			tabContent.setAttribute("tab", tabIndex);
			tabContent.className = "tabContent disabled " + plugin.getSubclass();
			this.bottomContainerContent.appendChild(tabContent);
			plugin.buildContent(tabContent);
	
			plugin.button = tabItem;
			plugin.container = tabContent;
	
			plugin.button.setAttribute("tabindex", 3000+plugin.getIndex());
			plugin.button.setAttribute("alt", "");
			plugin.setToolTip(plugin.getDefaultToolTip());
	
	
			// Show tab
			if (this.firstTabShown===undefined) {
				this.showTab(tabIndex);
				this.firstTabShown = true;
			}
			++this.currentTabIndex;
		}
	
		addRightBarPlugin(plugin) {
			var container = document.createElement('div');
			container.className = "rightBarPluginContainer " + plugin.getSubclass();
			this.rightContainer.appendChild(container);
			plugin.buildContent(container);
		}
	}

	paella.ExtendedAdapter = ExtendedAdapter;
	
	
	paella.extendedAdapter = new paella.ExtendedAdapter();
})();


try {
    module.export = paella;
} catch(e) {}

/*
Plugin override: PlayButtonOnScreen

Display always the big play button on the player, no matter the
stream is live or not.
*/
paella.addPlugin(function() {
	class PlayButtonOnScreenAlwaysPlugin extends paella.EventDrivenPlugin {
		getName() { return "ch.cern.paella.playButtonOnScreenAlwaysPlugin"; }
		getIndex() { return 1022; }

		constructor() {
			super();
			this.containerId = 'paella_plugin_PlayButtonOnScreen_Always';
			this.container = null;
			this.enabled = true;
			this.isPlaying = false;
			this.showIcon = true;
			this.firstPlay = false;
		}

		setup() {
			this.container = paella.LazyThumbnailContainer.GetIconElement();
			paella.player.videoContainer.domElement.appendChild(this.container);
			$(this.container).click(() =>  this.onPlayButtonClick());
		}

		checkEnabled(onSuccess) {
			this.showOnEnd = true;
			paella.data.read('relatedVideos', {id:paella.player.videoIdentifier}, (data) => {
                this.showOnEnd = !Array.isArray(data) ||  data.length == 0;
			});
			onSuccess(true);
		}

		getEvents() {
			return [paella.events.endVideo,paella.events.play,paella.events.pause,paella.events.showEditor,paella.events.hideEditor];
		}
		onEvent(eventType,params) {
			switch (eventType) {
				case paella.events.endVideo:
					this.endVideo();
					break;
				case paella.events.play:
					this.play();
					break;
				case paella.events.pause:
					this.pause();
					break;
				case paella.events.showEditor:
					this.showEditor();
					break;
				case paella.events.hideEditor:
					this.hideEditor();
					break;
			}
		}

		onPlayButtonClick() {
			this.firstPlay = true;
			this.checkStatus();
		}

		endVideo() {
			this.isPlaying = false;
			this.showIcon = this.showOnEnd;
			this.checkStatus();
		}

		play() {
			this.isPlaying = true;
			this.showIcon = false;
			this.checkStatus();
		}

		pause() {
			this.isPlaying = false;
			this.showIcon = true;
			this.checkStatus();
		}

		showEditor() {
			this.enabled = false;
			this.checkStatus();
		}

		hideEditor() {
			this.enabled = true;
			this.checkStatus();
		}

		checkStatus() {
			if ((this.enabled && this.isPlaying) || !this.enabled || !this.showIcon) {
				$(this.container).hide();
			}
			// Only show play button if none of the video players require mouse events
			else if (!paella.player.videoContainer.streamProvider.videoPlayers.every((p) => p.canvasData.mouseEventsSupport)) {
				$(this.container).show();
			}
	 }
	}

	paella.plugins.PlayButtonOnScreenAlwaysPlugin = PlayButtonOnScreenAlwaysPlugin;

	return PlayButtonOnScreenAlwaysPlugin;
});
paella.addPlugin(function() {
	class FlexSkipPlugin extends paella.ButtonPlugin {
		getAlignment() { return 'left'; }
		getName() { return "edu.harvard.dce.paella.flexSkipPlugin"; }
		getIndex() { return 121; }
		getSubclass() { return 'flexSkip_Rewind_10'; }
		getIconClass() { return 'icon-back-10-s'; }
		formatMessage() { return 'Rewind 10 seconds'; }
		getDefaultToolTip() { return paella.utils.dictionary.translate(this.formatMessage()); }
	
		checkEnabled(onSuccess) {
			onSuccess(!paella.player.isLiveStream());
		}
		
		action(button) {
			paella.player.videoContainer.currentTime()
				.then(function(currentTime) {
					paella.player.videoContainer.seekToTime(currentTime - 10);
				});
		}
	}

	paella.plugins.FlexSkipPlugin = FlexSkipPlugin;

	return FlexSkipPlugin;
});

paella.addPlugin(function() {

	return class FlexSkipForwardPlugin extends paella.plugins.FlexSkipPlugin {
		getIndex() { return 122; }
		getName() { return "edu.harvard.dce.paella.flexSkipForwardPlugin"; }
		getSubclass() { return 'flexSkip_Forward_30'; }
		getIconClass() { return 'icon-forward-30-s'; }
		formatMessage() { return 'Forward 30 seconds'; }
		
		action(button) {
			paella.player.videoContainer.currentTime()
				.then(function(currentTime) {
					paella.player.videoContainer.seekToTime(currentTime + 30);
				});
		}
	}
});

paella.addPlugin(function () {
    /////////////////////////////////////////////////
    // WebVTT Parser
    /////////////////////////////////////////////////
    return class WebVTTParserPlugin extends paella.CaptionParserPlugIn {
        get ext() { return ["vtt"] }
        getName() { return "es.teltek.paella.captions.WebVTTParserPlugin"; }

        parse(content, lang, next) {
            var captions = [];
            var self = this;
            var lls = content.split("\n");
            var c;
            var id = 0;
            var skip = false;
            for (var idx = 0; idx < lls.length; ++idx) {
                var ll = lls[idx].trim();
                if ((/^WEBVTT/.test(ll) && c === undefined) || ll.length === 0) {
                    continue;
                }
                if ((/^[0-9]+$/.test(ll) || /^[0-9]+ -/.test(ll)) && lls[idx - 1].trim().length === 0) {
                    continue;
                }
                if (/^NOTE/.test(ll) || /^STYLE/.test(ll)) {
                    skip = true;
                    continue;
                }
                if (/^(([0-9]+:)?[0-9]{2}:[0-9]{2}.[0-9]{3} --> ([0-9]+:)?[0-9]{2}:[0-9]{2}.[0-9]{3})/.test(ll)) {
                    skip = false;
                    if (c != undefined) {
                        captions.push(c);
                        id++;
                    }
                    c = {
                        id: id,
                        begin: self.parseTimeTextToSeg(ll.split("-->")[0]),
                        end: self.parseTimeTextToSeg(ll.split("-->")[1]),
                    }
                    continue;
                }
                if (c !== undefined && !skip) {
                    ll = ll.replace(/^- /, "");
                    ll = ll.replace(/<[^>]*>/g, "");
                    if (c.content === undefined) {
                        c.content = ll;
                    } else {
                        c.content += "\n" + ll;
                    }
                }
            }
            captions.push(c);
            if (captions.length > 0) {
                next(false, captions);
            } else {
                next(true);
            }
        }

        parseTimeTextToSeg(ttime) {
            var nseg = 0;
            var factor = 1;
            ttime = /(([0-9]+:)?[0-9]{2}:[0-9]{2}.[0-9]{3})/.exec(ttime);
            var split = ttime[0].split(":");
            for (var i = split.length - 1; i >= 0; i--) {
                factor = Math.pow(60, (split.length - 1 - i));
                nseg += split[i] * factor;
            }
            return nseg;
        }
    }
})

paella.addPlugin(function() {
  return class xAPISaverPlugin extends paella.userTracking.SaverPlugIn {
    getName() {return "es.teltek.paella.usertracking.xAPISaverPlugin";}

    setup(){
      this.endpoint = this.config.endpoint;
      this.auth = this.config.auth;
      this.user_info = {}
      this.paused = true
      this.played_segments = ""
      this.played_segments_segment_start = null
      this.played_segments_segment_end = null
      this.progress = 0
      this.duration = 0
      this.current_time = []
      this.completed = false
      this.volume = null
      this.speed = null
      this.language = "us-US"
      this.quality = null
      this.fullscreen = false
      this.title = "No title available"
      this.description = ""
      this.user_agent = ""
      this.total_time = 0
      this.total_time_start = 0
      this.total_time_end = 0
      this.session_id = ""

      let self = this
      this._loadDeps().then(function (){
        let conf = {
          "endpoint" : self.endpoint,
          "auth" : "Basic " + toBase64(self.auth)
        };
        ADL.XAPIWrapper.changeConfig(conf);
      })
      paella.events.bind(paella.events.timeUpdate, function(event,params){
        self.current_time.push(params.currentTime)
        if (self.current_time.length >=10){
          self.current_time = self.current_time.slice(-10)
        }

        var a = Math.round(self.current_time[0])
        var b = Math.round(self.current_time[9])

        if ((params.currentTime !== 0)  && (a + 1 >= b) && (b - 1 >= a)){
          self.progress = self.get_progress(params.currentTime, params.duration)
          if (self.progress >= 0.95 && self.completed === false){
            self.completed = true
            self.end_played_segment(params.currentTime)
            self.start_played_segment(params.currentTime)
            self.send_completed(params.currentTime, self.progress)
          }
        }
      })
    }

    get_session_data(){
      var myparams = ADL.XAPIWrapper.searchParams();
      var agent = JSON.stringify({"mbox" : this.user_info.email})
      var timestamp = new Date()
      timestamp.setDate(timestamp.getDate() - 1);
      timestamp = timestamp.toISOString()
      myparams['activity'] = window.location.href;
      myparams['verb'] = 'http://adlnet.gov/expapi/verbs/terminated';
      myparams['since'] = timestamp
      myparams['limit']	= 1;
      myparams['agent'] = agent
      var ret = ADL.XAPIWrapper.getStatements(myparams);
      if (ret.statements.length === 1){
        this.played_segments = ret.statements[0].result.extensions['https://w3id.org/xapi/video/extensions/played-segments']
        this.progress = ret.statements[0].result.extensions['https://w3id.org/xapi/video/extensions/progress']
        ADL.XAPIWrapper.lrs.registration = ret.statements[0].context.registration
      }
      else{
        ADL.XAPIWrapper.lrs.registration = ADL.ruuid()
      }
    }

    getCookie(cname) {
      var name = cname + "=";
      var decodedCookie = decodeURIComponent(document.cookie);
      var ca = decodedCookie.split(';');
      for(var i = 0; i <ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
          c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
          return c.substring(name.length, c.length);
        }
      }
      return "";
    }

    setCookie(cname, cvalue, exdays) {
      var d = new Date();
      d.setTime(d.getTime() + (exdays*24*60*60*1000));
      var expires = "expires="+ d.toUTCString();
      document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
    }

    checkCookie(){
      var user_info = this.getCookie("user_info");
      if (user_info === "") {
        user_info = JSON.stringify(generateName())
      }
      this.setCookie("user_info", user_info);
      return JSON.parse(user_info)
    }

    checkEnabled(onSuccess) {
      this._url = this.config.url;
      this._index = this.config.index || "paellaplayer";
      this._type = this.config.type || "usertracking";

      onSuccess(true);
    }

    _loadDeps() {
      return new Promise((resolve,reject) => {
        paella.require('resources/deps/xapiwrapper.min.js')
          .then(() => {
            return paella.require('resources/deps/random_name_generator.js')
          })
          .then(() => {
            resolve();
          });
      });
    }

    log(event, params) {
      var p = params;
      let self = this
      // console.log(event)
      // console.log(params)
      switch (event) {
        //Retrieve initial parameters from player
        case "paella:loadComplete":
        this.user_agent = navigator.userAgent.toString();
        this.get_title()
        this.get_description()
        paella.player.videoContainer.duration().then(function(duration) {
          return paella.player.videoContainer.streamProvider.mainAudioPlayer.volume()
          .then(function(volume) {
            return paella.player.videoContainer.getCurrentQuality().then(function(quality) {
              return paella.player.auth.userData().then(function (user_info){
                self.duration = duration
                self.volume = volume
                self.speed = 1
                if (paella.player.videoContainer.streamProvider.mainAudioPlayer.stream.language){
                  self.language = paella.player.videoContainer.streamProvider.mainAudioPlayer.stream.language.replace("_","-")
                }
                self.quality = quality.shortLabel()

                if (user_info.email && user_info.name){
                  self.user_info = user_info
                }
                else{
                  self.user_info = self.checkCookie()
                }

                self.get_session_data()

                self.send_initialized()
              });
            });
          });
        });
        window.onbeforeunload = function(e) {
          if (!self.paused){
            self.send_pause(self)
          }
          //TODO Algunas veces se envia terminated antes que paused
          self.send_terminated(self)
          // var dialogText = 'Dialog text here';
          // e.returnValue = dialogText;
          // window.onbeforeunload = null;
          // return dialogText;
        };
        break;
        case "paella:play":
        this.send_play(self)
        break;
        case "paella:pause":
        this.send_pause(self)
        break;
        case "paella:seektotime":
        this.send_seek(self, params)
        break;
        //Player options
        case "paella:setVolume":
        paella.player.videoContainer.currentTime()
        .then(function(currentTime) {
          var current_time = self.format_float(currentTime)
          self.volume = params.master
          //self.send_interacted(current_time, {"https://w3id.org/xapi/video/extensions/volume": params.master})
          var interacted = {"https://w3id.org/xapi/video/extensions/volume": self.format_float(params.master)}
          self.send_interacted(current_time, interacted)

        });
        break;
        case "paella:setplaybackrate":
        paella.player.videoContainer.currentTime()
        .then(function(currentTime) {
          var current_time = self.format_float(currentTime)
          self.speed = params.rate
          var interacted = {"https://w3id.org/xapi/video/extensions/speed": params.rate + "x"}
          self.send_interacted(current_time, interacted)
        })
        break;
        case "paella:qualityChanged":
        paella.player.videoContainer.currentTime().then(function(currentTime) {
          return paella.player.videoContainer.getCurrentQuality().then(function(quality) {
            self.quality = quality.shortLabel()
            var current_time = self.format_float(currentTime)
            var interacted = {"https://w3id.org/xapi/video/extensions/quality": quality.shortLabel()}
            self.send_interacted(current_time, interacted)
          })
        })
        break;
        case "paella:enterFullscreen":
        case "paella:exitFullscreen":
        paella.player.videoContainer.currentTime().then(function(currentTime) {
          var current_time = self.format_float(currentTime)
          self.fullscreen ? self.fullscreen = false : self.fullscreen = true
          var interacted = {"https://w3id.org/xapi/video/extensions/full-screen": self.fullscreen}
          self.send_interacted(current_time, interacted)
        })
        break;
        default:
        break;
      }
    }

    send(params){
      var agent = new ADL.XAPIStatement.Agent(this.user_info.email, this.user_info.name)
      var verb = new ADL.XAPIStatement.Verb(params.verb.id, params.verb.description)
      var activity = new ADL.XAPIStatement.Activity(window.location.href, this.title, this.description)
      activity.definition.type = "https://w3id.org/xapi/video/activity-type/video"
      paella.player.videoContainer.streamProvider.mainAudioPlayer.volume().then(function(volume){})
      var statement = new ADL.XAPIStatement(agent, verb, activity)
      statement.result = params.result
      if (params.verb.id === "http://adlnet.gov/expapi/verbs/initialized"){
        statement.generateId()
        this.session_id = statement.id
      }


      var ce_base = {
        "https://w3id.org/xapi/video/extensions/session-id": this.session_id,
        "https://w3id.org/xapi/video/extensions/length": Math.floor(this.duration),
        "https://w3id.org/xapi/video/extensions/user-agent": this.user_agent
      }
      var ce_interactions = {
        "https://w3id.org/xapi/video/extensions/volume": this.format_float(this.volume),
        "https://w3id.org/xapi/video/extensions/speed": this.speed + "x",
        "https://w3id.org/xapi/video/extensions/quality": this.quality,
        "https://w3id.org/xapi/video/extensions/full-screen": this.fullscreen
      }
      var context_extensions = {}
      if (params.interacted){
        context_extensions = $.extend({}, ce_base, params.interacted)
      }
      else{
        context_extensions = $.extend({}, ce_base, ce_interactions)
      }

      statement.context = {
        "language": this.language,
        "extensions": context_extensions,
        "contextActivities":{
          "category":[{
            "objectType":"Activity",
            "id":"https://w3id.org/xapi/video"
          }]
        }
      }

      // Dispatch the statement to the LRS
      var result = ADL.XAPIWrapper.sendStatement(statement);
    }

    send_initialized() {
      var statement = {
        "verb":{
          "id":"http://adlnet.gov/expapi/verbs/initialized",
          "description":"initalized"
        },
      }
      this.send(statement)
    }

    send_terminated(self) {
      paella.player.videoContainer.currentTime()
      .then(function(end_time) {
        var statement = {
          "verb":{
            "id":"http://adlnet.gov/expapi/verbs/terminated",
            "description":"terminated"
          },
          "result" : {
            "extensions":{
              "https://w3id.org/xapi/video/extensions/time" : end_time,
              "https://w3id.org/xapi/video/extensions/progress": self.progress,
              "https://w3id.org/xapi/video/extensions/played-segments": self.played_segments
            }
          }
        }
        self.send(statement)
      })
    }

    send_play(self){
      this.paused = false
      this.total_time_start = new Date().getTime() / 1000;
      paella.player.videoContainer.currentTime()
      .then(function(currentTime) {
        var start_time = self.format_float(currentTime)
        //When the video starts we force start_time to 0
        if (start_time <= 1){
          start_time = 0
        }
        self.start_played_segment(start_time)
        var statement = {
          "verb":{
            "id":"https://w3id.org/xapi/video/verbs/played",
            "description":"played"
          },
          "result" : {
            "extensions":{
              "https://w3id.org/xapi/video/extensions/time" : start_time
            }
          }
        }
        self.send(statement)
      });
    }

    send_pause(self){
      this.paused = true
      this.total_time_end = new Date().getTime() / 1000;
      this.total_time += (this.total_time_end - this.total_time_start)
      paella.player.videoContainer.currentTime().then(function(currentTime) {
        //return paella.player.videoContainer.duration().then(function(duration) {
        var end_time = self.format_float(currentTime)
        //self.progress = self.get_progress(end_time, duration)
        //If a video end, the player go to the video start and raise a pause event with currentTime = 0
        if (end_time === 0){
          end_time = self.duration
        }
        self.end_played_segment(end_time)
        var statement = {
          "verb":{
            "id":"https://w3id.org/xapi/video/verbs/paused",
            "description":"paused"
          },
          "result" : {
            "extensions":{
              "https://w3id.org/xapi/video/extensions/time" : end_time,
              "https://w3id.org/xapi/video/extensions/progress": self.progress,
              "https://w3id.org/xapi/video/extensions/played-segments": self.played_segments
            }
          }
        }
        self.send(statement)
      });
      //});
    }

    send_seek(self, params){
      var seekedto = this.format_float(params.newPosition)
      //FIXME Metodo para obtener el instante desde donde empieza el seek
      var a = this.current_time.filter(function(value){
        return value <= seekedto -1
      })
      if (a.length === 0){
        a = this.current_time.filter(function(value){
          return value >= seekedto + 1
        })
      }
      //In some cases, when you seek to the end of the video the array contains zeros at the end
      var seekedfrom = a.filter(Number).pop()
      this.current_time = []
      this.current_time.push(seekedto)
      // Fin de FIXME
      //If the video is paused it's not neccesary create a new segment, because the pause event already close a segment
      self.progress = self.get_progress(seekedfrom, self.duration)
      if (!this.paused){
        this.end_played_segment(seekedfrom)
        this.start_played_segment(seekedto)
      }
      //paella.player.videoContainer.duration().then(function(duration) {
      //var progress = self.get_progress(seekedfrom, duration)

      var statement = {
        "verb":{
          "id":"https://w3id.org/xapi/video/verbs/seeked",
          "description":"seeked"
        },
        "result" : {
          "extensions":{
            "https://w3id.org/xapi/video/extensions/time-from" : seekedfrom,
            "https://w3id.org/xapi/video/extensions/time-to": seekedto,
            // Aqui tambien deberiamos de enviar los segmentos reproducidos y el porcentaje
            "https://w3id.org/xapi/video/extensions/progress": self.progress,
            "https://w3id.org/xapi/video/extensions/played-segments": self.played_segments
          }
        }
      }
      self.send(statement)
      //})
    }

    send_completed(time, progress){
      var statement = {
        "verb":{
          "id":"http://adlnet.gov/xapi/verbs/completed",
          "description":"completed"
        },
        "result" : {
          "completion": true,
          "success": true,
          "duration": "PT" + this.total_time +"S",
          "extensions":{
            "https://w3id.org/xapi/video/extensions/time" : time,
            "https://w3id.org/xapi/video/extensions/progress" : progress,
            "https://w3id.org/xapi/video/extensions/played-segments": this.played_segments
          }
        }
      }
      this.send(statement)
    }

    send_interacted(current_time, interacted){
      var statement = {
        "verb":{
          "id":"http://adlnet.gov/expapi/verbs/interacted",
          "description":"interacted"
        },
        "result" : {
          "extensions":{
            "https://w3id.org/xapi/video/extensions/time" : current_time
          }
        },
        "interacted" : interacted
      }
      this.send(statement)
    }

    start_played_segment(start_time){
      this.played_segments_segment_start = start_time;
    }

    end_played_segment(end_time){
      var arr;
      arr = (this.played_segments === "")? []:this.played_segments.split("[,]");
      arr.push(this.played_segments_segment_start + "[.]" + end_time);
      this.played_segments = arr.join("[,]");
      this.played_segments_segment_end = end_time;
      //this.played_segments_segment_start = null;
    }

    format_float(number){
      number = parseFloat(number) //Ensure that number is a float
      return parseFloat(number.toFixed(3))
    }

    get_title(){
      if (paella.player.videoLoader.getMetadata().i18nTitle){
        this.title = paella.player.videoLoader.getMetadata().i18nTitle
      }
      else if (paella.player.videoLoader.getMetadata().title){
        this.title = paella.player.videoLoader.getMetadata().title
      }
    }

    get_description(){
      if (paella.player.videoLoader.getMetadata().i18nTitle){
        this.description = paella.player.videoLoader.getMetadata().i18nDescription
      }
      else {
        this.description = paella.player.videoLoader.getMetadata().description
      }
    }

    get_progress(currentTime, duration){
      var arr, arr2;

      //get played segments array
      arr = (this.played_segments === "")? []:this.played_segments.split("[,]");
      if(this.played_segments_segment_start != null){
        arr.push(this.played_segments_segment_start + "[.]" + currentTime);
      }

      arr2 = [];
      arr.forEach(function(v,i) {
        arr2[i] = v.split("[.]");
        arr2[i][0] *= 1;
        arr2[i][1] *= 1;
      });

      //sort the array
      arr2.sort(function(a,b) { return a[0] - b[0];});

      //normalize the segments
      arr2.forEach(function(v,i) {
        if(i > 0) {
          if(arr2[i][0] < arr2[i-1][1]) { 	//overlapping segments: this segment's starting point is less than last segment's end point.
          //console.log(arr2[i][0] + " < " + arr2[i-1][1] + " : " + arr2[i][0] +" = " +arr2[i-1][1] );
          arr2[i][0] = arr2[i-1][1];
          if(arr2[i][0] > arr2[i][1])
          arr2[i][1] = arr2[i][0];
        }
      }
    });

    //calculate progress_length
    var progress_length = 0;
    arr2.forEach(function(v,i) {
      if(v[1] > v[0])
      progress_length += v[1] - v[0];
    });

    var progress = 1 * (progress_length / duration).toFixed(2);
    return progress;
  }
}});


paella.addPlugin(function() {
    return class SingleStreamProfilePlugin extends paella.EventDrivenPlugin {
        getName() {
            return "es.upv.paella.singleStreamProfilePlugin";
        }

        checkEnabled(onSuccess) {
            let config = this.config;
            config.videoSets.forEach((videoSet,index) => {
                let validContent = videoSet.content
                if (validContent.length==1) {
                    let streamCount = 0;
                    paella.player.videoContainer.streamProvider.videoStreams.forEach((v) => {
                        if (validContent.indexOf(v.content)!=-1) {
                            streamCount++
                        }
                    })
                    if (streamCount>=1) {
                        onSuccess(true);
                        paella.addProfile(() => {
                            return new Promise((resolve,reject) => {
                                resolve({
                                    id:videoSet.id,
                                    name:{es:"Un stream"},
                                    hidden:false,
                                    icon:videoSet.icon,
                                    videos: [
                                        {
                                            content:validContent[0],
                                            rect:[
                                                { aspectRatio:"1/1",left:280,top:0,width:720,height:720 },
                                                { aspectRatio:"6/5",left:208,top:0,width:864,height:720 },
                                                { aspectRatio:"5/4",left:190,top:0,width:900,height:720 },
                                                { aspectRatio:"4/3",left:160,top:0,width:960,height:720 },
                                                { aspectRatio:"11/8",left:145,top:0,width:990,height:720 },
                                                { aspectRatio:"1.41/1",left:132,top:0,width:1015,height:720 },
                                                { aspectRatio:"1.43/1",left:125,top:0,width:1029,height:720 },
                                                { aspectRatio:"3/2",left:100,top:0,width:1080,height:720 },
                                                { aspectRatio:"16/10",left:64,top:0,width:1152,height:720 },
                                                { aspectRatio:"5/3",left:40,top:0,width:1200,height:720 },
                                                { aspectRatio:"16/9",left:0,top:0,width:1280,height:720 },
                                                { aspectRatio:"1.85/1",left:0,top:14,width:1280,height:692 },
                                                { aspectRatio:"2.35/1",left:0,top:87,width:1280,height:544 },
                                                { aspectRatio:"2.41/1",left:0,top:94,width:1280,height:531 },
                                                { aspectRatio:"2.76/1",left:0,top:128,width:1280,height:463 }
                                            ],
                                            visible:true,
                                            layer:1
                                        }
                                    ],
                                    background:{content:"slide_professor_paella.jpg",zIndex:5,rect:{left:0,top:0,width:1280,height:720},visible:true,layer:0},
                                    logos:[{content:"paella_logo.png",zIndex:5,rect:{top:10,left:10,width:49,height:42}}],
                                    buttons: [],
                                    onApply: function() {
                                    }
                                })
                            })
                        });
                    }
                    else {
                        onSuccess(false)
                    }
                }
            })
        }
    }
})

paella.addPlugin(function() {
	return class DualStreamProfilePlugin extends paella.EventDrivenPlugin {
		
		getName() {
			return "es.upv.paella.dualStreamProfilePlugin";
		}
		
		checkEnabled(onSuccess) {
            let config = this.config;
            config.videoSets.forEach((videoSet,index) => {
                let validContent = videoSet.content
                if (validContent.length==2) {
                    let streamCount = 0;
                    paella.player.videoContainer.streamProvider.videoStreams.forEach((v) => {
                        if (validContent.indexOf(v.content)!=-1) {
                            streamCount++
                        }
                    })
                    if (streamCount>=2) {
                        onSuccess(true)

                        let layout = 0;
                        const layouts = [
                            // First layout: side by side
                            {
                                videos: [
                                    {
                                        content:null,
                                        rect:[
                                            {aspectRatio:"16/9",width:560,height:315,top:198,left:712},
                                            {aspectRatio:"16/10",width:560,height:350,top:186,left:712},
                                            {aspectRatio:"4/3",width:560,height:420,top:153,left:712},
                                            {aspectRatio:"5/3",width:560,height:336,top:186,left:712},
                                            {aspectRatio:"5/4",width:560,height:448,top:140,left:712}
                                        ],
                                        visible:true,
                                        layer:1
                                    },
                                    {
                                        content:null,
                                        rect:[
                                            {aspectRatio:"16/9",width:688,height:387,top:166,left:10},
                                            {aspectRatio:"16/10",width:688,height:430,top:148,left:10},
                                            {aspectRatio:"4/3",width:688,height:516,top:111,left:10},
                                            {aspectRatio:"5/3",width:690,height:414,top:154,left:10},
                                            {aspectRatio:"5/4",width:690,height:552,top:96,left:10}
                                        ],
                                        visible:true,
                                        layer:"1"
                                    }
                                ],
                                buttons: [
                                    {
                                        rect: { left: 682, top: 565, width: 45, height: 45 },
                                        label:"Switch",
                                        icon:"icon_rotate.svg",
                                        layer: 2
                                    },
                                    {
                                        rect: { left: 682, top: 515, width: 45, height: 45 },
                                        label:"Minimize",
                                        icon:"minimize.svg",
                                        layer: 2
                                    }
                                ]
                            },

                            // Second layout: PIP left
                            {
                                videos:[
                                    {
                                        content:null,
                                        rect:[
                                            {aspectRatio:"16/9",left:0,top:0,width:1280,height:720},
                                            {aspectRatio:"16/10",left:64,top:0,width:1152,height:720},
                                            {aspectRatio:"5/3",left:40,top:0,width:1200,height:720},
                                            {aspectRatio:"5/4",left:190,top:0,width:900,height:720},
                                            {aspectRatio:"4/3",left:160,top:0,width:960,height:720}
                                        ],
                                        visible:true,
                                        layer:1
                                    },
                                    {
                                        content:null,
                                        rect:[
                                            {aspectRatio:"16/9",left:50,top:470,width:350,height:197},
                                            {aspectRatio:"16/10",left:50,top:448,width:350,height:219},
                                            {aspectRatio:"5/3",left:50,top:457,width:350,height:210},
                                            {aspectRatio:"5/4",left:50,top:387,width:350,height:280},
                                            {aspectRatio:"4/3",left:50,top:404,width:350,height:262}
                                        ],
                                        visible:true,
                                        layer:2
                                    }
                                ],
                                buttons: [
                                    {
                                        rect: { left: 388, top: 465, width: 45, height: 45 },
                                        label:"Switch",
                                        icon:"icon_rotate.svg",
                                        layer: 2
                                    },
                                    {
                                        rect: { left: 388, top: 415, width: 45, height: 45 },
                                        label:"Switch",
                                        icon:"minimize.svg",
                                        layer: 2
                                    }
                                ]
                            },

                            // Third layout: PIP right
                            {
                                videos: [
                                    {
                                        content:null,
                                        rect:[
                                            {aspectRatio:"16/9",left:0,top:0,width:1280,height:720},
                                            {aspectRatio:"16/10",left:64,top:0,width:1152,height:720},
                                            {aspectRatio:"5/3",left:40,top:0,width:1200,height:720},
                                            {aspectRatio:"5/4",left:190,top:0,width:900,height:720},
                                            {aspectRatio:"4/3",left:160,top:0,width:960,height:720}
                                        ],
                                        visible:true,
                                        layer:1
                                    },
                                    {
                                        content:null,
                                        rect:[
                                            {aspectRatio:"16/9",left:880,top:470,width:350,height:197},
                                            {aspectRatio:"16/10",left:880,top:448,width:350,height:219},
                                            {aspectRatio:"5/3",left:880,top:457,width:350,height:210},
                                            {aspectRatio:"5/4",left:880,top:387,width:350,height:280},
                                            {aspectRatio:"4/3",left:880,top:404,width:350,height:262}
                                        ],
                                        visible:true,
                                        layer:2
                                    }
                                ],
                                buttons: [
                                    {
                                        rect: { left: 848, top: 465, width: 45, height: 45 },
                                        onClick: function() { this.switch(); },
                                        label:"Switch",
                                        icon:"icon_rotate.svg",
                                        layer: 2
                                    },
                                    {
                                        rect: { left: 848, top: 415, width: 45, height: 45 },
                                        onClick: function() { this.switchMinimize(); },
                                        label:"Switch",
                                        icon:"minimize.svg",
                                        layer: 2
                                    }
                                ]
                            }
                        ];

                        function nextLayout() {
                            let selectedLayout = JSON.parse(JSON.stringify(layouts[layout]));
                            layout = (layout + 1) % layouts.length;
                            selectedLayout.videos[0].content = validContent[0];
                            selectedLayout.videos[1].content = validContent[1];
                            return selectedLayout;
                        }

                        paella.addProfile(() => {
                            const selectedLayout = nextLayout();
                            return new Promise((resolve) => {
                                resolve({
                                    id:videoSet.id,
                                    name:{es:"Dos streams con posición dinámica"},
                                    hidden:false,
                                    icon:videoSet.icon,
                                    videos: selectedLayout.videos,
                                    background:{content:"slide_professor_paella.jpg",zIndex:5,rect:{left:0,top:0,width:1280,height:720},visible:true,layer:0},
                                    logos:[{content:"paella_logo.png",zIndex:5,rect:{top:10,left:10,width:49,height:42}}],
                                    buttons: [
                                        {
                                            rect: selectedLayout.buttons[0].rect,
                                            onClick: function() { this.switch(); },
                                            label:"Switch",
                                            icon:"icon_rotate.svg",
                                            layer: 2
                                        },
                                        {
                                            rect: selectedLayout.buttons[1].rect,
                                            onClick: function() { this.switchMinimize(); },
                                            label:"Minimize",
                                            icon:"minimize.svg",
                                            layer: 2
                                        }
                                    ],
                                    onApply: function() {
                                    },
                                    switch: function() {
                                        let v0 = this.videos[0].content;
                                        let v1 = this.videos[1].content;
                                        this.videos[0].content = v1;
                                        this.videos[1].content = v0;
                                        paella.profiles.placeVideos();
                                    },
                                    switchMinimize: function() {
                                        const newLayout = nextLayout();
                                        this.videos = newLayout.videos;
                                        this.buttons[0].rect = newLayout.buttons[0].rect;
                                        this.buttons[1].rect = newLayout.buttons[1].rect;
                                        paella.profiles.placeVideos();
                                    }
                                })
                            })
                        });
                    }
                    else {
                        onSuccess(false);
                    }
                }
            })
        }
	};
});

paella.addPlugin(function() {
	return class TripleStreamProfilePlugin extends paella.EventDrivenPlugin {
		
		getName() {
			return "es.upv.paella.tripleStreamProfilePlugin";
		}
		
		checkEnabled(onSuccess) {
            let config = this.config;
            config.videoSets.forEach((videoSet,index) => {
                let validContent = videoSet.content
                if (validContent.length==3) {
                    let streamCount = 0;
                    paella.player.videoContainer.streamProvider.videoStreams.forEach((v) => {
                        if (validContent.indexOf(v.content)!=-1) {
                            streamCount++
                        }
                    })
                    if (streamCount>=3) {
                        onSuccess(true);
                        paella.addProfile(() => {
                            return new Promise((resolve,reject) => {
                                resolve({
                                    id:videoSet.id,
                                    name:{es:"Tres streams posición dinámica"},
                                    hidden:false,
                                    icon:videoSet.icon,
                                    videos: [
                                        {
                                            content: validContent[0],
                                            rect:[
                                                { aspectRatio:"16/9",left:239, top:17, width:803, height:451 }
                                            ],
                                            visible:true,
                                            layer:1
                                        },
                                        {
                                            content:  validContent[1],
                                            rect:[
                                                { aspectRatio:"16/9",left:44, top:482, width:389, height:218 }
                                            ],
                                            visible:true,
                                            layer:1
                                        },
                                        {
                                            content:  validContent[2],
                                            rect:[
                                                { aspectRatio:"16/9",left:847, top:482, width:389, height:218 }
                                            ],
                                            visible:true,
                                            layer:1
                                        }
                                    ],
                                    background: {content:"slide_professor_paella.jpg",zIndex:5,rect: { left:0,top:0,width:1280,height:720},visible: true,layer:0},
                                    logos: [{content:"paella_logo.png",zIndex:5,rect: { top:10,left:10,width:49,height:42}}],
                                    buttons: [
                                        {
                                            rect: { left: 618, top: 495, width: 45, height: 45 },
                                            onClick: function(event) { this.rotate(); },
                                            label:"Rotate",
                                            icon:"icon_rotate.svg",
                                            layer: 2
                                        }
                                    ],
                                    onApply: function() {
                                    },
                                    rotate: function() {
                                        let v0 = this.videos[0].content;
                                        let v1 = this.videos[1].content;
                                        let v2 = this.videos[2].content;
                                        this.videos[0].content = v2;
                                        this.videos[1].content = v0;
                                        this.videos[2].content = v1;
                                        paella.profiles.placeVideos();
                                    }
                                })
                            })
                        });
                    }
                    else {
                        onSuccess(false);
                    }
                }
            })
        }
	};
});

paella.addProfile(() => {
    return new Promise((resolve,reject) => {
        paella.events.bind(paella.events.videoReady,() => {
            let available = paella.player.videoContainer.streamProvider.videoStreams.some((v) => v.content=="blackboard")
			if(!available) {
                resolve(null);
            }
            else {
                resolve({
                    id:"blackboard_video_stream",
                    name:{es:"Pizarra"},
                    hidden:false,
                    icon:"s_p_blackboard.svg",
                    videos: [
                        {
                            content: "presentation",
                            rect:[
                            {aspectRatio:"16/9",left:10,top:70,width:432,height:243}],
                            visible:true,
                            layer:1
                        },
                        {
                            content:"blackboard",
                            rect:[{aspectRatio:"16/9",left:450,top:135,width:816,height:459}],
                            visible:true,
                            layer:1
                        },
                        {
                            content:"presenter",
                            rect:[{aspectRatio:"16/9",left:10,top:325,width:432,height:324}],
                            visible:true,
                            layer:1

                        }
                    ],
                    //blackBoardImages: {left:10,top:325,width:432,height:324},
                    background: {content:"slide_professor_paella.jpg",zIndex:5,rect: { left:0,top:0,width:1280,height:720},visible: true,layer:0},
                    logos: [{content:"paella_logo.png",zIndex:5,rect: { top:10,left:10,width:49,height:42}}],
                    buttons: [
                        {
                            rect: { left: 422, top: 295, width: 45, height: 45 },
                            onClick: function(event) { this.rotate(); },
                            label:"Rotate",
                            icon:"icon_rotate.svg",
                            layer: 2
                        }
                    ],
                    rotate: function() {
                        let v0 = this.videos[0].content;
                        let v1 = this.videos[1].content;
                        let v2 = this.videos[2].content;
                        this.videos[0].content = v2;
                        this.videos[1].content = v0;
                        this.videos[2].content = v1;
                        paella.profiles.placeVideos();
                    }
                });
            }
        });
    })
});

paella.addProfile(() => {
    return new Promise((resolve,reject) => {
        paella.events.bind(paella.events.videoReady, () => {
            // TODO: videoContainer.sourceData is deprecated. Update this code
            var n = paella.player.videoContainer.sourceData[0].sources;
            if (!n.chroma) {
                resolve(null);
            }
            else {
                resolve({
                    id:"chroma",
                    name:{es:"Polimedia"},
                    hidden:false,
                    icon:"chroma.svg",
                    videos: [
                        {
                            content:"presenter",rect:[
                                {aspectRatio:"16/9",left:0,top:0,width:1280,height:720},
                                {aspectRatio:"16/10",left:64,top:0,width:1152,height:720},
                                {aspectRatio:"5/3",left:40,top:0,width:1200,height:720},
                                {aspectRatio:"5/4",left:190,top:0,width:900,height:720},
                                {aspectRatio:"4/3",left:160,top:0,width:960,height:720}
                            ],visible:"true",layer:"1"
                        },
                        {
                            content:"presentation",rect:[
                                {aspectRatio:"16/9",left:0,top:0,width:1280,height:720},
                                {aspectRatio:"16/10",left:64,top:0,width:1152,height:720},
                                {aspectRatio:"5/3",left:40,top:0,width:1200,height:720},
                                {aspectRatio:"5/4",left:190,top:0,width:900,height:720},
                                {aspectRatio:"4/3",left:160,top:0,width:960,height:720}
                            ],visible:"true",layer:"0"
                        }
                    ],
                    background:{content:"default_background_paella.jpg",zIndex:5,rect:{left:0,top:0,width:1280,height:720},visible:"true",layer:"0"},
                    logos:[{content:"paella_logo.png",zIndex:5,rect:{top:10,left:10,width:49,height:42}}]
                })
            }
        })
    })
});


paella.addPlugin(function() {


	
	return class TrimmingLoaderPlugin extends paella.EventDrivenPlugin {
		
		getName() { return "es.upv.paella.trimmingPlayerPlugin"; }
			
		getEvents() { return [paella.events.controlBarLoaded, paella.events.showEditor, paella.events.hideEditor]; }
	
		onEvent(eventType,params) {
			switch (eventType) {
				case paella.events.controlBarLoaded:
					this.loadTrimming();
					break;
				case paella.events.showEditor:
					paella.player.videoContainer.disableTrimming();
					break;
				case paella.events.hideEditor:
					if (paella.player.config.trimming && paella.player.config.trimming.enabled) {
						paella.player.videoContainer.enableTrimming();
					}
					break;
			}
		}
		
		loadTrimming() {
			var videoId = paella.initDelegate.getId();
			paella.data.read('trimming',{id:videoId},function(data,status) {
				if (data && status && data.end>0) {
					paella.player.videoContainer.enableTrimming();
					paella.player.videoContainer.setTrimming(data.start, data.end)
						.then(() => {})
					
				}
				else {
					// Check for optional trim 'start' and 'end', in seconds, in location args
					var startTime =  paella.utils.parameters.get('start');
					var endTime = paella.utils.parameters.get('end');
					if (startTime && endTime) {
						paella.player.videoContainer.enableTrimming();
						paella.player.videoContainer.setTrimming(startTime, endTime)
							.then(() => {})

					}
				}
			});
		}
	}
});

paella.addPlugin(function() {

	return class AirPlayPlugin extends paella.ButtonPlugin {
		getIndex() { return 552; }
		getAlignment() { return 'right'; }
		getSubclass() { return "AirPlayButton"; }
		getIconClass() { return 'icon-airplay'; }
		getName() { return "es.upv.paella.airPlayPlugin"; }
		checkEnabled(onSuccess) {
			this._visible = false;
			// PIP is only available with single stream videos
            if (paella.player.videoContainer.streamProvider.videoStreams.length!=1) {
                onSuccess(false);
            }
            else {
				onSuccess(window.WebKitPlaybackTargetAvailabilityEvent);
			}
		}
		getDefaultToolTip() { return paella.utils.dictionary.translate("Emit to AirPlay."); }
	
		setup() {
			let video = paella.player.videoContainer.masterVideo().video;
			if (window.WebKitPlaybackTargetAvailabilityEvent) {
				video.addEventListener('webkitplaybacktargetavailabilitychanged', (event) => {
					switch (event.availability) {
					case "available":
						this._visible = true;
						break;
					case "not-available":
						this._visible = false;
						break;
					}
					this.updateClassName();
				});
			}
		}
	
		action(button) {
			let video = paella.player.videoContainer.masterVideo().video;
			video.webkitShowPlaybackTargetPicker();
		}
	
		updateClassName() {
			this.button.className = this.getButtonItemClass(true);
		}
	
		getButtonItemClass(selected) {
			return 'buttonPlugin ' + this.getSubclass() + " " + this.getAlignment() + " " + (this._visible ? "available":"not-available");
		}
	}
});


paella.addPlugin(function() {

	return class ArrowSlidesNavigator extends paella.EventDrivenPlugin {
		getName() { return "es.upv.paella.arrowSlidesNavigatorPlugin"; }

		checkEnabled(onSuccess) {
			if (!paella.initDelegate.initParams.videoLoader.frameList ||
				Object.keys(paella.initDelegate.initParams.videoLoader.frameList).length==0 ||
				paella.player.videoContainer.isMonostream)
			{
				onSuccess(false);
			}
			else {
				onSuccess(true);
			}
		}
		
		setup() {
			var self = this;
			this._showArrowsIn = this.config.showArrowsIn || 'slave';
			this.createOverlay();
				
			self._frames = [];		
			var frames = paella.initDelegate.initParams.videoLoader.frameList;
			var numFrames;
			if (frames) {
				var framesKeys = Object.keys(frames);
				numFrames = framesKeys.length;
	
				framesKeys.map(function(i){return Number(i, 10);})
				.sort(function(a, b){return a-b;})
				.forEach(function(key){
					self._frames.push(frames[key]);
				});
			}
		}
		
		createOverlay(){
			var self = this;
	
			let overlayContainer = paella.player.videoContainer.overlayContainer;
			
			if (!this.arrows) {
				this.arrows = document.createElement('div');
				this.arrows.id = "arrows";
				this.arrows.style.marginTop = "25%";
				
				let arrowNext = document.createElement('div');
				arrowNext.className = "buttonPlugin arrowSlideNavidator nextButton right icon-next2"
				this.arrows.appendChild(arrowNext);
		
				let arrowPrev = document.createElement('div');
				arrowPrev.className = "buttonPlugin arrowSlideNavidator prevButton left icon-previous2"
				this.arrows.appendChild(arrowPrev);
		
		
				$(arrowNext).click(function(e) {
					self.goNextSlide();
					e.stopPropagation();
				});
				$(arrowPrev).click(function(e) {
					self.goPrevSlide();
					e.stopPropagation();
				});			
			}
			
			if (this.container) {
				overlayContainer.removeElement(this.container);
			}

			let rect = null;
			let element = null;
			
			if (!paella.profiles.currentProfile) {
				return null;
			}

			this.config.content = this.config.content || ["presentation"];
			let profilesContent = [];
			paella.profiles.currentProfile.videos.forEach((profileData) => {
				profilesContent.push(profileData.content);
			});

			// Default content, if the "content" setting is not set in the configuration file
			let selectedContent = profilesContent.length==1 ? profilesContent[0] : (profilesContent.length>1 ? profilesContent[1] : "");

			this.config.content.some((preferredContent) => {
				if (profilesContent.indexOf(preferredContent)!=-1) {
					selectedContent = preferredContent;
					return true;
				}
			})


			if (!selectedContent) {
				this.container = overlayContainer.addLayer();
				this.container.style.marginRight = "0";
				this.container.style.marginLeft = "0";			
				this.arrows.style.marginTop = "25%";
			}
			else {
				let videoIndex = 0;
				paella.player.videoContainer.streamProvider.streams.forEach((stream,index) => {
					if (stream.type=="video" && selectedContent==stream.content) {
						videoIndex = index;
					}
				});
				element = document.createElement('div');
				rect = overlayContainer.getVideoRect(videoIndex);	// content
				this.container = overlayContainer.addElement(element,rect);
				this.visible = rect.visible;
				this.arrows.style.marginTop = "33%";
			}
			
			this.container.appendChild(this.arrows);
			this.hideArrows();
		}
		
		getCurrentRange() {
			return new Promise((resolve) => {
				if (this._frames.length<1) {
					resolve(null);
				}
				else {
					let trimming = null;
					let duration = 0;
					paella.player.videoContainer.duration()
						.then((d) => {
							duration = d;
							return paella.player.videoContainer.trimming();
						})
	
						.then((t) => {
							trimming = t;
							return paella.player.videoContainer.currentTime();
						})
	
						.then((currentTime) => {
							if (!this._frames.some((f1,i,array) => {
								if (i+1==array.length) { return; }
								let f0 = i==0 ? f1 : this._frames[i-1];
								let f2 = this._frames[i+1];
								let t0 = trimming.enabled ? f0.time - trimming.start : f0.time;
								let t1 = trimming.enabled ? f1.time - trimming.start : f1.time;
								let t2 = trimming.enabled ? f2.time - trimming.start : f2.time;
								if ((t1<currentTime && t2>currentTime) || t1==currentTime) {
									let range = {
										prev: t0,
										next: t2
									};
									if (t0<0) {
										range.prev = t1>0 ? t1 : 0;
									}
									resolve(range);
									return true;
								}
							})) {
								let t0 = this._frames[this._frames.length-2].time;
								let t1 = this._frames[this._frames.length-1].time;
								resolve({
									prev: trimming.enabled ? t0 - trimming.start : t0,
									next: trimming.enabled ? t1 - trimming.start : t1
								});
							}
						});
				}
			})
		}
	
		goNextSlide() {
			var self = this;
			let trimming;
			this.getCurrentRange()
				.then((range) => {
					return paella.player.videoContainer.seekToTime(range.next);
				})

				.then(() => {
					paella.player.videoContainer.play();
				});
		}
	
		goPrevSlide() {
			var self = this;
			let trimming = null;
			this.getCurrentRange()
				.then((range) => {
					return paella.player.videoContainer.seekToTime(range.prev);
				})
				
				.then(() => {
					paella.player.videoContainer.play();
				});
		}
		
		showArrows(){ if (this.visible) $(this.arrows).show(); }
		hideArrows(){ $(this.arrows).hide(); }
		
		getEvents() { return [paella.events.controlBarDidShow, paella.events.controlBarDidHide, paella.events.setComposition]; }
	
		onEvent(eventType,params) {
			var self = this;
			switch(eventType) {
				case paella.events.controlBarDidShow:
					this.showArrows();
					break;
				case paella.events.controlBarDidHide:
					this.hideArrows();
					break;
				case paella.events.setComposition:
					this.createOverlay();
					break;
			}
		}
	} 
});

paella.addPlugin(function() {
	return class AudioSelector extends paella.ButtonPlugin {
		getAlignment() { return 'right'; }
		getSubclass() { return "audioSelector"; }
		getIconClass() { return 'icon-headphone'; }
		getIndex() { return 2040; }
		getName() { return "es.upv.paella.audioSelector"; }
		getDefaultToolTip() { return paella.utils.dictionary.translate("Set audio stream"); }

		closeOnMouseOut() { return true; }
			
		checkEnabled(onSuccess) {
			this._mainPlayer = paella.player.videoContainer.streamProvider.mainVideoPlayer;
			this._mainPlayer.supportsMultiaudio()
				.then((supports)=> {
					if (supports) {
						this._legacyMode = false;
						return this._mainPlayer.getAudioTracks();
					}
					else {
						this._legacyMode = true;
						return paella.player.videoContainer.getAudioTags();
					}
				})

				.then((audioTracks) => {
					if (this._legacyMode) {
						this._tags = audioTracks;
						return Promise.resolve();
					}
					else {
						this._audioTracks = audioTracks;
						return this._mainPlayer.getCurrentAudioTrack();
					}
				})

				.then((defaultAudioTrack) => {
					if (this._legacyMode) {
						onSuccess(this._tags.length>1);
					}
					else {
						this._defaultAudioTrack = defaultAudioTrack;
						onSuccess(true);
					}
				})
		}

		getButtonType() { return paella.ButtonPlugin.type.menuButton; }

		getMenuContent() {
			let buttonItems = [];

			if (this._legacyMode) {
				this._tags.forEach((tag,index) => {
					buttonItems.push({
						id: index,
						title: tag,
						value: tag,
						icon: "",
						className: this.getButtonItemClass(tag),
						default: tag == paella.player.videoContainer.audioTag
					});
				});
			}
			else {
				this._audioTracks.forEach((track) => {
					buttonItems.push({
						id: track.id,
						title: track.groupId + " " + track.name,
						value: track.id,
						icon: "",
						className: this.getButtonItemClass(track.id),
						default: track.id == this._defaultAudioTrack.id
					});
				});
			}

			return buttonItems;
		}

		menuItemSelected(itemData) {
			if (this._legacyMode) {
				paella.player.videoContainer.setAudioTag(itemData.value);
			}
			else {
				this._mainPlayer.setCurrentAudioTrack(itemData.id);
			}
			paella.player.controls.hidePopUp(this.getName());
		}
		
		setQualityLabel() {
			if (this._legacyMode) {
				var This = this;
				paella.player.videoContainer.getCurrentQuality()
					.then(function(q) {
						This.setText(q.shortLabel());
					});
			}
			else {

			}
		}

		getButtonItemClass(tag) {
			return 'videoAudioTrackItem ' + tag;
		}
	}
});
paella.addPlugin(function() {
	return class BlackBoard2 extends paella.EventDrivenPlugin {
		getName() { return "es.upv.paella.blackBoardPlugin"; }
		getIndex() {return 10; }
		getAlignment() { return 'right'; }
		getSubclass() { return "blackBoardButton2"; }
		getDefaultToolTip() { return paella.utils.dictionary.translate("BlackBoard"); }
	
		checkEnabled(onSuccess) {
			this._blackBoardProfile = "s_p_blackboard2";
			this._blackBoardDIV = null;
			this._hasImages = null;
			this._active = false;
			this._creationTimer = 500;
			this._zImages = null;
			this._videoLength = null;
			this._keys = null;
			this._currentImage = null;
			this._next = null;
			this._prev = null;
			this._lensDIV = null;
			this._lensContainer = null;
			this._lensWidth = null;
			this._lensHeight = null;
			this._conImg = null;
			this._zoom = 250;
			this._currentZoom = null;
			this._maxZoom = 500;
			this._mousePos = null;
			this._containerRect = null;
			onSuccess(true);
		}
	
		getEvents() {
			return[
				paella.events.setProfile,
				paella.events.timeUpdate
			];
		}
	
		onEvent(event, params) {
			var self = this;
			switch(event){
				case paella.events.setProfile:
					if(params.profileName!=self._blackBoardProfile){
						if(self._active){
							self.destroyOverlay();
							self._active = false;
						}
						break;
					}
					else{ 
					
						if(!self._hasImages){
							paella.player.setProfile("slide_professor");
						}
						if(self._hasImages && !self._active){
							self.createOverlay();
							self._active = true;
						}
					}
					break;
				case paella.events.timeUpdate:
					if(self._active && self._hasImages) {
						paella.player.videoContainer.trimming()
							.then((trimmingData) => {
								if (trimmingData.enabled) {
									params.currentTime += trimmingData.start;
								}
								
								self.imageUpdate(event,params);
							})
					}
					break;
			}
		}
	
		setup() {
			var self = this;	
	
			var n = paella.player.videoContainer.sourceData[0].sources;
			if(n.hasOwnProperty("image")){
				self._hasImages = true;
				//  BRING THE IMAGE ARRAY TO LOCAL
				self._zImages = {};
				self._zImages = paella.player.videoContainer.sourceData[0].sources.image[0].frames; // COPY TO LOCAL
				self._videoLength = paella.player.videoContainer.sourceData[0].sources.image[0].duration; // video duration in frames
	
				// SORT KEYS FOR SEARCH CLOSEST
				self._keys = Object.keys(self._zImages);
				self._keys = self._keys.sort(function(a, b){
					a = a.slice(6);
					b = b.slice(6);
					return parseInt(a)-parseInt(b); 
				});
			}
			else{
				self._hasImages = false;
	
				if (paella.player.selectedProfile == self._blackBoardProfile) {
					let defaultprofile = paella.player.config.defaultProfile;
					paella.player.setProfile(defaultprofile);
				}
			}
	
	
			//NEXT
			this._next = 0;
			this._prev = 0;
	
			if(paella.player.selectedProfile == self._blackBoardProfile){
				self.createOverlay();
				self._active = true;
			}
	
			self._mousePos = {};
	
	
			paella.Profiles.loadProfile(self._blackBoardProfile,function(profileData) {
				self._containerRect = profileData.blackBoardImages;
			});
		}
	
		createLens() {
			var self = this;
			if(self._currentZoom == null) { self._currentZoom = self._zoom; }
			var lens = document.createElement("div");
			lens.className = "lensClass";
	
			self._lensDIV = lens;
	
			var p = $('.conImg').offset();
			var width = $('.conImg').width();
			var height = $('.conImg').height();
			lens.style.width = (width/(self._currentZoom/100))+"px";
			lens.style.height = (height/(self._currentZoom/100))+"px";
			self._lensWidth = parseInt(lens.style.width);
			self._lensHeight = parseInt(lens.style.height);
			$(self._lensContainer).append(lens);
			
			$(self._lensContainer).mousemove(function(event) {	
				let mouseX = (event.pageX-p.left);
				let mouseY = (event.pageY-p.top);
				
				self._mousePos.x = mouseX;
				self._mousePos.y = mouseY;
	
				let lensTop = (mouseY - self._lensHeight/2);
				lensTop = (lensTop < 0) ? 0 : lensTop;
				lensTop = (lensTop > (height-self._lensHeight)) ? (height-self._lensHeight) : lensTop; 
	
				let lensLeft = (mouseX - self._lensWidth/2);
				lensLeft = (lensLeft < 0) ? 0 : lensLeft;
				lensLeft = (lensLeft > (width-self._lensWidth)) ? (width-self._lensWidth) : lensLeft; 
	
				self._lensDIV.style.left = lensLeft + "px";
				self._lensDIV.style.top = lensTop + "px";
				if(self._currentZoom != 100){
					let x = (lensLeft) * 100 / (width-self._lensWidth);
					let y = (lensTop) * 100 / (height-self._lensHeight);
					self._blackBoardDIV.style.backgroundPosition = x.toString() + '% ' + y.toString() + '%';
				}
					
				else if(self._currentZoom == 100){
						var xRelative = mouseX * 100 / width;
						var yRelative = mouseY * 100 / height;
						self._blackBoardDIV.style.backgroundPosition = xRelative.toString() + '% ' + yRelative.toString() + '%';
					}
	
				self._blackBoardDIV.style.backgroundSize = self._currentZoom+'%';
			});
	
			$(self._lensContainer).bind('wheel mousewheel', function(e){
				let delta;
	
				if (e.originalEvent.wheelDelta !== undefined) {
					delta = e.originalEvent.wheelDelta;
				}
				else {
					delta = e.originalEvent.deltaY * -1;
				}
	
				if(delta > 0 && self._currentZoom<self._maxZoom) {
					self.reBuildLens(10);
				}
				else if(self._currentZoom>100){
					self.reBuildLens(-10);
				}
				else if(self._currentZoom==100){
					self._lensDIV.style.left = 0+"px";
					self._lensDIV.style.top = 0+"px";
				}
				self._blackBoardDIV.style.backgroundSize = (self._currentZoom)+"%";
			
			});	
		}
	
		reBuildLens(zoomValue) {
			var self = this;
			self._currentZoom += zoomValue;
			var p = $('.conImg').offset();
			var width = $('.conImg').width();
			var height = $('.conImg').height();
			self._lensDIV.style.width = (width/(self._currentZoom/100))+"px";
			self._lensDIV.style.height = (height/(self._currentZoom/100))+"px";
			self._lensWidth = parseInt(self._lensDIV.style.width);
			self._lensHeight = parseInt(self._lensDIV.style.height);
			
			if(self._currentZoom != 100){
				let mouseX = self._mousePos.x;
				let mouseY = self._mousePos.y;
	
				let lensTop = (mouseY - self._lensHeight/2);
				lensTop = (lensTop < 0) ? 0 : lensTop;
				lensTop = (lensTop > (height-self._lensHeight)) ? (height-self._lensHeight) : lensTop; 
	
				let lensLeft = (mouseX - self._lensWidth/2);
				lensLeft = (lensLeft < 0) ? 0 : lensLeft;
				lensLeft = (lensLeft > (width-self._lensWidth)) ? (width-self._lensWidth) : lensLeft; 
	
				self._lensDIV.style.left = lensLeft + "px";
				self._lensDIV.style.top = lensTop + "px";
	
				let x = (lensLeft) * 100 / (width-self._lensWidth);
				let y = (lensTop) * 100 / (height-self._lensHeight);
				self._blackBoardDIV.style.backgroundPosition = x.toString() + '% ' + y.toString() + '%';
			}
		}
	
		destroyLens() {
			var self=this;
			if(self._lensDIV){
				$(self._lensDIV).remove();
				self._blackBoardDIV.style.backgroundSize = 100+'%';
				self._blackBoardDIV.style.opacity = 0;
			}
			//self._currentZoom = self._zoom;
		}
	
		createOverlay() {
			var self = this;
	
			var blackBoardDiv = document.createElement("div");
			blackBoardDiv.className = "blackBoardDiv";
			self._blackBoardDIV = blackBoardDiv;
			self._blackBoardDIV.style.opacity = 0;
	
			var lensContainer = document.createElement("div");
			lensContainer.className = "lensContainer";
			self._lensContainer = lensContainer;
	
			var conImg = document.createElement("img");
			conImg.className = "conImg";
			self._conImg = conImg;
	
			if (self._currentImage) {
				self._conImg.src = self._currentImage;
				$(self._blackBoardDIV).css('background-image', 'url(' + self._currentImage + ')');
			}
	
			$(lensContainer).append(conImg);
	
			$(self._lensContainer).mouseenter(function(){self.createLens(); self._blackBoardDIV.style.opacity = 1.0;});
			$(self._lensContainer).mouseleave(function(){self.destroyLens();});
	
			setTimeout(function(){ // TIMER FOR NICE VIEW
				let overlayContainer = paella.player.videoContainer.overlayContainer;
				overlayContainer.addElement(blackBoardDiv, overlayContainer.getVideoRect(0));
				overlayContainer.addElement(lensContainer, self._containerRect);
			}, self._creationTimer);
		}
	
		destroyOverlay() {
			var self = this;
	
			if (self._blackBoardDIV) {
				$(self._blackBoardDIV).remove();
			}
			if (self._lensContainer){
				$(self._lensContainer).remove();
			}
		}
	
		imageUpdate(event,params) {
			var self = this;
			var sec = Math.round(params.currentTime);
			var src = $(self._blackBoardDIV).css('background-image');
	
			if($(self._blackBoardDIV).length>0){
	
				if(self._zImages.hasOwnProperty("frame_"+sec)) { // SWAP IMAGES WHEN PLAYING
					if(src == self._zImages["frame_"+sec]) {
						return;
					}
					else {
						src = self._zImages["frame_"+sec];
					}
				}
				else if(sec > self._next || sec < self._prev) {
					src = self.returnSrc(sec); 
				} // RELOAD IF OUT OF INTERVAL
				else {
					return;
				}
	
				//PRELOAD NEXT IMAGE
				var image = new Image();
				image.onload = function(){
					$(self._blackBoardDIV).css('background-image', 'url(' + src + ')'); // UPDATING IMAGE
				};
				image.src = src;
	
				self._currentImage = src;
				self._conImg.src = self._currentImage;
			}
		}
	
		returnSrc(sec) {
			var prev = 0;
			for (let i=0; i<this._keys.length; i++){
				var id = parseInt(this._keys[i].slice(6));
				var lastId = parseInt(this._keys[(this._keys.length-1)].slice(6));
				if(sec < id) {  // PREVIOUS IMAGE
					this._next = id; 
					this._prev = prev; 
					this._imageNumber = i-1;
					return this._zImages["frame_" + prev];	 // return previous and keep next change
				}
				else if (sec > lastId && sec < this._videoLength) { // LAST INTERVAL
					this._next = this._videoLength;
					this._prev = lastId;
					return this._zImages["frame_" + prev]; 
				}
				else {
					prev = id;
				}
			}
		}
	}

});

paella.addPlugin(() => {
	return class BreaksPlayerPlugin extends paella.EventDrivenPlugin {
		getName() { return "es.upv.paella.breaksPlayerPlugin"; }

		checkEnabled(onSuccess) {
			onSuccess(true);
		}

		setup() {
			this.breaks = [];
			this.status = false;
			this.lastTime = 0;
			paella.data.read('breaks', { id: paella.player.videoIdentifier }, (data) => {
				if (data && typeof (data) == 'object' && data.breaks && data.breaks.length > 0) {
					this.breaks = data.breaks;
				}
			});
		}

		getEvents() { return [ paella.events.timeUpdate ]; }

		onEvent(eventType, params) {
			paella.player.videoContainer.currentTime(true)
				.then((currentTime) => {
					// The event type checking must to be done using the time difference, because
					// the timeUpdate event may arrive before the seekToTime event
					let diff = Math.abs(currentTime - this.lastTime);
					this.checkBreaks(currentTime,diff>=1 ? paella.events.seekToTime : paella.events.timeUpdate);
					this.lastTime = currentTime;
				});
		}

		checkBreaks(currentTime,eventType) {
			let breakMessage = "";
			if (this.breaks.some((breakItem) => {
				if (breakItem.s<=currentTime && breakItem.e>=currentTime) {
					if (eventType==paella.events.timeUpdate && !this.status) {
						this.skipTo(breakItem.e);
					}
					breakMessage = breakItem.text;
					return true;
				}
			})) {
				this.showMessage(breakMessage);
				this.status = true;
			}
			else {
				this.hideMessage();
				this.status = false;
			}
		}

		skipTo(time) {
			paella.player.videoContainer.trimming()
				.then((trimming) => {
					if (trimming.enabled) {
						paella.player.videoContainer.seekToTime(time - trimming.start);
					}
					else {
						paella.player.videoContainer.seekToTime(time);
					}
				})
		}

		showMessage(text) {
			if (this.currentText != text) {
				if (this.messageContainer) {
					paella.player.videoContainer.overlayContainer.removeElement(this.messageContainer);
				}
				var rect = {
					left: 100,
					top: 350,
					width: 1080,
					height: 40
				};
				this.currentText = text;
				this.messageContainer = paella.player.videoContainer.overlayContainer.addText(paella.utils.dictionary.translate(text), rect);
				this.messageContainer.className = 'textBreak';
				this.currentText = text;
			}
		}

		hideMessage() {
			if (this.messageContainer) {
				paella.player.videoContainer.overlayContainer.removeElement(this.messageContainer);
				this.messageContainer = null;
			}
			this.currentText = "";
		}
	}
});


paella.addPlugin(() => {
    return class BufferedPlaybackCanvasPlugin extends paella.PlaybackCanvasPlugin {
        getName() { return "es.upv.paella.BufferedPlaybackCanvasPlugin"; }

        setup() {

        }

        drawCanvas(context,width,height,videoData) {
            function trimmedInstant(t) {
                t = videoData.trimming.enabled ? t - videoData.trimming.start : t;
                return t * width / videoData.trimming.duration;
            }

            let buffered = paella.player.videoContainer.streamProvider.buffered; 
            for (let i = 0; i<buffered.length; ++i) {
                let start = trimmedInstant(buffered.start(i));
                let end = trimmedInstant(buffered.end(i));
                this.drawBuffer(context,start,end,height);
            }
        }

        drawBuffer(context,start,end,height) {
            context[0].fillStyle = this.config.color;
            context[0].fillRect(start, 0, end, height);
        }
    }
})
paella.addPlugin(function() {
		
	/////////////////////////////////////////////////
	// DFXP Parser
	/////////////////////////////////////////////////
	return class DFXPParserPlugin extends paella.CaptionParserPlugIn {
		get ext() { return ["dfxp"] }
		getName() { return "es.upv.paella.captions.DFXPParserPlugin"; }
		parse(content, lang, next) {
			var captions = [];
			var self = this;
			var xmlDoc = $.parseXML(content);
			var xml = $(xmlDoc);
			var g_lang = xml.attr("xml:lang");
			
			var lls = xml.find("div");
			for(var idx=0; idx<lls.length; ++idx) {
				var ll = $(lls[idx]);
				var l_lang = ll.attr("xml:lang");
				if ((l_lang == undefined) || (l_lang == "")){
					if ((g_lang == undefined) || (g_lang == "")) {
						paella.log.debug("No xml:lang found! Using '" + lang + "' lang instead.");
						l_lang = lang;
					}
					else {
						l_lang = g_lang;
					}
				}
				//
				if (l_lang == lang) {
					ll.find("p").each(function(i, cap){
						var c = {
							id: i,
							begin: self.parseTimeTextToSeg(cap.getAttribute("begin")),
							end: self.parseTimeTextToSeg(cap.getAttribute("end")),
							content: $(cap).text().trim()
						};				
						captions.push(c);				
					});
					break;
				}
			}
			
			if (captions.length > 0) {
				next(false, captions);
			}
			else {
				next(true);
			}
		}

		parseTimeTextToSeg(ttime){
				var nseg = 0;
				var segtime = /^([0-9]*([.,][0-9]*)?)s/.test(ttime);
				if (segtime){
						nseg = parseFloat(RegExp.$1);
				}
				else {
						var split = ttime.split(":");
						var h = parseInt(split[0]);
						var m = parseInt(split[1]);
						var s = parseInt(split[2]);
						nseg = s+(m*60)+(h*60*60);
				}
				return nseg;
		}
	}
});

paella.addPlugin(function() {
	return class CaptionsPlugin extends paella.ButtonPlugin {
		getInstanceName() { return "captionsPlugin"; }	// plugin instance will be available in paella.plugins.captionsPlugin
		getAlignment() { return 'right'; }
		getSubclass() { return 'captionsPluginButton'; }
		getIconClass() { return 'icon-captions'; }
		getName() { return "es.upv.paella.captionsPlugin"; }
		getButtonType() { return paella.ButtonPlugin.type.popUpButton; }
		getDefaultToolTip() { return paella.utils.dictionary.translate("Subtitles"); }
		getIndex() { return 509; }
		closeOnMouseOut() { return false; }
	
		checkEnabled(onSuccess) {
			this._searchTimerTime = 1500;
			this._searchTimer = null;
			this._pluginButton = null;
			this._open = 0; // 0 closed, 1 st clic;
			this._parent = null;
			this._body = null;
			this._inner = null;
			this._bar = null;
			this._input = null;
			this._select = null;
			this._editor = null;
			this._activeCaptions = null;
			this._lastSel = null;
			this._browserLang = null;
			this._defaultBodyHeight = 280;
			this._autoScroll = true;
			this._searchOnCaptions = null;

			onSuccess(true);
		}
	
		showUI(){
			if(paella.captions.getAvailableLangs().length>=1){
				super.showUI();
			}
		}
	
		setup() {
			var self = this;
	
			// HIDE UI IF NO Captions
			if(!paella.captions.getAvailableLangs().length){
				paella.plugins.captionsPlugin.hideUI();
			}
	
			//BINDS
			paella.events.bind(paella.events.captionsEnabled,function(event,params){
				self.onChangeSelection(params);
			});
	
			paella.events.bind(paella.events.captionsDisabled,function(event,params){
				self.onChangeSelection(params);
			});
	
			paella.events.bind(paella.events.captionAdded,function(event,params){
				self.onCaptionAdded(params);
				paella.plugins.captionsPlugin.showUI();
			});
	
			paella.events.bind(paella.events.timeUpdate, function(event,params){
				if(self._searchOnCaptions){
					self.updateCaptionHiglighted(params);				
				}
	
			});
	
			paella.events.bind(paella.events.controlBarWillHide, function(evt) {
				self.cancelHideBar();
			});
	
			self._activeCaptions = paella.captions.getActiveCaptions();
	
			self._searchOnCaptions = self.config.searchOnCaptions || false;
		}
	
		cancelHideBar() {
			var thisClass = this;
			if(thisClass._open > 0){
				paella.player.controls.cancelHideBar();
			}
		}
	
		updateCaptionHiglighted(time) {
			var thisClass = this;
			var sel = null;
			var id = null;
			if(time){
				paella.player.videoContainer.trimming()
					.then((trimming) => {
						let offset = trimming.enabled ? trimming.start : 0;
						let c = paella.captions.getActiveCaptions();
						let caption = c && c.getCaptionAtTime(time.currentTime + offset);
						let id = caption && caption.id;
	
						if(id != null){
							sel = $( ".bodyInnerContainer[sec-id='"+id+"']" );
	
							if(sel != thisClass._lasSel){
								$(thisClass._lasSel).removeClass("Highlight");
							}
	
							if(sel){
								$(sel).addClass("Highlight");
								if(thisClass._autoScroll){
									thisClass.updateScrollFocus(id);
								}
								thisClass._lasSel = sel;
							}
						}
					});
			}
		}
	
		updateScrollFocus(id) {
			var thisClass = this;
			var resul = 0;
			var t = $(".bodyInnerContainer").slice(0,id);
			t = t.toArray();
	
			t.forEach(function(l){
				var i = $(l).outerHeight(true);
				resul += i;
			});
	
			var x = parseInt(resul / 280);
			$(".captionsBody").scrollTop( x*thisClass._defaultBodyHeight );
		}
	
		onCaptionAdded(obj) {
			var thisClass = this;
	
			var newCap = paella.captions.getCaptions(obj);
	
			var defOption = document.createElement("option"); // NO ONE SELECT
			defOption.text = newCap._lang.txt;
			defOption.value = obj;
	
			thisClass._select.add(defOption);
		}
	
		changeSelection() {
			var thisClass = this;
	
			var sel = $(thisClass._select).val();
			   if(sel == ""){ 
				   $(thisClass._body).empty();
				   paella.captions.setActiveCaptions(sel);
				   return;
			   } // BREAK IF NO ONE SELECTED
			paella.captions.setActiveCaptions(sel);
			thisClass._activeCaptions = sel;
			if(thisClass._searchOnCaptions){
				thisClass.buildBodyContent(paella.captions.getActiveCaptions()._captions,"list");	
			}
			thisClass.setButtonHideShow();
		}
		
		onChangeSelection(obj) {
			var thisClass = this;
	
			if(thisClass._activeCaptions != obj){
				$(thisClass._body).empty();
				if(obj==undefined){
					thisClass._select.value = "";
					$(thisClass._input).prop('disabled', true);
				}
				else{
					$(thisClass._input).prop('disabled', false);
					thisClass._select.value = obj;
					if(thisClass._searchOnCaptions){
						thisClass.buildBodyContent(paella.captions.getActiveCaptions()._captions,"list");
					}
				}
				thisClass._activeCaptions = obj;
				thisClass.setButtonHideShow();
			}
		}
	
		action() {
			var self = this;
			self._browserLang = paella.utils.dictionary.currentLanguage();
			self._autoScroll = true;
	
			switch(self._open){
				case 0:
					if(self._browserLang && paella.captions.getActiveCaptions()==undefined){
						self.selectDefaultBrowserLang(self._browserLang);
					}
					self._open = 1;
					paella.keyManager.enabled = false;
					break;
			
				case 1: 
					paella.keyManager.enabled = true;
					self._open = 0;
					break;
			}
		}
	
		buildContent(domElement) {
			var thisClass = this;
	
			//captions CONTAINER
			thisClass._parent = document.createElement('div');
			thisClass._parent.className = 'captionsPluginContainer';  
			//captions BAR
			   thisClass._bar = document.createElement('div');
			thisClass._bar.className = 'captionsBar';
			//captions BODY
			if(thisClass._searchOnCaptions){
				thisClass._body = document.createElement('div');
				thisClass._body.className = 'captionsBody';
				thisClass._parent.appendChild(thisClass._body);
				 //BODY JQUERY
				$(thisClass._body).scroll(function(){
					thisClass._autoScroll = false;
				});
	
				//INPUT
				thisClass._input = document.createElement("input");
				thisClass._input.className = "captionsBarInput";
				thisClass._input.type = "text";
				thisClass._input.id ="captionsBarInput";
				thisClass._input.name = "captionsString";
				thisClass._input.placeholder = paella.utils.dictionary.translate("Search captions");
				thisClass._bar.appendChild(thisClass._input);
	
				//INPUT jQuery
				 $(thisClass._input).change(function(){
					var text = $(thisClass._input).val();
					thisClass.doSearch(text);
				});
	
				$(thisClass._input).keyup(function(){
					var text = $(thisClass._input).val();
					if(thisClass._searchTimer != null){
						thisClass._searchTimer.cancel();
					}
					thisClass._searchTimer = new paella.utils.Timer(function(timer) {
						thisClass.doSearch(text);
					}, thisClass._searchTimerTime);			
				});
			}
	
				
	
			//SELECT
			thisClass._select = document.createElement("select");
			thisClass._select.className = "captionsSelector";
			
			var defOption = document.createElement("option"); // NO ONE SELECT
			defOption.text = paella.utils.dictionary.translate("None");
			defOption.value = "";
			thisClass._select.add(defOption);
	
			paella.captions.getAvailableLangs().forEach(function(l){
				var option = document.createElement("option");
				option.text = l.lang.txt;
				option.value = l.id;
				thisClass._select.add(option);
			});
	
			 thisClass._bar.appendChild(thisClass._select);
			 thisClass._parent.appendChild( thisClass._bar);
	
			//jQuery SELECT
			$(thisClass._select).change(function(){
			   thisClass.changeSelection();
			});
	
			//BUTTON EDITOR
			thisClass._editor = document.createElement("button");
			thisClass._editor.className = "editorButton";
			thisClass._editor.innerText = "";
			thisClass._bar.appendChild(thisClass._editor);
	
			//BUTTON jQuery
			$(thisClass._editor).prop("disabled",true);
			$(thisClass._editor).click(function(){
				var c = paella.captions.getActiveCaptions();        	
				paella.userTracking.log("paella:caption:edit", {id: c._captionsProvider + ':' + c._id, lang: c._lang});
				c.goToEdit();
			});
	
			domElement.appendChild(thisClass._parent);
		}
	
		selectDefaultBrowserLang(code) {
			var thisClass = this;
			var provider = null;
			paella.captions.getAvailableLangs().forEach(function(l){
				if(l.lang.code == code){ provider = l.id; }
			});
			
			if(provider){
				paella.captions.setActiveCaptions(provider);
			}
			/*
			else{
				$(thisClass._input).prop("disabled",true);
			}
			*/
	
		}
	
		doSearch(text) {
			var thisClass = this;
			var c = paella.captions.getActiveCaptions();
			if(c){
				if(text==""){thisClass.buildBodyContent(paella.captions.getActiveCaptions()._captions,"list");}
				else{
					c.search(text,function(err,resul){
						if(!err){
							thisClass.buildBodyContent(resul,"search");
						}
					});
				}
			}
		}
	
		setButtonHideShow() {
			var thisClass = this;
			var editor = $('.editorButton');
			var c = paella.captions.getActiveCaptions();
			var res = null;
			   if(c!=null){
				   $(thisClass._select).width('39%');
				
				c.canEdit(function(err, r){res=r;});
				if(res){
					$(editor).prop("disabled",false);
					$(editor).show();
				}
				else{
					$(editor).prop("disabled",true);
					$(editor).hide();
					$(thisClass._select).width('47%');
				}
			}
			else {
				$(editor).prop("disabled",true);
				$(editor).hide();
				$(thisClass._select).width('47%');
			}

			if(!thisClass._searchOnCaptions){
				if(res){$(thisClass._select).width('92%');}
				else{$(thisClass._select).width('100%');}
			 }
		}

		buildBodyContent(obj,type) {
			paella.player.videoContainer.trimming()
				.then((trimming)=>{
					var thisClass = this;
					$(thisClass._body).empty();
					obj.forEach(function(l){
						if(trimming.enabled && (l.end<trimming.start || l.begin>trimming.end)){
							return;
						}
						thisClass._inner = document.createElement('div');
						thisClass._inner.className = 'bodyInnerContainer';
						thisClass._inner.innerText = l.content;
						if(type=="list"){
							thisClass._inner.setAttribute('sec-begin',l.begin);
							thisClass._inner.setAttribute('sec-end',l.end);
							thisClass._inner.setAttribute('sec-id',l.id);
							thisClass._autoScroll = true;
						}
						if(type=="search"){
							thisClass._inner.setAttribute('sec-begin',l.time);
						}
						thisClass._body.appendChild(thisClass._inner);
	
						//JQUERY
						$(thisClass._inner).hover(
							function(){ 
								$(this).css('background-color','rgba(250, 161, 102, 0.5)');	           		
							},
							function(){ 
								$(this).removeAttr('style');
							}
						);
						$(thisClass._inner).click(function(){ 
								var secBegin = $(this).attr("sec-begin");
								paella.player.videoContainer.trimming()
									.then((trimming) => {
										let offset = trimming.enabled ? trimming.start : 0;
										paella.player.videoContainer.seekToTime(secBegin - offset + 0.1);
									});
						});
					});
			});
		}
	}
	
});
paella.addPlugin(function() {
	return class CaptionsOnScreen extends paella.EventDrivenPlugin {
		
		checkEnabled(onSuccess) {
			this.containerId = 'paella_plugin_CaptionsOnScreen';
			this.container = null;
			this.innerContainer = null;
			this.top = null;
			this.actualPos = null;
			this.lastEvent = null;
			this.controlsPlayback = null;
			this.captions = false;
			this.captionProvider = null;
			onSuccess(!paella.player.isLiveStream());
		}

		setup() {
		}

		getEvents() {
			return [paella.events.controlBarDidHide, paella.events.resize, paella.events.controlBarDidShow, paella.events.captionsEnabled, paella.events.captionsDisabled ,paella.events.timeUpdate];
		}

		onEvent(eventType,params) {
			var thisClass = this;

			switch (eventType) {
				case paella.events.controlBarDidHide:
					if(thisClass.lastEvent == eventType || thisClass.captions==false)break;
					thisClass.moveCaptionsOverlay("down");
					break;
				case paella.events.resize:
					if(thisClass.captions==false)break;
					if(paella.player.controls.isHidden()){
						thisClass.moveCaptionsOverlay("down");
					}
					else {
						thisClass.moveCaptionsOverlay("top");
					}
					break;

				case paella.events.controlBarDidShow:
					if(thisClass.lastEvent == eventType || thisClass.captions==false)break;
					thisClass.moveCaptionsOverlay("top");
					break;
				case paella.events.captionsEnabled:
					thisClass.buildContent(params);
					thisClass.captions = true;
					if(paella.player.controls.isHidden()){
						thisClass.moveCaptionsOverlay("down");
					}
					else {
						thisClass.moveCaptionsOverlay("top");
					}
					break;
				case paella.events.captionsDisabled:
					thisClass.hideContent();
					thisClass.captions = false;
					break;
				case paella.events.timeUpdate:
					if(thisClass.captions){ thisClass.updateCaptions(params); }
					break;

			}
			thisClass.lastEvent = eventType; 
		}

		buildContent(provider){
			var thisClass = this;
			thisClass.captionProvider = provider;

			if(thisClass.container==null){ // PARENT
				thisClass.container = document.createElement('div');
				thisClass.container.className = "CaptionsOnScreen";
				thisClass.container.id = thisClass.containerId;

				thisClass.innerContainer = document.createElement('div');
				thisClass.innerContainer.className = "CaptionsOnScreenInner";

				thisClass.container.appendChild(thisClass.innerContainer);			

				if(thisClass.controlsPlayback==null) thisClass.controlsPlayback = $('#playerContainer_controls_playback');


				paella.player.videoContainer.domElement.appendChild(thisClass.container);
			}
			else {
				$(thisClass.container).show();
			}
		}

		updateCaptions(time){
			if (this.captions) {
				paella.player.videoContainer.trimming()
					.then((trimming) => {
						let offset = trimming.enabled ? trimming.start : 0;
						var c = paella.captions.getActiveCaptions();
						var caption = c.getCaptionAtTime(time.currentTime + offset);
						if(caption){
							$(this.container).show();
							this.innerContainer.innerText = caption.content;
							this.moveCaptionsOverlay("auto");

						}
						else { 
							this.innerContainer.innerText = ""; 
							this.hideContent();
						}
					});
			}
		}
		
		hideContent(){
			var thisClass = this;

			$(thisClass.container).hide();
		}
	
		moveCaptionsOverlay(pos){
			var thisClass = this;
			var marginbottom = 10;

			if(thisClass.controlsPlayback==null) thisClass.controlsPlayback = $('#playerContainer_controls_playback');

			if(pos=="auto" || pos==undefined) {
				pos = paella.player.controls.isHidden() ? "down" : "top";
			}
			if(pos=="down"){
				var t = thisClass.container.offsetHeight;
				t -= thisClass.innerContainer.offsetHeight + marginbottom;
				thisClass.innerContainer.style.bottom = (0 - t) + "px";
			}
			if(pos=="top") {
				var t2 = thisClass.controlsPlayback.offset().top;
				t2 -= thisClass.innerContainer.offsetHeight + marginbottom;
				thisClass.innerContainer.style.bottom = (0-t2)+"px";
			}
		}

		getIndex() {
			return 1050;
		}

		getName() {
			return "es.upv.paella.overlayCaptionsPlugin";
		}
	}
});



(() => {

	function buildChromaVideoCanvas(stream, canvas) {
		class ChromaVideoCanvas extends bg.app.WindowController {
	
			constructor(stream) {
				super();
				this.stream = stream;
				this._chroma = bg.Color.White();
				this._crop = new bg.Vector4(0.3,0.01,0.3,0.01);
				this._transform = bg.Matrix4.Identity().translate(0.6,-0.04,0);
				this._bias = 0.01;
			}
	
			get chroma() { return this._chroma; }
			get bias() { return this._bias; }
			get crop() { return this._crop; }
			get transform() { return this._transform; }
			set chroma(c) { this._chroma = c; }
			set bias(b) { this._bias = b; }
			set crop(c) { this._crop = c; }
			set transform(t) { this._transform = t; }
	
			get video() {
				return this.texture ? this.texture.video : null;
			}
	
			loaded() {
				return new Promise((resolve) => {
					let checkLoaded = () => {
						if (this.video) {
							resolve(this);
						}
						else {
							setTimeout(checkLoaded,100);
						}
					}
					checkLoaded();
				});
			}
	
			buildShape() {
				this.plist = new bg.base.PolyList(this.gl);
				
				this.plist.vertex = [ -1,-1,0, 1,-1,0, 1,1,0, -1,1,0, ];
				this.plist.texCoord0 = [ 0,0, 1,0, 1,1, 0,1 ];
				this.plist.index = [ 0, 1, 2, 2, 3, 0 ];
				
				this.plist.build();
			}
			
			buildShader() {
				let vshader = `
						attribute vec4 position;
						attribute vec2 texCoord;
						uniform mat4 inTransform;
						varying vec2 vTexCoord;
						void main() {
							gl_Position = inTransform * position;
							vTexCoord = texCoord;
						}
					`;
				let fshader = `
						precision mediump float;
						varying vec2 vTexCoord;
						uniform sampler2D inTexture;
						uniform vec4 inChroma;
						uniform float inBias;
						uniform vec4 inCrop;
						void main() {
							vec4 result = texture2D(inTexture,vTexCoord);
							
							if ((result.r>=inChroma.r-inBias && result.r<=inChroma.r+inBias &&
								result.g>=inChroma.g-inBias && result.g<=inChroma.g+inBias &&
								result.b>=inChroma.b-inBias && result.b<=inChroma.b+inBias) ||
								(vTexCoord.x<inCrop.x || vTexCoord.x>inCrop.z || vTexCoord.y<inCrop.w || vTexCoord.y>inCrop.y)
							)
							{
								discard;
							}
							else {
								gl_FragColor = result;
							}
						}
					`;
				
				this.shader = new bg.base.Shader(this.gl);
				this.shader.addShaderSource(bg.base.ShaderType.VERTEX, vshader);
	
				this.shader.addShaderSource(bg.base.ShaderType.FRAGMENT, fshader);
	
				status = this.shader.link();
				if (!this.shader.status) {
					console.log(this.shader.compileError);
					console.log(this.shader.linkError);
				}
				
				this.shader.initVars(["position","texCoord"],["inTransform","inTexture","inChroma","inBias","inCrop"]);
			}
			
			init() {
				// Use WebGL V1 engine
				bg.Engine.Set(new bg.webgl1.Engine(this.gl));
	
				bg.base.Loader.RegisterPlugin(new bg.base.VideoTextureLoaderPlugin());
				
				this.buildShape();
				this.buildShader();
						
				this.pipeline = new bg.base.Pipeline(this.gl);
				bg.base.Pipeline.SetCurrent(this.pipeline);
				this.pipeline.clearColor = bg.Color.Transparent();
	
				bg.base.Loader.Load(this.gl,this.stream.src)
					.then((texture) => {
						this.texture = texture;
					});
			}
	
			frame(delta) {
				if (this.texture) {
					this.texture.update();
				}
			}
			
			display() {
				this.pipeline.clearBuffers(bg.base.ClearBuffers.COLOR | bg.base.ClearBuffers.DEPTH);
				
				if (this.texture) {
					this.shader.setActive();
					this.shader.setInputBuffer("position",this.plist.vertexBuffer,3);
					this.shader.setInputBuffer("texCoord",this.plist.texCoord0Buffer,2);
					this.shader.setMatrix4("inTransform",this.transform);
					this.shader.setTexture("inTexture",this.texture || bg.base.TextureCache.WhiteTexture(this.gl),bg.base.TextureUnit.TEXTURE_0);
					this.shader.setVector4("inChroma",this.chroma);
					this.shader.setValueFloat("inBias",this.bias);
					this.shader.setVector4("inCrop",new bg.Vector4(this.crop.x, 1.0 - this.crop.y, 1.0 - this.crop.z, this.crop.w));
					this.plist.draw();
					
					this.shader.disableInputBuffer("position");
					this.shader.disableInputBuffer("texCoord");
					this.shader.clearActive();
				}
			}
			
			reshape(width,height) {
				let canvas = this.canvas.domElement;
				canvas.width = width;
				canvas.height = height;
				this.pipeline.viewport = new bg.Viewport(0,0,width,height);
			}
			
			mouseMove(evt) { this.postRedisplay(); }
		}
	
		let controller = new ChromaVideoCanvas(stream);
		let mainLoop = bg.app.MainLoop.singleton;
	
		mainLoop.updateMode = bg.app.FrameUpdate.AUTO;
		mainLoop.canvas = canvas;
		mainLoop.run(controller);
	
		return controller.loaded();
	}
	
	class ChromaVideo extends paella.VideoElementBase {
		
		constructor(id,stream,left,top,width,height,streamName) {
			super(id,stream,'canvas',left,top,width,height);

			this._posterFrame = null;
			this._currentQuality = null;
			this._autoplay = false;
			this._streamName = null;
			this._streamName = streamName || 'chroma';
			var This = this;
	
			if (this._stream.sources[this._streamName]) {
				this._stream.sources[this._streamName].sort(function (a, b) {
					return a.res.h - b.res.h;
				});
			}
	
			this.video = null;
	
			function onProgress(event) {
				if (!This._ready && This.video.readyState==4) {
					This._ready = true;
					if (This._initialCurrentTipe!==undefined) {
						This.video.currentTime = This._initialCurrentTime;
						delete This._initialCurrentTime;
					}
					This._callReadyEvent();
				}
			}
	
			function evtCallback(event) { onProgress.apply(This,event); }
	
			function onUpdateSize() {
				if (This.canvasController) {
					let canvas = This.canvasController.canvas.domElement;
					This.canvasController.reshape($(canvas).width(),$(canvas).height());
				}
			}
	
			let timer = new paella.Timer(function(timer) {
				onUpdateSize();
			},500);
			timer.repeat = true;
		}
	
		defaultProfile() {
			return 'chroma';
		}
	
		_setVideoElem(video) {
			$(this.video).bind('progress', evtCallback);
			$(this.video).bind('loadstart',evtCallback);
			$(this.video).bind('loadedmetadata',evtCallback);
			$(this.video).bind('canplay',evtCallback);
			$(this.video).bind('oncanplay',evtCallback);
		}
	
		_loadDeps() {
			return new Promise((resolve,reject) => {
				if (!window.$paella_bg2e) {
					paella.require(paella.baseUrl + 'javascript/bg2e-es2015.js')
						.then(() => {
							window.$paella_bg2e = bg;
							resolve(window.$paella_bg2e);
						})
						.catch((err) => {
							console.error(err.message);
							reject();
						});
				}
				else {
					defer.resolve(window.$paella_bg2e);
				}
			});
		}
	
		_deferredAction(action) {
			return new Promise((resolve,reject) => {
				if (this.video) {
					resolve(action());
				}
				else {
					$(this.video).bind('canplay',() => {
						this._ready = true;
						resolve(action());
					});
				}
			});
		}
	
		_getQualityObject(index, s) {
			return {
				index: index,
				res: s.res,
				src: s.src,
				toString:function() { return this.res.w + "x" + this.res.h; },
				shortLabel:function() { return this.res.h + "p"; },
				compare:function(q2) { return this.res.w*this.res.h - q2.res.w*q2.res.h; }
			};
		}
	
		// Initialization functions
		
		getVideoData() {
			var This = this;
			return new Promise((resolve,reject) => {
				this._deferredAction(() => {
					resolve({
						duration: This.video.duration,
						currentTime: This.video.currentTime,
						volume: This.video.volume,
						paused: This.video.paused,
						ended: This.video.ended,
						res: {
							w: This.video.videoWidth,
							h: This.video.videoHeight
						}
					});
				});
			});
		}
		
		setPosterFrame(url) {
			this._posterFrame = url;
		}
	
		setAutoplay(auto) {
			this._autoplay = auto;
			if (auto && this.video) {
				this.video.setAttribute("autoplay",auto);
			}
		}
	
		load() {
			var This = this;
			return new Promise((resolve,reject) => {
				this._loadDeps() 
					.then(() => {
						var sources = this._stream.sources[this._streamName];
						if (this._currentQuality===null && this._videoQualityStrategy) {
							this._currentQuality = this._videoQualityStrategy.getQualityIndex(sources);
						}
	
						var stream = this._currentQuality<sources.length ? sources[this._currentQuality]:null;
						this.video = null;
						this.domElement.parentNode.style.backgroundColor = "transparent";
						if (stream) {
							this.canvasController = null;
							buildChromaVideoCanvas(stream,this.domElement)
								.then((canvasController) => {
									this.canvasController = canvasController;
									this.video = canvasController.video;
									this.video.pause();
									if (stream.crop) {
										this.canvasController.crop = new bg.Vector4(stream.crop.left,stream.crop.top,stream.crop.right,stream.crop.bottom);
									}
									if (stream.displacement) {
										this.canvasController.transform = bg.Matrix4.Translation(stream.displacement.x, stream.displacement.y, 0);
									}
									if (stream.chromaColor) {
										this.canvasController.chroma = new bg.Color(stream.chromaColor[0],
																						 stream.chromaColor[1],
																						 stream.chromaColor[2],
																						 stream.chromaColor[3])
									}
									if (stream.chromaBias) {
										this.canvasController.bias = stream.chromaBias;
									}
									resolve(stream);
								});
						}
						else {
							reject(new Error("Could not load video: invalid quality stream index"));
						}
					});
			});
		}
	
		getQualities() {
			return new Promise((resolve,reject) => {
				setTimeout(() => {
					var result = [];
					var sources = this._stream.sources[this._streamName];
					var index = -1;
					sources.forEach((s) => {
						index++;
						result.push(this._getQualityObject(index,s));
					});
					resolve(result);
				},10);
			});
		}
	
		setQuality(index) {
			return new Promise((resolve) => {
				var paused = this.video.paused;
				var sources = this._stream.sources[this._streamName];
				this._currentQuality = index<sources.length ? index:0;
				var currentTime = this.video.currentTime;
				this.freeze()
	
					.then(() => {
						this._ready = false;
						return this.load();
					})
	
					.then(() => {
						if (!paused) {
							this.play();
						}
						$(this.video).on('seeked',() => {
							this.unFreeze();
							resolve();
							$(this.video).off('seeked');
						});
						this.video.currentTime = currentTime;
					});
			});
		}
	
		getCurrentQuality() {
			return new Promise((resolve) => {	
				resolve(this._getQualityObject(this._currentQuality,this._stream.sources[this._streamName][this._currentQuality]));
			});
		}
	
		play() {
			return this._deferredAction(() => {
				bg.app.MainLoop.singleton.updateMode = bg.app.FrameUpdate.AUTO;
				this.video.play();
			});
		}
	
		pause() {
			return this._deferredAction(() => {
				bg.app.MainLoop.singleton.updateMode = bg.app.FrameUpdate.MANUAL;
				this.video.pause();
			});
		}
	
		isPaused() {
			return this._deferredAction(() => {
				return this.video.paused;
			});
		}
	
		duration() {
			return this._deferredAction(() => {
				return this.video.duration;
			});
		}
	
		setCurrentTime(time) {
			return this._deferredAction(() => {
				this.video.currentTime = time;
				$(this.video).on('seeked',() => {
					this.canvasController.postRedisplay();
					$(this.video).off('seeked');
				});
			});
		}
	
		currentTime() {
			return this._deferredAction(() => {
				return this.video.currentTime;
			});
		}
	
		setVolume(volume) {
			return this._deferredAction(() => {
				this.video.volume = volume;
			});
		}
	
		volume() {
			return this._deferredAction(() => {
				return this.video.volume;
			});
		}
	
		setPlaybackRate(rate) {
			return this._deferredAction(() => {
				this.video.playbackRate = rate;
			});
		}
	
		playbackRate() {
			return this._deferredAction(() => {
				return this.video.playbackRate;
			});
		}
	
		goFullScreen() {
			return this._deferredAction(() => {
				var elem = this.video;
				if (elem.requestFullscreen) {
					elem.requestFullscreen();
				}
				else if (elem.msRequestFullscreen) {
					elem.msRequestFullscreen();
				}
				else if (elem.mozRequestFullScreen) {
					elem.mozRequestFullScreen();
				}
				else if (elem.webkitEnterFullscreen) {
					elem.webkitEnterFullscreen();
				}
			});
		}
	
		unFreeze(){
			return this._deferredAction(() => {
				var c = document.getElementById(this.video.className + "canvas");
				$(c).remove();
			});
		}
		
		freeze(){
			var This = this;
			return this._deferredAction(function() {});
		}
	
		unload() {
			this._callUnloadEvent();
			return paella_DeferredNotImplemented();
		}
	
		getDimensions() {
			return paella_DeferredNotImplemented();
		}
	}
	
	paella.ChromaVideo = ChromaVideo;
	
	class ChromaVideoFactory extends paella.VideoFactory {
		isStreamCompatible(streamData) {
			try {
				if (paella.ChromaVideo._loaded) {
					return false;
				}
				if (paella.videoFactories.Html5VideoFactory.s_instances>0 && 
					paella.utils.userAgent.system.iOS)
				{
					return false;
				}
				for (var key in streamData.sources) {
					if (key=='chroma') return true;
				}
			}
			catch (e) {}
			return false;
		}
	
		getVideoObject(id, streamData, rect) {
			paella.ChromaVideo._loaded = true;
			++paella.videoFactories.Html5VideoFactory.s_instances;
			return new paella.ChromaVideo(id, streamData, rect.x, rect.y, rect.w, rect.h);
		}
	}

	paella.videoFactories.ChromaVideoFactory = ChromaVideoFactory;

})();

/*
paella.addPlugin(function() {
	return class CommentsPlugin extends paella.TabBarPlugin {
		get divPublishComment() { return this._divPublishComment; }
		set divPublishComment(v) { this._divPublishComment = v; }
		get divComments() { return this._divComments; }
		set divComments(v) { this._divComments = v; }
		get publishCommentTextArea() { return this._publishCommentTextArea; }
		set publishCommentTextArea(v) { this._publishCommentTextArea = v; }
		get publishCommentButtons() { return this._publishCommentButtons; }
		set publishCommentButtons(v) { this._publishCommentButtons = v; }
		get canPublishAComment() { return this._canPublishAComment; }
		set canPublishAComment(v) { this._canPublishAComment = v; }
		get comments() { return this._comments; }
		set comments(v) { this._comments = v; }
		get commentsTree() { return this._commentsTree; }
		set commentsTree(v) { this._commentsTree = v; }
		get domElement() { return this._domElement; }
		set domElement(v) { this._domElement = v; }
	
		getSubclass() { return "showCommentsTabBar"; }
		getName() { return "es.upv.paella.commentsPlugin"; }
		getTabName() { return paella.utils.dictionary.translate("Comments"); }
		checkEnabled(onSuccess) { onSuccess(true); }
		getIndex() { return 40; }
		getDefaultToolTip() { return paella.utils.dictionary.translate("Comments"); }
						
		action(tab) {
			this.loadContent();
		}
				
		buildContent(domElement) {
			this.domElement = domElement;
			this.canPublishAComment = paella.initDelegate.initParams.accessControl.permissions.canWrite;
			this.loadContent();
		}
					
		loadContent() {
			this.divRoot = this.domElement;
			this.divRoot.innerText ="";
			
			this.divPublishComment = document.createElement('div');
			this.divPublishComment.className = 'CommentPlugin_Publish';
			this.divPublishComment.id = 'CommentPlugin_Publish';

			this.divComments = document.createElement('div'); 
			this.divComments.className = 'CommentPlugin_Comments';
			this.divComments.id = 'CommentPlugin_Comments';

			if(this.canPublishAComment){
				this.divRoot.appendChild(this.divPublishComment);
				this.createPublishComment();
			}
			this.divRoot.appendChild(this.divComments);
			
			this.reloadComments();
		}
		
		//Allows the user to write a new comment
		createPublishComment() {
			var thisClass = this;
			var rootID = this.divPublishComment.id+"_entry";
			
			var divEntry;
			divEntry = document.createElement('div');
			divEntry.id = rootID;
			divEntry.className = 'comments_entry';
			
			var divSil;
			divSil = document.createElement('img');
			divSil.className = "comments_entry_silhouette";
			divSil.style.width = "48px";
			divSil.src = paella.initDelegate.initParams.accessControl.userData.avatar;
			divSil.id = rootID+"_silhouette";
			divEntry.appendChild(divSil);
			
			var divTextAreaContainer;
			divTextAreaContainer = document.createElement('div');
			divTextAreaContainer.className = "comments_entry_container";
			divTextAreaContainer.id = rootID+"_textarea_container";
			divEntry.appendChild(divTextAreaContainer);
			
			this.publishCommentTextArea = document.createElement('textarea');
			this.publishCommentTextArea.id = rootID+"_textarea";
			this.publishCommentTextArea.onclick = function(){paella.keyManager.enabled = false;};
			this.publishCommentTextArea.onblur = function(){paella.keyManager.enabled = true;};
			divTextAreaContainer.appendChild(this.publishCommentTextArea);
			
			this.publishCommentButtons = document.createElement('div');
			this.publishCommentButtons.id = rootID+"_buttons_area";
			divTextAreaContainer.appendChild(this.publishCommentButtons);
			
			var btnAddComment;
			btnAddComment = document.createElement('button');
			btnAddComment.id = rootID+"_btnAddComment";
			btnAddComment.className = "publish";
			btnAddComment.onclick = function(){
				var txtValue = thisClass.publishCommentTextArea.value;
				if (txtValue.replace(/\s/g,'') != "") {
					thisClass.addComment();
				}
			};
			btnAddComment.innerText = paella.utils.dictionary.translate("Publish");
			
			this.publishCommentButtons.appendChild(btnAddComment);
			
			divTextAreaContainer.commentsTextArea = this.publishCommentTextArea;
			divTextAreaContainer.commentsBtnAddComment = btnAddComment;
			divTextAreaContainer.commentsBtnAddCommentToInstant = this.btnAddCommentToInstant;
			
			this.divPublishComment.appendChild(divEntry);
		}
			
		addComment() {
			var thisClass = this;
			var txtValue = paella.AntiXSS.htmlEscape(thisClass.publishCommentTextArea.value);
			//var txtValue = thisClass.publishCommentTextArea.value;
			var now = new Date();
			
			this.comments.push({
				id: base.uuid(),
				userName:paella.initDelegate.initParams.accessControl.userData.name,
				mode: "normal",
				value: txtValue,
				created: now
			});

			var data = {
				allComments: this.comments
			};
			
			paella.data.write('comments',{id:paella.initDelegate.getId()},data,function(response,status){
				if (status) {thisClass.loadContent();}
			});
		}
		
		addReply(annotationID, domNodeId) {
			var thisClass = this;
			var textArea = document.getElementById(domNodeId);
			var txtValue = paella.AntiXSS.htmlEscape(textArea.value);
			var now = new Date();
			
			paella.keyManager.enabled = true;

			this.comments.push({
				id: base.uuid(),
				userName:paella.initDelegate.initParams.accessControl.userData.name,
				mode: "reply",
				parent: annotationID,
				value: txtValue,
				created: now
			});

			var data = {
				allComments: this.comments
			};
			
			paella.data.write('comments',{id:paella.initDelegate.getId()},data,function(response,status){
				if (status) thisClass.reloadComments();
			});
		}
		
		reloadComments() {     
			var thisClass = this;
			thisClass.commentsTree = [];
			thisClass.comments = [];
			this.divComments.innerText ="";
			
			paella.data.read('comments',{id:paella.initDelegate.getId()},function(data,status) {
				var i;
				var valueText;
				var comment;
				if (data && typeof(data)=='object' && data.allComments && data.allComments.length>0) {
					thisClass.comments = data.allComments;
					var tempDict = {};

					// obtain normal comments  
					for (i =0; i < data.allComments.length; ++i ) {
						valueText = data.allComments[i].value;
													
						if (data.allComments[i].mode !== "reply") { 
							comment = {};
							comment["id"] = data.allComments[i].id;
							comment["userName"] = data.allComments[i].userName;
							comment["mode"] = data.allComments[i].mode;
							comment["value"] = valueText;
							comment["created"] = data.allComments[i].created;
							comment["replies"] = [];    

							thisClass.commentsTree.push(comment); 
							tempDict[comment["id"]] = thisClass.commentsTree.length - 1;
						}
					}
				
					// obtain reply comments
					for (i =0; i < data.allComments.length; ++i ){
						valueText = data.allComments[i].value;

						if (data.allComments[i].mode === "reply") { 
							comment = {};
							comment["id"] = data.allComments[i].id;
							comment["userName"] = data.allComments[i].userName;
							comment["mode"] = data.allComments[i].mode;
							comment["value"] = valueText;
							comment["created"] = data.allComments[i].created;

							var index = tempDict[data.allComments[i].parent];
							thisClass.commentsTree[index]["replies"].push(comment);
						}
					}
					thisClass.displayComments();
				} 
			});
		}
		
		displayComments() {
			var thisClass = this;
			for (var i =0; i < thisClass.commentsTree.length; ++i ){
				var comment = thisClass.commentsTree[i];
				var e = thisClass.createACommentEntry(comment);
				thisClass.divComments.appendChild(e);
			} 
		}
		
		createACommentEntry(comment) {
			var thisClass = this;
			var rootID = this.divPublishComment.id+"_entry"+comment["id"];
			var users;
			
			var divEntry;
			divEntry = document.createElement('div');
			divEntry.id = rootID;
			divEntry.className = "comments_entry";
			
			var divSil;
			divSil = document.createElement('img');
			divSil.className = "comments_entry_silhouette";
			divSil.id = rootID+"_silhouette";

			divEntry.appendChild(divSil);
			
			var divCommentContainer;
			divCommentContainer = document.createElement('div');
			divCommentContainer.className = "comments_entry_container";
			divCommentContainer.id = rootID+"_comment_container";
			divEntry.appendChild(divCommentContainer);
			
			var divCommentMetadata;
			divCommentMetadata = document.createElement('div');
			divCommentMetadata.id = rootID+"_comment_metadata"; 
			divCommentContainer.appendChild(divCommentMetadata);
			
			
			
	//		var datePublish = comment["created"];
			var datePublish = "";
			if (comment["created"]) {
				var dateToday=new Date();
				var dateComment = paella.utils.timeParse.matterhornTextDateToDate(comment["created"]);			
				datePublish = paella.utils.timeParse.secondsToText((dateToday.getTime()-dateComment.getTime())/1000);
			}
			
			// var headLine = "<span class='comments_entry_username'>" + comment["userName"] + "</span>";
			// headLine += "<span class='comments_entry_datepublish'>" + datePublish + "</span>";
			// divCommentMetadata.innerHTML = headLine;
			
			
			var divCommentValue;
			divCommentValue = document.createElement('div');
			divCommentValue.id = rootID+"_comment_value";
			divCommentValue.className = "comments_entry_comment";
			divCommentContainer.appendChild(divCommentValue);		
			
			divCommentValue.innerText = comment["value"];
			
			var divCommentReply = document.createElement('div');
			divCommentReply.id = rootID+"_comment_reply";
			divCommentContainer.appendChild(divCommentReply);
			
			paella.data.read('userInfo',{username:comment["userName"]}, function(data,status) {
				if (data) {
					divSil.src = data.avatar;
					
					var headLine = "<span class='comments_entry_username'>" + data.name + " " + data.lastname + "</span>";
					headLine += "<span class='comments_entry_datepublish'>" + datePublish + "</span>";				
					divCommentMetadata.innerHTML = headLine;
				}
			});

			if (this.canPublishAComment == true) {
				//var btnRplyComment = document.createElement('button');
				var btnRplyComment = document.createElement('div');
				btnRplyComment.className = "reply_button";
				btnRplyComment.innerText = paella.utils.dictionary.translate("Reply");
				
				btnRplyComment.id = rootID+"_comment_reply_button";
				btnRplyComment.onclick = function(){
					var e = thisClass.createAReplyEntry(comment["id"]);
					this.style.display="none";
					this.parentElement.parentElement.appendChild(e);
				};
				divCommentReply.appendChild(btnRplyComment);
			}
			
			for (var i =0; i < comment.replies.length; ++i ){
				var e = thisClass.createACommentReplyEntry(comment["id"], comment["replies"][i]);
				divCommentContainer.appendChild(e);
			}
			return divEntry;
		}
		
		createACommentReplyEntry(parentID, comment) {
			var thisClass = this;
			var rootID = this.divPublishComment.id+"_entry_" + parentID + "_reply_" + comment["id"];

			var divEntry;
			divEntry = document.createElement('div');
			divEntry.id = rootID;
			divEntry.className = "comments_entry";
			
			var divSil;
			divSil = document.createElement('img');
			divSil.className = "comments_entry_silhouette";
			divSil.id = rootID+"_silhouette";

			divEntry.appendChild(divSil);
				
			var divCommentContainer;
			divCommentContainer = document.createElement('div');
			divCommentContainer.className = "comments_entry_container";
			divCommentContainer.id = rootID+"_comment_container";
			divEntry.appendChild(divCommentContainer);
				
			var divCommentMetadata;
			divCommentMetadata = document.createElement('div');
			divCommentMetadata.id = rootID+"_comment_metadata"; 
			divCommentContainer.appendChild(divCommentMetadata);
	//		var datePublish = comment["created"];
			var datePublish = "";
			if (comment["created"]) {
				var dateToday=new Date();
				var dateComment = paella.utils.timeParse.matterhornTextDateToDate(comment["created"]);			
				datePublish = paella.utils.timeParse.secondsToText((dateToday.getTime()-dateComment.getTime())/1000);
			}
			
			// var headLine = "<span class='comments_entry_username'>" + comment["userName"] + "</span>";
			// headLine += "<span class='comments_entry_datepublish'>" + datePublish + "</span>";
			// divCommentMetadata.innerHTML = headLine;
			
			var divCommentValue;
			divCommentValue = document.createElement('div');
			divCommentValue.id = rootID+"_comment_value";
			divCommentValue.className = "comments_entry_comment";
			divCommentContainer.appendChild(divCommentValue);		
			
			divCommentValue.innerText = comment["value"];
			
			paella.data.read('userInfo',{username:comment["userName"]}, function(data,status) {
				if (data) {
					divSil.src = data.avatar;
					
					var headLine = "<span class='comments_entry_username'>" + data.name + " " + data.lastname + "</span>";
					headLine += "<span class='comments_entry_datepublish'>" + datePublish + "</span>";				
					divCommentMetadata.innerHTML = headLine;
				}
			});	
				
			return divEntry;
		}
		
		//Allows the user to write a new reply
		createAReplyEntry(annotationID) {
			var thisClass = this;
			var rootID = this.divPublishComment.id+"_entry_" + annotationID + "_reply";

			var divEntry;
			divEntry = document.createElement('div');
			divEntry.id = rootID+"_entry";
			divEntry.className = "comments_entry";
			
			var divSil;
			divSil = document.createElement('img');
			divSil.className = "comments_entry_silhouette";
			divSil.style.width = "48px";		
			divSil.id = rootID+"_silhouette";
			divSil.src = paella.initDelegate.initParams.accessControl.userData.avatar;
			divEntry.appendChild(divSil);
			
			var divCommentContainer;
			divCommentContainer = document.createElement('div');
			divCommentContainer.className = "comments_entry_container comments_reply_container";
			divCommentContainer.id = rootID+"_reply_container";
			divEntry.appendChild(divCommentContainer);
		
			var textArea;
			textArea = document.createElement('textArea');
			textArea.onclick = function(){paella.keyManager.enabled = false;};
			textArea.draggable = false;
			textArea.id = rootID+"_textarea";
			divCommentContainer.appendChild(textArea);
			
			this.publishCommentButtons = document.createElement('div');
			this.publishCommentButtons.id = rootID+"_buttons_area";
			divCommentContainer.appendChild(this.publishCommentButtons);
			
			var btnAddComment;
			btnAddComment = document.createElement('button');
			btnAddComment.id = rootID+"_btnAddComment";
			btnAddComment.className = "publish";
			btnAddComment.onclick = function(){
				var txtValue = textArea.value;
				if (txtValue.replace(/\s/g,'') != "") {
					thisClass.addReply(annotationID,textArea.id);
				}
			};
			btnAddComment.innerText = paella.utils.dictionary.translate("Reply");
			
			this.publishCommentButtons.appendChild(btnAddComment);
			
			return divEntry;
		}
	}
});
*/


/*
paella.addPlugin(function() {

	return class DescriptionPlugin extends paella.TabBarPlugin {
		getSubclass() { return "showDescriptionTabBar"; }
		getName() { return "es.upv.paella.descriptionPlugin"; }
		getTabName() { return "Descripción"; }
				
		get domElement() { return this._domElement || null; }
		set domElement(d) { this._domElement = d; }
				
		buildContent(domElement) {
			this.domElement = domElement;
			this.loadContent();
		}
				
		action(tab) {
			this.loadContent();
		}
				
		loadContent() {
			var container = this.domElement;
			container.innerText = "Loading...";
			new paella.Timer(function(t) {
				container.innerText = "Loading done";
			},2000);
		}
	}
})
*/


paella.addPlugin(function() {
	return class extendedTabAdapterPlugin extends paella.ButtonPlugin {
		get currentUrl() { return this._currentUrl; }
		set currentUrl(v) { this._currentUrl = v; }
		get currentMaster() { return this._currentMaster; }
		set currentMaster(v) { this._currentMaster = v; }
		get currentSlave() { return this._currentSlave; }
		set currentSlave(v) { this._currentSlave = v; }
		get availableMasters() { return this._availableMasters; }
		set availableMasters(v) { this._availableMasters = v; }
		get availableSlaves() { return this._availableSlaves }
		set availableSlaves(v) { this._availableSlaves = v; }
		get showWidthRes() { return this._showWidthRes; }
		set showWidthRes(v) { this._showWidthRes = v; }

		getAlignment() { return 'right'; }
		getSubclass() { return "extendedTabAdapterPlugin"; }
		getIconClass() { return 'icon-folder'; }
		getIndex() { return 2030; }
		getName() { return "es.upv.paella.extendedTabAdapterPlugin"; }
		getDefaultToolTip() { return paella.utils.dictionary.translate("Extended Tab Adapter"); }
		getButtonType() { return paella.ButtonPlugin.type.popUpButton; }
		
		buildContent(domElement) {
			domElement.appendChild(paella.extendedAdapter.bottomContainer);
		}
	}
});
paella.addPlugin(function() {
	return class FootPrintsPlugin extends paella.ButtonPlugin {
		get INTERVAL_LENGTH() { return this._INTERVAL_LENGTH; }
		set INTERVAL_LENGTH(v) { this._INTERVAL_LENGTH = v; }
		get inPosition() { return this._inPosition; }
		set inPosition(v) { this._inPosition = v; }
		get outPosition() { return this._outPosition; }
		set outPosition(v) { this._outPosition = v; }
		get canvas() { return this._canvas; }
		set canvas(v) { this._canvas = v; }
		get footPrintsTimer() { return this._footPrintsTimer; }
		set footPrintsTimer(v) { this._footPrintsTimer = v; }
		get footPrintsData() { return this._footPrintsData; }
		set footPrintsData(v) { this._footPrintsData = v; }

		getAlignment() { return 'right'; }
		getSubclass() { return "footPrints"; }
		getIconClass() { return 'icon-stats'; }
		getIndex() { return 590; }
		getDefaultToolTip() { return paella.utils.dictionary.translate("Show statistics"); }
		getName() { return "es.upv.paella.footprintsPlugin"; }
		getButtonType() { return paella.ButtonPlugin.type.timeLineButton; }
	
		setup(){
			this._INTERVAL_LENGTH = 5;
			var thisClass = this;
			paella.events.bind(paella.events.timeUpdate, function(event) { thisClass.onTimeUpdate(); });
	
			switch(this.config.skin) {
			case 'custom':
				this.fillStyle = this.config.fillStyle;
				this.strokeStyle = this.config.strokeStyle;
				break;
	
			case 'dark':
				this.fillStyle = '#727272';
				this.strokeStyle = '#424242';
				break;
	
			case 'light':
				this.fillStyle = '#d8d8d8';
				this.strokeStyle = '#ffffff';
				break;
	
			default:
				this.fillStyle = '#d8d8d8';
				this.strokeStyle = '#ffffff';
				break;
			}
		}
	
		checkEnabled(onSuccess) {
			onSuccess(!paella.player.isLiveStream());
		}
	
		buildContent(domElement) {
			var container = document.createElement('div');
			container.className = 'footPrintsContainer';
	
			this.canvas = document.createElement('canvas');
			this.canvas.id = 'footPrintsCanvas';
			this.canvas.className = 'footPrintsCanvas';
			container.appendChild(this.canvas);
	
	
			domElement.appendChild(container);
		}
	
		onTimeUpdate() {
			let currentTime = -1;
			paella.player.videoContainer.currentTime()
				.then((c) => {
					currentTime = c;
					return paella.player.videoContainer.trimming();
				})
				.then((trimming) => {
					let videoCurrentTime = Math.round(currentTime + (trimming.enabled ? trimming.start : 0));
					if (this.inPosition <= videoCurrentTime && videoCurrentTime <= this.inPosition + this.INTERVAL_LENGTH) {
						this.outPosition = videoCurrentTime;
						if ((this.inPosition + this.INTERVAL_LENGTH)===this.outPosition) {
							this.trackFootPrint(this.inPosition, this.outPosition);
							this.inPosition = this.outPosition;
						}
					}
					else {
						this.trackFootPrint(this.inPosition, this.outPosition);
						this.inPosition = videoCurrentTime;
						this.outPosition = videoCurrentTime;
					}
				});
		}

		trackFootPrint(inPosition, outPosition) {
			var data = {"in": inPosition, "out": outPosition};
			paella.data.write('footprints',{id:paella.initDelegate.getId()}, data);
		}
	
		willShowContent() {
			var thisClass = this;
			this.loadFootprints();
			this.footPrintsTimer = new paella.utils.Timer(function(timer) {
				thisClass.loadFootprints();
			},5000);
			this.footPrintsTimer.repeat = true;
		}
	
		didHideContent() {
			if (this.footPrintsTimer!=null) {
				this.footPrintsTimer.cancel();
				this.footPrintsTimer = null;
			}
		}
	
		loadFootprints() {
			var thisClass = this;
			paella.data.read('footprints',{id:paella.initDelegate.getId()},function(data,status) {
				var footPrintsData = {};
				paella.player.videoContainer.duration().then(async (duration) => {
					var trimStart = Math.floor(await paella.player.videoContainer.trimStart());
	
					var lastPosition = -1;
					var lastViews = 0;
					for (var i = 0; i < data.length; i++) {
						var position = data[i].position - trimStart;
						if (position < duration){
							var views = data[i].views;
	
							if (position - 1 != lastPosition){
								for (var j = lastPosition + 1; j < position; j++) {
									footPrintsData[j] = lastViews;
								}
							}
							footPrintsData[position] = views;
							lastPosition = position;
							lastViews = views;
						}
					}
					thisClass.drawFootPrints(footPrintsData);
				});
			});
		}

		drawFootPrints(footPrintsData) {
			if (this.canvas) {
				var duration = Object.keys(footPrintsData).length;
				var ctx = this.canvas.getContext("2d");
				var h = 20;
				var i;
				for (i = 0; i<duration; ++i) {
					if (footPrintsData[i] > h) { h = footPrintsData[i]; }
				}
	
				this.canvas.setAttribute("width", duration);
				this.canvas.setAttribute("height", h);
				ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
				ctx.fillStyle = this.fillStyle; //'#faa166'; //'#9ED4EE';
				ctx.strokeStyle = this.strokeStyle; //'#fa8533'; //"#0000FF";
				ctx.lineWidth = 2;
	
				ctx.webkitImageSmoothingEnabled = false;
				ctx.mozImageSmoothingEnabled = false;
	
				for (i = 0; i<duration-1; ++i) {
					ctx.beginPath();
					ctx.moveTo(i, h);
					ctx.lineTo(i, h-footPrintsData[i]);
					ctx.lineTo(i+1, h-footPrintsData[i+1]);
					ctx.lineTo(i+1, h);
					ctx.closePath();
					ctx.fill();
	
					ctx.beginPath();
					ctx.moveTo(i, h-footPrintsData[i]);
					ctx.lineTo(i+1, h-footPrintsData[i+1]);
					ctx.closePath();
					ctx.stroke();
				}
			}
		}
	}
});

paella.addPlugin(function() {
	return class FrameCaptionsSearchPlugIn extends paella.SearchServicePlugIn {
		getName() { return "es.upv.paella.frameCaptionsSearchPlugin"; }

		search(text, next) {
			let re = RegExp(text,"i");
			let results = [];
			for (var key in paella.player.videoLoader.frameList) {
				var value = paella.player.videoLoader.frameList[key];
				if (typeof(value)=="object") {
					if (re.test(value.caption)) {
						results.push({
							time: key,
							content: value.caption,
							score: 0
						});					
					}
				}
			}
			if (next) {
				next(false, results);
			}
		}	
	}
});

paella.addPlugin(function() {
	return class FrameControlPlugin extends paella.ButtonPlugin {
		get frames() { return this._frames; }
		set frames(v) { this._frames = v; }
		get highResFrames() { return this._highResFrames; }
		set highResFrames(v) { this._highResFrames = v; }
		get currentFrame() { return this._currentFrame; }
		set currentFrame(v) { this._currentFrame = v; }
		get navButtons() { return this._navButtons; }
		set navButtons(v) { this._navButtons = v; }
		get buttons() {
			if (!this._buttons) {
				this._buttons = [];
			}
			return this._buttons;
		}
		set buttons(v) { this._buttons = v; }
		get contx() { return this._contx; }
		set contx(v) { this._contx = v; }
		
		getAlignment() { return 'right'; }
		getSubclass() { return "frameControl"; }
		getIconClass() { return 'icon-photo'; }
		getIndex() { return 510; }
		getName() { return "es.upv.paella.frameControlPlugin"; }
		getButtonType() { return paella.ButtonPlugin.type.timeLineButton; }
		getDefaultToolTip() { return paella.utils.dictionary.translate("Navigate by slides"); }

		checkEnabled(onSuccess) {
			this._img = null;
			this._searchTimer = null;
			this._searchTimerTime = 250;

			if (paella.initDelegate.initParams.videoLoader.frameList==null) onSuccess(false);
			else if (paella.initDelegate.initParams.videoLoader.frameList.length===0) onSuccess(false);
			else if (Object.keys(paella.initDelegate.initParams.videoLoader.frameList).length==0) onSuccess(false);
			else onSuccess(true);
		}

		setup() {
			this._showFullPreview = this.config.showFullPreview || "auto";
			
			var thisClass = this;
			var oldClassName;
			var blockCounter = 1;
			var correctJump = 0;
			var selectedItem = -1;
			var jumpAtItem;
			var Keys = {Tab:9,Return:13,Esc:27,End:35,Home:36,Left:37,Up:38,Right:39,Down:40};

			$(this.button).keyup(function(event) {
				var visibleItems = Math.floor(thisClass.contx.offsetWidth/100);
				var rest = thisClass.buttons.length%visibleItems;
				var blocks = Math.floor(thisClass.buttons.length/visibleItems);

				if (thisClass.isPopUpOpen()){
					if (event.keyCode == Keys.Left) {
					if(selectedItem > 0){
							thisClass.buttons[selectedItem].className = oldClassName;

							selectedItem--;

							if(blockCounter > blocks) correctJump = visibleItems - rest;
							jumpAtItem = ((visibleItems)*(blockCounter-1))-1-correctJump;

							if(selectedItem == jumpAtItem && selectedItem != 0){
								thisClass.navButtons.left.scrollContainer.scrollLeft -= visibleItems*105;
								--blockCounter;
							}

							if(this.hiResFrame) thisClass.removeHiResFrame();
							if (!paella.utils.userAgent.browser.IsMobileVersion) {
								thisClass.buttons[selectedItem].frameControl.onMouseOver(null,thisClass.buttons[selectedItem].frameData);
							}
							
							oldClassName = thisClass.buttons[selectedItem].className;
							thisClass.buttons[selectedItem].className = 'frameControlItem selected';
						}
					}
					else if (event.keyCode == Keys.Right) {
						if(selectedItem<thisClass.buttons.length-1){
							if(selectedItem >= 0){
								thisClass.buttons[selectedItem].className = oldClassName;
							}

							selectedItem++;

							if (blockCounter == 1) correctJump = 0;
							jumpAtItem = (visibleItems)*blockCounter-correctJump;

							if(selectedItem == jumpAtItem){
								thisClass.navButtons.left.scrollContainer.scrollLeft += visibleItems*105;
								++blockCounter;
							}

							if(this.hiResFrame)thisClass.removeHiResFrame();
							if (!paella.utils.userAgent.browser.IsMobileVersion) {
								thisClass.buttons[selectedItem].frameControl.onMouseOver(null,thisClass.buttons[selectedItem].frameData);
							}
							
							oldClassName = thisClass.buttons[selectedItem].className;
							thisClass.buttons[selectedItem].className = 'frameControlItem selected';
						}
					}
					else if (event.keyCode == Keys.Return) {
						thisClass.buttons[selectedItem].frameControl.onClick(null,thisClass.buttons[selectedItem].frameData);
						oldClassName = 'frameControlItem current';
					}
					else if (event.keyCode == Keys.Esc){
						thisClass.removeHiResFrame();
					}
				}
			});
		}

		buildContent(domElement) {
			var thisClass = this;
			this.frames = [];
			var container = document.createElement('div');
			container.className = 'frameControlContainer';

			thisClass.contx = container;

			var content = document.createElement('div');
			content.className = 'frameControlContent';

			this.navButtons = {
				left:document.createElement('div'),
				right:document.createElement('div')
			};
			this.navButtons.left.className = 'frameControl navButton left';
			this.navButtons.right.className = 'frameControl navButton right';

			var frame = this.getFrame(null);

			domElement.appendChild(this.navButtons.left);
			domElement.appendChild(container);
			container.appendChild(content);
			domElement.appendChild(this.navButtons.right);

			this.navButtons.left.scrollContainer = container;
			$(this.navButtons.left).click(function(event) {
				this.scrollContainer.scrollLeft -= 100;
			});

			this.navButtons.right.scrollContainer = container;
			$(this.navButtons.right).click(function(event) {
				this.scrollContainer.scrollLeft += 100;
			});

			content.appendChild(frame);

			var itemWidth = $(frame).outerWidth(true);
			content.innerText = '';
			$(window).mousemove(function(event) {
				if ($(content).offset().top>event.pageY || !$(content).is(":visible") ||
					($(content).offset().top + $(content).height())<event.pageY)
				{
					thisClass.removeHiResFrame();
				}
			});

			var frames = paella.initDelegate.initParams.videoLoader.frameList;
			var numFrames;
			if (frames) {
				var framesKeys = Object.keys(frames);
				numFrames = framesKeys.length;

				framesKeys.map(function(i){return Number(i, 10);})
				.sort(function(a, b){return a-b;})
				.forEach(function(key){
					var frameItem = thisClass.getFrame(frames[key]);
					content.appendChild(frameItem,'frameContrlItem_' + numFrames);
					thisClass.frames.push(frameItem);
				});
			}

			$(content).css({width:(numFrames * itemWidth) + 'px'});

			paella.events.bind(paella.events.setTrim,(event,params) => {
				this.updateFrameVisibility(params.trimEnabled,params.trimStart,params.trimEnd);
			});
			paella.player.videoContainer.trimming()
				.then((trimData) => {
					this.updateFrameVisibility(trimData.enabled,trimData.start,trimData.end);
				});
			

			paella.events.bind(paella.events.timeupdate,(event,params) => this.onTimeUpdate(params.currentTime) );
		}

		showHiResFrame(url,caption) {
			var frameRoot = document.createElement("div");
			var frame = document.createElement("div");
			var hiResImage = document.createElement('img');
			this._img = hiResImage;
			hiResImage.className = 'frameHiRes';
			hiResImage.setAttribute('src',url);
			hiResImage.setAttribute('style', 'width: 100%;');

			$(frame).append(hiResImage);
			$(frameRoot).append(frame);

			frameRoot.setAttribute('style', 'display: table;');
			frame.setAttribute('style', 'display: table-cell; vertical-align:middle;');

		    if (this.config.showCaptions === true){
			var captionContainer = document.createElement('p');
			captionContainer.className = "frameCaption";
			captionContainer.innerText = caption || "";
			frameRoot.append(captionContainer);
			this._caption = captionContainer;
		    }

			let overlayContainer = paella.player.videoContainer.overlayContainer;
			
			switch(this._showFullPreview) {
				case "auto":
					var streams = paella.initDelegate.initParams.videoLoader.streams;
					if (streams.length == 1){
						overlayContainer.addElement(frameRoot, overlayContainer.getVideoRect(0));
					}
					else if (streams.length >= 2){
						overlayContainer.addElement(frameRoot, overlayContainer.getVideoRect(1));
					}
					overlayContainer.enableBackgroundMode();
					this.hiResFrame = frameRoot;
					break;
				case "master":
					overlayContainer.addElement(frameRoot, overlayContainer.getVideoRect(0));
					overlayContainer.enableBackgroundMode();
					this.hiResFrame = frameRoot;
					break;
				case "slave":
					var streams = paella.initDelegate.initParams.videoLoader.streams;
					if (streams.length >= 2){
						overlayContainer.addElement(frameRoot, overlayContainer.getVideoRect(0));
						overlayContainer.enableBackgroundMode();
						this.hiResFrame = frameRoot;
					}
					break;
			}
		}

		removeHiResFrame() {
			var thisClass = this;
			var overlayContainer = paella.player.videoContainer.overlayContainer;
			if (this.hiResFrame) {
				overlayContainer.removeElement(this.hiResFrame);
			}
			overlayContainer.disableBackgroundMode();
			thisClass._img = null;
		}

		updateFrameVisibility(trimEnabled,trimStart,trimEnd) {
			var i;
			if (!trimEnabled) {
				for (i = 0; i<this.frames.length;++i) {
					$(this.frames[i]).show();
				}
			}
			else {
				for (i = 0; i<this.frames.length; ++i) {
					var frameElem = this.frames[i];
					var frameData = frameElem.frameData;
					if (frameData.time<trimStart) {
						if (this.frames.length>i+1 && this.frames[i+1].frameData.time>trimStart) {
							$(frameElem).show();
						}
						else {
							$(frameElem).hide();
						}
					}
					else if (frameData.time>trimEnd) {
						$(frameElem).hide();
					}
					else {
						$(frameElem).show();
					}
				}
			}
		}

		getFrame(frameData,id) {
			var frame = document.createElement('div');
			frame.className = 'frameControlItem';
			if (id) frame.id = id;
			if (frameData) {

				this.buttons.push(frame);

				frame.frameData = frameData;
				frame.frameControl = this;
				var image = frameData.thumb ? frameData.thumb:frameData.url;
				var labelTime = paella.utils.timeParse.secondsToTime(frameData.time);
				frame.innerHTML = '<img src="' + image + '" alt="" class="frameControlImage" title="'+labelTime+'" aria-label="'+labelTime+'"></img>';
				if (!paella.utils.userAgent.browser.IsMobileVersion) {
					$(frame).mouseover(function(event) {
						this.frameControl.onMouseOver(event,this.frameData);
					});
				}
				
				$(frame).mouseout(function(event) {
					this.frameControl.onMouseOut(event,this.frameData);
				});
				$(frame).click(function(event) {
					this.frameControl.onClick(event,this.frameData);
				});
			}
			return frame;
		}

		onMouseOver(event,frameData) {
			var frames = paella.initDelegate.initParams.videoLoader.frameList;
			var frame = frames[frameData.time];
			if (frame) {
				var image = frame.url;
				if(this._img){
				    this._img.setAttribute('src',image);
				    if (this.config.showCaptions === true){
					this._caption.innerText = frame.caption || "";
				    }
				}
				else{
					this.showHiResFrame(image,frame.caption);
				}
			}
			
			if(this._searchTimer != null){
				clearTimeout(this._searchTimer);
			}
		}

		onMouseOut(event,frameData) {
			this._searchTimer = setTimeout((timer) => this.removeHiResFrame(), this._searchTimerTime);
		}

		onClick(event,frameData) {
			paella.player.videoContainer.trimming()
				.then((trimming) => {
					let time = trimming.enabled ? frameData.time - trimming.start : frameData.time;
					if (time>0) {
						paella.player.videoContainer.seekToTime(time + 1);
					}
					else {
						paella.player.videoContainer.seekToTime(0);
					}
				});
		}

		onTimeUpdate(currentTime) {
			var frame = null;
			paella.player.videoContainer.trimming()
				.then((trimming) => {
				    let time = trimming.enabled ? currentTime + trimming.start : currentTime;

			for (var i = 0; i<this.frames.length; ++i) {
				if (this.frames[i].frameData && this.frames[i].frameData.time<=time) {
				    frame = this.frames[i];
				}
				else {
					break;
				}
			}
			if (this.currentFrame!=frame && frame) {
				//this.navButtons.left.scrollContainer.scrollLeft += 100;

				if (this.currentFrame) this.currentFrame.className = 'frameControlItem';
				this.currentFrame = frame;
				this.currentFrame.className = 'frameControlItem current';
			}


				});

		}
	}
});

paella.addPlugin(function() {
	return class FullScreenPlugin extends paella.ButtonPlugin {
		
		getIndex() { return 551; }
		getAlignment() { return 'right'; }
		getSubclass() { return "showFullScreenButton"; }
		getIconClass() { return 'icon-fullscreen'; }
		getName() { return "es.upv.paella.fullScreenButtonPlugin"; }
		checkEnabled(onSuccess) {
			this._reload = null;
			var enabled = paella.player.checkFullScreenCapability();
			onSuccess(enabled);
		}
		getDefaultToolTip() { return paella.utils.dictionary.translate("Go Fullscreen"); }
		
		setup() {
			this._reload = this.config.reloadOnFullscreen ? this.config.reloadOnFullscreen.enabled:false;
			paella.events.bind(paella.events.enterFullscreen, (event) => this.onEnterFullscreen());
			paella.events.bind(paella.events.exitFullscreen, (event) => this.onExitFullscreen());
		}
	
		action(button) {
			if (paella.player.isFullScreen()) {
				paella.player.exitFullScreen();
			}
			else if ((!paella.player.checkFullScreenCapability() || paella.utils.userAgent.browser.Explorer) && window.location !== window.parent.location) {
				// Iframe and no fullscreen support
				var url = window.location.href;
	
				paella.player.pause();
				paella.player.videoContainer.currentTime()
					.then((currentTime) => {
						var obj = this.secondsToHours(currentTime);
						window.open(url+"&time="+obj.h+"h"+obj.m+"m"+obj.s+"s&autoplay=true");
					});
				
				return;
			}
			else {
				paella.player.goFullScreen();
			}
	
			if (paella.player.config.player.reloadOnFullscreen && paella.player.videoContainer.supportAutoplay()) {
				setTimeout(() => {
					if(this._reload) {
						paella.player.videoContainer.setQuality(null)
							.then(() => {
							});
						//paella.player.reloadVideos();
					}
				}, 1000);
			}
		}
	
		secondsToHours(sec_numb) {
			var hours   = Math.floor(sec_numb / 3600);
			var minutes = Math.floor((sec_numb - (hours * 3600)) / 60);
			var seconds =  Math.floor(sec_numb - (hours * 3600) - (minutes * 60));
			var obj = {};
	
			if (hours < 10) {hours = "0"+hours;}
			if (minutes < 10) {minutes = "0"+minutes;}
			if (seconds < 10) {seconds = "0"+seconds;}
			obj.h = hours;
			obj.m = minutes;
			obj.s = seconds;
			return obj;
		}
	
		onEnterFullscreen() {
			this.setToolTip(paella.utils.dictionary.translate("Exit Fullscreen"));
			this.button.className = this.getButtonItemClass(true);
			this.changeIconClass('icon-windowed');
		}
		
		onExitFullscreen() {
			this.setToolTip(paella.utils.dictionary.translate("Go Fullscreen"));
			this.button.className = this.getButtonItemClass(false);
			this.changeIconClass('icon-fullscreen');
			setTimeout(() => {
				paella.player.onresize();
			}, 100);
		}
	
		getButtonItemClass(selected) {
			return 'buttonPlugin '+this.getAlignment() +' '+ this.getSubclass() + ((selected) ? ' active':'');
		}
	}
});

paella.addPlugin(function() {
	return class HelpPlugin extends paella.ButtonPlugin {

		getIndex() { return 509; }
		getAlignment() { return 'right'; }
		getSubclass() { return "helpButton"; }
		getIconClass() { return 'icon-help'; }
		getName() { return "es.upv.paella.helpPlugin"; }

		getDefaultToolTip() { return paella.utils.dictionary.translate("Show help") + ' (' + paella.utils.dictionary.translate("Paella version:") + ' ' + paella.version + ')'; }


		checkEnabled(onSuccess) { 
			var availableLangs = (this.config && this.config.langs) || [];
			onSuccess(availableLangs.length>0); 
		}

		action(button) {
			var mylang = paella.utils.dictionary.currentLanguage();
			
			var availableLangs = (this.config && this.config.langs) || [];
			var idx = availableLangs.indexOf(mylang);
			if (idx < 0) { idx = 0; }
							
			//paella.messageBox.showFrame("http://paellaplayer.upv.es/?page=usage");
			let url = "resources/style/help/help_" + availableLangs[idx] + ".html";
			if (paella.utils.userAgent.browser.IsMobileVersion) {
				window.open(url);
			}
			else {
				paella.messageBox.showFrame(url);
			}
		}
		
	}
});

(() => {

	let s_preventVideoDump = [];

	class HLSPlayer extends paella.Html5Video {
		get config() {
			let config = {
				autoStartLoad: true,
				startPosition : -1,
				capLevelToPlayerSize: true,
				debug: false,
				defaultAudioCodec: undefined,
				initialLiveManifestSize: 1,
				initialQualityLevel: 1,
				maxBufferLength: 30,
				maxMaxBufferLength: 600,
				maxBufferSize: 60*1000*1000,
				maxBufferHole: 0.5,
				lowBufferWatchdogPeriod: 0.5,
				highBufferWatchdogPeriod: 3,
				nudgeOffset: 0.1,
				nudgeMaxRetry : 3,
				maxFragLookUpTolerance: 0.2,
				liveSyncDurationCount: 3,
				liveMaxLatencyDurationCount: 10,
				enableWorker: true,
				enableSoftwareAES: true,
				manifestLoadingTimeOut: 10000,
				manifestLoadingMaxRetry: 1,
				manifestLoadingRetryDelay: 500,
				manifestLoadingMaxRetryTimeout : 64000,
				startLevel: undefined,
				levelLoadingTimeOut: 10000,
				levelLoadingMaxRetry: 4,
				levelLoadingRetryDelay: 500,
				levelLoadingMaxRetryTimeout: 64000,
				fragLoadingTimeOut: 20000,
				fragLoadingMaxRetry: 6,
				fragLoadingRetryDelay: 500,
				fragLoadingMaxRetryTimeout: 64000,
				startFragPrefetch: false,
				appendErrorMaxRetry: 3,
				
				// loader: customLoader,
				// fLoader: customFragmentLoader,
				// pLoader: customPlaylistLoader,
				// xhrSetup: XMLHttpRequestSetupCallback,
				// fetchSetup: FetchSetupCallback,
				// abrController: customAbrController,
				// timelineController: TimelineController,

				enableWebVTT: true,
				enableCEA708Captions: true,
				stretchShortVideoTrack: false,
				maxAudioFramesDrift : 1,
				forceKeyFrameOnDiscontinuity: true,
				abrEwmaFastLive: 5.0,
				abrEwmaSlowLive: 9.0,
				abrEwmaFastVoD: 4.0,
				abrEwmaSlowVoD: 15.0,
				abrEwmaDefaultEstimate: 500000,
				abrBandWidthFactor: 0.95,
				abrBandWidthUpFactor: 0.7,
				minAutoBitrate: 0
			};

			let pluginConfig = {};
			paella.player.config.player.methods.some((methodConfig) => {
				if (methodConfig.factory=="HLSVideoFactory") {
					pluginConfig = methodConfig.config || {};
					return true;
				}
			});

			for (let key in config) {
				if (pluginConfig[key]!=undefined) {
					config[key] = pluginConfig[key];
				}
			}

			return config;
		}

		constructor(id,stream,left,top,width,height) {
			super(id,stream,left,top,width,height,'hls');
		}
		
		_loadDeps() {
			return new Promise((resolve,reject) => {
				if (!window.$paella_hls) {
					paella.require(paella.baseUrl +'javascript/hls.min.js')
						.then((hls) => {
							window.$paella_hls = hls;
							resolve(window.$paella_hls);
						});
				}
				else {
					resolve(window.$paella_hls);
				}	
			});
		}
	
		_deferredAction(action) {
			return new Promise((resolve,reject) => {
				function processResult(actionResult) {
					if (actionResult instanceof Promise) {
						actionResult.then((p) => resolve(p))
							.catch((err) => reject(err));
					}
					else {
						resolve(actionResult);
					}
				}
	
				if (this.ready) {
					processResult(action());
				}
				else {
					let eventFunction = () => {
						processResult(action());
						$(this.video).unbind('canplay');
						$(this.video).unbind('loadedmetadata');
						if (timer) {
							clearTimeout(timer);
							timer = null;
						}
					};
					$(this.video).bind('canplay',eventFunction);
					$(this.video).bind('loadedmetadata',eventFunction);
					let timerFunction = () => {
						if (!this.ready) {
							if (!this._hls) {
								// iOS
								// In this way the recharge is forced, and it is possible to recover errors.
								console.debug("HLS video resume failed. Trying to recover.");
								let src = this.video.innerHTML;
								this.video.innerHTML = "";
								this.video.innerHTML = src;
								this.video.load();
								this.video.play();
							}
							timer = setTimeout(timerFunction, 1000);
						}
						else {
							eventFunction();
						}
					}
					let timer = setTimeout(timerFunction, 1000);
				}
			});
		}

		setupHls(video,url) {
			let initialQualityLevel = this.config.initialQualityLevel !== undefined ? this.config.initialQualityLevel : 1;
			return new Promise((resolve,reject) => {
				this._loadDeps()
					.then((Hls) => {
						if (Hls.isSupported()) {
							let cfg = this.config;
							//cfg.autoStartLoad = false;
							this._hls = new Hls(cfg);
							
							this.autoQuality = true;

							// For some streams there are problems if playback does not start after loading the
							// manifest. This flag is used to pause it again once the video is loaded
							let firstLoad = true;

							this._hls.on(Hls.Events.LEVEL_SWITCHED, (ev,data) => {
								if (firstLoad) {
									firstLoad = false;
									video.pause();
								}

								this._qualities = this._qualities || [];
								this._qualityIndex = this.autoQuality ? this._qualities.length - 1 : data.level;
								paella.events.trigger(paella.events.qualityChanged,{});
								if (console && console.log) console.log(`HLS: quality level changed to ${ data.level }`);
							});

							this._hls.on(Hls.Events.ERROR, (event, data) => {
								if (data.fatal) {
									switch (data.type) {
									case Hls.ErrorTypes.NETWORK_ERROR:
										if (data.details == Hls.ErrorDetails.MANIFEST_LOAD_ERROR) {
											// TODO: Manifest file not found
											console.error("paella.HLSPlayer: unrecoverable error in HLS Player. The video is not available.");
											reject(new Error("No such HLS stream: the video is not available"));
										}
										else {
											console.error("paella.HLSPlayer: Fatal network error encountered, try to recover");
											this._hls.startLoad();
										}
										break;
									case Hls.ErrorTypes.MEDIA_ERROR:
										console.error("paella.HLSPlayer: Fatal media error encountered, try to recover");
										this._hls.recoverMediaError();
										break;
									default:
										console.error("paella.HLSPlayer: Fatal error. Can not recover");
										this._hls.destroy();
										reject(new Errro("Invalid media"));
										break;
									}
								}
							});

							this._hls.on(Hls.Events.MANIFEST_PARSED, () => {
								if (!cfg.autoStartLoad) {
									this._hls.startLoad();
								}

								// Fixes hls.js problems when loading the initial quality level
								this._hls.currentLevel = this._hls.levels.length>=initialQualityLevel ? initialQualityLevel : -1;
								setTimeout(() => this._hls.currentLevel = -1, 1000);

								// Fixes hls.js problems loading some videos
								video.play();

								resolve(video);
							});

							this._hls.loadSource(url);
							this._hls.attachMedia(video);
						}
						else {
							reject(new Error("HLS not supported"));
						}
					})
			});
		}

		webGlDidLoad() {
			if (paella.utils.userAgent.system.iOS) {
				return super.webGlDidLoad();
			}
			// Register a new video loader in the webgl engine, to enable the
			// hls compatibility in webgl canvas
			bg.utils.HTTPResourceProvider.AddVideoLoader('m3u8', (url,onSuccess,onFail) => {
				var video = document.createElement("video");
				s_preventVideoDump.push(video);
				this.setupHls(video,url)
					.then(() => onSuccess(video))
					.catch(() => onFail());
			});
			return Promise.resolve();
		}

		loadVideoStream(canvasInstance,stream) {
			if (paella.utils.userAgent.system.iOS) {
				return super.loadVideoStream(canvasInstance,stream);
			}

			return canvasInstance.loadVideo(this,stream,(videoElem) => {
				return this.setupHls(videoElem,stream.src);
			});
		}

		supportsMultiaudio() {
			return this._deferredAction(() => {
				if (paella.utils.userAgent.system.iOS) {
					return this.video.audioTracks.length>1;
				}
				else {
					return this._hls.audioTracks.length>1;
				}
			});
		}
	
		getAudioTracks() {
			return this._deferredAction(() => {
				if (paella.utils.userAgent.system.iOS) {
					let result = [];
					Array.from(this.video.audioTracks).forEach((t) => {
						result.push({
							id: t.id,
							groupId: "",
							name: t.label,
							lang: t.language
						});
					})
					return result;
				}
				else {
					return this._hls.audioTracks;
				}
			});
		}

		setCurrentAudioTrack(trackId) {
			return this._deferredAction(() => {
				if (paella.utils.userAgent.system.iOS) {
					let found = false;
					Array.from(this.video.audioTracks).forEach((track) => {
						if (track.id==trackId) {
							found = true;
							track.enabled = true;
						}
						else {
							track.enabled = false;
						}
					});
					return found;
				}
				else {
					if (this._hls.audioTracks.some((track) => track.id==trackId)) {
						this._hls.audioTrack = trackId;
						return true;
					}
					else {

						return false;
					}
				}
			});
		}

		getCurrentAudioTrack() {
			return this._deferredAction(() => {
				if (paella.utils.userAgent.system.iOS) {
					let result = null;
					Array.from(this.video.audioTracks).some((t) => {
						if (t.enabled) {
							result = t;
							return true;
						}
					});
					return result;
				}
				else {
					let result = null;
					this._hls.audioTracks.some((t) => {
						if (t.id==this._hls.audioTrack) {
							result = t;
							return true;
						}
					});
					return result;
				}
			})
		}
	
		getQualities() {
			if (paella.utils.userAgent.system.iOS)// ||
		//		paella.utils.userAgent.browser.Safari)
			{
				return new Promise((resolve,reject) => {
					resolve([
						{
							index: 0,
							res: "",
							src: "",
							toString:function() { return "auto"; },
							shortLabel:function() { return "auto"; },
							compare:function(q2) { return 0; }
						}
					]);
				});
			}
			else {
				let This = this;
				return new Promise((resolve) => {
					if (!this._qualities || this._qualities.length==0) {
						This._qualities = [];
						This._hls.levels.forEach(function(q, index){
							This._qualities.push(This._getQualityObject(index, {
								index: index,
								res: { w:q.width, h:q.height },
								bitrate: q.bitrate
							}));					
						});
						if (this._qualities.length>1) {
							// If there is only one quality level, don't add the "auto" option
							This._qualities.push(
								This._getQualityObject(This._qualities.length, {
									index:This._qualities.length,
									res: { w:0, h:0 },
									bitrate: 0
								}));
						}
					}
					This.qualityIndex = This._qualities.length - 1;
					resolve(This._qualities);
				});
			}
		}

		disable(isMainAudioPlayer) {
			if (paella.utils.userAgent.system.iOS) {
				return;
			}
			
			this._currentQualityIndex = this._qualityIndex;
			this._hls.currentLevel = 0;
		}
	
		enable(isMainAudioPlayer) {
			if (this._currentQualityIndex !== undefined && this._currentQualityIndex !== null) {
				this.setQuality(this._currentQualityIndex);
				this._currentQualityIndex = null;
			}
		}

		printQualityes() {
			return new Promise((resolve,reject) => {
				this.getCurrentQuality()
					.then((cq)=>{
						return this.getNextQuality();
					})
					.then((nq) => {
						resolve();
					})
			});		
		}
		
		setQuality(index) {
			if (paella.utils.userAgent.system.iOS)// ||
				//paella.utils.userAgent.browser.Safari)
			{
				return Promise.resolve();
			}
			else if (index!==null) {
				try {
					this.qualityIndex = index;
					let level = index;
					this.autoQuality = false;
					if (index==this._qualities.length-1) {
						level = -1;
						this.autoQuality = true;
					}
					this._hls.currentLevel = level;
				}
				catch(err) {
	
				}
				return Promise.resolve();
			}
			else {
				return Promise.resolve();
			}
		}
	
		getNextQuality() {
			return new Promise((resolve,reject) => {
				let index = this.qualityIndex;
				resolve(this._qualities[index]);
			});
		}
		
		getCurrentQuality() {
			if (paella.utils.userAgent.system.iOS)// ||
				//paella.utils.userAgent.browser.Safari)
			{
				return Promise.resolve(0);
			}
			else {
				return new Promise((resolve,reject) => {
					resolve(this._qualities[this.qualityIndex]);
				});
			}
		}
	}

	paella.HLSPlayer = HLSPlayer;
	
	
	class HLSVideoFactory extends paella.VideoFactory {
		get config() {
			let hlsConfig = null;
			paella.player.config.player.methods.some((methodConfig) => {
				if (methodConfig.factory=="HLSVideoFactory") {
					hlsConfig = methodConfig;
				}
				return hlsConfig!=null;
			});
			return hlsConfig || {
				iOSMaxStreams: 1,
				androidMaxStreams: 1
			};
		}

		isStreamCompatible(streamData) {
			if (paella.videoFactories.HLSVideoFactory.s_instances===undefined) {
				paella.videoFactories.HLSVideoFactory.s_instances = 0;
			}
			try {
				let cfg = this.config;
				if ((paella.utils.userAgent.system.iOS &&
					paella.videoFactories.HLSVideoFactory.s_instances>=cfg.iOSMaxStreams) ||
					(paella.utils.userAgent.system.Android &&
					paella.videoFactories.HLSVideoFactory.s_instances>=cfg.androidMaxStreams))
			//	In some old mobile devices, playing a high number of HLS streams may cause that the browser tab crash
				{
					return false;
				}
				
				for (var key in streamData.sources) {
					if (key=='hls') return true;
				}
			}
			catch (e) {}
			return false;	
		}
	
		getVideoObject(id, streamData, rect) {
			++paella.videoFactories.HLSVideoFactory.s_instances;
			return new paella.HLSPlayer(id, streamData, rect.x, rect.y, rect.w, rect.h);
		}
	}

	paella.videoFactories.HLSVideoFactory = HLSVideoFactory;
	
})();

paella.addPlugin(() => {
    return class DefaultKeyPlugin extends paella.KeyPlugin {
        checkEnabled(onSuccess) {
			onSuccess(true);
        }
        
        getName() { return "es.upv.paella.defaultKeysPlugin"; }
    
        setup() {

        }

        onKeyPress(event) {
            // Matterhorn standard keys
			if (event.altKey && event.ctrlKey) {
				if (event.which==paella.Keys.P) {
                    this.togglePlayPause();
                    return true;
				}
				else if (event.which==paella.Keys.S) {
                    this.pause();
                    return true;
				}
				else if (event.which==paella.Keys.M) {
                    this.mute();
                    return true;
				}
				else if (event.which==paella.Keys.U) {
                    this.volumeUp();
                    return true;
				}
				else if (event.which==paella.Keys.D) {
                    this.volumeDown();
                    return true;
				}
			}
			else { // Paella player keys
				if (event.which==paella.Keys.Space) {
                    this.togglePlayPause();
                    return true;
				}
				else if (event.which==paella.Keys.Up) {
                    this.volumeUp();
                    return true;
				}
				else if (event.which==paella.Keys.Down) {
                    this.volumeDown();
                    return true;
				}
				else if (event.which==paella.Keys.M) {
                    this.mute();
                    return true;
				}
            }
            
            return false;
        }

        togglePlayPause() {
            paella.player.videoContainer.paused().then((p) => {
                p ? paella.player.play() : paella.player.pause();
            });
		}
	
		pause() {
			paella.player.pause();
		}
	
		mute() {
			var videoContainer = paella.player.videoContainer;
			if (videoContainer.muted) {
				videoContainer.unmute();
			}
			else {
				videoContainer.mute();
			}
			// videoContainer.volume().then(function(volume){
			// 	var newVolume = 0;
			// 	if (volume==0) { newVolume = 1.0; }
			// 	paella.player.videoContainer.setVolume({ master:newVolume, slave: 0});
			// });
		}
	
		volumeUp() {
			var videoContainer = paella.player.videoContainer;
			videoContainer.volume().then(function(volume){
				volume += 0.1;
				volume = (volume>1) ? 1.0:volume;
				paella.player.videoContainer.setVolume(volume);
			});
		}
	
		volumeDown() {
			var videoContainer = paella.player.videoContainer;
			videoContainer.volume().then(function(volume){
				volume -= 0.1;
				volume = (volume<0) ? 0.0:volume;
				paella.player.videoContainer.setVolume(volume);
			});
		}
    };
})

paella.addPlugin(() => {
    return class LegalPlugin extends paella.VideoOverlayButtonPlugin {
        getIndex() { return 0; }
        getSubclass() { return "legal"; }
        getAlignment() { return paella.player.config.plugins.list[this.getName()].position; }
        getDefaultToolTip() { return ""; }

        checkEnabled(onSuccess) {
            onSuccess(true);
        }

        setup() {
            let plugin = paella.player.config.plugins.list[this.getName()];
            let title = document.createElement('a');
            title.innerText = plugin.label;
            this._url = plugin.legalUrl;
            title.className = "";
            this.button.appendChild(title);
        }

        action(button) {
            window.open(this._url);
        }

        getName() { return "es.upv.paella.legalPlugin"; }
    }
});


paella.addPlugin(function() {
    return class LiveStreamIndicator extends paella.VideoOverlayButtonPlugin {
        isEditorVisible() { return paella.editor.instance!=null; }
        getIndex() {return 10;}
        getSubclass() { return "liveIndicator"; }
        getAlignment() { return 'right'; }
        getDefaultToolTip() { return paella.utils.dictionary.translate("This video is a live stream"); }
        getName() { return "es.upv.paella.liveStreamingIndicatorPlugin"; }

        checkEnabled(onSuccess) {
            onSuccess(paella.player.isLiveStream());
        }

        setup() {}

        action(button) {
            paella.messageBox.showMessage(paella.utils.dictionary.translate("Live streaming mode: This is a live video, so, some capabilities of the player are disabled"));
        }
    }
});

(() => {

class MpegDashVideo extends paella.Html5Video {

	constructor(id,stream,left,top,width,height) {
		super(id,stream,left,top,width,height,'mpd');
		this._posterFrame = null;
		this._player = null;
	}

	_loadDeps() {
		return new Promise((resolve,reject) => {
			if (!window.$paella_mpd) {
				paella.require(paella.baseUrl +'resources/deps/dash.all.js')
					.then(() => {
						window.$paella_mpd = true;
						resolve(window.$paella_mpd);
					});
			}
			else {
				resolve(window.$paella_mpd);
			}	
		});
	}

	_getQualityObject(item, index, bitrates) {
		var total = bitrates.length;
		var percent = Math.round(index * 100 / total);
		var label = index==0 ? "min":(index==total-1 ? "max":percent + "%");
		return {
			index: index,
			res: { w:null, h:null },
			bitrate: item.bitrate,
			src: null,
			toString:function() { return percent; },
			shortLabel:function() { return label; },
			compare:function(q2) { return this.bitrate - q2.bitrate; }
		};
	}

	webGlDidLoad() {
		// Register a new video loader in the webgl engine, to enable the
		// hls compatibility in webgl canvas
		bg.utils.HTTPResourceProvider.AddVideoLoader('mpd', (url,onSuccess,onFail) => {
			var video = document.createElement("video");
			s_preventVideoDump.push(video);
			// this.setupHls(video,url)
			// 	.then(() => onSuccess(video))
			// 	.catch(() => onFail());
		});
		return Promise.resolve();
	}

	loadVideoStream(canvasInstance,stream) {
		let This = this;
		return canvasInstance.loadVideo(this,stream,(videoElem) => {
			return new Promise((resolve,reject) => {
				this._loadDeps()
					.then(() => {
						
						var player = dashjs.MediaPlayer().create();
						player.initialize(videoElem,stream.src,true);
						player.getDebug().setLogToBrowserConsole(false);
						this._player = player;

						
						player.on(dashjs.MediaPlayer.events.STREAM_INITIALIZED, function(a,b) {
							var bitrates = player.getBitrateInfoListFor("video");
							This._deferredAction(function() {
								if (!This._firstPlay) {
									This._player.pause();
									This._firstPlay = true;
								}
								resolve();
							});
						});
				
					});
				});
		}); 
	}

	// load() {
	// 	let This = this;
	// 	return new Promise((resolve,reject) => {
	// 		var source = this._stream.sources.mpd;
	// 		if (source && source.length>0) {
	// 			source = source[0];
	// 			this._loadDeps()
	// 				.then(function() {
	// 					var context = dashContext;
	// 					var player = dashjs.MediaPlayer().create();
	// 					var dashContext = context;
	// 					player.initialize(This.video,source.src,true);
	// 					player.getDebug().setLogToBrowserConsole(false);
	// 					This._player = player;
	// 					player.on(dashjs.MediaPlayer.events.STREAM_INITIALIZED,function(a,b) {
	// 						var bitrates = player.getBitrateInfoListFor("video");
	// 						This._deferredAction(function() {
	// 							if (!This._firstPlay) {
	// 								This._player.pause();
	// 								This._firstPlay = true;	
	// 							}
	// 							resolve();
	// 						});
	// 					});
	// 				});
	// 		}
	// 		else {
	// 			reject(new Error("Invalid source"));
	// 		}
	// 	});
	// }

	supportAutoplay() {
		return true;
	}

	getQualities() {
		return new Promise((resolve) => {
			this._deferredAction(() => {
				if (!this._qualities) {
					this._qualities = [];
					this._player
						.getBitrateInfoListFor("video")

						.sort((a,b) => {
							return a.bitrate - b.bitrate;
						})

						.forEach((item,index,bitrates) => {
							this._qualities.push(this._getQualityObject(item,index,bitrates));
						});
						
					this.autoQualityIndex = this._qualities.length; 
					this._qualities.push({
						index: this.autoQualityIndex,
						res: { w:null, h:null },
						bitrate: -1,
						src: null,
						toString:function() { return "auto"; },
						shortLabel:function() { return "auto"; },
						compare:function(q2) { return this.bitrate - q2.bitrate; }
					});
					
				}
				resolve(this._qualities);
			});
		});
	}

	setQuality(index) {
		return new Promise((resolve,reject) => {
			let currentQuality = this._player.getQualityFor("video");
			if (index==this.autoQualityIndex) {
				this._player.setAutoSwitchQuality(true);
				resolve();
			}
			else if (index!=currentQuality) {
				this._player.setAutoSwitchQuality(false);
				this._player.off(dashjs.MediaPlayer.events.METRIC_CHANGED);
				this._player.on(dashjs.MediaPlayer.events.METRIC_CHANGED, (a,b) => {
					if (a.type=="metricchanged") {
						if (currentQuality!=this._player.getQualityFor("video")) {
							currentQuality = this._player.getQualityFor("video");
							resolve();
						}
					}
				});
				this._player.setQualityFor("video",index);
			}
			else {
				resolve();
			}
		});
	}

	getCurrentQuality() {
		return new Promise((resolve,reject) => {
			if (this._player.getAutoSwitchQuality()) {// auto quality
				resolve({
					index: this.autoQualityIndex,
					res: { w:null, h:null },
					bitrate: -1,
					src: null,
					toString:function() { return "auto"; },
					shortLabel:function() { return "auto"; },
					compare:function(q2) { return this.bitrate - q2.bitrate; }
				});
			}
			else {
				var index = this._player.getQualityFor("video");
				resolve(this._getQualityObject(this._qualities[index],index,this._player.getBitrateInfoListFor("video")));
			}
		});
	}

	unFreeze(){
		return paella_DeferredNotImplemented();
	}

	freeze(){
		return paella_DeferredNotImplemented();
	}

	unload() {
		this._callUnloadEvent();
		return paella_DeferredNotImplemented();
	}
}

paella.MpegDashVideo = MpegDashVideo;


class MpegDashVideoFactory extends paella.VideoFactory {
	isStreamCompatible(streamData) {
		try {
			if (paella.utils.userAgent.system.iOS) {
				return false;
			}
			for (var key in streamData.sources) {
				if (key=='mpd') return true;
			}
		}
		catch (e) {}
		return false;
	}

	getVideoObject(id, streamData, rect) {
		++paella.videoFactories.Html5VideoFactory.s_instances;
		return new paella.MpegDashVideo(id, streamData, rect.x, rect.y, rect.w, rect.h);
	}
}

paella.videoFactories.MpegDashVideoFactory = MpegDashVideoFactory;

})();


paella.addPlugin(function() {
	return class MultipleQualitiesPlugin extends paella.ButtonPlugin {
		
		getAlignment() { return 'right'; }
		getSubclass() { return "showMultipleQualitiesPlugin"; }
		getIconClass() { return 'icon-screen'; }
		getIndex() { return 2030; }
		getName() { return "es.upv.paella.multipleQualitiesPlugin"; }
		getDefaultToolTip() { return paella.utils.dictionary.translate("Change video quality"); }
		
		closeOnMouseOut() { return true; }
		
		checkEnabled(onSuccess) {
			this._available = [];
			paella.player.videoContainer.getQualities()
				.then((q) => {
					this._available = q;
					onSuccess(q.length>1);
				});
		}		
			
		setup() {
			this.setQualityLabel();
			paella.events.bind(paella.events.qualityChanged, (event) => this.setQualityLabel());
		}

		getButtonType() { return paella.ButtonPlugin.type.menuButton; }
		
		getMenuContent() {
			let buttonItems = [];

			const minVisibleQuality = this.config.minVisibleQuality !== undefined ? this.config.minVisibleQuality : 100;
			this._available.forEach((q,index) => {
				let resH = q.res && q.res.h || 0;
				if (resH>=minVisibleQuality || resH<=0) {
					buttonItems.push({
						id: q.shortLabel(),
						title: q.shortLabel(),
						value: index,
						icon: "",
						className: this.getButtonItemClass(q.shortLabel()),
						default: false
					});
				}
			});
			return buttonItems;
		}

		menuItemSelected(itemData) {
			paella.player.videoContainer.setQuality(itemData.value)
				.then(() => {
					paella.player.controls.hidePopUp(this.getName());
					this.setQualityLabel();
				});
		}

		setQualityLabel() {
			paella.player.videoContainer.getCurrentQuality()
				.then((q) => {
					this.setText(q.shortLabel());
				});
		}

		getButtonItemClass(profileName) {
			return 'multipleQualityItem ' + profileName;
		}
	}
});
paella.addPlugin(function() {
    return class PIPModePlugin extends paella.ButtonPlugin {
        getIndex() { return 551; }
        getAlignment() { return 'right'; }
        getSubclass() { return "PIPModeButton"; }
        getIconClass() { return 'icon-pip'; }
        getName() { return "es.upv.paella.pipModePlugin"; }
        checkEnabled(onSuccess) {
            var mainVideo = paella.player.videoContainer.masterVideo();
            var video = mainVideo.video;

            // PIP is only available with single stream videos
            if (paella.player.videoContainer.streamProvider.videoStreams.length!=1) {
                onSuccess(false);
            }
            else if (video && video.webkitSetPresentationMode) {
                onSuccess(true);
            }
            else if (video && 'pictureInPictureEnabled' in document) {
                onSuccess(true);
            }
            else {
                onSuccess(false);
            }
        }
        getDefaultToolTip() { return paella.utils.dictionary.translate("Set picture-in-picture mode."); }

        setup() {

        }

        action(button) {
            var video = paella.player.videoContainer.masterVideo().video;
            if (video.webkitSetPresentationMode) {
                if (video.webkitPresentationMode=="picture-in-picture") {
                    video.webkitSetPresentationMode("inline");
                }
                else {
                    video.webkitSetPresentationMode("picture-in-picture");
                }
            }
            else if ('pictureInPictureEnabled' in document) {
                if (video !== document.pictureInPictureElement) {
                    video.requestPictureInPicture();
                } else {
                    document.exitPictureInPicture();
                }
            }

        }
    }
});


paella.addPlugin(function() {
	return class PlayPauseButtonPlugin extends paella.ButtonPlugin {
		constructor() {
			super();
			this.playIconClass = 'icon-play';
			this.replayIconClass = 'icon-loop2';
			this.pauseIconClass = 'icon-pause';
			this.playSubclass = 'playButton';
			this.pauseSubclass = 'pauseButton';
		}
	
		getAlignment() { return 'left'; }
		getSubclass() { return this.playSubclass; }
		getIconClass() { return this.playIconClass; }
		getName() { return "es.upv.paella.playPauseButtonPlugin"; }
		getDefaultToolTip() { return paella.utils.dictionary.translate("Play"); }
		getIndex() { return 110; }
	
		checkEnabled(onSuccess) {
			onSuccess(true);
		}
	
		setup() {
			if (paella.player.playing()) {
				this.changeIconClass(this.playIconClass);
			}
			
			paella.events.bind(paella.events.play,(event) => {
				this.changeIconClass(this.pauseIconClass);
				this.changeSubclass(this.pauseSubclass);
				this.setToolTip(paella.utils.dictionary.translate("Pause"));
			});

			paella.events.bind(paella.events.pause,(event) => {
				this.changeIconClass(this.playIconClass);
				this.changeSubclass(this.playSubclass);
				this.setToolTip(paella.utils.dictionary.translate("Play"));
			});

			paella.events.bind(paella.events.ended,(event) => {
				this.changeIconClass(this.replayIconClass);
				this.changeSubclass(this.playSubclass);
				this.setToolTip(paella.utils.dictionary.translate("Play"));
			});
		}
	
		action(button) {
			paella.player.videoContainer.paused()
				.then(function(paused) {
					if (paused) {
						paella.player.play();
					}
					else {
						paella.player.pause();
					}
				});
		}
	}	
});



paella.addPlugin(function() {
	return class PlayButtonOnScreen extends paella.EventDrivenPlugin {
		constructor() {
			super();
			this.containerId = 'paella_plugin_PlayButtonOnScreen';
			this.container = null;
			this.enabled = true;
			this.isPlaying = false;
			this.showIcon = true;
			this.firstPlay = false;
		}
	
		checkEnabled(onSuccess) {
			this.showOnEnd = true;
			paella.data.read('relatedVideos', {id:paella.player.videoIdentifier}, (data) => {
                this.showOnEnd = !Array.isArray(data) ||  data.length == 0;
			});
			
			onSuccess(true);
		}
	
		getIndex() { return 1010; }
		getName() { return "es.upv.paella.playButtonOnScreenPlugin"; }
	
		setup() {
			this.container = paella.LazyThumbnailContainer.GetIconElement();
			paella.player.videoContainer.domElement.appendChild(this.container);
			$(this.container).click(() =>  this.onPlayButtonClick());
		}
	
		getEvents() {
			return [
				paella.events.ended,
				paella.events.endVideo,
				paella.events.play,
				paella.events.pause,
				paella.events.showEditor,
				paella.events.hideEditor
			];
		}
	
		onEvent(eventType,params) {
			switch (eventType) {
				case paella.events.ended:
				case paella.events.endVideo:
					this.endVideo();
					break;
				case paella.events.play:
					this.play();
					break;
				case paella.events.pause:
					this.pause();
					break;
				case paella.events.showEditor:
					this.showEditor();
					break;
				case paella.events.hideEditor:
					this.hideEditor();
					break;
			}
		}
	
		onPlayButtonClick() {
			this.firstPlay = true;
			this.checkStatus();
		}
	
		endVideo() {
			paella.player.videoContainer.ended()
			.then(ended => {
				if (ended) {
					this.isPlaying = false;
					this.showIcon = this.showOnEnd;
					this.checkStatus();
				} else {
					base.log.debug(`BTN ON SCREEN: The player is no longer in ended state.`);
				}
			});
		}
	
		play() {
			this.isPlaying = true;
			this.showIcon = false;
			if (!/dimmed/.test(this.container.className)) {
				this.container.className += " dimmed";
			}
			this.checkStatus();
		}
	
		pause() {
			this.isPlaying = false;
			this.showIcon = this.config.showOnPause;
			this.checkStatus();
		}
	
		showEditor() {
			this.enabled = false;
			this.checkStatus();
		}
	
		hideEditor() {
			this.enabled = true;
			this.checkStatus();
		}
		
		checkStatus() {
			if ((this.enabled && this.isPlaying) || !this.enabled || !this.showIcon) {
				$(this.container).hide();
			}
			// Only show play button if none of the video players require mouse events
			else if (!paella.player.videoContainer.streamProvider.videoPlayers.every((p) => p.canvasData.mouseEventsSupport)) {
				$(this.container).show();
			}
		}	
	}
});



paella.addPlugin(function() {
	return class PlaybackRate extends paella.ButtonPlugin {
		
		getAlignment() { return 'left'; }
		getSubclass() { return "showPlaybackRateButton"; }
		getIconClass() { return 'icon-screen'; }
		getIndex() { return 140; }
		getName() { return "es.upv.paella.playbackRatePlugin"; }
		getButtonType() { return paella.ButtonPlugin.type.menuButton; }
		getDefaultToolTip() { return paella.utils.dictionary.translate("Set playback rate"); }

		checkEnabled(onSuccess) {
			this.buttonItems = null;
			this.buttons =  [];
			this.selected_button =  null;
			this.defaultRate = null;
			this._domElement = null;
			this.available_rates =  null;
			var enabled = paella.player.videoContainer.masterVideo() instanceof paella.Html5Video;
			onSuccess(enabled && !paella.player.videoContainer.streamProvider.isLiveStreaming);
		}

		closeOnMouseOut() { return true; }

		setup() {
			this.defaultRate = 1.0;
			this.available_rates = this.config.availableRates || [0.75, 1, 1.25, 1.5];
		}

		getMenuContent() {
			let buttonItems = [];
			this.available_rates.forEach((rate) => {
				let profileName = rate + "x";
				buttonItems.push({
					id: profileName,
					title: profileName,
					value: rate,
					icon: "",
					className: this.getButtonItemClass(profileName),
					default: rate == 1.0
				});
			});

			return buttonItems;
		}

		menuItemSelected(itemData) {
			paella.player.videoContainer.setPlaybackRate(itemData.value);
			this.setText(itemData.title);
			paella.player.controls.hidePopUp(this.getName());
		}

		getText() {
			return "1x";
		}

		getButtonItemClass(profileName,selected) {
			return 'playbackRateItem ' + profileName  + ((selected) ? ' selected':'');
		}
	}
});

paella.addPlugin(function() {
	return class RatePlugin extends paella.ButtonPlugin {
		
		getAlignment() { return 'right'; }
		getSubclass() { return "rateButtonPlugin"; }
		getIconClass() { return 'icon-star'; }
		getIndex() { return 540; }
		getName() { return "es.upv.paella.ratePlugin"; }
		getButtonType() { return paella.ButtonPlugin.type.popUpButton; }
		getDefaultToolTip() { return paella.utils.dictionary.translate("Rate this video"); }		
		checkEnabled(onSuccess) {
			this.buttonItems = null;
			this.buttons =  [];
			this.selected_button =  null;
			this.score = 0;
			this.count = 0;
			this.myScore = 0;
			this.canVote = false;
			this.scoreContainer =  {
				header:null,
				rateButtons:null
			};
			paella.data.read('rate',{id:paella.initDelegate.getId()}, (data,status) => {
				if (data && typeof(data)=='object') {
					this.score = Number(data.mean).toFixed(1);
					this.count = data.count;
					this.myScore = data.myScore;
					this.canVote = data.canVote;
				}
				onSuccess(status);
			});
		}

		setup() {
		}

		setScore(s) {
			this.score = s;
			this.updateScore();
		}

		closeOnMouseOut() { return true; }

		updateHeader() {
			let score = paella.utils.dictionary.translate("Not rated");
			if (this.count>0) {
				score = '<i class="glyphicon glyphicon-star"></i>';
				score += ` ${ this.score } ${ this.count } ${ paella.utils.dictionary.translate('votes') }`;
			}

			this.scoreContainer.header.innerHTML = `
			<div>
				<h4>${ paella.utils.dictionary.translate('Video score') }:</h4>
				<h5>
					${ score }
				</h5>
				</h4>
				<h4>${ paella.utils.dictionary.translate('Vote:') }</h4>
			</div>
			`;
		}

		updateRateButtons() {
			this.scoreContainer.rateButtons.className = "rateButtons";
			this.buttons = [];
			if (this.canVote) {
				this.scoreContainer.rateButtons.innerText = "";
				for (let i = 0; i<5; ++i) {
					let btn = this.getStarButton(i + 1);
					this.buttons.push(btn);
					this.scoreContainer.rateButtons.appendChild(btn);
				}
			}
			else {
				this.scoreContainer.rateButtons.innerHTML = `<h5>${ paella.utils.dictionary.translate('Login to vote')}</h5>`;
			}
			this.updateVote();
		}

		buildContent(domElement) {
			var This = this;
			This._domElement = domElement;

			var header = document.createElement('div');
			domElement.appendChild(header);
			header.className = "rateContainerHeader";
			this.scoreContainer.header = header;
			this.updateHeader();

			var rateButtons = document.createElement('div');
			this.scoreContainer.rateButtons = rateButtons;
			domElement.appendChild(rateButtons);
			this.updateRateButtons();
		}

		getStarButton(score) {
			let This = this;
			let elem = document.createElement('i');
			elem.data = {
				score: score,
				active: false
			};
			elem.className = "starButton glyphicon glyphicon-star-empty";
			$(elem).click(function(event) {
				This.vote(this.data.score);
			});
			return elem;
		}

		vote(score) {
			this.myScore = score;
			let data = {
				mean: this.score,
				count: this.count,
				myScore: score,
				canVote: this.canVote
			};
			paella.data.write('rate',{id:paella.initDelegate.getId()}, data, (result) => {
				paella.data.read('rate',{id:paella.initDelegate.getId()}, (data,status) => {
					if (data && typeof(data)=='object') {
						this.score = Number(data.mean).toFixed(1);
						this.count = data.count;
						this.myScore = data.myScore;
						this.canVote = data.canVote;
					}
					this.updateHeader();
					this.updateRateButtons();
				});
			});
		}

		updateVote() {
			this.buttons.forEach((item,index) => {
				item.className = index<this.myScore ? "starButton glyphicon glyphicon-star" : "starButton glyphicon glyphicon-star-empty";
			});
		}
	}
});

// Change this data delegate to read the related videos form an external source
// Default behaviour is to get the related videos from the data.json file

paella.addDataDelegate("relatedVideos",() => {
    return class RelatedVideoDataDelegate extends paella.DataDelegate {
        read(context,params,onSuccess) {
            let videoMetadata = paella.player.videoLoader.getMetadata();
            if (videoMetadata.related) {
                onSuccess(videoMetadata.related);
            }
        }
    }
});

paella.addPlugin(() => {
    return class RelatedVideoPlugin extends paella.EventDrivenPlugin {
        getName() { return "es.upv.paella.relatedVideosPlugin"; }

        checkEnabled(onSuccess) {
            paella.data.read('relatedVideos', {id:paella.player.videoIdentifier}, (data) => {
                this._relatedVideos = data;
                onSuccess(Array.isArray(this._relatedVideos) &&  this._relatedVideos.length > 0);
            });
        }

        setup() {

        }

        getEvents() { return [
            paella.events.ended,
            paella.events.timeUpdate,
            paella.events.play,
            paella.events.seekTo,
            paella.events.seekToTime,
        ];}

        onEvent(eventType, params) {
            if (eventType == paella.events.ended) {
                this.showRelatedVideos();
            }
            else {
                this.hideRelatedVideos();
            }
        }

        showRelatedVideos() {
            this.hideRelatedVideos();
            let container = document.createElement('div');
            container.className = "related-video-container";

            function getRelatedVideoLink(data,className) {
                let linkContainer = document.createElement("a");
                linkContainer.className = "related-video-link " + className;
                linkContainer.innerHTML = `
                <img src="${ data.thumb }" alt="">
                <p>${ data.title }</p>
                `;
                linkContainer.addEventListener("click", function() {
                    try {
                        if (window.self !== window.top) {
                            window.parent.document.dispatchEvent(new CustomEvent('paella-change-video', { detail: data }));
                        }
                    }
                    catch (e) {

                    }
                    location.href = data.url;
                });
                return linkContainer;
            }

            this._messageContainer = paella.player.videoContainer.overlayContainer.addElement(container, {
                left: 0,
                right: 0,
                width: 1280,
                height: 720
            });
            switch (this._relatedVideos.length) {
            case 1:
                container.appendChild(getRelatedVideoLink(this._relatedVideos[0],'related-video-single'));
                break;
            case 2:
            default:
                container.appendChild(getRelatedVideoLink(this._relatedVideos[0],'related-video-dual-1'));
                container.appendChild(getRelatedVideoLink(this._relatedVideos[1],'related-video-dual-2'));
                break;
            }
            
            paella.player.videoContainer.attenuationEnabled = true;
        }

        hideRelatedVideos() {
            if (this._messageContainer) {
                paella.player.videoContainer.overlayContainer.removeElement(this._messageContainer);
                this._messageContainer = null;

                paella.player.videoContainer.attenuationEnabled = false;
            }
        }
    }
});


(() => {

class RTMPVideo extends paella.VideoElementBase {

	constructor(id,stream,left,top,width,height) {
		super(id,stream,'div',left,top,width,height);

		this._posterFrame = null;
		this._currentQuality = null;
		this._duration = 0;
		this._paused = true;
		this._streamName = null;
		this._flashId = null;
		this._swfContainer = null;
		this._flashVideo = null;
		this._volume = 1;

		this._flashId = id + 'Movie';
		this._streamName = 'rtmp';
		var This = this;

		this._stream.sources.rtmp.sort(function(a,b) {
			return a.res.h - b.res.h;
		});

		var processEvent = function(eventName,params) {
			if (eventName!="loadedmetadata" && eventName!="pause" && !This._isReady) {
				This._isReady = true;
				This._duration = params.duration;
				$(This.swfContainer).trigger("paella:flashvideoready");
			}
			if (eventName=="progress") {
				try { This.flashVideo.setVolume(This._volume); }
				catch(e) {}
				paella.log.debug("Flash video event: " + eventName + ", progress: " + This.flashVideo.currentProgress());
			}
			else if (eventName=="ended") {
				paella.log.debug("Flash video event: " + eventName);
				paella.events.trigger(paella.events.pause);
				paella.player.controls.showControls();
			}
			else {
				paella.log.debug("Flash video event: " + eventName);
			}
		};

		var eventReceived = function(eventName,params) {
			params = params.split(",");
			var processedParams = {};
			for (var i=0; i<params.length; ++i) {
				var splitted = params[i].split(":");
				var key = splitted[0];
				var value = splitted[1];
				if (value=="NaN") {
					value = NaN;
				}
				else if (/^true$/i.test(value)) {
					value = true;
				}
				else if (/^false$/i.test(value)) {
					value = false;
				}
				else if (!isNaN(parseFloat(value))) {
					value = parseFloat(value);
				}
				processedParams[key] = value;
			}
			processEvent(eventName,processedParams);
		};

		paella.events.bind(paella.events.flashVideoEvent,function(event,params) {
			if (This.flashId==params.source) {
				eventReceived(params.eventName,params.values);
			}
		});
	}

	get swfContainer() { return this._swfContainer; };

	get flashId() { return this._flashId; }

	get flashVideo() { return this._flashVideo; }

	_createSwfObject(swfFile,flashVars) {
		var id = this.identifier;
		var parameters = { wmode:'transparent' };

		var domElement = document.createElement('div');
		this.domElement.appendChild(domElement);
		domElement.id = id + "Movie";
		this._swfContainer = domElement;

		if (swfobject.hasFlashPlayerVersion("9.0.0")) {
			swfobject.embedSWF(swfFile,domElement.id,"100%","100%","9.0.0","",flashVars,parameters, null, function callbackFn(e){
				if (e.success == false){
					var message = document.createElement('div');

					var header = document.createElement('h3');
					header.innerText = paella.utils.dictionary.translate("Flash player problem");
					var text = document.createElement('div');
					text.innerHTML = paella.utils.dictionary.translate("A problem occurred trying to load flash player.") + "<br>" +
						paella.utils.dictionary.translate("Please go to {0} and install it.")
							.replace("{0}", "<a style='color: #800000; text-decoration: underline;' href='http://www.adobe.com/go/getflash'>http://www.adobe.com/go/getflash</a>") + '<br>' +

						paella.utils.dictionary.translate("If the problem presist, contact us.");

					var link = document.createElement('a');
					link.setAttribute("href", "http://www.adobe.com/go/getflash");
					link.innerHTML = '<img style="margin:5px;" src="http://www.adobe.com/images/shared/download_buttons/get_flash_player.gif" alt="Obtener Adobe Flash Player" />';

					message.appendChild(header);
					message.appendChild(text);
					message.appendChild(link);

					paella.messageBox.showError(message.innerHTML);
				}
			});
		}
		else {
			var message = document.createElement('div');

			var header = document.createElement('h3');
			header.innerText = paella.utils.dictionary.translate("Flash player needed");

			var text = document.createElement('div');

			text.innerHTML = paella.utils.dictionary.translate("You need at least Flash player 9 installed.") + "<br>" +
				paella.utils.dictionary.translate("Please go to {0} and install it.")
					.replace("{0}", "<a style='color: #800000; text-decoration: underline;' href='http://www.adobe.com/go/getflash'>http://www.adobe.com/go/getflash</a>");

			var link = document.createElement('a');
			link.setAttribute("href", "http://www.adobe.com/go/getflash");
			link.innerHTML = '<img style="margin:5px;" src="http://www.adobe.com/images/shared/download_buttons/get_flash_player.gif" alt="Obtener Adobe Flash Player" />';

			message.appendChild(header);
			message.appendChild(text);
			message.appendChild(link);

			paella.messageBox.showError(message.innerHTML);
		}

		var flashObj = $('#' + domElement.id)[0];
		return flashObj;
	}

	_deferredAction(action) {
		return new Promise((resolve,reject) => {
			if (this.ready) {
				resolve(action());
			}
			else {
				$(this.swfContainer).bind('paella:flashvideoready', () => {
					this._ready = true;
					resolve(action());
				});
			}
		});
	}

	_getQualityObject(index, s) {
		return {
			index: index,
			res: s.res,
			src: s.src,
			toString:function() { return this.res.w + "x" + this.res.h; },
			shortLabel:function() { return this.res.h + "p"; },
			compare:function(q2) { return this.res.w*this.res.h - q2.res.w*q2.res.h; }
		};
	}

	// Initialization functions
	getVideoData() {
		let FlashVideoPlugin = this;
		return new Promise((resolve,reject) => {
			this._deferredAction(() => {
				let videoData = {
					duration: FlashVideoPlugin.flashVideo.duration(),
					currentTime: FlashVideoPlugin.flashVideo.getCurrentTime(),
					volume: FlashVideoPlugin.flashVideo.getVolume(),
					paused: FlashVideoPlugin._paused,
					ended: FlashVideoPlugin._ended,
					res: {
						w: FlashVideoPlugin.flashVideo.getWidth(),
						h: FlashVideoPlugin.flashVideo.getHeight()
					}
				};
				resolve(videoData);
			});
		});
	}

	setPosterFrame(url) {
		if (this._posterFrame==null) {
			this._posterFrame = url;
			var posterFrame = document.createElement('img');
			posterFrame.src = url;
			posterFrame.className = "videoPosterFrameImage";
			posterFrame.alt = "poster frame";
			this.domElement.appendChild(posterFrame);
			this._posterFrameElement = posterFrame;
		}
	//	this.video.setAttribute("poster",url);
	}

	setAutoplay(auto) {
		this._autoplay = auto;
	}

	load() {
		var This = this;
		var sources = this._stream.sources.rtmp;
		if (this._currentQuality===null && this._videoQualityStrategy) {
			this._currentQuality = this._videoQualityStrategy.getQualityIndex(sources);
		}

		var isValid = function(stream) {
			return stream.src && typeof(stream.src)=='object' && stream.src.server && stream.src.stream;
		};

		var stream = this._currentQuality<sources.length ? sources[this._currentQuality]:null;
		if (stream) {
			if (!isValid(stream)) {
				return paella_DeferredRejected(new Error("Invalid video data"));
			}
			else {
				var subscription = false;
				if (stream.src.requiresSubscription===undefined && paella.player.config.player.rtmpSettings) {
					subscription = paella.player.config.player.rtmpSettings.requiresSubscription || false;
				}
				else if (stream.src.requiresSubscription) {
					subscription = stream.src.requiresSubscription;
				}
				var parameters = {};
				var swfName = 'resources/deps/player_streaming.swf';
				if (this._autoplay) {
					parameters.autoplay = this._autoplay;
				}
				if (paella.utils.parameters.get('debug')=="true") {
					parameters.debugMode = true;
				}

				parameters.playerId = this.flashId;
				parameters.isLiveStream = stream.isLiveStream!==undefined ? stream.isLiveStream:false;
				parameters.server = stream.src.server;
				parameters.stream = stream.src.stream;
				parameters.subscribe = subscription;
				if (paella.player.config.player.rtmpSettings && paella.player.config.player.rtmpSettings.bufferTime!==undefined) {
					parameters.bufferTime = paella.player.config.player.rtmpSettings.bufferTime;
				}
				this._flashVideo = this._createSwfObject(swfName,parameters);

				$(this.swfContainer).trigger("paella:flashvideoready");

				return this._deferredAction(function() {
					return stream;
				});
			}

		}
		else {
			return paella_DeferredRejected(new Error("Could not load video: invalid quality stream index"));
		}
	}

	getQualities() {
		return new Promise((resolve,reject) => {
			setTimeout(() => {
				var result = [];
				var sources = this._stream.sources.rtmp;
				var index = -1;
				sources.forEach((s) => {
					index++;
					result.push(this._getQualityObject(index,s));
				});
				resolve(result);
			},50);
		});
	}

	setQuality(index) {
		index = index!==undefined && index!==null ? index:0;
		return new Promise((resolve,reject) => {
			var paused = this._paused;
			var sources = this._stream.sources.rtmp;
			this._currentQuality = index<sources.length ? index:0;
			var source = sources[index];
			this._ready = false;
			this._isReady = false;
			this.load()
				.then(function() {
					resolve();
				});
		});
	}

	getCurrentQuality() {
		return new Promise((resolve,reject) => {
			resolve(this._getQualityObject(this._currentQuality,
										   this._stream.sources.rtmp[this._currentQuality]));
		});
	}

	play() {
		var This = this;
		return this._deferredAction(function() {
			if (This._posterFrameElement) {
				This._posterFrameElement.parentNode.removeChild(This._posterFrameElement);
				This._posterFrameElement = null;
			}
			This._paused = false;
			This.flashVideo.play();
		});
	}

	pause() {
		var This = this;
		return this._deferredAction(function() {
			This._paused = true;
			This.flashVideo.pause();
		});
	}

	isPaused() {
		var This = this;
		return this._deferredAction(function() {
			return This._paused;
		});
	}

	duration() {
		var This = this;
		return this._deferredAction(function() {
			return This.flashVideo.duration();
		});
	}

	setCurrentTime(time) {
		var This = this;
		return this._deferredAction(function() {
			var duration = This.flashVideo.duration();
			This.flashVideo.seekTo(time * 100 / duration);
		});
	}

	currentTime() {
		var This = this;
		return this._deferredAction(function() {
			return This.flashVideo.getCurrentTime();
		});
	}

	setVolume(volume) {
		var This = this;
		this._volume = volume;
		return this._deferredAction(function() {
			This.flashVideo.setVolume(volume);
		});
	}

	volume() {
		var This = this;
		return this._deferredAction(function() {
			return This.flashVideo.getVolume();
		});
	}

	setPlaybackRate(rate) {
		var This = this;
		return this._deferredAction(function() {
			This._playbackRate = rate;
		});
	}

	playbackRate() {
		var This = this;
		return this._deferredAction(function() {
			return This._playbackRate;
		});
	}

	goFullScreen() {
		return paella_DeferredNotImplemented();
	}

	unFreeze(){
		return this._deferredAction(function() {});
	}

	freeze() {
		return this._deferredAction(function() {});
	}

	unload() {
		this._callUnloadEvent();
		return paella_DeferredNotImplemented();
	}

	getDimensions() {
		return paella_DeferredNotImplemented();
	}
}

paella.RTMPVideo = RTMPVideo;

class RTMPVideoFactory extends paella.VideoFactory {
	isStreamCompatible(streamData) {
		try {
			if (paella.utils.userAgent.system.iOS || paella.utils.userAgent.system.Android) {
				return false;
			}
			for (var key in streamData.sources) {
				if (key=='rtmp') return true;
			}
		}
		catch (e) {}
		return false;
	}

	getVideoObject(id, streamData, rect) {
		return new paella.RTMPVideo(id, streamData, rect.x, rect.y, rect.w, rect.h);
	}
}

paella.videoFactories.RTMPVideoFactory = RTMPVideoFactory;

})();
/////////////////////////////////////////////////
// Caption Search
/////////////////////////////////////////////////
paella.addPlugin(function() {
	return class CaptionsSearchPlugIn extends paella.SearchServicePlugIn {
		getName() { return "es.upv.paella.search.captionsSearchPlugin"; }
	
		search(text, next) {
			paella.captions.search(text, next);
		}	
	}
});

paella.addPlugin(function() {
	return class SearchPlugin extends paella.ButtonPlugin {
		getAlignment() { return 'right'; }
		getSubclass() { return 'searchButton'; }
		getIconClass() { return 'icon-binoculars'; }
		getName() { return "es.upv.paella.searchPlugin"; }
		getButtonType() { return paella.ButtonPlugin.type.popUpButton; }	
		getDefaultToolTip() { return paella.utils.dictionary.translate("Search"); }
		getIndex() {return 510;}
		
		closeOnMouseOut() { return true; }
		
		checkEnabled(onSuccess) {
			this._open = false;
			this._sortDefault = 'time';
			this._colorSearch = false;
			this._localImages = null;
			this._searchTimer = null;
			this._searchTimerTime = 1500;
			this._searchBody = null;
			this._trimming = null;
			onSuccess(true);
		}

		setup() {
			var self = this;
			$('.searchButton').click(function(event){
				if(self._open){
					self._open = false;
				}
				else {
					self._open = true;
					setTimeout(function(){
						$("#searchBarInput").focus();
					}, 0);
				}
			});
			//GET THE FRAME LIST
			self._localImages = paella.initDelegate.initParams.videoLoader.frameList;

			//config
			self._colorSearch = self.config.colorSearch || false;
			self._sortDefault = self.config.sortType || "time";
			
			paella.events.bind(paella.events.controlBarWillHide, function(evt) { if(self._open)paella.player.controls.cancelHideBar(); });
			paella.player.videoContainer.trimming()
				.then((trimData) => {
					self._trimming = trimData;
				});
		}

		prettyTime(seconds){
			// TIME FORMAT
			var hou = Math.floor(seconds / 3600)%24;
			hou = ("00"+hou).slice(hou.toString().length);

			var min = Math.floor(seconds / 60)%60;
			min = ("00"+min).slice(min.toString().length);

			var sec = Math.floor(seconds % 60);
			sec = ("00"+sec).slice(sec.toString().length);
			var timestr = (hou+":"+min+":"+sec);

			return timestr;
		}

		search(text,cb){
			paella.searchService.search(text, cb);
		}

		getPreviewImage(time){
			var thisClass = this;
			var keys = Object.keys(thisClass._localImages);

			keys.push(time);

			keys.sort(function(a,b){
				return parseInt(a)-parseInt(b);
			});

			var n = keys.indexOf(time)-1;
			n = (n > 0) ? n : 0;

			var i = keys[n];
			i=parseInt(i);

			return thisClass._localImages[i].url;
		}

		createLoadingElement(parent){
			var loadingResults = document.createElement('div');
			loadingResults.className = "loader";

			var htmlLoader = "<svg version=\"1.1\" id=\"loader-1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" width=\"40px\" height=\"40px\" viewBox=\"0 0 50 50\" style=\"enable-background:new 0 0 50 50;\" xml:space=\"preserve\">"+
			"<path fill=\"#000\" d=\"M25.251,6.461c-10.318,0-18.683,8.365-18.683,18.683h4.068c0-8.071,6.543-14.615,14.615-14.615V6.461z\">"+
			"<animateTransform attributeType=\"xml\""+
			"attributeName=\"transform\""+
			"type=\"rotate\""+
			"from=\"0 25 25\""+
			"to=\"360 25 25\""+
			"dur=\"0.6s\""+
			"repeatCount=\"indefinite\"/>"+
			"</path>"+
			"</svg>";
			loadingResults.innerHTML = htmlLoader;
			parent.appendChild(loadingResults);
			var sBodyText = document.createElement('p');
			sBodyText.className = 'sBodyText';
			sBodyText.innerText = paella.utils.dictionary.translate("Searching") + "...";
			parent.appendChild(sBodyText);
		}

		createNotResultsFound(parent){
			var noResults = document.createElement('div');
			noResults.className = "noResults";
			noResults.innerText = paella.utils.dictionary.translate("Sorry! No results found.");
			parent.appendChild(noResults);
		}

		doSearch(txt,searchBody) {
			var thisClass = this;
			$(searchBody).empty();

			//LOADING CONTAINER
			thisClass.createLoadingElement(searchBody);
		
			thisClass.search(txt, function(err, results){

			$(searchBody).empty();
			//BUILD SEARCH RESULTS
			if(!err){
				if(results.length == 0){ // 0 RESULTS FOUND
					thisClass.createNotResultsFound(searchBody);
				}
				else {
					for(var i=0; i<results.length; i++){ // FILL THE BODY CONTAINER WITH RESULTS
						if (thisClass._trimming.enabled && results[i].time <= thisClass._trimming.start){
							continue;
						}
						//SEARCH SORT TYPE (TIME oR SCoRE)
						if(thisClass._sortDefault == 'score') {
							results.sort(function(a,b){
								return b.score - a.score;
							});
						}
						if(thisClass._sortDefault == 'time') {
							results.sort(function(a,b){
								return a.time - b.time;
							});
						}

						var sBodyInnerContainer = document.createElement('div');
						sBodyInnerContainer.className = 'sBodyInnerContainer';
						
						//COLOR
						if(thisClass._colorSearch){ 

							if(results[i].score <= 0.3) {$(sBodyInnerContainer).addClass('redScore');}

							if(results[i].score >= 0.7) {$(sBodyInnerContainer).addClass('greenScore');}
						}

						var TimePicContainer = document.createElement('div');
						TimePicContainer.className = 'TimePicContainer';


						var sBodyPicture = document.createElement('img');
						sBodyPicture.className = 'sBodyPicture';
						sBodyPicture.src = thisClass.getPreviewImage(results[i].time);

						
						var sBodyText = document.createElement('p');
						sBodyText.className = 'sBodyText';
						let time = thisClass._trimming.enabled ? results[i].time - thisClass._trimming.start : results[i].time;
						sBodyText.innerHTML = "<span class='timeSpan'>"+thisClass.prettyTime(time)+"</span>"+paella.AntiXSS.htmlEscape(results[i].content);


						TimePicContainer.appendChild(sBodyPicture);
						

						sBodyInnerContainer.appendChild(TimePicContainer);
						sBodyInnerContainer.appendChild(sBodyText);
						

						searchBody.appendChild(sBodyInnerContainer);
						//ADD SECS TO DOM FOR EASY HANDLE
						sBodyInnerContainer.setAttribute('sec', time);

						//jQuery Binds for the search
						$(sBodyInnerContainer).hover(
							function(){ 
								$(this).css('background-color','#faa166');	           		
							},
							function(){ 
								$(this).removeAttr('style');
							}
						);

						$(sBodyInnerContainer).click(function(){ 
							var sec = $(this).attr("sec");
							paella.player.videoContainer.seekToTime(parseInt(sec));
							paella.player.play();
						});
					}
				}
			}
			});
		}

		buildContent(domElement) {
			var thisClass = this;
			var myUrl = null;

			//SEARCH CONTAINER
			var searchPluginContainer = document.createElement('div');
			searchPluginContainer.className = 'searchPluginContainer';
				
				//SEARCH BODY
				var searchBody = document.createElement('div');
				searchBody.className = 'searchBody';
				searchPluginContainer.appendChild(searchBody);
				
				thisClass._searchBody = searchBody;


			//SEARCH BAR
			var searchBar = document.createElement('div');
			searchBar.className = 'searchBar';
			searchPluginContainer.appendChild(searchBar);

				//INPUT
				var input = document.createElement("input");
				input.className = "searchBarInput";
				input.type = "text";
				input.id ="searchBarInput";
				input.name = "searchString";
				input.placeholder = paella.utils.dictionary.translate("Search");
				searchBar.appendChild(input);

				$(input).change(function(){
					var text = $(input).val();
					if(thisClass._searchTimer != null){
						thisClass._searchTimer.cancel();
					}
					if(text!=""){
						thisClass.doSearch(text, searchBody);
					}
				});

				$(input).keyup(function(event){
					if(event.keyCode != 13){ //IF no ENTER PRESSED SETUP THE TIMER
						var text = $(input).val();
						if(thisClass._searchTimer != null){
							thisClass._searchTimer.cancel();
						}
						if(text!=""){
							thisClass._searchTimer = new paella.utils.Timer(function(timer) {
								thisClass.doSearch(text, searchBody);
							}, thisClass._searchTimerTime);
						}
						else {
							$(thisClass._searchBody).empty();
						}
					}			
				});
				
				$(input).focus(function(){
					paella.keyManager.enabled = false;
				});
				
				$(input).focusout(function(){
					paella.keyManager.enabled = true;
				});
				
				

			domElement.appendChild(searchPluginContainer);

		}

	}
});


paella.addPlugin(function() {
	return class SharePlugin extends paella.ButtonPlugin {
		getAlignment() { return 'right'; }
		getSubclass() { return 'shareButtonPlugin'; }
		getIconClass() { return 'icon-social'; }
		getIndex() { return 560; }
		getName() { return 'es.upv.paella.sharePlugin'; }
		getButtonType() { return paella.ButtonPlugin.type.popUpButton; }
		getDefaultToolTip() { return paella.utils.dictionary.translate('Share this video'); }
		
		checkEnabled(onSuccess) { onSuccess(true); }
		closeOnMouseOut() { return false; }

		setup() {
		}

		buildEmbed() {
			var self = this;
			var embed = document.createElement('div');

			embed.innerHTML = `
				<div>
					<div class="form-group">
						<label class="control-label">
							<input id="share-video-responsive" type="checkbox" value="3" name="mailId[]" > ${ paella.utils.dictionary.translate('Responsive design') }
						</label>
					</div>

					<div id="share-video-block-size" class="form-group">
						<label class="control-label"> ${ paella.utils.dictionary.translate('Video resolution') } </label>
						
						<div class="row">
							<div class="col-sm-6">
								<select id="share-video-size" class="form-control input-sm">
									<option value="640x360">360p</option>
									<option value="854x480">480p</option>
									<option value="1280x720">720p (HD)</option>
									<option value="1920x1080">1080p (Full HD)</option>
									<option value="2560x1440">1440p (2.5K)</option>
									<option value="3840x2160">2160p (4K UHD)</option>
									<option value="custom">${ paella.utils.dictionary.translate('Custom size') }</option>
								</select>
							</div>
							<div class="col-sm-3">
								<input id="share-video-width" type="number" class="form-control input-sm" value="640" disabled="disabled">
							</div>
							<div class="col-sm-3">
								<input id="share-video-height" type="number" class="form-control input-sm" value="360" disabled="disabled">
							</div>
						</div>
					</div>	

					<div id="share-video-block-resp" class="form-group" style="display:none;">
						<label class="control-label"> ${ paella.utils.dictionary.translate('Video resolution') } </label>
						
						<select id="share-video-size-resp" class="form-control input-sm">
							<option value="25">25%</option>
							<option value="33">33%</option>
							<option value="50">50%</option>
							<option value="33">66%</option>
							<option value="75">75%</option>
							<option value="100">100%</option>
						</select>						
					</div>						
					
					<div class="form-group">
						<label class="control-label">${ paella.utils.dictionary.translate('Embed code') }</label>
						
						<div id="share-video-embed" class="alert alert-share">
						</div>
					</div>
				</div>
			`;


			embed.querySelector("#share-video-responsive").onchange=function(event){
				var responsive = self._domElement.querySelector("#share-video-responsive").checked;
				if (responsive) {
					self._domElement.querySelector("#share-video-block-resp").style.display = "block";
					self._domElement.querySelector("#share-video-block-size").style.display = "none";
				}
				else {
					self._domElement.querySelector("#share-video-block-resp").style.display = "none";
					self._domElement.querySelector("#share-video-block-size").style.display = "block";
				}
				self.updateEmbedCode(); 
			}

			embed.querySelector("#share-video-size-resp").onchange=function(event){ self.updateEmbedCode(); }
			embed.querySelector("#share-video-width").onchange=function(event){ self.updateEmbedCode(); }
			embed.querySelector("#share-video-height").onchange=function(event){ self.updateEmbedCode(); }
			
			embed.querySelector("#share-video-size").onchange=function(event){ 
				var value = event.target? event.target.value: event.toElement.value;
				
				if (value == "custom") {
					embed.querySelector("#share-video-width").disabled = false;
					embed.querySelector("#share-video-height").disabled = false;
				}
				else {
					embed.querySelector("#share-video-width").disabled = true;
					embed.querySelector("#share-video-height").disabled = true;

					var size = value.trim().split("x");
					embed.querySelector("#share-video-width").value = size[0];
					embed.querySelector("#share-video-height").value = size[1];
				}
				self.updateEmbedCode();
			}
			return embed;
		}

		buildSocial() {
			var self = this;
			var social = document.createElement('div');
			social.innerHTML = `
				<div>
					<div class="form-group">
						<label class="control-label">${ paella.utils.dictionary.translate('Share on social networks') }</label>
						<div class="row" style="margin:0;">	
							<span id="share-btn-facebook" class="share-button button-icon icon-facebook" ></span>
							<span id="share-btn-twitter" class="share-button button-icon icon-twitter" ></span>
							<span id="share-btn-linkedin" class="share-button button-icon icon-linkedin" ></span>
						</div>
					</div>
				</div>
			`;

			social.querySelector("#share-btn-facebook").onclick=function(event){ self.onSocialClick('facebook'); }
			social.querySelector("#share-btn-twitter").onclick=function(event){ self.onSocialClick('twitter'); }
			social.querySelector("#share-btn-linkedin").onclick=function(event){ self.onSocialClick('linkedin'); }
			return social;
		}


		buildContent(domElement) {
			var hideSocial = this.config && this.config.hideSocial;
			this._domElement = domElement;

			domElement.appendChild(this.buildEmbed());
			if (!hideSocial) {
				domElement.appendChild(this.buildSocial());
			}

			this.updateEmbedCode();
		}


		getVideoUrl() {
			var url = document.location.href;
			return url;
		}

		onSocialClick(network) {
			var videoUrl = encodeURIComponent(this.getVideoUrl());
			var title = encodeURIComponent("");
			var shareUrl;

			switch (network) {
				case ('twitter'):
					shareUrl = `http://twitter.com/share?url=${videoUrl}&text=${title}`;
					break;
				case ('facebook'):
					shareUrl = `http://www.facebook.com/sharer.php?u=${videoUrl}&p[title]=${title}`;
					break;
				case ('linkedin'):
					shareUrl = `https://www.linkedin.com/shareArticle?mini=true&url=${videoUrl}&title=${title}`;
					break;
			}
			
			if (shareUrl) {
				window.open(shareUrl);
			}
			paella.player.controls.hidePopUp(this.getName());
		}

		updateEmbedCode() {
			var videoUrl = this.getVideoUrl();
			var responsive = this._domElement.querySelector("#share-video-responsive").checked;
			var width = this._domElement.querySelector("#share-video-width").value;
			var height = this._domElement.querySelector("#share-video-height").value;
			var respSize = this._domElement.querySelector("#share-video-size-resp").value;
			
			var embedCode = '';
			if (responsive) {
				embedCode = `<div style="width:${respSize}%"><div style="position:relative;display:block;height:0;padding:0;overflow:hidden;padding-bottom:56.25%"> <iframe allowfullscreen="true" webkitallowfullscreen="true" mozallowfullscreen="true" src="${videoUrl}" style="border:0px #FFFFFF none; position:absolute; width:100%; height:100%" name="Paella Player" scrolling="no" frameborder="0" marginheight="0px" marginwidth="0px" width="100%" height="100%"></iframe> </div></div>`;
			}
			else {
				embedCode = `<iframe allowfullscreen="true" webkitallowfullscreen="true" mozallowfullscreen="true" src="${videoUrl}" style="border:0px #FFFFFF none;" name="Paella Player" scrolling="no" frameborder="0" marginheight="0px" marginwidth="0px" width="${width}" height="${height}"></iframe>`;
			}

			this._domElement.querySelector("#share-video-embed").innerText = embedCode;
		}

	}
});

paella.addPlugin(function() {
	return class ShowEditorPlugin extends paella.VideoOverlayButtonPlugin {
		getName() {
			return "es.upv.paella.showEditorPlugin";
		}
		getSubclass() { return "showEditorButton"; }
		getIconClass() { return 'icon-pencil'; }
		getAlignment() { return 'right'; }
		getIndex() {return 10;}
		getDefaultToolTip() { return paella.utils.dictionary.translate("Enter editor mode"); }

		checkEnabled(onSuccess) {			
			if (this.config.editorUrl) {
				paella.initDelegate.initParams.accessControl.canWrite()
				.then((canWrite)=>{
					var enabled = (canWrite); // && !paella.utils.userAgent.browser.IsMobileVersion && !paella.player.isLiveStream());					
					onSuccess(enabled);
				});	
			}
			else {				
				onSuccess(false);
			}
		}

		action(button) {
			var editorUrl = this.config.editorUrl.replace("{id}", paella.player.videoIdentifier);
			window.location.href = editorUrl;
		}
	}
});


paella.addPlugin(function() {
	return class ThemeChooserPlugin extends paella.ButtonPlugin {
		getAlignment() { return 'right'; }
		getSubclass() { return "themeChooserPlugin"; }
		getIconClass() { return 'icon-paintbrush'; }
		getIndex() { return 2030; }
		getName() { return "es.upv.paella.themeChooserPlugin"; }	
		getDefaultToolTip() { return paella.utils.dictionary.translate("Change theme"); }	
		getButtonType() { return paella.ButtonPlugin.type.popUpButton; }
		
		checkEnabled(onSuccess) { 
			this.currentUrl = null;
			this.currentMaster = null;
			this.currentSlave = null;
			this.availableMasters = [];
			this.availableSlaves = [];
			if ( paella.player.config.skin && paella.player.config.skin.available
				&& (paella.player.config.skin.available instanceof Array) 
				&& (paella.player.config.skin.available.length >0)) {
				
				onSuccess(true);			
			}
			else {
				onSuccess(false);
			}
		}
		
		buildContent(domElement) {
			var This = this;
			paella.player.config.skin.available.forEach(function(item){
				var elem = document.createElement('div');
				elem.className = "themebutton";
				elem.innerText = item.replace('-',' ').replace('_',' ');
				$(elem).click(function(event) {
					paella.utils.skin.set(item);
					paella.player.controls.hidePopUp(This.getName());
				});
				
				domElement.appendChild(elem);			
			});
		}
	}
});


paella.addPlugin(() => {
	return class TimeMarksPlaybackCanvasPlugin extends paella.PlaybackCanvasPlugin {
		getName() { return "es.upv.paella.timeMarksPlaybackCanvasPlugin"; }

		setup() {
			this._frameList = paella.initDelegate.initParams.videoLoader.frameList;
			this._frameKeys = Object.keys(this._frameList);
			if( !this._frameList || !this._frameKeys.length) {
				this._hasSlides = false;
			}
			else {
				this._hasSlides = true;
				this._frameKeys = this._frameKeys.sort((a, b) => parseInt(a)-parseInt(b));
			}
		}

		drawCanvas(context,width,height,videoData) {
			if (this._hasSlides) {
				this._frameKeys.forEach((l) => {
					l = parseInt(l);
					let timeInstant = videoData.trimming.enabled ? l - videoData.trimming.start : l;
					if (timeInstant>0 && timeInstant<videoData.trimming.duration) {
						let left = timeInstant * width / videoData.trimming.duration;
						this.drawTimeMark(context, left, height);
					}

				})
			}
		}

		drawTimeMark(ctx,left,height){
			ctx[1].fillStyle = this.config.color;
			ctx[1].fillRect(left,0,1,height);	
		}
	}
});


paella.addDataDelegate("cameraTrack",() => {
    return class TrackCameraDataDelegate extends paella.DataDelegate {
        read(context,params,onSuccess) {
            let videoUrl = paella.player.videoLoader.getVideoUrl();
            if (videoUrl) {
                videoUrl += 'trackhd.json';
                paella.utils.ajax.get({ url:videoUrl },
                    (data) => {
                        if (typeof(data)==="string") {
                            try {
                                data = JSON.parse(data);
                            }
                            catch(err) {}
                        }
                        data.positions.sort((a,b) => {
                            return a.time-b.time;
                        });
                        onSuccess(data);
                    },
                    () => onSuccess(null) );
            }
            else {
                onSuccess(null);
            }
        }

        write(context,params,value,onSuccess) {
        }

        remove(context,params,onSuccess) {
        }
    };
});

(() => {
    // Used to connect the toolbar button with the track4k plugin
    let g_track4kPlugin = null;


    function updatePosition(positionData,nextFrameData) {
        let twinTime = nextFrameData ? (nextFrameData.time - positionData.time) * 1000 : 100;
        if (twinTime>2000) twinTime = 2000;
        let rect = (positionData && positionData.rect) || [0, 0, 0, 0];
        let offset_x = Math.abs(rect[0]);
        let offset_y = Math.abs(rect[1]);
        let view_width = rect[2];
        let view_height = rect[3];
        let zoom = this._videoData.originalWidth / view_width;
        let left = offset_x / this._videoData.originalWidth;
        let top = offset_y / this._videoData.originalHeight;
        paella.player.videoContainer.masterVideo().setZoom(zoom  * 100,left * zoom * 100, top * zoom * 100, twinTime);
    }

    function nextFrame(time) {
        let index = -1;
        time = Math.round(time);
        this._trackData.some((data,i) => {
            if (data.time>=time) {
                index = i;
            }
            return index!==-1;
        });
        // Index contains the current frame index
        if (this._trackData.length>index+1) {
            return this._trackData[index+1];
        }
        else {
            return null;
        }
    }

    function prevFrame(time) {
        let frame = this._trackData[0];
        time = Math.round(time);
        this._trackData.some((data, i, frames) => {
          if (frames[i+1]) {
            if (data.time <= time && frames[i+1].time > time) {
                return true;
            }
          } else {
            return true;
          }
          frame = data;
          return false;
        });
        return frame;
    }

    function curFrame(time) {
        let frameRect = null;
        time = Math.round(time);
        this._trackData.some((data,i, frames) => {
          if (data.time <= time) {
            if (frames[i+1]) {
              if (frames[i+1].time > time) {
                frameRect = data;
              }
            } else {
              frameRect = data;
            }
          }
          return frameRect!==null;
        });
        return frameRect;
    }


    paella.addPlugin(function() {
        return class Track4KPlugin extends paella.EventDrivenPlugin {
            constructor() {
                super();

                g_track4kPlugin = this;

                this._videoData = {};
                this._trackData = [];

                this._enabled = true;
            }

            checkEnabled(cb) {
                paella.data.read('cameraTrack',{id:paella.initDelegate.getId()},(data) => {
                    if (data) {
                        this._videoData.width = data.width;
                        this._videoData.height = data.height;
                        this._videoData.originalWidth = data.originalWidth;
                        this._videoData.originalHeight = data.originalHeight;
                        this._trackData = data.positions;
                        this._enabled = true;
                    }
                    else {
                        this._enabled = false;
                    }
                    cb(this._enabled);
                });
            }

            get enabled() { return this._enabled; }

            set enabled(e) {
                this._enabled = e;
                if (this._enabled) {
                  let thisClass = this;
                  paella.player.videoContainer.currentTime().then(function(time) {
                    thisClass.updateZoom(time);
                  });
                }
            }

            getName() { return "es.upv.paella.track4kPlugin"; }
            getEvents() {
                return [ paella.events.timeupdate, paella.events.play, paella.events.seekToTime ];
            }
            onEvent(eventType,data) {
                if (!this._trackData.length) return;
                if (eventType===paella.events.play) {
                }
                else if (eventType===paella.events.timeupdate) {
                    this.updateZoom(data.currentTime);
                }
                else if (eventType===paella.events.seekToTime) {
                    this.seekTo(data.newPosition);
                }
            }

            updateZoom(currentTime) {
              if (this._enabled) {
                let data = curFrame.apply(this,[currentTime]);
                let nextFrameData = nextFrame.apply(this,[currentTime]);
                if (data && this._lastPosition!==data) {
                    this._lastPosition = data;
                    updatePosition.apply(this,[data,nextFrameData]);
                }
              }
            }

            seekTo(time) {
                let data = prevFrame.apply(this,[time]);
                if (data && this._enabled) {
                    this._lastPosition = data;
                    updatePosition.apply(this,[data]);
                }
            }

        };
    });

    paella.addPlugin(function() {

        return class VideoZoomTrack4KPlugin extends paella.ButtonPlugin {
            getAlignment() { return 'right'; }
            getSubclass() { return "videoZoomToolbar"; }
            getIconClass() { return 'icon-screen'; }
            closeOnMouseOut() { return true; }
            getIndex() { return 2030; }
            getName() { return "es.upv.paella.videoZoomTrack4kPlugin"; }
            getDefaultToolTip() { return paella.utils.dictionary.translate("Set video zoom"); }
            getButtonType() { return paella.ButtonPlugin.type.popUpButton; }

            checkEnabled(onSuccess) {
                let players = paella.player.videoContainer.streamProvider.videoPlayers;
                let pluginData = paella.player.config.plugins.list[this.getName()];
                let playerIndex = pluginData.targetStreamIndex;
                let autoByDefault = pluginData.autoModeByDefault;
                this.targetPlayer = players.length>playerIndex ? players[playerIndex] : null;
                g_track4kPlugin.enabled = autoByDefault;
                onSuccess(paella.player.config.player.videoZoom.enabled &&
                            this.targetPlayer &&
                            this.targetPlayer.allowZoom());
            }

            setup() {
                if (this.config.autoModeByDefault) {
                    this.zoomAuto()
                }
                else {
                    this.resetZoom()
                }
            }

            buildContent(domElement) {
                this.changeIconClass("icon-mini-zoom-in");
                g_track4kPlugin.updateTrackingStatus = () => {
                    if (g_track4kPlugin.enabled) {
                      $('.zoom-auto').addClass("autoTrackingActivated");
                      $('.icon-mini-zoom-in').addClass("autoTrackingActivated");
                    } else {
                      $('.zoom-auto').removeClass("autoTrackingActivated");
                      $('.icon-mini-zoom-in').removeClass("autoTrackingActivated");
                    }
                };
                paella.events.bind(paella.events.videoZoomChanged, (evt,target) => {
                    g_track4kPlugin.updateTrackingStatus;
                });
                g_track4kPlugin.updateTrackingStatus;

                function getZoomButton(className,onClick,content) {
                    let btn = document.createElement('div');
                    btn.className = `videoZoomToolbarItem ${ className }`;
                    if (content) {
                        btn.innerText = content;
                    }
                    else {
                        btn.innerHTML = `<i class="glyphicon glyphicon-${ className }"></i>`;
                    }
                    $(btn).click(onClick);
                    return btn;
                }
                domElement.appendChild(getZoomButton('zoom-in',(evt) => {
                    this.zoomIn();
                }));
                domElement.appendChild(getZoomButton('zoom-out',(evt) => {
                    this.zoomOut();
                }));
                domElement.appendChild(getZoomButton('picture',(evt) => {
                    this.resetZoom();
                }));
                domElement.appendChild(getZoomButton('zoom-auto',(evt) => {
                    this.zoomAuto();
                    paella.player.controls.hidePopUp(this.getName());
                }, "auto"));
            }

            zoomIn() {
              g_track4kPlugin.enabled = false;
              this.targetPlayer.zoomIn();
            }

            zoomOut() {
              g_track4kPlugin.enabled = false;
              this.targetPlayer.zoomOut();
            }

            resetZoom() {
              g_track4kPlugin.enabled = false;
              this.targetPlayer.setZoom(100,0,0,500);
              if (g_track4kPlugin.updateTrackingStatus) g_track4kPlugin.updateTrackingStatus();
            }

            zoomAuto() {
              g_track4kPlugin.enabled = ! g_track4kPlugin.enabled;
              if (g_track4kPlugin.updateTrackingStatus) g_track4kPlugin.updateTrackingStatus();
            }
        };
    });
})();




(() => {

paella.plugins.translectures = {};
/*
Class ("paella.captions.translectures.Caption", paella.captions.Caption, {
	initialize: function(id, format, url, lang, editURL, next) {
		this.parent(id, format, url, lang, next);
		this._captionsProvider = "translecturesCaptionsProvider";
		this._editURL = editURL;
	},
	
	canEdit: function(next) {
		// next(err, canEdit)
		next(false, ((this._editURL != undefined)&&(this._editURL != "")));
	},
	
	goToEdit: function() {		
		var self = this;
		paella.player.auth.userData().then(function(userData){
			if (userData.isAnonymous == true) {
				self.askForAnonymousOrLoginEdit();
			}
			else {
				self.doEdit();
			}		
		});	
	},
		
	doEdit: function() {
		window.location.href = this._editURL;		
	},
	doLoginAndEdit: function() {
		paella.player.auth.login(this._editURL);
	},
	
	askForAnonymousOrLoginEdit: function() {
		var self = this;

		var messageBoxElem = document.createElement('div');
		messageBoxElem.className = "translecturesCaptionsMessageBox";

		var messageBoxTitle = document.createElement('div');
		messageBoxTitle.className = "title";
		messageBoxTitle.innerText = paella.utils.dictionary.translate("You are trying to modify the transcriptions, but you are not Logged in!");		
		messageBoxElem.appendChild(messageBoxTitle);

		var messageBoxAuthContainer = document.createElement('div');
		messageBoxAuthContainer.className = "authMethodsContainer";
		messageBoxElem.appendChild(messageBoxAuthContainer);

		// Anonymous edit
		var messageBoxAuth = document.createElement('div');
		messageBoxAuth.className = "authMethod";
		messageBoxAuthContainer.appendChild(messageBoxAuth);

		var messageBoxAuthLink = document.createElement('a');
		messageBoxAuthLink.href = "#";
		messageBoxAuthLink.style.color = "#004488";
		messageBoxAuth.appendChild(messageBoxAuthLink);

		var messageBoxAuthLinkImg = document.createElement('img');
		messageBoxAuthLinkImg.src = "resources/style/caption_mlangs_anonymous.png";
		messageBoxAuthLinkImg.alt = "Anonymous";
		messageBoxAuthLinkImg.style.height = "100px";
		messageBoxAuthLink.appendChild(messageBoxAuthLinkImg);

		var messageBoxAuthLinkText = document.createElement('p');
		messageBoxAuthLinkText.innerText = paella.utils.dictionary.translate("Continue editing the transcriptions anonymously");
		messageBoxAuthLink.appendChild(messageBoxAuthLinkText);

		$(messageBoxAuthLink).click(function() {
			self.doEdit();
		});

		// Auth edit
		messageBoxAuth = document.createElement('div');
		messageBoxAuth.className = "authMethod";
		messageBoxAuthContainer.appendChild(messageBoxAuth);

		messageBoxAuthLink = document.createElement('a');
		messageBoxAuthLink.href = "#";
		messageBoxAuthLink.style.color = "#004488";
		messageBoxAuth.appendChild(messageBoxAuthLink);

		messageBoxAuthLinkImg = document.createElement('img');
		messageBoxAuthLinkImg.src = "resources/style/caption_mlangs_lock.png";
		messageBoxAuthLinkImg.alt = "Login";
		messageBoxAuthLinkImg.style.height = "100px";
		messageBoxAuthLink.appendChild(messageBoxAuthLinkImg);

		messageBoxAuthLinkText = document.createElement('p');
		messageBoxAuthLinkText.innerText = paella.utils.dictionary.translate("Log in and edit the transcriptions");
		messageBoxAuthLink.appendChild(messageBoxAuthLinkText);


		$(messageBoxAuthLink).click(function() {
			self.doLoginAndEdit();
		});

		// Show UI		
		paella.messageBox.showElement(messageBoxElem);
	}
});

paella.captions.translectures.Caption = Caption;

Class ("paella.plugins.translectures.CaptionsPlugIn", paella.EventDrivenPlugin, {
		
	getName:function() { return "es.upv.paella.translecture.captionsPlugin"; },
	getEvents:function() { return []; },
	onEvent:function(eventType,params) {},

	checkEnabled: function(onSuccess) {
		var self = this;
		var video_id = paella.player.videoIdentifier;
				
		if ((this.config.tLServer == undefined) || (this.config.tLdb == undefined)){
			paella.log.warning(this.getName() + " plugin not configured!");
			onSuccess(false);
		}
		else {
			var langs_url = (this.config.tLServer + "/langs?db=${tLdb}&id=${videoId}").replace(/\$\{videoId\}/ig, video_id).replace(/\$\{tLdb\}/ig, this.config.tLdb);
			paella.utils.ajax.get({url: langs_url},
				function(data, contentType, returnCode, dataRaw) {					
					if (data.scode == 0) {
						data.langs.forEach(function(l){
							var l_get_url = (self.config.tLServer + "/dfxp?format=1&pol=0&db=${tLdb}&id=${videoId}&lang=${tl.lang.code}")
								.replace(/\$\{videoId\}/ig, video_id)
								.replace(/\$\{tLdb\}/ig, self.config.tLdb)
								.replace(/\$\{tl.lang.code\}/ig, l.code);
														
							var l_edit_url;							
							if (self.config.tLEdit) {
								l_edit_url = self.config.tLEdit
									.replace(/\$\{videoId\}/ig, video_id)
									.replace(/\$\{tLdb\}/ig, self.config.tLdb)
									.replace(/\$\{tl.lang.code\}/ig, l.code);
							}
							
							var l_txt = l.value;
				            switch(l.type){
						    	case 0:
						    		l_txt += " (" + paella.utils.dictionary.translate("Auto") + ")";
						    		break;
						    	case 1:
						    		l_txt += " (" + paella.utils.dictionary.translate("Under review") + ")";
						    		break;
						    }
														
							var c = new paella.captions.translectures.Caption(l.code , "dfxp", l_get_url, {code: l.code, txt: l_txt}, l_edit_url);
							paella.captions.addCaptions(c);
						});
						onSuccess(false);
					}
					else {
						paella.log.debug("Error getting available captions from translectures: " + langs_url);
						onSuccess(false);
					}
				},						
				function(data, contentType, returnCode) {
					paella.log.debug("Error getting available captions from translectures: " + langs_url);
					onSuccess(false);
				}
			);			
		}
	}	
});

//new paella.plugins.translectures.CaptionsPlugIn();
*/
})();
paella.addPlugin(function() {
	return class ElasticsearchSaverPlugin extends paella.userTracking.SaverPlugIn {
		getName() { return "es.upv.paella.usertracking.elasticsearchSaverPlugin"; }
		
		checkEnabled(onSuccess) {
			this._url = this.config.url;
			this._index = this.config.index || "paellaplayer";
			this._type = this.config.type || "usertracking";
			
			var enabled = true;
			if (this._url == undefined){
				enabled = false;
				paella.log.debug("No ElasticSearch URL found in config file. Disabling ElasticSearch PlugIn");
			}
			
			onSuccess(enabled);
		}
		
		log(event, params) {	
			var p = params;
			if (typeof(p) != "object") {
				p = {value: p};
			}
			
			let currentTime = 0;
			paella.player.videoContainer.currentTime()
				.then((t) => {
					currentTime = t;
					return paella.player.videoContainer.paused();
				})
				.then((paused) => {
					var log = {
						date: new Date(),
						video: paella.initDelegate.getId(),
						playing: !paused,
						time: parseInt(currentTime + paella.player.videoContainer.trimStart()),
						event: event,
						params: p
					};		
					
					paella.utils.ajax.post({url:this._url+ "/"+ this._index + "/" + this._type + "/", params:JSON.stringify(log) });
				});
		}
	}
});


paella.addPlugin(function() {
	return class GoogleAnalyticsTracking extends paella.userTracking.SaverPlugIn {
		getName() { return "es.upv.paella.usertracking.GoogleAnalyticsSaverPlugin"; }
			
		checkEnabled(onSuccess) {
			var trackingID = this.config.trackingID;
			var domain = this.config.domain || "auto";
			if (trackingID){
				paella.log.debug("Google Analitycs Enabled");
				/* jshint ignore:start */
					(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
					(i[r].q=i[r].q||[]).push(arguments);},i[r].l=1*new Date();a=s.createElement(o),
					m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
					})(window,document,'script','//www.google-analytics.com/analytics.js','__gaTracker');
				/* jshint ignore:end */
				__gaTracker('create', trackingID, domain);
				__gaTracker('send', 'pageview');
				onSuccess(true);
			}		
			else {
				paella.log.debug("No Google Tracking ID found in config file. Disabling Google Analitycs PlugIn");
				onSuccess(false);
			}				
		}
	
	
		log(event, params) {
			if ((this.config.category === undefined) || (this.config.category ===true)) {
				var category = this.config.category || "PaellaPlayer";
				var action = event;
				var label =  "";
				
				try {
					label = JSON.stringify(params);
				}
				catch(e) {}
				
				__gaTracker('send', 'event', category, action, label);
			}
		}
		
	}
});








var _paq = _paq || [];

paella.addPlugin(function() {
	return class PiwikAnalyticsTracking extends paella.userTracking.SaverPlugIn {

		getName() { return "es.upv.paella.usertracking.piwikSaverPlugIn"; }
		
		checkEnabled(onSuccess) {
			if (this.config.tracker && this.config.siteId) {
				_paq.push(['trackPageView']);
				_paq.push(['enableLinkTracking']);
				(function() {
					var u=this.config.tracker;
					_paq.push(['setTrackerUrl', u+'/piwik.php']);
					_paq.push(['setSiteId', this.config.siteId]);
					var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];
					g.type='text/javascript'; g.async=true; g.defer=true; g.src=u+'piwik.js'; s.parentNode.insertBefore(g,s);
					onSuccess(true);
				})();			
			}
			else {
				onSuccess(false);
			}
		}
		
		log(event, params) {
				var category = this.config.category || "PaellaPlayer";
				var action = event;
				var label =  "";
				
				try {
					label = JSON.stringify(params);
				}
				catch(e) {}
				
				_paq.push(['trackEvent', category, action, label]);
		}
	}
});


(() => {

paella.addCanvasPlugin("video360", true, true, () => {
	return class Video360Canvas extends paella.WebGLCanvas {
		constructor(stream) {
			super(stream);
		}

		loadVideo(videoPlugin,stream) {
			return new Promise((resolve,reject) => {
				let checkLoaded = () => {
					if (this.video) {
						resolve(this);
					}
					else {
						setTimeout(checkLoaded,100);
					}
				}
				checkLoaded();
			});
		}

		buildVideoSurface(sceneRoot,videoTexture) {
			let sphere = bg.scene.PrimitiveFactory.Sphere(this.gl,1,50);
			let sphereNode = new bg.scene.Node(this.gl);
			sphereNode.addComponent(sphere);
			sphere.getMaterial(0).texture = videoTexture;
			sphere.getMaterial(0).lightEmission = 0;
			sphere.getMaterial(0).lightEmissionMaskInvert = false;
			sphere.getMaterial(0).cullFace = false;
			sphereNode.addComponent(new bg.scene.Transform(bg.Matrix4.Scale(1,-1,1)));
			sceneRoot.addChild(sphereNode);
		}

		buildCamera() {
			let cameraNode = new bg.scene.Node(this.gl,"Camera");
			let camera = new bg.scene.Camera()
			cameraNode.addComponent(camera);
			cameraNode.addComponent(new bg.scene.Transform());
			let projection = new bg.scene.OpticalProjectionStrategy();
			projection.far = 100;
			projection.focalLength = 55;
			camera.projectionStrategy = projection;

			let oc = new bg.manipulation.OrbitCameraController();
			oc.maxPitch = 90;
			oc.minPitch = -90;
			oc.maxDistance = 0;
			oc.minDistance = 0;
			this._cameraController = oc;
			cameraNode.addComponent(oc);

			return cameraNode;
		}
	}
});

})();

(() => {

	function cyln2world(a, e) {
		return (new bg.Vector3(
			Math.cos(e) * Math.cos(a),
			Math.cos(e) * Math.sin(a),
			Math.sin(e)));
	}

	function world2fish(x, y, z) {
		let nz = z;
		if (z < -1.0) nz = -1.0;
		else if (z > 1.0) nz = 1.0;
		return (new bg.Vector2(
			Math.atan2(y, x),
			Math.acos(nz) / Math.PI)); // 0.0 to 1.0
	}

	function calcTexUv(i, j, lens) {
		const world = cyln2world(
			((i + 90) / 180.0 - 1.0) * Math.PI, // rotate 90 deg for polygon
			(0.5 - j / 180.0) * Math.PI);
		const ar = world2fish(
			Math.sin(-0.5 * Math.PI) * world.z + Math.cos(-0.5 * Math.PI) * world.x,
			world.y,
			Math.cos(-0.5 * Math.PI) * world.z - Math.sin(-0.5 * Math.PI) * world.x);

		const fishRad = 0.883;
		const fishRad2 = fishRad * 0.88888888888888;
		const fishCenter = 1.0 - 0.44444444444444;
		const x = (lens === 0) ?
			fishRad * ar.y * Math.cos(ar.x) * 0.5 + 0.25 :
			fishRad * (1.0 - ar.y) * Math.cos(-1.0 * ar.x + Math.PI) * 0.5 + 0.75;
		const y = (lens === 0) ?
			fishRad2 * ar.y * Math.sin(ar.x) + fishCenter :
			fishRad2 * (1.0 - ar.y) * Math.sin(-1.0 * ar.x + Math.PI) + fishCenter;
		return (new bg.Vector2(x, y));
	}

	function buildViewerNode(ctx) {
		let radius = 1;
		let node = new bg.scene.Node(ctx);
		let drw = new bg.scene.Drawable();
		node.addComponent(drw);

		let plist = new bg.base.PolyList(ctx);

		let vertex = [];
		let normals = [];
		let uvs = [];
		let index = [];
		for (let j = 0; j <= 180; j += 5) {
			for (let i = 0; i <= 360; i += 5) {
				vertex.push(new bg.Vector3(Math.sin(Math.PI * j / 180.0) * Math.sin(Math.PI * i / 180.0) * radius,
										   Math.cos(Math.PI * j / 180.0) * radius,
										   Math.sin(Math.PI * j / 180.0) * Math.cos(Math.PI * i / 180.0) * radius));

				normals.push(new bg.Vector3(0, 0, -1));
			}
			/* devide texture */
			for (let k = 0; k <= 180; k += 5) {
				uvs.push(calcTexUv(k, j, 0));
			}
			for (let l = 180; l <= 360; l += 5) {
				uvs.push(calcTexUv(l, j, 1));
			}
		}

		function addFace(v0, v1, v2, n0, n1, n2, uv0, uv1, uv2) {
			plist.vertex.push(v0.x); plist.vertex.push(v0.y); plist.vertex.push(v0.z);
			plist.vertex.push(v1.x); plist.vertex.push(v1.y); plist.vertex.push(v1.z);
			plist.vertex.push(v2.x); plist.vertex.push(v2.y); plist.vertex.push(v2.z);

			plist.normal.push(n0.x); plist.normal.push(n0.y); plist.normal.push(n0.z);
			plist.normal.push(n1.x); plist.normal.push(n1.y); plist.normal.push(n1.z);
			plist.normal.push(n2.x); plist.normal.push(n2.z); plist.normal.push(n2.z);

			plist.texCoord0.push(uv0.x); plist.texCoord0.push(1 - uv0.y);
			plist.texCoord0.push(uv1.x); plist.texCoord0.push(1 - uv1.y);
			plist.texCoord0.push(uv2.x); plist.texCoord0.push(1 - uv2.y);

			plist.index.push(plist.index.length);
			plist.index.push(plist.index.length);
			plist.index.push(plist.index.length);
		}

		for (let m = 0; m < 36; m++) {
			for (let n = 0; n < 72; n++) {
				const v = m * 73 + n;
				const t = (n < 36) ? m * 74 + n : m * 74 + n + 1;
				([uvs[t + 0], uvs[t + 1], uvs[t + 74]], [uvs[t + 1], uvs[t + 75], uvs[t + 74]]);

				let v0 = vertex[v + 0];	 let n0 = normals[v + 0];	let uv0 = uvs[t + 0];
				let v1 = vertex[v + 1];  let n1 = normals[v + 1];	let uv1 = uvs[t + 1];
				let v2 = vertex[v + 73]; let n2 = normals[v + 73];	let uv2 = uvs[t + 74];
				let v3 = vertex[v + 74]; let n3 = normals[v + 74];	let uv3 = uvs[t + 75];

				addFace(v0, v1, v2, n0, n1, n2, uv0, uv1, uv2);
				addFace(v1, v3, v2, n1, n3, n2, uv1, uv3, uv2);
			}
		}
		plist.build();

		drw.addPolyList(plist);

		let trx = bg.Matrix4.Scale(-1,1,1);
		node.addComponent(new bg.scene.Transform(trx));

		return node;
	}

	paella.addCanvasPlugin("video360Theta", true, true, () => {
		return class Video360ThetaCanvas extends paella.WebGLCanvas {
			constructor(stream) {
				super(stream);
			}
	
			loadVideo(videoPlugin,stream) {
				return new Promise((resolve,reject) => {
					let checkLoaded = () => {
						if (this.video) {
							resolve(this);
						}
						else {
							setTimeout(checkLoaded,100);
						}
					}
					checkLoaded();
				});
			}
	
			buildVideoSurface(sceneRoot,videoTexture) {
				let sphereNode = buildViewerNode(this.gl);
				let sphere = sphereNode.drawable;
				sphere.getMaterial(0).texture = videoTexture;
				sphere.getMaterial(0).lightEmission = 0;
				sphere.getMaterial(0).lightEmissionMaskInvert = false;
				sphere.getMaterial(0).cullFace = false;
				sceneRoot.addChild(sphereNode);
			}
	
			buildCamera() {
				let cameraNode = new bg.scene.Node(this.gl,"Camera");
				let camera = new bg.scene.Camera()
				cameraNode.addComponent(camera);
				cameraNode.addComponent(new bg.scene.Transform());
				let projection = new bg.scene.OpticalProjectionStrategy();
				projection.far = 100;
				projection.focalLength = 55;
				camera.projectionStrategy = projection;
	
				let oc = new bg.manipulation.OrbitCameraController();
				oc.maxPitch = 90;
				oc.minPitch = -90;
				oc.maxDistance = 0;
				oc.minDistance = 0;
				this._cameraController = oc;
				cameraNode.addComponent(oc);
	
				return cameraNode;
			}
		}
	});

})();

paella.addDataDelegate('metadata', () => {
    return class VideoManifestMetadataDataDelegate extends paella.DataDelegate {
        read(context, params, onSuccess) {
            let metadata = paella.player.videoLoader.getMetadata();
            onSuccess(metadata[params], true);
        }

        write(context, params, value, onSuccess) {
            onSuccess({}, true);
        }

        remove(context, params, onSuccess) {
            onSuccess({}, true);
        }
    }
});

paella.addPlugin(function() {

    return class VideoDataPlugin extends paella.VideoOverlayButtonPlugin {
        
        getIndex() { return 10; }

        getSubclass() {
            return "videoData";
        }

        getAlignment() {
            return 'left';
        }

        getDefaultToolTip() { return ""; }

        checkEnabled(onSuccess) {
            // Check if enabled
            let plugin = paella.player.config.plugins.list["es.upv.paella.videoDataPlugin"];
            let exclude = (plugin && plugin.excludeLocations) || [];
            let excludeParent = (plugin && plugin.excludeParentLocations) || [];
            let excluded = exclude.some((url) => {
                let re = RegExp(url,"i");
                return re.test(location.href);
            });

            if (window!=window.parent) {
                excluded = excluded || excludeParent.some((url) => {
                    let re = RegExp(url,"i");
                    try {
                        return re.test(parent.location.href);
                    }
                    catch(e) {
                        // Cross domain error
                        return false;
                    }
                });
            }
            onSuccess(!excluded);
        }

        setup() {
            let title = document.createElement("h1");
            title.innerText = "";
            title.className = "videoTitle";
            this.button.appendChild(title);

            paella.data.read("metadata","title",function(data) {
                title.innerText = data;
            });
        }

        action(button) {
        }

        getName() {
            return "es.upv.paella.videoDataPlugin";
        }
    }
});
paella.addPlugin(function() {
    let g_canvasWidth = 320;
    let g_canvasHeight = 180;

    function getThumbnailContainer(videoIndex) {
        let container = document.createElement('canvas');
        container.width = g_canvasWidth;
        container.height = g_canvasHeight;
        container.className = "zoom-thumbnail";
        container.id = "zoomContainer" + videoIndex;
        return container;
    }

    function getZoomRect() {
        let zoomRect = document.createElement('div');
        zoomRect.className = "zoom-rect";

        return zoomRect;
    }

    function updateThumbnail(thumbElem) {
        let player = thumbElem.player;
        let canvas = thumbElem.canvas;
        player.captureFrame()
            .then((frameData) => {
                let ctx = canvas.getContext("2d");
                ctx.drawImage(frameData.source,0,0,g_canvasWidth,g_canvasHeight);
            });
    }

    function setupButtons(videoPlayer) {
        let wrapper = videoPlayer.parent;
        let wrapperDom = wrapper.domElement;

        let zoomButton = document.createElement('div');
        wrapperDom.appendChild(zoomButton);
        zoomButton.className = "videoZoomButton btn zoomIn";
        zoomButton.innerHTML = '<i class="glyphicon glyphicon-zoom-in"></i>'
        $(zoomButton).on('mousedown',(e) => {
            e.preventDefault();
            paella.player.videoContainer.disablePlayOnClick();
            videoPlayer.zoomIn();
        });
        $(zoomButton).on('mouseup',(e) => {
            e.preventDefault();
            paella.player.videoContainer.enablePlayOnClick(1000);
        });

        zoomButton = document.createElement('div');
        wrapperDom.appendChild(zoomButton);
        zoomButton.className = "videoZoomButton btn zoomOut";
        zoomButton.innerHTML = '<i class="glyphicon glyphicon-zoom-out"></i>'
        $(zoomButton).on('mousedown',(e) => {
            e.preventDefault();
            paella.player.videoContainer.disablePlayOnClick();
            videoPlayer.zoomOut();
        });
        $(zoomButton).on('mouseup',(e) => {
            e.preventDefault();
            paella.player.videoContainer.enablePlayOnClick(1000);
        });

    }

    return class VideoZoomPlugin extends paella.VideoOverlayButtonPlugin {
        getIndex() {return 10; }
        getSubclass() { return "videoZoom"; }
        getAlignment() { return 'right'; }
        getDefaultToolTip() { return ""; }

        checkEnabled(onSuccess) {
            onSuccess(true);
        }

        setup() {
            var thisClass = this;
            this._thumbnails = [];
            this._visible = false;
            this._available = false;
            function checkVisibility() {
                let buttons = $('.videoZoomButton');
                let thumbs = $('.videoZoom');
                let showButtons = this.config.showButtons!==undefined ? this.config.showButtons : true;
                if (this._visible && this._available) {
                    showButtons ? buttons.show() : buttons.hide();
                    thumbs.show();
                }
                else {
                    buttons.hide();
                    thumbs.hide();
                }
            }

            let players = paella.player.videoContainer.streamProvider.videoPlayers;
            players.forEach((player,index) => {
                if (player.allowZoom()) {
                    this._available = player.zoomAvailable();
                    this._visible = this._available;
                    setupButtons.apply(this,[player]);
                    player.supportsCaptureFrame().then((supports) => {
                        if (supports) {
                            let thumbContainer = document.createElement('div');
                            thumbContainer.className = "zoom-container"
                            let thumb = getThumbnailContainer.apply(this,[index]);
                            let zoomRect = getZoomRect.apply(this);
                            this.button.appendChild(thumbContainer);
                            thumbContainer.appendChild(thumb);
                            thumbContainer.appendChild(zoomRect);
                            $(thumbContainer).hide();
                            this._thumbnails.push({
                                player:player,
                                thumbContainer:thumbContainer,
                                zoomRect:zoomRect,
                                canvas:thumb
                            });
                            checkVisibility.apply(this);
                        }
                    })
                }
            });
            
            let update = false;
            paella.events.bind(paella.events.play,(evt) => {
                let updateThumbs = () => {
                    this._thumbnails.forEach((item) => {
                        updateThumbnail(item);
                    });
                    if (update) {
                        setTimeout(() => {
                            updateThumbs();
                        }, 2000);
                    }
                }
                update = true;
                updateThumbs();
            });

            paella.events.bind(paella.events.pause,(evt) => {
                update = false;
            });

            paella.events.bind(paella.events.videoZoomChanged, (evt,target) => {
                this._thumbnails.some((thumb) => {
                    if (thumb.player==target.video) {
                        if (thumb.player.zoom>100) {
                            $(thumb.thumbContainer).show();
                            let x = target.video.zoomOffset.x * 100 / target.video.zoom;
                            let y = target.video.zoomOffset.y * 100 / target.video.zoom;
                            
                            let zoomRect = thumb.zoomRect;
                            $(zoomRect).css({
                                left: x + '%',
                                top: y + '%',
                                width: (10000 / target.video.zoom) + '%',
                                height: (10000 / target.video.zoom) + '%'
                            });
                        }
                        else {
                            $(thumb.thumbContainer).hide();
                        }
                        return true;
                    }
                })
            });

            paella.events.bind(paella.events.zoomAvailabilityChanged, (evt,target) => {
                this._available = target.available;
                this._visible = target.available;
                checkVisibility.apply(this);
            });

            paella.events.bind(paella.events.controlBarDidHide, () => {
                this._visible = false;
                checkVisibility.apply(this);
            });

            paella.events.bind(paella.events.controlBarDidShow, () => {
                this._visible = true;
                checkVisibility.apply(this);
            });
        }

        action(button) {
            //paella.messageBox.showMessage(paella.utils.dictionary.translate("Live streaming mode: This is a live video, so, some capabilities of the player are disabled"));
        }

        getName() {
            return "es.upv.paella.videoZoomPlugin";
        }
    }
});

paella.addPlugin(function() {
    
    return class VideoZoomToolbarPlugin extends paella.ButtonPlugin {
        getAlignment() { return 'right'; }
        getSubclass() { return "videoZoomToolbar"; }
        getIconClass() { return 'icon-screen'; }
        getIndex() { return 2030; }
        getName() { return "es.upv.paella.videoZoomToolbarPlugin"; }
        getDefaultToolTip() { return paella.utils.dictionary.translate("Set video zoom"); }
        getButtonType() { return paella.ButtonPlugin.type.popUpButton; }

        checkEnabled(onSuccess) {
            let players = paella.player.videoContainer.streamProvider.videoPlayers;
            let pluginData = paella.player.config.plugins.list["es.upv.paella.videoZoomToolbarPlugin"];
            let playerIndex = pluginData.targetStreamIndex;
            this.targetPlayer = players.length>playerIndex ? players[playerIndex] : null;
            onSuccess(paella.player.config.player.videoZoom.enabled &&
                        this.targetPlayer &&
                        this.targetPlayer.allowZoom());
    
        }
        
        buildContent(domElement) {
            paella.events.bind(paella.events.videoZoomChanged, (evt,target) => {
                this.setText(Math.round(target.video.zoom) + "%");
            });

            this.setText("100%");

            function getZoomButton(className,onClick) {
                let btn = document.createElement('div');
                btn.className = `videoZoomToolbarItem ${ className }`;
                btn.innerHTML = `<i class="glyphicon glyphicon-${ className }"></i>`;
                $(btn).click(onClick);
                return btn;
            }
            domElement.appendChild(getZoomButton('zoom-in',(evt) => {
                this.targetPlayer.zoomIn();
            }));
            domElement.appendChild(getZoomButton('zoom-out',(evt) => {
                this.targetPlayer.zoomOut();
            }));
        }
    }
});

paella.addPlugin(function() {
	return class ViewModePlugin extends paella.ButtonPlugin {
		
		getAlignment() { return 'right'; }
		getSubclass() { return "showViewModeButton"; }
		getIconClass() { return 'icon-presentation-mode'; }
		getIndex() { return 540; }
		getName() { return "es.upv.paella.viewModePlugin"; }
		//getButtonType() { return paella.ButtonPlugin.type.popUpButton; }
		getButtonType() { return paella.ButtonPlugin.type.menuButton; }
		getDefaultToolTip() { return paella.utils.dictionary.translate("Change video layout"); }		
		checkEnabled(onSuccess) {
			this.buttonItems =null;
			this.buttons = [];
			this.selected_button = null;
			this.active_profiles = null;
			this.active_profiles = this.config.activeProfiles;
			onSuccess(!paella.player.videoContainer.isMonostream);
		}
	
		closeOnMouseOut() { return true; }
	
		setup() {
			var thisClass = this;
	
			var Keys = {Tab:9,Return:13,Esc:27,End:35,Home:36,Left:37,Up:38,Right:39,Down:40};
	
			paella.events.bind(paella.events.setProfile,function(event,params) {
				thisClass.onProfileChange(params.profileName);
			});
		
			$(this.button).keyup(function(event) {
				if (thisClass.isPopUpOpen()){
					if (event.keyCode == Keys.Up) {
					if(thisClass.selected_button>0){
							if(thisClass.selected_button<thisClass.buttons.length)
								thisClass.buttons[thisClass.selected_button].className = 'viewModeItemButton '+thisClass.buttons[thisClass.selected_button].data.profile;
	
							thisClass.selected_button--;
							thisClass.buttons[thisClass.selected_button].className = thisClass.buttons[thisClass.selected_button].className+' selected';
						}
					}
					else if (event.keyCode == Keys.Down) {
						if( thisClass.selected_button < thisClass.buttons.length-1){
							if(thisClass.selected_button>=0)
								thisClass.buttons[thisClass.selected_button].className = 'viewModeItemButton '+thisClass.buttons[thisClass.selected_button].data.profile;
	
							thisClass.selected_button++;
							thisClass.buttons[thisClass.selected_button].className = thisClass.buttons[thisClass.selected_button].className+' selected';
						}
					}
					else if (event.keyCode == Keys.Return) {
						thisClass.onItemClick(thisClass.buttons[thisClass.selected_button],thisClass.buttons[thisClass.selected_button].data.profile,thisClass.buttons[thisClass.selected_button].data.profile);
					}
				}
			});
		}

		getMenuContent() {
			let buttonItems = [];
			paella.profiles.profileList.forEach((profileData,index) => {
				if (profileData.hidden) return;
				if (this.active_profiles) {
					let active = false;
					this.active_profiles.some((ap) => {
						if (ap == profile) {
							active = ap;
							return true;
						}
					});
					if (!active) {
						return;
					}
				}

	
				let current = paella.profiles.currentProfileName;

				let url = this.getButtonItemIcon(profileData);
				url = url.replace(/\\/ig,'/');
				buttonItems.push({
					id: profileData.id + "_button",
					title: "",
					value: profileData.id,
					icon: url,
					className: this.getButtonItemClass(profileData.id),
					default: current == profileData.id
				})
			})
			return buttonItems;
		}

		menuItemSelected(itemData) {
			paella.player.setProfile(itemData.value);
			paella.player.controls.hidePopUp(this.getName());
		}

		onProfileChange(profileName) {
			this.rebuildMenu();
		}
	
		getButtonItemClass(profileName) {
			return 'viewModeItemButton ' + profileName;
		}

		getButtonItemIcon(profileData) {
			return `${ paella.baseUrl }resources/style/${ profileData.icon }`;
		}
	}
});


paella.addPlugin(function() {
	return class VolumeRangePlugin extends paella.ButtonPlugin {
		getAlignment() { return 'left'; }
		getSubclass() { return 'volumeRangeButton'; }
		getIconClass() { return 'icon-volume-high'; }
		getName() { return "es.upv.paella.volumeRangePlugin"; }
		getDefaultToolTip() { return paella.utils.dictionary.translate("Volume"); }
		getIndex() {return 9999;}

		checkEnabled(onSuccess) {
			this._tempMasterVolume = 0;
			this._inputMaster = null;
			this._control_NotMyselfEvent = true;
			this._storedValue = false;
			var enabled = !paella.utils.userAgent.browser.IsMobileVersion;
			onSuccess(enabled);
		}

		setup() {
			var self = this;
			//STORE VALUES
			paella.events.bind(paella.events.videoUnloaded,function(event,params) {self.storeVolume();});
			//RECOVER VALUES
			paella.events.bind(paella.events.singleVideoReady,function(event,params) {self.loadStoredVolume(params);});

			paella.events.bind(paella.events.setVolume, function(evt,par){ self.updateVolumeOnEvent(par);});
		}

		updateVolumeOnEvent(volume){
			var thisClass = this;

			if(thisClass._control_NotMyselfEvent){
				thisClass._inputMaster = volume.master;
			}
			else {thisClass._control_NotMyselfEvent = true;}
		}

		storeVolume(){
			var This = this;
			paella.player.videoContainer.streamProvider.mainAudioPlayer.volume()
				.then(function(v) {
					This._tempMasterVolume = v;
					This._storedValue = true;
				});
		}

		loadStoredVolume(params){
			if (this._storedValue == false) {
				this.storeVolume();
			}

			if(this._tempMasterVolume){
				paella.player.videoContainer.setVolume(this._tempMasterVolume);
			}
			this._storedValue = false;
		}

		action(button) {
			if (paella.player.videoContainer.muted) {
				paella.player.videoContainer.unmute();
			}
			else {
				paella.player.videoContainer.mute();
			}
		}

		getExpandableContent() {
			var rangeInput = document.createElement('input');
			this._inputMaster = rangeInput;
			rangeInput.type = "range";
			rangeInput.min = 0;
			rangeInput.max = 1;
			rangeInput.step = 0.01;
			paella.player.videoContainer.audioPlayer.volume()
				.then((vol) => {
					rangeInput.value = vol;
				})

			let updateMasterVolume = () => {
				var masterVolume = $(rangeInput).val();
				var slaveVolume = 0;
				this._control_NotMyselfEvent = false;
				paella.player.videoContainer.setVolume(masterVolume);
			};
			$(rangeInput).bind('input', function (e) { updateMasterVolume(); });
			$(rangeInput).change(function() { updateMasterVolume(); });

			paella.events.bind(paella.events.setVolume, (event,params) => {
				rangeInput.value = params.master;
				this.updateClass();
			});
			this.updateClass();

			return rangeInput;
		}

		updateClass() {
			var selected = '';
			var self = this;
			
			paella.player.videoContainer.volume()
				.then((volume) => {
					if (volume === undefined) { selected = 'icon-volume-mid'; }
					else if (volume == 0) { selected = 'icon-volume-mute'; }
					else if (volume < 0.33) { selected = 'icon-volume-low'; }
					else if (volume < 0.66) { selected = 'icon-volume-mid'; }
					else { selected = 'icon-volume-high'; }
					this.changeIconClass(selected);
				})
		}
	};
});


(() => {

class WebmVideoFactory extends paella.VideoFactory {
	webmCapable() {
		var testEl = document.createElement( "video" );
		if ( testEl.canPlayType ) {
			return "" !== testEl.canPlayType( 'video/webm; codecs="vp8, vorbis"' );
		}
		else {
			return false;
		}
	}

	isStreamCompatible(streamData) {
		try {
			if (!this.webmCapable()) return false;
			for (var key in streamData.sources) {
				if (key=='webm') return true;
			}
		}
		catch (e) {}
		return false;
	}

	getVideoObject(id, streamData, rect) {
		return new paella.Html5Video(id, streamData, rect.x, rect.y, rect.w, rect.h,'webm');
	}
}

paella.videoFactories.WebmVideoFactory = WebmVideoFactory;

})();

paella.addPlugin(function() {
	return class WindowTitlePlugin extends paella.EventDrivenPlugin {
		
		getName() {
			return "es.upv.paella.windowTitlePlugin";
		}
		
		checkEnabled(onSuccess) {
			this._initDone = false;
			paella.player.videoContainer.masterVideo().duration()
				.then((d) => {
					this.loadTitle();
				});
			onSuccess(true);
		}

		loadTitle() {
			var title = paella.player.videoLoader.getMetadata() && paella.player.videoLoader.getMetadata().title;
			document.title = title || document.title;
			this._initDone = true;
		}
	};
});

(() => {
	
class YoutubeVideo extends paella.VideoElementBase {

	constructor(id,stream,left,top,width,height) {
		super(id,stream,'div',left,top,width,height);
		this._posterFrame = null;
		this._currentQuality = null;
		this._autoplay = false;
		this._readyPromise = null;

		this._readyPromise = $.Deferred();
	}

	get video() { return this._youtubePlayer; }

	_deferredAction(action) {
		return new Promise((resolve,reject) => {
			this._readyPromise.then(function() {
					resolve(action());
				},
				function() {
					reject();
				});
		});
	}

	_getQualityObject(index, s) {
		var level = 0;
		switch (s) {
			case 'small':
				level = 1;
				break;
			case 'medium':
				level = 2;
				break;
			case 'large':
				level = 3;
				break;
			case 'hd720':
				level = 4;
				break;
			case 'hd1080':
				level = 5;
				break;
			case 'highres':
				level = 6;
				break;
		}
		return {
			index: index,
			res: { w:null, h:null},
			src: null,
			label:s,
			level:level,
			bitrate:level,
			toString:function() { return this.label; },
			shortLabel:function() { return this.label; },
			compare:function(q2) { return this.level - q2.level; }
		};
	}

	_onStateChanged(e) {
		console.log("On state changed");
	}

	// Initialization functions
	getVideoData() {
		var This = this;
		return new Promise((resolve,reject) => {
			var stream = this._stream.sources.youtube[0];
			this._deferredAction(() => {
				var videoData = {
					duration: This.video.getDuration(),
					currentTime: This.video.getCurrentTime(),
					volume: This.video.getVolume(),
					paused: !This._playing,
					ended: This.video.ended,
					res: {
						w: stream.res.w,
						h: stream.res.h
					}
				};
				resolve(videoData);
			})
		});
	}

	setPosterFrame(url) {
		this._posterFrame = url;
	}

	setAutoplay(auto) {
		this._autoplay = auto;

	}

	setRect(rect,animate) {
		this._rect = JSON.parse(JSON.stringify(rect));
		var relativeSize = new paella.RelativeVideoSize();
		var percentTop = relativeSize.percentVSize(rect.top) + '%';
		var percentLeft = relativeSize.percentWSize(rect.left) + '%';
		var percentWidth = relativeSize.percentWSize(rect.width) + '%';
		var percentHeight = relativeSize.percentVSize(rect.height) + '%';
		var style = {top:percentTop,left:percentLeft,width:percentWidth,height:percentHeight,position:'absolute'};
		if (animate) {
			this.disableClassName();
			var thisClass = this;

			$('#' + this.identifier).animate(style,400,function(){
				thisClass.enableClassName();
				paella.events.trigger(paella.events.setComposition, { video:thisClass });
			});
			this.enableClassNameAfter(400);
		}
		else {
			$('#' + this.identifier).css(style);
			paella.events.trigger(paella.events.setComposition, { video:this });
		}
	}

	setVisible(visible,animate) {
		if (visible=="true" && animate) {
			$('#' + this.identifier).show();
			$('#' + this.identifier).animate({opacity:1.0},300);
		}
		else if (visible=="true" && !animate) {
			$('#' + this.identifier).show();
		}
		else if (visible=="false" && animate) {
			$('#' + this.identifier).animate({opacity:0.0},300);
		}
		else if (visible=="false" && !animate) {
			$('#' + this.identifier).hide();
		}
	}

	setLayer(layer) {
		$('#' + this.identifier).css({ zIndex:layer});
	}

	load() {
		var This = this;
		return new Promise((resolve,reject) => {
			this._qualityListReadyPromise = $.Deferred();
			paella.youtubePlayerVars.apiReadyPromise.
				then(() => {
					var stream = this._stream.sources.youtube[0];

					if (stream) {
						// TODO: poster frame
						this._youtubePlayer = new YT.Player(This.identifier, {
							height: '390',
							width: '640',
							videoId:stream.id,
							playerVars: {
								controls: 0,
								disablekb: 1
							},
							events: {
								onReady: function(e) {
									This._readyPromise.resolve();
								},
								onStateChanged:function(e) {
									console.log("state changed");
								},
								onPlayerStateChange:function(e) {
									console.log("state changed");
								}
							}
						});

						resolve();
					}
					else {
						reject(new Error("Could not load video: invalid quality stream index"));
					}
				});
		});
	}

	getQualities() {
		let This = this;
		return new Promise((resolve,reject) => {
			This._qualityListReadyPromise.then(function(q) {
				var result = [];
				var index = -1;
				This._qualities = {};
				q.forEach((item) => {
					index++;
					This._qualities[item] = This._getQualityObject(index,item);
					result.push(This._qualities[item]);
				});
				resolve(result);
			});
		});
	}

	setQuality(index) {
		return new Promise((resolve,reject) => {
			this._qualityListReadyPromise.then((q) => {
				for (var key in this._qualities) {
					var searchQ = this._qualities[key];
					if (typeof(searchQ)=="object" && searchQ.index==index) {
						this.video.setPlaybackQuality(searchQ.label);
						break;
					}
				}
				resolve();
			});
		});
	}

	getCurrentQuality() {
		return new Promise((resolve,reject) => {
			this._qualityListReadyPromise.then((q) => {
				resolve(this._qualities[this.video.getPlaybackQuality()]);
			});
		});
	}

	play() {
		let This = this;
		return new Promise((resolve,reject) => {
			This._playing = true;
			This.video.playVideo();
			new paella.utils.Timer((timer) => {
				var q = this.video.getAvailableQualityLevels();
				if (q.length) {
					timer.repeat = false;
					this._qualityListReadyPromise.resolve(q);
					resolve();
				}
				else {
					timer.repeat = true;
				}
			},500);
		});
	}

	pause() {
		return this._deferredAction(() => {
			this._playing = false;
			this.video.pauseVideo();
		});
	}

	isPaused() {
		return this._deferredAction(() => {
			return !this._playing;
		});
	}

	duration() {
		return this._deferredAction(() => {
			return this.video.getDuration();
		});
	}

	setCurrentTime(time) {
		return this._deferredAction(() => {
			this.video.seekTo(time);
		});
	}

	currentTime() {
		return this._deferredAction(() => {
			return this.video.getCurrentTime();
		});
	}

	setVolume(volume) {
		return this._deferredAction(() => {
			this.video.setVolume && this.video.setVolume(volume * 100);
		});
	}

	volume() {
		return this._deferredAction(() => {
			return this.video.getVolume() / 100;
		});
	}

	setPlaybackRate(rate) {
		return this._deferredAction(() => {
			this.video.playbackRate = rate;
		});
	}

	playbackRate() {
		return this._deferredAction(() => {
			return this.video.playbackRate;
		});
	}

	goFullScreen() {
		return this._deferredAction(() => {
			var elem = this.video;
			if (elem.requestFullscreen) {
				elem.requestFullscreen();
			}
			else if (elem.msRequestFullscreen) {
				elem.msRequestFullscreen();
			}
			else if (elem.mozRequestFullScreen) {
				elem.mozRequestFullScreen();
			}
			else if (elem.webkitEnterFullscreen) {
				elem.webkitEnterFullscreen();
			}
		});
	}

	unFreeze(){
		return this._deferredAction(() => {
			var c = document.getElementById(this.video.className + "canvas");
			$(c).remove();
		});
	}

	freeze(){
		return this._deferredAction(() => {
			var canvas = document.createElement("canvas");
			canvas.id = this.video.className + "canvas";
			canvas.width = this.video.videoWidth;
			canvas.height = this.video.videoHeight;
			canvas.style.cssText = this.video.style.cssText;
			canvas.style.zIndex = 2;

			var ctx = canvas.getContext("2d");
			ctx.drawImage(this.video, 0, 0, Math.ceil(canvas.width/16)*16, Math.ceil(canvas.height/16)*16);//Draw image
			this.video.parentElement.appendChild(canvas);
		});
	}

	unload() {
		this._callUnloadEvent();
		return paella_DeferredNotImplemented();
	}

	getDimensions() {
		return paella_DeferredNotImplemented();
	}
}

paella.YoutubeVideo = YoutubeVideo;

class YoutubeVideoFactory extends paella.VideoFactory {
	initYoutubeApi() {
		if (!this._initialized) {
			var tag = document.createElement('script');

			tag.src = "https://www.youtube.com/iframe_api";
			var firstScriptTag = document.getElementsByTagName('script')[0];
			firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

			paella.youtubePlayerVars = {
				apiReadyPromise: new $.Deferred()
			};
			this._initialized = true;
		}
	}

	isStreamCompatible(streamData) {
		try {
			for (var key in streamData.sources) {
				if (key=='youtube') return true;
			}
		}
		catch (e) {}
		return false;
	}

	getVideoObject(id, streamData, rect) {
		this.initYoutubeApi();
		return new paella.YoutubeVideo(id, streamData, rect.x, rect.y, rect.w, rect.h);
	}
}

paella.videoFactories.YoutubeVideoFactory = YoutubeVideoFactory;

//paella.youtubePlayerVars = {
//	apiReadyPromise: $.Promise()
//};


})();


function onYouTubeIframeAPIReady() {
	//	console.log("Youtube iframe API ready");
	paella.youtubePlayerVars.apiReadyPromise.resolve();
}


paella.addPlugin(function() {
  return class MatomoTracking extends paella.userTracking.SaverPlugIn {
    getName() { return "org.opencast.usertracking.MatomoSaverPlugIn"; }

    checkEnabled(onSuccess) {

      var site_id = this.config.site_id,
          server = this.config.server,
          heartbeat = this.config.heartbeat,
          privacy_url = this.config.privacy_policy_url,
          tracking_client = this.config.tracking_client_name,
          ask_for_concent = this.config.ask_for_concent,
          cookieconsent_base_color = this.config.cookieconsent_base_color,
          cookieconsent_highlight_color = this.config.cookieconsent_highlight_color,
          thisClass = this,
          trackingPermission,
          translations = [],
          tracked;

          paella.cookieconsent_base_color = this.cookieconsent_base_color;
          paella.cookieconsent_highlight_color = this.cookieconsent_highlight_color;

      function trackMatomo() {
        if (isTrackingPermission() && !tracked && server && site_id){
          if (server.substr(-1) != '/') server += '/';
          paella.require(server + tracking_client + ".js")
            .then((matomo) => {
              paella.log.debug("Matomo Analytics Enabled");
              paella.userTracking.matomotracker = Matomo.getAsyncTracker( server + tracking_client + ".php", site_id );
              paella.userTracking.matomotracker.client_id = thisClass.config.client_id;
              if (heartbeat && heartbeat > 0) paella.userTracking.matomotracker.enableHeartBeatTimer(heartbeat);
              if (Matomo && Matomo.MediaAnalytics) {
                paella.events.bind(paella.events.videoReady, () => {
                  Matomo.MediaAnalytics.scanForMedia();
                });
              }
              thisClass.registerVisit();
              tracked = true;
            });
          return true;
        }	else {
          paella.log.debug("No tracking permission or no Matomo Site ID found in config file. Disabling Matomo Analytics PlugIn");
          return false;
        }
      }

      function initCookieNotification() {
          // load cookieconsent lib from remote server
          paella.require(paella.baseUrl + 'javascript/cookieconsent.min.js')
            .then(() => {
              paella.log.debug("Matomo: cookie consent lib loaded");
              window.cookieconsent.initialise({
                  "palette": {
                      "popup": {
                          "background": cookieconsent_base_color
                      },
                      "button": {
                          "background": cookieconsent_highlight_color
                      }
                  },
                  "type": "opt-in",
                  "position": "top",
                  "content": {
                      "message": translate('matomo_message', "This site would like to use Matomo to collect usage information of the recordings."),
                      "allow": translate('matomo_accept', "Accept"),
                      "deny": translate('matomo_deny', "Deny"),
                      "link": translate('matomo_more_info', "More information"),
                      "policy": translate('matomo_policy', "Cookie Policy"),
                      // link to the Matomo platform privacy policy - describing what are we collecting
                      // through the platform
                      "href": privacy_url
                  },
                  onInitialise: function (status) {
                      var type = this.options.type;
                      var didConsent = this.hasConsented();
                      // enable cookies - send user data to the platform
                      // only if the user enabled cookies
                      if (type == 'opt-in' && didConsent) {
                          setTrackingPermission(true);
                      } else {
                          setTrackingPermission(false);
                      }
                  },
                  onStatusChange: function (status, chosenBefore) {
                      var type = this.options.type;
                      var didConsent = this.hasConsented();
                      // enable cookies - send user data to the platform
                      // only if the user enabled cookies
                      setTrackingPermission(type == 'opt-in' && didConsent);
                  },
                  onRevokeChoice: function () {
                      var type = this.options.type;
                      var didConsent = this.hasConsented();
                      // disable cookies - set what to do when
                      // the user revokes cookie usage
                      setTrackingPermission(type == 'opt-in' && didConsent);
                  }
              })
          });
      }

      function initTranslate(language, funcSuccess, funcError) {
          paella.log.debug('Matomo: selecting language ' + language.slice(0,2));
          var jsonstr = window.location.origin + '/player/localization/paella_' + language.slice(0,2) + '.json';
          $.ajax({
              url: jsonstr,
              dataType: 'json',
              success: function (data) {
                  if (data) {
                      data.value_locale = language;
                      translations = data;
                      if (funcSuccess) {
                          funcSuccess(translations);
                      }
                  } else if (funcError) {
                      funcError();
                  }
              },
              error: function () {
                  if (funcError) {
                      funcError();
                  }
              }
          });
      }

      function translate(str, strIfNotFound) {
          return (translations[str] != undefined) ? translations[str] : strIfNotFound;
      }

      function isTrackingPermission() {
          if (checkDoNotTrackStatus() || !trackingPermission) {
              return false;
          } else {
              return true;
          }
      }

      function checkDoNotTrackStatus() {
          if (window.navigator.doNotTrack == 1 || window.navigator.msDoNotTrack == 1) {
              paella.log.debug("Matomo: Browser DoNotTrack: true");
              return true;
          }
          paella.log.debug("Matomo: Browser DoNotTrack: false");
          return false;
      }

      function setTrackingPermission(permissionStatus) {
          trackingPermission = permissionStatus;
          paella.log.debug("Matomo: trackingPermissions: " + permissionStatus);
          trackMatomo();
      };

      if (ask_for_concent) {
        initTranslate(navigator.language, function () {
            paella.log.debug('Matomo: Successfully translated.');
            initCookieNotification();
        }, function () {
            paella.log.debug('Matomo: Error translating.');
            initCookieNotification();
        });
      } else {
        trackingPermission = true;
      }

      onSuccess(trackMatomo());

    } // checkEnabled

    registerVisit() {
      var title,
          event_id,
          series_title,
          series_id,
          presenter,
          view_mode;

      if (paella.opencast && paella.opencast._episode) {
        title = paella.opencast._episode.dcTitle;
        event_id = paella.opencast._episode.id;
        presenter = paella.opencast._episode.dcCreator;
        paella.userTracking.matomotracker.setCustomVariable(5, "client",
          (paella.userTracking.matomotracker.client_id || "Paella Opencast"));
      } else {
        paella.userTracking.matomotracker.setCustomVariable(5, "client",
          (paella.userTracking.matomotracker.client_id || "Paella Standalone"));
      }

      if (paella.opencast && paella.opencast._episode && paella.opencast._episode.mediapackage) {
        series_id = paella.opencast._episode.mediapackage.series;
        series_title = paella.opencast._episode.mediapackage.seriestitle;
      }

      if (title)
        paella.userTracking.matomotracker.setCustomVariable(1, "event", title + " (" + event_id + ")", "page");
      if (series_title)
        paella.userTracking.matomotracker.setCustomVariable(2, "series", series_title + " (" + series_id + ")", "page");
      if (presenter) paella.userTracking.matomotracker.setCustomVariable(3, "presenter", presenter, "page");
      paella.userTracking.matomotracker.setCustomVariable(4, "view_mode", view_mode, "page");
      if (title && presenter) {
        paella.userTracking.matomotracker.setDocumentTitle(title + " - " + (presenter || "Unknown"));
        paella.userTracking.matomotracker.trackPageView(title + " - " + (presenter || "Unknown"));
      } else {
        paella.userTracking.matomotracker.trackPageView();
      }
    }

    loadTitle() {
      var title = paella.player.videoLoader.getMetadata() && paella.player.videoLoader.getMetadata().title;
      return title;
    }

    log(event, params) {
      if (paella.userTracking.matomotracker === undefined) {
        paella.log.debug("Matomo Tracker is missing");
        return;
      }
      if ((this.config.category === undefined) || (this.config.category ===true)) {

        var value = "";

        try {
          value = JSON.stringify(params);
        } catch(e) {}

        switch (event) {
          case paella.events.play:
            paella.userTracking.matomotracker.trackEvent("Player.Controls","Play", this.loadTitle());
            break;
          case paella.events.pause:
            paella.userTracking.matomotracker.trackEvent("Player.Controls","Pause", this.loadTitle());
            break;
          case paella.events.endVideo:
            paella.userTracking.matomotracker.trackEvent("Player.Status","Ended", this.loadTitle());
            break;
          case paella.events.showEditor:
            paella.userTracking.matomotracker.trackEvent("Player.Editor","Show", this.loadTitle());
            break;
          case paella.events.hideEditor:
            paella.userTracking.matomotracker.trackEvent("Player.Editor","Hide", this.loadTitle());
            break;
          case paella.events.enterFullscreen:
            paella.userTracking.matomotracker.trackEvent("Player.View","Fullscreen", this.loadTitle());
            break;
          case paella.events.exitFullscreen:
            paella.userTracking.matomotracker.trackEvent("Player.View","ExitFullscreen", this.loadTitle());
            break;
          case paella.events.loadComplete:
            paella.userTracking.matomotracker.trackEvent("Player.Status","LoadComplete", this.loadTitle());
            break;
          case paella.events.showPopUp:
            paella.userTracking.matomotracker.trackEvent("Player.PopUp","Show", value);
            break;
          case paella.events.hidePopUp:
            paella.userTracking.matomotracker.trackEvent("Player.PopUp","Hide", value);
            break;
          case paella.events.captionsEnabled:
            paella.userTracking.matomotracker.trackEvent("Player.Captions","Enabled", value);
            break;
          case paella.events.captionsDisabled:
            paella.userTracking.matomotracker.trackEvent("Player.Captions","Disabled", value);
            break;
          case paella.events.setProfile:
            paella.userTracking.matomotracker.trackEvent("Player.View","Profile", value);
            break;
          case paella.events.seekTo:
          case paella.events.seekToTime:
            paella.userTracking.matomotracker.trackEvent("Player.Controls","Seek", value);
            break;
          case paella.events.setVolume:
            paella.userTracking.matomotracker.trackEvent("Player.Settings","Volume", value);
            break;
          case paella.events.resize:
            paella.userTracking.matomotracker.trackEvent("Player.View","resize", value);
            break;
          case paella.events.setPlaybackRate:
            paella.userTracking.matomotracker.trackEvent("Player.Controls","PlaybackRate", value);
            break;

        }
      }
    }

  }
});

paella.addPlugin(function() {
    var self = this;
    return class X5gonTracking extends paella.userTracking.SaverPlugIn {
        getName() { 
            return "org.opencast.usertracking.x5gonSaverPlugIn"; 
        };

        checkEnabled(onSuccess) {
            var urlCookieconsentJS = "https://cdnjs.cloudflare.com/ajax/libs/cookieconsent2/3.1.0/cookieconsent.min.js",
                token = this.config.token,
                translations = [],
                path,
                testingEnvironment = this.config.testing_environment,
                trackingPermission,
                tracked;
           
            function trackX5gon() {
                paella.log.debug("X5GON: trackX5gon permission check [trackingPermission " + trackingPermission + "] [tracked " + tracked + "]");
                if (isTrackingPermission() && !tracked) {
                    if (!token) {
                        paella.log.debug("X5GON: token missing! Disabling X5gon PlugIn");
                        onSuccess(false);
                        }
                    else {
                        // load x5gon lib from remote server
                        paella.log.debug("X5GON: trackX5gon loading x5gon-snippet, token: " + token);
                        paella.require("https://platform.x5gon.org/api/v1/snippet/latest/x5gon-log.min.js")
                            .then((x5gon) => {
                                paella.log.debug("X5GON: external x5gon snippet loaded");
                                if (typeof x5gonActivityTracker !== 'undefined') {
                                    x5gonActivityTracker(token, testingEnvironment);
                                    paella.log.debug("X5GON: send data to X5gon servers");
                                    tracked = true;
                                }                                             
                            });
                    }
                    onSuccess(true);
                } else {
                    onSuccess(false);
                }
            }

            function initCookieNotification() {
                // load cookieconsent lib from remote server
                paella.require(urlCookieconsentJS)
                    .then((cookieconsent) => {
                        paella.log.debug("X5GON: external cookie consent lib loaded");
                        window.cookieconsent.initialise({
                            "palette": {
                                "popup": {
                                    "background": "#1d8a8a"
                                },
                                "button": {
                                    "background": "#62ffaa"
                                }
                            },
                            "type": "opt-in",
                            "position": "top",
                            "content": {
                                "message": translate('x5_message', "On this site the X5gon service can be included, to provide personalized Open Educational Ressources."),
                                "allow": translate('x5_accept', "Accept"),
                                "deny": translate('x5_deny', "Deny"),
                                "link": translate('x5_more_info', "More information"),
                                "policy": translate('x5_policy', "Cookie Policy"),
                                // link to the X5GON platform privacy policy - describing what are we collecting
                                // through the platform
                                "href": "https://platform.x5gon.org/privacy-policy"
                            },
                            onInitialise: function (status) {
                                var type = this.options.type;
                                var didConsent = this.hasConsented();
                                // enable cookies - send user data to the platform
                                // only if the user enabled cookies
                                if (type == 'opt-in' && didConsent) {
                                    setTrackingPermission(true);
                                } else {
                                    setTrackingPermission(false);
                                }
                            },
                            onStatusChange: function (status, chosenBefore) {
                                var type = this.options.type;
                                var didConsent = this.hasConsented();
                                // enable cookies - send user data to the platform
                                // only if the user enabled cookies
                                if (type == 'opt-in' && didConsent) {
                                    setTrackingPermission(true);
                                } else {
                                    setTrackingPermission(false);
                                }
                            },
                            onRevokeChoice: function () {
                                var type = this.options.type;
                                var didConsent = this.hasConsented();
                                // disable cookies - set what to do when
                                // the user revokes cookie usage
                                if (type == 'opt-in' && didConsent) {
                                    setTrackingPermission(true);
                                } else {
                                    setTrackingPermission(false);
                                }
                            }
                        });
                    });
            }

            function initTranslate(language, funcSuccess, funcError) {
                paella.log.debug('X5GON: selecting language ' + language.slice(0,2));
                var jsonstr = window.location.origin + '/player/localization/paella_' + language.slice(0,2) + '.json';
                $.ajax({
                    url: jsonstr,
                    dataType: 'json',
                    success: function (data) {
                        if (data) {
                            data.value_locale = language;
                            translations = data;
                            if (funcSuccess) {
                                funcSuccess(translations);
                            }
                        } else if (funcError) {
                            funcError();
                        }
                    },
                    error: function () {
                        if (funcError) {
                            funcError();
                        }
                    }
                });
            }

            function translate(str, strIfNotFound) {
                return (translations[str] != undefined) ? translations[str] : strIfNotFound;
            }

            function isTrackingPermission() {
                if (checkDoNotTrackStatus() || !trackingPermission) {
                    return false;
                } else {
                    return true;   
                }  
            }

            function checkDoNotTrackStatus() {
                if (window.navigator.doNotTrack == 1 || window.navigator.msDoNotTrack == 1) {
                    paella.log.debug("X5GON: Browser DoNotTrack: true");
                    return true;
                }
                paella.log.debug("X5GON: Browser DoNotTrack: false");
                return false;
            }

            function setTrackingPermission(permissionStatus) {
                trackingPermission = permissionStatus;
                paella.log.debug("X5GON: trackingPermissions: " + permissionStatus);
                trackX5gon();
            };

            initTranslate(navigator.language, function () {
                paella.log.debug('X5GON: Successfully translated.');
                initCookieNotification();
            }, function () {
                paella.log.debug('X5gon: Error translating.');
                initCookieNotification();
            });

            trackX5gon();

            onSuccess(true);
        };

        log(event, params) {
            if ((this.config.category === undefined) || (this.config.category === true)) {
                var category = this.config.category || "PaellaPlayer";
                var action = event;
                var label = "";

                try {
                    label = JSON.stringify(params);
                } catch (e) {}
            }
        };
    };
});
