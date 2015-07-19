EXPORT Types := MODULE

	EXPORT t_TokenID :=UNSIGNED8;
	EXPORT t_LookupID := UNSIGNED8;
	EXPORT t_Offset := UNSIGNED8;
	EXPORT t_Type := STRING;
	EXPORT t_Value:= STRING;
	EXPORT t_Line:= STRING;
	EXPORT t_String:=STRING;
	EXPORT t_Kind:=STRING;
	EXPORT t_Orth:=STRING;
	EXPORT t_Length:=UNSIGNED8;
	
	EXPORT RAW :=RECORD
		t_Line txt;
	END;	
	
	EXPORT t_text:=RECORD
		t_String line;
	END;
	
	EXPORT t_Token := RECORD
		t_TokenID TokenID:=(t_TokenID)xmlunicode('tokenID');
		//t_Offset beginOffset:=(t_Offset)xmlunicode('beginOffset');
		//t_Offset endOffset:=(t_Offset)xmlunicode('endOffset');
		//t_Type majorType:=(t_Type)xmlunicode('majorType');
		//t_Type minorType:=(t_Type)xmlunicode('minorType');
		t_Kind kind:=(t_Kind)xmlunicode('kind');
		t_Length TokenLen:=(t_Length)xmlunicode('length');
		t_Orth orth:=(t_Orth)xmlunicode('orth');
		t_String value:=(t_String)xmlunicode('string');
	END;

	EXPORT t_Lookup := RECORD
		//t_LookupID lookupID:=(t_lookupID)xmlunicode('lookupID');
		t_Offset beginOffset:=(t_Offset)xmlunicode('beginOffset');
		t_Offset endOffset:=(t_Offset)xmlunicode('endOffset');
		t_Type majorType:=(t_Type)xmlunicode('majorType');
		t_Type minorType:=(t_Type)xmlunicode('minorType');
		t_value value:=(t_Value)xmlunicode('value');
	END;
	
	EXPORT gazList := RECORD
		t_String gazListKey;
		t_String majorType;
		t_String minorType;
		t_String entry;
	END;
	
	EXPORT gaztteerRec:=RECORD
		t_String input;
		LINKCOUNTED dataset(gazList) lists;
	END;
	
	EXPORT t_japerule:=RECORD
		t_String template;
		t_String input_Annotation;
		t_String rule_priority;
		t_String rule_name;
	END;
	
	EXPORT t_Annotation:=RECORD
		t_Offset beginOffset:=(t_Offset)xmlunicode('beginOffset');
		t_Offset endOffset:=(t_Offset)xmlunicode('endOffset');
		t_Type annoType:=(t_Type)xmlunicode('type');
		t_String japerule:=(t_String)xmlunicode('rule');
		t_String value:=(t_String)xmlunicode('value');
	END;
	
		
END;
