public class Derivation implements Factor {
    private Factor deFactor;

    public Derivation(Factor factor) {
        this.deFactor = factor;
    } //初始化

    @Override
    public Poly toPoly() { //toPoly时顺带进行求导操作。
        return deFactor.toPoly().derivate();
    }
}
