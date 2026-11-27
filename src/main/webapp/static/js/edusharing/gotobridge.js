'use strict';

function o_registerEdusharingGoToBridge(serverOrigin, goToPath) {
	document.addEventListener('click', function(e) {
		if (e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey || window.parent === window) {
			return;
		}
		const a = e.target && e.target.closest ? e.target.closest('a[href]') : null;
		if (!a) {
			return;
		}
		let url;
		try {
			url = new URL(a.href);
		} catch (err) {
			return;
		}
		const id = url.searchParams.get('identifier');
		if (url.origin !== serverOrigin || url.pathname !== goToPath || !id || !/^[\w-]{1,64}$/.test(id)) {
			return;
		}
		e.preventDefault();
		let w = window;
		for (let i = 0; i < 16; i++) {
			const p = w.parent;
			if (!p || p === w) {
				break;
			}
			try {
				p.postMessage({ type: 'olat-edusharing-goto', id: id }, serverOrigin);
			} catch (err) {
				// ignore
			}
			w = p;
		}
	}, true);
}
