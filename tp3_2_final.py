import sys
import numpy as np

file = sys.argv[1]

# Here we make the content of the file into a list of triplets (tuple of 3 elements)
def words_tuple(file):
	line = (sentence.strip('\n').split() for sentence in file)
	list_tuple = []
	for sentence in line:
		i = 0
		while i < (len(sentence)-2):
			list_tuple.append((sentence[i], sentence[i+1], sentence[i+2]))
			i += 1
	# Here we return a dictionary with the form {(a, b, c)}
	return list_tuple

# We count how many times a triplet (a, b, c) appears in the dictionary
def tuple_in_dico_abc(dico):
	dico_abc = {}
	for t in dico:
		if t in dico_abc:
			dico_abc[t] +=1
		else:
			dico_abc[t] = 1
	# Here we return a dictionary with the form {(a, b, c): times appearing in the corpus}
	return dico_abc

# We count how many times (a,b) appears in the dictionary
def tuple_in_dico_ab(dico):
	dico_ab = {}
	for tuples, values in dico.items():
		if (tuples[0],tuples[1]) in dico_ab:
			dico_ab[(tuples[0], tuples[1])] += values
		else:
			dico_ab[(tuples[0], tuples[1])] = values
	# Here we return a dictionary with the form {(a, b): times appearing in the corpus}
	return dico_ab

# We count the p(c|(a, b)) by giving the number of times appears the (a, b, c) divide by (a, b)
def probability_c_ab(dico_abc, dico_ab):
	for tuples, values in dico_abc.items():
		probability_abc = values
		probability_ab = dico_ab[(tuples[0], tuples[1])]
		dico_abc[tuples] = probability_abc/probability_ab
	# Here we return a dictionary with the form {(a, b, c): p}
	return dico_abc

# Here we change the form of the dictionary (a, b,)
def probability_distrib(dico_abc):
	probability_distrib = {}
	for tuples, values in dico_abc.items():
		if (tuples[0],tuples[1]) in probability_distrib:
			probability_distrib[(tuples[0], tuples[1])][tuples[2]] = values 
		else: 	
			probability_distrib[(tuples[0], tuples[1])] = {tuples[2]: values}
	# The code to count the sum of the values in the nested dico, if == 1, correct
	# dct_sum = {k: sum(v.values()) for k, v in probability_distrib.items()}
	# Here we return a dictionary with the form {(a, b):{c:p}}
	return probability_distrib

# It's a fonction to generate a word randomly
def sample_from_discrete_distrib(distrib):
	words, probas = list(zip(*distrib.items()))
	return np.random.choice(words, p = probas)

# Here we give the dictionary with the form {(a, b): {c:p}}, to generate nb words 
def generation(distrib, nb):
	sentence = []
	word = ''
	# We make the pair ab a variable "pair", so later we can change the value in it
	# to continue generating words
	pair = ('BEGIN', 'NOW')
	while len(sentence) < nb and word != 'END':
		word = sample_from_discrete_distrib(distrib[pair])
		pair = (pair[1], word)
		sentence.append(word)
	print(' '.join(sentence[:-1]))
	# Here we return a list of strings
	return sentence

if __name__ == "__main__":
	
	file_words_tuple = words_tuple(open(file))

	# 2.1-2 
	# we should calculate all the probability of all the tuples
	tuple_in_dico_abc = tuple_in_dico_abc(file_words_tuple)

	# Here we generate a dictionary of key= ab, value= how many times the same ab appears
	tuple_in_dico_ab = tuple_in_dico_ab(tuple_in_dico_abc)

	# 2.1-3 Ecrivez une fonction qui a partir d'une liste de triplets estime les probabilite p(c|(a, b))
	# Here I calculate the probability p(c|(a, b)) in a dict like {(a, b, c): p(c|(a, b))}
	probability_c_ab = probability_c_ab(tuple_in_dico_abc, tuple_in_dico_ab)

	# We change the form of the dict probability_c_ab a: {(a, b):{c:p(c|(a,b))}}
	probability_distrib = probability_distrib(probability_c_ab)
	# print(probability_distrib)

	# 2.2 Generation
	generation = generation(probability_distrib, 100)
	# print(generation)
