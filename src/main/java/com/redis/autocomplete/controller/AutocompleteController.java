package com.redis.autocomplete.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.redis.autocomplete.entity.Word;
import com.redis.autocomplete.service.SingleWordAutocompleteService;
import com.redis.autocomplete.service.MultiwordAutocompleteService;

@Controller
@RequestMapping("/")
public class AutocompleteController {
		
	@Autowired
	private MultiwordAutocompleteService multiwordService;
	@Autowired
	private SingleWordAutocompleteService autocompletService;
	
	@GetMapping
	public String index() {
		return "index";
	}

	@GetMapping("/search")
	@ResponseBody
	public List<Word> search(@RequestParam("term") String pattern) {
		return multiwordService.complete(pattern);
	}

	@PostMapping("/generate")
	@ResponseBody
	public String generateWord(String word) throws Exception {
		multiwordService.generate(word);
		return "success";
	}

	@PostMapping(value = "/incr")
	@ResponseBody
	public String incrementScore(String id, String value) throws Exception {
		multiwordService.increment(Word.builder().id(id).value(value).build());
		return "success";
	}
	
	@GetMapping("/test")
	@ResponseBody
	public List<Word> test(@RequestParam("term") String pattern) {
		return multiwordService.complete(pattern);
	}
}
