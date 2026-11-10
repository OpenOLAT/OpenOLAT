// OO-10045: intermediate page for navigations to OpenOlat pages in a frame of the
// content domain. It hands the target over to the OpenOlat window.
// - On the content domain: post a message to the ancestors, only the OpenOlat
//   window (main domain) receives it.
// - On the main domain (phase 2): call gotopath() of the first ancestor of the
//   same origin which is an OpenOlat window.
// The link stays visible as fallback.
(function() {
	var a = document.getElementById('o_contentnav');
	if (!a) return;
	if (window.top === window) {
		window.location.replace(a.href);
		return;
	}
	var path = a.getAttribute('data-path');
	var server = a.getAttribute('data-server');
	if (!path || !server) return;

	var sameOrigin = window.location.origin === server;
	var w = window;
	for (var i = 0; i < 16; i++) {
		var p = w.parent;
		if (!p || p === w) break;
		try {
			if (sameOrigin) {
				if (typeof p.gotopath === 'function' && p.o_info) {
					// Same rule as the message bridge: only after a click of the user, a
					// content page cannot move the OpenOlat window with a hidden frame.
					// Without activation, the link stays as fallback.
					var activation = p.navigator.userActivation;
					if (!activation || activation.isActive) {
						p.gotopath(path);
					}
					return;
				}
			} else {
				p.postMessage({ type: 'olat-gotopath', path: path }, server);
			}
		} catch (e) {
			// window of another origin
		}
		w = p;
	}
})();
