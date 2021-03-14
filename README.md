# Genetic-Algorithm
A simple genetic algorithm written from scratch

Note: I do not own the code within FitnessCalculator.java. This was provided to us by our professor.

## Purpose
The purpose of the assignment was to create a genetic algorithm from scratch that could figure out how to unshred a "shredded document". These shredded documents were provided to us in the form of text files that were denoted as shredded. The algorithm would use make use of a variety of genetic operators in an attempt to bring the shredded file back into a fully readable text.

### Genetic Operators
The algorithm implemented four types of genetic operators. Two types of crossover and two types of mutation. 

Crossover types:
* Order crossover (OX)
* Uniform Order Crossover (UOX)

Mutation types:
* Reciprocal Exchange (RE)
* Inversion (I)

Different levels of crossover and mutation could be specified by the user upon running the code as well as a number of other customizable parameters.

## Usage
After cloning the repo and compiling the .java files, you can run the genetic algorithm by using this command. It's a long one...
```
$ java GeneticAlgorithm {filename} {population size} {maximum genetations} {k} {crossover chance (as a decimal)} {mutation chance (as a decimal)} {crossover type} {mutation type} {random seed}
```

An example of this would be:
```
$ java GeneticAlgorithm document1-shredded.txt 100 100 3 0.9 0.1 OX RE 12345678
```

The program should now be running. Information regarding the parameters will be printed out at the beginning, information about each specific generation will be printed throughout execution, and information regarding the best solution will be printed at the end.
