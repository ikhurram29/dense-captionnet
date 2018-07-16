import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class TagsReplacer {
	private MaxentTagger tagger = new MaxentTagger("tagger/english/models/english-left3words-distsim.tagger");
	private List<String> fileLines = new ArrayList<String>();
	private List<String> taggedLines = new ArrayList<String>();
	private List<String> lines = new ArrayList<String>();
//	private List<String> tags2Retain= Arrays.asList("NN","NNS","NNP","NNPS","JJ","JJR","JJS");
//	private List<String> tags2Retain= Arrays.asList("VB","VBD","VBG","VBN","VBP","VBZ","JJ","JJR","JJS");
	private List<String> tags2Replace= Arrays.asList("JJ","JJR","JJS");

	private void readFiles(String actualInputFile, String taggedOutputFile, String outputPath) {
		try {
			File f = new File(taggedOutputFile);
			BufferedReader b = new BufferedReader(new FileReader(f));
			String readLine = "";
			while ((readLine = b.readLine()) != null) {
				taggedLines.add(readLine);
				System.out.println(readLine);
			}
			System.out.println("File read successfully!");
			b.close();
			f = new File(actualInputFile);
			b = new BufferedReader(new FileReader(f));
			while ((readLine = b.readLine()) != null) {
				fileLines.add(readLine);
				System.out.println(readLine);
			}
			System.out.println("Files read successfully!");
			b.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		replaceTags();
		writeFileData(outputPath);
	}
	private void replaceTags(){
		int index=0;
		for(String taggedLine: taggedLines){
			String replaced=replaceTagsByWords(taggedLine, fileLines.get(index));
			lines.add(replaced);
			index++;
		}
	}
	private void writeFileData(String filePathOutput){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filePathOutput, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		for(String line:this.lines){
			writer.println(line);
		}
		System.out.println("tag removed string written successfully!");
		writer.close();
	}
	private String replaceTagsByWords(String taggedString, String originalInput) {
		StringBuilder replacedString = new StringBuilder("");
		System.out.println(taggedString);
		System.out.println(originalInput);
//		JJ JJ trees in the JJ JJ background <unk> a JJ JJ fence and a JJ JJ fence <unk>
		String[] tokens = taggedString.split(" ");
		String[] originalTokens=originalInput.split(" ");
		if("a JJ JJ sky with a JJ JJ sky in the background".equals(taggedString.trim()))
		for (int i = 0; i < tokens.length; i++) {
			if(tags2Replace.contains(tokens[i])){
				if(i+1<tokens.length){
					int indexOfNoun=calculateNoun(tokens,i);
					int total=calculateTotalAdjectives(tokens, indexOfNoun);
					System.out.println("total adj:"+total);
					Set<String> originalAdjectives= new HashSet<String>();
					for(int j=0;j< originalTokens.length; j++){
						if(originalTokens[j].replaceAll("[^\\w]", "").equals(tokens[indexOfNoun])){
							originalAdjectives.addAll(calculateAllAdjectives(tokens[indexOfNoun],originalTokens));
//							System.out.println(tokens[i]);
//							System.out.println(tokens[i+1]);
//							System.out.println(originalTokens[j]);
//							if((j-1)<0){
//								continue;
//							}else{
//								System.out.println("=>"+originalTokens[j-1]);
//								replaceAdjectives();
//								tokens[i]=originalTokens[j-1];
//							}
						}
					}
					tokens=replaceAdjectives(originalAdjectives,tokens, indexOfNoun, total);
				}
			}
//			replacedString.append(" "+tokens[i]);
		}
		
		for (int i = 0; i < tokens.length; i++) {
			if(tags2Replace.contains(tokens[i])){
			}else if("<unk>".equals(tokens[i]) && i==(tokens.length-1)){
			}else if("<unk>".equals(tokens[i])){
				replacedString.append(", ");
			}else
			replacedString.append(" "+tokens[i]);
		}
		return replacedString.toString().trim();
	}
	private int calculateNoun(String[] tokens, int index){
		int i=0;
		for(int j=index+1;j<tokens.length;j++){
			String tagged = tagger.tagString(tokens[j]);
			String[] taggedTokens = tagged.split("_");
			if(!tags2Replace.contains(taggedTokens[0])){
				System.out.println("noun: "+taggedTokens[0]);
				i=j;
				break;
			}
		}
		return i;
	}
	private int calculateTotalAdjectives(String[] tokens, int index){
		int total=0;
		for(int i=(index-1); i>=0;i--){
//			String tagged = tagger.tagString(tokens[i]);
//			String[] taggedTokens = tagged.split("_");
			if(tags2Replace.contains(tokens[i])){
				total++;
			}else{
				break;
			}
		}
		return total;
	}
	private List<String> calculateAllAdjectives(String token2Match, String[] tokens){
		List<String> adjc=new ArrayList<String>();
		List<Integer> indexes=new ArrayList<Integer>();
		for(int i=0;i<tokens.length;i++){
			if(token2Match.trim().equals(tokens[i].trim().replaceAll("[^\\w]", ""))){
				indexes.add(i);
			}
		}
		for(Integer index:indexes){
//			System.out.println("index: "+index);
			for(int i=(index-1); i>=0;i--){
	//		for(int i=(tokens.length-1); i>=0;i--){
			String pureToken=tokens[i].replaceAll("[^\\w]", "");
				String tagged = tagger.tagString(pureToken);
//				System.out.println(tagged);
				String[] taggedTokens = tagged.split("_");
				if(tags2Replace.contains(taggedTokens[1].trim())){
					System.out.println("adj in original: "+taggedTokens[0]);
					adjc.add(taggedTokens[0]);
				}else{
					break;
				}
			}
		}
		return adjc;
	}
	private String[] replaceAdjectives(Set<String> originalAdj, String[] tokens, int indexOfNoun, int total){
		int index=0;
		List<String> originalAdjList = new ArrayList<String>(originalAdj);

		for(int i=(indexOfNoun-1); i>=total;i--){
//			String tagged = tagger.tagString(tokens[i]);
			System.out.println("tagged:"+tokens[i]);
//			String[] taggedTokens = tagged.split("_");
			if(tags2Replace.contains(tokens[i].trim())){
				if(originalAdjList.size()>index){
					tokens[i]=originalAdjList.get(index);
					index++;
				}else{
//					tokens[i]="";
				}
			}else{
				break;
			}
		}
		return tokens;
	}
	public static void main(String[] args) {
		TagsReplacer tagReplacer = new TagsReplacer();
		tagReplacer.readFiles("input.en","output_infer", "output_infer_tags_replaced");
	
	}
}
