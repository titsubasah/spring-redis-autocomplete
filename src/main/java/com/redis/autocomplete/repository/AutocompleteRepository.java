package com.redis.autocomplete.repository;

public interface AutocompleteRepository {
	String create(String word, String identifier);

	double incr(String word, String identifier);

	String getKey(String firstLetter);
}
