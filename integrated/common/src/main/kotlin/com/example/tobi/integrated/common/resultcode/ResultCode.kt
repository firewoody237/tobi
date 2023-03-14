package com.example.tobi.integrated.common.resultcode

enum class ResultCode(val code: Int, val msg: String) {

    //1000 : 성공
    SUCCESS(1000, "성공"),

    //1100 : 파라미터체크 및 api 존재 여부
    ERROR_NOT_EXISTS_URI(1100, "존재하지 않는 API입니다"),
    ERROR_NOT_SUPPORTED_HTTP_METHOD(1101, "제공되지 않는 Http Method 입니다"),
    ERROR_PARAMETER_NOT_EXISTS(1102, "유효하지 않은 파라미터 입니다."),
    ERROR_PARAMETER_TYPE(1103, "유효하지 않는 파라미터 타입 입니다."),
    ERROR_PARAMETER_JSON_PARSING(1104, "요청 json 파싱 오류 입니다."),
    ERROR_HTTP_BODY(1105, "읽을 수 없는 http body 형식입니다."),
    ERROR_NOT_MEDIA_TYPE(1106, "적용 불가능한 Content-Type 입니다."),
    ERROR_ACCESS_DENIED(1107, "해당 API에 대한 권한이 없습니다."),
    ERROR_NOTHING_TO_MODIFY(1108, "변경사항이 존재하지 않습니다."),

    //2000 : 유저관련
    ERROR_USER_NOT_EXISTS(2000, "존재하지 않는 유저입니다."),
    ERROR_USER_ALREADY_EXISTS(2001, "이미 존재하는 ID 입니다."),
    ERROR_USER_NOT_ADMIN(2002, "Admin 사용자가 아닙니다."),
    ERROR_USER_CONNECTION(2003, "사용자 호출 api 통신 오류입니다."),
    ERROR_USER_RESPONSE(2004, "사용자 호출 응답 오류 입니다."),

    //2100 : 포인트 관련
    ERROR_POINT_MINUS(2101, "포인트가 음수입니다."),


    //3000 : 게시글관련
    ERROR_POST_NOT_EXIST(3000, "존재하지 않는 게시글입니다."),
    ERROR_POST_TITLE_WITH_USER_ALREADY_EXISTS(3001, "이미 같은 제목으로된 게시글이 존재합니다."),
    ERROR_REQUESTER_NOT_POST_AUTHOR(3002, "게시글의 저자가 아닙니다."),
    ERROR_POST_ALREADY_EXISTS(3003, "이미 존재하는 게시글 입니다."),

    //8000 : PGCode
    ERROR_PGCODE_NOT_EXISTS(8001, "존재하지 않는 PGCode입니다."),
    ERROR_PGCODE_ALREADY_EXISTS(8002, "이미 같은 PGCode가 존재합니다."),

    //8100 : PG
    ERROR_PG_NOT_EXISTS(8101, "존재하지 않는 PG입니다."),
    ERROR_PG_ALREADY_EXISTS(8102, "이미 같은 PG가 존재합니다."),

    //8200 : Payment
    ERROR_PAYMENT_NOT_EXISTS(8201, "존재하지 않는 Payment입니다."),
    ERROR_PAYMENT_ALREADY_EXISTS(8202, "이미 같은 Payment가 존재합니다."),

    //8300 : Package
    ERROR_PACKAGE_NOT_EXISTS(8301, "존재하지 않는 Package입니다."),
    ERROR_PACKAGE_ALREADY_EXISTS(8302, "이미 같은 Package가 존재합니다."),

    //8400 : Bundle
    ERROR_BUNDLE_NOT_EXISTS(8401, "존재하지 않는 Bundle입니다."),
    ERROR_BUNDLE_ALREADY_EXISTS(8402, "이미 같은 Bundle가 존재합니다."),

    //9000 : 확인이 힘든 오류
    ERROR_DB(9002, "DB 변경 중 오류"),
    ERROR_CIPHER(9003, "암호화 모듈 오류"),
    ERROR_ETC(9999, "기타 에러");
}