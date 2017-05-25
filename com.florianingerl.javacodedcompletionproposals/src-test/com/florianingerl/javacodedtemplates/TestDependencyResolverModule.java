package com.florianingerl.javacodedtemplates;

import java.io.File;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class TestDependencyResolverModule extends AbstractModule {

	private IJavaCompiler javaCompiler;

	public TestDependencyResolverModule(IJavaCompiler javaCompiler) {
		this.javaCompiler = javaCompiler;
	}

	@Override
	protected void configure() {

	}

	@Provides
	public ITemplateStoreDirProvider getTemplateStoreDirProvider() {
		return new ITemplateStoreDirProvider() {

			@Override
			public File getTemplateStoreDir() {
				File dir = new File("./javacodedtemplatestore");
				if (!dir.exists()) {
					dir.mkdir();
				}
				return dir;
			}

		};
	}

	@Provides
	public IJavaCompiler getJavaCompiler() {
		return javaCompiler;
	}

}
