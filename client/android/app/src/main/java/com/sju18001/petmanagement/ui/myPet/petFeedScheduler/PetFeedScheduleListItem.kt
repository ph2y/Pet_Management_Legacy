package com.sju18001.petmanagement.ui.myPet.petFeedScheduler

import java.time.LocalTime

class PetFeedScheduleListItem(_id: Long, _feedTime: LocalTime, _petIdList: ArrayList<String>, _memo: String?, _isTurnedOn: Boolean) {
    var id: Long = _id
    var feedTime: LocalTime = _feedTime
    var petIdList: ArrayList<String> = _petIdList
    var memo: String? = _memo
    var isTurnedOn: Boolean = _isTurnedOn
}