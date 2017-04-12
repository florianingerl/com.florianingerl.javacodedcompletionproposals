package com.florianingerl.javacodedcompletionproposals;

public class TemplateTranslator {

	static final String PATTERN_LAMBDA = "\\s*(?<arguments>\\((?<rep>\\s*String\\s+(?<argument>[\\p{Alpha}_$][\\p{Alpha}\\p{Digit}_$]*)\\s*)(,(?rep))*\\))\\s*-\\s*\\>\\s*(?<body>\\{(//.*+(\r)?\n|/\\*[\\s\\S]*?\\*/|\"(?:\\\\.|[^\"\\\\]++)*+\"|[^\"{}/]++|(?body))*+\\})\\s*";
}

