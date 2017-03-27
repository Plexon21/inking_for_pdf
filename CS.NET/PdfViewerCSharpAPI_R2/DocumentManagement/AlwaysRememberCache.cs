// -----------------------------------------------------------------------
// <copyright file="DummyCache.cs" company="">
// TODO: Update copyright text.
// </copyright>
// -----------------------------------------------------------------------

namespace PdfTools.PdfViewerCSharpAPI.DocumentManagement
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;
    using Requests;
    using PdfTools.PdfViewerCSharpAPI.Utilities;



    /// <summary>
    /// TODO: Update summary.
    /// </summary>
    public class AlwaysRememberCache<Arguments, ObjectToCache, Parametrization> : IGenericCache<Arguments, ObjectToCache, Parametrization>
    {

        private IDictionary<Arguments, ObjectToCache> dict;
        private ISet<Arguments> guessSet;
        private ISet<Arguments> exactlyLoadedSet;
        private Dictionary<Arguments, APdfRequest<Arguments, ObjectToCache>> pendingRequests; //arguments that are loading asynchronously and have not returned yet
        private Parametrization param;
        private int numPagesLoadedOnLastGuessGeneration = 0;
        private Object dictLock = new Object();

        public AlwaysRememberCache()
        {
            dict = new Dictionary<Arguments, ObjectToCache>();
            guessSet = new HashSet<Arguments>();
            exactlyLoadedSet = new HashSet<Arguments>();
            pendingRequests = new Dictionary<Arguments, APdfRequest<Arguments, ObjectToCache>>();
        }
        public AlwaysRememberCache(LoadObjectAsync<Arguments, ObjectToCache> loadObjectDelegate)
            : this()
        {
            this.loadObjectAsyncDelegate = loadObjectDelegate;
        }

        public ObjectToCache GetGuess(Arguments arguments)
        {
            lock (dictLock)
            {
                if (dict.Count <= 0)
                    return Get(arguments);//we cant guess anything when the dict is empty

                if (!dict.ContainsKey(arguments))
                {
                    //if we have loaded a lot more pages since we last generated a new guess, we should invalidate that old guess and generate a new one
                    if (numPagesLoadedOnLastGuessGeneration < (dict.Count - guessSet.Count) / 4)
                    {
                        guessGenerator.InvalidateGuess();
                        numPagesLoadedOnLastGuessGeneration = dict.Count - guessSet.Count;
                    }
                    dict.Add(arguments, guessGenerator.GenerateGuess(dict));
                    guessSet.Add(arguments);
                }
                return dict[arguments];
            }
        }


        public ObjectToCache Get(Arguments arguments)
        {
            bool mustLoad;
            lock (dictLock)
            {
                mustLoad = !dict.ContainsKey(arguments) || guessSet.Contains(arguments);
            }

            if (mustLoad)
            {
                //synchronous load

                if (pendingRequests.ContainsKey(arguments))
                {
                    //this request ist already pending, wait for its completion
                    //ObjectToCache res = pendingRequests[arguments].Wait().output;
                    //LoadCompleted(arguments, res);
                    AsyncLoadCompleted(pendingRequests[arguments].Wait(), null);

                }
                else
                {
                    //generate request to load
                    ObjectToCache result = loadObjectAsyncDelegate(arguments).Wait().output;
                    LoadCompleted(arguments, result);
                }
            }

            //asynchronous load (load other elements to cache that are 'close' to the requested one)
            if (predictionAlgorithm != null)
            {
                IList<Arguments> argses = predictionAlgorithm(arguments);
                foreach (Arguments args in argses)
                {
                    loadAsync(args);
                }
            }

            lock (dictLock)
            {
                return dict[arguments];
            }

        }

        private APdfRequest<Arguments, ObjectToCache> loadAsync(Arguments args)
        {
            lock (dictLock)
            {
                if (!pendingRequests.ContainsKey(args) && (!dict.ContainsKey(args) || guessSet.Contains(args)))
                {
                    APdfRequest<Arguments, ObjectToCache> request = loadObjectAsyncDelegate(args);
                    pendingRequests.Add(args, request);
                    request.Completed += AsyncLoadCompleted;
                    return request;
                }
                return pendingRequests.ContainsKey(args) ? pendingRequests[args] : null;
            }
        }

        public void AsyncLoadCompleted(APdfRequest<Arguments, ObjectToCache>.InOutTuple result, PdfViewerCSharpAPI.Utilities.PdfViewerException ex)
        {
            if (ex != null)
                return;

            //only add the loaded values if there is a pending request for it
            if (pendingRequests.ContainsKey(result.arguments))
            {
                //do not load if we have already loaded the same key/value combination
                if(objectEquatorDelegate == null || !dict.ContainsKey(result.arguments) || !objectEquatorDelegate(dict[result.arguments], result.output))
                {
                    LoadCompleted(result.arguments, result.output);
                }
            }
        }
        private void LoadCompleted(Arguments args, ObjectToCache result)
        {
            lock (dictLock)
            {
                if (onFirstLoadParametrizer != null)
                    onFirstLoadParametrizer(result, param);
                guessSet.Remove(args);
                pendingRequests.Remove(args);
                exactlyLoadedSet.Add(args);
                if (dict.ContainsKey(args))
                    dict[args] = result;
                else
                    dict.Add(args, result);
                if (ItemLoadedToCache != null)
                    ItemLoadedToCache(args);
            }
        }

        public void ChangeParametrization(Parametrization newParam)
        {
            if (guessGenerator != null)
                guessGenerator.InvalidateGuess();

            lock (dictLock)
            {
                foreach (ObjectToCache cachedObject in dict.Values)
                {
                    parametrizationChanger(cachedObject, param, newParam);
                }
            }
            this.param = newParam;
        }

        public void InvalidateCache()
        {
            lock (dictLock)
            {
                dict.Clear();
                guessSet.Clear();
                exactlyLoadedSet.Clear();
                pendingRequests.Clear();
                if (guessGenerator != null)
                    guessGenerator.InvalidateGuess();
                numPagesLoadedOnLastGuessGeneration = 0;
            }
        }

        /// <summary>
        /// loads object to cache if necessary asynchronously
        /// </summary>
        /// <param name="arguments">arguments</param>
        /// <returns>null if the element is already loaded, otherwise request</returns>
        public APdfRequest<Arguments, ObjectToCache> GetAsync(Arguments arguments)
        {
            return loadAsync(arguments);
        }

        public void CancelPendingRequest(APdfRequest<Arguments, ObjectToCache> request)
        {
            pendingRequests.Remove(request.InputArguments);
        }

        public bool ExactlyLoaded(Arguments arguments)
        {
            return exactlyLoadedSet.Contains(arguments);
        }

        public event Action<Arguments> ItemLoadedToCache;

        public override string ToString()
        {
            //Collection<IFormattable> list = dict.Keys as ICollection<IFormattable>;
            lock (dict)
            {
                return Utilities.Logger.ListToString<Arguments>(dict.Keys);
            }
        }

        #region set delegates and members
        LoadObjectAsync<Arguments, ObjectToCache> loadObjectAsyncDelegate = null;
        public LoadObjectAsync<Arguments, ObjectToCache> LoadObjectAsyncDelegate
        {
            set
            {
                loadObjectAsyncDelegate = value;
            }
        }
        ObjectEquator<ObjectToCache> objectEquatorDelegate = null;
        public ObjectEquator<ObjectToCache> ObjectEquatorDelegate
        {
            set
            {
                objectEquatorDelegate = value;
            }
        }
        PredictAdditionalObjectsToLoad<Arguments> predictionAlgorithm = null;
        public PredictAdditionalObjectsToLoad<Arguments> PredictionAlgorithm
        {
            set
            {
                predictionAlgorithm = value;
            }
        }
        FirstLoadParametrizer<ObjectToCache, Parametrization> onFirstLoadParametrizer = null;
        public FirstLoadParametrizer<ObjectToCache, Parametrization> FirstLoadParametrizer
        {
            set
            {
                onFirstLoadParametrizer = value;
            }
        }

        ParametrizationChanger<ObjectToCache, Parametrization> parametrizationChanger = null;
        public ParametrizationChanger<ObjectToCache, Parametrization> ParametrizationChanger
        {
            set
            {
                parametrizationChanger = value;
            }
        }

        IGuessGenerator<Arguments, ObjectToCache> guessGenerator = null;
        public IGuessGenerator<Arguments, ObjectToCache> GuessGenerator
        {
            set
            {
                guessGenerator = value;
            }
        }
        #endregion
    }
}
