package com.redis.autocomplete.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands.Aggregate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.redis.autocomplete.entity.Word;
import com.redis.autocomplete.service.MultiwordAutocompleteService;

@Service
public class MultiwordAutocompleteServiceImpl implements MultiwordAutocompleteService {

	protected static final String DELIMITER = ":";
	protected static final String PREFIX = "autocomplete" + DELIMITER;
	protected static final String WORDS = PREFIX + "words:";
	protected static final String TITLE = PREFIX + "reference:title";
	protected static final String INTERSECT = PREFIX + "intersect:";
	protected static final int DEFAULT_SIZE = 10;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Override
	public List<Word> complete(String term) {
		if (term.trim().isEmpty()) {
			return Collections.emptyList();
		}

		return find(term.toLowerCase());
	}

	@Override
	public void generate(String multiword) {
		String uuid = UUID.randomUUID().toString();
		multiword = multiword.trim();
		storeUuid(uuid, multiword);
		String[] words = multiword.split(" ");
		for (int i = 0; i < words.length; i++) {
			generateKey(uuid, words[i]);
		}
	}

	@Override
	public boolean increment(Word word) {
		for (int i = 1; i < word.getValue().length() + 1; i++) {
			String key = word.getValue().substring(0, i);
			stringRedisTemplate.opsForZSet().incrementScore(WORDS + key, word.getId(), 1);
		}
		return true;
	}
	
	private List<Word> find(String term) {
		if (term.split(" ").length > 1) {
			return findMultiWord(term);
		} else {
			return findSingleWord(term);
		}
	}

	private List<Word> findSingleWord(String term) {
		return findByKey(WORDS + term);
	}

	private List<Word> findMultiWord(String term) {
		final String[] splits = term.split(" ");
		final List<String> words = new ArrayList<>();
		final StringBuilder destKey = new StringBuilder(INTERSECT);
		for (int i = 0; i < splits.length; i++) {
			words.add(WORDS + splits[i]);
			destKey.append(splits[i]);
		}
		final String firstElement = words.remove(0);
		stringRedisTemplate.opsForZSet().intersectAndStore(firstElement, words, destKey.toString(), Aggregate.MAX);
		stringRedisTemplate.expire(destKey.toString(), 5, TimeUnit.MINUTES);
		return findByKey(destKey.toString());
	}

	private List<Word> findByKey(String key) {
		final List<Word> words = new ArrayList<>();
		Set<TypedTuple<String>> result = stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, 0,
				DEFAULT_SIZE);
		result.forEach(member -> {
			words.add(Word.builder().id(member.getValue()).value(getTitle(member.getValue()))
					.score(member.getScore().intValue()).build());
		});
		return words;
	}

	private void generateKey(String uuid, String word) {
		Assert.hasLength(word, "Word cannot be empty or null");
		for (int i = 1; i < word.length() + 1; i++) {
			String key = word.substring(0, i).toLowerCase();
			stringRedisTemplate.opsForZSet().add(WORDS + key, uuid, 0);
			System.out.println("ZADD "+WORDS+key + " " + 0 + " "+uuid);
		}
	}

	private void storeUuid(String uuid, String multiword) {
		Assert.hasLength(multiword, "Multiword not empty");
		stringRedisTemplate.opsForHash().put(TITLE, uuid, multiword);
		System.out.println("HSET "+ TITLE + " "+uuid + " \""+multiword + "\"" );
	}

	private String getTitle(String uuid) {
		return (String) stringRedisTemplate.opsForHash().get(TITLE, uuid);
	}

}
