import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.text.Normalizer;

public class TP3 {
	
	public static String textSegmentation(String sentences, String phoneme){
		
		// put all symbols in ArrayList<String> by the function symbolsToArrayList
		ArrayList<String> symbols = new ArrayList<>();
		
		HashMap<String, Integer> symbols_calcul = new HashMap<>();
		
		symbols = symbolsToArrayList(phoneme);
		// put symbols in the symbols_calcul
		for(int j = 0; j < symbols.size(); j++) {
			symbols_calcul.put(symbols.get(j), 0);
		}
		
		String text_segmentation = "";
		
		int correct = 0;
		int incorrect = 0;
		int nb_sentences = 0;
		for (String sentence: sentences.split("\n")) {
			String nsentence = Normalizer.normalize(sentence, Normalizer.Form.NFKD);
			String sentenceNoSeg = sentenceNoSeg(nsentence);
			String sentenceSeg = sentenceSeg(nsentence);
			// normalisation function
			sentenceNoSeg = normalisation(sentenceNoSeg);
			
			boolean isFound = sentenceNoSeg.contains("BEGAIEMENT");
			if (isFound == true) {
				sentenceNoSeg = "";
			}
			ArrayList<String> new_sentence = new ArrayList<>();
			// sentenceSegmentation function
			new_sentence = sentenceSegmentation(sentenceNoSeg, symbols);
			
			for (int i = 0; i < new_sentence.size();i++) {
				if (!symbols_calcul.containsKey(new_sentence.get(i))) {
					symbols_calcul.put(new_sentence.get(i), 1);
				} else {
					symbols_calcul.put(new_sentence.get(i), symbols_calcul.get(new_sentence.get(i))+1);
				}
			}
			
			String final_sentence = listToStringSpaces(new_sentence);
			
			text_segmentation += final_sentence;
			text_segmentation += "\n";
			if (final_sentence == "") {
				final_sentence = " ";
			}
			if (final_sentence.equals(sentenceSeg)==false) {
				incorrect += 1;
//				System.out.println(nb_sentences);
				System.out.println("Details des phrases incorrectement segmentee:");
				System.out.println("Les phrases originaux: \n"+nsentence);
				System.out.println("Les phrases non-segmentees: \n"+sentenceNoSeg);
				System.out.println("Les phrases prediction: \n"+final_sentence);
				System.out.println("Les phrases reference: \n"+sentenceSeg);
			}
			if (final_sentence.equals(sentenceSeg) == true) {
				correct += 1;
			}
			nb_sentences += 1;
		}
		System.out.println("Nombre des phrases correctement predites: "+correct);
		System.out.println("Nombre des phrases incorrectement predites: "+incorrect);
		System.out.println("Nombre des phrases totals: "+nb_sentences);
		accuracy(correct, nb_sentences);
		
		return text_segmentation;
	}
	
	public static ArrayList<String> sentenceSegmentation(String sentenceNoSeg, ArrayList<String> symbols){
	
		int length = sentenceNoSeg.length();
		ArrayList<String> new_sentence = new ArrayList<>();
		
		for (int counter = 0; counter < length ; counter ++) {
			int word_length = 6;
			while (word_length > 0) {
				if (counter + word_length > length) {
				
					if (symbols.contains(sentenceNoSeg.substring(counter, length))) {
						new_sentence.add(sentenceNoSeg.substring(counter, length));
						counter = length;
					}
				} else { 
					if (symbols.contains(sentenceNoSeg.substring(counter, counter + word_length))) {
						new_sentence.add(sentenceNoSeg.substring(counter, counter + word_length));
						counter += word_length-1;
						word_length -= 10;
					}
				}
				word_length -= 1;
			}
		}
		return new_sentence;
	}
	
	public static ArrayList<String> symbolsToArrayList(String phoneme) {
		ArrayList<String> symbols = new ArrayList<>();
		
		for (String symbol: phoneme.split("\n")) {
			String nsymbol = Normalizer.normalize(symbol.split(":")[0], Normalizer.Form.NFKD);
			symbols.add(nsymbol);
		}
		symbols.add("|");
		symbols.add("\u02E9");
		symbols.add("\u02E5");
		symbols.add("\u02E7");
		symbols.add("\u02E7\u02E5");
		symbols.add("\u02E9\u02E5");
		symbols.add("\u02E9\u02E7");
		symbols.add("\u02E7\u02E9");
		symbols.add("\u006D\u006D\u006D...");
		symbols.add("\u0259\u0259\u0259...");
		
		return symbols;
	}
	
	public static String sentenceNoSeg(String sentence) {
		
		String sentenceNoSeg = sentence.split("@@@")[0];
		
		return sentenceNoSeg;
	}
	
	public static String sentenceSeg(String sentence) {
		
		String sentenceSeg = sentence.split("@@@")[1];
		
		return sentenceSeg;
	}
	
	public static String normalisation(String sentence) {
		// toPipe
		sentence = sentence.replaceAll("\u25CA", "|");
		// <> contenu doit etre reserve
		sentence = sentence.replaceAll("\\<", "").replaceAll("\\>","");
		// []
		sentence = sentence.replaceAll("\\[.*?\\]", "");
		// wæ̃ -> w̃æ
		sentence = sentence.replaceAll("\u0077\u00e6\u0303", "\u0077\u0303\u00e6");
		// ̍ṽ̍ -> ṽ̩
		sentence = sentence.replaceAll("̍̍\u0076\u0303\u030D", "\u0076\u0329\u0303̩");
		// ə+...
//		sentence = sentence.replaceAll("ə+[\u2026(...)]", "əəə...");
		// m+...
		sentence = sentence.replaceAll("\u006D+[\u2026(...)]", "\u006D\u006D\u006D...");
		sentence = sentence.replaceAll("\"qh+.*ə!\"", "qhə");
	
		return sentence;
	}
	
	public static String listToStringSpaces(ArrayList<String> sentence) {
		String output = "";
		
		for (int i = 0; i < sentence.size();i++ ) {
			output += " ";
			output += sentence.get(i);
		}
		return output;
	}
	
	public static double accuracy(Integer correct, Integer nb_sentence) {
		double correct_d = correct;
		double nb_sentence_d = nb_sentence;
		double accuracy = correct_d/nb_sentence_d;
		System.out.println("accuracy: "+ accuracy);
		return accuracy;
	}

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
	
	public static void main(String[] args) {
	String na_phoneme = readFile("na_phonemes.txt");
	String na_text = readFile("final_na_examples.txt");
	
	String textSegmentation = textSegmentation(na_text, na_phoneme);
	
	}

}
