/*
 * Copyright (C) 2006, Robin Rosenberg <robin.rosenberg@dewire.com>
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

package org.spearce.jgit.pgm;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.spearce.jgit.awtui.AwtAuthenticator;
import org.spearce.jgit.errors.TransportException;
import org.spearce.jgit.lib.Repository;

/** Command line entry point. */
public class Main {
	private static boolean showStackTrace;

	/**
	 * Execute the command line.
	 * 
	 * @param argv
	 *            arguments.
	 */
	public static void main(final String[] argv) {
		try {
			AwtAuthenticator.install();
			configureHttpProxy();
			execute(argv);
		} catch (Die err) {
			System.err.println("fatal: " + err.getMessage());
			if (showStackTrace)
				err.printStackTrace();
			System.exit(128);
		} catch (Exception err) {
			if (!showStackTrace && err.getCause() != null
					&& err instanceof TransportException)
				System.err.println("fatal: " + err.getCause().getMessage());

			if (err.getClass().getName().startsWith("org.spearce.jgit.errors.")) {
				System.err.println("fatal: " + err.getMessage());
				if (showStackTrace)
					err.printStackTrace();
				System.exit(128);
			}
			err.printStackTrace();
			System.exit(1);
		}
	}

	private static void execute(final String[] argv) throws Exception {
		int argi = 0;
		String gitdir = ".git";

		for (; argi < argv.length; argi++) {
			final String arg = argv[argi];
			if (arg.startsWith("--git-dir="))
				gitdir = arg.substring("--git-dir=".length());
			else if (arg.equals("--show-stack-trace"))
				showStackTrace = true;
			else if (arg.startsWith("--"))
				usage();
			else
				break;
		}

		if (argi == argv.length)
			usage();
		final TextBuiltin cmd = createCommand(argv[argi++]);
		cmd.db = new Repository(new File(gitdir));
		try {
			cmd.execute(subarray(argv, argi));
		} finally {
			if (cmd.out != null)
				cmd.out.flush();
		}
	}

	private static String[] subarray(final String[] argv, final int i) {
		return Arrays.asList(argv).subList(i, argv.length).toArray(
				new String[0]);
	}

	private static TextBuiltin createCommand(final String name) {
		final StringBuilder s = new StringBuilder();
		s.append(mypackage());
		s.append('.');
		boolean upnext = true;
		for (int i = 0; i < name.length(); i++) {
			final char c = name.charAt(i);
			if (c == '-') {
				upnext = true;
				continue;
			}
			if (upnext)
				s.append(Character.toUpperCase(c));
			else
				s.append(c);
			upnext = false;
		}
		try {
			return (TextBuiltin) Class.forName(s.toString()).newInstance();
		} catch (Exception e) {
			System.err.println("error: " + name + " is not a jgit command.");
			System.exit(1);
			return null;
		}
	}

	private static String mypackage() {
		final String p = Main.class.getName();
		final int dot = p.lastIndexOf('.');
		return p.substring(0, dot);
	}

	private static void usage() {
		System.err.println("jgit [--git-dir=path] cmd ...");
		System.exit(1);
	}

	private static void configureHttpProxy() {
		final String s = System.getenv("http_proxy");
		if (s == null || s.equals(""))
			return;

		final URL u;
		try {
			u = new URL(s);
		} catch (MalformedURLException e) {
			throw new Die("Invalid http_proxy: " + s + ": " + e.getMessage());
		}
		if (!"http".equals(u.getProtocol()))
			throw new Die("Invalid http_proxy: " + s + ": Only http supported.");

		final String proxyHost = u.getHost();
		final int proxyPort = u.getPort();

		System.setProperty("http.proxyHost", proxyHost);
		if (proxyPort > 0)
			System.setProperty("http.proxyPort", String.valueOf(proxyPort));

		final String userpass = u.getUserInfo();
		if (userpass != null && userpass.contains(":")) {
			final int c = userpass.indexOf(':');
			final String user = userpass.substring(0, c);
			final String pass = userpass.substring(c + 1);
			AwtAuthenticator.add(new AwtAuthenticator.CachedAuthentication(
					proxyHost, proxyPort, user, pass));
		}
	}
}