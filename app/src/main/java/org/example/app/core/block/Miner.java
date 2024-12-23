package block;

public class Miner {
    private EthereumMiner miner;

    public Miner() {
        miner = new EthereumMiner();
    }

    public Block mine(Block lastBlock) {
        return miner.mineBlock(lastBlock);
    }
}
