package com.florianingerl.javacodedcompletionproposals;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.List;
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
			Method m = clazz.getMethod(tv.getName());
			List<String> argumentNames = Stream.of(m.getTypeParameters()).map((TypeVariable<Method> tvm) -> {
				return tvm.getName();
			}).collect(Collectors.toList());
			int i = 0;
			String[] arguments = new String[argumentNames.size()];
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
