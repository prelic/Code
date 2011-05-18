#include <iostream>
#include <stdlib.h>
#include <time.h>
#include <ctype.h>

using namespace std;

#define CHROMOSOME_LENGTH 13
const char* target = "hello, world!";


#define INITIAL_POPULATION 100

#define CORRECT_SPOT_REWARD 20
#define INCORRECT_SPOT_REWARD 2

//note that tunable parameters must account 
//for every chromosome of the population (no holes, must add to INITIAL_POPULATION)
//------TUNABLE MUTATION PARAMETERS-----------
#define TOP 							10	//top number to keep
#define RANDOM_START 					10	//randomized chromosomes
#define RANDOM_END 						20
#define BIASED_REPRODUCTION_START 		20	//reproductions from fit parents
#define BIASED_REPRODUCTION_END 		50
#define NONBIASED_REPRODUCTION_START 	50	//reproductions for any parents
#define NONBIASED_REPRODUCTION_END 		60
#define BIASED_MUTATION_START 			60	//mutations of fit parents
#define BIASED_MUTATION_END 			90
#define NONBIASED_MUTATION_START	 	90	//mutations of any parents
#define NONBIASED_MUTATION_END 			100
//-------------------------------------------

//cut out ASCII 'command' keys & keep printable symbols
#define ASCII_LOWER_BOUND 32
#define ASCII_UPPER_BOUND 127

struct chr
{
	char val[CHROMOSOME_LENGTH];
	int fitness;
	
};
typedef struct chr chromosome;



chromosome* population[INITIAL_POPULATION];

void initializePopulation(int offset);
void printPopulation();
void init();
int evalFitness(char* val);
void evalPopulationFitness();
chromosome* mostFit();
chromosome* leastFit();
void sortPopulation();
void run(int generations);
void generateNewPopulation();
int main()
{
	srand(time(NULL));
	init();
	run(0);
	return 0;
}

//---------------
//init
//
//Allocates memory for each chromosome
//---------------
void init()
{
	for(int i = 0; i < INITIAL_POPULATION; i++)
	{
		population[i] = (chromosome*)malloc(sizeof(chromosome));
	}
}

//---------------
//initializePopulation
//takes:
//	offset - the index from which the random assignment starts
//
//initializePopulation iterates from offset until the end of the population
//and initializes each string to a random character with ASCII value between
//ASCII_LOWER_BOUND and ASCII_UPPER_BOUND
//---------------
void initializePopulation(int offset)
{
	int randnum = 0;

	//for each chromosome
	for(int i = offset; i < INITIAL_POPULATION; i++)
	{
		//for each character in that chromosome
		for(int j = 0; j < CHROMOSOME_LENGTH; j++)
		{
			//generate a number between ASCII_LOWER_BOUND and ASCII_UPPER_BOUND
			randnum = rand()%(ASCII_UPPER_BOUND-ASCII_LOWER_BOUND+1)+ASCII_LOWER_BOUND;
			
			//assign the character version of that digit to the chromosome
			population[i]->val[j] = (char)tolower(randnum);
		}
	}
}

//---------------
//printPopulation
//
//Prints the value and fitness of each chromosome in the population to stdout
//---------------
void printPopulation()
{
	for(int i = 0; i < INITIAL_POPULATION; i++)
	{
		for(int j = 0; j < CHROMOSOME_LENGTH; j++)
		{
			cout<<population[i]->val[j];
		}
		cout<<" "<<population[i]->fitness<<endl;
		cout<<"\n";
	}
	cout<<"------------------"<<endl;
}


//---------------
//evalFitness
//
//takes:
//	val - the chromosome to be evaluated
//
//returns:
//	the fitness for that chromosome
//
//For a given chromosome, the fitness score is evaluated as:
//	-10 points for a correct character in the correct spot
//	-2 points for a correct character in the incorrect spot
//---------------
int evalFitness(char* val)
{
	int fitness = 0;
	
	//for each character of the given string
	for(int i = 0; i < CHROMOSOME_LENGTH; i++)
	{
		//if the target character is the same (and same position), add 10
		if(val[i] == target[i])
		{
			fitness+=CORRECT_SPOT_REWARD;
		}
	}
	
	//for each character of the given string
	for(int i = 0; i < CHROMOSOME_LENGTH; i++)
	{
		//compare to each character of the target
		for(int j = 0; j < strlen(target); j++)
		{
			//if they're the same and the spots aren't the same (avoid double counting), add 2
			if(i != j && val[i] == target[j])
				fitness+=INCORRECT_SPOT_REWARD;
		}
	}
	return fitness;
}
//---------------
//evalPopulationFitness
//
//Calculates the fitness for each chromosome in the population
//---------------
void evalPopulationFitness()
{
	//for each chromosome, calculate the fitness
	for(int i = 0; i < INITIAL_POPULATION; i++)
	{
		population[i]->fitness = evalFitness(population[i]->val);
	}
}

//---------------
//mostFit
//
//returns
//	-the most fit chromosome of the population
//
//Iterates over the population, returns the chromosome with the highest fitness
//---------------
chromosome* mostFit()
{
	int maxval = 0;	//max fitness value
	int maxind = 0;	//index of max fitness value
	
	//for each chromosome in the population
	for(int i = 0; i < INITIAL_POPULATION; i++)
	{
		//if current should be new max
		if(population[i]->fitness > maxval)
		{	
			//set its index and value
			maxind = i;
			maxval = population[i]->fitness;
		}
	}
	return population[maxind];	
}

//---------------
//leastFit
//
//returns
//	-the least fit chromosome of the population
//
//Iterates over the population, returns the chromosome with the lowest fitness
//---------------
chromosome* leastFit()
{
	int minval=0;	//track min value
	int minind=0;	//track index of min value
	
	//for each chromosome of the population
	for(int i = 0; i < INITIAL_POPULATION; i++)
	{
		//if current should be new min
		if(population[i]->fitness <= minval)
		{
			//set value and index
			minind = i;
			minval = population[i]->fitness;
		}
	}
	return population[minind];
}

//---------------
//run takes:
//	generations - number of generations to be run.  If generations is 0, run until completion
//
//run is the engine of the program:
//	1.  Initialize population to random
//	2.  Evaluate fitness for each chromosome
//	3.  Sort population
//	4.  Generate new population based on genetic operators
//	5.  Evaluate new population
//	6.  Sort new population
//	7.  Check to see if target string or max generations achieved
//	8.  If termination conditions not met, goto 4.
//---------------
void run(int generations)
{
	int num_gens = 0;	//tracks current generation
	initializePopulation(0);	//randomly initialize entire population
	evalPopulationFitness();	//evaluate fitness for initial population
	sortPopulation();	//sort initial population
	
	//generations option not set, run until we generate a chromosome that matches the target
	if(generations == 0)
	{
		do{
			generateNewPopulation();	//generate new population	
			evalPopulationFitness();	//evaluate its fitness
			sortPopulation();	//sort it	
			num_gens++;	//increase #generations
			cout<<"gen: "<<num_gens<<" "<<mostFit()->val<<" "<<mostFit()->fitness<<endl;
		}while(strcmp(mostFit()->val,target) != 0);	//repeat while there is no perfect match
	}
	//run for a set number of generations
	else
	{
		for(int i = 0; i < generations; i++)
		{
			generateNewPopulation();	//tracks current generation
			evalPopulationFitness();	//evaluate fitness
			sortPopulation();	//sort		
			
			cout<<"gen: "<<i<<" "<<mostFit()->val<<" "<<mostFit()->fitness<<endl;
			
			//if target has been reached, don't continue
			if(strcmp(mostFit()->val, target)==0)
				return;
		}
	}
}
//-----------------
//sortPopulation
//
//Selection sort (for simplicity) of the population array, based on fitness
//Runs in O(n^2) worst case
//*could be replaced by faster sort
//-----------------
void sortPopulation()
{
	int max;	//track max fitness
	
	//for each spot in the population
	for(int i = 0; i < INITIAL_POPULATION-1; i++)
	{
		max = i;
		//find the max value, move it there
		for(int j = i+1; j<INITIAL_POPULATION; j++)
		{
			if(population[j]->fitness > population[max]->fitness)
			{
				max = j;
			}
		}	
		
		//swap fitness & value
		chromosome temp;
		temp.fitness = population[i]->fitness;
		strcpy(temp.val,population[i]->val);
		
		population[i]->fitness = population[max]->fitness;
		strcpy(population[i]->val,population[max]->val);
		
		population[max]->fitness = temp.fitness;
		strcpy(population[max]->val,temp.val);
	}
}

//-----------------
//generateNewPopulation
//
//This is the genetic modification code.  It does the following modifications in order:
//	1.  Add new random chromosomes
//	2.  Reproduce with top chromosomes
//	3.  Reproduce with entire pop
//	4.  Mutate top chromosomes
//	5.  Mutate all chromosomes
//*note that these can be tuned by the parameters above main
//These mutations are done in order to introduce new possible solutions to the population
//to avoid becoming stuck on local optima
//------------------
void generateNewPopulation()
{
	//Add random chromosomes
	for(int i = RANDOM_START; i < RANDOM_END; i++)
	{
		for(int j = 0; j < CHROMOSOME_LENGTH; j++)
		{
			population[i]->val[j] = (char)tolower(rand()%(ASCII_UPPER_BOUND-ASCII_LOWER_BOUND+1)+ASCII_LOWER_BOUND);
		}
	}
	
	//Add reproductions of top chromosomes
	for(int i = BIASED_REPRODUCTION_START; i < BIASED_REPRODUCTION_END; i++)
	{
		//pick random index for parents from top chromosomes
		int mom = rand() % TOP;
		int dad = rand() % TOP;
		
		//pick random split point
		int splitind = rand() %CHROMOSOME_LENGTH;
		
		//create new chromosome from first half of mom concatenated with
		//second half of dad
		for(int j = 0; j < splitind; j++)
		{
			population[i]->val[j] = population[mom]->val[j];
		}
		for(int j = splitind; j < CHROMOSOME_LENGTH; j++)
		{
			population[i]->val[j] = population[dad]->val[j];
		}
	}
	
	//Add reproductions of any other chromosomes
	for(int i = NONBIASED_REPRODUCTION_START; i < NONBIASED_REPRODUCTION_END; i++)
	{
		//pick indexes of parents (not limited to top chromosomes)
		int mom = rand()%NONBIASED_REPRODUCTION_START;
		int dad = rand()%NONBIASED_REPRODUCTION_START;
		
		//index to split on
		int splitind = rand()%CHROMOSOME_LENGTH;
		
		//create new chromosome by filling in data from the parents
		for(int j = 0; j < splitind; j++)
		{
			population[i]->val[j] = population[mom]->val[j];
		}
		for(int j = splitind; j < CHROMOSOME_LENGTH; j++)
		{
			population[i]->val[j] = population[dad]->val[j];
		}
	}
	
	//Mutations of top chromosomes
	for(int i = BIASED_MUTATION_START; i < BIASED_MUTATION_END; i++)
	{
		//pick a parent to mutate
		int parent = rand()%(TOP);
		
		//copy it over parent chromosome 
		strcpy(population[i]->val,population[parent]->val);
		
		//generate index of mutation
		int mutateind = rand()%CHROMOSOME_LENGTH;
		
		//set index of mutation to be random
		population[i]->val[mutateind] = (char)tolower(rand()%(ASCII_UPPER_BOUND-ASCII_LOWER_BOUND+1)+ASCII_LOWER_BOUND);
	}
	
	//Mutations of any other chromosome
	for(int i = NONBIASED_MUTATION_START; i < NONBIASED_MUTATION_END; i++)
	{
		//pick parent to mutate
		int parent = rand()%(NONBIASED_MUTATION_START);
		
		//copy over parent's chromosome
		strcpy(population[i]->val,population[parent]->val);
		
		//pick index to mutate on
		int mutateind = rand()%CHROMOSOME_LENGTH;
		
		//set mutate index to random
		population[i]->val[mutateind] = (char)tolower(rand()%(ASCII_UPPER_BOUND-ASCII_LOWER_BOUND+1)+ASCII_LOWER_BOUND);
	}
}