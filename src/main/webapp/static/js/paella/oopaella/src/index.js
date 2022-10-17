
import { Paella, utils } from 'paella-core';
import getBasicPluginContext from 'paella-basic-plugins';

import packageData from "../package.json";


window.onload = async () => {
	const initParams = {
		customPluginContext: [
			getBasicPluginContext()
		], 
		configUrl: configMapperUrl,
		getVideoId: function() {return "";},
		getManifestUrl: async function() {
			return manifestMapperUrl;
		}
	};
	
	class PaellaPlayer extends Paella {
		get version() {
			const player = packageData.version;
			const coreLibrary = super.version;
			const pluginModules = this.pluginModules.map(m => `${ m.moduleName }: ${ m.moduleVersion }`);
			return {
				player,
				coreLibrary,
				pluginModules
			};
		}
	}
	
	let paella = new PaellaPlayer('player-container', initParams);

	try {
		await paella.loadManifest();
	}
	catch (e) {
		console.error(e);
	}

}
