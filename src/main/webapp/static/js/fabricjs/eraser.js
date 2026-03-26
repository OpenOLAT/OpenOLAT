//import * as fabric from 'fabric';
//import { classRegistry, LayoutManager, FixedLayout, Path, Group } from 'fabric';

function _OverloadYield(e, d) {
  this.v = e, this.k = d;
}
function _arrayLikeToArray(r, a) {
  (null == a || a > r.length) && (a = r.length);
  for (var e = 0, n = Array(a); e < a; e++) n[e] = r[e];
  return n;
}
function _arrayWithHoles(r) {
  if (Array.isArray(r)) return r;
}
function _arrayWithoutHoles(r) {
  if (Array.isArray(r)) return _arrayLikeToArray(r);
}
function _assertThisInitialized(e) {
  if (void 0 === e) throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
  return e;
}
function asyncGeneratorStep(n, t, e, r, o, a, c) {
  try {
    var i = n[a](c),
      u = i.value;
  } catch (n) {
    return void e(n);
  }
  i.done ? t(u) : Promise.resolve(u).then(r, o);
}
function _asyncToGenerator(n) {
  return function () {
    var t = this,
      e = arguments;
    return new Promise(function (r, o) {
      var a = n.apply(t, e);
      function _next(n) {
        asyncGeneratorStep(a, r, o, _next, _throw, "next", n);
      }
      function _throw(n) {
        asyncGeneratorStep(a, r, o, _next, _throw, "throw", n);
      }
      _next(void 0);
    });
  };
}
function _callSuper(t, o, e) {
  return o = _getPrototypeOf(o), _possibleConstructorReturn(t, _isNativeReflectConstruct() ? Reflect.construct(o, e || [], _getPrototypeOf(t).constructor) : o.apply(t, e));
}
function _classCallCheck(a, n) {
  if (!(a instanceof n)) throw new TypeError("Cannot call a class as a function");
}
function _defineProperties(e, r) {
  for (var t = 0; t < r.length; t++) {
    var o = r[t];
    o.enumerable = o.enumerable || false, o.configurable = true, "value" in o && (o.writable = true), Object.defineProperty(e, _toPropertyKey(o.key), o);
  }
}
function _createClass(e, r, t) {
  return r && _defineProperties(e.prototype, r), Object.defineProperty(e, "prototype", {
    writable: false
  }), e;
}
function _defineProperty(e, r, t) {
  return (r = _toPropertyKey(r)) in e ? Object.defineProperty(e, r, {
    value: t,
    enumerable: true,
    configurable: true,
    writable: true
  }) : e[r] = t, e;
}
function _get() {
  return _get = "undefined" != typeof Reflect && Reflect.get ? Reflect.get.bind() : function (e, t, r) {
    var p = _superPropBase(e, t);
    if (p) {
      var n = Object.getOwnPropertyDescriptor(p, t);
      return n.get ? n.get.call(arguments.length < 3 ? e : r) : n.value;
    }
  }, _get.apply(null, arguments);
}
function _getPrototypeOf(t) {
  return _getPrototypeOf = Object.setPrototypeOf ? Object.getPrototypeOf.bind() : function (t) {
    return t.__proto__ || Object.getPrototypeOf(t);
  }, _getPrototypeOf(t);
}
function _inherits(t, e) {
  if ("function" != typeof e && null !== e) throw new TypeError("Super expression must either be null or a function");
  t.prototype = Object.create(e && e.prototype, {
    constructor: {
      value: t,
      writable: true,
      configurable: true
    }
  }), Object.defineProperty(t, "prototype", {
    writable: false
  }), e && _setPrototypeOf(t, e);
}
function _isNativeReflectConstruct() {
  try {
    var t = !Boolean.prototype.valueOf.call(Reflect.construct(Boolean, [], function () {}));
  } catch (t) {}
  return (_isNativeReflectConstruct = function () {
    return !!t;
  })();
}
function _iterableToArray(r) {
  if ("undefined" != typeof Symbol && null != r[Symbol.iterator] || null != r["@@iterator"]) return Array.from(r);
}
function _iterableToArrayLimit(r, l) {
  var t = null == r ? null : "undefined" != typeof Symbol && r[Symbol.iterator] || r["@@iterator"];
  if (null != t) {
    var e,
      n,
      i,
      u,
      a = [],
      f = true,
      o = false;
    try {
      if (i = (t = t.call(r)).next, 0 === l) {
        if (Object(t) !== t) return;
        f = !1;
      } else for (; !(f = (e = i.call(t)).done) && (a.push(e.value), a.length !== l); f = !0);
    } catch (r) {
      o = true, n = r;
    } finally {
      try {
        if (!f && null != t.return && (u = t.return(), Object(u) !== u)) return;
      } finally {
        if (o) throw n;
      }
    }
    return a;
  }
}
function _nonIterableRest() {
  throw new TypeError("Invalid attempt to destructure non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method.");
}
function _nonIterableSpread() {
  throw new TypeError("Invalid attempt to spread non-iterable instance.\nIn order to be iterable, non-array objects must have a [Symbol.iterator]() method.");
}
function ownKeys(e, r) {
  var t = Object.keys(e);
  if (Object.getOwnPropertySymbols) {
    var o = Object.getOwnPropertySymbols(e);
    r && (o = o.filter(function (r) {
      return Object.getOwnPropertyDescriptor(e, r).enumerable;
    })), t.push.apply(t, o);
  }
  return t;
}
function _objectSpread2(e) {
  for (var r = 1; r < arguments.length; r++) {
    var t = null != arguments[r] ? arguments[r] : {};
    r % 2 ? ownKeys(Object(t), true).forEach(function (r) {
      _defineProperty(e, r, t[r]);
    }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(e, Object.getOwnPropertyDescriptors(t)) : ownKeys(Object(t)).forEach(function (r) {
      Object.defineProperty(e, r, Object.getOwnPropertyDescriptor(t, r));
    });
  }
  return e;
}
function _possibleConstructorReturn(t, e) {
  if (e && ("object" == typeof e || "function" == typeof e)) return e;
  if (void 0 !== e) throw new TypeError("Derived constructors may only return object or undefined");
  return _assertThisInitialized(t);
}
function _regenerator() {
  /*! regenerator-runtime -- Copyright (c) 2014-present, Facebook, Inc. -- license (MIT): https://github.com/babel/babel/blob/main/packages/babel-helpers/LICENSE */
  var e,
    t,
    r = "function" == typeof Symbol ? Symbol : {},
    n = r.iterator || "@@iterator",
    o = r.toStringTag || "@@toStringTag";
  function i(r, n, o, i) {
    var c = n && n.prototype instanceof Generator ? n : Generator,
      u = Object.create(c.prototype);
    return _regeneratorDefine(u, "_invoke", function (r, n, o) {
      var i,
        c,
        u,
        f = 0,
        p = o || [],
        y = false,
        G = {
          p: 0,
          n: 0,
          v: e,
          a: d,
          f: d.bind(e, 4),
          d: function (t, r) {
            return i = t, c = 0, u = e, G.n = r, a;
          }
        };
      function d(r, n) {
        for (c = r, u = n, t = 0; !y && f && !o && t < p.length; t++) {
          var o,
            i = p[t],
            d = G.p,
            l = i[2];
          r > 3 ? (o = l === n) && (u = i[(c = i[4]) ? 5 : (c = 3, 3)], i[4] = i[5] = e) : i[0] <= d && ((o = r < 2 && d < i[1]) ? (c = 0, G.v = n, G.n = i[1]) : d < l && (o = r < 3 || i[0] > n || n > l) && (i[4] = r, i[5] = n, G.n = l, c = 0));
        }
        if (o || r > 1) return a;
        throw y = true, n;
      }
      return function (o, p, l) {
        if (f > 1) throw TypeError("Generator is already running");
        for (y && 1 === p && d(p, l), c = p, u = l; (t = c < 2 ? e : u) || !y;) {
          i || (c ? c < 3 ? (c > 1 && (G.n = -1), d(c, u)) : G.n = u : G.v = u);
          try {
            if (f = 2, i) {
              if (c || (o = "next"), t = i[o]) {
                if (!(t = t.call(i, u))) throw TypeError("iterator result is not an object");
                if (!t.done) return t;
                u = t.value, c < 2 && (c = 0);
              } else 1 === c && (t = i.return) && t.call(i), c < 2 && (u = TypeError("The iterator does not provide a '" + o + "' method"), c = 1);
              i = e;
            } else if ((t = (y = G.n < 0) ? u : r.call(n, G)) !== a) break;
          } catch (t) {
            i = e, c = 1, u = t;
          } finally {
            f = 1;
          }
        }
        return {
          value: t,
          done: y
        };
      };
    }(r, o, i), true), u;
  }
  var a = {};
  function Generator() {}
  function GeneratorFunction() {}
  function GeneratorFunctionPrototype() {}
  t = Object.getPrototypeOf;
  var c = [][n] ? t(t([][n]())) : (_regeneratorDefine(t = {}, n, function () {
      return this;
    }), t),
    u = GeneratorFunctionPrototype.prototype = Generator.prototype = Object.create(c);
  function f(e) {
    return Object.setPrototypeOf ? Object.setPrototypeOf(e, GeneratorFunctionPrototype) : (e.__proto__ = GeneratorFunctionPrototype, _regeneratorDefine(e, o, "GeneratorFunction")), e.prototype = Object.create(u), e;
  }
  return GeneratorFunction.prototype = GeneratorFunctionPrototype, _regeneratorDefine(u, "constructor", GeneratorFunctionPrototype), _regeneratorDefine(GeneratorFunctionPrototype, "constructor", GeneratorFunction), GeneratorFunction.displayName = "GeneratorFunction", _regeneratorDefine(GeneratorFunctionPrototype, o, "GeneratorFunction"), _regeneratorDefine(u), _regeneratorDefine(u, o, "Generator"), _regeneratorDefine(u, n, function () {
    return this;
  }), _regeneratorDefine(u, "toString", function () {
    return "[object Generator]";
  }), (_regenerator = function () {
    return {
      w: i,
      m: f
    };
  })();
}
function _regeneratorAsync(n, e, r, t, o) {
  var a = _regeneratorAsyncGen(n, e, r, t, o);
  return a.next().then(function (n) {
    return n.done ? n.value : a.next();
  });
}
function _regeneratorAsyncGen(r, e, t, o, n) {
  return new _regeneratorAsyncIterator(_regenerator().w(r, e, t, o), n || Promise);
}
function _regeneratorAsyncIterator(t, e) {
  function n(r, o, i, f) {
    try {
      var c = t[r](o),
        u = c.value;
      return u instanceof _OverloadYield ? e.resolve(u.v).then(function (t) {
        n("next", t, i, f);
      }, function (t) {
        n("throw", t, i, f);
      }) : e.resolve(u).then(function (t) {
        c.value = t, i(c);
      }, function (t) {
        return n("throw", t, i, f);
      });
    } catch (t) {
      f(t);
    }
  }
  var r;
  this.next || (_regeneratorDefine(_regeneratorAsyncIterator.prototype), _regeneratorDefine(_regeneratorAsyncIterator.prototype, "function" == typeof Symbol && Symbol.asyncIterator || "@asyncIterator", function () {
    return this;
  })), _regeneratorDefine(this, "_invoke", function (t, o, i) {
    function f() {
      return new e(function (e, r) {
        n(t, i, e, r);
      });
    }
    return r = r ? r.then(f, f) : f();
  }, true);
}
function _regeneratorDefine(e, r, n, t) {
  var i = Object.defineProperty;
  try {
    i({}, "", {});
  } catch (e) {
    i = 0;
  }
  _regeneratorDefine = function (e, r, n, t) {
    function o(r, n) {
      _regeneratorDefine(e, r, function (e) {
        return this._invoke(r, n, e);
      });
    }
    r ? i ? i(e, r, {
      value: n,
      enumerable: !t,
      configurable: !t,
      writable: !t
    }) : e[r] = n : (o("next", 0), o("throw", 1), o("return", 2));
  }, _regeneratorDefine(e, r, n, t);
}
function _regeneratorKeys(e) {
  var n = Object(e),
    r = [];
  for (var t in n) r.unshift(t);
  return function e() {
    for (; r.length;) if ((t = r.pop()) in n) return e.value = t, e.done = false, e;
    return e.done = true, e;
  };
}
function _regeneratorValues(e) {
  if (null != e) {
    var t = e["function" == typeof Symbol && Symbol.iterator || "@@iterator"],
      r = 0;
    if (t) return t.call(e);
    if ("function" == typeof e.next) return e;
    if (!isNaN(e.length)) return {
      next: function () {
        return e && r >= e.length && (e = void 0), {
          value: e && e[r++],
          done: !e
        };
      }
    };
  }
  throw new TypeError(typeof e + " is not iterable");
}
function _setPrototypeOf(t, e) {
  return _setPrototypeOf = Object.setPrototypeOf ? Object.setPrototypeOf.bind() : function (t, e) {
    return t.__proto__ = e, t;
  }, _setPrototypeOf(t, e);
}
function _slicedToArray(r, e) {
  return _arrayWithHoles(r) || _iterableToArrayLimit(r, e) || _unsupportedIterableToArray(r, e) || _nonIterableRest();
}
function _superPropBase(t, o) {
  for (; !{}.hasOwnProperty.call(t, o) && null !== (t = _getPrototypeOf(t)););
  return t;
}
function _toConsumableArray(r) {
  return _arrayWithoutHoles(r) || _iterableToArray(r) || _unsupportedIterableToArray(r) || _nonIterableSpread();
}
function _toPrimitive(t, r) {
  if ("object" != typeof t || !t) return t;
  var e = t[Symbol.toPrimitive];
  if (void 0 !== e) {
    var i = e.call(t, r);
    if ("object" != typeof i) return i;
    throw new TypeError("@@toPrimitive must return a primitive value.");
  }
  return ("string" === r ? String : Number)(t);
}
function _toPropertyKey(t) {
  var i = _toPrimitive(t, "string");
  return "symbol" == typeof i ? i : i + "";
}
function _unsupportedIterableToArray(r, a) {
  if (r) {
    if ("string" == typeof r) return _arrayLikeToArray(r, a);
    var t = {}.toString.call(r).slice(8, -1);
    return "Object" === t && r.constructor && (t = r.constructor.name), "Map" === t || "Set" === t ? Array.from(r) : "Arguments" === t || /^(?:Ui|I)nt(?:8|16|32)(?:Clamped)?Array$/.test(t) ? _arrayLikeToArray(r, a) : void 0;
  }
}
function _regeneratorRuntime() {

  var r = _regenerator(),
    e = r.m(_regeneratorRuntime),
    t = (Object.getPrototypeOf ? Object.getPrototypeOf(e) : e.__proto__).constructor;
  function n(r) {
    var e = "function" == typeof r && r.constructor;
    return !!e && (e === t || "GeneratorFunction" === (e.displayName || e.name));
  }
  var o = {
    throw: 1,
    return: 2,
    break: 3,
    continue: 3
  };
  function a(r) {
    var e, t;
    return function (n) {
      e || (e = {
        stop: function () {
          return t(n.a, 2);
        },
        catch: function () {
          return n.v;
        },
        abrupt: function (r, e) {
          return t(n.a, o[r], e);
        },
        delegateYield: function (r, o, a) {
          return e.resultName = o, t(n.d, _regeneratorValues(r), a);
        },
        finish: function (r) {
          return t(n.f, r);
        }
      }, t = function (r, t, o) {
        n.p = e.prev, n.n = e.next;
        try {
          return r(t, o);
        } finally {
          e.next = n.n;
        }
      }), e.resultName && (e[e.resultName] = n.v, e.resultName = void 0), e.sent = n.v, e.next = n.n;
      try {
        return r.call(this, e);
      } finally {
        n.p = e.prev, n.n = e.next;
      }
    };
  }
  return (_regeneratorRuntime = function () {
    return {
      wrap: function (e, t, n, o) {
        return r.w(a(e), t, n, o && o.reverse());
      },
      isGeneratorFunction: n,
      mark: r.m,
      awrap: function (r, e) {
        return new _OverloadYield(r, e);
      },
      AsyncIterator: _regeneratorAsyncIterator,
      async: function (r, e, t, o, u) {
        return (n(e) ? _regeneratorAsyncGen : _regeneratorAsync)(a(r), e, t, o, u);
      },
      keys: _regeneratorKeys,
      values: _regeneratorValues
    };
  })();
}

var ClippingGroup = /*#__PURE__*/function (_Group) {
  _inherits(ClippingGroup, _Group);
  function ClippingGroup(objects, options) {
    var _this;
    _classCallCheck(this, ClippingGroup);
    _this = _callSuper(this, ClippingGroup, [objects, _objectSpread2({
      originX: 'center',
      originY: 'center',
      left: 0,
      top: 0,
      layoutManager: new fabric.LayoutManager(new fabric.FixedLayout())
    }, options)]);
    _defineProperty(_assertThisInitialized(_this), "blockErasing", false);
    return _this;
  }
  _createClass(ClippingGroup, [{
    key: "drawObject",
    value: function drawObject(ctx) {
      var paths = [];
      var objects = [];
      this._objects.forEach(function (object) {
        return (object instanceof fabric.Path ? paths : objects).push(object);
      });
      ctx.save();
      ctx.fillStyle = 'black';
      ctx.fillRect(-this.width / 2, -this.height / 2, this.width, this.height);
      ctx.restore();
      !this.blockErasing && paths.forEach(function (path) {
        path.render(ctx);
      });
      objects.forEach(function (object) {
        object.globalCompositeOperation = object.inverted ? 'destination-out' : 'source-in';
        object.render(ctx);
      });
    }
  }]);
  return ClippingGroup;
}(fabric.Group);
_defineProperty(ClippingGroup, "type", 'clipping');
fabric.classRegistry.setClass(ClippingGroup);

var drawImage = function drawImage(destination, source) {
  var globalCompositeOperation = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : 'source-over';
  destination.save();
  destination.imageSmoothingEnabled = true;
  destination.imageSmoothingQuality = 'high';
  destination.globalCompositeOperation = globalCompositeOperation;
  destination.resetTransform();
  destination.drawImage(source.canvas, 0, 0);
  destination.restore();
};

/**
 *
 * @param destination context to erase
 * @param source context on which the path is drawn upon
 * @param erasingEffect effect to apply to {@link source} after clipping {@link destination}:
 * - drawing all non erasable visuals to achieve a selective erasing effect.
 * - drawing all erasable visuals without their erasers to achieve an undo erasing effect.
 */
var erase = function erase(destination, source, erasingEffect) {
  // clip destination
  drawImage(destination, source, 'destination-out');

  // draw erasing effect
  if (erasingEffect) {
    drawImage(source, erasingEffect, 'source-in');
  } else {
    source.save();
    source.resetTransform();
    source.clearRect(0, 0, source.canvas.width, source.canvas.height);
    source.restore();
  }
};

function walk$1(objects) {
  return objects.flatMap(function (object) {
    if (!object.erasable || object.isNotVisible()) {
      return [];
    } else if (object instanceof fabric.Group && object.erasable === 'deep') {
      return walk$1(object.getObjects());
    } else {
      return [object];
    }
  });
}
function drawCanvas(ctx, canvas, objects) {
  canvas.clearContext(ctx);
  ctx.imageSmoothingEnabled = canvas.imageSmoothingEnabled;
  ctx.imageSmoothingQuality = 'high';
  // @ts-expect-error node-canvas stuff
  ctx.patternQuality = 'best';
  canvas._renderBackground(ctx);
  ctx.save();
  ctx.transform.apply(ctx, _toConsumableArray(canvas.viewportTransform));
  objects.forEach(function (object) {
    return object.render(ctx);
  });
  ctx.restore();
  var clipPath = canvas.clipPath;
  if (clipPath) {
    // fabric crap
    clipPath._set('canvas', canvas);
    clipPath.shouldCache();
    clipPath._transformDone = true;
    // @ts-expect-error fabric type crap
    clipPath.renderCache({
      forClipping: true
    });
    canvas.drawClipPathOnCanvas(ctx, clipPath);
  }
  canvas._renderOverlay(ctx);
}

/**
 * Prepare the pattern for the erasing brush
 * This pattern will be drawn on the top context after clipping the main context,
 * achieving a visual effect of erasing only erasable objects.
 *
 * This is designed to support erasing a collection with both erasable and non-erasable objects while maintaining object stacking.\
 * Iterates over collections to allow nested selective erasing.\
 * Prepares objects before rendering the pattern brush.\
 * If brush is **NOT** inverted render all non-erasable objects.\
 * If brush is inverted render all objects, erasable objects without their eraser.
 * This will render the erased parts as if they were not erased in the first place, achieving an undo effect.
 *
 * Caveat:
 * Does not support erasing effects of shadows
 *
 */
function draw(ctx, _ref, _ref2) {
  var inverted = _ref.inverted,
    opacity = _ref.opacity;
  var canvas = _ref2.canvas,
    _ref2$objects = _ref2.objects,
    objects = _ref2$objects === void 0 ? canvas._objectsToRender || canvas._objects : _ref2$objects,
    _ref2$background = _ref2.background,
    background = _ref2$background === void 0 ? canvas.backgroundImage : _ref2$background,
    _ref2$overlay = _ref2.overlay,
    overlay = _ref2$overlay === void 0 ? canvas.overlayImage : _ref2$overlay;
  // prepare tree
  var alpha = 1 - opacity;
  var restore = walk$1([].concat(_toConsumableArray(objects), _toConsumableArray([background, overlay].filter(function (d) {
    return !!d;
  })))).map(function (object) {
    if (!inverted) {
      var _object$parent;
      //  render only non-erasable objects
      var _opacity = object.opacity;
      object.opacity *= alpha;
      (_object$parent = object.parent) === null || _object$parent === void 0 || _object$parent.set('dirty', true);
      return {
        object: object,
        opacity: _opacity
      };
    } else if (object.clipPath instanceof ClippingGroup) {
      //  render all objects without eraser
      object.clipPath['blockErasing'] = true;
      object.clipPath.set('dirty', true);
      object.set('dirty', true);
      return {
        object: object,
        clipPath: object.clipPath
      };
    }
  });

  // draw
  drawCanvas(ctx, canvas, objects);

  // restore
  restore.forEach(function (entry) {
    if (!entry) {
      return;
    }
    if (entry.opacity) {
      var _entry$object$parent;
      entry.object.opacity = entry.opacity;
      (_entry$object$parent = entry.object.parent) === null || _entry$object$parent === void 0 || _entry$object$parent.set('dirty', true);
    } else if (entry.clipPath) {
      entry.clipPath['blockErasing'] = false;
      entry.clipPath.set('dirty', true);
      entry.object.set('dirty', true);
    }
  });
}

function walk(objects, path) {
  return objects.flatMap(function (object) {
    if (!object.erasable || !object.intersectsWithObject(path)) {
      return [];
    } else if (object instanceof fabric.Group && object.erasable === 'deep') {
      return walk(object.getObjects(), path);
    } else {
      return [object];
    }
  });
}
var assertClippingGroup = function assertClippingGroup(object) {
  var curr = object.clipPath;
  if (curr instanceof ClippingGroup) {
    return curr;
  }
  var strokeWidth = object.strokeWidth;
  var strokeWidthFactor = new fabric.Point(strokeWidth, strokeWidth);
  var strokeVector = object.strokeUniform ? strokeWidthFactor.divide(object.getObjectScaling()) : strokeWidthFactor;
  var next = new ClippingGroup([], {
    width: object.width + strokeVector.x,
    height: object.height + strokeVector.y
  });
  if (curr) {
    var _curr$translateToOrig = curr.translateToOriginPoint(new fabric.Point(), curr.originX, curr.originY),
      x = _curr$translateToOrig.x,
      y = _curr$translateToOrig.y;
    curr.originX = curr.originY = 'center';
    fabric.util.sendObjectToPlane(curr, undefined, fabric.util.createTranslateMatrix(x, y));
    next.add(curr);
  }
  return object.clipPath = next;
};
function commitErasing(object, sourceInObjectPlane) {
  var clipPath = assertClippingGroup(object);
  clipPath.add(sourceInObjectPlane);
  clipPath.set('dirty', true);
  object.set('dirty', true);
}
function eraseObject(_x, _x2) {
  return _eraseObject.apply(this, arguments);
}
function _eraseObject() {
  _eraseObject = _asyncToGenerator( /*#__PURE__*/_regeneratorRuntime().mark(function _callee4(object, source) {
    var clone;
    return _regeneratorRuntime().wrap(function _callee4$(_context4) {
      while (1) switch (_context4.prev = _context4.next) {
        case 0:
          _context4.next = 2;
          return source.clone();
        case 2:
          clone = _context4.sent;
          fabric.util.sendObjectToPlane(clone, undefined, object.calcTransformMatrix());
          commitErasing(object, clone);
          return _context4.abrupt("return", clone);
        case 6:
        case "end":
          return _context4.stop();
      }
    }, _callee4);
  }));
  return _eraseObject.apply(this, arguments);
}
function eraseCanvasDrawable(_x3, _x4, _x5) {
  return _eraseCanvasDrawable.apply(this, arguments);
}
function _eraseCanvasDrawable() {
  _eraseCanvasDrawable = _asyncToGenerator( /*#__PURE__*/_regeneratorRuntime().mark(function _callee5(object, vpt, source) {
    var clone, d;
    return _regeneratorRuntime().wrap(function _callee5$(_context5) {
      while (1) switch (_context5.prev = _context5.next) {
        case 0:
          _context5.next = 2;
          return source.clone();
        case 2:
          clone = _context5.sent;
          d = vpt && object.translateToOriginPoint(new fabric.Point(), object.originX, object.originY);
          fabric.util.sendObjectToPlane(clone, undefined, d ? fabric.util.multiplyTransformMatrixArray([[1, 0, 0, 1, d.x, d.y],
          // apply vpt from center of drawable
          vpt, [1, 0, 0, 1, -d.x, -d.y], object.calcTransformMatrix()]) : object.calcTransformMatrix());
          commitErasing(object, clone);
          return _context5.abrupt("return", clone);
        case 7:
        case "end":
          return _context5.stop();
      }
    }, _callee5);
  }));
  return _eraseCanvasDrawable.apply(this, arguments);
}
var setCanvasDimensions = function setCanvasDimensions(el, ctx, _ref) {
  var width = _ref.width,
    height = _ref.height;
  var retinaScaling = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : 1;
  el.width = width;
  el.height = height;
  if (retinaScaling > 1) {
    el.setAttribute('width', (width * retinaScaling).toString());
    el.setAttribute('height', (height * retinaScaling).toString());
    ctx.scale(retinaScaling, retinaScaling);
  }
};

/**
 * Supports **selective** erasing: only erasable objects are affected by the eraser brush.
 *
 * Supports **{@link inverted}** erasing: the brush can "undo" erasing.
 *
 * Supports **alpha** erasing: setting the alpha channel of the `color` property controls the eraser intensity.
 *
 * In order to support selective erasing, the brush clips the entire canvas and
 * masks all non-erasable objects over the erased path, see {@link draw}.
 *
 * If **{@link inverted}** draws all objects, erasable objects without their eraser, over the erased path.
 * This achieves the desired effect of seeming to erase or undo erasing on erasable objects only.
 *
 * After erasing is done the `end` event {@link ErasingEndEvent} is fired, after which erasing will be committed to the tree.
 * @example
 * canvas = new Canvas();
 * const eraser = new EraserBrush(canvas);
 * canvas.freeDrawingBrush = eraser;
 * canvas.isDrawingMode = true;
 * eraser.on('start', (e) => {
 *    console.log('started erasing');
 *    // prevent erasing
 *    e.preventDefault();
 * });
 * eraser.on('end', (e) => {
 *    const { targets: erasedTargets, path } = e.detail;
 *    e.preventDefault(); // prevent erasing being committed to the tree
 *    eraser.commit({ targets: erasedTargets, path }); // commit manually since default was prevented
 * });
 *
 * In case of performance issues trace {@link drawEffect} calls and consider preventing it from executing
 * @example
 * const eraser = new EraserBrush(canvas);
 * eraser.on('redraw', (e) => {
 *    // prevent effect redraw on pointer down (e.g. useful if canvas didn't change)
 *    e.detail.type === 'start' && e.preventDefault());
 *    // prevent effect redraw after canvas has rendered (effect will become stale)
 *    e.detail.type === 'render' && e.preventDefault());
 * });
 */
var EraserBrush = /*#__PURE__*/function (_fabric$PencilBrush) {
  _inherits(EraserBrush, _fabric$PencilBrush);
  function EraserBrush(canvas) {
    var _this;
    _classCallCheck(this, EraserBrush);
    _this = _callSuper(this, EraserBrush, [canvas]);
    /**
     * When set to `true` the brush will create a visual effect of undoing erasing
     */
    _defineProperty(_assertThisInitialized(_this), "inverted", false);
    _defineProperty(_assertThisInitialized(_this), "active", false);
    var el = document.createElement('canvas');
    var ctx = el.getContext('2d');
    if (!ctx) {
      throw new Error('Failed to get context');
    }
    setCanvasDimensions(el, ctx, canvas, _this.canvas.getRetinaScaling());
    _this.effectContext = ctx;
    _this.eventEmitter = new EventTarget();
    return _this;
  }

  /**
   * @returns disposer make sure to call it to avoid memory leaks
   */
  _createClass(EraserBrush, [{
    key: "on",
    value: function on(type, cb, options) {
      var _this2 = this;
      this.eventEmitter.addEventListener(type, cb, options);
      return function () {
        return _this2.eventEmitter.removeEventListener(type, cb, options);
      };
    }
  }, {
    key: "drawEffect",
    value: function drawEffect() {
      draw(this.effectContext, {
        opacity: new fabric.Color(this.color).getAlpha(),
        inverted: this.inverted
      }, {
        canvas: this.canvas
      });
    }

    /**
     * @override
     */
  }, {
    key: "_setBrushStyles",
    value: function _setBrushStyles() {
      var ctx = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : this.canvas.contextTop;
      _get(_getPrototypeOf(EraserBrush.prototype), "_setBrushStyles", this).call(this, ctx);
      ctx.strokeStyle = 'black';
    }

    /**
     * @override strictly speaking the eraser needs a full render only if it has opacity set.
     * However since {@link PencilBrush} is designed for subclassing that is what we have to work with.
     */
  }, {
    key: "needsFullRender",
    value: function needsFullRender() {
      return true;
    }

    /**
     * @override erase
     */
  }, {
    key: "_render",
    value: function _render() {
      var ctx = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : this.canvas.getTopContext();
      _get(_getPrototypeOf(EraserBrush.prototype), "_render", this).call(this, ctx);
      erase(this.canvas.getContext(), ctx, this.effectContext);
    }

    /**
     * @override {@link drawEffect}
     */
  }, {
    key: "onMouseDown",
    value: function onMouseDown(pointer, context) {
      var _this3 = this;
      if (!this.eventEmitter.dispatchEvent(new CustomEvent('start', {
        detail: context,
        cancelable: true
      }))) {
        return;
      }
      this.active = true;
      this.eventEmitter.dispatchEvent(new CustomEvent('redraw', {
        detail: {
          type: 'start'
        },
        cancelable: true
      })) && this.drawEffect();

      // consider a different approach
      this._disposer = this.canvas.on('after:render', function (_ref2) {
        var ctx = _ref2.ctx;
        if (ctx !== _this3.canvas.getContext()) {
          return;
        }
        _this3.eventEmitter.dispatchEvent(new CustomEvent('redraw', {
          detail: {
            type: 'render'
          },
          cancelable: true
        })) && _this3.drawEffect();
        _this3._render();
      });
      _get(_getPrototypeOf(EraserBrush.prototype), "onMouseDown", this).call(this, pointer, context);
    }

    /**
     * @override run if active
     */
  }, {
    key: "onMouseMove",
    value: function onMouseMove(pointer, context) {
      this.active && this.eventEmitter.dispatchEvent(new CustomEvent('move', {
        detail: context,
        cancelable: true
      })) && _get(_getPrototypeOf(EraserBrush.prototype), "onMouseMove", this).call(this, pointer, context);
    }

    /**
     * @override run if active, dispose of {@link drawEffect} listener
     */
  }, {
    key: "onMouseUp",
    value: function onMouseUp(context) {
      var _this$_disposer;
      this.active && _get(_getPrototypeOf(EraserBrush.prototype), "onMouseUp", this).call(this, context);
      this.active = false;
      (_this$_disposer = this._disposer) === null || _this$_disposer === void 0 || _this$_disposer.call(this);
      delete this._disposer;
      return false;
    }

    /**
     * @override {@link fabric.PencilBrush} logic
     */
  }, {
    key: "convertPointsToSVGPath",
    value: function convertPointsToSVGPath(points) {
      return _get(_getPrototypeOf(EraserBrush.prototype), "convertPointsToSVGPath", this).call(this, this.decimate ? this.decimatePoints(points, this.decimate) : points);
    }

    /**
     * @override
     */
  }, {
    key: "createPath",
    value: function createPath(pathData) {
      var path = _get(_getPrototypeOf(EraserBrush.prototype), "createPath", this).call(this, pathData);
      path.set(this.inverted ? {
        globalCompositeOperation: 'source-over',
        stroke: 'white'
      } : {
        globalCompositeOperation: 'destination-out',
        stroke: 'black',
        opacity: new fabric.Color(this.color).getAlpha()
      });
      return path;
    }
  }, {
    key: "commit",
    value: function () {
      var _commit = _asyncToGenerator( /*#__PURE__*/_regeneratorRuntime().mark(function _callee3(_ref3) {
        var path, targets;
        return _regeneratorRuntime().wrap(function _callee3$(_context3) {
          while (1) switch (_context3.prev = _context3.next) {
            case 0:
              path = _ref3.path, targets = _ref3.targets;
              _context3.t0 = Map;
              _context3.next = 4;
              return Promise.all([].concat(_toConsumableArray(targets.map( /*#__PURE__*/function () {
                var _ref4 = _asyncToGenerator( /*#__PURE__*/_regeneratorRuntime().mark(function _callee(object) {
                  return _regeneratorRuntime().wrap(function _callee$(_context) {
                    while (1) switch (_context.prev = _context.next) {
                      case 0:
                        _context.t0 = object;
                        _context.next = 3;
                        return eraseObject(object, path);
                      case 3:
                        _context.t1 = _context.sent;
                        return _context.abrupt("return", [_context.t0, _context.t1]);
                      case 5:
                      case "end":
                        return _context.stop();
                    }
                  }, _callee);
                }));
                return function (_x7) {
                  return _ref4.apply(this, arguments);
                };
              }())), _toConsumableArray([[this.canvas.backgroundImage, !this.canvas.backgroundVpt ? this.canvas.viewportTransform : undefined], [this.canvas.overlayImage, !this.canvas.overlayVpt ? this.canvas.viewportTransform : undefined]].filter(function (_ref5) {
                var _ref6 = _slicedToArray(_ref5, 1),
                  object = _ref6[0];
                return !!(object !== null && object !== void 0 && object.erasable);
              }).map( /*#__PURE__*/function () {
                var _ref8 = _asyncToGenerator( /*#__PURE__*/_regeneratorRuntime().mark(function _callee2(_ref7) {
                  var _ref9, object, vptFlag;
                  return _regeneratorRuntime().wrap(function _callee2$(_context2) {
                    while (1) switch (_context2.prev = _context2.next) {
                      case 0:
                        _ref9 = _slicedToArray(_ref7, 2), object = _ref9[0], vptFlag = _ref9[1];
                        _context2.t0 = object;
                        _context2.next = 4;
                        return eraseCanvasDrawable(object, vptFlag, path);
                      case 4:
                        _context2.t1 = _context2.sent;
                        return _context2.abrupt("return", [_context2.t0, _context2.t1]);
                      case 6:
                      case "end":
                        return _context2.stop();
                    }
                  }, _callee2);
                }));
                return function (_x8) {
                  return _ref8.apply(this, arguments);
                };
              }()))));
            case 4:
              _context3.t1 = _context3.sent;
              return _context3.abrupt("return", new _context3.t0(_context3.t1));
            case 6:
            case "end":
              return _context3.stop();
          }
        }, _callee3, this);
      }));
      function commit(_x6) {
        return _commit.apply(this, arguments);
      }
      return commit;
    }()
    /**
     * @override handle events
     */
  }, {
    key: "_finalizeAndAddPath",
    value: function _finalizeAndAddPath() {
      var points = this['_points'];
      if (points.length < 2) {
        this.eventEmitter.dispatchEvent(new CustomEvent('cancel', {
          cancelable: false
        }));
        return;
      }
      var path = this.createPath(this.convertPointsToSVGPath(points));
      var targets = walk(this.canvas.getObjects(), path);
      this.eventEmitter.dispatchEvent(new CustomEvent('end', {
        detail: {
          path: path,
          targets: targets
        },
        cancelable: true
      })) && this.commit({
        path: path,
        targets: targets
      });
      this.canvas.clearContext(this.canvas.contextTop);
      this.canvas.requestRenderAll();
      this._resetShadow();
    }
  }, {
    key: "dispose",
    value: function dispose() {
      var canvas = this.effectContext.canvas;
      // prompt GC
      canvas.width = canvas.height = 0;
      // release ref?
      // delete this.effectContext
    }
  }]);
  return EraserBrush;
}(fabric.PencilBrush);

var messageID = -1;
var isImageDataTransparent = function isImageDataTransparent(imageData) {
  return imageData.data.every(function (x, i) {
    return i % 4 !== 3 || x === 0;
  });
};
function isTransparent(object, worker) {
  // should also move to offscreen
  var canvas = object.toCanvasElement({
    // multiplier: 0.1,
    enableRetinaScaling: false,
    viewportTransform: false,
    withoutTransform: true,
    withoutShadow: true
  });
  var id = ++messageID;
  return new Promise(function (resolve, reject) {
    var _canvas$getContext;
    var imageData = (_canvas$getContext = canvas.getContext('2d')) === null || _canvas$getContext === void 0 ? void 0 : _canvas$getContext.getImageData(0, 0, canvas.width, canvas.height);
    if (!imageData) {
      reject();
    } else if (!worker) {
      resolve(isImageDataTransparent(imageData));
    } else {
      var messageHandler = function messageHandler(e) {
        if (e.data.messageID === id) {
          worker.removeEventListener('message', messageHandler);
          resolve(e.data.isTransparent);
        }
      };
      worker.addEventListener('message', messageHandler);
      worker.postMessage({
        imageData: imageData,
        messageID: id
      }, []);
    }
  });
}
isTransparent.installWorker = function installWorker() {
  addEventListener('message', function (e) {
    var _e$data = e.data,
      imageData = _e$data.imageData,
      messageID = _e$data.messageID;
    postMessage({
      isTransparent: isImageDataTransparent(imageData),
      messageID: messageID
    });
  });
};

// export { ClippingGroup, EraserBrush, eraseCanvasDrawable, eraseObject, isTransparent };
//# sourceMappingURL=eraser.js.map
