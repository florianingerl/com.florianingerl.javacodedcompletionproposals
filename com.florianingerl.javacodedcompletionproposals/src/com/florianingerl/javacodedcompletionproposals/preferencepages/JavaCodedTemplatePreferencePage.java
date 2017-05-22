/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.florianingerl.javacodedcompletionproposals.preferencepages;

import org.eclipse.jdt.internal.ui.preferences.JavaTemplatePreferencePage;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.window.Window;

import com.florianingerl.javacodedcompletionproposals.JavaCodedTemplatePlugin;

public class JavaCodedTemplatePreferencePage extends JavaTemplatePreferencePage {

	public JavaCodedTemplatePreferencePage() {
		setPreferenceStore(JavaCodedTemplatePlugin.getDefault().getPreferenceStore());
		setTemplateStore(JavaCodedTemplatePlugin.getDefault().getTemplateStore());
	}

	/*
	 * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#
	 * createTemplateEditDialog2(org.eclipse.jface.text.templates.Template,
	 * boolean, boolean)
	 */
	@Override
	protected Template editTemplate(Template template, boolean edit, boolean isNameModifiable) {
		com.florianingerl.javacodedcompletionproposals.preferencepages.EditJavaCodedTemplateDialog dialog = new com.florianingerl.javacodedcompletionproposals.preferencepages.EditJavaCodedTemplateDialog(
				getShell(), template, edit, isNameModifiable, getContextTypeRegistry());
		if (dialog.open() == Window.OK) {
			return dialog.getTemplate();
		}
		return null;
	}

}
