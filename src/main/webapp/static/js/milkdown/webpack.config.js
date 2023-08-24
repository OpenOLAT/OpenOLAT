const path = require('path');

module.exports = {
	entry: './src/index.js',
	output: {
		path: path.join(__dirname, "dist"),
		filename: 'oomilkdown.js',
		library: 'oomilkdown'
	},
	
	module: {
		rules: [
			{
				test: /\.js$/,
				exclude: /(node_modules)/,
				use: {
					loader: 'babel-loader',
					options: {
						presets: ['@babel/preset-env']
					}
				}
			}
		]
	},

	performance: {
		hints: false,
		maxEntrypointSize: 1048576,
		maxAssetSize: 1048576
	}
}