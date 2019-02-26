package com.redis.autocomplete;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import com.redis.autocomplete.service.SingleWordAutocompleteService;
import com.redis.autocomplete.service.MultiwordAutocompleteService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MultiwordTest {
	protected static final Logger logger = LoggerFactory.getLogger(MultiwordTest.class);

	@Autowired
	protected RedisConnectionFactory redisConnectionFactory;

	@Autowired
	protected StringRedisTemplate stringRedisTemplate;

	@Autowired
	protected MultiwordAutocompleteService multiwordAutocompleteService;
	
	@Autowired
	protected ResourceLoader resourceLoader;
	
	@Before
	public void init() {
		redisConnectionFactory.getConnection().flushAll();
	}
	
	@Test
	public void generateWordTest() throws Exception {		
		long startTime = System.currentTimeMillis();
		Resource resource = resourceLoader.getResource("classpath:multiword.txt"); // Load 20k word file
		BufferedReader in = new BufferedReader(new InputStreamReader(resource.getInputStream()));

		String word = null;
		while ((word = in.readLine()) != null) {
			if (StringUtils.isBlank(word))
				continue;
			multiwordAutocompleteService.generate(word);
		}
	}
	
	@Test 
	public void generateManual() throws Exception {
		multiwordAutocompleteService.generate("Apple Iphone");
	}

}
