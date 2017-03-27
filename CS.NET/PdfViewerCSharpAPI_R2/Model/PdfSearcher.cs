// -----------------------------------------------------------------------
// <copyright file="Searcher.cs" company="">
// TODO: Update copyright text.
// </copyright>
// -----------------------------------------------------------------------

namespace PdfTools.PdfViewerCSharpAPI.Model
{
    using PdfTools.PdfViewerCSharpAPI.Utilities;
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using System.Text.RegularExpressions;

    /// <summary>
    /// TODO: Update summary.
    /// </summary>
    public class PdfSearcher
    {
        public PdfSearcher(IPdfCanvas canvas){
            this.canvas = canvas;
            matchCase = false;
            wrap = true;
            previous = false;
            useRegex = false;
            textFragmentIndexDict = new Dictionary<int,Dictionary<int,PdfTextFragment>>();
            searchableTextCache = new Dictionary<int, string>();
            matchesCache = new Dictionary<int, IList<Match>>();
        }

        public bool MatchCase
        {
            set
            {
                if (this.matchCase != value)
                {
                    lastSearchString = "";//treat next search as if it was a first search for said string
                    this.matchCase = value;
                }
            }
            get
            {
                return this.matchCase;
            }
        }
        public bool Wrap
        {
            set
            {
                this.wrap = value;
            }
            get
            {
                return this.wrap;
            }
        }
        public bool Previous
        {
            set
            {
                this.previous = value;
            }
            get
            {
                return this.previous;
            }
        }
        public bool UseRegex
        {
            set
            {
                if(this.useRegex != value){
                    lastSearchString = "";//treat next search as if it was a first search for said string
                    this.useRegex = value;
                }
            }
            get
            {
                return this.useRegex;
            }
        }

       public void OnNewDocumentOpened()
        {
            //clearcache
            textFragmentIndexDict.Clear();
            searchableTextCache.Clear();
            lastSearchString = "";
        }

        public void Search(string toSearch, int startPage, int startIndex)
        {
            if (toSearch == null || toSearch.Length == 0)
            {
                SearchCompleted(null);
                return;
            }
            //Console.WriteLine(String.Format("Searching \"{0}\" on page {1} index {2}", toSearch, startPage, startIndex));
            bool repeatedSearch = String.Compare(lastSearchString, toSearch) == 0;

            lastSearchString = toSearch;
            if (repeatedSearch)
            {
                //offset the startindex to not get the same result again
                startIndex += previous ? -1 : 1;
            }   
            else
            {
                if (!useRegex)
                {
                    toSearch = Regex.Replace(toSearch, "\\\\ ", " ");// make literal white space characters to whitespaces ("\ " to  " ")
                    toSearch = Regex.Escape(toSearch);  //escape regex literals ("." to "\.")
                }
                toSearch = Regex.Replace(toSearch, "\\\\ ", " ");// make literal white space characters to whitespaces ("\ " to  " ")
                toSearch = Regex.Replace(toSearch, "\\s+", "\\s+");//make any positive number of whitespaces match any other positive number of whitespaces (YES, this line DOES do something)
                RegexOptions regexOptions = RegexOptions.Singleline;
                if (!matchCase)
                    regexOptions = regexOptions | RegexOptions.IgnoreCase;
                regex = new Regex(toSearch, regexOptions);
                startIndex = previous ? int.MaxValue : 0; //TODO later i will maybe want to start searching from specific location, instead from start of page

                matchesCache.Clear();
            }

            bool firstTime = true;//marks the first time we visit the startPage, if we visit it a second time, we have not found anything
            //iterate through pages that have to be loaded
            for (int currentPage = startPage; ; currentPage += previous ? -1 : 1)
            {
                if (currentPage > canvas.PageCount)
                {
                    if (!wrap)
                    {
                        //We have not found anything and reached the end
                        SearchCompleted(null);
                        return;
                    }
                    //We have reached the end of the document -> continue at beginning
                    currentPage = 1;
                    startIndex = 0;
                }
                else if (currentPage <= 0)
                {
                    if (!wrap)
                    {
                        //We have not found anything backwards and reached the beginning
                        SearchCompleted(null);
                        return;
                    }
                    //We have reached the beginning of the document -> continue at end
                    currentPage = canvas.PageCount;
                    startIndex = int.MaxValue;
                }

                //load textfragments
                StringBuilder searchableText = new StringBuilder("");
                for(int page = currentPage; page <= canvas.PageCount && page < currentPage + maxNumberOfPagesPreloaded; page++)
                {
                    try
                    {
                        loadPage(page);
                    }
                    catch (PdfNoFileOpenedException)
                    {
                        //the file is not open anymore
                        SearchCompleted(null);
                        return;
                    }
                    searchableText.Append(searchableTextCache[page]);
                }

                if (!matchesCache.ContainsKey(currentPage))
                {
                    // we need to search for matches on this page, as they are not in cache yet
                    MatchCollection matches = regex.Matches(searchableText.ToString());
                    matchesCache.Add(currentPage, new List<Match>());
                    foreach (Match match in matches)
                    {
                        if (match.Index < searchableTextCache[currentPage].Length)
                            matchesCache[currentPage].Add(match);
                        else
                            break;
                    }
                }

                //find the next match we are interested in starting from startIndex
                Match nextMatch = null;
                foreach (Match match in matchesCache[currentPage])
                {
                    if (!previous)
                    {
                        if (match.Index >= startIndex && (nextMatch == null || match.Index < nextMatch.Index))
                            nextMatch = match;
                    }
                    else
                    {
                        if (match.Index <= startIndex && (nextMatch == null || match.Index > nextMatch.Index))
                            nextMatch = match;
                    }
                }

                if (nextMatch == null)
                {
                    //no match on this page, go to next (or previous) page

                    //special case if we have checked the entire document and reached the startPage again and still not found anything: give up.
                    if (currentPage == startPage)
                    {
                        if (!firstTime)
                        {
                            //if we are on startPage for the second time, we have found nothing and should stop looking
                            SearchCompleted(null);
                            return;
                        }
                        firstTime = false;
                    }
                    //the startIndex is not valid if we are not on the first page
                    startIndex = previous ? int.MaxValue : 0;
                    continue;
                }
                else
                {
                    //We found a match: we have to translate our match to textfragments for highlighting

                    //create the empty lists for the textRects, that will be highlighted
                    Dictionary<int, IList<PdfSourceRect>> textRects = new Dictionary<int, IList<PdfSourceRect>>();
                    textRects.Add(currentPage, new List<PdfSourceRect>());

                    int fragmentIndex = 0;
                    int page = currentPage;
                    int previousPagesTextLength = 0; //the length of all text on previous pages (this is used to offset the index to be relative to page instead of currentPage)

                    //iterate over textfragments and extract their textRects
                    while (textFragmentIndexDict.ContainsKey(page))
                    {
                        PdfTextFragment fragment = null;
                        //offset the fragmentIndex to be relative to page for getting correct fragment
                        fragment = textFragmentIndexDict[page][fragmentIndex - previousPagesTextLength];

                        int startSubIndex = 0;//index of start of match in relation to firstTextFragment
                        int endSubIndex = fragment.Text.Length;//index of end of match in relation to firstTextFragment

                        if (nextMatch.Index > fragmentIndex + fragment.Text.Length)
                        {
                            //we have not found the region with the match yet
                            //carry on with next fragment
                        }
                        else if (nextMatch.Index + nextMatch.Length < fragmentIndex)
                        {
                            //We have passed the fragment already and can end searching
                            break;
                        }
                        else
                        {
                            //we are looking at a fragment that is intersecting with the match
                            if (fragmentIndex < nextMatch.Index)
                            {
                                //the match starts in the middle of this textfragment
                                startSubIndex = nextMatch.Index - fragmentIndex;
                            }
                            if (fragmentIndex + fragment.Text.Length > nextMatch.Index + nextMatch.Length)
                            {
                                //the match ends before the end of the fragment
                                endSubIndex = nextMatch.Index + nextMatch.Length - fragmentIndex;
                            }
                            PdfSourceRect rect = null;
                            if (startSubIndex == 0 && endSubIndex == fragment.Text.Length)
                                rect = fragment.RectOnUnrotatedPage;
                            else
                                //rect = canvas.DocumentManager.RequestTextFragmentSubRect(fragment, startSubIndex, endSubIndex - startSubIndex).Wait();
                                rect = fragment.GetRectOnUnrotatedPage(startSubIndex, endSubIndex);
                            textRects[page].Add(rect);
                        }

                        //increment index and if necessary page
                        fragmentIndex += fragment.Text.Length + 1;
                        if (!textFragmentIndexDict[page].ContainsKey(fragmentIndex - previousPagesTextLength))
                        {
                            if (page == canvas.PageCount)
                            {
                                //we reached the end of the document: end here
                                break;
                            }
                            else
                            {
                                //There are no more fragments on page: go to the next page
                                previousPagesTextLength += searchableTextCache[page].Length;
                                page++;
                                textRects.Add(page, new List<PdfSourceRect>());
                            }
                        }
                    }//end of iteration over textFragments
                    SearchCompleted(new SearchResult(currentPage, textRects, nextMatch.Index, nextMatch.Value));
                    //Console.WriteLine(String.Format("Found match on page {0} with index {1}", currentPage, nextMatch.Index));
                    return;
                }
                /*
                if (!previous)
                    startIndex = 0;
                else
                    startIndex = int.MaxValue;*/
            }//end for all pages
        }

        private void loadPage(int currentPage)
        {
            if (textFragmentIndexDict.ContainsKey(currentPage))
                return;

            //currentPage is not loaded yet, we do this now:
            IList<PdfTextFragment> fragments = canvas.GetTextWithinPageRange(currentPage, currentPage);
            Dictionary<int, PdfTextFragment> pageDict = new Dictionary<int, PdfTextFragment>();
            StringBuilder pageText = new StringBuilder("");
            int appendIndex = 0;
            foreach (PdfTextFragment frag in fragments)
            {
                pageText.Append(frag.Text).Append(" ");
                pageDict.Add(appendIndex, frag);
                appendIndex += frag.Text.Length + 1;
            }
            textFragmentIndexDict.Add(currentPage, pageDict);
            searchableTextCache.Add(currentPage, pageText.ToString());
        }

        public class SearchResult
        {

            public SearchResult(int pageNo, Dictionary<int, IList<PdfSourceRect>> textRects, int index, string text)
            {
                this.pageNo = pageNo;
                this.textRects = textRects;
                this.index = index;
                this.text = text;
            }

            public int Index
            {
                get
                {
                    return index;
                }
            }
            public int PageNo
            {
                get
                {
                    return pageNo;
                }
            }
            /// <summary>
            /// Dictionary, which maps pages to a list of all contained textrects that are part of the match
            /// </summary>
            public Dictionary<int, IList<PdfSourceRect>> TextRects
            {
                get
                {
                    return textRects;
                }
            }

            public string Text
            {
                get
                {
                    return text;
                }
            }

            private int pageNo;
            private Dictionary<int, IList<PdfSourceRect>> textRects;
            private int index;
            private string text;
        };

        public event Action<SearchResult> SearchCompleted;


        private Dictionary<int, Dictionary<int, PdfTextFragment>> textFragmentIndexDict;// a dictionary in which each entry is maps pagenumbers o a dictionary, which maps indexes to textfragments within the dictionary
        private Dictionary<int, string> searchableTextCache;
        private Dictionary<int, IList<Match>> matchesCache;


        private Regex regex;
        private IPdfCanvas canvas;
        private bool matchCase, wrap, previous, useRegex;
        private string lastSearchString = "";

        private const int maxNumberOfPagesPreloaded = 2; //the amount of pages that is preloaded before searching a match (should be at least 2)
    }
}
