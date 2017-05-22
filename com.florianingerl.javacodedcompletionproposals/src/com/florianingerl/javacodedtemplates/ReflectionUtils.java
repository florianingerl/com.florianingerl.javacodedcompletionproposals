package com.florianingerl.javacodedtemplates;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Assert;

public class ReflectionUtils {

	public static Method findAnyMethod(Class<?> clazz, String name) {
		Optional<Method> o = Stream.of(clazz.getMethods()).filter((Method m) -> {
			return m.getName().equals(name);
		}).findAny();
		Assert.isTrue(o.isPresent());
		return o.get();
	}

}
