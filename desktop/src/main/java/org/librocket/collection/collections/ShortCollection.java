package org.librocket.collection.collections;

import org.librocket.collection.iterators.ShortIterable;
import org.librocket.collection.lists.ShortIterator;

/* JADX INFO: renamed from: org.a.a.a.d */
/* JADX INFO: loaded from: game-lib.jar:org/a/a/a/d.class */
public interface ShortCollection extends LongCollection, ShortIterable {
    @Override // java.util.Collection, java.lang.Iterable, org.librocket.collection.iterators.ShortIterable
    /* JADX INFO: renamed from: a */
    ShortIterator iterator();
}
