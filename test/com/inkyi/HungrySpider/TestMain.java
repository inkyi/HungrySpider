package com.inkyi.HungrySpider;

public class TestMain {
	
	public static void main(String[] args) {
		Spider.create().thread(10).executes(1000).urlLength(6).bloomFilter(10000000, 4).run();
	}
}
