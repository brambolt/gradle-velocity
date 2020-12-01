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

import org.gradle.api.Project
import org.gradle.api.file.FileTree

/**
 * Utility class for converting a directory to a file tree.
 */
class InputFiles {

  static final String DEFAULT_INPUT_MASK = '**/*.vtl'

  String inputMask

  Project project

  InputFiles(Project project) {
    this(project, DEFAULT_INPUT_MASK)
  }

  InputFiles(Project project, String inputMask) {
    this.project = project
    this.inputMask = inputMask
  }

  FileTree apply(File baseFile) {
    switch (baseFile) {
      case { !baseFile.exists() }:
        return getEmptyInput()
      case { baseFile.isFile() }:
        return getSingleFileInput(baseFile)
      default:
        return getFileTreeInput(baseFile)
    }
  }

  FileTree getEmptyInput() {
    project.files().asFileTree
  }

  FileTree getFileTreeInput(File inputDir) {
    project.fileTree(inputDir) {
      include inputMask
    }
  }

  FileTree getSingleFileInput(File inputFile) {
    File tmpDir = File.createTempDir('brambolt', 'files')
    tmpDir.deleteOnExit()
    project.copy {
      from inputFile
      into tmpDir
      rename { String basename ->
        File to = new File(tmpDir, basename)
        to.deleteOnExit()
        basename
      }
    }
    project.fileTree(tmpDir)
  }
}