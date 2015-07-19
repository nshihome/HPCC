import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.creole.ANNIEConstants;
import gate.creole.SerialAnalyserController;
import gate.creole.gazetteer.AbstractGazetteer;
import gate.creole.gazetteer.Lookup;
import gate.util.GateException;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//Author: Nuo Shi

public class Gazetteer{
	
	
	
	public static class gazList{
		String gazlistkey;
		String majortype;
		String minortype;
		String entry;
		public gazList(String gazListKey, String majorType, String minorType, String entry){
			this.gazlistkey=gazListKey;
			this.majortype=majorType;
			this.minortype=minorType;
			this.entry=entry;
		}
		public gazList(){
			
		}
	}
	
	String input;
	gazList lists[];
	
	
	public Gazetteer(String input, gazList lists[]){
		this.input=input;
		this.lists=lists;
		
	}
	public Gazetteer(){
		
	}
	
	/* Create lists.def */
	private static void eclAddLookup(gazList lists[], Map<String, Lookup> lookupLists){
		int i=0;
		while(i<lists.length){
			Lookup lookup = lookupLists.get(lists[i].gazlistkey);
			if(lookup==null){
				lookup=new Lookup(lists[i].gazlistkey,lists[i].majortype,lists[i].minortype,"");
				lookupLists.put(lists[i].gazlistkey, lookup);
			}
			i++;
		}
	}
	
	
	/* Create each gazetteer list, add key and stringvalue to each entry */
	private static  void eclAddGazList(gazList lists[], Map<String, ArrayList<String> > gazLists){
		int i=0;
		while(i<lists.length){
			ArrayList<String> gazList = gazLists.get(lists[i].gazlistkey);
			if(gazList==null){
				gazList = new ArrayList<String>();
				gazList.add(lists[i].entry);
				gazLists.put(lists[i].gazlistkey,gazList);
			}
			else{
				gazList.add(lists[i].entry);
				gazLists.put(lists[i].gazlistkey,gazList);
			}
			i++;
		}
	}
	 
	
	
	
	public static String run(Iterator<Gazetteer> gazrecs) throws Exception{
			
	
		//String listsDefLocation="/Users/nuoshi/Desktop/GateGaz/lists.def";
		String listsDefLocation="/home/nshi/gate_module/gazList/lists.def";
		Map<String, Lookup> lookupLists = new HashMap<String, Lookup>();
		Map<String, ArrayList<String> > gazLists = new HashMap<String, ArrayList<String>>();
		gate.Corpus corpus=null;
		//gate.creole.SerialAnalyserController pipeline=null;
		String result=null;  
		
		while(gazrecs.hasNext()){
			Gazetteer g=gazrecs.next();
			eclAddLookup(g.lists,lookupLists);
			eclAddGazList(g.lists, gazLists);
		
			System.out.println("Initialising GATE...");
			initCreole("/home/nshi/hpccdemo/gate_install");
			Gate.init();
		
			System.out.println("Creating gate copus\n");
			corpus=createCorpusbyDoc(g.input);
		
			System.out.println("Using gate to process the documents");
			result=runProcessingResources(listsDefLocation, corpus, lookupLists, gazLists);  
		
		}
		return result; 
	
	}
	
	
	
	
private  static void initCreole(String gateHome) throws GateException{
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
	
	
	private static gate.Corpus createCorpusbyDoc(String docXML)throws Exception{
		gate.Corpus corpus=null;
		if(docXML!=null && docXML!=""){
			Document indoc=Factory.newDocument(docXML);
			corpus=Factory.newCorpus("My Gate Corpus");
			corpus.add(indoc);
			System.out.println("Corpus created successfully\n");
		}else{
			throw new Exception("Corpus text is null\n");
		}
		return corpus;
	}
	
	
	private  static String runProcessingResources(String overrideGazLists,  gate.Corpus corpus,Map<String, Lookup> lookupLists, Map<String, ArrayList<String> > gazLists) throws Exception{
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
			
		//FeatureMap params = Factory.newFeatureMap();
		//params.put("listsURL", null); 
		
		
		pipeline.add((gate.LanguageAnalyser)Factory.createResource("gate.creole.gazetteer.DefaultGazetteer",params));
		System.out.println("Gazetteer created\n"); 
		pipeline.setCorpus(corpus);
		
		addToGazetteer(pipeline,lookupLists,gazLists);
		
		System.out.println("Running processing resources over corpus...\n");
		pipeline.execute();
		System.out.println("--Running PR done\n\n");
		
		return corpus.get(0).toXml();
		
	} 
	
	/* Edit gazetteer list programaticallly */
	private  static ProcessingResource getProcessingResourceByName(SerialAnalyserController pipeline, String name){
		ProcessingResource result = null;
		ProcessingResource temp;
		Iterator prIter = pipeline.getPRs().iterator();
		while(prIter.hasNext()){
			temp=(ProcessingResource) prIter.next();
			if(temp.getName().contains(name)){
				result=temp;
				break;
			}
		}
		System.out.println(result.getName());
		return result;
	}
	
	private  static void addToGazetteer(SerialAnalyserController pipeline, Map<String, Lookup> lookupLists, Map<String, ArrayList<String> > gazLists)throws Exception{
		ProcessingResource gazetteer = getProcessingResourceByName(pipeline,"Gazetteer");
		if((gazetteer==null)||(!(gazetteer instanceof AbstractGazetteer)))
			try {
				throw new GateException("The keyword gazetteer is missing!");
			} catch (GateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		Iterator lookupIter = lookupLists.values().iterator();
		if(lookupIter==null)
			throw new Exception("lookupList is null!");
		Lookup lookup;
		while(lookupIter.hasNext()){
			lookup=(Lookup) lookupIter.next();
			ArrayList<String> gazlist = gazLists.get(lookup.list);
			if(gazlist==null)
				throw new Exception("user Gazlist is null!");
			Iterator entryIter = gazlist.iterator();
			if(entryIter==null)
				throw new Exception("entryIter is null");
			String entry;
			while(entryIter.hasNext()){
				entry=(String) entryIter.next();
			  ((AbstractGazetteer)gazetteer).add(entry, lookup);
			}
		   
		}
	}
	
	
	
	public static void main(String[] args) throws Exception{
		
		String text="<tokenID>1</tokenID>dffa<string>nshi</string><tokenID>2</tokenID>dfsaf<string>raleigh</string>";
		
		String result=null;
		try {
		  	
			gazList[] mylists = new gazList[2];
			mylists[0]=new gazList("person.lst","person","girl","nshi");
			mylists[1]=new gazList("location.lst","location","city","raleigh");
				
			ArrayList<Gazetteer> a=new ArrayList<Gazetteer>();
			a.add(new Gazetteer(text,mylists));	
			result=Gazetteer.run(a.iterator());
			
			
		}
		catch(Exception e){
			e.printStackTrace();
		}

		System.out.println(result);
	}	
	
	
	
}
