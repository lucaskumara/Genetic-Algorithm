import java.util.Random;
import java.util.Arrays;
import java.lang.Math;

public class GeneticAlgorithm {

    public GeneticAlgorithm(String filename, int populationSize, int maxGenerations, int k, double crossover, double mutation, String crossoverType, String mutationType, long randomSeed) {

        // Create an instance of fitness calculator to use
        FitnessCalculator fitnessCalc = new FitnessCalculator();

        // Create an instance of random to use
        Random random = new Random(randomSeed);

        // Display GA parameters
        System.out.println("GA Parameters:");
        System.out.println("Filename: " + filename);
        System.out.println("Population Size: " + populationSize);
        System.out.println("Max Generations: " + maxGenerations);
        System.out.println("K (tournament selection): " + k);
        System.out.println("Crossover: " + (crossover * 100) + "%");
        System.out.println("Mutation: " + (mutation * 100) + "%");
        System.out.println("Crossover Type: " + crossoverType);
        System.out.println("Mutation Type: " + mutationType);
        System.out.println("Random Seed: " + randomSeed);

        // Choose which file to unshred
        final char[][] shreddedDocument = fitnessCalc.getShreddedDocument(filename);

        // Generate random initial population
        int[][] population = initializePopulation(populationSize, random);

        // Loop through generations
        for (int gen = 1; gen <= maxGenerations; gen++) {

            // Evaluate fitness of each permutation in population
            double[] scores = evaluateFitness(fitnessCalc, shreddedDocument, population);
            
            // Find lowest fitness score and average score
            double average = 0.0;

            // ** This is finding 1 elite **
            double bestScore = Double.MAX_VALUE;
            int[] bestSolution = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14}; // This will be overwritten

            for (int i = 0; i < scores.length; i++) {
                if (scores[i] < bestScore) {
                    bestScore = scores[i];
                    bestSolution = population[i];
                }

                average += scores[i];
            }

            // Divide average by number of scores
            average /= scores.length;

            System.out.println();
            System.out.println("Generation " + gen + ":");
            System.out.println("Best Fitness Value: " + bestScore);
            System.out.println("Average Fitness Value: " + average);

            // Use tournament selection to determine the best solutions
            int[][] newPopulation = tournamentSelection(population, scores, k, random);

            // Apply crossover/mutation to fill the rest of the population
            switch(crossoverType) {
                case "OX":
                    orderCrossover(newPopulation, crossover, random);
                    break;
                case "UOX":
                    uniformOrderCrossover(newPopulation, crossover, random);
                    break;
            }

            // Apply mutation
            switch(mutationType) {
                case "RE":
                    reciprocalExchange(newPopulation, mutation, random);
                    break;
                case "I":
                    inversion(newPopulation, mutation, random);
                    break;
            }

            // Ensure the elite (best solution) is in the new population
            ensureElite(newPopulation, bestSolution, random);

            // Overwrite current population with new one
            population = Arrays.copyOf(newPopulation, populationSize);

        }

        // Evaluate scores of final generation
        double[] scores = evaluateFitness(fitnessCalc, shreddedDocument, population);

        // Display best solution and fitness score
        int bestIndex = 0;
        double best = Double.MAX_VALUE;

        for (int i = 0; i < scores.length; i++) {
            if (scores[i] < best) {
                best = scores[i];
                bestIndex = i;
            }
        }

        System.out.println();
        System.out.println("GA Finished.");
        System.out.println();
        System.out.println("Best Solution: " + Arrays.toString(population[bestIndex]));
        System.out.println("Best Fitness Value: " + best);

    }

    private int[][] initializePopulation(int populationSize, Random random) {
        // Generates a specified amount of initial value permutations

        int[] defaultValues = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 };
        int[][] population = new int[populationSize][15];

        for (int i = 0; i < population.length; i++) {

            // Set default values to shuffle around
            population[i] = Arrays.copyOf(defaultValues, defaultValues.length);

            for (int j = 0; j < population[i].length; j++) {

                // Indentify random index to swap
                int randomIndex = random.nextInt(population[i].length);

                // Swap
                int temp = population[i][j];
                population[i][j] = population[i][randomIndex];
                population[i][randomIndex] = temp;
            }
        }

        return population;

    }

    private double[] evaluateFitness(FitnessCalculator fitnessCalc, char[][] shredded, int[][] population) {
        // Evaluates a list of permutations and returns a list of corresponding fitness scores

        double[] scores = new double[population.length];

        // Loop through population and store fitness scores
        for (int i = 0; i < population.length; i++) {
            double score = fitnessCalc.fitness(shredded, population[i]);
            scores[i] = score;
        }

        return scores;
    }

    private int[][] tournamentSelection(int[][] population, double[] scores, int k, Random random) {
        // Randomly selects k permutations from population and takes the best one until a new population is formed.

        int[] tournament = new int[k];
        int[][] newPopulation = new int[population.length][population[0].length];

        for (int i = 0; i < population.length; i++) {

            // Randomly select k indexes
            for (int j = 0; j < k; j++) {
                tournament[j] = random.nextInt(population.length);
            }

            // Pick best chromosome
            int bestIndex = 0;
            double bestScore = Double.MAX_VALUE;

            for (int index : tournament) {
                if (scores[index] < bestScore) {
                    bestIndex = index;
                    bestScore = scores[index];
                }
            }

            newPopulation[i] = population[bestIndex];

        }

        return newPopulation;
    }

    private void orderCrossover(int[][] population, double chance, Random random) {
        // Loops through the population in pairs and applies order crossover

        for (int i = 0; i < population.length; i += 2) {
            if (random.nextDouble() <= chance) {

                // Obtain permutations
                int[] parent1 = population[i];
                int[] parent2 = population[i + 1];

                // Randomly select two indexes
                int index1 = random.nextInt(population[i].length);
                int index2 = random.nextInt(population[i].length);

                int lower = Math.min(index1, index2);
                int higher = Math.max(index1, index2);

                // Create children
                int[] child1 = new int[population[i].length];
                int[] child2 = new int[population[i].length];

                // Copy values between random indexes
                for (int j = lower; j <= higher; j++) {
                    child1[j] = parent1[j];
                    child2[j] = parent2[j];
                }

                // Insert remaining values
                int insertionIndex1 = 0;
                int insertionIndex2 = 0;

                for (int j = 0; j < population[i].length; j++) {

                    // Insert if parent1 value isnt in child2
                    if (!contains(parent1[j], child2, lower, higher)) {
                        child2[insertionIndex1] = parent1[j];
                        insertionIndex1++;
                    }

                    // Insert if parent2 value isnt in child1
                    if (!contains(parent2[j], child1, lower, higher)) {
                        child1[insertionIndex2] = parent2[j];
                        insertionIndex2++;
                    }

                    // Move the insertion indexes forward for 
                    while (insertionIndex1 >= lower && insertionIndex1 <= higher) {
                        insertionIndex1++;
                    }

                    while (insertionIndex2 >= lower && insertionIndex2 <= higher) {
                        insertionIndex2++;
                    }
                }

                // Overwrite the parents
                population[i] = child1;
                population[i + 1] = child2;

            }
        }

    }

    private void uniformOrderCrossover(int[][] population, double chance, Random random) {
        // Loops through the population in pairs and applies uniform order crossover

        int[] mask = new int[population[0].length];

        // Loop through population applying crossover
        for (int i = 0; i < population.length; i += 2) {
            if (random.nextDouble() <= chance) {

                // Create mask with random 1's and 0's
                for (int j = 0; j < mask.length; j++) {
                    mask[j] = random.nextInt(2);
                }

                // Create lists to store the values copied
                int[] copiedParentValues1 = new int[population[0].length];
                int[] copiedParentValues2 = new int[population[0].length];
                int amount = 0; // Represents the number of values as the mask could be all 1's or all 0's

                // Obtain permutations
                int[] parent1 = population[i];
                int[] parent2 = population[i + 1];
                
                // Create children
                int[] child1 = new int[population[i].length];
                int[] child2 = new int[population[i].length];

                // Loop through mask and copy values
                for (int j = 0; j < mask.length; j++) {
                    if (mask[j] == 1) {

                        // Copy in values
                        child1[j] = parent1[j];
                        child2[j] = parent2[j];

                        // Track the values added
                        copiedParentValues1[amount] = parent1[j];
                        copiedParentValues2[amount] = parent2[j];
                        amount++;

                    }
                }

                // Insert remaining values
                int insertionIndex = 0;
                int indexParent1 = 0;
                int indexParent2 = 0;

                while (insertionIndex < population[i].length) {

                    // Move the insertion index forward if the next index wasnt a valid spot to insert
                    while (insertionIndex < mask.length && mask[insertionIndex] == 1) {
                        insertionIndex++;
                    }

                    // If the index has exceeded the mask length, stop
                    if (insertionIndex == mask.length) {
                        break;
                    }
                    
                    // Find a value to insert from parent1 to child2
                    for (int j = indexParent1; j < population[i].length; j++) {
                        if (!contains(parent1[j], copiedParentValues2, 0, amount - 1)) {
                            child2[insertionIndex] = parent1[j];
                            indexParent1 = j + 1;
                            break;
                        }
                    }

                    // Find a value to insert from parent2 to child1
                    for (int j = indexParent2; j < population[i].length; j++) {
                        if (!contains(parent2[j], copiedParentValues1, 0, amount - 1)) {
                            child1[insertionIndex] = parent2[j];
                            indexParent2 = j + 1;
                            break;
                        }
                    }

                    // Increment forward to move to next index
                    insertionIndex++;

                }

                // Overwrite the parents
                population[i] = child1;
                population[i + 1] = child2;

            }
        }

    }

    private void reciprocalExchange(int[][] population, double chance, Random random) {
        // Loops through the population and applies reciprocal exchange mutation

        // Randomly apply mutation to population
        for (int i = 0; i < population.length; i++) {
            if (random.nextDouble() <= chance) {

                int[] solution = population[i];

                // Randomly select two indexes to swap
                int index1 = random.nextInt(solution.length);
                int index2 = random.nextInt(solution.length);

                // Swap
                int temp = solution[index1];
                solution[index1] = solution[index2];
                solution[index2] = temp;

            }
        }

    }

    private void inversion(int[][] population, double chance, Random random) {
        // Loops through the population and applies inversion mutation

        // Randomly apply mutation to population
        for (int i = 0; i < population.length; i++) {
            if (random.nextDouble() <= chance) {

                int[] solution = population[i];

                // Randomly select two indexes for the range
                int index1 = random.nextInt(solution.length);
                int index2 = random.nextInt(solution.length);

                int lower = Math.min(index1, index2);
                int higher = Math.max(index1, index2);

                // Swap only if lower is less than higher
                while (lower < higher) {

                    // Swap
                    int temp = solution[lower];
                    solution[lower] = solution[higher];
                    solution[higher] = temp;

                    // Move indexes inward
                    lower++;
                    higher--;

                }

            }
        }
    }

    private void ensureElite(int[][] population, int[] elite, Random random) {
        // If the elite is not in the population, insert it into a random index

        boolean insert = true;

        // Loop through the population to see if elite is there
        for (int[] solution : population) {
            if (Arrays.equals(solution, elite)) {
                insert = false; // Dont insert if we already have it
            }
        }

        if (insert) {
            population[random.nextInt(population.length)] = elite;
        }

    }

    private boolean contains(int value, int[] array, int lower, int higher) {
        // Helper functiont to return if value is in array
        for (int i = lower; i <= higher; i++) {
            if (array[i] == value) {
                return true;
            }
        }
        return false;
    }
    
    public static void main(String[] args) {

        // Parse GA parameters
        String filename = args[0];
        int populationSize = Integer.parseInt(args[1]);
        int maxGenerations = Integer.parseInt(args[2]);
        int k = Integer.parseInt(args[3]);
        double crossover = Double.parseDouble(args[4]);
        double mutation = Double.parseDouble(args[5]);
        String crossoverType = args[6];
        String mutationType = args[7];
        long randomSeed = Long.parseLong(args[8]);

        new GeneticAlgorithm(filename, populationSize, maxGenerations, k, crossover, mutation, crossoverType, mutationType, randomSeed);

    }

}