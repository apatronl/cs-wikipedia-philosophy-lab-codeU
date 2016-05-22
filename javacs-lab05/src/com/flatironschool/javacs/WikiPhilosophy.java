package com.flatironschool.javacs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import org.jsoup.select.Elements;

public class WikiPhilosophy {

	final static WikiFetcher wf = new WikiFetcher();

	/**
	 * Tests a conjecture about Wikipedia and Philosophy.
	 *
	 * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
	 *
	 * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
	 *
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		String baseURL = "https://en.wikipedia.org";
		List<String> visited = new ArrayList<String>();
		boolean foundPhilosophy = false;

		while (!foundPhilosophy) {
			visited.add(url);
			Elements paragraphs = wf.fetchWikipedia(url);
			Element firstPara = paragraphs.get(0);
			Iterable<Node> iter = new WikiNodeIterable(firstPara);

			String validURL = getURL(iter, url);
			String completeURL = baseURL + validURL;
			if (validURL != null) {
				// We got to Philosophy!
				if (completeURL.equals("https://en.wikipedia.org/wiki/Philosophy")) {
					visited.add(completeURL);
					foundPhilosophy = true;
				} else {
					url = completeURL;
				}
			} else {
				// Not really found philosophy, just ended up in a loop
				// with no valid URLs
				foundPhilosophy = true;
				System.out.println("ERROR");
			}
		}

		// Output visited URLs
		for (String visitedURL: visited) System.out.println(visitedURL);
	}

	// Get a "valid" URL, returns null if not found
	private static String getURL(Iterable<Node> iter, String currentURL) {
		for (Node node : iter) {
			if (node instanceof Element) {
				Element curr = (Element)node;
				if (isValid(curr, currentURL)) {
					return node.attr("href");
				}
			}
		}
		return null;
	}

	private static boolean isValid(Element e, String currentURL) {
		String url = "https://en.wikipedia.org" + e.attr("href");

		Element temp = e;

		// Check italics
		while (temp != null) {
			if (temp.tagName().equals("i") || temp.tagName().equals("em")) {
				return false;
			}
			temp = temp.parent();
		}

		// Some attributes return empty strings (not valid)
		if (e.attr("href").equals("")) {
			return false;
		}

		// Skip citations or pronunciation help URLs
		if (e.attr("href").startsWith("/wiki/Help:") || e.attr("href").startsWith("#cite_note")){
            return false;
        }

		// Link to the current page
		if (url.equals(currentURL)) {
			return false;
		}

		return true;
	}
}
