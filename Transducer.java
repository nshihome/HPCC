//import gate.Annotation;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.ANNIEConstants;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;

import java.io.*;
import java.net.*;
import java.util.Scanner;

//Author: Nuo Shi

public class Transducer{
	private static gate.Corpus corpus;
	private static gate.creole.SerialAnalyserController pipeline;
	
	static String readFile(String path) throws IOException
	{
		//byte[] encoded = Files.readAllBytes(Paths.get(path));
		//return new String(encoded, encoding);
		
		String content=new Scanner(new File(path)).useDelimiter("\\Z").next();
		return content;
	}
	
	public static String run(String input, String jape_path, String input_annotation, String rule_priority, String rule_name) throws Exception{
			
	
		System.out.println("Initialising GATE...");
		initCreole("/home/nshi/hpccdemo/gate_install");
		//Gate.init();
		
		System.out.println("Creating gate copus\n");
		createCorpusbyDoc(input);
		
		System.out.println("Get jape grammarURL");
		URL grammarURL=getJapeURL(jape_path,input_annotation,rule_priority,rule_name);
		
		
		System.out.println("Using gate to process the documents");
		String result=runProcessingResources(grammarURL);  
		
		return result; 
	}
	
	
	
private static void initCreole(String gateHome) throws GateException{
		System.setProperty("gate.home",gateHome);
		Gate.init();
		try {
			Gate.getCreoleRegister().registerDirectories(new File(
					Gate.getPluginsHome(),ANNIEConstants.PLUGIN_DIR).toURI().toURL());
		} 
		catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
			}	
		
	}
	
	
	private static void createCorpusbyDoc(String text)throws Exception{
		if(text!=null && text!=""){
			Document indoc=Factory.newDocument(text);
			corpus=Factory.newCorpus("My Gate Corpus");
			corpus.add(indoc);
			System.out.println("Corpus created successfully\n");
		}else{
			throw new Exception("Corpus text is null\n");
		}
	}
	
	private static URL getJapeURL(String japePath, String input_annotation, String rule_priority, String rule_name) throws Exception{
		//Charset japeCharset=Charset.forName("UTF-8");
		String japeContent=readFile(japePath);
		System.out.println(japeContent);
		String newJapeContent=japeContent.replaceAll("@INPUT_ANNOTATION@", input_annotation).
				replaceAll("@RULE_PRIORITY@", rule_priority).replaceAll("@RULE_NAME@", rule_name);
		
		final byte[] bytes = newJapeContent.getBytes("UTF-8");
		URLStreamHandler handler = new URLStreamHandler(){
			public URLConnection openConnection(URL u){
				return new URLConnection(u){
						public void connect(){
							//do nothing, but superclass method is abstract
						}
				public InputStream getInputStream(){
					return new ByteArrayInputStream(bytes);
				}
			 };
			}
		};
		
		URL grammarURL = new URL(null, "file:/dummy.jape",handler);
		return grammarURL;
	}
	
	
	
	private static String runProcessingResources( URL grammarURL) throws Exception{
		pipeline = (SerialAnalyserController) Factory
				.createResource("gate.creole.SerialAnalyserController");

		
		FeatureMap transducerParams = gate.Utils.featureMap();
		transducerParams.put("grammarURL", grammarURL);
		
		pipeline.add((gate.LanguageAnalyser)Factory.createResource("gate.creole.Transducer",transducerParams));
		
		System.out.println("jape Transducer created\n"); 
		
		
		pipeline.setCorpus(corpus);
		
		System.out.println("Running processing resources over corpus...\n");
		pipeline.execute();
		System.out.println("--Running PR done\n\n");
		
		return corpus.get(0).toXml();
		
	}
	

public static void main(String[] args) throws Exception{
		
		String text="<tokenID>1</tokenID>dffa<string>Alice</string><tokenID>3</tokenID>fdagfa<string>Helen</string>";
			
		//String japePath="/Users/nuoshi/Desktop/GateDoc/transducer/femaleCategory.jape";
		String japePath="/home/nshi/gate_module/japerule/femaleCategory.jape";
		
		String result=null;
		try {
		  	//Tokenizer token= new Tokenizer();
			result=Transducer.run(text,japePath,"Lookup","20","girlCategory");
		}
		catch(Exception e){
			e.printStackTrace();
		}

		System.out.println(result);
	}	
	
	
	
}
