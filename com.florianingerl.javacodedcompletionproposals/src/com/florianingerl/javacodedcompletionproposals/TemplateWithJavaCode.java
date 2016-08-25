package com.florianingerl.javacodedcompletionproposals;

public class TemplateWithJavaCode {

	private String name;
	private String description;

	public TemplateWithJavaCode(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public TemplateWithJavaCode(String xmlFileName) {

	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

}
