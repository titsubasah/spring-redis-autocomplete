package com.redis.autocomplete.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.redis.autocomplete.entity.Word;
import com.redis.autocomplete.repository.AutocompleteRepository;
import com.redis.autocomplete.service.SingleWordAutocompleteService;

@Service
public class SingleWordAutocompleteServiceImpl implements SingleWordAutocompleteService {
	private final double min = 0;
	private final double max = 5;
	private final int offset = 10;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Autowired
	private AutocompleteRepository repository;

	@Override
	public List<Word> complete(String word) {
		return complete(word, min, max, offset);
	}

	@Override
	public List<Word> complete2(String word) {
		Assert.hasLength(word, "Word cannot be empty or null");
		String trimedWord = word.trim();
		int trimedWordLength = trimedWord.length();
		String key = repository.getKey(trimedWord);
		List<Word> autocompletes = new ArrayList<>();
		for (int i = trimedWordLength; i < offset; i++) {
			if (autocompletes.size() >= offset)
				break;

			Cursor<TypedTuple<String>> cursors = stringRedisTemplate.opsForZSet().scan(key + i,
					new ScanOptions.ScanOptionsBuilder().match(trimedWord + "*").build());
			cursors.forEachRemaining(item -> {
				int minLength = Math.min(item.getValue().length(), trimedWordLength);
				if (item.getValue().startsWith(trimedWord.substring(0, minLength))
						&& item.getValue().endsWith(DEFAULT_DELIMITER)) {
					autocompletes.add(Word.builder().value(item.getValue().replace(DEFAULT_DELIMITER, ""))
							.score(item.getScore().intValue()).build());
				}
				if (autocompletes.size() >= offset) {
					return;
				}
			});
		}
		Collections.sort(autocompletes);
		return autocompletes;
	}

	@Override
	public List<Word> complete(String word, double min, double max, int offset) {
		Assert.hasLength(word, "Word cannot be empty or null");

		String trimedWord = word.trim();
		int trimedWordLength = trimedWord.length();
			
		String key = repository.getKey(trimedWord);
		List<Word> autocompletes = new ArrayList<>();
		for (int i = trimedWordLength; i < offset; i++) {
			if (autocompletes.size() == offset)
				break;
			int count = stringRedisTemplate.opsForZSet().count(key + i, 1, Double.POSITIVE_INFINITY).intValue();
			Set<TypedTuple<String>> rangeResultsWithScore = stringRedisTemplate.opsForZSet()
					.reverseRangeByScoreWithScores(key + i, 1, Double.POSITIVE_INFINITY, 0, count);
			if (rangeResultsWithScore.isEmpty())
				continue;

			for (TypedTuple<String> typedTuple : rangeResultsWithScore) {
				if (autocompletes.size() == offset)
					break;
				String value = typedTuple.getValue();
				int minLength = Math.min(value.length(), trimedWordLength);
				if (!value.endsWith(DEFAULT_DELIMITER) || !value.startsWith(trimedWord.substring(0, minLength)))
					continue;
				autocompletes.add(Word.builder().value(value.replace(DEFAULT_DELIMITER, ""))
						.score(typedTuple.getScore().intValue()).build());
			}
		}
		Collections.sort(autocompletes);
		return autocompletes;
	}

	@Override
	public void add(String word) {
		repository.create(word, DEFAULT_DELIMITER);
	}

	@Override
	public double incr(String word) {
		return repository.incr(word, DEFAULT_DELIMITER);
	}

	@Override
	public void clear(String key) {
		stringRedisTemplate.delete(key);
	}
}
