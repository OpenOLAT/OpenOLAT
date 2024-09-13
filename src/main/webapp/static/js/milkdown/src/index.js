import { Editor, rootCtx, defaultValueCtx, editorViewOptionsCtx, editorViewCtx} from '@milkdown/core';
import { listener, listenerCtx } from '@milkdown/plugin-listener';
import { commonmark } from '@milkdown/preset-commonmark';
import {remark} from 'remark';
import strip from 'strip-markdown';

import { gfm } from '@milkdown/preset-gfm';
import { placeholder, placeholderCtx } from './placeholder';

/**
 * Render a markdown text as read-only HTML.
 * @param  targetDomId  The HTML is added inside the DOM element with the id targetDomId
 * @param  text         The text with makdown syntax
 */
export function ooMdView(targetDomId, text) {
	const editable = () => false;
	Editor
		.make()
		.config((ctx) => {
			ctx.set(rootCtx, targetDomId);
			ctx.set(defaultValueCtx, text);
			ctx.update(editorViewOptionsCtx, (prev) => ({
				...prev,
				editable
			}));
		})
		.use(commonmark)
		.create();
}

/**
 * Create an editor to edit a markdown text in a flex form MarkdownElement.
 * @param  targetDomId     The editor is added inside the DOM element with the id targetDomId
 * @param  text            The text to edit
 * @param  updateListener  A listener of text update events in JavaScript that is injected by the renderer
 * @param  onBlur          A listener to the blur event of the editor in JavaScript that is injected by the renderer
 * @param  placeholderText An optional placeholder string
 */
export async function ooMdEditFormElement(targetDomId, text, updateListener, onBlur, placeholderText) {
	var editor = await new Editor()
		.config((ctx) => {
			ctx.set(rootCtx, targetDomId);
			ctx.set(defaultValueCtx, text);
			ctx.set(placeholderCtx, placeholderText);
			const listener = ctx.get(listenerCtx);
			listener.markdownUpdated((ctx, markdown, prevMarkdown) => {
				if (markdown !== prevMarkdown) {
					updateListener(markdown);
				}
			});
		})
		.use(commonmark)
		.use(gfm)
		.use(placeholder)
		.use(listener)
		.create();

		// Set a custom class to prose mirror to style it like a form element.
		// https://github.com/orgs/Milkdown/discussions/61
		// https://github.com/ueberdosis/tiptap/issues/524#issuecomment-705737849
		const view = editor.action(ctx => ctx.get(editorViewCtx));
		view.dom.classList.add('form-control');

		// Handle on blur to fire FormEvent.ON_CHANGED
		view.props.handleDOMEvents = {
			...(view.props.handleDOMEvents || {}),
			'blur': onBlur
		};
}

export function stripMd(targetDomId, text) {
	remark()
		.use(strip)
		.process(text)
		.then((stripedText) => {
			document.getElementById(targetDomId).textContent=stripedText.value.replace(/(?:\n\n)/g, '\n');
		});
}
