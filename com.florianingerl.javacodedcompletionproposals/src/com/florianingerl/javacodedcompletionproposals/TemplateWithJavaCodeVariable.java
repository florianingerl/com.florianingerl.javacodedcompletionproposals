package com.florianingerl.javacodedcompletionproposals;

public class TemplateWithJavaCodeVariable {

	private final String fName;
	private int[] fOffsets;
	private TemplateWithJavaCodeVariableType fType;
	private final int fInitialLength;
	private boolean fIsResolved = false;

	public TemplateWithJavaCodeVariable(String name, TemplateWithJavaCodeVariableType type, int[] offsets,
			int initialLength) {
		this.fName = name;
		this.fType = type;
		this.fOffsets = offsets;
		this.fInitialLength = initialLength;
	}

}
