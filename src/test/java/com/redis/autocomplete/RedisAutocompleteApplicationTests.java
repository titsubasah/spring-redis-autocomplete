package com.redis.autocomplete;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import com.redis.autocomplete.entity.Word;
import com.redis.autocomplete.service.SingleWordAutocompleteService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisAutocompleteApplicationTests {
	protected static final Logger logger = LoggerFactory.getLogger(RedisAutocompleteApplicationTests.class);
	
	@Autowired
	protected RedisConnectionFactory redisConnectionFactory;

	@Autowired
	protected StringRedisTemplate stringRedisTemplate;

	@Autowired
	protected SingleWordAutocompleteService autocompleteService;

	@Autowired
	protected ResourceLoader resourceLoader;
	
	protected List<String> myWords = new ArrayList<>();

	protected long autocomplete(String word) {		
		long startTime = System.currentTimeMillis();
		List<Word> autocompleteDatas = autocompleteService.complete(word);
		String firstWord = "";
		if(!autocompleteDatas.isEmpty()) firstWord = autocompleteDatas.get(0).getValue();

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("prefix={}, elapsed={}ms, size={}, firstWord={}", word, elapsed, autocompleteDatas.size(), firstWord);		
		return elapsed;
	}
}

