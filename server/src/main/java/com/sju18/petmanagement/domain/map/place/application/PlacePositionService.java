package com.sju18.petmanagement.domain.map.place.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PlacePositionService {
    final double RADIUS_OF_EARTH_BY_METER = 6378000;
    final double ONE_RADIAN = 180 / Math.PI;

    protected Double calcMinLatForRange(Double originalLat, Double rangeByMeter) {
        return originalLat - (rangeByMeter / RADIUS_OF_EARTH_BY_METER) *(ONE_RADIAN);
    }
    protected Double calcMaxLatForRange(Double originalLat, Double rangeByMeter) {
        return originalLat + (rangeByMeter / RADIUS_OF_EARTH_BY_METER) * (ONE_RADIAN);
    }
    protected Double calcMinLongForRange(Double originalLat, Double originalLong, Double rangeByMeter) {
        return originalLong + (rangeByMeter / RADIUS_OF_EARTH_BY_METER) * (ONE_RADIAN) / Math.cos(originalLat * (ONE_RADIAN));
    }
    protected Double calcMaxLongForRange(Double originalLat, Double originalLong, Double rangeByMeter) {
        return originalLong - (rangeByMeter / RADIUS_OF_EARTH_BY_METER) * (ONE_RADIAN) / Math.cos(originalLat * (ONE_RADIAN));
    }
}
