package com.redis.autocomplete.service;

import java.util.List;

import com.redis.autocomplete.entity.Word;

public interface SingleWordAutocompleteService {

	String DEFAULT_DELIMITER = "§";

	List<Word> complete(final String word);
	
	List<Word> complete2(final String word);

	List<Word> complete(final String word, final double min, final double max, final int offset);

	void add(final String word);

	double incr(final String word);

	void clear(final String key);
}
