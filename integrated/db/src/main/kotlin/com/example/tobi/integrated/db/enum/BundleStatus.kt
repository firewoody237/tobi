package com.example.tobi.integrated.db.enum

enum class BundleStatus(val description: String) {
    WAITING("대기중"),
    PAID("결제완료"),
    CANCELED("취소완료"),
    ABORT("거래오류"),
}