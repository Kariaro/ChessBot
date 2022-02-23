#pragma once

#ifndef UTILS_H
#define UTILS_H

#include "utils_type.h"
#include <intrin.h>

namespace Utils {
    inline uint64_t lowestOneBit(uint64_t i) {
        return (uint64_t)((int64_t)(i) & -(int64_t)(i));
    }

    inline uint32_t numberOfTrailingZeros(uint64_t i) {
        unsigned long r;
        _BitScanForward64(&r, i);
        return r;
    }
}

#endif // !UTILS_H

