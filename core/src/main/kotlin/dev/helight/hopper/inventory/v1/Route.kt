package dev.helight.hopper.inventory.v1

interface Route {
    fun asMap(): MutableMap<Int, InteractivePoint>
    fun build() {}
    fun put(i: Int, node: InteractivePoint) {
        asMap()[i] = node
    }

    operator fun get(absolute: Int, relative: Int, page: Int): InteractivePoint? {
        return asMap()[absolute]
    } /*

    default int pageAmount(int rows){
        double d = (double)new TreeSet<>(asMap().keySet()).last() / (double)(rows * 9);
        return (int)Math.ceil(d);
    };

    default int pageIndex(int offset, int rows) {
        double d = (double)offset / (double)(rows*9);
        return (int)Math.ceil(d);
    };

     */
}