package com.inkyi.util;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.BitSet;

/**
 * A Bloom filter implementation using MurmurHash.
 * @param <T>the type of filtered values.
 */
public class BloomFilter<T> implements Serializable {

	private final BitSet filter;

	// Filter size 过滤器大小
	private final int m;

	// Number of hash functions 由散列函数生成的数
	private final int k;

	/**
	 * Create a Bloom filter by setting the probability of false positive and
	 * the estimated number of elements being filtered. The size of the filter
	 * (in bits) and the number of hash functions will be calculated by
	 * formulas: m = (-n * ln p) / (ln 2)^2 and k = (m * ln 2) / n
	 * @param p the probability of false positive in a range (0, 1)
	 * @param capacity the estimated count of elements being filtered in a range (0,2^63 - 1)
	 */
	public BloomFilter(final double p, final long capacity) {
		if (p <= 0.0 || p >= 1.0) {
			throw new IllegalArgumentException("p must be in (0, 1)");
		}

		if (capacity <= 0) {
			throw new IllegalArgumentException("capacity must be in (0, 2^63 - 1)");
		}

		this.m = Math.max(1, (int) Math.round((-capacity * Math.log(p)) / Math.pow(Math.log(2), 2)));
		this.k = Math.max(1, (int) Math.round(m * Math.log(2) / capacity));
		this.filter = new BitSet(m);
	}

	/**
	 * Create a Bloom filter by directly setting the size of the filter (inbits) and the number of hash functions.
	 * @param m the size of the filter (in bits)
	 * @param k the number of hash functions
	 */
	public BloomFilter(final int m, final int k) {
		if (m <= 0) {
			throw new IllegalArgumentException("m must be positive");
		}

		if (k <= 0) {
			throw new IllegalArgumentException("k must be positive");
		}

		this.k = k;
		this.m = m;
		this.filter = new BitSet(m);
	}
	
	/**
	 * Add a value to the filter. The value is considered as a byte array
	 * value.toString().getBytes(Charset.forName("UTF-8"))
	 * @param value value
	 */
	public void add(final T value) {
		if (value == null) {
			throw new NullPointerException("value is null");
		}

		final byte[] data = getData(value);

		if (data.length == 0) {
			throw new IllegalArgumentException("value data is empty");
		}

		int h = 0;
		for (int i = 0; i < k; i++) {
			h = MurmurHash2.hash(data, h);
			final int idx = Math.abs(h % m);
			filter.set(idx);
		}
	}

	/**
	 * Test if a value is in filter. If the result if positive, there is a
	 * probability that the value is not in the filter. If the result is
	 * negative, this is for sure. The value is considered as a byte array
	 * value.toString().getBytes(Charset.forName("UTF-8"))
	 * 
	 * @param value value
	 */
	public boolean test(final T value) {
		if (value == null) {
			throw new NullPointerException("value is null");
		}

		final byte[] data = getData(value);
		if (data.length == 0) {
			throw new IllegalArgumentException("value data is empty");
		}
		int h = 0;
		for (int i = 0; i < k; i++) {
			h = MurmurHash2.hash(data, h);
			final int idx = Math.abs(h % m);
			if (!filter.get(idx))
				return false;
		}

		return true;
	}

	private byte[] getData(final T value) {
		return value.toString().getBytes(Charset.forName("UTF-8"));
	}
}
