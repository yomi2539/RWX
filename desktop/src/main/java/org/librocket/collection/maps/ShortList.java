package org.librocket.collection.maps;

import org.librocket.collection.collections.ShortCollection;
import org.librocket.collection.lists.ShortIterator;
import org.librocket.collection.sets.ShortListIterator;

/* JADX INFO: renamed from: org.a.a.d.d */
/* JADX INFO: loaded from: game-lib.jar:org/a/a/d/d.class */
public interface ShortList extends ShortCollection, LongList {
    @Override // org.librocket.collection.collections.ShortCollection, java.util.Collection, java.lang.Iterable, org.librocket.collection.iterators.ShortIterable
    /* JADX INFO: renamed from: a */
    ShortIterator iterator();

    @Override // java.util.List
    /* JADX INFO: renamed from: b, reason: merged with bridge method [inline-methods] */
    ShortListIterator listIterator();

    @Override // java.util.List
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    ShortListIterator listIterator(int i);

    @Override // java.util.List
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    ShortList subList(int i, int i2);

    @Override // java.util.List
    @Deprecated
    /* JADX INFO: renamed from: b, reason: merged with bridge method [inline-methods] */
    Short remove(int i);
}
