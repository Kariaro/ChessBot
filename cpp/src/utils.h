#pragma once

#ifndef UTILS_H
#define UTILS_H

#include "utils_type.h"
#include <intrin.h>

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

#endif // !UTILS_H

