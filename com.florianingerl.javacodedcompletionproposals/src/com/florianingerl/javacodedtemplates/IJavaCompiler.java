package com.florianingerl.javacodedtemplates;

import java.io.File;

public interface IJavaCompiler {
	public void compile(File srcFile) throws CompilationException;
}
