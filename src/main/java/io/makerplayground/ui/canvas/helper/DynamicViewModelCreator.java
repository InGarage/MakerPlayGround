package io.makerplayground.ui.canvas.helper;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.HashMap;
import java.util.function.Predicate;

/**
 * A helper class for creating instances of ViewModel based on the number of Model in an ObservableList provided.
 *
 * @param <T> type of Model of the ViewModel to be created by the instance of this class
 * @param <U> type of ViewModel to be created
 */
public class DynamicViewModelCreator<T, U> {
    private final ObservableList<T> model;
    private final ViewModelFactory<T, U> viewModelFactory;
    private final Predicate<T> filter;

    private final ObservableMap<T, U> controllerMap;

    public DynamicViewModelCreator(ObservableList<T> model, ViewModelFactory<T, U> viewModelFactory) {
        this(model, viewModelFactory, null);
    }

    public DynamicViewModelCreator(ObservableList<T> model, ViewModelFactory<T, U> viewModelFactory, Predicate<T> filter) {
        this.model = model;
        this.viewModelFactory = viewModelFactory;
        this.filter = filter;

        this.controllerMap = FXCollections.observableMap(new HashMap<>());

        for (T t : model) {
            if (this.filter == null || this.filter.test(t))
                addController(t);
        }

        model.addListener(new ListChangeListener<T>() {
            @Override
            public void onChanged(Change<? extends T> c) {
                while (c.next()) {
                    if (c.wasPermutated()) {
                        throw new UnsupportedOperationException();
                    } else if (c.wasUpdated()) {
                        for (T updatedItem : c.getList().subList(c.getFrom(), c.getTo())) {
                            if (filter != null) {
                                if (filter.test(updatedItem) && !controllerMap.containsKey(updatedItem))
                                    addController(updatedItem);
                                else if (!filter.test(updatedItem) && controllerMap.containsKey(updatedItem))
                                    removeController(updatedItem);
                            }
                        }
                    } else {
                        for (T removedItem : c.getRemoved()) {
                            if (filter == null || filter.test(removedItem))
                                removeController(removedItem);
                        }
                        for (T addedItem : c.getAddedSubList()) {
                            if (filter == null || filter.test(addedItem))
                                addController(addedItem);
                        }
                    }
                }
            }
        });
    }

    ObservableMap<T, U> getControllerMap() {
        return controllerMap;
    }

    private void addController(T model) {
        U node = viewModelFactory.newInstance(model);
        controllerMap.put(model, node);
    }

    private void removeController(T model) {
        U node = controllerMap.remove(model);
        if (node == null)
            throw new IllegalStateException();
    }

}