package com.example.db // 👈 패키지 이름 확인

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log // Log import 추가

// 통계 데이터를 담을 데이터 클래스
data class GarbageStat(val name: String, val count: Int)

/**
 * 쓰레기 데이터 저장을 위한 SQLite 데이터베이스를 관리하는 클래스
 */
class GarbageDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "GarbageDBHelper" // 로그 태그
        private const val DATABASE_VERSION = 1 // 👈 DB 버전 (테이블 구조 변경 시 1씩 올림)
        private const val DATABASE_NAME = "GarbageDB.db"
        private const val TABLE_GARBAGE = "garbage_entries"
        private const val KEY_ID = "id"
        private const val KEY_GARBAGE_TYPE = "garbage_type"
    }

    /**
     * DB 파일이 처음 생성될 때 호출되어 테이블을 만듭니다.
     */
    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "onCreate: 테이블($TABLE_GARBAGE) 생성...")
        val createTableQuery = """
            CREATE TABLE $TABLE_GARBAGE (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_GARBAGE_TYPE TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    /**
     * DATABASE_VERSION이 변경될 때 호출됩니다. (예: 1 -> 2)
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.w(TAG, "onUpgrade: DB 업그레이드. oldVersion=$oldVersion, newVersion=$newVersion")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GARBAGE") // 기존 테이블 삭제
        onCreate(db) // 새 테이블 생성
    }

    /**
     * 새로운 쓰레기 데이터를 DB에 추가합니다.
     * @param garbageType YOLO가 분석한 객체 이름 (예: "Soju")
     */
    fun addEntry(garbageType: String) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put(KEY_GARBAGE_TYPE, garbageType)
            }
            // insert 결과를 로그로 확인
            val rowId = db.insert(TABLE_GARBAGE, null, values)
            if (rowId == -1L) {
                Log.e(TAG, "addEntry: '$garbageType' 삽입 실패!")
            } else {
                Log.d(TAG, "addEntry: '$garbageType' 삽입 성공! (Row ID: $rowId)")
            }
        }
    }

    /**
     * DB에서 모든 쓰레기 종류별 개수를 세어 순위대로 정렬된 목록을 반환합니다.
     * (StatisticsActivity - 막대그래프용)
     * @return List<GarbageStat> (예: [GarbageStat("Soju", 10), GarbageStat("Paper", 5)])
     */
    fun getStatistics(): List<GarbageStat> {
        Log.d(TAG, "getStatistics: 통계 조회 시작...")
        val statsList = mutableListOf<GarbageStat>()
        val selectQuery = """
            SELECT $KEY_GARBAGE_TYPE, COUNT($KEY_GARBAGE_TYPE) as count
            FROM $TABLE_GARBAGE
            GROUP BY $KEY_GARBAGE_TYPE
            ORDER BY count DESC
        """.trimIndent()

        readableDatabase.use { db ->
            db.rawQuery(selectQuery, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GARBAGE_TYPE))
                        val count = cursor.getInt(cursor.getColumnIndexOrThrow("count"))
                        statsList.add(GarbageStat(name, count))
                    } while (cursor.moveToNext())
                }
            }
        }
        Log.d(TAG, "getStatistics: 조회된 통계 종류 건수: ${statsList.size}")
        return statsList
    }

    /**
     * DB에 저장된 모든 쓰레기 기록을 삭제합니다.
     * (StatisticsActivity - 초기화 버튼용)
     */
    fun clearAllEntries() {
        Log.d(TAG, "clearAllEntries: 모든 데이터 삭제 중...")
        writableDatabase.use { db ->
            val rowsAffected = db.delete(TABLE_GARBAGE, null, null)
            Log.d(TAG, "clearAllEntries: $rowsAffected 개 행 삭제 완료.")
        }
    }

    /**
     * DB에서 특정 항목(예: "Soju")의 총 개수를 반환합니다.
     * (ProfitReportActivity - 성과 리포트용)
     * @param garbageType 찾고자 하는 쓰레기 이름
     * @return 해당 쓰레기의 총 개수 (Int)
     */
    fun getSpecificItemCount(garbageType: String): Int {
        Log.d(TAG, "getSpecificItemCount: '$garbageType' 개수 조회 시작...")
        val selectQuery = """
            SELECT COUNT(*)
            FROM $TABLE_GARBAGE
            WHERE $KEY_GARBAGE_TYPE = ?
        """.trimIndent()

        var count = 0
        readableDatabase.use { db ->
            // rawQuery의 두 번째 인자로 selectionArgs(찾을 값)를 전달합니다.
            db.rawQuery(selectQuery, arrayOf(garbageType)).use { cursor ->
                // COUNT(*) 쿼리는 항상 0번 인덱스에 결과(개수)를 반환합니다.
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(0)
                }
            }
        }
        Log.d(TAG, "getSpecificItemCount: '$garbageType' 개수: $count")
        return count
    }
}

