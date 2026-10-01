package com.corrodinggames.rts.game.map;

import com.corrodinggames.rts.game.ScorchMark;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.gameFramework.*;
import com.corrodinggames.rts.gameFramework.graphics.GamePaint;
import com.corrodinggames.rts.gameFramework.graphics.GraphicsEngine;
import com.corrodinggames.rts.gameFramework.graphics.RenderTargetMode;
import com.corrodinggames.rts.gameFramework.graphics.Texture;
import com.corrodinggames.rts.gameFramework.ui.GameUI;
import io.github.rwx.geometry.Rect;
import io.github.rwx.geometry.RectF;
import io.github.rwx.render.frame.GameCanvasBlendMode;
import io.github.rwx.render.canvas.Paint;

import java.util.ArrayList;
import java.util.IdentityHashMap;

/* JADX INFO: renamed from: com.corrodinggames.rts.game.b.c */
/* JADX INFO: loaded from: game-lib.jar:com/corrodinggames/rts/game/b/c.class */
public final class LayerBufferManager {
    private GraphicsEngine graphicsBackend;
    private boolean smoothFogFadingEnabled;

    /* JADX INFO: renamed from: f */
    int gridOriginWorldX;

    /* JADX INFO: renamed from: g */
    int gridOriginWorldY;

    /* JADX INFO: renamed from: h */
    int cellBufferPixelSize;

    /* JADX INFO: renamed from: i */
    int cellWorldExtent;

    /* JADX INFO: renamed from: j */
    int cellInnerBufferPixelSize;

    /* JADX INFO: renamed from: k */
    int cellWorldStepSize;

    /* JADX INFO: renamed from: l */
    float invCellWorldStepSize;

    /* JADX INFO: renamed from: n */
    boolean useFogBlitComposite;

    /* JADX INFO: renamed from: a */
    int gridCellsPerAxis = 7;

    /* JADX INFO: renamed from: b */
    public Texture bufferLayerTexture = null;

    /* JADX INFO: renamed from: c */
    public GraphicsEngine bufferLayerGraphics = null;

    /* JADX INFO: renamed from: d */
    LayerBufferCell[][] gridCells = (LayerBufferCell[][]) null;

    /* JADX INFO: renamed from: e */
    public GamePaint copyBlitPaint = new GamePaint();

    /* JADX INFO: renamed from: m */
    float renderScale = 1.0f;

    /* JADX INFO: renamed from: o */
    Rect tmpRect = new Rect();

    /* JADX INFO: renamed from: p */
    int redrawFrameCounter = 0;

    public void bindGraphicsBackend(GraphicsEngine graphicsEngine) {
        if (graphicsEngine == null || this.graphicsBackend == graphicsEngine) {
            return;
        }
        releaseLayerBuffers();
        this.graphicsBackend = graphicsEngine;
        this.smoothFogFadingEnabled = TileMap.softFogFadingEnabled
                && graphicsEngine.backendCapabilities().getSupportsSmoothFogLayerBuffers();
    }

    public void releaseLayerBuffers() {
        io.github.rwx.mod.registry.TerrainRegistry.releaseGraphics();
        GraphicsEngine previousBackend = this.graphicsBackend;
        IdentityHashMap<Texture, Boolean> releasedTextures = new IdentityHashMap<>();
        Texture fallbackTexture = previousBackend != null ? previousBackend.r() : null;
        releaseTarget(this.bufferLayerGraphics, this.bufferLayerTexture, fallbackTexture, releasedTextures);
        if (this.gridCells != null) {
            for (int i = 0; i < this.gridCells.length; i++) {
                LayerBufferCell[] row = this.gridCells[i];
                if (row == null) {
                    continue;
                }
                for (int i2 = 0; i2 < row.length; i2++) {
                    LayerBufferCell layerBufferCell = row[i2];
                    if (layerBufferCell == null) {
                        continue;
                    }
                    releaseTarget(layerBufferCell.cellGraphicsCopy, layerBufferCell.cellLayerTexture, fallbackTexture, releasedTextures);
                    releaseTarget(layerBufferCell.fadeOutGraphics, layerBufferCell.fadeOutTexture, fallbackTexture, releasedTextures);
                }
            }
        }
        this.bufferLayerGraphics = null;
        this.bufferLayerTexture = null;
        this.gridCells = null;
        this.cellBufferPixelSize = 0;
        this.cellInnerBufferPixelSize = 0;
        this.cellWorldExtent = 0;
        this.cellWorldStepSize = 0;
        this.invCellWorldStepSize = 0.0f;
        this.useFogBlitComposite = false;
        this.renderScale = 1.0f;
        this.redrawFrameCounter = 0;
        this.graphicsBackend = null;
        this.smoothFogFadingEnabled = false;
    }

    private static void releaseTarget(
            GraphicsEngine graphicsEngine,
            Texture texture,
            Texture fallbackTexture,
            IdentityHashMap<Texture, Boolean> releasedTextures
    ) {
        if (graphicsEngine != null) {
            graphicsEngine.q();
        }
        if (texture != null && texture != fallbackTexture && releasedTextures.put(texture, Boolean.TRUE) == null) {
            texture.o();
        }
    }

    boolean isSmoothFogFadingEnabled() {
        return this.smoothFogFadingEnabled;
    }

    /**
     * Returns the backend that owns this manager's targets and capability policy. The active
     * render backend may temporarily be a post-processing target; that backend must not change
     * layer-buffer creation or redraw policy.
     */
    GraphicsEngine resourceBackend() {
        if (this.graphicsBackend == null) {
            throw new IllegalStateException("LayerBufferManager graphics backend is not bound");
        }
        return this.graphicsBackend;
    }

    /* JADX INFO: renamed from: a */
    public void updateGridParams() {
        GameEngine gameEngine = GameEngine.getInstance();
        this.renderScale = computeRenderScale();
        if (this.renderScale > 1.0f) {
        }
        this.cellWorldExtent = (int) (this.cellBufferPixelSize / this.renderScale);
        this.cellWorldStepSize = (int) (this.cellInnerBufferPixelSize / this.renderScale);
        this.invCellWorldStepSize = 1.0f / this.cellWorldStepSize;
        this.gridOriginWorldX = gameEngine.viewpointXInt - (this.cellWorldExtent / 2);
        this.gridOriginWorldY = gameEngine.viewpointYInt - (this.cellWorldExtent / 2);
        float f = 1.0f / 20;
        this.gridOriginWorldX = ((int) (this.gridOriginWorldX * f)) * 20;
        this.gridOriginWorldY = ((int) (this.gridOriginWorldY * f)) * 20;
        for (int i = 0; i < this.gridCellsPerAxis; i++) {
            for (int i2 = 0; i2 < this.gridCellsPerAxis; i2++) {
                LayerBufferCell layerBufferCell = this.gridCells[i][i2];
                layerBufferCell.needsRedraw = true;
                layerBufferCell.preRendered = false;
            }
        }
    }

    /* JADX INFO: renamed from: b */
    public void updateCellIndices() {
        for (int i = 0; i < this.gridCellsPerAxis; i++) {
            for (int i2 = 0; i2 < this.gridCellsPerAxis; i2++) {
                LayerBufferCell layerBufferCell = this.gridCells[i][i2];
                layerBufferCell.gridX = i;
                layerBufferCell.gridY = i2;
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public void scrollGridY(int i) {
        LayerBufferCell[] layerBufferCellArr = new LayerBufferCell[this.gridCellsPerAxis];
        if (i > 0) {
            for (int i2 = 0; i2 < this.gridCellsPerAxis; i2++) {
                layerBufferCellArr[i2] = this.gridCells[i2][0];
            }
            for (int i3 = 1; i3 < this.gridCellsPerAxis; i3++) {
                for (int i4 = 0; i4 < this.gridCellsPerAxis; i4++) {
                    this.gridCells[i4][i3 - 1] = this.gridCells[i4][i3];
                }
            }
            for (int i5 = 0; i5 < this.gridCellsPerAxis; i5++) {
                this.gridCells[i5][this.gridCellsPerAxis - 1] = layerBufferCellArr[i5];
            }
            for (int i6 = 0; i6 < this.gridCellsPerAxis; i6++) {
                markReusedCellForRedraw(this.gridCells[i6][this.gridCellsPerAxis - 1]);
            }
        } else {
            for (int i7 = 0; i7 < this.gridCellsPerAxis; i7++) {
                layerBufferCellArr[i7] = this.gridCells[i7][this.gridCellsPerAxis - 1];
            }
            for (int i8 = this.gridCellsPerAxis - 2; i8 >= 0; i8--) {
                for (int i9 = 0; i9 < this.gridCellsPerAxis; i9++) {
                    this.gridCells[i9][i8 + 1] = this.gridCells[i9][i8];
                }
            }
            for (int i10 = 0; i10 < this.gridCellsPerAxis; i10++) {
                this.gridCells[i10][0] = layerBufferCellArr[i10];
            }
            for (int i11 = 0; i11 < this.gridCellsPerAxis; i11++) {
                markReusedCellForRedraw(this.gridCells[i11][0]);
            }
        }
        updateCellIndices();
    }

    /* JADX INFO: renamed from: b */
    public void scrollGridX(int i) {
        LayerBufferCell[] layerBufferCellArr = new LayerBufferCell[this.gridCellsPerAxis];
        if (i > 0) {
            for (int i2 = 0; i2 < this.gridCellsPerAxis; i2++) {
                layerBufferCellArr[i2] = this.gridCells[0][i2];
            }
            for (int i3 = 1; i3 < this.gridCellsPerAxis; i3++) {
                for (int i4 = 0; i4 < this.gridCellsPerAxis; i4++) {
                    this.gridCells[i3 - 1][i4] = this.gridCells[i3][i4];
                }
            }
            for (int i5 = 0; i5 < this.gridCellsPerAxis; i5++) {
                this.gridCells[this.gridCellsPerAxis - 1][i5] = layerBufferCellArr[i5];
            }
            for (int i6 = 0; i6 < this.gridCellsPerAxis; i6++) {
                markReusedCellForRedraw(this.gridCells[this.gridCellsPerAxis - 1][i6]);
            }
        } else {
            for (int i7 = 0; i7 < this.gridCellsPerAxis; i7++) {
                layerBufferCellArr[i7] = this.gridCells[this.gridCellsPerAxis - 1][i7];
            }
            for (int i8 = this.gridCellsPerAxis - 2; i8 >= 0; i8--) {
                for (int i9 = 0; i9 < this.gridCellsPerAxis; i9++) {
                    this.gridCells[i8 + 1][i9] = this.gridCells[i8][i9];
                }
            }
            for (int i10 = 0; i10 < this.gridCellsPerAxis; i10++) {
                this.gridCells[0][i10] = layerBufferCellArr[i10];
            }
            for (int i11 = 0; i11 < this.gridCellsPerAxis; i11++) {
                markReusedCellForRedraw(this.gridCells[0][i11]);
            }
        }
        updateCellIndices();
    }

    private void markReusedCellForRedraw(LayerBufferCell layerBufferCell) {
        layerBufferCell.needsRedraw = true;
    }

    private void invalidateCell(LayerBufferCell layerBufferCell, boolean smoothFade) {
        if (smoothFade) {
            layerBufferCell.enableSmoothFade = true;
        } else {
            layerBufferCell.needsRedraw = true;
        }
    }

    /* JADX INFO: renamed from: a */
    public LayerBufferCell getCellAt(int i, int i2) {
        if (i < 0 || i >= this.gridCellsPerAxis || i2 < 0 || i2 >= this.gridCellsPerAxis || this.gridCells == null) {
            return null;
        }
        return this.gridCells[i][i2];
    }

    /* JADX INFO: renamed from: a */
    public void invalidateTileArea(int i, int i2, boolean z) {
        TileMap tileMap = GameEngine.getInstance().tileMap;
        int i3 = tileMap.tileWorldSizeX;
        int i4 = tileMap.tileWorldSizeY;
        int i5 = i * i3;
        int i6 = i2 * i4;
        invalidateWorldRect((i5 - this.gridOriginWorldX) - i3, (i6 - this.gridOriginWorldY) - i4, 3 * i3, 3 * i4, z);
    }

    /* JADX INFO: renamed from: c */
    public void invalidateAllCells() {
        if (this.gridCells != null) {
            for (int i = 0; i < this.gridCellsPerAxis; i++) {
                for (int i2 = 0; i2 < this.gridCellsPerAxis; i2++) {
                    this.gridCells[i][i2].needsRedraw = true;
                }
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public void invalidateWorldRect(int i, int i2, int i3, int i4, boolean z) {
        LayerBufferCell cellAt;
        LayerBufferCell cellAt2;
        LayerBufferCell cellAt3;
        int i5 = (int) (i * this.invCellWorldStepSize);
        int i6 = (int) (i2 * this.invCellWorldStepSize);
        LayerBufferCell cellAt4 = getCellAt(i5, i6);
        if (cellAt4 != null) {
            invalidateCell(cellAt4, z);
            boolean z2 = false;
            boolean z3 = false;
            if (i + i3 >= (cellAt4.gridX * this.cellWorldStepSize) + this.cellWorldExtent) {
                z2 = true;
            }
            if (i2 + i4 >= (cellAt4.gridY * this.cellWorldStepSize) + this.cellWorldExtent) {
                z3 = true;
            }
            if (z2 && (cellAt3 = getCellAt(i5 + 1, i6)) != null) {
                invalidateCell(cellAt3, z);
            }
            if (z3 && (cellAt2 = getCellAt(i5, i6 + 1)) != null) {
                invalidateCell(cellAt2, z);
            }
            if (z2 && z3 && (cellAt = getCellAt(i5 + 1, i6 + 1)) != null) {
                invalidateCell(cellAt, z);
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public void applyScorchToCells(ScorchMark scorchMark) {
        RectF rectFC = scorchMark.c();
        for (int i = 0; i < this.gridCellsPerAxis; i++) {
            for (int i2 = 0; i2 < this.gridCellsPerAxis; i2++) {
                if (this.gridCells != null) {
                    LayerBufferCell layerBufferCell = this.gridCells[i][i2];
                    if (Utility.rectanglesOverlap(layerBufferCell.getWorldBoundsRect(), rectFC)) {
                        boolean z = this.renderScale != 1.0f;
                        if (z) {
                        }
                        scorchMark.a(layerBufferCell.cellGraphicsCopy, layerBufferCell.getWorldLeft(), layerBufferCell.getWorldTop(), this.renderScale);
                        layerBufferCell.cellGraphicsCopy.p();
                        if (z) {
                        }
                    }
                }
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public void renderScorchMarksInCell(int i, int i2, GraphicsEngine graphicsEngine) {
        LayerBufferCell layerBufferCell = this.gridCells[i][i2];
        boolean z = this.renderScale != 1.0f;
        if (z) {
        }
        Rect worldBoundsRect = layerBufferCell.getWorldBoundsRect();
        Utility.grow(worldBoundsRect, 95.0f);
        GameObject[] gameObjectArrA = GameObject.fastGameObjectList.a();
        int size = GameObject.fastGameObjectList.size();
        for (int i3 = 0; i3 < size; i3++) {
            GameObject gameObject = gameObjectArrA[i3];
            if (gameObject instanceof ScorchMark) {
                ScorchMark scorchMark = (ScorchMark) gameObject;
                if (worldBoundsRect.b((int) scorchMark.posX, (int) scorchMark.posY)) {
                    scorchMark.a(graphicsEngine, layerBufferCell.getWorldLeft(), layerBufferCell.getWorldTop(), this.renderScale);
                }
            }
        }
        if (z) {
        }
    }

    /* JADX INFO: renamed from: b */
    public void drawDebugCellOverlay(int i, int i2, GraphicsEngine graphicsEngine) {
        GameEngine gameEngine = GameEngine.getInstance();
        TileMap tileMap = gameEngine.tileMap;
        if (gameEngine.gameUI.returnsFalse()) {
            int i3 = this.gridOriginWorldX + (i * this.cellWorldStepSize);
            int i4 = this.gridOriginWorldY + (i2 * this.cellWorldStepSize);
            int i5 = this.cellWorldExtent;
            int i6 = this.cellWorldExtent;
            int i7 = tileMap.groundLayer.widthTiles;
            int i8 = tileMap.groundLayer.heightTiles;
            if (((int) (i3 * tileMap.tileScaleX)) < 0) {
            }
            if (((int) (i4 * tileMap.tileScaleY)) < 0) {
            }
            if (((int) ((i3 + i5) * tileMap.tileScaleX)) > i7 - 1) {
                int i9 = i7 - 1;
            }
            if (((int) ((i4 + i6) * tileMap.tileScaleY)) > i8 - 1) {
                int i10 = i8 - 1;
            }
            if (this.renderScale < 0.4d) {
                return;
            }
            boolean z = this.renderScale != 1.0f;
            if (z) {
                graphicsEngine.i();
                graphicsEngine.a(this.renderScale, this.renderScale);
            }
            if (z) {
                graphicsEngine.j();
            }
        }
    }

    /* JADX INFO: renamed from: b */
    public void preRenderCell(int i, int i2) {
        LayerBufferCell layerBufferCell = TileMap.layerBufferManager.gridCells[i][i2];
        GameEngine gameEngine = GameEngine.getInstance();
        TileMap tileMap = gameEngine.tileMap;
        layerBufferCell.preRendered = true;
        boolean z = false;
        GraphicsEngine graphicsEngine = this.bufferLayerGraphics;
        graphicsEngine.b(-16777216);
        Texture texture = gameEngine.minimap.unitsTexture;
        if (texture != null) {
            Rect rect = new Rect();
            RectF rectF = new RectF();
            setMinimapSourceRect(
                    rect,
                    this.gridOriginWorldX + (i * this.cellWorldStepSize),
                    this.gridOriginWorldY + (i2 * this.cellWorldStepSize),
                    this.gridOriginWorldX + ((i + 1) * this.cellWorldStepSize),
                    this.gridOriginWorldY + ((i2 + 1) * this.cellWorldStepSize),
                    tileMap.tileWorldSizeX * tileMap.tileCountX,
                    tileMap.tileWorldSizeY * tileMap.tileCountY,
                    texture.p,
                    texture.q
            );
            rectF.a(0.0f, 0.0f, this.cellBufferPixelSize, this.cellBufferPixelSize);
            graphicsEngine.a(texture, rect, rectF, this.copyBlitPaint);
        }
        graphicsEngine.p();
        if (!z) {
            if (resourceBackend().backendCapabilities().getClearLayerBuffersBeforeCopy()) {
                layerBufferCell.cellGraphicsCopy.a(0, GameCanvasBlendMode.Clear);
            }
            layerBufferCell.cellGraphicsCopy.b(this.bufferLayerTexture, 0.0f, 0.0f, (Paint) null);
            layerBufferCell.cellGraphicsCopy.p();
        }
    }

    static void setMinimapSourceRect(Rect rect, int worldLeft, int worldTop, int worldRight, int worldBottom, int mapWorldWidth, int mapWorldHeight, int textureWidth, int textureHeight) {
        rect.a(
                (int) ((((float) worldLeft) / ((float) mapWorldWidth)) * textureWidth),
                (int) ((((float) worldTop) / ((float) mapWorldHeight)) * textureHeight),
                (int) ((((float) worldRight) / ((float) mapWorldWidth)) * textureWidth),
                (int) ((((float) worldBottom) / ((float) mapWorldHeight)) * textureHeight)
        );
    }

    /* JADX INFO: renamed from: c */
    public void renderCell(int i, int i2) {
        LayerBufferCell layerBufferCell = TileMap.layerBufferManager.gridCells[i][i2];
        renderCellInto(i, i2, this.bufferLayerGraphics, false);
    }

    /* JADX INFO: renamed from: c */
    public void renderCellInto(int i, int i2, GraphicsEngine graphicsEngine) {
        renderCellInto(i, i2, graphicsEngine, false);
    }

    private void renderCellInto(int i, int i2, GraphicsEngine graphicsEngine, boolean directToCellTexture) {
        LayerBufferCell layerBufferCell = TileMap.layerBufferManager.gridCells[i][i2];
        GameEngine gameEngine = GameEngine.getInstance();
        TileMap tileMap = gameEngine.tileMap;
        boolean z = false;
        if (gameEngine.settingsEngine.renderFancyWater) {
            z = true;
        }
        if (GameEngine.isSpaceGame() || GameEngine.isMapDebugMode()) {
            z = true;
        }
        if (z) {
            graphicsEngine.a(0, GameCanvasBlendMode.Clear);
        } else {
            boolean z2 = false;
            if (GameEngine.isSpaceGame()) {
                z2 = true;
            }
            if (resourceBackend().backendCapabilities().getClearLayerBuffersBeforeCopy()) {
                z2 = true;
            }
            if (GameUI.showModernSidebar) {
            }
            if (tileMap.fogEnabled) {
            }
            if (z2) {
                graphicsEngine.b(-16777216);
            }
        }
        if (resourceBackend().backendCapabilities().getClearLayerBuffersBeforeCopy()) {
            graphicsEngine.a(0, GameCanvasBlendMode.Clear);
        }
        int i3 = this.gridOriginWorldX + (i * this.cellWorldStepSize);
        int i4 = this.gridOriginWorldY + (i2 * this.cellWorldStepSize);
        boolean z3 = false;
        boolean z4 = false;
        if (!tileMap.groundLayer.hasAlpha) {
            z3 = true;
        }
        if (tileMap.fogEnabled) {
            z4 = true;
        }
        if (TileMap.fogDebugGlobalFlag) {
            z3 = false;
            z4 = false;
        }
        if (z3) {
            graphicsEngine.a(true);
        }
        tileMap.groundLayer.renderLayerRegion(graphicsEngine, i3, i4, i3, i4, this.cellWorldExtent, this.cellWorldExtent, this.renderScale, this.renderScale, tileMap.fogEnabled, false, false);
        if (tileMap.groundDetailsLayer != null) {
            if (z3 && tileMap.groundDetailsLayer.hasAlpha) {
                graphicsEngine.f();
                GameEngine.log("Ending blit early");
            }
            tileMap.groundDetailsLayer.renderLayerRegion(graphicsEngine, i3, i4, i3, i4, this.cellWorldExtent, this.cellWorldExtent, this.renderScale, this.renderScale, tileMap.fogEnabled, false, false);
        }
        if (tileMap.groundDetails2Layer != null) {
            if (z3 && tileMap.groundDetails2Layer.hasAlpha) {
                graphicsEngine.f();
                GameEngine.log("Ending blit early");
            }
            tileMap.groundDetails2Layer.renderLayerRegion(graphicsEngine, i3, i4, i3, i4, this.cellWorldExtent, this.cellWorldExtent, this.renderScale, this.renderScale, tileMap.fogEnabled, false, false);
        }
        for (MapLayer mapLayer : tileMap.mapLayers) {
            if (mapLayer.isItemsLayer) {
                if (z3 && mapLayer.hasAlpha) {
                    graphicsEngine.f();
                    GameEngine.log("Ending blit early");
                }
                mapLayer.renderLayerRegion(graphicsEngine, i3, i4, i3, i4, this.cellWorldExtent, this.cellWorldExtent, this.renderScale, this.renderScale, tileMap.fogEnabled, false, false);
            }
        }
        renderScorchMarksInCell(i, i2, graphicsEngine);
        if (tileMap.fogEnabled) {
            if (z4) {
                graphicsEngine.a(false);
            }
            tileMap.groundLayer.renderLayerRegion(graphicsEngine, i3, i4, i3, i4, this.cellWorldExtent, this.cellWorldExtent, this.renderScale, this.renderScale, tileMap.fogEnabled, true, true);
        }
        if (z3 || z4) {
            graphicsEngine.f();
        }
        if (gameEngine.gameUI.returnsFalse()) {
            drawDebugCellOverlay(i, i2, graphicsEngine);
        }
        layerBufferCell.needsRedraw = false;
        layerBufferCell.enableSmoothFade = false;
        layerBufferCell.fadeFrameCount = 0;
        layerBufferCell.preRendered = false;
        graphicsEngine.p();
        if (!directToCellTexture) {
            if (z || resourceBackend().backendCapabilities().getClearLayerBuffersBeforeCopy()) {
                layerBufferCell.cellGraphicsCopy.a(0, GameCanvasBlendMode.Clear);
            }
            layerBufferCell.cellGraphicsCopy.b(this.bufferLayerTexture, 0.0f, 0.0f, (Paint) null);
            layerBufferCell.cellGraphicsCopy.p();
        }
        if (TileMap.c) {
            layerBufferCell.cellGraphicsCopy.a(VariableScope.nullOrMissingString + layerBufferCell.redrawVersion, 40.0f, 40.0f, TileMap.fogAtlasDebugRedStrokePaint);
        }
        layerBufferCell.redrawVersion++;
        io.github.rwx.mod.registry.TerrainRegistry.invalidateCell(layerBufferCell);
    }

    private void drawTerrainCell(LayerBufferCell cell, Texture texture, Paint paint) {
        GameEngine engine = GameEngine.getInstance();
        if (!io.github.rwx.mod.registry.TerrainRegistry.drawCell(cell, engine.tileMap,
                engine.renderGraphicsEngine, resourceBackend(), texture, cell.tileSrcRect,
                cell.screenDstRectF, paint, this.renderScale)) {
            engine.renderGraphicsEngine.a(texture, cell.tileSrcRect, cell.screenDstRectF, paint);
        }
    }

    /* JADX INFO: renamed from: d */
    public void update() {
        if (GameEngine.isNonAndroidVersion && !GameEngine.isJavaDesktopVersion && !GameEngine.isGDXVersion) {
            return;
        }
        GameEngine gameEngine = GameEngine.getInstance();
        if (this.graphicsBackend == null) {
            bindGraphicsBackend(gameEngine.renderGraphicsEngine);
        }
        int iMax = Math.max((int) gameEngine.currentScreenWidthPixels, (int) gameEngine.currentScreenHeightPixels) + 3;
        if (this.gridCells != null && this.cellBufferPixelSize * this.gridCellsPerAxis < iMax + this.cellBufferPixelSize + 1) {
            GameEngine.log("map", "screen must have changed size, layerBufferSize too small at " + this.gridCellsPerAxis + ", adding to LayerBitmapBuffer");
            GameEngine.log("map", "new viewpoint:" + gameEngine.currentScreenWidthPixels + ", " + gameEngine.currentScreenHeightPixels);
            resizeBufferGrid(this.gridCellsPerAxis + 1);
        }
        if (this.gridCells == null) {
            // TileMap initializes the user preference after the manager is bound during engine startup.
            this.smoothFogFadingEnabled = TileMap.softFogFadingEnabled
                    && resourceBackend().backendCapabilities().getSupportsSmoothFogLayerBuffers();
            GameEngine.log("map", "setupLayerBuffers for size:" + iMax);
            long jNanoTime = System.nanoTime();
            GraphicsEngine resourceBackend = resourceBackend();
            int fixedLayerBufferPixelSize = resourceBackend
                    .backendCapabilities()
                    .getFixedLayerBufferPixelSize();
            boolean z = fixedLayerBufferPixelSize > 0;
            if (z) {
                this.cellBufferPixelSize = fixedLayerBufferPixelSize;
                this.gridCellsPerAxis = (iMax / this.cellBufferPixelSize) + 2
                        + Math.max(0, resourceBackend.backendCapabilities().getExtraLayerBufferCells());
            } else {
                iMax = Math.max(600, iMax);
                this.cellBufferPixelSize = (iMax / (this.gridCellsPerAxis - 2)) + 7 + 4;
                this.cellBufferPixelSize = ((int) ((this.cellBufferPixelSize * (1.0f / 20)) + 0.5f)) * 20;
            }
            if (this.cellBufferPixelSize * this.gridCellsPerAxis < iMax + this.cellBufferPixelSize + 1) {
                GameEngine.logColored("layerBufferSize is too small");
                GameEngine.logColored("layerBufferCount:" + this.gridCellsPerAxis);
                GameEngine.logColored("(layerBufferSize*(layerBufferCount):" + (this.cellBufferPixelSize * this.gridCellsPerAxis));
                GameEngine.logColored("longest+layerBufferSize+1:" + (iMax + this.cellBufferPixelSize + 1));
                GameEngine.logColored("longest:" + iMax);
                if (z) {
                    this.gridCellsPerAxis++;
                } else {
                    this.cellBufferPixelSize += 100;
                }
            }
            GameEngine.log("layerBufferSize:" + this.cellBufferPixelSize);
            this.cellInnerBufferPixelSize = this.cellBufferPixelSize - 4;
            GameEngine.logColored("layerBuffer:" + this.gridCellsPerAxis + "x" + this.gridCellsPerAxis + " = " + (this.gridCellsPerAxis * this.gridCellsPerAxis) + (this.smoothFogFadingEnabled ? " x2 for soft fade " : VariableScope.nullOrMissingString));
            this.gridCells = new LayerBufferCell[this.gridCellsPerAxis][this.gridCellsPerAxis];
            z = false;
            if (gameEngine.settingsEngine.renderFancyWater) {
                z = true;
            }
            if (GameEngine.isSpaceGame() || GameEngine.isMapDebugMode()) {
                z = true;
            }
            if (this.cellBufferPixelSize <= 0) {
                GameEngine.logColored("layerBuffer buffer size was too small at: " + this.cellBufferPixelSize);
                this.cellBufferPixelSize = 512;
            }
            if (z) {
                this.bufferLayerTexture = resourceBackend.a(this.cellBufferPixelSize, this.cellBufferPixelSize, true);
            } else {
                this.bufferLayerTexture = resourceBackend.a(this.cellBufferPixelSize, this.cellBufferPixelSize, false);
            }
            this.bufferLayerTexture.b(true);
            this.bufferLayerGraphics = resourceBackend.b(this.bufferLayerTexture, RenderTargetMode.IMMEDIATE);
            initMissingLayerBufferImages();
            GameEngine.log("----- layerBuffers create in:" + ((System.nanoTime() - jNanoTime) / 1000000.0d) + " ms");
        }
    }

    /* JADX INFO: renamed from: c */
    public void resizeBufferGrid(int i) {
        if (i < this.gridCellsPerAxis) {
            GameEngine.logWarningAndStack("newLayerBufferCount:" + i);
            return;
        }
        LayerBufferCell[][] layerBufferCellArr = new LayerBufferCell[i][i];
        for (int i2 = 0; i2 < this.gridCellsPerAxis; i2++) {
            for (int i3 = 0; i3 < this.gridCellsPerAxis; i3++) {
                layerBufferCellArr[i2][i3] = this.gridCells[i2][i3];
            }
        }
        this.gridCells = layerBufferCellArr;
        this.gridCellsPerAxis = i;
        initMissingLayerBufferImages();
    }

    /* JADX INFO: renamed from: e */
    public void disableSmoothFogFading() {
        this.smoothFogFadingEnabled = false;
        for (int i = 0; i < this.gridCellsPerAxis; i++) {
            for (int i2 = 0; i2 < this.gridCellsPerAxis; i2++) {
                LayerBufferCell layerBufferCell = this.gridCells[i][i2];
                if (layerBufferCell != null) {
                    if (layerBufferCell.fadeOutGraphics != null) {
                        layerBufferCell.fadeOutGraphics.q();
                        layerBufferCell.fadeOutGraphics = null;
                    }
                    if (layerBufferCell.fadeOutTexture != null) {
                        layerBufferCell.fadeOutTexture.o();
                        layerBufferCell.fadeOutTexture = null;
                    }
                }
            }
        }
    }

    /* JADX INFO: renamed from: f */
    public void initMissingLayerBufferImages() {
        GameEngine gameEngine = GameEngine.getInstance();
        GraphicsEngine resourceBackend = resourceBackend();
        ArrayList<LayerBufferCell> arrayList = null;
        boolean z = false;
        for (int i = 0; i < this.gridCellsPerAxis; i++) {
            for (int i2 = 0; i2 < this.gridCellsPerAxis; i2++) {
                if (this.gridCells[i][i2] == null) {
                    LayerBufferCell layerBufferCell = new LayerBufferCell(this, i, i2);
                    layerBufferCell.gridLinearIndex = this.redrawFrameCounter;
                    this.redrawFrameCounter++;
                    this.gridCells[i][i2] = layerBufferCell;
                    if (this.cellBufferPixelSize <= 0) {
                        GameEngine.logColored("initMissingLayerBufferImages: layerBuffer buffer size was too small at: " + this.cellBufferPixelSize);
                        this.cellBufferPixelSize = 512;
                    }
                    if (z) {
                        layerBufferCell.cellLayerTexture = resourceBackend.r();
                    } else if (gameEngine.settingsEngine.renderFancyWater) {
                        layerBufferCell.cellLayerTexture = resourceBackend.a(this.cellBufferPixelSize, this.cellBufferPixelSize, true);
                    } else {
                        layerBufferCell.cellLayerTexture = resourceBackend.a(this.cellBufferPixelSize, this.cellBufferPixelSize, false);
                    }
                    layerBufferCell.cellLayerTexture.b(true);
                    if (layerBufferCell.cellLayerTexture.A()) {
                        if (!z) {
                            GameEngine.logColored("initMissingLayerBufferImages: Failed to create map buffer at :" + this.cellBufferPixelSize + "px");
                        }
                        z = true;
                        layerBufferCell.cellLayerTexture = resourceBackend.r();
                        layerBufferCell.cellLayerTexture.b(true);
                    }
                    try {
                        layerBufferCell.cellGraphicsCopy = resourceBackend.b(layerBufferCell.cellLayerTexture, RenderTargetMode.IMMEDIATE);
                    } catch (OutOfMemoryError e) {
                        if (!z) {
                            GameEngine.reportOOM(AssetType.gameImageCreate, e);
                        }
                        z = true;
                        layerBufferCell.cellLayerTexture = resourceBackend.r();
                        layerBufferCell.cellLayerTexture.b(true);
                        layerBufferCell.cellGraphicsCopy = resourceBackend.b(layerBufferCell.cellLayerTexture, RenderTargetMode.IMMEDIATE);
                    }
                    if (arrayList == null) {
                        arrayList = new ArrayList();
                    }
                    arrayList.add(layerBufferCell);
                }
            }
        }
        if (z && this.smoothFogFadingEnabled) {
            disableSmoothFogFading();
        }
        if (arrayList != null) {
            for (LayerBufferCell layerBufferCell2 : arrayList) {
                if (this.smoothFogFadingEnabled) {
                    try {
                        layerBufferCell2.initFadeBufferTexture();
                    } catch (OutOfMemoryError e2) {
                        disableSmoothFogFading();
                        GameEngine.logColored("Not enough free memory to enable smooth fog fading");
                        System.gc();
                    }
                }
            }
        }
        updateGridParams();
    }

    /* JADX INFO: renamed from: g */
    public float computeRenderScale() {
        GameEngine gameEngine = GameEngine.getInstance();
        if (gameEngine.zoom > 1.0f) {
            return 1.0f;
        }
        return gameEngine.zoom;
    }

    public boolean hasVisiblePendingRedraws() {
        if (this.gridCells == null) {
            return true;
        }
        for (int i = 0; i < this.gridCellsPerAxis; i++) {
            for (int i2 = 0; i2 < this.gridCellsPerAxis; i2++) {
                LayerBufferCell layerBufferCell = this.gridCells[i][i2];
                if (layerBufferCell == null) {
                    return true;
                }
                if (!layerBufferCell.screenDstRect.a()) {
                    if (layerBufferCell.needsRedraw || layerBufferCell.enableSmoothFade) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void renderVisiblePendingRedrawsNow() {
        renderVisiblePendingRedraws(Integer.MAX_VALUE);
    }

    public int renderVisiblePendingRedraws(int budgetMs) {
        return renderPendingRedraws(budgetMs, true);
    }

    public int renderPendingRedraws(int budgetMs) {
        return renderPendingRedraws(budgetMs, false);
    }

    private int renderPendingRedraws(int budgetMs, boolean visibleOnly) {
        if (this.gridCells == null) {
            return 0;
        }
        if (budgetMs <= 0) {
            return 0;
        }
        GameEngine gameEngine = GameEngine.getInstance();
        long deadlineNanos = budgetMs == Integer.MAX_VALUE ? Long.MAX_VALUE : System.nanoTime() + (budgetMs * 1000000L);
        int renderedCount = 0;
        boolean lockedFogAtlas = false;
        try {
            for (int i = 0; i < this.gridCellsPerAxis; i++) {
                for (int i2 = 0; i2 < this.gridCellsPerAxis; i2++) {
                    LayerBufferCell layerBufferCell = this.gridCells[i][i2];
                    if (layerBufferCell == null || (visibleOnly && layerBufferCell.screenDstRect.a())) {
                        continue;
                    }
                    if (!layerBufferCell.needsRedraw && !layerBufferCell.enableSmoothFade) {
                        continue;
                    }
                    if (renderedCount > 0 && System.nanoTime() >= deadlineNanos) {
                        return renderedCount;
                    }
                    if (resourceBackend().backendCapabilities().getRequiresFogAtlasLock()
                            && !lockedFogAtlas) {
                        TileMap.acquireFogAtlasLock();
                        lockedFogAtlas = true;
                    }
                    gameEngine.renderGraphicsEngine.i();
                    renderCell(i, i2);
                    gameEngine.renderGraphicsEngine.j();
                    renderedCount++;
                }
            }
            return renderedCount;
        } finally {
            if (lockedFogAtlas) {
                TileMap.releaseFogAtlasLock();
            }
        }
    }

    /* JADX INFO: renamed from: a */
    public void setRenderScale(float f) {
        GameEngine gameEngine = GameEngine.getInstance();
        TileMap tileMap = gameEngine.tileMap;
        Long lValueOf = null;
        boolean z = false;
        float fComputeRenderScale = computeRenderScale();
        boolean z2 = false;
        float f2 = fComputeRenderScale / this.renderScale;
        if (Utility.abs(f2 - 1.0f) < 0.01f) {
            f2 = 1.0f;
        }
        if (fComputeRenderScale > 0.6d) {
            float f3 = 0.3f;
            if (GameEngine.isPC()) {
                f3 = 0.1f;
            }
            if (fComputeRenderScale - this.renderScale > f3) {
                z2 = true;
            }
            if (fComputeRenderScale == 1.0f && this.renderScale != 1.0f) {
                z2 = true;
            }
        }
        if (f2 != 1.0f) {
            int i = 10;
            float f4 = 0.03f;
            if (fComputeRenderScale < 0.3f) {
                i = 20;
                f4 = 0.09f;
            } else if (fComputeRenderScale < 0.5f) {
                i = 20;
                f4 = 0.07f;
            }
            if (fComputeRenderScale > 1.3f) {
                i = 7;
            }
            if (!GameEngine.isPC()) {
                i += 10;
            }
            if (Utility.abs(tileMap.fogScale - fComputeRenderScale) > 0.03f) {
                tileMap.fogScale = gameEngine.zoom;
                tileMap.fogFadeStep = 0;
            } else {
                tileMap.fogFadeStep++;
            }
            if (tileMap.fogFadeStep < 3) {
                tileMap.fogFadeSpeed = 0.0f;
            } else if (Utility.abs(fComputeRenderScale - this.renderScale) > f4) {
                tileMap.fogFadeSpeed += 1.0f;
            }
            if (tileMap.fogFadeSpeed > i) {
                tileMap.fogFadeSpeed = 0.0f;
                z2 = true;
            }
        }
        int preloadWorldMargin = Math.max(
                0,
                resourceBackend().backendCapabilities().getLayerBufferScrollPreloadWorldMargin()
        );
        if (gameEngine.viewpointXInt + gameEngine.visibleWorldWidth + 4.0f + preloadWorldMargin > this.gridOriginWorldX + (this.gridCellsPerAxis * this.cellWorldStepSize)) {
            this.gridOriginWorldX += this.cellWorldStepSize;
            scrollGridX(1);
        }
        if (gameEngine.viewpointXInt - 1 - preloadWorldMargin < this.gridOriginWorldX) {
            this.gridOriginWorldX -= this.cellWorldStepSize;
            scrollGridX(-1);
        }
        if (gameEngine.viewpointYInt + gameEngine.visibleWorldHeight + 4.0f + preloadWorldMargin > this.gridOriginWorldY + (this.gridCellsPerAxis * this.cellWorldStepSize)) {
            this.gridOriginWorldY += this.cellWorldStepSize;
            scrollGridY(1);
        }
        if (gameEngine.viewpointYInt - 1 - preloadWorldMargin < this.gridOriginWorldY) {
            this.gridOriginWorldY -= this.cellWorldStepSize;
            scrollGridY(-1);
        }
        if (gameEngine.viewpointXInt + gameEngine.visibleWorldWidth + 4.0f > this.gridOriginWorldX + (this.gridCellsPerAxis * this.cellWorldStepSize)) {
            z2 = true;
        }
        if (gameEngine.viewpointXInt - 1 < this.gridOriginWorldX) {
            z2 = true;
        }
        if (gameEngine.viewpointYInt + gameEngine.visibleWorldHeight + 4.0f > this.gridOriginWorldY + (this.gridCellsPerAxis * this.cellWorldStepSize)) {
            z2 = true;
        }
        if (gameEngine.viewpointYInt - 1 < this.gridOriginWorldY) {
            z2 = true;
        }
        if (z2) {
            updateGridParams();
        }
        float f5 = gameEngine.zoom / this.renderScale;
        if (Utility.abs(f5 - 1.0f) < 1.0E-4f) {
            f5 = 1.0f;
        }
        float f6 = (gameEngine.currentScreenWidthPixels / f5) + 2.0f;
        float f7 = (gameEngine.currentScreenHeightPixels / f5) + 2.0f;
        if (f5 != 1.0f) {
            gameEngine.renderGraphicsEngine.k();
            gameEngine.renderGraphicsEngine.a(f5, f5);
            tileMap.tempRectTile.a(gameEngine.screenClipRect);
            tileMap.tempRectTile.c = ((int) (tileMap.tempRectTile.a + (tileMap.tempRectTile.b() / f5))) + 2;
            tileMap.tempRectTile.d = ((int) (tileMap.tempRectTile.b + (tileMap.tempRectTile.c() / f5))) + 2;
            gameEngine.renderGraphicsEngine.a(tileMap.tempRectTile);
        }
        float f8 = (this.gridOriginWorldX - gameEngine.viewpointXSnapped) * this.renderScale;
        float f9 = (this.gridOriginWorldY - gameEngine.viewpointYSnapped) * this.renderScale;
        float f10 = (int) f8;
        float f11 = (int) f9;
        int i2 = 0;
        boolean z3 = false;
        if (GameEngine.isPC() && gameEngine.zoom < 0.3d) {
            z3 = true;
        }
        this.copyBlitPaint.a(z3);
        this.copyBlitPaint.d(z3);
        this.copyBlitPaint.b(z3);
        boolean z4 = false;
        for (int i3 = 0; i3 < this.gridCellsPerAxis; i3++) {
            try {
                for (int i4 = 0; i4 < this.gridCellsPerAxis; i4++) {
                    LayerBufferCell layerBufferCell = this.gridCells[i3][i4];
                    int i5 = (int) (f10 + (i3 * this.cellWorldStepSize * this.renderScale));
                    int i6 = (int) (f11 + (i4 * this.cellWorldStepSize * this.renderScale));
                    if (layerBufferCell.enableSmoothFade && !this.useFogBlitComposite) {
                        layerBufferCell.fadeFrameCount++;
                    }
                    layerBufferCell.screenDstRect.a(i5 + 1, i6 + 1, (i5 + this.cellBufferPixelSize) - 2, (i6 + this.cellBufferPixelSize) - 2);
                    if (layerBufferCell.screenDstRect.a <= f6 && layerBufferCell.screenDstRect.b <= f7) {
                        if (layerBufferCell.screenDstRect.c > f6) {
                            layerBufferCell.screenDstRect.c = (int) f6;
                        }
                        if (layerBufferCell.screenDstRect.d > f7) {
                            layerBufferCell.screenDstRect.d = (int) f7;
                        }
                        int i7 = (int) ((0.0f - gameEngine.viewpointXSnapped) * this.renderScale);
                        int i8 = (int) ((0.0f - gameEngine.viewpointYSnapped) * this.renderScale);
                        int worldWidth = (int) ((tileMap.getWorldWidth() - gameEngine.viewpointXSnapped) * this.renderScale);
                        int worldHeight = (int) ((tileMap.getWorldHeight() - gameEngine.viewpointYSnapped) * this.renderScale);
                        if (layerBufferCell.screenDstRect.a < i7) {
                            layerBufferCell.screenDstRect.a = i7;
                        }
                        if (layerBufferCell.screenDstRect.b < i8) {
                            layerBufferCell.screenDstRect.b = i8;
                        }
                        if (layerBufferCell.screenDstRect.c > worldWidth) {
                            layerBufferCell.screenDstRect.c = worldWidth;
                        }
                        if (layerBufferCell.screenDstRect.d > worldHeight) {
                            layerBufferCell.screenDstRect.d = worldHeight;
                        }
                        if (!layerBufferCell.screenDstRect.a()) {
                            boolean z5 = false;
                            boolean z6 = true;
                            if (layerBufferCell.needsRedraw) {
                                z5 = true;
                                z6 = false;
                            }
                            if (layerBufferCell.enableSmoothFade) {
                                int i9 = 10;
                                if (i2 > 3) {
                                    i9 = 10 + 2;
                                }
                                if (i2 > 6) {
                                    i9 += 2;
                                }
                                if (layerBufferCell.fadeFrameCount > i9) {
                                    layerBufferCell.fadeFrameCount = 0;
                                    z5 = true;
                                    i2++;
                                }
                            }
                            if (z5) {
                                z = true;
                                boolean z7 = false;
                                long jA = PerformanceProfiler.a();
                                if (lValueOf == null) {
                                    lValueOf = Long.valueOf(jA);
                                } else {
                                    int i10 = 200;
                                    if (this.useFogBlitComposite) {
                                        i10 = 30;
                                    }
                                    if (PerformanceProfiler.a(lValueOf.longValue(), jA) > i10) {
                                        z7 = true;
                                        this.useFogBlitComposite = true;
                                    }
                                }
                                if (z7
                                        && resourceBackend().backendCapabilities().getSupportsLayerBufferPreRendering()
                                        && layerBufferCell.needsRedraw
                                        && !layerBufferCell.preRendered) {
                                    preRenderCell(i3, i4);
                                }
                                if (!z7) {
                                    if (this.smoothFogFadingEnabled) {
                                        if (layerBufferCell.fadeOutTexture != null && layerBufferCell.fadeOutTexture.p != layerBufferCell.cellLayerTexture.p) {
                                            GameEngine.log("wrong sized fadeOutBitmap width:" + layerBufferCell.fadeOutTexture.p + " vs " + layerBufferCell.cellLayerTexture.p);
                                            layerBufferCell.fadeOutTexture.o();
                                            layerBufferCell.fadeOutTexture = null;
                                        }
                                        if (layerBufferCell.fadeOutTexture == null) {
                                            try {
                                                layerBufferCell.initFadeBufferTexture();
                                            } catch (OutOfMemoryError e) {
                                                e.printStackTrace();
                                                GameEngine.reportOOM(AssetType.gameImageCreate, e);
                                                disableSmoothFogFading();
                                                GameEngine.logColored("Not enough free memory to keep smooth fog fading");
                                                System.gc();
                                            }
                                            if (this.smoothFogFadingEnabled && layerBufferCell.fadeOutTexture == null) {
                                                gameEngine.alert("Disabling smooth fog fading due to error");
                                                disableSmoothFogFading();
                                                GameEngine.logColored("fadeOutBitmap == null");
                                                System.gc();
                                            }
                                        }
                                    }
                                    if (this.smoothFogFadingEnabled) {
                                        if (layerBufferCell.fadeProgressRatio > 0.0f) {
                                        }
                                        Texture texture = layerBufferCell.cellLayerTexture;
                                        layerBufferCell.cellLayerTexture = layerBufferCell.fadeOutTexture;
                                        layerBufferCell.fadeOutTexture = texture;
                                        GraphicsEngine graphicsEngine = layerBufferCell.cellGraphicsCopy;
                                        layerBufferCell.cellGraphicsCopy = layerBufferCell.fadeOutGraphics;
                                        layerBufferCell.fadeOutGraphics = graphicsEngine;
                                        if (z6) {
                                            layerBufferCell.fadeProgressRatio = 1.0f;
                                        } else {
                                            layerBufferCell.fadeProgressRatio = 0.0f;
                                        }
                                    } else {
                                        layerBufferCell.fadeProgressRatio = 0.0f;
                                    }
                                    if (resourceBackend().backendCapabilities().getRequiresFogAtlasLock()
                                            && !z4) {
                                        TileMap.acquireFogAtlasLock();
                                        z4 = true;
                                    }
                                    gameEngine.renderGraphicsEngine.i();
                                    renderCell(i3, i4);
                                    gameEngine.renderGraphicsEngine.j();
                                    if (TileMap.fogProfilingEnabled) {
                                        PerformanceProfiler.a("re-drawTile", jA);
                                    }
                                }
                            }
                            layerBufferCell.tileSrcRect.a(layerBufferCell.screenDstRect);
                            layerBufferCell.tileSrcRect.a(-i5, -i6);
                            layerBufferCell.screenDstRectF.a(layerBufferCell.screenDstRect);
                            layerBufferCell.screenDstRectF.a(-f10, -f11);
                            layerBufferCell.screenDstRectF.a(f8, f9);
                            if (layerBufferCell.fadeProgressRatio > 0.0f) {
                                layerBufferCell.fadeBlendPaint.a(z3);
                                layerBufferCell.fadeBlendPaint.c((int) ((1.0f - layerBufferCell.fadeProgressRatio) * 255.0f));
                                drawTerrainCell(layerBufferCell, layerBufferCell.fadeOutTexture, this.copyBlitPaint);
                                if (layerBufferCell.fadeProgressRatio < 0.98d) {
                                    drawTerrainCell(layerBufferCell, layerBufferCell.cellLayerTexture, layerBufferCell.fadeBlendPaint);
                                }
                                layerBufferCell.fadeProgressRatio -= 0.1f * f;
                            } else if (layerBufferCell.cellLayerTexture.A()) {
                                gameEngine.renderGraphicsEngine.a(layerBufferCell.cellLayerTexture, layerBufferCell.screenDstRectF, this.copyBlitPaint, 0.0f, 0.0f, 0, 0);
                            } else {
                                drawTerrainCell(layerBufferCell, layerBufferCell.cellLayerTexture, this.copyBlitPaint);
                            }
                        }
                    }
                }
            } finally {
                if (z4) {
                    TileMap.releaseFogAtlasLock();
                    z4 = false;
                }
            }
        }
        if (f5 != 1.0f) {
            gameEngine.renderGraphicsEngine.l();
        }
        if (!z) {
            this.useFogBlitComposite = false;
        }
    }
}
