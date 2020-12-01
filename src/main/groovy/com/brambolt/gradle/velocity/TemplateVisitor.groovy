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

package com.brambolt.gradle.velocity

import com.brambolt.gradle.velocity.tasks.Velocity
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.EmptyFileVisitor
import org.gradle.api.file.FileVisitDetails

/**
 * Utility class for applying a Velocity engine with a file visitation pattern.
 */
class TemplateVisitor extends EmptyFileVisitor {

  Project project

  Velocity task

  VelocityEngine engine

  TemplateVisitor(Velocity task, VelocityEngine engine) {
    this.project = task.project
    this.task = task
    this.engine = engine
  }

  @Override
  void visitFile(FileVisitDetails details) {
    try {
      project.logger.info("Template processing visit: ${details}")
      VelocityContext context =  getContext()
      File outputFile = getOutputFile(details)
      outputFile.getParentFile().mkdirs()
      details.getFile().withReader { reader ->
        outputFile.withWriter { writer ->
          engine.evaluate(context, writer, details.getRelativePath().toString(), reader)
        }
      }
    } catch (IOException x) {
      throw new GradleException("Failed to process " + details, x)
    }
  }

  File getOutputFile(FileVisitDetails details) {
    String basename = details.getFile().getName()
    String extension = basename.contains('.') ? basename.substring(basename.lastIndexOf('.') + 1) : ''
    String basenameWithoutExtension = basename.substring(0, basename.size()- extension.size()- 1)
    File outputDir = details.relativePath.getFile(task.outputDir).parentFile
    new File(outputDir, basenameWithoutExtension)
  }

  VelocityContext getContext() {
    VelocityContext context = new VelocityContext()
    if (null != task.contextValues)
      for (Map.Entry<String, Object> assignment: task.contextValues.entrySet())
        context.put(assignment.getKey(), assignment.getValue())
    context
  }
}