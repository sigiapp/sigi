package com.example.sigi

data class Plant(
    val id: String = "",
    val name: String = "",
    val englishName: String = "",
    val imageUrl: String = ""
) {
    // 매개변수가 없는 기본 생성자
    constructor() : this("", "", "")
}
