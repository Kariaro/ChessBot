#pragma once

#ifndef __UTILS_H__
#define __UTILS_H__

#include "utils_type.h"
#include <intrin.h>

/*
#ifdef _MSC_VER
#include <intrin.h>

static uint __inline _builtin_ctz(int64 i) {
    i = ~i & (i - 1);
    if (i <= 0) return i & 32;
    int n = 1;
    if (i > 1 << 16) { n += 16; i >>= 16; }
    if (i > 1 <<  8) { n +=  8; i >>=  8; }
    if (i > 1 <<  4) { n +=  4; i >>=  4; }
    if (i > 1 <<  2) { n +=  2; i >>=  2; }
    return n + (i >> 1);
}

#endif
*/

namespace Utils {
    inline uint64 lowestOneBit(uint64 i) {
        return (uint64)((int64)(i) & -(int64)(i));
    }

    inline uint numberOfTrailingZeros(uint64 i) {
        unsigned long r;
        _BitScanForward64(&r, i);
        return r;
    }
}

#endif // !__UTILS_H__

