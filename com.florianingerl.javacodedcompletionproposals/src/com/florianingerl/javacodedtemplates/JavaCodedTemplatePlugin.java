package com.florianingerl.javacodedtemplates;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;

public class JavaCodedTemplatePlugin extends AbstractUIPlugin {

	private static final String TEMPLATES_KEY = "com.github.florianingerl.javacodedtemplatesplugin.custom_templates";

	private static JavaCodedTemplatePlugin instance = null;

	private IPreferenceStore fPreferenceStore;
	private TemplateStore fTemplateStore;

	public static JavaCodedTemplatePlugin getDefault() {
		if (instance == null)
			instance = new JavaCodedTemplatePlugin();
		return instance;
	}

	private JavaCodedTemplatePlugin() {

	}

	public IPreferenceStore getPreferenceStore() {
		if (fPreferenceStore == null)
			fPreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, getBundle().getSymbolicName());
		return fPreferenceStore;
	}

	public TemplateStore getTemplateStore() {
		if (fTemplateStore == null) {

			final IPreferenceStore store = getPreferenceStore();
			fTemplateStore = new TemplateStore(
					null /*
							 * JavaPlugin.getDefault().
							 * getCodeTemplateContextRegistry()
							 */, store, TEMPLATES_KEY);

			try {
				fTemplateStore.load();
			} catch (IOException e) {
				e.printStackTrace();
			}
			fTemplateStore.startListeningForPreferenceChanges();

		}

		return fTemplateStore;
	}

	public TemplateContextType getTemplateContextType() {
		TemplateContextType tct = new TemplateContextType();
		tct.addResolver(new TemplateVariableResolver());
		return tct;
	}

	/*
	 * private Image image = null; public Image getImage() {
	 * 
	 * if (image == null) { Path path = new Path("icons/igel.gif"); URL url =
	 * Platform.find(getBundle(), path);
	 * 
	 * image = ImageDescriptor.createFromURL(url).createImage(); } return image;
	 * }
	 */

	private File templateStoreDir;

	public File getTemplateStoreDir() {
		if (templateStoreDir == null) {
			File parent = null;
			try {
				parent = new File(Platform.getInstallLocation().getURL().toURI());
			} catch (URISyntaxException e) {
				parent = new File(System.getProperty("user.home"));
			}
			templateStoreDir = new File(parent, "javacodedtemplatestore");
			if (!templateStoreDir.exists()) {
				templateStoreDir.mkdir();
			}
		}
		return templateStoreDir;
	}

}
