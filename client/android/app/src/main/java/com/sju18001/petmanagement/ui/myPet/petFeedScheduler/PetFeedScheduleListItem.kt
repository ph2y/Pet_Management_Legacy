package com.sju18001.petmanagement.ui.myPet.petFeedScheduler

import java.time.LocalTime

class PetFeedScheduleListItem(_feedTime: LocalTime, _petList: ArrayList<Int>, _memo: String, _isTurnedOn: Boolean) {
    var feedTime: LocalTime = _feedTime
    var petList: ArrayList<Int> = _petList
    var memo: String = _memo
    var isTurnedOn: Boolean = _isTurnedOn
}