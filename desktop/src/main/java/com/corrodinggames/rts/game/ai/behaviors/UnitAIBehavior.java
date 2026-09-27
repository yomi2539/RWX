package com.corrodinggames.rts.game.ai.behaviors;

import com.corrodinggames.rts.game.ai.AIController;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import com.corrodinggames.rts.gameFramework.utility.UnitList;
import java.io.IOException;
import java.util.Iterator;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.a.a.c */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/a/a/c.class */
public abstract class UnitAIBehavior extends AIBehavior {

    /* JADX INFO: renamed from: a */
    UnitList managedUnits = new UnitList();

    /* JADX INFO: renamed from: c */
    public abstract boolean isApplicableToUnit(AIController aIController, OrderableUnit orderableUnit);

    @Override // com.corrodinggames.rts.game.ai.behaviors.AIBehavior
    /* JADX INFO: renamed from: a */
    public void readFromInputStream(GameInputStream gameInputStream) throws IOException {
        super.readFromInputStream(gameInputStream);
        int i = gameInputStream.readInt();
        for (int i2 = 0; i2 < i; i2++) {
            OrderableUnit unitEntity = gameInputStream.readOrderableUnit();
            if (unitEntity != null) {
                this.managedUnits.add(unitEntity);
            }
        }
    }

    @Override // com.corrodinggames.rts.game.ai.behaviors.AIBehavior
    public void a(GameOutputStream gameOutputStream) throws IOException {
        super.a(gameOutputStream);
        gameOutputStream.writeInt(this.managedUnits.size());
        Iterator it = this.managedUnits.iterator();
        while (it.hasNext()) {
            gameOutputStream.writeOrderableUnit((OrderableUnit) it.next());
        }
    }

    @Override // com.corrodinggames.rts.game.ai.behaviors.AIBehavior
    /* JADX INFO: renamed from: a */
    public void onUnitAdded(AIController aIController, OrderableUnit orderableUnit) {
        if (isApplicableToUnit(aIController, orderableUnit) && !this.managedUnits.contains(orderableUnit)) {
            this.managedUnits.add(orderableUnit);
        }
    }

    @Override // com.corrodinggames.rts.game.ai.behaviors.AIBehavior
    /* JADX INFO: renamed from: b */
    public void onUnitRemoved(AIController aIController, OrderableUnit orderableUnit) {
    }
}
