/*
 * Copyright 2017-2020 Brambolt ehf.
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

package com.brambolt.gradle.velocity.tasks

import com.brambolt.gradle.velocity.EngineFactory
import com.brambolt.gradle.velocity.InputFiles
import com.brambolt.gradle.velocity.TemplateVisitor
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.apache.velocity.app.VelocityEngine
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import static groovy.io.FileType.FILES

/**
 * A Gradle task for instantiating Velocity templates using the Velocity engine.
 */
class Velocity extends DefaultTask {

  /**
   * The default input path to locate template files with.
   */
  static final String DEFAULT_VELOCITY_INPUT_PATH = 'src/main/vtl'

  /**
   * The default task name.
   */
  static final String DEFAULT_VELOCITY_TASK_NAME = 'velocity'

  /**
   * The path where the input templates are located.
   */
  String inputPath

  /**
   * The location to write the instantiated templates to.
   */
  File outputDir

  /**
   * The Velocity context to instantiate the input templates with.
   */
  Map<String, Object> contextValues = [:]

  /**
   * If the instantiation is strict then an exception will be thrown if any
   * template variables remain in the instantiated templates after the
   * context has been applied. Defaults to false.
   */
  boolean strict = false

  boolean sort = true

  /**
   * The output directory to write the instantiated templates to. Defaults to
   * <code>&lt;buildDir&gt;/velocity</code>.
   * @return The output directory
   */
  @OutputDirectory
  File getOutputDir() {
    outputDir
  }

  /**
   * The directory where the templates are located. Defaults to
   * <code>src/main/templates</code>.
   * @return The input directory that holds the templates
   */
  @Input
  File getInputFile() {
    project.file(inputPath)
  }

  /**
   * The Velocity template context for instantiation.
   * @return The instantiation context
   */
  @Input
  Map<String, Object> getContext() {
    contextValues
  }

  /**
   * Applies the parameter closure to the template context. Syntactic sugar.
   * @param closure A closure to add values to the Velocity context
   * @return The resulting Velocity context after the closure has been called
   */
  Map<String, Object> context(Closure closure) {
    closure.setDelegate(contextValues)
    closure.call()
    getContext()
  }

  /**
   * Adds the key-value pairs in the parameter map to the Velocity context.
   * @param values The values to add
   * @return The Velocity context after the values have been added
   */
  Map<String, Object> context(Map<String, Object> values) {
    contextValues.putAll(values)
    getContext()
  }

  /**
   * Adds the key-value pairs in the parameter properties stream to the context.
   * @param propertiesStream A stream opened from a Java properties file or equivalent
   * @return The Velocity context after the values have been added
   */
  Map<String, Object> context(InputStream propertiesStream) {
    Properties properties = new Properties()
    properties.load(propertiesStream)
    contextValues.putAll(properties)
    getContext()
  }

  /**
   * Adds the key-value pairs in the parameter properties file to the context.
   * @param propertiesFile The Java properties file to add
   * @return The Velocity context after the values have been added
   */
  Map<String, Object> context(File propertiesFile) {
    FileInputStream stream = null
    try {
      stream = new FileInputStream(propertiesFile)
      context(stream)
    } catch (Exception x) {
      throw new GradleException(
        "Unable to load Velocity context from properties file: ${propertiesFile}", x)
    } finally {
      if (null != stream)
        stream.close()
    }
  }

  /**
   * Configures the task. This method is re-entrant, the task can be configured
   * any number of times, or not at all.
   * @param closure The configuration closure
   * @return The configured task
   */
  @Override
  Task configure(Closure closure) {
    // Force execution, until input/output is handled correctly:
    outputs.upToDateWhen { false }
    super.configure(closure)
    configureDefaults()
    configureChecking()
    this
  }

  /**
   * Applies default values.
   */
  void configureDefaults() {
    if (null == inputPath || inputPath.isEmpty())
      inputPath = 'src/main/vtl'
    if (null == outputDir)
      outputDir = new File(project.buildDir, 'velocity')
  }

  /**
   * If instantiation is strict, a check is configured to make sure an
   * exception is thrown if any template variables are missing from the context.
   */
  protected void configureChecking() {
    doLast {
      if (strict)
        throwIfUninstantiated(checkCompleteInstantiation(outputDir))
    }
  }

  /**
   * Creates a map of instantated template files to the list of lines in the
   * files where a template variable failed to be instantiated. If every
   * variable has a context value then the map will be empty; if one or more
   * variables are missing from the context then one or more files will
   * appear in the output, and map to the lines where instantiation failed.
   * @param dir The directory containing the instantiated templates to check
   * @return The map of files to lines with uninstantiated variables
   */
  protected Map<File, List<String>> checkCompleteInstantiation(File dir) {
    Map<File, List<String>> uninstantiated = [:]
    dir.eachFileRecurse(FILES) { File instantiated ->
      List<String> uninstantiatedLines = checkInstantiation(instantiated)
      if (!uninstantiatedLines.isEmpty())
        uninstantiated.put(instantiated, uninstantiatedLines)
    }
    uninstantiated
  }

  /**
   * Returns a list of lines with uninstantiated template variables, from the
   * parameter file. The output is empty if everything was instantiated.
   * @param instantiated The file to check
   * @return The lines with uninstantiated template variables
   */
  List<String> checkInstantiation(File instantiated) {
    List<String> uninstantiatedLines = []
    instantiated.eachLine { String line ->
      if (isUninstantiated(line))
        uninstantiatedLines.add(line)
      line // For the IDE type checker
    }
    uninstantiatedLines
  }

  /**
   * Determines whether a line has an uninstantiated variable. The check simply
   * looks for the start of a variable insertion, e.g. <code>${</code>. This
   * probably fails in a lot of cases, but has proven sufficiently useful for
   * a lot of cases.
   * @param line The line to check
   * @return True iff the line contains the <code>${</code> pattern
   */
  boolean isUninstantiated(String line) {
    line.contains('${') // Pretty rudimentary...
  }

  /**
   * Throws a Gradle exception if the parameter map is non-empty.
   * @param uninstantiated The map of uninstantiated template variable occurrences
   */
  void throwIfUninstantiated(Map<File, List<String>> uninstantiated) {
    if (uninstantiated.isEmpty())
      return // Nothing to worry about...
    String message = "Instantiation was incomplete and strict checking is configured:\n" +
      (uninstantiated.inject(new StringBuilder()) { builder, entry ->
        builder.append("${entry.key}:\n\t${(entry.value as List).join('\n\t')}\n")
        builder
      }).toString()
    throw new GradleException(message)
  }

  /**
   * Executes the task.
   */
  @TaskAction
  void apply() {
    if (!outputDir.exists())
      outputDir.mkdirs()
    inputFiles.visit(createVisitor(createEngine(getInputFiles())))
  }

  /**
   * Converts the input directory to a file tree.
   * @return The file tree of the input files.
   */
  FileTree getInputFiles() {
    new InputFiles(project).apply(getInputFile())
  }

  /**
   * Creates a template visitor for the parameter engine.
   * @param engine The Velocity engine being used
   * @return A template visitor for the engine
   * @see TemplateVisitor
   */
  TemplateVisitor createVisitor(VelocityEngine engine) {
    new TemplateVisitor(this, engine)
  }

  /**
   * Creates a Velocity engine instance for the input files.
   * @param inputFiles The input files to instantiate as Velocity templates
   * @return A Velocity engine to instantiate the parameter files
   */
  VelocityEngine createEngine(FileTree inputFiles) {
    new EngineFactory(project).apply(inputFiles)
  }
}

