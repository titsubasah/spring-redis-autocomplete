package com.redis.autocomplete.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.redis.autocomplete.repository.AutocompleteRepository;

@Component
public class AutocompleteRepositoryImpl implements AutocompleteRepository {
	protected static final String DELIMITER = ":";
	protected static final String PREFIX = "autocomplete" + DELIMITER;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Override
	public String create(final String word, final String identifier) {
		Assert.hasLength(word, "Word cannot be empty or null");
		String trimedWord = word.trim();
		String firstLetter = getPrefix(trimedWord);

		String generatedKey = generateKey(firstLetter, trimedWord.length());
		if (!hasKey(generatedKey, trimedWord, identifier)) {
			stringRedisTemplate.opsForZSet().add(generatedKey, trimedWord + identifier, 1);
			System.out.println(firstLetter);
			stringRedisTemplate.opsForZSet().add(generatedKey, firstLetter, 0);
			for (int index = 1; index < trimedWord.length(); index++) {
				System.out.println("index " + index + "value: " + trimedWord.substring(0, index));
				stringRedisTemplate.opsForZSet().add(generatedKey, trimedWord.substring(0, index), 0);
			}
		}
		return generatedKey;
	}

	@Override
	public String getKey(String word) {
		Assert.hasLength(word, "Word cannot be empty or null");
		String firstLetter = getPrefix(word);
		return generateKeyWithoutLength(firstLetter);
	}

	@Override
	public double incr(final String word, final String identifier) {
		String trimedWord = word.trim();
		String firstLetter = getPrefix(trimedWord);

		String generatedKey = generateKey(firstLetter, trimedWord.length());
		if (!hasKey(generatedKey, trimedWord, identifier))
			return 0;
		return stringRedisTemplate.opsForZSet().incrementScore(generatedKey, trimedWord + identifier, 1);
	}

	private String generateKey(final String firstLetter, int length) {
		return generateKeyWithoutLength(firstLetter) + length;
	}

	private String generateKeyWithoutLength(final String firstLetter) {
		return PREFIX + firstLetter + DELIMITER;
	}

	private boolean hasKey(final String key, final String word, final String identifier) {
		Double exist = stringRedisTemplate.opsForZSet().score(key, word.trim() + identifier);
		return exist != null;
	}

	private String getPrefix(final String word) {
		return word.substring(0, 1);
	}
}
