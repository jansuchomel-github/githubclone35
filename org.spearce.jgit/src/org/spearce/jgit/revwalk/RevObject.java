/*
 * Copyright (C) 2008, Shawn O. Pearce <spearce@spearce.org>
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Git Development Community nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.spearce.jgit.revwalk;

import java.io.IOException;

import org.spearce.jgit.errors.IncorrectObjectTypeException;
import org.spearce.jgit.errors.MissingObjectException;
import org.spearce.jgit.lib.AnyObjectId;
import org.spearce.jgit.lib.ObjectId;

/** Base object type accessed during revision walking. */
public abstract class RevObject extends ObjectId {
	static final int PARSED = 1;

	int flags;

	RevObject(final AnyObjectId name) {
		super(name);
	}

	abstract void parse(RevWalk walk) throws MissingObjectException,
			IncorrectObjectTypeException, IOException;

	/**
	 * Get the name of this object.
	 * 
	 * @return unique hash of this object.
	 */
	public ObjectId getId() {
		return this;
	}

	@Override
	public boolean equals(final ObjectId o) {
		return this == o;
	}

	@Override
	public boolean equals(final Object o) {
		return this == o;
	}

	/**
	 * Test to see if the flag has been set on this object.
	 * 
	 * @param flag
	 *            the flag to test.
	 * @return true if the flag has been added to this object; false if not.
	 */
	public boolean has(final RevFlag flag) {
		return (flags & flag.mask) != 0;
	}

	/**
	 * Test to see if any flag in the set has been set on this object.
	 * 
	 * @param set
	 *            the flags to test.
	 * @return true if any flag in the set has been added to this object; false
	 *         if not.
	 */
	public boolean hasAny(final RevFlagSet set) {
		return (flags & set.mask) != 0;
	}

	/**
	 * Test to see if all flags in the set have been set on this object.
	 * 
	 * @param set
	 *            the flags to test.
	 * @return true if all flags of the set have been added to this object;
	 *         false if some or none have been added.
	 */
	public boolean hasAll(final RevFlagSet set) {
		return (flags & set.mask) == set.mask;
	}

	/**
	 * Add a flag to this object.
	 * <p>
	 * If the flag is already set on this object then the method has no effect.
	 * 
	 * @param flag
	 *            the flag to mark on this object, for later testing.
	 */
	public void add(final RevFlag flag) {
		flags |= flag.mask;
	}

	/**
	 * Add a set of flags to this object.
	 * 
	 * @param set
	 *            the set of flags to mark on this object, for later testing.
	 */
	public void add(final RevFlagSet set) {
		flags |= set.mask;
	}

	/**
	 * Remove a flag from this object.
	 * <p>
	 * If the flag is not set on this object then the method has no effect.
	 * 
	 * @param flag
	 *            the flag to remove from this object.
	 */
	public void remove(final RevFlag flag) {
		flags &= ~flag.mask;
	}

	/**
	 * Remove a set of flags from this object.
	 * 
	 * @param set
	 *            the flag to remove from this object.
	 */
	public void remove(final RevFlagSet set) {
		flags &= ~set.mask;
	}

	/** Release as much memory as possible from this object. */
	public void dispose() {
		// Nothing needs to be done for most objects.
	}
}