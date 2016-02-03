/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.developerstudio.msf4j.artifact.generator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.codegen.ClientOptInput;
import io.swagger.codegen.DefaultGenerator;
import io.swagger.codegen.config.CodegenConfigurator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.wso2.developerstudio.msf4j.artifact.util.GeneratorUtils;

/**
 * Class for generating JAX-RS services Java code from a Swagger API definition using IO Swagger Codegen libraries
 */
public class SwaggerToJavaGenerator {
	private Boolean verbose;
	private String generatedCodeLanguage;
	private String outputLocation;
	private String swaggerSpec;
	private String templateDir;
	private String auth;
	private String systemProperties;
	private String configFile;
	private Boolean skipOverwrite;
	private String apiPackage;
	private String modelPackage;
	private String instantiationTypes;
	private String typeMappings;
	private String additionalProperties;
	private String languageSpecificPrimitives;
	private String importMappings;
	private String invokerPackage;
	private String groupId;
	private String artifactId;
	private String artifactVersion;
	private String library;

	public SwaggerToJavaGenerator(String swaggerSpec, String outputLocation, String packageName) {
		this.swaggerSpec = swaggerSpec;
		this.outputLocation = outputLocation;
		this.apiPackage = packageName + ".api";
		this.modelPackage = packageName + ".model";
		this.generatedCodeLanguage = "jaxrs";
	}

	public void generateService() {

		// attempt to read from config file
		CodegenConfigurator configurator = CodegenConfigurator.fromFile(configFile);

		// if a config file wasn't specified or we were unable to read it
		if (configurator == null) {
			// createa a fresh configurator
			configurator = new CodegenConfigurator();
		}

		// now override with any specified parameters
		if (verbose != null) {
			configurator.setVerbose(verbose);
		}

		if (skipOverwrite != null) {
			configurator.setSkipOverwrite(skipOverwrite);
		}

		if (StringUtils.isNotEmpty(swaggerSpec)) {
			configurator.setInputSpec(swaggerSpec);
		}

		if (StringUtils.isNotEmpty(generatedCodeLanguage)) {
			configurator.setLang(generatedCodeLanguage);
		}

		if (StringUtils.isNotEmpty(outputLocation)) {
			configurator.setOutputDir(outputLocation);
		}

		if (StringUtils.isNotEmpty(auth)) {
			configurator.setAuth(auth);
		}

		if (StringUtils.isNotEmpty(templateDir)) {
			configurator.setTemplateDir(templateDir);
		}

		if (StringUtils.isNotEmpty(apiPackage)) {
			configurator.setApiPackage(apiPackage);
		}

		if (StringUtils.isNotEmpty(modelPackage)) {
			configurator.setModelPackage(modelPackage);
		}

		if (StringUtils.isNotEmpty(invokerPackage)) {
			configurator.setInvokerPackage(invokerPackage);
		}

		if (StringUtils.isNotEmpty(groupId)) {
			configurator.setGroupId(groupId);
		}

		if (StringUtils.isNotEmpty(artifactId)) {
			configurator.setArtifactId(artifactId);
		}

		if (StringUtils.isNotEmpty(artifactVersion)) {
			configurator.setArtifactVersion(artifactVersion);
		}

		if (StringUtils.isNotEmpty(library)) {
			configurator.setLibrary(library);
		}

		setSystemProperties(configurator);
		setInstantiationTypes(configurator);
		setImportMappings(configurator);
		setTypeMappings(configurator);
		setAdditionalProperties(configurator);
		setLanguageSpecificPrimitives(configurator);

		final ClientOptInput clientOptInput = configurator.toClientOptInput();
		new DefaultGenerator().opts(clientOptInput).generate();
	}

	private void setSystemProperties(CodegenConfigurator configurator) {
		final Map<String, String> map = createMapFromKeyValuePairs(systemProperties);
		for (Map.Entry<String, String> entry : map.entrySet()) {
			configurator.addSystemProperty(entry.getKey(), entry.getValue());
		}
	}

	private void setInstantiationTypes(CodegenConfigurator configurator) {
		final Map<String, String> map = createMapFromKeyValuePairs(instantiationTypes);
		for (Map.Entry<String, String> entry : map.entrySet()) {
			configurator.addInstantiationType(entry.getKey(), entry.getValue());
		}
	}

	private void setImportMappings(CodegenConfigurator configurator) {
		final Map<String, String> map = createMapFromKeyValuePairs(importMappings);
		for (Map.Entry<String, String> entry : map.entrySet()) {
			configurator.addImportMapping(entry.getKey(), entry.getValue());
		}
	}

	private void setTypeMappings(CodegenConfigurator configurator) {
		final Map<String, String> map = createMapFromKeyValuePairs(typeMappings);
		for (Map.Entry<String, String> entry : map.entrySet()) {
			configurator.addTypeMapping(entry.getKey(), entry.getValue());
		}
	}

	private void setAdditionalProperties(CodegenConfigurator configurator) {
		final Map<String, String> map = createMapFromKeyValuePairs(additionalProperties);
		for (Map.Entry<String, String> entry : map.entrySet()) {
			configurator.addAdditionalProperty(entry.getKey(), entry.getValue());
		}
	}

	private void setLanguageSpecificPrimitives(CodegenConfigurator configurator) {
		final Set<String> set = createSetFromCsvList(languageSpecificPrimitives);
		for (String item : set) {
			configurator.addLanguageSpecificPrimitive(item);
		}
	}

	private Set<String> createSetFromCsvList(String csvProperty) {
		final List<String> values = GeneratorUtils.splitCommaSeparatedList(csvProperty);
		return new HashSet<String>(values);
	}

	private Map<String, String> createMapFromKeyValuePairs(String commaSeparatedKVPairs) {
		final List<Pair<String, String>> pairs = GeneratorUtils.parseCommaSeparatedTuples(commaSeparatedKVPairs);
		Map<String, String> result = new HashMap<String, String>();
		for (Pair<String, String> pair : pairs) {
			result.put(pair.getLeft(), pair.getRight());
		}
		return result;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getArtifactVersion() {
		return artifactVersion;
	}

	public void setArtifactVersion(String artifactVersion) {
		this.artifactVersion = artifactVersion;
	}

}
