package com.florianingerl.javacodedcompletionproposals.preferencepages;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.layout.GridData;

public class TemplateListPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	/**
	 * @wbp.parser.constructor
	 */
	public TemplateListPreferencePage() {
		// TODO Auto-generated constructor stub
	}

	public TemplateListPreferencePage(String title) {
		super(title);
		// TODO Auto-generated constructor stub
	}

	public TemplateListPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Control createContents(Composite parent) {
		Composite panel = new Composite(parent, SWT.NULL);
		panel.setLayout(new GridLayout(1, false));

		ListViewer listViewer = new ListViewer(panel, SWT.SINGLE);
		List list = listViewer.getList();
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		listViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {

			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return new String[] { "Hello", "my", "dear" };
			}
		});

		return panel;
	}

}
