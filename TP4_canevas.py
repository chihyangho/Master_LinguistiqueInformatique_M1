#!/usr/bin/env python
# -*- coding: utf-8 -*-

# The following from...import... is in case in we use python2?
from __future__ import division, print_function
# loadTrainTest function return (train, test), train is a list of 
from lingspam_data_reader import loadTrainTest

from math import log

SPAM = 0
HAM = 1


class NaiveBayesClassifier:

    def __init__(self):
        """
        self.prior : devra contenir les probabilités P(spam) et P(ham)
            self.prior[SPAM] -> P(spam)
            self.prior[HAM] -> P(ham)

        self.likelihood : devra contenir les probabilités du type
            P(X_i | label = spam) et P(X_i | label = ham)

            exemple: self.likelihood[HAM]["today"] doit contenir
                P(X_today = 1 | ham) c'est-à-dire la probabilité qu'un
                ham contienne le mot "today"

        self.features : doit contenir l'ensemble des mots utilisés pour
            la classification.

        """
        # [spam, ham]
        self.prior = [0.0, 0.0]
        self.likelihood = [{}, {}]
        # This is for after when we wanna know if the words in an unknown email exist in the training
        self.features = []

    def estimate_parameters(self, training_examples, smooth=1e-4):
        """
        Estime tous les paramètres du modèle par fréquence relative
            (et lissage) à l'aide de l'ensemble d'entraînement.

        training_examples : liste de couples (label, set de strings)
            label : 0 ou 1 (SPAM ou HAM)
            chaque couple représente un mail

        smooth : valeur lambda pour effectuer un lissage add-lambda
        """
        # TODO : implémenter cette méthode

        # Put words from spam in all_spam_dico
        # Put words from ham in all_ham_dico
        all_ham_dico = {}
        all_spam_dico = {}
        nb_ham = 0
        nb_spam = 0
        for example in training_examples:
            if example[0] == 1:
                nb_ham += 1
                for word in example[1]:
                    if word in all_ham_dico:
                        all_ham_dico[word] += 1
                    else:
                        all_ham_dico[word] = 1
            elif example[0] == 0:
                nb_spam += 1
                for word in example[1]:
                    if word in all_spam_dico:
                        all_spam_dico[word] += 1
                    else:
                        all_spam_dico[word] = 1

        # Put probability of words in self.likelihood
        for k, v in all_ham_dico.items():
            p_v = v/nb_ham
            self.likelihood[1][k] = p_v + smooth

        for k, v in all_spam_dico.items():
            p_v = v/nb_spam
            self.likelihood[0][k] = p_v + smooth
        # Put probability of being spam or ham in self.prior
        self.prior[0] = nb_spam/(nb_ham+nb_spam)
        self.prior[1] = nb_ham/(nb_ham+nb_spam)

        features_list = []
        # Put words into features_list
        for k in self.likelihood[0].keys():
            features_list.append(k)
        for k in self.likelihood[1].keys():
            features_list.append(k)
        # Use set to remove the doublons
        self.features = set(features_list)

        # Optional: if one word appears in both  dictionaries, remove
        # if one word appears only one time in a dictionary not another, remove
        for k, v in all_ham_dico.items():
            if k in all_spam_dico.keys():
                if all_spam_dico[k] > 30 and all_ham_dico[k] > 30:
                    self.features.remove(k)
            elif v == 1 and k not in all_spam_dico.keys():
                self.features.remove(k)
        for k, v in all_spam_dico.items():
            if v == 1 and k not in all_ham_dico.keys():
                self.features.remove(k)        

        # print(self.prior)
        # print(self.likelihood)

    def predict(self, example):
        """
        Réalise une prédiction d'après la règle de décision (renvoie HAM ou SPAM)

        example : set de strings représentant un mail
        """
        # TODO : implémenter cette méthode
        
        # log(a*b) = log(a) + log(b)
        # Here we calculate the probability of the example being a spam
        count_spam = 0
        count_ham = 0
        for word in example[1]:
            if word in self.features:
                if word in self.likelihood[0]:
                    count_spam += log(self.likelihood[0][word])
                else:
                    count_spam += log(1e-4)
            else:
                count_spam += log(1e-4)
        # Here we calculate the probability of the example being a ham
        for word in example[1]:
            if word in self.features:
                if word in self.likelihood[1]:
                    count_ham += log(self.likelihood[1][word])
                else:
                    count_ham += log(1e-4)
            else:
                count_ham += log(1e-4)
        # If the count_spam + log(self.prior[0]) is bigger, then it's a spam, otherwise, ham
        if (count_spam + log(self.prior[0])) > (count_ham + log(self.prior[1])):
            return 0
        else:
            return 1

    def evaluate(self, test_examples):
        """
        Calcule et renvoie l'exactitude du classifieur sur l'ensemble
        d'exemples.

        test_examples : liste de couples (label, set de strings)
        """
        # TODO : implémenter cette méthode

        correct = 0
        false = 0
        # if the (prediction) value corresponds to its real value, correct += 1, otherwise, false += 1
        for mail in test_examples:
            # We need to add self.predict to call the function of predict
            value = self.predict(mail)
            if value == mail[0]:
                correct += 1
            else:
                false +=1
        exactitude = correct/(correct+false)
        print(exactitude)
        return exactitude


def main():

    train, test = loadTrainTest("./lemm_stop")

    # print(str(len(train)) + " training examples")
    # print(str(len(test)) + " testing examples")

    # in a class we can initialise the class by the following code
    nb = NaiveBayesClassifier()

    nb.estimate_parameters(train)
    nb.evaluate(test)

    # TODO : écrire le code pour entraîner le classifieur et le tester




if __name__ == '__main__':

    main()





