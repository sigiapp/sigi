package com.example.sigi

data class Plant(
    var name: String = "",
    var englishName: String = "",
    var imageUrl: String = ""
) {
    // 매개변수가 없는 기본 생성자
    constructor() : this("", "", "")
}
