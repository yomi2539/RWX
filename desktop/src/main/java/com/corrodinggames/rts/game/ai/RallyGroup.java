package com.corrodinggames.rts.game.ai;

import com.corrodinggames.rts.game.units.BaseUnit;
import com.corrodinggames.rts.game.units.OrderableUnit;
import com.corrodinggames.rts.gameFramework.network.GameInputStream;
import com.corrodinggames.rts.gameFramework.network.GameOutputStream;
import java.io.IOException;
import java.util.Iterator;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.a.l */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/a/l.class */
public class RallyGroup extends AIUnitGroupBase {

    /* JADX INFO: renamed from: a */
    float lifetime;

    public RallyGroup(AIController aIController) {
        super(aIController);
        this.lifetime = 0.0f;
    }

    @Override // com.corrodinggames.rts.game.ai.AIStrategyNode, com.corrodinggames.rts.gameFramework.Serializable
    public void a(GameOutputStream gameOutputStream) throws IOException {
        gameOutputStream.writeInt(this.units.size());
        Iterator it = this.units.iterator();
        while (it.hasNext()) {
            gameOutputStream.writeOrderableUnit((OrderableUnit) it.next());
        }
        gameOutputStream.writeByte(1);
        gameOutputStream.writeInt(this.unitsNeedingTransport.size());
        Iterator it2 = this.unitsNeedingTransport.iterator();
        while (it2.hasNext()) {
            gameOutputStream.writeOrderableUnit((OrderableUnit) it2.next());
        }
        gameOutputStream.writeFloat(this.lifetime);
        super.a(gameOutputStream);
    }

    @Override // com.corrodinggames.rts.game.ai.AIStrategyNode
    /* JADX INFO: renamed from: a */
    public void readFromInputStream(GameInputStream gameInputStream) throws IOException {
        clearUnits();
        int i = gameInputStream.readInt();
        for (int i2 = 0; i2 < i; i2++) {
            OrderableUnit unitEntity = gameInputStream.readOrderableUnit();
            if (unitEntity != null) {
                addUnit(unitEntity);
            }
        }
        if (gameInputStream.readByte() >= 1) {
            this.unitsNeedingTransport.clear();
            int i3 = gameInputStream.readInt();
            for (int i4 = 0; i4 < i3; i4++) {
                OrderableUnit unitEntity2 = gameInputStream.readOrderableUnit();
                if (unitEntity2 != null) {
                    this.unitsNeedingTransport.add(unitEntity2);
                }
            }
            this.lifetime = gameInputStream.readFloat();
        }
        super.readFromInputStream(gameInputStream);
    }

    @Override // com.corrodinggames.rts.game.ai.AIUnitGroupBase
    /* JADX INFO: renamed from: c */
    public void updateAI(float f) {
        removeDeadUnits();
        if (!isAssignedToTransporter()) {
            this.lifetime += f;
        }
        Iterator it = this.units.iterator();
        while (it.hasNext()) {
            OrderableUnit orderableUnit = (OrderableUnit) it.next();
            if (getDistanceSqToUnit((BaseUnit) orderableUnit) < 3600.0f && orderableUnit.transportContainer == null) {
                if (orderableUnit.aB == this) {
                    orderableUnit.aB = null;
                }
                it.remove();
            }
        }
        if (this.units.size() == 0 || this.lifetime > 5000.0f) {
            destroy();
        }
    }

    /* JADX INFO: renamed from: c */
    public void addUnitNeedingTransport(OrderableUnit orderableUnit) {
        addUnit(orderableUnit);
        this.unitsNeedingTransport.add(orderableUnit);
    }
}
