package com.florianingerl.javacodedcompletionproposals.preferencepages;

import java.io.File;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.florianingerl.javacodedcompletionproposals.TemplateWithJavaCode;
import com.florianingerl.javacodedcompletionproposals.TemplatesWithJavaCodeStore;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
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

		TableViewer tableViewer = new TableViewer(parent,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());

		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		TableViewerColumn firstColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		firstColumn.getColumn().setText("Name");
		firstColumn.getColumn().setWidth(200);
		firstColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				TemplateWithJavaCode template = (TemplateWithJavaCode) element;
				return template.getName();
			}

		});

		TableViewerColumn secondColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		secondColumn.getColumn().setText("Description");
		secondColumn.getColumn().setWidth(400);
		secondColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				TemplateWithJavaCode template = (TemplateWithJavaCode) element;
				return template.getDescription();
			}
		});
		File dir = new File(
				"C:\\Users\\Hermann\\git\\com.florian.javacodedcompletionproposal\\com.florianingerl.javacodedcompletionproposals\\templates");
		TemplatesWithJavaCodeStore.loadAndCompileAllTemplates(dir);

		tableViewer.setInput(TemplatesWithJavaCodeStore.templates);

		return panel;
	}

}
