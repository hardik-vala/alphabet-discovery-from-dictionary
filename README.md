# Alphabet Discovery from Dictionary

A Java solution to the *Alphabet Discovery from Dictionary* problem.

## Problem

The *Alphabet Discovery from Dictionary* problem,

**_Given a dictionary of words in some unknown language, ordered according to the language's alphabet, determine that alphabet, i.e. the language's letters and their order._**

For example, consider the following dictionary of words,

```
cbca  (First word)
cbb   .
aa    .
aba   (Fourth word)
```

The corresponding alphabet is `c, a, b`. (This will serve as a running example in the explanation of the solution's algorithm in the next section.)

## Solution

Here, I'll describe the general algorithm behind the implemented solution.

At its core, the solution reduces the problem to a directed graph and then [topologically sorts](http://www.geeksforgeeks.org/topological-sorting/) it. Each vertex corresponds to a letter `x` and a directed edge `x --> y` exists if `x` precedes `y` in the alphabet. Here's the directed graph created for the example above,

```
 -- c --
|       |
v       v
a ----> b
```

A topological sorting of the graph yields, `c, a, b`, the alphabet. (There are a number of algorithms for generating a toplogical sort for a directed graph but the particular solution here employs [Kahn's algorithm](http://www.geeksforgeeks.org/topological-sorting-indegree-based-solution/).)

To generate the graph, the dictionary can be conceptualized as a matrix where each row corresponds to a word (left-aligned) and each column is a single letter wide. The matrix for the example would look something like this,

| c | b | c | a |
|---|---|---|---|
| **c** | **b** | **b** | |
| **a** | **a** | | |
| **a** | **b** | **a** | |

For each column (in order), we can walk through contiguous pairs of letters falling in the column (in order), ignoring empty column entries. For each pair `(x, y)`, if the prefix of the corresponding words, up to the column (exclusive), are equal, then `x` precedes `y` in the alphabet and the directed edge `x --> y` can be added to the graph. For example, lets consider column 3,

| c |
|---|
| **b** |
| |
| **a** |

with pairs `(c, b)` and `(b, a)` (ignoring the empty entries). For the first pair `(c, b)`, the prefix of `c` is `cb` and the prefix of `b` is the same. Hence, we can assert `c` precedes `b` in the alphabet and add the directed edge `c --> b` to the graph. For the next pair `(b, a)`, the prefix of `b` is `cb` but the prefix of `a` is `ab`. Hence, we cannot conclude anything regarding the ordering of `b` and `a`.

Proceeding in this way, the directed graph can be built and then topologically sort it to produce the alphabet.

There are a few problems to be aware of: The implementation requires there exists a single topological sort of the graph, otherwise the alphabet is ambiguous. This can happen if the input dictionary is _underspecified_, i.e. there is not enough information in terms of the orderings of all letter pairs. For the following dictionary,

```
c
ac
ab
```

the corresponding graph looks like,

```
 -- c --
|       |
v       v
a       b
```

from which the ordering of `a` and `b` cannot be determined.

Another problem can occur if the directed graph contains a cycle. Topological sorting requires the graph be acyclic, but if the dictionary is malformed, it's possible to generate a cyclic graph and no alphabet exists in this case. For the following dictionary not sorted properly,

```
c
a
b
c
```

the graph looks like,

```
 -- c <-
|       |
v       |
a ----> b
```

which contains a contradiction regarding the ordering of `b` and `c`, and hence no alphabet exists.

## Requirements

Java 7+

## Run

Running `make` in the project directory will compile and run the solution on the sample dictionary of English words, `sample_dict.txt`, and should yield the following,

```
Alphabet: [a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z]
```

To run the solution on a different dictionary, store the dictionary in a single file with each word on its own line and all the words ordered according to the language, and then execute the following,

```
make DICT=\path\to\dict_file
```

For fun, you can reverse the sample dictionary as follows,

```
cat sample_dict.txt | sort -r > rev_dict.txt
```

and then run,

```
make DICT=rev_dict.txt
```

and you should get,

```
Alphabet: [z, y, x, w, v, u, t, s, r, q, p, o, n, m, l, k, j, i, h, g, f, e, d, c, b, a]
```

## License

MIT

