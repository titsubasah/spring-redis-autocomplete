package com.redis.autocomplete;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;

public class SingleWordTest extends RedisAutocompleteApplicationTests {

	@Before
	public void before() throws IOException {
		redisConnectionFactory.getConnection().flushAll();

		long startTime = System.currentTimeMillis();
		Resource resource = resourceLoader.getResource("classpath:names.txt");
		BufferedReader in = new BufferedReader(new InputStreamReader(resource.getInputStream()));

		String word = null;
		while ((word = in.readLine()) != null) {
			if (StringUtils.isBlank(word))
				continue;
			autocompleteService.add(word);
		}

		long elapsed = System.currentTimeMillis() - startTime;
		Long totalCount = redisConnectionFactory.getConnection().dbSize(); // get saved key size
		logger.info("Add 20k words on redis. elapsed={}ms, totalCount={}", elapsed, totalCount);
	}

	@Test
	public void autocompleteTest() throws Exception {
		long elapsed = 0;
		for (int i = 0; i < myWords.size(); i++) {
			elapsed = elapsed + autocomplete(myWords.get(i));
		}
		logger.info("Test autocomplete 20k words average elapsed={}ms", ((float) elapsed / myWords.size()));
	}
}
