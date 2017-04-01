package com.sikayetvar.textmining.api.util;

import java.util.Objects;

public class Pair<K, V> {
	private K left;
	private V right;

	public Pair(K left, V right) {
		this.left = left;
		this.right = right;
	}

	public K getLeft() {
		return left;
	}

	public void setLeft(K left) {
		this.left = left;
	}

	public V getRight() {
		return right;
	}

	public void setRight(V right) {
		this.right = right;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		if (obj instanceof Pair<?, ?>) {
			Pair<?, ?> other = (Pair<?, ?>) obj;
			if (Objects.equals(this.left, other.left) && Objects.equals(this.right, other.right))
				return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(left, right);
	}

	/**
	 * <p>
	 * Returns a String representation of this pair using the format {@code ($left,$right)}.
	 * </p>
	 * 
	 * @return a string describing this object, not null
	 */
	@Override
	public String toString() {
		return new StringBuilder().append('(').append(getLeft()).append(',').append(getRight()).append(')').toString();
	}

}
