/*
 * Bach - Java Shell Builder
 * Copyright (C) 2017 Christian Stein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class BasicsTests {

  @Test
  void isJavaFile() {
    assertFalse(Basics.isJavaFile(Paths.get("")));
    assertFalse(Basics.isJavaFile(Paths.get("a/b")));
    assertTrue(Basics.isJavaFile(Paths.get("src/test/java/BasicsTests.java")));
  }

  @Test
  void isJarFile() {
    assertFalse(Basics.isJarFile(Paths.get("")));
    assertFalse(Basics.isJarFile(Paths.get("a/b")));
  }

  @Test
  void resolve() throws IOException {
    Path temp = Files.createTempDirectory("resolve-");
    new Basics.Resolvable("org.opentest4j", "opentest4j", "1.0.0-SNAPSHOT")
        .resolve(temp, Basics.Resolvable.REPOSITORIES);
    new Basics.Resolvable("org.opentest4j", "opentest4j", "1.0.0-ALPHA")
        .resolve(temp, Basics.Resolvable.REPOSITORIES);
  }

  private void createFiles(Path directory, int count) throws IOException {
    for (int i = 0; i < count; i++) {
      Files.createFile(directory.resolve("file-" + i));
    }
  }

  private void assertTreeDumpMatches(Path root, String... expected) {
    expected[0] = expected[0].replace(File.separatorChar, '/');
    List<String> dumpedLines = new ArrayList<>();
    Basics.treeDump(root, line -> dumpedLines.add(line.replace(File.separatorChar, '/')));
    assertLinesMatch(List.of(expected), dumpedLines);
  }

  @Test
  void tree() throws IOException {
    Path root = Files.createTempDirectory("tree-delete-");
    assertTrue(Files.exists(root));
    assertEquals(1, Files.walk(root).count());
    assertTreeDumpMatches(root, root.toString(), ".");

    createFiles(root, 3);
    assertEquals(1 + 3, Files.walk(root).count());
    assertTreeDumpMatches(root, root.toString(), ".", "./file-0", "./file-1", "./file-2");

    createFiles(Files.createDirectory(root.resolve("a")), 3);
    createFiles(Files.createDirectory(root.resolve("b")), 3);
    createFiles(Files.createDirectory(root.resolve("x")), 3);
    assertTrue(Files.exists(root));
    assertEquals(1 + 3 + 4 * 3, Files.walk(root).count());
    assertTreeDumpMatches(
        root,
        root.toString(),
        ".",
        "./a",
        "./a/file-0",
        "./a/file-1",
        "./a/file-2",
        "./b",
        "./b/file-0",
        "./b/file-1",
        "./b/file-2",
        "./file-0",
        "./file-1",
        "./file-2",
        "./x",
        "./x/file-0",
        "./x/file-1",
        "./x/file-2");

    Basics.treeDelete(root, path -> path.startsWith(root.resolve("b")));
    assertEquals(1 + 2 + 3 * 3, Files.walk(root).count());
    assertTreeDumpMatches(
        root,
        root.toString(),
        ".",
        "./a",
        "./a/file-0",
        "./a/file-1",
        "./a/file-2",
        "./file-0",
        "./file-1",
        "./file-2",
        "./x",
        "./x/file-0",
        "./x/file-1",
        "./x/file-2");

    Basics.treeDelete(root, path -> path.endsWith("file-0"));
    assertEquals(1 + 2 + 3 * 2, Files.walk(root).count());
    assertTreeDumpMatches(
        root,
        root.toString(),
        ".",
        "./a",
        "./a/file-1",
        "./a/file-2",
        "./file-1",
        "./file-2",
        "./x",
        "./x/file-1",
        "./x/file-2");

    Basics.treeCopy(root.resolve("x"), root.resolve("a/b/c"));
    assertEquals(1 + 4 + 4 * 2, Files.walk(root).count());
    assertTreeDumpMatches(
        root,
        root.toString(),
        ".",
        "./a",
        "./a/b",
        "./a/b/c",
        "./a/b/c/file-1",
        "./a/b/c/file-2",
        "./a/file-1",
        "./a/file-2",
        "./file-1",
        "./file-2",
        "./x",
        "./x/file-1",
        "./x/file-2");

    Basics.treeCopy(root.resolve("x"), root.resolve("x/y"));
    assertTreeDumpMatches(
        root,
        root.toString(),
        ".",
        "./a",
        "./a/b",
        "./a/b/c",
        "./a/b/c/file-1",
        "./a/b/c/file-2",
        "./a/file-1",
        "./a/file-2",
        "./file-1",
        "./file-2",
        "./x",
        "./x/file-1",
        "./x/file-2",
        "./x/y",
        "./x/y/file-1",
        "./x/y/file-2");

    Basics.treeDelete(root);
    assertTrue(Files.notExists(root));
    assertTreeDumpMatches(root, "dumpTree failed: path '" + root + "' does not exist");
  }
}