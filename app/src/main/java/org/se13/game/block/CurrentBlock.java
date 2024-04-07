package org.se13.game.block;

import org.se13.game.item.FeverItem;
import org.se13.game.item.TetrisItem;

public class CurrentBlock {
    private final Block block;
    private final TetrisItem item;
    private BlockPosition position;
    private int rotate;

    public CurrentBlock(Block block) {
        this(block, null);
    }

    public CurrentBlock(Block block, TetrisItem item) {
        this.block = block;
        this.position = new BlockPosition(block.startOffset);
        this.rotate = 0;
        this.item = item;
    }

    public BlockPosition[] shape() {
        return block.shape(rotate);
    }

    public BlockPosition getPosition() {
        return this.position;
    }

    public BlockColor getColor() { return block.blockColor; }

    public void rotateCW() {
        rotate = (rotate + 1) % 4;
    }

    public void rotateCCW() {
        if (rotate == 0) {
            rotate = 3;
        } else {
            rotate--;
        }
    }

    public void move(int rows, int columns) {
        int newRowIndex = position.getRowIndex() + rows;
        int newColIndex = position.getColIndex() + columns;

        position.setRowIndex(newRowIndex);
        position.setColIndex(newColIndex);
    }

    public void reset() {
        rotate = 0;
        position = block.startOffset;
    }

    public CellID getId() { return block.cellId; }
    public int getRotateState() {
        return rotate;
    }

    public Cell[] cells() {
        BlockPosition[] shape = shape();
        Cell[] cells = new Cell[shape.length];
        for (int i = 0; i < shape.length; i++) {
            cells[i] = new Cell(shape[i], getId());
        }

        return bindItem(cells, item);
    }

    private Cell[] bindItem(Cell[] original, TetrisItem item) {
        if (item instanceof FeverItem) {
            int mutate = ((FeverItem) item).getPosition();
            original[mutate] = new Cell(original[mutate].position(), item.getId());
            return original;
        }

        return original;
    }
}
