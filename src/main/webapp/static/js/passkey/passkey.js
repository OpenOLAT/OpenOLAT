function base64urlToBuffer(baseurl64String) {
	// Base64url to Base64
	var padding = "==".slice(0, (4 - (baseurl64String.length % 4)) % 4);
	var base64String = baseurl64String.replace(/-/g, "+").replace(/_/g, "/") + padding;
	var str = atob(base64String);
	// Binary string to buffer
	var buffer = new ArrayBuffer(str.length);
	var byteView = new Uint8Array(buffer);
	for (let i = 0; i < str.length; i++) {
		byteView[i] = str.charCodeAt(i);
	}
	return buffer;
}

function bufferToBase64url(buffer) {
	// Buffer to binary string
	var byteView = new Uint8Array(buffer);
	var str = "";
	for (const charCode of byteView) {
		str += String.fromCharCode(charCode);
	}

	// Binary string to base64
	var base64String = btoa(str);

	// Base64 to base64url
	// We assume that the base64url string is well-formed.
	return base64String.replace(/\+/g, "-").replace(/\//g, "_",).replace(/=/g, "");
}