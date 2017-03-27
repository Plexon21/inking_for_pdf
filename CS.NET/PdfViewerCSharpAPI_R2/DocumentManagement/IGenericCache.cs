// -----------------------------------------------------------------------
// <copyright file="IGeneriCache.cs" company="">
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

    public delegate APdfRequest<Arguments, ObjectToCache> LoadObjectAsync<Arguments, ObjectToCache>(Arguments arguments);
    public delegate bool ObjectEquator<ObjectToCache>(ObjectToCache obj1, ObjectToCache obj2);

    public delegate IList<Arguments> PredictAdditionalObjectsToLoad<Arguments>(Arguments arguments);
    
    public delegate void FirstLoadParametrizer<ObjectToCache, Parametrization>(ObjectToCache loadedObject, Parametrization parametrization);
    public delegate void ParametrizationChanger<ObjectToCache, Parametrization>(ObjectToCache loadedObject, Parametrization oldParam, Parametrization newParam);

    

    public interface IGuessGenerator<Arguments, ObjectToCache>
    {
        ObjectToCache GenerateGuess(IDictionary<Arguments, ObjectToCache> realValues);
        void InvalidateGuess();
    }

    /// <summary>
    /// TODO: Update summary.
    /// </summary>
    public interface IGenericCache<Arguments, ObjectToCache, Parametrization>
    {
        ObjectToCache Get(Arguments arguments);
        ObjectToCache GetGuess(Arguments arguments);

        void InvalidateCache();

        void ChangeParametrization(Parametrization param);

        LoadObjectAsync<Arguments, ObjectToCache> LoadObjectAsyncDelegate { set; }
        ObjectEquator<ObjectToCache> ObjectEquatorDelegate { set; }

        PredictAdditionalObjectsToLoad<Arguments> PredictionAlgorithm { set; }
    
        FirstLoadParametrizer<ObjectToCache, Parametrization> FirstLoadParametrizer{ set; }
        ParametrizationChanger<ObjectToCache, Parametrization> ParametrizationChanger { set; }
    
        IGuessGenerator<Arguments, ObjectToCache> GuessGenerator { set; }

        APdfRequest<Arguments, ObjectToCache> GetAsync(Arguments arguments);

        void CancelPendingRequest(APdfRequest<Arguments, ObjectToCache> request);

        bool ExactlyLoaded(Arguments arguments);

        event Action<Arguments> ItemLoadedToCache;
    }
}
