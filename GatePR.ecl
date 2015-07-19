import java;

/* Author: Nuo Shi */

// Declaration for the ECL tokeniser function
String getTokens(String corpus):= 
			IMPORT (java,'Tokeniser.run:(Ljava/lang/String;)Ljava/lang/String;');
	
//Raw text
text:=DATASET([{'Helen lives in Dallas. She left for Raleigh last Tuesday to visit her friends Alice'+
				'Helen lives in Dallas. She left for Raleigh last Tuesday to visit her friends Alice'+
				'Helen lives in Dallas. She left for Raleigh last Tuesday to visit her friends Alice'

				}],{String line});

//Call ECL tokeniser function
doc_tokens:=PROJECT(text,transform({String doc},SELF.doc:=getTokens(left.line);));
//output(doc_tokens);

featurerec:=RECORD
	string name;
	string value;
end;

annotrec:=RECORD
	string id;
	string start_offset;
	string end_offset;
	string annotype;
	dataset(featurerec) features;
end;

annotrec p:=transform
	self.id:=xmltext('@Id');
	self.start_offset:=xmltext('@StartNode');
	self.end_offset:=xmltext('@EndNode');
	self.annotype:=xmltext('@Type');
	self.features:=xmlproject ('Feature',transform(featurerec,
													self.name:=xmltext('Name'),
													self.value:=xmltext('Value')));
end;

dstokens:=parse(doc_tokens, doc, p, XML('GateDocument/AnnotationSet/Annotation'));
//output(dstokens);


tokenrec:=record
	string tokenID;
	string content;
	string annotype;
	string start_offset;
	string end_offset;
	string tokenLength;
	string tokenOrth;
	string kind;	
end;


tokens:=project(dstokens(annotype='Token'), transform(tokenrec, 
								  self.tokenID:=left.id,
								  self.annotype:=left.annotype,
								  self.start_offset:=left.start_offset,
								  self.end_offset:=left.end_offset,
								  self.tokenLength:=left.features(name='length')[1].value,
								  self.tokenOrth:=left.features(name='orth')[1].value,
								  self.kind:=left.features(name='kind')[1].value,
								  self.content:=left.features(name='string')[1].value));
								  
output(tokens);


//Gazetteer

//Lookup lists
lookupDataset := DATASET([{doc_tokens[1].doc,
	[{'female.lst','person_first','female','Helen'},
	{'female.lst','person_first','female','Alice'},
	{'location.lst','location','capital','Raleigh'},
	{'time.lst','time','date','Tuesday'}]}],$.Types.gaztteerRec);

//Call ECL gazetteer funtion to getlookups	
String getLookups(LINKCOUNTED dataset($.Types.gaztteerRec) d):=import(java,'Gazetteer.run:(<LGazetteer;)Ljava/lang/String;');
doc_lookup:=getLookups(lookupDataset);
//output(doc_lookup);

//Parse output
doc_lookup_ds:=dataset([{doc_lookup}],{String doc_l});
dslookups:=parse(doc_lookup_ds, doc_l, p, XML('GateDocument/AnnotationSet/Annotation'));
//output(dslookups);

lookuprec:=record
	string tokenID;
	//string content;
	string annotype;
	string start_offset;
	string end_offset;
	//string kind;
	string majortype;
	string minortype;	
end;

lookups:=project(dslookups(annotype='Lookup'), transform(lookuprec, 
								  self.tokenID:=left.id,
								  self.start_offset:=left.start_offset,
								  self.end_offset:=left.end_offset,
								  self.annotype:=left.annotype,
								  self.majortype:=left.features(name='majorType')[1].value,
								  self.minortype:=left.features(name='minorType')[1].value
								  //self.kind:=left.features(name='')[1].value
								 ));
								  
//output(lookups);



lookups_content:=join(tokens(annotype='Token'), lookups(annotype='Lookup'), 
						left.start_offset=right.start_offset and left.end_offset=right.end_offset);
						
lookupannot :=record
	string content:=lookups_content.content;
	string majortype:=lookups_content.majortype;
	string minortype:=lookups_content.minortype;
end;


output(lookups_content,lookupannot);


//Jape Transducer declaration
String getAnnotations(String tokenInput, String rule_template, String input_annotation, String rule_priority, String output_category):=
	IMPORT(java,
	'Transducer.run:(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;');

//execute person jape_rule
String person_rule:='/home/nshi/gate_module/japerule/femaleCategory.jape';
doc_person:= getAnnotations( doc_lookup, person_rule, 'Lookup','20','girlCategory');

//Parse output
doc_person_ds:=dataset([{doc_person}],{String doc_p});
dspersons:=parse(doc_person_ds, doc_p, p, XML('GateDocument/AnnotationSet/Annotation'));
personrec:=record
	string annotype;
	string start_offset;
	string end_offset;
	string rulename;
end;
persons:=project(dspersons(annotype!='Token' and annotype!='SpaceToken' and annotype!='Lookup'), transform(personrec, 
								  //self.tokenID:=left.id,
								  self.start_offset:=left.start_offset,
								  self.end_offset:=left.end_offset,
								  self.annotype:=left.annotype,
								  self.rulename:=left.features(name='rule')[1].value	 
								 ));
//output(persons);

person_content:=join(tokens(annotype='Token'), persons, 
						left.start_offset=right.start_offset and left.end_offset=right.end_offset);
personanno :=record
	string annotype:='female';
	string content:=person_content.content;
	string rulename:=person_content.rulename;
	//string minortype:=lookups_content.minortype;
end;

output(person_content,personanno);

