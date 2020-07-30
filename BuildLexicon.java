import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuildLexicon {

	// Affiche les paires triées par likelihood ratio
	private static void printSortedLikelikhoodTable(HashMap<String, Double> res) {

		for (Entry<String, Double> r : MapUtil.sortByValue(res)) {
			System.out.println(r.getKey() + "->" + r.getValue());
		}

	}

	// Afficher la table de coocurences par ordre de fréquences croissantes
	private static void printSortedCoocTable(
			HashMap<String, HashMap<String, Integer>> coocTable) {

		HashMap<String, Double> res = new HashMap<String, Double>();
		for (String frWord : coocTable.keySet()) {
			HashMap<String, Integer> map = coocTable.get(frWord);

			for (String enWord : map.keySet()) {
				res.put(frWord + "-" + enWord, (double) map.get(enWord));
			}
		}
		
		for (Entry<String, Double> r : MapUtil.sortByValue(res)) {
		    System.out.println(r.getKey() + "->" + r.getValue());
		}

	}

	// Retourne le contenu d'un fichier sous forme d'une chaine de caractères
	public static String readFile(String filename) {
		try {
			FileInputStream stream = new FileInputStream(new File(filename));
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
					fc.size());
			stream.close();
			return Charset.defaultCharset().decode(bb).toString();
			//System.out.println(Charset.defaultCharset().decode(bb).toString());
		} catch (IOException e) {
//			System.out.println("Erreur lors de l'accès au fichier " + filename);
			System.exit(1);
		}
		return null;
	}

    // compte le nombre de phrases dans lequel apparait un mot donné
    // attention : les mots répétés plusieurs fois dans une phrase ne compte que
    // une fois
    // 
    // Méthode à réaliser
    public static HashMap<String, Integer> countSentencesWithWord(String txt) {
    	
    	HashMap<String, Integer> WordCounts = new HashMap<>();
    	
    	for (String line: txt.split("\n")) {
    		//uniqWord is a set
    		HashSet<String> uniqWord = new HashSet<String>(Arrays.asList(line.split(" ")));
    		
    		for (String word : uniqWord) {
	    		if (!WordCounts.containsKey(word)) {
	    			WordCounts.put(word, 1);
	    		} else {
	    			WordCounts.put(word, WordCounts.get(word)+1);
	    		}
    		}
    	}
	return WordCounts;
    }

    // Construit la table de coocurence : à une paire (mot français, mot
    // anglais), on associe le nombre de bi-phrases dans lesquelles cette paire
    // apparait
    // Attention : paires répétées plusieurs fois dans une bi-phrase ne doivent
    // être comptées qu'une fois.
    //
    // Méthode à réaliser
    private static HashMap<String, HashMap<String, Integer>> buildCoocTable(
									    String fr, String en) {
    	
    	HashMap<String, HashMap<String, Integer>> coocTable = new HashMap<>();
    	String[] lines_fr = fr.split("\n");
    	String[] lines_en = en.split("\n");

    	
    	for (int i = 0; i < lines_fr.length; i++) {
    		HashSet<String> uniqWord_fr = new HashSet<>(Arrays.asList(lines_fr[i].split(" ")));
    		HashSet<String> uniqWord_en = new HashSet<>(Arrays.asList(lines_en[i].split(" ")));
    		for(String word_fr: uniqWord_fr) {
    			HashMap<String, Integer> coocurrence = coocTable.get(word_fr);
    			if (coocurrence == null) {
    				coocurrence = new HashMap<String, Integer>();
    				coocTable.put(word_fr, coocurrence);
    			} 
    			for (String word_en: uniqWord_en) {
    				Integer occurrence = coocurrence.get(word_en);
    				coocurrence.put(word_en, occurrence == null? 1 : occurrence + 1);
    				// si occurrence n'est pas dedans, on met une valuer de 1, sinon, on l'ajoute un
    			}
    		}
    	}
//    System.out.println(coocTable);
    return coocTable;
    	
    }

    // Construit la liste des paires et de leur likelihoodRatio
    //
    // Méthode à réaliser
    public static HashMap<String, Double> constructLikelihoodTable(
								   HashMap<String, HashMap<String, Integer>> table,
								   HashMap<String, Integer> frWordCounts,
								   HashMap<String, Integer> enWordCounts, int nSentences) {
    	HashMap<String, Double> ratios = new HashMap<>();
    	
    	for (String word_fr: table.keySet()) {
    		for (String word_en: table.get(word_fr).keySet()) {
    			double ratio = likelihoodRatio(enWordCounts.get(word_en), frWordCounts.get(word_fr), table.get(word_fr).get(word_en), nSentences);
    			if (ratio != Double.NaN) {
    				ratios.put(word_fr + "-" + word_en, ratio);
    			}
    			
    		}
    	}
//    System.out.println(ratios);
	return ratios;
	
    }

    public static double likelihoodRatio(int nA, int nB, int nAB, int n) {
    	
    	double na = nA;
    	double nb = nB;
    	double nab = nAB;
    	double nSentence = n;
    	
    	double pAB = nab / nSentence;
    	double pA = na/nSentence;
    	double pB = nb/nSentence;
    	
    	double pNA = 1-pA;
    	double pNB = 1-pB;
    	double pANB = pA - pAB;
    	double pNAB = pB - pAB;
    	double pNANB = 1 - pAB - pANB - pNAB;
    	
    	double ratio = 0.0;
    	
    	if (pAB != 0.0) {
    		ratio += pAB * Math.log(pAB/pA/pB);
    	} else {
    		ratio += 0.0;
    	}
    	if (pANB != 0.0) {
    		ratio += pANB * Math.log(pANB/pA/pNB);
    	} else {
    		ratio += 0.0;
    	}
    	if (pNAB != 0.0) {
    		ratio += pNAB * Math.log(pNAB/pNA/pB);
    	} else {
    		ratio += 0.0;
    	}
    	if(pNANB != 0.0) {
    		ratio += pNANB * Math.log(pNANB/pNA/pNB);
    	} else {
    		ratio += 0.0;
    	}
    	
    	ratio = Math.sqrt(ratio*2*nSentence);
    	
	return ratio;
    }
    
    public static void main(String[] args) {
	String fr = readFile("french.corpus");
	String en = readFile("english.corpus");
//    String fr = "le vache et le veau\nle chien et le chat";
//    String en = "the cow and the calf\nthe dog and the cat";
	
	int nSentences = fr.split("\n").length;
	
	
	HashMap<String, Integer> frWordCounts = countSentencesWithWord(fr);
	HashMap<String, Integer> enWordCounts = countSentencesWithWord(en);
	HashMap<String, HashMap<String, Integer>> coocTable = buildCoocTable(
									     fr, en);
	
	// attention, la méthode détruit la table de coocurrence !!!
	printSortedCoocTable(coocTable);

	HashMap<String, Double> res = constructLikelihoodTable(coocTable,
							       frWordCounts, enWordCounts, nSentences);
	printSortedLikelikhoodTable(res);
	
    }
    
}
//