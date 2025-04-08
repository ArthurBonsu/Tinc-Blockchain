package org.example.app.core.smartcontract;

import org.example.app.core.block.Block;
import java.util.ArrayList;
import java.util.List;

public class SmartContractProcess {
    private List<Block> blocks = new ArrayList<>();
    
    public int getBlockCount() {
        return blocks.size();
    }
    
    public void addBlock(Block block) {
        blocks.add(block);
    }
    
    public List<Block> getBlocks() {
        return new ArrayList<>(blocks);
    }
}