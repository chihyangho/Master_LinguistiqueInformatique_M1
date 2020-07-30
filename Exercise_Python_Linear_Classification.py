# -*- encoding: utf-8 -*-
from __future__ import unicode_literals, print_function, division
import numpy as np
import random

import sys

filename = sys.argv[1]
spambase = sys.argv[2]

# random.seed(nb) can fix the result of random.random(), but why do we put it here?
random.seed(9)

def data_reader(filename):
    to_binary = {"?": 3, "y": 2, "n": 1}
    labels = {"democrat": 1, "republican": -1}

    data = []
    for line in open(filename, "r"):
        line = line.strip()

        label = int(labels[line.split(",")[0]])
        observation = np.array(
            [to_binary[o] for o in line.split(",")[1:]] + [1]) # why + [1]?
        data.append((label, observation))
    # data 形式: (label(1/-1), observation)
    return data

def spam_reader(filename):
    to_binary = {1: 1, 0: -1}
    data = []
    for line in open(filename, "r"):
        line = line.strip()
        label = to_binary[int(line.split(",")[-1])]
        observation = np.array(
            [float(o) for o in line.split(",")[:-1] + [1.0]])

        data.append((label, observation))

    return data

# Codes in class TD, de page 2, question 4
# pour quoi on fait 1 ou -1 au lieu de 0, parce que si c'est 0 ce sera toujours 0
def classify(observation, vecteur):
	if observation.dot(vecteur) >= 0:
		return 1
	else:
		return -1

# Codes in class TD, de page 2, question 5
def accuracy(corpus, vecteur):
	# if the label is the same as the prediction by the vecteur, then it's correct
	# corpus est un ensemble de tuples
	corpus_test = data_reader(filename)

	not_correct = 0
	total = 0

	for example in corpus:
		prediction = classify(example[1], vecteur)
		total += 1

		if prediction != example[0]:
			not_correct += 1
            
	taux_not_correct = not_correct / total
	return taux_not_correct
 
def learn(nb, corpus):
	# The vector always starts by 0 
    vecteur = np.zeros((17,), dtype = int)

    i = 0
    while i < nb:
        label, o = corpus[i]
        if label != classify(o, vecteur):   
            vecteur = vecteur + o.dot(label)
        i += 1
    erreur = accuracy(corpus, vecteur)
    return vecteur, erreur

def learn_test(vector, corpus, nb):
	for label, o in corpus:
		if label != classify(o, vector):
			vector += label*o
	taux_not_correct = accuracy(corpus, vector)
	return vector, taux_not_correct

# compare two taux d'erreur pour obtenir le meilleur taux d'erreur
def learn_less_error(nb, corpus):
    vecteur = np.zeros((17,), dtype = int)
    origine_error = accuracy(corpus, vecteur)
    new_error = 0
    # c = 0
    i = 0
    while i < nb:
        label, o = corpus[i]
        if label != classify(o, vecteur):
        	vecteur = vecteur + o.dot(label)

        	a = new_error
        	b = origine_error
        	new_error = accuracy(corpus, vecteur)
        	# the following if boucle never goes in
        	if a > new_error and new_error == origine_error:
        		# c = i
        		i = nb

        	origine_error = new_error
        i += 1
    return vecteur, new_error, c

def learn_spam(nb, corpus):
	vecteur = np.zeros((58,), dtype = float)
	i = 0
	while i < nb:
		label, o = corpus[i]
		if label != classify(o, vecteur):
			vecteur = vecteur + o.dot(label)
		i += 1
	erreur = accuracy(corpus, vecteur)
	return vecteur, erreur

def learn_spam_test(vector, corpus, nb):
	for label, o in corpus:
		if label != classify(o, vector):
			vector += label*o
	erreur = accuracy(corpus, vector)
	return vector, erreur

def main_partie1(filename):
    w = np.array([25, -12, 67, -104, -43, 46, -18, -10, 45,-33, 54, -39, 43, -19, 5, -2, 55])
    # data = data_reader("./house-votes-84-data")
    data = data_reader(filename)
    random.shuffle(data)
    test, train = data[100:], data[:100]
    return test, train


def main_partie2(spambase):
    data = spam_reader(spambase)
    random.shuffle(data)
    train, test = data[:3500], data[3500:]
    return train, test

if __name__ == "__main__" :
    test, train = main_partie1(filename)
    vector, taux_not_correct = learn(100, train)
    print("Train vector:", vector, "Train incorrect:", taux_not_correct)
    vector2, taux_not_correct2 = learn_test(vector, test, 100)
    print("Test vector:", vector2, "Test incorrect:", taux_not_correct2)
    
    train, test = main_partie2(spambase)
    vector_spam, taux_not_correct_spam = learn_spam(100, train)
    print("Spam train vector:", vector_spam, "Spam train incorrect:", taux_not_correct_spam)
    vector_spam2, taux_not_correct_spam2 = learn_spam_test(vector_spam, test, 100)
    print("Spam test vector:", vector_spam2, "Spam test incorrect:", taux_not_correct_spam2)
    pass
