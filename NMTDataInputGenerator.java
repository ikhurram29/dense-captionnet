import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class NMTDataInputGenerator {

	JsonParser jParser;
	PrintWriter out;
	private Map<Integer,List<String>> regionCaptions= new HashMap<Integer, List<String>>();
	private Map<Integer,List<String>> attributeCaption= new HashMap<Integer,List<String>>();
	private Map<Integer,List<String>> attributes= new HashMap<Integer,List<String>>();
	private int imageId4Regions;
	private int imageId4Attributes;
	
	public NMTDataInputGenerator() {
	}

	public void readRegionCaptions(String path) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));     
			if (br.readLine() != null) {
				JsonFactory jfactory = new JsonFactory();
				jParser = jfactory.createParser(new File(path));
				while (jParser.nextToken() != JsonToken.END_OBJECT){
					while (jParser.nextToken() != JsonToken.END_OBJECT){
						String fieldnameTop = jParser.getCurrentName();
						if ("results".equals(fieldnameTop)) {
							while (jParser.nextToken() != JsonToken.END_ARRAY) {
								while (jParser.nextToken() != JsonToken.END_OBJECT){
									String tagName=jParser.getCurrentName();
									if(tagName!=null){
										if("img_name".equals(tagName.trim())){
											jParser.nextToken();
											System.out.println("->"+jParser.getValueAsString());
											imageId4Regions=parseImageName(jParser.getValueAsString());
											List<String> regions=regionCaptions.get(imageId4Regions);
											if(regions==null){
												regions=new ArrayList<String>();
												regionCaptions.put(imageId4Regions, regions);
											}
										}
										if("captions".equals(tagName.trim())){
											while (jParser.nextToken() != JsonToken.END_ARRAY) {
												if(jParser.getValueAsString()!=null)
													if(regionCaptions.get(imageId4Regions).size()<10)
														regionCaptions.get(imageId4Regions).add(jParser.getValueAsString());
											}
										}
									}
								}
							}
						}
						break;
					}
				}
				closeAll();
			}
			br.close();
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void readAttributesCaptions(String path) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));     
			if (br.readLine() != null) {
				JsonFactory jfactory = new JsonFactory();
				jParser = jfactory.createParser(new File(path));
				while (jParser.nextToken() != JsonToken.END_OBJECT){
					while (jParser.nextToken() != JsonToken.END_OBJECT){
						String fieldnameTop = jParser.getCurrentName();
						if ("results".equals(fieldnameTop)) {
							while (jParser.nextToken() != JsonToken.END_ARRAY) {
								while (jParser.nextToken() != JsonToken.END_OBJECT){
									String tagName=jParser.getCurrentName();
									if(tagName!=null){
										if("img_name".equals(tagName.trim())){
											jParser.nextToken();
											System.out.println("->"+jParser.getValueAsString());
											imageId4Attributes=parseImageName(jParser.getValueAsString());
											List<String> regions=attributeCaption.get(imageId4Attributes);
											if(regions==null){
												regions=new ArrayList<String>();
												attributeCaption.put(imageId4Attributes, regions);
											}
										}
										if("captions".equals(tagName.trim())){
											while (jParser.nextToken() != JsonToken.END_ARRAY) {
												if(jParser.getValueAsString()!=null)
													if(attributeCaption.get(imageId4Attributes).size()<10)
														attributeCaption.get(imageId4Attributes).add(jParser.getValueAsString());
											}
										}
									}
								}
							}
						}
						break;
					}
				}
				closeAll();
			}
			br.close();
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		computeAttributes(this.attributeCaption);
	}
	private void computeAttributes(Map<Integer,List<String>> attributeCaptions){
		for(Integer imageId: attributeCaptions.keySet()){
			for(String attrib:attributeCaptions.get(imageId)){
				String objectName=attrib.split(" ")[0];
				String adjective=attrib.split(" ")[3];
				List<String> attribs=this.attributes.get(imageId);
				if(attribs==null){
					attribs=new ArrayList<String>();
					this.attributes.put(imageId, attribs);
				}
				this.attributes.get(imageId).add(adjective+" "+objectName);
			}
		}
	}
	private int parseImageName(String name){
		String[] items=name.split("_");
		String imageName=items[items.length-1];
		items=imageName.split("\\.");
		String id=items[0];
		return Integer.parseInt(id);
//		return 25552;
	} 
	public void closeAll() throws IOException {
		jParser.close();
//		out.close();
	}
	public void printAll(){
//		TextSimilarity similarity=new TextSimilarity();
//		Disco discoRAM = new DISCO(discoDir, true);
//		similarity.directedTextSimilarity(arg0, arg1, arg2, DISCO.SimilarityMeasure)
		for(Integer imageId: regionCaptions.keySet()){
			System.out.println("image id: "+imageId+"--regions:");
			for(String caption:regionCaptions.get(imageId)){
				System.out.print(caption);
				System.out.print(", ");
			}
			for(String attrib: attributes.get(imageId)){
				System.out.println(attrib);
				System.out.println(", ");
			}
			System.out.println();
		}
	}
	public void writeAll(String nmtFilePath, String imageIdsFilePath){
		System.out.println("writing file for nmt...");
		PrintWriter inputWriter = null;
		PrintWriter inputWriterIds = null;
		try {
			inputWriter = new PrintWriter(nmtFilePath, "UTF-8");
			inputWriterIds = new PrintWriter(imageIdsFilePath, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		int index=1;
		StringBuilder inputString=null;
		for (Integer imageId : regionCaptions.keySet()) {
			inputString = new StringBuilder();
			for (String caption : regionCaptions.get(imageId)) {
				inputString.append(caption);
				inputString.append(", ");
			}
			for (String attrib : attributes.get(imageId)) {
				inputString.append(attrib);
				inputString.append(", ");
			}
			inputString.replace(inputString.lastIndexOf(", "),
					inputString.length(), "");

			System.out.print(index+"-");
			inputWriter.println(inputString);
			inputWriterIds.println(imageId);
			index++;
		}
		inputWriter.close();
		inputWriterIds.close();
		System.out.println("file written successfully!");
	}

	public static void main(String[] args) {
		NMTDataInputGenerator generator = new NMTDataInputGenerator();
		generator.readRegionCaptions("regions_results.json");
		generator.readAttributesCaptions("attrib_results.json");
		generator.printAll();
		generator.writeAll("input_untagged.en", "input_image_ids.en");
	}
}
