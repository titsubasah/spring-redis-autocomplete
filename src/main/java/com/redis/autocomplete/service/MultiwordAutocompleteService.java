package com.redis.autocomplete.service;

import java.util.List;

import com.redis.autocomplete.entity.Word;

public interface MultiwordAutocompleteService {
	
	List<Word> complete (String term);
	
	void generate (String multiword);
	
	boolean increment (Word word);
}
