package com.florianingerl.javacodedcompletionproposals;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xml.sax.SAXException;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class EclipsePluginDependencyResolverModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ITemplatesWithJavaCodeLocator.class).toInstance(new ITemplatesWithJavaCodeLocator() {
			@Override
			public List<TemplateWithJavaCode> getTemplatesWithJavaCode(String identifierPrefix) {
				List<TemplateWithJavaCode> result = new LinkedList<TemplateWithJavaCode>();
				try {
					TemplateWithJavaCode template = new TemplateWithJavaCode();
					template.loadFromStream(IOUtils.toInputStream(
							"<templatewithjavacode><name>full property</name><description>Java property with change notifications</description></templatewithjavacode>"));
					result.add(template);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return result;
			}
		});
	}

	@Provides
	public IWorkbenchWindow getActiveWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

}
