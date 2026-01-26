//package com.example.googlevision
//
//// Google 공식 서류(EntityAnnotation) import는 이제 필요 없습니다.
//
///**
// * VisionApiAnalyzer가 분석 결과를 Activity에 전달할 때 사용하는
// * 새로운 약속(인터페이스)입니다.
// */
//interface VisionAnalysisListener {
//    /**
//     * 분석이 성공했을 때 '이름과 점수' 쌍의 목록을 전달합니다.
//     */
//    fun onVisionAnalyzed(labels: List<Pair<String, Float>>)
//
//    /**
//     * 분석 중 오류가 발생했을 때 호출됩니다.
//     */
//    fun onVisionError(exception: Exception)
//}