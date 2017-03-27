package com.pdf_tools.pdfviewer.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.*;

import com.pdf_tools.pdfviewer.Model.IPdfCommon.IOnCloseCompletedListener;
import com.pdf_tools.pdfviewer.Model.IPdfCommon.IOnOpenCompletedListener;
import com.pdf_tools.pdfviewer.converter.geom.Rectangle;

public class PdfSearcher implements IOnOpenCompletedListener, IOnCloseCompletedListener
{

    public PdfSearcher(PdfCanvas canvas, PdfViewerController controller)
    {
        this.controller = controller;
        this.canvas = canvas;
        wrap = true;
        matchCase = false;
        previous = false;
        useRegex = false;
        textFragmentIndexMap = new HashMap<Integer, Map<Integer, PdfTextFragment>>();
        searchableTextCache = new HashMap<Integer, String>();
        matchesCache = new HashMap<Integer, List<Match>>();
        controller.registerOnOpenCompleted(this);
    }

    public synchronized void configureSearcher(boolean matchCase, boolean wrap, boolean previous, boolean useRegex)
    {
        if (this.matchCase != matchCase || this.useRegex != useRegex)
        {
            lastSearchString = "";// treat next search as if it was a first
                                  // search for said string
        }
        this.matchCase = matchCase;
        this.wrap = wrap;
        this.previous = previous;
        this.useRegex = useRegex;
    }

    @Override
    public void onOpenCompleted(PdfViewerException e)
    {
        clearCache();
        lastSearchString = "";
    }

    @Override
    public void onCloseCompleted(PdfViewerException e)
    {
        clearCache();
        lastSearchString = "";
    }

    private void clearCache()
    {
        for (Map<Integer, PdfTextFragment> map : textFragmentIndexMap.values())
        {
            for (PdfTextFragment frag : map.values())
            {
                frag.finalize();
            }
        }
        textFragmentIndexMap.clear();
        searchableTextCache.clear();
    }

    public synchronized void search(String toSearch, int startPage, int startIndex) throws PdfViewerException
    {
        DebugLogger.log("Searching for \"" + toSearch + "\" on page " + startPage + " from index " + startIndex);

        if (toSearch == null || toSearch.isEmpty())
        {
            controller.fireOnSearchCompleted(0, 0, null);
            return;
        }

        boolean repeatedSearch = lastSearchString.compareTo(toSearch) == 0;
        lastSearchString = toSearch;

        if (repeatedSearch)
        {
            // offset the startindex to not get the same result again
            startIndex += previous ? -1 : 1;
        } else
        {
            if (!useRegex)
            {
                toSearch = toSearch.replaceAll("\\\\ ", " ");// make literal
                                                             // white space
                                                             // characters to
                                                             // whitespaces
                                                             // ("\ " to "
                                                             // ")
                toSearch = Pattern.quote(toSearch); // escape regex literals
                                                    // ("." to "\.")
            }
            toSearch = toSearch.replaceAll("\\\\ ", " ");// make literal white
                                                         // space characters
                                                         // to whitespaces
                                                         // ("\ " to " ")
            toSearch = toSearch.replaceAll("\\s+", "\\\\s+"); // make any amount
                                                              // of whitespaces
                                                              // to a regex that
                                                              // matches any
                                                              // amount of
                                                              // whitespaces
            int regexOptions = Pattern.DOTALL;
            if (!matchCase)
                regexOptions |= Pattern.CASE_INSENSITIVE;
            Pattern pattern = Pattern.compile(toSearch, regexOptions);
            matcher = pattern.matcher("");
            startIndex = previous ? Integer.MAX_VALUE : 0;
            matchesCache.clear();
        }

        boolean firstTime = true;// marks the first time we visit the startPage,
                                 // if we visit it a second time, we have not
                                 // found anything
        // iterate through pages that have to be loaded
        for (int currentPage = startPage;; currentPage += previous ? -1 : 1)
        {
            if (currentPage > canvas.getPageCount())
            {
                if (!wrap)
                {
                    // We have not found anything and reached the end
                    controller.fireOnSearchCompleted(0, 0, null);
                    return;
                }
                // We have reached the end of the document -> continue at
                // beginning
                currentPage = 1;
                startIndex = 0;
            } else if (currentPage <= 0)
            {
                if (!wrap)
                {
                    // We have not found anything backwards and reached the
                    // beginning
                    controller.fireOnSearchCompleted(0, 0, null);
                    return;
                }
                // We have reached the beginning of the document -> continue at
                // end
                currentPage = canvas.getPageCount();
                startIndex = Integer.MAX_VALUE;
            }

            // load textfragments
            StringBuilder searchableText = new StringBuilder("");
            for (int page = currentPage; page <= canvas.getPageCount() && page < currentPage + maxNumberOfPagesPreloaded; page++)
            {
                loadPage(page);
                searchableText.append(searchableTextCache.get(page));
            }

            if (!matchesCache.containsKey(currentPage))
            {
                // we need to search for matches on this page, as they are not
                // in cache yet
                matchesCache.put(currentPage, new ArrayList<Match>());
                matcher.reset(searchableText.toString());
                while (matcher.find() && matcher.start() < searchableTextCache.get(currentPage).length())
                {
                    // add all matches, that start within this page
                    matchesCache.get(currentPage).add(new Match(matcher));
                }
            }

            // find the next match we are interested in starting from startIndex
            Match nextMatch = null;
            for (Match match : matchesCache.get(currentPage))
            {
                if (!previous)
                {
                    if (match.startIndex >= startIndex && (nextMatch == null || match.startIndex < nextMatch.startIndex))
                        nextMatch = match;
                } else
                {
                    if (match.startIndex <= startIndex && (nextMatch == null || match.startIndex > nextMatch.startIndex))
                        nextMatch = match;
                }
            }

            if (nextMatch == null)
            {
                // no match on this page, go to next (or previous) page
                // special case if we have checked the entire document and
                // reached the startPage again and still not found anything:
                // give up.
                if (currentPage == startPage)
                {
                    if (!firstTime)
                    {
                        // if we are on startPage for the second time, we have
                        // found nothing and should stop looking
                        controller.fireOnSearchCompleted(0, 0, null);
                        return;
                    }
                    firstTime = false;
                }
                // the startIndex is not valid if we are not on the first page
                startIndex = previous ? Integer.MAX_VALUE : 0;
                continue;
            } else
            {
                // We found a match: we have to translate our match to
                // textfragments for highlighting

                // create the empty lists for the textRects, that will be
                // highlighted
                Map<Integer, List<Rectangle.Double>> textRects = new HashMap<Integer, List<Rectangle.Double>>();
                textRects.put(currentPage, new ArrayList<Rectangle.Double>());

                int fragmentIndex = 0;
                int page = currentPage;
                int previousPagesTextLength = 0; // the length of all text on
                                                 // previous pages (this is
                                                 // used to offset the index
                                                 // to be relative to page
                                                 // instead of currentPage)

                // iterate over textfragments and extract their textRects
                while (textFragmentIndexMap.containsKey(page))
                {
                    PdfTextFragment fragment = null;
                    // offset the fragmentIndex to be relative to page for
                    // getting correct fragment
                    fragment = textFragmentIndexMap.get(page).get(fragmentIndex - previousPagesTextLength);

                    int startSubIndex = 0;// index of start of match in relation
                                          // to firstTextFragment
                    int endSubIndex = fragment.getText().length();// index of
                                                                  // end of
                                                                  // match in
                                                                  // relation
                                                                  // to
                                                                  // firstTextFragment

                    if (nextMatch.startIndex > fragmentIndex + fragment.getText().length())
                    {
                        // we have not found the region with the match yet
                        // carry on with next fragment
                    } else if (nextMatch.endIndex < fragmentIndex)
                    {
                        // We have passed the fragment already and can end
                        // searching
                        break;
                    } else
                    {
                        // we are looking at a fragment that is intersecting
                        // with the match
                        if (fragmentIndex < nextMatch.startIndex)
                        {
                            // the match starts in the middle of this
                            // textfragment
                            startSubIndex = nextMatch.startIndex - fragmentIndex;
                        }
                        if (fragmentIndex + fragment.getText().length() > nextMatch.endIndex)
                        {
                            // the match ends before the end of the fragment
                            endSubIndex = nextMatch.endIndex - fragmentIndex;
                        }
                        Rectangle.Double rect = null;
                        if (startSubIndex == 0 && endSubIndex == fragment.getText().length())
                            rect = fragment.getRectOnUnrotatedPage();
                        else
                            rect = fragment.getRectOnUnrotatedPage(startSubIndex, endSubIndex);
                        textRects.get(page).add(rect);
                    }

                    // increment index and if necessary page
                    fragmentIndex += fragment.getText().length() + 1;
                    if (!textFragmentIndexMap.get(page).containsKey(fragmentIndex - previousPagesTextLength))
                    {
                        if (page == canvas.getPageCount())
                        {
                            // we reached the end of the document: end here
                            break;
                        } else
                        {
                            // There are no more fragments on page: go to the
                            // next page
                            previousPagesTextLength += searchableTextCache.get(page).length();
                            page++;
                            textRects.put(page, new ArrayList<Rectangle.Double>());
                        }
                    }
                } // end of iteration over textFragments
                DebugLogger.log("returning result");
                controller.fireOnSearchCompleted(currentPage, nextMatch.startIndex, textRects);
                DebugLogger.log("Ending search");
                return;
            }
        } // end pages loop
          // unreachable
    }

    private void loadPage(int currentPage) throws PdfViewerException
    {
        if (textFragmentIndexMap.containsKey(currentPage))
            return;

        // currentPage is not loaded yet, we do this now:
        List<PdfTextFragment> fragments = canvas.GetTextWithinPageRange(currentPage, currentPage, controller.getZoom());
        Map<Integer, PdfTextFragment> pageDict = new HashMap<Integer, PdfTextFragment>();
        StringBuilder pageText = new StringBuilder("");
        int appendIndex = 0;
        for (PdfTextFragment frag : fragments)
        {
            pageText.append(frag.getText()).append(" ");
            pageDict.put(appendIndex, frag);
            appendIndex += frag.getText().length() + 1;
        }
        textFragmentIndexMap.put(currentPage, pageDict);
        searchableTextCache.put(currentPage, pageText.toString());
    }

    private class Match
    {
        public Match(Matcher matcher)
        {
            startIndex = matcher.start();
            endIndex = matcher.end();
        }

        public int startIndex;
        public int endIndex;
    }

    private static int maxNumberOfPagesPreloaded = 3;

    private Map<Integer, Map<Integer, PdfTextFragment>> textFragmentIndexMap;
    private Map<Integer, String> searchableTextCache;
    private Map<Integer, List<Match>> matchesCache;
    private Matcher matcher;

    private boolean matchCase, wrap, previous, useRegex;
    private String lastSearchString = "";

    private PdfViewerController controller;
    private PdfCanvas canvas;
}
