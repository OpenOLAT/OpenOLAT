"use strict";

var _this5 = void 0;

function asyncGeneratorStep(gen, resolve, reject, _next, _throw, key, arg) { try { var info = gen[key](arg); var value = info.value; } catch (error) { reject(error); return; } if (info.done) { resolve(value); } else { Promise.resolve(value).then(_next, _throw); } }

function _asyncToGenerator(fn) { return function () { var self = this, args = arguments; return new Promise(function (resolve, reject) { var gen = fn.apply(self, args); function _next(value) { asyncGeneratorStep(gen, resolve, reject, _next, _throw, "next", value); } function _throw(err) { asyncGeneratorStep(gen, resolve, reject, _next, _throw, "throw", err); } _next(undefined); }); }; }

function _get(target, property, receiver) { if (typeof Reflect !== "undefined" && Reflect.get) { _get = Reflect.get; } else { _get = function _get(target, property, receiver) { var base = _superPropBase(target, property); if (!base) return; var desc = Object.getOwnPropertyDescriptor(base, property); if (desc.get) { return desc.get.call(receiver); } return desc.value; }; } return _get(target, property, receiver || target); }

function _superPropBase(object, property) { while (!Object.prototype.hasOwnProperty.call(object, property)) { object = _getPrototypeOf(object); if (object === null) break; } return object; }

function _toConsumableArray(arr) { return _arrayWithoutHoles(arr) || _iterableToArray(arr) || _unsupportedIterableToArray(arr) || _nonIterableSpread(); }

function _nonIterableSpread() { throw new TypeError("Invalid attempt to spread non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method."); }

function _unsupportedIterableToArray(o, minLen) { if (!o) return; if (typeof o === "string") return _arrayLikeToArray(o, minLen); var n = Object.prototype.toString.call(o).slice(8, -1); if (n === "Object" && o.constructor) n = o.constructor.name; if (n === "Map" || n === "Set") return Array.from(o); if (n === "Arguments" || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(n)) return _arrayLikeToArray(o, minLen); }

function _iterableToArray(iter) { if (typeof Symbol !== "undefined" && Symbol.iterator in Object(iter)) return Array.from(iter); }

function _arrayWithoutHoles(arr) { if (Array.isArray(arr)) return _arrayLikeToArray(arr); }

function _arrayLikeToArray(arr, len) { if (len == null || len > arr.length) len = arr.length; for (var i = 0, arr2 = new Array(len); i < len; i++) { arr2[i] = arr[i]; } return arr2; }

function _typeof(obj) { "@babel/helpers - typeof"; if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { _typeof = function _typeof(obj) { return typeof obj; }; } else { _typeof = function _typeof(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return _typeof(obj); }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function"); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, writable: true, configurable: true } }); if (superClass) _setPrototypeOf(subClass, superClass); }

function _setPrototypeOf(o, p) { _setPrototypeOf = Object.setPrototypeOf || function _setPrototypeOf(o, p) { o.__proto__ = p; return o; }; return _setPrototypeOf(o, p); }

function _createSuper(Derived) { var hasNativeReflectConstruct = _isNativeReflectConstruct(); return function _createSuperInternal() { var Super = _getPrototypeOf(Derived), result; if (hasNativeReflectConstruct) { var NewTarget = _getPrototypeOf(this).constructor; result = Reflect.construct(Super, arguments, NewTarget); } else { result = Super.apply(this, arguments); } return _possibleConstructorReturn(this, result); }; }

function _possibleConstructorReturn(self, call) { if (call && (_typeof(call) === "object" || typeof call === "function")) { return call; } return _assertThisInitialized(self); }

function _assertThisInitialized(self) { if (self === void 0) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return self; }

function _isNativeReflectConstruct() { if (typeof Reflect === "undefined" || !Reflect.construct) return false; if (Reflect.construct.sham) return false; if (typeof Proxy === "function") return true; try { Date.prototype.toString.call(Reflect.construct(Date, [], function () {})); return true; } catch (e) { return false; } }

function _getPrototypeOf(o) { _getPrototypeOf = Object.setPrototypeOf ? Object.getPrototypeOf : function _getPrototypeOf(o) { return o.__proto__ || Object.getPrototypeOf(o); }; return _getPrototypeOf(o); }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } }

function _createClass(Constructor, protoProps, staticProps) { if (protoProps) _defineProperties(Constructor.prototype, protoProps); if (staticProps) _defineProperties(Constructor, staticProps); return Constructor; }

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
  video: {
    zIndex: 1
  },
  background: {
    zIndex: 0
  }
};
window.paella = window.paella || {};
paella.player = null;
paella.version = "6.5.1 - build: 9e04eb8";

(function buildBaseUrl() {
  if (window.paella_debug_baseUrl) {
    paella.baseUrl = window.paella_debug_baseUrl;
  } else {
    paella.baseUrl = location.href.replace(/[^/]*$/, '');
  }
})();

paella.events = {
  play: "paella:play",
  pause: "paella:pause",
  next: "paella:next",
  previous: "paella:previous",
  seeking: "paella:seeking",
  seeked: "paella:seeked",
  timeupdate: "paella:timeupdate",
  timeUpdate: "paella:timeupdate",
  seekTo: "paella:setseek",
  endVideo: "paella:endvideo",
  // Triggered when a single video stream ends (once per video)
  ended: "paella:ended",
  // Triggered when the video ends 
  seekToTime: "paella:seektotime",
  setTrim: "paella:settrim",
  setPlaybackRate: "paella:setplaybackrate",
  setVolume: 'paella:setVolume',
  setComposition: 'paella:setComposition',
  loadStarted: 'paella:loadStarted',
  loadComplete: 'paella:loadComplete',
  loadPlugins: 'paella:loadPlugins',
  error: 'paella:error',
  documentChanged: 'paella:documentChanged',
  didSaveChanges: 'paella:didsavechanges',
  controlBarWillHide: 'paella:controlbarwillhide',
  controlBarDidHide: 'paella:controlbardidhide',
  controlBarDidShow: 'paella:controlbardidshow',
  hidePopUp: 'paella:hidePopUp',
  showPopUp: 'paella:showPopUp',
  enterFullscreen: 'paella:enterFullscreen',
  exitFullscreen: 'paella:exitFullscreen',
  resize: 'paella:resize',
  // params: { width:paellaPlayerContainer width, height:paellaPlayerContainer height }
  videoZoomChanged: 'paella:videoZoomChanged',
  audioTagChanged: 'paella:audiotagchanged',
  zoomAvailabilityChanged: 'paella:zoomavailabilitychanged',
  qualityChanged: 'paella:qualityChanged',
  singleVideoReady: 'paella:singleVideoReady',
  singleVideoUnloaded: 'paella:singleVideoUnloaded',
  videoReady: 'paella:videoReady',
  videoUnloaded: 'paella:videoUnloaded',
  controlBarLoaded: 'paella:controlBarLoaded',
  flashVideoEvent: 'paella:flashVideoEvent',
  captionAdded: 'paella:caption:add',
  // Event triggered when new caption is available.
  captionsEnabled: 'paella:caption:enabled',
  // Event triguered when a caption es enabled.
  captionsDisabled: 'paella:caption:disabled',
  // Event triguered when a caption es disabled.
  profileListChanged: 'paella:profilelistchanged',
  setProfile: 'paella:setprofile',
  seekAvailabilityChanged: 'paella:seekAvailabilityChanged',
  trigger: function trigger(event, params) {
    $(document).trigger(event, params);
  },
  bind: function bind(event, callback) {
    $(document).bind(event, function (event, params) {
      callback(event, params);
    });
  },
  setupExternalListener: function setupExternalListener() {
    window.addEventListener("message", function (event) {
      if (event.data && event.data.event) {
        paella.events.trigger(event.data.event, event.data.params);
      }
    }, false);
  }
};
paella.events.setupExternalListener();

(function () {
  paella.utils = paella.utils || {}; // This class requires jquery

  paella.utils.ajax = {
    // onSuccess/onFail(data,type,returnCode,rawData)
    send: function send(type, params, onSuccess, onFail) {
      this.assertParams(params);
      var ajaxObj = jQuery.ajax({
        url: params.url,
        data: params.params,
        type: type
      });

      if (typeof onSuccess == 'function') {
        ajaxObj.done(function (data, textStatus, jqXHR) {
          var contentType = jqXHR.getResponseHeader('content-type');
          onSuccess(data, contentType, jqXHR.status, jqXHR.responseText);
        });
      }

      if (typeof onFail == 'function') {
        ajaxObj.fail(function (jqXHR, textStatus, error) {
          onFail(textStatus + ' : ' + error, 'text/plain', jqXHR.status, jqXHR.responseText);
        });
      }
    },
    assertParams: function assertParams(params) {
      if (!params.url) throw new Error("paella.utils.ajax.send: url parameter not found");
      if (!params.params) params.params = {};
    }
  };

  paella.utils.ajax["get"] = function (params, onSuccess, onFail) {
    paella.utils.ajax.send('get', params, onSuccess, onFail);
  };

  paella.utils.ajax["post"] = function (params, onSuccess, onFail) {
    paella.utils.ajax.send('post', params, onSuccess, onFail);
  };

  paella.utils.ajax["put"] = function (params, onSuccess, onFail) {
    paella.utils.ajax.send('put', params, onSuccess, onFail);
  };

  paella.utils.ajax["delete"] = function (params, onSuccess, onFail) {
    paella.utils.ajax.send('delete', params, onSuccess, onFail);
  }; // TODO: AsyncLoader is deprecated and should be replaced by promises


  var AsyncLoaderCallback = /*#__PURE__*/function () {
    function AsyncLoaderCallback(name) {
      _classCallCheck(this, AsyncLoaderCallback);

      this.name = name;
      this.prevCb = null;
      this.nextCb = null;
      this.loader = null;
    }

    _createClass(AsyncLoaderCallback, [{
      key: "load",
      value: function load(onSuccess, onError) {
        onSuccess(); // If error: onError()
      }
    }]);

    return AsyncLoaderCallback;
  }();

  paella.utils.AsyncLoaderCallback = AsyncLoaderCallback;

  var AjaxCallback = /*#__PURE__*/function (_paella$utils$AsyncLo) {
    _inherits(AjaxCallback, _paella$utils$AsyncLo);

    var _super = _createSuper(AjaxCallback);

    _createClass(AjaxCallback, [{
      key: "getParams",
      value: function getParams() {
        return this.params;
      }
    }, {
      key: "willLoad",
      value: function willLoad(callback) {}
    }, {
      key: "didLoadSuccess",
      value: function didLoadSuccess(callback) {
        return true;
      }
    }, {
      key: "didLoadFail",
      value: function didLoadFail(callback) {
        return false;
      }
    }]);

    function AjaxCallback(params, type) {
      var _this;

      _classCallCheck(this, AjaxCallback);

      _this = _super.call(this);
      _this.params = null;
      _this.type = 'get';
      _this.data = null;
      _this.mimeType = null;
      _this.statusCode = null;
      _this.rawData = null;
      _this.name = "ajaxCallback";
      if (type) _this.type = type;
      if (typeof params == 'string') _this.params = {
        url: params
      };else if (_typeof(params) == 'object') _this.params = params;else _this.params = {};
      return _this;
    }

    _createClass(AjaxCallback, [{
      key: "load",
      value: function load(onSuccess, onError) {
        var This = this;
        if (typeof this.willLoad == 'function') this.willLoad(this);
        paella.utils.ajax.send(this.type, this.getParams(), function (data, type, code, rawData) {
          var status = true;
          This.data = data;
          This.mimeType = type;
          This.statusCode = code;
          This.rawData = rawData;
          if (typeof This.didLoadSuccess == 'function') status = This.didLoadSuccess(This);
          if (status) onSuccess();else onError();
        }, function (data, type, code, rawData) {
          var status = false;
          This.data = data;
          This.mimeType = type;
          This.statusCode = code;
          This.rawData = rawData;
          if (typeof This.didLoadFail == 'function') status = This.didLoadFail(This);
          if (status) onSuccess();else onError();
        });
      }
    }]);

    return AjaxCallback;
  }(paella.utils.AsyncLoaderCallback);

  paella.utils.AjaxCallback = AjaxCallback;

  var JSONCallback = /*#__PURE__*/function (_paella$utils$AjaxCal) {
    _inherits(JSONCallback, _paella$utils$AjaxCal);

    var _super2 = _createSuper(JSONCallback);

    function JSONCallback(params, type) {
      _classCallCheck(this, JSONCallback);

      return _super2.call(this, params, type);
    }

    _createClass(JSONCallback, [{
      key: "didLoadSuccess",
      value: function didLoadSuccess(callback) {
        if (_typeof(callback.data) == 'object') return true;

        try {
          callback.data = JSON.parse(callback.data);
          return true;
        } catch (e) {
          callback.data = {
            error: "Unexpected data format",
            data: callback.data
          };
          return false;
        }
      }
    }]);

    return JSONCallback;
  }(paella.utils.AjaxCallback);

  paella.utils.JSONCallback = JSONCallback;

  var AsyncLoader = /*#__PURE__*/function () {
    _createClass(AsyncLoader, [{
      key: "clearError",
      value: function clearError() {
        this.errorCallbacks = [];
      }
    }]);

    function AsyncLoader() {
      _classCallCheck(this, AsyncLoader);

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

    _createClass(AsyncLoader, [{
      key: "addCallback",
      value: function addCallback(cb, name) {
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
    }, {
      key: "getCallback",
      value: function getCallback(name) {
        return this.callbackArray[name];
      }
    }, {
      key: "load",
      value: function load(onSuccess, onError) {
        console.warn("paella.utils.AsyncLoader is deprecated. Consider to replace it with JavaScript promises.");
        var This = this;

        if (this.currentCb) {
          this.currentCb.load(function () {
            This.onComplete(This.currentCb, This.currentCb.__cbName__, true);
            This.currentCb = This.currentCb.nextCb;
            This.load(onSuccess, onError);
          }, function () {
            This.onComplete(This.currentCb, This.currentCb.__cbName__, false);

            if (This.continueOnError) {
              This.errorCallbacks.push(This.currentCb);
              This.currentCb = This.currentCb.nextCb;
              This.load(onSuccess, onError);
            } else if (typeof onError == 'function') {
              onError();
            }
          });
        } else if (typeof onSuccess == 'function') {
          onSuccess();
        }
      }
    }, {
      key: "onComplete",
      value: function onComplete(callback, cbName, status) {}
    }]);

    return AsyncLoader;
  }();

  paella.utils.AsyncLoader = AsyncLoader;
})();

(function () {
  var g_delegateCallbacks = {};
  var g_dataDelegates = [];

  var DataDelegate = /*#__PURE__*/function () {
    function DataDelegate() {
      _classCallCheck(this, DataDelegate);
    }

    _createClass(DataDelegate, [{
      key: "read",
      value: function read(context, params, onSuccess) {
        if (typeof onSuccess == 'function') {
          onSuccess({}, true);
        }
      }
    }, {
      key: "write",
      value: function write(context, params, value, onSuccess) {
        if (typeof onSuccess == 'function') {
          onSuccess({}, true);
        }
      }
    }, {
      key: "remove",
      value: function remove(context, params, onSuccess) {
        if (typeof onSuccess == 'function') {
          onSuccess({}, true);
        }
      }
    }]);

    return DataDelegate;
  }();

  paella.DataDelegate = DataDelegate;
  paella.dataDelegates = {};

  var Data = /*#__PURE__*/function () {
    _createClass(Data, [{
      key: "enabled",
      get: function get() {
        return this._enabled;
      }
    }, {
      key: "dataDelegates",
      get: function get() {
        return g_dataDelegates;
      }
    }]);

    function Data(config) {
      _classCallCheck(this, Data);

      this._enabled = config.data.enabled; // Delegate callbacks

      var executedCallbacks = [];

      var _loop = function _loop(context) {
        var callback = g_delegateCallbacks[context];
        var DelegateClass = null;
        var delegateName = null;

        if (!executedCallbacks.some(function (execCallbackData) {
          if (execCallbackData.callback == callback) {
            delegateName = execCallbackData.delegateName;
            return true;
          }
        })) {
          DelegateClass = g_delegateCallbacks[context]();
          delegateName = DelegateClass.name;
          paella.dataDelegates[delegateName] = DelegateClass;
          executedCallbacks.push({
            callback: callback,
            delegateName: delegateName
          });
        }

        if (!config.data.dataDelegates[context]) {
          config.data.dataDelegates[context] = delegateName;
        }
      };

      for (var context in g_delegateCallbacks) {
        _loop(context);
      }

      for (var key in config.data.dataDelegates) {
        try {
          var delegateName = config.data.dataDelegates[key];
          var DelegateClass = paella.dataDelegates[delegateName];
          var delegateInstance = new DelegateClass();
          g_dataDelegates[key] = delegateInstance;
        } catch (e) {
          console.warn("Warning: delegate not found - " + delegateName);
        }
      } // Default data delegate


      if (!this.dataDelegates["default"]) {
        this.dataDelegates["default"] = new paella.dataDelegates.DefaultDataDelegate();
      }
    }

    _createClass(Data, [{
      key: "read",
      value: function read(context, key, onSuccess) {
        var del = this.getDelegate(context);
        del.read(context, key, onSuccess);
      }
    }, {
      key: "write",
      value: function write(context, key, params, onSuccess) {
        var del = this.getDelegate(context);
        del.write(context, key, params, onSuccess);
      }
    }, {
      key: "remove",
      value: function remove(context, key, onSuccess) {
        var del = this.getDelegate(context);
        del.remove(context, key, onSuccess);
      }
    }, {
      key: "getDelegate",
      value: function getDelegate(context) {
        if (this.dataDelegates[context]) return this.dataDelegates[context];else return this.dataDelegates["default"];
      }
    }]);

    return Data;
  }();

  paella.Data = Data;

  paella.addDataDelegate = function (context, callback) {
    if (Array.isArray(context)) {
      context.forEach(function (ctx) {
        g_delegateCallbacks[ctx] = callback;
      });
    } else if (typeof context == "string") {
      g_delegateCallbacks[context] = callback;
    }
  };
})();

paella.addDataDelegate(["default", "trimming"], function () {
  paella.dataDelegates.DefaultDataDelegate = /*#__PURE__*/function (_paella$DataDelegate) {
    _inherits(CookieDataDelegate, _paella$DataDelegate);

    var _super3 = _createSuper(CookieDataDelegate);

    function CookieDataDelegate() {
      _classCallCheck(this, CookieDataDelegate);

      return _super3.apply(this, arguments);
    }

    _createClass(CookieDataDelegate, [{
      key: "serializeKey",
      value: function serializeKey(context, params) {
        if (_typeof(params) == 'object') {
          params = JSON.stringify(params);
        }

        return context + '|' + params;
      }
    }, {
      key: "read",
      value: function read(context, params, onSuccess) {
        var key = this.serializeKey(context, params);
        var value = paella.utils.cookies.get(key);

        try {
          value = unescape(value);
          value = JSON.parse(value);
        } catch (e) {}

        if (typeof onSuccess == 'function') {
          onSuccess(value, true);
        }
      }
    }, {
      key: "write",
      value: function write(context, params, value, onSuccess) {
        var key = this.serializeKey(context, params);

        if (_typeof(value) == 'object') {
          value = JSON.stringify(value);
        }

        value = escape(value);
        paella.utils.cookies.set(key, value);

        if (typeof onSuccess == 'function') {
          onSuccess({}, true);
        }
      }
    }, {
      key: "remove",
      value: function remove(context, params, onSuccess) {
        var key = this.serializeKey(context, params);

        if ((typeof value === "undefined" ? "undefined" : _typeof(value)) == 'object') {
          value = JSON.stringify(value);
        }

        paella.utils.cookies.set(key, '');

        if (typeof onSuccess == 'function') {
          onSuccess({}, true);
        }
      }
    }]);

    return CookieDataDelegate;
  }(paella.DataDelegate);

  return paella.dataDelegates.DefaultDataDelegate;
}); // Will be initialized inmediately after loading config.json, in PaellaPlayer.onLoadConfig()

paella.data = null;

(function () {
  paella.utils = paella.utils || {};

  var Dictionary = /*#__PURE__*/function () {
    function Dictionary() {
      _classCallCheck(this, Dictionary);

      this._dictionary = {};
    }

    _createClass(Dictionary, [{
      key: "addDictionary",
      value: function addDictionary(dict) {
        for (var key in dict) {
          this._dictionary[key] = dict[key];
        }
      }
    }, {
      key: "translate",
      value: function translate(key) {
        return this._dictionary[key] || key;
      }
    }, {
      key: "currentLanguage",
      value: function currentLanguage() {
        var lang = navigator.language || window.navigator.userLanguage;
        return lang.substr(0, 2).toLowerCase();
      }
    }]);

    return Dictionary;
  }();

  paella.utils.dictionary = new Dictionary();
})();

(function () {
  var MessageBox = /*#__PURE__*/function () {
    _createClass(MessageBox, [{
      key: "modalContainerClassName",
      get: function get() {
        return 'modalMessageContainer';
      }
    }, {
      key: "frameClassName",
      get: function get() {
        return 'frameContainer';
      }
    }, {
      key: "messageClassName",
      get: function get() {
        return 'messageContainer';
      }
    }, {
      key: "errorClassName",
      get: function get() {
        return 'errorContainer';
      }
    }, {
      key: "currentMessageBox",
      get: function get() {
        return this._currentMessageBox;
      },
      set: function set(m) {
        this._currentMessageBox = m;
      }
    }, {
      key: "messageContainer",
      get: function get() {
        return this._messageContainer;
      }
    }, {
      key: "onClose",
      get: function get() {
        return this._onClose;
      },
      set: function set(c) {
        this._onClose = c;
      }
    }]);

    function MessageBox() {
      var _this2 = this;

      _classCallCheck(this, MessageBox);

      this._messageContainer = null;
      $(window).resize(function (event) {
        return _this2.adjustTop();
      });
    }

    _createClass(MessageBox, [{
      key: "showFrame",
      value: function showFrame(src, params) {
        var closeButton = true;
        var onClose = null;

        if (params) {
          closeButton = params.closeButton;
          onClose = params.onClose;
        }

        this.doShowFrame(src, closeButton, onClose);
      }
    }, {
      key: "doShowFrame",
      value: function doShowFrame(src, closeButton, onClose) {
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
        } else {
          $('body')[0].appendChild(modalContainer);
        }

        this.currentMessageBox = modalContainer;
        this._messageContainer = messageContainer;
        this.adjustTop();

        if (closeButton) {
          this.createCloseButton();
        }
      }
    }, {
      key: "showElement",
      value: function showElement(domElement, params) {
        var closeButton = true;
        var onClose = null;
        var className = this.messageClassName;

        if (params) {
          className = params.className;
          closeButton = params.closeButton;
          onClose = params.onClose;
        }

        this.doShowElement(domElement, closeButton, className, onClose);
      }
    }, {
      key: "showMessage",
      value: function showMessage(message, params) {
        var closeButton = true;
        var onClose = null;
        var className = this.messageClassName;

        if (params) {
          className = params.className;
          closeButton = params.closeButton;
          onClose = params.onClose;
        }

        this.doShowMessage(message, closeButton, className, onClose);
      }
    }, {
      key: "doShowElement",
      value: function doShowElement(domElement, closeButton, className, onClose) {
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
    }, {
      key: "doShowMessage",
      value: function doShowMessage(message, closeButton, className, onClose) {
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
        } else {
          $('body')[0].appendChild(modalContainer);
        }

        this.currentMessageBox = modalContainer;
        this._messageContainer = messageContainer;
        this.adjustTop();

        if (closeButton) {
          this.createCloseButton();
        }
      }
    }, {
      key: "showError",
      value: function showError(message, params) {
        var closeButton = false;
        var onClose = null;

        if (params) {
          closeButton = params.closeButton;
          onClose = params.onClose;
        }

        this.doShowError(message, closeButton, onClose);
      }
    }, {
      key: "doShowError",
      value: function doShowError(message, closeButton, onClose) {
        this.doShowMessage(message, closeButton, this.errorClassName, onClose);
      }
    }, {
      key: "createCloseButton",
      value: function createCloseButton() {
        var _this3 = this;

        if (this._messageContainer) {
          var closeButton = document.createElement('span');

          this._messageContainer.appendChild(closeButton);

          closeButton.className = 'paella_messageContainer_closeButton icon-cancel-circle';
          $(closeButton).click(function (event) {
            return _this3.onCloseButtonClick();
          });
          $(window).keyup(function (evt) {
            if (evt.keyCode == 27) {
              _this3.onCloseButtonClick();
            }
          });
        }
      }
    }, {
      key: "adjustTop",
      value: function adjustTop() {
        if (this.currentMessageBox) {
          var msgHeight = $(this._messageContainer).outerHeight();
          var containerHeight = $(this.currentMessageBox).height();
          var top = containerHeight / 2 - msgHeight / 2;
          this._messageContainer.style.marginTop = top + 'px';
        }
      }
    }, {
      key: "close",
      value: function close() {
        if (this.currentMessageBox && this.currentMessageBox.parentNode) {
          var msgBox = this.currentMessageBox;
          var parent = msgBox.parentNode;
          $('#playerContainer').removeClass("modalVisible");
          $(msgBox).animate({
            opacity: 0.0
          }, 300, function () {
            parent.removeChild(msgBox);
          });

          if (this.onClose) {
            this.onClose();
          }
        }
      }
    }, {
      key: "onCloseButtonClick",
      value: function onCloseButtonClick() {
        this.close();
      }
    }]);

    return MessageBox;
  }();

  paella.MessageBox = MessageBox;
  paella.messageBox = new paella.MessageBox();
})();

(function () {
  paella.utils = paella.utils || {};
  paella.utils.cookies = {
    set: function set(name, value) {
      document.cookie = name + "=" + value;
    },
    get: function get(name) {
      var i,
          x,
          y,
          ARRcookies = document.cookie.split(";");

      for (i = 0; i < ARRcookies.length; i++) {
        x = ARRcookies[i].substr(0, ARRcookies[i].indexOf("="));
        y = ARRcookies[i].substr(ARRcookies[i].indexOf("=") + 1);
        x = x.replace(/^\s+|\s+$/g, "");

        if (x == name) {
          return unescape(y);
        }
      }
    }
  };
  paella.utils.hashParams = {
    extractUrl: function extractUrl() {
      var urlOnly = window.location.href;

      if (urlOnly.lastIndexOf('#') >= 0) {
        urlOnly = urlOnly.substr(0, urlOnly.lastIndexOf('#'));
      }

      return urlOnly;
    },
    extractParams: function extractParams() {
      var params = window.location.href;

      if (params.lastIndexOf('#') >= 0) {
        params = params.substr(window.location.href.lastIndexOf('#'));
      } else {
        params = "";
      }

      return params;
    },
    clear: function clear() {
      window.location.href = this.extractUrl() + '#';
    },
    unset: function unset(key) {
      var url = location.href;
      var lastIndex = url.lastIndexOf('#');
      var urlOnly = this.extractUrl();

      if (lastIndex >= 0 && lastIndex + 1 < url.length) {
        var newParams = "";
        var params = url.substr(url.lastIndexOf('#') + 1);
        params = params.split('&');

        for (var i = 0; i < params.length; ++i) {
          var current = params[i];
          var keyValue = current.split('=');

          if (keyValue.length >= 2 && keyValue[0] != key || keyValue.length < 2) {
            if (newParams == "") newParams += '#';else newParams += "&";
            newParams += current;
          }
        }

        if (newParams == "") newParams = "#";
        location.href = urlOnly + newParams;
      }
    },
    set: function set(key, value) {
      if (key && value) {
        this.unset(key);
        var url = this.extractUrl();
        var params = this.extractParams();
        var result = url;

        if (params.length == 0) {
          result += '#' + key + '=' + value;
        } else if (params.length == 1) {
          result += params + key + '=' + value;
        } else {
          result += params + "&" + key + '=' + value;
        }

        location.href = result;
      }
    },
    get: function get(key) {
      var url = location.href;
      var index = url.indexOf("#");
      if (index == -1) return "";
      index = url.indexOf(key, index) + key.length;

      if (url.charAt(index) == "=") {
        var result = url.indexOf("&", index);

        if (result == -1) {
          result = url.length;
        }

        return url.substring(index + 1, result);
      }

      return "";
    }
  };
  paella.utils.parameters = {
    list: null,
    parse: function parse() {
      if (!this.list) {
        var url = window.location.href;
        this.list = {};

        if (/(http|https|file)?:\/\/([a-z0-9.\-_\/\~:]*\?)([a-z0-9.\/\-_\%\=\&]*)\#*/i.test(url)) {
          var params = RegExp.$3;
          var paramArray = params.split('&');
          this.list = {};

          for (var i = 0; i < paramArray.length; ++i) {
            var keyValue = paramArray[i].split('=');
            var key = keyValue[0];
            var value = keyValue.length == 2 ? keyValue[1] : '';
            this.list[key] = value;
          }
        }
      }
    },
    get: function get(parameter) {
      if (this.list == null) {
        this.parse();
      }

      return this.list[parameter];
    },
    extractUrl: function extractUrl() {
      var urlOnly = paella.utils.hashParams.extractUrl();

      if (urlOnly.lastIndexOf('?') >= 0) {
        urlOnly = urlOnly.substr(0, urlOnly.lastIndexOf('?'));
      }

      return urlOnly;
    },
    extractParams: function extractParams() {
      // Primero quitar los parámetros hash
      var urlAndParams = paella.utils.hashParams.extractUrl();
      var params = urlAndParams;

      if (params.lastIndexOf('?') >= 0) {
        params = params.substr(window.location.href.lastIndexOf('?'));
      } else {
        params = "";
      }

      return params;
    },
    // Pasa los parámetros de la URL a hash. Esta acción recargará la página
    toHash: function toHash() {
      var urlOnly = this.extractUrl();
      var hashParameters = paella.utils.hashParams.extractParams();
      var parameters = this.extractParams();
      var newHashParams = "";
      var result = urlOnly;

      if (parameters.length == 0 || parameters.length == 1) {
        result += hashParameters;
      } else {
        parameters = parameters.substr(1);
        parameters = parameters.split('&');

        for (var i = 0; i < parameters.length; ++i) {
          keyValue = parameters[i].split('=');

          if (keyValue.length >= 2 && paella.utils.hashParams.get(keyValue[0]) == '') {
            if (hashParameters == "" && newHashParams == "") newHashParams += '#';else newHashParams += '&';
            newHashParams += keyValue[0] + '=' + keyValue[1];
          }
        }

        result += hashParameters + newHashParams;
      }

      if (location.href != result) {
        location.href = result;
      }
    }
  };
})();

(function () {
  paella.utils = paella.utils || {};

  function parseOperatingSystem(userAgentString) {
    this.system.MacOS = /Macintosh/.test(userAgentString);
    this.system.Windows = /Windows/.test(userAgentString);
    this.system.iPhone = /iPhone/.test(userAgentString);
    this.system.iPodTouch = /iPod/.test(userAgentString);
    this.system.iPad = /iPad/.test(userAgentString) || /FxiOS/.test(userAgentString);
    this.system.iOS = this.system.iPhone || this.system.iPad || this.system.iPodTouch;
    this.system.Android = /Android/.test(userAgentString);
    this.system.Linux = this.system.Android ? false : /Linux/.test(userAgentString);

    if (this.system.MacOS) {
      this.system.OSName = "Mac OS X";
      parseMacOSVersion.apply(this, [userAgentString]);
    } else if (this.system.Windows) {
      this.system.OSName = "Windows";
      parseWindowsVersion.apply(this, [userAgentString]);
    } else if (this.system.Linux) {
      this.system.OSName = "Linux";
      parseLinuxVersion.apply(this, [userAgentString]);
    } else if (this.system.iOS) {
      this.system.OSName = "iOS";
      parseIOSVersion.apply(this, [userAgentString]);
    } else if (this.system.Android) {
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

    this.browser.Chrome = /Chrome\/([\d\.]+) Safari\//.test(userAgentString) || /Chrome\/([\d\.]+) Electron\//.test(userAgentString);

    if (this.browser.Chrome) {
      this.browser.Name = "Chrome";
      this.browser.Vendor = "Google";
      this.browser.Version.versionString = RegExp.$1;
    } // The attribute this.browser.Chrome will still be true, because it is the same browser after all


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

    var firefoxIOS = this.browser.Firefox || /FxiOS\/(\d+\.\d+)/.test(userAgentString);

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
      var re = /\Mozilla\/5.0 \(([^)]+)\) like Gecko/;
      var matches = re.exec(userAgentString);

      if (matches) {
        re = /rv:(.*)/;
        var version = re.exec(matches[1]);
        this.browser.Explorer = true;
        this.browser.Name = "Internet Explorer";
        this.browser.Vendor = "Microsoft";

        if (version) {
          this.browser.Version.versionString = version[1];
        } else {
          this.browser.Version.versionString = "unknown";
        }
      }
    } else {
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
    } else if (this.system.Android) {
      this.browser.IsMobileVersion = true;
      this.browser.Android = /Version\/([\d\.]+) Mobile/.test(userAgentString);

      if (this.browser.MobileSafari) {
        this.browser.Name = "Android Browser";
        this.browser.Vendor = "Google";
        this.browser.Version.versionString = RegExp.$1;
      } else {
        this.browser.Chrome = /Chrome\/([\d\.]+)/.test(userAgentString);
        this.browser.Name = "Chrome";
        this.browser.Vendor = "Google";
        this.browser.Version.versionString = RegExp.$1;
      }

      this.browser.Safari = false;
    } else {
      this.browser.IsMobileVersion = false;
    }

    parseBrowserVersion.apply(this, [userAgentString]);
  }

  function parseBrowserVersion(userAgentString) {
    if (/([\d]+)\.([\d]+)\.*([\d]*)/.test(this.browser.Version.versionString)) {
      this.browser.Version.major = Number(RegExp.$1);
      this.browser.Version.minor = Number(RegExp.$2);
      this.browser.Version.revision = RegExp.$3 ? Number(RegExp.$3) : 0;
    }
  }

  function parseMacOSVersion(userAgentString) {
    var versionString = /Mac OS X (\d+_\d+_*\d*)/.test(userAgentString) ? RegExp.$1 : '';
    this.system.Version = {}; // Safari/Chrome

    if (versionString != '') {
      if (/(\d+)_(\d+)_*(\d*)/.test(versionString)) {
        this.system.Version.major = Number(RegExp.$1);
        this.system.Version.minor = Number(RegExp.$2);
        this.system.Version.revision = RegExp.$3 ? Number(RegExp.$3) : 0;
      }
    } // Firefox/Opera
    else {
        versionString = /Mac OS X (\d+\.\d+\.*\d*)/.test(userAgentString) ? RegExp.$1 : 'Unknown';

        if (/(\d+)\.(\d+)\.*(\d*)/.test(versionString)) {
          this.system.Version.major = Number(RegExp.$1);
          this.system.Version.minor = Number(RegExp.$2);
          this.system.Version.revision = RegExp.$3 ? Number(RegExp.$3) : 0;
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
      this.system.Version.revision = 0; // Solo por compatibilidad

      this.system.Version.stringValue = "NT " + this.system.Version.major + "." + this.system.Version.minor;
      var major = this.system.Version.major;
      var minor = this.system.Version.minor;
      var name = 'undefined';

      if (major == 5) {
        if (minor == 0) this.system.Version.name = '2000';else this.system.Version.name = 'XP';
      } else if (major == 6) {
        if (minor == 0) this.system.Version.name = 'Vista';else if (minor == 1) this.system.Version.name = '7';else if (minor == 2) this.system.Version.name = '8';
      } else if (major == 10) {
        this.system.Version.name = "10";
      }
    } else {
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
      this.system.Version.revision = RegExp.$3 ? Number(RegExp.$3) : 0;
      this.system.Version.stringValue = this.system.Version.major + "." + this.system.Version.minor + '.' + this.system.Version.revision;
      this.system.Version.name = "iOS";
    } else {
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
      this.system.Version.revision = RegExp.$3 ? Number(RegExp.$3) : 0;
      this.system.Version.stringValue = this.system.Version.major + "." + this.system.Version.minor + '.' + this.system.Version.revision;
    } else {
      this.system.Version.major = 0;
      this.system.Version.minor = 0;
      this.system.Version.revision = 0;
    }

    if (/Build\/([a-zA-Z]+)/.test(userAgentString)) {
      this.system.Version.name = RegExp.$1;
    } else {
      this.system.Version.name = "Unknown version";
    }

    this.system.Version.stringValue = this.system.Version.major + "." + this.system.Version.minor + '.' + this.system.Version.revision;
  }

  function getInfoString() {
    return navigator.userAgent;
  }

  var UserAgent = /*#__PURE__*/function () {
    function UserAgent() {
      var userAgentString = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : null;

      _classCallCheck(this, UserAgent);

      if (!userAgentString) {
        userAgentString = navigator.userAgent;
      }

      this._system = {};
      this._browser = {};
      parseOperatingSystem.apply(this, [userAgentString]);
      parseBrowser.apply(this, [userAgentString]);
    }

    _createClass(UserAgent, [{
      key: "getInfoString",
      value: function getInfoString() {
        return navigator.userAgent;
      }
    }, {
      key: "system",
      get: function get() {
        return this._system;
      }
    }, {
      key: "browser",
      get: function get() {
        return this._browser;
      }
    }, {
      key: "infoString",
      get: function get() {
        navigator.userAgent;
      }
    }]);

    return UserAgent;
  }();

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


(function () {
  var MouseManager = /*#__PURE__*/function () {
    _createClass(MouseManager, [{
      key: "targetObject",
      get: function get() {
        return this._targetObject;
      },
      set: function set(t) {
        this._targetObject = t;
      }
    }]);

    function MouseManager() {
      var _this4 = this;

      _classCallCheck(this, MouseManager);

      paella.events.bind('mouseup', function (event) {
        return _this4.up(event);
      });
      paella.events.bind('mousemove', function (event) {
        return _this4.move(event);
      });
      paella.events.bind('mouseover', function (event) {
        return _this4.over(event);
      });
    }

    _createClass(MouseManager, [{
      key: "down",
      value: function down(targetObject, event) {
        this.targetObject = targetObject;

        if (this.targetObject && this.targetObject.down) {
          var pageX = event.pageX || (event.changedTouches.length > 0 ? event.changedTouches[0].pageX : 0);
          var pageY = event.pageY || (event.changedTouches.length > 0 ? event.changedTouches[0].pageY : 0);
          this.targetObject.down(event, pageX, pageY);
          event.cancelBubble = true;
        }

        return false;
      }
    }, {
      key: "up",
      value: function up(event) {
        if (this.targetObject && this.targetObject.up) {
          var pageX = event.pageX || (event.changedTouches.length > 0 ? event.changedTouches[0].pageX : 0);
          var pageY = event.pageY || (event.changedTouches.length > 0 ? event.changedTouches[0].pageY : 0);
          this.targetObject.up(event, pageX, pageY);
          event.cancelBubble = true;
        }

        this.targetObject = null;
        return false;
      }
    }, {
      key: "out",
      value: function out(event) {
        if (this.targetObject && this.targetObject.out) {
          var pageX = event.pageX || (event.changedTouches.length > 0 ? event.changedTouches[0].pageX : 0);
          var pageY = event.pageY || (event.changedTouches.length > 0 ? event.changedTouches[0].pageY : 0);
          this.targetObject.out(event, pageX, pageY);
          event.cancelBubble = true;
        }

        return false;
      }
    }, {
      key: "move",
      value: function move(event) {
        if (this.targetObject && this.targetObject.move) {
          var pageX = event.pageX || (event.changedTouches.length > 0 ? event.changedTouches[0].pageX : 0);
          var pageY = event.pageY || (event.changedTouches.length > 0 ? event.changedTouches[0].pageY : 0);
          this.targetObject.move(event, pageX, pageY);
          event.cancelBubble = true;
        }

        return false;
      }
    }, {
      key: "over",
      value: function over(event) {
        if (this.targetObject && this.targetObject.over) {
          var pageX = event.pageX || (event.changedTouches.length > 0 ? event.changedTouches[0].pageX : 0);
          var pageY = event.pageY || (event.changedTouches.length > 0 ? event.changedTouches[0].pageY : 0);
          this.targetObject.over(event, pageX, pageY);
          event.cancelBubble = true;
        }

        return false;
      }
    }]);

    return MouseManager;
  }();

  paella.MouseManager = MouseManager;
})(); // paella.utils
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
  get: function get(folder) {
    if (paella.player && paella.player.config && paella.player.config.folders && paella.player.config.folders[folder]) {
      return paella.player.config.folders[folder];
    }

    return undefined;
  },
  profiles: function profiles() {
    return paella.baseUrl + (paella.utils.folders.get("profiles") || "config/profiles");
  },
  resources: function resources() {
    return paella.baseUrl + (paella.utils.folders.get("resources") || "resources");
  },
  skins: function skins() {
    return paella.baseUrl + (paella.utils.folders.get("skins") || paella.utils.folders.get("resources") + "/style");
  }
};
paella.utils.styleSheet = {
  removeById: function removeById(id) {
    var outStyleSheet = $(document.head).find('#' + id)[0];

    if (outStyleSheet) {
      document.head.removeChild(outStyleSheet);
    }
  },
  remove: function remove(fileName) {
    var links = document.head.getElementsByTagName('link');

    for (var i = 0; i < links.length; ++i) {
      if (links[i].href) {
        document.head.removeChild(links[i]);
        break;
      }
    }
  },
  add: function add(fileName, id) {
    var link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = fileName;
    link.type = 'text/css';
    link.media = 'screen';
    link.charset = 'utf-8';
    if (id) link.id = id;
    document.head.appendChild(link);
  },
  swap: function swap(outFile, inFile) {
    this.remove(outFile);
    this.add(inFile);
  }
};
paella.utils.skin = {
  set: function set(skinName) {
    var skinId = 'paellaSkin';
    paella.utils.styleSheet.removeById(skinId);
    paella.utils.styleSheet.add(paella.utils.folders.skins() + '/style_' + skinName + '.css');
    paella.utils.cookies.set("skin", skinName);
  },
  restore: function restore(defaultSkin) {
    var storedSkin = paella.utils.cookies.get("skin");

    if (storedSkin && storedSkin != "") {
      this.set(storedSkin);
    } else {
      this.set(defaultSkin);
    }
  }
};
paella.utils.timeParse = {
  timeToSeconds: function timeToSeconds(timeString) {
    var hours = 0;
    var minutes = 0;
    var seconds = 0;

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
  secondsToTime: function secondsToTime(seconds) {
    var hrs = ~~(seconds / 3600);
    if (hrs < 10) hrs = '0' + hrs;
    var mins = ~~(seconds % 3600 / 60);
    if (mins < 10) mins = '0' + mins;
    var secs = Math.floor(seconds % 60);
    if (secs < 10) secs = '0' + secs;
    return hrs + ':' + mins + ':' + secs;
  },
  secondsToText: function secondsToText(secAgo) {
    // Seconds
    if (secAgo <= 1) {
      return paella.utils.dictionary.translate("1 second ago");
    }

    if (secAgo < 60) {
      return paella.utils.dictionary.translate("{0} seconds ago").replace(/\{0\}/g, secAgo);
    } // Minutes


    var minAgo = Math.round(secAgo / 60);

    if (minAgo <= 1) {
      return paella.utils.dictionary.translate("1 minute ago");
    }

    if (minAgo < 60) {
      return paella.utils.dictionary.translate("{0} minutes ago").replace(/\{0\}/g, minAgo);
    } //Hours


    var hourAgo = Math.round(secAgo / (60 * 60));

    if (hourAgo <= 1) {
      return paella.utils.dictionary.translate("1 hour ago");
    }

    if (hourAgo < 24) {
      return paella.utils.dictionary.translate("{0} hours ago").replace(/\{0\}/g, hourAgo);
    } //Days


    var daysAgo = Math.round(secAgo / (60 * 60 * 24));

    if (daysAgo <= 1) {
      return paella.utils.dictionary.translate("1 day ago");
    }

    if (daysAgo < 24) {
      return paella.utils.dictionary.translate("{0} days ago").replace(/\{0\}/g, daysAgo);
    } //Months


    var monthsAgo = Math.round(secAgo / (60 * 60 * 24 * 30));

    if (monthsAgo <= 1) {
      return paella.utils.dictionary.translate("1 month ago");
    }

    if (monthsAgo < 12) {
      return paella.utils.dictionary.translate("{0} months ago").replace(/\{0\}/g, monthsAgo);
    } //Years


    var yearsAgo = Math.round(secAgo / (60 * 60 * 24 * 365));

    if (yearsAgo <= 1) {
      return paella.utils.dictionary.translate("1 year ago");
    }

    return paella.utils.dictionary.translate("{0} years ago").replace(/\{0\}/g, yearsAgo);
  },
  matterhornTextDateToDate: function matterhornTextDateToDate(mhdate) {
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

paella.utils.objectFromString = function (str) {
  var arr = str.split(".");
  var fn = window || _this5;

  for (var i = 0, len = arr.length; i < len; i++) {
    fn = fn[arr[i]];
  }

  if (typeof fn !== "function") {
    throw new Error("constructor not found");
  }

  return fn;
};

paella.utils.uuid = function () {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    var r = Math.random() * 16 | 0;
    var v = (c == 'x' ? r : r) & 0x3 | 0x8;
    return v.toString(16);
  });
};

(function () {
  var TimerManager = /*#__PURE__*/function () {
    function TimerManager() {
      _classCallCheck(this, TimerManager);

      this.timerArray = [];
      this.lastId = 0;
    }

    _createClass(TimerManager, [{
      key: "setupTimer",
      value: function setupTimer(timer, time) {
        this.lastId++;
        timer.timerId = this.lastId;
        timer.timeout = time;
        this.timerArray[this.lastId] = timer;
        timer.jsTimerId = setTimeout("g_timerManager.executeTimerCallback(".concat(this.lastId, ")"), time);
      }
    }, {
      key: "executeTimerCallback",
      value: function executeTimerCallback(timerId) {
        var timer = this.timerArray[timerId];

        if (timer && timer.callback) {
          timer.callback(timer, timer.params);
        }

        if (timer.repeat) {
          timer.jsTimerId = setTimeout("g_timerManager.executeTimerCallback(".concat(timer.timerId, ")"), timer.timeout);
        }
      }
    }]);

    return TimerManager;
  }();

  window.g_timerManager = new TimerManager();

  var Timer = /*#__PURE__*/function () {
    _createClass(Timer, null, [{
      key: "sleep",
      value: function sleep(milliseconds) {
        var start = new Date().getTime();

        for (var i = 0; i < 1e7; ++i) {
          if (new Date().getTime() - start > milliseconds) {
            break;
          }
        }
      }
    }]);

    function Timer(callback, time, params) {
      _classCallCheck(this, Timer);

      this.callback = callback;
      this.params = params;
      this._repeat = false;
      g_timerManager.setupTimer(this, time);
    }

    _createClass(Timer, [{
      key: "cancel",
      value: function cancel() {
        clearTimeout(this.jsTimerId);
      }
    }, {
      key: "repeat",
      get: function get() {
        return this._repeat;
      },
      set: function set(r) {
        this._repeat = r;
      }
    }]);

    return Timer;
  }();

  paella.utils.Timer = Timer;
})();

(function () {
  // Include scripts in header
  var g_requiredScripts = {};

  paella.require = function (path) {
    if (!g_requiredScripts[path]) {
      g_requiredScripts[path] = new Promise(function (resolve, reject) {
        paella.utils.ajax.get({
          url: path
        }, function (data) {
          try {
            var _module = {
              exports: null
            };
            var exports = null;
            eval(data);

            if (_module && _module.exports) {
              resolve(_module.exports);
            } else {
              var geval = eval;
              geval(data);
              resolve();
            }
          } catch (err) {
            reject(err);
          }
        }, function (err) {
          reject(err);
        });
      });
    }

    return g_requiredScripts[path];
  };

  paella.tabIndex = new ( /*#__PURE__*/function () {
    function TabIndexManager() {
      _classCallCheck(this, TabIndexManager);

      this._last = 1;
    }

    _createClass(TabIndexManager, [{
      key: "insertAfter",
      value: function insertAfter(target, elements) {
        if (target.tabIndex == null || target.tabIndex == -1) {
          throw Error("Insert tab index: the target element does not have a valid tabindex.");
        }

        var targetIndex = -1;
        var newTabIndexElements = this.tabIndexElements;
        newTabIndexElements.some(function (elem, i) {
          if (elem == target) {
            targetIndex = i;
            return true;
          }
        });
        newTabIndexElements.splice.apply(newTabIndexElements, [targetIndex + 1, 0].concat(_toConsumableArray(elements)));
        newTabIndexElements.forEach(function (elem, index) {
          elem.tabIndex = index;
          +1;
        });
        this._last = newTabIndexElements.length;
      }
    }, {
      key: "removeTabIndex",
      value: function removeTabIndex(elements) {
        var _this6 = this;

        Array.from(elements).forEach(function (e) {
          e.removeAttribute("tabindex");
        });
        this.tabIndexElements.forEach(function (elem, index) {
          elem.tabIndex = index + 1;
          _this6._last = elem.tabIndex + 1;
        });
      }
    }, {
      key: "next",
      get: function get() {
        return this._last++;
      }
    }, {
      key: "last",
      get: function get() {
        return this._last - 1;
      }
    }, {
      key: "tabIndexElements",
      get: function get() {
        var result = Array.from($('[tabindex]')); // Sort by tabIndex

        result.sort(function (a, b) {
          return a.tabIndex - b.tabIndex;
        });
        return result;
      }
    }]);

    return TabIndexManager;
  }())();

  paella.URL = /*#__PURE__*/function () {
    function PaellaURL(urlText) {
      _classCallCheck(this, PaellaURL);

      this._urlText = urlText;
    }

    _createClass(PaellaURL, [{
      key: "appendPath",
      value: function appendPath(text) {
        if (this._urlText.endsWith("/") && text.startsWith("/")) {
          this._urlText += text.substring(1, text.length);
        } else if (this._urlText.endsWith("/") || text.startsWith("/")) {
          this._urlText += text;
        } else {
          this._urlText += "/" + text;
        }

        return this;
      }
    }, {
      key: "text",
      get: function get() {
        return this._urlText;
      }
    }, {
      key: "isAbsolute",
      get: function get() {
        return new RegExp('^([a-z]+://|//)', 'i').test(this._urlText) || /^\//.test(this._urlText); // We consider that the URLs starting with / are absolute and local to this server
      }
    }, {
      key: "isExternal",
      get: function get() {
        var thisUrl = new URL(this.absoluteUrl);
        var localUrl = new URL(location.href);
        return thisUrl.hostname != localUrl.hostname;
      }
    }, {
      key: "absoluteUrl",
      get: function get() {
        var result = "";

        if (new RegExp('^([a-z]+://|//)', 'i').test(this._urlText)) {
          result = this._urlText;
        } else if (/^\//.test(this._urlText)) {
          result = "".concat(location.origin).concat(this._urlText);
        } else {
          var pathname = location.pathname;

          if (pathname.lastIndexOf(".") > pathname.lastIndexOf("/")) {
            pathname = pathname.substring(0, pathname.lastIndexOf("/")) + '/';
          }

          result = "".concat(location.origin).concat(pathname).concat(this._urlText);
        }

        result = new URL(result).href;
        return result;
      }
    }]);

    return PaellaURL;
  }();

  var Log = /*#__PURE__*/function () {
    function Log() {
      _classCallCheck(this, Log);

      this._currentLevel = 0;
      var logLevelParam = paella.utils.parameters.get("logLevel");
      logLevelParam = logLevelParam ? logLevelParam : paella.utils.hashParams.get("logLevel");
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

    _createClass(Log, [{
      key: "logMessage",
      value: function logMessage(level, message) {
        var prefix = "";

        if (typeof level == "string") {
          message = level;
        } else if (level >= paella.log.kLevelError && level <= paella.log.kLevelLog) {
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

        if (this._currentLevel >= level && console.log) {
          console.log(prefix + message);
        }
      }
    }, {
      key: "error",
      value: function error(message) {
        this.logMessage(paella.log.kLevelError, message);
      }
    }, {
      key: "warning",
      value: function warning(message) {
        this.logMessage(paella.log.kLevelWarning, message);
      }
    }, {
      key: "debug",
      value: function debug(message) {
        this.logMessage(paella.log.kLevelDebug, message);
      }
    }, {
      key: "log",
      value: function log(message) {
        this.logMessage(paella.log.kLevelLog, message);
      }
    }, {
      key: "setLevel",
      value: function setLevel(level) {
        this._currentLevel = level;
      }
    }]);

    return Log;
  }();

  Log.kLevelError = 1;
  Log.kLevelWarning = 2;
  Log.kLevelDebug = 3;
  Log.kLevelLog = 4;
  paella.log = new Log();
})();

paella.AntiXSS = {
  htmlEscape: function htmlEscape(str) {
    return String(str).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/'/g, '&#39;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  },
  htmlUnescape: function htmlUnescape(value) {
    return String(value).replace(/&quot;/g, '"').replace(/&#39;/g, "'").replace(/&lt;/g, '<').replace(/&gt;/g, '>').replace(/&amp;/g, '&');
  }
};

function paella_DeferredResolved(param) {
  return new Promise(function (resolve) {
    resolve(param);
  });
}

function paella_DeferredRejected(param) {
  return new Promise(function (resolve, reject) {
    reject(param);
  });
}

function paella_DeferredNotImplemented() {
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


(function () {
  var Node = /*#__PURE__*/function () {
    _createClass(Node, [{
      key: "identifier",
      get: function get() {
        return this._identifier;
      },
      set: function set(id) {
        this._identifier = id;
      }
    }, {
      key: "nodeList",
      get: function get() {
        return this._nodeList;
      }
    }, {
      key: "parent",
      get: function get() {
        return this._parent;
      },
      set: function set(p) {
        this._parent = p;
      }
    }]);

    function Node(id) {
      _classCallCheck(this, Node);

      this._nodeList = {};
      this.identifier = id;
    }

    _createClass(Node, [{
      key: "addTo",
      value: function addTo(parentNode) {
        parentNode.addNode(this);
      }
    }, {
      key: "addNode",
      value: function addNode(childNode) {
        childNode.parent = this;
        this.nodeList[childNode.identifier] = childNode;
        return childNode;
      }
    }, {
      key: "getNode",
      value: function getNode(id) {
        return this.nodeList[id];
      }
    }, {
      key: "removeNode",
      value: function removeNode(childNode) {
        if (this.nodeList[childNode.identifier]) {
          delete this.nodeList[childNode.identifier];
          return true;
        }

        return false;
      }
    }]);

    return Node;
  }();

  paella.Node = Node;

  var DomNode = /*#__PURE__*/function (_paella$Node) {
    _inherits(DomNode, _paella$Node);

    var _super4 = _createSuper(DomNode);

    _createClass(DomNode, [{
      key: "domElement",
      get: function get() {
        return this._domElement;
      }
    }, {
      key: "domElementType",
      get: function get() {
        return this._elementType;
      },
      set: function set(newType) {
        this._elementType = newType;
        var oldElement = this._domElement;
        var parent = oldElement.parentNode;
        var newElement = document.createElement(newType);
        parent.removeChild(oldElement);
        parent.appendChild(newElement);
        this._domElement = newElement;
        newElement.innerHTML = oldElement.innerHTML;

        for (var i = 0; i < oldElement.attributes.length; ++i) {
          var attr = oldElement.attributes[i];
          newElement.setAttribute(attr.name, attr.value);
        }
      }
    }]);

    function DomNode(elementType, id, style) {
      var _this7;

      _classCallCheck(this, DomNode);

      _this7 = _super4.call(this, id);
      _this7._elementType = elementType;
      _this7._domElement = document.createElement(elementType);
      _this7.domElement.id = id;
      if (style) _this7.style = style;
      return _this7;
    }

    _createClass(DomNode, [{
      key: "addNode",
      value: function addNode(childNode) {
        var returnValue = _get(_getPrototypeOf(DomNode.prototype), "addNode", this).call(this, childNode);

        this.domElement.appendChild(childNode.domElement);
        return returnValue;
      }
    }, {
      key: "onresize",
      value: function onresize() {}
    }, {
      key: "removeNode",
      value: function removeNode(childNode) {
        if (_get(_getPrototypeOf(DomNode.prototype), "removeNode", this).call(this, childNode)) {
          this.domElement.removeChild(childNode.domElement);
        }
      }
    }, {
      key: "style",
      set: function set(s) {
        $(this.domElement).css(s);
      }
    }]);

    return DomNode;
  }(paella.Node);

  paella.DomNode = DomNode;

  var Button = /*#__PURE__*/function (_paella$DomNode) {
    _inherits(Button, _paella$DomNode);

    var _super5 = _createSuper(Button);

    _createClass(Button, [{
      key: "isToggle",
      get: function get() {
        return this._isToggle;
      },
      set: function set(t) {
        this._isToggle = t;
      }
    }]);

    function Button(id, className, action, isToggle) {
      var _this8;

      _classCallCheck(this, Button);

      var style = {};
      _this8 = _super5.call(this, 'div', id, style);
      _this8.isToggle = isToggle;
      _this8.domElement.className = className;

      if (isToggle) {
        $(_this8.domElement).click(function (event) {
          _this8.toggleIcon();
        });
      }

      $(_this8.domElement).click('click', action);
      return _this8;
    }

    _createClass(Button, [{
      key: "isToggled",
      value: function isToggled() {
        if (this.isToggle) {
          var element = this.domElement;
          return /([a-zA-Z0-9_]+)_active/.test(element.className);
        } else {
          return false;
        }
      }
    }, {
      key: "toggle",
      value: function toggle() {
        this.toggleIcon();
      }
    }, {
      key: "toggleIcon",
      value: function toggleIcon() {
        var element = this.domElement;

        if (/([a-zA-Z0-9_]+)_active/.test(element.className)) {
          element.className = RegExp.$1;
        } else {
          element.className = element.className + '_active';
        }
      }
    }, {
      key: "show",
      value: function show() {
        $(this.domElement).show();
      }
    }, {
      key: "hide",
      value: function hide() {
        $(this.domElement).hide();
      }
    }, {
      key: "visible",
      value: function visible() {
        return this.domElement.visible();
      }
    }]);

    return Button;
  }(paella.DomNode);

  paella.Button = Button;
})();

(function () {
  var g_profiles = [];

  paella.addProfile = function (cb) {
    cb().then(function (profileData) {
      if (profileData) {
        g_profiles.push(profileData);

        if (typeof profileData.onApply != "function") {
          profileData.onApply = function () {};
        }

        if (typeof profileData.onDeactivte != "function") {
          profileData.onDeactivate = function () {};
        }

        paella.events.trigger(paella.events.profileListChanged, {
          profileData: profileData
        });
      }
    });
  }; // Utility functions


  function hideBackground() {
    var bkgNode = this.container.getNode("videoContainerBackground");
    if (bkgNode) this.container.removeNode(bkgNode);
  }

  function showBackground(bkgData) {
    if (!bkgData) return;
    hideBackground.apply(this);
    this.backgroundData = bkgData;
    var style = {
      backgroundImage: "url(".concat(paella.baseUrl).concat(paella.utils.folders.get("resources"), "/style/").concat(bkgData.content, ")"),
      backgroundSize: "100% 100%",
      zIndex: bkgData.layer,
      position: 'absolute',
      left: bkgData.rect.left + "px",
      right: bkgData.rect.right + "px",
      width: "100%",
      height: "100%"
    };
    this.container.addNode(new paella.DomNode('div', "videoContainerBackground", style));
  }

  function hideAllLogos() {
    if (this.logos == undefined) return;

    for (var i = 0; i < this.logos.length; ++i) {
      var logoId = this.logos[i].content.replace(/\./ig, "-");
      var logo = this.container.getNode(logoId);
      $(logo.domElement).hide();
    }
  }

  function showLogos(logos) {
    this.logos = logos;
    var relativeSize = new paella.RelativeVideoSize();

    for (var i = 0; i < logos.length; ++i) {
      var logo = logos[i];
      var logoId = logo.content.replace(/\./ig, "-");
      var logoNode = this.container.getNode(logoId);
      var rect = logo.rect;

      if (!logoNode) {
        style = {};
        logoNode = this.container.addNode(new paella.DomNode('img', logoId, style));
        logoNode.domElement.setAttribute('src', "".concat(paella.baseUrl).concat(paella.utils.folders.get("resources"), "/style/").concat(logo.content));
      } else {
        $(logoNode.domElement).show();
      }

      var percentTop = Number(relativeSize.percentVSize(rect.top)) + '%';
      var percentLeft = Number(relativeSize.percentWSize(rect.left)) + '%';
      var percentWidth = Number(relativeSize.percentWSize(rect.width)) + '%';
      var percentHeight = Number(relativeSize.percentVSize(rect.height)) + '%';
      var style = {
        top: percentTop,
        left: percentLeft,
        width: percentWidth,
        height: percentHeight,
        position: 'absolute',
        zIndex: logo.zIndex
      };
      $(logoNode.domElement).css(style);
    }
  }

  function hideButtons() {
    var _this9 = this;

    if (this.buttons) {
      this.buttons.forEach(function (btn) {
        _this9.container.removeNode(_this9.container.getNode(btn.id));
      });
      this.buttons = null;
    }
  }

  function showButtons(buttons, profileData) {
    var _this10 = this;

    hideButtons.apply(this);

    if (buttons) {
      var relativeSize = new paella.RelativeVideoSize();
      this.buttons = buttons;
      buttons.forEach(function (btn, index) {
        btn.id = "button_" + index;
        var rect = btn.rect;
        var percentTop = relativeSize.percentVSize(rect.top) + '%';
        var percentLeft = relativeSize.percentWSize(rect.left) + '%';
        var percentWidth = relativeSize.percentWSize(rect.width) + '%';
        var percentHeight = relativeSize.percentVSize(rect.height) + '%';
        var url = paella.baseUrl;
        url = url.replace(/\\/ig, '/');
        var style = {
          top: percentTop,
          left: percentLeft,
          width: percentWidth,
          height: percentHeight,
          position: 'absolute',
          zIndex: btn.layer,
          backgroundImage: "url(".concat(paella.baseUrl).concat(paella.utils.folders.get("resources"), "/style/").concat(btn.icon, ")"),
          backgroundSize: '100% 100%',
          display: 'block'
        };

        var logoNode = _this10.container.addNode(new paella.DomNode('div', btn.id, style));

        logoNode.domElement.className = "paella-profile-button";
        logoNode.domElement.data = {
          action: btn.onClick,
          profileData: profileData
        };
        $(logoNode.domElement).click(function (evt) {
          this.data.action.apply(this.data.profileData, [evt]);
          evt.stopPropagation();
          return false;
        });
      });
    }
  }

  function getClosestRect(profileData, videoDimensions) {
    var minDiff = 10;
    var re = /([0-9\.]+)\/([0-9\.]+)/;
    var result = profileData.rect[0];
    var videoAspectRatio = videoDimensions.h == 0 ? 1.777777 : videoDimensions.w / videoDimensions.h;
    var profileAspectRatio = 1;
    var reResult = false;
    profileData.rect.forEach(function (rect) {
      if (reResult = re.exec(rect.aspectRatio)) {
        profileAspectRatio = Number(reResult[1]) / Number(reResult[2]);
      }

      var diff = Math.abs(profileAspectRatio - videoAspectRatio);

      if (minDiff > diff) {
        minDiff = diff;
        result = rect;
      }
    });
    return result;
  }

  function applyProfileWithJson(profileData, animate) {
    var _this11 = this;

    if (animate == undefined) animate = true;
    if (!profileData) return;

    var getProfile = function getProfile(content) {
      var result = null;
      profileData && profileData.videos.some(function (videoProfile) {
        if (videoProfile.content == content) {
          result = videoProfile;
        }

        return result != null;
      });
      return result;
    };

    var applyVideoRect = function applyVideoRect(profile, videoData, videoWrapper, player) {
      var frameStrategy = _this11.profileFrameStrategy;

      if (frameStrategy) {
        var rect = getClosestRect(profile, videoData.res);
        var videoSize = videoData.res;
        var containerSize = {
          width: $(_this11.domElement).width(),
          height: $(_this11.domElement).height()
        };
        var scaleFactor = rect.width / containerSize.width;
        var scaledVideoSize = {
          width: videoSize.w * scaleFactor,
          height: videoSize.h * scaleFactor
        };
        rect.left = Number(rect.left);
        rect.top = Number(rect.top);
        rect.width = Number(rect.width);
        rect.height = Number(rect.height);
        rect = frameStrategy.adaptFrame(scaledVideoSize, rect);
        var visible = /true/i.test(profile.visible);
        rect.visible = visible;
        var layer = parseInt(profile.layer);
        videoWrapper.domElement.style.zIndex = layer;
        videoWrapper.setRect(rect, animate);
        videoWrapper.setVisible(visible, animate); // The disable/enable functions may not be called on main audio player

        var isMainAudioPlayer = paella.player.videoContainer.streamProvider.mainAudioPlayer == player;
        visible ? player.enable(isMainAudioPlayer) : player.disable(isMainAudioPlayer);
      }
    };

    profileData && profileData.onApply();
    hideAllLogos.apply(this);
    profileData && showLogos.apply(this, [profileData.logos]);
    hideBackground.apply(this);
    profileData && showBackground.apply(this, [profileData.background]);
    hideButtons.apply(this);
    profileData && showButtons.apply(this, [profileData.buttons, profileData]);
    this.streamProvider.videoStreams.forEach(function (streamData, index) {
      var profile = getProfile(streamData.content);
      var player = _this11.streamProvider.videoPlayers[index];
      var videoWrapper = _this11.videoWrappers[index];

      if (profile) {
        player.getVideoData().then(function (data) {
          applyVideoRect(profile, data, videoWrapper, player);
        });
      } else if (videoWrapper) {
        videoWrapper.setVisible(false, animate);
        player.disable(paella.player.videoContainer.streamProvider.mainAudioPlayer == player);
      }
    });
  }

  var profileReloadCount = 0;
  var maxProfileReloadCunt = 20;

  var Profiles = /*#__PURE__*/function () {
    function Profiles() {
      var _this12 = this;

      _classCallCheck(this, Profiles);

      paella.events.bind(paella.events.controlBarDidHide, function () {
        return _this12.hideButtons();
      });
      paella.events.bind(paella.events.controlBarDidShow, function () {
        return _this12.showButtons();
      });
      paella.events.bind(paella.events.profileListChanged, function () {
        if (paella.player && paella.player.videoContainer && (!_this12.currentProfile || _this12.currentProfileName != _this12.currentProfile.id)) {
          _this12.setProfile(_this12.currentProfileName, false);
        }
      });
    }

    _createClass(Profiles, [{
      key: "getDefaultProfile",
      value: function getDefaultProfile() {
        if (paella.player.videoContainer.masterVideo() && paella.player.videoContainer.masterVideo().defaultProfile()) {
          return paella.player.videoContainer.masterVideo().defaultProfile();
        }

        if (paella.player && paella.player.config && paella.player.config.defaultProfile) {
          return paella.player.config.defaultProfile;
        }

        return undefined;
      }
    }, {
      key: "loadProfile",
      value: function loadProfile(profileId) {
        var result = null;
        g_profiles.some(function (profile) {
          if (profile.id == profileId) {
            result = profile;
          }

          return result;
        });
        return result;
      }
    }, {
      key: "setProfile",
      value: function setProfile(profileName, animate) {
        var _this13 = this;

        if (!profileName) {
          return false;
        }

        animate = paella.utils.userAgent.browser.Explorer ? false : animate;

        if (this.currentProfile) {
          this.currentProfile.onDeactivate();
        }

        if (!paella.player.videoContainer.ready) {
          return false; // Nothing to do, the video is not loaded
        } else {
          var profileData = this.loadProfile(profileName) || g_profiles.length > 0 && g_profiles[0];

          if (!profileData && g_profiles.length == 0) {
            if (profileReloadCount < maxProfileReloadCunt) {
              profileReloadCount++; // Try to load the profile again later, maybe the profiles are not loaded yet

              setTimeout(function () {
                _this13.setProfile(profileName, animate);
              }, 100);
              return false;
            } else {
              console.error("No valid video layout profiles were found. Check that the 'content' attribute setting in 'videoSets', at config.json file, matches the 'content' property in the video manifest.");
              return false;
            }
          } else {
            this._currentProfileName = profileName;
            applyProfileWithJson.apply(paella.player.videoContainer, [profileData, animate]);
            return true;
          }
        }
      }
    }, {
      key: "getProfile",
      value: function getProfile(profileName) {
        var result = null;
        this.profileList.some(function (p) {
          if (p.id == profileName) {
            result = p;
            return true;
          }
        });
        return result;
      }
    }, {
      key: "placeVideos",
      value: function placeVideos() {
        this.setProfile(this._currentProfileName, false);
      }
    }, {
      key: "hideButtons",
      value: function hideButtons() {
        $('.paella-profile-button').hide();
      }
    }, {
      key: "showButtons",
      value: function showButtons() {
        $('.paella-profile-button').show();
      }
    }, {
      key: "profileList",
      get: function get() {
        return g_profiles;
      }
    }, {
      key: "currentProfile",
      get: function get() {
        return this.getProfile(this._currentProfileName);
      }
    }, {
      key: "currentProfileName",
      get: function get() {
        return this._currentProfileName;
      }
    }]);

    return Profiles;
  }();

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


(function () {
  var VideoQualityStrategy = /*#__PURE__*/function () {
    function VideoQualityStrategy() {
      _classCallCheck(this, VideoQualityStrategy);
    }

    _createClass(VideoQualityStrategy, [{
      key: "getParams",
      value: function getParams() {
        return paella.player.config.player.videoQualityStrategyParams || {};
      }
    }, {
      key: "getQualityIndex",
      value: function getQualityIndex(source) {
        if (source.length > 0) {
          return source[source.length - 1];
        } else {
          return source;
        }
      }
    }], [{
      key: "Factory",
      value: function Factory() {
        var config = paella.player.config;

        try {
          var strategyClass = config.player.videoQualityStrategy;
          var ClassObject = paella.utils.objectFromString(strategyClass);
          var strategy = new ClassObject();

          if (strategy instanceof paella.VideoQualityStrategy) {
            return strategy;
          }
        } catch (e) {}

        return null;
      }
    }]);

    return VideoQualityStrategy;
  }();

  paella.VideoQualityStrategy = VideoQualityStrategy;

  var BestFitVideoQualityStrategy = /*#__PURE__*/function (_paella$VideoQualityS) {
    _inherits(BestFitVideoQualityStrategy, _paella$VideoQualityS);

    var _super6 = _createSuper(BestFitVideoQualityStrategy);

    function BestFitVideoQualityStrategy() {
      _classCallCheck(this, BestFitVideoQualityStrategy);

      return _super6.apply(this, arguments);
    }

    _createClass(BestFitVideoQualityStrategy, [{
      key: "getQualityIndex",
      value: function getQualityIndex(source) {
        var index = source.length - 1;

        if (source.length > 0) {
          var selected = source[0];
          var win_w = $(window).width();
          var win_h = $(window).height();
          var win_res = win_w * win_h;

          if (selected.res && selected.res.w && selected.res.h) {
            var selected_res = parseInt(selected.res.w) * parseInt(selected.res.h);
            var selected_diff = Math.abs(win_res - selected_res);

            for (var i = 0; i < source.length; ++i) {
              var res = source[i].res;

              if (res) {
                var m_res = parseInt(source[i].res.w) * parseInt(source[i].res.h);
                var m_diff = Math.abs(win_res - m_res);

                if (m_diff <= selected_diff) {
                  selected_diff = m_diff;
                  index = i;
                }
              }
            }
          }
        }

        return index;
      }
    }]);

    return BestFitVideoQualityStrategy;
  }(paella.VideoQualityStrategy);

  paella.BestFitVideoQualityStrategy = BestFitVideoQualityStrategy;

  var LimitedBestFitVideoQualityStrategy = /*#__PURE__*/function (_paella$VideoQualityS2) {
    _inherits(LimitedBestFitVideoQualityStrategy, _paella$VideoQualityS2);

    var _super7 = _createSuper(LimitedBestFitVideoQualityStrategy);

    function LimitedBestFitVideoQualityStrategy() {
      _classCallCheck(this, LimitedBestFitVideoQualityStrategy);

      return _super7.apply(this, arguments);
    }

    _createClass(LimitedBestFitVideoQualityStrategy, [{
      key: "getQualityIndex",
      value: function getQualityIndex(source) {
        var index = source.length - 1;
        var params = this.getParams();

        if (source.length > 0) {
          //var selected = source[0];
          var selected = null;
          var win_h = $(window).height();
          var maxRes = params.maxAutoQualityRes || 720;
          var diff = Number.MAX_VALUE;
          source.forEach(function (item, i) {
            if (item.res && item.res.h <= maxRes) {
              var itemDiff = Math.abs(win_h - item.res.h);

              if (itemDiff < diff) {
                selected = item;
                index = i;
              }
            }
          });
        }

        return index;
      }
    }]);

    return LimitedBestFitVideoQualityStrategy;
  }(paella.VideoQualityStrategy);

  paella.LimitedBestFitVideoQualityStrategy = LimitedBestFitVideoQualityStrategy;

  var VideoFactory = /*#__PURE__*/function () {
    function VideoFactory() {
      _classCallCheck(this, VideoFactory);
    }

    _createClass(VideoFactory, [{
      key: "isStreamCompatible",
      value: function isStreamCompatible(streamData) {
        return false;
      }
    }, {
      key: "getVideoObject",
      value: function getVideoObject(id, streamData, rect) {
        return null;
      }
    }]);

    return VideoFactory;
  }();

  paella.VideoFactory = VideoFactory;
  paella.videoFactories = paella.videoFactories || {};
  paella.videoFactory = {
    _factoryList: [],
    initFactories: function initFactories() {
      if (paella.videoFactories) {
        var This = this;
        paella.player.config.player.methods.forEach(function (method) {
          if (method.enabled && paella.videoFactories[method.factory]) {
            This.registerFactory(new paella.videoFactories[method.factory]());
          }
        });
        this.registerFactory(new paella.videoFactories.EmptyVideoFactory());
      }
    },
    getVideoObject: function getVideoObject(id, streamData, rect) {
      if (this._factoryList.length == 0) {
        this.initFactories();
      }

      var selectedFactory = null;

      if (this._factoryList.some(function (factory) {
        if (factory.isStreamCompatible(streamData)) {
          selectedFactory = factory;
          return true;
        }
      })) {
        return selectedFactory.getVideoObject(id, streamData, rect);
      }

      return null;
    },
    registerFactory: function registerFactory(factory) {
      this._factoryList.push(factory);
    }
  };
})();

(function () {
  var AudioElementBase = /*#__PURE__*/function (_paella$DomNode2) {
    _inherits(AudioElementBase, _paella$DomNode2);

    var _super8 = _createSuper(AudioElementBase);

    function AudioElementBase(id, stream) {
      var _this14;

      _classCallCheck(this, AudioElementBase);

      _this14 = _super8.call(this, 'div', id);
      _this14._stream = stream;
      _this14._ready = false;
      return _this14;
    }

    _createClass(AudioElementBase, [{
      key: "setAutoplay",
      value: function setAutoplay() {
        return Promise.reject(new Error("no such compatible video player"));
      }
    }, {
      key: "load",
      value: function load() {
        return Promise.reject(new Error("no such compatible video player"));
      }
    }, {
      key: "play",
      value: function play() {
        return Promise.reject(new Error("no such compatible video player"));
      }
    }, {
      key: "pause",
      value: function pause() {
        return Promise.reject(new Error("no such compatible video player"));
      }
    }, {
      key: "isPaused",
      value: function isPaused() {
        return Promise.reject(new Error("no such compatible video player"));
      }
    }, {
      key: "duration",
      value: function duration() {
        return Promise.reject(new Error("no such compatible video player"));
      }
    }, {
      key: "setCurrentTime",
      value: function setCurrentTime(time) {
        return Promise.reject(new Error("no such compatible video player"));
      }
    }, {
      key: "currentTime",
      value: function currentTime() {
        return Promise.reject(new Error("no such compatible video player"));
      }
    }, {
      key: "setVolume",
      value: function setVolume(volume) {
        return Promise.reject(new Error("no such compatible video player"));
      }
    }, {
      key: "volume",
      value: function volume() {
        return Promise.reject(new Error("no such compatible video player"));
      }
    }, {
      key: "setPlaybackRate",
      value: function setPlaybackRate(rate) {
        return Promise.reject(new Error("no such compatible video player"));
      }
    }, {
      key: "playbackRate",
      value: function playbackRate() {
        return Promise.reject(new Error("no such compatible video player"));
      }
    }, {
      key: "unload",
      value: function unload() {
        return Promise.reject(new Error("no such compatible video player"));
      }
    }, {
      key: "getQualities",
      value: function getQualities() {
        return Promise.resolve([{
          index: 0,
          res: {
            w: 0,
            h: 1
          },
          src: "",
          toString: function toString() {
            return "";
          },
          shortLabel: function shortLabel() {
            return "";
          },
          compare: function compare() {
            return 0;
          }
        }]);
      }
    }, {
      key: "getCurrentQuality",
      value: function getCurrentQuality() {
        return Promise.resolve(0);
      }
    }, {
      key: "defaultProfile",
      value: function defaultProfile() {
        return null;
      }
    }, {
      key: "supportAutoplay",
      value: function supportAutoplay() {
        return false;
      }
    }, {
      key: "ready",
      get: function get() {
        return this._ready;
      }
    }, {
      key: "currentTimeSync",
      get: function get() {
        return null;
      }
    }, {
      key: "volumeSync",
      get: function get() {
        return null;
      }
    }, {
      key: "pausedSync",
      get: function get() {
        return null;
      }
    }, {
      key: "durationSync",
      get: function get() {
        return null;
      }
    }, {
      key: "stream",
      get: function get() {
        return this._stream;
      }
    }]);

    return AudioElementBase;
  }(paella.DomNode);

  ;
  paella.AudioElementBase = AudioElementBase;
  paella.audioFactories = {};

  var AudioFactory = /*#__PURE__*/function () {
    function AudioFactory() {
      _classCallCheck(this, AudioFactory);
    }

    _createClass(AudioFactory, [{
      key: "isStreamCompatible",
      value: function isStreamCompatible(streamData) {
        return false;
      }
    }, {
      key: "getAudioObject",
      value: function getAudioObject(id, streamData) {
        return null;
      }
    }]);

    return AudioFactory;
  }();

  paella.AudioFactory = AudioFactory;
  paella.audioFactory = {
    _factoryList: [],
    initFactories: function initFactories() {
      if (paella.audioFactories) {
        var This = this;
        paella.player.config.player.audioMethods = paella.player.config.player.audioMethods || {};
        paella.player.config.player.audioMethods.forEach(function (method) {
          if (method.enabled) {
            This.registerFactory(new paella.audioFactories[method.factory]());
          }
        });
      }
    },
    getAudioObject: function getAudioObject(id, streamData) {
      if (this._factoryList.length == 0) {
        this.initFactories();
      }

      var selectedFactory = null;

      if (this._factoryList.some(function (factory) {
        if (factory.isStreamCompatible(streamData)) {
          selectedFactory = factory;
          return true;
        }
      })) {
        return selectedFactory.getAudioObject(id, streamData);
      }

      return null;
    },
    registerFactory: function registerFactory(factory) {
      this._factoryList.push(factory);
    }
  };
})();

(function () {
  function checkReady(cb) {
    var This = this;
    return new Promise(function (resolve, reject) {
      if (This._ready) {
        resolve(typeof cb == 'function' ? cb() : true);
      } else {
        var doCheck = function doCheck() {
          if (This.audio.readyState >= This.audio.HAVE_CURRENT_DATA) {
            This._ready = true;
            resolve(typeof cb == 'function' ? cb() : true);
          } else {
            setTimeout(doCheck, 50);
          }
        };

        doCheck();
      }
    });
  }

  var MultiformatAudioElement = /*#__PURE__*/function (_paella$AudioElementB) {
    _inherits(MultiformatAudioElement, _paella$AudioElementB);

    var _super9 = _createSuper(MultiformatAudioElement);

    function MultiformatAudioElement(id, stream) {
      var _this15;

      _classCallCheck(this, MultiformatAudioElement);

      _this15 = _super9.call(this, id, stream);
      _this15._streamName = "audio";
      _this15._audio = document.createElement('audio');

      _this15.domElement.appendChild(_this15._audio);

      return _this15;
    }

    _createClass(MultiformatAudioElement, [{
      key: "setAutoplay",
      value: function setAutoplay(ap) {
        this.audio.autoplay = ap;
      }
    }, {
      key: "load",
      value: function load() {
        var This = this;
        var sources = this._stream.sources[this._streamName];
        var stream = sources.length > 0 ? sources[0] : null;
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
          return checkReady.apply(this, [function () {
            return stream;
          }]);
        } else {
          return Promise.reject(new Error("Could not load video: invalid quality stream index"));
        }
      }
    }, {
      key: "play",
      value: function play() {
        var _this16 = this;

        return checkReady.apply(this, [function () {
          _this16.audio.play();
        }]);
      }
    }, {
      key: "pause",
      value: function pause() {
        var _this17 = this;

        return checkReady.apply(this, [function () {
          _this17.audio.pause();
        }]);
      }
    }, {
      key: "isPaused",
      value: function isPaused() {
        var _this18 = this;

        return checkReady.apply(this, [function () {
          return _this18.audio.paused;
        }]);
      }
    }, {
      key: "duration",
      value: function duration() {
        var _this19 = this;

        return checkReady.apply(this, [function () {
          return _this19.audio.duration;
        }]);
      }
    }, {
      key: "setCurrentTime",
      value: function setCurrentTime(time) {
        var _this20 = this;

        return checkReady.apply(this, [function () {
          _this20.audio.currentTime = time;
        }]);
      }
    }, {
      key: "currentTime",
      value: function currentTime() {
        var _this21 = this;

        return checkReady.apply(this, [function () {
          return _this21.audio.currentTime;
        }]);
      }
    }, {
      key: "setVolume",
      value: function setVolume(volume) {
        var _this22 = this;

        return checkReady.apply(this, [function () {
          return _this22.audio.volume = volume;
        }]);
      }
    }, {
      key: "volume",
      value: function volume() {
        var _this23 = this;

        return checkReady.apply(this, [function () {
          return _this23.audio.volume;
        }]);
      }
    }, {
      key: "setPlaybackRate",
      value: function setPlaybackRate(rate) {
        var _this24 = this;

        return checkReady.apply(this, [function () {
          _this24.audio.playbackRate = rate;
        }]);
      }
    }, {
      key: "playbackRate",
      value: function playbackRate() {
        var _this25 = this;

        return checkReady.apply(this, [function () {
          return _this25.audio.playbackRate;
        }]);
      }
    }, {
      key: "unload",
      value: function unload() {
        return Promise.resolve();
      }
    }, {
      key: "buffered",
      get: function get() {
        return this.audio && this.audio.buffered;
      }
    }, {
      key: "audio",
      get: function get() {
        return this._audio;
      }
    }, {
      key: "currentTimeSync",
      get: function get() {
        return this.ready ? this.audio.currentTimeSync : null;
      }
    }, {
      key: "volumeSync",
      get: function get() {
        return this.ready ? this.audio.volumeSync : null;
      }
    }, {
      key: "pausedSync",
      get: function get() {
        return this.ready ? this.audio.pausedSync : null;
      }
    }, {
      key: "durationSync",
      get: function get() {
        return this.ready ? this.audio.durationSync : null;
      }
    }]);

    return MultiformatAudioElement;
  }(paella.AudioElementBase);

  ;
  paella.MultiformatAudioElement = MultiformatAudioElement;

  var MultiformatAudioFactory = /*#__PURE__*/function () {
    function MultiformatAudioFactory() {
      _classCallCheck(this, MultiformatAudioFactory);
    }

    _createClass(MultiformatAudioFactory, [{
      key: "isStreamCompatible",
      value: function isStreamCompatible(streamData) {
        return true;
      }
    }, {
      key: "getAudioObject",
      value: function getAudioObject(id, streamData) {
        return new paella.MultiformatAudioElement(id, streamData);
      }
    }]);

    return MultiformatAudioFactory;
  }();

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


(function () {
  paella.Profiles = {
    profileList: null,
    getDefaultProfile: function getDefaultProfile() {
      if (paella.player.videoContainer.masterVideo() && paella.player.videoContainer.masterVideo().defaultProfile()) {
        return paella.player.videoContainer.masterVideo().defaultProfile();
      }

      if (paella.player && paella.player.config && paella.player.config.defaultProfile) {
        return paella.player.config.defaultProfile;
      }

      return undefined;
    },
    loadProfile: function loadProfile(profileName, onSuccessFunction) {
      var defaultProfile = this.getDefaultProfile();
      this.loadProfileList(function (data) {
        var profileData;

        if (data[profileName]) {
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
    loadProfileList: function loadProfileList(onSuccessFunction) {
      var thisClass = this;

      if (this.profileList == null) {
        var params = {
          url: paella.utils.folders.profiles() + "/profiles.json"
        };
        paella.utils.ajax.get(params, function (data, mimetype, code) {
          if (typeof data == "string") {
            data = JSON.parse(data);
          }

          thisClass.profileList = data;
          onSuccessFunction(thisClass.profileList);
        }, function (data, mimetype, code) {
          paella.log.debug("Error loading video profiles. Check your Paella Player configuration");
        });
      } else {
        onSuccessFunction(thisClass.profileList);
      }
    }
  };

  var RelativeVideoSize = /*#__PURE__*/function () {
    function RelativeVideoSize() {
      _classCallCheck(this, RelativeVideoSize);
    }

    _createClass(RelativeVideoSize, [{
      key: "proportionalHeight",
      value: function proportionalHeight(newWidth) {
        return Math.floor(this.h * newWidth / this.w);
      }
    }, {
      key: "proportionalWidth",
      value: function proportionalWidth(newHeight) {
        return Math.floor(this.w * newHeight / this.h);
      }
    }, {
      key: "percentVSize",
      value: function percentVSize(pxSize) {
        return pxSize * 100 / this.h;
      }
    }, {
      key: "percentWSize",
      value: function percentWSize(pxSize) {
        return pxSize * 100 / this.w;
      }
    }, {
      key: "aspectRatio",
      value: function aspectRatio() {
        return this.w / this.h;
      }
    }, {
      key: "w",
      get: function get() {
        return this._w || 1280;
      },
      set: function set(v) {
        this._w = v;
      }
    }, {
      key: "h",
      get: function get() {
        return this._h || 720;
      },
      set: function set(v) {
        this._h = v;
      }
    }]);

    return RelativeVideoSize;
  }();

  paella.RelativeVideoSize = RelativeVideoSize;

  var VideoRect = /*#__PURE__*/function (_paella$DomNode3) {
    _inherits(VideoRect, _paella$DomNode3);

    var _super10 = _createSuper(VideoRect);

    function VideoRect(id, domType, left, top, width, height) {
      var _this26;

      _classCallCheck(this, VideoRect);

      _this26 = _super10.call(this, domType, id, {});
      var zoomSettings = paella.player.config.player.videoZoom || {};

      var zoomEnabled = (zoomSettings.enabled !== undefined ? zoomSettings.enabled : true) && _this26.allowZoom();

      _this26.style = zoomEnabled ? {
        width: _this26._zoom + '%',
        height: "100%",
        position: 'absolute'
      } : {
        width: "100%",
        height: "100%"
      };
      _this26._rect = null;
      var eventCapture = document.createElement('div');
      setTimeout(function () {
        return _this26.domElement.parentElement.appendChild(eventCapture);
      }, 10);
      eventCapture.id = id + "EventCapture";
      eventCapture.style.position = "absolute";
      eventCapture.style.top = "0px";
      eventCapture.style.left = "0px";
      eventCapture.style.right = "0px";
      eventCapture.style.bottom = "0px";
      _this26.eventCapture = eventCapture;

      if (zoomEnabled) {
        var checkZoomAvailable = function checkZoomAvailable() {
          var minWindowSize = paella.player.config.player && paella.player.config.player.videoZoom && paella.player.config.player.videoZoom.minWindowSize || 500;
          var available = $(window).width() >= minWindowSize;

          if (this._zoomAvailable != available) {
            this._zoomAvailable = available;
            paella.events.trigger(paella.events.zoomAvailabilityChanged, {
              available: available
            });
          }
        };

        var mousePos = function mousePos(evt) {
          return {
            x: evt.originalEvent.offsetX,
            y: evt.originalEvent.offsetY
          };
        };

        var wheelDelta = function wheelDelta(evt) {
          var wheel = evt.originalEvent.deltaY * (paella.utils.userAgent.Firefox ? 2 : 1);
          var maxWheel = 6;
          return -Math.abs(wheel) < maxWheel ? wheel : maxWheel * Math.sign(wheel);
        };

        var touchesLength = function touchesLength(p0, p1) {
          return Math.sqrt((p1.x - p0.x) * (p1.x - p0.x) + (p1.y - p0.y) * (p1.y - p0.y));
        };

        var centerPoint = function centerPoint(p0, p1) {
          return {
            x: (p1.x - p0.x) / 2 + p0.x,
            y: (p1.y - p0.y) / 2 + p0.y
          };
        };

        var panImage = function panImage(o) {
          var center = {
            x: this._mouseCenter.x - o.x * 1.1,
            y: this._mouseCenter.y - o.y * 1.1
          };
          var videoSize = {
            w: $(this.domElement).width(),
            h: $(this.domElement).height()
          };
          var maxOffset = this._zoom - 100;
          var offset = {
            x: center.x * maxOffset / videoSize.w * (maxOffset / 100),
            y: center.y * maxOffset / videoSize.h * (maxOffset / 100)
          };

          if (offset.x > maxOffset) {
            offset.x = maxOffset;
          } else if (offset.x < 0) {
            offset.x = 0;
          } else {
            this._mouseCenter.x = center.x;
          }

          if (offset.y > maxOffset) {
            offset.y = maxOffset;
          } else if (offset.y < 0) {
            offset.y = 0;
          } else {
            this._mouseCenter.y = center.y;
          }

          $(this.domElement).css({
            left: "-" + offset.x + "%",
            top: "-" + offset.y + "%"
          });
          this._zoomOffset = {
            x: offset.x,
            y: offset.y
          };
          paella.events.trigger(paella.events.videoZoomChanged, {
            video: this
          });
        };

        var clearAltScrollMessage = function clearAltScrollMessage() {
          var animate = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : true;
          animate ? $(altScrollMessageContainer).animate({
            opacity: 0.0
          }) : $(altScrollMessageContainer).css({
            opacity: 0.0
          });
        };

        var showAltScrollMessage = function showAltScrollMessage() {
          if (altScrollMessageTimer) {
            clearTimeout(altScrollMessageTimer);
            altScrollMessageTimer = null;
          } else {
            $(altScrollMessageContainer).css({
              opacity: 1.0
            });
          }

          altScrollMessageTimer = setTimeout(function () {
            clearAltScrollMessage();
            altScrollMessageTimer = null;
          }, 500);
        };

        _this26._zoomAvailable = true;
        checkZoomAvailable.apply(_assertThisInitialized(_this26));
        $(window).resize(function () {
          checkZoomAvailable.apply(_assertThisInitialized(_this26));
        });
        _this26._zoom = 100;
        _this26._mouseCenter = {
          x: 0,
          y: 0
        };
        _this26._mouseDown = {
          x: 0,
          y: 0
        };
        _this26._zoomOffset = {
          x: 0,
          y: 0
        };
        _this26._maxZoom = zoomSettings.max || 400;
        $(_this26.domElement).css({
          width: "100%",
          height: "100%",
          left: "0%",
          top: "0%"
        });
        Object.defineProperty(_assertThisInitialized(_this26), 'zoom', {
          get: function get() {
            return this._zoom;
          }
        });
        Object.defineProperty(_assertThisInitialized(_this26), 'zoomOffset', {
          get: function get() {
            return this._zoomOffset;
          }
        });
        var touches = [];
        $(eventCapture).on('touchstart', function (evt) {
          if (!_this26.allowZoom() || !_this26._zoomAvailable) return;
          touches = [];
          var videoOffset = $(_this26.domElement).offset();

          for (var i = 0; i < evt.originalEvent.targetTouches.length; ++i) {
            var touch = evt.originalEvent.targetTouches[i];
            touches.push({
              x: touch.screenX - videoOffset.left,
              y: touch.screenY - videoOffset.top
            });
          }

          if (touches.length > 1) evt.preventDefault();
        });
        $(eventCapture).on('touchmove', function (evt) {
          if (!_this26.allowZoom() || !_this26._zoomAvailable) return;
          var curTouches = [];
          var videoOffset = $(_this26.domElement).offset();

          for (var i = 0; i < evt.originalEvent.targetTouches.length; ++i) {
            var touch = evt.originalEvent.targetTouches[i];
            curTouches.push({
              x: touch.screenX - videoOffset.left,
              y: touch.screenY - videoOffset.top
            });
          }

          if (curTouches.length > 1 && touches.length > 1) {
            var l0 = touchesLength(touches[0], touches[1]);
            var l1 = touchesLength(curTouches[0], curTouches[1]);
            var delta = l1 - l0;
            var center = centerPoint(touches[0], touches[1]);
            _this26._mouseCenter = center;
            _this26._zoom += delta;
            _this26._zoom = _this26._zoom < 100 ? 100 : _this26._zoom;
            _this26._zoom = _this26._zoom > _this26._maxZoom ? _this26._maxZoom : _this26._zoom;
            var newVideoSize = {
              w: $(_this26.domElement).width(),
              h: $(_this26.domElement).height()
            };
            var mouse = _this26._mouseCenter;
            $(_this26.domElement).css({
              width: _this26._zoom + '%',
              height: _this26._zoom + '%'
            });
            var maxOffset = _this26._zoom - 100;
            var offset = {
              x: mouse.x * maxOffset / newVideoSize.w,
              y: mouse.y * maxOffset / newVideoSize.h
            };
            offset.x = offset.x < maxOffset ? offset.x : maxOffset;
            offset.y = offset.y < maxOffset ? offset.y : maxOffset;
            $(_this26.domElement).css({
              left: "-" + offset.x + "%",
              top: "-" + offset.y + "%"
            });
            _this26._zoomOffset = {
              x: offset.x,
              y: offset.y
            };
            paella.events.trigger(paella.events.videoZoomChanged, {
              video: _assertThisInitialized(_this26)
            });
            touches = curTouches;
            evt.preventDefault();
          } else if (curTouches.length > 0) {
            var desp = {
              x: curTouches[0].x - touches[0].x,
              y: curTouches[0].y - touches[0].y
            };
            panImage.apply(_assertThisInitialized(_this26), [desp]);
            touches = curTouches;
            evt.preventDefault();
          }
        });
        $(eventCapture).on('touchend', function (evt) {
          if (!_this26.allowZoom() || !_this26._zoomAvailable) return;
          if (touches.length > 1) evt.preventDefault();
        });

        _this26.zoomIn = function () {
          if (_this26._zoom >= _this26._maxZoom || !_this26._zoomAvailable) return;

          if (!_this26._mouseCenter) {
            _this26._mouseCenter = {
              x: $(_this26.domElement).width() / 2,
              y: $(_this26.domElement).height() / 2
            };
          }

          _this26._zoom += 25;
          _this26._zoom = _this26._zoom < 100 ? 100 : _this26._zoom;
          _this26._zoom = _this26._zoom > _this26._maxZoom ? _this26._maxZoom : _this26._zoom;
          var newVideoSize = {
            w: $(_this26.domElement).width(),
            h: $(_this26.domElement).height()
          };
          var mouse = _this26._mouseCenter;
          $(_this26.domElement).css({
            width: _this26._zoom + '%',
            height: _this26._zoom + '%'
          });
          var maxOffset = _this26._zoom - 100;
          var offset = {
            x: mouse.x * maxOffset / newVideoSize.w * (maxOffset / 100),
            y: mouse.y * maxOffset / newVideoSize.h * (maxOffset / 100)
          };
          offset.x = offset.x < maxOffset ? offset.x : maxOffset;
          offset.y = offset.y < maxOffset ? offset.y : maxOffset;
          $(_this26.domElement).css({
            left: "-" + offset.x + "%",
            top: "-" + offset.y + "%"
          });
          _this26._zoomOffset = {
            x: offset.x,
            y: offset.y
          };
          paella.events.trigger(paella.events.videoZoomChanged, {
            video: _assertThisInitialized(_this26)
          });
        };

        _this26.zoomOut = function () {
          if (_this26._zoom <= 100 || !_this26._zoomAvailable) return;

          if (!_this26._mouseCenter) {
            _this26._mouseCenter = {
              x: $(_this26.domElement).width() / 2,
              y: $(_this26.domElement).height() / 2
            };
          }

          _this26._zoom -= 25;
          _this26._zoom = _this26._zoom < 100 ? 100 : _this26._zoom;
          _this26._zoom = _this26._zoom > _this26._maxZoom ? _this26._maxZoom : _this26._zoom;
          var newVideoSize = {
            w: $(_this26.domElement).width(),
            h: $(_this26.domElement).height()
          };
          var mouse = _this26._mouseCenter;
          $(_this26.domElement).css({
            width: _this26._zoom + '%',
            height: _this26._zoom + '%'
          });
          var maxOffset = _this26._zoom - 100;
          var offset = {
            x: mouse.x * maxOffset / newVideoSize.w * (maxOffset / 100),
            y: mouse.y * maxOffset / newVideoSize.h * (maxOffset / 100)
          };
          offset.x = offset.x < maxOffset ? offset.x : maxOffset;
          offset.y = offset.y < maxOffset ? offset.y : maxOffset;
          $(_this26.domElement).css({
            left: "-" + offset.x + "%",
            top: "-" + offset.y + "%"
          });
          _this26._zoomOffset = {
            x: offset.x,
            y: offset.y
          };
          paella.events.trigger(paella.events.videoZoomChanged, {
            video: _assertThisInitialized(_this26)
          });
        };

        var altScrollMessageContainer = document.createElement('div');
        altScrollMessageContainer.className = "alt-scroll-message-container";
        altScrollMessageContainer.innerHTML = "<p>" + paella.utils.dictionary.translate("Use Alt+Scroll to zoom the video") + "</p>";
        eventCapture.appendChild(altScrollMessageContainer);
        $(altScrollMessageContainer).css({
          opacity: 0.0
        });
        var altScrollMessageTimer = null;
        $(eventCapture).on('mousewheel wheel', function (evt) {
          if (!_this26.allowZoom() || !_this26._zoomAvailable) return;

          if (!evt.altKey) {
            showAltScrollMessage();
            return;
          } else {
            clearAltScrollMessage(false);

            if (altScrollMessageTimer) {
              clearTimeout(altScrollMessageTimer);
              altScrollMessageTimer = null;
            }
          }

          var mouse = mousePos(evt);
          var wheel = wheelDelta(evt);
          if (_this26._zoom >= _this26._maxZoom && wheel > 0) return;
          _this26._zoom += wheel;
          _this26._zoom = _this26._zoom < 100 ? 100 : _this26._zoom;
          _this26._zoom = _this26._zoom > _this26._maxZoom ? _this26._maxZoom : _this26._zoom;
          var newVideoSize = {
            w: $(_this26.domElement).width(),
            h: $(_this26.domElement).height()
          };
          $(_this26.domElement).css({
            width: _this26._zoom + '%',
            height: _this26._zoom + '%'
          });
          var maxOffset = _this26._zoom - 100;
          var offset = {
            x: mouse.x * maxOffset / newVideoSize.w * (maxOffset / 100),
            y: mouse.y * maxOffset / newVideoSize.h * (maxOffset / 100)
          };
          offset.x = offset.x < maxOffset ? offset.x : maxOffset;
          offset.y = offset.y < maxOffset ? offset.y : maxOffset;
          $(_this26.domElement).css({
            left: "-" + offset.x + "%",
            top: "-" + offset.y + "%"
          });
          _this26._zoomOffset = {
            x: offset.x,
            y: offset.y
          };
          paella.events.trigger(paella.events.videoZoomChanged, {
            video: _assertThisInitialized(_this26)
          });
          _this26._mouseCenter = mouse;
          evt.stopPropagation();
          return false;
        });
        $(eventCapture).on('mousedown', function (evt) {
          _this26._mouseDown = mousePos(evt);
          _this26.drag = true;
        });
        $(eventCapture).on('mousemove', function (evt) {
          if (!_this26.allowZoom() || !_this26._zoomAvailable) return;
          var mouse = mousePos(evt);
          var offset = {
            x: mouse.x - _this26._mouseDown.x,
            y: mouse.y - _this26._mouseDown.y
          }; // We have not found out why there are sometimes sudden jumps in the
          // position of the mouse cursos, so we avoid the problem

          if ((Math.abs(offset.x) > 80 || Math.abs(_this26.y) > 80) && _this26.drag) {
            _this26._mouseDown = mouse;
            return;
          }

          _this26.drag = evt.buttons > 0;

          if (_this26.drag) {
            paella.player.videoContainer.disablePlayOnClick();
            panImage.apply(_assertThisInitialized(_this26), [offset]);
            _this26._mouseDown = mouse;
          }
        });
        $(eventCapture).on('mouseup', function (evt) {
          if (!_this26.allowZoom() || !_this26._zoomAvailable) return;
          _this26.drag = false;
          paella.player.videoContainer.enablePlayOnClick(1000);
        });
        $(eventCapture).on('mouseleave', function (evt) {
          _this26.drag = false;
        });
      }

      return _this26;
    }

    _createClass(VideoRect, [{
      key: "allowZoom",
      value: function allowZoom() {
        return !this.canvasData.mouseEventsSupport;
      }
    }, {
      key: "setZoom",
      value: function setZoom(zoom, left, top) {
        var tween = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : 0;

        if (this.zoomAvailable()) {
          this._zoomOffset.x = left;
          this._zoomOffset.y = top;
          this._zoom = zoom;

          if (tween == 0) {
            $(this.domElement).css({
              width: this._zoom + '%',
              height: this._zoom + '%',
              left: "-" + this._zoomOffset.x + "%",
              top: "-" + this._zoomOffset.y + "%"
            });
          } else {
            $(this.domElement).stop(true, false).animate({
              width: this._zoom + '%',
              height: this._zoom + '%',
              left: "-" + this._zoomOffset.x + "%",
              top: "-" + this._zoomOffset.y + "%"
            }, tween, "linear");
          }

          paella.events.trigger(paella.events.videoZoomChanged, {
            video: this
          });
        }
      }
    }, {
      key: "captureFrame",
      value: function captureFrame() {
        return Promise.resolve(null);
      }
    }, {
      key: "supportsCaptureFrame",
      value: function supportsCaptureFrame() {
        return Promise.resolve(false);
      } // zoomAvailable will only return true if the zoom is enabled, the
      // video plugin supports zoom and the current video resolution is higher than
      // the current video size

    }, {
      key: "zoomAvailable",
      value: function zoomAvailable() {
        return this.allowZoom() && this._zoomAvailable;
      }
    }, {
      key: "disableEventCapture",
      value: function disableEventCapture() {
        this.eventCapture.style.pointerEvents = 'none';
      }
    }, {
      key: "enableEventCapture",
      value: function enableEventCapture() {
        this.eventCapture.style.pointerEvents = '';
      }
    }, {
      key: "canvasData",
      get: function get() {
        var canvasType = this._stream && Array.isArray(this._stream.canvas) && this._stream.canvas[0];

        var canvasData = canvasType && paella.getVideoCanvasData(this._stream.canvas[0]) || {
          mouseEventsSupport: false,
          webglSupport: false
        };
        return canvasData;
      }
    }]);

    return VideoRect;
  }(paella.DomNode);

  paella.VideoRect = VideoRect;

  var VideoElementBase = /*#__PURE__*/function (_paella$VideoRect) {
    _inherits(VideoElementBase, _paella$VideoRect);

    var _super11 = _createSuper(VideoElementBase);

    function VideoElementBase(id, stream, containerType, left, top, width, height) {
      var _this27;

      _classCallCheck(this, VideoElementBase);

      _this27 = _super11.call(this, id, containerType, left, top, width, height);
      _this27._stream = stream;
      _this27._ready = false;
      _this27._autoplay = false;
      _this27._videoQualityStrategy = null;
      if (_this27._stream.preview) _this27.setPosterFrame(_this27._stream.preview);

      if (_this27.canvasData.mouseEventsSupport) {
        _this27.disableEventCapture();
      }

      return _this27;
    }

    _createClass(VideoElementBase, [{
      key: "defaultProfile",
      value: function defaultProfile() {
        return null;
      } // Synchronous functions: returns null if the resource is not loaded. Use only if 
      // the resource is loaded.

    }, {
      key: "setVideoQualityStrategy",
      // Initialization functions
      value: function setVideoQualityStrategy(strategy) {
        this._videoQualityStrategy = strategy;
      }
    }, {
      key: "setPosterFrame",
      value: function setPosterFrame(url) {
        paella.log.debug("TODO: implement setPosterFrame() function");
      }
    }, {
      key: "setAutoplay",
      value: function setAutoplay(autoplay) {
        this._autoplay = autoplay;
      }
    }, {
      key: "setMetadata",
      value: function setMetadata(data) {
        this._metadata = data;
      }
    }, {
      key: "load",
      value: function load() {
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "supportAutoplay",
      value: function supportAutoplay() {
        return true;
      } // Video canvas functions

    }, {
      key: "videoCanvas",
      value: function videoCanvas() {
        return Promise.reject(new Error("VideoElementBase::videoCanvas(): Not implemented in child class."));
      } // Multi audio functions

    }, {
      key: "supportsMultiaudio",
      value: function supportsMultiaudio() {
        return Promise.resolve(false);
      }
    }, {
      key: "getAudioTracks",
      value: function getAudioTracks() {
        return Promise.resolve([]);
      }
    }, {
      key: "setCurrentAudioTrack",
      value: function setCurrentAudioTrack(trackId) {
        return Promise.resolve(false);
      }
    }, {
      key: "getCurrentAudioTrack",
      value: function getCurrentAudioTrack() {
        return Promise.resolve(-1);
      } // Playback functions

    }, {
      key: "getVideoData",
      value: function getVideoData() {
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "play",
      value: function play() {
        paella.log.debug("TODO: implement play() function in your VideoElementBase subclass");
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "pause",
      value: function pause() {
        paella.log.debug("TODO: implement pause() function in your VideoElementBase subclass");
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "isPaused",
      value: function isPaused() {
        paella.log.debug("TODO: implement isPaused() function in your VideoElementBase subclass");
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "duration",
      value: function duration() {
        paella.log.debug("TODO: implement duration() function in your VideoElementBase subclass");
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "setCurrentTime",
      value: function setCurrentTime(time) {
        paella.log.debug("TODO: implement setCurrentTime() function in your VideoElementBase subclass");
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "currentTime",
      value: function currentTime() {
        paella.log.debug("TODO: implement currentTime() function in your VideoElementBase subclass");
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "setVolume",
      value: function setVolume(volume) {
        paella.log.debug("TODO: implement setVolume() function in your VideoElementBase subclass");
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "volume",
      value: function volume() {
        paella.log.debug("TODO: implement volume() function in your VideoElementBase subclass");
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "setPlaybackRate",
      value: function setPlaybackRate(rate) {
        paella.log.debug("TODO: implement setPlaybackRate() function in your VideoElementBase subclass");
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "playbackRate",
      value: function playbackRate() {
        paella.log.debug("TODO: implement playbackRate() function in your VideoElementBase subclass");
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "getQualities",
      value: function getQualities() {
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "setQuality",
      value: function setQuality(index) {
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "getCurrentQuality",
      value: function getCurrentQuality() {
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "unload",
      value: function unload() {
        this._callUnloadEvent();

        return paella_DeferredNotImplemented();
      }
    }, {
      key: "getDimensions",
      value: function getDimensions() {
        return paella_DeferredNotImplemented(); // { width:X, height:Y }
      }
    }, {
      key: "goFullScreen",
      value: function goFullScreen() {
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "freeze",
      value: function freeze() {
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "unFreeze",
      value: function unFreeze() {
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "disable",
      value: function disable(isMainAudioPlayer) {
        console.log("Disable video requested");
      }
    }, {
      key: "enable",
      value: function enable(isMainAudioPlayer) {
        console.log("Enable video requested");
      } // Utility functions

    }, {
      key: "setClassName",
      value: function setClassName(className) {
        this.domElement.className = className;
      }
    }, {
      key: "_callReadyEvent",
      value: function _callReadyEvent() {
        paella.events.trigger(paella.events.singleVideoReady, {
          sender: this
        });
      }
    }, {
      key: "_callUnloadEvent",
      value: function _callUnloadEvent() {
        paella.events.trigger(paella.events.singleVideoUnloaded, {
          sender: this
        });
      }
    }, {
      key: "ready",
      get: function get() {
        return this._ready;
      }
    }, {
      key: "stream",
      get: function get() {
        return this._stream;
      }
    }, {
      key: "currentTimeSync",
      get: function get() {
        return null;
      }
    }, {
      key: "volumeSync",
      get: function get() {
        return null;
      }
    }, {
      key: "pausedSync",
      get: function get() {
        return null;
      }
    }, {
      key: "durationSync",
      get: function get() {
        return null;
      }
    }]);

    return VideoElementBase;
  }(paella.VideoRect);

  paella.VideoElementBase = VideoElementBase;

  var EmptyVideo = /*#__PURE__*/function (_paella$VideoElementB) {
    _inherits(EmptyVideo, _paella$VideoElementB);

    var _super12 = _createSuper(EmptyVideo);

    function EmptyVideo(id, stream, left, top, width, height) {
      _classCallCheck(this, EmptyVideo);

      return _super12.call(this, id, stream, 'div', left, top, width, height);
    } // Initialization functions


    _createClass(EmptyVideo, [{
      key: "setPosterFrame",
      value: function setPosterFrame(url) {}
    }, {
      key: "setAutoplay",
      value: function setAutoplay(auto) {}
    }, {
      key: "load",
      value: function load() {
        return paella_DeferredRejected(new Error("no such compatible video player"));
      }
    }, {
      key: "play",
      value: function play() {
        return paella_DeferredRejected(new Error("no such compatible video player"));
      }
    }, {
      key: "pause",
      value: function pause() {
        return paella_DeferredRejected(new Error("no such compatible video player"));
      }
    }, {
      key: "isPaused",
      value: function isPaused() {
        return paella_DeferredRejected(new Error("no such compatible video player"));
      }
    }, {
      key: "duration",
      value: function duration() {
        return paella_DeferredRejected(new Error("no such compatible video player"));
      }
    }, {
      key: "setCurrentTime",
      value: function setCurrentTime(time) {
        return paella_DeferredRejected(new Error("no such compatible video player"));
      }
    }, {
      key: "currentTime",
      value: function currentTime() {
        return paella_DeferredRejected(new Error("no such compatible video player"));
      }
    }, {
      key: "setVolume",
      value: function setVolume(volume) {
        return paella_DeferredRejected(new Error("no such compatible video player"));
      }
    }, {
      key: "volume",
      value: function volume() {
        return paella_DeferredRejected(new Error("no such compatible video player"));
      }
    }, {
      key: "setPlaybackRate",
      value: function setPlaybackRate(rate) {
        return paella_DeferredRejected(new Error("no such compatible video player"));
      }
    }, {
      key: "playbackRate",
      value: function playbackRate() {
        return paella_DeferredRejected(new Error("no such compatible video player"));
      }
    }, {
      key: "unFreeze",
      value: function unFreeze() {
        return paella_DeferredRejected(new Error("no such compatible video player"));
      }
    }, {
      key: "freeze",
      value: function freeze() {
        return paella_DeferredRejected(new Error("no such compatible video player"));
      }
    }, {
      key: "unload",
      value: function unload() {
        return paella_DeferredRejected(new Error("no such compatible video player"));
      }
    }, {
      key: "getDimensions",
      value: function getDimensions() {
        return paella_DeferredRejected(new Error("no such compatible video player"));
      }
    }]);

    return EmptyVideo;
  }(paella.VideoElementBase);

  paella.EmptyVideo = EmptyVideo;

  var EmptyVideoFactory = /*#__PURE__*/function (_paella$VideoFactory) {
    _inherits(EmptyVideoFactory, _paella$VideoFactory);

    var _super13 = _createSuper(EmptyVideoFactory);

    function EmptyVideoFactory() {
      _classCallCheck(this, EmptyVideoFactory);

      return _super13.apply(this, arguments);
    }

    _createClass(EmptyVideoFactory, [{
      key: "isStreamCompatible",
      value: function isStreamCompatible(streamData) {
        return true;
      }
    }, {
      key: "getVideoObject",
      value: function getVideoObject(id, streamData, rect) {
        return new paella.EmptyVideo(id, streamData, rect.x, rect.y, rect.w, rect.h);
      }
    }]);

    return EmptyVideoFactory;
  }(paella.VideoFactory);

  paella.videoFactories.EmptyVideoFactory = EmptyVideoFactory;

  var Html5Video = /*#__PURE__*/function (_paella$VideoElementB2) {
    _inherits(Html5Video, _paella$VideoElementB2);

    var _super14 = _createSuper(Html5Video);

    function Html5Video(id, stream, left, top, width, height, streamName) {
      var _this28;

      _classCallCheck(this, Html5Video);

      _this28 = _super14.call(this, id, stream, 'video', left, top, width, height);
      _this28._currentQuality = null;
      _this28._autoplay = false;
      _this28._streamName = streamName || 'mp4';
      _this28._playbackRate = 1;

      if (_this28._stream.sources[_this28._streamName]) {
        _this28._stream.sources[_this28._streamName].sort(function (a, b) {
          return a.res.h - b.res.h;
        });
      }

      _this28.video.preload = "auto";

      _this28.video.setAttribute("playsinline", ""); //this.video.setAttribute("tabindex","-1");


      _this28._configureVideoEvents(_this28.video);

      return _this28;
    }

    _createClass(Html5Video, [{
      key: "_configureVideoEvents",
      value: function _configureVideoEvents(videoElement) {
        var _this29 = this;

        function onProgress(event) {
          if (!this._ready && this.video.readyState == 4) {
            this._ready = true;

            if (this._initialCurrentTipe !== undefined) {
              this.video.currentTime = this._initialCurrentTime;
              delete this._initialCurrentTime;
            }

            this._callReadyEvent();
          }
        }

        var evtCallback = function evtCallback(event) {
          onProgress.apply(_this29, [event]);
        };

        $(this.video).bind('progress', evtCallback);
        $(this.video).bind('loadstart', evtCallback);
        $(this.video).bind('loadedmetadata', evtCallback);
        $(this.video).bind('canplay', evtCallback);
        $(this.video).bind('oncanplay', evtCallback); // Save current time to resume video

        $(this.video).bind('timeupdate', function (evt) {
          _this29._resumeCurrentTime = _this29.video.currentTime;
        });
        $(this.video).bind('ended', function (evt) {
          paella.events.trigger(paella.events.endVideo);
        });
        $(this.video).bind('emptied', function (evt) {
          if (_this29._resumeCurrentTime && !isNaN(_this29._resumeCurrentTime)) {
            _this29.video.currentTime = _this29._resumeCurrentTime;
          }
        }); // Fix safari setQuelity bug

        if (paella.utils.userAgent.browser.Safari) {
          $(this.video).bind('canplay canplaythrough', function (evt) {
            _this29._resumeCurrentTime && (_this29.video.currentTime = _this29._resumeCurrentTime);
          });
        }
      }
    }, {
      key: "_deferredAction",
      value: function _deferredAction(action) {
        var _this30 = this;

        return new Promise(function (resolve, reject) {
          function processResult(actionResult) {
            if (actionResult instanceof Promise) {
              actionResult.then(function (p) {
                return resolve(p);
              })["catch"](function (err) {
                return reject(err);
              });
            } else {
              resolve(actionResult);
            }
          }

          if (_this30.ready) {
            processResult(action());
          } else {
            $(_this30.video).bind('canplay', function () {
              processResult(action());
              $(_this30.video).unbind('canplay');
              $(_this30.video).unbind('loadedmetadata');
            });
          }
        });
      }
    }, {
      key: "_getQualityObject",
      value: function _getQualityObject(index, s) {
        return {
          index: index,
          res: s.res,
          src: s.src,
          toString: function toString() {
            return this.res.w == 0 ? "auto" : this.res.w + "x" + this.res.h;
          },
          shortLabel: function shortLabel() {
            return this.res.w == 0 ? "auto" : this.res.h + "p";
          },
          compare: function compare(q2) {
            return this.res.w * this.res.h - q2.res.w * q2.res.h;
          }
        };
      }
    }, {
      key: "captureFrame",
      value: function captureFrame() {
        var _this31 = this;

        return new Promise(function (resolve) {
          resolve({
            source: _this31.video,
            width: _this31.video.videoWidth,
            height: _this31.video.videoHeight
          });
        });
      }
    }, {
      key: "supportsCaptureFrame",
      value: function supportsCaptureFrame() {
        return Promise.resolve(true);
      } // Initialization functions

    }, {
      key: "getVideoData",
      value: function getVideoData() {
        var _this32 = this;

        var This = this;
        return new Promise(function (resolve, reject) {
          _this32._deferredAction(function () {
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
    }, {
      key: "setPosterFrame",
      value: function setPosterFrame(url) {
        this._posterFrame = url;
      }
    }, {
      key: "setAutoplay",
      value: function setAutoplay(auto) {
        this._autoplay = auto;

        if (auto && this.video) {
          this.video.setAttribute("autoplay", auto);
        }
      }
    }, {
      key: "videoCanvas",
      value: function videoCanvas() {
        var canvasType = this._stream.canvas || ["video"];
        return paella.getVideoCanvas(canvasType);
      }
    }, {
      key: "webGlDidLoad",
      value: function webGlDidLoad() {
        return Promise.resolve();
      }
    }, {
      key: "load",
      value: function load() {
        var _this33 = this;

        return new Promise(function (resolve, reject) {
          var sources = _this33._stream.sources[_this33._streamName];

          if (_this33._currentQuality === null && _this33._videoQualityStrategy) {
            _this33._currentQuality = _this33._videoQualityStrategy.getQualityIndex(sources);
          }

          var stream = _this33._currentQuality < sources.length ? sources[_this33._currentQuality] : null;
          _this33.video.innerText = "";

          _this33.videoCanvas().then(function (CanvasClass) {
            var canvasInstance = new CanvasClass(stream);
            _this33._zoomAvailable = canvasInstance.allowZoom();

            if (window.$paella_bg && bg.app && canvasInstance instanceof bg.app.WindowController) {
              // WebGL canvas
              _this33.domElementType = 'canvas';

              if (stream) {
                // WebGL engine load callback
                return new Promise(function (webglResolve, webglReject) {
                  _this33.webGlDidLoad().then(function () {
                    _this33.canvasController = null;
                    var mainLoop = bg.app.MainLoop.singleton;
                    mainLoop.updateMode = bg.app.FrameUpdate.AUTO;
                    mainLoop.canvas = _this33.domElement;
                    mainLoop.run(canvasInstance);
                    return _this33.loadVideoStream(canvasInstance, stream);
                  }).then(function (canvas) {
                    webglResolve(canvas);
                  })["catch"](function (err) {
                    return webglReject(err);
                  });
                });
              } else {
                Promise.reject(new Error("Invalid stream data."));
              }
            } else {
              return _this33.loadVideoStream(canvasInstance, stream);
            }
          }).then(function (canvas) {
            if (canvas && paella.WebGLCanvas && canvas instanceof paella.WebGLCanvas) {
              _this33._video = canvas.video;

              _this33._video.pause();

              _this33._configureVideoEvents(_this33.video);
            }

            resolve(stream);
          })["catch"](function (err) {
            reject(err);
          });
        });
      }
    }, {
      key: "loadVideoStream",
      value: function loadVideoStream(canvasInstance, stream) {
        return canvasInstance.loadVideo(this, stream);
      }
    }, {
      key: "disable",
      value: function disable(isMainAudioPlayer) {//if (isMainAudioPlayer) return;
        //this._isDisabled = true;
        //this._playState = !this.video.paused;
        //this.video.pause();
      }
    }, {
      key: "enable",
      value: function enable(isMainAudioPlayer) {//if (isMainAudioPlayer) return;
        //this._isDisabled = false;
        //if (this._playState) {
        //	this.video.play();
        //}
      }
    }, {
      key: "getQualities",
      value: function getQualities() {
        var _this34 = this;

        return new Promise(function (resolve, reject) {
          setTimeout(function () {
            var result = [];
            var sources = _this34._stream.sources[_this34._streamName];
            var index = -1;
            sources.forEach(function (s) {
              index++;
              result.push(_this34._getQualityObject(index, s));
            });
            resolve(result);
          }, 10);
        });
      }
    }, {
      key: "setQuality",
      value: function setQuality(index) {
        var _this35 = this;

        return new Promise(function (resolve) {
          var paused = _this35.video.paused;
          var sources = _this35._stream.sources[_this35._streamName];
          _this35._currentQuality = index < sources.length ? index : 0;
          var currentTime = _this35.video.currentTime;
          var This = _this35;

          var onSeek = function onSeek() {
            This.unFreeze().then(function () {
              resolve();
              This.video.removeEventListener('seeked', onSeek, false);
            });
          };

          _this35.freeze().then(function () {
            return _this35.load();
          }).then(function () {
            if (!paused) {
              _this35.play();
            }

            _this35.video.addEventListener('seeked', onSeek);

            _this35.video.currentTime = currentTime;
          });
        });
      }
    }, {
      key: "getCurrentQuality",
      value: function getCurrentQuality() {
        var _this36 = this;

        return new Promise(function (resolve) {
          resolve(_this36._getQualityObject(_this36._currentQuality, _this36._stream.sources[_this36._streamName][_this36._currentQuality]));
        });
      }
    }, {
      key: "play",
      value: function play() {
        var _this37 = this;

        return this._deferredAction(function () {
          if (!_this37._isDisabled) {
            return _this37.video.play();
          } else {
            return Promise.resolve();
          }
        });
      }
    }, {
      key: "pause",
      value: function pause() {
        var _this38 = this;

        return this._deferredAction(function () {
          if (!_this38._isDisabled) {
            return _this38.video.pause();
          } else {
            return Promise.resolve();
          }
        });
      }
    }, {
      key: "isPaused",
      value: function isPaused() {
        var _this39 = this;

        return this._deferredAction(function () {
          return _this39.video.paused;
        });
      }
    }, {
      key: "duration",
      value: function duration() {
        var _this40 = this;

        return this._deferredAction(function () {
          return _this40.video.duration;
        });
      }
    }, {
      key: "setCurrentTime",
      value: function setCurrentTime(time) {
        var _this41 = this;

        return this._deferredAction(function () {
          (time == 0 || time) && !isNaN(time) && (_this41.video.currentTime = time);
        });
      }
    }, {
      key: "currentTime",
      value: function currentTime() {
        var _this42 = this;

        return this._deferredAction(function () {
          return _this42.video.currentTime;
        });
      }
    }, {
      key: "setVolume",
      value: function setVolume(volume) {
        var _this43 = this;

        return this._deferredAction(function () {
          _this43.video.volume = volume;

          if (volume == 0) {
            _this43.video.setAttribute("muted", "muted");

            _this43.video.muted = true;
          } else {
            _this43.video.removeAttribute("muted");

            _this43.video.muted = false;
          }
        });
      }
    }, {
      key: "volume",
      value: function volume() {
        var _this44 = this;

        return this._deferredAction(function () {
          return _this44.video.volume;
        });
      }
    }, {
      key: "setPlaybackRate",
      value: function setPlaybackRate(rate) {
        var _this45 = this;

        return this._deferredAction(function () {
          _this45._playbackRate = rate;
          _this45.video.playbackRate = rate;
        });
      }
    }, {
      key: "playbackRate",
      value: function playbackRate() {
        var _this46 = this;

        return this._deferredAction(function () {
          return _this46.video.playbackRate;
        });
      }
    }, {
      key: "supportAutoplay",
      value: function supportAutoplay() {
        var macOS10_12_safari = paella.utils.userAgent.system.MacOS && paella.utils.userAgent.system.Version.minor >= 12 && paella.utils.userAgent.browser.Safari;
        var iOS = paella.utils.userAgent.system.iOS; // Autoplay does not work from Chrome version 64

        var chrome_v64 = paella.utils.userAgent.browser.Chrome && paella.utils.userAgent.browser.Version.major == 64;

        if (macOS10_12_safari || iOS || chrome_v64) {
          return false;
        } else {
          return true;
        }
      }
    }, {
      key: "goFullScreen",
      value: function goFullScreen() {
        var _this47 = this;

        return this._deferredAction(function () {
          var elem = _this47.video;

          if (elem.requestFullscreen) {
            elem.requestFullscreen();
          } else if (elem.msRequestFullscreen) {
            elem.msRequestFullscreen();
          } else if (elem.mozRequestFullScreen) {
            elem.mozRequestFullScreen();
          } else if (elem.webkitEnterFullscreen) {
            elem.webkitEnterFullscreen();
          }
        });
      }
    }, {
      key: "unFreeze",
      value: function unFreeze() {
        var _this48 = this;

        return this._deferredAction(function () {
          var c = document.getElementById(_this48.video.id + "canvas");

          if (c) {
            $(c).remove();
          }
        });
      }
    }, {
      key: "freeze",
      value: function freeze() {
        var This = this;
        return this._deferredAction(function () {
          var canvas = document.createElement("canvas");
          canvas.id = This.video.id + "canvas";
          canvas.className = "freezeFrame";
          canvas.width = This.video.videoWidth;
          canvas.height = This.video.videoHeight;
          canvas.style.cssText = This.video.style.cssText;
          canvas.style.zIndex = 2;
          var ctx = canvas.getContext("2d");
          ctx.drawImage(This.video, 0, 0, Math.ceil(canvas.width / 16) * 16, Math.ceil(canvas.height / 16) * 16); //Draw image

          This.video.parentElement.appendChild(canvas);
        });
      }
    }, {
      key: "unload",
      value: function unload() {
        this._callUnloadEvent();

        return paella_DeferredNotImplemented();
      }
    }, {
      key: "getDimensions",
      value: function getDimensions() {
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "buffered",
      get: function get() {
        return this.video && this.video.buffered;
      }
    }, {
      key: "video",
      get: function get() {
        if (this.domElementType == 'video') {
          return this.domElement;
        } else {
          this._video = this._video || document.createElement('video');
          return this._video;
        }
      }
    }, {
      key: "ready",
      get: function get() {
        // Fix Firefox specific issue when video reaches the end
        if (paella.utils.userAgent.browser.Firefox && this.video.currentTime == this.video.duration && this.video.readyState == 2) {
          this.video.currentTime = 0;
        }

        return this.video.readyState >= 3;
      } // Synchronous functions: returns null if the resource is not loaded. Use only if 
      // the resource is loaded.

    }, {
      key: "currentTimeSync",
      get: function get() {
        return this.ready ? this.video.currentTime : null;
      }
    }, {
      key: "volumeSync",
      get: function get() {
        return this.ready ? this.video.volume : null;
      }
    }, {
      key: "pausedSync",
      get: function get() {
        return this.ready ? this.video.paused : null;
      }
    }, {
      key: "durationSync",
      get: function get() {
        return this.ready ? this.video.duration : null;
      }
    }]);

    return Html5Video;
  }(paella.VideoElementBase);

  paella.Html5Video = Html5Video;

  paella.Html5Video.IsAutoplaySupported = function () {
    var debug = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : false;
    return new Promise(function (resolve) {
      // Create video element to test autoplay
      var video = document.createElement('video');
      video.src = 'data:video/mp4;base64,AAAAIGZ0eXBtcDQyAAACAGlzb21pc28yYXZjMW1wNDEAAAAIZnJlZQAAC+htZGF0AAACqQYF//+l3EXpvebZSLeWLNgg2SPu73gyNjQgLSBjb3JlIDE1NSByMjkwMSA3ZDBmZjIyIC0gSC4yNjQvTVBFRy00IEFWQyBjb2RlYyAtIENvcHlsZWZ0IDIwMDMtMjAxOCAtIGh0dHA6Ly93d3cudmlkZW9sYW4ub3JnL3gyNjQuaHRtbCAtIG9wdGlvbnM6IGNhYmFjPTEgcmVmPTEgZGVibG9jaz0xOjA6MCBhbmFseXNlPTB4MToweDEgbWU9ZGlhIHN1Ym1lPTEgcHN5PTEgcHN5X3JkPTEuMDA6MC4wMCBtaXhlZF9yZWY9MCBtZV9yYW5nZT0xNiBjaHJvbWFfbWU9MSB0cmVsbGlzPTAgOHg4ZGN0PTAgY3FtPTAgZGVhZHpvbmU9MjEsMTEgZmFzdF9wc2tpcD0xIGNocm9tYV9xcF9vZmZzZXQ9MCB0aHJlYWRzPTEgbG9va2FoZWFkX3RocmVhZHM9MSBzbGljZWRfdGhyZWFkcz0wIG5yPTAgZGVjaW1hdGU9MSBpbnRlcmxhY2VkPTAgYmx1cmF5X2NvbXBhdD0wIGNvbnN0cmFpbmVkX2ludHJhPTAgYmZyYW1lcz0zIGJfcHlyYW1pZD0yIGJfYWRhcHQ9MSBiX2JpYXM9MCBkaXJlY3Q9MSB3ZWlnaHRiPTEgb3Blbl9nb3A9MCB3ZWlnaHRwPTEga2V5aW50PTMyMCBrZXlpbnRfbWluPTMyIHNjZW5lY3V0PTQwIGludHJhX3JlZnJlc2g9MCByYz1jcmYgbWJ0cmVlPTAgY3JmPTQwLjAgcWNvbXA9MC42MCBxcG1pbj0wIHFwbWF4PTY5IHFwc3RlcD00IGlwX3JhdGlvPTEuNDAgcGJfcmF0aW89MS4zMCBhcT0xOjEuMDAAgAAAAJBliIQD/2iscx5avG2BdVkxRtUop8zs5zVIqfxQM03W1oVb8spYPP0yjO506xIxgVQ4iSPGOtcDZBVYGqcTatSa730A8XTpnUDpUSrpyaCe/P8eLds/XenOFYMh8UIGMBwzhVOniajqOPFOmmFC20nufGOpJw81hGhgFwCO6a8acwB0P6LNhZZoRD0y2AZMQfEA0AAHAAAACEGaIRiEP5eAANAABwDSGK8UmBURhUGyINeXiuMlXvJnPVVQKigqVGy8PAuVNiWY94iJ/jL/HeT+/usIfmc/dsQ/TV87CTfXhD8C/4xCP3V+DJP8UP3iJdT8okfAuRJF8zkPgh5/J7XzGT8o9pJ+tvlST+g3uwh1330Q9qd4IbnwOQ9dexCHf8mQfVJ57wET8acsIcn6UT6p7yoP2uQ97fFAhrNARXaou2QkEJxrmP6ZBa7TiE6Uqx04OcnChy+OrfwfRWfSYRbS2wmENdDIKUQSkggeXbLb10CIHL5BPgiBydo+HEEPILBbH9zZOdw/77EbN8euVRS/ZcjbZ/D63aLDh1MTme4vfGzFjXkw9r7U8EhcddAmwXGKjo9o53+/8Rnm1rnt6yh3hLD9/htcZnjjGcW9ZQlj6DKIGrrPo/l6C6NyeVr07mB/N6VlGb5fkLBZM42iUNiIGnMJzShmmlFtEsO0mr5CMcFiJdrZQjdIxsYwpU4xlzmD2+oPtjSLVZiDh2lHDRmHubAxXMROEt0z4GkcCYCk832HaXZSM+4vZbUwJa2ysgmfAQMTEM9gxxct7h5xLdrMnHUnB2bXMO2rEnqnnjWHyFYTrzmZTjJ3N4qP+Pv5VHYzZuAa9jnrg35h5hu/Q87uewVnjbJrtcOOm6b9lltPS6n/mkxgxSyqLJVzr/bYt039aTYyhmveJTdeiG7kLfmn9bqjXfxdfZoz53RDcxw+jP7n7TtT8BsB3jUvxe7n5Gbrm9/5QzQ3cxxl9s6ojDMDg3R7Bx//b5rwuSI84z2fuP8/nPxj/wvHNccSL3n77sCEv+AUwlVzHAFkYCkDkdRIORiUg5GJSDkYlIORgKQsjI9E1d0PUP5zV31MSkvI+AAAAAtBmkMYjP/4v5j6wQDGGK/rogCQL/+rZ+PHZ8R11ITSYLDLmXtUdt5a5V+63JHBE/z0/3cCf4av6uOAGtQmr8mCvCxwSI/c7KILm624qm/Kb4fKK5P1GWvX/S84SiSuyTIfk3zVghdRlzZpLZXgddiJjKTGb43OFQCup1nyCbjWgjmOozS6mXGEDsuoVDkSR7/Q8ErEhAZqgHJ5yCxkICvpE+HztDoOSTYiiBCW6shBKQM/Aw5DdbsGWUc/3XEIhH8HXJSDU8mZDApXSSZR+4fbKiOTUHmUgYd7HOLNG544Zy3F+ZPqxMwuGkCo/HxfLXrebdQakkweTwTqUgIDlwvPC181Z5eZ7cDTV905pDXGj/KiRAk3p+hlgHPvRW35PT4b163gUGkmIl0Ds4OBn6G64lkPsnQPNFs8AtwH4PSeYoz9s5uh/jFX0tlr7f+xzN6PuDvyOmKvYhdYK5FLAOkbJ6E/r7fxRZ1g63PPgeLsfir/0iq9K7IW+eWH4ONNCdL5oyV/TSILB+ob8z1ZWUf9p50jIFh6l64geGZ785/8OQanz95/ZPwGF03PMeYdkuH6x5Q/gcx5bg2RejM+RPQ6Vg6D43WOe+1fDKbVqr9P6Y5S9fuwD56fvq62ESHISopAae8/mbMD2la4/h/K9uSYuhxZDNszxgmQmd2kQDoEU6g46KneCXN/b9b5Ez/4iQOfBj4EuXyfp8MlAlFg8P486y4HT9H680pqN9xN164m7ReXPWHU7pw7F9Pw3FEDjQrHHnM3KfE8KWrl2JyxrdR90wr+HPPrkO5v1XT88+iU5MfGOeswl1uQxhiAGn4O62zaMJmDbSrMNY4LEV/jc+TjMQJRwOsUrW8aDyVoo87t8G+Qtfm6fOy6DNK9crM2f3KQJ0YEPc5JM0eSQsjYSFkZFIWRkUgcB1El5HwAAAAIAZ5iRCX/y4AA7liudRsNCYNGAb/ocSIJGilo13xUupVcYzmaVbkEY3nch7y9pfI1qxo3V9n9Q+r84e3e7dCfx+aLdD6S8dDaqzc6eqH9onVdingfJndPc1yaRhn4JD1jsj85o/le4m9cE2W1F8unegGNvOuknfzBmz4/Us9R+kC7xW5e9Z1Z9JsGeb/z6XkKkxiNh1C3Ns5jTVxB9x1poY49zmq+xsXNh0uy75DZf0JM9Uq8ghKrZiQDyAlHf4dqw48mtmlozgUMkh7VJ7vbIW1UNI81pRTT7C3WOOa3mw0RNjAoMLjtm1+VqQBEhHw+6VBvNTwCBkyvjU+kVMA1OU8elyGQX0xTlHRM8iPGg3CO8B5AzpOm2M7J75cG3PPGc42ztXyYzat3TyZ54CyDqZi1/Mn4B6T1fhMSD0uk5lKsOHIktX1Sfud/I3Ew+McUpwm3bxVdAy7uiGeiXWbe3cMBmCruk4yW18G6dEf9prnjcT6HUZG5bBSQGYSQscX2KCZoWxWkVS0w6IkwqdVJ+Akyey/Hl0MyrcAMI6Sgq3HMn95sBcc4ZadQLT31gNKo6qyebwmyK63HlMTK40Zj3FGuboBQ3Zsg87Jf3Gg1SDlG6fRVl2+5Cc6q+0OcUNRyCfLIG157ZHTSCwD9UpZtZDLki0BCLgAAAAhBmmQYiv+BgQDyne7dSHRhSQ/D31OEhh0h14FMQDwlvgJODIIYGxb7iHQo1mvJn3hOUUli9mTrUMuuPv/W2bsX3X7l9k7jtvT/Cuf4Kmbbhn0zmtjx7GWFyjrJfyHHxs5mxuTjdr2/drXoPhh1rb2XOnE9H3BdBqm1I+K5Sd1hYCevn6PbJcDyUHpysOZeLu+VoYklOlicG52cbxZbzvVeiS4jb+qyJoL62Ox+nSrUhOkCNMf8dz5iEi+C5iYZciyXk6gmIvSJVQDNTiO2i1a6pGORhiNVWGAMBHNHyHbmWtqB9AYbSdGR5qQzHnGF9HWaHfTzIqQMNEioRwE00KEllO+UcuPFmOs0Kl9lgy1DgKSKfGaaVFc7RNrn0nOddM6OfOG51GuoJSCnOpRjIvLAMAAAAA1NfU1+Ro9v/o+AANDABwAABedtb292AAAAbG12aGQAAAAA18kDNdfJAzUAAAPoAAAAowABAAABAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAAAAGGlvZHMAAAAAEICAgAcAT////v7/AAACknRyYWsAAABcdGtoZAAAAAPXyQM118kDNQAAAAEAAAAAAAAAnwAAAAAAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAEAAAAAAOAAAACAAAAAAACRlZHRzAAAAHGVsc3QAAAAAAAAAAQAAAJ8AABZEAAEAAAAAAgptZGlhAAAAIG1kaGQAAAAA18kDNdfJAzUAAV+QAAA3qlXEAAAAAAAtaGRscgAAAAAAAAAAdmlkZQAAAAAAAAAAAAAAAFZpZGVvSGFuZGxlcgAAAAG1bWluZgAAABR2bWhkAAAAAQAAAAAAAAAAAAAAJGRpbmYAAAAcZHJlZgAAAAAAAAABAAAADHVybCAAAAABAAABdXN0YmwAAACYc3RzZAAAAAAAAAABAAAAiGF2YzEAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAOAAgAEgAAABIAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAY//8AAAAyYXZjQwFNQAr/4QAaZ01ACuyiLy+AtQYBBkAAAATAAAEsI8SJZYABAAVo74OcgAAAABhzdHRzAAAAAAAAAAEAAAAFAAALIgAAABRzdHNzAAAAAAAAAAEAAAABAAAAEXNkdHAAAAAAIBAQGBAAAAAwY3R0cwAAAAAAAAAEAAAAAgAAFkQAAAABAAAhZgAAAAEAAAsiAAAAAQAAFkQAAAAcc3RzYwAAAAAAAAABAAAAAQAAAAEAAAABAAAAKHN0c3oAAAAAAAAAAAAAAAUAAANBAAAADAAAAA8AAAAMAAAADAAAACRzdGNvAAAAAAAAAAUAAAAwAAADdQAABhAAAAjPAAAKyQAAAlp0cmFrAAAAXHRraGQAAAAD18kDNdfJAzUAAAACAAAAAAAAAKMAAAAAAAAAAAAAAAEBAAAAAAEAAAAAAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAAAAAAkZWR0cwAAABxlbHN0AAAAAAAAAAEAAABzAAAIQAABAAAAAAG+bWRpYQAAACBtZGhkAAAAANfJAzXXyQM1AACsRAAAHABVxAAAAAAAJWhkbHIAAAAAAAAAAHNvdW4AAAAAAAAAAAAAAABNb25vAAAAAXFtaW5mAAAAEHNtaGQAAAAAAAAAAAAAACRkaW5mAAAAHGRyZWYAAAAAAAAAAQAAAAx1cmwgAAAAAQAAATVzdGJsAAAAZ3N0c2QAAAAAAAAAAQAAAFdtcDRhAAAAAAAAAAEAAAAAAAAAAAACABAAAAAArEQAAAAAADNlc2RzAAAAAAOAgIAiAAIABICAgBRAFQAAAAAAAAAAAAAABYCAgAISCAaAgIABAgAAABhzdHRzAAAAAAAAAAEAAAAHAAAEAAAAABxzdHNjAAAAAAAAAAEAAAABAAAAAQAAAAEAAAAwc3RzegAAAAAAAAAAAAAABwAAAAQAAAAEAAACiwAAArAAAAHuAAABNwAAAAQAAAAsc3RjbwAAAAAAAAAHAAADcQAAA4EAAAOFAAAGHwAACNsAAArVAAAMDAAAABpzZ3BkAQAAAHJvbGwAAAACAAAAAf//AAAAHHNiZ3AAAAAAcm9sbAAAAAEAAAAHAAAAAQAAABR1ZHRhAAAADG5hbWVNb25vAAAAb3VkdGEAAABnbWV0YQAAAAAAAAAhaGRscgAAAAAAAAAAbWRpcmFwcGwAAAAAAAAAAAAAAAA6aWxzdAAAADKpdG9vAAAAKmRhdGEAAAABAAAAAEhhbmRCcmFrZSAxLjEuMiAyMDE4MDkwNTAw';
      video.load(); //video.style.display = 'none';

      if (debug) {
        video.style = "position: fixed; top: 0px; right: 0px; z-index: 1000000;";
        document.body.appendChild(video);
      } else {
        video.style.display = 'none';
      }

      video.playing = false;
      video.play().then(function (status) {
        resolve(true);
      })["catch"](function (err) {
        resolve(false);
      });
    });
  };

  var Html5VideoFactory = /*#__PURE__*/function () {
    function Html5VideoFactory() {
      _classCallCheck(this, Html5VideoFactory);
    }

    _createClass(Html5VideoFactory, [{
      key: "isStreamCompatible",
      value: function isStreamCompatible(streamData) {
        try {
          if (paella.videoFactories.Html5VideoFactory.s_instances > 0 && paella.utils.userAgent.system.iOS && paella.utils.userAgent.system.Version.major <= 10 && paella.utils.userAgent.system.Version.minor < 3) {
            return false;
          }

          for (var key in streamData.sources) {
            if (key == 'mp4' || key == 'mp3') return true;
          }
        } catch (e) {}

        return false;
      }
    }, {
      key: "getVideoObject",
      value: function getVideoObject(id, streamData, rect) {
        ++paella.videoFactories.Html5VideoFactory.s_instances;
        return new paella.Html5Video(id, streamData, rect.x, rect.y, rect.w, rect.h);
      }
    }]);

    return Html5VideoFactory;
  }();

  paella.videoFactories.Html5VideoFactory = Html5VideoFactory;
  paella.videoFactories.Html5VideoFactory.s_instances = 0;

  var ImageVideo = /*#__PURE__*/function (_paella$VideoElementB3) {
    _inherits(ImageVideo, _paella$VideoElementB3);

    var _super15 = _createSuper(ImageVideo);

    function ImageVideo(id, stream, left, top, width, height) {
      var _this49;

      _classCallCheck(this, ImageVideo);

      _this49 = _super15.call(this, id, stream, 'img', left, top, width, height);
      _this49._posterFrame = null;
      _this49._currentQuality = null;
      _this49._currentTime = 0;
      _this49._duration = 0;
      _this49._ended = false;
      _this49._playTimer = null;
      _this49._playbackRate = 1;
      _this49._frameArray = null;

      _this49._stream.sources.image.sort(function (a, b) {
        return a.res.h - b.res.h;
      });

      return _this49;
    }

    _createClass(ImageVideo, [{
      key: "_deferredAction",
      value: function _deferredAction(action) {
        var _this50 = this;

        return new Promise(function (_resolve) {
          if (_this50.ready) {
            _resolve(action());
          } else {
            var _resolve = function resolve() {
              _this50._ready = true;

              _resolve(action());
            };

            $(_this50.video).bind('paella:imagevideoready', _resolve);
          }
        });
      }
    }, {
      key: "_getQualityObject",
      value: function _getQualityObject(index, s) {
        return {
          index: index,
          res: s.res,
          src: s.src,
          toString: function toString() {
            return Number(this.res.w) + "x" + Number(this.res.h);
          },
          shortLabel: function shortLabel() {
            return this.res.h + "p";
          },
          compare: function compare(q2) {
            return Number(this.res.w) * Number(this.res.h) - Number(q2.res.w) * Number(q2.res.h);
          }
        };
      }
    }, {
      key: "_loadCurrentFrame",
      value: function _loadCurrentFrame() {
        var This = this;

        if (this._frameArray) {
          var frame = this._frameArray[0];

          this._frameArray.some(function (f) {
            if (This._currentTime < f.time) {
              return true;
            } else {
              frame = f.src;
            }
          });

          this.img.src = frame;
        }
      } // Initialization functions

      /*allowZoom:function() {
      	return false;
      },*/

    }, {
      key: "getVideoData",
      value: function getVideoData() {
        var _this51 = this;

        return new Promise(function (resolve) {
          _this51._deferredAction(function () {
            var imgStream = _this51._stream.sources.image[_this51._currentQuality];
            var videoData = {
              duration: _this51._duration,
              currentTime: _this51._currentTime,
              volume: 0,
              paused: _this51._paused,
              ended: _this51._ended,
              res: {
                w: imgStream.res.w,
                h: imgStream.res.h
              }
            };
            resolve(videoData);
          });
        });
      }
    }, {
      key: "setPosterFrame",
      value: function setPosterFrame(url) {
        this._posterFrame = url;
      }
    }, {
      key: "setAutoplay",
      value: function setAutoplay(auto) {
        this._autoplay = auto;

        if (auto && this.video) {
          this.video.setAttribute("autoplay", auto);
        }
      }
    }, {
      key: "load",
      value: function load() {
        var This = this;
        var sources = this._stream.sources.image;

        if (this._currentQuality === null && this._videoQualityStrategy) {
          this._currentQuality = this._videoQualityStrategy.getQualityIndex(sources);
        }

        var stream = this._currentQuality < sources.length ? sources[this._currentQuality] : null;

        if (stream) {
          this._frameArray = [];

          for (var key in stream.frames) {
            var time = Math.floor(Number(key.replace("frame_", "")));

            this._frameArray.push({
              src: stream.frames[key],
              time: time
            });
          }

          this._frameArray.sort(function (a, b) {
            return a.time - b.time;
          });

          this._ready = true;
          this._currentTime = 0;
          this._duration = stream.duration;

          this._loadCurrentFrame();

          paella.events.trigger("paella:imagevideoready");
          return this._deferredAction(function () {
            return stream;
          });
        } else {
          return paella_DeferredRejected(new Error("Could not load video: invalid quality stream index"));
        }
      }
    }, {
      key: "supportAutoplay",
      value: function supportAutoplay() {
        return true;
      }
    }, {
      key: "getQualities",
      value: function getQualities() {
        var _this52 = this;

        return new Promise(function (resolve) {
          setTimeout(function () {
            var result = [];
            var sources = _this52._stream.sources[_this52._streamName];
            var index = -1;
            sources.forEach(function (s) {
              index++;
              result.push(_this52._getQualityObject(index, s));
            });
            resolve(result);
          }, 10);
        });
      }
    }, {
      key: "setQuality",
      value: function setQuality(index) {
        var _this53 = this;

        return new Promise(function (resolve) {
          var paused = _this53._paused;
          var sources = _this53._stream.sources.image;
          _this53._currentQuality = index < sources.length ? index : 0;
          var currentTime = _this53._currentTime;

          _this53.load().then(function () {
            this._loadCurrentFrame();

            resolve();
          });
        });
      }
    }, {
      key: "getCurrentQuality",
      value: function getCurrentQuality() {
        var _this54 = this;

        return new Promise(function (resolve) {
          resolve(_this54._getQualityObject(_this54._currentQuality, _this54._stream.sources.image[_this54._currentQuality]));
        });
      }
    }, {
      key: "play",
      value: function play() {
        var This = this;
        return this._deferredAction(function () {
          This._playTimer = new paella.utils.Timer(function () {
            This._currentTime += 0.25 * This._playbackRate;

            This._loadCurrentFrame();
          }, 250);
          This._playTimer.repeat = true;
        });
      }
    }, {
      key: "pause",
      value: function pause() {
        var This = this;
        return this._deferredAction(function () {
          This._playTimer.repeat = false;
          This._playTimer = null;
        });
      }
    }, {
      key: "isPaused",
      value: function isPaused() {
        var _this55 = this;

        return this._deferredAction(function () {
          return _this55._paused;
        });
      }
    }, {
      key: "duration",
      value: function duration() {
        var _this56 = this;

        return this._deferredAction(function () {
          return _this56._duration;
        });
      }
    }, {
      key: "setCurrentTime",
      value: function setCurrentTime(time) {
        var _this57 = this;

        return this._deferredAction(function () {
          _this57._currentTime = time;

          _this57._loadCurrentFrame();
        });
      }
    }, {
      key: "currentTime",
      value: function currentTime() {
        var _this58 = this;

        return this._deferredAction(function () {
          return _this58._currentTime;
        });
      }
    }, {
      key: "setVolume",
      value: function setVolume(volume) {
        return this._deferredAction(function () {// No audo sources in image video
        });
      }
    }, {
      key: "volume",
      value: function volume() {
        return this._deferredAction(function () {
          // No audo sources in image video
          return 0;
        });
      }
    }, {
      key: "setPlaybackRate",
      value: function setPlaybackRate(rate) {
        var _this59 = this;

        return this._deferredAction(function () {
          _this59._playbackRate = rate;
        });
      }
    }, {
      key: "playbackRate",
      value: function playbackRate() {
        var _this60 = this;

        return this._deferredAction(function () {
          return _this60._playbackRate;
        });
      }
    }, {
      key: "goFullScreen",
      value: function goFullScreen() {
        var _this61 = this;

        return this._deferredAction(function () {
          var elem = _this61.img;

          if (elem.requestFullscreen) {
            elem.requestFullscreen();
          } else if (elem.msRequestFullscreen) {
            elem.msRequestFullscreen();
          } else if (elem.mozRequestFullScreen) {
            elem.mozRequestFullScreen();
          } else if (elem.webkitEnterFullscreen) {
            elem.webkitEnterFullscreen();
          }
        });
      }
    }, {
      key: "unFreeze",
      value: function unFreeze() {
        return this._deferredAction(function () {});
      }
    }, {
      key: "freeze",
      value: function freeze() {
        return this._deferredAction(function () {});
      }
    }, {
      key: "unload",
      value: function unload() {
        this._callUnloadEvent();

        return paella_DeferredNotImplemented();
      }
    }, {
      key: "getDimensions",
      value: function getDimensions() {
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "img",
      get: function get() {
        return this.domElement;
      }
    }, {
      key: "imgStream",
      get: function get() {
        this._stream.sources.image[this._currentQuality];
      }
    }, {
      key: "_paused",
      get: function get() {
        return this._playTimer == null;
      }
    }]);

    return ImageVideo;
  }(paella.VideoElementBase);

  paella.ImageVideo = ImageVideo;

  var ImageVideoFactory = /*#__PURE__*/function () {
    function ImageVideoFactory() {
      _classCallCheck(this, ImageVideoFactory);
    }

    _createClass(ImageVideoFactory, [{
      key: "isStreamCompatible",
      value: function isStreamCompatible(streamData) {
        try {
          for (var key in streamData.sources) {
            if (key == 'image') return true;
          }
        } catch (e) {}

        return false;
      }
    }, {
      key: "getVideoObject",
      value: function getVideoObject(id, streamData, rect) {
        return new paella.ImageVideo(id, streamData, rect.x, rect.y, rect.w, rect.h);
      }
    }]);

    return ImageVideoFactory;
  }();

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


(function () {
  var BackgroundContainer = /*#__PURE__*/function (_paella$DomNode4) {
    _inherits(BackgroundContainer, _paella$DomNode4);

    var _super16 = _createSuper(BackgroundContainer);

    function BackgroundContainer(id, image) {
      var _this62;

      _classCallCheck(this, BackgroundContainer);

      _this62 = _super16.call(this, 'img', id, {
        position: 'relative',
        top: '0px',
        left: '0px',
        right: '0px',
        bottom: '0px',
        zIndex: GlobalParams.background.zIndex
      });

      _this62.domElement.setAttribute('src', image);

      _this62.domElement.setAttribute('alt', '');

      _this62.domElement.setAttribute('width', '100%');

      _this62.domElement.setAttribute('height', '100%');

      return _this62;
    }

    _createClass(BackgroundContainer, [{
      key: "setImage",
      value: function setImage(image) {
        this.domElement.setAttribute('src', image);
      }
    }]);

    return BackgroundContainer;
  }(paella.DomNode);

  paella.BackgroundContainer = BackgroundContainer;

  var VideoOverlay = /*#__PURE__*/function (_paella$DomNode5) {
    _inherits(VideoOverlay, _paella$DomNode5);

    var _super17 = _createSuper(VideoOverlay);

    _createClass(VideoOverlay, [{
      key: "size",
      get: function get() {
        if (!this._size) {
          this._size = {
            w: 1280,
            h: 720
          };
        }

        return this._size;
      }
    }]);

    function VideoOverlay() {
      var _this63;

      _classCallCheck(this, VideoOverlay);

      var style = {
        position: 'absolute',
        left: '0px',
        right: '0px',
        top: '0px',
        bottom: '0px',
        overflow: 'hidden',
        zIndex: 10
      };
      _this63 = _super17.call(this, 'div', 'overlayContainer', style);

      _this63.domElement.setAttribute("role", "main");

      return _this63;
    }

    _createClass(VideoOverlay, [{
      key: "_generateId",
      value: function _generateId() {
        return Math.ceil(Date.now() * Math.random());
      }
    }, {
      key: "enableBackgroundMode",
      value: function enableBackgroundMode() {
        this.domElement.className = 'overlayContainer background';
      }
    }, {
      key: "disableBackgroundMode",
      value: function disableBackgroundMode() {
        this.domElement.className = 'overlayContainer';
      }
    }, {
      key: "clear",
      value: function clear() {
        this.domElement.innerText = "";
      }
    }, {
      key: "getVideoRect",
      value: function getVideoRect(index) {
        return paella.player.videoContainer.getVideoRect(index);
      }
    }, {
      key: "addText",
      value: function addText(text, rect, isDebug) {
        var textElem = document.createElement('div');
        textElem.innerText = text;
        textElem.className = "videoOverlayText";
        if (isDebug) textElem.style.backgroundColor = "red";
        return this.addElement(textElem, rect);
      }
    }, {
      key: "addElement",
      value: function addElement(element, rect) {
        this.domElement.appendChild(element);
        element.style.position = 'absolute';
        element.style.left = this.getHSize(rect.left) + '%';
        element.style.top = this.getVSize(rect.top) + '%';
        element.style.width = this.getHSize(rect.width) + '%';
        element.style.height = this.getVSize(rect.height) + '%';
        return element;
      }
    }, {
      key: "getLayer",
      value: function getLayer(id, zindex) {
        id = id || this._generateId();
        return $(this.domElement).find("#" + id)[0] || this.addLayer(id, zindex);
      }
    }, {
      key: "addLayer",
      value: function addLayer(id, zindex) {
        zindex = zindex || 10;
        var element = document.createElement('div');
        element.className = "row";
        element.id = id || this._generateId();
        return this.addElement(element, {
          left: 0,
          top: 0,
          width: 1280,
          height: 720
        });
      }
    }, {
      key: "removeLayer",
      value: function removeLayer(id) {
        var elem = $(this.domElement).find("#" + id)[0];

        if (elem) {
          this.domElement.removeChild(elem);
        }
      }
    }, {
      key: "removeElement",
      value: function removeElement(element) {
        if (element) {
          try {
            this.domElement.removeChild(element);
          } catch (e) {}
        }
      }
    }, {
      key: "getVSize",
      value: function getVSize(px) {
        return px * 100 / this.size.h;
      }
    }, {
      key: "getHSize",
      value: function getHSize(px) {
        return px * 100 / this.size.w;
      }
    }]);

    return VideoOverlay;
  }(paella.DomNode);

  paella.VideoOverlay = VideoOverlay;

  var VideoWrapper = /*#__PURE__*/function (_paella$DomNode6) {
    _inherits(VideoWrapper, _paella$DomNode6);

    var _super18 = _createSuper(VideoWrapper);

    function VideoWrapper(id, left, top, width, height) {
      var _this64;

      _classCallCheck(this, VideoWrapper);

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
      _this64 = _super18.call(this, 'div', id, style);
      _this64._rect = {
        left: left,
        top: top,
        width: width,
        height: height
      };
      _this64.domElement.className = "videoWrapper";
      return _this64;
    }

    _createClass(VideoWrapper, [{
      key: "setRect",
      value: function setRect(rect, animate) {
        this._rect = JSON.parse(JSON.stringify(rect));
        var relativeSize = new paella.RelativeVideoSize();
        var percentTop = relativeSize.percentVSize(rect.top) + '%';
        var percentLeft = relativeSize.percentWSize(rect.left) + '%';
        var percentWidth = relativeSize.percentWSize(rect.width) + '%';
        var percentHeight = relativeSize.percentVSize(rect.height) + '%';
        var style = {
          top: percentTop,
          left: percentLeft,
          width: percentWidth,
          height: percentHeight,
          position: 'absolute'
        };

        if (animate) {
          this.disableClassName();
          var thisClass = this;
          $(this.domElement).animate(style, 400, function () {
            thisClass.enableClassName();
            paella.events.trigger(paella.events.setComposition, {
              video: thisClass
            });
          });
          this.enableClassNameAfter(400);
        } else {
          $(this.domElement).css(style);
          paella.events.trigger(paella.events.setComposition, {
            video: this
          });
        }
      }
    }, {
      key: "getRect",
      value: function getRect() {
        return this._rect;
      }
    }, {
      key: "disableClassName",
      value: function disableClassName() {
        this.classNameBackup = this.domElement.className;
        this.domElement.className = "";
      }
    }, {
      key: "enableClassName",
      value: function enableClassName() {
        this.domElement.className = this.classNameBackup;
      }
    }, {
      key: "enableClassNameAfter",
      value: function enableClassNameAfter(millis) {
        setTimeout("$('#" + this.domElement.id + "')[0].className = '" + this.classNameBackup + "'", millis);
      }
    }, {
      key: "setVisible",
      value: function setVisible(visible, animate) {
        var _this65 = this;

        if (typeof visible == "string") {
          visible = /true/i.test(visible) ? true : false;
        }

        if (visible && animate) {
          $(this.domElement).show();
          $(this.domElement).animate({
            opacity: 1.0
          }, 300);
        } else if (visible && !animate) {
          $(this.domElement).show();
        } else if (!visible && animate) {
          $(this.domElement).animate({
            opacity: 0.0
          }, 300, function () {
            return $(_this65.domElement).hide();
          });
        } else if (!visible && !animate) {
          $(this.domElement).hide();
        }
      }
    }, {
      key: "setLayer",
      value: function setLayer(layer) {
        this.domElement.style.zIndex = layer;
      }
    }]);

    return VideoWrapper;
  }(paella.DomNode);

  paella.VideoWrapper = VideoWrapper;
  paella.SeekType = {
    FULL: 1,
    BACKWARDS_ONLY: 2,
    FORWARD_ONLY: 3,
    DISABLED: 4
  }; // This function is used to manage the timer to enable and disable the click and double click events
  // interaction with the video container timeout.

  function clearClickEventsTimeout() {
    if (this._clickEventsTimeout) {
      clearTimeout(this._clickEventsTimeout);
      this._clickEventsTimeout = null;
    }
  }

  var VideoContainerBase = /*#__PURE__*/function (_paella$DomNode7) {
    _inherits(VideoContainerBase, _paella$DomNode7);

    var _super19 = _createSuper(VideoContainerBase);

    function VideoContainerBase(id) {
      var _this66;

      _classCallCheck(this, VideoContainerBase);

      var style = {
        position: 'absolute',
        left: '0px',
        right: '0px',
        top: '0px',
        bottom: '0px',
        overflow: 'hidden'
      };
      _this66 = _super19.call(this, 'div', id, style);
      _this66._trimming = {
        enabled: false,
        start: 0,
        end: 0
      };
      _this66.timeupdateEventTimer = null;
      _this66.timeupdateInterval = 250;
      _this66.masterVideoData = null;
      _this66.slaveVideoData = null;
      _this66.currentMasterVideoData = null;
      _this66.currentSlaveVideoData = null;
      _this66._force = false;
      _this66._clickEventsEnabled = true;
      _this66._seekDisabled = false;
      _this66._seekType = paella.SeekType.FULL;
      _this66._seekTimeLimit = 0;
      _this66._attenuationEnabled = false;
      $(_this66.domElement).dblclick(function (evt) {
        if (_this66._clickEventsEnabled) {
          paella.player.isFullScreen() ? paella.player.exitFullScreen() : paella.player.goFullScreen();
        }
      });
      var dblClickTimer = null;
      $(_this66.domElement).click(function (evt) {
        var doClick = function doClick() {
          if (!_this66._clickEventsEnabled) return;
          paella.player.videoContainer.paused().then(function (paused) {
            // If some player needs mouse events support, the click is ignored
            if (_this66.streamProvider.videoPlayers.some(function (p) {
              return p.canvasData.mouseEventsSupport;
            })) {
              return;
            }

            if (paused) {
              paella.player.play();
            } else {
              paella.player.pause();
            }
          });
        }; // the dblClick timer prevents the single click from running when the user double clicks


        if (dblClickTimer) {
          clearTimeout(dblClickTimer);
          dblClickTimer = null;
        } else {
          dblClickTimer = setTimeout(function () {
            dblClickTimer = null;
            doClick();
          }, 200);
        }
      });

      _this66.domElement.addEventListener("touchstart", function (event) {
        if (paella.player.controls) {
          paella.player.controls.restartHideTimer();
        }
      });

      return _this66;
    }

    _createClass(VideoContainerBase, [{
      key: "triggerTimeupdate",
      value: function triggerTimeupdate() {
        var _this67 = this;

        var paused = 0;
        var duration = 0;
        this.paused().then(function (p) {
          paused = p;
          return _this67.duration();
        }).then(function (d) {
          duration = d;
          return _this67.currentTime();
        }).then(function (currentTime) {
          if (!paused || _this67._force) {
            _this67._seekTimeLimit = currentTime > _this67._seekTimeLimit ? currentTime : _this67._seekTimeLimit;
            _this67._force = false;
            paella.events.trigger(paella.events.timeupdate, {
              videoContainer: _this67,
              currentTime: currentTime,
              duration: duration
            });
          }
        });
      }
    }, {
      key: "startTimeupdate",
      value: function startTimeupdate() {
        var _this68 = this;

        this.timeupdateEventTimer = new paella.utils.Timer(function (timer) {
          _this68.triggerTimeupdate();
        }, this.timeupdateInterval);
        this.timeupdateEventTimer.repeat = true;
      }
    }, {
      key: "stopTimeupdate",
      value: function stopTimeupdate() {
        if (this.timeupdateEventTimer) {
          this.timeupdateEventTimer.repeat = false;
        }

        this.timeupdateEventTimer = null;
      }
    }, {
      key: "enablePlayOnClick",
      value: function enablePlayOnClick() {
        var _this69 = this;

        var timeout = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 0;
        clearClickEventsTimeout.apply(this);

        if (timeout) {
          this._clickEventsTimeout = setTimeout(function () {
            _this69._clickEventsEnabled = true;
          }, timeout);
        } else {
          this._clickEventsEnabled = true;
        }
      }
    }, {
      key: "disablePlayOnClick",
      value: function disablePlayOnClick() {
        clearClickEventsTimeout.apply(this);
        this._clickEventsEnabled = false;
      }
    }, {
      key: "isPlayOnClickEnabled",
      value: function isPlayOnClickEnabled() {
        return this._clickEventsEnabled;
      }
    }, {
      key: "play",
      value: function play() {
        this.streamProvider.startVideoSync(this.audioPlayer);
        this.startTimeupdate();
        setTimeout(function () {
          return paella.events.trigger(paella.events.play);
        }, 50);
      }
    }, {
      key: "pause",
      value: function pause() {
        paella.events.trigger(paella.events.pause);
        this.stopTimeupdate();
        this.streamProvider.stopVideoSync();
      }
    }, {
      key: "seekTo",
      value: function seekTo(newPositionPercent) {
        var _this70 = this;

        return new Promise(function (resolve, reject) {
          var time = 0;
          paella.player.videoContainer.currentTime().then(function (t) {
            time = t;
            return paella.player.videoContainer.duration();
          }).then(function (duration) {
            if (_this70._seekTimeLimit > 0 && _this70._seekType == paella.SeekType.BACKWARDS_ONLY) {
              time = _this70._seekTimeLimit;
            }

            var currentPercent = time / duration * 100;

            switch (_this70._seekType) {
              case paella.SeekType.FULL:
                break;

              case paella.SeekType.BACKWARDS_ONLY:
                if (newPositionPercent > currentPercent) {
                  reject(new Error("Warning: Seek is disabled"));
                  return;
                }

                break;

              case paella.SeekType.FORWARD_ONLY:
                if (newPositionPercent < currentPercent) {
                  reject(new Error("Warning: Seek is disabled"));
                  return;
                }

                break;

              case paella.SeekType.DISABLED:
                reject(new Error("Warning: Seek is disabled"));
                return;
            }

            _this70.setCurrentPercent(newPositionPercent).then(function (timeData) {
              _this70._force = true;

              _this70.triggerTimeupdate();

              paella.events.trigger(paella.events.seekToTime, {
                newPosition: timeData.time
              });
              paella.events.trigger(paella.events.seekTo, {
                newPositionPercent: newPositionPercent
              });
              resolve();
            });
          });
        });
      }
    }, {
      key: "seekToTime",
      value: function seekToTime(time) {
        var _this71 = this;

        return new Promise(function (resolve, reject) {
          paella.player.videoContainer.currentTime().then(function (currentTime) {
            if (_this71._seekTimeLimit && _this71._seekType == paella.SeekType.BACKWARDS_ONLY) {
              currentTime = _this71._seekTimeLimit;
            }

            switch (_this71._seekType) {
              case paella.SeekType.FULL:
                break;

              case paella.SeekType.BACKWARDS_ONLY:
                if (time > currentTime) {
                  reject(new Error("Warning: Seek is disabled"));
                  return;
                }

                break;

              case paella.SeekType.FORWARD_ONLY:
                if (time < currentTime) {
                  reject(new Error("Warning: Seek is disabled"));
                  return;
                }

                break;

              case paella.SeekType.DISABLED:
                reject(new Error("Warning: Seek is disabled"));
                return;
            }

            _this71.setCurrentTime(time).then(function (timeData) {
              _this71._force = true;

              _this71.triggerTimeupdate();

              var percent = timeData.time * 100 / timeData.duration;
              paella.events.trigger(paella.events.seekToTime, {
                newPosition: timeData.time
              });
              paella.events.trigger(paella.events.seekTo, {
                newPositionPercent: percent
              });
            });
          });
        });
      }
    }, {
      key: "setPlaybackRate",
      value: function setPlaybackRate(params) {
        paella.events.trigger(paella.events.setPlaybackRate, {
          rate: params
        });
      }
    }, {
      key: "mute",
      value: function mute() {}
    }, {
      key: "unmute",
      value: function unmute() {}
    }, {
      key: "setVolume",
      value: function setVolume(params) {}
    }, {
      key: "volume",
      value: function volume() {
        return 1;
      }
    }, {
      key: "trimStart",
      value: function trimStart() {
        var _this72 = this;

        return new Promise(function (resolve) {
          resolve(_this72._trimming.start);
        });
      }
    }, {
      key: "trimEnd",
      value: function trimEnd() {
        var _this73 = this;

        return new Promise(function (resolve) {
          resolve(_this73._trimming.end);
        });
      }
    }, {
      key: "trimEnabled",
      value: function trimEnabled() {
        var _this74 = this;

        return new Promise(function (resolve) {
          resolve(_this74._trimming.enabled);
        });
      }
    }, {
      key: "trimming",
      value: function trimming() {
        var _this75 = this;

        return new Promise(function (resolve) {
          resolve(_this75._trimming);
        });
      }
    }, {
      key: "enableTrimming",
      value: function enableTrimming() {
        this._trimming.enabled = true;
        var cap = paella.captions.getActiveCaptions();
        if (cap !== undefined) paella.plugins.captionsPlugin.buildBodyContent(cap._captions, "list");
        paella.events.trigger(paella.events.setTrim, {
          trimEnabled: this._trimming.enabled,
          trimStart: this._trimming.start,
          trimEnd: this._trimming.end
        });
      }
    }, {
      key: "disableTrimming",
      value: function disableTrimming() {
        this._trimming.enabled = false;
        var cap = paella.captions.getActiveCaptions();
        if (cap !== undefined) paella.plugins.captionsPlugin.buildBodyContent(cap._captions, "list");
        paella.events.trigger(paella.events.setTrim, {
          trimEnabled: this._trimming.enabled,
          trimStart: this._trimming.start,
          trimEnd: this._trimming.end
        });
      }
    }, {
      key: "setTrimming",
      value: function setTrimming(start, end) {
        var _this76 = this;

        return new Promise(function (resolve) {
          var currentTime = 0;

          _this76.currentTime(true).then(function (c) {
            currentTime = c;
            return _this76.duration();
          }).then(function (duration) {
            _this76._trimming.start = Math.floor(start);
            _this76._trimming.end = Math.floor(end);

            if (_this76._trimming.enabled) {
              if (currentTime < _this76._trimming.start) {
                _this76.setCurrentTime(0);
              }

              if (currentTime > _this76._trimming.end) {
                _this76.setCurrentTime(duration);
              }

              var cap = paella.captions.getActiveCaptions();
              if (cap !== undefined) paella.plugins.captionsPlugin.buildBodyContent(cap._captions, "list");
            }

            paella.events.trigger(paella.events.setTrim, {
              trimEnabled: _this76._trimming.enabled,
              trimStart: _this76._trimming.start,
              trimEnd: _this76._trimming.end
            });
            resolve();
          });
        });
      }
    }, {
      key: "setTrimmingStart",
      value: function setTrimmingStart(start) {
        return this.setTrimming(start, this._trimming.end);
      }
    }, {
      key: "setTrimmingEnd",
      value: function setTrimmingEnd(end) {
        return this.setTrimming(this._trimming.start, end);
      }
    }, {
      key: "setCurrentPercent",
      value: function setCurrentPercent(percent) {
        var _this77 = this;

        var duration = 0;
        return new Promise(function (resolve) {
          _this77.duration().then(function (d) {
            duration = d;
            return _this77.trimming();
          }).then(function (trimming) {
            var position = 0;

            if (trimming.enabled) {
              var start = trimming.start;
              var end = trimming.end;
              duration = end - start;
              var trimedPosition = percent * duration / 100;
              position = parseFloat(trimedPosition);
            } else {
              position = percent * duration / 100;
            }

            return _this77.setCurrentTime(position);
          }).then(function (timeData) {
            resolve(timeData);
          });
        });
      }
    }, {
      key: "setCurrentTime",
      value: function setCurrentTime(time) {
        paella.log.debug("VideoContainerBase.setCurrentTime(" + time + ")");
      }
    }, {
      key: "currentTime",
      value: function currentTime() {
        paella.log.debug("VideoContainerBase.currentTime()");
        return 0;
      }
    }, {
      key: "duration",
      value: function duration() {
        paella.log.debug("VideoContainerBase.duration()");
        return 0;
      }
    }, {
      key: "paused",
      value: function paused() {
        paella.log.debug("VideoContainerBase.paused()");
        return true;
      }
    }, {
      key: "setupVideo",
      value: function setupVideo(onSuccess) {
        paella.log.debug("VideoContainerBase.setupVide()");
      }
    }, {
      key: "isReady",
      value: function isReady() {
        paella.log.debug("VideoContainerBase.isReady()");
        return true;
      }
    }, {
      key: "onresize",
      value: function (_onresize) {
        function onresize() {
          return _onresize.apply(this, arguments);
        }

        onresize.toString = function () {
          return _onresize.toString();
        };

        return onresize;
      }(function () {
        _get(_getPrototypeOf(VideoContainerBase.prototype), "onresize", this).call(this, onresize);
      })
    }, {
      key: "ended",
      value: function ended() {
        var _this78 = this;

        return new Promise(function (resolve) {
          var duration = 0;

          _this78.duration().then(function (d) {
            duration = d;
            return _this78.currentTime();
          }).then(function (currentTime) {
            resolve(Math.floor(duration) <= Math.ceil(currentTime));
          });
        });
      }
    }, {
      key: "attenuationEnabled",
      set: function set(att) {
        this._attenuationEnabled = att;
        Array.from(paella.player.videoContainer.container.domElement.children).forEach(function (ch) {
          if (ch.id == "overlayContainer") {
            return;
          }

          if (att) {
            $(ch).addClass("dimmed-element");
          } else {
            $(ch).removeClass("dimmed-element");
          }
        });
      },
      get: function get() {
        return this._attenuationEnabled;
      }
    }, {
      key: "seekType",
      set: function set(type) {
        switch (type) {
          case paella.SeekType.FULL:
          case paella.SeekType.BACKWARDS_ONLY:
          case paella.SeekType.FORWARD_ONLY:
          case paella.SeekType.DISABLED:
            this._seekType = type;
            paella.events.trigger(paella.events.seekAvailabilityChanged, {
              type: type,
              enabled: type == paella.SeekType.FULL,
              disabled: type != paella.SeekType.FULL
            });
            break;

          default:
            throw new Error("Invalid seekType. Allowed seek types:\n\t\t\t\tpaella.SeekType.FULL\n\t\t\t\tpaella.SeekType.BACKWARDS_ONLY\n\t\t\t\tpaella.SeekType.FORWARD_ONLY\n\t\t\t\tpaella.SeekType.DISABLED");
        }
      },
      get: function get() {
        return this._seekType;
      }
    }]);

    return VideoContainerBase;
  }(paella.DomNode);

  paella.VideoContainerBase = VideoContainerBase; // Profile frame strategies

  var ProfileFrameStrategy = /*#__PURE__*/function () {
    function ProfileFrameStrategy() {
      _classCallCheck(this, ProfileFrameStrategy);
    }

    _createClass(ProfileFrameStrategy, [{
      key: "valid",
      value: function valid() {
        return true;
      }
    }, {
      key: "adaptFrame",
      value: function adaptFrame(videoDimensions, frameRect) {
        return frameRect;
      }
    }], [{
      key: "Factory",
      value: function Factory() {
        var config = paella.player.config;

        try {
          var strategyClass = config.player.profileFrameStrategy;
          var ClassObject = paella.utils.objectFromString(strategyClass);
          var strategy = new ClassObject();

          if (strategy instanceof paella.ProfileFrameStrategy) {
            return strategy;
          }
        } catch (e) {}

        return null;
      }
    }]);

    return ProfileFrameStrategy;
  }();

  paella.ProfileFrameStrategy = ProfileFrameStrategy;

  var LimitedSizeProfileFrameStrategy = /*#__PURE__*/function (_ProfileFrameStrategy) {
    _inherits(LimitedSizeProfileFrameStrategy, _ProfileFrameStrategy);

    var _super20 = _createSuper(LimitedSizeProfileFrameStrategy);

    function LimitedSizeProfileFrameStrategy() {
      _classCallCheck(this, LimitedSizeProfileFrameStrategy);

      return _super20.apply(this, arguments);
    }

    _createClass(LimitedSizeProfileFrameStrategy, [{
      key: "adaptFrame",
      value: function adaptFrame(videoDimensions, frameRect) {
        if (videoDimensions.width < frameRect.width || videoDimensions.height < frameRect.height) {
          var frameRectCopy = JSON.parse(JSON.stringify(frameRect));
          frameRectCopy.width = videoDimensions.width;
          frameRectCopy.height = videoDimensions.height;
          var diff = {
            w: frameRect.width - videoDimensions.width,
            h: frameRect.height - videoDimensions.height
          };
          frameRectCopy.top = frameRectCopy.top + diff.h / 2;
          frameRectCopy.left = frameRectCopy.left + diff.w / 2;
          return frameRectCopy;
        }

        return frameRect;
      }
    }]);

    return LimitedSizeProfileFrameStrategy;
  }(ProfileFrameStrategy);

  paella.LimitedSizeProfileFrameStrategy = LimitedSizeProfileFrameStrategy;

  function updateBuffers() {
    // Initial implementation: use the mainStream buffered property
    var mainBuffered = this.mainPlayer && this.mainPlayer.buffered;

    if (mainBuffered) {
      this._bufferedData = [];

      for (var i = 0; i < mainBuffered.length; ++i) {
        this._bufferedData.push({
          start: mainBuffered.start(i),
          end: mainBuffered.end(i)
        });
      }
    }
  }

  var StreamProvider = /*#__PURE__*/function () {
    function StreamProvider(videoData) {
      _classCallCheck(this, StreamProvider);

      this._mainStream = null;
      this._videoStreams = [];
      this._audioStreams = [];
      this._mainPlayer = null;
      this._audioPlayer = null;
      this._videoPlayers = [];
      this._audioPlayers = [];
      this._players = [];
      this._autoplay = paella.utils.parameters.get('autoplay') == 'true' || this.isLiveStreaming;
      this._startTime = 0;
      this._bufferedData = [];
      var streamProvider = this;
      this._buffered = {
        start: function start(index) {
          if (index < 0 || index >= streamProvider._bufferedData.length) {
            throw new Error("Buffered index out of bounds.");
          }

          return streamProvider._bufferedData[index].start;
        },
        end: function end(index) {
          if (index < 0 || index >= streamProvider._bufferedData.length) {
            throw new Error("Buffered index out of bounds.");
          }

          return streamProvider._bufferedData[index].end;
        }
      };
      Object.defineProperty(this._buffered, "length", {
        get: function get() {
          return streamProvider._bufferedData.length;
        }
      });
    }

    _createClass(StreamProvider, [{
      key: "init",
      value: function init(videoData) {
        var _this79 = this;

        if (videoData.length == 0) throw Error("Empty video data.");
        this._videoData = videoData;

        if (!this._videoData.some(function (stream) {
          return stream.role == "master";
        })) {
          this._videoData[0].role = "master";
        }

        this._videoData.forEach(function (stream, index) {
          stream.type = stream.type || 'video';

          if (stream.role == 'master') {
            _this79._mainStream = stream;
          }

          if (stream.type == 'video') {
            _this79._videoStreams.push(stream);
          } else if (stream.type == 'audio') {
            _this79._audioStreams.push(stream);
          }
        });

        if (this._videoStreams.length == 0) {
          throw new Error("No video streams found. Paella Player requires at least one video stream.");
        } // Create video players


        var autoplay = this.autoplay;

        this._videoStreams.forEach(function (videoStream, index) {
          var rect = {
            x: 0,
            y: 0,
            w: 1280,
            h: 720
          };
          var player = paella.videoFactory.getVideoObject("video_".concat(index), videoStream, rect);
          player.setVideoQualityStrategy(_this79._qualityStrategy);
          player.setAutoplay(autoplay);

          if (videoStream == _this79._mainStream) {
            _this79._mainPlayer = player;
            _this79._audioPlayer = player;
          } else {
            player.setVolume(0);
          }

          _this79._videoPlayers.push(player);

          _this79._players.push(player);
        }); // Create audio player


        this._audioStreams.forEach(function (audioStream, index) {
          var player = paella.audioFactory.getAudioObject("audio_".concat(index), audioStream);
          player.setAutoplay(autoplay);

          if (player) {
            _this79._audioPlayers.push(player);

            _this79._players.push(player);
          }
        });
      }
    }, {
      key: "startVideoSync",
      value: function startVideoSync(syncProviderPlayer) {
        var _this80 = this;

        this._syncProviderPlayer = syncProviderPlayer;
        this._audioPlayer = syncProviderPlayer; // The player that provides the synchronization is also used as main audio player.

        this.stopVideoSync();
        console.debug("Start sync to player:");
        console.debug(this._syncProviderPlayer);
        var maxDiff = 0.1;
        var totalTime = 0;
        var numberOfSyncs = 0;
        var syncFrequency = 0;
        var maxSyncFrequency = 0.2;

        var sync = function sync() {
          _this80._syncProviderPlayer.currentTime().then(function (t) {
            _this80.players.forEach(function (player) {
              if (player != syncProviderPlayer && player.currentTimeSync != null && Math.abs(player.currentTimeSync - t) > maxDiff) {
                console.debug("Sync player current time: ".concat(player.currentTimeSync, " to time ").concat(t));
                console.debug(player);
                ++numberOfSyncs;
                player.setCurrentTime(t);

                if (syncFrequency > maxSyncFrequency) {
                  maxDiff *= 1.5;
                  console.log("Maximum syncrhonization frequency reached. Increasing max difference syncronization time to ".concat(maxDiff));
                }
              }
            });
          });

          totalTime += 1000;
          syncFrequency = numberOfSyncs / (totalTime / 1000);
          _this80._syncTimer = setTimeout(function () {
            return sync();
          }, 1000);
        };

        this._syncTimer = setTimeout(function () {
          return sync();
        }, 1000);
      }
    }, {
      key: "stopVideoSync",
      value: function stopVideoSync() {
        if (this._syncTimer) {
          console.debug("Stop video sync");
          clearTimeout(this._syncTimer);
          this._syncTimer = null;
        }
      }
    }, {
      key: "loadVideos",
      value: function loadVideos() {
        var promises = [];

        this._players.forEach(function (player) {
          promises.push(player.load());
        });

        return Promise.all(promises);
      }
    }, {
      key: "callPlayerFunction",
      value: function callPlayerFunction(fnName) {
        var _this81 = this;

        var promises = [];
        var functionArguments = [];

        for (var i = 1; i < arguments.length; ++i) {
          functionArguments.push(arguments[i]);
        }

        this.players.forEach(function (player) {
          promises.push(player[fnName].apply(player, functionArguments));
        });
        return new Promise(function (resolve, reject) {
          Promise.all(promises).then(function () {
            if (fnName == 'play' && !_this81._firstPlay) {
              _this81._firstPlay = true;

              if (_this81._startTime) {
                _this81.players.forEach(function (p) {
                  return p.setCurrentTime(_this81._startTime);
                });
              }
            }

            resolve();
          })["catch"](function (err) {
            reject(err);
          });
        });
      }
    }, {
      key: "buffered",
      get: function get() {
        updateBuffers.apply(this);
        return this._buffered;
      }
    }, {
      key: "startTime",
      get: function get() {
        return this._startTime;
      },
      set: function set(s) {
        this._startTime = s;
      }
    }, {
      key: "isMonostream",
      get: function get() {
        return this._videoStreams.length == 1;
      }
    }, {
      key: "mainStream",
      get: function get() {
        return this._mainStream;
      }
    }, {
      key: "videoStreams",
      get: function get() {
        //return this._videoData;
        return this._videoStreams;
      }
    }, {
      key: "audioStreams",
      get: function get() {
        return this._audioStreams;
      }
    }, {
      key: "streams",
      get: function get() {
        return this._videoStreams.concat(this._audioStreams);
      }
    }, {
      key: "videoPlayers",
      get: function get() {
        return this._videoPlayers;
      }
    }, {
      key: "audioPlayers",
      get: function get() {
        return this._audioPlayers;
      }
    }, {
      key: "players",
      get: function get() {
        return this._videoPlayers.concat(this._audioPlayers);
      }
    }, {
      key: "mainVideoPlayer",
      get: function get() {
        return this._mainPlayer;
      }
    }, {
      key: "mainAudioPlayer",
      get: function get() {
        return this._audioPlayer;
      }
    }, {
      key: "mainPlayer",
      get: function get() {
        return this.mainVideoPlayer || this.mainAudioPlayer;
      }
    }, {
      key: "isLiveStreaming",
      get: function get() {
        return paella.player.isLiveStream();
      }
    }, {
      key: "qualityStrategy",
      set: function set(strategy) {
        this._qualityStrategy = strategy;

        this._videoPlayers.forEach(function (player) {
          player.setVideoQualityStrategy(strategy);
        });
      },
      get: function get() {
        return this._qualityStrategy || null;
      }
    }, {
      key: "autoplay",
      get: function get() {
        return this.supportAutoplay && this._autoplay;
      },
      set: function set(ap) {
        if (!this.supportAutoplay || this.isLiveStreaming) return;
        this._autoplay = ap;

        if (this.videoPlayers) {
          this.videoPlayers.forEach(function (player) {
            return player.setAutoplay(ap);
          });
          this.audioPlayers.forEach(function (player) {
            return player.setAutoplay(ap);
          });
        }
      }
    }, {
      key: "supportAutoplay",
      get: function get() {
        return this.videoPlayers.every(function (player) {
          return player.supportAutoplay();
        });
      }
    }]);

    return StreamProvider;
  }();

  paella.StreamProvider = StreamProvider;

  function addVideoWrapper(id, videoPlayer) {
    var wrapper = new paella.VideoWrapper(id);
    wrapper.addNode(videoPlayer);
    this.videoWrappers.push(wrapper);
    this.container.addNode(wrapper);
    return wrapper;
  }

  var VideoContainer = /*#__PURE__*/function (_paella$VideoContaine) {
    _inherits(VideoContainer, _paella$VideoContaine);

    var _super21 = _createSuper(VideoContainer);

    _createClass(VideoContainer, [{
      key: "streamProvider",
      get: function get() {
        return this._streamProvider;
      }
    }, {
      key: "ready",
      get: function get() {
        return this._ready;
      }
    }, {
      key: "isMonostream",
      get: function get() {
        return this._streamProvider.isMonostream;
      }
    }, {
      key: "trimmingHandler",
      get: function get() {
        return this._trimmingHandler;
      }
    }, {
      key: "videoWrappers",
      get: function get() {
        return this._videoWrappers;
      }
    }, {
      key: "container",
      get: function get() {
        return this._container;
      }
    }, {
      key: "profileFrameStrategy",
      get: function get() {
        return this._profileFrameStrategy;
      }
    }, {
      key: "sourceData",
      get: function get() {
        return this._sourceData;
      }
    }]);

    function VideoContainer(id) {
      var _this82;

      _classCallCheck(this, VideoContainer);

      _this82 = _super21.call(this, id);
      _this82._streamProvider = new paella.StreamProvider();
      _this82._ready = false;
      _this82._videoWrappers = [];
      _this82._container = new paella.DomNode('div', 'playerContainer_videoContainer_container', {
        position: 'relative',
        display: 'block',
        marginLeft: 'auto',
        marginRight: 'auto',
        width: '1024px',
        height: '567px'
      });

      _this82._container.domElement.setAttribute('role', 'main');

      _this82.addNode(_this82._container);

      _this82.overlayContainer = new paella.VideoOverlay(_this82.domElement);

      _this82.container.addNode(_this82.overlayContainer);

      _this82.setProfileFrameStrategy(paella.ProfileFrameStrategy.Factory());

      _this82.setVideoQualityStrategy(paella.VideoQualityStrategy.Factory());

      _this82._audioTag = paella.player.config.player.defaultAudioTag || paella.utils.dictionary.currentLanguage();
      _this82._audioPlayer = null; // Initial volume level

      _this82._volume = paella.utils.cookies.get("volume") ? Number(paella.utils.cookies.get("volume")) : 1;

      if (paella.player.startMuted) {
        _this82._volume = 0;
      }

      _this82._muted = false;
      return _this82;
    } // Playback and status functions


    _createClass(VideoContainer, [{
      key: "play",
      value: function play() {
        var _this83 = this;

        return new Promise(function (resolve, reject) {
          _this83.ended().then(function (ended) {
            if (ended) {
              _this83._streamProvider.startTime = 0;

              _this83.seekToTime(0);
            } else {
              _this83.streamProvider.startTime = _this83._startTime;
            }

            return _this83.streamProvider.callPlayerFunction('play');
          }).then(function () {
            _get(_getPrototypeOf(VideoContainer.prototype), "play", _this83).call(_this83);

            resolve();
          })["catch"](function (err) {
            reject(err);
          });
        });
      }
    }, {
      key: "pause",
      value: function pause() {
        var _this84 = this;

        return new Promise(function (resolve, reject) {
          _this84.streamProvider.callPlayerFunction('pause').then(function () {
            _get(_getPrototypeOf(VideoContainer.prototype), "pause", _this84).call(_this84);

            resolve();
          })["catch"](function (err) {
            reject(err);
          });
        });
      }
    }, {
      key: "setCurrentTime",
      value: function setCurrentTime(time) {
        var _this85 = this;

        return new Promise(function (resolve, reject) {
          _this85.trimming().then(function (trimmingData) {
            if (trimmingData.enabled) {
              time += trimmingData.start;

              if (time < trimmingData.start) {
                time = trimmingData.start;
              }

              if (time > trimmingData.end) {
                time = trimmingData.end;
              }
            }

            return _this85.streamProvider.callPlayerFunction('setCurrentTime', time);
          }).then(function () {
            return _this85.duration(false);
          }).then(function (duration) {
            resolve({
              time: time,
              duration: duration
            });
          })["catch"](function (err) {
            reject(err);
          });
        });
      }
    }, {
      key: "currentTime",
      value: function currentTime() {
        var _this86 = this;

        var ignoreTrimming = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : false;
        return new Promise(function (resolve) {
          var trimmingData = null;
          var p = ignoreTrimming ? Promise.resolve({
            enabled: false
          }) : _this86.trimming();
          p.then(function (t) {
            trimmingData = t;
            return _this86.masterVideo().currentTime();
          }).then(function (time) {
            if (trimmingData.enabled) {
              time = time - trimmingData.start;
            }

            resolve(time);
          });
        });
      }
    }, {
      key: "setPlaybackRate",
      value: function setPlaybackRate(rate) {
        this.streamProvider.callPlayerFunction('setPlaybackRate', rate);

        _get(_getPrototypeOf(VideoContainer.prototype), "setPlaybackRate", this).call(this, rate);
      }
    }, {
      key: "mute",
      value: function mute() {
        var _this87 = this;

        return new Promise(function (resolve) {
          _this87._muted = true;

          _this87._audioPlayer.setVolume(0).then(function () {
            paella.events.trigger(paella.events.setVolume, {
              master: 0
            });
            resolve();
          });
        });
      }
    }, {
      key: "unmute",
      value: function unmute() {
        var _this88 = this;

        return new Promise(function (resolve) {
          _this88._muted = false;

          _this88._audioPlayer.setVolume(_this88._volume).then(function () {
            paella.events.trigger(paella.events.setVolume, {
              master: _this88._volume
            });
            resolve();
          });
        });
      }
    }, {
      key: "setVolume",
      value: function setVolume(params) {
        var _this89 = this;

        if (_typeof(params) == 'object') {
          console.warn("videoContainer.setVolume(): set parameter as object is deprecated");
          return Promise.resolve();
        } else if (params == 0) {
          return this.mute();
        } else {
          return new Promise(function (resolve, reject) {
            paella.utils.cookies.set("volume", params);
            _this89._volume = params;

            _this89._audioPlayer.setVolume(params).then(function () {
              paella.events.trigger(paella.events.setVolume, {
                master: params
              });
              resolve(params);
            })["catch"](function (err) {
              reject(err);
            });
          });
        }
      }
    }, {
      key: "volume",
      value: function volume() {
        return this._audioPlayer.volume();
      }
    }, {
      key: "duration",
      value: function duration() {
        var _this90 = this;

        var ignoreTrimming = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : false;
        return new Promise(function (resolve) {
          var trimmingData = null;
          var p = ignoreTrimming ? Promise.resolve({
            enabled: false
          }) : _this90.trimming();
          p.then(function (t) {
            trimmingData = t;
            return _this90.masterVideo().duration();
          }).then(function (duration) {
            if (trimmingData.enabled) {
              duration = trimmingData.end - trimmingData.start;
            }

            resolve(duration);
          });
        });
      }
    }, {
      key: "paused",
      value: function paused() {
        return this.masterVideo().isPaused();
      } // Video quality functions

    }, {
      key: "getQualities",
      value: function getQualities() {
        return this.masterVideo().getQualities();
      }
    }, {
      key: "setQuality",
      value: function setQuality(index) {
        var qualities = [];
        var promises = [];
        this.streamProvider.videoPlayers.forEach(function (player) {
          var playerData = {
            player: player,
            promise: player.getQualities()
          };
          qualities.push(playerData);
          promises.push(playerData.promise);
        });
        return new Promise(function (resolve) {
          var resultPromises = [];
          Promise.all(promises).then(function () {
            qualities.forEach(function (data) {
              data.promise.then(function (videoQualities) {
                var videoQuality = videoQualities.length > index ? index : videoQualities.length - 1;
                resultPromises.push(data.player.setQuality(videoQuality));
              });
            });
            return Promise.all(resultPromises);
          }).then(function () {
            //setTimeout(() => {
            paella.events.trigger(paella.events.qualityChanged);
            resolve(); //},10);
          });
        });
      }
    }, {
      key: "getCurrentQuality",
      value: function getCurrentQuality() {
        return this.masterVideo().getCurrentQuality();
      } // Current audio functions

    }, {
      key: "getAudioTags",
      value: function getAudioTags() {
        var _this91 = this;

        return new Promise(function (resolve) {
          var lang = [];
          var p = _this91.streamProvider.players;
          p.forEach(function (player) {
            if (player.stream.audioTag) {
              lang.push(player.stream.audioTag);
            }
          });
          resolve(lang);
        });
      }
    }, {
      key: "setAudioTag",
      value: function setAudioTag(lang) {
        var _this92 = this;

        this.streamProvider.stopVideoSync();
        return new Promise(function (resolve) {
          var audioSet = false;
          var firstAudioPlayer = null;
          var promises = [];

          _this92.streamProvider.players.forEach(function (player) {
            if (!firstAudioPlayer) {
              firstAudioPlayer = player;
            }

            if (!audioSet && player.stream.audioTag == lang) {
              audioSet = true;
              _this92._audioPlayer = player;
            }

            promises.push(player.setVolume(0));
          }); // NOTE: The audio only streams must define a valid audio tag


          if (!audioSet && _this92.streamProvider.mainVideoPlayer) {
            _this92._audioPlayer = _this92.streamProvider.mainVideoPlayer;
          } else if (!audioSet && firstAudioPlayer) {
            _this92._audioPlayer = firstAudioPlayer;
          }

          Promise.all(promises).then(function () {
            return _this92._audioPlayer.setVolume(_this92._volume);
          }).then(function () {
            _this92._audioTag = _this92._audioPlayer.stream.audioTag;
            paella.events.trigger(paella.events.audioTagChanged);

            _this92.streamProvider.startVideoSync(_this92.audioPlayer);

            resolve();
          });
        });
      }
    }, {
      key: "setProfileFrameStrategy",
      value: function setProfileFrameStrategy(strategy) {
        this._profileFrameStrategy = strategy;
      }
    }, {
      key: "setVideoQualityStrategy",
      value: function setVideoQualityStrategy(strategy) {
        this.streamProvider.qualityStrategy = strategy;
      }
    }, {
      key: "autoplay",
      value: function autoplay() {
        return this.streamProvider.autoplay;
      }
    }, {
      key: "supportAutoplay",
      value: function supportAutoplay() {
        return this.streamProvider.supportAutoplay;
      }
    }, {
      key: "setAutoplay",
      value: function setAutoplay() {
        var ap = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : true;
        this.streamProvider.autoplay = ap;
        return this.streamProvider.supportAutoplay;
      }
    }, {
      key: "masterVideo",
      value: function masterVideo() {
        return this.streamProvider.mainVideoPlayer || this.audioPlayer;
      }
    }, {
      key: "getVideoRect",
      value: function getVideoRect(videoIndex) {
        if (this.videoWrappers.length > videoIndex) {
          return this.videoWrappers[videoIndex].getRect();
        } else {
          throw new Error("Video wrapper with index ".concat(videoIndex, " not found"));
        }
      }
    }, {
      key: "setStreamData",
      value: function setStreamData(videoData) {
        var _this93 = this;

        var urlParamTime = paella.utils.parameters.get("time");
        var hashParamTime = paella.utils.hashParams.get("time");
        var timeString = hashParamTime ? hashParamTime : urlParamTime ? urlParamTime : "0s";
        var startTime = paella.utils.timeParse.timeToSeconds(timeString);

        if (startTime) {
          this._startTime = startTime;
        }

        videoData.forEach(function (stream) {
          for (var type in stream.sources) {
            var source = stream.sources[type];
            source.forEach(function (item) {
              if (item.res) {
                item.res.w = Number(item.res.w);
                item.res.h = Number(item.res.h);
              }
            });
          }
        });
        this._sourceData = videoData;
        return new Promise(function (resolve, reject) {
          _this93.streamProvider.init(videoData);

          var streamDataAudioTag = null;
          videoData.forEach(function (video) {
            if (video.audioTag && streamDataAudioTag == null) {
              streamDataAudioTag = video.audioTag;
            }

            if (video.audioTag == _this93._audioTag) {
              streamDataAudioTag = _this93._audioTag;
            }
          });

          if (streamDataAudioTag != _this93._audioTag && streamDataAudioTag != null) {
            _this93._audioTag = streamDataAudioTag;
          }

          _this93.streamProvider.videoPlayers.forEach(function (player, index) {
            addVideoWrapper.apply(_this93, ['videoPlayerWrapper_' + index, player]);
            player.setAutoplay(_this93.autoplay());
          });

          _this93.streamProvider.loadVideos()["catch"](function (err) {
            reject(err);
          }).then(function () {
            return _this93.setAudioTag(_this93.audioTag);
          }).then(function () {
            var endedTimer = null;

            var setupEndEventTimer = function setupEndEventTimer() {
              _this93.stopTimeupdate();

              if (endedTimer) {
                clearTimeout(endedTimer);
                endedTimer = null;
              }

              endedTimer = setTimeout(function () {
                paella.events.trigger(paella.events.ended);
              }, 1000);
            };

            var eventBindingObject = _this93.masterVideo().video || _this93.masterVideo().audio;

            $(eventBindingObject).bind('timeupdate', function (evt) {
              _this93.trimming().then(function (trimmingData) {
                var current = evt.currentTarget.currentTime;
                var duration = evt.currentTarget.duration;

                if (trimmingData.enabled) {
                  current -= trimmingData.start;
                  duration = trimmingData.end - trimmingData.start;
                }

                if (current >= duration) {
                  _this93.streamProvider.callPlayerFunction('pause');

                  setupEndEventTimer();
                }
              });
            });
            paella.events.bind(paella.events.endVideo, function (event) {
              setupEndEventTimer();
            });
            _this93._ready = true;
            paella.events.trigger(paella.events.videoReady);
            var profileToUse = paella.utils.parameters.get('profile') || paella.utils.cookies.get('profile') || paella.profiles.getDefaultProfile();

            if (paella.profiles.setProfile(profileToUse, false)) {
              resolve();
            } else if (!paella.profiles.setProfile(paella.profiles.getDefaultProfile(), false)) {
              resolve();
            }
          });
        });
      }
    }, {
      key: "resizePortrait",
      value: function resizePortrait() {
        var width = paella.player.isFullScreen() == true ? $(window).width() : $(this.domElement).width();
        var relativeSize = new paella.RelativeVideoSize();
        var height = relativeSize.proportionalHeight(width);
        this.container.domElement.style.width = width + 'px';
        this.container.domElement.style.height = height + 'px';
        var containerHeight = paella.player.isFullScreen() == true ? $(window).height() : $(this.domElement).height();
        var newTop = containerHeight / 2 - height / 2;
        this.container.domElement.style.top = newTop + "px";
      }
    }, {
      key: "resizeLandscape",
      value: function resizeLandscape() {
        var height = paella.player.isFullScreen() == true ? $(window).height() : $(this.domElement).height();
        var relativeSize = new paella.RelativeVideoSize();
        var width = relativeSize.proportionalWidth(height);
        this.container.domElement.style.width = width + 'px';
        this.container.domElement.style.height = height + 'px';
        this.container.domElement.style.top = '0px';
      }
    }, {
      key: "onresize",
      value: function onresize() {
        _get(_getPrototypeOf(VideoContainer.prototype), "onresize", this).call(this);

        var relativeSize = new paella.RelativeVideoSize();
        var aspectRatio = relativeSize.aspectRatio();
        var width = paella.player.isFullScreen() == true ? $(window).width() : $(this.domElement).width();
        var height = paella.player.isFullScreen() == true ? $(window).height() : $(this.domElement).height();
        var containerAspectRatio = width / height;

        if (containerAspectRatio > aspectRatio) {
          this.resizeLandscape();
        } else {
          this.resizePortrait();
        } //paella.profiles.setProfile(paella.player.selectedProfile,false)

      } // the duration and the current time are returned taking into account the trimming, for example:
      //	trimming: { enabled: true, start: 10, end: 110 } 
      //	currentTime: 0,	> the actual time is 10
      //	duration: 100 > the actual duration is (at least) 110

    }, {
      key: "getVideoData",
      value: function getVideoData() {
        var _this94 = this;

        return new Promise(function (resolve, reject) {
          var videoData = {
            currentTime: 0,
            volume: 0,
            muted: _this94.muted,
            duration: 0,
            paused: false,
            audioTag: _this94.audioTag,
            trimming: {
              enabled: false,
              start: 0,
              end: 0
            }
          };

          _this94.currentTime().then(function (currentTime) {
            videoData.currentTime = currentTime;
            return _this94.volume();
          }).then(function (v) {
            videoData.volume = v;
            return _this94.duration();
          }).then(function (d) {
            videoData.duration = d;
            return _this94.paused();
          }).then(function (p) {
            videoData.paused = p;
            return _this94.trimming();
          }).then(function (trimming) {
            videoData.trimming = trimming;
            resolve(videoData);
          })["catch"](function (err) {
            return reject(err);
          });
        });
      }
    }, {
      key: "muted",
      get: function get() {
        return this._muted;
      }
    }, {
      key: "audioTag",
      get: function get() {
        return this._audioTag;
      }
    }, {
      key: "audioPlayer",
      get: function get() {
        return this._audioPlayer;
      }
    }]);

    return VideoContainer;
  }(paella.VideoContainerBase);

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


(function () {
  var PluginManager = /*#__PURE__*/function () {
    _createClass(PluginManager, [{
      key: "setupPlugin",
      value: function setupPlugin(plugin) {
        plugin.setup();
        this.enabledPlugins.push(plugin);

        if (eval("plugin instanceof paella.UIPlugin")) {
          plugin.checkVisibility();
        }
      }
    }, {
      key: "checkPluginsVisibility",
      value: function checkPluginsVisibility() {
        this.enabledPlugins.forEach(function (plugin) {
          if (eval("plugin instanceof paella.UIPlugin")) {
            plugin.checkVisibility();
          }
        });
      }
    }]);

    function PluginManager() {
      var _this95 = this;

      _classCallCheck(this, PluginManager);

      this.targets = null;
      this.pluginList = [];
      this.eventDrivenPlugins = [];
      this.enabledPlugins = [];
      this.doResize = true;
      this.targets = {};
      paella.events.bind(paella.events.loadPlugins, function (event) {
        _this95.loadPlugins("paella.DeferredLoadPlugin");
      });
      var timer = new paella.utils.Timer(function () {
        if (paella.player && paella.player.controls && _this95.doResize) paella.player.controls.onresize();
      }, 1000);
      timer.repeat = true;
    }

    _createClass(PluginManager, [{
      key: "setTarget",
      value: function setTarget(pluginType, target) {
        if (target.addPlugin) {
          this.targets[pluginType] = target;
        }
      }
    }, {
      key: "getTarget",
      value: function getTarget(pluginType) {
        // PluginManager can handle event-driven events:
        if (pluginType == "eventDriven") {
          return this;
        } else {
          var target = this.targets[pluginType];
          return target;
        }
      }
    }, {
      key: "registerPlugin",
      value: function registerPlugin(plugin) {
        // Registra los plugins en una lista y los ordena
        this.importLibraries(plugin);
        this.pluginList.push(plugin);
        this.pluginList.sort(function (a, b) {
          return a.getIndex() - b.getIndex();
        });
      }
    }, {
      key: "importLibraries",
      value: function importLibraries(plugin) {
        plugin.getDependencies().forEach(function (lib) {
          var script = document.createElement('script');
          script.type = "text/javascript";
          script.src = 'javascript/' + lib + '.js';
          document.head.appendChild(script);
        });
      } // callback => function(plugin,pluginConfig)

    }, {
      key: "loadPlugins",
      value: function loadPlugins(pluginBaseClass) {
        if (pluginBaseClass != undefined) {
          var This = this;
          this.foreach(function (plugin, config) {
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
    }, {
      key: "foreach",
      value: function foreach(callback) {
        var enablePluginsByDefault = false;
        var pluginsConfig = {};

        try {
          enablePluginsByDefault = paella.player.config.plugins.enablePluginsByDefault;
        } catch (e) {}

        try {
          pluginsConfig = paella.player.config.plugins.list;
        } catch (e) {}

        this.pluginList.forEach(function (plugin) {
          var name = plugin.getName();
          var config = pluginsConfig[name];

          if (!config) {
            config = {
              enabled: enablePluginsByDefault
            };
          }

          callback(plugin, config);
        });
      }
    }, {
      key: "addPlugin",
      value: function addPlugin(plugin) {
        var _this96 = this;

        // Prevent add a plugin twice
        if (plugin.__added__) return;
        plugin.__added__ = true;
        plugin.checkEnabled(function (isEnabled) {
          if (plugin.type == "eventDriven" && isEnabled) {
            paella.pluginManager.setupPlugin(plugin);

            _this96.eventDrivenPlugins.push(plugin);

            var events = plugin.getEvents();

            var eventBind = function eventBind(event, params) {
              plugin.onEvent(event.type, params);
            };

            for (var i = 0; i < events.length; ++i) {
              var eventName = events[i];
              paella.events.bind(eventName, eventBind);
            }
          }
        });
      }
    }, {
      key: "getPlugin",
      value: function getPlugin(name) {
        for (var i = 0; i < this.pluginList.length; ++i) {
          if (this.pluginList[i].getName() == name) return this.pluginList[i];
        }

        return null;
      }
    }, {
      key: "registerPlugins",
      value: function registerPlugins() {
        g_pluginCallbackList.forEach(function (pluginCallback) {
          var PluginClass = pluginCallback();
          var pluginInstance = new PluginClass();

          if (pluginInstance.getInstanceName()) {
            paella.plugins = paella.plugins || {};
            paella.plugins[pluginInstance.getInstanceName()] = pluginInstance;
          }

          paella.pluginManager.registerPlugin(pluginInstance);
        });
      }
    }]);

    return PluginManager;
  }();

  paella.PluginManager = PluginManager;
  paella.pluginManager = new paella.PluginManager();
  var g_pluginCallbackList = [];

  paella.addPlugin = function (cb) {
    g_pluginCallbackList.push(cb);
  };

  var Plugin = /*#__PURE__*/function () {
    function Plugin() {
      _classCallCheck(this, Plugin);
    }

    _createClass(Plugin, [{
      key: "isLoaded",
      value: function isLoaded() {
        return this.__loaded__;
      }
    }, {
      key: "getDependencies",
      value: function getDependencies() {
        return [];
      }
    }, {
      key: "load",
      value: function load(pluginManager) {
        if (this.__loaded__) return;
        this.__loaded__ = true;
        var target = pluginManager.getTarget(this.type);

        if (target && target.addPlugin) {
          target.addPlugin(this);
        }
      }
    }, {
      key: "getInstanceName",
      value: function getInstanceName() {
        return null;
      }
    }, {
      key: "getRootNode",
      value: function getRootNode(id) {
        return null;
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        onSuccess(true);
      }
    }, {
      key: "setup",
      value: function setup() {}
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 0;
      }
    }, {
      key: "getName",
      value: function getName() {
        return "";
      }
    }, {
      key: "type",
      get: function get() {
        return "";
      }
    }]);

    return Plugin;
  }();

  paella.Plugin = Plugin;

  var FastLoadPlugin = /*#__PURE__*/function (_paella$Plugin) {
    _inherits(FastLoadPlugin, _paella$Plugin);

    var _super22 = _createSuper(FastLoadPlugin);

    function FastLoadPlugin() {
      _classCallCheck(this, FastLoadPlugin);

      return _super22.apply(this, arguments);
    }

    return FastLoadPlugin;
  }(paella.Plugin);

  var EarlyLoadPlugin = /*#__PURE__*/function (_paella$Plugin2) {
    _inherits(EarlyLoadPlugin, _paella$Plugin2);

    var _super23 = _createSuper(EarlyLoadPlugin);

    function EarlyLoadPlugin() {
      _classCallCheck(this, EarlyLoadPlugin);

      return _super23.apply(this, arguments);
    }

    return EarlyLoadPlugin;
  }(paella.Plugin);

  var DeferredLoadPlugin = /*#__PURE__*/function (_paella$Plugin3) {
    _inherits(DeferredLoadPlugin, _paella$Plugin3);

    var _super24 = _createSuper(DeferredLoadPlugin);

    function DeferredLoadPlugin() {
      _classCallCheck(this, DeferredLoadPlugin);

      return _super24.apply(this, arguments);
    }

    return DeferredLoadPlugin;
  }(paella.Plugin);

  paella.FastLoadPlugin = FastLoadPlugin;
  paella.EarlyLoadPlugin = EarlyLoadPlugin;
  paella.DeferredLoadPlugin = DeferredLoadPlugin;

  function addMenuItemTabindex(plugin) {
    if (plugin.button.tabIndex > 0) {
      paella.tabIndex.insertAfter(plugin.button, plugin.menuContent.children);
    }
  }

  function removeMenuItemTabindexplugin(plugin) {
    if (plugin.button.tabIndex > 0) {
      paella.tabIndex.removeTabIndex(plugin.menuContent.children);
    }
  }

  function _hideContainer(identifier, container, swapFocus) {
    paella.events.trigger(paella.events.hidePopUp, {
      container: container
    });
    container.plugin.willHideContent();

    if (container.plugin.getButtonType() == paella.ButtonPlugin.type.menuButton) {
      removeMenuItemTabindexplugin(container.plugin);
    }

    $(container.element).hide();
    $(this.domElement).css({
      width: '0px'
    });
    container.button.className = container.button.className.replace(' selected', '');
    this.currentContainerId = -1;
    container.plugin.didHideContent();

    if (container.plugin.getButtonType() == paella.ButtonPlugin.type.menuButton && swapFocus) {
      $(container.button).focus();
    }
  }

  function _showContainer(identifier, container, button, swapFocus) {
    paella.events.trigger(paella.events.showPopUp, {
      container: container
    });
    container.plugin.willShowContent();
    container.button.className = container.button.className + ' selected';
    $(container.element).show();

    if (container.plugin.getButtonType() == paella.ButtonPlugin.type.menuButton) {
      addMenuItemTabindex(container.plugin);
    }

    var width = $(container.element).width();

    if (container.plugin.getAlignment() == 'right') {
      var right = $(button.parentElement).width() - $(button).position().left - $(button).width();
      $(this.domElement).css({
        width: width + 'px',
        right: right + 'px',
        left: ''
      });
    } else {
      var left = $(button).position().left;
      $(this.domElement).css({
        width: width + 'px',
        left: left + 'px',
        right: ''
      });
    }

    this.currentContainerId = identifier;

    if (container.plugin.getButtonType() == paella.ButtonPlugin.type.menuButton && container.plugin.menuContent.children.length > 0 && swapFocus) {
      $(container.plugin.menuContent.children[0]).focus();
    }

    container.plugin.didShowContent();
  }

  var PopUpContainer = /*#__PURE__*/function (_paella$DomNode8) {
    _inherits(PopUpContainer, _paella$DomNode8);

    var _super25 = _createSuper(PopUpContainer);

    function PopUpContainer(id, className) {
      var _this97;

      _classCallCheck(this, PopUpContainer);

      var style = {};
      _this97 = _super25.call(this, 'div', id, style);
      _this97.containers = null;
      _this97.currentContainerId = -1;
      _this97.domElement.className = className;
      _this97.containers = {};
      return _this97;
    }

    _createClass(PopUpContainer, [{
      key: "hideContainer",
      value: function hideContainer(identifier, button) {
        var swapFocus = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : false;
        var container = this.containers[identifier];

        if (container) {
          _hideContainer.apply(this, [identifier, container, swapFocus]);
        }
      }
    }, {
      key: "showContainer",
      value: function showContainer(identifier, button) {
        var swapFocus = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : false;
        var container = this.containers[identifier];

        if (container && this.currentContainerId != identifier && this.currentContainerId != -1) {
          var prevContainer = this.containers[this.currentContainerId];

          _hideContainer.apply(this, [this.currentContainerId, prevContainer, swapFocus]);

          _showContainer.apply(this, [identifier, container, button, swapFocus]);
        } else if (container && this.currentContainerId == identifier) {
          _hideContainer.apply(this, [identifier, container, swapFocus]);
        } else if (container) {
          _showContainer.apply(this, [identifier, container, button, swapFocus]);
        }
      }
    }, {
      key: "registerContainer",
      value: function registerContainer(identifier, domElement, button, plugin) {
        var containerInfo = {
          identifier: identifier,
          button: button,
          element: domElement,
          plugin: plugin
        };
        this.containers[identifier] = containerInfo;

        if (plugin.closeOnMouseOut && plugin.closeOnMouseOut()) {
          var popUpId = identifier;
          var btn = button;
          $(domElement).mouseleave(function (evt) {
            paella.player.controls.playbackControl().hidePopUp(popUpId, btn);
          });
        } // this.domElement.appendChild(domElement);


        $(domElement).hide();
        button.popUpIdentifier = identifier;
        button.sourcePlugin = plugin;
        $(button).click(function (event) {
          if (!this.plugin.isPopUpOpen()) {
            paella.player.controls.playbackControl().showPopUp(this.popUpIdentifier, this, false);
          } else {
            paella.player.controls.playbackControl().hidePopUp(this.popUpIdentifier, this, false);
          }
        });
        $(button).keypress(function (event) {
          if (event.keyCode == 13 && !this.plugin.isPopUpOpen()) {
            if (this.plugin.isPopUpOpen()) {
              paella.player.controls.playbackControl().hidePopUp(this.popUpIdentifier, this, true);
            } else {
              paella.player.controls.playbackControl().showPopUp(this.popUpIdentifier, this, true);
            }
          } else if (event.keyCode == 27) {
            paella.player.controls.playbackControl().hidePopUp(this.popUpIdentifier, this, true);
          }

          event.preventDefault();
        });
        $(button).keyup(function (event) {
          event.preventDefault();
        });
        plugin.containerManager = this;
      }
    }]);

    return PopUpContainer;
  }(paella.DomNode);

  paella.PopUpContainer = PopUpContainer;

  var TimelineContainer = /*#__PURE__*/function (_paella$PopUpContaine) {
    _inherits(TimelineContainer, _paella$PopUpContaine);

    var _super26 = _createSuper(TimelineContainer);

    function TimelineContainer() {
      _classCallCheck(this, TimelineContainer);

      return _super26.apply(this, arguments);
    }

    _createClass(TimelineContainer, [{
      key: "hideContainer",
      value: function hideContainer(identifier, button) {
        var container = this.containers[identifier];

        if (container && this.currentContainerId == identifier) {
          paella.events.trigger(paella.events.hidePopUp, {
            container: container
          });
          container.plugin.willHideContent();
          $(container.element).hide();
          container.button.className = container.button.className.replace(' selected', '');
          this.currentContainerId = -1;
          $(this.domElement).css({
            height: '0px'
          });
          container.plugin.didHideContent();
        }
      }
    }, {
      key: "showContainer",
      value: function showContainer(identifier, button) {
        var height = 0;
        var container = this.containers[identifier];

        if (container && this.currentContainerId != identifier && this.currentContainerId != -1) {
          var prevContainer = this.containers[this.currentContainerId];
          prevContainer.button.className = prevContainer.button.className.replace(' selected', '');
          container.button.className = container.button.className + ' selected';
          paella.events.trigger(paella.events.hidePopUp, {
            container: prevContainer
          });
          prevContainer.plugin.willHideContent();
          $(prevContainer.element).hide();
          prevContainer.plugin.didHideContent();
          paella.events.trigger(paella.events.showPopUp, {
            container: container
          });
          container.plugin.willShowContent();
          $(container.element).show();
          this.currentContainerId = identifier;
          height = $(container.element).height();
          $(this.domElement).css({
            height: height + 'px'
          });
          container.plugin.didShowContent();
        } else if (container && this.currentContainerId == identifier) {
          paella.events.trigger(paella.events.hidePopUp, {
            container: container
          });
          container.plugin.willHideContent();
          $(container.element).hide();
          container.button.className = container.button.className.replace(' selected', '');
          $(this.domElement).css({
            height: '0px'
          });
          this.currentContainerId = -1;
          container.plugin.didHideContent();
        } else if (container) {
          paella.events.trigger(paella.events.showPopUp, {
            container: container
          });
          container.plugin.willShowContent();
          container.button.className = container.button.className + ' selected';
          $(container.element).show();
          this.currentContainerId = identifier;
          height = $(container.element).height();
          $(this.domElement).css({
            height: height + 'px'
          });
          container.plugin.didShowContent();
        }
      }
    }]);

    return TimelineContainer;
  }(paella.PopUpContainer);

  paella.TimelineContainer = TimelineContainer;

  var UIPlugin = /*#__PURE__*/function (_paella$DeferredLoadP) {
    _inherits(UIPlugin, _paella$DeferredLoadP);

    var _super27 = _createSuper(UIPlugin);

    function UIPlugin() {
      _classCallCheck(this, UIPlugin);

      return _super27.apply(this, arguments);
    }

    _createClass(UIPlugin, [{
      key: "checkVisibility",
      value: function checkVisibility() {
        var modes = this.config.visibleOn || [paella.PaellaPlayer.mode.standard, paella.PaellaPlayer.mode.fullscreen, paella.PaellaPlayer.mode.embed];
        var visible = false;
        modes.forEach(function (m) {
          if (m == paella.player.getPlayerMode()) {
            visible = true;
          }
        });

        if (visible) {
          this.showUI();
        } else {
          this.hideUI();
        }
      }
    }, {
      key: "hideUI",
      value: function hideUI() {
        this.ui.setAttribute('aria-hidden', 'true');
        $(this.ui).hide();
      }
    }, {
      key: "showUI",
      value: function showUI() {
        var thisClass = this;
        paella.pluginManager.enabledPlugins.forEach(function (p) {
          if (p == thisClass) {
            thisClass.ui.setAttribute('aria-hidden', 'false');
            $(thisClass.ui).show();
          }
        });
      }
    }, {
      key: "ui",
      get: function get() {
        return this._ui;
      },
      set: function set(val) {
        this._ui = val;
      }
    }]);

    return UIPlugin;
  }(paella.DeferredLoadPlugin);

  paella.UIPlugin = UIPlugin;

  var ButtonPlugin = /*#__PURE__*/function (_paella$UIPlugin) {
    _inherits(ButtonPlugin, _paella$UIPlugin);

    var _super28 = _createSuper(ButtonPlugin);

    _createClass(ButtonPlugin, [{
      key: "type",
      get: function get() {
        return 'button';
      }
    }]);

    function ButtonPlugin() {
      var _this98;

      _classCallCheck(this, ButtonPlugin);

      _this98 = _super28.call(this);
      _this98.subclass = '';
      _this98.container = null;
      _this98.containerManager = null;
      _this98._domElement = null;
      return _this98;
    }

    _createClass(ButtonPlugin, [{
      key: "getAlignment",
      value: function getAlignment() {
        return 'left'; // or right
      } // Returns the button subclass.

    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "myButtonPlugin";
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return "";
      }
    }, {
      key: "addSubclass",
      value: function addSubclass($subclass) {
        $(this.container).addClass($subclass);
      }
    }, {
      key: "removeSubclass",
      value: function removeSubclass($subclass) {
        $(this.container).removeClass($subclass);
      }
    }, {
      key: "action",
      value: function action(button) {// Implement this if you want to do something when the user push the plugin button
      }
    }, {
      key: "getName",
      value: function getName() {
        return "ButtonPlugin";
      }
    }, {
      key: "getMinWindowSize",
      value: function getMinWindowSize() {
        return this.config.minWindowSize || 0;
      }
    }, {
      key: "buildContent",
      value: function buildContent(domElement) {// Override if your plugin
      }
    }, {
      key: "getMenuContent",
      value: function getMenuContent() {
        return [];
      }
    }, {
      key: "willShowContent",
      value: function willShowContent() {
        paella.log.debug(this.getName() + " willDisplayContent");
      }
    }, {
      key: "didShowContent",
      value: function didShowContent() {
        paella.log.debug(this.getName() + " didDisplayContent");
      }
    }, {
      key: "willHideContent",
      value: function willHideContent() {
        paella.log.debug(this.getName() + " willHideContent");
      }
    }, {
      key: "didHideContent",
      value: function didHideContent() {
        paella.log.debug(this.getName() + " didHideContent");
      }
    }, {
      key: "getButtonType",
      value: function getButtonType() {
        //return paella.ButtonPlugin.type.popUpButton;
        //return paella.ButtonPlugin.type.timeLineButton;
        //return paella.ButtonPlugin.type.menuButton;
        return paella.ButtonPlugin.type.actionButton;
      }
    }, {
      key: "getText",
      value: function getText() {
        return "";
      }
    }, {
      key: "getAriaLabel",
      value: function getAriaLabel() {
        return "";
      }
    }, {
      key: "setText",
      value: function setText(text) {
        this.container.innerHTML = '<span class="button-text">' + paella.AntiXSS.htmlEscape(text) + '</span>';

        if (this._i) {
          this.container.appendChild(this._i);
        }
      }
    }, {
      key: "hideButton",
      value: function hideButton() {
        this.hideUI();
      }
    }, {
      key: "showButton",
      value: function showButton() {
        this.showUI();
      } // Utility functions: do not override

    }, {
      key: "changeSubclass",
      value: function changeSubclass(newSubclass) {
        this.subclass = newSubclass;
        this.container.className = this.getClassName();
      }
    }, {
      key: "changeIconClass",
      value: function changeIconClass(newClass) {
        this._i.className = 'button-icon ' + newClass;
      }
    }, {
      key: "getClassName",
      value: function getClassName() {
        return paella.ButtonPlugin.kClassName + ' ' + this.getAlignment() + ' ' + this.subclass;
      }
    }, {
      key: "getContainerClassName",
      value: function getContainerClassName() {
        if (this.getButtonType() == paella.ButtonPlugin.type.timeLineButton) {
          return paella.ButtonPlugin.kTimeLineClassName + ' ' + this.getSubclass();
        } else if (this.getButtonType() == paella.ButtonPlugin.type.popUpButton) {
          return paella.ButtonPlugin.kPopUpClassName + ' ' + this.getSubclass();
        } else if (this.getButtonType() == paella.ButtonPlugin.type.menuButton) {
          return paella.ButtonPlugin.kPopUpClassName + ' menuContainer ' + this.getSubclass();
        }
      }
    }, {
      key: "setToolTip",
      value: function setToolTip(message) {
        this.button.setAttribute("title", message);
        this.button.setAttribute("aria-label", message);
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return "";
      }
    }, {
      key: "isPopUpOpen",
      value: function isPopUpOpen() {
        return this.button.popUpIdentifier == this.containerManager.currentContainerId;
      }
    }, {
      key: "getExpandableContent",
      value: function getExpandableContent() {
        return null;
      }
    }, {
      key: "expand",
      value: function expand() {
        if (this._expand) {
          $(this._expand).show();
        }
      }
    }, {
      key: "contract",
      value: function contract() {
        if (this._expand) {
          $(this._expand).hide();
        }
      }
    }, {
      key: "rebuildMenu",
      value: function rebuildMenu() {
        var _this99 = this;

        function getButtonItem(itemData, plugin) {
          var elem = document.createElement('div');
          elem.className = itemData.className + " menuItem";

          if (itemData["default"]) {
            elem.className += " selected";
          }

          elem.id = itemData.id;
          elem.innerText = itemData.title;

          if (itemData.icon) {
            elem.style.backgroundImage = "url(".concat(itemData.icon, ")");
            $(elem).addClass('icon');
          }

          elem.data = {
            itemData: itemData,
            plugin: plugin
          };

          function menuItemSelect(button, data, event) {
            data.plugin.menuItemSelected(data.itemData);
            var buttons = button.parentElement ? button.parentElement.children : [];

            for (var i = 0; i < buttons.length; ++i) {
              $(buttons[i]).removeClass('selected');
            }

            $(button).addClass('selected');
          }

          $(elem).click(function (event) {
            menuItemSelect(this, this.data, event);
          });
          $(elem).keypress(function (event) {
            if (event.keyCode == 13) {
              menuItemSelect(this, this.data, event);
            }

            event.preventDefault();
          });
          $(elem).keyup(function (event) {
            if (event.keyCode == 27) {
              paella.player.controls.hidePopUp(this.data.plugin.getName(), null, true);
            }

            event.preventDefault();
          });
          return elem;
        }

        var menuContent = this.getMenuContent();
        this.menuContent.innerHTML = "";
        menuContent.forEach(function (menuItem) {
          _this99.menuContent.appendChild(getButtonItem(menuItem, _this99));
        });
      }
    }, {
      key: "menuContent",
      set: function set(domElem) {
        this._domElement = domElem;
      },
      get: function get() {
        return this._domElement;
      }
    }], [{
      key: "BuildPluginButton",
      value: function BuildPluginButton(plugin, id) {
        plugin.subclass = plugin.getSubclass();
        var elem = document.createElement('div');
        var ariaLabel = plugin.getAriaLabel() || paella.utils.dictionary.translate(plugin.config.ariaLabel) || "";

        if (ariaLabel != "") {
          elem = document.createElement('button');
        }

        elem.className = plugin.getClassName();
        elem.id = id;
        var buttonText = document.createElement('span');
        buttonText.className = "button-text";
        buttonText.innerHTML = paella.AntiXSS.htmlEscape(plugin.getText());
        buttonText.plugin = plugin;
        elem.appendChild(buttonText);

        if (ariaLabel) {
          var tabIndex = paella.tabIndex.next;
          elem.setAttribute("tabindex", tabIndex);
          elem.setAttribute("aria-label", ariaLabel);
        }

        elem.setAttribute("alt", "");
        elem.plugin = plugin;
        plugin.button = elem;
        plugin.container = elem;
        plugin.ui = elem;
        plugin.setToolTip(plugin.getDefaultToolTip());
        var icon = document.createElement('i');
        icon.className = 'button-icon ' + plugin.getIconClass();
        icon.plugin = plugin;
        elem.appendChild(icon);
        plugin._i = icon;

        function onAction(self) {
          paella.userTracking.log("paella:button:action", self.plugin.getName());
          self.plugin.action(self);
        }

        $(elem).click(function (event) {
          onAction(this);
        });
        $(elem).keypress(function (event) {
          onAction(this);
          event.preventDefault();
        });
        $(elem).focus(function (event) {
          plugin.expand();
        });
        return elem;
      }
    }, {
      key: "BuildPluginExpand",
      value: function BuildPluginExpand(plugin, id) {
        var expandContent = plugin.getExpandableContent();

        if (expandContent) {
          var expand = document.createElement('span');
          expand.plugin = plugin;
          expand.className = 'expandable-content ' + plugin.getClassName();
          plugin._expand = expand;
          expand.appendChild(expandContent);
          $(plugin._expand).hide();
          return expand;
        }

        return null;
      }
    }, {
      key: "BuildPluginPopUp",
      value: function BuildPluginPopUp(parent, plugin, id) {
        plugin.subclass = plugin.getSubclass();
        var elem = document.createElement('div');
        parent.appendChild(elem);
        elem.className = plugin.getContainerClassName();
        elem.id = id;
        elem.plugin = plugin;
        plugin.buildContent(elem);
        return elem;
      }
    }, {
      key: "BuildPluginMenu",
      value: function BuildPluginMenu(parent, plugin, id) {
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
    }]);

    return ButtonPlugin;
  }(paella.UIPlugin);

  paella.ButtonPlugin = ButtonPlugin;
  paella.ButtonPlugin.alignment = {
    left: 'left',
    right: 'right'
  };
  paella.ButtonPlugin.kClassName = 'buttonPlugin';
  paella.ButtonPlugin.kPopUpClassName = 'buttonPluginPopUp';
  paella.ButtonPlugin.kTimeLineClassName = 'buttonTimeLine';
  paella.ButtonPlugin.type = {
    actionButton: 1,
    popUpButton: 2,
    timeLineButton: 3,
    menuButton: 4
  };

  var VideoOverlayButtonPlugin = /*#__PURE__*/function (_paella$ButtonPlugin) {
    _inherits(VideoOverlayButtonPlugin, _paella$ButtonPlugin);

    var _super29 = _createSuper(VideoOverlayButtonPlugin);

    function VideoOverlayButtonPlugin() {
      _classCallCheck(this, VideoOverlayButtonPlugin);

      return _super29.apply(this, arguments);
    }

    _createClass(VideoOverlayButtonPlugin, [{
      key: "getSubclass",
      // Returns the button subclass.
      value: function getSubclass() {
        return "myVideoOverlayButtonPlugin" + " " + this.getAlignment();
      }
    }, {
      key: "action",
      value: function action(button) {// Implement this if you want to do something when the user push the plugin button
      }
    }, {
      key: "getName",
      value: function getName() {
        return "VideoOverlayButtonPlugin";
      }
    }, {
      key: "type",
      get: function get() {
        return 'videoOverlayButton';
      }
    }, {
      key: "tabIndex",
      get: function get() {
        return -1;
      }
    }]);

    return VideoOverlayButtonPlugin;
  }(paella.ButtonPlugin);

  paella.VideoOverlayButtonPlugin = VideoOverlayButtonPlugin;

  var EventDrivenPlugin = /*#__PURE__*/function (_paella$EarlyLoadPlug) {
    _inherits(EventDrivenPlugin, _paella$EarlyLoadPlug);

    var _super30 = _createSuper(EventDrivenPlugin);

    _createClass(EventDrivenPlugin, [{
      key: "type",
      get: function get() {
        return 'eventDriven';
      }
    }]);

    function EventDrivenPlugin() {
      var _this100;

      _classCallCheck(this, EventDrivenPlugin);

      _this100 = _super30.call(this);

      var events = _this100.getEvents();

      for (var i = 0; i < events.length; ++i) {
        var event = events[i];

        if (event == paella.events.loadStarted) {
          _this100.onEvent(paella.events.loadStarted);
        }
      }

      return _this100;
    }

    _createClass(EventDrivenPlugin, [{
      key: "getEvents",
      value: function getEvents() {
        return [];
      }
    }, {
      key: "onEvent",
      value: function onEvent(eventType, params) {}
    }, {
      key: "getName",
      value: function getName() {
        return "EventDrivenPlugin";
      }
    }]);

    return EventDrivenPlugin;
  }(paella.EarlyLoadPlugin);

  paella.EventDrivenPlugin = EventDrivenPlugin;
})();

(function () {
  var VideoCanvas = /*#__PURE__*/function () {
    function VideoCanvas(stream) {
      _classCallCheck(this, VideoCanvas);

      this._stream = stream;
    }

    _createClass(VideoCanvas, [{
      key: "loadVideo",
      value: function loadVideo(videoPlugin, stream) {
        return Promise.reject(new Error("Not implemented"));
      }
    }, {
      key: "allowZoom",
      value: function allowZoom() {
        return true;
      }
    }]);

    return VideoCanvas;
  }();

  paella.VideoCanvas = VideoCanvas;

  function initWebGLCanvas() {
    if (!paella.WebGLCanvas) {
      var WebGLCanvas = /*#__PURE__*/function (_bg$app$WindowControl) {
        _inherits(WebGLCanvas, _bg$app$WindowControl);

        var _super31 = _createSuper(WebGLCanvas);

        function WebGLCanvas(stream) {
          var _this101;

          _classCallCheck(this, WebGLCanvas);

          _this101 = _super31.call(this);
          _this101._stream = stream;
          return _this101;
        }

        _createClass(WebGLCanvas, [{
          key: "loaded",
          value: function loaded() {
            var _this102 = this;

            return new Promise(function (resolve) {
              var checkLoaded = function checkLoaded() {
                if (_this102.video) {
                  resolve(_this102);
                } else {
                  setTimeout(checkLoaded, 100);
                }
              };

              checkLoaded();
            });
          }
        }, {
          key: "loadVideo",
          value: function loadVideo(videoPlugin, stream) {
            return Promise.reject(new Error("Not implemented"));
          }
        }, {
          key: "allowZoom",
          value: function allowZoom() {
            return false;
          } // WebGL engine functions

        }, {
          key: "registerPlugins",
          value: function registerPlugins() {
            bg.base.Loader.RegisterPlugin(new bg.base.TextureLoaderPlugin());
            bg.base.Loader.RegisterPlugin(new bg.base.VideoTextureLoaderPlugin());
            bg.base.Loader.RegisterPlugin(new bg.base.VWGLBLoaderPlugin());
          }
        }, {
          key: "loadVideoTexture",
          value: function loadVideoTexture() {
            return bg.base.Loader.Load(this.gl, this.stream.src);
          }
        }, {
          key: "buildVideoSurface",
          value: function buildVideoSurface(sceneRoot, videoTexture) {
            var sphere = bg.scene.PrimitiveFactory.Sphere(this.gl, 1, 50);
            var sphereNode = new bg.scene.Node(this.gl);
            sphereNode.addComponent(sphere);
            sphere.getMaterial(0).texture = videoTexture;
            sphere.getMaterial(0).lightEmission = 0;
            sphere.getMaterial(0).lightEmissionMaskInvert = false;
            sphere.getMaterial(0).cullFace = false;
            sphereNode.addComponent(new bg.scene.Transform(bg.Matrix4.Scale(1, -1, 1)));
            sceneRoot.addChild(sphereNode);
          }
        }, {
          key: "buildCamera",
          value: function buildCamera() {
            var cameraNode = new bg.scene.Node(this.gl, "Camera");
            var camera = new bg.scene.Camera();
            cameraNode.addComponent(camera);
            cameraNode.addComponent(new bg.scene.Transform());
            var projection = new bg.scene.OpticalProjectionStrategy();
            projection.far = 100;
            projection.focalLength = 55;
            camera.projectionStrategy = projection;
            var oc = new bg.manipulation.OrbitCameraController();
            oc.maxPitch = 90;
            oc.minPitch = -90;
            oc.maxDistance = 0;
            oc.minDistance = 0;
            this._cameraController = oc;
            cameraNode.addComponent(oc);
            return cameraNode;
          }
        }, {
          key: "buildScene",
          value: function buildScene() {
            var _this103 = this;

            this._root = new bg.scene.Node(this.gl, "Root node");
            this.registerPlugins();
            this.loadVideoTexture().then(function (texture) {
              _this103._texture = texture;

              _this103.buildVideoSurface(_this103._root, texture);
            });
            var lightNode = new bg.scene.Node(this.gl, "Light");
            var light = new bg.base.Light();
            light.ambient = bg.Color.White();
            light.diffuse = bg.Color.Black();
            light.specular = bg.Color.Black();
            lightNode.addComponent(new bg.scene.Light(light));

            this._root.addChild(lightNode);

            var cameraNode = this.buildCamera();
            this._camera = cameraNode.component("bg.scene.Camera");

            this._root.addChild(cameraNode);
          }
        }, {
          key: "init",
          value: function init() {
            bg.Engine.Set(new bg.webgl1.Engine(this.gl));
            this.buildScene();
            this._renderer = bg.render.Renderer.Create(this.gl, bg.render.RenderPath.FORWARD);
            this._inputVisitor = new bg.scene.InputVisitor();
          }
        }, {
          key: "frame",
          value: function frame(delta) {
            if (this.texture) {
              this.texture.update();
            }

            this._renderer.frame(this._root, delta);

            this.postReshape();
          }
        }, {
          key: "display",
          value: function display() {
            this._renderer.display(this._root, this._camera);
          }
        }, {
          key: "reshape",
          value: function reshape(width, height) {
            this._camera.viewport = new bg.Viewport(0, 0, width, height);

            if (!this._camera.projectionStrategy) {
              this._camera.projection.perspective(60, this._camera.viewport.aspectRatio, 0.1, 100);
            }
          }
        }, {
          key: "mouseDrag",
          value: function mouseDrag(evt) {
            this._inputVisitor.mouseDrag(this._root, evt);

            this.postRedisplay();
          }
        }, {
          key: "mouseWheel",
          value: function mouseWheel(evt) {
            this._inputVisitor.mouseWheel(this._root, evt);

            this.postRedisplay();
          }
        }, {
          key: "touchMove",
          value: function touchMove(evt) {
            this._inputVisitor.touchMove(this._root, evt);

            this.postRedisplay();
          }
        }, {
          key: "mouseDown",
          value: function mouseDown(evt) {
            this._inputVisitor.mouseDown(this._root, evt);
          }
        }, {
          key: "touchStar",
          value: function touchStar(evt) {
            this._inputVisitor.touchStar(this._root, evt);
          }
        }, {
          key: "mouseUp",
          value: function mouseUp(evt) {
            this._inputVisitor.mouseUp(this._root, evt);
          }
        }, {
          key: "mouseMove",
          value: function mouseMove(evt) {
            this._inputVisitor.mouseMove(this._root, evt);
          }
        }, {
          key: "mouseOut",
          value: function mouseOut(evt) {
            this._inputVisitor.mouseOut(this._root, evt);
          }
        }, {
          key: "touchEnd",
          value: function touchEnd(evt) {
            this._inputVisitor.touchEnd(this._root, evt);
          }
        }, {
          key: "stream",
          get: function get() {
            return this._stream;
          }
        }, {
          key: "video",
          get: function get() {
            return this.texture ? this.texture.video : null;
          }
        }, {
          key: "camera",
          get: function get() {
            return this._camera;
          }
        }, {
          key: "texture",
          get: function get() {
            return this._texture;
          }
        }]);

        return WebGLCanvas;
      }(bg.app.WindowController);

      paella.WebGLCanvas = WebGLCanvas;
    }
  }

  function buildVideoCanvas(stream) {
    if (!paella.WebGLCanvas) {
      var WebGLCanvas = /*#__PURE__*/function (_bg$app$WindowControl2) {
        _inherits(WebGLCanvas, _bg$app$WindowControl2);

        var _super32 = _createSuper(WebGLCanvas);

        function WebGLCanvas(stream) {
          var _this104;

          _classCallCheck(this, WebGLCanvas);

          _this104 = _super32.call(this);
          _this104._stream = stream;
          return _this104;
        }

        _createClass(WebGLCanvas, [{
          key: "allowZoom",
          value: function allowZoom() {
            return false;
          }
        }, {
          key: "loaded",
          value: function loaded() {
            var _this105 = this;

            return new Promise(function (resolve) {
              var checkLoaded = function checkLoaded() {
                if (_this105.video) {
                  resolve(_this105);
                } else {
                  setTimeout(checkLoaded, 100);
                }
              };

              checkLoaded();
            });
          }
        }, {
          key: "registerPlugins",
          value: function registerPlugins() {
            bg.base.Loader.RegisterPlugin(new bg.base.TextureLoaderPlugin());
            bg.base.Loader.RegisterPlugin(new bg.base.VideoTextureLoaderPlugin());
            bg.base.Loader.RegisterPlugin(new bg.base.VWGLBLoaderPlugin());
          }
        }, {
          key: "loadVideoTexture",
          value: function loadVideoTexture() {
            return bg.base.Loader.Load(this.gl, this.stream.src);
          }
        }, {
          key: "buildVideoSurface",
          value: function buildVideoSurface(sceneRoot, videoTexture) {
            var sphere = bg.scene.PrimitiveFactory.Sphere(this.gl, 1, 50);
            var sphereNode = new bg.scene.Node(this.gl);
            sphereNode.addComponent(sphere);
            sphere.getMaterial(0).texture = videoTexture;
            sphere.getMaterial(0).lightEmission = 0;
            sphere.getMaterial(0).lightEmissionMaskInvert = false;
            sphere.getMaterial(0).cullFace = false;
            sphereNode.addComponent(new bg.scene.Transform(bg.Matrix4.Scale(1, -1, 1)));
            sceneRoot.addChild(sphereNode);
          }
        }, {
          key: "buildCamera",
          value: function buildCamera() {
            var cameraNode = new bg.scene.Node(this.gl, "Camera");
            var camera = new bg.scene.Camera();
            cameraNode.addComponent(camera);
            cameraNode.addComponent(new bg.scene.Transform());
            var projection = new bg.scene.OpticalProjectionStrategy();
            projection.far = 100;
            projection.focalLength = 55;
            camera.projectionStrategy = projection;
            var oc = new bg.manipulation.OrbitCameraController();
            oc.maxPitch = 90;
            oc.minPitch = -90;
            oc.maxDistance = 0;
            oc.minDistance = 0;
            this._cameraController = oc;
            cameraNode.addComponent(oc);
            return cameraNode;
          }
        }, {
          key: "buildScene",
          value: function buildScene() {
            var _this106 = this;

            this._root = new bg.scene.Node(this.gl, "Root node");
            this.registerPlugins();
            this.loadVideoTexture().then(function (texture) {
              _this106._texture = texture;

              _this106.buildVideoSurface(_this106._root, texture);
            });
            var lightNode = new bg.scene.Node(this.gl, "Light");
            var light = new bg.base.Light();
            light.ambient = bg.Color.White();
            light.diffuse = bg.Color.Black();
            light.specular = bg.Color.Black();
            lightNode.addComponent(new bg.scene.Light(light));

            this._root.addChild(lightNode);

            var cameraNode = this.buildCamera();
            this._camera = cameraNode.component("bg.scene.Camera");

            this._root.addChild(cameraNode);
          }
        }, {
          key: "init",
          value: function init() {
            bg.Engine.Set(new bg.webgl1.Engine(this.gl));
            this.buildScene();
            this._renderer = bg.render.Renderer.Create(this.gl, bg.render.RenderPath.FORWARD);
            this._inputVisitor = new bg.scene.InputVisitor();
          }
        }, {
          key: "frame",
          value: function frame(delta) {
            if (this.texture) {
              this.texture.update();
            }

            this._renderer.frame(this._root, delta);

            this.postReshape();
          }
        }, {
          key: "display",
          value: function display() {
            this._renderer.display(this._root, this._camera);
          }
        }, {
          key: "reshape",
          value: function reshape(width, height) {
            this._camera.viewport = new bg.Viewport(0, 0, width, height);

            if (!this._camera.projectionStrategy) {
              this._camera.projection.perspective(60, this._camera.viewport.aspectRatio, 0.1, 100);
            }
          }
        }, {
          key: "mouseDrag",
          value: function mouseDrag(evt) {
            this._inputVisitor.mouseDrag(this._root, evt);

            this.postRedisplay();
          }
        }, {
          key: "mouseWheel",
          value: function mouseWheel(evt) {
            this._inputVisitor.mouseWheel(this._root, evt);

            this.postRedisplay();
          }
        }, {
          key: "touchMove",
          value: function touchMove(evt) {
            this._inputVisitor.touchMove(this._root, evt);

            this.postRedisplay();
          }
        }, {
          key: "mouseDown",
          value: function mouseDown(evt) {
            this._inputVisitor.mouseDown(this._root, evt);
          }
        }, {
          key: "touchStar",
          value: function touchStar(evt) {
            this._inputVisitor.touchStar(this._root, evt);
          }
        }, {
          key: "mouseUp",
          value: function mouseUp(evt) {
            this._inputVisitor.mouseUp(this._root, evt);
          }
        }, {
          key: "mouseMove",
          value: function mouseMove(evt) {
            this._inputVisitor.mouseMove(this._root, evt);
          }
        }, {
          key: "mouseOut",
          value: function mouseOut(evt) {
            this._inputVisitor.mouseOut(this._root, evt);
          }
        }, {
          key: "touchEnd",
          value: function touchEnd(evt) {
            this._inputVisitor.touchEnd(this._root, evt);
          }
        }, {
          key: "stream",
          get: function get() {
            return this._stream;
          }
        }, {
          key: "video",
          get: function get() {
            return this.texture ? this.texture.video : null;
          }
        }, {
          key: "camera",
          get: function get() {
            return this._camera;
          }
        }, {
          key: "texture",
          get: function get() {
            return this._texture;
          }
        }]);

        return WebGLCanvas;
      }(bg.app.WindowController);

      paella.WebGLCanvas = WebGLCanvas;
    }

    return paella.WebGLCanvas;
  }

  var g_canvasCallbacks = {};

  paella.addCanvasPlugin = function (canvasType, webglSupport, mouseEventsSupport, canvasPluginCallback) {
    g_canvasCallbacks[canvasType] = {
      callback: canvasPluginCallback,
      webglSupport: webglSupport,
      mouseEventsSupport: mouseEventsSupport
    };
  };

  function loadWebGLDeps() {
    return new Promise(function (resolve) {
      if (!window.$paella_bg) {
        paella.require("".concat(paella.baseUrl, "javascript/bg2e-es2015.js")).then(function () {
          window.$paella_bg = bg;
          buildVideoCanvas(); // loadWebGLDeps();

          resolve(window.$paella_bg);
        });
      } else {
        resolve(window.$paella_bg);
      }
    });
  }

  function loadCanvasPlugin(canvasType) {
    return new Promise(function (resolve, reject) {
      var callbackData = g_canvasCallbacks[canvasType];

      if (callbackData) {
        (callbackData.webglSupport ? loadWebGLDeps() : Promise.resolve()).then(function () {
          resolve(callbackData.callback());
        })["catch"](function (err) {
          reject(err);
        });
      } else {
        reject(new Error("No such canvas type: \"".concat(canvasType, "\"")));
      }
    });
  }

  paella.getVideoCanvas = function (type) {
    return new Promise(function (resolve, reject) {
      var canvasData = g_canvasCallbacks[type];

      if (!canvasData) {
        reject(new Error("No such canvas type: " + type));
      } else {
        if (canvasData.webglSupport) {
          loadWebGLDeps().then(function () {
            resolve(canvasData.callback());
          });
        } else {
          resolve(canvasData.callback());
        }
      }
    });
  };

  paella.getVideoCanvasData = function (type) {
    return g_canvasCallbacks[type];
  }; // Standard <video> canvas


  paella.addCanvasPlugin("video", false, false, function () {
    return /*#__PURE__*/function (_paella$VideoCanvas) {
      _inherits(VideoCanvas, _paella$VideoCanvas);

      var _super33 = _createSuper(VideoCanvas);

      function VideoCanvas(stream) {
        _classCallCheck(this, VideoCanvas);

        return _super33.call(this, stream);
      }

      _createClass(VideoCanvas, [{
        key: "loadVideo",
        value: function loadVideo(videoPlugin, stream) {
          var doLoadCallback = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : null;
          return new Promise(function (resolve, reject) {
            doLoadCallback = doLoadCallback || function (video) {
              return new Promise(function (cbResolve, cbReject) {
                var sourceElem = video.querySelector('source');

                if (!sourceElem) {
                  sourceElem = document.createElement('source');
                  video.appendChild(sourceElem);
                }

                if (video._posterFrame) {
                  video.setAttribute("poster", video._posterFrame);
                }

                sourceElem.src = stream.src;
                sourceElem.type = stream.type;
                video.load();
                video.playbackRate = videoPlugin._playbackRate || 1;
                cbResolve();
              });
            };

            doLoadCallback(videoPlugin.video).then(function () {
              resolve(stream);
            })["catch"](function (err) {
              reject(err);
            });
          });
        }
      }]);

      return VideoCanvas;
    }(paella.VideoCanvas);
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


(function () {
  var CaptionParserManager = /*#__PURE__*/function () {
    _createClass(CaptionParserManager, [{
      key: "addPlugin",
      value: function addPlugin(plugin) {
        var self = this;
        var ext = plugin.ext;

        if ((Array.isArray && Array.isArray(ext) || ext instanceof Array) == false) {
          ext = [ext];
        }

        if (ext.length == 0) {
          paella.log.debug("No extension provided by the plugin " + plugin.getName());
        } else {
          paella.log.debug("New captionParser added: " + plugin.getName());
          ext.forEach(function (f) {
            self._formats[f] = plugin;
          });
        }
      }
    }]);

    function CaptionParserManager() {
      _classCallCheck(this, CaptionParserManager);

      this._formats = {};
      paella.pluginManager.setTarget('captionParser', this);
    }

    return CaptionParserManager;
  }();

  var captionParserManager = new CaptionParserManager();

  var SearchCallback = /*#__PURE__*/function (_paella$utils$AsyncLo2) {
    _inherits(SearchCallback, _paella$utils$AsyncLo2);

    var _super34 = _createSuper(SearchCallback);

    function SearchCallback(caption, text) {
      var _this107;

      _classCallCheck(this, SearchCallback);

      _this107 = _super34.call(this);
      _this107.name = "captionSearchCallback";
      _this107.caption = caption;
      _this107.text = text;
      return _this107;
    }

    _createClass(SearchCallback, [{
      key: "load",
      value: function load(onSuccess, onError) {
        var _this108 = this;

        this.caption.search(this.text, function (err, result) {
          if (err) {
            onError();
          } else {
            _this108.result = result;
            onSuccess();
          }
        });
      }
    }]);

    return SearchCallback;
  }(paella.utils.AsyncLoaderCallback);

  paella.captions = {
    parsers: {},
    _captions: {},
    _activeCaption: undefined,
    addCaptions: function addCaptions(captions) {
      var cid = captions._captionsProvider + ':' + captions._id;
      this._captions[cid] = captions;
      paella.events.trigger(paella.events.captionAdded, cid);
    },
    getAvailableLangs: function getAvailableLangs() {
      var ret = [];
      var self = this;
      Object.keys(this._captions).forEach(function (k) {
        var c = self._captions[k];
        ret.push({
          id: k,
          lang: c._lang
        });
      });
      return ret;
    },
    getCaptions: function getCaptions(cid) {
      if (cid && this._captions[cid]) {
        return this._captions[cid];
      }

      return undefined;
    },
    getActiveCaptions: function getActiveCaptions(cid) {
      return this._activeCaption;
    },
    setActiveCaptions: function setActiveCaptions(cid) {
      this._activeCaption = this.getCaptions(cid);

      if (this._activeCaption != undefined) {
        paella.events.trigger(paella.events.captionsEnabled, cid);
      } else {
        paella.events.trigger(paella.events.captionsDisabled);
      }

      return this._activeCaption;
    },
    getCaptionAtTime: function getCaptionAtTime(cid, time) {
      var c = this.getCaptions(cid);

      if (c != undefined) {
        return c.getCaptionAtTime(time);
      }

      return undefined;
    },
    search: function search(text, next) {
      var self = this;
      var asyncLoader = new paella.utils.AsyncLoader();
      this.getAvailableLangs().forEach(function (l) {
        asyncLoader.addCallback(new SearchCallback(self.getCaptions(l.id), text));
      });
      asyncLoader.load(function () {
        var res = [];
        Object.keys(asyncLoader.callbackArray).forEach(function (k) {
          res = res.concat(asyncLoader.getCallback(k).result);
        });
        if (next) next(false, res);
      }, function () {
        if (next) next(true);
      });
    }
  };

  var Caption = /*#__PURE__*/function () {
    function Caption(id, format, url, lang, next) {
      _classCallCheck(this, Caption);

      this._id = id;
      this._format = format;
      this._url = url;
      this._captions = undefined;
      this._index = undefined;

      if (typeof lang == "string") {
        lang = {
          code: lang,
          txt: lang
        };
      }

      this._lang = lang;
      this._captionsProvider = "downloadCaptionsProvider";
      this.reloadCaptions(next);
    }

    _createClass(Caption, [{
      key: "canEdit",
      value: function canEdit(next) {
        // next(err, canEdit)
        next(false, false);
      }
    }, {
      key: "goToEdit",
      value: function goToEdit() {}
    }, {
      key: "reloadCaptions",
      value: function reloadCaptions(next) {
        var self = this;
        jQuery.ajax({
          url: self._url,
          cache: false,
          type: 'get',
          dataType: "text",
          xhrFields: {
            withCredentials: true
          }
        }).then(function (dataRaw) {
          var parser = captionParserManager._formats[self._format];

          if (parser == undefined) {
            paella.log.debug("Error adding captions: Format not supported!");

            if (!paella.player.videoContainer) {
              paella.log.debug("Video container is not ready, delaying parse until next reload");
              return;
            }

            paella.player.videoContainer.duration(true).then(function (duration) {
              self._captions = [{
                id: 0,
                begin: 0,
                end: duration,
                content: paella.utils.dictionary.translate("Error! Captions format not supported.")
              }];

              if (next) {
                next(true);
              }
            });
          } else {
            parser.parse(dataRaw, self._lang.code, function (err, c) {
              if (!err) {
                self._captions = c;
                self._index = lunr(function () {
                  var thisLunr = this;
                  thisLunr.ref('id');
                  thisLunr.field('content', {
                    boost: 10
                  });

                  self._captions.forEach(function (cap) {
                    thisLunr.add({
                      id: cap.id,
                      content: cap.content
                    });
                  });
                });
              }

              if (next) {
                next(err);
              }
            });
          }
        }).fail(function (error) {
          paella.log.debug("Error loading captions: " + self._url);

          if (next) {
            next(true);
          }
        });
      }
    }, {
      key: "getCaptions",
      value: function getCaptions() {
        return this._captions;
      }
    }, {
      key: "getCaptionAtTime",
      value: function getCaptionAtTime(time) {
        if (this._captions != undefined) {
          for (var i = 0; i < this._captions.length; ++i) {
            var l_cap = this._captions[i];

            if (l_cap.begin <= time && l_cap.end >= time) {
              return l_cap;
            }
          }
        }

        return undefined;
      }
    }, {
      key: "getCaptionById",
      value: function getCaptionById(id) {
        if (this._captions != undefined) {
          for (var i = 0; i < this._captions.length; ++i) {
            var l_cap = this._captions[i];

            if (l_cap.id == id) {
              return l_cap;
            }
          }
        }

        return undefined;
      }
    }, {
      key: "search",
      value: function search(txt, next) {
        var _this109 = this;

        var self = this;

        if (this._index == undefined) {
          if (next) {
            next(true, "Error. No captions found.");
          }
        } else {
          var results = [];
          paella.player.videoContainer.trimming().then(function (trimming) {
            _this109._index.search(txt).forEach(function (s) {
              var c = self.getCaptionById(s.ref);

              if (trimming.enabled && (c.end < trimming.start || c.begin > trimming.end)) {
                return;
              }

              results.push({
                time: c.begin,
                content: c.content,
                score: s.score
              });
            });

            if (next) {
              next(false, results);
            }
          });
        }
      }
    }]);

    return Caption;
  }();

  paella.captions.Caption = Caption;

  var CaptionParserPlugIn = /*#__PURE__*/function (_paella$FastLoadPlugi) {
    _inherits(CaptionParserPlugIn, _paella$FastLoadPlugi);

    var _super35 = _createSuper(CaptionParserPlugIn);

    function CaptionParserPlugIn() {
      _classCallCheck(this, CaptionParserPlugIn);

      return _super35.apply(this, arguments);
    }

    _createClass(CaptionParserPlugIn, [{
      key: "getIndex",
      value: function getIndex() {
        return -1;
      }
    }, {
      key: "parse",
      value: function parse(content, lang, next) {
        throw new Error('paella.CaptionParserPlugIn#parse must be overridden by subclass');
      }
    }, {
      key: "type",
      get: function get() {
        return 'captionParser';
      }
    }, {
      key: "ext",
      get: function get() {
        if (!this._ext) {
          this._ext = [];
        }

        return this._ext;
      }
    }]);

    return CaptionParserPlugIn;
  }(paella.FastLoadPlugin);

  paella.CaptionParserPlugIn = CaptionParserPlugIn;
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


(function () {
  var searchServiceManager = {
    _plugins: [],
    addPlugin: function addPlugin(plugin) {
      this._plugins.push(plugin);
    },
    initialize: function initialize() {
      paella.pluginManager.setTarget('SearchServicePlugIn', this);
    }
  };

  var SearchCallback = /*#__PURE__*/function (_paella$utils$AsyncLo3) {
    _inherits(SearchCallback, _paella$utils$AsyncLo3);

    var _super36 = _createSuper(SearchCallback);

    function SearchCallback(plugin, text) {
      var _this110;

      _classCallCheck(this, SearchCallback);

      _this110 = _super36.call(this);
      _this110.name = "searchCallback";
      _this110.plugin = plugin;
      _this110.text = text;
      return _this110;
    }

    _createClass(SearchCallback, [{
      key: "load",
      value: function load(onSuccess, onError) {
        var _this111 = this;

        this.plugin.search(this.text, function (err, result) {
          if (err) {
            onError();
          } else {
            _this111.result = result;
            onSuccess();
          }
        });
      }
    }]);

    return SearchCallback;
  }(paella.utils.AsyncLoaderCallback);

  paella.searchService = {
    search: function search(text, next) {
      var asyncLoader = new paella.utils.AsyncLoader();
      paella.userTracking.log("paella:searchService:search", text);

      searchServiceManager._plugins.forEach(function (p) {
        asyncLoader.addCallback(new SearchCallback(p, text));
      });

      asyncLoader.load(function () {
        var res = [];
        Object.keys(asyncLoader.callbackArray).forEach(function (k) {
          res = res.concat(asyncLoader.getCallback(k).result);
        });
        if (next) next(false, res);
      }, function () {
        if (next) next(true);
      });
    }
  };

  var SearchServicePlugIn = /*#__PURE__*/function (_paella$FastLoadPlugi2) {
    _inherits(SearchServicePlugIn, _paella$FastLoadPlugi2);

    var _super37 = _createSuper(SearchServicePlugIn);

    function SearchServicePlugIn() {
      _classCallCheck(this, SearchServicePlugIn);

      return _super37.apply(this, arguments);
    }

    _createClass(SearchServicePlugIn, [{
      key: "getIndex",
      value: function getIndex() {
        return -1;
      }
    }, {
      key: "search",
      value: function search(text, next) {
        throw new Error('paella.SearchServicePlugIn#search must be overridden by subclass');
      }
    }, {
      key: "type",
      get: function get() {
        return 'SearchServicePlugIn';
      }
    }]);

    return SearchServicePlugIn;
  }(paella.FastLoadPlugin);

  paella.SearchServicePlugIn = SearchServicePlugIn;
  searchServiceManager.initialize();
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


(function () {
  var userTrackingManager = {
    _plugins: [],
    addPlugin: function addPlugin(plugin) {
      var _this112 = this;

      plugin.checkEnabled(function (isEnabled) {
        if (isEnabled) {
          plugin.setup();

          _this112._plugins.push(plugin);
        }
      });
    },
    initialize: function initialize() {
      paella.pluginManager.setTarget('userTrackingSaverPlugIn', this);
    }
  };
  paella.userTracking = {};
  userTrackingManager.initialize();

  var SaverPlugIn = /*#__PURE__*/function (_paella$FastLoadPlugi3) {
    _inherits(SaverPlugIn, _paella$FastLoadPlugi3);

    var _super38 = _createSuper(SaverPlugIn);

    function SaverPlugIn() {
      _classCallCheck(this, SaverPlugIn);

      return _super38.apply(this, arguments);
    }

    _createClass(SaverPlugIn, [{
      key: "getIndex",
      value: function getIndex() {
        return -1;
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        onSuccess(true);
      }
    }, {
      key: "log",
      value: function log(event, params) {
        throw new Error('paella.userTracking.SaverPlugIn#log must be overridden by subclass');
      }
    }, {
      key: "type",
      get: function get() {
        return 'userTrackingSaverPlugIn';
      }
    }]);

    return SaverPlugIn;
  }(paella.FastLoadPlugin);

  paella.userTracking.SaverPlugIn = SaverPlugIn;
  var evsentsToLog = {};

  paella.userTracking.log = function (event, params) {
    if (evsentsToLog[event] != undefined) {
      evsentsToLog[event].cancel();
    }

    evsentsToLog[event] = new paella.utils.Timer(function (timer) {
      userTrackingManager._plugins.forEach(function (p) {
        p.log(event, params);
      });

      delete evsentsToLog[event];
    }, 500);
  }; //////////////////////////////////////////////////////////
  // Log automatic events
  //////////////////////////////////////////////////////////
  // Log simple events


  [paella.events.play, paella.events.pause, paella.events.endVideo, paella.events.showEditor, paella.events.hideEditor, paella.events.enterFullscreen, paella.events.exitFullscreen, paella.events.loadComplete].forEach(function (event) {
    paella.events.bind(event, function (ev, params) {
      paella.userTracking.log(event);
    });
  }); // Log show/hide PopUp

  [paella.events.showPopUp, paella.events.hidePopUp].forEach(function (event) {
    paella.events.bind(event, function (ev, params) {
      paella.userTracking.log(event, params.identifier);
    });
  }); // Log captions Events

  [// paella.events.captionAdded, 
  paella.events.captionsEnabled, paella.events.captionsDisabled].forEach(function (event) {
    paella.events.bind(event, function (ev, params) {
      var log;

      if (params != undefined) {
        var c = paella.captions.getCaptions(params);
        log = {
          id: params,
          lang: c._lang,
          url: c._url
        };
      }

      paella.userTracking.log(event, log);
    });
  }); // Log setProfile

  [paella.events.setProfile].forEach(function (event) {
    paella.events.bind(event, function (ev, params) {
      paella.userTracking.log(event, params.profileName);
    });
  }); // Log seek events

  [paella.events.seekTo, paella.events.seekToTime].forEach(function (event) {
    paella.events.bind(event, function (ev, params) {
      var log;

      try {
        JSON.stringify(params);
        log = params;
      } catch (e) {}

      paella.userTracking.log(event, log);
    });
  }); // Log param events

  [paella.events.setVolume, paella.events.resize, paella.events.setPlaybackRate, paella.events.qualityChanged].forEach(function (event) {
    paella.events.bind(event, function (ev, params) {
      var log;

      try {
        JSON.stringify(params);
        log = params;
      } catch (e) {}

      paella.userTracking.log(event, log);
    });
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


(function () {
  var TimeControl = /*#__PURE__*/function (_paella$DomNode9) {
    _inherits(TimeControl, _paella$DomNode9);

    var _super39 = _createSuper(TimeControl);

    function TimeControl(id) {
      var _this113;

      _classCallCheck(this, TimeControl);

      _this113 = _super39.call(this, 'div', id, {
        left: "0%"
      });
      _this113.domElement.className = 'timeControlOld';
      _this113.domElement.className = 'timeControl'; //this.domElement.innerText = "0:00:00";

      var thisClass = _assertThisInitialized(_this113);

      paella.events.bind(paella.events.timeupdate, function (event, params) {
        thisClass.onTimeUpdate(params);
      });
      return _this113;
    }

    _createClass(TimeControl, [{
      key: "onTimeUpdate",
      value: function onTimeUpdate(memo) {
        this.domElement.innerText = this.secondsToHours(parseInt(memo.currentTime));
      }
    }, {
      key: "secondsToHours",
      value: function secondsToHours(sec_numb) {
        var hours = Math.floor(sec_numb / 3600);
        var minutes = Math.floor((sec_numb - hours * 3600) / 60);
        var seconds = sec_numb - hours * 3600 - minutes * 60;

        if (hours < 10) {
          hours = "0" + hours;
        }

        if (minutes < 10) {
          minutes = "0" + minutes;
        }

        if (seconds < 10) {
          seconds = "0" + seconds;
        }

        return hours + ':' + minutes + ':' + seconds;
      }
    }]);

    return TimeControl;
  }(paella.DomNode);

  paella.TimeControl = TimeControl;

  var PlaybackCanvasPlugin = /*#__PURE__*/function (_paella$DeferredLoadP2) {
    _inherits(PlaybackCanvasPlugin, _paella$DeferredLoadP2);

    var _super40 = _createSuper(PlaybackCanvasPlugin);

    _createClass(PlaybackCanvasPlugin, [{
      key: "type",
      get: function get() {
        return 'playbackCanvas';
      }
    }, {
      key: "playbackBarCanvas",
      get: function get() {
        return this._playbackBarCanvas;
      }
    }]);

    function PlaybackCanvasPlugin() {
      _classCallCheck(this, PlaybackCanvasPlugin);

      return _super40.call(this);
    }

    _createClass(PlaybackCanvasPlugin, [{
      key: "drawCanvas",
      value: function drawCanvas(context, width, height, videoData) {// videoData: {
        //		duration: fullDuration,
        //		trimming: {
        //			enabled: true | false,
        //			start: trimmingStart,
        //			end: trimmingEnd,
        //			duration: trimmedDuration | duration if trimming is not enabled
        //		}
        //	}
      }
    }]);

    return PlaybackCanvasPlugin;
  }(paella.DeferredLoadPlugin);

  paella.PlaybackCanvasPlugin = PlaybackCanvasPlugin;

  var PlaybackBarCanvas = /*#__PURE__*/function () {
    function PlaybackBarCanvas(canvasElem) {
      _classCallCheck(this, PlaybackBarCanvas);

      this._parent = canvasElem;
      this._plugins = [];
      paella.pluginManager.setTarget('playbackCanvas', this);
    }

    _createClass(PlaybackBarCanvas, [{
      key: "addPlugin",
      value: function addPlugin(plugin) {
        var _this114 = this;

        plugin._playbackBarCanvas = this;
        plugin.checkEnabled(function (isEnabled) {
          if (isEnabled) {
            plugin.setup();

            _this114._plugins.push(plugin);
          }
        });
      }
    }, {
      key: "resize",
      value: function resize(w, h) {
        this.canvas[0].width = w;
        this.canvas[0].height = h;
        this.canvas[1].width = w;
        this.canvas[1].height = h;
        this.drawCanvas();
      }
    }, {
      key: "drawCanvas",
      value: function drawCanvas() {
        var _this115 = this;

        var duration = 0;
        paella.player.videoContainer.duration(true).then(function (d) {
          duration = d;
          return paella.player.videoContainer.trimming();
        }).then(function (trimming) {
          var trimmedDuration = 0;

          if (trimming.enabled) {
            trimmedDuration = trimming.end - trimming.start;
          }

          var videoData = {
            duration: duration,
            trimming: {
              enabled: trimming.enabled,
              start: trimming.start,
              end: trimming.end,
              duration: trimming.enabled ? trimming.end - trimming.start : duration
            }
          };
          var ctx = _this115.context;
          var w = _this115.width;
          var h = _this115.height;

          _this115.clearCanvas();

          _this115._plugins.forEach(function (plugin) {
            plugin.drawCanvas(ctx, w, h, videoData);
          });
        });
      }
    }, {
      key: "clearCanvas",
      value: function clearCanvas() {
        var clear = function clear(ctx, w, h) {
          ctx.clearRect(0, 0, w, h);
        };

        clear(this.context[0], this.width, this.height);
        clear(this.context[1], this.width, this.height);
      }
    }, {
      key: "parent",
      get: function get() {
        return this._parent;
      }
    }, {
      key: "canvas",
      get: function get() {
        var _this116 = this;

        if (!this._canvas) {
          var createCanvas = function createCanvas(index) {
            var result = document.createElement("canvas");
            result.className = "playerContainer_controls_playback_playbackBar_canvas layer_" + index;
            result.id = "playerContainer_controls_playback_playbackBar_canvas_" + index;
            result.width = $(_this116.parent).width();
            result.height = $(_this116.parent).height();
            return result;
          };

          this._canvas = [createCanvas(0), createCanvas(1)];
          $(this._parent).prepend(this._canvas[0]);
          $(this._parent).append(this._canvas[1]);
        }

        return this._canvas;
      }
    }, {
      key: "context",
      get: function get() {
        if (!this._context) {
          this._context = [this.canvas[0].getContext("2d"), this.canvas[1].getContext("2d")];
        }

        return this._context;
      }
    }, {
      key: "width",
      get: function get() {
        return this.canvas[0].width;
      }
    }, {
      key: "height",
      get: function get() {
        return this.canvas[0].height;
      }
    }]);

    return PlaybackBarCanvas;
  }();

  var PlaybackBar = /*#__PURE__*/function (_paella$DomNode10) {
    _inherits(PlaybackBar, _paella$DomNode10);

    var _super41 = _createSuper(PlaybackBar);

    function PlaybackBar(id) {
      var _this117;

      _classCallCheck(this, PlaybackBar);

      var style = {};
      _this117 = _super41.call(this, 'div', id, style);
      _this117.playbackFullId = '';
      _this117.updatePlayBar = true;
      _this117.timeControlId = '';
      _this117._images = null;
      _this117._prev = null;
      _this117._next = null;
      _this117._videoLength = null;
      _this117._lastSrc = null;
      _this117._aspectRatio = 1.777777778;
      _this117._hasSlides = null;
      _this117._imgNode = null;
      _this117._canvas = null;
      _this117.domElement.className = "playbackBar";

      _this117.domElement.setAttribute("alt", ""); //this.domElement.setAttribute("title", "Timeline Slider");


      _this117.domElement.setAttribute("aria-label", "Timeline Slider");

      _this117.domElement.setAttribute("role", "slider");

      _this117.domElement.setAttribute("aria-valuemin", "0");

      _this117.domElement.setAttribute("aria-valuemax", "100");

      _this117.domElement.setAttribute("aria-valuenow", "0");

      _this117.domElement.setAttribute("tabindex", paella.tabIndex.next);

      $(_this117.domElement).keyup(function (event) {
        var currentTime = 0;
        var duration = 0;
        paella.player.videoContainer.currentTime().then(function (t) {
          currentTime = t;
          return paella.player.videoContainer.duration();
        }).then(function (d) {
          duration = d;
          var curr, selectedPosition;

          switch (event.keyCode) {
            case 37:
              //Left
              curr = 100 * currentTime / duration;
              selectedPosition = curr - 5;
              paella.player.videoContainer.seekTo(selectedPosition);
              break;

            case 39:
              //Right
              curr = 100 * currentTime / duration;
              selectedPosition = curr + 5;
              paella.player.videoContainer.seekTo(selectedPosition);
              break;
          }
        });
      });
      _this117.playbackFullId = id + "_full";
      _this117.timeControlId = id + "_timeControl";
      var playbackFull = new paella.DomNode('div', _this117.playbackFullId, {
        width: '0%'
      });
      playbackFull.domElement.className = "playbackBarFull";

      _this117.addNode(playbackFull);

      _this117.addNode(new paella.TimeControl(_this117.timeControlId));

      var thisClass = _assertThisInitialized(_this117);

      paella.events.bind(paella.events.timeupdate, function (event, params) {
        thisClass.onTimeUpdate(params);
      });
      $(_this117.domElement).bind('mousedown', function (event) {
        paella.utils.mouseManager.down(thisClass, event);
        event.stopPropagation();
      });
      $(playbackFull.domElement).bind('mousedown', function (event) {
        paella.utils.mouseManager.down(thisClass, event);
        event.stopPropagation();
      });

      if (!paella.utils.userAgent.browser.IsMobileVersion) {
        $(_this117.domElement).bind('mousemove', function (event) {
          thisClass.movePassive(event);
          paella.utils.mouseManager.move(event);
        });
        $(playbackFull.domElement).bind('mousemove', function (event) {
          paella.utils.mouseManager.move(event);
        });
        $(_this117.domElement).bind("mouseout", function (event) {
          thisClass.mouseOut(event);
        });
      }

      _this117.domElement.addEventListener('touchstart', function (event) {
        paella.utils.mouseManager.down(thisClass, event);
        event.stopPropagation();
      }, false);

      _this117.domElement.addEventListener('touchmove', function (event) {
        thisClass.movePassive(event);
        paella.utils.mouseManager.move(event);
      }, false);

      _this117.domElement.addEventListener('touchend', function (event) {
        paella.utils.mouseManager.up(event);
        thisClass.clearTimeOverlay();
      }, false);

      $(_this117.domElement).bind('mouseup', function (event) {
        paella.utils.mouseManager.up(event);
      });
      $(playbackFull.domElement).bind('mouseup', function (event) {
        paella.utils.mouseManager.up(event);
      });

      if (paella.player.isLiveStream()) {
        $(_this117.domElement).hide();
      }

      paella.events.bind(paella.events.seekAvailabilityChanged, function (e, data) {
        if (data.type != paella.SeekType.DISABLED) {
          $(playbackFull.domElement).removeClass("disabled");
        } else {
          $(playbackFull.domElement).addClass("disabled");
        }
      });
      _this117._canvas = new PlaybackBarCanvas(_this117.domElement);
      return _this117;
    }

    _createClass(PlaybackBar, [{
      key: "mouseOut",
      value: function mouseOut(event) {
        this.clearTimeOverlay();
      }
    }, {
      key: "clearTimeOverlay",
      value: function clearTimeOverlay() {
        if (this._hasSlides) {
          $("#divTimeImageOverlay").remove();
        } else {
          $("#divTimeOverlay").remove();
        }
      }
    }, {
      key: "movePassive",
      value: function movePassive(event) {
        var This = this;

        function updateTimePreview(duration, trimming) {
          // CONTROLS_BAR POSITON
          var p = $(This.domElement);
          var pos = p.offset();
          var width = p.width();
          var clientX = event.touches ? event.touches[0].clientX : event.clientX;
          var left = clientX - pos.left;
          left = left < 0 ? 0 : left;
          var position = left * 100 / width; // GET % OF THE STREAM

          var time = position * duration / 100;

          if (trimming.enabled) {
            time += trimming.start;
          }

          var hou = Math.floor((time - trimming.start) / 3600) % 24;
          hou = ("00" + hou).slice(hou.toString().length);
          var min = Math.floor((time - trimming.start) / 60) % 60;
          min = ("00" + min).slice(min.toString().length);
          var sec = Math.floor((time - trimming.start) % 60);
          sec = ("00" + sec).slice(sec.toString().length);
          var timestr = hou + ":" + min + ":" + sec; // CREATING THE OVERLAY

          if (This._hasSlides) {
            if ($("#divTimeImageOverlay").length == 0) This.setupTimeImageOverlay(timestr, pos.top, width);else {
              $("#divTimeOverlay")[0].innerText = timestr; //IF CREATED, UPDATE TIME AND IMAGE
            } // CALL IMAGEUPDATE

            This.imageUpdate(time);
          } else {
            if ($("#divTimeOverlay").length == 0) {
              This.setupTimeOnly(timestr, pos.top, width);
            } else {
              $("#divTimeOverlay")[0].innerText = timestr;
            }
          } // UPDATE POSITION IMAGE OVERLAY


          if (This._hasSlides) {
            var ancho = $("#divTimeImageOverlay").width();
            var posx = clientX - ancho / 2;

            if (clientX > ancho / 2 + pos.left && clientX < pos.left + width - ancho / 2) {
              // LEFT
              $("#divTimeImageOverlay").css("left", posx); // CENTER THE DIV HOVER THE MOUSE
            } else if (clientX < width / 2) $("#divTimeImageOverlay").css("left", pos.left);else $("#divTimeImageOverlay").css("left", pos.left + width - ancho);
          } // UPDATE POSITION TIME OVERLAY


          var ancho2 = $("#divTimeOverlay").width();
          var posx2 = clientX - ancho2 / 2;

          if (clientX > ancho2 / 2 + pos.left && clientX < pos.left + width - ancho2 / 2) {
            $("#divTimeOverlay").css("left", posx2); // CENTER THE DIV HOVER THE MOUSE
          } else if (clientX < width / 2) $("#divTimeOverlay").css("left", pos.left);else $("#divTimeOverlay").css("left", pos.left + width - ancho2 - 2);

          if (This._hasSlides) {
            $("#divTimeImageOverlay").css("bottom", $('.playbackControls').height());
          }
        }

        var duration = 0;
        paella.player.videoContainer.duration().then(function (d) {
          duration = d;
          return paella.player.videoContainer.trimming();
        }).then(function (trimming) {
          updateTimePreview(duration, trimming);
        });
      }
    }, {
      key: "imageSetup",
      value: function imageSetup() {
        var _this118 = this;

        return new Promise(function (resolve) {
          paella.player.videoContainer.duration().then(function (duration) {
            //  BRING THE IMAGE ARRAY TO LOCAL
            _this118._images = {};
            var n = paella.initDelegate.initParams.videoLoader.frameList;

            if (!n || Object.keys(n).length === 0) {
              _this118._hasSlides = false;
              return;
            } else {
              _this118._hasSlides = true;
            }

            _this118._images = n; // COPY TO LOCAL

            _this118._videoLength = duration; // SORT KEYS FOR SEARCH CLOSEST

            _this118._keys = Object.keys(_this118._images);
            _this118._keys = _this118._keys.sort(function (a, b) {
              return parseInt(a) - parseInt(b);
            }); // SORT FRAME NUMBERS STRINGS
            //NEXT

            _this118._next = 0;
            _this118._prev = 0;
            resolve();
          });
        });
      }
    }, {
      key: "imageUpdate",
      value: function imageUpdate(sec) {
        var src = $("#imgOverlay").attr('src');
        $(this._imgNode).show();

        if (sec > this._next || sec < this._prev) {
          src = this.getPreviewImageSrc(sec);

          if (src) {
            this._lastSrc = src;
            $("#imgOverlay").attr('src', src); // UPDATING IMAGE
          } else {
            this.hideImg();
          }
        } // RELOAD IF OUT OF INTERVAL
        else {
            if (src != undefined) {
              return;
            } else {
              $("#imgOverlay").attr('src', this._lastSrc);
            } // KEEP LAST IMAGE

          }
      }
    }, {
      key: "hideImg",
      value: function hideImg() {
        $(this._imgNode).hide();
      }
    }, {
      key: "getPreviewImageSrc",
      value: function getPreviewImageSrc(sec) {
        var keys = Object.keys(this._images);
        keys.push(sec);
        keys.sort(function (a, b) {
          return parseInt(a) - parseInt(b);
        });
        var n = keys.indexOf(sec) - 1;
        n = n > 0 ? n : 0;
        var i = keys[n];
        var next = keys[n + 2];
        var prev = keys[n];
        next = next == undefined ? keys.length - 1 : parseInt(next);
        this._next = next;
        prev = prev == undefined ? 0 : parseInt(prev);
        this._prev = prev;
        i = parseInt(i);

        if (this._images[i]) {
          return this._images[i].url || this._images[i].url;
        } else return false;
      }
    }, {
      key: "setupTimeImageOverlay",
      value: function setupTimeImageOverlay(time_str, top, width) {
        var div = document.createElement("div");
        div.className = "divTimeImageOverlay";
        div.id = "divTimeImageOverlay";
        var aux = Math.round(width / 10);
        div.style.width = Math.round(aux * self._aspectRatio) + "px"; //KEEP ASPECT RATIO 4:3
        //div.style.height = Math.round(aux)+"px";

        if (this._hasSlides) {
          var img = document.createElement("img");
          img.className = "imgOverlay";
          img.id = "imgOverlay";
          this._imgNode = img;
          div.appendChild(img);
        }

        var div2 = document.createElement("div");
        div2.className = "divTimeOverlay";
        div2.style.top = top - 20 + "px";
        div2.id = "divTimeOverlay";
        div2.innerText = time_str;
        div.appendChild(div2); //CHILD OF CONTROLS_BAR

        $(this.domElement).parent().append(div);
      }
    }, {
      key: "setupTimeOnly",
      value: function setupTimeOnly(time_str, top, width) {
        var div2 = document.createElement("div");
        div2.className = "divTimeOverlay";
        div2.style.top = top - 20 + "px";
        div2.id = "divTimeOverlay";
        div2.innerText = time_str; //CHILD OF CONTROLS_BAR

        $(this.domElement).parent().append(div2);
      }
    }, {
      key: "playbackFull",
      value: function playbackFull() {
        return this.getNode(this.playbackFullId);
      }
    }, {
      key: "timeControl",
      value: function timeControl() {
        return this.getNode(this.timeControlId);
      }
    }, {
      key: "setPlaybackPosition",
      value: function setPlaybackPosition(percent) {
        this.playbackFull().domElement.style.width = percent + '%';
      }
    }, {
      key: "isSeeking",
      value: function isSeeking() {
        return !this.updatePlayBar;
      }
    }, {
      key: "onTimeUpdate",
      value: function onTimeUpdate(memo) {
        if (this.updatePlayBar) {
          var currentTime = memo.currentTime;
          var duration = memo.duration;
          this.setPlaybackPosition(currentTime * 100 / duration);
        }
      }
    }, {
      key: "down",
      value: function down(event, x, y) {
        this.updatePlayBar = false;
        this.move(event, x, y);
      }
    }, {
      key: "move",
      value: function move(event, x, y) {
        var width = $(this.domElement).width();
        var selectedPosition = x - $(this.domElement).offset().left; // pixels

        if (selectedPosition < 0) {
          selectedPosition = 0;
        } else if (selectedPosition > width) {
          selectedPosition = 100;
        } else {
          selectedPosition = selectedPosition * 100 / width; // percent
        }

        this.setPlaybackPosition(selectedPosition);
      }
    }, {
      key: "up",
      value: function up(event, x, y) {
        var width = $(this.domElement).width();
        var selectedPosition = x - $(this.domElement).offset().left; // pixels

        if (selectedPosition < 0) {
          selectedPosition = 0;
        } else if (selectedPosition > width) {
          selectedPosition = 100;
        } else {
          selectedPosition = selectedPosition * 100 / width; // percent
        }

        paella.player.videoContainer.seekTo(selectedPosition);
        this.updatePlayBar = true;
      }
    }, {
      key: "onresize",
      value: function onresize() {
        this.imageSetup();
        var elem = $(this.domElement);

        this._canvas.resize(elem.width(), elem.height());
      }
    }]);

    return PlaybackBar;
  }(paella.DomNode);

  paella.PlaybackBar = PlaybackBar;

  var PlaybackControl = /*#__PURE__*/function (_paella$DomNode11) {
    _inherits(PlaybackControl, _paella$DomNode11);

    var _super42 = _createSuper(PlaybackControl);

    _createClass(PlaybackControl, [{
      key: "addPlugin",
      value: function addPlugin(plugin) {
        var _this120 = this;

        var id = 'buttonPlugin' + this.buttonPlugins.length;
        this.buttonPlugins.push(plugin);
        var button = paella.ButtonPlugin.BuildPluginButton(plugin, id);
        button.plugin = plugin;
        var expand = paella.ButtonPlugin.BuildPluginExpand(plugin, id);
        plugin.button = button;
        plugin._expandElement = expand;
        this.pluginsContainer.domElement.appendChild(button);

        if (expand) {
          var This = this;
          $(button).mouseover(function (evt) {
            evt.target.plugin.expand();
            This._expandedPlugin = evt.target.plugin;
          });
          this.pluginsContainer.domElement.appendChild(expand);
        }

        $(button).hide();
        plugin.checkEnabled(function (isEnabled) {
          var parent;

          if (isEnabled) {
            $(plugin.button).show();
            paella.pluginManager.setupPlugin(plugin);
            var id = 'buttonPlugin' + _this120.buttonPlugins.length;

            if (plugin.getButtonType() == paella.ButtonPlugin.type.popUpButton) {
              parent = _this120.popUpPluginContainer.domElement;
              var popUpContent = paella.ButtonPlugin.BuildPluginPopUp(parent, plugin, id + '_container');

              _this120.popUpPluginContainer.registerContainer(plugin.getName(), popUpContent, button, plugin);
            } else if (plugin.getButtonType() == paella.ButtonPlugin.type.timeLineButton) {
              parent = _this120.timeLinePluginContainer.domElement;
              var timeLineContent = paella.ButtonPlugin.BuildPluginPopUp(parent, plugin, id + '_timeline');

              _this120.timeLinePluginContainer.registerContainer(plugin.getName(), timeLineContent, button, plugin);
            } else if (plugin.getButtonType() == paella.ButtonPlugin.type.menuButton) {
              parent = _this120.popUpPluginContainer.domElement;
              var popUpContent = paella.ButtonPlugin.BuildPluginMenu(parent, plugin, id + '_container');

              _this120.popUpPluginContainer.registerContainer(plugin.getName(), popUpContent, button, plugin);
            }
          } else {
            _this120.pluginsContainer.domElement.removeChild(plugin.button);
          }
        });
      }
    }]);

    function PlaybackControl(id) {
      var _this119;

      _classCallCheck(this, PlaybackControl);

      var style = {};
      _this119 = _super42.call(this, 'div', id, style);
      _this119.playbackBarId = '';
      _this119.pluginsContainer = null;
      _this119._popUpPluginContainer = null;
      _this119._timeLinePluginContainer = null;
      _this119.playbackPluginsWidth = 0;
      _this119.popupPluginsWidth = 0;
      _this119.minPlaybackBarSize = 120;
      _this119.playbackBarInstance = null;
      _this119.buttonPlugins = [];
      _this119.domElement.className = 'playbackControls';
      _this119.playbackBarId = id + '_playbackBar';

      var thisClass = _assertThisInitialized(_this119);

      _this119.pluginsContainer = new paella.DomNode('div', id + '_playbackBarPlugins');
      _this119.pluginsContainer.domElement.className = 'playbackBarPlugins';

      _this119.pluginsContainer.domElement.setAttribute("role", "toolbar");

      _this119.addNode(_this119.pluginsContainer);

      _this119.addNode(new paella.PlaybackBar(_this119.playbackBarId));

      paella.pluginManager.setTarget('button', _assertThisInitialized(_this119));
      $(window).mousemove(function (evt) {
        if (_this119._expandedPlugin && $(window).height() - evt.clientY > 50) {
          _this119._expandedPlugin.contract();

          _this119._expandPlugin = null;
        }
      });
      return _this119;
    }

    _createClass(PlaybackControl, [{
      key: "showPopUp",
      value: function showPopUp(identifier, button) {
        var swapFocus = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : false;
        this.popUpPluginContainer.showContainer(identifier, button, swapFocus);
        this.timeLinePluginContainer.showContainer(identifier, button, swapFocus);
        this.hideCrossTimelinePopupButtons(identifier, this.popUpPluginContainer, this.timeLinePluginContainer, button, swapFocus);
      } // Hide popUpPluginContainer when a timeLinePluginContainer popup opens, and visa versa

    }, {
      key: "hideCrossTimelinePopupButtons",
      value: function hideCrossTimelinePopupButtons(identifier, popupContainer, timelineContainer, button) {
        var swapFocus = arguments.length > 4 && arguments[4] !== undefined ? arguments[4] : true;
        var containerToHide = null;

        if (popupContainer.containers[identifier] && timelineContainer.containers[timelineContainer.currentContainerId]) {
          containerToHide = timelineContainer;
        } else if (timelineContainer.containers[identifier] && popupContainer.containers[popupContainer.currentContainerId]) {
          containerToHide = popupContainer;
        }

        if (containerToHide) {
          var hideId = containerToHide.currentContainerId;
          var hidePugin = paella.pluginManager.getPlugin(hideId);

          if (hidePugin) {
            containerToHide.hideContainer(hideId, hidePugin.button, swapFocus);
          }
        }
      }
    }, {
      key: "hidePopUp",
      value: function hidePopUp(identifier, button) {
        var swapFocus = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : true;
        this.popUpPluginContainer.hideContainer(identifier, button, swapFocus);
        this.timeLinePluginContainer.hideContainer(identifier, button, swapFocus);
      }
    }, {
      key: "playbackBar",
      value: function playbackBar() {
        if (this.playbackBarInstance == null) {
          this.playbackBarInstance = this.getNode(this.playbackBarId);
        }

        return this.playbackBarInstance;
      }
    }, {
      key: "onresize",
      value: function onresize() {
        var windowSize = $(this.domElement).width();
        paella.log.debug("resize playback bar (width=" + windowSize + ")");

        for (var i = 0; i < this.buttonPlugins.length; ++i) {
          var plugin = this.buttonPlugins[i];
          var minSize = plugin.getMinWindowSize();

          if (minSize > 0 && windowSize < minSize) {
            plugin.hideUI();
          } else {
            plugin.checkVisibility();
          }
        }

        this.getNode(this.playbackBarId).onresize();
      }
    }, {
      key: "popUpPluginContainer",
      get: function get() {
        if (!this._popUpPluginContainer) {
          this._popUpPluginContainer = new paella.PopUpContainer(this.identifier + '_popUpPluginContainer', 'popUpPluginContainer');
          this.addNode(this._popUpPluginContainer);
        }

        return this._popUpPluginContainer;
      }
    }, {
      key: "timeLinePluginContainer",
      get: function get() {
        if (!this._timeLinePluginContainer) {
          this._timeLinePluginContainer = new paella.TimelineContainer(this.identifier + '_timelinePluginContainer', 'timelinePluginContainer');
          this.addNode(this._timeLinePluginContainer);
        }

        return this._timeLinePluginContainer;
      }
    }]);

    return PlaybackControl;
  }(paella.DomNode);

  paella.PlaybackControl = PlaybackControl;

  var ControlsContainer = /*#__PURE__*/function (_paella$DomNode12) {
    _inherits(ControlsContainer, _paella$DomNode12);

    var _super43 = _createSuper(ControlsContainer);

    _createClass(ControlsContainer, [{
      key: "addPlugin",
      value: function addPlugin(plugin) {
        var id = 'videoOverlayButtonPlugin' + this.buttonPlugins.length;
        this.buttonPlugins.push(plugin);
        var button = paella.ButtonPlugin.BuildPluginButton(plugin, id);
        this.videoOverlayButtons.domElement.appendChild(button);
        plugin.button = button;
        $(button).hide();
        plugin.checkEnabled(function (isEnabled) {
          if (isEnabled) {
            $(plugin.button).show();
            paella.pluginManager.setupPlugin(plugin);
          }
        });
      }
    }]);

    function ControlsContainer(id) {
      var _this121;

      _classCallCheck(this, ControlsContainer);

      _this121 = _super43.call(this, 'div', id);
      _this121.playbackControlId = '';
      _this121.editControlId = '';
      _this121.isEnabled = true;
      _this121.autohideTimer = null;
      _this121.hideControlsTimeMillis = 3000;
      _this121.playbackControlInstance = null;
      _this121.videoOverlayButtons = null;
      _this121.buttonPlugins = [];
      _this121._hidden = false;
      _this121._over = false;
      _this121.viewControlId = id + '_view';
      _this121.playbackControlId = id + '_playback';
      _this121.editControlId = id + '_editor';

      _this121.addNode(new paella.PlaybackControl(_this121.playbackControlId));

      var thisClass = _assertThisInitialized(_this121);

      paella.events.bind(paella.events.showEditor, function (event) {
        thisClass.onShowEditor();
      });
      paella.events.bind(paella.events.hideEditor, function (event) {
        thisClass.onHideEditor();
      });
      paella.events.bind(paella.events.play, function (event) {
        thisClass.onPlayEvent();
      });
      paella.events.bind(paella.events.pause, function (event) {
        thisClass.onPauseEvent();
      });
      $(document).mousemove(function (event) {
        paella.player.controls.restartHideTimer();
      });
      $(_this121.domElement).bind("mousemove", function (event) {
        thisClass._over = true;
      });
      $(_this121.domElement).bind("mouseout", function (event) {
        thisClass._over = false;
      });
      paella.events.bind(paella.events.endVideo, function (event) {
        thisClass.onEndVideoEvent();
      });
      paella.events.bind('keydown', function (event) {
        thisClass.onKeyEvent();
      });
      _this121.videoOverlayButtons = new paella.DomNode('div', id + '_videoOverlayButtonPlugins');
      _this121.videoOverlayButtons.domElement.className = 'videoOverlayButtonPlugins';

      _this121.videoOverlayButtons.domElement.setAttribute("role", "toolbar");

      _this121.addNode(_this121.videoOverlayButtons);

      paella.pluginManager.setTarget('videoOverlayButton', _assertThisInitialized(_this121));
      return _this121;
    }

    _createClass(ControlsContainer, [{
      key: "onShowEditor",
      value: function onShowEditor() {
        var editControl = this.editControl();
        if (editControl) $(editControl.domElement).hide();
      }
    }, {
      key: "onHideEditor",
      value: function onHideEditor() {
        var editControl = this.editControl();
        if (editControl) $(editControl.domElement).show();
      }
    }, {
      key: "enterEditMode",
      value: function enterEditMode() {
        var playbackControl = this.playbackControl();
        var editControl = this.editControl();

        if (playbackControl && editControl) {
          $(playbackControl.domElement).hide();
        }
      }
    }, {
      key: "exitEditMode",
      value: function exitEditMode() {
        var playbackControl = this.playbackControl();
        var editControl = this.editControl();

        if (playbackControl && editControl) {
          $(playbackControl.domElement).show();
        }
      }
    }, {
      key: "playbackControl",
      value: function playbackControl() {
        if (this.playbackControlInstance == null) {
          this.playbackControlInstance = this.getNode(this.playbackControlId);
        }

        return this.playbackControlInstance;
      }
    }, {
      key: "editControl",
      value: function editControl() {
        return this.getNode(this.editControlId);
      }
    }, {
      key: "disable",
      value: function disable() {
        this.isEnabled = false;
        this.hide();
      }
    }, {
      key: "enable",
      value: function enable() {
        this.isEnabled = true;
        this.show();
      }
    }, {
      key: "isHidden",
      value: function isHidden() {
        return this._hidden;
      }
    }, {
      key: "hide",
      value: function hide() {
        var This = this;
        this._doHide = true;

        function hideIfNotCanceled() {
          if (This._doHide) {
            $(This.domElement).css({
              opacity: 0.0
            });
            $(This.domElement).hide();
            This.domElement.setAttribute('aria-hidden', 'true');
            This._hidden = true;
            paella.events.trigger(paella.events.controlBarDidHide);
          }
        }

        paella.events.trigger(paella.events.controlBarWillHide);

        if (this._doHide) {
          if (!paella.utils.userAgent.browser.IsMobileVersion && !paella.utils.userAgent.browser.Explorer) {
            $(this.domElement).animate({
              opacity: 0.0
            }, {
              duration: 300,
              complete: hideIfNotCanceled
            });
          } else {
            hideIfNotCanceled();
          }
        }
      }
    }, {
      key: "showPopUp",
      value: function showPopUp(identifier) {
        this.playbackControl().showPopUp(identifier);
      }
    }, {
      key: "hidePopUp",
      value: function hidePopUp(identifier) {
        this.playbackControl().hidePopUp(identifier);
      }
    }, {
      key: "show",
      value: function show() {
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
    }, {
      key: "autohideTimeout",
      value: function autohideTimeout() {
        var playbackBar = this.playbackControl().playbackBar();

        if (playbackBar.isSeeking() || this._over) {
          paella.player.controls.restartHideTimer();
        } else {
          paella.player.controls.hideControls();
        }
      }
    }, {
      key: "hideControls",
      value: function hideControls() {
        var _this122 = this;

        paella.player.videoContainer.paused().then(function (paused) {
          if (!paused) {
            _this122.hide();
          } else {
            _this122.show();
          }
        });
      }
    }, {
      key: "showControls",
      value: function showControls() {
        this.show();
      }
    }, {
      key: "onPlayEvent",
      value: function onPlayEvent() {
        this.restartHideTimer();
      }
    }, {
      key: "onPauseEvent",
      value: function onPauseEvent() {
        this.clearAutohideTimer();
      }
    }, {
      key: "onEndVideoEvent",
      value: function onEndVideoEvent() {
        this.show();
        this.clearAutohideTimer();
      }
    }, {
      key: "onKeyEvent",
      value: function onKeyEvent() {
        this.restartHideTimer();
        paella.player.videoContainer.paused().then(function (paused) {
          if (!paused) {
            paella.player.controls.restartHideTimer();
          }
        });
      }
    }, {
      key: "cancelHideBar",
      value: function cancelHideBar() {
        this.restartTimerEvent();
      }
    }, {
      key: "restartTimerEvent",
      value: function restartTimerEvent() {
        var _this123 = this;

        if (this.isHidden()) {
          this.showControls();
        }

        this._doHide = false;
        paella.player.videoContainer.paused(function (paused) {
          if (!paused) {
            _this123.restartHideTimer();
          }
        });
      }
    }, {
      key: "clearAutohideTimer",
      value: function clearAutohideTimer() {
        if (this.autohideTimer != null) {
          this.autohideTimer.cancel();
          this.autohideTimer = null;
        }
      }
    }, {
      key: "restartHideTimer",
      value: function restartHideTimer() {
        this.showControls();
        this.clearAutohideTimer();
        var thisClass = this;
        this.autohideTimer = new paella.utils.Timer(function (timer) {
          thisClass.autohideTimeout();
        }, this.hideControlsTimeMillis);
      }
    }, {
      key: "onresize",
      value: function onresize() {
        this.playbackControl().onresize();
      }
    }]);

    return ControlsContainer;
  }(paella.DomNode);

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


(function () {
  var LoaderContainer = /*#__PURE__*/function (_paella$DomNode13) {
    _inherits(LoaderContainer, _paella$DomNode13);

    var _super44 = _createSuper(LoaderContainer);

    function LoaderContainer(id) {
      var _this124;

      _classCallCheck(this, LoaderContainer);

      _this124 = _super44.call(this, 'div', id, {
        position: 'fixed',
        backgroundColor: 'white',
        opacity: '0.7',
        top: '0px',
        left: '0px',
        right: '0px',
        bottom: '0px',
        zIndex: 10000
      });
      _this124.timer = null;
      _this124.loader = null;
      _this124.loaderPosition = 0;
      _this124.loader = _this124.addNode(new paella.DomNode('i', '', {
        width: "100px",
        height: "100px",
        color: "black",
        display: "block",
        marginLeft: "auto",
        marginRight: "auto",
        marginTop: "32%",
        fontSize: "100px"
      }));
      _this124.loader.domElement.className = "icon-spinner";
      paella.events.bind(paella.events.loadComplete, function (event, params) {
        _this124.loadComplete(params);
      });
      _this124.timer = new paella.utils.Timer(function (timer) {
        //thisClass.loaderPosition -= 128;
        //thisClass.loader.domElement.style.backgroundPosition = thisClass.loaderPosition + 'px';
        _this124.loader.domElement.style.transform = "rotate(".concat(_this124.loaderPosition, "deg");
        _this124.loaderPosition += 45;
      }, 250);
      _this124.timer.repeat = true;
      return _this124;
    }

    _createClass(LoaderContainer, [{
      key: "loadComplete",
      value: function loadComplete(params) {
        $(this.domElement).hide();
        this.timer.repeat = false;
      }
    }]);

    return LoaderContainer;
  }(paella.DomNode);

  paella.LoaderContainer = LoaderContainer;
  paella.Keys = {
    Space: 32,
    Left: 37,
    Up: 38,
    Right: 39,
    Down: 40,
    A: 65,
    B: 66,
    C: 67,
    D: 68,
    E: 69,
    F: 70,
    G: 71,
    H: 72,
    I: 73,
    J: 74,
    K: 75,
    L: 76,
    M: 77,
    N: 78,
    O: 79,
    P: 80,
    Q: 81,
    R: 82,
    S: 83,
    T: 84,
    U: 85,
    V: 86,
    W: 87,
    X: 88,
    Y: 89,
    Z: 90
  };

  var KeyPlugin = /*#__PURE__*/function (_paella$FastLoadPlugi4) {
    _inherits(KeyPlugin, _paella$FastLoadPlugi4);

    var _super45 = _createSuper(KeyPlugin);

    function KeyPlugin() {
      _classCallCheck(this, KeyPlugin);

      return _super45.apply(this, arguments);
    }

    _createClass(KeyPlugin, [{
      key: "onKeyPress",
      value: function onKeyPress(key) {
        console.log(key);
        return false;
      }
    }, {
      key: "type",
      get: function get() {
        return 'keyboard';
      }
    }]);

    return KeyPlugin;
  }(paella.FastLoadPlugin);

  paella.KeyPlugin = KeyPlugin;
  var g_keyboardEventSet = false;

  var KeyManager = /*#__PURE__*/function () {
    _createClass(KeyManager, [{
      key: "isPlaying",
      get: function get() {
        return this._isPlaying;
      },
      set: function set(p) {
        this._isPlaying = p;
      }
    }, {
      key: "enabled",
      get: function get() {
        return this._enabled !== undefined ? this._enabled : true;
      },
      set: function set(e) {
        this._enabled = e;
      }
    }]);

    function KeyManager() {
      _classCallCheck(this, KeyManager);

      this._isPlaying = false;
      var thisClass = this;
      paella.events.bind(paella.events.loadComplete, function (event, params) {
        thisClass.loadComplete(event, params);
      });
      paella.events.bind(paella.events.play, function (event) {
        thisClass.onPlay();
      });
      paella.events.bind(paella.events.pause, function (event) {
        thisClass.onPause();
      });
      paella.pluginManager.setTarget('keyboard', this);
      this._pluginList = [];
    }

    _createClass(KeyManager, [{
      key: "addPlugin",
      value: function addPlugin(plugin) {
        var _this125 = this;

        if (plugin.checkEnabled(function (e) {
          _this125._pluginList.push(plugin);

          plugin.setup();
        })) ;
      }
    }, {
      key: "loadComplete",
      value: function loadComplete(event, params) {
        var _this126 = this;

        if (g_keyboardEventSet) {
          return;
        }

        paella.events.bind("keyup", function (event) {
          _this126.keyUp(event);
        });
        g_keyboardEventSet = true;
      }
    }, {
      key: "onPlay",
      value: function onPlay() {
        this.isPlaying = true;
      }
    }, {
      key: "onPause",
      value: function onPause() {
        this.isPlaying = false;
      }
    }, {
      key: "keyUp",
      value: function keyUp(event) {
        if (!this.enabled) return;

        this._pluginList.some(function (plugin) {
          return plugin.onKeyPress(event);
        });
      }
    }]);

    return KeyManager;
  }();

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


(function () {
  var VideoLoader = /*#__PURE__*/function () {
    function VideoLoader() {
      _classCallCheck(this, VideoLoader);

      this.metadata = {
        // Video metadata
        title: "",
        duration: 0
      };
      this.streams = []; // {sources:{mp4:{src:"videourl.mp4",type:"video/mp4"},
      //			 ogg:{src:"videourl.ogv",type:"video/ogg"},
      //			 webm:{src:"videourl.webm",type:"video/webm"},
      //			 flv:{src:"videourl.flv",type:"video/x-flv"},
      //			 rtmp:{src:"rtmp://server.com/endpoint/url.loquesea",type="video/mp4 | video/x-flv"},
      //			 image:{frames:{frame_1:'frame_1.jpg',...frame_n:'frame_n.jpg'},duration:183},
      //	preview:'video_preview.jpg'}

      this.frameList = []; // frameList[timeInstant] = { id:"frame_id", mimetype:"image/jpg", time:timeInstant, url:"image_url"}

      this.loadStatus = false;
      this.codecStatus = false;
    }

    _createClass(VideoLoader, [{
      key: "getMetadata",
      value: function getMetadata() {
        return this.metadata;
      }
    }, {
      key: "getVideoId",
      value: function getVideoId() {
        return paella.initDelegate.getId();
      }
    }, {
      key: "getVideoUrl",
      value: function getVideoUrl() {
        // This function must to return the base video URL
        return "";
      }
    }, {
      key: "getDataUrl",
      value: function getDataUrl() {// This function must to return the location of the video data file
      }
    }, {
      key: "loadVideo",
      value: function loadVideo(onSuccess) {
        // This function must to:
        //	- load this.streams and this.frameList
        // 	- Check streams compatibility using this.isStreamCompatible(streamIndex)
        //	- Set this.loadStatus = true if load is Ok, or false if something gone wrong
        //	- Set this.codecStatus = true if the browser can reproduce all streams
        //	- Call onSuccess()
        onSuccess();
      }
    }]);

    return VideoLoader;
  }();

  paella.VideoLoader = VideoLoader;

  var AccessControl = /*#__PURE__*/function () {
    function AccessControl() {
      _classCallCheck(this, AccessControl);
    }

    _createClass(AccessControl, [{
      key: "canRead",
      value: function canRead() {
        return paella_DeferredResolved(true);
      }
    }, {
      key: "canWrite",
      value: function canWrite() {
        return paella_DeferredResolved(false);
      }
    }, {
      key: "userData",
      value: function userData() {
        return paella_DeferredResolved({
          username: 'anonymous',
          name: 'Anonymous',
          avatar: paella.utils.folders.resources() + '/images/default_avatar.png',
          isAnonymous: true
        });
      }
    }, {
      key: "getAuthenticationUrl",
      value: function getAuthenticationUrl(callbackParams) {
        var authCallback = this._authParams.authCallbackName && window[this._authParams.authCallbackName];

        if (!authCallback && paella.player.config.auth) {
          authCallback = paella.player.config.auth.authCallbackName && window[paella.player.config.auth.authCallbackName];
        }

        if (typeof authCallback == "function") {
          return authCallback(callbackParams);
        }

        return "";
      }
    }]);

    return AccessControl;
  }();

  paella.AccessControl = AccessControl;

  var PlayerBase = /*#__PURE__*/function () {
    _createClass(PlayerBase, [{
      key: "checkCompatibility",
      value: function checkCompatibility() {
        var message = "";

        if (paella.utils.parameters.get('ignoreBrowserCheck')) {
          return true;
        }

        if (paella.utils.userAgent.browser.IsMobileVersion) return true;
        var isCompatible = paella.utils.userAgent.browser.Chrome || paella.utils.userAgent.browser.EdgeChromium || paella.utils.userAgent.browser.Safari || paella.utils.userAgent.browser.Firefox || paella.utils.userAgent.browser.Opera || paella.utils.userAgent.browser.Edge || paella.utils.userAgent.browser.Explorer && paella.utils.userAgent.browser.Version.major >= 9;

        if (isCompatible) {
          return true;
        } else {
          var errorMessage = paella.utils.dictionary.translate("It seems that your browser is not HTML 5 compatible");
          paella.events.trigger(paella.events.error, {
            error: errorMessage
          });
          message = errorMessage + '<div style="display:block;width:470px;height:140px;margin-left:auto;margin-right:auto;font-family:Verdana,sans-sherif;font-size:12px;"><a href="http://www.google.es/chrome" style="color:#004488;float:left;margin-right:20px;"><img src="' + paella.utils.folders.resources() + 'images/chrome.png" style="width:80px;height:80px" alt="Google Chrome"></img><p>Google Chrome</p></a><a href="http://windows.microsoft.com/en-US/internet-explorer/products/ie/home" style="color:#004488;float:left;margin-right:20px;"><img src="' + paella.utils.folders.resources() + 'images/explorer.png" style="width:80px;height:80px" alt="Internet Explorer 9"></img><p>Internet Explorer 9</p></a><a href="http://www.apple.com/safari/" style="float:left;margin-right:20px;color:#004488"><img src="' + paella.utils.folders.resources() + 'images/safari.png" style="width:80px;height:80px" alt="Safari"></img><p>Safari 5</p></a><a href="http://www.mozilla.org/firefox/" style="float:left;color:#004488"><img src="' + paella.utils.folders.resources() + 'images/firefox.png" style="width:80px;height:80px" alt="Firefox"></img><p>Firefox 12</p></a></div>';
          message += '<div style="margin-top:30px;"><a id="ignoreBrowserCheckLink" href="#" onclick="window.location = window.location + \'&ignoreBrowserCheck=true\'">' + paella.utils.dictionary.translate("Continue anyway") + '</a></div>';
          paella.messageBox.showError(message, {
            height: '40%'
          });
        }

        return false;
      }
    }]);

    function PlayerBase(playerId) {
      _classCallCheck(this, PlayerBase);

      this.config = null;
      this.playerId = '';
      this.mainContainer = null;
      this.videoContainer = null;
      this.controls = null;
      this.accessControl = null;

      if (paella.utils.parameters.get('log') != undefined) {
        var log = 0;

        switch (paella.utils.parameters.get('log')) {
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
      } else {
        paella.player = this;
        this.playerId = playerId;
        this.mainContainer = $('#' + this.playerId)[0];
        var thisClass = this;
        paella.events.bind(paella.events.loadComplete, function (event, params) {
          thisClass.loadComplete(event, params);
        });
      }
    }

    _createClass(PlayerBase, [{
      key: "loadComplete",
      value: function loadComplete(event, params) {}
    }, {
      key: "repoUrl",
      get: function get() {
        return paella.player.videoLoader._url || paella.player.config.standalone && paella.player.config.standalone.repository;
      }
    }, {
      key: "videoUrl",
      get: function get() {
        return paella.player.videoLoader.getVideoUrl();
      }
    }, {
      key: "dataUrl",
      get: function get() {
        return paella.player.videoLoader.getDataUrl();
      }
    }, {
      key: "videoId",
      get: function get() {
        return paella.initDelegate.getId();
      }
    }, {
      key: "startMuted",
      get: function get() {
        return /true/.test(paella.utils.parameters.get("muted"));
      }
    }, {
      key: "auth",
      get: function get() {
        return {
          login: function login(redirect) {
            redirect = redirect || window.location.href;
            var url = paella.initDelegate.initParams.accessControl.getAuthenticationUrl(redirect);

            if (url) {
              window.location.href = url;
            }
          },
          // The following functions returns promises
          canRead: function canRead() {
            return paella.initDelegate.initParams.accessControl.canRead();
          },
          canWrite: function canWrite() {
            return paella.initDelegate.initParams.accessControl.canWrite();
          },
          userData: function userData() {
            return paella.initDelegate.initParams.accessControl.userData();
          }
        };
      }
    }]);

    return PlayerBase;
  }();

  paella.PlayerBase = PlayerBase;

  var InitDelegate = /*#__PURE__*/function () {
    _createClass(InitDelegate, [{
      key: "initParams",
      get: function get() {
        if (!this._initParams) {
          this._initParams = {
            configUrl: paella.baseUrl + 'config/config.json',
            dictionaryUrl: paella.baseUrl + 'localization/paella',
            accessControl: null,
            videoLoader: null,
            disableUserInterface: function disableUserInterface() {
              return /true/i.test(paella.utils.parameters.get("disable-ui"));
            } // Other parameters set externally:
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
    }]);

    function InitDelegate(params) {
      _classCallCheck(this, InitDelegate);

      if (arguments.length == 2) {
        this._config = arguments[0];
      }

      if (params) {
        for (var key in params) {
          this.initParams[key] = params[key];
        }
      }

      if (!this.initParams.getId) {
        this.initParams.getId = function () {
          return paella.utils.parameters.get('id') || "noid";
        };
      }
    }

    _createClass(InitDelegate, [{
      key: "getId",
      value: function getId() {
        return this.initParams.getId();
      }
    }, {
      key: "loadDictionary",
      value: function loadDictionary() {
        var _this127 = this;

        return new Promise(function (resolve) {
          paella.utils.ajax.get({
            url: _this127.initParams.dictionaryUrl + "_" + paella.utils.dictionary.currentLanguage() + '.json'
          }, function (data, type, returnCode) {
            paella.utils.dictionary.addDictionary(data);
            resolve(data);
          }, function (data, type, returnCode) {
            resolve();
          });
        });
      }
    }, {
      key: "loadConfig",
      value: function loadConfig() {
        var _this128 = this;

        var loadAccessControl = function loadAccessControl(data) {
          var AccessControlClass = paella.utils.objectFromString(data.player.accessControlClass || "paella.AccessControl");
          _this128.initParams.accessControl = new AccessControlClass();
        };

        if (this.initParams.config) {
          return new Promise(function (resolve) {
            loadAccessControl(_this128.initParams.config);
            resolve(_this128.initParams.config);
          });
        } else if (this.initParams.loadConfig) {
          return new Promise(function (resolve, reject) {
            _this128.initParams.loadConfig(_this128.initParams.configUrl).then(function (data) {
              loadAccessControl(data);
              resolve(data);
            })["catch"](function (err) {
              reject(err);
            });
          });
        } else {
          return new Promise(function (resolve, reject) {
            var configUrl = _this128.initParams.configUrl;
            var params = {};
            params.url = configUrl;
            paella.utils.ajax.get(params, function (data, type, returnCode) {
              try {
                data = JSON.parse(data);
              } catch (e) {}

              loadAccessControl(data);
              resolve(data);
            }, function (data, type, returnCode) {
              paella.messageBox.showError(paella.utils.dictionary.translate("Error! Config file not found. Please configure paella!")); //onSuccess({});
            });
          });
        }
      }
    }]);

    return InitDelegate;
  }();

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


(function () {
  var PaellaPlayer = /*#__PURE__*/function (_paella$PlayerBase) {
    _inherits(PaellaPlayer, _paella$PlayerBase);

    var _super46 = _createSuper(PaellaPlayer);

    _createClass(PaellaPlayer, [{
      key: "getPlayerMode",
      value: function getPlayerMode() {
        if (paella.player.isFullScreen()) {
          return paella.PaellaPlayer.mode.fullscreen;
        } else if (window.self !== window.top) {
          return paella.PaellaPlayer.mode.embed;
        }

        return paella.PaellaPlayer.mode.standard;
      }
    }, {
      key: "checkFullScreenCapability",
      value: function checkFullScreenCapability() {
        var fs = document.getElementById(paella.player.mainContainer.id);

        if (fs.webkitRequestFullScreen || fs.mozRequestFullScreen || fs.msRequestFullscreen || fs.requestFullScreen) {
          return true;
        }

        if (paella.utils.userAgent.browser.IsMobileVersion && paella.player.videoContainer.isMonostream) {
          return true;
        }

        return false;
      }
    }, {
      key: "addFullScreenListeners",
      value: function addFullScreenListeners() {
        var thisClass = this;

        var onFullScreenChangeEvent = function onFullScreenChangeEvent() {
          setTimeout(function () {
            paella.pluginManager.checkPluginsVisibility();
          }, 1000);
          var fs = document.getElementById(paella.player.mainContainer.id);

          if (paella.player.isFullScreen()) {
            fs.style.width = '100%';
            fs.style.height = '100%';
          } else {
            fs.style.width = '';
            fs.style.height = '';
          }

          if (thisClass.isFullScreen()) {
            paella.events.trigger(paella.events.enterFullscreen);
          } else {
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
    }, {
      key: "isFullScreen",
      value: function isFullScreen() {
        var webKitIsFullScreen = document.webkitIsFullScreen === true;
        var msIsFullScreen = document.msFullscreenElement !== undefined && document.msFullscreenElement !== null;
        var mozIsFullScreen = document.mozFullScreen === true;
        var stdIsFullScreen = document.fullScreenElement !== undefined && document.fullScreenElement !== null;
        return webKitIsFullScreen || msIsFullScreen || mozIsFullScreen || stdIsFullScreen;
      }
    }, {
      key: "goFullScreen",
      value: function goFullScreen() {
        if (!this.isFullScreen()) {
          if (paella.utils.userAgent.system.iOS && (paella.utils.userAgent.browser.Version.major < 12 || !paella.utils.userAgent.system.iPad)) {
            paella.player.videoContainer.masterVideo().goFullScreen();
          } else {
            var fs = document.getElementById(paella.player.mainContainer.id);

            if (fs.webkitRequestFullScreen) {
              fs.webkitRequestFullScreen();
            } else if (fs.mozRequestFullScreen) {
              fs.mozRequestFullScreen();
            } else if (fs.msRequestFullscreen) {
              fs.msRequestFullscreen();
            } else if (fs.requestFullScreen) {
              fs.requestFullScreen();
            }
          }
        }
      }
    }, {
      key: "exitFullScreen",
      value: function exitFullScreen() {
        if (this.isFullScreen()) {
          if (document.webkitCancelFullScreen) {
            document.webkitCancelFullScreen();
          } else if (document.mozCancelFullScreen) {
            document.mozCancelFullScreen();
          } else if (document.msExitFullscreen()) {
            document.msExitFullscreen();
          } else if (document.cancelFullScreen) {
            document.cancelFullScreen();
          }
        }
      }
    }, {
      key: "setProfile",
      value: function setProfile(profileName, animate) {
        if (paella.profiles.setProfile(profileName, animate)) {
          var profileData = paella.player.getProfile(profileName);

          if (profileData && !paella.player.videoContainer.isMonostream) {
            paella.utils.cookies.set('lastProfile', profileName);
          }

          paella.events.trigger(paella.events.setProfile, {
            profileName: profileName
          });
        }
      }
    }, {
      key: "getProfile",
      value: function getProfile(profileName) {
        return paella.profiles.getProfile(profileName);
      }
    }]);

    function PaellaPlayer(playerId) {
      var _this129;

      _classCallCheck(this, PaellaPlayer);

      _this129 = _super46.call(this, playerId);
      _this129.player = null;
      _this129.videoIdentifier = '';
      _this129.loader = null; // Video data:

      _this129.videoData = null; // if initialization ok

      if (_this129.playerId == playerId) {
        _this129.loadPaellaPlayer();

        var thisClass = _assertThisInitialized(_this129);
      }

      return _this129;
    }

    _createClass(PaellaPlayer, [{
      key: "loadPaellaPlayer",
      value: function loadPaellaPlayer() {
        var This = this;
        this.loader = new paella.LoaderContainer('paellaPlayer_loader');
        $('body')[0].appendChild(this.loader.domElement);
        paella.events.trigger(paella.events.loadStarted);
        paella.initDelegate.loadDictionary().then(function () {
          return paella.initDelegate.loadConfig();
        }).then(function (config) {
          This.accessControl = paella.initDelegate.initParams.accessControl;
          This.videoLoader = paella.initDelegate.initParams.videoLoader;
          This.onLoadConfig(config);

          if (config.skin) {
            var skin = config.skin["default"] || 'dark';
            paella.utils.skin.restore(skin);
          }
        });
      }
    }, {
      key: "onLoadConfig",
      value: function onLoadConfig(configData) {
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
            } catch (e) {
              paella.log.warning("Error selecting video quality strategy: strategy not found");
            }

            this.videoContainer.setVideoQualityStrategy(videoQualityStrategy);
            this.mainContainer.appendChild(this.videoContainer.domElement);
          }

          $(window).resize(function (event) {
            paella.player.onresize();
          });
          this.onload();
        }

        paella.pluginManager.loadPlugins("paella.FastLoadPlugin");
      }
    }, {
      key: "onload",
      value: function onload() {
        var thisClass = this;
        var ac = this.accessControl;
        var canRead = false;
        var userData = {};
        this.accessControl.canRead().then(function (c) {
          canRead = c;
          return thisClass.accessControl.userData();
        }).then(function (d) {
          userData = d;

          if (canRead) {
            thisClass.loadVideo();
          } else if (userData.isAnonymous) {
            var redirectUrl = paella.initDelegate.initParams.accessControl.getAuthenticationUrl("player/?id=" + paella.player.videoIdentifier);
            var message = '<div>' + paella.utils.dictionary.translate("You are not authorized to view this resource") + '</div>';

            if (redirectUrl) {
              message += '<div class="login-link"><a href="' + redirectUrl + '">' + paella.utils.dictionary.translate("Login") + '</a></div>';
            }

            thisClass.unloadAll(message);
          } else {
            var errorMessage = paella.utils.dictionary.translate("You are not authorized to view this resource");
            thisClass.unloadAll(errorMessage);
            paella.events.trigger(paella.events.error, {
              error: errorMessage
            });
          }
        })["catch"](function (error) {
          var errorMessage = paella.utils.dictionary.translate(error);
          thisClass.unloadAll(errorMessage);
          paella.events.trigger(paella.events.error, {
            error: errorMessage
          });
        });
      }
    }, {
      key: "onresize",
      value: function onresize() {
        this.videoContainer.onresize();
        if (this.controls) this.controls.onresize(); // Resize the layout profile

        if (this.videoContainer.ready) {
          var cookieProfile = paella.utils.cookies.get('lastProfile');

          if (cookieProfile) {
            this.setProfile(cookieProfile, false);
          } else {
            this.setProfile(paella.player.selectedProfile, false);
          }
        }

        paella.events.trigger(paella.events.resize, {
          width: $(this.videoContainer.domElement).width(),
          height: $(this.videoContainer.domElement).height()
        });
      }
    }, {
      key: "unloadAll",
      value: function unloadAll(message) {
        var loaderContainer = $('#paellaPlayer_loader')[0];
        this.mainContainer.innerText = "";
        paella.messageBox.showError(message);
      }
    }, {
      key: "reloadVideos",
      value: function reloadVideos(masterQuality, slaveQuality) {
        if (this.videoContainer) {
          this.videoContainer.reloadVideos(masterQuality, slaveQuality);
          this.onresize();
        }
      }
    }, {
      key: "loadVideo",
      value: function loadVideo() {
        if (this.videoIdentifier) {
          var This = this;
          var loader = paella.player.videoLoader;
          this.onresize();
          loader.loadVideo(function () {
            var playOnLoad = false;
            This.videoContainer.setStreamData(loader.streams).then(function () {
              paella.events.trigger(paella.events.loadComplete);
              This.addFullScreenListeners();
              This.onresize(); // If the player has been loaded using lazyLoad, the video should be
              // played as soon as it loads

              if (This.videoContainer.autoplay() || g_lazyLoadInstance != null) {
                This.play();
              } else if (loader.metadata.preview) {
                This.lazyLoadContainer = new LazyThumbnailContainer(loader.metadata.preview);
                document.body.appendChild(This.lazyLoadContainer.domElement);
              }
            })["catch"](function (error) {
              console.error(error);
              var msg = error.message || "Could not load the video";
              paella.messageBox.showError(paella.utils.dictionary.translate(msg));
            });
          });
        }
      }
    }, {
      key: "showPlaybackBar",
      value: function showPlaybackBar() {
        if (!this.controls) {
          this.controls = new paella.ControlsContainer(this.playerId + '_controls');
          this.mainContainer.appendChild(this.controls.domElement);
          this.controls.onresize();
          paella.events.trigger(paella.events.loadPlugins, {
            pluginManager: paella.pluginManager
          });
        }
      }
    }, {
      key: "isLiveStream",
      value: function isLiveStream() {
        var loader = paella.initDelegate.initParams.videoLoader;

        var checkSource = function checkSource(sources, index) {
          if (sources.length > index) {
            var source = sources[index];

            for (var key in source.sources) {
              if (_typeof(source.sources[key]) == "object") {
                for (var i = 0; i < source.sources[key].length; ++i) {
                  var stream = source.sources[key][i];
                  if (stream.isLiveStream) return true;
                }
              }
            }
          }

          return false;
        };

        return checkSource(loader.streams, 0) || checkSource(loader.streams, 1);
      }
    }, {
      key: "loadPreviews",
      value: function loadPreviews() {
        var streams = paella.initDelegate.initParams.videoLoader.streams;
        var slavePreviewImg = null;
        var masterPreviewImg = streams[0].preview;

        if (streams.length >= 2) {
          slavePreviewImg = streams[1].preview;
        }

        if (masterPreviewImg) {
          var masterRect = paella.player.videoContainer.overlayContainer.getVideoRect(0);
          this.masterPreviewElem = document.createElement('img');
          this.masterPreviewElem.src = masterPreviewImg;
          paella.player.videoContainer.overlayContainer.addElement(this.masterPreviewElem, masterRect);
        }

        if (slavePreviewImg) {
          var slaveRect = paella.player.videoContainer.overlayContainer.getVideoRect(1);
          this.slavePreviewElem = document.createElement('img');
          this.slavePreviewElem.src = slavePreviewImg;
          paella.player.videoContainer.overlayContainer.addElement(this.slavePreviewElem, slaveRect);
        }

        paella.events.bind(paella.events.timeUpdate, function (event) {
          paella.player.unloadPreviews();
        });
      }
    }, {
      key: "unloadPreviews",
      value: function unloadPreviews() {
        if (this.masterPreviewElem) {
          paella.player.videoContainer.overlayContainer.removeElement(this.masterPreviewElem);
          this.masterPreviewElem = null;
        }

        if (this.slavePreviewElem) {
          paella.player.videoContainer.overlayContainer.removeElement(this.slavePreviewElem);
          this.slavePreviewElem = null;
        }
      }
    }, {
      key: "loadComplete",
      value: function loadComplete(event, params) {
        var thisClass = this; //var master = paella.player.videoContainer.masterVideo();

        paella.pluginManager.loadPlugins("paella.EarlyLoadPlugin");

        if (paella.player.videoContainer._autoplay) {
          this.play();
        }
      }
    }, {
      key: "play",
      value: function play() {
        var _this130 = this;

        if (!this.videoContainer) {
          // play() is called from lazyLoadContainer
          this.lazyLoadContainer.destroyElements();
          this.lazyLoadContainer = null;
          this._onPlayClosure && this._onPlayClosure();
        } else if (this.lazyLoadContainer) {
          // play() has been called by a user interaction
          document.body.removeChild(this.lazyLoadContainer.domElement);
          this.lazyLoadContainer = null;
        }

        if (this.videoContainer) {
          return new Promise(function (resolve, reject) {
            _this130.videoContainer.play().then(function () {
              if (paella.initDelegate.initParams.disableUserInterface()) {
                resolve();
              } else if (!_this130.controls) {
                if (!_this130.controls) {
                  _this130.showPlaybackBar();

                  paella.events.trigger(paella.events.controlBarLoaded);

                  _this130.controls.onresize();
                }

                resolve();
              }
            })["catch"](function (err) {
              reject(err);
            });
          });
        }
      }
    }, {
      key: "pause",
      value: function pause() {
        return this.videoContainer.pause();
      }
    }, {
      key: "playing",
      value: function playing() {
        var _this131 = this;

        return new Promise(function (resolve) {
          _this131.paused().then(function (p) {
            resolve(!p);
          });
        });
      }
    }, {
      key: "paused",
      value: function paused() {
        return this.videoContainer.paused();
      }
    }, {
      key: "selectedProfile",
      get: function get() {
        return paella.profiles.currentProfileName;
      }
    }]);

    return PaellaPlayer;
  }(paella.PlayerBase);

  paella.PaellaPlayer = PaellaPlayer;
  window.PaellaPlayer = PaellaPlayer;
  paella.PaellaPlayer.mode = {
    standard: 'standard',
    fullscreen: 'fullscreen',
    embed: 'embed'
  };

  var LazyThumbnailContainer = /*#__PURE__*/function (_paella$DomNode14) {
    _inherits(LazyThumbnailContainer, _paella$DomNode14);

    var _super47 = _createSuper(LazyThumbnailContainer);

    _createClass(LazyThumbnailContainer, null, [{
      key: "GetIconElement",
      value: function GetIconElement() {
        var container = document.createElement('div');
        container.className = "play-button-on-screen";
        container.style.width = "100%";
        container.style.height = "100%";
        container.style.pointerEvents = "none";
        var icon = document.createElement('div');
        icon['className'] = 'play-icon';
        container.appendChild(icon);
        return container;
      }
    }]);

    function LazyThumbnailContainer(src) {
      var _this132;

      _classCallCheck(this, LazyThumbnailContainer);

      _this132 = _super47.call(this, 'img', 'lazyLoadThumbnailContainer', {});
      var url = new paella.URL(src);

      if (!url.isAbsolute) {
        url = new paella.URL(paella.player.repoUrl).appendPath(paella.player.videoIdentifier).appendPath(src);
      }

      _this132.domElement.src = url.absoluteUrl;
      _this132.domElement.alt = "";
      _this132.container = LazyThumbnailContainer.GetIconElement();

      if (!paella.player.videoContainer) {
        document.body.appendChild(_this132.container);
      }

      return _this132;
    }

    _createClass(LazyThumbnailContainer, [{
      key: "setImage",
      value: function setImage(url) {
        this.domElement.src = url;
      }
    }, {
      key: "onClick",
      value: function onClick(closure) {
        this.domElement.onclick = closure;
      }
    }, {
      key: "destroyElements",
      value: function destroyElements() {
        document.body.removeChild(this.domElement);
        document.body.removeChild(this.container);
      }
    }]);

    return LazyThumbnailContainer;
  }(paella.DomNode);

  paella.LazyThumbnailContainer = LazyThumbnailContainer;
  var g_lazyLoadInstance = null;

  var PaellaPlayerLazy = /*#__PURE__*/function (_PaellaPlayer) {
    _inherits(PaellaPlayerLazy, _PaellaPlayer);

    var _super48 = _createSuper(PaellaPlayerLazy);

    function PaellaPlayerLazy(playerId, initDelegate) {
      var _this133;

      _classCallCheck(this, PaellaPlayerLazy);

      _this133 = _super48.call(this, playerId, initDelegate);
      g_lazyLoadInstance = _assertThisInitialized(_this133);
      return _this133;
    }

    _createClass(PaellaPlayerLazy, [{
      key: "loadComplete",
      value: function loadComplete(event, params) {}
    }, {
      key: "onLoadConfig",
      value: function onLoadConfig(configData) {
        //paella.data = new paella.Data(configData);
        this.config = configData;
        this.videoIdentifier = paella.initDelegate.getId();

        if (this.videoIdentifier) {
          $(window).resize(function (event) {
            paella.player.onresize();
          });
          this.onload();
        }
      }
    }, {
      key: "loadVideo",
      value: function loadVideo() {
        var _this134 = this;

        if (this.videoIdentifier) {
          var This = this;
          var loader = paella.player.videoLoader;
          this.onresize();
          loader.loadVideo(function () {
            if (!loader.metadata.preview) {
              paella.load(_this134.playerId, paella.loaderFunctionParams);
              g_lazyLoadInstance = null; // Lazy load is disabled when the video has no preview
            } else {
              _this134.lazyLoadContainer = new LazyThumbnailContainer(loader.metadata.preview);
              document.body.appendChild(_this134.lazyLoadContainer.domElement);

              _this134.lazyLoadContainer.onClick(function () {
                _this134.lazyLoadContainer.destroyElements();

                _this134.lazyLoadContainer = null;
                _this134._onPlayClosure && _this134._onPlayClosure();
              });

              paella.events.trigger(paella.events.loadComplete);
            }
          });
        }
      }
    }, {
      key: "onresize",
      value: function onresize() {}
    }, {
      key: "onPlay",
      set: function set(closure) {
        this._onPlayClosure = closure;
      }
    }]);

    return PaellaPlayerLazy;
  }(PaellaPlayer);

  paella.PaellaPlayerLazy = PaellaPlayerLazy;
  /* Initializer function */

  window.initPaellaEngage = function (playerId, initDelegate) {
    if (!initDelegate) {
      initDelegate = new paella.InitDelegate();
    }

    paella.initDelegate = initDelegate;
    paellaPlayer = new PaellaPlayer(playerId, paella.initDelegate);
  };
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


(function () {
  // Default Video Loader
  //
  var DefaultVideoLoader = /*#__PURE__*/function (_paella$VideoLoader) {
    _inherits(DefaultVideoLoader, _paella$VideoLoader);

    var _super49 = _createSuper(DefaultVideoLoader);

    function DefaultVideoLoader(data) {
      var _this135;

      _classCallCheck(this, DefaultVideoLoader);

      _this135 = _super49.call(this, data);
      _this135._url = null;

      if (_typeof(data) == "object") {
        _this135._data = data;
      } else {
        try {
          _this135._data = JSON.parse(data);
        } catch (e) {
          _this135._url = data;
        }
      }

      return _this135;
    }

    _createClass(DefaultVideoLoader, [{
      key: "getVideoUrl",
      value: function getVideoUrl() {
        if (paella.initDelegate.initParams.videoUrl) {
          return typeof paella.initDelegate.initParams.videoUrl == "function" ? paella.initDelegate.initParams.videoUrl() : paella.initDelegate.initParams.videoUrl;
        } else {
          var url = this._url || paella.player.config.standalone && paella.player.config.standalone.repository || '';
          return (/\/$/.test(url) ? url : url + '/') + paella.initDelegate.getId() + '/';
        }
      }
    }, {
      key: "getDataUrl",
      value: function getDataUrl() {
        if (paella.initDelegate.initParams.dataUrl) {
          return typeof paella.initDelegate.initParams.dataUrl == 'function' ? paella.initDelegate.initParams.dataUrl() : paella.initDelegate.initParams.dataUrl;
        } else {
          return this.getVideoUrl() + 'data.json';
        }
      }
    }, {
      key: "loadVideo",
      value: function loadVideo(onSuccess) {
        var _this136 = this;

        var loadVideoDelegate = paella.initDelegate.initParams.loadVideo;
        var url = this._url || this.getDataUrl();

        if (this._data) {
          this.loadVideoData(this._data, onSuccess);
        } else if (loadVideoDelegate) {
          loadVideoDelegate().then(function (data) {
            _this136._data = data;

            _this136.loadVideoData(_this136._data, onSuccess);
          });
        } else if (url) {
          var This = this;
          paella.utils.ajax.get({
            url: this.getDataUrl()
          }, function (data, type, err) {
            if (typeof data == "string") {
              try {
                data = JSON.parse(data);
              } catch (e) {}
            }

            This._data = data;
            This.loadVideoData(This._data, onSuccess);
          }, function (data, type, err) {
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
    }, {
      key: "loadVideoData",
      value: function loadVideoData(data, onSuccess) {
        var This = this;

        if (data.metadata) {
          this.metadata = data.metadata;
        }

        if (data.streams) {
          data.streams.forEach(function (stream) {
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
          this.loadBlackboard(data.streams[0], data.blackboard);
        }

        this.streams = data.streams;
        this.frameList = data.frameList;
        this.loadStatus = this.streams.length > 0;
        onSuccess();
      }
    }, {
      key: "loadFrameData",
      value: function loadFrameData(data) {
        var This = this;

        if (data.frameList && data.frameList.forEach) {
          var newFrames = {};
          data.frameList.forEach(function (frame) {
            if (!/^[a-zA-Z]+:\/\//.test(frame.url) && !/^data:/.test(frame.url)) {
              frame.url = This.getVideoUrl() + frame.url;
            }

            if (frame.thumb && !/^[a-zA-Z]+:\/\//.test(frame.thumb) && !/^data:/.test(frame.thumb)) {
              frame.thumb = This.getVideoUrl() + frame.thumb;
            }

            var id = frame.time;
            newFrames[id] = frame;
          });
          data.frameList = newFrames;
        }
      }
    }, {
      key: "loadStream",
      value: function loadStream(stream) {
        var This = this;

        if (stream.preview && !/^[a-zA-Z]+:\/\//.test(stream.preview) && !/^data:/.test(stream.preview)) {
          stream.preview = This.getVideoUrl() + stream.preview;
        }

        if (!stream.sources) {
          return;
        }

        if (stream.sources.image) {
          stream.sources.image.forEach(function (image) {
            if (image.frames.forEach) {
              var newFrames = {};
              image.frames.forEach(function (frame) {
                if (frame.src && !/^[a-zA-Z]+:\/\//.test(frame.src) && !/^data:/.test(frame.src)) {
                  frame.src = This.getVideoUrl() + frame.src;
                }

                if (frame.thumb && !/^[a-zA-Z]+:\/\//.test(frame.thumb) && !/^data:/.test(frame.thumb)) {
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

                if (typeof sourceItem.src == "string") {
                  if (sourceItem.src.match(pattern) == null) {
                    sourceItem.src = This.getVideoUrl() + sourceItem.src;
                  }
                }

                sourceItem.type = sourceItem.mimetype;
              });
            }
          } else {
            delete stream.sources[type];
          }
        }
      }
    }, {
      key: "loadCaptions",
      value: function loadCaptions(captions) {
        if (captions) {
          for (var i = 0; i < captions.length; ++i) {
            var url = captions[i].url;

            if (!/^[a-zA-Z]+:\/\//.test(url)) {
              url = this.getVideoUrl() + url;
            }

            var c = new paella.captions.Caption(i, captions[i].format, url, {
              code: captions[i].lang,
              txt: captions[i].text
            });
            paella.captions.addCaptions(c);
          }
        }
      }
    }, {
      key: "loadBlackboard",
      value: function loadBlackboard(stream, blackboard) {
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
        blackboard.frames.forEach(function (frame) {
          var id = "frame_" + Math.round(frame.time);

          if (!/^[a-zA-Z]+:\/\//.test(frame.src)) {
            frame.src = This.getVideoUrl() + frame.src;
          }

          imageObject.frames[id] = frame.src;
        });
        stream.sources.image.push(imageObject);
      }
    }]);

    return DefaultVideoLoader;
  }(paella.VideoLoader);

  paella.DefaultVideoLoader = DefaultVideoLoader;

  var DefaultInitDelegate = /*#__PURE__*/function (_paella$InitDelegate) {
    _inherits(DefaultInitDelegate, _paella$InitDelegate);

    var _super50 = _createSuper(DefaultInitDelegate);

    function DefaultInitDelegate() {
      _classCallCheck(this, DefaultInitDelegate);

      return _super50.apply(this, arguments);
    }

    return DefaultInitDelegate;
  }(paella.InitDelegate);

  paella.DefaultInitDelegate = DefaultInitDelegate;

  function getManifestFromParameters(params) {
    var master = null;

    if (master = paella.utils.parameters.get('video')) {
      var slave = paella.utils.parameters.get('videoSlave');
      slave = slave && decodeURIComponent(slave);
      var masterPreview = paella.utils.parameters.get('preview');
      masterPreview = masterPreview && decodeURIComponent(masterPreview);
      var slavePreview = paella.utils.parameters.get('previewSlave');
      slavePreview = slavePreview && decodeURIComponent(slavePreview);
      var title = paella.utils.parameters.get('title') || "Untitled Video";
      var data = {
        metadata: {
          title: title
        },
        streams: [{
          sources: {
            mp4: [{
              src: decodeURIComponent(master),
              mimetype: "video/mp4",
              res: {
                w: 0,
                h: 0
              }
            }]
          },
          preview: masterPreview,
          type: "video",
          content: "presenter"
        }],
        frameList: []
      };

      if (slave) {
        data.streams.push({
          sources: {
            mp4: [{
              src: slave,
              mimetype: "video/mp4",
              res: {
                w: 0,
                h: 0
              }
            }]
          },
          preview: slavePreview,
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


  paella.load = function (playerContainer, params) {
    paella.loaderFunctionParams = params;
    var auth = params && params.auth || {}; // Build custom init data using url parameters

    var data = getManifestFromParameters(params);

    if (data) {
      params.data = data;
    }

    var initObjects = params;
    initObjects.videoLoader = new paella.DefaultVideoLoader(params.data || params.url);
    paella.initDelegate = new paella.DefaultInitDelegate(initObjects);
    new PaellaPlayer(playerContainer, paella.initDelegate);
  };
  /*
   *	playerContainer	Player DOM container id
   *	params.configUrl		Url to the config json file
   *	params.config			Use this configuration file
   *	params.data				Paella video data schema
   *	params.url				Repository URL
   *  forceLazyLoad			Use lazyLoad even if your browser does not allow automatic playback of the video
   */


  paella.lazyLoad = function (playerContainer, params) {
    var forceLazyLoad = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : true;
    paella.loaderFunctionParams = params;
    var auth = params && params.auth || {}; // Check autoplay. If autoplay is enabled, this function must call paella.load()

    paella.Html5Video.IsAutoplaySupported().then(function (supported) {
      var disableUI = /true/i.test(paella.utils.parameters.get("disable-ui"));

      if ((supported || forceLazyLoad) && !disableUI) {
        // Build custom init data using url parameters
        var data = getManifestFromParameters(params);

        if (data) {
          params.data = data;
        }

        var initObjects = params;
        initObjects.videoLoader = new paella.DefaultVideoLoader(params.data || params.url);
        paella.initDelegate = new paella.DefaultInitDelegate(initObjects);
        var lazyLoad = new paella.PaellaPlayerLazy(playerContainer, paella.initDelegate);

        lazyLoad.onPlay = function () {
          $('#' + playerContainer).innerHTML = "";
          paella.load(playerContainer, params);
        };
      } else {
        paella.load(playerContainer, params);
      }
    });
  };
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


(function () {
  var RightBarPlugin = /*#__PURE__*/function (_paella$DeferredLoadP3) {
    _inherits(RightBarPlugin, _paella$DeferredLoadP3);

    var _super51 = _createSuper(RightBarPlugin);

    function RightBarPlugin() {
      _classCallCheck(this, RightBarPlugin);

      return _super51.apply(this, arguments);
    }

    _createClass(RightBarPlugin, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.RightBarPlugin";
      }
    }, {
      key: "buildContent",
      value: function buildContent(domElement) {}
    }, {
      key: "type",
      get: function get() {
        return 'rightBarPlugin';
      }
    }]);

    return RightBarPlugin;
  }(paella.DeferredLoadPlugin);

  paella.RightBarPlugin = RightBarPlugin;

  var TabBarPlugin = /*#__PURE__*/function (_paella$DeferredLoadP4) {
    _inherits(TabBarPlugin, _paella$DeferredLoadP4);

    var _super52 = _createSuper(TabBarPlugin);

    function TabBarPlugin() {
      _classCallCheck(this, TabBarPlugin);

      return _super52.apply(this, arguments);
    }

    _createClass(TabBarPlugin, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.TabBarPlugin";
      }
    }, {
      key: "getTabName",
      value: function getTabName() {
        return "New Tab";
      }
    }, {
      key: "action",
      value: function action(tab) {}
    }, {
      key: "buildContent",
      value: function buildContent(domElement) {}
    }, {
      key: "setToolTip",
      value: function setToolTip(message) {
        this.button.setAttribute("title", message);
        this.button.setAttribute("aria-label", message);
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return "";
      }
    }, {
      key: "type",
      get: function get() {
        return 'tabBarPlugin';
      }
    }]);

    return TabBarPlugin;
  }(paella.DeferredLoadPlugin);

  paella.TabBarPlugin = TabBarPlugin;

  var ExtendedAdapter = /*#__PURE__*/function () {
    function ExtendedAdapter() {
      _classCallCheck(this, ExtendedAdapter);

      this.rightContainer = null;
      this.bottomContainer = null;
      this.rightBarPlugins = [];
      this.tabBarPlugins = [];
      this.currentTabIndex = 0;
      this.bottomContainerTabs = null;
      this.bottomContainerContent = null;
      this.rightContainer = document.createElement('div'); //this.rightContainer.id = this.settings.rightContainerId;

      this.rightContainer.className = "rightPluginContainer";
      this.bottomContainer = document.createElement('div'); //this.bottomContainer.id = this.settings.bottomContainerId;

      this.bottomContainer.className = "tabsPluginContainer";
      var tabs = document.createElement('div'); //tabs.id = 'bottomContainer_tabs';

      tabs.className = 'tabsLabelContainer';
      this.bottomContainerTabs = tabs;
      this.bottomContainer.appendChild(tabs);
      var bottomContent = document.createElement('div'); //bottomContent.id = 'bottomContainer_content';

      bottomContent.className = 'tabsContentContainer';
      this.bottomContainerContent = bottomContent;
      this.bottomContainer.appendChild(bottomContent);
      this.initPlugins();
    }

    _createClass(ExtendedAdapter, [{
      key: "initPlugins",
      value: function initPlugins() {
        paella.pluginManager.setTarget('rightBarPlugin', this);
        paella.pluginManager.setTarget('tabBarPlugin', this);
      }
    }, {
      key: "addPlugin",
      value: function addPlugin(plugin) {
        var thisClass = this;
        plugin.checkEnabled(function (isEnabled) {
          if (isEnabled) {
            paella.pluginManager.setupPlugin(plugin);

            if (plugin.type == 'rightBarPlugin') {
              thisClass.rightBarPlugins.push(plugin);
              thisClass.addRightBarPlugin(plugin);
            }

            if (plugin.type == 'tabBarPlugin') {
              thisClass.tabBarPlugins.push(plugin);
              thisClass.addTabPlugin(plugin);
            }
          }
        });
      }
    }, {
      key: "showTab",
      value: function showTab(tabIndex) {
        var i = 0;
        var labels = this.bottomContainer.getElementsByClassName("tabLabel");
        var contents = this.bottomContainer.getElementsByClassName("tabContent");

        for (i = 0; i < labels.length; ++i) {
          if (labels[i].getAttribute("tab") == tabIndex) {
            labels[i].className = "tabLabel enabled";
          } else {
            labels[i].className = "tabLabel disabled";
          }
        }

        for (i = 0; i < contents.length; ++i) {
          if (contents[i].getAttribute("tab") == tabIndex) {
            contents[i].className = "tabContent enabled";
          } else {
            contents[i].className = "tabContent disabled";
          }
        }
      }
    }, {
      key: "addTabPlugin",
      value: function addTabPlugin(plugin) {
        var thisClass = this;
        var tabIndex = this.currentTabIndex; // Add tab

        var tabItem = document.createElement('div');
        tabItem.setAttribute("tab", tabIndex);
        tabItem.className = "tabLabel disabled";
        tabItem.innerText = plugin.getTabName();
        tabItem.plugin = plugin;
        $(tabItem).click(function (event) {
          if (/disabled/.test(this.className)) {
            thisClass.showTab(tabIndex);
            this.plugin.action(this);
          }
        });
        $(tabItem).keyup(function (event) {
          if (event.keyCode == 13) {
            if (/disabledTabItem/.test(this.className)) {
              thisClass.showTab(tabIndex);
              this.plugin.action(this);
            }
          }
        });
        this.bottomContainerTabs.appendChild(tabItem); // Add tab content

        var tabContent = document.createElement('div');
        tabContent.setAttribute("tab", tabIndex);
        tabContent.className = "tabContent disabled " + plugin.getSubclass();
        this.bottomContainerContent.appendChild(tabContent);
        plugin.buildContent(tabContent);
        plugin.button = tabItem;
        plugin.container = tabContent;
        plugin.button.setAttribute("tabindex", 3000 + plugin.getIndex());
        plugin.button.setAttribute("alt", "");
        plugin.setToolTip(plugin.getDefaultToolTip()); // Show tab

        if (this.firstTabShown === undefined) {
          this.showTab(tabIndex);
          this.firstTabShown = true;
        }

        ++this.currentTabIndex;
      }
    }, {
      key: "addRightBarPlugin",
      value: function addRightBarPlugin(plugin) {
        var container = document.createElement('div');
        container.className = "rightBarPluginContainer " + plugin.getSubclass();
        this.rightContainer.appendChild(container);
        plugin.buildContent(container);
      }
    }]);

    return ExtendedAdapter;
  }();

  paella.ExtendedAdapter = ExtendedAdapter;
  paella.extendedAdapter = new paella.ExtendedAdapter();
})();

try {
  module["export"] = paella;
} catch (e) {}
/*
Plugin override: PlayButtonOnScreen

Display always the big play button on the player, no matter the
stream is live or not.
*/


paella.addPlugin(function () {
  var PlayButtonOnScreenAlwaysPlugin = /*#__PURE__*/function (_paella$EventDrivenPl) {
    _inherits(PlayButtonOnScreenAlwaysPlugin, _paella$EventDrivenPl);

    var _super53 = _createSuper(PlayButtonOnScreenAlwaysPlugin);

    _createClass(PlayButtonOnScreenAlwaysPlugin, [{
      key: "getName",
      value: function getName() {
        return "ch.cern.paella.playButtonOnScreenAlwaysPlugin";
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 1022;
      }
    }]);

    function PlayButtonOnScreenAlwaysPlugin() {
      var _this137;

      _classCallCheck(this, PlayButtonOnScreenAlwaysPlugin);

      _this137 = _super53.call(this);
      _this137.containerId = 'paella_plugin_PlayButtonOnScreen_Always';
      _this137.container = null;
      _this137.enabled = true;
      _this137.isPlaying = false;
      _this137.showIcon = true;
      _this137.firstPlay = false;
      return _this137;
    }

    _createClass(PlayButtonOnScreenAlwaysPlugin, [{
      key: "setup",
      value: function setup() {
        var _this138 = this;

        this.container = paella.LazyThumbnailContainer.GetIconElement();
        paella.player.videoContainer.domElement.appendChild(this.container);
        $(this.container).click(function () {
          return _this138.onPlayButtonClick();
        });
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        var _this139 = this;

        this.showOnEnd = true;
        paella.data.read('relatedVideos', {
          id: paella.player.videoIdentifier
        }, function (data) {
          _this139.showOnEnd = !Array.isArray(data) || data.length == 0;
        });
        onSuccess(true);
      }
    }, {
      key: "getEvents",
      value: function getEvents() {
        return [paella.events.endVideo, paella.events.play, paella.events.pause, paella.events.showEditor, paella.events.hideEditor];
      }
    }, {
      key: "onEvent",
      value: function onEvent(eventType, params) {
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
    }, {
      key: "onPlayButtonClick",
      value: function onPlayButtonClick() {
        this.firstPlay = true;
        this.checkStatus();
      }
    }, {
      key: "endVideo",
      value: function endVideo() {
        this.isPlaying = false;
        this.showIcon = this.showOnEnd;
        this.checkStatus();
      }
    }, {
      key: "play",
      value: function play() {
        this.isPlaying = true;
        this.showIcon = false;
        this.checkStatus();
      }
    }, {
      key: "pause",
      value: function pause() {
        this.isPlaying = false;
        this.showIcon = true;
        this.checkStatus();
      }
    }, {
      key: "showEditor",
      value: function showEditor() {
        this.enabled = false;
        this.checkStatus();
      }
    }, {
      key: "hideEditor",
      value: function hideEditor() {
        this.enabled = true;
        this.checkStatus();
      }
    }, {
      key: "checkStatus",
      value: function checkStatus() {
        if (this.enabled && this.isPlaying || !this.enabled || !this.showIcon) {
          $(this.container).hide();
        } // Only show play button if none of the video players require mouse events
        else if (!paella.player.videoContainer.streamProvider.videoPlayers.every(function (p) {
            return p.canvasData.mouseEventsSupport;
          })) {
            $(this.container).show();
          }
      }
    }]);

    return PlayButtonOnScreenAlwaysPlugin;
  }(paella.EventDrivenPlugin);

  paella.plugins.PlayButtonOnScreenAlwaysPlugin = PlayButtonOnScreenAlwaysPlugin;
  return PlayButtonOnScreenAlwaysPlugin;
});
paella.addPlugin(function () {
  var FlexSkipPlugin = /*#__PURE__*/function (_paella$ButtonPlugin2) {
    _inherits(FlexSkipPlugin, _paella$ButtonPlugin2);

    var _super54 = _createSuper(FlexSkipPlugin);

    function FlexSkipPlugin() {
      _classCallCheck(this, FlexSkipPlugin);

      return _super54.apply(this, arguments);
    }

    _createClass(FlexSkipPlugin, [{
      key: "getAlignment",
      value: function getAlignment() {
        return 'left';
      }
    }, {
      key: "getName",
      value: function getName() {
        return "edu.harvard.dce.paella.flexSkipPlugin";
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 121;
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return 'flexSkip_Rewind_10';
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-back-10-s';
      }
    }, {
      key: "formatMessage",
      value: function formatMessage() {
        return 'Rewind 10 seconds';
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate(this.formatMessage());
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        onSuccess(!paella.player.isLiveStream());
      }
    }, {
      key: "action",
      value: function action(button) {
        paella.player.videoContainer.currentTime().then(function (currentTime) {
          paella.player.videoContainer.seekToTime(currentTime - 10);
        });
      }
    }]);

    return FlexSkipPlugin;
  }(paella.ButtonPlugin);

  paella.plugins.FlexSkipPlugin = FlexSkipPlugin;
  return FlexSkipPlugin;
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$plugins$FlexS) {
    _inherits(FlexSkipForwardPlugin, _paella$plugins$FlexS);

    var _super55 = _createSuper(FlexSkipForwardPlugin);

    function FlexSkipForwardPlugin() {
      _classCallCheck(this, FlexSkipForwardPlugin);

      return _super55.apply(this, arguments);
    }

    _createClass(FlexSkipForwardPlugin, [{
      key: "getIndex",
      value: function getIndex() {
        return 122;
      }
    }, {
      key: "getName",
      value: function getName() {
        return "edu.harvard.dce.paella.flexSkipForwardPlugin";
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return 'flexSkip_Forward_30';
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-forward-30-s';
      }
    }, {
      key: "formatMessage",
      value: function formatMessage() {
        return 'Forward 30 seconds';
      }
    }, {
      key: "action",
      value: function action(button) {
        paella.player.videoContainer.currentTime().then(function (currentTime) {
          paella.player.videoContainer.seekToTime(currentTime + 30);
        });
      }
    }]);

    return FlexSkipForwardPlugin;
  }(paella.plugins.FlexSkipPlugin);
});
paella.addPlugin(function () {
  /////////////////////////////////////////////////
  // WebVTT Parser
  /////////////////////////////////////////////////
  return /*#__PURE__*/function (_paella$CaptionParser) {
    _inherits(WebVTTParserPlugin, _paella$CaptionParser);

    var _super56 = _createSuper(WebVTTParserPlugin);

    function WebVTTParserPlugin() {
      _classCallCheck(this, WebVTTParserPlugin);

      return _super56.apply(this, arguments);
    }

    _createClass(WebVTTParserPlugin, [{
      key: "getName",
      value: function getName() {
        return "es.teltek.paella.captions.WebVTTParserPlugin";
      }
    }, {
      key: "parse",
      value: function parse(content, lang, next) {
        var captions = [];
        var self = this;
        var lls = content.split("\n");
        var c;
        var id = 0;
        var skip = false;

        for (var idx = 0; idx < lls.length; ++idx) {
          var ll = lls[idx].trim();

          if (/^WEBVTT/.test(ll) && c === undefined || ll.length === 0) {
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
              end: self.parseTimeTextToSeg(ll.split("-->")[1])
            };
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
    }, {
      key: "parseTimeTextToSeg",
      value: function parseTimeTextToSeg(ttime) {
        var nseg = 0;
        var factor = 1;
        ttime = /(([0-9]+:)?[0-9]{2}:[0-9]{2}.[0-9]{3})/.exec(ttime);
        var split = ttime[0].split(":");

        for (var i = split.length - 1; i >= 0; i--) {
          factor = Math.pow(60, split.length - 1 - i);
          nseg += split[i] * factor;
        }

        return nseg;
      }
    }, {
      key: "ext",
      get: function get() {
        return ["vtt"];
      }
    }]);

    return WebVTTParserPlugin;
  }(paella.CaptionParserPlugIn);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$userTracking$) {
    _inherits(xAPISaverPlugin, _paella$userTracking$);

    var _super57 = _createSuper(xAPISaverPlugin);

    function xAPISaverPlugin() {
      _classCallCheck(this, xAPISaverPlugin);

      return _super57.apply(this, arguments);
    }

    _createClass(xAPISaverPlugin, [{
      key: "getName",
      value: function getName() {
        return "es.teltek.paella.usertracking.xAPISaverPlugin";
      }
    }, {
      key: "setup",
      value: function setup() {
        this.endpoint = this.config.endpoint;
        this.auth = this.config.auth;
        this.user_info = {};
        this.paused = true;
        this.played_segments = "";
        this.played_segments_segment_start = null;
        this.played_segments_segment_end = null;
        this.progress = 0;
        this.duration = 0;
        this.current_time = [];
        this.completed = false;
        this.volume = null;
        this.speed = null;
        this.language = "us-US";
        this.quality = null;
        this.fullscreen = false;
        this.title = "No title available";
        this.description = "";
        this.user_agent = "";
        this.total_time = 0;
        this.total_time_start = 0;
        this.total_time_end = 0;
        this.session_id = "";
        var self = this;

        this._loadDeps().then(function () {
          var conf = {
            "endpoint": self.endpoint,
            "auth": "Basic " + toBase64(self.auth)
          };
          ADL.XAPIWrapper.changeConfig(conf);
        });

        paella.events.bind(paella.events.timeUpdate, function (event, params) {
          self.current_time.push(params.currentTime);

          if (self.current_time.length >= 10) {
            self.current_time = self.current_time.slice(-10);
          }

          var a = Math.round(self.current_time[0]);
          var b = Math.round(self.current_time[9]);

          if (params.currentTime !== 0 && a + 1 >= b && b - 1 >= a) {
            self.progress = self.get_progress(params.currentTime, params.duration);

            if (self.progress >= 0.95 && self.completed === false) {
              self.completed = true;
              self.end_played_segment(params.currentTime);
              self.start_played_segment(params.currentTime);
              self.send_completed(params.currentTime, self.progress);
            }
          }
        });
      }
    }, {
      key: "get_session_data",
      value: function get_session_data() {
        var myparams = ADL.XAPIWrapper.searchParams();
        var agent = JSON.stringify({
          "mbox": this.user_info.email
        });
        var timestamp = new Date();
        timestamp.setDate(timestamp.getDate() - 1);
        timestamp = timestamp.toISOString();
        myparams['activity'] = window.location.href;
        myparams['verb'] = 'http://adlnet.gov/expapi/verbs/terminated';
        myparams['since'] = timestamp;
        myparams['limit'] = 1;
        myparams['agent'] = agent;
        var ret = ADL.XAPIWrapper.getStatements(myparams);

        if (ret.statements.length === 1) {
          this.played_segments = ret.statements[0].result.extensions['https://w3id.org/xapi/video/extensions/played-segments'];
          this.progress = ret.statements[0].result.extensions['https://w3id.org/xapi/video/extensions/progress'];
          ADL.XAPIWrapper.lrs.registration = ret.statements[0].context.registration;
        } else {
          ADL.XAPIWrapper.lrs.registration = ADL.ruuid();
        }
      }
    }, {
      key: "getCookie",
      value: function getCookie(cname) {
        var name = cname + "=";
        var decodedCookie = decodeURIComponent(document.cookie);
        var ca = decodedCookie.split(';');

        for (var i = 0; i < ca.length; i++) {
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
    }, {
      key: "setCookie",
      value: function setCookie(cname, cvalue, exdays) {
        var d = new Date();
        d.setTime(d.getTime() + exdays * 24 * 60 * 60 * 1000);
        var expires = "expires=" + d.toUTCString();
        document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
      }
    }, {
      key: "checkCookie",
      value: function checkCookie() {
        var user_info = this.getCookie("user_info");

        if (user_info === "") {
          user_info = JSON.stringify(generateName());
        }

        this.setCookie("user_info", user_info);
        return JSON.parse(user_info);
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        this._url = this.config.url;
        this._index = this.config.index || "paellaplayer";
        this._type = this.config.type || "usertracking";
        onSuccess(true);
      }
    }, {
      key: "_loadDeps",
      value: function _loadDeps() {
        return new Promise(function (resolve, reject) {
          paella.require('resources/deps/xapiwrapper.min.js').then(function () {
            return paella.require('resources/deps/random_name_generator.js');
          }).then(function () {
            resolve();
          });
        });
      }
    }, {
      key: "log",
      value: function log(event, params) {
        var p = params;
        var self = this; // console.log(event)
        // console.log(params)

        switch (event) {
          //Retrieve initial parameters from player
          case "paella:loadComplete":
            this.user_agent = navigator.userAgent.toString();
            this.get_title();
            this.get_description();
            paella.player.videoContainer.duration().then(function (duration) {
              return paella.player.videoContainer.streamProvider.mainAudioPlayer.volume().then(function (volume) {
                return paella.player.videoContainer.getCurrentQuality().then(function (quality) {
                  return paella.player.auth.userData().then(function (user_info) {
                    self.duration = duration;
                    self.volume = volume;
                    self.speed = 1;

                    if (paella.player.videoContainer.streamProvider.mainAudioPlayer.stream.language) {
                      self.language = paella.player.videoContainer.streamProvider.mainAudioPlayer.stream.language.replace("_", "-");
                    }

                    self.quality = quality.shortLabel();

                    if (user_info.email && user_info.name) {
                      self.user_info = user_info;
                    } else {
                      self.user_info = self.checkCookie();
                    }

                    self.get_session_data();
                    self.send_initialized();
                  });
                });
              });
            });

            window.onbeforeunload = function (e) {
              if (!self.paused) {
                self.send_pause(self);
              } //TODO Algunas veces se envia terminated antes que paused


              self.send_terminated(self); // var dialogText = 'Dialog text here';
              // e.returnValue = dialogText;
              // window.onbeforeunload = null;
              // return dialogText;
            };

            break;

          case "paella:play":
            this.send_play(self);
            break;

          case "paella:pause":
            this.send_pause(self);
            break;

          case "paella:seektotime":
            this.send_seek(self, params);
            break;
          //Player options

          case "paella:setVolume":
            paella.player.videoContainer.currentTime().then(function (currentTime) {
              var current_time = self.format_float(currentTime);
              self.volume = params.master; //self.send_interacted(current_time, {"https://w3id.org/xapi/video/extensions/volume": params.master})

              var interacted = {
                "https://w3id.org/xapi/video/extensions/volume": self.format_float(params.master)
              };
              self.send_interacted(current_time, interacted);
            });
            break;

          case "paella:setplaybackrate":
            paella.player.videoContainer.currentTime().then(function (currentTime) {
              var current_time = self.format_float(currentTime);
              self.speed = params.rate;
              var interacted = {
                "https://w3id.org/xapi/video/extensions/speed": params.rate + "x"
              };
              self.send_interacted(current_time, interacted);
            });
            break;

          case "paella:qualityChanged":
            paella.player.videoContainer.currentTime().then(function (currentTime) {
              return paella.player.videoContainer.getCurrentQuality().then(function (quality) {
                self.quality = quality.shortLabel();
                var current_time = self.format_float(currentTime);
                var interacted = {
                  "https://w3id.org/xapi/video/extensions/quality": quality.shortLabel()
                };
                self.send_interacted(current_time, interacted);
              });
            });
            break;

          case "paella:enterFullscreen":
          case "paella:exitFullscreen":
            paella.player.videoContainer.currentTime().then(function (currentTime) {
              var current_time = self.format_float(currentTime);
              self.fullscreen ? self.fullscreen = false : self.fullscreen = true;
              var interacted = {
                "https://w3id.org/xapi/video/extensions/full-screen": self.fullscreen
              };
              self.send_interacted(current_time, interacted);
            });
            break;

          default:
            break;
        }
      }
    }, {
      key: "send",
      value: function send(params) {
        var agent = new ADL.XAPIStatement.Agent(this.user_info.email, this.user_info.name);
        var verb = new ADL.XAPIStatement.Verb(params.verb.id, params.verb.description);
        var activity = new ADL.XAPIStatement.Activity(window.location.href, this.title, this.description);
        activity.definition.type = "https://w3id.org/xapi/video/activity-type/video";
        paella.player.videoContainer.streamProvider.mainAudioPlayer.volume().then(function (volume) {});
        var statement = new ADL.XAPIStatement(agent, verb, activity);
        statement.result = params.result;

        if (params.verb.id === "http://adlnet.gov/expapi/verbs/initialized") {
          statement.generateId();
          this.session_id = statement.id;
        }

        var ce_base = {
          "https://w3id.org/xapi/video/extensions/session-id": this.session_id,
          "https://w3id.org/xapi/video/extensions/length": Math.floor(this.duration),
          "https://w3id.org/xapi/video/extensions/user-agent": this.user_agent
        };
        var ce_interactions = {
          "https://w3id.org/xapi/video/extensions/volume": this.format_float(this.volume),
          "https://w3id.org/xapi/video/extensions/speed": this.speed + "x",
          "https://w3id.org/xapi/video/extensions/quality": this.quality,
          "https://w3id.org/xapi/video/extensions/full-screen": this.fullscreen
        };
        var context_extensions = {};

        if (params.interacted) {
          context_extensions = $.extend({}, ce_base, params.interacted);
        } else {
          context_extensions = $.extend({}, ce_base, ce_interactions);
        }

        statement.context = {
          "language": this.language,
          "extensions": context_extensions,
          "contextActivities": {
            "category": [{
              "objectType": "Activity",
              "id": "https://w3id.org/xapi/video"
            }]
          }
        }; // Dispatch the statement to the LRS

        var result = ADL.XAPIWrapper.sendStatement(statement);
      }
    }, {
      key: "send_initialized",
      value: function send_initialized() {
        var statement = {
          "verb": {
            "id": "http://adlnet.gov/expapi/verbs/initialized",
            "description": "initalized"
          }
        };
        this.send(statement);
      }
    }, {
      key: "send_terminated",
      value: function send_terminated(self) {
        paella.player.videoContainer.currentTime().then(function (end_time) {
          var statement = {
            "verb": {
              "id": "http://adlnet.gov/expapi/verbs/terminated",
              "description": "terminated"
            },
            "result": {
              "extensions": {
                "https://w3id.org/xapi/video/extensions/time": end_time,
                "https://w3id.org/xapi/video/extensions/progress": self.progress,
                "https://w3id.org/xapi/video/extensions/played-segments": self.played_segments
              }
            }
          };
          self.send(statement);
        });
      }
    }, {
      key: "send_play",
      value: function send_play(self) {
        this.paused = false;
        this.total_time_start = new Date().getTime() / 1000;
        paella.player.videoContainer.currentTime().then(function (currentTime) {
          var start_time = self.format_float(currentTime); //When the video starts we force start_time to 0

          if (start_time <= 1) {
            start_time = 0;
          }

          self.start_played_segment(start_time);
          var statement = {
            "verb": {
              "id": "https://w3id.org/xapi/video/verbs/played",
              "description": "played"
            },
            "result": {
              "extensions": {
                "https://w3id.org/xapi/video/extensions/time": start_time
              }
            }
          };
          self.send(statement);
        });
      }
    }, {
      key: "send_pause",
      value: function send_pause(self) {
        this.paused = true;
        this.total_time_end = new Date().getTime() / 1000;
        this.total_time += this.total_time_end - this.total_time_start;
        paella.player.videoContainer.currentTime().then(function (currentTime) {
          //return paella.player.videoContainer.duration().then(function(duration) {
          var end_time = self.format_float(currentTime); //self.progress = self.get_progress(end_time, duration)
          //If a video end, the player go to the video start and raise a pause event with currentTime = 0

          if (end_time === 0) {
            end_time = self.duration;
          }

          self.end_played_segment(end_time);
          var statement = {
            "verb": {
              "id": "https://w3id.org/xapi/video/verbs/paused",
              "description": "paused"
            },
            "result": {
              "extensions": {
                "https://w3id.org/xapi/video/extensions/time": end_time,
                "https://w3id.org/xapi/video/extensions/progress": self.progress,
                "https://w3id.org/xapi/video/extensions/played-segments": self.played_segments
              }
            }
          };
          self.send(statement);
        }); //});
      }
    }, {
      key: "send_seek",
      value: function send_seek(self, params) {
        var seekedto = this.format_float(params.newPosition); //FIXME Metodo para obtener el instante desde donde empieza el seek

        var a = this.current_time.filter(function (value) {
          return value <= seekedto - 1;
        });

        if (a.length === 0) {
          a = this.current_time.filter(function (value) {
            return value >= seekedto + 1;
          });
        } //In some cases, when you seek to the end of the video the array contains zeros at the end


        var seekedfrom = a.filter(Number).pop();
        this.current_time = [];
        this.current_time.push(seekedto); // Fin de FIXME
        //If the video is paused it's not neccesary create a new segment, because the pause event already close a segment

        self.progress = self.get_progress(seekedfrom, self.duration);

        if (!this.paused) {
          this.end_played_segment(seekedfrom);
          this.start_played_segment(seekedto);
        } //paella.player.videoContainer.duration().then(function(duration) {
        //var progress = self.get_progress(seekedfrom, duration)


        var statement = {
          "verb": {
            "id": "https://w3id.org/xapi/video/verbs/seeked",
            "description": "seeked"
          },
          "result": {
            "extensions": {
              "https://w3id.org/xapi/video/extensions/time-from": seekedfrom,
              "https://w3id.org/xapi/video/extensions/time-to": seekedto,
              // Aqui tambien deberiamos de enviar los segmentos reproducidos y el porcentaje
              "https://w3id.org/xapi/video/extensions/progress": self.progress,
              "https://w3id.org/xapi/video/extensions/played-segments": self.played_segments
            }
          }
        };
        self.send(statement); //})
      }
    }, {
      key: "send_completed",
      value: function send_completed(time, progress) {
        var statement = {
          "verb": {
            "id": "http://adlnet.gov/xapi/verbs/completed",
            "description": "completed"
          },
          "result": {
            "completion": true,
            "success": true,
            "duration": "PT" + this.total_time + "S",
            "extensions": {
              "https://w3id.org/xapi/video/extensions/time": time,
              "https://w3id.org/xapi/video/extensions/progress": progress,
              "https://w3id.org/xapi/video/extensions/played-segments": this.played_segments
            }
          }
        };
        this.send(statement);
      }
    }, {
      key: "send_interacted",
      value: function send_interacted(current_time, interacted) {
        var statement = {
          "verb": {
            "id": "http://adlnet.gov/expapi/verbs/interacted",
            "description": "interacted"
          },
          "result": {
            "extensions": {
              "https://w3id.org/xapi/video/extensions/time": current_time
            }
          },
          "interacted": interacted
        };
        this.send(statement);
      }
    }, {
      key: "start_played_segment",
      value: function start_played_segment(start_time) {
        this.played_segments_segment_start = start_time;
      }
    }, {
      key: "end_played_segment",
      value: function end_played_segment(end_time) {
        var arr;
        arr = this.played_segments === "" ? [] : this.played_segments.split("[,]");
        arr.push(this.played_segments_segment_start + "[.]" + end_time);
        this.played_segments = arr.join("[,]");
        this.played_segments_segment_end = end_time; //this.played_segments_segment_start = null;
      }
    }, {
      key: "format_float",
      value: function format_float(number) {
        number = parseFloat(number); //Ensure that number is a float

        return parseFloat(number.toFixed(3));
      }
    }, {
      key: "get_title",
      value: function get_title() {
        if (paella.player.videoLoader.getMetadata().i18nTitle) {
          this.title = paella.player.videoLoader.getMetadata().i18nTitle;
        } else if (paella.player.videoLoader.getMetadata().title) {
          this.title = paella.player.videoLoader.getMetadata().title;
        }
      }
    }, {
      key: "get_description",
      value: function get_description() {
        if (paella.player.videoLoader.getMetadata().i18nTitle) {
          this.description = paella.player.videoLoader.getMetadata().i18nDescription;
        } else {
          this.description = paella.player.videoLoader.getMetadata().description;
        }
      }
    }, {
      key: "get_progress",
      value: function get_progress(currentTime, duration) {
        var arr, arr2; //get played segments array

        arr = this.played_segments === "" ? [] : this.played_segments.split("[,]");

        if (this.played_segments_segment_start != null) {
          arr.push(this.played_segments_segment_start + "[.]" + currentTime);
        }

        arr2 = [];
        arr.forEach(function (v, i) {
          arr2[i] = v.split("[.]");
          arr2[i][0] *= 1;
          arr2[i][1] *= 1;
        }); //sort the array

        arr2.sort(function (a, b) {
          return a[0] - b[0];
        }); //normalize the segments

        arr2.forEach(function (v, i) {
          if (i > 0) {
            if (arr2[i][0] < arr2[i - 1][1]) {
              //overlapping segments: this segment's starting point is less than last segment's end point.
              //console.log(arr2[i][0] + " < " + arr2[i-1][1] + " : " + arr2[i][0] +" = " +arr2[i-1][1] );
              arr2[i][0] = arr2[i - 1][1];
              if (arr2[i][0] > arr2[i][1]) arr2[i][1] = arr2[i][0];
            }
          }
        }); //calculate progress_length

        var progress_length = 0;
        arr2.forEach(function (v, i) {
          if (v[1] > v[0]) progress_length += v[1] - v[0];
        });
        var progress = 1 * (progress_length / duration).toFixed(2);
        return progress;
      }
    }]);

    return xAPISaverPlugin;
  }(paella.userTracking.SaverPlugIn);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$EventDrivenPl2) {
    _inherits(SingleStreamProfilePlugin, _paella$EventDrivenPl2);

    var _super58 = _createSuper(SingleStreamProfilePlugin);

    function SingleStreamProfilePlugin() {
      _classCallCheck(this, SingleStreamProfilePlugin);

      return _super58.apply(this, arguments);
    }

    _createClass(SingleStreamProfilePlugin, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.singleStreamProfilePlugin";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        var config = this.config;
        config.videoSets.forEach(function (videoSet, index) {
          var validContent = videoSet.content;

          if (validContent.length == 1) {
            var streamCount = 0;
            paella.player.videoContainer.streamProvider.videoStreams.forEach(function (v) {
              if (validContent.indexOf(v.content) != -1) {
                streamCount++;
              }
            });

            if (streamCount >= 1) {
              onSuccess(true);
              paella.addProfile(function () {
                return new Promise(function (resolve, reject) {
                  resolve({
                    id: videoSet.id,
                    name: {
                      es: "Un stream"
                    },
                    hidden: false,
                    icon: videoSet.icon,
                    videos: [{
                      content: validContent[0],
                      rect: [{
                        aspectRatio: "1/1",
                        left: 280,
                        top: 0,
                        width: 720,
                        height: 720
                      }, {
                        aspectRatio: "6/5",
                        left: 208,
                        top: 0,
                        width: 864,
                        height: 720
                      }, {
                        aspectRatio: "5/4",
                        left: 190,
                        top: 0,
                        width: 900,
                        height: 720
                      }, {
                        aspectRatio: "4/3",
                        left: 160,
                        top: 0,
                        width: 960,
                        height: 720
                      }, {
                        aspectRatio: "11/8",
                        left: 145,
                        top: 0,
                        width: 990,
                        height: 720
                      }, {
                        aspectRatio: "1.41/1",
                        left: 132,
                        top: 0,
                        width: 1015,
                        height: 720
                      }, {
                        aspectRatio: "1.43/1",
                        left: 125,
                        top: 0,
                        width: 1029,
                        height: 720
                      }, {
                        aspectRatio: "3/2",
                        left: 100,
                        top: 0,
                        width: 1080,
                        height: 720
                      }, {
                        aspectRatio: "16/10",
                        left: 64,
                        top: 0,
                        width: 1152,
                        height: 720
                      }, {
                        aspectRatio: "5/3",
                        left: 40,
                        top: 0,
                        width: 1200,
                        height: 720
                      }, {
                        aspectRatio: "16/9",
                        left: 0,
                        top: 0,
                        width: 1280,
                        height: 720
                      }, {
                        aspectRatio: "1.85/1",
                        left: 0,
                        top: 14,
                        width: 1280,
                        height: 692
                      }, {
                        aspectRatio: "2.35/1",
                        left: 0,
                        top: 87,
                        width: 1280,
                        height: 544
                      }, {
                        aspectRatio: "2.41/1",
                        left: 0,
                        top: 94,
                        width: 1280,
                        height: 531
                      }, {
                        aspectRatio: "2.76/1",
                        left: 0,
                        top: 128,
                        width: 1280,
                        height: 463
                      }],
                      visible: true,
                      layer: 1
                    }],
                    background: {
                      content: "slide_professor_paella.jpg",
                      zIndex: 5,
                      rect: {
                        left: 0,
                        top: 0,
                        width: 1280,
                        height: 720
                      },
                      visible: true,
                      layer: 0
                    },
                    logos: [{
                      content: "paella_logo.png",
                      zIndex: 5,
                      rect: {
                        top: 10,
                        left: 10,
                        width: 49,
                        height: 42
                      }
                    }],
                    buttons: [],
                    onApply: function onApply() {}
                  });
                });
              });
            } else {
              onSuccess(false);
            }
          }
        });
      }
    }]);

    return SingleStreamProfilePlugin;
  }(paella.EventDrivenPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$EventDrivenPl3) {
    _inherits(DualStreamProfilePlugin, _paella$EventDrivenPl3);

    var _super59 = _createSuper(DualStreamProfilePlugin);

    function DualStreamProfilePlugin() {
      _classCallCheck(this, DualStreamProfilePlugin);

      return _super59.apply(this, arguments);
    }

    _createClass(DualStreamProfilePlugin, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.dualStreamProfilePlugin";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        var config = this.config;
        config.videoSets.forEach(function (videoSet, index) {
          var validContent = videoSet.content;

          if (validContent.length == 2) {
            var streamCount = 0;
            paella.player.videoContainer.streamProvider.videoStreams.forEach(function (v) {
              if (validContent.indexOf(v.content) != -1) {
                streamCount++;
              }
            });

            if (streamCount >= 2) {
              var nextLayout = function nextLayout() {
                var selectedLayout = JSON.parse(JSON.stringify(layouts[layout]));
                layout = (layout + 1) % layouts.length;
                selectedLayout.videos[0].content = validContent[0];
                selectedLayout.videos[1].content = validContent[1];
                return selectedLayout;
              };

              onSuccess(true);
              var layout = 0;
              var layouts = [// First layout: side by side
              {
                videos: [{
                  content: null,
                  rect: [{
                    aspectRatio: "16/9",
                    width: 560,
                    height: 315,
                    top: 198,
                    left: 712
                  }, {
                    aspectRatio: "16/10",
                    width: 560,
                    height: 350,
                    top: 186,
                    left: 712
                  }, {
                    aspectRatio: "4/3",
                    width: 560,
                    height: 420,
                    top: 153,
                    left: 712
                  }, {
                    aspectRatio: "5/3",
                    width: 560,
                    height: 336,
                    top: 186,
                    left: 712
                  }, {
                    aspectRatio: "5/4",
                    width: 560,
                    height: 448,
                    top: 140,
                    left: 712
                  }],
                  visible: true,
                  layer: 1
                }, {
                  content: null,
                  rect: [{
                    aspectRatio: "16/9",
                    width: 688,
                    height: 387,
                    top: 166,
                    left: 10
                  }, {
                    aspectRatio: "16/10",
                    width: 688,
                    height: 430,
                    top: 148,
                    left: 10
                  }, {
                    aspectRatio: "4/3",
                    width: 688,
                    height: 516,
                    top: 111,
                    left: 10
                  }, {
                    aspectRatio: "5/3",
                    width: 690,
                    height: 414,
                    top: 154,
                    left: 10
                  }, {
                    aspectRatio: "5/4",
                    width: 690,
                    height: 552,
                    top: 96,
                    left: 10
                  }],
                  visible: true,
                  layer: "1"
                }],
                buttons: [{
                  rect: {
                    left: 682,
                    top: 565,
                    width: 45,
                    height: 45
                  },
                  label: "Switch",
                  icon: "icon_rotate.svg",
                  layer: 2
                }, {
                  rect: {
                    left: 682,
                    top: 515,
                    width: 45,
                    height: 45
                  },
                  label: "Minimize",
                  icon: "minimize.svg",
                  layer: 2
                }]
              }, // Second layout: PIP left
              {
                videos: [{
                  content: null,
                  rect: [{
                    aspectRatio: "16/9",
                    left: 0,
                    top: 0,
                    width: 1280,
                    height: 720
                  }, {
                    aspectRatio: "16/10",
                    left: 64,
                    top: 0,
                    width: 1152,
                    height: 720
                  }, {
                    aspectRatio: "5/3",
                    left: 40,
                    top: 0,
                    width: 1200,
                    height: 720
                  }, {
                    aspectRatio: "5/4",
                    left: 190,
                    top: 0,
                    width: 900,
                    height: 720
                  }, {
                    aspectRatio: "4/3",
                    left: 160,
                    top: 0,
                    width: 960,
                    height: 720
                  }],
                  visible: true,
                  layer: 1
                }, {
                  content: null,
                  rect: [{
                    aspectRatio: "16/9",
                    left: 50,
                    top: 470,
                    width: 350,
                    height: 197
                  }, {
                    aspectRatio: "16/10",
                    left: 50,
                    top: 448,
                    width: 350,
                    height: 219
                  }, {
                    aspectRatio: "5/3",
                    left: 50,
                    top: 457,
                    width: 350,
                    height: 210
                  }, {
                    aspectRatio: "5/4",
                    left: 50,
                    top: 387,
                    width: 350,
                    height: 280
                  }, {
                    aspectRatio: "4/3",
                    left: 50,
                    top: 404,
                    width: 350,
                    height: 262
                  }],
                  visible: true,
                  layer: 2
                }],
                buttons: [{
                  rect: {
                    left: 388,
                    top: 465,
                    width: 45,
                    height: 45
                  },
                  label: "Switch",
                  icon: "icon_rotate.svg",
                  layer: 2
                }, {
                  rect: {
                    left: 388,
                    top: 415,
                    width: 45,
                    height: 45
                  },
                  label: "Switch",
                  icon: "minimize.svg",
                  layer: 2
                }]
              }, // Third layout: PIP right
              {
                videos: [{
                  content: null,
                  rect: [{
                    aspectRatio: "16/9",
                    left: 0,
                    top: 0,
                    width: 1280,
                    height: 720
                  }, {
                    aspectRatio: "16/10",
                    left: 64,
                    top: 0,
                    width: 1152,
                    height: 720
                  }, {
                    aspectRatio: "5/3",
                    left: 40,
                    top: 0,
                    width: 1200,
                    height: 720
                  }, {
                    aspectRatio: "5/4",
                    left: 190,
                    top: 0,
                    width: 900,
                    height: 720
                  }, {
                    aspectRatio: "4/3",
                    left: 160,
                    top: 0,
                    width: 960,
                    height: 720
                  }],
                  visible: true,
                  layer: 1
                }, {
                  content: null,
                  rect: [{
                    aspectRatio: "16/9",
                    left: 880,
                    top: 470,
                    width: 350,
                    height: 197
                  }, {
                    aspectRatio: "16/10",
                    left: 880,
                    top: 448,
                    width: 350,
                    height: 219
                  }, {
                    aspectRatio: "5/3",
                    left: 880,
                    top: 457,
                    width: 350,
                    height: 210
                  }, {
                    aspectRatio: "5/4",
                    left: 880,
                    top: 387,
                    width: 350,
                    height: 280
                  }, {
                    aspectRatio: "4/3",
                    left: 880,
                    top: 404,
                    width: 350,
                    height: 262
                  }],
                  visible: true,
                  layer: 2
                }],
                buttons: [{
                  rect: {
                    left: 848,
                    top: 465,
                    width: 45,
                    height: 45
                  },
                  onClick: function onClick() {
                    this["switch"]();
                  },
                  label: "Switch",
                  icon: "icon_rotate.svg",
                  layer: 2
                }, {
                  rect: {
                    left: 848,
                    top: 415,
                    width: 45,
                    height: 45
                  },
                  onClick: function onClick() {
                    this.switchMinimize();
                  },
                  label: "Switch",
                  icon: "minimize.svg",
                  layer: 2
                }]
              }];
              paella.addProfile(function () {
                var selectedLayout = nextLayout();
                return new Promise(function (resolve) {
                  resolve({
                    id: videoSet.id,
                    name: {
                      es: "Dos streams con posición dinámica"
                    },
                    hidden: false,
                    icon: videoSet.icon,
                    videos: selectedLayout.videos,
                    background: {
                      content: "slide_professor_paella.jpg",
                      zIndex: 5,
                      rect: {
                        left: 0,
                        top: 0,
                        width: 1280,
                        height: 720
                      },
                      visible: true,
                      layer: 0
                    },
                    logos: [{
                      content: "paella_logo.png",
                      zIndex: 5,
                      rect: {
                        top: 10,
                        left: 10,
                        width: 49,
                        height: 42
                      }
                    }],
                    buttons: [{
                      rect: selectedLayout.buttons[0].rect,
                      onClick: function onClick() {
                        this["switch"]();
                      },
                      label: "Switch",
                      icon: "icon_rotate.svg",
                      layer: 2
                    }, {
                      rect: selectedLayout.buttons[1].rect,
                      onClick: function onClick() {
                        this.switchMinimize();
                      },
                      label: "Minimize",
                      icon: "minimize.svg",
                      layer: 2
                    }],
                    onApply: function onApply() {},
                    "switch": function _switch() {
                      var v0 = this.videos[0].content;
                      var v1 = this.videos[1].content;
                      this.videos[0].content = v1;
                      this.videos[1].content = v0;
                      paella.profiles.placeVideos();
                    },
                    switchMinimize: function switchMinimize() {
                      var newLayout = nextLayout();
                      this.videos = newLayout.videos;
                      this.buttons[0].rect = newLayout.buttons[0].rect;
                      this.buttons[1].rect = newLayout.buttons[1].rect;
                      paella.profiles.placeVideos();
                    }
                  });
                });
              });
            } else {
              onSuccess(false);
            }
          }
        });
      }
    }]);

    return DualStreamProfilePlugin;
  }(paella.EventDrivenPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$EventDrivenPl4) {
    _inherits(TripleStreamProfilePlugin, _paella$EventDrivenPl4);

    var _super60 = _createSuper(TripleStreamProfilePlugin);

    function TripleStreamProfilePlugin() {
      _classCallCheck(this, TripleStreamProfilePlugin);

      return _super60.apply(this, arguments);
    }

    _createClass(TripleStreamProfilePlugin, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.tripleStreamProfilePlugin";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        var config = this.config;
        config.videoSets.forEach(function (videoSet, index) {
          var validContent = videoSet.content;

          if (validContent.length == 3) {
            var streamCount = 0;
            paella.player.videoContainer.streamProvider.videoStreams.forEach(function (v) {
              if (validContent.indexOf(v.content) != -1) {
                streamCount++;
              }
            });

            if (streamCount >= 3) {
              onSuccess(true);
              paella.addProfile(function () {
                return new Promise(function (resolve, reject) {
                  resolve({
                    id: videoSet.id,
                    name: {
                      es: "Tres streams posición dinámica"
                    },
                    hidden: false,
                    icon: videoSet.icon,
                    videos: [{
                      content: validContent[0],
                      rect: [{
                        aspectRatio: "16/9",
                        left: 239,
                        top: 17,
                        width: 803,
                        height: 451
                      }],
                      visible: true,
                      layer: 1
                    }, {
                      content: validContent[1],
                      rect: [{
                        aspectRatio: "16/9",
                        left: 44,
                        top: 482,
                        width: 389,
                        height: 218
                      }],
                      visible: true,
                      layer: 1
                    }, {
                      content: validContent[2],
                      rect: [{
                        aspectRatio: "16/9",
                        left: 847,
                        top: 482,
                        width: 389,
                        height: 218
                      }],
                      visible: true,
                      layer: 1
                    }],
                    background: {
                      content: "slide_professor_paella.jpg",
                      zIndex: 5,
                      rect: {
                        left: 0,
                        top: 0,
                        width: 1280,
                        height: 720
                      },
                      visible: true,
                      layer: 0
                    },
                    logos: [{
                      content: "paella_logo.png",
                      zIndex: 5,
                      rect: {
                        top: 10,
                        left: 10,
                        width: 49,
                        height: 42
                      }
                    }],
                    buttons: [{
                      rect: {
                        left: 618,
                        top: 495,
                        width: 45,
                        height: 45
                      },
                      onClick: function onClick(event) {
                        this.rotate();
                      },
                      label: "Rotate",
                      icon: "icon_rotate.svg",
                      layer: 2
                    }],
                    onApply: function onApply() {},
                    rotate: function rotate() {
                      var v0 = this.videos[0].content;
                      var v1 = this.videos[1].content;
                      var v2 = this.videos[2].content;
                      this.videos[0].content = v2;
                      this.videos[1].content = v0;
                      this.videos[2].content = v1;
                      paella.profiles.placeVideos();
                    }
                  });
                });
              });
            } else {
              onSuccess(false);
            }
          }
        });
      }
    }]);

    return TripleStreamProfilePlugin;
  }(paella.EventDrivenPlugin);
});
paella.addProfile(function () {
  return new Promise(function (resolve, reject) {
    paella.events.bind(paella.events.videoReady, function () {
      var available = paella.player.videoContainer.streamProvider.videoStreams.some(function (v) {
        return v.content == "blackboard";
      });

      if (!available) {
        resolve(null);
      } else {
        resolve({
          id: "blackboard_video_stream",
          name: {
            es: "Pizarra"
          },
          hidden: false,
          icon: "s_p_blackboard.svg",
          videos: [{
            content: "presentation",
            rect: [{
              aspectRatio: "16/9",
              left: 10,
              top: 70,
              width: 432,
              height: 243
            }],
            visible: true,
            layer: 1
          }, {
            content: "blackboard",
            rect: [{
              aspectRatio: "16/9",
              left: 450,
              top: 135,
              width: 816,
              height: 459
            }],
            visible: true,
            layer: 1
          }, {
            content: "presenter",
            rect: [{
              aspectRatio: "16/9",
              left: 10,
              top: 325,
              width: 432,
              height: 324
            }],
            visible: true,
            layer: 1
          }],
          //blackBoardImages: {left:10,top:325,width:432,height:324},
          background: {
            content: "slide_professor_paella.jpg",
            zIndex: 5,
            rect: {
              left: 0,
              top: 0,
              width: 1280,
              height: 720
            },
            visible: true,
            layer: 0
          },
          logos: [{
            content: "paella_logo.png",
            zIndex: 5,
            rect: {
              top: 10,
              left: 10,
              width: 49,
              height: 42
            }
          }],
          buttons: [{
            rect: {
              left: 422,
              top: 295,
              width: 45,
              height: 45
            },
            onClick: function onClick(event) {
              this.rotate();
            },
            label: "Rotate",
            icon: "icon_rotate.svg",
            layer: 2
          }],
          rotate: function rotate() {
            var v0 = this.videos[0].content;
            var v1 = this.videos[1].content;
            var v2 = this.videos[2].content;
            this.videos[0].content = v2;
            this.videos[1].content = v0;
            this.videos[2].content = v1;
            paella.profiles.placeVideos();
          }
        });
      }
    });
  });
});
paella.addProfile(function () {
  return new Promise(function (resolve, reject) {
    paella.events.bind(paella.events.videoReady, function () {
      // TODO: videoContainer.sourceData is deprecated. Update this code
      var n = paella.player.videoContainer.sourceData[0].sources;

      if (!n.chroma) {
        resolve(null);
      } else {
        resolve({
          id: "chroma",
          name: {
            es: "Polimedia"
          },
          hidden: false,
          icon: "chroma.svg",
          videos: [{
            content: "presenter",
            rect: [{
              aspectRatio: "16/9",
              left: 0,
              top: 0,
              width: 1280,
              height: 720
            }, {
              aspectRatio: "16/10",
              left: 64,
              top: 0,
              width: 1152,
              height: 720
            }, {
              aspectRatio: "5/3",
              left: 40,
              top: 0,
              width: 1200,
              height: 720
            }, {
              aspectRatio: "5/4",
              left: 190,
              top: 0,
              width: 900,
              height: 720
            }, {
              aspectRatio: "4/3",
              left: 160,
              top: 0,
              width: 960,
              height: 720
            }],
            visible: "true",
            layer: "1"
          }, {
            content: "presentation",
            rect: [{
              aspectRatio: "16/9",
              left: 0,
              top: 0,
              width: 1280,
              height: 720
            }, {
              aspectRatio: "16/10",
              left: 64,
              top: 0,
              width: 1152,
              height: 720
            }, {
              aspectRatio: "5/3",
              left: 40,
              top: 0,
              width: 1200,
              height: 720
            }, {
              aspectRatio: "5/4",
              left: 190,
              top: 0,
              width: 900,
              height: 720
            }, {
              aspectRatio: "4/3",
              left: 160,
              top: 0,
              width: 960,
              height: 720
            }],
            visible: "true",
            layer: "0"
          }],
          background: {
            content: "default_background_paella.jpg",
            zIndex: 5,
            rect: {
              left: 0,
              top: 0,
              width: 1280,
              height: 720
            },
            visible: "true",
            layer: "0"
          },
          logos: [{
            content: "paella_logo.png",
            zIndex: 5,
            rect: {
              top: 10,
              left: 10,
              width: 49,
              height: 42
            }
          }]
        });
      }
    });
  });
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$EventDrivenPl5) {
    _inherits(TrimmingLoaderPlugin, _paella$EventDrivenPl5);

    var _super61 = _createSuper(TrimmingLoaderPlugin);

    function TrimmingLoaderPlugin() {
      _classCallCheck(this, TrimmingLoaderPlugin);

      return _super61.apply(this, arguments);
    }

    _createClass(TrimmingLoaderPlugin, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.trimmingPlayerPlugin";
      }
    }, {
      key: "getEvents",
      value: function getEvents() {
        return [paella.events.controlBarLoaded, paella.events.showEditor, paella.events.hideEditor];
      }
    }, {
      key: "onEvent",
      value: function onEvent(eventType, params) {
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
    }, {
      key: "loadTrimming",
      value: function loadTrimming() {
        var videoId = paella.initDelegate.getId();
        paella.data.read('trimming', {
          id: videoId
        }, function (data, status) {
          if (data && status && data.end > 0) {
            paella.player.videoContainer.enableTrimming();
            paella.player.videoContainer.setTrimming(data.start, data.end).then(function () {});
          } else {
            // Check for optional trim 'start' and 'end', in seconds, in location args
            var startTime = paella.utils.parameters.get('start');
            var endTime = paella.utils.parameters.get('end');

            if (startTime && endTime) {
              paella.player.videoContainer.enableTrimming();
              paella.player.videoContainer.setTrimming(startTime, endTime).then(function () {});
            }
          }
        });
      }
    }]);

    return TrimmingLoaderPlugin;
  }(paella.EventDrivenPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin3) {
    _inherits(AirPlayPlugin, _paella$ButtonPlugin3);

    var _super62 = _createSuper(AirPlayPlugin);

    function AirPlayPlugin() {
      _classCallCheck(this, AirPlayPlugin);

      return _super62.apply(this, arguments);
    }

    _createClass(AirPlayPlugin, [{
      key: "getIndex",
      value: function getIndex() {
        return 552;
      }
    }, {
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "AirPlayButton";
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-airplay';
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.airPlayPlugin";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        this._visible = false; // PIP is only available with single stream videos

        if (paella.player.videoContainer.streamProvider.videoStreams.length != 1) {
          onSuccess(false);
        } else {
          onSuccess(window.WebKitPlaybackTargetAvailabilityEvent);
        }
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Emit to AirPlay.");
      }
    }, {
      key: "setup",
      value: function setup() {
        var _this140 = this;

        var video = paella.player.videoContainer.masterVideo().video;

        if (window.WebKitPlaybackTargetAvailabilityEvent) {
          video.addEventListener('webkitplaybacktargetavailabilitychanged', function (event) {
            switch (event.availability) {
              case "available":
                _this140._visible = true;
                break;

              case "not-available":
                _this140._visible = false;
                break;
            }

            _this140.updateClassName();
          });
        }
      }
    }, {
      key: "action",
      value: function action(button) {
        var video = paella.player.videoContainer.masterVideo().video;
        video.webkitShowPlaybackTargetPicker();
      }
    }, {
      key: "updateClassName",
      value: function updateClassName() {
        this.button.className = this.getButtonItemClass(true);
      }
    }, {
      key: "getButtonItemClass",
      value: function getButtonItemClass(selected) {
        return 'buttonPlugin ' + this.getSubclass() + " " + this.getAlignment() + " " + (this._visible ? "available" : "not-available");
      }
    }]);

    return AirPlayPlugin;
  }(paella.ButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$EventDrivenPl6) {
    _inherits(ArrowSlidesNavigator, _paella$EventDrivenPl6);

    var _super63 = _createSuper(ArrowSlidesNavigator);

    function ArrowSlidesNavigator() {
      _classCallCheck(this, ArrowSlidesNavigator);

      return _super63.apply(this, arguments);
    }

    _createClass(ArrowSlidesNavigator, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.arrowSlidesNavigatorPlugin";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        if (!paella.initDelegate.initParams.videoLoader.frameList || Object.keys(paella.initDelegate.initParams.videoLoader.frameList).length == 0 || paella.player.videoContainer.isMonostream) {
          onSuccess(false);
        } else {
          onSuccess(true);
        }
      }
    }, {
      key: "setup",
      value: function setup() {
        var self = this;
        this._showArrowsIn = this.config.showArrowsIn || 'slave';
        this.createOverlay();
        self._frames = [];
        var frames = paella.initDelegate.initParams.videoLoader.frameList;
        var numFrames;

        if (frames) {
          var framesKeys = Object.keys(frames);
          numFrames = framesKeys.length;
          framesKeys.map(function (i) {
            return Number(i, 10);
          }).sort(function (a, b) {
            return a - b;
          }).forEach(function (key) {
            self._frames.push(frames[key]);
          });
        }
      }
    }, {
      key: "createOverlay",
      value: function createOverlay() {
        var self = this;
        var overlayContainer = paella.player.videoContainer.overlayContainer;

        if (!this.arrows) {
          this.arrows = document.createElement('div');
          this.arrows.id = "arrows";
          this.arrows.style.marginTop = "25%";
          var arrowNext = document.createElement('div');
          arrowNext.className = "buttonPlugin arrowSlideNavidator nextButton right icon-next2";
          this.arrows.appendChild(arrowNext);
          var arrowPrev = document.createElement('div');
          arrowPrev.className = "buttonPlugin arrowSlideNavidator prevButton left icon-previous2";
          this.arrows.appendChild(arrowPrev);
          $(arrowNext).click(function (e) {
            self.goNextSlide();
            e.stopPropagation();
          });
          $(arrowPrev).click(function (e) {
            self.goPrevSlide();
            e.stopPropagation();
          });
        }

        if (this.container) {
          overlayContainer.removeElement(this.container);
        }

        var rect = null;
        var element = null;

        if (!paella.profiles.currentProfile) {
          return null;
        }

        this.config.content = this.config.content || ["presentation"];
        var profilesContent = [];
        paella.profiles.currentProfile.videos.forEach(function (profileData) {
          profilesContent.push(profileData.content);
        }); // Default content, if the "content" setting is not set in the configuration file

        var selectedContent = profilesContent.length == 1 ? profilesContent[0] : profilesContent.length > 1 ? profilesContent[1] : "";
        this.config.content.some(function (preferredContent) {
          if (profilesContent.indexOf(preferredContent) != -1) {
            selectedContent = preferredContent;
            return true;
          }
        });

        if (!selectedContent) {
          this.container = overlayContainer.addLayer();
          this.container.style.marginRight = "0";
          this.container.style.marginLeft = "0";
          this.arrows.style.marginTop = "25%";
        } else {
          var videoIndex = 0;
          paella.player.videoContainer.streamProvider.streams.forEach(function (stream, index) {
            if (stream.type == "video" && selectedContent == stream.content) {
              videoIndex = index;
            }
          });
          element = document.createElement('div');
          rect = overlayContainer.getVideoRect(videoIndex); // content

          this.container = overlayContainer.addElement(element, rect);
          this.visible = rect.visible;
          this.arrows.style.marginTop = "33%";
        }

        this.container.appendChild(this.arrows);
        this.hideArrows();
      }
    }, {
      key: "getCurrentRange",
      value: function getCurrentRange() {
        var _this141 = this;

        return new Promise(function (resolve) {
          if (_this141._frames.length < 1) {
            resolve(null);
          } else {
            var trimming = null;
            var duration = 0;
            paella.player.videoContainer.duration().then(function (d) {
              duration = d;
              return paella.player.videoContainer.trimming();
            }).then(function (t) {
              trimming = t;
              return paella.player.videoContainer.currentTime();
            }).then(function (currentTime) {
              if (!_this141._frames.some(function (f1, i, array) {
                if (i + 1 == array.length) {
                  return;
                }

                var f0 = i == 0 ? f1 : _this141._frames[i - 1];
                var f2 = _this141._frames[i + 1];
                var t0 = trimming.enabled ? f0.time - trimming.start : f0.time;
                var t1 = trimming.enabled ? f1.time - trimming.start : f1.time;
                var t2 = trimming.enabled ? f2.time - trimming.start : f2.time;

                if (t1 < currentTime && t2 > currentTime || t1 == currentTime) {
                  var range = {
                    prev: t0,
                    next: t2
                  };

                  if (t0 < 0) {
                    range.prev = t1 > 0 ? t1 : 0;
                  }

                  resolve(range);
                  return true;
                }
              })) {
                var t0 = _this141._frames[_this141._frames.length - 2].time;
                var t1 = _this141._frames[_this141._frames.length - 1].time;
                resolve({
                  prev: trimming.enabled ? t0 - trimming.start : t0,
                  next: trimming.enabled ? t1 - trimming.start : t1
                });
              }
            });
          }
        });
      }
    }, {
      key: "goNextSlide",
      value: function goNextSlide() {
        var self = this;
        var trimming;
        this.getCurrentRange().then(function (range) {
          return paella.player.videoContainer.seekToTime(range.next);
        }).then(function () {
          paella.player.videoContainer.play();
        });
      }
    }, {
      key: "goPrevSlide",
      value: function goPrevSlide() {
        var self = this;
        var trimming = null;
        this.getCurrentRange().then(function (range) {
          return paella.player.videoContainer.seekToTime(range.prev);
        }).then(function () {
          paella.player.videoContainer.play();
        });
      }
    }, {
      key: "showArrows",
      value: function showArrows() {
        if (this.visible) $(this.arrows).show();
      }
    }, {
      key: "hideArrows",
      value: function hideArrows() {
        $(this.arrows).hide();
      }
    }, {
      key: "getEvents",
      value: function getEvents() {
        return [paella.events.controlBarDidShow, paella.events.controlBarDidHide, paella.events.setComposition];
      }
    }, {
      key: "onEvent",
      value: function onEvent(eventType, params) {
        var self = this;

        switch (eventType) {
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
    }]);

    return ArrowSlidesNavigator;
  }(paella.EventDrivenPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin4) {
    _inherits(AudioSelector, _paella$ButtonPlugin4);

    var _super64 = _createSuper(AudioSelector);

    function AudioSelector() {
      _classCallCheck(this, AudioSelector);

      return _super64.apply(this, arguments);
    }

    _createClass(AudioSelector, [{
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "audioSelector";
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-headphone';
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 2040;
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.audioSelector";
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Set audio stream");
      }
    }, {
      key: "closeOnMouseOut",
      value: function closeOnMouseOut() {
        return true;
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        var _this142 = this;

        this._mainPlayer = paella.player.videoContainer.streamProvider.mainVideoPlayer;

        this._mainPlayer.supportsMultiaudio().then(function (supports) {
          if (supports) {
            _this142._legacyMode = false;
            return _this142._mainPlayer.getAudioTracks();
          } else {
            _this142._legacyMode = true;
            return paella.player.videoContainer.getAudioTags();
          }
        }).then(function (audioTracks) {
          if (_this142._legacyMode) {
            _this142._tags = audioTracks;
            return Promise.resolve();
          } else {
            _this142._audioTracks = audioTracks;
            return _this142._mainPlayer.getCurrentAudioTrack();
          }
        }).then(function (defaultAudioTrack) {
          if (_this142._legacyMode) {
            onSuccess(_this142._tags.length > 1);
          } else {
            _this142._defaultAudioTrack = defaultAudioTrack;
            onSuccess(true);
          }
        });
      }
    }, {
      key: "getButtonType",
      value: function getButtonType() {
        return paella.ButtonPlugin.type.menuButton;
      }
    }, {
      key: "getMenuContent",
      value: function getMenuContent() {
        var _this143 = this;

        var buttonItems = [];

        if (this._legacyMode) {
          this._tags.forEach(function (tag, index) {
            buttonItems.push({
              id: index,
              title: tag,
              value: tag,
              icon: "",
              className: _this143.getButtonItemClass(tag),
              "default": tag == paella.player.videoContainer.audioTag
            });
          });
        } else {
          this._audioTracks.forEach(function (track) {
            buttonItems.push({
              id: track.id,
              title: track.groupId + " " + track.name,
              value: track.id,
              icon: "",
              className: _this143.getButtonItemClass(track.id),
              "default": track.id == _this143._defaultAudioTrack.id
            });
          });
        }

        return buttonItems;
      }
    }, {
      key: "menuItemSelected",
      value: function menuItemSelected(itemData) {
        if (this._legacyMode) {
          paella.player.videoContainer.setAudioTag(itemData.value);
        } else {
          this._mainPlayer.setCurrentAudioTrack(itemData.id);
        }

        paella.player.controls.hidePopUp(this.getName());
      }
    }, {
      key: "setQualityLabel",
      value: function setQualityLabel() {
        if (this._legacyMode) {
          var This = this;
          paella.player.videoContainer.getCurrentQuality().then(function (q) {
            This.setText(q.shortLabel());
          });
        } else {}
      }
    }, {
      key: "getButtonItemClass",
      value: function getButtonItemClass(tag) {
        return 'videoAudioTrackItem ' + tag;
      }
    }]);

    return AudioSelector;
  }(paella.ButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$EventDrivenPl7) {
    _inherits(BlackBoard2, _paella$EventDrivenPl7);

    var _super65 = _createSuper(BlackBoard2);

    function BlackBoard2() {
      _classCallCheck(this, BlackBoard2);

      return _super65.apply(this, arguments);
    }

    _createClass(BlackBoard2, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.blackBoardPlugin";
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 10;
      }
    }, {
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "blackBoardButton2";
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("BlackBoard");
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
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
    }, {
      key: "getEvents",
      value: function getEvents() {
        return [paella.events.setProfile, paella.events.timeUpdate];
      }
    }, {
      key: "onEvent",
      value: function onEvent(event, params) {
        var self = this;

        switch (event) {
          case paella.events.setProfile:
            if (params.profileName != self._blackBoardProfile) {
              if (self._active) {
                self.destroyOverlay();
                self._active = false;
              }

              break;
            } else {
              if (!self._hasImages) {
                paella.player.setProfile("slide_professor");
              }

              if (self._hasImages && !self._active) {
                self.createOverlay();
                self._active = true;
              }
            }

            break;

          case paella.events.timeUpdate:
            if (self._active && self._hasImages) {
              paella.player.videoContainer.trimming().then(function (trimmingData) {
                if (trimmingData.enabled) {
                  params.currentTime += trimmingData.start;
                }

                self.imageUpdate(event, params);
              });
            }

            break;
        }
      }
    }, {
      key: "setup",
      value: function setup() {
        var self = this;
        var n = paella.player.videoContainer.sourceData[0].sources;

        if (n.hasOwnProperty("image")) {
          self._hasImages = true; //  BRING THE IMAGE ARRAY TO LOCAL

          self._zImages = {};
          self._zImages = paella.player.videoContainer.sourceData[0].sources.image[0].frames; // COPY TO LOCAL

          self._videoLength = paella.player.videoContainer.sourceData[0].sources.image[0].duration; // video duration in frames
          // SORT KEYS FOR SEARCH CLOSEST

          self._keys = Object.keys(self._zImages);
          self._keys = self._keys.sort(function (a, b) {
            a = a.slice(6);
            b = b.slice(6);
            return parseInt(a) - parseInt(b);
          });
        } else {
          self._hasImages = false;

          if (paella.player.selectedProfile == self._blackBoardProfile) {
            var defaultprofile = paella.player.config.defaultProfile;
            paella.player.setProfile(defaultprofile);
          }
        } //NEXT


        this._next = 0;
        this._prev = 0;

        if (paella.player.selectedProfile == self._blackBoardProfile) {
          self.createOverlay();
          self._active = true;
        }

        self._mousePos = {};
        paella.Profiles.loadProfile(self._blackBoardProfile, function (profileData) {
          self._containerRect = profileData.blackBoardImages;
        });
      }
    }, {
      key: "createLens",
      value: function createLens() {
        var self = this;

        if (self._currentZoom == null) {
          self._currentZoom = self._zoom;
        }

        var lens = document.createElement("div");
        lens.className = "lensClass";
        self._lensDIV = lens;
        var p = $('.conImg').offset();
        var width = $('.conImg').width();
        var height = $('.conImg').height();
        lens.style.width = width / (self._currentZoom / 100) + "px";
        lens.style.height = height / (self._currentZoom / 100) + "px";
        self._lensWidth = parseInt(lens.style.width);
        self._lensHeight = parseInt(lens.style.height);
        $(self._lensContainer).append(lens);
        $(self._lensContainer).mousemove(function (event) {
          var mouseX = event.pageX - p.left;
          var mouseY = event.pageY - p.top;
          self._mousePos.x = mouseX;
          self._mousePos.y = mouseY;
          var lensTop = mouseY - self._lensHeight / 2;
          lensTop = lensTop < 0 ? 0 : lensTop;
          lensTop = lensTop > height - self._lensHeight ? height - self._lensHeight : lensTop;
          var lensLeft = mouseX - self._lensWidth / 2;
          lensLeft = lensLeft < 0 ? 0 : lensLeft;
          lensLeft = lensLeft > width - self._lensWidth ? width - self._lensWidth : lensLeft;
          self._lensDIV.style.left = lensLeft + "px";
          self._lensDIV.style.top = lensTop + "px";

          if (self._currentZoom != 100) {
            var x = lensLeft * 100 / (width - self._lensWidth);
            var y = lensTop * 100 / (height - self._lensHeight);
            self._blackBoardDIV.style.backgroundPosition = x.toString() + '% ' + y.toString() + '%';
          } else if (self._currentZoom == 100) {
            var xRelative = mouseX * 100 / width;
            var yRelative = mouseY * 100 / height;
            self._blackBoardDIV.style.backgroundPosition = xRelative.toString() + '% ' + yRelative.toString() + '%';
          }

          self._blackBoardDIV.style.backgroundSize = self._currentZoom + '%';
        });
        $(self._lensContainer).bind('wheel mousewheel', function (e) {
          var delta;

          if (e.originalEvent.wheelDelta !== undefined) {
            delta = e.originalEvent.wheelDelta;
          } else {
            delta = e.originalEvent.deltaY * -1;
          }

          if (delta > 0 && self._currentZoom < self._maxZoom) {
            self.reBuildLens(10);
          } else if (self._currentZoom > 100) {
            self.reBuildLens(-10);
          } else if (self._currentZoom == 100) {
            self._lensDIV.style.left = 0 + "px";
            self._lensDIV.style.top = 0 + "px";
          }

          self._blackBoardDIV.style.backgroundSize = self._currentZoom + "%";
        });
      }
    }, {
      key: "reBuildLens",
      value: function reBuildLens(zoomValue) {
        var self = this;
        self._currentZoom += zoomValue;
        var p = $('.conImg').offset();
        var width = $('.conImg').width();
        var height = $('.conImg').height();
        self._lensDIV.style.width = width / (self._currentZoom / 100) + "px";
        self._lensDIV.style.height = height / (self._currentZoom / 100) + "px";
        self._lensWidth = parseInt(self._lensDIV.style.width);
        self._lensHeight = parseInt(self._lensDIV.style.height);

        if (self._currentZoom != 100) {
          var mouseX = self._mousePos.x;
          var mouseY = self._mousePos.y;
          var lensTop = mouseY - self._lensHeight / 2;
          lensTop = lensTop < 0 ? 0 : lensTop;
          lensTop = lensTop > height - self._lensHeight ? height - self._lensHeight : lensTop;
          var lensLeft = mouseX - self._lensWidth / 2;
          lensLeft = lensLeft < 0 ? 0 : lensLeft;
          lensLeft = lensLeft > width - self._lensWidth ? width - self._lensWidth : lensLeft;
          self._lensDIV.style.left = lensLeft + "px";
          self._lensDIV.style.top = lensTop + "px";
          var x = lensLeft * 100 / (width - self._lensWidth);
          var y = lensTop * 100 / (height - self._lensHeight);
          self._blackBoardDIV.style.backgroundPosition = x.toString() + '% ' + y.toString() + '%';
        }
      }
    }, {
      key: "destroyLens",
      value: function destroyLens() {
        var self = this;

        if (self._lensDIV) {
          $(self._lensDIV).remove();
          self._blackBoardDIV.style.backgroundSize = 100 + '%';
          self._blackBoardDIV.style.opacity = 0;
        } //self._currentZoom = self._zoom;

      }
    }, {
      key: "createOverlay",
      value: function createOverlay() {
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
        $(self._lensContainer).mouseenter(function () {
          self.createLens();
          self._blackBoardDIV.style.opacity = 1.0;
        });
        $(self._lensContainer).mouseleave(function () {
          self.destroyLens();
        });
        setTimeout(function () {
          // TIMER FOR NICE VIEW
          var overlayContainer = paella.player.videoContainer.overlayContainer;
          overlayContainer.addElement(blackBoardDiv, overlayContainer.getVideoRect(0));
          overlayContainer.addElement(lensContainer, self._containerRect);
        }, self._creationTimer);
      }
    }, {
      key: "destroyOverlay",
      value: function destroyOverlay() {
        var self = this;

        if (self._blackBoardDIV) {
          $(self._blackBoardDIV).remove();
        }

        if (self._lensContainer) {
          $(self._lensContainer).remove();
        }
      }
    }, {
      key: "imageUpdate",
      value: function imageUpdate(event, params) {
        var self = this;
        var sec = Math.round(params.currentTime);
        var src = $(self._blackBoardDIV).css('background-image');

        if ($(self._blackBoardDIV).length > 0) {
          if (self._zImages.hasOwnProperty("frame_" + sec)) {
            // SWAP IMAGES WHEN PLAYING
            if (src == self._zImages["frame_" + sec]) {
              return;
            } else {
              src = self._zImages["frame_" + sec];
            }
          } else if (sec > self._next || sec < self._prev) {
            src = self.returnSrc(sec);
          } // RELOAD IF OUT OF INTERVAL
          else {
              return;
            } //PRELOAD NEXT IMAGE


          var image = new Image();

          image.onload = function () {
            $(self._blackBoardDIV).css('background-image', 'url(' + src + ')'); // UPDATING IMAGE
          };

          image.src = src;
          self._currentImage = src;
          self._conImg.src = self._currentImage;
        }
      }
    }, {
      key: "returnSrc",
      value: function returnSrc(sec) {
        var prev = 0;

        for (var i = 0; i < this._keys.length; i++) {
          var id = parseInt(this._keys[i].slice(6));
          var lastId = parseInt(this._keys[this._keys.length - 1].slice(6));

          if (sec < id) {
            // PREVIOUS IMAGE
            this._next = id;
            this._prev = prev;
            this._imageNumber = i - 1;
            return this._zImages["frame_" + prev]; // return previous and keep next change
          } else if (sec > lastId && sec < this._videoLength) {
            // LAST INTERVAL
            this._next = this._videoLength;
            this._prev = lastId;
            return this._zImages["frame_" + prev];
          } else {
            prev = id;
          }
        }
      }
    }]);

    return BlackBoard2;
  }(paella.EventDrivenPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$EventDrivenPl8) {
    _inherits(BreaksPlayerPlugin, _paella$EventDrivenPl8);

    var _super66 = _createSuper(BreaksPlayerPlugin);

    function BreaksPlayerPlugin() {
      _classCallCheck(this, BreaksPlayerPlugin);

      return _super66.apply(this, arguments);
    }

    _createClass(BreaksPlayerPlugin, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.breaksPlayerPlugin";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        onSuccess(true);
      }
    }, {
      key: "setup",
      value: function setup() {
        var _this144 = this;

        this.breaks = [];
        this.status = false;
        this.lastTime = 0;
        paella.data.read('breaks', {
          id: paella.player.videoIdentifier
        }, function (data) {
          if (data && _typeof(data) == 'object' && data.breaks && data.breaks.length > 0) {
            _this144.breaks = data.breaks;
          }
        });
      }
    }, {
      key: "getEvents",
      value: function getEvents() {
        return [paella.events.timeUpdate];
      }
    }, {
      key: "onEvent",
      value: function onEvent(eventType, params) {
        var _this145 = this;

        paella.player.videoContainer.currentTime(true).then(function (currentTime) {
          // The event type checking must to be done using the time difference, because
          // the timeUpdate event may arrive before the seekToTime event
          var diff = Math.abs(currentTime - _this145.lastTime);

          _this145.checkBreaks(currentTime, diff >= 1 ? paella.events.seekToTime : paella.events.timeUpdate);

          _this145.lastTime = currentTime;
        });
      }
    }, {
      key: "checkBreaks",
      value: function checkBreaks(currentTime, eventType) {
        var _this146 = this;

        var breakMessage = "";

        if (this.breaks.some(function (breakItem) {
          if (breakItem.s <= currentTime && breakItem.e >= currentTime) {
            if (eventType == paella.events.timeUpdate && !_this146.status) {
              _this146.skipTo(breakItem.e);
            }

            breakMessage = breakItem.text;
            return true;
          }
        })) {
          this.showMessage(breakMessage);
          this.status = true;
        } else {
          this.hideMessage();
          this.status = false;
        }
      }
    }, {
      key: "skipTo",
      value: function skipTo(time) {
        paella.player.videoContainer.trimming().then(function (trimming) {
          if (trimming.enabled) {
            paella.player.videoContainer.seekToTime(time - trimming.start);
          } else {
            paella.player.videoContainer.seekToTime(time);
          }
        });
      }
    }, {
      key: "showMessage",
      value: function showMessage(text) {
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
    }, {
      key: "hideMessage",
      value: function hideMessage() {
        if (this.messageContainer) {
          paella.player.videoContainer.overlayContainer.removeElement(this.messageContainer);
          this.messageContainer = null;
        }

        this.currentText = "";
      }
    }]);

    return BreaksPlayerPlugin;
  }(paella.EventDrivenPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$PlaybackCanva) {
    _inherits(BufferedPlaybackCanvasPlugin, _paella$PlaybackCanva);

    var _super67 = _createSuper(BufferedPlaybackCanvasPlugin);

    function BufferedPlaybackCanvasPlugin() {
      _classCallCheck(this, BufferedPlaybackCanvasPlugin);

      return _super67.apply(this, arguments);
    }

    _createClass(BufferedPlaybackCanvasPlugin, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.BufferedPlaybackCanvasPlugin";
      }
    }, {
      key: "setup",
      value: function setup() {}
    }, {
      key: "drawCanvas",
      value: function drawCanvas(context, width, height, videoData) {
        function trimmedInstant(t) {
          t = videoData.trimming.enabled ? t - videoData.trimming.start : t;
          return t * width / videoData.trimming.duration;
        }

        var buffered = paella.player.videoContainer.streamProvider.buffered;

        for (var i = 0; i < buffered.length; ++i) {
          var start = trimmedInstant(buffered.start(i));
          var end = trimmedInstant(buffered.end(i));
          this.drawBuffer(context, start, end, height);
        }
      }
    }, {
      key: "drawBuffer",
      value: function drawBuffer(context, start, end, height) {
        context[0].fillStyle = this.config.color;
        context[0].fillRect(start, 0, end, height);
      }
    }]);

    return BufferedPlaybackCanvasPlugin;
  }(paella.PlaybackCanvasPlugin);
});
paella.addPlugin(function () {
  /////////////////////////////////////////////////
  // DFXP Parser
  /////////////////////////////////////////////////
  return /*#__PURE__*/function (_paella$CaptionParser2) {
    _inherits(DFXPParserPlugin, _paella$CaptionParser2);

    var _super68 = _createSuper(DFXPParserPlugin);

    function DFXPParserPlugin() {
      _classCallCheck(this, DFXPParserPlugin);

      return _super68.apply(this, arguments);
    }

    _createClass(DFXPParserPlugin, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.captions.DFXPParserPlugin";
      }
    }, {
      key: "parse",
      value: function parse(content, lang, next) {
        var captions = [];
        var self = this;
        var xmlDoc = $.parseXML(content);
        var xml = $(xmlDoc);
        var g_lang = xml.attr("xml:lang");
        var lls = xml.find("div");

        for (var idx = 0; idx < lls.length; ++idx) {
          var ll = $(lls[idx]);
          var l_lang = ll.attr("xml:lang");

          if (l_lang == undefined || l_lang == "") {
            if (g_lang == undefined || g_lang == "") {
              paella.log.debug("No xml:lang found! Using '" + lang + "' lang instead.");
              l_lang = lang;
            } else {
              l_lang = g_lang;
            }
          } //


          if (l_lang == lang) {
            ll.find("p").each(function (i, cap) {
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
        } else {
          next(true);
        }
      }
    }, {
      key: "parseTimeTextToSeg",
      value: function parseTimeTextToSeg(ttime) {
        var nseg = 0;
        var segtime = /^([0-9]*([.,][0-9]*)?)s/.test(ttime);

        if (segtime) {
          nseg = parseFloat(RegExp.$1);
        } else {
          var split = ttime.split(":");
          var h = parseInt(split[0]);
          var m = parseInt(split[1]);
          var s = parseInt(split[2]);
          nseg = s + m * 60 + h * 60 * 60;
        }

        return nseg;
      }
    }, {
      key: "ext",
      get: function get() {
        return ["dfxp"];
      }
    }]);

    return DFXPParserPlugin;
  }(paella.CaptionParserPlugIn);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin5) {
    _inherits(CaptionsPlugin, _paella$ButtonPlugin5);

    var _super69 = _createSuper(CaptionsPlugin);

    function CaptionsPlugin() {
      _classCallCheck(this, CaptionsPlugin);

      return _super69.apply(this, arguments);
    }

    _createClass(CaptionsPlugin, [{
      key: "getInstanceName",
      value: function getInstanceName() {
        return "captionsPlugin";
      } // plugin instance will be available in paella.plugins.captionsPlugin

    }, {
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return 'captionsPluginButton';
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-captions';
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.captionsPlugin";
      }
    }, {
      key: "getButtonType",
      value: function getButtonType() {
        return paella.ButtonPlugin.type.popUpButton;
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Subtitles");
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 509;
      }
    }, {
      key: "closeOnMouseOut",
      value: function closeOnMouseOut() {
        return false;
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
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
    }, {
      key: "showUI",
      value: function showUI() {
        if (paella.captions.getAvailableLangs().length >= 1) {
          _get(_getPrototypeOf(CaptionsPlugin.prototype), "showUI", this).call(this);
        }
      }
    }, {
      key: "setup",
      value: function setup() {
        var self = this; // HIDE UI IF NO Captions

        if (!paella.captions.getAvailableLangs().length) {
          paella.plugins.captionsPlugin.hideUI();
        } //BINDS


        paella.events.bind(paella.events.captionsEnabled, function (event, params) {
          self.onChangeSelection(params);
        });
        paella.events.bind(paella.events.captionsDisabled, function (event, params) {
          self.onChangeSelection(params);
        });
        paella.events.bind(paella.events.captionAdded, function (event, params) {
          self.onCaptionAdded(params);
          paella.plugins.captionsPlugin.showUI();
        });
        paella.events.bind(paella.events.timeUpdate, function (event, params) {
          if (self._searchOnCaptions) {
            self.updateCaptionHiglighted(params);
          }
        });
        paella.events.bind(paella.events.controlBarWillHide, function (evt) {
          self.cancelHideBar();
        });
        self._activeCaptions = paella.captions.getActiveCaptions();
        self._searchOnCaptions = self.config.searchOnCaptions || false;
      }
    }, {
      key: "cancelHideBar",
      value: function cancelHideBar() {
        var thisClass = this;

        if (thisClass._open > 0) {
          paella.player.controls.cancelHideBar();
        }
      }
    }, {
      key: "updateCaptionHiglighted",
      value: function updateCaptionHiglighted(time) {
        var thisClass = this;
        var sel = null;
        var id = null;

        if (time) {
          paella.player.videoContainer.trimming().then(function (trimming) {
            var offset = trimming.enabled ? trimming.start : 0;
            var c = paella.captions.getActiveCaptions();
            var caption = c && c.getCaptionAtTime(time.currentTime + offset);
            var id = caption && caption.id;

            if (id != null) {
              sel = $(".bodyInnerContainer[sec-id='" + id + "']");

              if (sel != thisClass._lasSel) {
                $(thisClass._lasSel).removeClass("Highlight");
              }

              if (sel) {
                $(sel).addClass("Highlight");

                if (thisClass._autoScroll) {
                  thisClass.updateScrollFocus(id);
                }

                thisClass._lasSel = sel;
              }
            }
          });
        }
      }
    }, {
      key: "updateScrollFocus",
      value: function updateScrollFocus(id) {
        var thisClass = this;
        var resul = 0;
        var t = $(".bodyInnerContainer").slice(0, id);
        t = t.toArray();
        t.forEach(function (l) {
          var i = $(l).outerHeight(true);
          resul += i;
        });
        var x = parseInt(resul / 280);
        $(".captionsBody").scrollTop(x * thisClass._defaultBodyHeight);
      }
    }, {
      key: "onCaptionAdded",
      value: function onCaptionAdded(obj) {
        var thisClass = this;
        var newCap = paella.captions.getCaptions(obj);
        var defOption = document.createElement("option"); // NO ONE SELECT

        defOption.text = newCap._lang.txt;
        defOption.value = obj;

        thisClass._select.add(defOption);
      }
    }, {
      key: "changeSelection",
      value: function changeSelection() {
        var thisClass = this;
        var sel = $(thisClass._select).val();

        if (sel == "") {
          $(thisClass._body).empty();
          paella.captions.setActiveCaptions(sel);
          return;
        } // BREAK IF NO ONE SELECTED


        paella.captions.setActiveCaptions(sel);
        thisClass._activeCaptions = sel;

        if (thisClass._searchOnCaptions) {
          thisClass.buildBodyContent(paella.captions.getActiveCaptions()._captions, "list");
        }

        thisClass.setButtonHideShow();
      }
    }, {
      key: "onChangeSelection",
      value: function onChangeSelection(obj) {
        var thisClass = this;

        if (thisClass._activeCaptions != obj) {
          $(thisClass._body).empty();

          if (obj == undefined) {
            thisClass._select.value = "";
            $(thisClass._input).prop('disabled', true);
          } else {
            $(thisClass._input).prop('disabled', false);
            thisClass._select.value = obj;

            if (thisClass._searchOnCaptions) {
              thisClass.buildBodyContent(paella.captions.getActiveCaptions()._captions, "list");
            }
          }

          thisClass._activeCaptions = obj;
          thisClass.setButtonHideShow();
        }
      }
    }, {
      key: "action",
      value: function action() {
        var self = this;
        self._browserLang = paella.utils.dictionary.currentLanguage();
        self._autoScroll = true;

        switch (self._open) {
          case 0:
            if (self._browserLang && paella.captions.getActiveCaptions() == undefined) {
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
    }, {
      key: "buildContent",
      value: function buildContent(domElement) {
        var thisClass = this; //captions CONTAINER

        thisClass._parent = document.createElement('div');
        thisClass._parent.className = 'captionsPluginContainer'; //captions BAR

        thisClass._bar = document.createElement('div');
        thisClass._bar.className = 'captionsBar'; //captions BODY

        if (thisClass._searchOnCaptions) {
          thisClass._body = document.createElement('div');
          thisClass._body.className = 'captionsBody';

          thisClass._parent.appendChild(thisClass._body); //BODY JQUERY


          $(thisClass._body).scroll(function () {
            thisClass._autoScroll = false;
          }); //INPUT

          thisClass._input = document.createElement("input");
          thisClass._input.className = "captionsBarInput";
          thisClass._input.type = "text";
          thisClass._input.id = "captionsBarInput";
          thisClass._input.name = "captionsString";
          thisClass._input.placeholder = paella.utils.dictionary.translate("Search captions");

          thisClass._bar.appendChild(thisClass._input); //INPUT jQuery


          $(thisClass._input).change(function () {
            var text = $(thisClass._input).val();
            thisClass.doSearch(text);
          });
          $(thisClass._input).keyup(function () {
            var text = $(thisClass._input).val();

            if (thisClass._searchTimer != null) {
              thisClass._searchTimer.cancel();
            }

            thisClass._searchTimer = new paella.utils.Timer(function (timer) {
              thisClass.doSearch(text);
            }, thisClass._searchTimerTime);
          });
        } //SELECT


        thisClass._select = document.createElement("select");
        thisClass._select.className = "captionsSelector";
        var defOption = document.createElement("option"); // NO ONE SELECT

        defOption.text = paella.utils.dictionary.translate("None");
        defOption.value = "";

        thisClass._select.add(defOption);

        paella.captions.getAvailableLangs().forEach(function (l) {
          var option = document.createElement("option");
          option.text = l.lang.txt;
          option.value = l.id;

          thisClass._select.add(option);
        });

        thisClass._bar.appendChild(thisClass._select);

        thisClass._parent.appendChild(thisClass._bar); //jQuery SELECT


        $(thisClass._select).change(function () {
          thisClass.changeSelection();
        }); //BUTTON EDITOR

        thisClass._editor = document.createElement("button");
        thisClass._editor.className = "editorButton";
        thisClass._editor.innerText = "";

        thisClass._bar.appendChild(thisClass._editor); //BUTTON jQuery


        $(thisClass._editor).prop("disabled", true);
        $(thisClass._editor).click(function () {
          var c = paella.captions.getActiveCaptions();
          paella.userTracking.log("paella:caption:edit", {
            id: c._captionsProvider + ':' + c._id,
            lang: c._lang
          });
          c.goToEdit();
        });
        domElement.appendChild(thisClass._parent);
      }
    }, {
      key: "selectDefaultBrowserLang",
      value: function selectDefaultBrowserLang(code) {
        var thisClass = this;
        var provider = null;
        paella.captions.getAvailableLangs().forEach(function (l) {
          if (l.lang.code == code) {
            provider = l.id;
          }
        });

        if (provider) {
          paella.captions.setActiveCaptions(provider);
        }
        /*
        else{
        	$(thisClass._input).prop("disabled",true);
        }
        */

      }
    }, {
      key: "doSearch",
      value: function doSearch(text) {
        var thisClass = this;
        var c = paella.captions.getActiveCaptions();

        if (c) {
          if (text == "") {
            thisClass.buildBodyContent(paella.captions.getActiveCaptions()._captions, "list");
          } else {
            c.search(text, function (err, resul) {
              if (!err) {
                thisClass.buildBodyContent(resul, "search");
              }
            });
          }
        }
      }
    }, {
      key: "setButtonHideShow",
      value: function setButtonHideShow() {
        var thisClass = this;
        var editor = $('.editorButton');
        var c = paella.captions.getActiveCaptions();
        var res = null;

        if (c != null) {
          $(thisClass._select).width('39%');
          c.canEdit(function (err, r) {
            res = r;
          });

          if (res) {
            $(editor).prop("disabled", false);
            $(editor).show();
          } else {
            $(editor).prop("disabled", true);
            $(editor).hide();
            $(thisClass._select).width('47%');
          }
        } else {
          $(editor).prop("disabled", true);
          $(editor).hide();
          $(thisClass._select).width('47%');
        }

        if (!thisClass._searchOnCaptions) {
          if (res) {
            $(thisClass._select).width('92%');
          } else {
            $(thisClass._select).width('100%');
          }
        }
      }
    }, {
      key: "buildBodyContent",
      value: function buildBodyContent(obj, type) {
        var _this147 = this;

        paella.player.videoContainer.trimming().then(function (trimming) {
          var thisClass = _this147;
          $(thisClass._body).empty();
          obj.forEach(function (l) {
            if (trimming.enabled && (l.end < trimming.start || l.begin > trimming.end)) {
              return;
            }

            thisClass._inner = document.createElement('div');
            thisClass._inner.className = 'bodyInnerContainer';
            thisClass._inner.innerText = l.content;

            if (type == "list") {
              thisClass._inner.setAttribute('sec-begin', l.begin);

              thisClass._inner.setAttribute('sec-end', l.end);

              thisClass._inner.setAttribute('sec-id', l.id);

              thisClass._autoScroll = true;
            }

            if (type == "search") {
              thisClass._inner.setAttribute('sec-begin', l.time);
            }

            thisClass._body.appendChild(thisClass._inner); //JQUERY


            $(thisClass._inner).hover(function () {
              $(this).css('background-color', 'rgba(250, 161, 102, 0.5)');
            }, function () {
              $(this).removeAttr('style');
            });
            $(thisClass._inner).click(function () {
              var secBegin = $(this).attr("sec-begin");
              paella.player.videoContainer.trimming().then(function (trimming) {
                var offset = trimming.enabled ? trimming.start : 0;
                paella.player.videoContainer.seekToTime(secBegin - offset + 0.1);
              });
            });
          });
        });
      }
    }]);

    return CaptionsPlugin;
  }(paella.ButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$EventDrivenPl9) {
    _inherits(CaptionsOnScreen, _paella$EventDrivenPl9);

    var _super70 = _createSuper(CaptionsOnScreen);

    function CaptionsOnScreen() {
      _classCallCheck(this, CaptionsOnScreen);

      return _super70.apply(this, arguments);
    }

    _createClass(CaptionsOnScreen, [{
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
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
    }, {
      key: "setup",
      value: function setup() {}
    }, {
      key: "getEvents",
      value: function getEvents() {
        return [paella.events.controlBarDidHide, paella.events.resize, paella.events.controlBarDidShow, paella.events.captionsEnabled, paella.events.captionsDisabled, paella.events.timeUpdate];
      }
    }, {
      key: "onEvent",
      value: function onEvent(eventType, params) {
        var thisClass = this;

        switch (eventType) {
          case paella.events.controlBarDidHide:
            if (thisClass.lastEvent == eventType || thisClass.captions == false) break;
            thisClass.moveCaptionsOverlay("down");
            break;

          case paella.events.resize:
            if (thisClass.captions == false) break;

            if (paella.player.controls.isHidden()) {
              thisClass.moveCaptionsOverlay("down");
            } else {
              thisClass.moveCaptionsOverlay("top");
            }

            break;

          case paella.events.controlBarDidShow:
            if (thisClass.lastEvent == eventType || thisClass.captions == false) break;
            thisClass.moveCaptionsOverlay("top");
            break;

          case paella.events.captionsEnabled:
            thisClass.buildContent(params);
            thisClass.captions = true;

            if (paella.player.controls.isHidden()) {
              thisClass.moveCaptionsOverlay("down");
            } else {
              thisClass.moveCaptionsOverlay("top");
            }

            break;

          case paella.events.captionsDisabled:
            thisClass.hideContent();
            thisClass.captions = false;
            break;

          case paella.events.timeUpdate:
            if (thisClass.captions) {
              thisClass.updateCaptions(params);
            }

            break;
        }

        thisClass.lastEvent = eventType;
      }
    }, {
      key: "buildContent",
      value: function buildContent(provider) {
        var thisClass = this;
        thisClass.captionProvider = provider;

        if (thisClass.container == null) {
          // PARENT
          thisClass.container = document.createElement('div');
          thisClass.container.className = "CaptionsOnScreen";
          thisClass.container.id = thisClass.containerId;
          thisClass.innerContainer = document.createElement('div');
          thisClass.innerContainer.className = "CaptionsOnScreenInner";
          thisClass.container.appendChild(thisClass.innerContainer);
          if (thisClass.controlsPlayback == null) thisClass.controlsPlayback = $('#playerContainer_controls_playback');
          paella.player.videoContainer.domElement.appendChild(thisClass.container);
        } else {
          $(thisClass.container).show();
        }
      }
    }, {
      key: "updateCaptions",
      value: function updateCaptions(time) {
        var _this148 = this;

        if (this.captions) {
          paella.player.videoContainer.trimming().then(function (trimming) {
            var offset = trimming.enabled ? trimming.start : 0;
            var c = paella.captions.getActiveCaptions();
            var caption = c.getCaptionAtTime(time.currentTime + offset);

            if (caption) {
              $(_this148.container).show();
              _this148.innerContainer.innerText = caption.content;

              _this148.moveCaptionsOverlay("auto");
            } else {
              _this148.innerContainer.innerText = "";

              _this148.hideContent();
            }
          });
        }
      }
    }, {
      key: "hideContent",
      value: function hideContent() {
        var thisClass = this;
        $(thisClass.container).hide();
      }
    }, {
      key: "moveCaptionsOverlay",
      value: function moveCaptionsOverlay(pos) {
        var thisClass = this;
        var marginbottom = 10;
        if (thisClass.controlsPlayback == null) thisClass.controlsPlayback = $('#playerContainer_controls_playback');

        if (pos == "auto" || pos == undefined) {
          pos = paella.player.controls.isHidden() ? "down" : "top";
        }

        if (pos == "down") {
          var t = thisClass.container.offsetHeight;
          t -= thisClass.innerContainer.offsetHeight + marginbottom;
          thisClass.innerContainer.style.bottom = 0 - t + "px";
        }

        if (pos == "top") {
          var t2 = thisClass.controlsPlayback.offset().top;
          t2 -= thisClass.innerContainer.offsetHeight + marginbottom;
          thisClass.innerContainer.style.bottom = 0 - t2 + "px";
        }
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 1050;
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.overlayCaptionsPlugin";
      }
    }]);

    return CaptionsOnScreen;
  }(paella.EventDrivenPlugin);
});

(function () {
  function buildChromaVideoCanvas(stream, canvas) {
    var ChromaVideoCanvas = /*#__PURE__*/function (_bg$app$WindowControl3) {
      _inherits(ChromaVideoCanvas, _bg$app$WindowControl3);

      var _super71 = _createSuper(ChromaVideoCanvas);

      function ChromaVideoCanvas(stream) {
        var _this149;

        _classCallCheck(this, ChromaVideoCanvas);

        _this149 = _super71.call(this);
        _this149.stream = stream;
        _this149._chroma = bg.Color.White();
        _this149._crop = new bg.Vector4(0.3, 0.01, 0.3, 0.01);
        _this149._transform = bg.Matrix4.Identity().translate(0.6, -0.04, 0);
        _this149._bias = 0.01;
        return _this149;
      }

      _createClass(ChromaVideoCanvas, [{
        key: "loaded",
        value: function loaded() {
          var _this150 = this;

          return new Promise(function (resolve) {
            var checkLoaded = function checkLoaded() {
              if (_this150.video) {
                resolve(_this150);
              } else {
                setTimeout(checkLoaded, 100);
              }
            };

            checkLoaded();
          });
        }
      }, {
        key: "buildShape",
        value: function buildShape() {
          this.plist = new bg.base.PolyList(this.gl);
          this.plist.vertex = [-1, -1, 0, 1, -1, 0, 1, 1, 0, -1, 1, 0];
          this.plist.texCoord0 = [0, 0, 1, 0, 1, 1, 0, 1];
          this.plist.index = [0, 1, 2, 2, 3, 0];
          this.plist.build();
        }
      }, {
        key: "buildShader",
        value: function buildShader() {
          var vshader = "\n\t\t\t\t\t\tattribute vec4 position;\n\t\t\t\t\t\tattribute vec2 texCoord;\n\t\t\t\t\t\tuniform mat4 inTransform;\n\t\t\t\t\t\tvarying vec2 vTexCoord;\n\t\t\t\t\t\tvoid main() {\n\t\t\t\t\t\t\tgl_Position = inTransform * position;\n\t\t\t\t\t\t\tvTexCoord = texCoord;\n\t\t\t\t\t\t}\n\t\t\t\t\t";
          var fshader = "\n\t\t\t\t\t\tprecision mediump float;\n\t\t\t\t\t\tvarying vec2 vTexCoord;\n\t\t\t\t\t\tuniform sampler2D inTexture;\n\t\t\t\t\t\tuniform vec4 inChroma;\n\t\t\t\t\t\tuniform float inBias;\n\t\t\t\t\t\tuniform vec4 inCrop;\n\t\t\t\t\t\tvoid main() {\n\t\t\t\t\t\t\tvec4 result = texture2D(inTexture,vTexCoord);\n\t\t\t\t\t\t\t\n\t\t\t\t\t\t\tif ((result.r>=inChroma.r-inBias && result.r<=inChroma.r+inBias &&\n\t\t\t\t\t\t\t\tresult.g>=inChroma.g-inBias && result.g<=inChroma.g+inBias &&\n\t\t\t\t\t\t\t\tresult.b>=inChroma.b-inBias && result.b<=inChroma.b+inBias) ||\n\t\t\t\t\t\t\t\t(vTexCoord.x<inCrop.x || vTexCoord.x>inCrop.z || vTexCoord.y<inCrop.w || vTexCoord.y>inCrop.y)\n\t\t\t\t\t\t\t)\n\t\t\t\t\t\t\t{\n\t\t\t\t\t\t\t\tdiscard;\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\telse {\n\t\t\t\t\t\t\t\tgl_FragColor = result;\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t}\n\t\t\t\t\t";
          this.shader = new bg.base.Shader(this.gl);
          this.shader.addShaderSource(bg.base.ShaderType.VERTEX, vshader);
          this.shader.addShaderSource(bg.base.ShaderType.FRAGMENT, fshader);
          status = this.shader.link();

          if (!this.shader.status) {
            console.log(this.shader.compileError);
            console.log(this.shader.linkError);
          }

          this.shader.initVars(["position", "texCoord"], ["inTransform", "inTexture", "inChroma", "inBias", "inCrop"]);
        }
      }, {
        key: "init",
        value: function init() {
          var _this151 = this;

          // Use WebGL V1 engine
          bg.Engine.Set(new bg.webgl1.Engine(this.gl));
          bg.base.Loader.RegisterPlugin(new bg.base.VideoTextureLoaderPlugin());
          this.buildShape();
          this.buildShader();
          this.pipeline = new bg.base.Pipeline(this.gl);
          bg.base.Pipeline.SetCurrent(this.pipeline);
          this.pipeline.clearColor = bg.Color.Transparent();
          bg.base.Loader.Load(this.gl, this.stream.src).then(function (texture) {
            _this151.texture = texture;
          });
        }
      }, {
        key: "frame",
        value: function frame(delta) {
          if (this.texture) {
            this.texture.update();
          }
        }
      }, {
        key: "display",
        value: function display() {
          this.pipeline.clearBuffers(bg.base.ClearBuffers.COLOR | bg.base.ClearBuffers.DEPTH);

          if (this.texture) {
            this.shader.setActive();
            this.shader.setInputBuffer("position", this.plist.vertexBuffer, 3);
            this.shader.setInputBuffer("texCoord", this.plist.texCoord0Buffer, 2);
            this.shader.setMatrix4("inTransform", this.transform);
            this.shader.setTexture("inTexture", this.texture || bg.base.TextureCache.WhiteTexture(this.gl), bg.base.TextureUnit.TEXTURE_0);
            this.shader.setVector4("inChroma", this.chroma);
            this.shader.setValueFloat("inBias", this.bias);
            this.shader.setVector4("inCrop", new bg.Vector4(this.crop.x, 1.0 - this.crop.y, 1.0 - this.crop.z, this.crop.w));
            this.plist.draw();
            this.shader.disableInputBuffer("position");
            this.shader.disableInputBuffer("texCoord");
            this.shader.clearActive();
          }
        }
      }, {
        key: "reshape",
        value: function reshape(width, height) {
          var canvas = this.canvas.domElement;
          canvas.width = width;
          canvas.height = height;
          this.pipeline.viewport = new bg.Viewport(0, 0, width, height);
        }
      }, {
        key: "mouseMove",
        value: function mouseMove(evt) {
          this.postRedisplay();
        }
      }, {
        key: "chroma",
        get: function get() {
          return this._chroma;
        },
        set: function set(c) {
          this._chroma = c;
        }
      }, {
        key: "bias",
        get: function get() {
          return this._bias;
        },
        set: function set(b) {
          this._bias = b;
        }
      }, {
        key: "crop",
        get: function get() {
          return this._crop;
        },
        set: function set(c) {
          this._crop = c;
        }
      }, {
        key: "transform",
        get: function get() {
          return this._transform;
        },
        set: function set(t) {
          this._transform = t;
        }
      }, {
        key: "video",
        get: function get() {
          return this.texture ? this.texture.video : null;
        }
      }]);

      return ChromaVideoCanvas;
    }(bg.app.WindowController);

    var controller = new ChromaVideoCanvas(stream);
    var mainLoop = bg.app.MainLoop.singleton;
    mainLoop.updateMode = bg.app.FrameUpdate.AUTO;
    mainLoop.canvas = canvas;
    mainLoop.run(controller);
    return controller.loaded();
  }

  var ChromaVideo = /*#__PURE__*/function (_paella$VideoElementB4) {
    _inherits(ChromaVideo, _paella$VideoElementB4);

    var _super72 = _createSuper(ChromaVideo);

    function ChromaVideo(id, stream, left, top, width, height, streamName) {
      var _this152;

      _classCallCheck(this, ChromaVideo);

      _this152 = _super72.call(this, id, stream, 'canvas', left, top, width, height);
      _this152._posterFrame = null;
      _this152._currentQuality = null;
      _this152._autoplay = false;
      _this152._streamName = null;
      _this152._streamName = streamName || 'chroma';

      var This = _assertThisInitialized(_this152);

      if (_this152._stream.sources[_this152._streamName]) {
        _this152._stream.sources[_this152._streamName].sort(function (a, b) {
          return a.res.h - b.res.h;
        });
      }

      _this152.video = null;

      function onProgress(event) {
        if (!This._ready && This.video.readyState == 4) {
          This._ready = true;

          if (This._initialCurrentTipe !== undefined) {
            This.video.currentTime = This._initialCurrentTime;
            delete This._initialCurrentTime;
          }

          This._callReadyEvent();
        }
      }

      function evtCallback(event) {
        onProgress.apply(This, event);
      }

      function onUpdateSize() {
        if (This.canvasController) {
          var canvas = This.canvasController.canvas.domElement;
          This.canvasController.reshape($(canvas).width(), $(canvas).height());
        }
      }

      var timer = new paella.Timer(function (timer) {
        onUpdateSize();
      }, 500);
      timer.repeat = true;
      return _this152;
    }

    _createClass(ChromaVideo, [{
      key: "defaultProfile",
      value: function defaultProfile() {
        return 'chroma';
      }
    }, {
      key: "_setVideoElem",
      value: function _setVideoElem(video) {
        $(this.video).bind('progress', evtCallback);
        $(this.video).bind('loadstart', evtCallback);
        $(this.video).bind('loadedmetadata', evtCallback);
        $(this.video).bind('canplay', evtCallback);
        $(this.video).bind('oncanplay', evtCallback);
      }
    }, {
      key: "_loadDeps",
      value: function _loadDeps() {
        return new Promise(function (resolve, reject) {
          if (!window.$paella_bg2e) {
            paella.require(paella.baseUrl + 'javascript/bg2e-es2015.js').then(function () {
              window.$paella_bg2e = bg;
              resolve(window.$paella_bg2e);
            })["catch"](function (err) {
              console.error(err.message);
              reject();
            });
          } else {
            defer.resolve(window.$paella_bg2e);
          }
        });
      }
    }, {
      key: "_deferredAction",
      value: function _deferredAction(action) {
        var _this153 = this;

        return new Promise(function (resolve, reject) {
          if (_this153.video) {
            resolve(action());
          } else {
            $(_this153.video).bind('canplay', function () {
              _this153._ready = true;
              resolve(action());
            });
          }
        });
      }
    }, {
      key: "_getQualityObject",
      value: function _getQualityObject(index, s) {
        return {
          index: index,
          res: s.res,
          src: s.src,
          toString: function toString() {
            return this.res.w + "x" + this.res.h;
          },
          shortLabel: function shortLabel() {
            return this.res.h + "p";
          },
          compare: function compare(q2) {
            return this.res.w * this.res.h - q2.res.w * q2.res.h;
          }
        };
      } // Initialization functions

    }, {
      key: "getVideoData",
      value: function getVideoData() {
        var _this154 = this;

        var This = this;
        return new Promise(function (resolve, reject) {
          _this154._deferredAction(function () {
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
    }, {
      key: "setPosterFrame",
      value: function setPosterFrame(url) {
        this._posterFrame = url;
      }
    }, {
      key: "setAutoplay",
      value: function setAutoplay(auto) {
        this._autoplay = auto;

        if (auto && this.video) {
          this.video.setAttribute("autoplay", auto);
        }
      }
    }, {
      key: "load",
      value: function load() {
        var _this155 = this;

        var This = this;
        return new Promise(function (resolve, reject) {
          _this155._loadDeps().then(function () {
            var sources = _this155._stream.sources[_this155._streamName];

            if (_this155._currentQuality === null && _this155._videoQualityStrategy) {
              _this155._currentQuality = _this155._videoQualityStrategy.getQualityIndex(sources);
            }

            var stream = _this155._currentQuality < sources.length ? sources[_this155._currentQuality] : null;
            _this155.video = null;
            _this155.domElement.parentNode.style.backgroundColor = "transparent";

            if (stream) {
              _this155.canvasController = null;
              buildChromaVideoCanvas(stream, _this155.domElement).then(function (canvasController) {
                _this155.canvasController = canvasController;
                _this155.video = canvasController.video;

                _this155.video.pause();

                if (stream.crop) {
                  _this155.canvasController.crop = new bg.Vector4(stream.crop.left, stream.crop.top, stream.crop.right, stream.crop.bottom);
                }

                if (stream.displacement) {
                  _this155.canvasController.transform = bg.Matrix4.Translation(stream.displacement.x, stream.displacement.y, 0);
                }

                if (stream.chromaColor) {
                  _this155.canvasController.chroma = new bg.Color(stream.chromaColor[0], stream.chromaColor[1], stream.chromaColor[2], stream.chromaColor[3]);
                }

                if (stream.chromaBias) {
                  _this155.canvasController.bias = stream.chromaBias;
                }

                resolve(stream);
              });
            } else {
              reject(new Error("Could not load video: invalid quality stream index"));
            }
          });
        });
      }
    }, {
      key: "getQualities",
      value: function getQualities() {
        var _this156 = this;

        return new Promise(function (resolve, reject) {
          setTimeout(function () {
            var result = [];
            var sources = _this156._stream.sources[_this156._streamName];
            var index = -1;
            sources.forEach(function (s) {
              index++;
              result.push(_this156._getQualityObject(index, s));
            });
            resolve(result);
          }, 10);
        });
      }
    }, {
      key: "setQuality",
      value: function setQuality(index) {
        var _this157 = this;

        return new Promise(function (resolve) {
          var paused = _this157.video.paused;
          var sources = _this157._stream.sources[_this157._streamName];
          _this157._currentQuality = index < sources.length ? index : 0;
          var currentTime = _this157.video.currentTime;

          _this157.freeze().then(function () {
            _this157._ready = false;
            return _this157.load();
          }).then(function () {
            if (!paused) {
              _this157.play();
            }

            $(_this157.video).on('seeked', function () {
              _this157.unFreeze();

              resolve();
              $(_this157.video).off('seeked');
            });
            _this157.video.currentTime = currentTime;
          });
        });
      }
    }, {
      key: "getCurrentQuality",
      value: function getCurrentQuality() {
        var _this158 = this;

        return new Promise(function (resolve) {
          resolve(_this158._getQualityObject(_this158._currentQuality, _this158._stream.sources[_this158._streamName][_this158._currentQuality]));
        });
      }
    }, {
      key: "play",
      value: function play() {
        var _this159 = this;

        return this._deferredAction(function () {
          bg.app.MainLoop.singleton.updateMode = bg.app.FrameUpdate.AUTO;

          _this159.video.play();
        });
      }
    }, {
      key: "pause",
      value: function pause() {
        var _this160 = this;

        return this._deferredAction(function () {
          bg.app.MainLoop.singleton.updateMode = bg.app.FrameUpdate.MANUAL;

          _this160.video.pause();
        });
      }
    }, {
      key: "isPaused",
      value: function isPaused() {
        var _this161 = this;

        return this._deferredAction(function () {
          return _this161.video.paused;
        });
      }
    }, {
      key: "duration",
      value: function duration() {
        var _this162 = this;

        return this._deferredAction(function () {
          return _this162.video.duration;
        });
      }
    }, {
      key: "setCurrentTime",
      value: function setCurrentTime(time) {
        var _this163 = this;

        return this._deferredAction(function () {
          _this163.video.currentTime = time;
          $(_this163.video).on('seeked', function () {
            _this163.canvasController.postRedisplay();

            $(_this163.video).off('seeked');
          });
        });
      }
    }, {
      key: "currentTime",
      value: function currentTime() {
        var _this164 = this;

        return this._deferredAction(function () {
          return _this164.video.currentTime;
        });
      }
    }, {
      key: "setVolume",
      value: function setVolume(volume) {
        var _this165 = this;

        return this._deferredAction(function () {
          _this165.video.volume = volume;
        });
      }
    }, {
      key: "volume",
      value: function volume() {
        var _this166 = this;

        return this._deferredAction(function () {
          return _this166.video.volume;
        });
      }
    }, {
      key: "setPlaybackRate",
      value: function setPlaybackRate(rate) {
        var _this167 = this;

        return this._deferredAction(function () {
          _this167.video.playbackRate = rate;
        });
      }
    }, {
      key: "playbackRate",
      value: function playbackRate() {
        var _this168 = this;

        return this._deferredAction(function () {
          return _this168.video.playbackRate;
        });
      }
    }, {
      key: "goFullScreen",
      value: function goFullScreen() {
        var _this169 = this;

        return this._deferredAction(function () {
          var elem = _this169.video;

          if (elem.requestFullscreen) {
            elem.requestFullscreen();
          } else if (elem.msRequestFullscreen) {
            elem.msRequestFullscreen();
          } else if (elem.mozRequestFullScreen) {
            elem.mozRequestFullScreen();
          } else if (elem.webkitEnterFullscreen) {
            elem.webkitEnterFullscreen();
          }
        });
      }
    }, {
      key: "unFreeze",
      value: function unFreeze() {
        var _this170 = this;

        return this._deferredAction(function () {
          var c = document.getElementById(_this170.video.className + "canvas");
          $(c).remove();
        });
      }
    }, {
      key: "freeze",
      value: function freeze() {
        var This = this;
        return this._deferredAction(function () {});
      }
    }, {
      key: "unload",
      value: function unload() {
        this._callUnloadEvent();

        return paella_DeferredNotImplemented();
      }
    }, {
      key: "getDimensions",
      value: function getDimensions() {
        return paella_DeferredNotImplemented();
      }
    }]);

    return ChromaVideo;
  }(paella.VideoElementBase);

  paella.ChromaVideo = ChromaVideo;

  var ChromaVideoFactory = /*#__PURE__*/function (_paella$VideoFactory2) {
    _inherits(ChromaVideoFactory, _paella$VideoFactory2);

    var _super73 = _createSuper(ChromaVideoFactory);

    function ChromaVideoFactory() {
      _classCallCheck(this, ChromaVideoFactory);

      return _super73.apply(this, arguments);
    }

    _createClass(ChromaVideoFactory, [{
      key: "isStreamCompatible",
      value: function isStreamCompatible(streamData) {
        try {
          if (paella.ChromaVideo._loaded) {
            return false;
          }

          if (paella.videoFactories.Html5VideoFactory.s_instances > 0 && paella.utils.userAgent.system.iOS) {
            return false;
          }

          for (var key in streamData.sources) {
            if (key == 'chroma') return true;
          }
        } catch (e) {}

        return false;
      }
    }, {
      key: "getVideoObject",
      value: function getVideoObject(id, streamData, rect) {
        paella.ChromaVideo._loaded = true;
        ++paella.videoFactories.Html5VideoFactory.s_instances;
        return new paella.ChromaVideo(id, streamData, rect.x, rect.y, rect.w, rect.h);
      }
    }]);

    return ChromaVideoFactory;
  }(paella.VideoFactory);

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


paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin6) {
    _inherits(extendedTabAdapterPlugin, _paella$ButtonPlugin6);

    var _super74 = _createSuper(extendedTabAdapterPlugin);

    function extendedTabAdapterPlugin() {
      _classCallCheck(this, extendedTabAdapterPlugin);

      return _super74.apply(this, arguments);
    }

    _createClass(extendedTabAdapterPlugin, [{
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "extendedTabAdapterPlugin";
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-folder';
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 2030;
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.extendedTabAdapterPlugin";
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Extended Tab Adapter");
      }
    }, {
      key: "getButtonType",
      value: function getButtonType() {
        return paella.ButtonPlugin.type.popUpButton;
      }
    }, {
      key: "buildContent",
      value: function buildContent(domElement) {
        domElement.appendChild(paella.extendedAdapter.bottomContainer);
      }
    }, {
      key: "currentUrl",
      get: function get() {
        return this._currentUrl;
      },
      set: function set(v) {
        this._currentUrl = v;
      }
    }, {
      key: "currentMaster",
      get: function get() {
        return this._currentMaster;
      },
      set: function set(v) {
        this._currentMaster = v;
      }
    }, {
      key: "currentSlave",
      get: function get() {
        return this._currentSlave;
      },
      set: function set(v) {
        this._currentSlave = v;
      }
    }, {
      key: "availableMasters",
      get: function get() {
        return this._availableMasters;
      },
      set: function set(v) {
        this._availableMasters = v;
      }
    }, {
      key: "availableSlaves",
      get: function get() {
        return this._availableSlaves;
      },
      set: function set(v) {
        this._availableSlaves = v;
      }
    }, {
      key: "showWidthRes",
      get: function get() {
        return this._showWidthRes;
      },
      set: function set(v) {
        this._showWidthRes = v;
      }
    }]);

    return extendedTabAdapterPlugin;
  }(paella.ButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin7) {
    _inherits(FootPrintsPlugin, _paella$ButtonPlugin7);

    var _super75 = _createSuper(FootPrintsPlugin);

    function FootPrintsPlugin() {
      _classCallCheck(this, FootPrintsPlugin);

      return _super75.apply(this, arguments);
    }

    _createClass(FootPrintsPlugin, [{
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "footPrints";
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-stats';
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 590;
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Show statistics");
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.footprintsPlugin";
      }
    }, {
      key: "getButtonType",
      value: function getButtonType() {
        return paella.ButtonPlugin.type.timeLineButton;
      }
    }, {
      key: "setup",
      value: function setup() {
        this._INTERVAL_LENGTH = 5;
        var thisClass = this;
        paella.events.bind(paella.events.timeUpdate, function (event) {
          thisClass.onTimeUpdate();
        });

        switch (this.config.skin) {
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
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        onSuccess(!paella.player.isLiveStream());
      }
    }, {
      key: "buildContent",
      value: function buildContent(domElement) {
        var container = document.createElement('div');
        container.className = 'footPrintsContainer';
        this.canvas = document.createElement('canvas');
        this.canvas.id = 'footPrintsCanvas';
        this.canvas.className = 'footPrintsCanvas';
        container.appendChild(this.canvas);
        domElement.appendChild(container);
      }
    }, {
      key: "onTimeUpdate",
      value: function onTimeUpdate() {
        var _this171 = this;

        var currentTime = -1;
        paella.player.videoContainer.currentTime().then(function (c) {
          currentTime = c;
          return paella.player.videoContainer.trimming();
        }).then(function (trimming) {
          var videoCurrentTime = Math.round(currentTime + (trimming.enabled ? trimming.start : 0));

          if (_this171.inPosition <= videoCurrentTime && videoCurrentTime <= _this171.inPosition + _this171.INTERVAL_LENGTH) {
            _this171.outPosition = videoCurrentTime;

            if (_this171.inPosition + _this171.INTERVAL_LENGTH === _this171.outPosition) {
              _this171.trackFootPrint(_this171.inPosition, _this171.outPosition);

              _this171.inPosition = _this171.outPosition;
            }
          } else {
            _this171.trackFootPrint(_this171.inPosition, _this171.outPosition);

            _this171.inPosition = videoCurrentTime;
            _this171.outPosition = videoCurrentTime;
          }
        });
      }
    }, {
      key: "trackFootPrint",
      value: function trackFootPrint(inPosition, outPosition) {
        var data = {
          "in": inPosition,
          "out": outPosition
        };
        paella.data.write('footprints', {
          id: paella.initDelegate.getId()
        }, data);
      }
    }, {
      key: "willShowContent",
      value: function willShowContent() {
        var thisClass = this;
        this.loadFootprints();
        this.footPrintsTimer = new paella.utils.Timer(function (timer) {
          thisClass.loadFootprints();
        }, 5000);
        this.footPrintsTimer.repeat = true;
      }
    }, {
      key: "didHideContent",
      value: function didHideContent() {
        if (this.footPrintsTimer != null) {
          this.footPrintsTimer.cancel();
          this.footPrintsTimer = null;
        }
      }
    }, {
      key: "loadFootprints",
      value: function loadFootprints() {
        var thisClass = this;
        paella.data.read('footprints', {
          id: paella.initDelegate.getId()
        }, function (data, status) {
          var footPrintsData = {};
          paella.player.videoContainer.duration().then( /*#__PURE__*/function () {
            var _ref = _asyncToGenerator( /*#__PURE__*/regeneratorRuntime.mark(function _callee(duration) {
              var trimStart, lastPosition, lastViews, i, position, views, j;
              return regeneratorRuntime.wrap(function _callee$(_context) {
                while (1) {
                  switch (_context.prev = _context.next) {
                    case 0:
                      _context.t0 = Math;
                      _context.next = 3;
                      return paella.player.videoContainer.trimStart();

                    case 3:
                      _context.t1 = _context.sent;
                      trimStart = _context.t0.floor.call(_context.t0, _context.t1);
                      lastPosition = -1;
                      lastViews = 0;

                      for (i = 0; i < data.length; i++) {
                        position = data[i].position - trimStart;

                        if (position < duration) {
                          views = data[i].views;

                          if (position - 1 != lastPosition) {
                            for (j = lastPosition + 1; j < position; j++) {
                              footPrintsData[j] = lastViews;
                            }
                          }

                          footPrintsData[position] = views;
                          lastPosition = position;
                          lastViews = views;
                        }
                      }

                      thisClass.drawFootPrints(footPrintsData);

                    case 9:
                    case "end":
                      return _context.stop();
                  }
                }
              }, _callee);
            }));

            return function (_x) {
              return _ref.apply(this, arguments);
            };
          }());
        });
      }
    }, {
      key: "drawFootPrints",
      value: function drawFootPrints(footPrintsData) {
        if (this.canvas) {
          var duration = Object.keys(footPrintsData).length;
          var ctx = this.canvas.getContext("2d");
          var h = 20;
          var i;

          for (i = 0; i < duration; ++i) {
            if (footPrintsData[i] > h) {
              h = footPrintsData[i];
            }
          }

          this.canvas.setAttribute("width", duration);
          this.canvas.setAttribute("height", h);
          ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
          ctx.fillStyle = this.fillStyle; //'#faa166'; //'#9ED4EE';

          ctx.strokeStyle = this.strokeStyle; //'#fa8533'; //"#0000FF";

          ctx.lineWidth = 2;
          ctx.webkitImageSmoothingEnabled = false;
          ctx.mozImageSmoothingEnabled = false;

          for (i = 0; i < duration - 1; ++i) {
            ctx.beginPath();
            ctx.moveTo(i, h);
            ctx.lineTo(i, h - footPrintsData[i]);
            ctx.lineTo(i + 1, h - footPrintsData[i + 1]);
            ctx.lineTo(i + 1, h);
            ctx.closePath();
            ctx.fill();
            ctx.beginPath();
            ctx.moveTo(i, h - footPrintsData[i]);
            ctx.lineTo(i + 1, h - footPrintsData[i + 1]);
            ctx.closePath();
            ctx.stroke();
          }
        }
      }
    }, {
      key: "INTERVAL_LENGTH",
      get: function get() {
        return this._INTERVAL_LENGTH;
      },
      set: function set(v) {
        this._INTERVAL_LENGTH = v;
      }
    }, {
      key: "inPosition",
      get: function get() {
        return this._inPosition;
      },
      set: function set(v) {
        this._inPosition = v;
      }
    }, {
      key: "outPosition",
      get: function get() {
        return this._outPosition;
      },
      set: function set(v) {
        this._outPosition = v;
      }
    }, {
      key: "canvas",
      get: function get() {
        return this._canvas;
      },
      set: function set(v) {
        this._canvas = v;
      }
    }, {
      key: "footPrintsTimer",
      get: function get() {
        return this._footPrintsTimer;
      },
      set: function set(v) {
        this._footPrintsTimer = v;
      }
    }, {
      key: "footPrintsData",
      get: function get() {
        return this._footPrintsData;
      },
      set: function set(v) {
        this._footPrintsData = v;
      }
    }]);

    return FootPrintsPlugin;
  }(paella.ButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$SearchService) {
    _inherits(FrameCaptionsSearchPlugIn, _paella$SearchService);

    var _super76 = _createSuper(FrameCaptionsSearchPlugIn);

    function FrameCaptionsSearchPlugIn() {
      _classCallCheck(this, FrameCaptionsSearchPlugIn);

      return _super76.apply(this, arguments);
    }

    _createClass(FrameCaptionsSearchPlugIn, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.frameCaptionsSearchPlugin";
      }
    }, {
      key: "search",
      value: function search(text, next) {
        var re = RegExp(text, "i");
        var results = [];

        for (var key in paella.player.videoLoader.frameList) {
          var value = paella.player.videoLoader.frameList[key];

          if (_typeof(value) == "object") {
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
    }]);

    return FrameCaptionsSearchPlugIn;
  }(paella.SearchServicePlugIn);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin8) {
    _inherits(FrameControlPlugin, _paella$ButtonPlugin8);

    var _super77 = _createSuper(FrameControlPlugin);

    function FrameControlPlugin() {
      _classCallCheck(this, FrameControlPlugin);

      return _super77.apply(this, arguments);
    }

    _createClass(FrameControlPlugin, [{
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "frameControl";
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-photo';
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 510;
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.frameControlPlugin";
      }
    }, {
      key: "getButtonType",
      value: function getButtonType() {
        return paella.ButtonPlugin.type.timeLineButton;
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Navigate by slides");
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        this._img = null;
        this._searchTimer = null;
        this._searchTimerTime = 250;
        if (paella.initDelegate.initParams.videoLoader.frameList == null) onSuccess(false);else if (paella.initDelegate.initParams.videoLoader.frameList.length === 0) onSuccess(false);else if (Object.keys(paella.initDelegate.initParams.videoLoader.frameList).length == 0) onSuccess(false);else onSuccess(true);
      }
    }, {
      key: "setup",
      value: function setup() {
        this._showFullPreview = this.config.showFullPreview || "auto";
        var thisClass = this;
        var oldClassName;
        var blockCounter = 1;
        var correctJump = 0;
        var selectedItem = -1;
        var jumpAtItem;
        var Keys = {
          Tab: 9,
          Return: 13,
          Esc: 27,
          End: 35,
          Home: 36,
          Left: 37,
          Up: 38,
          Right: 39,
          Down: 40
        };
        $(this.button).keyup(function (event) {
          var visibleItems = Math.floor(thisClass.contx.offsetWidth / 100);
          var rest = thisClass.buttons.length % visibleItems;
          var blocks = Math.floor(thisClass.buttons.length / visibleItems);

          if (thisClass.isPopUpOpen()) {
            if (event.keyCode == Keys.Left) {
              if (selectedItem > 0) {
                thisClass.buttons[selectedItem].className = oldClassName;
                selectedItem--;
                if (blockCounter > blocks) correctJump = visibleItems - rest;
                jumpAtItem = visibleItems * (blockCounter - 1) - 1 - correctJump;

                if (selectedItem == jumpAtItem && selectedItem != 0) {
                  thisClass.navButtons.left.scrollContainer.scrollLeft -= visibleItems * 105;
                  --blockCounter;
                }

                if (this.hiResFrame) thisClass.removeHiResFrame();

                if (!paella.utils.userAgent.browser.IsMobileVersion) {
                  thisClass.buttons[selectedItem].frameControl.onMouseOver(null, thisClass.buttons[selectedItem].frameData);
                }

                oldClassName = thisClass.buttons[selectedItem].className;
                thisClass.buttons[selectedItem].className = 'frameControlItem selected';
              }
            } else if (event.keyCode == Keys.Right) {
              if (selectedItem < thisClass.buttons.length - 1) {
                if (selectedItem >= 0) {
                  thisClass.buttons[selectedItem].className = oldClassName;
                }

                selectedItem++;
                if (blockCounter == 1) correctJump = 0;
                jumpAtItem = visibleItems * blockCounter - correctJump;

                if (selectedItem == jumpAtItem) {
                  thisClass.navButtons.left.scrollContainer.scrollLeft += visibleItems * 105;
                  ++blockCounter;
                }

                if (this.hiResFrame) thisClass.removeHiResFrame();

                if (!paella.utils.userAgent.browser.IsMobileVersion) {
                  thisClass.buttons[selectedItem].frameControl.onMouseOver(null, thisClass.buttons[selectedItem].frameData);
                }

                oldClassName = thisClass.buttons[selectedItem].className;
                thisClass.buttons[selectedItem].className = 'frameControlItem selected';
              }
            } else if (event.keyCode == Keys.Return) {
              thisClass.buttons[selectedItem].frameControl.onClick(null, thisClass.buttons[selectedItem].frameData);
              oldClassName = 'frameControlItem current';
            } else if (event.keyCode == Keys.Esc) {
              thisClass.removeHiResFrame();
            }
          }
        });
      }
    }, {
      key: "buildContent",
      value: function buildContent(domElement) {
        var _this172 = this;

        var thisClass = this;
        this.frames = [];
        var container = document.createElement('div');
        container.className = 'frameControlContainer';
        thisClass.contx = container;
        var content = document.createElement('div');
        content.className = 'frameControlContent';
        this.navButtons = {
          left: document.createElement('div'),
          right: document.createElement('div')
        };
        this.navButtons.left.className = 'frameControl navButton left';
        this.navButtons.right.className = 'frameControl navButton right';
        var frame = this.getFrame(null);
        domElement.appendChild(this.navButtons.left);
        domElement.appendChild(container);
        container.appendChild(content);
        domElement.appendChild(this.navButtons.right);
        this.navButtons.left.scrollContainer = container;
        $(this.navButtons.left).click(function (event) {
          this.scrollContainer.scrollLeft -= 100;
        });
        this.navButtons.right.scrollContainer = container;
        $(this.navButtons.right).click(function (event) {
          this.scrollContainer.scrollLeft += 100;
        });
        content.appendChild(frame);
        var itemWidth = $(frame).outerWidth(true);
        content.innerText = '';
        $(window).mousemove(function (event) {
          if ($(content).offset().top > event.pageY || !$(content).is(":visible") || $(content).offset().top + $(content).height() < event.pageY) {
            thisClass.removeHiResFrame();
          }
        });
        var frames = paella.initDelegate.initParams.videoLoader.frameList;
        var numFrames;

        if (frames) {
          var framesKeys = Object.keys(frames);
          numFrames = framesKeys.length;
          framesKeys.map(function (i) {
            return Number(i, 10);
          }).sort(function (a, b) {
            return a - b;
          }).forEach(function (key) {
            var frameItem = thisClass.getFrame(frames[key]);
            content.appendChild(frameItem, 'frameContrlItem_' + numFrames);
            thisClass.frames.push(frameItem);
          });
        }

        $(content).css({
          width: numFrames * itemWidth + 'px'
        });
        paella.events.bind(paella.events.setTrim, function (event, params) {
          _this172.updateFrameVisibility(params.trimEnabled, params.trimStart, params.trimEnd);
        });
        paella.player.videoContainer.trimming().then(function (trimData) {
          _this172.updateFrameVisibility(trimData.enabled, trimData.start, trimData.end);
        });
        paella.events.bind(paella.events.timeupdate, function (event, params) {
          return _this172.onTimeUpdate(params.currentTime);
        });
      }
    }, {
      key: "showHiResFrame",
      value: function showHiResFrame(url, caption) {
        var frameRoot = document.createElement("div");
        var frame = document.createElement("div");
        var hiResImage = document.createElement('img');
        this._img = hiResImage;
        hiResImage.className = 'frameHiRes';
        hiResImage.setAttribute('src', url);
        hiResImage.setAttribute('style', 'width: 100%;');
        $(frame).append(hiResImage);
        $(frameRoot).append(frame);
        frameRoot.setAttribute('style', 'display: table;');
        frame.setAttribute('style', 'display: table-cell; vertical-align:middle;');

        if (this.config.showCaptions === true) {
          var captionContainer = document.createElement('p');
          captionContainer.className = "frameCaption";
          captionContainer.innerText = caption || "";
          frameRoot.append(captionContainer);
          this._caption = captionContainer;
        }

        var overlayContainer = paella.player.videoContainer.overlayContainer;

        switch (this._showFullPreview) {
          case "auto":
            var streams = paella.initDelegate.initParams.videoLoader.streams;

            if (streams.length == 1) {
              overlayContainer.addElement(frameRoot, overlayContainer.getVideoRect(0));
            } else if (streams.length >= 2) {
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

            if (streams.length >= 2) {
              overlayContainer.addElement(frameRoot, overlayContainer.getVideoRect(0));
              overlayContainer.enableBackgroundMode();
              this.hiResFrame = frameRoot;
            }

            break;
        }
      }
    }, {
      key: "removeHiResFrame",
      value: function removeHiResFrame() {
        var thisClass = this;
        var overlayContainer = paella.player.videoContainer.overlayContainer;

        if (this.hiResFrame) {
          overlayContainer.removeElement(this.hiResFrame);
        }

        overlayContainer.disableBackgroundMode();
        thisClass._img = null;
      }
    }, {
      key: "updateFrameVisibility",
      value: function updateFrameVisibility(trimEnabled, trimStart, trimEnd) {
        var i;

        if (!trimEnabled) {
          for (i = 0; i < this.frames.length; ++i) {
            $(this.frames[i]).show();
          }
        } else {
          for (i = 0; i < this.frames.length; ++i) {
            var frameElem = this.frames[i];
            var frameData = frameElem.frameData;

            if (frameData.time < trimStart) {
              if (this.frames.length > i + 1 && this.frames[i + 1].frameData.time > trimStart) {
                $(frameElem).show();
              } else {
                $(frameElem).hide();
              }
            } else if (frameData.time > trimEnd) {
              $(frameElem).hide();
            } else {
              $(frameElem).show();
            }
          }
        }
      }
    }, {
      key: "getFrame",
      value: function getFrame(frameData, id) {
        var frame = document.createElement('div');
        frame.className = 'frameControlItem';
        if (id) frame.id = id;

        if (frameData) {
          this.buttons.push(frame);
          frame.frameData = frameData;
          frame.frameControl = this;
          var image = frameData.thumb ? frameData.thumb : frameData.url;
          var labelTime = paella.utils.timeParse.secondsToTime(frameData.time);
          frame.innerHTML = '<img src="' + image + '" alt="" class="frameControlImage" title="' + labelTime + '" aria-label="' + labelTime + '"></img>';

          if (!paella.utils.userAgent.browser.IsMobileVersion) {
            $(frame).mouseover(function (event) {
              this.frameControl.onMouseOver(event, this.frameData);
            });
          }

          $(frame).mouseout(function (event) {
            this.frameControl.onMouseOut(event, this.frameData);
          });
          $(frame).click(function (event) {
            this.frameControl.onClick(event, this.frameData);
          });
        }

        return frame;
      }
    }, {
      key: "onMouseOver",
      value: function onMouseOver(event, frameData) {
        var frames = paella.initDelegate.initParams.videoLoader.frameList;
        var frame = frames[frameData.time];

        if (frame) {
          var image = frame.url;

          if (this._img) {
            this._img.setAttribute('src', image);

            if (this.config.showCaptions === true) {
              this._caption.innerText = frame.caption || "";
            }
          } else {
            this.showHiResFrame(image, frame.caption);
          }
        }

        if (this._searchTimer != null) {
          clearTimeout(this._searchTimer);
        }
      }
    }, {
      key: "onMouseOut",
      value: function onMouseOut(event, frameData) {
        var _this173 = this;

        this._searchTimer = setTimeout(function (timer) {
          return _this173.removeHiResFrame();
        }, this._searchTimerTime);
      }
    }, {
      key: "onClick",
      value: function onClick(event, frameData) {
        paella.player.videoContainer.trimming().then(function (trimming) {
          var time = trimming.enabled ? frameData.time - trimming.start : frameData.time;

          if (time > 0) {
            paella.player.videoContainer.seekToTime(time + 1);
          } else {
            paella.player.videoContainer.seekToTime(0);
          }
        });
      }
    }, {
      key: "onTimeUpdate",
      value: function onTimeUpdate(currentTime) {
        var _this174 = this;

        var frame = null;
        paella.player.videoContainer.trimming().then(function (trimming) {
          var time = trimming.enabled ? currentTime + trimming.start : currentTime;

          for (var i = 0; i < _this174.frames.length; ++i) {
            if (_this174.frames[i].frameData && _this174.frames[i].frameData.time <= time) {
              frame = _this174.frames[i];
            } else {
              break;
            }
          }

          if (_this174.currentFrame != frame && frame) {
            //this.navButtons.left.scrollContainer.scrollLeft += 100;
            if (_this174.currentFrame) _this174.currentFrame.className = 'frameControlItem';
            _this174.currentFrame = frame;
            _this174.currentFrame.className = 'frameControlItem current';
          }
        });
      }
    }, {
      key: "frames",
      get: function get() {
        return this._frames;
      },
      set: function set(v) {
        this._frames = v;
      }
    }, {
      key: "highResFrames",
      get: function get() {
        return this._highResFrames;
      },
      set: function set(v) {
        this._highResFrames = v;
      }
    }, {
      key: "currentFrame",
      get: function get() {
        return this._currentFrame;
      },
      set: function set(v) {
        this._currentFrame = v;
      }
    }, {
      key: "navButtons",
      get: function get() {
        return this._navButtons;
      },
      set: function set(v) {
        this._navButtons = v;
      }
    }, {
      key: "buttons",
      get: function get() {
        if (!this._buttons) {
          this._buttons = [];
        }

        return this._buttons;
      },
      set: function set(v) {
        this._buttons = v;
      }
    }, {
      key: "contx",
      get: function get() {
        return this._contx;
      },
      set: function set(v) {
        this._contx = v;
      }
    }]);

    return FrameControlPlugin;
  }(paella.ButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin9) {
    _inherits(FullScreenPlugin, _paella$ButtonPlugin9);

    var _super78 = _createSuper(FullScreenPlugin);

    function FullScreenPlugin() {
      _classCallCheck(this, FullScreenPlugin);

      return _super78.apply(this, arguments);
    }

    _createClass(FullScreenPlugin, [{
      key: "getIndex",
      value: function getIndex() {
        return 551;
      }
    }, {
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "showFullScreenButton";
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-fullscreen';
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.fullScreenButtonPlugin";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        this._reload = null;
        var enabled = paella.player.checkFullScreenCapability();
        onSuccess(enabled);
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Go Fullscreen");
      }
    }, {
      key: "setup",
      value: function setup() {
        var _this175 = this;

        this._reload = this.config.reloadOnFullscreen ? this.config.reloadOnFullscreen.enabled : false;
        paella.events.bind(paella.events.enterFullscreen, function (event) {
          return _this175.onEnterFullscreen();
        });
        paella.events.bind(paella.events.exitFullscreen, function (event) {
          return _this175.onExitFullscreen();
        });
      }
    }, {
      key: "action",
      value: function action(button) {
        var _this176 = this;

        if (paella.player.isFullScreen()) {
          paella.player.exitFullScreen();
        } else if ((!paella.player.checkFullScreenCapability() || paella.utils.userAgent.browser.Explorer) && window.location !== window.parent.location) {
          // Iframe and no fullscreen support
          var url = window.location.href;
          paella.player.pause();
          paella.player.videoContainer.currentTime().then(function (currentTime) {
            var obj = _this176.secondsToHours(currentTime);

            window.open(url + "&time=" + obj.h + "h" + obj.m + "m" + obj.s + "s&autoplay=true");
          });
          return;
        } else {
          paella.player.goFullScreen();
        }

        if (paella.player.config.player.reloadOnFullscreen && paella.player.videoContainer.supportAutoplay()) {
          setTimeout(function () {
            if (_this176._reload) {
              paella.player.videoContainer.setQuality(null).then(function () {}); //paella.player.reloadVideos();
            }
          }, 1000);
        }
      }
    }, {
      key: "secondsToHours",
      value: function secondsToHours(sec_numb) {
        var hours = Math.floor(sec_numb / 3600);
        var minutes = Math.floor((sec_numb - hours * 3600) / 60);
        var seconds = Math.floor(sec_numb - hours * 3600 - minutes * 60);
        var obj = {};

        if (hours < 10) {
          hours = "0" + hours;
        }

        if (minutes < 10) {
          minutes = "0" + minutes;
        }

        if (seconds < 10) {
          seconds = "0" + seconds;
        }

        obj.h = hours;
        obj.m = minutes;
        obj.s = seconds;
        return obj;
      }
    }, {
      key: "onEnterFullscreen",
      value: function onEnterFullscreen() {
        this.setToolTip(paella.utils.dictionary.translate("Exit Fullscreen"));
        this.button.className = this.getButtonItemClass(true);
        this.changeIconClass('icon-windowed');
      }
    }, {
      key: "onExitFullscreen",
      value: function onExitFullscreen() {
        this.setToolTip(paella.utils.dictionary.translate("Go Fullscreen"));
        this.button.className = this.getButtonItemClass(false);
        this.changeIconClass('icon-fullscreen');
        setTimeout(function () {
          paella.player.onresize();
        }, 100);
      }
    }, {
      key: "getButtonItemClass",
      value: function getButtonItemClass(selected) {
        return 'buttonPlugin ' + this.getAlignment() + ' ' + this.getSubclass() + (selected ? ' active' : '');
      }
    }]);

    return FullScreenPlugin;
  }(paella.ButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin10) {
    _inherits(HelpPlugin, _paella$ButtonPlugin10);

    var _super79 = _createSuper(HelpPlugin);

    function HelpPlugin() {
      _classCallCheck(this, HelpPlugin);

      return _super79.apply(this, arguments);
    }

    _createClass(HelpPlugin, [{
      key: "getIndex",
      value: function getIndex() {
        return 509;
      }
    }, {
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "helpButton";
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-help';
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.helpPlugin";
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Show help") + ' (' + paella.utils.dictionary.translate("Paella version:") + ' ' + paella.version + ')';
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        var availableLangs = this.config && this.config.langs || [];
        onSuccess(availableLangs.length > 0);
      }
    }, {
      key: "action",
      value: function action(button) {
        var mylang = paella.utils.dictionary.currentLanguage();
        var availableLangs = this.config && this.config.langs || [];
        var idx = availableLangs.indexOf(mylang);

        if (idx < 0) {
          idx = 0;
        } //paella.messageBox.showFrame("http://paellaplayer.upv.es/?page=usage");


        var url = "resources/style/help/help_" + availableLangs[idx] + ".html";

        if (paella.utils.userAgent.browser.IsMobileVersion) {
          window.open(url);
        } else {
          paella.messageBox.showFrame(url);
        }
      }
    }]);

    return HelpPlugin;
  }(paella.ButtonPlugin);
});

(function () {
  var s_preventVideoDump = [];

  var HLSPlayer = /*#__PURE__*/function (_paella$Html5Video) {
    _inherits(HLSPlayer, _paella$Html5Video);

    var _super80 = _createSuper(HLSPlayer);

    _createClass(HLSPlayer, [{
      key: "config",
      get: function get() {
        var config = {
          autoStartLoad: true,
          startPosition: -1,
          capLevelToPlayerSize: true,
          debug: false,
          defaultAudioCodec: undefined,
          initialLiveManifestSize: 1,
          initialQualityLevel: 1,
          maxBufferLength: 30,
          maxMaxBufferLength: 600,
          maxBufferSize: 60 * 1000 * 1000,
          maxBufferHole: 0.5,
          lowBufferWatchdogPeriod: 0.5,
          highBufferWatchdogPeriod: 3,
          nudgeOffset: 0.1,
          nudgeMaxRetry: 3,
          maxFragLookUpTolerance: 0.2,
          liveSyncDurationCount: 3,
          liveMaxLatencyDurationCount: 10,
          enableWorker: true,
          enableSoftwareAES: true,
          manifestLoadingTimeOut: 10000,
          manifestLoadingMaxRetry: 1,
          manifestLoadingRetryDelay: 500,
          manifestLoadingMaxRetryTimeout: 64000,
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
          maxAudioFramesDrift: 1,
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
        var pluginConfig = {};
        paella.player.config.player.methods.some(function (methodConfig) {
          if (methodConfig.factory == "HLSVideoFactory") {
            pluginConfig = methodConfig.config || {};
            return true;
          }
        });

        for (var key in config) {
          if (pluginConfig[key] != undefined) {
            config[key] = pluginConfig[key];
          }
        }

        return config;
      }
    }]);

    function HLSPlayer(id, stream, left, top, width, height) {
      _classCallCheck(this, HLSPlayer);

      return _super80.call(this, id, stream, left, top, width, height, 'hls');
    }

    _createClass(HLSPlayer, [{
      key: "_loadDeps",
      value: function _loadDeps() {
        return new Promise(function (resolve, reject) {
          if (!window.$paella_hls) {
            paella.require(paella.baseUrl + 'javascript/hls.min.js').then(function (hls) {
              window.$paella_hls = hls;
              resolve(window.$paella_hls);
            });
          } else {
            resolve(window.$paella_hls);
          }
        });
      }
    }, {
      key: "_deferredAction",
      value: function _deferredAction(action) {
        var _this177 = this;

        return new Promise(function (resolve, reject) {
          function processResult(actionResult) {
            if (actionResult instanceof Promise) {
              actionResult.then(function (p) {
                return resolve(p);
              })["catch"](function (err) {
                return reject(err);
              });
            } else {
              resolve(actionResult);
            }
          }

          if (_this177.ready) {
            processResult(action());
          } else {
            var eventFunction = function eventFunction() {
              processResult(action());
              $(_this177.video).unbind('canplay');
              $(_this177.video).unbind('loadedmetadata');

              if (timer) {
                clearTimeout(timer);
                timer = null;
              }
            };

            $(_this177.video).bind('canplay', eventFunction);
            $(_this177.video).bind('loadedmetadata', eventFunction);

            var timerFunction = function timerFunction() {
              if (!_this177.ready) {
                if (!_this177._hls) {
                  // iOS
                  // In this way the recharge is forced, and it is possible to recover errors.
                  console.debug("HLS video resume failed. Trying to recover.");
                  var src = _this177.video.innerHTML;
                  _this177.video.innerHTML = "";
                  _this177.video.innerHTML = src;

                  _this177.video.load();

                  _this177.video.play();
                }

                timer = setTimeout(timerFunction, 1000);
              } else {
                eventFunction();
              }
            };

            var timer = setTimeout(timerFunction, 1000);
          }
        });
      }
    }, {
      key: "setupHls",
      value: function setupHls(video, url) {
        var _this178 = this;

        var initialQualityLevel = this.config.initialQualityLevel !== undefined ? this.config.initialQualityLevel : 1;
        return new Promise(function (resolve, reject) {
          _this178._loadDeps().then(function (Hls) {
            if (Hls.isSupported()) {
              var cfg = _this178.config; //cfg.autoStartLoad = false;

              _this178._hls = new Hls(cfg);
              _this178.autoQuality = true; // For some streams there are problems if playback does not start after loading the
              // manifest. This flag is used to pause it again once the video is loaded

              var firstLoad = true;

              _this178._hls.on(Hls.Events.LEVEL_SWITCHED, function (ev, data) {
                if (firstLoad) {
                  firstLoad = false;
                  video.pause();
                }

                _this178._qualities = _this178._qualities || [];
                _this178._qualityIndex = _this178.autoQuality ? _this178._qualities.length - 1 : data.level;
                paella.events.trigger(paella.events.qualityChanged, {});
                if (console && console.log) console.log("HLS: quality level changed to ".concat(data.level));
              });

              _this178._hls.on(Hls.Events.ERROR, function (event, data) {
                if (data.fatal) {
                  switch (data.type) {
                    case Hls.ErrorTypes.NETWORK_ERROR:
                      if (data.details == Hls.ErrorDetails.MANIFEST_LOAD_ERROR) {
                        // TODO: Manifest file not found
                        console.error("paella.HLSPlayer: unrecoverable error in HLS Player. The video is not available.");
                        reject(new Error("No such HLS stream: the video is not available"));
                      } else {
                        console.error("paella.HLSPlayer: Fatal network error encountered, try to recover");

                        _this178._hls.startLoad();
                      }

                      break;

                    case Hls.ErrorTypes.MEDIA_ERROR:
                      console.error("paella.HLSPlayer: Fatal media error encountered, try to recover");

                      _this178._hls.recoverMediaError();

                      break;

                    default:
                      console.error("paella.HLSPlayer: Fatal error. Can not recover");

                      _this178._hls.destroy();

                      reject(new Errro("Invalid media"));
                      break;
                  }
                }
              });

              _this178._hls.on(Hls.Events.MANIFEST_PARSED, function () {
                if (!cfg.autoStartLoad) {
                  _this178._hls.startLoad();
                } // Fixes hls.js problems when loading the initial quality level


                _this178._hls.currentLevel = _this178._hls.levels.length >= initialQualityLevel ? initialQualityLevel : -1;
                setTimeout(function () {
                  return _this178._hls.currentLevel = -1;
                }, 1000); // Fixes hls.js problems loading some videos

                video.play();
                resolve(video);
              });

              _this178._hls.loadSource(url);

              _this178._hls.attachMedia(video);
            } else {
              reject(new Error("HLS not supported"));
            }
          });
        });
      }
    }, {
      key: "webGlDidLoad",
      value: function webGlDidLoad() {
        var _this179 = this;

        if (paella.utils.userAgent.system.iOS) {
          return _get(_getPrototypeOf(HLSPlayer.prototype), "webGlDidLoad", this).call(this);
        } // Register a new video loader in the webgl engine, to enable the
        // hls compatibility in webgl canvas


        bg.utils.HTTPResourceProvider.AddVideoLoader('m3u8', function (url, onSuccess, onFail) {
          var video = document.createElement("video");
          s_preventVideoDump.push(video);

          _this179.setupHls(video, url).then(function () {
            return onSuccess(video);
          })["catch"](function () {
            return onFail();
          });
        });
        return Promise.resolve();
      }
    }, {
      key: "loadVideoStream",
      value: function loadVideoStream(canvasInstance, stream) {
        var _this180 = this;

        if (paella.utils.userAgent.system.iOS) {
          return _get(_getPrototypeOf(HLSPlayer.prototype), "loadVideoStream", this).call(this, canvasInstance, stream);
        }

        return canvasInstance.loadVideo(this, stream, function (videoElem) {
          return _this180.setupHls(videoElem, stream.src);
        });
      }
    }, {
      key: "supportsMultiaudio",
      value: function supportsMultiaudio() {
        var _this181 = this;

        return this._deferredAction(function () {
          if (paella.utils.userAgent.system.iOS) {
            return _this181.video.audioTracks.length > 1;
          } else {
            return _this181._hls.audioTracks.length > 1;
          }
        });
      }
    }, {
      key: "getAudioTracks",
      value: function getAudioTracks() {
        var _this182 = this;

        return this._deferredAction(function () {
          if (paella.utils.userAgent.system.iOS) {
            var result = [];
            Array.from(_this182.video.audioTracks).forEach(function (t) {
              result.push({
                id: t.id,
                groupId: "",
                name: t.label,
                lang: t.language
              });
            });
            return result;
          } else {
            return _this182._hls.audioTracks;
          }
        });
      }
    }, {
      key: "setCurrentAudioTrack",
      value: function setCurrentAudioTrack(trackId) {
        var _this183 = this;

        return this._deferredAction(function () {
          if (paella.utils.userAgent.system.iOS) {
            var found = false;
            Array.from(_this183.video.audioTracks).forEach(function (track) {
              if (track.id == trackId) {
                found = true;
                track.enabled = true;
              } else {
                track.enabled = false;
              }
            });
            return found;
          } else {
            if (_this183._hls.audioTracks.some(function (track) {
              return track.id == trackId;
            })) {
              _this183._hls.audioTrack = trackId;
              return true;
            } else {
              return false;
            }
          }
        });
      }
    }, {
      key: "getCurrentAudioTrack",
      value: function getCurrentAudioTrack() {
        var _this184 = this;

        return this._deferredAction(function () {
          if (paella.utils.userAgent.system.iOS) {
            var result = null;
            Array.from(_this184.video.audioTracks).some(function (t) {
              if (t.enabled) {
                result = t;
                return true;
              }
            });
            return result;
          } else {
            var _result = null;

            _this184._hls.audioTracks.some(function (t) {
              if (t.id == _this184._hls.audioTrack) {
                _result = t;
                return true;
              }
            });

            return _result;
          }
        });
      }
    }, {
      key: "getQualities",
      value: function getQualities() {
        var _this185 = this;

        if (paella.utils.userAgent.system.iOS) // ||
          //		paella.utils.userAgent.browser.Safari)
          {
            return new Promise(function (resolve, reject) {
              resolve([{
                index: 0,
                res: "",
                src: "",
                toString: function toString() {
                  return "auto";
                },
                shortLabel: function shortLabel() {
                  return "auto";
                },
                compare: function compare(q2) {
                  return 0;
                }
              }]);
            });
          } else {
          var _This = this;

          return new Promise(function (resolve) {
            if (!_this185._qualities || _this185._qualities.length == 0) {
              _This._qualities = [];

              _This._hls.levels.forEach(function (q, index) {
                _This._qualities.push(_This._getQualityObject(index, {
                  index: index,
                  res: {
                    w: q.width,
                    h: q.height
                  },
                  bitrate: q.bitrate
                }));
              });

              if (_this185._qualities.length > 1) {
                // If there is only one quality level, don't add the "auto" option
                _This._qualities.push(_This._getQualityObject(_This._qualities.length, {
                  index: _This._qualities.length,
                  res: {
                    w: 0,
                    h: 0
                  },
                  bitrate: 0
                }));
              }
            }

            _This.qualityIndex = _This._qualities.length - 1;
            resolve(_This._qualities);
          });
        }
      }
    }, {
      key: "disable",
      value: function disable(isMainAudioPlayer) {
        if (paella.utils.userAgent.system.iOS) {
          return;
        }

        this._currentQualityIndex = this._qualityIndex;
        this._hls.currentLevel = 0;
      }
    }, {
      key: "enable",
      value: function enable(isMainAudioPlayer) {
        if (this._currentQualityIndex !== undefined && this._currentQualityIndex !== null) {
          this.setQuality(this._currentQualityIndex);
          this._currentQualityIndex = null;
        }
      }
    }, {
      key: "printQualityes",
      value: function printQualityes() {
        var _this186 = this;

        return new Promise(function (resolve, reject) {
          _this186.getCurrentQuality().then(function (cq) {
            return _this186.getNextQuality();
          }).then(function (nq) {
            resolve();
          });
        });
      }
    }, {
      key: "setQuality",
      value: function setQuality(index) {
        if (paella.utils.userAgent.system.iOS) // ||
          //paella.utils.userAgent.browser.Safari)
          {
            return Promise.resolve();
          } else if (index !== null) {
          try {
            this.qualityIndex = index;
            var level = index;
            this.autoQuality = false;

            if (index == this._qualities.length - 1) {
              level = -1;
              this.autoQuality = true;
            }

            this._hls.currentLevel = level;
          } catch (err) {}

          return Promise.resolve();
        } else {
          return Promise.resolve();
        }
      }
    }, {
      key: "getNextQuality",
      value: function getNextQuality() {
        var _this187 = this;

        return new Promise(function (resolve, reject) {
          var index = _this187.qualityIndex;
          resolve(_this187._qualities[index]);
        });
      }
    }, {
      key: "getCurrentQuality",
      value: function getCurrentQuality() {
        var _this188 = this;

        if (paella.utils.userAgent.system.iOS) // ||
          //paella.utils.userAgent.browser.Safari)
          {
            return Promise.resolve(0);
          } else {
          return new Promise(function (resolve, reject) {
            resolve(_this188._qualities[_this188.qualityIndex]);
          });
        }
      }
    }]);

    return HLSPlayer;
  }(paella.Html5Video);

  paella.HLSPlayer = HLSPlayer;

  var HLSVideoFactory = /*#__PURE__*/function (_paella$VideoFactory3) {
    _inherits(HLSVideoFactory, _paella$VideoFactory3);

    var _super81 = _createSuper(HLSVideoFactory);

    function HLSVideoFactory() {
      _classCallCheck(this, HLSVideoFactory);

      return _super81.apply(this, arguments);
    }

    _createClass(HLSVideoFactory, [{
      key: "isStreamCompatible",
      value: function isStreamCompatible(streamData) {
        if (paella.videoFactories.HLSVideoFactory.s_instances === undefined) {
          paella.videoFactories.HLSVideoFactory.s_instances = 0;
        }

        try {
          var cfg = this.config;

          if (paella.utils.userAgent.system.iOS && paella.videoFactories.HLSVideoFactory.s_instances >= cfg.iOSMaxStreams || paella.utils.userAgent.system.Android && paella.videoFactories.HLSVideoFactory.s_instances >= cfg.androidMaxStreams) //	In some old mobile devices, playing a high number of HLS streams may cause that the browser tab crash
            {
              return false;
            }

          for (var key in streamData.sources) {
            if (key == 'hls') return true;
          }
        } catch (e) {}

        return false;
      }
    }, {
      key: "getVideoObject",
      value: function getVideoObject(id, streamData, rect) {
        ++paella.videoFactories.HLSVideoFactory.s_instances;
        return new paella.HLSPlayer(id, streamData, rect.x, rect.y, rect.w, rect.h);
      }
    }, {
      key: "config",
      get: function get() {
        var hlsConfig = null;
        paella.player.config.player.methods.some(function (methodConfig) {
          if (methodConfig.factory == "HLSVideoFactory") {
            hlsConfig = methodConfig;
          }

          return hlsConfig != null;
        });
        return hlsConfig || {
          iOSMaxStreams: 1,
          androidMaxStreams: 1
        };
      }
    }]);

    return HLSVideoFactory;
  }(paella.VideoFactory);

  paella.videoFactories.HLSVideoFactory = HLSVideoFactory;
})();

paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$KeyPlugin) {
    _inherits(DefaultKeyPlugin, _paella$KeyPlugin);

    var _super82 = _createSuper(DefaultKeyPlugin);

    function DefaultKeyPlugin() {
      _classCallCheck(this, DefaultKeyPlugin);

      return _super82.apply(this, arguments);
    }

    _createClass(DefaultKeyPlugin, [{
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        onSuccess(true);
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.defaultKeysPlugin";
      }
    }, {
      key: "setup",
      value: function setup() {}
    }, {
      key: "onKeyPress",
      value: function onKeyPress(event) {
        // Matterhorn standard keys
        if (event.altKey && event.ctrlKey) {
          if (event.which == paella.Keys.P) {
            this.togglePlayPause();
            return true;
          } else if (event.which == paella.Keys.S) {
            this.pause();
            return true;
          } else if (event.which == paella.Keys.M) {
            this.mute();
            return true;
          } else if (event.which == paella.Keys.U) {
            this.volumeUp();
            return true;
          } else if (event.which == paella.Keys.D) {
            this.volumeDown();
            return true;
          }
        } else {
          // Paella player keys
          if (event.which == paella.Keys.Space) {
            this.togglePlayPause();
            return true;
          } else if (event.which == paella.Keys.Up) {
            this.volumeUp();
            return true;
          } else if (event.which == paella.Keys.Down) {
            this.volumeDown();
            return true;
          } else if (event.which == paella.Keys.M) {
            this.mute();
            return true;
          }
        }

        return false;
      }
    }, {
      key: "togglePlayPause",
      value: function togglePlayPause() {
        paella.player.videoContainer.paused().then(function (p) {
          p ? paella.player.play() : paella.player.pause();
        });
      }
    }, {
      key: "pause",
      value: function pause() {
        paella.player.pause();
      }
    }, {
      key: "mute",
      value: function mute() {
        var videoContainer = paella.player.videoContainer;

        if (videoContainer.muted) {
          videoContainer.unmute();
        } else {
          videoContainer.mute();
        } // videoContainer.volume().then(function(volume){
        // 	var newVolume = 0;
        // 	if (volume==0) { newVolume = 1.0; }
        // 	paella.player.videoContainer.setVolume({ master:newVolume, slave: 0});
        // });

      }
    }, {
      key: "volumeUp",
      value: function volumeUp() {
        var videoContainer = paella.player.videoContainer;
        videoContainer.volume().then(function (volume) {
          volume += 0.1;
          volume = volume > 1 ? 1.0 : volume;
          paella.player.videoContainer.setVolume(volume);
        });
      }
    }, {
      key: "volumeDown",
      value: function volumeDown() {
        var videoContainer = paella.player.videoContainer;
        videoContainer.volume().then(function (volume) {
          volume -= 0.1;
          volume = volume < 0 ? 0.0 : volume;
          paella.player.videoContainer.setVolume(volume);
        });
      }
    }]);

    return DefaultKeyPlugin;
  }(paella.KeyPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$VideoOverlayB) {
    _inherits(LegalPlugin, _paella$VideoOverlayB);

    var _super83 = _createSuper(LegalPlugin);

    function LegalPlugin() {
      _classCallCheck(this, LegalPlugin);

      return _super83.apply(this, arguments);
    }

    _createClass(LegalPlugin, [{
      key: "getIndex",
      value: function getIndex() {
        return 0;
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "legal";
      }
    }, {
      key: "getAlignment",
      value: function getAlignment() {
        return paella.player.config.plugins.list[this.getName()].position;
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return "";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        onSuccess(true);
      }
    }, {
      key: "setup",
      value: function setup() {
        var plugin = paella.player.config.plugins.list[this.getName()];
        var title = document.createElement('a');
        title.innerText = plugin.label;
        this._url = plugin.legalUrl;
        title.className = "";
        this.button.appendChild(title);
      }
    }, {
      key: "action",
      value: function action(button) {
        window.open(this._url);
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.legalPlugin";
      }
    }]);

    return LegalPlugin;
  }(paella.VideoOverlayButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$VideoOverlayB2) {
    _inherits(LiveStreamIndicator, _paella$VideoOverlayB2);

    var _super84 = _createSuper(LiveStreamIndicator);

    function LiveStreamIndicator() {
      _classCallCheck(this, LiveStreamIndicator);

      return _super84.apply(this, arguments);
    }

    _createClass(LiveStreamIndicator, [{
      key: "isEditorVisible",
      value: function isEditorVisible() {
        return paella.editor.instance != null;
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 10;
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "liveIndicator";
      }
    }, {
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("This video is a live stream");
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.liveStreamingIndicatorPlugin";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        onSuccess(paella.player.isLiveStream());
      }
    }, {
      key: "setup",
      value: function setup() {}
    }, {
      key: "action",
      value: function action(button) {
        paella.messageBox.showMessage(paella.utils.dictionary.translate("Live streaming mode: This is a live video, so, some capabilities of the player are disabled"));
      }
    }]);

    return LiveStreamIndicator;
  }(paella.VideoOverlayButtonPlugin);
});

(function () {
  var MpegDashVideo = /*#__PURE__*/function (_paella$Html5Video2) {
    _inherits(MpegDashVideo, _paella$Html5Video2);

    var _super85 = _createSuper(MpegDashVideo);

    function MpegDashVideo(id, stream, left, top, width, height) {
      var _this189;

      _classCallCheck(this, MpegDashVideo);

      _this189 = _super85.call(this, id, stream, left, top, width, height, 'mpd');
      _this189._posterFrame = null;
      _this189._player = null;
      return _this189;
    }

    _createClass(MpegDashVideo, [{
      key: "_loadDeps",
      value: function _loadDeps() {
        return new Promise(function (resolve, reject) {
          if (!window.$paella_mpd) {
            paella.require(paella.baseUrl + 'resources/deps/dash.all.js').then(function () {
              window.$paella_mpd = true;
              resolve(window.$paella_mpd);
            });
          } else {
            resolve(window.$paella_mpd);
          }
        });
      }
    }, {
      key: "_getQualityObject",
      value: function _getQualityObject(item, index, bitrates) {
        var total = bitrates.length;
        var percent = Math.round(index * 100 / total);
        var label = index == 0 ? "min" : index == total - 1 ? "max" : percent + "%";
        return {
          index: index,
          res: {
            w: null,
            h: null
          },
          bitrate: item.bitrate,
          src: null,
          toString: function toString() {
            return percent;
          },
          shortLabel: function shortLabel() {
            return label;
          },
          compare: function compare(q2) {
            return this.bitrate - q2.bitrate;
          }
        };
      }
    }, {
      key: "webGlDidLoad",
      value: function webGlDidLoad() {
        // Register a new video loader in the webgl engine, to enable the
        // hls compatibility in webgl canvas
        bg.utils.HTTPResourceProvider.AddVideoLoader('mpd', function (url, onSuccess, onFail) {
          var video = document.createElement("video");
          s_preventVideoDump.push(video); // this.setupHls(video,url)
          // 	.then(() => onSuccess(video))
          // 	.catch(() => onFail());
        });
        return Promise.resolve();
      }
    }, {
      key: "loadVideoStream",
      value: function loadVideoStream(canvasInstance, stream) {
        var _this190 = this;

        var This = this;
        return canvasInstance.loadVideo(this, stream, function (videoElem) {
          return new Promise(function (resolve, reject) {
            _this190._loadDeps().then(function () {
              var player = dashjs.MediaPlayer().create();
              player.initialize(videoElem, stream.src, true);
              player.getDebug().setLogToBrowserConsole(false);
              _this190._player = player;
              player.on(dashjs.MediaPlayer.events.STREAM_INITIALIZED, function (a, b) {
                var bitrates = player.getBitrateInfoListFor("video");

                This._deferredAction(function () {
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
      } // load() {
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

    }, {
      key: "supportAutoplay",
      value: function supportAutoplay() {
        return true;
      }
    }, {
      key: "getQualities",
      value: function getQualities() {
        var _this191 = this;

        return new Promise(function (resolve) {
          _this191._deferredAction(function () {
            if (!_this191._qualities) {
              _this191._qualities = [];

              _this191._player.getBitrateInfoListFor("video").sort(function (a, b) {
                return a.bitrate - b.bitrate;
              }).forEach(function (item, index, bitrates) {
                _this191._qualities.push(_this191._getQualityObject(item, index, bitrates));
              });

              _this191.autoQualityIndex = _this191._qualities.length;

              _this191._qualities.push({
                index: _this191.autoQualityIndex,
                res: {
                  w: null,
                  h: null
                },
                bitrate: -1,
                src: null,
                toString: function toString() {
                  return "auto";
                },
                shortLabel: function shortLabel() {
                  return "auto";
                },
                compare: function compare(q2) {
                  return this.bitrate - q2.bitrate;
                }
              });
            }

            resolve(_this191._qualities);
          });
        });
      }
    }, {
      key: "setQuality",
      value: function setQuality(index) {
        var _this192 = this;

        return new Promise(function (resolve, reject) {
          var currentQuality = _this192._player.getQualityFor("video");

          if (index == _this192.autoQualityIndex) {
            _this192._player.setAutoSwitchQuality(true);

            resolve();
          } else if (index != currentQuality) {
            _this192._player.setAutoSwitchQuality(false);

            _this192._player.off(dashjs.MediaPlayer.events.METRIC_CHANGED);

            _this192._player.on(dashjs.MediaPlayer.events.METRIC_CHANGED, function (a, b) {
              if (a.type == "metricchanged") {
                if (currentQuality != _this192._player.getQualityFor("video")) {
                  currentQuality = _this192._player.getQualityFor("video");
                  resolve();
                }
              }
            });

            _this192._player.setQualityFor("video", index);
          } else {
            resolve();
          }
        });
      }
    }, {
      key: "getCurrentQuality",
      value: function getCurrentQuality() {
        var _this193 = this;

        return new Promise(function (resolve, reject) {
          if (_this193._player.getAutoSwitchQuality()) {
            // auto quality
            resolve({
              index: _this193.autoQualityIndex,
              res: {
                w: null,
                h: null
              },
              bitrate: -1,
              src: null,
              toString: function toString() {
                return "auto";
              },
              shortLabel: function shortLabel() {
                return "auto";
              },
              compare: function compare(q2) {
                return this.bitrate - q2.bitrate;
              }
            });
          } else {
            var index = _this193._player.getQualityFor("video");

            resolve(_this193._getQualityObject(_this193._qualities[index], index, _this193._player.getBitrateInfoListFor("video")));
          }
        });
      }
    }, {
      key: "unFreeze",
      value: function unFreeze() {
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "freeze",
      value: function freeze() {
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "unload",
      value: function unload() {
        this._callUnloadEvent();

        return paella_DeferredNotImplemented();
      }
    }]);

    return MpegDashVideo;
  }(paella.Html5Video);

  paella.MpegDashVideo = MpegDashVideo;

  var MpegDashVideoFactory = /*#__PURE__*/function (_paella$VideoFactory4) {
    _inherits(MpegDashVideoFactory, _paella$VideoFactory4);

    var _super86 = _createSuper(MpegDashVideoFactory);

    function MpegDashVideoFactory() {
      _classCallCheck(this, MpegDashVideoFactory);

      return _super86.apply(this, arguments);
    }

    _createClass(MpegDashVideoFactory, [{
      key: "isStreamCompatible",
      value: function isStreamCompatible(streamData) {
        try {
          if (paella.utils.userAgent.system.iOS) {
            return false;
          }

          for (var key in streamData.sources) {
            if (key == 'mpd') return true;
          }
        } catch (e) {}

        return false;
      }
    }, {
      key: "getVideoObject",
      value: function getVideoObject(id, streamData, rect) {
        ++paella.videoFactories.Html5VideoFactory.s_instances;
        return new paella.MpegDashVideo(id, streamData, rect.x, rect.y, rect.w, rect.h);
      }
    }]);

    return MpegDashVideoFactory;
  }(paella.VideoFactory);

  paella.videoFactories.MpegDashVideoFactory = MpegDashVideoFactory;
})();

paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin11) {
    _inherits(MultipleQualitiesPlugin, _paella$ButtonPlugin11);

    var _super87 = _createSuper(MultipleQualitiesPlugin);

    function MultipleQualitiesPlugin() {
      _classCallCheck(this, MultipleQualitiesPlugin);

      return _super87.apply(this, arguments);
    }

    _createClass(MultipleQualitiesPlugin, [{
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "showMultipleQualitiesPlugin";
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-screen';
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 2030;
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.multipleQualitiesPlugin";
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Change video quality");
      }
    }, {
      key: "closeOnMouseOut",
      value: function closeOnMouseOut() {
        return true;
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        var _this194 = this;

        this._available = [];
        paella.player.videoContainer.getQualities().then(function (q) {
          _this194._available = q;
          onSuccess(q.length > 1);
        });
      }
    }, {
      key: "setup",
      value: function setup() {
        var _this195 = this;

        this.setQualityLabel();
        paella.events.bind(paella.events.qualityChanged, function (event) {
          return _this195.setQualityLabel();
        });
      }
    }, {
      key: "getButtonType",
      value: function getButtonType() {
        return paella.ButtonPlugin.type.menuButton;
      }
    }, {
      key: "getMenuContent",
      value: function getMenuContent() {
        var _this196 = this;

        var buttonItems = [];
        var minVisibleQuality = this.config.minVisibleQuality !== undefined ? this.config.minVisibleQuality : 100;

        this._available.forEach(function (q, index) {
          var resH = q.res && q.res.h || 0;

          if (resH >= minVisibleQuality || resH <= 0) {
            buttonItems.push({
              id: q.shortLabel(),
              title: q.shortLabel(),
              value: index,
              icon: "",
              className: _this196.getButtonItemClass(q.shortLabel()),
              "default": false
            });
          }
        });

        return buttonItems;
      }
    }, {
      key: "menuItemSelected",
      value: function menuItemSelected(itemData) {
        var _this197 = this;

        paella.player.videoContainer.setQuality(itemData.value).then(function () {
          paella.player.controls.hidePopUp(_this197.getName());

          _this197.setQualityLabel();
        });
      }
    }, {
      key: "setQualityLabel",
      value: function setQualityLabel() {
        var _this198 = this;

        paella.player.videoContainer.getCurrentQuality().then(function (q) {
          _this198.setText(q.shortLabel());
        });
      }
    }, {
      key: "getButtonItemClass",
      value: function getButtonItemClass(profileName) {
        return 'multipleQualityItem ' + profileName;
      }
    }]);

    return MultipleQualitiesPlugin;
  }(paella.ButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin12) {
    _inherits(PIPModePlugin, _paella$ButtonPlugin12);

    var _super88 = _createSuper(PIPModePlugin);

    function PIPModePlugin() {
      _classCallCheck(this, PIPModePlugin);

      return _super88.apply(this, arguments);
    }

    _createClass(PIPModePlugin, [{
      key: "getIndex",
      value: function getIndex() {
        return 551;
      }
    }, {
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "PIPModeButton";
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-pip';
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.pipModePlugin";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        var mainVideo = paella.player.videoContainer.masterVideo();
        var video = mainVideo.video; // PIP is only available with single stream videos

        if (paella.player.videoContainer.streamProvider.videoStreams.length != 1) {
          onSuccess(false);
        } else if (video && video.webkitSetPresentationMode) {
          onSuccess(true);
        } else if (video && 'pictureInPictureEnabled' in document) {
          onSuccess(true);
        } else {
          onSuccess(false);
        }
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Set picture-in-picture mode.");
      }
    }, {
      key: "setup",
      value: function setup() {}
    }, {
      key: "action",
      value: function action(button) {
        var video = paella.player.videoContainer.masterVideo().video;

        if (video.webkitSetPresentationMode) {
          if (video.webkitPresentationMode == "picture-in-picture") {
            video.webkitSetPresentationMode("inline");
          } else {
            video.webkitSetPresentationMode("picture-in-picture");
          }
        } else if ('pictureInPictureEnabled' in document) {
          if (video !== document.pictureInPictureElement) {
            video.requestPictureInPicture();
          } else {
            document.exitPictureInPicture();
          }
        }
      }
    }]);

    return PIPModePlugin;
  }(paella.ButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin13) {
    _inherits(PlayPauseButtonPlugin, _paella$ButtonPlugin13);

    var _super89 = _createSuper(PlayPauseButtonPlugin);

    function PlayPauseButtonPlugin() {
      var _this199;

      _classCallCheck(this, PlayPauseButtonPlugin);

      _this199 = _super89.call(this);
      _this199.playIconClass = 'icon-play';
      _this199.replayIconClass = 'icon-loop2';
      _this199.pauseIconClass = 'icon-pause';
      _this199.playSubclass = 'playButton';
      _this199.pauseSubclass = 'pauseButton';
      return _this199;
    }

    _createClass(PlayPauseButtonPlugin, [{
      key: "getAlignment",
      value: function getAlignment() {
        return 'left';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return this.playSubclass;
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return this.playIconClass;
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.playPauseButtonPlugin";
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Play");
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 110;
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        onSuccess(true);
      }
    }, {
      key: "setup",
      value: function setup() {
        var _this200 = this;

        if (paella.player.playing()) {
          this.changeIconClass(this.playIconClass);
        }

        paella.events.bind(paella.events.play, function (event) {
          _this200.changeIconClass(_this200.pauseIconClass);

          _this200.changeSubclass(_this200.pauseSubclass);

          _this200.setToolTip(paella.utils.dictionary.translate("Pause"));
        });
        paella.events.bind(paella.events.pause, function (event) {
          _this200.changeIconClass(_this200.playIconClass);

          _this200.changeSubclass(_this200.playSubclass);

          _this200.setToolTip(paella.utils.dictionary.translate("Play"));
        });
        paella.events.bind(paella.events.ended, function (event) {
          _this200.changeIconClass(_this200.replayIconClass);

          _this200.changeSubclass(_this200.playSubclass);

          _this200.setToolTip(paella.utils.dictionary.translate("Play"));
        });
      }
    }, {
      key: "action",
      value: function action(button) {
        paella.player.videoContainer.paused().then(function (paused) {
          if (paused) {
            paella.player.play();
          } else {
            paella.player.pause();
          }
        });
      }
    }]);

    return PlayPauseButtonPlugin;
  }(paella.ButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$EventDrivenPl10) {
    _inherits(PlayButtonOnScreen, _paella$EventDrivenPl10);

    var _super90 = _createSuper(PlayButtonOnScreen);

    function PlayButtonOnScreen() {
      var _this201;

      _classCallCheck(this, PlayButtonOnScreen);

      _this201 = _super90.call(this);
      _this201.containerId = 'paella_plugin_PlayButtonOnScreen';
      _this201.container = null;
      _this201.enabled = true;
      _this201.isPlaying = false;
      _this201.showIcon = true;
      _this201.firstPlay = false;
      return _this201;
    }

    _createClass(PlayButtonOnScreen, [{
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        var _this202 = this;

        this.showOnEnd = true;
        paella.data.read('relatedVideos', {
          id: paella.player.videoIdentifier
        }, function (data) {
          _this202.showOnEnd = !Array.isArray(data) || data.length == 0;
        });
        onSuccess(true);
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 1010;
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.playButtonOnScreenPlugin";
      }
    }, {
      key: "setup",
      value: function setup() {
        var _this203 = this;

        this.container = paella.LazyThumbnailContainer.GetIconElement();
        paella.player.videoContainer.domElement.appendChild(this.container);
        $(this.container).click(function () {
          return _this203.onPlayButtonClick();
        });
      }
    }, {
      key: "getEvents",
      value: function getEvents() {
        return [paella.events.ended, paella.events.endVideo, paella.events.play, paella.events.pause, paella.events.showEditor, paella.events.hideEditor];
      }
    }, {
      key: "onEvent",
      value: function onEvent(eventType, params) {
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
    }, {
      key: "onPlayButtonClick",
      value: function onPlayButtonClick() {
        this.firstPlay = true;
        this.checkStatus();
      }
    }, {
      key: "endVideo",
      value: function endVideo() {
        var _this204 = this;

        paella.player.videoContainer.ended().then(function (ended) {
          if (ended) {
            _this204.isPlaying = false;
            _this204.showIcon = _this204.showOnEnd;

            _this204.checkStatus();
          } else {
            base.log.debug("BTN ON SCREEN: The player is no longer in ended state.");
          }
        });
      }
    }, {
      key: "play",
      value: function play() {
        this.isPlaying = true;
        this.showIcon = false;

        if (!/dimmed/.test(this.container.className)) {
          this.container.className += " dimmed";
        }

        this.checkStatus();
      }
    }, {
      key: "pause",
      value: function pause() {
        this.isPlaying = false;
        this.showIcon = this.config.showOnPause;
        this.checkStatus();
      }
    }, {
      key: "showEditor",
      value: function showEditor() {
        this.enabled = false;
        this.checkStatus();
      }
    }, {
      key: "hideEditor",
      value: function hideEditor() {
        this.enabled = true;
        this.checkStatus();
      }
    }, {
      key: "checkStatus",
      value: function checkStatus() {
        if (this.enabled && this.isPlaying || !this.enabled || !this.showIcon) {
          $(this.container).hide();
        } // Only show play button if none of the video players require mouse events
        else if (!paella.player.videoContainer.streamProvider.videoPlayers.every(function (p) {
            return p.canvasData.mouseEventsSupport;
          })) {
            $(this.container).show();
          }
      }
    }]);

    return PlayButtonOnScreen;
  }(paella.EventDrivenPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin14) {
    _inherits(PlaybackRate, _paella$ButtonPlugin14);

    var _super91 = _createSuper(PlaybackRate);

    function PlaybackRate() {
      _classCallCheck(this, PlaybackRate);

      return _super91.apply(this, arguments);
    }

    _createClass(PlaybackRate, [{
      key: "getAlignment",
      value: function getAlignment() {
        return 'left';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "showPlaybackRateButton";
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-screen';
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 140;
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.playbackRatePlugin";
      }
    }, {
      key: "getButtonType",
      value: function getButtonType() {
        return paella.ButtonPlugin.type.menuButton;
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Set playback rate");
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        this.buttonItems = null;
        this.buttons = [];
        this.selected_button = null;
        this.defaultRate = null;
        this._domElement = null;
        this.available_rates = null;
        var enabled = paella.player.videoContainer.masterVideo() instanceof paella.Html5Video;
        onSuccess(enabled && !paella.player.videoContainer.streamProvider.isLiveStreaming);
      }
    }, {
      key: "closeOnMouseOut",
      value: function closeOnMouseOut() {
        return true;
      }
    }, {
      key: "setup",
      value: function setup() {
        this.defaultRate = 1.0;
        this.available_rates = this.config.availableRates || [0.75, 1, 1.25, 1.5];
      }
    }, {
      key: "getMenuContent",
      value: function getMenuContent() {
        var _this205 = this;

        var buttonItems = [];
        this.available_rates.forEach(function (rate) {
          var profileName = rate + "x";
          buttonItems.push({
            id: profileName,
            title: profileName,
            value: rate,
            icon: "",
            className: _this205.getButtonItemClass(profileName),
            "default": rate == 1.0
          });
        });
        return buttonItems;
      }
    }, {
      key: "menuItemSelected",
      value: function menuItemSelected(itemData) {
        paella.player.videoContainer.setPlaybackRate(itemData.value);
        this.setText(itemData.title);
        paella.player.controls.hidePopUp(this.getName());
      }
    }, {
      key: "getText",
      value: function getText() {
        return "1x";
      }
    }, {
      key: "getButtonItemClass",
      value: function getButtonItemClass(profileName, selected) {
        return 'playbackRateItem ' + profileName + (selected ? ' selected' : '');
      }
    }]);

    return PlaybackRate;
  }(paella.ButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin15) {
    _inherits(RatePlugin, _paella$ButtonPlugin15);

    var _super92 = _createSuper(RatePlugin);

    function RatePlugin() {
      _classCallCheck(this, RatePlugin);

      return _super92.apply(this, arguments);
    }

    _createClass(RatePlugin, [{
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "rateButtonPlugin";
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-star';
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 540;
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.ratePlugin";
      }
    }, {
      key: "getButtonType",
      value: function getButtonType() {
        return paella.ButtonPlugin.type.popUpButton;
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Rate this video");
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        var _this206 = this;

        this.buttonItems = null;
        this.buttons = [];
        this.selected_button = null;
        this.score = 0;
        this.count = 0;
        this.myScore = 0;
        this.canVote = false;
        this.scoreContainer = {
          header: null,
          rateButtons: null
        };
        paella.data.read('rate', {
          id: paella.initDelegate.getId()
        }, function (data, status) {
          if (data && _typeof(data) == 'object') {
            _this206.score = Number(data.mean).toFixed(1);
            _this206.count = data.count;
            _this206.myScore = data.myScore;
            _this206.canVote = data.canVote;
          }

          onSuccess(status);
        });
      }
    }, {
      key: "setup",
      value: function setup() {}
    }, {
      key: "setScore",
      value: function setScore(s) {
        this.score = s;
        this.updateScore();
      }
    }, {
      key: "closeOnMouseOut",
      value: function closeOnMouseOut() {
        return true;
      }
    }, {
      key: "updateHeader",
      value: function updateHeader() {
        var score = paella.utils.dictionary.translate("Not rated");

        if (this.count > 0) {
          score = '<i class="glyphicon glyphicon-star"></i>';
          score += " ".concat(this.score, " ").concat(this.count, " ").concat(paella.utils.dictionary.translate('votes'));
        }

        this.scoreContainer.header.innerHTML = "\n\t\t\t<div>\n\t\t\t\t<h4>".concat(paella.utils.dictionary.translate('Video score'), ":</h4>\n\t\t\t\t<h5>\n\t\t\t\t\t").concat(score, "\n\t\t\t\t</h5>\n\t\t\t\t</h4>\n\t\t\t\t<h4>").concat(paella.utils.dictionary.translate('Vote:'), "</h4>\n\t\t\t</div>\n\t\t\t");
      }
    }, {
      key: "updateRateButtons",
      value: function updateRateButtons() {
        this.scoreContainer.rateButtons.className = "rateButtons";
        this.buttons = [];

        if (this.canVote) {
          this.scoreContainer.rateButtons.innerText = "";

          for (var i = 0; i < 5; ++i) {
            var btn = this.getStarButton(i + 1);
            this.buttons.push(btn);
            this.scoreContainer.rateButtons.appendChild(btn);
          }
        } else {
          this.scoreContainer.rateButtons.innerHTML = "<h5>".concat(paella.utils.dictionary.translate('Login to vote'), "</h5>");
        }

        this.updateVote();
      }
    }, {
      key: "buildContent",
      value: function buildContent(domElement) {
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
    }, {
      key: "getStarButton",
      value: function getStarButton(score) {
        var This = this;
        var elem = document.createElement('i');
        elem.data = {
          score: score,
          active: false
        };
        elem.className = "starButton glyphicon glyphicon-star-empty";
        $(elem).click(function (event) {
          This.vote(this.data.score);
        });
        return elem;
      }
    }, {
      key: "vote",
      value: function vote(score) {
        var _this207 = this;

        this.myScore = score;
        var data = {
          mean: this.score,
          count: this.count,
          myScore: score,
          canVote: this.canVote
        };
        paella.data.write('rate', {
          id: paella.initDelegate.getId()
        }, data, function (result) {
          paella.data.read('rate', {
            id: paella.initDelegate.getId()
          }, function (data, status) {
            if (data && _typeof(data) == 'object') {
              _this207.score = Number(data.mean).toFixed(1);
              _this207.count = data.count;
              _this207.myScore = data.myScore;
              _this207.canVote = data.canVote;
            }

            _this207.updateHeader();

            _this207.updateRateButtons();
          });
        });
      }
    }, {
      key: "updateVote",
      value: function updateVote() {
        var _this208 = this;

        this.buttons.forEach(function (item, index) {
          item.className = index < _this208.myScore ? "starButton glyphicon glyphicon-star" : "starButton glyphicon glyphicon-star-empty";
        });
      }
    }]);

    return RatePlugin;
  }(paella.ButtonPlugin);
}); // Change this data delegate to read the related videos form an external source
// Default behaviour is to get the related videos from the data.json file

paella.addDataDelegate("relatedVideos", function () {
  return /*#__PURE__*/function (_paella$DataDelegate2) {
    _inherits(RelatedVideoDataDelegate, _paella$DataDelegate2);

    var _super93 = _createSuper(RelatedVideoDataDelegate);

    function RelatedVideoDataDelegate() {
      _classCallCheck(this, RelatedVideoDataDelegate);

      return _super93.apply(this, arguments);
    }

    _createClass(RelatedVideoDataDelegate, [{
      key: "read",
      value: function read(context, params, onSuccess) {
        var videoMetadata = paella.player.videoLoader.getMetadata();

        if (videoMetadata.related) {
          onSuccess(videoMetadata.related);
        }
      }
    }]);

    return RelatedVideoDataDelegate;
  }(paella.DataDelegate);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$EventDrivenPl11) {
    _inherits(RelatedVideoPlugin, _paella$EventDrivenPl11);

    var _super94 = _createSuper(RelatedVideoPlugin);

    function RelatedVideoPlugin() {
      _classCallCheck(this, RelatedVideoPlugin);

      return _super94.apply(this, arguments);
    }

    _createClass(RelatedVideoPlugin, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.relatedVideosPlugin";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        var _this209 = this;

        paella.data.read('relatedVideos', {
          id: paella.player.videoIdentifier
        }, function (data) {
          _this209._relatedVideos = data;
          onSuccess(Array.isArray(_this209._relatedVideos) && _this209._relatedVideos.length > 0);
        });
      }
    }, {
      key: "setup",
      value: function setup() {}
    }, {
      key: "getEvents",
      value: function getEvents() {
        return [paella.events.ended, paella.events.timeUpdate, paella.events.play, paella.events.seekTo, paella.events.seekToTime];
      }
    }, {
      key: "onEvent",
      value: function onEvent(eventType, params) {
        if (eventType == paella.events.ended) {
          this.showRelatedVideos();
        } else {
          this.hideRelatedVideos();
        }
      }
    }, {
      key: "showRelatedVideos",
      value: function showRelatedVideos() {
        this.hideRelatedVideos();
        var container = document.createElement('div');
        container.className = "related-video-container";

        function getRelatedVideoLink(data, className) {
          var linkContainer = document.createElement("a");
          linkContainer.className = "related-video-link " + className;
          linkContainer.innerHTML = "\n                <img src=\"".concat(data.thumb, "\" alt=\"\">\n                <p>").concat(data.title, "</p>\n                ");
          linkContainer.addEventListener("click", function () {
            try {
              if (window.self !== window.top) {
                window.parent.document.dispatchEvent(new CustomEvent('paella-change-video', {
                  detail: data
                }));
              }
            } catch (e) {}

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
            container.appendChild(getRelatedVideoLink(this._relatedVideos[0], 'related-video-single'));
            break;

          case 2:
          default:
            container.appendChild(getRelatedVideoLink(this._relatedVideos[0], 'related-video-dual-1'));
            container.appendChild(getRelatedVideoLink(this._relatedVideos[1], 'related-video-dual-2'));
            break;
        }

        paella.player.videoContainer.attenuationEnabled = true;
      }
    }, {
      key: "hideRelatedVideos",
      value: function hideRelatedVideos() {
        if (this._messageContainer) {
          paella.player.videoContainer.overlayContainer.removeElement(this._messageContainer);
          this._messageContainer = null;
          paella.player.videoContainer.attenuationEnabled = false;
        }
      }
    }]);

    return RelatedVideoPlugin;
  }(paella.EventDrivenPlugin);
});

(function () {
  var RTMPVideo = /*#__PURE__*/function (_paella$VideoElementB5) {
    _inherits(RTMPVideo, _paella$VideoElementB5);

    var _super95 = _createSuper(RTMPVideo);

    function RTMPVideo(id, stream, left, top, width, height) {
      var _this210;

      _classCallCheck(this, RTMPVideo);

      _this210 = _super95.call(this, id, stream, 'div', left, top, width, height);
      _this210._posterFrame = null;
      _this210._currentQuality = null;
      _this210._duration = 0;
      _this210._paused = true;
      _this210._streamName = null;
      _this210._flashId = null;
      _this210._swfContainer = null;
      _this210._flashVideo = null;
      _this210._volume = 1;
      _this210._flashId = id + 'Movie';
      _this210._streamName = 'rtmp';

      var This = _assertThisInitialized(_this210);

      _this210._stream.sources.rtmp.sort(function (a, b) {
        return a.res.h - b.res.h;
      });

      var processEvent = function processEvent(eventName, params) {
        if (eventName != "loadedmetadata" && eventName != "pause" && !This._isReady) {
          This._isReady = true;
          This._duration = params.duration;
          $(This.swfContainer).trigger("paella:flashvideoready");
        }

        if (eventName == "progress") {
          try {
            This.flashVideo.setVolume(This._volume);
          } catch (e) {}

          paella.log.debug("Flash video event: " + eventName + ", progress: " + This.flashVideo.currentProgress());
        } else if (eventName == "ended") {
          paella.log.debug("Flash video event: " + eventName);
          paella.events.trigger(paella.events.pause);
          paella.player.controls.showControls();
        } else {
          paella.log.debug("Flash video event: " + eventName);
        }
      };

      var eventReceived = function eventReceived(eventName, params) {
        params = params.split(",");
        var processedParams = {};

        for (var i = 0; i < params.length; ++i) {
          var splitted = params[i].split(":");
          var key = splitted[0];
          var value = splitted[1];

          if (value == "NaN") {
            value = NaN;
          } else if (/^true$/i.test(value)) {
            value = true;
          } else if (/^false$/i.test(value)) {
            value = false;
          } else if (!isNaN(parseFloat(value))) {
            value = parseFloat(value);
          }

          processedParams[key] = value;
        }

        processEvent(eventName, processedParams);
      };

      paella.events.bind(paella.events.flashVideoEvent, function (event, params) {
        if (This.flashId == params.source) {
          eventReceived(params.eventName, params.values);
        }
      });
      return _this210;
    }

    _createClass(RTMPVideo, [{
      key: "_createSwfObject",
      value: function _createSwfObject(swfFile, flashVars) {
        var id = this.identifier;
        var parameters = {
          wmode: 'transparent'
        };
        var domElement = document.createElement('div');
        this.domElement.appendChild(domElement);
        domElement.id = id + "Movie";
        this._swfContainer = domElement;

        if (swfobject.hasFlashPlayerVersion("9.0.0")) {
          swfobject.embedSWF(swfFile, domElement.id, "100%", "100%", "9.0.0", "", flashVars, parameters, null, function callbackFn(e) {
            if (e.success == false) {
              var message = document.createElement('div');
              var header = document.createElement('h3');
              header.innerText = paella.utils.dictionary.translate("Flash player problem");
              var text = document.createElement('div');
              text.innerHTML = paella.utils.dictionary.translate("A problem occurred trying to load flash player.") + "<br>" + paella.utils.dictionary.translate("Please go to {0} and install it.").replace("{0}", "<a style='color: #800000; text-decoration: underline;' href='http://www.adobe.com/go/getflash'>http://www.adobe.com/go/getflash</a>") + '<br>' + paella.utils.dictionary.translate("If the problem presist, contact us.");
              var link = document.createElement('a');
              link.setAttribute("href", "http://www.adobe.com/go/getflash");
              link.innerHTML = '<img style="margin:5px;" src="http://www.adobe.com/images/shared/download_buttons/get_flash_player.gif" alt="Obtener Adobe Flash Player" />';
              message.appendChild(header);
              message.appendChild(text);
              message.appendChild(link);
              paella.messageBox.showError(message.innerHTML);
            }
          });
        } else {
          var message = document.createElement('div');
          var header = document.createElement('h3');
          header.innerText = paella.utils.dictionary.translate("Flash player needed");
          var text = document.createElement('div');
          text.innerHTML = paella.utils.dictionary.translate("You need at least Flash player 9 installed.") + "<br>" + paella.utils.dictionary.translate("Please go to {0} and install it.").replace("{0}", "<a style='color: #800000; text-decoration: underline;' href='http://www.adobe.com/go/getflash'>http://www.adobe.com/go/getflash</a>");
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
    }, {
      key: "_deferredAction",
      value: function _deferredAction(action) {
        var _this211 = this;

        return new Promise(function (resolve, reject) {
          if (_this211.ready) {
            resolve(action());
          } else {
            $(_this211.swfContainer).bind('paella:flashvideoready', function () {
              _this211._ready = true;
              resolve(action());
            });
          }
        });
      }
    }, {
      key: "_getQualityObject",
      value: function _getQualityObject(index, s) {
        return {
          index: index,
          res: s.res,
          src: s.src,
          toString: function toString() {
            return this.res.w + "x" + this.res.h;
          },
          shortLabel: function shortLabel() {
            return this.res.h + "p";
          },
          compare: function compare(q2) {
            return this.res.w * this.res.h - q2.res.w * q2.res.h;
          }
        };
      } // Initialization functions

    }, {
      key: "getVideoData",
      value: function getVideoData() {
        var _this212 = this;

        var FlashVideoPlugin = this;
        return new Promise(function (resolve, reject) {
          _this212._deferredAction(function () {
            var videoData = {
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
    }, {
      key: "setPosterFrame",
      value: function setPosterFrame(url) {
        if (this._posterFrame == null) {
          this._posterFrame = url;
          var posterFrame = document.createElement('img');
          posterFrame.src = url;
          posterFrame.className = "videoPosterFrameImage";
          posterFrame.alt = "poster frame";
          this.domElement.appendChild(posterFrame);
          this._posterFrameElement = posterFrame;
        } //	this.video.setAttribute("poster",url);

      }
    }, {
      key: "setAutoplay",
      value: function setAutoplay(auto) {
        this._autoplay = auto;
      }
    }, {
      key: "load",
      value: function load() {
        var This = this;
        var sources = this._stream.sources.rtmp;

        if (this._currentQuality === null && this._videoQualityStrategy) {
          this._currentQuality = this._videoQualityStrategy.getQualityIndex(sources);
        }

        var isValid = function isValid(stream) {
          return stream.src && _typeof(stream.src) == 'object' && stream.src.server && stream.src.stream;
        };

        var stream = this._currentQuality < sources.length ? sources[this._currentQuality] : null;

        if (stream) {
          if (!isValid(stream)) {
            return paella_DeferredRejected(new Error("Invalid video data"));
          } else {
            var subscription = false;

            if (stream.src.requiresSubscription === undefined && paella.player.config.player.rtmpSettings) {
              subscription = paella.player.config.player.rtmpSettings.requiresSubscription || false;
            } else if (stream.src.requiresSubscription) {
              subscription = stream.src.requiresSubscription;
            }

            var parameters = {};
            var swfName = 'resources/deps/player_streaming.swf';

            if (this._autoplay) {
              parameters.autoplay = this._autoplay;
            }

            if (paella.utils.parameters.get('debug') == "true") {
              parameters.debugMode = true;
            }

            parameters.playerId = this.flashId;
            parameters.isLiveStream = stream.isLiveStream !== undefined ? stream.isLiveStream : false;
            parameters.server = stream.src.server;
            parameters.stream = stream.src.stream;
            parameters.subscribe = subscription;

            if (paella.player.config.player.rtmpSettings && paella.player.config.player.rtmpSettings.bufferTime !== undefined) {
              parameters.bufferTime = paella.player.config.player.rtmpSettings.bufferTime;
            }

            this._flashVideo = this._createSwfObject(swfName, parameters);
            $(this.swfContainer).trigger("paella:flashvideoready");
            return this._deferredAction(function () {
              return stream;
            });
          }
        } else {
          return paella_DeferredRejected(new Error("Could not load video: invalid quality stream index"));
        }
      }
    }, {
      key: "getQualities",
      value: function getQualities() {
        var _this213 = this;

        return new Promise(function (resolve, reject) {
          setTimeout(function () {
            var result = [];
            var sources = _this213._stream.sources.rtmp;
            var index = -1;
            sources.forEach(function (s) {
              index++;
              result.push(_this213._getQualityObject(index, s));
            });
            resolve(result);
          }, 50);
        });
      }
    }, {
      key: "setQuality",
      value: function setQuality(index) {
        var _this214 = this;

        index = index !== undefined && index !== null ? index : 0;
        return new Promise(function (resolve, reject) {
          var paused = _this214._paused;
          var sources = _this214._stream.sources.rtmp;
          _this214._currentQuality = index < sources.length ? index : 0;
          var source = sources[index];
          _this214._ready = false;
          _this214._isReady = false;

          _this214.load().then(function () {
            resolve();
          });
        });
      }
    }, {
      key: "getCurrentQuality",
      value: function getCurrentQuality() {
        var _this215 = this;

        return new Promise(function (resolve, reject) {
          resolve(_this215._getQualityObject(_this215._currentQuality, _this215._stream.sources.rtmp[_this215._currentQuality]));
        });
      }
    }, {
      key: "play",
      value: function play() {
        var This = this;
        return this._deferredAction(function () {
          if (This._posterFrameElement) {
            This._posterFrameElement.parentNode.removeChild(This._posterFrameElement);

            This._posterFrameElement = null;
          }

          This._paused = false;
          This.flashVideo.play();
        });
      }
    }, {
      key: "pause",
      value: function pause() {
        var This = this;
        return this._deferredAction(function () {
          This._paused = true;
          This.flashVideo.pause();
        });
      }
    }, {
      key: "isPaused",
      value: function isPaused() {
        var This = this;
        return this._deferredAction(function () {
          return This._paused;
        });
      }
    }, {
      key: "duration",
      value: function duration() {
        var This = this;
        return this._deferredAction(function () {
          return This.flashVideo.duration();
        });
      }
    }, {
      key: "setCurrentTime",
      value: function setCurrentTime(time) {
        var This = this;
        return this._deferredAction(function () {
          var duration = This.flashVideo.duration();
          This.flashVideo.seekTo(time * 100 / duration);
        });
      }
    }, {
      key: "currentTime",
      value: function currentTime() {
        var This = this;
        return this._deferredAction(function () {
          return This.flashVideo.getCurrentTime();
        });
      }
    }, {
      key: "setVolume",
      value: function setVolume(volume) {
        var This = this;
        this._volume = volume;
        return this._deferredAction(function () {
          This.flashVideo.setVolume(volume);
        });
      }
    }, {
      key: "volume",
      value: function volume() {
        var This = this;
        return this._deferredAction(function () {
          return This.flashVideo.getVolume();
        });
      }
    }, {
      key: "setPlaybackRate",
      value: function setPlaybackRate(rate) {
        var This = this;
        return this._deferredAction(function () {
          This._playbackRate = rate;
        });
      }
    }, {
      key: "playbackRate",
      value: function playbackRate() {
        var This = this;
        return this._deferredAction(function () {
          return This._playbackRate;
        });
      }
    }, {
      key: "goFullScreen",
      value: function goFullScreen() {
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "unFreeze",
      value: function unFreeze() {
        return this._deferredAction(function () {});
      }
    }, {
      key: "freeze",
      value: function freeze() {
        return this._deferredAction(function () {});
      }
    }, {
      key: "unload",
      value: function unload() {
        this._callUnloadEvent();

        return paella_DeferredNotImplemented();
      }
    }, {
      key: "getDimensions",
      value: function getDimensions() {
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "swfContainer",
      get: function get() {
        return this._swfContainer;
      }
    }, {
      key: "flashId",
      get: function get() {
        return this._flashId;
      }
    }, {
      key: "flashVideo",
      get: function get() {
        return this._flashVideo;
      }
    }]);

    return RTMPVideo;
  }(paella.VideoElementBase);

  paella.RTMPVideo = RTMPVideo;

  var RTMPVideoFactory = /*#__PURE__*/function (_paella$VideoFactory5) {
    _inherits(RTMPVideoFactory, _paella$VideoFactory5);

    var _super96 = _createSuper(RTMPVideoFactory);

    function RTMPVideoFactory() {
      _classCallCheck(this, RTMPVideoFactory);

      return _super96.apply(this, arguments);
    }

    _createClass(RTMPVideoFactory, [{
      key: "isStreamCompatible",
      value: function isStreamCompatible(streamData) {
        try {
          if (paella.utils.userAgent.system.iOS || paella.utils.userAgent.system.Android) {
            return false;
          }

          for (var key in streamData.sources) {
            if (key == 'rtmp') return true;
          }
        } catch (e) {}

        return false;
      }
    }, {
      key: "getVideoObject",
      value: function getVideoObject(id, streamData, rect) {
        return new paella.RTMPVideo(id, streamData, rect.x, rect.y, rect.w, rect.h);
      }
    }]);

    return RTMPVideoFactory;
  }(paella.VideoFactory);

  paella.videoFactories.RTMPVideoFactory = RTMPVideoFactory;
})(); /////////////////////////////////////////////////
// Caption Search
/////////////////////////////////////////////////


paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$SearchService2) {
    _inherits(CaptionsSearchPlugIn, _paella$SearchService2);

    var _super97 = _createSuper(CaptionsSearchPlugIn);

    function CaptionsSearchPlugIn() {
      _classCallCheck(this, CaptionsSearchPlugIn);

      return _super97.apply(this, arguments);
    }

    _createClass(CaptionsSearchPlugIn, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.search.captionsSearchPlugin";
      }
    }, {
      key: "search",
      value: function search(text, next) {
        paella.captions.search(text, next);
      }
    }]);

    return CaptionsSearchPlugIn;
  }(paella.SearchServicePlugIn);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin16) {
    _inherits(SearchPlugin, _paella$ButtonPlugin16);

    var _super98 = _createSuper(SearchPlugin);

    function SearchPlugin() {
      _classCallCheck(this, SearchPlugin);

      return _super98.apply(this, arguments);
    }

    _createClass(SearchPlugin, [{
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return 'searchButton';
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-binoculars';
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.searchPlugin";
      }
    }, {
      key: "getButtonType",
      value: function getButtonType() {
        return paella.ButtonPlugin.type.popUpButton;
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Search");
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 510;
      }
    }, {
      key: "closeOnMouseOut",
      value: function closeOnMouseOut() {
        return true;
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
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
    }, {
      key: "setup",
      value: function setup() {
        var self = this;
        $('.searchButton').click(function (event) {
          if (self._open) {
            self._open = false;
          } else {
            self._open = true;
            setTimeout(function () {
              $("#searchBarInput").focus();
            }, 0);
          }
        }); //GET THE FRAME LIST

        self._localImages = paella.initDelegate.initParams.videoLoader.frameList; //config

        self._colorSearch = self.config.colorSearch || false;
        self._sortDefault = self.config.sortType || "time";
        paella.events.bind(paella.events.controlBarWillHide, function (evt) {
          if (self._open) paella.player.controls.cancelHideBar();
        });
        paella.player.videoContainer.trimming().then(function (trimData) {
          self._trimming = trimData;
        });
      }
    }, {
      key: "prettyTime",
      value: function prettyTime(seconds) {
        // TIME FORMAT
        var hou = Math.floor(seconds / 3600) % 24;
        hou = ("00" + hou).slice(hou.toString().length);
        var min = Math.floor(seconds / 60) % 60;
        min = ("00" + min).slice(min.toString().length);
        var sec = Math.floor(seconds % 60);
        sec = ("00" + sec).slice(sec.toString().length);
        var timestr = hou + ":" + min + ":" + sec;
        return timestr;
      }
    }, {
      key: "search",
      value: function search(text, cb) {
        paella.searchService.search(text, cb);
      }
    }, {
      key: "getPreviewImage",
      value: function getPreviewImage(time) {
        var thisClass = this;
        var keys = Object.keys(thisClass._localImages);
        keys.push(time);
        keys.sort(function (a, b) {
          return parseInt(a) - parseInt(b);
        });
        var n = keys.indexOf(time) - 1;
        n = n > 0 ? n : 0;
        var i = keys[n];
        i = parseInt(i);
        return thisClass._localImages[i].url;
      }
    }, {
      key: "createLoadingElement",
      value: function createLoadingElement(parent) {
        var loadingResults = document.createElement('div');
        loadingResults.className = "loader";
        var htmlLoader = "<svg version=\"1.1\" id=\"loader-1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" width=\"40px\" height=\"40px\" viewBox=\"0 0 50 50\" style=\"enable-background:new 0 0 50 50;\" xml:space=\"preserve\">" + "<path fill=\"#000\" d=\"M25.251,6.461c-10.318,0-18.683,8.365-18.683,18.683h4.068c0-8.071,6.543-14.615,14.615-14.615V6.461z\">" + "<animateTransform attributeType=\"xml\"" + "attributeName=\"transform\"" + "type=\"rotate\"" + "from=\"0 25 25\"" + "to=\"360 25 25\"" + "dur=\"0.6s\"" + "repeatCount=\"indefinite\"/>" + "</path>" + "</svg>";
        loadingResults.innerHTML = htmlLoader;
        parent.appendChild(loadingResults);
        var sBodyText = document.createElement('p');
        sBodyText.className = 'sBodyText';
        sBodyText.innerText = paella.utils.dictionary.translate("Searching") + "...";
        parent.appendChild(sBodyText);
      }
    }, {
      key: "createNotResultsFound",
      value: function createNotResultsFound(parent) {
        var noResults = document.createElement('div');
        noResults.className = "noResults";
        noResults.innerText = paella.utils.dictionary.translate("Sorry! No results found.");
        parent.appendChild(noResults);
      }
    }, {
      key: "doSearch",
      value: function doSearch(txt, searchBody) {
        var thisClass = this;
        $(searchBody).empty(); //LOADING CONTAINER

        thisClass.createLoadingElement(searchBody);
        thisClass.search(txt, function (err, results) {
          $(searchBody).empty(); //BUILD SEARCH RESULTS

          if (!err) {
            if (results.length == 0) {
              // 0 RESULTS FOUND
              thisClass.createNotResultsFound(searchBody);
            } else {
              for (var i = 0; i < results.length; i++) {
                // FILL THE BODY CONTAINER WITH RESULTS
                if (thisClass._trimming.enabled && results[i].time <= thisClass._trimming.start) {
                  continue;
                } //SEARCH SORT TYPE (TIME oR SCoRE)


                if (thisClass._sortDefault == 'score') {
                  results.sort(function (a, b) {
                    return b.score - a.score;
                  });
                }

                if (thisClass._sortDefault == 'time') {
                  results.sort(function (a, b) {
                    return a.time - b.time;
                  });
                }

                var sBodyInnerContainer = document.createElement('div');
                sBodyInnerContainer.className = 'sBodyInnerContainer'; //COLOR

                if (thisClass._colorSearch) {
                  if (results[i].score <= 0.3) {
                    $(sBodyInnerContainer).addClass('redScore');
                  }

                  if (results[i].score >= 0.7) {
                    $(sBodyInnerContainer).addClass('greenScore');
                  }
                }

                var TimePicContainer = document.createElement('div');
                TimePicContainer.className = 'TimePicContainer';
                var sBodyPicture = document.createElement('img');
                sBodyPicture.className = 'sBodyPicture';
                sBodyPicture.src = thisClass.getPreviewImage(results[i].time);
                var sBodyText = document.createElement('p');
                sBodyText.className = 'sBodyText';
                var time = thisClass._trimming.enabled ? results[i].time - thisClass._trimming.start : results[i].time;
                sBodyText.innerHTML = "<span class='timeSpan'>" + thisClass.prettyTime(time) + "</span>" + paella.AntiXSS.htmlEscape(results[i].content);
                TimePicContainer.appendChild(sBodyPicture);
                sBodyInnerContainer.appendChild(TimePicContainer);
                sBodyInnerContainer.appendChild(sBodyText);
                searchBody.appendChild(sBodyInnerContainer); //ADD SECS TO DOM FOR EASY HANDLE

                sBodyInnerContainer.setAttribute('sec', time); //jQuery Binds for the search

                $(sBodyInnerContainer).hover(function () {
                  $(this).css('background-color', '#faa166');
                }, function () {
                  $(this).removeAttr('style');
                });
                $(sBodyInnerContainer).click(function () {
                  var sec = $(this).attr("sec");
                  paella.player.videoContainer.seekToTime(parseInt(sec));
                  paella.player.play();
                });
              }
            }
          }
        });
      }
    }, {
      key: "buildContent",
      value: function buildContent(domElement) {
        var thisClass = this;
        var myUrl = null; //SEARCH CONTAINER

        var searchPluginContainer = document.createElement('div');
        searchPluginContainer.className = 'searchPluginContainer'; //SEARCH BODY

        var searchBody = document.createElement('div');
        searchBody.className = 'searchBody';
        searchPluginContainer.appendChild(searchBody);
        thisClass._searchBody = searchBody; //SEARCH BAR

        var searchBar = document.createElement('div');
        searchBar.className = 'searchBar';
        searchPluginContainer.appendChild(searchBar); //INPUT

        var input = document.createElement("input");
        input.className = "searchBarInput";
        input.type = "text";
        input.id = "searchBarInput";
        input.name = "searchString";
        input.placeholder = paella.utils.dictionary.translate("Search");
        searchBar.appendChild(input);
        $(input).change(function () {
          var text = $(input).val();

          if (thisClass._searchTimer != null) {
            thisClass._searchTimer.cancel();
          }

          if (text != "") {
            thisClass.doSearch(text, searchBody);
          }
        });
        $(input).keyup(function (event) {
          if (event.keyCode != 13) {
            //IF no ENTER PRESSED SETUP THE TIMER
            var text = $(input).val();

            if (thisClass._searchTimer != null) {
              thisClass._searchTimer.cancel();
            }

            if (text != "") {
              thisClass._searchTimer = new paella.utils.Timer(function (timer) {
                thisClass.doSearch(text, searchBody);
              }, thisClass._searchTimerTime);
            } else {
              $(thisClass._searchBody).empty();
            }
          }
        });
        $(input).focus(function () {
          paella.keyManager.enabled = false;
        });
        $(input).focusout(function () {
          paella.keyManager.enabled = true;
        });
        domElement.appendChild(searchPluginContainer);
      }
    }]);

    return SearchPlugin;
  }(paella.ButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin17) {
    _inherits(SharePlugin, _paella$ButtonPlugin17);

    var _super99 = _createSuper(SharePlugin);

    function SharePlugin() {
      _classCallCheck(this, SharePlugin);

      return _super99.apply(this, arguments);
    }

    _createClass(SharePlugin, [{
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return 'shareButtonPlugin';
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-social';
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 560;
      }
    }, {
      key: "getName",
      value: function getName() {
        return 'es.upv.paella.sharePlugin';
      }
    }, {
      key: "getButtonType",
      value: function getButtonType() {
        return paella.ButtonPlugin.type.popUpButton;
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate('Share this video');
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        onSuccess(true);
      }
    }, {
      key: "closeOnMouseOut",
      value: function closeOnMouseOut() {
        return false;
      }
    }, {
      key: "setup",
      value: function setup() {}
    }, {
      key: "buildEmbed",
      value: function buildEmbed() {
        var self = this;
        var embed = document.createElement('div');
        embed.innerHTML = "\n\t\t\t\t<div>\n\t\t\t\t\t<div class=\"form-group\">\n\t\t\t\t\t\t<label class=\"control-label\">\n\t\t\t\t\t\t\t<input id=\"share-video-responsive\" type=\"checkbox\" value=\"3\" name=\"mailId[]\" > ".concat(paella.utils.dictionary.translate('Responsive design'), "\n\t\t\t\t\t\t</label>\n\t\t\t\t\t</div>\n\n\t\t\t\t\t<div id=\"share-video-block-size\" class=\"form-group\">\n\t\t\t\t\t\t<label class=\"control-label\"> ").concat(paella.utils.dictionary.translate('Video resolution'), " </label>\n\t\t\t\t\t\t\n\t\t\t\t\t\t<div class=\"row\">\n\t\t\t\t\t\t\t<div class=\"col-sm-6\">\n\t\t\t\t\t\t\t\t<select id=\"share-video-size\" class=\"form-control input-sm\">\n\t\t\t\t\t\t\t\t\t<option value=\"640x360\">360p</option>\n\t\t\t\t\t\t\t\t\t<option value=\"854x480\">480p</option>\n\t\t\t\t\t\t\t\t\t<option value=\"1280x720\">720p (HD)</option>\n\t\t\t\t\t\t\t\t\t<option value=\"1920x1080\">1080p (Full HD)</option>\n\t\t\t\t\t\t\t\t\t<option value=\"2560x1440\">1440p (2.5K)</option>\n\t\t\t\t\t\t\t\t\t<option value=\"3840x2160\">2160p (4K UHD)</option>\n\t\t\t\t\t\t\t\t\t<option value=\"custom\">").concat(paella.utils.dictionary.translate('Custom size'), "</option>\n\t\t\t\t\t\t\t\t</select>\n\t\t\t\t\t\t\t</div>\n\t\t\t\t\t\t\t<div class=\"col-sm-3\">\n\t\t\t\t\t\t\t\t<input id=\"share-video-width\" type=\"number\" class=\"form-control input-sm\" value=\"640\" disabled=\"disabled\">\n\t\t\t\t\t\t\t</div>\n\t\t\t\t\t\t\t<div class=\"col-sm-3\">\n\t\t\t\t\t\t\t\t<input id=\"share-video-height\" type=\"number\" class=\"form-control input-sm\" value=\"360\" disabled=\"disabled\">\n\t\t\t\t\t\t\t</div>\n\t\t\t\t\t\t</div>\n\t\t\t\t\t</div>\t\n\n\t\t\t\t\t<div id=\"share-video-block-resp\" class=\"form-group\" style=\"display:none;\">\n\t\t\t\t\t\t<label class=\"control-label\"> ").concat(paella.utils.dictionary.translate('Video resolution'), " </label>\n\t\t\t\t\t\t\n\t\t\t\t\t\t<select id=\"share-video-size-resp\" class=\"form-control input-sm\">\n\t\t\t\t\t\t\t<option value=\"25\">25%</option>\n\t\t\t\t\t\t\t<option value=\"33\">33%</option>\n\t\t\t\t\t\t\t<option value=\"50\">50%</option>\n\t\t\t\t\t\t\t<option value=\"33\">66%</option>\n\t\t\t\t\t\t\t<option value=\"75\">75%</option>\n\t\t\t\t\t\t\t<option value=\"100\">100%</option>\n\t\t\t\t\t\t</select>\t\t\t\t\t\t\n\t\t\t\t\t</div>\t\t\t\t\t\t\n\t\t\t\t\t\n\t\t\t\t\t<div class=\"form-group\">\n\t\t\t\t\t\t<label class=\"control-label\">").concat(paella.utils.dictionary.translate('Embed code'), "</label>\n\t\t\t\t\t\t\n\t\t\t\t\t\t<div id=\"share-video-embed\" class=\"alert alert-share\">\n\t\t\t\t\t\t</div>\n\t\t\t\t\t</div>\n\t\t\t\t</div>\n\t\t\t");

        embed.querySelector("#share-video-responsive").onchange = function (event) {
          var responsive = self._domElement.querySelector("#share-video-responsive").checked;

          if (responsive) {
            self._domElement.querySelector("#share-video-block-resp").style.display = "block";
            self._domElement.querySelector("#share-video-block-size").style.display = "none";
          } else {
            self._domElement.querySelector("#share-video-block-resp").style.display = "none";
            self._domElement.querySelector("#share-video-block-size").style.display = "block";
          }

          self.updateEmbedCode();
        };

        embed.querySelector("#share-video-size-resp").onchange = function (event) {
          self.updateEmbedCode();
        };

        embed.querySelector("#share-video-width").onchange = function (event) {
          self.updateEmbedCode();
        };

        embed.querySelector("#share-video-height").onchange = function (event) {
          self.updateEmbedCode();
        };

        embed.querySelector("#share-video-size").onchange = function (event) {
          var value = event.target ? event.target.value : event.toElement.value;

          if (value == "custom") {
            embed.querySelector("#share-video-width").disabled = false;
            embed.querySelector("#share-video-height").disabled = false;
          } else {
            embed.querySelector("#share-video-width").disabled = true;
            embed.querySelector("#share-video-height").disabled = true;
            var size = value.trim().split("x");
            embed.querySelector("#share-video-width").value = size[0];
            embed.querySelector("#share-video-height").value = size[1];
          }

          self.updateEmbedCode();
        };

        return embed;
      }
    }, {
      key: "buildSocial",
      value: function buildSocial() {
        var self = this;
        var social = document.createElement('div');
        social.innerHTML = "\n\t\t\t\t<div>\n\t\t\t\t\t<div class=\"form-group\">\n\t\t\t\t\t\t<label class=\"control-label\">".concat(paella.utils.dictionary.translate('Share on social networks'), "</label>\n\t\t\t\t\t\t<div class=\"row\" style=\"margin:0;\">\t\n\t\t\t\t\t\t\t<span id=\"share-btn-facebook\" class=\"share-button button-icon icon-facebook\" ></span>\n\t\t\t\t\t\t\t<span id=\"share-btn-twitter\" class=\"share-button button-icon icon-twitter\" ></span>\n\t\t\t\t\t\t\t<span id=\"share-btn-linkedin\" class=\"share-button button-icon icon-linkedin\" ></span>\n\t\t\t\t\t\t</div>\n\t\t\t\t\t</div>\n\t\t\t\t</div>\n\t\t\t");

        social.querySelector("#share-btn-facebook").onclick = function (event) {
          self.onSocialClick('facebook');
        };

        social.querySelector("#share-btn-twitter").onclick = function (event) {
          self.onSocialClick('twitter');
        };

        social.querySelector("#share-btn-linkedin").onclick = function (event) {
          self.onSocialClick('linkedin');
        };

        return social;
      }
    }, {
      key: "buildContent",
      value: function buildContent(domElement) {
        var hideSocial = this.config && this.config.hideSocial;
        this._domElement = domElement;
        domElement.appendChild(this.buildEmbed());

        if (!hideSocial) {
          domElement.appendChild(this.buildSocial());
        }

        this.updateEmbedCode();
      }
    }, {
      key: "getVideoUrl",
      value: function getVideoUrl() {
        var url = document.location.href;
        return url;
      }
    }, {
      key: "onSocialClick",
      value: function onSocialClick(network) {
        var videoUrl = encodeURIComponent(this.getVideoUrl());
        var title = encodeURIComponent("");
        var shareUrl;

        switch (network) {
          case 'twitter':
            shareUrl = "http://twitter.com/share?url=".concat(videoUrl, "&text=").concat(title);
            break;

          case 'facebook':
            shareUrl = "http://www.facebook.com/sharer.php?u=".concat(videoUrl, "&p[title]=").concat(title);
            break;

          case 'linkedin':
            shareUrl = "https://www.linkedin.com/shareArticle?mini=true&url=".concat(videoUrl, "&title=").concat(title);
            break;
        }

        if (shareUrl) {
          window.open(shareUrl);
        }

        paella.player.controls.hidePopUp(this.getName());
      }
    }, {
      key: "updateEmbedCode",
      value: function updateEmbedCode() {
        var videoUrl = this.getVideoUrl();

        var responsive = this._domElement.querySelector("#share-video-responsive").checked;

        var width = this._domElement.querySelector("#share-video-width").value;

        var height = this._domElement.querySelector("#share-video-height").value;

        var respSize = this._domElement.querySelector("#share-video-size-resp").value;

        var embedCode = '';

        if (responsive) {
          embedCode = "<div style=\"width:".concat(respSize, "%\"><div style=\"position:relative;display:block;height:0;padding:0;overflow:hidden;padding-bottom:56.25%\"> <iframe allowfullscreen=\"true\" webkitallowfullscreen=\"true\" mozallowfullscreen=\"true\" src=\"").concat(videoUrl, "\" style=\"border:0px #FFFFFF none; position:absolute; width:100%; height:100%\" name=\"Paella Player\" scrolling=\"no\" frameborder=\"0\" marginheight=\"0px\" marginwidth=\"0px\" width=\"100%\" height=\"100%\"></iframe> </div></div>");
        } else {
          embedCode = "<iframe allowfullscreen=\"true\" webkitallowfullscreen=\"true\" mozallowfullscreen=\"true\" src=\"".concat(videoUrl, "\" style=\"border:0px #FFFFFF none;\" name=\"Paella Player\" scrolling=\"no\" frameborder=\"0\" marginheight=\"0px\" marginwidth=\"0px\" width=\"").concat(width, "\" height=\"").concat(height, "\"></iframe>");
        }

        this._domElement.querySelector("#share-video-embed").innerText = embedCode;
      }
    }]);

    return SharePlugin;
  }(paella.ButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$VideoOverlayB3) {
    _inherits(ShowEditorPlugin, _paella$VideoOverlayB3);

    var _super100 = _createSuper(ShowEditorPlugin);

    function ShowEditorPlugin() {
      _classCallCheck(this, ShowEditorPlugin);

      return _super100.apply(this, arguments);
    }

    _createClass(ShowEditorPlugin, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.showEditorPlugin";
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "showEditorButton";
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-pencil';
      }
    }, {
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 10;
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Enter editor mode");
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        if (this.config.editorUrl) {
          paella.initDelegate.initParams.accessControl.canWrite().then(function (canWrite) {
            var enabled = canWrite; // && !paella.utils.userAgent.browser.IsMobileVersion && !paella.player.isLiveStream());					

            onSuccess(enabled);
          });
        } else {
          onSuccess(false);
        }
      }
    }, {
      key: "action",
      value: function action(button) {
        var editorUrl = this.config.editorUrl.replace("{id}", paella.player.videoIdentifier);
        window.location.href = editorUrl;
      }
    }]);

    return ShowEditorPlugin;
  }(paella.VideoOverlayButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin18) {
    _inherits(ThemeChooserPlugin, _paella$ButtonPlugin18);

    var _super101 = _createSuper(ThemeChooserPlugin);

    function ThemeChooserPlugin() {
      _classCallCheck(this, ThemeChooserPlugin);

      return _super101.apply(this, arguments);
    }

    _createClass(ThemeChooserPlugin, [{
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "themeChooserPlugin";
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-paintbrush';
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 2030;
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.themeChooserPlugin";
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Change theme");
      }
    }, {
      key: "getButtonType",
      value: function getButtonType() {
        return paella.ButtonPlugin.type.popUpButton;
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        this.currentUrl = null;
        this.currentMaster = null;
        this.currentSlave = null;
        this.availableMasters = [];
        this.availableSlaves = [];

        if (paella.player.config.skin && paella.player.config.skin.available && paella.player.config.skin.available instanceof Array && paella.player.config.skin.available.length > 0) {
          onSuccess(true);
        } else {
          onSuccess(false);
        }
      }
    }, {
      key: "buildContent",
      value: function buildContent(domElement) {
        var This = this;
        paella.player.config.skin.available.forEach(function (item) {
          var elem = document.createElement('div');
          elem.className = "themebutton";
          elem.innerText = item.replace('-', ' ').replace('_', ' ');
          $(elem).click(function (event) {
            paella.utils.skin.set(item);
            paella.player.controls.hidePopUp(This.getName());
          });
          domElement.appendChild(elem);
        });
      }
    }]);

    return ThemeChooserPlugin;
  }(paella.ButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$PlaybackCanva2) {
    _inherits(TimeMarksPlaybackCanvasPlugin, _paella$PlaybackCanva2);

    var _super102 = _createSuper(TimeMarksPlaybackCanvasPlugin);

    function TimeMarksPlaybackCanvasPlugin() {
      _classCallCheck(this, TimeMarksPlaybackCanvasPlugin);

      return _super102.apply(this, arguments);
    }

    _createClass(TimeMarksPlaybackCanvasPlugin, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.timeMarksPlaybackCanvasPlugin";
      }
    }, {
      key: "setup",
      value: function setup() {
        this._frameList = paella.initDelegate.initParams.videoLoader.frameList;
        this._frameKeys = Object.keys(this._frameList);

        if (!this._frameList || !this._frameKeys.length) {
          this._hasSlides = false;
        } else {
          this._hasSlides = true;
          this._frameKeys = this._frameKeys.sort(function (a, b) {
            return parseInt(a) - parseInt(b);
          });
        }
      }
    }, {
      key: "drawCanvas",
      value: function drawCanvas(context, width, height, videoData) {
        var _this216 = this;

        if (this._hasSlides) {
          this._frameKeys.forEach(function (l) {
            l = parseInt(l);
            var timeInstant = videoData.trimming.enabled ? l - videoData.trimming.start : l;

            if (timeInstant > 0 && timeInstant < videoData.trimming.duration) {
              var left = timeInstant * width / videoData.trimming.duration;

              _this216.drawTimeMark(context, left, height);
            }
          });
        }
      }
    }, {
      key: "drawTimeMark",
      value: function drawTimeMark(ctx, left, height) {
        ctx[1].fillStyle = this.config.color;
        ctx[1].fillRect(left, 0, 1, height);
      }
    }]);

    return TimeMarksPlaybackCanvasPlugin;
  }(paella.PlaybackCanvasPlugin);
});
paella.addDataDelegate("cameraTrack", function () {
  return /*#__PURE__*/function (_paella$DataDelegate3) {
    _inherits(TrackCameraDataDelegate, _paella$DataDelegate3);

    var _super103 = _createSuper(TrackCameraDataDelegate);

    function TrackCameraDataDelegate() {
      _classCallCheck(this, TrackCameraDataDelegate);

      return _super103.apply(this, arguments);
    }

    _createClass(TrackCameraDataDelegate, [{
      key: "read",
      value: function read(context, params, onSuccess) {
        var videoUrl = paella.player.videoLoader.getVideoUrl();

        if (videoUrl) {
          videoUrl += 'trackhd.json';
          paella.utils.ajax.get({
            url: videoUrl
          }, function (data) {
            if (typeof data === "string") {
              try {
                data = JSON.parse(data);
              } catch (err) {}
            }

            data.positions.sort(function (a, b) {
              return a.time - b.time;
            });
            onSuccess(data);
          }, function () {
            return onSuccess(null);
          });
        } else {
          onSuccess(null);
        }
      }
    }, {
      key: "write",
      value: function write(context, params, value, onSuccess) {}
    }, {
      key: "remove",
      value: function remove(context, params, onSuccess) {}
    }]);

    return TrackCameraDataDelegate;
  }(paella.DataDelegate);
});

(function () {
  // Used to connect the toolbar button with the track4k plugin
  var g_track4kPlugin = null;

  function updatePosition(positionData, nextFrameData) {
    var twinTime = nextFrameData ? (nextFrameData.time - positionData.time) * 1000 : 100;
    if (twinTime > 2000) twinTime = 2000;
    var rect = positionData && positionData.rect || [0, 0, 0, 0];
    var offset_x = Math.abs(rect[0]);
    var offset_y = Math.abs(rect[1]);
    var view_width = rect[2];
    var view_height = rect[3];
    var zoom = this._videoData.originalWidth / view_width;
    var left = offset_x / this._videoData.originalWidth;
    var top = offset_y / this._videoData.originalHeight;
    paella.player.videoContainer.masterVideo().setZoom(zoom * 100, left * zoom * 100, top * zoom * 100, twinTime);
  }

  function nextFrame(time) {
    var index = -1;
    time = Math.round(time);

    this._trackData.some(function (data, i) {
      if (data.time >= time) {
        index = i;
      }

      return index !== -1;
    }); // Index contains the current frame index


    if (this._trackData.length > index + 1) {
      return this._trackData[index + 1];
    } else {
      return null;
    }
  }

  function prevFrame(time) {
    var frame = this._trackData[0];
    time = Math.round(time);

    this._trackData.some(function (data, i, frames) {
      if (frames[i + 1]) {
        if (data.time <= time && frames[i + 1].time > time) {
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
    var frameRect = null;
    time = Math.round(time);

    this._trackData.some(function (data, i, frames) {
      if (data.time <= time) {
        if (frames[i + 1]) {
          if (frames[i + 1].time > time) {
            frameRect = data;
          }
        } else {
          frameRect = data;
        }
      }

      return frameRect !== null;
    });

    return frameRect;
  }

  paella.addPlugin(function () {
    return /*#__PURE__*/function (_paella$EventDrivenPl12) {
      _inherits(Track4KPlugin, _paella$EventDrivenPl12);

      var _super104 = _createSuper(Track4KPlugin);

      function Track4KPlugin() {
        var _this217;

        _classCallCheck(this, Track4KPlugin);

        _this217 = _super104.call(this);
        g_track4kPlugin = _assertThisInitialized(_this217);
        _this217._videoData = {};
        _this217._trackData = [];
        _this217._enabled = true;
        return _this217;
      }

      _createClass(Track4KPlugin, [{
        key: "checkEnabled",
        value: function checkEnabled(cb) {
          var _this218 = this;

          paella.data.read('cameraTrack', {
            id: paella.initDelegate.getId()
          }, function (data) {
            if (data) {
              _this218._videoData.width = data.width;
              _this218._videoData.height = data.height;
              _this218._videoData.originalWidth = data.originalWidth;
              _this218._videoData.originalHeight = data.originalHeight;
              _this218._trackData = data.positions;
              _this218._enabled = true;
            } else {
              _this218._enabled = false;
            }

            cb(_this218._enabled);
          });
        }
      }, {
        key: "getName",
        value: function getName() {
          return "es.upv.paella.track4kPlugin";
        }
      }, {
        key: "getEvents",
        value: function getEvents() {
          return [paella.events.timeupdate, paella.events.play, paella.events.seekToTime];
        }
      }, {
        key: "onEvent",
        value: function onEvent(eventType, data) {
          if (!this._trackData.length) return;

          if (eventType === paella.events.play) {} else if (eventType === paella.events.timeupdate) {
            this.updateZoom(data.currentTime);
          } else if (eventType === paella.events.seekToTime) {
            this.seekTo(data.newPosition);
          }
        }
      }, {
        key: "updateZoom",
        value: function updateZoom(currentTime) {
          if (this._enabled) {
            var data = curFrame.apply(this, [currentTime]);
            var nextFrameData = nextFrame.apply(this, [currentTime]);

            if (data && this._lastPosition !== data) {
              this._lastPosition = data;
              updatePosition.apply(this, [data, nextFrameData]);
            }
          }
        }
      }, {
        key: "seekTo",
        value: function seekTo(time) {
          var data = prevFrame.apply(this, [time]);

          if (data && this._enabled) {
            this._lastPosition = data;
            updatePosition.apply(this, [data]);
          }
        }
      }, {
        key: "enabled",
        get: function get() {
          return this._enabled;
        },
        set: function set(e) {
          this._enabled = e;

          if (this._enabled) {
            var thisClass = this;
            paella.player.videoContainer.currentTime().then(function (time) {
              thisClass.updateZoom(time);
            });
          }
        }
      }]);

      return Track4KPlugin;
    }(paella.EventDrivenPlugin);
  });
  paella.addPlugin(function () {
    return /*#__PURE__*/function (_paella$ButtonPlugin19) {
      _inherits(VideoZoomTrack4KPlugin, _paella$ButtonPlugin19);

      var _super105 = _createSuper(VideoZoomTrack4KPlugin);

      function VideoZoomTrack4KPlugin() {
        _classCallCheck(this, VideoZoomTrack4KPlugin);

        return _super105.apply(this, arguments);
      }

      _createClass(VideoZoomTrack4KPlugin, [{
        key: "getAlignment",
        value: function getAlignment() {
          return 'right';
        }
      }, {
        key: "getSubclass",
        value: function getSubclass() {
          return "videoZoomToolbar";
        }
      }, {
        key: "getIconClass",
        value: function getIconClass() {
          return 'icon-screen';
        }
      }, {
        key: "closeOnMouseOut",
        value: function closeOnMouseOut() {
          return true;
        }
      }, {
        key: "getIndex",
        value: function getIndex() {
          return 2030;
        }
      }, {
        key: "getName",
        value: function getName() {
          return "es.upv.paella.videoZoomTrack4kPlugin";
        }
      }, {
        key: "getDefaultToolTip",
        value: function getDefaultToolTip() {
          return paella.utils.dictionary.translate("Set video zoom");
        }
      }, {
        key: "getButtonType",
        value: function getButtonType() {
          return paella.ButtonPlugin.type.popUpButton;
        }
      }, {
        key: "checkEnabled",
        value: function checkEnabled(onSuccess) {
          var players = paella.player.videoContainer.streamProvider.videoPlayers;
          var pluginData = paella.player.config.plugins.list[this.getName()];
          var playerIndex = pluginData.targetStreamIndex;
          var autoByDefault = pluginData.autoModeByDefault;
          this.targetPlayer = players.length > playerIndex ? players[playerIndex] : null;
          g_track4kPlugin.enabled = autoByDefault;
          onSuccess(paella.player.config.player.videoZoom.enabled && this.targetPlayer && this.targetPlayer.allowZoom());
        }
      }, {
        key: "setup",
        value: function setup() {
          if (this.config.autoModeByDefault) {
            this.zoomAuto();
          } else {
            this.resetZoom();
          }
        }
      }, {
        key: "buildContent",
        value: function buildContent(domElement) {
          var _this219 = this;

          this.changeIconClass("icon-mini-zoom-in");

          g_track4kPlugin.updateTrackingStatus = function () {
            if (g_track4kPlugin.enabled) {
              $('.zoom-auto').addClass("autoTrackingActivated");
              $('.icon-mini-zoom-in').addClass("autoTrackingActivated");
            } else {
              $('.zoom-auto').removeClass("autoTrackingActivated");
              $('.icon-mini-zoom-in').removeClass("autoTrackingActivated");
            }
          };

          paella.events.bind(paella.events.videoZoomChanged, function (evt, target) {
            g_track4kPlugin.updateTrackingStatus;
          });
          g_track4kPlugin.updateTrackingStatus;

          function getZoomButton(className, onClick, content) {
            var btn = document.createElement('div');
            btn.className = "videoZoomToolbarItem ".concat(className);

            if (content) {
              btn.innerText = content;
            } else {
              btn.innerHTML = "<i class=\"glyphicon glyphicon-".concat(className, "\"></i>");
            }

            $(btn).click(onClick);
            return btn;
          }

          domElement.appendChild(getZoomButton('zoom-in', function (evt) {
            _this219.zoomIn();
          }));
          domElement.appendChild(getZoomButton('zoom-out', function (evt) {
            _this219.zoomOut();
          }));
          domElement.appendChild(getZoomButton('picture', function (evt) {
            _this219.resetZoom();
          }));
          domElement.appendChild(getZoomButton('zoom-auto', function (evt) {
            _this219.zoomAuto();

            paella.player.controls.hidePopUp(_this219.getName());
          }, "auto"));
        }
      }, {
        key: "zoomIn",
        value: function zoomIn() {
          g_track4kPlugin.enabled = false;
          this.targetPlayer.zoomIn();
        }
      }, {
        key: "zoomOut",
        value: function zoomOut() {
          g_track4kPlugin.enabled = false;
          this.targetPlayer.zoomOut();
        }
      }, {
        key: "resetZoom",
        value: function resetZoom() {
          g_track4kPlugin.enabled = false;
          this.targetPlayer.setZoom(100, 0, 0, 500);
          if (g_track4kPlugin.updateTrackingStatus) g_track4kPlugin.updateTrackingStatus();
        }
      }, {
        key: "zoomAuto",
        value: function zoomAuto() {
          g_track4kPlugin.enabled = !g_track4kPlugin.enabled;
          if (g_track4kPlugin.updateTrackingStatus) g_track4kPlugin.updateTrackingStatus();
        }
      }]);

      return VideoZoomTrack4KPlugin;
    }(paella.ButtonPlugin);
  });
})();

(function () {
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

paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$userTracking$2) {
    _inherits(ElasticsearchSaverPlugin, _paella$userTracking$2);

    var _super106 = _createSuper(ElasticsearchSaverPlugin);

    function ElasticsearchSaverPlugin() {
      _classCallCheck(this, ElasticsearchSaverPlugin);

      return _super106.apply(this, arguments);
    }

    _createClass(ElasticsearchSaverPlugin, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.usertracking.elasticsearchSaverPlugin";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        this._url = this.config.url;
        this._index = this.config.index || "paellaplayer";
        this._type = this.config.type || "usertracking";
        var enabled = true;

        if (this._url == undefined) {
          enabled = false;
          paella.log.debug("No ElasticSearch URL found in config file. Disabling ElasticSearch PlugIn");
        }

        onSuccess(enabled);
      }
    }, {
      key: "log",
      value: function log(event, params) {
        var _this220 = this;

        var p = params;

        if (_typeof(p) != "object") {
          p = {
            value: p
          };
        }

        var currentTime = 0;
        paella.player.videoContainer.currentTime().then(function (t) {
          currentTime = t;
          return paella.player.videoContainer.paused();
        }).then(function (paused) {
          var log = {
            date: new Date(),
            video: paella.initDelegate.getId(),
            playing: !paused,
            time: parseInt(currentTime + paella.player.videoContainer.trimStart()),
            event: event,
            params: p
          };
          paella.utils.ajax.post({
            url: _this220._url + "/" + _this220._index + "/" + _this220._type + "/",
            params: JSON.stringify(log)
          });
        });
      }
    }]);

    return ElasticsearchSaverPlugin;
  }(paella.userTracking.SaverPlugIn);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$userTracking$3) {
    _inherits(GoogleAnalyticsTracking, _paella$userTracking$3);

    var _super107 = _createSuper(GoogleAnalyticsTracking);

    function GoogleAnalyticsTracking() {
      _classCallCheck(this, GoogleAnalyticsTracking);

      return _super107.apply(this, arguments);
    }

    _createClass(GoogleAnalyticsTracking, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.usertracking.GoogleAnalyticsSaverPlugin";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        var trackingID = this.config.trackingID;
        var domain = this.config.domain || "auto";

        if (trackingID) {
          paella.log.debug("Google Analitycs Enabled");
          /* jshint ignore:start */

          (function (i, s, o, g, r, a, m) {
            i['GoogleAnalyticsObject'] = r;
            i[r] = i[r] || function () {
              (i[r].q = i[r].q || []).push(arguments);
            }, i[r].l = 1 * new Date();
            a = s.createElement(o), m = s.getElementsByTagName(o)[0];
            a.async = 1;
            a.src = g;
            m.parentNode.insertBefore(a, m);
          })(window, document, 'script', '//www.google-analytics.com/analytics.js', '__gaTracker');
          /* jshint ignore:end */


          __gaTracker('create', trackingID, domain);

          __gaTracker('send', 'pageview');

          onSuccess(true);
        } else {
          paella.log.debug("No Google Tracking ID found in config file. Disabling Google Analitycs PlugIn");
          onSuccess(false);
        }
      }
    }, {
      key: "log",
      value: function log(event, params) {
        if (this.config.category === undefined || this.config.category === true) {
          var category = this.config.category || "PaellaPlayer";
          var action = event;
          var label = "";

          try {
            label = JSON.stringify(params);
          } catch (e) {}

          __gaTracker('send', 'event', category, action, label);
        }
      }
    }]);

    return GoogleAnalyticsTracking;
  }(paella.userTracking.SaverPlugIn);
});

var _paq = _paq || [];

paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$userTracking$4) {
    _inherits(PiwikAnalyticsTracking, _paella$userTracking$4);

    var _super108 = _createSuper(PiwikAnalyticsTracking);

    function PiwikAnalyticsTracking() {
      _classCallCheck(this, PiwikAnalyticsTracking);

      return _super108.apply(this, arguments);
    }

    _createClass(PiwikAnalyticsTracking, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.usertracking.piwikSaverPlugIn";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        if (this.config.tracker && this.config.siteId) {
          _paq.push(['trackPageView']);

          _paq.push(['enableLinkTracking']);

          (function () {
            var u = this.config.tracker;

            _paq.push(['setTrackerUrl', u + '/piwik.php']);

            _paq.push(['setSiteId', this.config.siteId]);

            var d = document,
                g = d.createElement('script'),
                s = d.getElementsByTagName('script')[0];
            g.type = 'text/javascript';
            g.async = true;
            g.defer = true;
            g.src = u + 'piwik.js';
            s.parentNode.insertBefore(g, s);
            onSuccess(true);
          })();
        } else {
          onSuccess(false);
        }
      }
    }, {
      key: "log",
      value: function log(event, params) {
        var category = this.config.category || "PaellaPlayer";
        var action = event;
        var label = "";

        try {
          label = JSON.stringify(params);
        } catch (e) {}

        _paq.push(['trackEvent', category, action, label]);
      }
    }]);

    return PiwikAnalyticsTracking;
  }(paella.userTracking.SaverPlugIn);
});

(function () {
  paella.addCanvasPlugin("video360", true, true, function () {
    return /*#__PURE__*/function (_paella$WebGLCanvas) {
      _inherits(Video360Canvas, _paella$WebGLCanvas);

      var _super109 = _createSuper(Video360Canvas);

      function Video360Canvas(stream) {
        _classCallCheck(this, Video360Canvas);

        return _super109.call(this, stream);
      }

      _createClass(Video360Canvas, [{
        key: "loadVideo",
        value: function loadVideo(videoPlugin, stream) {
          var _this221 = this;

          return new Promise(function (resolve, reject) {
            var checkLoaded = function checkLoaded() {
              if (_this221.video) {
                resolve(_this221);
              } else {
                setTimeout(checkLoaded, 100);
              }
            };

            checkLoaded();
          });
        }
      }, {
        key: "buildVideoSurface",
        value: function buildVideoSurface(sceneRoot, videoTexture) {
          var sphere = bg.scene.PrimitiveFactory.Sphere(this.gl, 1, 50);
          var sphereNode = new bg.scene.Node(this.gl);
          sphereNode.addComponent(sphere);
          sphere.getMaterial(0).texture = videoTexture;
          sphere.getMaterial(0).lightEmission = 0;
          sphere.getMaterial(0).lightEmissionMaskInvert = false;
          sphere.getMaterial(0).cullFace = false;
          sphereNode.addComponent(new bg.scene.Transform(bg.Matrix4.Scale(1, -1, 1)));
          sceneRoot.addChild(sphereNode);
        }
      }, {
        key: "buildCamera",
        value: function buildCamera() {
          var cameraNode = new bg.scene.Node(this.gl, "Camera");
          var camera = new bg.scene.Camera();
          cameraNode.addComponent(camera);
          cameraNode.addComponent(new bg.scene.Transform());
          var projection = new bg.scene.OpticalProjectionStrategy();
          projection.far = 100;
          projection.focalLength = 55;
          camera.projectionStrategy = projection;
          var oc = new bg.manipulation.OrbitCameraController();
          oc.maxPitch = 90;
          oc.minPitch = -90;
          oc.maxDistance = 0;
          oc.minDistance = 0;
          this._cameraController = oc;
          cameraNode.addComponent(oc);
          return cameraNode;
        }
      }]);

      return Video360Canvas;
    }(paella.WebGLCanvas);
  });
})();

(function () {
  function cyln2world(a, e) {
    return new bg.Vector3(Math.cos(e) * Math.cos(a), Math.cos(e) * Math.sin(a), Math.sin(e));
  }

  function world2fish(x, y, z) {
    var nz = z;
    if (z < -1.0) nz = -1.0;else if (z > 1.0) nz = 1.0;
    return new bg.Vector2(Math.atan2(y, x), Math.acos(nz) / Math.PI); // 0.0 to 1.0
  }

  function calcTexUv(i, j, lens) {
    var world = cyln2world(((i + 90) / 180.0 - 1.0) * Math.PI, // rotate 90 deg for polygon
    (0.5 - j / 180.0) * Math.PI);
    var ar = world2fish(Math.sin(-0.5 * Math.PI) * world.z + Math.cos(-0.5 * Math.PI) * world.x, world.y, Math.cos(-0.5 * Math.PI) * world.z - Math.sin(-0.5 * Math.PI) * world.x);
    var fishRad = 0.883;
    var fishRad2 = fishRad * 0.88888888888888;
    var fishCenter = 1.0 - 0.44444444444444;
    var x = lens === 0 ? fishRad * ar.y * Math.cos(ar.x) * 0.5 + 0.25 : fishRad * (1.0 - ar.y) * Math.cos(-1.0 * ar.x + Math.PI) * 0.5 + 0.75;
    var y = lens === 0 ? fishRad2 * ar.y * Math.sin(ar.x) + fishCenter : fishRad2 * (1.0 - ar.y) * Math.sin(-1.0 * ar.x + Math.PI) + fishCenter;
    return new bg.Vector2(x, y);
  }

  function buildViewerNode(ctx) {
    var radius = 1;
    var node = new bg.scene.Node(ctx);
    var drw = new bg.scene.Drawable();
    node.addComponent(drw);
    var plist = new bg.base.PolyList(ctx);
    var vertex = [];
    var normals = [];
    var uvs = [];
    var index = [];

    for (var j = 0; j <= 180; j += 5) {
      for (var i = 0; i <= 360; i += 5) {
        vertex.push(new bg.Vector3(Math.sin(Math.PI * j / 180.0) * Math.sin(Math.PI * i / 180.0) * radius, Math.cos(Math.PI * j / 180.0) * radius, Math.sin(Math.PI * j / 180.0) * Math.cos(Math.PI * i / 180.0) * radius));
        normals.push(new bg.Vector3(0, 0, -1));
      }
      /* devide texture */


      for (var k = 0; k <= 180; k += 5) {
        uvs.push(calcTexUv(k, j, 0));
      }

      for (var l = 180; l <= 360; l += 5) {
        uvs.push(calcTexUv(l, j, 1));
      }
    }

    function addFace(v0, v1, v2, n0, n1, n2, uv0, uv1, uv2) {
      plist.vertex.push(v0.x);
      plist.vertex.push(v0.y);
      plist.vertex.push(v0.z);
      plist.vertex.push(v1.x);
      plist.vertex.push(v1.y);
      plist.vertex.push(v1.z);
      plist.vertex.push(v2.x);
      plist.vertex.push(v2.y);
      plist.vertex.push(v2.z);
      plist.normal.push(n0.x);
      plist.normal.push(n0.y);
      plist.normal.push(n0.z);
      plist.normal.push(n1.x);
      plist.normal.push(n1.y);
      plist.normal.push(n1.z);
      plist.normal.push(n2.x);
      plist.normal.push(n2.z);
      plist.normal.push(n2.z);
      plist.texCoord0.push(uv0.x);
      plist.texCoord0.push(1 - uv0.y);
      plist.texCoord0.push(uv1.x);
      plist.texCoord0.push(1 - uv1.y);
      plist.texCoord0.push(uv2.x);
      plist.texCoord0.push(1 - uv2.y);
      plist.index.push(plist.index.length);
      plist.index.push(plist.index.length);
      plist.index.push(plist.index.length);
    }

    for (var m = 0; m < 36; m++) {
      for (var n = 0; n < 72; n++) {
        var v = m * 73 + n;
        var t = n < 36 ? m * 74 + n : m * 74 + n + 1;
        [uvs[t + 0], uvs[t + 1], uvs[t + 74]], [uvs[t + 1], uvs[t + 75], uvs[t + 74]];
        var v0 = vertex[v + 0];
        var n0 = normals[v + 0];
        var uv0 = uvs[t + 0];
        var v1 = vertex[v + 1];
        var n1 = normals[v + 1];
        var uv1 = uvs[t + 1];
        var v2 = vertex[v + 73];
        var n2 = normals[v + 73];
        var uv2 = uvs[t + 74];
        var v3 = vertex[v + 74];
        var n3 = normals[v + 74];
        var uv3 = uvs[t + 75];
        addFace(v0, v1, v2, n0, n1, n2, uv0, uv1, uv2);
        addFace(v1, v3, v2, n1, n3, n2, uv1, uv3, uv2);
      }
    }

    plist.build();
    drw.addPolyList(plist);
    var trx = bg.Matrix4.Scale(-1, 1, 1);
    node.addComponent(new bg.scene.Transform(trx));
    return node;
  }

  paella.addCanvasPlugin("video360Theta", true, true, function () {
    return /*#__PURE__*/function (_paella$WebGLCanvas2) {
      _inherits(Video360ThetaCanvas, _paella$WebGLCanvas2);

      var _super110 = _createSuper(Video360ThetaCanvas);

      function Video360ThetaCanvas(stream) {
        _classCallCheck(this, Video360ThetaCanvas);

        return _super110.call(this, stream);
      }

      _createClass(Video360ThetaCanvas, [{
        key: "loadVideo",
        value: function loadVideo(videoPlugin, stream) {
          var _this222 = this;

          return new Promise(function (resolve, reject) {
            var checkLoaded = function checkLoaded() {
              if (_this222.video) {
                resolve(_this222);
              } else {
                setTimeout(checkLoaded, 100);
              }
            };

            checkLoaded();
          });
        }
      }, {
        key: "buildVideoSurface",
        value: function buildVideoSurface(sceneRoot, videoTexture) {
          var sphereNode = buildViewerNode(this.gl);
          var sphere = sphereNode.drawable;
          sphere.getMaterial(0).texture = videoTexture;
          sphere.getMaterial(0).lightEmission = 0;
          sphere.getMaterial(0).lightEmissionMaskInvert = false;
          sphere.getMaterial(0).cullFace = false;
          sceneRoot.addChild(sphereNode);
        }
      }, {
        key: "buildCamera",
        value: function buildCamera() {
          var cameraNode = new bg.scene.Node(this.gl, "Camera");
          var camera = new bg.scene.Camera();
          cameraNode.addComponent(camera);
          cameraNode.addComponent(new bg.scene.Transform());
          var projection = new bg.scene.OpticalProjectionStrategy();
          projection.far = 100;
          projection.focalLength = 55;
          camera.projectionStrategy = projection;
          var oc = new bg.manipulation.OrbitCameraController();
          oc.maxPitch = 90;
          oc.minPitch = -90;
          oc.maxDistance = 0;
          oc.minDistance = 0;
          this._cameraController = oc;
          cameraNode.addComponent(oc);
          return cameraNode;
        }
      }]);

      return Video360ThetaCanvas;
    }(paella.WebGLCanvas);
  });
})();

paella.addDataDelegate('metadata', function () {
  return /*#__PURE__*/function (_paella$DataDelegate4) {
    _inherits(VideoManifestMetadataDataDelegate, _paella$DataDelegate4);

    var _super111 = _createSuper(VideoManifestMetadataDataDelegate);

    function VideoManifestMetadataDataDelegate() {
      _classCallCheck(this, VideoManifestMetadataDataDelegate);

      return _super111.apply(this, arguments);
    }

    _createClass(VideoManifestMetadataDataDelegate, [{
      key: "read",
      value: function read(context, params, onSuccess) {
        var metadata = paella.player.videoLoader.getMetadata();
        onSuccess(metadata[params], true);
      }
    }, {
      key: "write",
      value: function write(context, params, value, onSuccess) {
        onSuccess({}, true);
      }
    }, {
      key: "remove",
      value: function remove(context, params, onSuccess) {
        onSuccess({}, true);
      }
    }]);

    return VideoManifestMetadataDataDelegate;
  }(paella.DataDelegate);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$VideoOverlayB4) {
    _inherits(VideoDataPlugin, _paella$VideoOverlayB4);

    var _super112 = _createSuper(VideoDataPlugin);

    function VideoDataPlugin() {
      _classCallCheck(this, VideoDataPlugin);

      return _super112.apply(this, arguments);
    }

    _createClass(VideoDataPlugin, [{
      key: "getIndex",
      value: function getIndex() {
        return 10;
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "videoData";
      }
    }, {
      key: "getAlignment",
      value: function getAlignment() {
        return 'left';
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return "";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        // Check if enabled
        var plugin = paella.player.config.plugins.list["es.upv.paella.videoDataPlugin"];
        var exclude = plugin && plugin.excludeLocations || [];
        var excludeParent = plugin && plugin.excludeParentLocations || [];
        var excluded = exclude.some(function (url) {
          var re = RegExp(url, "i");
          return re.test(location.href);
        });

        if (window != window.parent) {
          excluded = excluded || excludeParent.some(function (url) {
            var re = RegExp(url, "i");

            try {
              return re.test(parent.location.href);
            } catch (e) {
              // Cross domain error
              return false;
            }
          });
        }

        onSuccess(!excluded);
      }
    }, {
      key: "setup",
      value: function setup() {
        var title = document.createElement("h1");
        title.innerText = "";
        title.className = "videoTitle";
        this.button.appendChild(title);
        paella.data.read("metadata", "title", function (data) {
          title.innerText = data;
        });
      }
    }, {
      key: "action",
      value: function action(button) {}
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.videoDataPlugin";
      }
    }]);

    return VideoDataPlugin;
  }(paella.VideoOverlayButtonPlugin);
});
paella.addPlugin(function () {
  var g_canvasWidth = 320;
  var g_canvasHeight = 180;

  function getThumbnailContainer(videoIndex) {
    var container = document.createElement('canvas');
    container.width = g_canvasWidth;
    container.height = g_canvasHeight;
    container.className = "zoom-thumbnail";
    container.id = "zoomContainer" + videoIndex;
    return container;
  }

  function getZoomRect() {
    var zoomRect = document.createElement('div');
    zoomRect.className = "zoom-rect";
    return zoomRect;
  }

  function updateThumbnail(thumbElem) {
    var player = thumbElem.player;
    var canvas = thumbElem.canvas;
    player.captureFrame().then(function (frameData) {
      var ctx = canvas.getContext("2d");
      ctx.drawImage(frameData.source, 0, 0, g_canvasWidth, g_canvasHeight);
    });
  }

  function setupButtons(videoPlayer) {
    var wrapper = videoPlayer.parent;
    var wrapperDom = wrapper.domElement;
    var zoomButton = document.createElement('div');
    wrapperDom.appendChild(zoomButton);
    zoomButton.className = "videoZoomButton btn zoomIn";
    zoomButton.innerHTML = '<i class="glyphicon glyphicon-zoom-in"></i>';
    $(zoomButton).on('mousedown', function (e) {
      e.preventDefault();
      paella.player.videoContainer.disablePlayOnClick();
      videoPlayer.zoomIn();
    });
    $(zoomButton).on('mouseup', function (e) {
      e.preventDefault();
      paella.player.videoContainer.enablePlayOnClick(1000);
    });
    zoomButton = document.createElement('div');
    wrapperDom.appendChild(zoomButton);
    zoomButton.className = "videoZoomButton btn zoomOut";
    zoomButton.innerHTML = '<i class="glyphicon glyphicon-zoom-out"></i>';
    $(zoomButton).on('mousedown', function (e) {
      e.preventDefault();
      paella.player.videoContainer.disablePlayOnClick();
      videoPlayer.zoomOut();
    });
    $(zoomButton).on('mouseup', function (e) {
      e.preventDefault();
      paella.player.videoContainer.enablePlayOnClick(1000);
    });
  }

  return /*#__PURE__*/function (_paella$VideoOverlayB5) {
    _inherits(VideoZoomPlugin, _paella$VideoOverlayB5);

    var _super113 = _createSuper(VideoZoomPlugin);

    function VideoZoomPlugin() {
      _classCallCheck(this, VideoZoomPlugin);

      return _super113.apply(this, arguments);
    }

    _createClass(VideoZoomPlugin, [{
      key: "getIndex",
      value: function getIndex() {
        return 10;
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "videoZoom";
      }
    }, {
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return "";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        onSuccess(true);
      }
    }, {
      key: "setup",
      value: function setup() {
        var _this223 = this;

        var thisClass = this;
        this._thumbnails = [];
        this._visible = false;
        this._available = false;

        function checkVisibility() {
          var buttons = $('.videoZoomButton');
          var thumbs = $('.videoZoom');
          var showButtons = this.config.showButtons !== undefined ? this.config.showButtons : true;

          if (this._visible && this._available) {
            showButtons ? buttons.show() : buttons.hide();
            thumbs.show();
          } else {
            buttons.hide();
            thumbs.hide();
          }
        }

        var players = paella.player.videoContainer.streamProvider.videoPlayers;
        players.forEach(function (player, index) {
          if (player.allowZoom()) {
            _this223._available = player.zoomAvailable();
            _this223._visible = _this223._available;
            setupButtons.apply(_this223, [player]);
            player.supportsCaptureFrame().then(function (supports) {
              if (supports) {
                var thumbContainer = document.createElement('div');
                thumbContainer.className = "zoom-container";
                var thumb = getThumbnailContainer.apply(_this223, [index]);
                var zoomRect = getZoomRect.apply(_this223);

                _this223.button.appendChild(thumbContainer);

                thumbContainer.appendChild(thumb);
                thumbContainer.appendChild(zoomRect);
                $(thumbContainer).hide();

                _this223._thumbnails.push({
                  player: player,
                  thumbContainer: thumbContainer,
                  zoomRect: zoomRect,
                  canvas: thumb
                });

                checkVisibility.apply(_this223);
              }
            });
          }
        });
        var update = false;
        paella.events.bind(paella.events.play, function (evt) {
          var updateThumbs = function updateThumbs() {
            _this223._thumbnails.forEach(function (item) {
              updateThumbnail(item);
            });

            if (update) {
              setTimeout(function () {
                updateThumbs();
              }, 2000);
            }
          };

          update = true;
          updateThumbs();
        });
        paella.events.bind(paella.events.pause, function (evt) {
          update = false;
        });
        paella.events.bind(paella.events.videoZoomChanged, function (evt, target) {
          _this223._thumbnails.some(function (thumb) {
            if (thumb.player == target.video) {
              if (thumb.player.zoom > 100) {
                $(thumb.thumbContainer).show();
                var x = target.video.zoomOffset.x * 100 / target.video.zoom;
                var y = target.video.zoomOffset.y * 100 / target.video.zoom;
                var zoomRect = thumb.zoomRect;
                $(zoomRect).css({
                  left: x + '%',
                  top: y + '%',
                  width: 10000 / target.video.zoom + '%',
                  height: 10000 / target.video.zoom + '%'
                });
              } else {
                $(thumb.thumbContainer).hide();
              }

              return true;
            }
          });
        });
        paella.events.bind(paella.events.zoomAvailabilityChanged, function (evt, target) {
          _this223._available = target.available;
          _this223._visible = target.available;
          checkVisibility.apply(_this223);
        });
        paella.events.bind(paella.events.controlBarDidHide, function () {
          _this223._visible = false;
          checkVisibility.apply(_this223);
        });
        paella.events.bind(paella.events.controlBarDidShow, function () {
          _this223._visible = true;
          checkVisibility.apply(_this223);
        });
      }
    }, {
      key: "action",
      value: function action(button) {//paella.messageBox.showMessage(paella.utils.dictionary.translate("Live streaming mode: This is a live video, so, some capabilities of the player are disabled"));
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.videoZoomPlugin";
      }
    }]);

    return VideoZoomPlugin;
  }(paella.VideoOverlayButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin20) {
    _inherits(VideoZoomToolbarPlugin, _paella$ButtonPlugin20);

    var _super114 = _createSuper(VideoZoomToolbarPlugin);

    function VideoZoomToolbarPlugin() {
      _classCallCheck(this, VideoZoomToolbarPlugin);

      return _super114.apply(this, arguments);
    }

    _createClass(VideoZoomToolbarPlugin, [{
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "videoZoomToolbar";
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-screen';
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 2030;
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.videoZoomToolbarPlugin";
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Set video zoom");
      }
    }, {
      key: "getButtonType",
      value: function getButtonType() {
        return paella.ButtonPlugin.type.popUpButton;
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        var players = paella.player.videoContainer.streamProvider.videoPlayers;
        var pluginData = paella.player.config.plugins.list["es.upv.paella.videoZoomToolbarPlugin"];
        var playerIndex = pluginData.targetStreamIndex;
        this.targetPlayer = players.length > playerIndex ? players[playerIndex] : null;
        onSuccess(paella.player.config.player.videoZoom.enabled && this.targetPlayer && this.targetPlayer.allowZoom());
      }
    }, {
      key: "buildContent",
      value: function buildContent(domElement) {
        var _this224 = this;

        paella.events.bind(paella.events.videoZoomChanged, function (evt, target) {
          _this224.setText(Math.round(target.video.zoom) + "%");
        });
        this.setText("100%");

        function getZoomButton(className, onClick) {
          var btn = document.createElement('div');
          btn.className = "videoZoomToolbarItem ".concat(className);
          btn.innerHTML = "<i class=\"glyphicon glyphicon-".concat(className, "\"></i>");
          $(btn).click(onClick);
          return btn;
        }

        domElement.appendChild(getZoomButton('zoom-in', function (evt) {
          _this224.targetPlayer.zoomIn();
        }));
        domElement.appendChild(getZoomButton('zoom-out', function (evt) {
          _this224.targetPlayer.zoomOut();
        }));
      }
    }]);

    return VideoZoomToolbarPlugin;
  }(paella.ButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin21) {
    _inherits(ViewModePlugin, _paella$ButtonPlugin21);

    var _super115 = _createSuper(ViewModePlugin);

    function ViewModePlugin() {
      _classCallCheck(this, ViewModePlugin);

      return _super115.apply(this, arguments);
    }

    _createClass(ViewModePlugin, [{
      key: "getAlignment",
      value: function getAlignment() {
        return 'right';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return "showViewModeButton";
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-presentation-mode';
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 540;
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.viewModePlugin";
      } //getButtonType() { return paella.ButtonPlugin.type.popUpButton; }

    }, {
      key: "getButtonType",
      value: function getButtonType() {
        return paella.ButtonPlugin.type.menuButton;
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Change video layout");
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        this.buttonItems = null;
        this.buttons = [];
        this.selected_button = null;
        this.active_profiles = null;
        this.active_profiles = this.config.activeProfiles;
        onSuccess(!paella.player.videoContainer.isMonostream);
      }
    }, {
      key: "closeOnMouseOut",
      value: function closeOnMouseOut() {
        return true;
      }
    }, {
      key: "setup",
      value: function setup() {
        var thisClass = this;
        var Keys = {
          Tab: 9,
          Return: 13,
          Esc: 27,
          End: 35,
          Home: 36,
          Left: 37,
          Up: 38,
          Right: 39,
          Down: 40
        };
        paella.events.bind(paella.events.setProfile, function (event, params) {
          thisClass.onProfileChange(params.profileName);
        });
        $(this.button).keyup(function (event) {
          if (thisClass.isPopUpOpen()) {
            if (event.keyCode == Keys.Up) {
              if (thisClass.selected_button > 0) {
                if (thisClass.selected_button < thisClass.buttons.length) thisClass.buttons[thisClass.selected_button].className = 'viewModeItemButton ' + thisClass.buttons[thisClass.selected_button].data.profile;
                thisClass.selected_button--;
                thisClass.buttons[thisClass.selected_button].className = thisClass.buttons[thisClass.selected_button].className + ' selected';
              }
            } else if (event.keyCode == Keys.Down) {
              if (thisClass.selected_button < thisClass.buttons.length - 1) {
                if (thisClass.selected_button >= 0) thisClass.buttons[thisClass.selected_button].className = 'viewModeItemButton ' + thisClass.buttons[thisClass.selected_button].data.profile;
                thisClass.selected_button++;
                thisClass.buttons[thisClass.selected_button].className = thisClass.buttons[thisClass.selected_button].className + ' selected';
              }
            } else if (event.keyCode == Keys.Return) {
              thisClass.onItemClick(thisClass.buttons[thisClass.selected_button], thisClass.buttons[thisClass.selected_button].data.profile, thisClass.buttons[thisClass.selected_button].data.profile);
            }
          }
        });
      }
    }, {
      key: "getMenuContent",
      value: function getMenuContent() {
        var _this225 = this;

        var buttonItems = [];
        paella.profiles.profileList.forEach(function (profileData, index) {
          if (profileData.hidden) return;

          if (_this225.active_profiles) {
            var active = false;

            _this225.active_profiles.some(function (ap) {
              if (ap == profile) {
                active = ap;
                return true;
              }
            });

            if (!active) {
              return;
            }
          }

          var current = paella.profiles.currentProfileName;

          var url = _this225.getButtonItemIcon(profileData);

          url = url.replace(/\\/ig, '/');
          buttonItems.push({
            id: profileData.id + "_button",
            title: "",
            value: profileData.id,
            icon: url,
            className: _this225.getButtonItemClass(profileData.id),
            "default": current == profileData.id
          });
        });
        return buttonItems;
      }
    }, {
      key: "menuItemSelected",
      value: function menuItemSelected(itemData) {
        paella.player.setProfile(itemData.value);
        paella.player.controls.hidePopUp(this.getName());
      }
    }, {
      key: "onProfileChange",
      value: function onProfileChange(profileName) {
        this.rebuildMenu();
      }
    }, {
      key: "getButtonItemClass",
      value: function getButtonItemClass(profileName) {
        return 'viewModeItemButton ' + profileName;
      }
    }, {
      key: "getButtonItemIcon",
      value: function getButtonItemIcon(profileData) {
        return "".concat(paella.baseUrl, "resources/style/").concat(profileData.icon);
      }
    }]);

    return ViewModePlugin;
  }(paella.ButtonPlugin);
});
paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$ButtonPlugin22) {
    _inherits(VolumeRangePlugin, _paella$ButtonPlugin22);

    var _super116 = _createSuper(VolumeRangePlugin);

    function VolumeRangePlugin() {
      _classCallCheck(this, VolumeRangePlugin);

      return _super116.apply(this, arguments);
    }

    _createClass(VolumeRangePlugin, [{
      key: "getAlignment",
      value: function getAlignment() {
        return 'left';
      }
    }, {
      key: "getSubclass",
      value: function getSubclass() {
        return 'volumeRangeButton';
      }
    }, {
      key: "getIconClass",
      value: function getIconClass() {
        return 'icon-volume-high';
      }
    }, {
      key: "getName",
      value: function getName() {
        return "es.upv.paella.volumeRangePlugin";
      }
    }, {
      key: "getDefaultToolTip",
      value: function getDefaultToolTip() {
        return paella.utils.dictionary.translate("Volume");
      }
    }, {
      key: "getIndex",
      value: function getIndex() {
        return 9999;
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        this._tempMasterVolume = 0;
        this._inputMaster = null;
        this._control_NotMyselfEvent = true;
        this._storedValue = false;
        var enabled = !paella.utils.userAgent.browser.IsMobileVersion;
        onSuccess(enabled);
      }
    }, {
      key: "setup",
      value: function setup() {
        var self = this; //STORE VALUES

        paella.events.bind(paella.events.videoUnloaded, function (event, params) {
          self.storeVolume();
        }); //RECOVER VALUES

        paella.events.bind(paella.events.singleVideoReady, function (event, params) {
          self.loadStoredVolume(params);
        });
        paella.events.bind(paella.events.setVolume, function (evt, par) {
          self.updateVolumeOnEvent(par);
        });
      }
    }, {
      key: "updateVolumeOnEvent",
      value: function updateVolumeOnEvent(volume) {
        var thisClass = this;

        if (thisClass._control_NotMyselfEvent) {
          thisClass._inputMaster = volume.master;
        } else {
          thisClass._control_NotMyselfEvent = true;
        }
      }
    }, {
      key: "storeVolume",
      value: function storeVolume() {
        var This = this;
        paella.player.videoContainer.streamProvider.mainAudioPlayer.volume().then(function (v) {
          This._tempMasterVolume = v;
          This._storedValue = true;
        });
      }
    }, {
      key: "loadStoredVolume",
      value: function loadStoredVolume(params) {
        if (this._storedValue == false) {
          this.storeVolume();
        }

        if (this._tempMasterVolume) {
          paella.player.videoContainer.setVolume(this._tempMasterVolume);
        }

        this._storedValue = false;
      }
    }, {
      key: "action",
      value: function action(button) {
        if (paella.player.videoContainer.muted) {
          paella.player.videoContainer.unmute();
        } else {
          paella.player.videoContainer.mute();
        }
      }
    }, {
      key: "getExpandableContent",
      value: function getExpandableContent() {
        var _this226 = this;

        var rangeInput = document.createElement('input');
        this._inputMaster = rangeInput;
        rangeInput.type = "range";
        rangeInput.min = 0;
        rangeInput.max = 1;
        rangeInput.step = 0.01;
        paella.player.videoContainer.audioPlayer.volume().then(function (vol) {
          rangeInput.value = vol;
        });

        var updateMasterVolume = function updateMasterVolume() {
          var masterVolume = $(rangeInput).val();
          var slaveVolume = 0;
          _this226._control_NotMyselfEvent = false;
          paella.player.videoContainer.setVolume(masterVolume);
        };

        $(rangeInput).bind('input', function (e) {
          updateMasterVolume();
        });
        $(rangeInput).change(function () {
          updateMasterVolume();
        });
        paella.events.bind(paella.events.setVolume, function (event, params) {
          rangeInput.value = params.master;

          _this226.updateClass();
        });
        this.updateClass();
        return rangeInput;
      }
    }, {
      key: "updateClass",
      value: function updateClass() {
        var _this227 = this;

        var selected = '';
        var self = this;
        paella.player.videoContainer.volume().then(function (volume) {
          if (volume === undefined) {
            selected = 'icon-volume-mid';
          } else if (volume == 0) {
            selected = 'icon-volume-mute';
          } else if (volume < 0.33) {
            selected = 'icon-volume-low';
          } else if (volume < 0.66) {
            selected = 'icon-volume-mid';
          } else {
            selected = 'icon-volume-high';
          }

          _this227.changeIconClass(selected);
        });
      }
    }]);

    return VolumeRangePlugin;
  }(paella.ButtonPlugin);
});

(function () {
  var WebmVideoFactory = /*#__PURE__*/function (_paella$VideoFactory6) {
    _inherits(WebmVideoFactory, _paella$VideoFactory6);

    var _super117 = _createSuper(WebmVideoFactory);

    function WebmVideoFactory() {
      _classCallCheck(this, WebmVideoFactory);

      return _super117.apply(this, arguments);
    }

    _createClass(WebmVideoFactory, [{
      key: "webmCapable",
      value: function webmCapable() {
        var testEl = document.createElement("video");

        if (testEl.canPlayType) {
          return "" !== testEl.canPlayType('video/webm; codecs="vp8, vorbis"');
        } else {
          return false;
        }
      }
    }, {
      key: "isStreamCompatible",
      value: function isStreamCompatible(streamData) {
        try {
          if (!this.webmCapable()) return false;

          for (var key in streamData.sources) {
            if (key == 'webm') return true;
          }
        } catch (e) {}

        return false;
      }
    }, {
      key: "getVideoObject",
      value: function getVideoObject(id, streamData, rect) {
        return new paella.Html5Video(id, streamData, rect.x, rect.y, rect.w, rect.h, 'webm');
      }
    }]);

    return WebmVideoFactory;
  }(paella.VideoFactory);

  paella.videoFactories.WebmVideoFactory = WebmVideoFactory;
})();

paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$EventDrivenPl13) {
    _inherits(WindowTitlePlugin, _paella$EventDrivenPl13);

    var _super118 = _createSuper(WindowTitlePlugin);

    function WindowTitlePlugin() {
      _classCallCheck(this, WindowTitlePlugin);

      return _super118.apply(this, arguments);
    }

    _createClass(WindowTitlePlugin, [{
      key: "getName",
      value: function getName() {
        return "es.upv.paella.windowTitlePlugin";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
        var _this228 = this;

        this._initDone = false;
        paella.player.videoContainer.masterVideo().duration().then(function (d) {
          _this228.loadTitle();
        });
        onSuccess(true);
      }
    }, {
      key: "loadTitle",
      value: function loadTitle() {
        var title = paella.player.videoLoader.getMetadata() && paella.player.videoLoader.getMetadata().title;
        document.title = title || document.title;
        this._initDone = true;
      }
    }]);

    return WindowTitlePlugin;
  }(paella.EventDrivenPlugin);
});

(function () {
  var YoutubeVideo = /*#__PURE__*/function (_paella$VideoElementB6) {
    _inherits(YoutubeVideo, _paella$VideoElementB6);

    var _super119 = _createSuper(YoutubeVideo);

    function YoutubeVideo(id, stream, left, top, width, height) {
      var _this229;

      _classCallCheck(this, YoutubeVideo);

      _this229 = _super119.call(this, id, stream, 'div', left, top, width, height);
      _this229._posterFrame = null;
      _this229._currentQuality = null;
      _this229._autoplay = false;
      _this229._readyPromise = null;
      _this229._readyPromise = $.Deferred();
      return _this229;
    }

    _createClass(YoutubeVideo, [{
      key: "_deferredAction",
      value: function _deferredAction(action) {
        var _this230 = this;

        return new Promise(function (resolve, reject) {
          _this230._readyPromise.then(function () {
            resolve(action());
          }, function () {
            reject();
          });
        });
      }
    }, {
      key: "_getQualityObject",
      value: function _getQualityObject(index, s) {
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
          res: {
            w: null,
            h: null
          },
          src: null,
          label: s,
          level: level,
          bitrate: level,
          toString: function toString() {
            return this.label;
          },
          shortLabel: function shortLabel() {
            return this.label;
          },
          compare: function compare(q2) {
            return this.level - q2.level;
          }
        };
      }
    }, {
      key: "_onStateChanged",
      value: function _onStateChanged(e) {
        console.log("On state changed");
      } // Initialization functions

    }, {
      key: "getVideoData",
      value: function getVideoData() {
        var _this231 = this;

        var This = this;
        return new Promise(function (resolve, reject) {
          var stream = _this231._stream.sources.youtube[0];

          _this231._deferredAction(function () {
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
          });
        });
      }
    }, {
      key: "setPosterFrame",
      value: function setPosterFrame(url) {
        this._posterFrame = url;
      }
    }, {
      key: "setAutoplay",
      value: function setAutoplay(auto) {
        this._autoplay = auto;
      }
    }, {
      key: "setRect",
      value: function setRect(rect, animate) {
        this._rect = JSON.parse(JSON.stringify(rect));
        var relativeSize = new paella.RelativeVideoSize();
        var percentTop = relativeSize.percentVSize(rect.top) + '%';
        var percentLeft = relativeSize.percentWSize(rect.left) + '%';
        var percentWidth = relativeSize.percentWSize(rect.width) + '%';
        var percentHeight = relativeSize.percentVSize(rect.height) + '%';
        var style = {
          top: percentTop,
          left: percentLeft,
          width: percentWidth,
          height: percentHeight,
          position: 'absolute'
        };

        if (animate) {
          this.disableClassName();
          var thisClass = this;
          $('#' + this.identifier).animate(style, 400, function () {
            thisClass.enableClassName();
            paella.events.trigger(paella.events.setComposition, {
              video: thisClass
            });
          });
          this.enableClassNameAfter(400);
        } else {
          $('#' + this.identifier).css(style);
          paella.events.trigger(paella.events.setComposition, {
            video: this
          });
        }
      }
    }, {
      key: "setVisible",
      value: function setVisible(visible, animate) {
        if (visible == "true" && animate) {
          $('#' + this.identifier).show();
          $('#' + this.identifier).animate({
            opacity: 1.0
          }, 300);
        } else if (visible == "true" && !animate) {
          $('#' + this.identifier).show();
        } else if (visible == "false" && animate) {
          $('#' + this.identifier).animate({
            opacity: 0.0
          }, 300);
        } else if (visible == "false" && !animate) {
          $('#' + this.identifier).hide();
        }
      }
    }, {
      key: "setLayer",
      value: function setLayer(layer) {
        $('#' + this.identifier).css({
          zIndex: layer
        });
      }
    }, {
      key: "load",
      value: function load() {
        var _this232 = this;

        var This = this;
        return new Promise(function (resolve, reject) {
          _this232._qualityListReadyPromise = $.Deferred();
          paella.youtubePlayerVars.apiReadyPromise.then(function () {
            var stream = _this232._stream.sources.youtube[0];

            if (stream) {
              // TODO: poster frame
              _this232._youtubePlayer = new YT.Player(This.identifier, {
                height: '390',
                width: '640',
                videoId: stream.id,
                playerVars: {
                  controls: 0,
                  disablekb: 1
                },
                events: {
                  onReady: function onReady(e) {
                    This._readyPromise.resolve();
                  },
                  onStateChanged: function onStateChanged(e) {
                    console.log("state changed");
                  },
                  onPlayerStateChange: function onPlayerStateChange(e) {
                    console.log("state changed");
                  }
                }
              });
              resolve();
            } else {
              reject(new Error("Could not load video: invalid quality stream index"));
            }
          });
        });
      }
    }, {
      key: "getQualities",
      value: function getQualities() {
        var This = this;
        return new Promise(function (resolve, reject) {
          This._qualityListReadyPromise.then(function (q) {
            var result = [];
            var index = -1;
            This._qualities = {};
            q.forEach(function (item) {
              index++;
              This._qualities[item] = This._getQualityObject(index, item);
              result.push(This._qualities[item]);
            });
            resolve(result);
          });
        });
      }
    }, {
      key: "setQuality",
      value: function setQuality(index) {
        var _this233 = this;

        return new Promise(function (resolve, reject) {
          _this233._qualityListReadyPromise.then(function (q) {
            for (var key in _this233._qualities) {
              var searchQ = _this233._qualities[key];

              if (_typeof(searchQ) == "object" && searchQ.index == index) {
                _this233.video.setPlaybackQuality(searchQ.label);

                break;
              }
            }

            resolve();
          });
        });
      }
    }, {
      key: "getCurrentQuality",
      value: function getCurrentQuality() {
        var _this234 = this;

        return new Promise(function (resolve, reject) {
          _this234._qualityListReadyPromise.then(function (q) {
            resolve(_this234._qualities[_this234.video.getPlaybackQuality()]);
          });
        });
      }
    }, {
      key: "play",
      value: function play() {
        var _this235 = this;

        var This = this;
        return new Promise(function (resolve, reject) {
          This._playing = true;
          This.video.playVideo();
          new paella.utils.Timer(function (timer) {
            var q = _this235.video.getAvailableQualityLevels();

            if (q.length) {
              timer.repeat = false;

              _this235._qualityListReadyPromise.resolve(q);

              resolve();
            } else {
              timer.repeat = true;
            }
          }, 500);
        });
      }
    }, {
      key: "pause",
      value: function pause() {
        var _this236 = this;

        return this._deferredAction(function () {
          _this236._playing = false;

          _this236.video.pauseVideo();
        });
      }
    }, {
      key: "isPaused",
      value: function isPaused() {
        var _this237 = this;

        return this._deferredAction(function () {
          return !_this237._playing;
        });
      }
    }, {
      key: "duration",
      value: function duration() {
        var _this238 = this;

        return this._deferredAction(function () {
          return _this238.video.getDuration();
        });
      }
    }, {
      key: "setCurrentTime",
      value: function setCurrentTime(time) {
        var _this239 = this;

        return this._deferredAction(function () {
          _this239.video.seekTo(time);
        });
      }
    }, {
      key: "currentTime",
      value: function currentTime() {
        var _this240 = this;

        return this._deferredAction(function () {
          return _this240.video.getCurrentTime();
        });
      }
    }, {
      key: "setVolume",
      value: function setVolume(volume) {
        var _this241 = this;

        return this._deferredAction(function () {
          _this241.video.setVolume && _this241.video.setVolume(volume * 100);
        });
      }
    }, {
      key: "volume",
      value: function volume() {
        var _this242 = this;

        return this._deferredAction(function () {
          return _this242.video.getVolume() / 100;
        });
      }
    }, {
      key: "setPlaybackRate",
      value: function setPlaybackRate(rate) {
        var _this243 = this;

        return this._deferredAction(function () {
          _this243.video.playbackRate = rate;
        });
      }
    }, {
      key: "playbackRate",
      value: function playbackRate() {
        var _this244 = this;

        return this._deferredAction(function () {
          return _this244.video.playbackRate;
        });
      }
    }, {
      key: "goFullScreen",
      value: function goFullScreen() {
        var _this245 = this;

        return this._deferredAction(function () {
          var elem = _this245.video;

          if (elem.requestFullscreen) {
            elem.requestFullscreen();
          } else if (elem.msRequestFullscreen) {
            elem.msRequestFullscreen();
          } else if (elem.mozRequestFullScreen) {
            elem.mozRequestFullScreen();
          } else if (elem.webkitEnterFullscreen) {
            elem.webkitEnterFullscreen();
          }
        });
      }
    }, {
      key: "unFreeze",
      value: function unFreeze() {
        var _this246 = this;

        return this._deferredAction(function () {
          var c = document.getElementById(_this246.video.className + "canvas");
          $(c).remove();
        });
      }
    }, {
      key: "freeze",
      value: function freeze() {
        var _this247 = this;

        return this._deferredAction(function () {
          var canvas = document.createElement("canvas");
          canvas.id = _this247.video.className + "canvas";
          canvas.width = _this247.video.videoWidth;
          canvas.height = _this247.video.videoHeight;
          canvas.style.cssText = _this247.video.style.cssText;
          canvas.style.zIndex = 2;
          var ctx = canvas.getContext("2d");
          ctx.drawImage(_this247.video, 0, 0, Math.ceil(canvas.width / 16) * 16, Math.ceil(canvas.height / 16) * 16); //Draw image

          _this247.video.parentElement.appendChild(canvas);
        });
      }
    }, {
      key: "unload",
      value: function unload() {
        this._callUnloadEvent();

        return paella_DeferredNotImplemented();
      }
    }, {
      key: "getDimensions",
      value: function getDimensions() {
        return paella_DeferredNotImplemented();
      }
    }, {
      key: "video",
      get: function get() {
        return this._youtubePlayer;
      }
    }]);

    return YoutubeVideo;
  }(paella.VideoElementBase);

  paella.YoutubeVideo = YoutubeVideo;

  var YoutubeVideoFactory = /*#__PURE__*/function (_paella$VideoFactory7) {
    _inherits(YoutubeVideoFactory, _paella$VideoFactory7);

    var _super120 = _createSuper(YoutubeVideoFactory);

    function YoutubeVideoFactory() {
      _classCallCheck(this, YoutubeVideoFactory);

      return _super120.apply(this, arguments);
    }

    _createClass(YoutubeVideoFactory, [{
      key: "initYoutubeApi",
      value: function initYoutubeApi() {
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
    }, {
      key: "isStreamCompatible",
      value: function isStreamCompatible(streamData) {
        try {
          for (var key in streamData.sources) {
            if (key == 'youtube') return true;
          }
        } catch (e) {}

        return false;
      }
    }, {
      key: "getVideoObject",
      value: function getVideoObject(id, streamData, rect) {
        this.initYoutubeApi();
        return new paella.YoutubeVideo(id, streamData, rect.x, rect.y, rect.w, rect.h);
      }
    }]);

    return YoutubeVideoFactory;
  }(paella.VideoFactory);

  paella.videoFactories.YoutubeVideoFactory = YoutubeVideoFactory; //paella.youtubePlayerVars = {
  //	apiReadyPromise: $.Promise()
  //};
})();

function onYouTubeIframeAPIReady() {
  //	console.log("Youtube iframe API ready");
  paella.youtubePlayerVars.apiReadyPromise.resolve();
}

paella.addPlugin(function () {
  return /*#__PURE__*/function (_paella$userTracking$5) {
    _inherits(MatomoTracking, _paella$userTracking$5);

    var _super121 = _createSuper(MatomoTracking);

    function MatomoTracking() {
      _classCallCheck(this, MatomoTracking);

      return _super121.apply(this, arguments);
    }

    _createClass(MatomoTracking, [{
      key: "getName",
      value: function getName() {
        return "org.opencast.usertracking.MatomoSaverPlugIn";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
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
          if (isTrackingPermission() && !tracked && server && site_id) {
            if (server.substr(-1) != '/') server += '/';

            paella.require(server + tracking_client + ".js").then(function (matomo) {
              paella.log.debug("Matomo Analytics Enabled");
              paella.userTracking.matomotracker = Matomo.getAsyncTracker(server + tracking_client + ".php", site_id);
              paella.userTracking.matomotracker.client_id = thisClass.config.client_id;
              if (heartbeat && heartbeat > 0) paella.userTracking.matomotracker.enableHeartBeatTimer(heartbeat);

              if (Matomo && Matomo.MediaAnalytics) {
                paella.events.bind(paella.events.videoReady, function () {
                  Matomo.MediaAnalytics.scanForMedia();
                });
              }

              thisClass.registerVisit();
              tracked = true;
            });

            return true;
          } else {
            paella.log.debug("No tracking permission or no Matomo Site ID found in config file. Disabling Matomo Analytics PlugIn");
            return false;
          }
        }

        function initCookieNotification() {
          // load cookieconsent lib from remote server
          paella.require(paella.baseUrl + 'javascript/cookieconsent.min.js').then(function () {
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
              onInitialise: function onInitialise(status) {
                var type = this.options.type;
                var didConsent = this.hasConsented(); // enable cookies - send user data to the platform
                // only if the user enabled cookies

                if (type == 'opt-in' && didConsent) {
                  setTrackingPermission(true);
                } else {
                  setTrackingPermission(false);
                }
              },
              onStatusChange: function onStatusChange(status, chosenBefore) {
                var type = this.options.type;
                var didConsent = this.hasConsented(); // enable cookies - send user data to the platform
                // only if the user enabled cookies

                setTrackingPermission(type == 'opt-in' && didConsent);
              },
              onRevokeChoice: function onRevokeChoice() {
                var type = this.options.type;
                var didConsent = this.hasConsented(); // disable cookies - set what to do when
                // the user revokes cookie usage

                setTrackingPermission(type == 'opt-in' && didConsent);
              }
            });
          });
        }

        function initTranslate(language, funcSuccess, funcError) {
          paella.log.debug('Matomo: selecting language ' + language.slice(0, 2));
          var jsonstr = window.location.origin + '/player/localization/paella_' + language.slice(0, 2) + '.json';
          $.ajax({
            url: jsonstr,
            dataType: 'json',
            success: function success(data) {
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
            error: function error() {
              if (funcError) {
                funcError();
              }
            }
          });
        }

        function translate(str, strIfNotFound) {
          return translations[str] != undefined ? translations[str] : strIfNotFound;
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
        }

        ;

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

    }, {
      key: "registerVisit",
      value: function registerVisit() {
        var title, event_id, series_title, series_id, presenter, view_mode;

        if (paella.opencast && paella.opencast._episode) {
          title = paella.opencast._episode.dcTitle;
          event_id = paella.opencast._episode.id;
          presenter = paella.opencast._episode.dcCreator;
          paella.userTracking.matomotracker.setCustomVariable(5, "client", paella.userTracking.matomotracker.client_id || "Paella Opencast");
        } else {
          paella.userTracking.matomotracker.setCustomVariable(5, "client", paella.userTracking.matomotracker.client_id || "Paella Standalone");
        }

        if (paella.opencast && paella.opencast._episode && paella.opencast._episode.mediapackage) {
          series_id = paella.opencast._episode.mediapackage.series;
          series_title = paella.opencast._episode.mediapackage.seriestitle;
        }

        if (title) paella.userTracking.matomotracker.setCustomVariable(1, "event", title + " (" + event_id + ")", "page");
        if (series_title) paella.userTracking.matomotracker.setCustomVariable(2, "series", series_title + " (" + series_id + ")", "page");
        if (presenter) paella.userTracking.matomotracker.setCustomVariable(3, "presenter", presenter, "page");
        paella.userTracking.matomotracker.setCustomVariable(4, "view_mode", view_mode, "page");

        if (title && presenter) {
          paella.userTracking.matomotracker.setDocumentTitle(title + " - " + (presenter || "Unknown"));
          paella.userTracking.matomotracker.trackPageView(title + " - " + (presenter || "Unknown"));
        } else {
          paella.userTracking.matomotracker.trackPageView();
        }
      }
    }, {
      key: "loadTitle",
      value: function loadTitle() {
        var title = paella.player.videoLoader.getMetadata() && paella.player.videoLoader.getMetadata().title;
        return title;
      }
    }, {
      key: "log",
      value: function log(event, params) {
        if (paella.userTracking.matomotracker === undefined) {
          paella.log.debug("Matomo Tracker is missing");
          return;
        }

        if (this.config.category === undefined || this.config.category === true) {
          var value = "";

          try {
            value = JSON.stringify(params);
          } catch (e) {}

          switch (event) {
            case paella.events.play:
              paella.userTracking.matomotracker.trackEvent("Player.Controls", "Play", this.loadTitle());
              break;

            case paella.events.pause:
              paella.userTracking.matomotracker.trackEvent("Player.Controls", "Pause", this.loadTitle());
              break;

            case paella.events.endVideo:
              paella.userTracking.matomotracker.trackEvent("Player.Status", "Ended", this.loadTitle());
              break;

            case paella.events.showEditor:
              paella.userTracking.matomotracker.trackEvent("Player.Editor", "Show", this.loadTitle());
              break;

            case paella.events.hideEditor:
              paella.userTracking.matomotracker.trackEvent("Player.Editor", "Hide", this.loadTitle());
              break;

            case paella.events.enterFullscreen:
              paella.userTracking.matomotracker.trackEvent("Player.View", "Fullscreen", this.loadTitle());
              break;

            case paella.events.exitFullscreen:
              paella.userTracking.matomotracker.trackEvent("Player.View", "ExitFullscreen", this.loadTitle());
              break;

            case paella.events.loadComplete:
              paella.userTracking.matomotracker.trackEvent("Player.Status", "LoadComplete", this.loadTitle());
              break;

            case paella.events.showPopUp:
              paella.userTracking.matomotracker.trackEvent("Player.PopUp", "Show", value);
              break;

            case paella.events.hidePopUp:
              paella.userTracking.matomotracker.trackEvent("Player.PopUp", "Hide", value);
              break;

            case paella.events.captionsEnabled:
              paella.userTracking.matomotracker.trackEvent("Player.Captions", "Enabled", value);
              break;

            case paella.events.captionsDisabled:
              paella.userTracking.matomotracker.trackEvent("Player.Captions", "Disabled", value);
              break;

            case paella.events.setProfile:
              paella.userTracking.matomotracker.trackEvent("Player.View", "Profile", value);
              break;

            case paella.events.seekTo:
            case paella.events.seekToTime:
              paella.userTracking.matomotracker.trackEvent("Player.Controls", "Seek", value);
              break;

            case paella.events.setVolume:
              paella.userTracking.matomotracker.trackEvent("Player.Settings", "Volume", value);
              break;

            case paella.events.resize:
              paella.userTracking.matomotracker.trackEvent("Player.View", "resize", value);
              break;

            case paella.events.setPlaybackRate:
              paella.userTracking.matomotracker.trackEvent("Player.Controls", "PlaybackRate", value);
              break;
          }
        }
      }
    }]);

    return MatomoTracking;
  }(paella.userTracking.SaverPlugIn);
});
paella.addPlugin(function () {
  var self = this;
  return /*#__PURE__*/function (_paella$userTracking$6) {
    _inherits(X5gonTracking, _paella$userTracking$6);

    var _super122 = _createSuper(X5gonTracking);

    function X5gonTracking() {
      _classCallCheck(this, X5gonTracking);

      return _super122.apply(this, arguments);
    }

    _createClass(X5gonTracking, [{
      key: "getName",
      value: function getName() {
        return "org.opencast.usertracking.x5gonSaverPlugIn";
      }
    }, {
      key: "checkEnabled",
      value: function checkEnabled(onSuccess) {
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
            } else {
              // load x5gon lib from remote server
              paella.log.debug("X5GON: trackX5gon loading x5gon-snippet, token: " + token);

              paella.require("https://platform.x5gon.org/api/v1/snippet/latest/x5gon-log.min.js").then(function (x5gon) {
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
          paella.require(urlCookieconsentJS).then(function (cookieconsent) {
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
              onInitialise: function onInitialise(status) {
                var type = this.options.type;
                var didConsent = this.hasConsented(); // enable cookies - send user data to the platform
                // only if the user enabled cookies

                if (type == 'opt-in' && didConsent) {
                  setTrackingPermission(true);
                } else {
                  setTrackingPermission(false);
                }
              },
              onStatusChange: function onStatusChange(status, chosenBefore) {
                var type = this.options.type;
                var didConsent = this.hasConsented(); // enable cookies - send user data to the platform
                // only if the user enabled cookies

                if (type == 'opt-in' && didConsent) {
                  setTrackingPermission(true);
                } else {
                  setTrackingPermission(false);
                }
              },
              onRevokeChoice: function onRevokeChoice() {
                var type = this.options.type;
                var didConsent = this.hasConsented(); // disable cookies - set what to do when
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
          paella.log.debug('X5GON: selecting language ' + language.slice(0, 2));
          var jsonstr = window.location.origin + '/player/localization/paella_' + language.slice(0, 2) + '.json';
          $.ajax({
            url: jsonstr,
            dataType: 'json',
            success: function success(data) {
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
            error: function error() {
              if (funcError) {
                funcError();
              }
            }
          });
        }

        function translate(str, strIfNotFound) {
          return translations[str] != undefined ? translations[str] : strIfNotFound;
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
        }

        ;
        initTranslate(navigator.language, function () {
          paella.log.debug('X5GON: Successfully translated.');
          initCookieNotification();
        }, function () {
          paella.log.debug('X5gon: Error translating.');
          initCookieNotification();
        });
        trackX5gon();
        onSuccess(true);
      }
    }, {
      key: "log",
      value: function log(event, params) {
        if (this.config.category === undefined || this.config.category === true) {
          var category = this.config.category || "PaellaPlayer";
          var action = event;
          var label = "";

          try {
            label = JSON.stringify(params);
          } catch (e) {}
        }
      }
    }]);

    return X5gonTracking;
  }(paella.userTracking.SaverPlugIn);
});