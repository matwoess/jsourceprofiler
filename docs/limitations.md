# Limitations

## General
- Only the project itself is instrumented (no library classes without source code)
- Run-time exceptions inside and outside of try blocks cannot be considered for the
  resulting coverage data (of the following statements).

## Temporary
- Custom build tools (like Ant, Maven and Gradle) are not supported yet.
- Imperfect grammar:
    - The ATG is kept simple, minimal and generic. While we can successfully parse and instrument large projects,
      we do not claim to find every possible code block. The fuzzy approach leads to some special structures
      being currently skipped.
- The hit count alone does tell us how long the method execution took
