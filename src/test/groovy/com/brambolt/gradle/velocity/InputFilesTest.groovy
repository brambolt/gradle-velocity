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

import com.brambolt.gradle.velocity.tasks.VelocityTest
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class InputFilesTest {

  @Test
  void testGetEmptyInput() {
    Project project = ProjectBuilder.builder().build()
    FileCollection empty = new InputFiles(project).getEmptyInput()
    assertEquals(0, empty.size())
  }

  @Test
  void testGetEmptyInputWithProjectFile() {
    Project project = ProjectBuilder.builder().build()
    File projectFile = new File(project.projectDir, 'Grimm')
    projectFile.text = 'Once upon a time, the end.'
    projectFile.deleteOnExit()
    FileCollection empty = new InputFiles(project).getEmptyInput()
    assertEquals(0, empty.size())
  }

  @Test
  void testGetSingleFileInput() {
    Project project = ProjectBuilder.builder().build()
    File projectFile = new File(project.projectDir, 'Galaxy')
    projectFile.text = 'Weit draussen, in einem unerforschtem Einoeden.'
    projectFile.deleteOnExit()
    FileCollection singleFile = new InputFiles(project).getSingleFileInput(projectFile)
    assertEquals(1, singleFile.size())
    FileCollection again = new InputFiles(project).apply(projectFile)
    assertEquals(1, again.size())
  }

  @Test
  void testGetFileTreeInput() {
    Project project = VelocityTest.buildProject()
    FileCollection fileTree = new InputFiles(project).getFileTreeInput(project.projectDir)
    assertEquals(2, fileTree.size())
    FileCollection again = new InputFiles(project).apply(project.projectDir)
    assertEquals(2, again.size())
  }
}
