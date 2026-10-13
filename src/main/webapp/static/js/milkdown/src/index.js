import { Editor, rootCtx, defaultValueCtx, editorViewOptionsCtx, editorViewCtx} from '@milkdown/core';
import { listener, listenerCtx } from '@milkdown/plugin-listener';
import { commonmark } from '@milkdown/preset-commonmark';
import {remark} from 'remark';
import remarkGfm from 'remark-gfm';
import strip from 'strip-markdown';
import { toString } from 'mdast-util-to-string';

import { gfm } from '@milkdown/preset-gfm';
import { $prose } from '@milkdown/utils';
import { keymap } from '@milkdown/prose/keymap';
import { addColumnAfter, addColumnBefore, addRowAfter, addRowBefore, deleteColumn, deleteRow, isInTable } from '@milkdown/prose/tables';
import { placeholder, placeholderCtx } from './placeholder';

/**
 * Render a markdown text as read-only HTML. Uses the same presets as the
 * editor (CommonMark and GFM), so tables, strikethrough and task lists render
 * in the view like in the editor.
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
		.use(gfm)
		.create();
}

/**
 * Keyboard shortcuts to add and delete table rows and columns. The GFM preset
 * ships the commands but binds no keys, and the editor has no table toolbar.
 * The shortcuts are active only when the cursor is in a table.
 */
const inTable = (command) => (state, dispatch, view) => isInTable(state) && command(state, dispatch, view);
const tableShortcuts = $prose(() => keymap({
	'Mod-Shift-ArrowDown': inTable(addRowAfter),
	'Mod-Shift-ArrowUp': inTable(addRowBefore),
	'Mod-Shift-ArrowRight': inTable(addColumnAfter),
	'Mod-Shift-ArrowLeft': inTable(addColumnBefore),
	'Mod-Shift-Backspace': inTable(deleteRow),
	'Mod-Alt-Backspace': inTable(deleteColumn)
}));

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
		.use(tableShortcuts)
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

/**
 * Render a markdown text as plain text, e.g. as a preview in a list.
 * The markdown formatting is removed and the remaining tree is converted to
 * plain text. The tree is not serialized back to markdown, because
 * remark-stringify would escape characters again (e.g. \_ or \*).
 * Paragraphs are separated by a single line break. GFM syntax (tables,
 * strikethrough, task lists) is parsed like in the editor. A table becomes
 * one line per row with the cell texts separated by a middle dot, because
 * strip-markdown would drop the whole table.
 * @param  targetDomId  The text is set as text content of the DOM element with the id targetDomId
 * @param  text         The text with markdown syntax
 */
export function stripMd(targetDomId, text) {
	const flattenTables = () => (tree) => {
		tree.children = tree.children.map((node) => {
			if (node.type !== 'table') {
				return node;
			}
			const value = node.children
				.map((row) => row.children.map((cell) => toString(cell)).join(' \u00b7 '))
				.join('\n');
			return { type: 'paragraph', children: [{ type: 'text', value }] };
		});
	};
	const processor = remark().use(remarkGfm).use(flattenTables).use(strip);
	processor
		.run(processor.parse(text || ''))
		.then((tree) => {
			const strippedText = tree.children
				.map((node) => toString(node))
				.filter((value) => value.length > 0)
				.join('\n');
			const el = document.getElementById(targetDomId);
			if (el) {
				el.textContent = strippedText;
			}
		});
}
