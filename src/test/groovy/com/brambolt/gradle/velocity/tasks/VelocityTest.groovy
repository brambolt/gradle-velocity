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

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class VelocityTest {

  static Project buildProject() {
    Project project = ProjectBuilder.builder().build()
    project.ext.srcDir = project.file('src/main/vtl')
    project.srcDir.mkdirs()
    [ 'test1.vtl', 'test2.vtl' ]
      .collect { String basename -> [
        basename: basename,
        is: Thread.currentThread().getContextClassLoader().getResourceAsStream(basename) ]}
      .collect { Map acc -> [
        basename: acc.basename,
        text: new Scanner((InputStream) acc.is).useDelimiter('\\Z').next() ]}
      .each { Map acc ->
        File vtlFile = new File((File) project.srcDir, (String) acc.basename)
        vtlFile.text = acc.text
        vtlFile.deleteOnExit()
      }
    project
  }

  @Test
  void testNoFiles() {
    Project project = ProjectBuilder.builder().build()
    Task velocity = project.task([type: Velocity], 'velocity') {
      contextValues = [not: 'important']
      inputPath = "${project.projectDir}/src/main/vtl"
      outputDir = project.buildDir
    }
    velocity.apply()
    assertNull(project.buildDir.listFiles())
  }

  @Test
  void testWithoutContext() {
    Project project = buildProject()
    Task velocity = project.task([type: Velocity], 'velocity') {
      contextValues = [not: 'important']
      inputPath = project.srcDir
      outputDir = project.buildDir
    }
    velocity.apply()
    assertEquals(2, project.buildDir.listFiles().toList().size())
    [ 'test1', 'test2' ]
      .collect { String basename -> [
        basename: basename,
        source: new File((File) project.srcDir, "${basename}.vtl"),
        target: new File((File) project.buildDir, basename) ]}
      .each { Map acc ->
        // Check the source files:
        assertTrue(acc.source.exists() && acc.source.isFile())
        // Check the target files:
        assertTrue(acc.target.exists() && acc.target.isFile())
        // Compare... (there is no context, and no substitution...):
        assertEquals(acc.source.text, acc.target.text) }
  }

  @Test
  void testSingleInstantiation() {
    Project project = buildProject()
    Task velocity = project.task([type: Velocity], 'velocity') {
    }

  }
}
