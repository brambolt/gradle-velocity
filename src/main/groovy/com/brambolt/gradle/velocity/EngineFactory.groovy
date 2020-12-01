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

import org.apache.velocity.app.VelocityEngine
import org.gradle.api.Project
import org.gradle.api.file.FileCollection

import javax.annotation.Nullable

/**
 * Sets up a Velocity engine for instantiating templates.
 */
class EngineFactory {

  /**
   * The Gradle project the engine will be used for. Can be null, in which case
   * no logging takes place. The project is not used for anything else.
   */
  @Nullable
  Project project

  /**
   * Constructor. The resulting engine factory will not log anything.
   */
  EngineFactory() {
    this(null)
  }

  /**
   * Constructor. The resulting engine factory will log property assignments.
   * @param project The Gradle project the engine factory is for
   */
  EngineFactory(Project project) {
    this.project = project
  }

  /**
   * Factory method - creates a Velocity engine for the parameter input files.
   * @param inputFiles The root directory of the templates to instantiate
   * @return A Velocity engine to instantiate the templates with
   */
  VelocityEngine apply(FileCollection inputFiles) {
    VelocityEngine engine = new VelocityEngine()
    setProperty(engine, VelocityEngine.RESOURCE_LOADERS, "file")
    setProperty(engine, VelocityEngine.FILE_RESOURCE_LOADER_CACHE, "true")
    setProperty(engine, VelocityEngine.FILE_RESOURCE_LOADER_PATH, getInputs(inputFiles))
    engine
  }

  /**
   * Sets a property on the parameter engine.
   * @param engine The engine to set the property on
   * @param name The property name
   * @param value The property value
   */
  void setProperty(VelocityEngine engine, String name, Object value) {
    engine.setProperty(name, value)
    if (null != project)
      project.getLogger().info("Velocity engine property: " + name + " = " + value)
  }

  /**
   * Converts the paramter file collection to a comma-separated list of
   * absolute paths.
   * @param inputFiles The input files as a file collection
   * @return The input files as a comma-separate list of absolute paths
   */
  String getInputs(FileCollection inputFiles) {
    inputFiles.inject(new StringBuilder()) { StringBuilder b, File f ->
      0 == b.length() ? b.append(f.absolutePath) : b.append(',').append(f.absolutePath)
    }
  }
}