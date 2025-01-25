# Limitations

## General
- Only the project itself is instrumented (libraries without source code cannot be included)
- Run-time exceptions (and skipped statements) inside and outside of `try` blocks cannot be considered for the
  resulting coverage data.

## Temporary
- Custom build tools (like Ant, Maven and Gradle) are not supported yet.
- Imperfect grammar:
    - The ATG is kept simple, minimal and generic. While we can successfully parse and instrument large projects,
      we do not claim to find every possible code block. The fuzzy approach leads to some special structures
      being currently ignored.
- The hit count alone does not tell us how **long** it took to execute a code block 
