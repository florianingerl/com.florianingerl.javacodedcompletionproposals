package com.florianingerl.javacodedcompletionproposals;

import com.google.inject.Injector;

public class ServiceLocator {

	private static Injector injector;

	public static Injector getInjector() {
		return injector;
	}

	public static void setInjector(Injector injector) {
		ServiceLocator.injector = injector;
	}

}
