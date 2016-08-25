package com.florianingerl.javacodedcompletionproposals;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class EclipsePluginDependencyResolverModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ITemplatesWithJavaCodeLocator.class).toInstance(new ITemplatesWithJavaCodeLocator() {
			@Override
			public List<TemplateWithJavaCode> getTemplatesWithJavaCode(String identifierPrefix) {
				List<TemplateWithJavaCode> result = new LinkedList<TemplateWithJavaCode>();
				result.add(new TemplateWithJavaCode("full property", "Java property with change notifications"));
				return result;
			}
		});
	}

	@Provides
	public IWorkbenchWindow getActiveWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

}
