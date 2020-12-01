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

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class SimpleContextSpec extends Specification {

  @Rule TemporaryFolder testProjectDir = new TemporaryFolder()

  File template

  File outputDir

  String fileName

  def setup() {
    fileName = 'test.template'
    template = new File(testProjectDir.newFolder('vtl'), "${fileName}.vtl")
    template.text = '''
The Deliverator ${belongsOrNot}
'''
    outputDir = testProjectDir.newFolder('out')
    testProjectDir.newFile('build.gradle') << """
plugins {
  id 'com.brambolt.gradle.velocity'
}

velocity {
  context {
    belongsOrNot = 'belongs'
  }
  inputPath = '${template.parentFile.absolutePath}'
  outputDir = new File('${outputDir.absolutePath}')
}
"""
  }

  def 'can run task with simple context'() {
    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir.root)
      .withArguments('velocity')
      .withPluginClasspath()
      .build()
    then:
    null != result
    new File(outputDir, fileName).text.contains('The Deliverator belongs')
  }
}
