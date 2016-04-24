##### Cloning the repository

```
$ git clone https://github.com/universal-automata/liblevenshtein-java-cli.git
Cloning into 'liblevenshtein-java-cli'...
remote: Counting objects: 61, done.
remote: Compressing objects: 100% (45/45), done.
remote: Total 61 (delta 7), reused 56 (delta 2), pack-reused 0
Unpacking objects: 100% (61/61), done.
Checking connectivity... done.

$ cd liblevenshtein-java-cli
```

##### Building the command-line interface

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

##### Getting help on its usage

```
$ ./build/install/liblevenshtein-java-cli/bin/liblevenshtein-java-cli --help
13:21:24.651 [main] INFO  c.g.l.CommandLineInterface - Parsing command-line args [--help]
usage: liblevenshtein-java-cli [-a
       <STANDARD|TRANSPOSITION|MERGE_AND_SPLIT>] [--colorize] [-d
       <PATH|URI>] [-h] [-i] [-m <INTEGER>] [-q <STRING> <STRING> <...>]
       [-s] [--serialize <PATH>] [--source-format
       <PROTOBUF|BYTECODE|PLAIN_TEXT>] [--target-format
       <PROTOBUF|BYTECODE|PLAIN_TEXT>]
Command-Line Interface to liblevenshtein (Java)

 -a,--algorithm <STANDARD|TRANSPOSITION|MERGE_AND_SPLIT>   Levenshtein
                                                           algorithm to
                                                           use (Default:
                                                           TRANSPOSITION)
    --colorize                                             Colorize output
 -d,--dictionary <PATH|URI>                                Filesystem path
                                                           or
                                                           Java-compatible
                                                           URI to a
                                                           dictionary of
                                                           terms (Default:
                                                           jar:file:///hom
                                                           e/dylon/Workspa
                                                           ce/liblevenshte
                                                           in-java/java-cl
                                                           i/build/install
                                                           /liblevenshtein
                                                           -java-cli/lib/l
                                                           iblevenshtein-j
                                                           ava-cli-2.2.1.j
                                                           ar!/wordsEn.txt
                                                           )
 -h,--help                                                 print this help
                                                           text
 -i,--include-distance                                     Include the
                                                           Levenshtein
                                                           distance with
                                                           each spelling
                                                           candidate
                                                           (Default:
                                                           false)
 -m,--max-distance <INTEGER>                               Maximun,
                                                           Levenshtein
                                                           distance a
                                                           spelling
                                                           candidatemay be
                                                           from the query
                                                           term (Default:
                                                           2)
 -q,--query <STRING> <STRING> <...>                        Terms to query
                                                           against the
                                                           dictionary
 -s,--is-sorted                                            Specifies that
                                                           the dictionary
                                                           is sorted
                                                           lexicographical
                                                           ly, in
                                                           ascending order
                                                           (Default:
                                                           false)
    --serialize <PATH>                                     Path to save
                                                           the serialized
                                                           dictionary
    --source-format <PROTOBUF|BYTECODE|PLAIN_TEXT>         Format of the
                                                           source
                                                           dictionary
                                                           (Default:
                                                           adaptive)
    --target-format <PROTOBUF|BYTECODE|PLAIN_TEXT>         Format of the
                                                           serialized
                                                           dictionary
                                                           (Default:
                                                           PROTOBUF)

Example: liblevenshtein-java-cli \
  --algorithm TRANSPOSITION \
  --max-distance 2 \
  --include-distance \
  --query mispelled mispelling \
  --colorize
```

##### Converting from Plain Text to Protocol Buffers

```
$ ./build/install/liblevenshtein-java-cli/bin/liblevenshtein-java-cli --dictionary /tmp/dictionary.txt --source-format PLAIN_TEXT --serialize /tmp/dictionary.proto.bytes --target-format PROTOBUF
12:21:30.875 [main] INFO  c.g.l.CommandLineInterface - Parsing command-line args [--dictionary, /tmp/dictionary.txt, --source-format, PLAIN_TEXT, --serialize, /tmp/dictionary.proto.bytes, --target-format, PROTOBUF]
12:21:30.975 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [10000] of [109582] terms
12:21:30.998 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [20000] of [109582] terms
12:21:31.020 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [30000] of [109582] terms
12:21:31.038 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [40000] of [109582] terms
12:21:31.055 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [50000] of [109582] terms
12:21:31.073 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [60000] of [109582] terms
12:21:31.093 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [70000] of [109582] terms
12:21:31.111 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [80000] of [109582] terms
12:21:31.127 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [90000] of [109582] terms
12:21:31.145 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [100000] of [109582] terms
12:21:31.171 [main] INFO  c.g.d.l.l.factory.TransducerBuilder - Building transducer out of [109582] terms with algorithm [TRANSPOSITION], defaultMaxDistance [2], includeDistance [false], and maxCandidates [2147483647]
12:21:31.183 [main] INFO  c.g.l.CommandLineInterface - Serializing [109582] terms in the dictionary to [/tmp/dictionary.proto.bytes] as format [PROTOBUF]
```

##### Querying the dictionary while including candidate distances

```
$ ./build/install/liblevenshtein-java-cli/bin/liblevenshtein-java-cli --dictionary /tmp/dictionary.proto.bytes --source-format PROTOBUF --algorithm TRANSPOSITION --max-distance 2 --include-distance --query mispelled mispelling --colorize
12:24:09.029 [main] INFO  c.g.l.CommandLineInterface - Parsing command-line args [--dictionary, /tmp/dictionary.proto.bytes, --source-format, PROTOBUF, --algorithm, TRANSPOSITION, --max-distance, 2, --include-distance, --query, mispelled, mispelling, --colorize]
12:24:09.224 [main] INFO  c.g.d.l.l.factory.TransducerBuilder - Building transducer out of [109582] terms with algorithm [TRANSPOSITION], defaultMaxDistance [2], includeDistance [true], and maxCandidates [2147483647]
+-------------------------------------------------------------------------------
| Spelling Candidates for Query Term: "mispelled"
+-------------------------------------------------------------------------------
| d("mispelled", "spelled") = [2]
| d("mispelled", "impelled") = [2]
| d("mispelled", "dispelled") = [1]
| d("mispelled", "miscalled") = [2]
| d("mispelled", "respelled") = [2]
| d("mispelled", "misspelled") = [1]
+-------------------------------------------------------------------------------
| Spelling Candidates for Query Term: "mispelling"
+-------------------------------------------------------------------------------
| d("mispelling", "spelling") = [2]
| d("mispelling", "impelling") = [2]
| d("mispelling", "dispelling") = [1]
| d("mispelling", "misbilling") = [2]
| d("mispelling", "miscalling") = [2]
| d("mispelling", "misdealing") = [2]
| d("mispelling", "respelling") = [2]
| d("mispelling", "misspelling") = [1]
| d("mispelling", "misspellings") = [2]
```

##### Querying the dictionary without including candidate distances

```
$ ./build/install/liblevenshtein-java-cli/bin/liblevenshtein-java-cli --dictionary /tmp/dictionary.proto.bytes --source-format PROTOBUF --algorithm TRANSPOSITION --max-distance 2 --query mispelled mispelling --colorize
12:24:30.437 [main] INFO  c.g.l.CommandLineInterface - Parsing command-line args [--dictionary, /tmp/dictionary.proto.bytes, --source-format, PROTOBUF, --algorithm, TRANSPOSITION, --max-distance, 2, --query, mispelled, mispelling, --colorize]
12:24:30.636 [main] INFO  c.g.d.l.l.factory.TransducerBuilder - Building transducer out of [109582] terms with algorithm [TRANSPOSITION], defaultMaxDistance [2], includeDistance [false], and maxCandidates [2147483647]
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
