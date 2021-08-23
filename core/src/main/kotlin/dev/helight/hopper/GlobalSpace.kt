package dev.helight.hopper

fun Class<*>.toKey(): ComponentID {
    return bitIdGen(name.hashCode(), simpleName.hashCode().toUShort(), 1U)
}

fun TypeGroup.toKey(): ComponentID {
    return when (size) {
        0 -> bitIdGen(Int.MIN_VALUE, 0U, 2U)
        1 -> this[0].toKey()
        else -> {
            var it = 0
            for (clazz in this) {
                it = it.xor(clazz.name.hashCode())
            }
            bitIdGen(it, size.toUShort(), 2U)
        }
    }
}

fun customId(customId: Int): ComponentID = bitIdGen(customId, 0U, 3U)

internal fun bitIdGen(hash: Int, depth: UShort, type: UShort): ULong = hash.toULong().and(4294967295UL)
    .or(depth.toULong().shl(32))
    .or(type.toULong().shl(48))