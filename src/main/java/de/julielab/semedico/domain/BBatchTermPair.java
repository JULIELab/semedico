/**
 * BBatchTermPair.java
 *
 * Copyright (c) 2013, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 07.02.2013
 **/

/**
 * 
 */
package de.julielab.semedico.domain;

import java.io.Serializable;

/**
 * @author faessler
 * 
 */
public class BBatchTermPair implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3029545910480269427L;
	public String term1;
	public String term2;

	/**
	 * Copies the pair values rather then passing the reference since the
	 * original could be changed and we don't want the changes to write through.
	 * 
	 * @param pair
	 */
	public void setPair(BBatchTermPair pair) {
		this.term1 = pair.term1;
		this.term2 = pair.term2;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BBatchTermPair [term1=" + term1 + ", term2=" + term2 + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((term1 == null) ? 0 : term1.hashCode());
		result = prime * result + ((term2 == null) ? 0 : term2.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BBatchTermPair other = (BBatchTermPair) obj;
		if (term1 == null) {
			if (other.term1 != null)
				return false;
		} else if (!term1.equals(other.term1))
			return false;
		if (term2 == null) {
			if (other.term2 != null)
				return false;
		} else if (!term2.equals(other.term2))
			return false;
		return true;
	}

}
