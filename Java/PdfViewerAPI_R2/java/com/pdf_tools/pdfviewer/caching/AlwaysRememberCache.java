package com.pdf_tools.pdfviewer.caching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pdf_tools.pdfviewer.Model.PdfViewerException;

public class AlwaysRememberCache<Arguments, ObjectToCache, Item, Parametrization>
        implements IGenericCache<Arguments, ObjectToCache, Item, Parametrization>
{

    private Map<Arguments, ObjectToCache> dict;
    private Set<Arguments> guessSet;
    private Set<Arguments> argumentsLoaded;
    private Parametrization param;
    private int numPagesLoadedOnLastGuessGeneration = 0;

    public AlwaysRememberCache()
    {
        dict = new HashMap<Arguments, ObjectToCache>();
        guessSet = new HashSet<Arguments>();
        argumentsLoaded = new HashSet<Arguments>();
        itemLoadedToCacheListenerList = new ArrayList<IItemLoadedToCacheListener<Arguments>>();
    }

    @Override
    public ObjectToCache get(Arguments arguments) throws PdfViewerException
    {

        if (objectLoader == null)
            throw new IllegalStateException("objectLoader of AlwaysRememberCache has not been set");
        List<Arguments> argses;
        if (predictor != null)
        {
            argses = predictor.predictObjectsToLoad(arguments);
        } else
        {
            argses = Collections.singletonList(arguments);
        }

        for (Arguments args : argses)
        {
            if (!dict.containsKey(args) || guessSet.contains(args))
            {
                ObjectToCache result = objectLoader.loadObject(args);
                argumentsLoaded.add(args);
                if (parametrizer != null)
                    parametrizer.firstLoadParametrize(result, param);
                guessSet.remove(args);
                dict.put(args, result);
                for (IItemLoadedToCacheListener<Arguments> listener : itemLoadedToCacheListenerList)
                    listener.onItemLoadedToCache(args);
            }
        }
        return dict.get(arguments);
    }

    @Override
    public ObjectToCache getGuess(Arguments arguments) throws PdfViewerException
    {
        if (objectLoader == null)
            throw new IllegalStateException("objectLoader of AlwaysRememberCache has not been set");
        if (dict.size() <= 0 || guessGenerator == null)
            return get(arguments); // We have to get a real value if the cache
                                   // is empty

        if (!dict.containsKey(arguments))
        {
            // if we have loaded a lot more pages since we last generated a new
            // guess, we should invalidate that old guess and generate a new one
            if (numPagesLoadedOnLastGuessGeneration < (dict.size() - guessSet.size()) / 4)
            {
                guessGenerator.invalidateGuess();
                numPagesLoadedOnLastGuessGeneration = dict.size() - guessSet.size();
            }

            dict.put(arguments, guessGenerator.generateGuess(dict));
            guessSet.add(arguments);
        }
        return dict.get(arguments);
    }

    @Override
    public void invalidateCache()
    {
        if (this.itemUnloader != null)
        {
            for (ObjectToCache obj : dict.values())
                itemUnloader.unloadItem(obj);
        }
        dict.clear();
        guessSet.clear();
        if (guessGenerator != null)
            guessGenerator.invalidateGuess();
        numPagesLoadedOnLastGuessGeneration = 0;
    }

    @Override
    public void changeParametrization(Parametrization newParam)
    {
        if (guessGenerator != null)
            guessGenerator.invalidateGuess();
        for (ObjectToCache element : dict.values())
            parametrizer.changeParametrizationOfLoadedObject(element, param, newParam);
        this.param = newParam;
    }

    @Override
    public void setObjectLoader(com.pdf_tools.pdfviewer.caching.IGenericCache.IObjectLoader<Arguments, ObjectToCache> loader)
    {
        this.objectLoader = loader;
    }

    IObjectLoader<Arguments, ObjectToCache> objectLoader = null;

    @Override
    public void setItemAdder(IGenericCache.IItemAdder<Object, Arguments, Item> adder)
    {
        this.itemAdder = adder;
    }

    @Override
    public void addItemToCache(Arguments arguments, Item item)
    {
        this.itemAdder.add(this.dict, arguments, item);
    }

    IItemAdder<Object, Arguments, Item> itemAdder = null;

    @Override
    public void setItemRemover(IGenericCache.IItemRemover<Object, Arguments, Item> remover)
    {
        this.itemRemover = remover;
    }

    @Override
    public void removeItemFromCache(Arguments args, Item item)
    {
        this.itemRemover.remove(this.dict, args, item);
    }

    IItemRemover<Object, Arguments, Item> itemRemover = null;

    @Override
    public void setArgumentsToLoadPredictor(com.pdf_tools.pdfviewer.caching.IGenericCache.IArgumentsToLoadPredictor<Arguments> predictor)
    {
        this.predictor = predictor;
    }

    IArgumentsToLoadPredictor<Arguments> predictor;

    @Override
    public void setParametrizer(com.pdf_tools.pdfviewer.caching.IGenericCache.IParametrizer<ObjectToCache, Parametrization> parametrizer)
    {
        this.parametrizer = parametrizer;
    }

    IParametrizer<ObjectToCache, Parametrization> parametrizer = null;

    @Override
    public void setGuessGenerator(com.pdf_tools.pdfviewer.caching.IGenericCache.IGuessGenerator<Arguments, ObjectToCache> guessGenerator)
    {
        this.guessGenerator = guessGenerator;
    }

    IGuessGenerator<Arguments, ObjectToCache> guessGenerator = null;

    @Override
    public void addItemLoadedToCacheListener(com.pdf_tools.pdfviewer.caching.IGenericCache.IItemLoadedToCacheListener<Arguments> listener)
    {
        itemLoadedToCacheListenerList.add(listener);
    }

    List<IItemLoadedToCacheListener<Arguments>> itemLoadedToCacheListenerList;

    @Override
    public void setItemUnloader(com.pdf_tools.pdfviewer.caching.IGenericCache.IItemUnloader<ObjectToCache> unloader)
    {
        this.itemUnloader = unloader;
    }

    IItemUnloader<ObjectToCache> itemUnloader = null;

    @Override
    public void unloadObject(Arguments arguments)
    {
        this.dict.remove(arguments);
    }

    @Override
    public Set<Arguments> getArgumentsLoaded()
    {
        return argumentsLoaded;
    }

}
