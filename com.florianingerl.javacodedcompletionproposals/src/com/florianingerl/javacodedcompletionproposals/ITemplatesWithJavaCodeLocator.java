package com.florianingerl.javacodedcompletionproposals;

import java.util.List;

public interface ITemplatesWithJavaCodeLocator
{

	List<TemplateWithJavaCode> getTemplatesWithJavaCode(String identifierPrefix);
	
}
