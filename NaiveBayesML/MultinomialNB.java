import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


public class MultinomialNB {

	public static Set<String> vocabWords = new HashSet<String>();
	public static int countTotalFiles = 0;
	public static HashMap<String, ArrayList<String>> textc = new HashMap<String, ArrayList<String>>();
	public static HashMap<String, Double> prior = new HashMap<String, Double>();
	public static ArrayList<HashMap<String, Double>> condProb = new ArrayList<HashMap<String,Double>>(); 
	public static int[] totalFilesInDocs = new int[5];
	public static ArrayList<String> classNames = new ArrayList<String>();
	public static ArrayList<HashMap<String,Integer>> tct = new ArrayList<HashMap<String,Integer>>();
	public static int countTestFiles = 0;
 	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		//File trainingFile=new File("E:\\My stuff\\UTD\\Text books\\3rd sem\\ML\\Assignments\\Assignment 4\\20news-bydate-train");
		//File testFile = new File("E:\\My stuff\\UTD\\Text books\\3rd sem\\ML\\Assignments\\Assignment 4\\20news-bydate-test");
		File trainingFile = new File(args[0]);
		File testFile = new File(args[1]);
		File[] fitr = trainingFile.listFiles();
		File[] fite = testFile.listFiles();
		File[] fiveFilesTr = new File[5]; 
		File[] fiveFilesTe = new File[5]; 
		int[] ra = new Random().ints(0, 19).distinct().limit(5).toArray();
		for(int i=0;i<5;i++) {
			
			fiveFilesTr[i]=fitr[ra[i]];
			fiveFilesTe[i]=fite[ra[i]];
		}
		
		System.out.println("Training files : ");
		System.out.println();
		for(int v=0;v<5;v++)
			System.out.println(fiveFilesTr[v].getName());
		System.out.println();
		System.out.println("Test files : ");
		System.out.println();
		for(int v=0;v<5;v++)
			System.out.println(fiveFilesTe[v].getName());
		System.out.println();

		extractVocabulary(fiveFilesTr);
		calculateProbability();
		countTokensOfTerm(fiveFilesTr);
		applyMultinomialNB(fiveFilesTe);
	}
	
	/**
	 * @param file
	 * @throws IOException
	 */
	public static void extractVocabulary(File[] file) throws IOException{
		String str;
		int countFilesInDocs = 0, count =0;
		File files[] = file;
		for(int k=0;k<5;k++){
			countFilesInDocs = 0;
			ArrayList<String> vocabWordsPerClass = new ArrayList<String>();
			ArrayList<String> readWords = new ArrayList<String>();
			File f = files[k];
			File newFiles[] = f.listFiles();
			for(File newf : newFiles){
				File fil = new File(newf.getAbsolutePath());
				countTotalFiles++;
				countFilesInDocs++;
				FileReader fr = new FileReader(fil);
				BufferedReader bf = new BufferedReader(fr);
				while((str =bf.readLine()) != null)
				{								
					if(str.contains("Lines:"))
					{
						break;
					}													
				}
				while((str = bf.readLine()) != null){
					readWords.add(str.toLowerCase());
				}
			}
			vocabWordsPerClass = RemoveStopWords.remove(readWords);
			for(String st: vocabWordsPerClass)
				vocabWords.add(st);
			textc.put(f.getName(), vocabWordsPerClass);
			classNames.add(f.getName());
			totalFilesInDocs[count] =  countFilesInDocs;
			count++;
		}
	}
	
	/**
	 * 
	 */
	public static void calculateProbability(){
		for(int i=0; i< totalFilesInDocs.length; i++){
			prior.put(classNames.get(i),(double)totalFilesInDocs[i]/countTotalFiles);
		}
	}
	
	/**
	 * @param file
	 */
	public static void countTokensOfTerm(File[] file){
		File files[] = file;
		int value =0;
		Double val;
		for(int p=0;p<5;p++){
			HashMap<String,Integer> classCountTokens = new HashMap<String, Integer>();
			ArrayList<String> ne = new ArrayList<String>();
			File f=files[p];
			ne = textc.get(f.getName());
			for(String st : ne){
				if(!classCountTokens.containsKey(st)){
					classCountTokens.put(st, 1);
				}else if(vocabWords.contains(st)){
					classCountTokens.put(st, classCountTokens.get(st)+1);
				}
			}

			tct.add(classCountTokens);
			HashMap<String, Double> condProbMap = new HashMap<String, Double>();
			for(String st:vocabWords){
				if(classCountTokens.get(st) == null)
					value =0;
				else
					value = classCountTokens.get(st);
				val = ((double)(value+1)/(ne.size()+vocabWords.size()));
				condProbMap.put(st, val);
			}
			condProb.add(condProbMap);
		}
	}
	
	/**
	 * @param file
	 * @throws IOException
	 */
	public static void applyMultinomialNB(File[] file) throws IOException{
		File files[] = file;
		int predictCount = 0;
		for(int d=0;d<5;d++){
			File f=files[d];
			File newFiles[] = f.listFiles();
			for(File newf : newFiles){
				ArrayList<String> testWords = new ArrayList<String>();
				double[] score = new double[5];
				testWords = extractTestTokens(newf);
				for(int w=0;w<5;w++){
					File fl = files[w];
					score[w] = Math.log(prior.get(fl.getName()));
					for(String st : testWords){
						if(!vocabWords.contains(st))
							continue;
						score[w]+= Math.log(condProb.get(w).get(st));
					}
				}
				double max = score[0];
				int index = 0;
				for(int s = 1; s < score.length; s++){
					if(max < score[s]){
						max = score[s];
						index = s;
					}
				}
				if(files[index].getName().equals(f.getName())){
					predictCount++;
				}
				countTestFiles++;
			}
			
		}
		System.out.println("The Accuracy is : "+((double)predictCount/countTestFiles)*100+" %");
		
	}
	
	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<String> extractTestTokens(File file) throws IOException{
		String str;	
		ArrayList<String> readWordsFromTest = new ArrayList<String>();
		ArrayList<String> wordsAfterStopTest = new ArrayList<String>();
		FileReader fr = new FileReader(file);
		BufferedReader bf = new BufferedReader(fr);
		while((str =bf.readLine()) != null)
		{								
			if(str.contains("Lines:"))
			{
				break;
			}													
		}
		while((str = bf.readLine()) != null){
			readWordsFromTest.add(str.toLowerCase());
		}
		wordsAfterStopTest = RemoveStopWords.remove(readWordsFromTest);
		return wordsAfterStopTest;
	}
}
