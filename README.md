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
20:00:34.433 [main] INFO  c.g.l.CommandLineInterface - Parsing command-line args [--help]
usage: liblevenshtein-java-cli [-a <ALGORITHM>] [--colorize] -d <PATH|URI>
       [-h] [-i] [-m <INTEGER>] [-q <STRING> <...>] [-s] [--serialize
       <PATH>] [--source-format <FORMAT>] [--target-format <FORMAT>]

Command-Line Interface to liblevenshtein (Java)

<FORMAT> specifies the serialization format of the dictionary,
and may be one of the following:
  1. PROTOBUF
     - (de)serialize the dictionary as a protobuf stream.
     - This is the preferred format.
     - See: https://developers.google.com/protocol-buffers/
  2. BYTECODE
     - (de)serialize the dictionary as a Java, bytecode stream.
  3. PLAIN_TEXT
     - (de)serialize the dictionary as a plain text file.
     - Terms are delimited by newlines.

<ALGORITHM> specifies the Levenshtein algorithm to use for
querying-against the dictionary, and may be one of the following:
  1. STANDARD
     - Use the standard, Levenshtein distance which considers the
     following elementary operations:
       o Insertion
       o Deletion
       o Substitution
     - An elementary operation is an operation that incurs a penalty of
     one unit.
  2. TRANSPOSITION
     - Extend the standard, Levenshtein distance to include transpositions
     as elementary operations.
       o A transposition is a swapping of two, consecutive characters as
       follows: ba -> ab
       o With the standard distance, this would require at least two
       operations:
         + An insertion and a deletion
         + A deletion and an insertion
         + Two substitutions
  3. MERGE_AND_SPLIT
     - Extend the standard, Levenshtein distance to include merges and
     splits as elementary operations.
       o A merge takes two characters and merges them into a single one.
         + For example: ab -> c
       o A split takes a single character and splits it into two others
         + For example: a -> bc
       o With the standard distance, these would require at least two
       operations:
         + Merge:
           > A deletion and a substitution
           > A substitution and a deletion
         + Split:
           > An insertion and a substitution
           > A substitution and an insertion

 -a,--algorithm <ALGORITHM>    Levenshtein algorithm to use (Default:
                               TRANSPOSITION)
    --colorize                 Colorize output
 -d,--dictionary <PATH|URI>    Filesystem path or Java-compatible URI to a
                               dictionary of terms
 -h,--help                     print this help text
 -i,--include-distance         Include the Levenshtein distance with each
                               spelling candidate (Default: false)
 -m,--max-distance <INTEGER>   Maximun, Levenshtein distance a spelling
                               candidatemay be from the query term
                               (Default: 2)
 -q,--query <STRING> <...>     Terms to query against the dictionary.  You
                               may specify multiple terms.
 -s,--is-sorted                Specifies that the dictionary is sorted
                               lexicographically, in ascending order
                               (Default: false)
    --serialize <PATH>         Path to save the serialized dictionary
    --source-format <FORMAT>   Format of the source dictionary (Default:
                               adaptively-try each format until one works)
    --target-format <FORMAT>   Format of the serialized dictionary
                               (Default: PROTOBUF)

Example: liblevenshtein-java-cli \
  --algorithm TRANSPOSITION \
  --max-distance 2 \
  --include-distance \
  --query mispelled mispelling \
  --colorize
```

##### Converting from Plain Text to Protocol Buffers

```
$ ./build/install/liblevenshtein-java-cli/bin/liblevenshtein-java-cli --dictionary https://raw.githubusercontent.com/universal-automata/liblevenshtein-java/2.2.1/src/test/resources/wordsEn.txt --source-format PLAIN_TEXT --serialize /tmp/dictionary.protobuf.bytes --target-format PROTOBUF
20:40:25.945 [main] INFO  c.g.l.CommandLineInterface - Parsing command-line args [--dictionary, https://raw.githubusercontent.com/universal-automata/liblevenshtein-java/2.2.1/src/test/resources/wordsEn.txt, --source-format, PLAIN_TEXT, --serialize, /tmp/dictionary.protobuf.bytes, --target-format, PROTOBUF]
20:40:26.909 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [10000] of [109582] terms
20:40:26.932 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [20000] of [109582] terms
20:40:26.954 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [30000] of [109582] terms
20:40:26.971 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [40000] of [109582] terms
20:40:26.987 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [50000] of [109582] terms
20:40:27.003 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [60000] of [109582] terms
20:40:27.021 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [70000] of [109582] terms
20:40:27.037 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [80000] of [109582] terms
20:40:27.052 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [90000] of [109582] terms
20:40:27.069 [main] INFO  c.g.d.l.collection.dawg.AbstractDawg - Added [100000] of [109582] terms
20:40:27.093 [main] INFO  c.g.d.l.l.factory.TransducerBuilder - Building transducer out of [109582] terms with algorithm [TRANSPOSITION], defaultMaxDistance [2], includeDistance [false], and maxCandidates [2147483647]
20:40:27.103 [main] INFO  c.g.l.CommandLineInterface - Serializing [109582] terms in the dictionary to [/tmp/dictionary.protobuf.bytes] as format [PROTOBUF]
```

##### Querying the dictionary while including candidate distances

```
$ ./build/install/liblevenshtein-java-cli/bin/liblevenshtein-java-cli --dictionary /tmp/dictionary.protobuf.bytes --source-format PROTOBUF --algorithm TRANSPOSITION --max-distance 2 --include-distance --query mispelled mispelling --colorize
12:24:09.029 [main] INFO  c.g.l.CommandLineInterface - Parsing command-line args [--dictionary, /tmp/dictionary.protobuf.bytes, --source-format, PROTOBUF, --algorithm, TRANSPOSITION, --max-distance, 2, --include-distance, --query, mispelled, mispelling, --colorize]
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
$ ./build/install/liblevenshtein-java-cli/bin/liblevenshtein-java-cli --dictionary /tmp/dictionary.protobuf.bytes --source-format PROTOBUF --algorithm TRANSPOSITION --max-distance 2 --query mispelled mispelling --colorize
12:24:30.437 [main] INFO  c.g.l.CommandLineInterface - Parsing command-line args [--dictionary, /tmp/dictionary.protobuf.bytes, --source-format, PROTOBUF, --algorithm, TRANSPOSITION, --max-distance, 2, --query, mispelled, mispelling, --colorize]
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

##### Supported, dictionary sources

The library is designed to read dictionaries from filesystem paths,
Java-compatible URIs (including web URLs and [Jar resources][jar-resource-uri]),
[process substitutions][proc-sub] in Unix shells, and standard input
(e.g. [piped input][shell-piping]).

[proc-sub]: http://tldp.org/LDP/abs/html/process-sub.html "Chapter 23. Process Substitution"
[shell-piping]: http://tldp.org/HOWTO/Bash-Prog-Intro-HOWTO-4.html "4. Pipes"
[jar-resource-uri]: http://stackoverflow.com/a/2049705/206543 "Get a File or URI object for a file inside an archive with Java?"
