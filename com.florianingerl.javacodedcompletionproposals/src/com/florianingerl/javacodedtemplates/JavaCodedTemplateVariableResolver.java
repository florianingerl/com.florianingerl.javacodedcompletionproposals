package com.florianingerl.javacodedtemplates;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

public class JavaCodedTemplateVariableResolver extends TemplateVariableResolver {

	private Class<?> clazz;

	public JavaCodedTemplateVariableResolver(Class<?> clazz) {
		super("javaCoded", "A template variable resolver for a java-coded template variable");
		this.clazz = clazz;
	}

	@Override
	public void resolve(TemplateVariable tv, TemplateContext tc) {
		Assert.isTrue(tv.getVariableType().getName().equals("javaCoded"));

		try {
			Method m = ReflectionUtils.findAnyMethod(clazz, tv.getName());
			List<String> argumentNames = tv.getVariableType().getParams();
			String[] arguments = new String[argumentNames.size()];
			int i = 0;
			for (String argument : argumentNames) {
				arguments[i++] = tc.getVariable(argument);
				if (arguments[i - 1] == null)
					arguments[i - 1] = "";
			}
			tc.setVariable(tv.getName(), (String) m.invoke(null, arguments));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
