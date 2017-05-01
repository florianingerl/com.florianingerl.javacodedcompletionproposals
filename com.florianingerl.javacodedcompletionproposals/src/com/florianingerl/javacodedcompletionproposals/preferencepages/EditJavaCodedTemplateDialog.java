package com.florianingerl.javacodedcompletionproposals.preferencepages;

import org.eclipse.jdt.internal.ui.preferences.EditTemplateDialog;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.swt.widgets.Shell;

import com.florianingerl.javacodedcompletionproposals.TemplateTranslator;

public class EditJavaCodedTemplateDialog extends EditTemplateDialog {

	public EditJavaCodedTemplateDialog(Shell parent, Template template, boolean edit, boolean isNameModifiable,
			ContextTypeRegistry registry) {
		super(parent, template, edit, isNameModifiable, registry);
	}
	
	@Override
	public void okPressed()
	{
		super.okPressed();
		System.out.println("ok was pressed!");
		
		TemplateTranslator translator = new TemplateTranslator();
		try {
			translator.translate(getTemplate()) ;
		} catch (TemplateException e) {
			e.printStackTrace();
		}
		
	}

}
