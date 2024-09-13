/**
 * Based on https://github.com/HexMox/milkdown-plugin-placeholder
 */

import { createSlice, createTimer } from '@milkdown/ctx';
import { Plugin, PluginKey } from '@milkdown/prose/state';
import { InitReady, prosePluginsCtx } from '@milkdown/core';

export const placeholderCtx = createSlice('Please input here...', 'placeholder');
export const placeholderTimerCtx = createSlice([], 'editorStateTimer');

export const PlaceholderReady = createTimer('PlaceholderReady');

const key = new PluginKey('MILKDOWN_PLACEHOLDER');

export const placeholder = (ctx) => {
	ctx.inject(placeholderCtx).inject(placeholderTimerCtx, [InitReady]).record(PlaceholderReady);

	return async () => {
		await ctx.waitTimers(placeholderTimerCtx);

		const prosePlugins = ctx.get(prosePluginsCtx);

		const update = (view) => {
			const placeholder = ctx.get(placeholderCtx)
			const doc = view.state.doc
			if (
				view.editable &&
				doc.childCount === 1 &&
				doc.firstChild?.isTextblock &&
				doc.firstChild?.content.size === 0 &&
				doc.firstChild?.type.name === 'paragraph'
			) {
				view.dom.setAttribute('data-placeholder', placeholder);
			} else {
				view.dom.removeAttribute('data-placeholder');
			}
		}

		const plugins = [
			...prosePlugins,
			new Plugin({
				key,
				view(view) {
					update(view);

					return { update };
				},
			}),
		];

		ctx.set(prosePluginsCtx, plugins);
		ctx.done(PlaceholderReady);
	};
};
