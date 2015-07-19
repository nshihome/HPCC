import gate.Annotation;
//import gate.AnnotationSet;
import gate.FeatureMap;
import gate.Gate;
import gate.Document;
//import gate.GateConstants;
import gate.util.GateException;
import gate.Factory;
import gate.creole.ANNIEConstants;
import gate.creole.SerialAnalyserController;


import java.util.ArrayList;
//import gate.creole.gazetteer.Gazetteer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.io.File;
import java.net.MalformedURLException;


public class GateTest{
	private static gate.Corpus corpus;
	
	public static void run(String text, String overrideGazLists) throws Exception{
		//Gate.init();
		
		System.out.println("Initialising GATE...");
		initCreole("/home/hpccdemo/gate_install");
		//Gate.init();
		
		System.out.println("Creating gate copus\n");
		createCorpusbyText(text);
	
		System.out.println("Using gate Tokeniser to process the documents\n");
		String docXML=runTokeniser();   //Tokenizer
		
		System.out.println("Display Documents\n");
		String tokens=displayTokens();
		System.out.println(tokens);
		
		
		System.out.println("Using gate Gazetteer to process the documents\n");
		runGazetteer(overrideGazLists,docXML);   //Gazetteer
		System.out.println("Display Documents\n");
		//String lookups=displayLookups();
		//System.out.println(lookups);
		System.out.println(corpus.get(0).toXml());
		
		System.out.println("Using gate Gazetteer to process the documents\n");
		runGazetteer("/Users/nuoshi/Desktop/GateGaz/newlists/lists.def",docXML);   //Gazetteer
		System.out.println("Display Documents\n");
		String newlookups=displayLookups();
		System.out.println(newlookups);
		
		System.out.println("Demo Done\n");
		
		//return result;
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
	
	
	private static void createCorpusbyText(String text)throws Exception{
		if(text!=null && text!=""){
			Document indoc=Factory.newDocument(text);
			corpus=Factory.newCorpus("My Gate Corpus");
			corpus.add(indoc);
			System.out.println("Corpus created successfully\n");
		}else{
			throw new Exception("Corpus text is null\n");
		}
	}
	
	
	private static String runTokeniser() throws Exception{
		SerialAnalyserController pipeline = (SerialAnalyserController) Factory
				.createResource("gate.creole.SerialAnalyserController");
		pipeline.add((gate.LanguageAnalyser)Factory.createResource("gate.creole.tokeniser.DefaultTokeniser"));
		System.out.println("Tokeniser created\n");
		pipeline.setCorpus(corpus);
		
		System.out.println("Running processing resources over corpus...\n");
		pipeline.execute();
		System.out.println("--Running PR done\n\n");
		
		return corpus.get(0).toXml();
		
		
	}
	
	private static void runGazetteer(String overrideGazLists, String docXML) throws Exception{
		gate.creole.SerialAnalyserController pipeline = (SerialAnalyserController) Factory
				.createResource("gate.creole.SerialAnalyserController");
		
		File GazLists=null;
		FeatureMap params = Factory.newFeatureMap();
		if(overrideGazLists!=null && overrideGazLists!=""){
			GazLists=new File(overrideGazLists);
			if (!GazLists.exists()){
				throw new Exception("Override Gazetteer Lists"+overrideGazLists+" not found\n");
			}
			params.put("listsURL", GazLists.toURI().toURL()); 
		}
		pipeline.add((gate.LanguageAnalyser)Factory.createResource("gate.creole.gazetteer.DefaultGazetteer",params));
		System.out.println("Gazetteer created\n"); 
		
		corpus=null;
		corpus=Factory.newCorpus("My Gate Corpus2");
		
		Document doc = Factory.newDocument(docXML);
		corpus.add(doc);
		
		pipeline.setCorpus(corpus);
		
		System.out.println("Running processing resources over corpus...\n");
		pipeline.execute();
		System.out.println("--Running PR done\n\n");
	}
	
	
	
	
	private static String displayTokens() throws Exception{
		String result="";
		Iterator documentIterator = corpus.iterator();
		while(documentIterator.hasNext()){
			Document currDoc = (Document)documentIterator.next();
			//System.out.println("The tokens of document \"" +currDoc.getSourceUrl().getFile()+"\" are:");
			Set<Annotation> tokenSet = new HashSet<Annotation>(currDoc.getAnnotations().get("Token"));
			Iterator tokenIterator =tokenSet.iterator();
			Annotation currToken;
			StringBuffer sb = new StringBuffer();
			while(tokenIterator.hasNext()){
				currToken=(Annotation) tokenIterator.next();
				long beginOffset=currToken.getStartNode().getOffset().intValue();
				long endOffset=currToken.getEndNode().getOffset().intValue();
				int annoID = currToken.getId();
				    
				FeatureMap annoFeatures = currToken.getFeatures();
			    //Object featureValue=annoFeatures.get("gender");
				sb.append("<TokenAnnotation>").append("<tokenID>").append(String.valueOf(annoID)).append("</tokenID>");
				sb.append("<beginOffset>").append(String.valueOf(beginOffset)).append("</beginOffset>");
				sb.append("<endOffset>").append(String.valueOf(endOffset)).append("</endOffset>");
				sb.append("<kind>").append(String.valueOf(currToken.getFeatures().get("kind"))).append("</kind>");
				sb.append("<length>").append(String.valueOf(currToken.getFeatures().get("length"))).append("</length>");
				sb.append("<orth>").append(String.valueOf(currToken.getFeatures().get("orth"))).append("</orth>");
				sb.append("<string>").append(String.valueOf(currToken.getFeatures().get("string"))).append("</string>");
				//sb.append("<value>").append(currDoc.getContent().getContent(beginOffset, endOffset).toString()).append("</value>");
				sb.append("</TokenAnnotation>");
				}//annotationIterator  
			result=result+sb.toString();
		}//documentIterator 
		
			
			return "<result>"+result+"</result>";
		
	}
	
	
	private static String displayLookups( ) throws Exception{
		String result="";
		Iterator documentIterator = corpus.iterator();
		while(documentIterator.hasNext()){
			Document currDoc = (Document)documentIterator.next();
			Set<Annotation> lookupSet = new HashSet<Annotation>(currDoc.getAnnotations().get("Lookup"));
			Iterator lookupIterator =lookupSet.iterator();
			Annotation currLookup;
			StringBuffer sb = new StringBuffer();
			while(lookupIterator.hasNext()){
				currLookup=(Annotation) lookupIterator.next();
				long beginOffset=currLookup.getStartNode().getOffset().intValue();
				long endOffset=currLookup.getEndNode().getOffset().intValue();
				int annoID = currLookup.getId();
				    
				FeatureMap annoFeatures = currLookup.getFeatures();
			   // Object featureValue=annoFeatures.get("gender");
				sb.append("<LookupAnnotation>").append("<lookupID>").append(String.valueOf(annoID)).append("</lookupID>");
				sb.append("<beginOffset>").append(String.valueOf(beginOffset)).append("</beginOffset>");
				sb.append("<endOffset>").append(String.valueOf(endOffset)).append("</endOffset>");
				sb.append("<majorType>").append(String.valueOf(currLookup.getFeatures().get("majorType"))).append("</majorType>");
				sb.append("<minorType>").append(String.valueOf(currLookup.getFeatures().get("minorType"))).append("</minorType>");
				sb.append("<value>").append(currDoc.getContent().getContent(beginOffset, endOffset).toString()).append("</value>");
				sb.append("</LookupAnnotation>");
				}//annotationIterator
			result=result+sb.toString();
		}//documentIterator 
			
			
			
			return "<result>"+result+"</result>";
		
	}
	
public static void main(String[] args) throws Exception{
		
		String text="nshi lived in Dalian. Then she went to nankai University in 2008";
		String overrideGazLists="/Users/nuoshi/Desktop/GateGaz/lists.def";
		GateTest.run(text,overrideGazLists);
		//GateTest.runGazetteer(overrideGazLists);
	}	
	
	
	
	
	
}


