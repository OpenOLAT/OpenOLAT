import { Editor, rootCtx, defaultValueCtx, editorViewOptionsCtx, editorViewCtx} from '@milkdown/core';
import { listener, listenerCtx } from '@milkdown/plugin-listener';
import { commonmark } from '@milkdown/preset-commonmark';


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
 * @param  targetDomId  The editor is added inside the DOM element with the id targetDomId
 * @param  text         The text to edit
 */
export async function ooMdEditFormElement(targetDomId, text, updateListener, onBlur) {
	var editor = await new Editor()
		.config((ctx) => {
			ctx.set(rootCtx, targetDomId);
			ctx.set(defaultValueCtx, text);

			const listener = ctx.get(listenerCtx);
			listener.markdownUpdated((ctx, markdown, prevMarkdown) => {
				if (markdown !== prevMarkdown) {
					updateListener(markdown);
				}
			});
		})
		.use(commonmark)
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