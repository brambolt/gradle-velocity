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

package com.brambolt.gradle

import com.brambolt.gradle.velocity.tasks.Velocity
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.file.FileResolver

import javax.inject.Inject

/**
 * A Gradle plug-in for working with Velocity templates.
 */
class VelocityPlugin implements Plugin<Project> {

  final FileResolver fileResolver

  @Inject
  VelocityPlugin(FileResolver fileResolver) {
    this.fileResolver = fileResolver
  }

  /**
   * Applies the plug-in to the parameter project.
   * @param project The project to apply the plug-in to
   */
  void apply(Project project) {
    project.logger.debug("Applying ${getClass().getCanonicalName()}.")
    createTemplatesTask(project)
  }

  /**
   * Creates a Velocity template instantiation task named <code>velocity</code>.
   * @param project The project to create the task for
   * @return The created task
   */
  Task createTemplatesTask(Project project) {
    createTemplatesTask(project, 'velocity')
  }

  /**
   * Creates a Velocity template instantiation task with the parameter name.
   * @param project The project to create the task for
   * @param name The name of the task to create
   * @return The created task
   */
  Task createTemplatesTask(Project project, String name) {
    project.task([type: Velocity], name)
  }
}
