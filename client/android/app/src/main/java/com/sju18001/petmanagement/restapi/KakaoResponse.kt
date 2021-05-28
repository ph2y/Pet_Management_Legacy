package com.sju18001.petmanagement.restapi

data class Documents(
    var documents: List<Place>
)

data class Place(
    var id: String,
    var place_name: String,
    var category_name: String,
    var category_group_code: String,
    var category_group_name: String,
    var phone: String,
    var address_name: String,
    var road_address_name: String,
    var x: String,
    var y: String,
    var place_url: String,
    var distance: String
)

data class Meta(
    var total_count:Int,
    var pageable_count:Int,
    var is_end:Boolean,
    var same_name:RegionInfo
)

data class RegionInfo(
    var region: List<String>,
    var keyword: String,
    var selected_region: String
)