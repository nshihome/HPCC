import gate.Gate;
import gate.Document;
import gate.util.GateException;
import gate.Factory;
import gate.creole.ANNIEConstants;
import gate.creole.SerialAnalyserController;
import java.io.File;
import java.net.MalformedURLException;

//Author: Nuo Shi

public class Tokeniser{
	private static gate.Corpus corpus;
	
	public static String run(String text) throws Exception{
		
		
		System.out.println("Initialising GATE...");
		initCreole("/home/nshi/hpccdemo/gate_install");
		
		
		System.out.println("Creating gate copus\n");
		createCorpusbyText(text);
	
		System.out.println("Using gate to process the documents\n");
		String docXML=runProcessingResources();   //Tokenizer
		
		
		System.out.println("Demo Done\n");
		
		return docXML;
		//return text;
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
	
	
	
	private static String runProcessingResources() throws Exception{
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
	
	
	public static void main(String[] args){
		String text="Anna lived in Raleigh for five years ";
		String docXML=null;
		try {
		  	//Tokenizer token= new Tokenizer();
			docXML=Tokeniser.run(text);
		}
		catch(Exception e){
			e.printStackTrace();
		}

		System.out.println(docXML);
	}	
}
