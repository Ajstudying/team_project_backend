package com.example.commerce.admin

import com.example.commerce.books.Books
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RestController
@RequestMapping("/api/book-commerce/admin-service")
class AdminController(private val adminService: AdminService) {

    //오늘의 북 데이터 받아서
    @PostMapping
    fun todayDataToMyServer() {
        println("오늘의 북데이터 가져오기")
        adminService.fetchTodayData()
    }

    @PostMapping("/stock")
    fun stockStatusDataToMyServer() {
        println("책 재고량 가져오기")
        adminService.fetchStockStatusData()
    }

    //해당 날짜의 오늘의 북 데이터 가져오기
    @GetMapping
    fun fetchTodayLetter() : ResponseEntity<TodayLetterResponse> {
        val currentDateTime = LocalDateTime.now()
        // 출력 형식을 정의하기 위한 DateTimeFormatter 사용 (선택 사항)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDateTime = currentDateTime.format(formatter)

        val todayData = transaction {
            //                        .join(Books, JoinType.INNER,
//                        onColumn = Books.itemId, otherColumn = TodayBook.itemId)
            val todayData =
                TodayBook.select { TodayBook.createdDate eq "2023-10-31" }
                    .mapNotNull { r ->
                        TodayLetterResponse(
                            r[TodayBook.id].value, r[TodayBook.cover],
                            r[TodayBook.title], r[TodayBook.author],
                            r[TodayBook.priceSales], r[TodayBook.todayLetter],
                            r[TodayBook.itemId], r[TodayBook.createdDate]
                        )
                    }
            return@transaction todayData.firstOrNull()
        }
        if (todayData != null) {
            return ResponseEntity.status(HttpStatus.OK).body(todayData)
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    }
    @GetMapping("/upload-files")
    fun imageFilesToMyServer(){
        println("메인베너 이미지 배열 db에 넣고 가져오기")
        adminService.uploadImageFileData()
    }

}