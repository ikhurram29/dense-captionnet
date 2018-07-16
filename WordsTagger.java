import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class WordsTagger {
	private MaxentTagger tagger = new MaxentTagger("tagger/english/models/english-left3words-distsim.tagger");
	private List<String> fileLines = new ArrayList<String>();
	private List<String> taggedLines = new ArrayList<String>();
//	private List<String> tags2Retain= Arrays.asList("NN","NNS","NNP","NNPS","JJ","JJR","JJS");
//	private List<String> tags2Retain= Arrays.asList("VB","VBD","VBG","VBN","VBP","VBZ","JJ","JJR","JJS");
	private List<String> tags2Retain= Arrays.asList("JJ","JJR","JJS");
	public void processFile(String filePath, String filePathOutput) {
		System.out.println("Reading form file: " + filePath);
		readFileData(filePath);
		writeFileData(filePathOutput);
	}

	private void readFileData(String filePath) {
		try {
			File f = new File(filePath);
			BufferedReader b = new BufferedReader(new FileReader(f));
			String readLine = "";
			while ((readLine = b.readLine()) != null) {
				fileLines.add(readLine);
				String tagged = tagger.tagString(readLine);
				System.out.println(tagged);
				String replaced=replaceWordsByTags(tagged);
				taggedLines.add(replaced);
				System.out.println(replaced);
			}
			System.out.println("File read successfully!");
			b.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void writeFileData(String filePathOutput){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filePathOutput, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		for(String tagged:this.taggedLines){
			writer.println(tagged);
		}
		System.out.println("Tagged string written successfully!");
		writer.close();
	}
	private String replaceWordsByTags(String taggedString) {
		StringBuilder replacedString = new StringBuilder("");
		String[] tokens = taggedString.split(" ");
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].contains("_")) {
				String[] taggedTokens = tokens[i].split("_");
				if(tags2Retain.contains(taggedTokens[1].trim())){
					replacedString.append(taggedTokens[1]);
					replacedString.append(" ");
				}else{
					replacedString.append(taggedTokens[0].toLowerCase());
					replacedString.append(" ");
				}
			}
		}
		return replacedString.toString().trim();
	}

	public static void main(String[] args) {
		WordsTagger wordsTaggerInput = new WordsTagger();
		wordsTaggerInput.processFile("input_untagged.en", "input.en");
	
	}
}
