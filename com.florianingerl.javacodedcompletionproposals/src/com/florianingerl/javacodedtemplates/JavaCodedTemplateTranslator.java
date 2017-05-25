package com.florianingerl.javacodedtemplates;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableType;

import com.florianingerl.util.regex.Capture;
import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;

public class JavaCodedTemplateTranslator {

	public void translate(Template template) throws TemplateException {

		JavaCoded2EclipseTemplateConverter jc2etc = new JavaCoded2EclipseTemplateConverter();
		template = jc2etc.convert(template, true);

		TemplateTranslator translator = new TemplateTranslator();
		TemplateBuffer buffer = translator.translate(template);

		Set<String> parameters = new HashSet<String>();
		Set<String> variables = new HashSet<String>();

		for (TemplateVariable variable : buffer.getVariables()) {
			if (variable.getVariableType().getName().equals("javaCoded")) {
				parameters.addAll(variable.getVariableType().getParams());
			} else {
				variables.add(variable.getName());
			}
		}

		if (!variables.containsAll(parameters)) {
			throw new TemplateException("Not all parameters of Java-coded template variables could be resolved!");
		}

	}
}
