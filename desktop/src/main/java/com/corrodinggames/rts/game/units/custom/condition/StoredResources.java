package com.corrodinggames.rts.game.units.custom.condition;

import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.custom.condition.resources.Resource;
import com.corrodinggames.rts.game.units.custom.price.UnitPrice;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.ui.TextRenderQueue;
import com.corrodinggames.rts.gameFramework.utility.FastArrayList;

import java.io.IOException;
import java.util.Collections;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.custom.e.f */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/custom/e/f.class */
public final class StoredResources {
    public static final StoredResources a = new StoredResources().a();
    public final FastArrayList<StoredResourceEntry> b = new FastArrayList();
    boolean c;

    public StoredResources a() {
        this.c = true;
        return this;
    }

    public void b() {
        this.b.clear();
    }

    public boolean c() {
        if (this.b.size == 0) {
            return true;
        }
        int i = this.b.size;
        Object[] objArrA = this.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            if (((StoredResourceEntry) objArrA[i2]).b != 0.0d) {
                return false;
            }
        }
        return true;
    }

    public double a(Resource resource) {
        int i = this.b.size;
        Object[] objArrA = this.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            if (storedResourceEntry.a == resource) {
                return storedResourceEntry.b;
            }
        }
        return 0.0d;
    }

    public double b(Resource resource) {
        int i = this.b.size;
        Object[] objArrA = this.b.a();
        int i2 = 0;
        for (int i3 = 0; i3 < i; i3++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i3];
            if (storedResourceEntry.a == resource) {
                i2 = (int) (((double) i2) + storedResourceEntry.b);
            }
            if (storedResourceEntry.a.v == resource) {
                i2 = (int) (((double) i2) + storedResourceEntry.b);
            }
        }
        return i2;
    }

    public void a(StoredResources storedResources) {
        b();
        b(storedResources);
    }

    public void a(Resource resource, double d) {
        if (this.c) {
            throw new RuntimeException("StoredResources are locked");
        }
        int i = this.b.size;
        Object[] objArrA = this.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            if (storedResourceEntry.a == resource) {
                storedResourceEntry.b = d;
                return;
            }
        }
        StoredResourceEntry storedResourceEntry2 = new StoredResourceEntry(resource);
        storedResourceEntry2.b = d;
        this.b.add(storedResourceEntry2);
    }

    public void a(double d) {
        if (this.c) {
            throw new RuntimeException("StoredResources are locked");
        }
        int i = this.b.size;
        Object[] objArrA = this.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            ((StoredResourceEntry) objArrA[i2]).b *= d;
        }
    }

    public void b(Resource resource, double d) {
        if (this.c) {
            throw new RuntimeException("StoredResources are locked");
        }
        if (d == 0.0d) {
            return;
        }
        FastArrayList fastArrayList = this.b;
        int i = fastArrayList.size;
        Object[] objArrA = fastArrayList.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            if (storedResourceEntry.a == resource) {
                storedResourceEntry.b += d;
                return;
            }
        }
        StoredResourceEntry storedResourceEntry2 = new StoredResourceEntry(resource);
        storedResourceEntry2.b = d;
        fastArrayList.add(storedResourceEntry2);
    }

    public void c(Resource resource, double d) {
        if (this.c) {
            throw new RuntimeException("StoredResources are locked");
        }
        if (d == 0.0d) {
            return;
        }
        FastArrayList fastArrayList = this.b;
        int i = fastArrayList.size;
        Object[] objArrA = fastArrayList.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            if (storedResourceEntry.a == resource) {
                storedResourceEntry.b += d;
                return;
            }
        }
        StoredResourceEntry storedResourceEntry2 = new StoredResourceEntry(resource);
        storedResourceEntry2.b = d;
        fastArrayList.add(storedResourceEntry2);
    }

    public void d(Resource resource, double d) {
        if (this.c) {
            throw new RuntimeException("StoredResources are locked");
        }
        b(resource, -d);
    }

    public void a(UnitPrice unitPrice, double d, double d2) {
        if (unitPrice.b >= d && unitPrice.b <= d2) {
            c(Resource.D, unitPrice.b);
        }
        a(unitPrice.k, d, d2);
    }

    public void b(UnitPrice unitPrice, double d, double d2) {
        if (unitPrice.b >= d && unitPrice.b <= d2) {
            c(Resource.D, -unitPrice.b);
        }
        b(unitPrice.k, d, d2);
    }

    public void a(UnitPrice unitPrice) {
        c(Resource.D, unitPrice.b);
        b(unitPrice.k);
    }

    public void b(StoredResources storedResources) {
        if (this.c) {
            throw new RuntimeException("StoredResources are locked");
        }
        int i = storedResources.b.size;
        Object[] objArrA = storedResources.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            b(storedResourceEntry.a, storedResourceEntry.b);
        }
    }

    public void a(StoredResources storedResources, double d, double d2) {
        if (this.c) {
            throw new RuntimeException("StoredResources are locked");
        }
        int i = storedResources.b.size;
        Object[] objArrA = storedResources.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            if (storedResourceEntry.b >= d && storedResourceEntry.b <= d2) {
                b(storedResourceEntry.a, storedResourceEntry.b);
            }
        }
    }

    public void a(StoredResources storedResources, double d) {
        if (this.c) {
            throw new RuntimeException("StoredResources are locked");
        }
        int i = storedResources.b.size;
        Object[] objArrA = storedResources.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            b(storedResourceEntry.a, storedResourceEntry.b * d);
        }
    }

    public void c(StoredResources storedResources) {
        if (this.c) {
            throw new RuntimeException("StoredResources are locked");
        }
        int i = storedResources.b.size;
        Object[] objArrA = storedResources.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            d(storedResourceEntry.a, storedResourceEntry.b);
        }
    }

    public void b(StoredResources storedResources, double d, double d2) {
        if (this.c) {
            throw new RuntimeException("StoredResources are locked");
        }
        int i = storedResources.b.size;
        Object[] objArrA = storedResources.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            if (storedResourceEntry.b >= d && storedResourceEntry.b <= d2) {
                d(storedResourceEntry.a, storedResourceEntry.b);
            }
        }
    }

    public static StoredResources a(StoredResources storedResources, StoredResources storedResources2) {
        StoredResources storedResources3 = new StoredResources();
        storedResources3.b(storedResources);
        storedResources3.b(storedResources2);
        return storedResources3;
    }

    public static StoredResources b(StoredResources storedResources, StoredResources storedResources2) {
        StoredResources storedResources3 = new StoredResources();
        storedResources3.b(storedResources);
        storedResources3.c(storedResources2);
        return storedResources3;
    }

    public static StoredResources b(StoredResources storedResources, double d) {
        StoredResources storedResources2 = new StoredResources();
        storedResources2.a(storedResources, d);
        return storedResources2;
    }

    public static StoredResources d(StoredResources storedResources) {
        StoredResources storedResources2 = new StoredResources();
        storedResources2.b(storedResources);
        return storedResources2;
    }

    public static int a(StoredResources storedResources, BaseUnit baseUnit) {
        double dA;
        int iMin = 9999;
        int i = storedResources.b.size;
        Object[] objArrA = storedResources.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            if (storedResourceEntry.b > 0.0d) {
                if (storedResourceEntry.a.t) {
                    dA = baseUnit.team.c(storedResourceEntry.a);
                } else {
                    dA = baseUnit.a(storedResourceEntry.a);
                }
                iMin = Utility.min(iMin, (int) (dA / storedResourceEntry.b));
            }
        }
        return iMin;
    }

    public static boolean b(StoredResources storedResources, BaseUnit baseUnit) {
        double dA;
        int i = storedResources.b.size;
        Object[] objArrA = storedResources.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            if (storedResourceEntry.a.t) {
                dA = baseUnit.team.c(storedResourceEntry.a);
            } else {
                dA = baseUnit.a(storedResourceEntry.a);
            }
            if (storedResourceEntry.b > dA) {
                return false;
            }
        }
        return true;
    }

    public static boolean a(StoredResources storedResources, BaseUnit baseUnit, double d) {
        double dA;
        int i = storedResources.b.size;
        Object[] objArrA = storedResources.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            if (storedResourceEntry.a.t) {
                dA = baseUnit.team.c(storedResourceEntry.a);
            } else {
                dA = baseUnit.a(storedResourceEntry.a);
            }
            if (storedResourceEntry.b * d > dA) {
                return false;
            }
        }
        return true;
    }

    public static void c(StoredResources storedResources, BaseUnit baseUnit) {
        int i = storedResources.b.size;
        Object[] objArrA = storedResources.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            if (storedResourceEntry.a.t) {
                baseUnit.team.getCustomResources().d(storedResourceEntry.a, storedResourceEntry.b);
            } else {
                baseUnit.getCustomResources().d(storedResourceEntry.a, storedResourceEntry.b);
            }
        }
    }

    public static void b(StoredResources storedResources, BaseUnit baseUnit, double d) {
        int i = storedResources.b.size;
        Object[] objArrA = storedResources.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            if (storedResourceEntry.a.t) {
                baseUnit.team.getCustomResources().d(storedResourceEntry.a, storedResourceEntry.b * d);
            } else {
                baseUnit.getCustomResources().d(storedResourceEntry.a, storedResourceEntry.b * d);
            }
        }
    }

    public static void d(StoredResources storedResources, BaseUnit baseUnit) {
        int i = storedResources.b.size;
        Object[] objArrA = storedResources.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            if (storedResourceEntry.a.t) {
                baseUnit.team.getCustomResources().b(storedResourceEntry.a, storedResourceEntry.b);
            } else {
                baseUnit.getCustomResources().b(storedResourceEntry.a, storedResourceEntry.b);
            }
        }
    }

    public static void c(StoredResources storedResources, BaseUnit baseUnit, double d) {
        int i = storedResources.b.size;
        Object[] objArrA = storedResources.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            if (storedResourceEntry.a.t) {
                baseUnit.team.getCustomResources().b(storedResourceEntry.a, storedResourceEntry.b * d);
            } else {
                baseUnit.getCustomResources().b(storedResourceEntry.a, storedResourceEntry.b * d);
            }
        }
    }

    public static boolean a(StoredResources storedResources, BaseUnit baseUnit, BaseUnit baseUnit2) {
        boolean z = false;
        int i = storedResources.b.size;
        Object[] objArrA = storedResources.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            Resource resource = storedResourceEntry.a;
            double d = storedResourceEntry.b;
            if (d != 0.0d) {
                double dA = resource.a(baseUnit);
                double dA2 = resource.a(baseUnit2);
                if (d >= 0.0d) {
                    if (dA > 0.0d) {
                        double dMin = Utility.min(dA, d);
                        resource.b(baseUnit, -dMin);
                        resource.b(baseUnit2, dMin);
                        z = true;
                    }
                } else if (dA2 > 0.0d) {
                    double dMin2 = Utility.min(dA2, -d);
                    resource.b(baseUnit2, -dMin2);
                    resource.b(baseUnit, dMin2);
                    z = true;
                }
            }
        }
        return z;
    }

    public String a(boolean z, boolean z2, int i, boolean z3, boolean z4) {
        TextRenderQueue textRenderQueue = new TextRenderQueue();
        a(textRenderQueue, z, z2, i, z3, z4, null, 0);
        return textRenderQueue.getText();
    }

    public void a(TextRenderQueue textRenderQueue, boolean z, boolean z2, int i, boolean z3, boolean z4, BaseUnit baseUnit, int i2) {
        String str;
        int i3 = this.b.size;
        if (i3 == 0) {
            return;
        }
        if (z) {
            str = "\n";
        } else {
            str = " | ";
        }
        int i4 = 0;
        Object[] objArrA = this.b.a();
        for (int i5 = 0; i5 < i3; i5++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i5];
            if ((storedResourceEntry.b > 0.0d || z4) && i4 < i) {
                Resource resource = storedResourceEntry.a;
                if (z3 || !resource.a()) {
                    boolean z5 = false;
                    if (resource.y != null && resource.z) {
                        z5 = true;
                        int iC = textRenderQueue.getCharWidth() - 2;
                        if (iC < 2) {
                            iC = 2;
                        }
                        textRenderQueue.addTexture(resource.y, iC * 3, iC);
                    }
                    String str2 = resource.a(storedResourceEntry.b, false, z5) + str;
                    boolean z6 = false;
                    int iIntValue = 0;
                    if (resource.m != null && resource.n) {
                        z6 = true;
                        iIntValue = resource.m.intValue();
                    }
                    if (baseUnit != null && resource.a(baseUnit) < storedResourceEntry.b) {
                        z6 = true;
                        iIntValue = i2;
                    }
                    if (z6) {
                        textRenderQueue.addColoredTextWithColor(str2, iIntValue);
                    } else {
                        textRenderQueue.addText(str2);
                    }
                    i4++;
                }
            }
        }
    }

    public void a(GameOutputStream gameOutputStream) throws IOException {
        if (this.b.size == 0) {
            gameOutputStream.writeByte(-1);
            return;
        }
        gameOutputStream.writeByte(0);
        gameOutputStream.writeShort((short) this.b.size);
        int i = this.b.size;
        Object[] objArrA = this.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            gameOutputStream.writeStringUTF(storedResourceEntry.a.b);
            gameOutputStream.writeDouble(storedResourceEntry.b);
        }
    }

    public void a(GameInputStream gameInputStream) throws IOException {
        if (this.c) {
            throw new RuntimeException("StoredResources are locked");
        }
        if (gameInputStream.readByte() == -1) {
            return;
        }
        short shortValue = gameInputStream.readShortValue();
        this.b.clear();
        for (int i = 0; i < shortValue; i++) {
            Resource resourceB = Resource.b(gameInputStream.readUTF());
            double d = gameInputStream.readDouble();
            if (resourceB != null && d != 0.0d) {
                this.b.add(new StoredResourceEntry(resourceB, d));
            }
        }
    }

    public int d() {
        int i = 0;
        int i2 = this.b.size;
        Object[] objArrA = this.b.a();
        for (int i3 = 0; i3 < i2; i3++) {
            if (((StoredResourceEntry) objArrA[i3]).b != 0.0d) {
                i++;
            }
        }
        return i;
    }

    public boolean e(StoredResources storedResources) {
        if (d() != storedResources.d()) {
            return false;
        }
        int i = this.b.size;
        Object[] objArrA = this.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            if (!Utility.b(storedResourceEntry.b, storedResources.a(storedResourceEntry.a))) {
                return false;
            }
        }
        return true;
    }

    public boolean f(StoredResources storedResources) {
        int i = this.b.size;
        Object[] objArrA = this.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            if (storedResourceEntry.b > 0.0d && storedResources.b(storedResourceEntry.a) > 0.0d) {
                return true;
            }
        }
        return false;
    }

    public StoredResources a(BaseUnit baseUnit) {
        double dA;
        StoredResources storedResources = new StoredResources();
        int i = this.b.size;
        Object[] objArrA = this.b.a();
        for (int i2 = 0; i2 < i; i2++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i2];
            if (storedResourceEntry.a.t) {
                dA = baseUnit.team.c(storedResourceEntry.a);
            } else {
                dA = baseUnit.a(storedResourceEntry.a);
            }
            if (dA < storedResourceEntry.b) {
                storedResources.b(storedResourceEntry.a, storedResourceEntry.b - dA);
            }
        }
        if (storedResources.c()) {
            return a;
        }
        return storedResources;
    }

    public String a(BaseUnit baseUnit, String str, int i, boolean z) {
        double dA;
        String str2 = null;
        int i2 = 0;
        int i3 = this.b.size;
        Object[] objArrA = this.b.a();
        for (int i4 = 0; i4 < i3; i4++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i4];
            if (z || !storedResourceEntry.a.a()) {
                if (storedResourceEntry.a.t) {
                    dA = baseUnit.team.c(storedResourceEntry.a);
                } else {
                    dA = baseUnit.a(storedResourceEntry.a);
                }
                if (dA < storedResourceEntry.b) {
                    double d = storedResourceEntry.b - dA;
                    String strI = storedResourceEntry.a.i();
                    if (str2 == null) {
                        str2 = strI;
                    } else {
                        str2 = str2 + str + strI;
                    }
                    i2++;
                    if (i2 > i) {
                        break;
                    }
                } else {
                    continue;
                }
            }
        }
        return str2;
    }

    public void g(StoredResources storedResources) {
        b();
        b(storedResources);
    }

    public void c(Resource resource) {
        FastArrayList fastArrayList = this.b;
        int i = fastArrayList.size;
        Object[] objArrA = fastArrayList.a();
        for (int i2 = 0; i2 < i; i2++) {
            if (((StoredResourceEntry) objArrA[i2]).a == resource) {
                return;
            }
        }
        StoredResourceEntry storedResourceEntry = new StoredResourceEntry(resource);
        storedResourceEntry.b = 0.0d;
        fastArrayList.add(storedResourceEntry);
    }

    public void e() {
        // from class: com.corrodinggames.rts.game.units.custom.e.f.1
// java.util.Comparator
        /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
        Collections.sort(this.b, (storedResourceEntry, storedResourceEntry2) -> {
            if (storedResourceEntry.a == null || storedResourceEntry2.a == null) {
                return 0;
            }
            return Float.compare(storedResourceEntry.a.x, storedResourceEntry2.a.x);
        });
    }
}
