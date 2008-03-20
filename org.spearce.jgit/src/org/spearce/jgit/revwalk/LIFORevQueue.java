/*
 *  Copyright (C) 2008  Shawn Pearce <spearce@spearce.org>
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License, version 2, as published by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 */
package org.spearce.jgit.revwalk;

/** A queue of commits in LIFO order. */
public class LIFORevQueue extends BlockRevQueue {
	private Block head;

	public void add(final RevCommit c) {
		Block b = head;
		if (b == null || !b.canUnpop()) {
			b = free.newBlock();
			b.resetToEnd();
			b.next = head;
			head = b;
		}
		b.unpop(c);
	}

	public RevCommit pop() {
		final Block b = head;
		if (b == null)
			return null;

		final RevCommit c = b.pop();
		if (b.isEmpty()) {
			head = b.next;
			free.freeBlock(b);
		}
		return c;
	}

	public void clear() {
		head = null;
		free.clear();
	}

	public String toString() {
		final StringBuffer s = new StringBuffer();
		for (Block q = head; q != null; q = q.next) {
			for (int i = q.headIndex; i < q.tailIndex; i++) {
				s.append(q.commits[i]);
				s.append(' ');
				s.append(q.commits[i].commitTime);
				s.append('\n');
			}
		}
		return s.toString();
	}
}
