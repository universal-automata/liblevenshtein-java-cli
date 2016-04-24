```
$ gradle installDist
:compileJavawarning: No processor claimed any of these annotations: lombok.extern.slf4j.Slf4j,lombok.experimental.ExtensionMethod,lombok.Getter,lombok.RequiredArgsConstructor,edu.umd.cs.findbugs.annotations.SuppressFBWarnings
1 warning

:processResources
:classes
:jar
:startScripts
:installDist

BUILD SUCCESSFUL

Total time: 4.925 secs

This build could be faster, please consider using the Gradle Daemon: https://docs.gradle.org/2.12/userguide/gradle_daemon.html
```

```
$ ./build/install/liblevenshtein-java-cli/bin/liblevenshtein-java-cli --help
12:17:52.845 [main] INFO  c.g.l.CommandLineInterface - Parsing command-line args [--help]
usage: liblevenshtein-java-cli [-a] [--colorize] [-d <PATH|URI>] [-h] [-i]
       [-m <INTEGER>] [-q <STRING> <STRING> <...>] [-s] [--serialize
       <PATH>] [--source-format <PROTOBUF|BYTECODE|PLAIN_TEXT>]
       [--target-format <PROTOBUF|BYTECODE|PLAIN_TEXT>]
Command-Line Interface to liblevenshtein (Java)

 -a,--algorithm                                      Levenshtein algorithm
                                                     to use (Default:
                                                     TRANSPOSITION)
    --colorize                                       Colorize output
 -d,--dictionary <PATH|URI>                          Filesystem path or
                                                     Java-compatible URI
                                                     to a dictionary of
                                                     terms (Default:
                                                     jar:file:///home/dylo
                                                     n/Workspace/liblevens
                                                     htein-java/java-cli/b
                                                     uild/install/libleven
                                                     shtein-java-cli/lib/l
                                                     iblevenshtein-java-cl
                                                     i-2.2.1.jar!/wordsEn.
                                                     txt)
 -h,--help                                           print this help text
 -i,--include-distance                               Include the
                                                     Levenshtein distance
                                                     with each spelling
                                                     candidate (Default:
                                                     false)
 -m,--max-distance <INTEGER>                         Maximun, Levenshtein
                                                     distance a spelling
                                                     candidatemay be from
                                                     the query term
                                                     (Default: 2)
 -q,--query <STRING> <STRING> <...>                  Terms to query
                                                     against the
                                                     dictionary
 -s,--is-sorted                                      Specifies that the
                                                     dictionary is sorted
                                                     lexicographically, in
                                                     ascending order
                                                     (Default: false)
    --serialize <PATH>                               Path to save the
                                                     serialized dictionary
    --source-format <PROTOBUF|BYTECODE|PLAIN_TEXT>   Format of the source
                                                     dictionary (Default:
                                                     adaptive)
    --target-format <PROTOBUF|BYTECODE|PLAIN_TEXT>   Format of the
                                                     serialized dictionary
                                                     (Default: PROTOBUF)

Example: liblevenshtein-java-cli \
  --algorithm TRANSPOSITION \
  --max-distance 2 \
  --include-distance \
  --query mispelled mispelling \
  --colorize
```

```
$ ./build/install/liblevenshtein-java-cli/bin/liblevenshtein-java-cli --algorithm TRANSPOSITION --max-distance 2 --query mispelled mispelling --colorize
12:17:24.784 [main] INFO  c.g.l.CommandLineInterface - Parsing command-line args [--algorithm, TRANSPOSITION, --max-distance, 2, --query, mispelled, mispelling, --colorize]
12:17:24.791 [main] INFO  c.g.l.CommandLineInterface - Attempting to deserialize dictionary as a [PROTOBUF] stream
12:17:24.793 [main] WARN  c.g.l.CommandLineInterface - No dictionary specified, defaulting to [jar:file:///home/dylon/Workspace/liblevenshtein-java/java-cli/build/install/liblevenshtein-java-cli/lib/liblevenshtein-java-cli-2.2.1.jar!/wordsEn.txt]
12:17:24.802 [main] WARN  c.g.l.CommandLineInterface - Nope, dictionary is not a [PROTOBUF] stream
12:17:24.802 [main] INFO  c.g.l.CommandLineInterface - Attempting to deserialize dictionary as a [BYTECODE] stream
12:17:24.803 [main] WARN  c.g.l.CommandLineInterface - No dictionary specified, defaulting to [jar:file:///home/dylon/Workspace/liblevenshtein-java/java-cli/build/install/liblevenshtein-java-cli/lib/liblevenshtein-java-cli-2.2.1.jar!/wordsEn.txt]
12:17:24.804 [main] WARN  c.g.l.CommandLineInterface - Nope, dictionary is not a [BYTECODE] stream
12:17:24.804 [main] INFO  c.g.l.CommandLineInterface - Attempting to deserialize dictionary as a [PLAIN_TEXT] stream
12:17:24.804 [main] WARN  c.g.l.CommandLineInterface - No dictionary specified, defaulting to [jar:file:///home/dylon/Workspace/liblevenshtein-java/java-cli/build/install/liblevenshtein-java-cli/lib/liblevenshtein-java-cli-2.2.1.jar!/wordsEn.txt]
12:17:24.903 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [10000] of [109582] terms
12:17:24.925 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [20000] of [109582] terms
12:17:24.947 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [30000] of [109582] terms
12:17:24.965 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [40000] of [109582] terms
12:17:24.982 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [50000] of [109582] terms
12:17:24.999 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [60000] of [109582] terms
12:17:25.020 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [70000] of [109582] terms
12:17:25.037 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [80000] of [109582] terms
12:17:25.053 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [90000] of [109582] terms
12:17:25.072 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [100000] of [109582] terms
12:17:25.097 [main] INFO  c.g.d.l.l.factory.TransducerBuilder - Building transducer out of [109582] terms with algorithm [TRANSPOSITION], defaultMaxDistance [2], includeDistance [false], and maxCandidates [2147483647]
+-------------------------------------------------------------------------------
| Spelling Candidates for Query Term: "mispelled"
+-------------------------------------------------------------------------------
| "mispelled" ~ "spelled"
| "mispelled" ~ "impelled"
| "mispelled" ~ "dispelled"
| "mispelled" ~ "miscalled"
| "mispelled" ~ "respelled"
| "mispelled" ~ "misspelled"
+-------------------------------------------------------------------------------
| Spelling Candidates for Query Term: "mispelling"
+-------------------------------------------------------------------------------
| "mispelling" ~ "spelling"
| "mispelling" ~ "impelling"
| "mispelling" ~ "dispelling"
| "mispelling" ~ "misbilling"
| "mispelling" ~ "miscalling"
| "mispelling" ~ "misdealing"
| "mispelling" ~ "respelling"
| "mispelling" ~ "misspelling"
| "mispelling" ~ "misspellings"
```
