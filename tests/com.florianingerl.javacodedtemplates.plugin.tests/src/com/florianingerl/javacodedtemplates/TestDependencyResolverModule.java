package com.florianingerl.javacodedtemplates;

import static org.junit.Assert.assertTrue;

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
				File dir = new File(System.getProperty("user.home"));
				System.out.println("HomeDir=" + dir.getAbsolutePath());
				dir = new File(dir, "javacodedtemplatestore");
				if (!dir.exists()) {
					assertTrue("Directory could not be created!", dir.mkdir());
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
