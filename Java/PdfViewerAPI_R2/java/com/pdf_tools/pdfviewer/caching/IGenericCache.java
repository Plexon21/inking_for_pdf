package com.pdf_tools.pdfviewer.caching;

import java.util.Map;
import java.util.Set;
import java.util.List;

import com.pdf_tools.pdfviewer.Model.PdfViewerException;

public interface IGenericCache<Arguments, ObjectToCache, Item, Parametrization>
{

    public interface IObjectLoader<Arguments, ObjectToCache>
    {
        ObjectToCache loadObject(Arguments args) throws PdfViewerException;
    }

    public interface IItemAdder<Object, Arguments, Item>
    {
        /**
         * Add a single item to an argument of the cache object. E.g: Add a
         * single notation to an already existing list of annotations for a
         * page. In this scenario the with {@code obj.get(Argument page)} on
         * gets the list of annotations for {@code page} and can then add the
         * {@code item} to this list.
         * 
         * @param obj:
         *            Cache object - most of the times a Map
         * @param argument:
         *            Key for the map
         * @param item:
         *            Item to be added at the {@code key} location of the map
         */
        void add(Object obj, Arguments argument, Item item);
    }

    public interface IItemRemover<Object, Arguments, Item>
    {
        void remove(Object obj, Arguments argument, Item item);
    }

    public interface IArgumentsToLoadPredictor<Arguments>
    {
        List<Arguments> predictObjectsToLoad(Arguments arguments) throws PdfViewerException;
    }

    public interface IParametrizer<ObjectToCache, Parametrization>
    {
        void firstLoadParametrize(ObjectToCache loadedObject, Parametrization param);

        void changeParametrizationOfLoadedObject(ObjectToCache loadedObject, Parametrization oldParam, Parametrization newParam);
    }

    public interface IGuessGenerator<Arguments, ObjectToCache>
    {
        ObjectToCache generateGuess(Map<Arguments, ObjectToCache> realValues);

        void invalidateGuess();
    }

    public interface IItemLoadedToCacheListener<Arguments>
    {
        void onItemLoadedToCache(Arguments args);
    }

    public interface IItemUnloader<ObjectToCache>
    {
        void unloadItem(ObjectToCache objectToUnload);
    }

    void unloadObject(Arguments arguments);

    ObjectToCache get(Arguments arguments) throws PdfViewerException;

    ObjectToCache getGuess(Arguments arguments) throws PdfViewerException;

    void invalidateCache();

    void addItemToCache(Arguments args, Item item);

    void removeItemFromCache(Arguments args, Item item);

    Set<Arguments> getArgumentsLoaded();

    void changeParametrization(Parametrization param);

    // setting of "delegates"
    void setObjectLoader(IObjectLoader<Arguments, ObjectToCache> loader);

    void setItemAdder(IItemAdder<Object, Arguments, Item> adder);

    void setItemRemover(IItemRemover<Object, Arguments, Item> remover);

    void setArgumentsToLoadPredictor(IArgumentsToLoadPredictor<Arguments> predictor);

    void setParametrizer(IParametrizer<ObjectToCache, Parametrization> parametrizer);

    void setGuessGenerator(IGuessGenerator<Arguments, ObjectToCache> guessGenerator);

    void setItemUnloader(IItemUnloader<ObjectToCache> unloader);

    void addItemLoadedToCacheListener(IItemLoadedToCacheListener<Arguments> listener);

}
