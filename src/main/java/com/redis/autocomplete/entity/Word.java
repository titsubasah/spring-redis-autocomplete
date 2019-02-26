package com.redis.autocomplete.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Word implements Comparable<Word> {

	private String id;
	private String value;
	private int score;

	@Override
	public int compareTo(Word o) {
		return ((Integer) o.getScore()).compareTo(this.score);
	}

}
