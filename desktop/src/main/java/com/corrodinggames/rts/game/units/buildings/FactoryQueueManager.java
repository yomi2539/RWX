package com.corrodinggames.rts.game.units.buildings;

import android.graphics.PointF;
import com.corrodinggames.rts.game.PlayerTeam;
import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.game.units.UnitType;
import com.corrodinggames.rts.game.units.actions.AbstractUnitAction;
import com.corrodinggames.rts.game.units.actions.ActionId;
import com.corrodinggames.rts.game.units.actions.PopupQueueAction;
import com.corrodinggames.rts.game.units.custom.AnimationTag;
import com.corrodinggames.rts.game.units.custom.price.UnitPrice;
import com.corrodinggames.rts.gameFramework.GameEngine;
import com.corrodinggames.rts.gameFramework.Serializable;
import com.corrodinggames.rts.gameFramework.Utility;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.network.NetworkEngine;
import com.corrodinggames.rts.gameFramework.utility.FastArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.units.d.k */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/units/d/k.class */
public class FactoryQueueManager {
    OrderableUnit a;
    public PointF b = null;
    public final FastArrayList<Projectile> c = new FastArrayList();
    final FastArrayList<Projectile> d = new FastArrayList<>();
    public float e;
    Projectile f;

    public FactoryQueueManager(OrderableUnit orderableUnit) {
        this.a = orderableUnit;
    }

    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeFloat(this.e);
        gameOutputStream.writeInt(this.c.size());
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            ((Serializable) it.next()).a(gameOutputStream);
        }
        gameOutputStream.writeBoolean(this.b != null);
        if (this.b != null) {
            gameOutputStream.writeFloat(this.b.x);
            gameOutputStream.writeFloat(this.b.y);
        }
    }

    public void a(GameInputStream gameInputStream) throws IOException {
        this.e = gameInputStream.readFloat();
        int i = gameInputStream.readInt();
        this.c.clear();
        for (int i2 = 0; i2 < i; i2++) {
            Projectile projectile = new Projectile();
            projectile.readFromStream(gameInputStream);
            if (AbstractUnitAction.isActionIdSpecified(projectile.j)) {
                if (this.a.validateActionId(projectile.j) == null) {
                    GameEngine.log("Factory", this.a.r() + " no longer has the action:" + projectile.j);
                } else {
                    this.c.add(projectile);
                }
            } else {
                GameEngine.log("Factory", "buildQueue has uIndex of -1, skipping");
            }
        }
        if (gameInputStream.getProtocolVersion() >= 5) {
            if (gameInputStream.readBoolean()) {
                if (this.b == null) {
                    this.b = new PointF();
                }
                this.b.x = gameInputStream.readFloat();
                this.b.y = gameInputStream.readFloat();
                return;
            }
            this.b = null;
        }
    }

    public BaseUnit a(Projectile projectile, float f, boolean z, float f2) {
        AbstractUnitAction abstractUnitActionA = this.a.validateActionId(projectile.j);
        if (abstractUnitActionA == null) {
            NetworkEngine.reportDesyncVerbose("specialAction=null on completeQueueItem for item.uIndex:" + projectile.j + " id:" + this.a.objectId, true);
            return null;
        }
        UnitType unitType = abstractUnitActionA.getUnitType();
        if (unitType == null) {
            NetworkEngine.reportDesyncVerbose("unitType=null on completeQueueItem for item.uIndex:" + projectile.j + " id:" + this.a.objectId, false);
            return null;
        }
        return a(unitType, f, z, f2);
    }

    public void a(BaseUnit baseUnit, float f, boolean z) {
        baseUnit.spawnExitLockTimer = 30.0f;
        if (this.a instanceof ExperimentalLandFactory) {
            baseUnit.spawnExitLockTimer += 40.0f;
        }
        if (baseUnit instanceof OrderableUnit) {
            OrderableUnit orderableUnit = (OrderableUnit) baseUnit;
            orderableUnit.j(90.0f);
            if (orderableUnit.getMoveSpeed() < 0.75d) {
                baseUnit.spawnExitLockTimer += 30.0f;
            }
            if (orderableUnit.getMoveSpeed() < 0.55d) {
                baseUnit.spawnExitLockTimer += 20.0f;
            }
            float f2 = z ? 0.0f : 1.0f;
            float fFastCos = this.a.posX + (Utility.fastCos(baseUnit.rotationSpeed) * f);
            float fFastSin = this.a.posY + (Utility.fastSin(baseUnit.rotationSpeed) * f);
            if (this.b != null) {
                if (f != 0.0f) {
                    orderableUnit.appendMoveWaypoint(fFastCos, fFastSin);
                }
                orderableUnit.appendMoveWaypoint(this.b.x + f2, this.b.y);
            } else {
                float fFastSin2 = fFastCos - (Utility.fastSin(baseUnit.rotationSpeed) * f2);
                float fFastCos2 = fFastSin + (Utility.fastCos(baseUnit.rotationSpeed) * f2);
                if (f != 0.0f) {
                    orderableUnit.appendMoveWaypoint(fFastSin2, fFastCos2);
                }
            }
        }
    }

    public BaseUnit a(UnitType unitType, float f, boolean z, float f2) {
        BaseUnit baseUnitA = unitType.a();
        baseUnitA.posX = this.a.posX;
        baseUnitA.posY = this.a.posY + 5.0f;
        baseUnitA.rotationSpeed = 90.0f + f2;
        baseUnitA.f(this.a.team);
        baseUnitA.setCommandTargetUnit(this.a);
        a(baseUnitA, f, z);
        GameEngine gameEngine = GameEngine.getInstance();
        if (baseUnitA.team == gameEngine.playerTeam) {
            gameEngine.gameUI.warLogDisplay.logUnitCreated(baseUnitA);
        }
        return baseUnitA;
    }

    public final boolean a() {
        return this.c.size == 0;
    }

    public Projectile a(PopupQueueAction popupQueueAction, boolean z) {
        return a(popupQueueAction, z, (PointF) null, (BaseUnit) null);
    }

    public Projectile a(PopupQueueAction popupQueueAction, boolean z, PointF pointF, BaseUnit baseUnit) {
        Projectile projectile = new Projectile();
        projectile.j = popupQueueAction.getActionId();
        projectile.targetPoint = pointF;
        projectile.targetUnit = baseUnit;
        if (projectile.j == null) {
            throw new RuntimeException("item.uIndex==null??");
        }
        projectile.launchDelay = 1;
        projectile.b = popupQueueAction.K();
        projectile.c = popupQueueAction.getPrice();
        projectile.d = popupQueueAction.getAdditionalCost();
        projectile.e = popupQueueAction.getAnimationSet();
        projectile.f = popupQueueAction.isHighPriority();
        projectile.unitType = popupQueueAction.getUnitType();
        projectile.isHighPriority = popupQueueAction.isHighPriorityQueue();
        if (!z) {
            PlayerTeam.b((BaseUnit) this.a);
            if (projectile.isHighPriority) {
                int i = 0;
                for (int i2 = 0; i2 < this.c.size() && ((Projectile) this.c.get(i2)).isHighPriority; i2++) {
                    i = i2 + 1;
                }
                if (i != 0 || this.c.size() != 0) {
                }
                this.c.add(i, projectile);
            } else {
                this.c.add(projectile);
            }
            PlayerTeam.c(this.a);
        } else {
            this.d.add(projectile);
        }
        return projectile;
    }

    public Projectile b(PopupQueueAction popupQueueAction, boolean z) {
        if (z) {
            if (a(popupQueueAction.getActionId(), true) > 0) {
                Projectile projectileA = a(popupQueueAction, true);
                projectileA.k = true;
                return projectileA;
            }
            return null;
        }
        FastArrayList fastArrayList = this.c;
        ListIterator listIterator = fastArrayList.listIterator(fastArrayList.size());
        while (listIterator.hasPrevious()) {
            Projectile projectile = (Projectile) listIterator.previous();
            if (projectile.j.equals(popupQueueAction.getActionId())) {
                PlayerTeam.b((BaseUnit) this.a);
                listIterator.remove();
                PlayerTeam.c(this.a);
                return projectile;
            }
        }
        return null;
    }

    public void a(Projectile projectile) {
        this.f = projectile;
        this.a.updateUnitMovement();
    }

    public Projectile b() {
        return this.f;
    }

    public UnitPrice c() {
        if (this.f == null || this.f.d == null) {
            return null;
        }
        return UnitPrice.a(this.f.d, -(this.f.b * this.a.getNanoFactorySpeed() * 60.0f));
    }

    public AbstractUnitAction d() {
        if (this.f != null) {
            return this.a.validateActionId(this.f.j);
        }
        return null;
    }

    public void a(float f) {
        if (!a()) {
            Projectile projectile = (Projectile) f().get(0);
            if (this.f != projectile) {
                if (projectile.m < 0.0f) {
                    projectile.m = 0.0f;
                    ((FactoryQueueInterface) this.a).b(projectile);
                }
                if (this.f != null) {
                    this.e = projectile.m;
                }
                a(projectile);
            }
            float fCx = projectile.b * this.a.getNanoFactorySpeed() * f;
            boolean z = false;
            if (projectile.d != null) {
                if (this.e + fCx > 1.0f) {
                    fCx = 1.0f - this.e;
                    z = true;
                }
                double d = ((double) (this.e + fCx)) - projectile.n;
                double d2 = 0.0d;
                if (z) {
                    d2 = 1.0d - projectile.n;
                } else if (d >= 0.009999999776482582d) {
                    d2 = ((double) ((int) (d / 0.009999999776482582d))) * 0.009999999776482582d;
                }
                boolean z2 = false;
                if (d2 > 0.0d && this.a.team.resourceShortageTracker.a(projectile.d)) {
                    z2 = true;
                }
                if (!z2 && (d2 <= 0.0d || projectile.d.c(this.a, d2))) {
                    projectile.n += d2;
                } else {
                    if (!z2) {
                        this.a.team.resourceShortageTracker.a(projectile.d, this.a, d2);
                    }
                    fCx = 0.0f;
                    z = false;
                }
            }
            this.e += fCx;
            if (z) {
                this.e = 1.0f;
            }
            projectile.m = this.e;
            if (this.e >= 1.0f) {
                if (projectile.f && ((FactoryQueueInterface) this.a).dA()) {
                    this.e = 1.0f;
                    return;
                }
                PlayerTeam.b((BaseUnit) this.a);
                this.e = 0.0f;
                projectile.launchDelay--;
                if (projectile.launchDelay <= 0) {
                    List listF = f();
                    if (listF.size() == 0) {
                        GameEngine.logColored("-------------buildQueue empty for:" + projectile.j);
                        GameEngine.logColored("-------------");
                    } else {
                        listF.remove(0);
                    }
                }
                PlayerTeam.c(this.a);
                ((FactoryQueueInterface) this.a).a(projectile);
                return;
            }
            return;
        }
        a((Projectile) null);
        this.e = 0.0f;
        if (this.d.size > 0) {
            Projectile projectile2 = (Projectile) this.d.get(0);
            if (projectile2.b > 10.0f && projectile2.m <= 0.0f) {
                projectile2.m = 1.0f;
                AbstractUnitAction abstractUnitActionA = this.a.validateActionId(projectile2.j);
                if (abstractUnitActionA != null && abstractUnitActionA.usesExtraLagHidingInUI()) {
                    abstractUnitActionA.a(this.a);
                }
            }
        }
    }

    public void e() {
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            Projectile projectile = (Projectile) it.next();
            if (this.a.validateActionId(projectile.j) == null) {
                b(projectile);
                c(projectile);
                it.remove();
            }
        }
    }

    public void a(boolean z) {
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            Projectile projectile = (Projectile) it.next();
            if (z) {
                b(projectile);
            }
            c(projectile);
            it.remove();
        }
    }

    private void b(Projectile projectile) {
        if (((FactoryQueueInterface) this.a).c(projectile)) {
            if (projectile.d != null && projectile.n > 0.0d) {
                projectile.d.a((BaseUnit) this.a, projectile.n, true);
            }
            projectile.c.h(this.a);
        }
    }

    private void c(Projectile projectile) {
    }

    public int a(UnitType unitType) {
        int i = 0;
        int i2 = this.c.size;
        if (i2 != 0) {
            Object[] objArrA = this.c.a();
            for (int i3 = 0; i3 < i2; i3++) {
                Projectile projectile = (Projectile) objArrA[i3];
                if (projectile.f && projectile.unitType == unitType) {
                    i += projectile.launchDelay;
                }
            }
        }
        return i;
    }

    public int a(ActionId actionId, boolean z) {
        return a(actionId, z, false);
    }

    public int a(AnimationTag animationTag) {
        if (animationTag == null) {
            return this.c.size;
        }
        int i = 0;
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            if (AnimationTag.a(animationTag, ((Projectile) it.next()).e)) {
                i++;
            }
        }
        return i;
    }

    public int a(ActionId actionId, boolean z, boolean z2) {
        int i = 0;
        if (this.c.size != 0) {
            for (Projectile projectile : this.c) {
                if (AbstractUnitAction.NONE_ACTION_ID == actionId || projectile.j.equals(actionId)) {
                    if (!z2 || projectile.f) {
                        i += projectile.launchDelay;
                    }
                }
            }
        }
        if (z && this.d.size != 0) {
            for (Projectile projectile2 : this.d) {
                if (AbstractUnitAction.NONE_ACTION_ID == actionId || projectile2.j.equals(actionId)) {
                    if (!z2 || projectile2.f) {
                        if (!projectile2.k) {
                            i += projectile2.launchDelay;
                        } else {
                            i -= projectile2.launchDelay;
                        }
                    }
                }
            }
        }
        return i;
    }

    public AbstractUnitAction b(UnitType unitType) {
        ArrayList arrayListN = this.a.getAvailableActions();
        int size = arrayListN.size();
        for (int i = 0; i < size; i++) {
            AbstractUnitAction abstractUnitAction = (AbstractUnitAction) arrayListN.get(i);
            if (abstractUnitAction != null && (abstractUnitAction instanceof PopupQueueAction)) {
                PopupQueueAction popupQueueAction = (PopupQueueAction) abstractUnitAction;
                if (popupQueueAction.getUnitType() == unitType) {
                    return popupQueueAction;
                }
            }
        }
        return null;
    }

    public Projectile a(AbstractUnitAction abstractUnitAction, boolean z, PointF pointF, BaseUnit baseUnit) {
        if (abstractUnitAction instanceof PopupQueueAction) {
            PopupQueueAction popupQueueAction = (PopupQueueAction) abstractUnitAction;
            if (!z) {
                if (abstractUnitAction.canAfford((BaseUnit) this.a, false) && abstractUnitAction.b(this.a)) {
                    if ((!popupQueueAction.isHighPriority() || this.a.team.getNonBuildingUnitCountIncludingQueued() < this.a.team.getUnitCap()) && popupQueueAction.getPrice().c(this.a)) {
                        return a(popupQueueAction, false, pointF, baseUnit);
                    }
                    return null;
                }
                return null;
            }
            Projectile projectileB = b(popupQueueAction, false);
            if (projectileB != null) {
                b(projectileB);
                c(projectileB);
                return projectileB;
            }
            return null;
        }
        return null;
    }

    public void a(AbstractUnitAction abstractUnitAction, boolean z) {
        if (abstractUnitAction instanceof PopupQueueAction) {
            PopupQueueAction popupQueueAction = (PopupQueueAction) abstractUnitAction;
            if (!z) {
                if (abstractUnitAction.canAfford((BaseUnit) this.a, true)) {
                    if ((!popupQueueAction.isHighPriority() || this.a.team.getNonBuildingUnitCountIncludingQueued() < this.a.team.getUnitCap()) && popupQueueAction.getPrice().b(this.a, abstractUnitAction.usesExtraLagHidingInUI())) {
                        a(popupQueueAction, true);
                        return;
                    }
                    return;
                }
                return;
            }
            if (b(popupQueueAction, true) != null) {
                popupQueueAction.getPrice().e(this.a, abstractUnitAction.usesExtraLagHidingInUI());
            }
        }
    }

    public void a(AbstractUnitAction abstractUnitAction) {
        if (this.d.size() != 0) {
            Projectile projectile = null;
            for (Projectile projectile2 : this.d) {
                if (projectile2.j.equals(abstractUnitAction.getActionId())) {
                    projectile = projectile2;
                }
            }
            if (projectile != null) {
                if (!projectile.k) {
                    projectile.c.e(this.a, abstractUnitAction.usesExtraLagHidingInUI());
                } else {
                    projectile.c.d(this.a, abstractUnitAction.usesExtraLagHidingInUI());
                }
                this.d.remove(projectile);
            }
        }
    }

    public List f() {
        return this.c;
    }

    public FastArrayList g() {
        return this.c;
    }
}
