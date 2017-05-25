package com.florianingerl.javacodedtemplates;

import java.io.File;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class EclipsePluginDependencyResolverModule extends AbstractModule {

	@Override
	protected void configure() {

	}

	@Provides
	public IWorkbenchWindow getActiveWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	@Provides
	public ITemplateStoreDirProvider getTemplateStoreDirProvider() {
		return new ITemplateStoreDirProvider() {

			@Override
			public File getTemplateStoreDir() {
				return JavaCodedTemplatePlugin.getDefault().getTemplateStoreDir();
			}

		};
	}

}
