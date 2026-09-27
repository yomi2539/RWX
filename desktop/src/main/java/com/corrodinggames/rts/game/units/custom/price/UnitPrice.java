package com.corrodinggames.rts.game.units.custom.price;

import android.graphics.Color;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.custom.ConfigParseException;
import com.corrodinggames.rts.game.units.custom.CustomUnitConfig;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.game.units.custom.condition.resources.Resource;
import com.corrodinggames.rts.game.units.custom.condition.StoredResourceEntry;
import com.corrodinggames.rts.game.units.custom.condition.StoredResources;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.ui.LagHidingManager;
import com.corrodinggames.rts.gameFramework.ui.TextRenderQueue;
import com.corrodinggames.rts.gameFramework.utility.IniFile;
import java.io.IOException;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.custom.d.b */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/custom/d/b.class */
public class UnitPrice extends PriceCondition implements Comparable<UnitPrice> {
    public int b;
    public float c;
    public float d;
    public float e;
    public int f;
    public int g;
    public int h;
    public int i;
    public int j;
    public StoredResources k = m;
    private static final StoredResources m = new StoredResources().a();
    public static final UnitPrice a = a(0);
    static final int l = Color.a(255, 0, 100, 0);

    public int a() {
        return this.b;
    }

    public int b() {
        if (this.k == m) {
            return this.b;
        }
        int i = this.b;
        int i2 = this.k.b.size;
        Object[] objArrA = this.k.b.a();
        for (int i3 = 0; i3 < i2; i3++) {
            StoredResourceEntry storedResourceEntry = (StoredResourceEntry) objArrA[i3];
            if (storedResourceEntry.b > 0.0d) {
                float fB = storedResourceEntry.a.b();
                if (fB != 0.0f) {
                    i += (int) (((double) fB) * storedResourceEntry.b);
                }
            }
        }
        return i;
    }

    public static UnitPrice a(UnitPrice unitPrice, UnitPrice unitPrice2) {
        UnitPrice unitPrice3 = new UnitPrice();
        unitPrice3.b = unitPrice.b + unitPrice2.b;
        unitPrice3.c = unitPrice.c + unitPrice2.c;
        unitPrice3.d = unitPrice.d + unitPrice2.d;
        unitPrice3.e = unitPrice.e + unitPrice2.e;
        unitPrice3.f = unitPrice.f + unitPrice2.f;
        if (!unitPrice.k.c() || !unitPrice2.k.c()) {
            unitPrice3.k = StoredResources.a(unitPrice.k, unitPrice2.k);
        }
        return unitPrice3;
    }

    public static UnitPrice a(UnitPrice unitPrice, float f) {
        UnitPrice unitPrice2 = new UnitPrice();
        unitPrice2.b = (int) (unitPrice.b * f);
        unitPrice2.c = unitPrice.c * f;
        unitPrice2.d = unitPrice.d * f;
        unitPrice2.e = unitPrice.e * f;
        unitPrice2.f = (int) (unitPrice.f * f);
        if (!unitPrice.k.c()) {
            unitPrice2.k = StoredResources.b(unitPrice.k, f);
        }
        return unitPrice2;
    }

    public static UnitPrice a(int i) {
        UnitPrice unitPrice = new UnitPrice();
        unitPrice.b = i;
        return unitPrice;
    }

    public static UnitPrice a(CustomUnitConfig customUnitConfig, IniFile iniFile, String str, String str2, boolean z) throws ConfigParseException {
        String string = iniFile.getString(str, str2, (String) null);
        if (string == null && !z) {
            throw new RuntimeException("Could not find " + str2 + " in configuration file under:" + str);
        }
        try {
            return b(customUnitConfig, string);
        } catch (ConfigParseException e) {
            throw new ConfigParseException("[" + str + "]" + str2 + ": " + e.getMessage());
        }
    }

    public static UnitPrice a(CustomUnitConfig customUnitConfig, IniFile iniFile, String str, String str2, UnitPrice unitPrice) throws ConfigParseException {
        String string = iniFile.getString(str, str2, (String) null);
        if (string == null) {
            return unitPrice;
        }
        try {
            return b(customUnitConfig, string);
        } catch (ConfigParseException e) {
            throw new ConfigParseException("[" + str + "]" + str2 + ": " + e.getMessage());
        }
    }

    public static UnitPrice b(CustomUnitConfig customUnitConfig, IniFile iniFile, String str, String str2, UnitPrice unitPrice) throws ConfigParseException {
        String string = iniFile.getString(str, str2, (String) null);
        if (string == null) {
            return unitPrice;
        }
        try {
            return a(customUnitConfig, string);
        } catch (ConfigParseException e) {
            throw new ConfigParseException("[" + str + "]" + str2 + ": " + e.getMessage());
        }
    }

    public static void b(int i) throws ConfigParseException {
        if (i < 0 || i > 31) {
            throw new ConfigParseException("Flag id must be between 0-31 (is:" + i + ")");
        }
    }

    public static int a(int i, String str) throws ConfigParseException {
        if (str.contains("-")) {
            String[] strArrSplitByChar = Utility.splitByChar(str, '-');
            if (strArrSplitByChar.length != 2) {
                throw new ConfigParseException("Unexpected flag id: " + str);
            }
            int i2 = Integer.parseInt(strArrSplitByChar[0]);
            int i3 = Integer.parseInt(strArrSplitByChar[1]);
            b(i2);
            b(i3);
            if (i3 < i2) {
                throw new ConfigParseException("end<start in flag id: " + str);
            }
            for (int i4 = i2; i4 <= i3; i4++) {
                i |= 1 << i4;
            }
            return i;
        }
        int i5 = Integer.parseInt(str);
        b(i5);
        return i | (1 << i5);
    }

    public static UnitPrice a(CustomUnitConfig customUnitConfig, String str) throws ConfigParseException {
        UnitPrice unitPriceB = b(customUnitConfig, str);
        if (unitPriceB != null && unitPriceB.f != 0) {
            throw new ConfigParseException("Ammo not supported on streaming price:" + str);
        }
        return unitPriceB;
    }

    public static UnitPrice b(CustomUnitConfig customUnitConfig, String str) throws ConfigParseException {
        String strTrim;
        String strTrim2;
        if (str == null) {
            return a;
        }
        UnitPrice unitPrice = new UnitPrice();
        for (String str2 : str.split(",|\\|")) {
            String strTrim3 = str2.trim();
            if (!strTrim3.equals(VariableScope.nullOrMissingString)) {
                String[] strArrSplit = strTrim3.split("=|:");
                if (strArrSplit.length == 1) {
                    strTrim = "credits";
                    strTrim2 = strArrSplit[0];
                } else if (strArrSplit.length == 2) {
                    strTrim = strArrSplit[0].trim();
                    strTrim2 = strArrSplit[1].trim();
                } else {
                    throw new ConfigParseException("Unknown price format:" + str);
                }
                try {
                    if (strTrim.equals("credits")) {
                        unitPrice.b = Integer.parseInt(strTrim2);
                    } else if (strTrim.equals("energy")) {
                        unitPrice.c = Float.parseFloat(strTrim2);
                    } else if (strTrim.equals("hp")) {
                        unitPrice.d = Float.parseFloat(strTrim2);
                    } else if (strTrim.equals("shield")) {
                        unitPrice.e = Float.parseFloat(strTrim2);
                    } else if (strTrim.equals("ammo")) {
                        unitPrice.f = Integer.parseInt(strTrim2);
                    } else if (strTrim.equals("hasFlag")) {
                        unitPrice.i = a(unitPrice.i, strTrim2);
                    } else if (strTrim.equals("hasMissingFlag")) {
                        unitPrice.j = a(unitPrice.j, strTrim2);
                    } else if (strTrim.equals("setFlag")) {
                        unitPrice.g = a(unitPrice.g, strTrim2);
                    } else if (strTrim.equals("unsetFlag")) {
                        unitPrice.h = a(unitPrice.h, strTrim2);
                    } else {
                        Resource resourceFindCustomResourceInList = customUnitConfig.findCustomResourceInList(strTrim);
                        if (resourceFindCustomResourceInList != null) {
                            float f = Float.parseFloat(strTrim2);
                            if (unitPrice.k == m) {
                                unitPrice.k = new StoredResources();
                            }
                            unitPrice.k.a(resourceFindCustomResourceInList, f);
                        } else {
                            throw new ConfigParseException("Unknown price type:" + strTrim);
                        }
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    String str3 = "Bad price number:" + strTrim2 + " in " + str;
                    if (0 != 0) {
                        str3 = str3 + " (Hint: A whole number was expected)";
                    }
                    throw new ConfigParseException(str3);
                }
            }
        }
        if (unitPrice.k != m) {
            unitPrice.k.a();
        }
        if (!unitPrice.d()) {
            return a;
        }
        return unitPrice;
    }

    public int a(BaseUnit baseUnit, boolean z) {
        int iMin = 9999;
        if (!z && this.b > 0) {
            iMin = Utility.min(9999, (int) (baseUnit.team.credits / ((double) this.b)));
        }
        if (this.c > 0.0f) {
            iMin = Utility.min(iMin, (int) (baseUnit.currentEnergy / this.c));
        }
        if (this.d > 0.0f) {
            iMin = Utility.min(iMin, (int) (baseUnit.currentHealth / this.d));
        }
        if (this.e > 0.0f) {
            iMin = Utility.min(iMin, (int) (baseUnit.shield / this.e));
        }
        if (this.f > 0) {
            iMin = Utility.min(iMin, baseUnit.ammo / this.f);
        }
        if (!this.k.c()) {
            iMin = Utility.min(iMin, StoredResources.a(this.k, baseUnit));
        }
        if (!f(baseUnit)) {
            iMin = 0;
        }
        return iMin;
    }

    @Override // com.corrodinggames.rts.game.units.custom.price.PriceCondition
    public boolean b(BaseUnit baseUnit, double d) {
        if (this.b > 0 && !baseUnit.team.hasCredits(((double) this.b) * d)) {
            return false;
        }
        if (this.c > 0.0f && baseUnit.currentEnergy < ((double) this.c) * d) {
            return false;
        }
        if (this.d > 0.0f && baseUnit.currentHealth < ((double) this.d) * d) {
            return false;
        }
        if (this.e > 0.0f && baseUnit.shield < ((double) this.e) * d) {
            return false;
        }
        if ((this.f > 0 && baseUnit.ammo < ((double) this.f) * d) || !f(baseUnit)) {
            return false;
        }
        if (!this.k.c() && !StoredResources.a(this.k, baseUnit, d)) {
            return false;
        }
        return true;
    }

    @Override // com.corrodinggames.rts.game.units.custom.price.PriceCondition
    public boolean b(BaseUnit baseUnit) {
        if (this.b > 0 && !baseUnit.team.hasCredits(this.b)) {
            return false;
        }
        if (this.c > 0.0f && baseUnit.currentEnergy < this.c) {
            return false;
        }
        if (this.d > 0.0f && baseUnit.currentHealth < this.d) {
            return false;
        }
        if (this.e > 0.0f && baseUnit.shield < this.e) {
            return false;
        }
        if ((this.f > 0 && baseUnit.ammo < this.f) || !f(baseUnit)) {
            return false;
        }
        if (!this.k.c() && !StoredResources.b(this.k, baseUnit)) {
            return false;
        }
        return true;
    }

    public boolean a(BaseUnit baseUnit, BaseUnit baseUnit2) {
        boolean z = false;
        if (!this.k.c() && StoredResources.a(this.k, baseUnit, baseUnit2)) {
            z = true;
        }
        return z;
    }

    public static void d(BaseUnit baseUnit) {
        if (baseUnit.currentEnergy < 0.0f) {
            baseUnit.currentEnergy = 0.0f;
        }
        if (baseUnit.currentEnergy > baseUnit.bd()) {
            baseUnit.currentEnergy = baseUnit.bd();
        }
        if (baseUnit.shield < 0.0f) {
            baseUnit.shield = 0.0f;
        }
        if (baseUnit.shield > baseUnit.unitEnergyMax) {
            baseUnit.shield = baseUnit.unitEnergyMax;
        }
        if (baseUnit.currentHealth > baseUnit.maxHealth) {
            baseUnit.currentHealth = baseUnit.maxHealth;
        }
        if (baseUnit.ammo < 0) {
            baseUnit.ammo = 0;
        }
    }

    public void e(BaseUnit baseUnit) {
        if (this.h != 0) {
            baseUnit.unitFlags &= this.h ^ (-1);
        }
        if (this.g != 0) {
            baseUnit.unitFlags |= this.g;
        }
    }

    public int c(int i) {
        if (this.h != 0) {
            i &= this.h ^ (-1);
        }
        if (this.g != 0) {
            i |= this.g;
        }
        return i;
    }

    public static boolean a(int i, int i2) {
        return (i & (1 << i2)) != 0;
    }

    public boolean f(BaseUnit baseUnit) {
        if (this.i != 0 && !b(baseUnit.unitFlags, this.i)) {
            return false;
        }
        if (this.j != 0 && c(baseUnit.unitFlags, this.j)) {
            return false;
        }
        return true;
    }

    public static boolean b(int i, int i2) {
        return (i2 & i) == i2;
    }

    public static boolean c(int i, int i2) {
        return (i2 & i) != 0;
    }

    @Override // com.corrodinggames.rts.game.units.custom.price.PriceCondition
    public void a(BaseUnit baseUnit) {
        baseUnit.team.credits -= (double) this.b;
        baseUnit.currentEnergy -= this.c;
        baseUnit.currentHealth -= this.d;
        baseUnit.shield -= this.e;
        baseUnit.ammo -= this.f;
        e(baseUnit);
        if (!this.k.c()) {
            StoredResources.c(this.k, baseUnit);
        }
        d(baseUnit);
    }

    @Override // com.corrodinggames.rts.game.units.custom.price.PriceCondition
    public void a(BaseUnit baseUnit, double d) {
        baseUnit.team.credits -= ((double) this.b) * d;
        baseUnit.currentEnergy = (float) (((double) baseUnit.currentEnergy) - (((double) this.c) * d));
        baseUnit.currentHealth = (float) (((double) baseUnit.currentHealth) - (((double) this.d) * d));
        baseUnit.shield = (float) (((double) baseUnit.shield) - (((double) this.e) * d));
        baseUnit.ammo = (int) (((double) baseUnit.ammo) - (((double) this.f) * d));
        e(baseUnit);
        if (!this.k.c()) {
            StoredResources.b(this.k, baseUnit, d);
        }
        d(baseUnit);
    }

    public void g(BaseUnit baseUnit) {
        if (this.b > 0) {
            baseUnit.team.b(this.b);
        } else {
            baseUnit.team.credits += (double) this.b;
        }
        baseUnit.currentEnergy += this.c;
        baseUnit.currentHealth += this.d;
        baseUnit.shield += this.e;
        baseUnit.ammo += this.f;
        e(baseUnit);
        if (!this.k.c()) {
            StoredResources.d(this.k, baseUnit);
        }
        d(baseUnit);
    }

    public void h(BaseUnit baseUnit) {
        baseUnit.team.credits += (double) this.b;
        baseUnit.currentEnergy += this.c;
        baseUnit.currentHealth += this.d;
        baseUnit.shield += this.e;
        baseUnit.ammo += this.f;
        e(baseUnit);
        if (!this.k.c()) {
            StoredResources.d(this.k, baseUnit);
        }
        d(baseUnit);
    }

    public void a(BaseUnit baseUnit, double d, boolean z) {
        if (z) {
            baseUnit.team.credits += ((double) this.b) * d;
        }
        baseUnit.currentEnergy = (float) (((double) baseUnit.currentEnergy) + (((double) this.c) * d));
        baseUnit.currentHealth = (float) (((double) baseUnit.currentHealth) + (((double) this.d) * d));
        baseUnit.shield = (float) (((double) baseUnit.shield) + (((double) this.e) * d));
        baseUnit.ammo = (int) (((double) baseUnit.ammo) + (((double) this.f) * d));
        e(baseUnit);
        if (!this.k.c()) {
            StoredResources.c(this.k, baseUnit, d);
        }
        d(baseUnit);
    }

    public boolean c() {
        if (this == a) {
            return true;
        }
        if (this.b != 0 || this.c != 0.0f || this.d != 0.0f || this.e != 0.0f || this.f != 0 || !this.k.c()) {
            return false;
        }
        return true;
    }

    public boolean d() {
        if (this == a) {
            return false;
        }
        if (this.b != 0 || this.c != 0.0f || this.d != 0.0f || this.e != 0.0f || this.f != 0 || this.g != 0 || this.h != 0 || this.i != 0 || this.j != 0 || !this.k.c()) {
            return true;
        }
        return false;
    }

    public boolean e() {
        if (this == a) {
            return false;
        }
        if (this.b != 0 || this.c != 0.0f || this.d != 0.0f || this.e != 0.0f || this.f != 0 || this.g != 0 || this.h != 0) {
            return true;
        }
        return false;
    }

    public String a(boolean z, boolean z2, int i, boolean z3) {
        TextRenderQueue textRenderQueue = new TextRenderQueue();
        a(textRenderQueue, z, z2, i, z3);
        return textRenderQueue.getText();
    }

    public void a(TextRenderQueue textRenderQueue, boolean z, boolean z2, int i, boolean z3, BaseUnit baseUnit, int i2) {
        b(textRenderQueue, z, z2, i, z3, baseUnit, i2);
    }

    private void a(TextRenderQueue textRenderQueue, boolean z, boolean z2, int i, boolean z3) {
        b(textRenderQueue, z, z2, i, z3, null, 0);
    }

    private void b(TextRenderQueue textRenderQueue, boolean z, boolean z2, int i, boolean z3, BaseUnit baseUnit, int i2) {
        String str;
        if (z) {
            str = "\n";
        } else {
            str = " | ";
        }
        int i3 = 0;
        if (this.b > 0 && 0 < i) {
            int i4 = l;
            if (baseUnit != null && baseUnit.team.credits < this.b) {
                i4 = i2;
            }
            textRenderQueue.addColoredTextWithColor("$" + this.b + str, i4);
            i3 = 0 + 1;
        }
        if (z2) {
            if (this.c > 0.0f && i3 < i) {
                textRenderQueue.addText(Utility.padString(this.c) + " energy" + str);
                i3++;
            }
            if (this.d > 0.0f && i3 < i) {
                textRenderQueue.addText(Utility.padString(this.d) + " hp" + str);
                i3++;
            }
            if (this.e > 0.0f && i3 < i) {
                textRenderQueue.addText(Utility.padString(this.e) + " shield" + str);
                i3++;
            }
            if (this.f > 0 && i3 < i) {
                textRenderQueue.addText(Utility.padString(this.f) + " ammo" + str);
                i3++;
            }
        }
        if (!this.k.c()) {
            this.k.a(textRenderQueue, z, z2, i - i3, z3, false, baseUnit, i2);
        }
        textRenderQueue.trimLastTextSuffix(str);
    }

    public UnitPrice i(BaseUnit baseUnit) {
        UnitPrice unitPrice = new UnitPrice();
        if (this.b > 0 && baseUnit.team.credits < this.b) {
            unitPrice.b = this.b - ((int) baseUnit.team.credits);
        }
        if (!this.k.c()) {
            unitPrice.k = this.k.a(baseUnit);
        }
        return unitPrice;
    }

    public String a(BaseUnit baseUnit, int i, boolean z) {
        String strA;
        String str = null;
        if (this.b > 0 && 0 < i && baseUnit.team.credits < this.b) {
            if (0 == 0) {
                str = VariableScope.nullOrMissingString;
            }
            str = str + "credits, ";
            int i2 = 0 + 1;
        }
        if (!this.k.c() && (strA = this.k.a(baseUnit, ", ", i, z)) != null) {
            if (str == null) {
                str = VariableScope.nullOrMissingString;
            }
            str = str + strA;
        }
        if (str != null) {
            return Utility.removeSuffix(str, ", ");
        }
        return null;
    }

    @Override // java.lang.Comparable
    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public int compareTo(UnitPrice unitPrice) {
        return this.b - unitPrice.b;
    }

    public static void a(GameOutputStream gameOutputStream, UnitPrice unitPrice) throws IOException {
        gameOutputStream.writeBoolean(unitPrice != null);
        if (unitPrice != null) {
            unitPrice.a(gameOutputStream);
        }
    }

    public void a(GameOutputStream gameOutputStream) throws IOException {
        boolean z = false;
        boolean z2 = false;
        if (this.c != 0.0f || this.d != 0.0f || this.e != 0.0f || this.f != 0) {
            z = true;
        }
        if (this.g != 0 || this.h != 0 || this.i != 0 || this.j != 0) {
            z = true;
        }
        if (!this.k.c()) {
            z2 = true;
        }
        byte b = 0;
        if (z) {
            b = (byte) (0 | 1);
        }
        if (z2) {
            b = (byte) (b | 2);
        }
        gameOutputStream.writeByte(b);
        gameOutputStream.writeInt(this.b);
        if (z) {
            gameOutputStream.writeFloat(this.c);
            gameOutputStream.writeFloat(this.d);
            gameOutputStream.writeFloat(this.e);
            gameOutputStream.writeInt(this.f);
            gameOutputStream.writeInt(this.g);
            gameOutputStream.writeInt(this.h);
            gameOutputStream.writeInt(this.i);
            gameOutputStream.writeInt(this.j);
        }
        if (z2) {
            this.k.a(gameOutputStream);
        }
    }

    public static UnitPrice a(GameInputStream gameInputStream) throws IOException {
        if (gameInputStream.readBoolean()) {
            return b(gameInputStream);
        }
        return null;
    }

    public static UnitPrice b(GameInputStream gameInputStream) throws IOException {
        UnitPrice unitPrice = new UnitPrice();
        byte b = gameInputStream.readByte();
        boolean zB = b(b, 1);
        boolean zB2 = b(b, 2);
        unitPrice.b = gameInputStream.readInt();
        if (zB) {
            unitPrice.c = gameInputStream.readFloat();
            unitPrice.d = gameInputStream.readFloat();
            unitPrice.e = gameInputStream.readFloat();
            unitPrice.f = gameInputStream.readInt();
            unitPrice.g = gameInputStream.readInt();
            unitPrice.h = gameInputStream.readInt();
            unitPrice.i = gameInputStream.readInt();
            unitPrice.j = gameInputStream.readInt();
        }
        if (zB2) {
            unitPrice.k = new StoredResources();
            unitPrice.k.a(gameInputStream);
        }
        return unitPrice;
    }

    public boolean b(BaseUnit baseUnit, boolean z) {
        if (c(baseUnit, z)) {
            d(baseUnit, z);
            return true;
        }
        return false;
    }

    public boolean c(BaseUnit baseUnit, boolean z) {
        if (this.b > 0 && !baseUnit.team.hasCreditsIncludingAntiLagCredit(this.b)) {
            return false;
        }
        if (z) {
            return LagHidingManager.canAffordWithSnapshots(baseUnit, this);
        }
        return b(baseUnit);
    }

    public void d(BaseUnit baseUnit, boolean z) {
        baseUnit.team.energy -= (double) this.b;
        baseUnit.team.teamUnitCount = 0;
        if (z) {
            LagHidingManager.addResourcesToSnapshot(baseUnit, this);
        }
    }

    public void e(BaseUnit baseUnit, boolean z) {
        baseUnit.team.energy += (double) this.b;
        baseUnit.team.teamUnitCount = 0;
        if (z) {
            LagHidingManager.removeResourcesFromSnapshot(baseUnit, this);
        }
    }

    public static boolean b(UnitPrice unitPrice, UnitPrice unitPrice2) {
        if (unitPrice2 == unitPrice) {
            return true;
        }
        if (unitPrice2 == null || unitPrice == null) {
            return false;
        }
        return unitPrice2.b(unitPrice);
    }

    public boolean b(UnitPrice unitPrice) {
        if (this.b != unitPrice.b || this.d != unitPrice.d || this.e != unitPrice.e || this.f != unitPrice.f || this.k.c() != unitPrice.k.c()) {
            return false;
        }
        if (!this.k.c() && !unitPrice.k.c() && !this.k.e(unitPrice.k)) {
            return false;
        }
        return true;
    }

    public boolean c(UnitPrice unitPrice) {
        if (this.b > 0 && unitPrice.b > 0) {
            return true;
        }
        if (this.d > 0.0f && unitPrice.d > 0.0f) {
            return true;
        }
        if (this.e > 0.0f && unitPrice.e > 0.0f) {
            return true;
        }
        if (this.f > 0 && unitPrice.f > 0) {
            return true;
        }
        if (!this.k.c() && !unitPrice.k.c() && this.k.f(unitPrice.k)) {
            return true;
        }
        return false;
    }
}
